/* Matlab Engine Interface

 Copyright (c) 1998-2002 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.matlab;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.Token;
import ptolemy.data.ScalarToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;
import ptolemy.data.ArrayToken;
import ptolemy.math.Complex;
import ptolemy.math.ComplexMatrixMath;

//////////////////////////////////////////////////////////////////////////
//// Engine

// NOTE: PLEASE DO NOT remove the leading '*'s from this javadoc...
// javadoc screws up the formatting of the tables for some reason
// if the lines are all left aligned without '*'s. (Next time we'll
// have to switch to html tables... :-)
/**
 * Provides a java API to the matlab environment. It uses an
 * intermediary C++ language layer (ptmatlab) that converts between
 * the java environment using the Java Native Interface and the matlab
 * environment using the matlab engine API and associated
 * mx-functions.<p>
 *
 * The intermediary layer is built as a DLL on Windows systems
 * (ptmatlab.dll).  This shared library is placed into the $PTII/bin
 * directory (that should be in the user's path) when this package is
 * built. Ptmatlab depends on matlab's engine API shared libraries
 * (libeng and libmx) that should also be installed in the user's path
 * (usually the case when matlab is installed and matlab's bin
 * directory is added to the path).<p>
 *
 * The bulk of the work done by this class is the conversion between
 * PtolemyII Tokens and matlab variables ("mxArrays").<p>
 *
 * {@link #get(String name)} converts a matlab engine mxArray (ma)
 * variable to a Ptolemy II Token. Recursion is used if ma is a struct
 * or cell.  The type of the Token returned is determined according to
 * the following table:
 * <pre>
 *     Matlab Type              PtolemyII Token
 *     ------------------------------------------------------------------
 *     'double'                 Double, if mxArray dimension is 1x1,
 *                              DoubleMatrix otherwise.
 *                              Complex, if mxArray is mxCOMPLEX and 1x1,
 *                              ComplexMatrix otherwise.
 *     'struct'                 RecordToken, if mxArray dimension 1x1,
 *                              ArrayToken of ArrayTokens of RecordTokens
 *                              {{RecordToken, ...}, {...}}  otherwise.
 *     'cell'                   ArrayToken of whatever Tokens the cell
 *                              elements resolve to through recursion
 *                              of _convertMxArrayToToken(). Note that
 *                              PtolemyII is more restrictive here in that
 *                              it requires all array elements to be of
 *                              the same type (not all matlab cell variables
 *                              may be converted to PtolemyII ArrayTokens).
 *     'char'                   StringToken, if the mxArray is 1xn,
 *                              ArrayToken of StringTokens otherwise.
 *     ------------------------------------------------------------------
 * </pre>
 * <p>
 *
 * {@link #put(String name, Token t)} converts a PtolemyII Token to a
 * matlab engine mxArray. Recursion is used if t is a RecordToken or
 * ArrayToken.  The type of mxArray created is determined according to
 * the following table:
 * <pre>
 *     PtolemyII Token          Matlab type
 *     ------------------------------------------------------------------
 *     ArrayToken               'cell', 1xn, elements are determined by
 *                              recursing this method on ArrayToken
 *                              elements.
 *     RecordToken              'struct', 1x1, fields are determined by
 *                              recursing this method on RecordToken
 *                              fields
 *     StringToken              'char', 1xn
 *     ComplexMatrixToken       'double', mxCOMPLEX, nxm
 *     MatrixToken              'double', mxREAL, nxm
 *     ComplexToken             'double', mxCOMPLEX, 1x1
 *     ScalarToken              'double', mxREAL, 1x1
 *     ------------------------------------------------------------------
 * </pre>
 * <p>
 * Debug statements to stdout are enabled by calling {@link
 * #setDebugging} with a byte parameter > 0. 1 enables basic tracing,
 * 2 includes traces from the dll as well.<p>
 *
 * {@link #evalString(String)} send a string to the matlab engine for
 * evaluation.<p>
 *
 * {@link #open} and {@link #close} are used to open / close the
 * connection to the matlab engine.<p>
 *
 * All callers share the same matlab engine and its workspace. Engine's methods
 * synchronize on the static {@link #semaphore} to prevent
 * overlapping calls to the same method from different threads. Use
 * Engine.{@link #semaphore} to synchronize across multiple method calls
 * if needed.<p>
 *
 * @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited.
 * @version $Id$
 */
public class Engine {
    /** Load the "ptmatlab" native interface. */
    static {
        System.loadLibrary("ptmatlab");
    }

    /** Matlab engine stdout buffer. One per matlab engine / all instances of
     *  this class.
     */
    static long engOutputBuffer = 0;
    static int engOutputBufferSize = 2048;

    /** Matlab engine handle - c++ native (Engine*) converted to java long. */
    static long eng = 0;

    /** Counts the number of (this) instances using eng. */
    static int engUserCount = 0;

    /** Used for Synchronization */
    // semaphore is public so that javadoc works.
    public static Integer semaphore = new Integer(0);

    /** Construct an instance of the matlab engine interface.
     * The matlab engine is not activated at this time.
     * <p>
     * Ptmatlab.dll is loaded by the system library loader the
     * first time this class is loaded.
     * @see #open().
     */
    public Engine() {
        debug = 0;
    }

    /** Enable/disable debug statements to stdout.
     * @param d Non-zero to enable debug statements, zero to disable.
     */
    public void setDebugging(byte d) {
        debug = d;
    }

    /** Open a connection to the default matlab engine installed on
     * this host.
     * @see #open(String) below.
     */
    public void open() throws IllegalActionException {
        open(null);              // Use default invocation, no
        // output buffering
    }

    /** Open a connection to a matlab engine.  Currently all matlab
     * Engine interface (this class) instances use the same matlab
     * engine instance.  A handle to this engine is saved in the "eng"
     * static member of this class, and a usage count ("engUserCount")
     * is maintained.
     * @param startCmd hostname or command to use to start the engine.
     * @exception IllegalActionException If the matlab engine open is
     * unsuccessful.  This will typically occur if ptmatlab (.dll)
     * cannot be located or if the matlab bin directory is not in the
     * path.
     * <p>
     * For more information, see matlab engine API reference engOpen()
     */
    public void open(String startCmd) throws IllegalActionException {
        synchronized(semaphore) {
            if (eng == 0) {
                long ne = ptmatlabEngOpen(startCmd);
                if (ne != 0) {
                    eng = ne;
                    engOutputBuffer =
                        ptmatlabEngOutputBuffer(eng, engOutputBufferSize);
                    engUserCount = 1;
                    if (debug > 0) {
                        System.out.println("matlabEngine.open(" + startCmd
                                + ") = " + eng + ", engUserCount = "
                                + engUserCount);
                    }
                }
            } else {
                engUserCount++;
                if (debug > 0) {
                    System.out.println("matlabEngine.open(" + startCmd
                            + ") : reusing eng = " + eng
                            + ", engUserCount = "+engUserCount);
                }
            }
            if (eng == 0) {
                throw new IllegalActionException("matlabEngine.open("
                        + startCmd
                        + ") : can't find matlab"
                        + "engine.");
            }
        }
    }

    /** Close a connection to a matlab engine.
     * This will also close the matlab engine if this instance was the last
     * user of the matlab engine.
     * <p>
     * For more information, see matlab engine API reference engClose()
     */
    public int close() {
        int retval = 0;
        synchronized(semaphore) {
            if (eng != 0 && engUserCount > 0) {
                engUserCount--;
                if (debug > 0) {
                    System.out.println("matlabEngine.close() : engUserCount = "
                            + engUserCount);
                }
                if (engUserCount <= 0) {
                    retval = ptmatlabEngClose(eng, engOutputBuffer);
                    eng = 0;
                }
            }
        }
        return retval;
    }

    /** Copy of a common error message. */
    static String errNotOpened = "matlab engine not opened.";

    /** Send a string for evaluation to the matlab engine.
     * @param evalStr string to evaluate.
     * @exception IllegalActionException If the matlab engine is not opened.
     */
    public int evalString(String evalStr) throws IllegalActionException {
        int retval;
        synchronized(semaphore) {
            if (eng == 0) {
                throw new IllegalActionException("matlabEngine.evalStr(): "
                        + errNotOpened);
            }
            if (debug > 0) {
                System.out.println("matlabEngine.evalString(\""
                        + evalStr + "\")");
            }
            retval = ptmatlabEngEvalString(eng, evalStr);
        }
	return retval;
    }

    /** Return a Token from the matlab engine.
     * @param name Matlab variable name used to initialize the returned Token
     * @return PtolemyII Token.
     * @exception IllegalActionException If the matlab engine is not opened, or
     * if the matlab variable was not found in the engine. In this case, the
     * matlab engine's stdout is included in the exception message.
     * @see Engine
     */
    public Token get(String name) throws IllegalActionException {
        Token retval = null;
        synchronized(semaphore) {
            if (eng == 0) {
                throw new IllegalActionException("matlabEngine.get(): "
                        + errNotOpened);
            }
            long ma = ptmatlabEngGetArray(eng, name);
            if (ma == 0) {
                throw new IllegalActionException("matlabEngine.get(" + name
                        + "): can't find matlab "
                        + "variable \""
                        + name + "\"\n"
                        + getOutput().stringValue());
            }
            retval = _convertMxArrayToToken(ma);
            ptmatlabDestroy(ma, name);
            if (debug > 0) {
                System.out.println("matlabEngine.get(" + name + ") = "
                        + retval.toString());
            }
        }
        return retval;
    }

    /** Get last matlab stdout
     * @return PtolemyII StringToken
     */
    public StringToken getOutput() {
        String str = "";
        synchronized(semaphore) {
            str = ptmatlabGetOutput(engOutputBuffer, engOutputBufferSize);
        }
        return new StringToken(str);
    }

    /** Create a matlab variable using name and a Token.
     * @param name matlab variable name.
     * @param t Token to provide value.
     * @see Engine
     */
    public int put(String name, Token t) throws IllegalActionException {
        int retval;
        synchronized(semaphore) {
            if (eng == 0) {
                throw new IllegalActionException("matlabEngine.put(): "
                        + errNotOpened);
            }
            if (debug > 0) {
                System.out.println("matlabEngine.put(" + name + ", "
                        + t.toString()+")");
            }
            long ma = _createMxArray(name, t);
            retval = ptmatlabEngPutArray(eng, name, ma);
            ptmatlabDestroy(ma, name);
        }
        return retval;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                 ////

    // Engine functions - native methods implemented in ptmatlab.cc.
    private native long ptmatlabEngOpen(String startCmd);
    private native int ptmatlabEngClose(long e, long outputBuffer);
    private native int ptmatlabEngEvalString(long e, String s);
    private native long ptmatlabEngGetArray(long e, String name);
    private native int ptmatlabEngPutArray(long e, String name, long mxArray);
    private native long ptmatlabEngOutputBuffer(long e, int n);

    // C-Mx style functions
    private native long ptmatlabCreateCellMatrix(String name, int n, int m);
    private native long
    ptmatlabCreateString(String name, String s, int n, int m);
    private native long
    ptmatlabCreateDoubleMatrixOneDim(String name, double[]a, int length);
    private native long
    ptmatlabCreateDoubleMatrix(String name, double[][]a, int n, int m);
    private native long
    ptmatlabCreateComplexMatrixOneDim(String name, Complex[]a, int length);
    private native long
    ptmatlabCreateComplexMatrix(String name, Complex[][]a, int n, int m);
    private native long
    ptmatlabCreateStructMatrix(String name, Object[] fieldNames,
            int n, int m);
    private native void
    ptmatlabDestroy(long mxArray, String name);
    private native long
    ptmatlabGetCell(long mxArray, int n, int m);
    private native String
    ptmatlabGetClassName(long mxArray);
    private native int[]
    ptmatlabGetDimensions(long mxArray);
    private native Complex[][]
    ptmatlabGetComplexMatrix(long mxArray, int n, int m);
    private native double[][]
    ptmatlabGetDoubleMatrix(long mxArray, int n, int m);
    private native String ptmatlabGetFieldNameByNumber(long mxArray, int k);
    private native long
    ptmatlabGetFieldByNumber(long mxArray, int k, int n, int m);
    private native int ptmatlabGetNumberOfFields(long mxArray);
    private native String ptmatlabGetString(long mxArray, int n);
    private native String ptmatlabGetOutput(long outputBuffer, int n);
    private native boolean ptmatlabIsComplex(long mxArray);
    private native void
    ptmatlabSetCell(String name, long mxArray,
            int n, int m, long valueMxArray);
    private native void
    ptmatlabSetString(String name, long mxArray,
            int n, String s, int slen);
    private native void
    ptmatlabSetStructField(String name, long mxArray, String fieldName,
            int n, int m, long valueMxArray);


    // Converts a matlab engine mxArray (ma) variable to a Ptolemy II Token.
    // @param ma Pointer to the matlab engine variable's mxArray
    // structure as a java long.
    // @return Ptolemy II Token of type that corresponds to ma's type.
    // @exception IllegalActionException If ma cannot be obtained from
    // the matlab engine, or if the mxArray type is not one of
    // 'double', 'struct', 'char' or 'cell', or if not all elements of
    // an ArrayToken to be created are of the same type.
    // @see Engine

    private Token _convertMxArrayToToken(long ma)
            throws IllegalActionException {
        String maClassStr = ptmatlabGetClassName(ma);
        int[] dims = ptmatlabGetDimensions(ma);
        int nRows = dims[0];
        int nCols = dims[1];
        boolean scalar = nCols == 1 && nRows == 1;
        Token retval = null;
        if (maClassStr.equals("double")) {
            if (ptmatlabIsComplex(ma)) {
                Complex[][] a = ptmatlabGetComplexMatrix(ma, nRows, nCols);
                if (a == null) {
		    throw new IllegalActionException("can't get complex "
                            + "matrix from matlab "
                            + "engine.");
		}
		if (scalar) {
		    retval = new ComplexToken(a[0][0]);
                } else {
		    retval = new ComplexMatrixToken(a);
		}
            } else {
                double[][] a = ptmatlabGetDoubleMatrix(ma, nRows, nCols);
                if (a == null) {
		    throw new IllegalActionException("can't get double "
                            + "matrix from matlab "
                            + "engine.");
		}
		if (scalar) {
                    double tmp = a[0][0];
                    if (tmp == Math.floor(tmp)
                            && Math.abs(tmp) <= Integer.MAX_VALUE)
                        retval = new IntToken((int)tmp);
                    else
                        retval = new DoubleToken(tmp);
                } else {
		    retval = new DoubleMatrixToken(a);
		}
            }
        } else if (maClassStr.equals("struct")) {
            int nfields = ptmatlabGetNumberOfFields(ma);
            Token[] ta = new Token[nCols];
            Token[] tr = new Token[nRows];
            String[] fieldNames = new String[nfields];
            for (int k = 0; k < nfields; k++) {
                fieldNames[k] = ptmatlabGetFieldNameByNumber(ma, k);
            }
            Token[] fieldValues = new Token[nfields];
            for (int n = 0; n < nRows; n++) {
                for (int m = 0; m < nCols; m++) {
                    for (int k = 0; k < nfields; k++) {
                        long fma = ptmatlabGetFieldByNumber(ma, k, n, m);
                        if (fma != 0) {
                            fieldValues[k] = _convertMxArrayToToken(fma);
                        } else {
                            throw new IllegalActionException("can't get field "
                                    + fieldNames[k]
                                    + "from matlab "
                                    + "struct "
                                    + nRows + "x"
                                    + nCols);
                        }
                    }
                    ta[m] = new RecordToken(fieldNames, fieldValues);
                }
                tr[n] = new ArrayToken(ta);
            }
            if (scalar) {
                retval = ((ArrayToken)tr[0]).getElement(0);
            } else {
                retval = new ArrayToken(tr);
            }
        } else if (maClassStr.equals("cell")) {
            Token[] ta = new Token[nCols];
            Token[] tr = new Token[nRows];
            for (int n = 0; n < nRows; n++) {
                for (int m = 0; m < nCols; m++) {
                    long cma = ptmatlabGetCell(ma, n, m);
                    if (cma != 0) {
                        ta[m] = _convertMxArrayToToken(cma);
                    } // else - throw exception?
                }
                tr[n] = new ArrayToken(ta);
                // If not all tokens are of the same, this will throw
		// an exception.
            }
            if (scalar) {
                retval = ((ArrayToken)tr[0]).getElement(0);
            } else if (nRows == 1) {
                retval = tr[0];
            } else {
                retval = new ArrayToken(tr);
            }
        } else if (maClassStr.equals("char")) {
            if (nRows == 1)
                retval = new StringToken(ptmatlabGetString(ma, 0));
            else {
                Token[] ta = new Token[nRows];
                for (int n = 0; n < nRows; n++) {
                    ta[n] = new StringToken(ptmatlabGetString(ma, n));
                }
                retval = new ArrayToken(ta);
            }
        } else {
            throw new IllegalActionException("no support for mxArray class "
                    + maClassStr + " " + dims[0]
                    + " x " + dims[1]);
        }
        return retval;
    }

    // Creates (recursively) a matlab engine mxArray given a Ptolemy II Token.
    // @param name Matlab variable name to be created.
    // @param t PtolemyII Token providing the value for the variable.
    // @return Matlab engine mxArray pointer cast to java long.
    // @exception IllegalActionException If array creation failed, or if the
    // Token was not one of the types supported by _createMxArray().
    // @see Engine

    private long _createMxArray(String name, Token t)
            throws IllegalActionException {
        long ma = 0;
        if (t instanceof ArrayToken) {
            Token[] ta = ((ArrayToken)t).arrayValue();
            if (!(ta[0] instanceof StringToken)) {
                ma = ptmatlabCreateCellMatrix(name, 1, ta.length);
                if (ma == 0) {
		    throw new IllegalActionException("couldn't create cell "
                            + "array "+name);
		}
                for (int n = 0; n < ta.length; n++) {
                    long fma = _createMxArray("("+n+")", ta[n]);
                    if (fma == 0) {
			throw new IllegalActionException("couldn't create "
                                + "array for index "
                                + n
                                + " in cell array "
                                + name);
		    }
                    ptmatlabSetCell(name, ma, 0, n, fma);
                }
            } else {
                String s = ((StringToken)ta[0]).stringValue();
                ma = ptmatlabCreateString(name, s, ta.length, s.length());
                for (int n = 1; n < ta.length; n++) {
                    s = ((StringToken)ta[n]).stringValue();
                    ptmatlabSetString(name, ma, n, s, s.length());
                }
            }
        } else if (t instanceof RecordToken) {
            Object[] fieldNames = (((RecordToken)t).labelSet()).toArray();
            ma = ptmatlabCreateStructMatrix(name, fieldNames, 1, 1);
            if (ma == 0) {
		throw new IllegalActionException("couldn't create struct "
                        + "array " + name);
	    }
            for (int n = 0; n < fieldNames.length; n++) {
                Token f = ((RecordToken)t).get((String)fieldNames[n]);
                long fma = _createMxArray((String)fieldNames[n], f);
                if (fma == 0) {
		    throw new IllegalActionException("couldn't create array "
                            + "for field "
                            + fieldNames[n]
                            + " in struct " + name);
		}
                ptmatlabSetStructField(name, ma, (String)fieldNames[n],
                        0, 0, fma );
            }
        } else if (t instanceof StringToken) {
            String s = ((StringToken)t).stringValue();
            ma = ptmatlabCreateString(name, s, 1, s.length());
        } else if (t instanceof ComplexMatrixToken) {
            Complex[][] a = ((ComplexMatrixToken)t).complexMatrix();
            ma = ptmatlabCreateComplexMatrix(name, a, a.length, a[0].length);
        } else if (t instanceof MatrixToken) {
            double[][] a = ((MatrixToken)t).doubleMatrix();
            ma = ptmatlabCreateDoubleMatrix(name, a, a.length, a[0].length);
        } else if (t instanceof ComplexToken) {
            Complex[] a = {((ComplexToken)t).complexValue()};
            ma = ptmatlabCreateComplexMatrixOneDim(name, a, a.length);
        } else {
            double[] a = {((ScalarToken)t).doubleValue()};
            ma = ptmatlabCreateDoubleMatrixOneDim(name, a, a.length);
        }
        if (ma == 0) {
	    throw new IllegalActionException("couldn't create array for "
                    + name);
	}
        return ma;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Debug statements are sent to stdout if non-zero. If 1, only
    // this class sends debug statements, if 2 then ptmatlab.dll also
    // sends debug statements.
    private byte debug = 0;
}
