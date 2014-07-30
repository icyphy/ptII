/* Matlab Engine Interface

 Copyright (c) 1998-2014 The Regents of the University of California and
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

 */
package ptolemy.matlab;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;

///////////////////////////////////////////////////////////////////
//// Engine

/**
 Provides a java API to the matlab environment. It uses an intermediary
 C++ language layer (ptmatlab) that converts between the java environment
 using the Java Native Interface and the matlab environment using the
 matlab engine API and associated mx-functions.<p>

 The intermediary layer is built as a DLL on Windows systems
 (ptmatlab.dll).  This shared library is placed into the $PTII/bin
 directory (that should be in the user's path) when this package is
 built. Ptmatlab depends on matlab's engine API shared libraries (libeng
 and libmx) that should also be installed in the user's path (usually the
 case when matlab is installed and matlab's bin directory is added to the
 path).<p>

 The bulk of the work done by this class is the conversion between
 PtolemyII Tokens and matlab variables ("mxArrays").<p>

 {@link #get(long[] eng, String name)} and
 {@link ptolemy.matlab.Engine#get(long[], String,
 Engine.ConversionParameters)} convert a matlab engine mxArray
 (ma) variable to a Ptolemy II Token. Recursion is used if ma is a struct
 or cell. The type of the Token returned is determined according to
 the following table:

 <table border="1">
 <caption><em>Conversion from matlab to PtolemyII types (get())
 </em></caption>
 <tr><th>Matlab Type<th>PtolemyII Token
 <tr>
 <td>'double'
 <td>Double, if mxArray dimension is 1x1 and
 {@link Engine.ConversionParameters#getScalarMatrices} is true,
 DoubleMatrix otherwise.
 Complex, if mxArray is mxCOMPLEX, 1x1, and
 {@link Engine.ConversionParameters#getScalarMatrices} is true,
 ComplexMatrix otherwise.<br>
 <em>Note:</em>
 If {@link Engine.ConversionParameters#getIntMatrices} is true and
 all matrix double values can be cast to integers without loss of
 precision then an IntToken or IntTokenMatrix is returned.
 <tr>
 <td>'struct'
 <td>RecordToken, if mxArray dimension 1x1, ArrayToken of ArrayTokens
 of RecordTokens {{RecordToken,...}, {...}} ("two-dimensional" ArrayToken)
 otherwise.
 <tr>
 <td>'cell'
 <td>ArrayToken of whatever Tokens the cell elements resolve to through
 recursion of _convertMxArrayToToken(). In the special case of a cell
 array of doubles, an {int} is always returned if all cell double
 values can be losslessly converted to integers.
 Note that PtolemyII is more
 restrictive here in that it requires all array elements to be of the
 same type (not all matlab cell variables may be converted to PtolemyII
 ArrayTokens).
 <tr>
 <td>'char'
 <td>StringToken, if the mxArray is 1xn, ArrayToken of StringTokens
 otherwise.
 </table>
 <p>
 {@link #put(long[] eng, String name, Token t)} converts a PtolemyII
 Token to a matlab engine mxArray. Recursion is used if t is a
 RecordToken or ArrayToken. The type of mxArray created is determined
 according to the following table.

 <table border="1">
 <caption><em>Conversion from PtolemyII to matlab types (put())
 </em></caption>
 <tr><th>PtolemyII Token<th>Matlab type
 <tr>
 <td>ArrayToken
 <td>'cell', 1xn, elements are determined by recursing this method
 on ArrayToken elements.
 <tr>
 <td>RecordToken
 <td>'struct', 1x1, fields are determined by recursing this method on
 RecordToken fields
 <tr>
 <td>StringToken
 <td>'char', 1xn
 <tr>
 <td>ComplexMatrixToken
 <td>'double', mxCOMPLEX, nxm
 <tr>
 <td>MatrixToken
 <td>'double', mxREAL, nxm
 <tr>
 <td>ComplexToken
 <td>'double', mxCOMPLEX, 1x1
 <tr>
 <td>ScalarToken
 <td>'double', mxREAL, 1x1
 </table>
 <p>
 Debug statements to stdout are enabled by calling {@link
 #setDebugging} with a byte parameter > 0. 1 enables basic tracing,
 2 includes traces from the dll as well.

 <p>{@link #evalString(long[], String)} send a string to the matlab
 engine for evaluation.

 {@link #open} and {@link #close} are used to open / close the
 connection to the matlab engine.<p>

 All callers share the same matlab engine and its workspace.
 Methods of Engine synchronize on the static {@link #semaphore} to
 prevent overlapping calls to the same method from different threads.
 Use Engine. {@link #semaphore} to synchronize across multiple method
 calls if needed.<p>

 @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited.
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (zkemenczy)
 @Pt.AcceptedRating Red (cxh)
 */
public class Engine {
    /** Load the "ptmatlab" native interface. Use a classpath-relative
     * pathname without the shared library suffix (which is selected
     * and appended by {@link UtilityFunctions#loadLibrary}) for
     * portability. */
    static {
        UtilityFunctions.loadLibrary("ptolemy/matlab/ptmatlab");
    }

    /** Output buffer (allocated for each opened instance) size. */
    static int engOutputBufferSize = 2048;

    /** Used for synchronization. */
    public static final Object semaphore = new Object();

    // semaphore is public so that javadoc works.
    // ptolemy.matlab.Expression also uses this semaphore

    /** Data conversion parameters used by {@link
     * ptolemy.matlab.Engine#get(long[], String,
     * Engine.ConversionParameters)}. */
    public static class ConversionParameters {
        /** If true (default), 1x1 matrices are returned as
         * appropriate ScalarToken.*/
        public boolean getScalarMatrices = true;

        /** If true, double matrices where all elements represent
         * integers are returned as IntMatrixTokens (default false).*/
        public boolean getIntMatrices = false;
    }

    /** Construct an instance of the matlab engine interface.
     * The matlab engine is not activated at this time.
     * <p>
     * Ptmatlab.dll is loaded by the system library loader the
     * first time this class is loaded.
     * @see #open()
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
     * this host with its output buffered.
     * @return long[2] retval engine handle; retval[0] is the real
     * engine handle, retval[1] is a pointer to the engine output
     * buffer; both should be preserved and passed to subsequent engine
     * calls.
     * @exception IllegalActionException If the matlab engine open is
     * unsuccessful.  This will typically occur if ptmatlab (.dll)
     * cannot be located or if the matlab bin directory is not in the
     * path.
     * @see #open(String, boolean)
     */
    public long[] open() throws IllegalActionException {
        return open(null, true); // Use default invocation, with

        // output buffering
    }

    /** Open a connection to the default matlab engine installed on
     * this host with specified output buffering.
     * @param needOutput selects whether the output should be buffered
     * or not.
     * @return long[2] retval engine handle; retval[0] is the real
     * engine handle, retval[1] is a pointer to the engine output
     * buffer; both should be preserved and passed to subsequent engine
     * calls.
     * @exception IllegalActionException If the matlab engine open is
     * unsuccessful.  This will typically occur if ptmatlab (.dll)
     * cannot be located or if the matlab bin directory is not in the
     * path.
     * @see #open(String, boolean)
     */
    public long[] open(boolean needOutput) throws IllegalActionException {
        return open(null, needOutput); // Use default invocation

        // output buffering
    }

    /** Open a connection to a matlab engine.<p>
     * For more information, see the matlab engine API reference engOpen()
     * @param startCmd hostname or command to use to start the engine.
     * @param needOutput selects whether the output should be buffered
     * or not.
     * @return long[2] retval engine handle; retval[0] is the real
     * engine handle, retval[1] is a pointer to the engine output
     * buffer; both should be preserved and passed to subsequent engine
     * calls.
     * @exception IllegalActionException If the matlab engine open is
     * unsuccessful.  This will typically occur if ptmatlab (.dll)
     * cannot be located or if the matlab bin directory is not in the
     * path.
     * @see #getOutput(long[])
     */
    public long[] open(String startCmd, boolean needOutput)
            throws IllegalActionException {
        long[] retval = new long[2];

        synchronized (semaphore) {
            retval[0] = ptmatlabEngOpen(startCmd);

            if (retval[0] == 0) {
                String Path = "";
                try {
                    Path = System.getenv("PATH");
                } catch (Throwable throwable) {
                    Path = throwable.toString();
                }
                throw new IllegalActionException(
                        "matlabEngine.open("
                                + startCmd
                                + ") : can't find Matlab engine. "
                                + "The PATH for this process is \""
                                + Path
                                + "\". Try starting "
                                + "\"matlab\" by hand from a shell to verify that "
                                + "Matlab is set up properly and the license is "
                                + "correct.\n"
                                + "Under Windows, try running \"matlab /regserver\", "
                                + "the Matlab C API communicates with Matlab via COM, "
                                + "and apparently the COM interface is not "
                                + "automatically registered when Matlab is "
                                + "installed.\n"
                                + "Under Mac OS X, 'matlab' must be in the PATH, "
                                + "it may be easiest to create a link from /usr/bin/matlab "
                                + "to the location of the matlab script:\n "
                                + "sudo ln -s /Applications/MATLAB_R2011a.app/bin/matlab /usr/bin/matlab\n"
                                + "Under Linux and other types of UNIX, csh must be "
                                + "installed in /bin/csh.");
            }

            if (needOutput) {
                retval[1] = ptmatlabEngOutputBuffer(retval[0],
                        engOutputBufferSize);
            } // else retval[1] = 0;

            if (debug > 0) {
                System.out.println(retval[0] + " = matlabEngine.open(\""
                        + startCmd + "\")");
            }
        }

        return retval;
    }

    /** Close a connection to a matlab engine.
     * This will also close the matlab engine if this instance was the last
     * user of the matlab engine.
     * <p>
     * For more information, see matlab engine API reference engClose()
     * @param eng An array of longs with length 2. eng[0] is the real
     * engine handle, eng[1] is a pointer to the engine output
     * buffer.
     * @return The value returned by calling engClose() in the
     * Matlab interface.
     */
    public int close(long[] eng) {
        int retval = 0;

        if (eng == null) {
            return -1;
        }

        synchronized (semaphore) {
            if (debug > 0) {
                System.out.println("matlabEngine.close(" + eng[0] + ")");
            }

            retval = ptmatlabEngClose(eng[0], eng[1]);
        }

        return retval;
    }

    /** Copy of a common error message. */
    static String errNotOpened = "matlab engine not opened.";

    /** Send a string for evaluation to the matlab engine.
     * @param eng An array of two longs; eng[0] is the real
     * engine handle, eng[1] is a pointer to the engine output
     * buffer.
     * @param evalStr string to evaluate.
     * @return The value returned by the ptmatlabEngEvalString() native method.
     * @exception IllegalActionException If the matlab engine is not opened.
     */
    public int evalString(long[] eng, String evalStr)
            throws IllegalActionException {
        int retval;

        synchronized (semaphore) {
            if (eng == null || eng[0] == 0) {
                throw new IllegalActionException("matlabEngine.evalStr(): "
                        + errNotOpened);
            }

            if (debug > 0) {
                System.out.println("matlabEngine.evalString(\"" + evalStr
                        + "\")");
            }

            retval = ptmatlabEngEvalString(eng[0], evalStr);
        }

        return retval;
    }

    /** Return a Token from the matlab engine using default
     * {@link Engine.ConversionParameters} values.
     * @param eng An array of longs with length 2. eng[0] is the real
     * engine handle, eng[1] is a pointer to the engine output
     * buffer.
     * @param name Matlab variable name used to initialize the returned Token
     * @return PtolemyII Token.
     * @exception IllegalActionException If the matlab engine is not opened, or
     * if the matlab variable was not found in the engine. In this case, the
     * matlab engine's stdout is included in the exception message.
     * @see Expression
     */
    public Token get(long[] eng, String name) throws IllegalActionException {
        return get(eng, name, new ConversionParameters());
    }

    /** Return a Token from the matlab engine using specified
     * {@link Engine.ConversionParameters} values.
     * @param eng An array of longs with length 2. eng[0] is the real
     * engine handle, eng[1] is a pointer to the engine output
     * buffer.
     * @param name Matlab variable name used to initialize the returned Token
     * @param par The ConversionParameter to use.
     * @return PtolemyII Token.
     * @exception IllegalActionException If the matlab engine is not opened, or
     * if the matlab variable was not found in the engine. In this case, the
     * matlab engine's stdout is included in the exception message.
     * @see Expression
     */
    public Token get(long[] eng, String name, ConversionParameters par)
            throws IllegalActionException {
        Token retval = null;

        synchronized (semaphore) {
            if (eng == null || eng[0] == 0) {
                throw new IllegalActionException("matlabEngine.get(): "
                        + errNotOpened);
            }

            long ma = ptmatlabEngGetArray(eng[0], name);

            if (ma == 0) {
                throw new IllegalActionException("matlabEngine.get(" + name
                        + "): can't find matlab " + "variable \"" + name
                        + "\"\n" + getOutput(eng).stringValue());
            }

            retval = _convertMxArrayToToken(ma, par);
            ptmatlabDestroy(ma, name);

            if (debug > 0) {
                System.out.println("matlabEngine.get(" + name + ") = "
                        + retval.toString());
            }
        }

        return retval;
    }

    /** Get last matlab stdout.
     * @param eng An array of longs with length 2. eng[0] is the real
     * engine handle, eng[1] is a pointer to the engine output
     * buffer.
     * @return PtolemyII StringToken
     */
    public StringToken getOutput(long[] eng) {
        String str = "";

        synchronized (semaphore) {
            if (eng != null && eng[1] != 0) {
                str = ptmatlabGetOutput(eng[1], engOutputBufferSize);
            }
        }

        return new StringToken(str);
    }

    /** Create a matlab variable using name and a Token.
     * @param eng An array of longs with length 2. eng[0] is the real
     * engine handle, eng[1] is a pointer to the engine output
     * buffer.
     * @param name matlab variable name.
     * @param t Token to provide value.
     * @return The result of calling engPutArray() in the Matlab
     * C library.
     * @exception IllegalActionException If the engine is not opened.
     * @see Engine
     */
    public int put(long[] eng, String name, Token t)
            throws IllegalActionException {
        int retval;

        synchronized (semaphore) {
            if (eng == null || eng[0] == 0) {
                throw new IllegalActionException("matlabEngine.put(): "
                        + errNotOpened);
            }

            if (debug > 0) {
                System.out.println("matlabEngine.put(" + name + ", "
                        + t.toString() + ")");
            }

            long ma = _createMxArray(name, t);
            retval = ptmatlabEngPutArray(eng[0], name, ma);
            ptmatlabDestroy(ma, name);
        }

        return retval;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Engine functions - native methods implemented in ptmatlab.cc.
    private native long ptmatlabEngOpen(String startCmd);

    private native int ptmatlabEngClose(long e, long outputBuffer);

    private native int ptmatlabEngEvalString(long e, String s);

    private native long ptmatlabEngGetArray(long e, String name);

    private native int ptmatlabEngPutArray(long e, String name, long mxArray);

    private native long ptmatlabEngOutputBuffer(long e, int n);

    // C-Mx style functions
    private native long ptmatlabCreateCellMatrix(String name, int n, int m);

    private native long ptmatlabCreateString(String name, String s, int n, int m);

    private native long ptmatlabCreateDoubleMatrixOneDim(String name,
            double[] a, int length);

    private native long ptmatlabCreateDoubleMatrix(String name, double[][] a,
            int n, int m);

    private native long ptmatlabCreateComplexMatrixOneDim(String name,
            Complex[] a, int length);

    private native long ptmatlabCreateComplexMatrix(String name, Complex[][] a,
            int n, int m);

    private native long ptmatlabCreateStructMatrix(String name,
            Object[] fieldNames, int n, int m);

    private native void ptmatlabDestroy(long mxArray, String name);

    private native long ptmatlabGetCell(long mxArray, int n, int m);

    private native String ptmatlabGetClassName(long mxArray);

    private native int[] ptmatlabGetDimensions(long mxArray);

    private native Complex[][] ptmatlabGetComplexMatrix(long mxArray, int n,
            int m);

    private native double[][] ptmatlabGetDoubleMatrix(long mxArray, int n, int m);

    private native int[][] ptmatlabGetLogicalMatrix(long mxArray, int nRows,
            int nCols);

    private native String ptmatlabGetFieldNameByNumber(long mxArray, int k);

    private native long ptmatlabGetFieldByNumber(long mxArray, int k, int n,
            int m);

    private native int ptmatlabGetNumberOfFields(long mxArray);

    private native String ptmatlabGetString(long mxArray, int n);

    private native String ptmatlabGetOutput(long outputBuffer, int n);

    private native boolean ptmatlabIsComplex(long mxArray);

    private native void ptmatlabSetCell(String name, long mxArray, int n,
            int m, long valueMxArray);

    private native void ptmatlabSetString(String name, long mxArray, int n,
            String s, int slen);

    private native void ptmatlabSetStructField(String name, long mxArray,
            String fieldName, int n, int m, long valueMxArray);

    // Converts a matlab engine mxArray (ma) variable to a Ptolemy II Token.
    // @param ma Pointer to the matlab engine variable's mxArray
    // structure as a java long.
    // @return Ptolemy II Token of type that corresponds to ma's type.
    // @exception IllegalActionException If ma cannot be obtained from
    // the matlab engine, or if the mxArray type is not one of
    // 'double', 'struct', 'char' or 'cell', or if not all elements of
    // an ArrayToken to be created are of the same type.
    // @see Engine
    private Token _convertMxArrayToToken(long ma, ConversionParameters par)
            throws IllegalActionException {
        String maClassStr = ptmatlabGetClassName(ma);
        int[] dims = ptmatlabGetDimensions(ma);
        int nRows = dims[0];
        int nCols = dims[1];
        boolean scalarStructs = nCols == 1 && nRows == 1;
        boolean scalarMatrices = nCols == 1 && nRows == 1
                && par.getScalarMatrices;
        Token retval = null;

        if (maClassStr.equals("double")) {
            if (ptmatlabIsComplex(ma)) {
                Complex[][] a = ptmatlabGetComplexMatrix(ma, nRows, nCols);

                if (a == null) {
                    throw new IllegalActionException(
                            "can't get complex matrix from matlab engine.");
                }

                if (scalarMatrices) {
                    retval = new ComplexToken(a[0][0]);
                } else {
                    retval = new ComplexMatrixToken(a);
                }
            } else {
                double[][] a = ptmatlabGetDoubleMatrix(ma, nRows, nCols);

                if (a == null) {
                    throw new IllegalActionException(
                            "can't get double matrix from matlab engine.");
                }

                if (scalarMatrices) {
                    double tmp = a[0][0];

                    if (_doubleIsInteger(tmp)) {
                        retval = new IntToken((int) tmp);
                    } else {
                        retval = new DoubleToken(tmp);
                    }
                } else {
                    boolean allIntegers = par.getIntMatrices;

                    for (int i = 0; allIntegers && i < a.length; i++) {
                        for (int j = 0; allIntegers && j < a[0].length; j++) {
                            allIntegers &= _doubleIsInteger(a[i][j]);
                        }
                    }

                    if (allIntegers) {
                        int[][] tmp = new int[a.length][a[0].length];

                        for (int i = 0; i < a.length; i++) {
                            for (int j = 0; j < a[0].length; j++) {
                                tmp[i][j] = (int) a[i][j];
                            }
                        }

                        retval = new IntMatrixToken(tmp);
                    } else {
                        retval = new DoubleMatrixToken(a);
                    }
                }
            }
        } else if (maClassStr.equals("logical")) {
            int[][] a = ptmatlabGetLogicalMatrix(ma, nRows, nCols);

            if (a == null) {
                throw new IllegalActionException(
                        "can't get logical matrix from matlab engine.");
            }

            if (scalarMatrices) {
                retval = new IntToken(a[0][0]);
            } else {
                retval = new IntMatrixToken(a);
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
                            fieldValues[k] = _convertMxArrayToToken(fma, par);
                        } else {
                            throw new IllegalActionException("can't get field "
                                    + fieldNames[k] + "from matlab "
                                    + "struct " + nRows + "x" + nCols);
                        }
                    }

                    ta[m] = new RecordToken(fieldNames, fieldValues);
                }

                tr[n] = new ArrayToken(ta);
            }

            if (scalarStructs) {
                retval = ((ArrayToken) tr[0]).getElement(0);
            } else {
                retval = new ArrayToken(tr);
            }
        } else if (maClassStr.equals("cell")) {
            Token[] ta = new Token[nCols];
            Token[] tr = new Token[nRows];

            for (int n = 0; n < nRows; n++) {
                boolean anyIntegers = false;
                boolean anyDoubles = false;

                for (int m = 0; m < nCols; m++) {
                    long cma = ptmatlabGetCell(ma, n, m);

                    if (cma != 0) {
                        ta[m] = _convertMxArrayToToken(cma, par);

                        // Track whether we get mixed types back
                        if (ta[m] instanceof IntToken) {
                            anyIntegers = true;
                        } else if (ta[m] instanceof DoubleToken) {
                            anyDoubles = true;
                        }
                    } // else - throw exception?
                }

                if (anyIntegers && anyDoubles) {
                    for (int m = 0; m < ta.length; m++) {
                        if (ta[m] instanceof IntToken) {
                            ta[m] = DoubleToken.convert(ta[m]);
                        }
                    }
                }

                tr[n] = new ArrayToken(ta);

                // If not all tokens are of the same, this will throw
                // an exception.
            }

            if (nRows == 1) {
                retval = tr[0];
            } else {
                retval = new ArrayToken(tr);
            }
        } else if (maClassStr.equals("char")) {
            if (nRows == 1) {
                retval = new StringToken(ptmatlabGetString(ma, 0));
            } else {
                Token[] ta = new Token[nRows];

                for (int n = 0; n < nRows; n++) {
                    ta[n] = new StringToken(ptmatlabGetString(ma, n));
                }

                retval = new ArrayToken(ta);
            }
        } else {
            throw new IllegalActionException("no support for mxArray class "
                    + maClassStr + " " + dims[0] + " x " + dims[1]);
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
            Token[] ta = ((ArrayToken) t).arrayValue();

            if (!(ta[0] instanceof StringToken)) {
                ma = ptmatlabCreateCellMatrix(name, 1, ta.length);

                if (ma == 0) {
                    throw new IllegalActionException("couldn't create cell "
                            + "array " + name);
                }

                for (int n = 0; n < ta.length; n++) {
                    long fma = _createMxArray("(" + n + ")", ta[n]);

                    if (fma == 0) {
                        throw new IllegalActionException(
                                "couldn't create array for index " + n
                                + " in cell array " + name);
                    }

                    ptmatlabSetCell(name, ma, 0, n, fma);
                }
            } else {
                String s = ((StringToken) ta[0]).stringValue();
                ma = ptmatlabCreateString(name, s, ta.length, s.length());

                for (int n = 1; n < ta.length; n++) {
                    s = ((StringToken) ta[n]).stringValue();
                    ptmatlabSetString(name, ma, n, s, s.length());
                }
            }
        } else if (t instanceof RecordToken) {
            Object[] fieldNames = ((RecordToken) t).labelSet().toArray();
            ma = ptmatlabCreateStructMatrix(name, fieldNames, 1, 1);

            if (ma == 0) {
                throw new IllegalActionException("couldn't create struct "
                        + "array " + name);
            }

            for (Object fieldName : fieldNames) {
                Token f = ((RecordToken) t).get((String) fieldName);
                long fma = _createMxArray((String) fieldName, f);

                if (fma == 0) {
                    throw new IllegalActionException(
                            "couldn't create array for field " + fieldName
                            + " in struct " + name);
                }

                ptmatlabSetStructField(name, ma, (String) fieldName, 0, 0, fma);
            }
        } else if (t instanceof StringToken) {
            String s = ((StringToken) t).stringValue();
            ma = ptmatlabCreateString(name, s, 1, s.length());
        } else if (t instanceof ComplexMatrixToken) {
            Complex[][] a = ((ComplexMatrixToken) t).complexMatrix();
            ma = ptmatlabCreateComplexMatrix(name, a, a.length, a[0].length);
        } else if (t instanceof MatrixToken) {
            double[][] a = ((MatrixToken) t).doubleMatrix();
            ma = ptmatlabCreateDoubleMatrix(name, a, a.length, a[0].length);
        } else if (t instanceof ComplexToken) {
            Complex[] a = { ((ComplexToken) t).complexValue() };
            ma = ptmatlabCreateComplexMatrixOneDim(name, a, a.length);
        } else {
            double[] a = new double[1];

            if (t instanceof BooleanToken) {
                a[0] = ((BooleanToken) t).booleanValue() ? 1.0 : 0.0;
            } else if (t instanceof DoubleToken) {
                a[0] = ((ScalarToken) t).doubleValue();
            } else if (t instanceof IntToken) {
                a[0] = ((ScalarToken) t).intValue();
            } else {
                throw new IllegalActionException(
                        "Token "
                                + t
                                + " is of type "
                                + t.getType()
                                + ", it should be one of "
                                + "ArrayToken, RecordToken, StringToken, ComplexMatrixToken, "
                                + "MatrixToken, ComplexToken, BooleanToken, DoubleToken or IntToken.");
            }

            ma = ptmatlabCreateDoubleMatrixOneDim(name, a, 1);
        }

        if (ma == 0) {
            throw new IllegalActionException("couldn't create array for "
                    + name);
        }

        return ma;
    }

    private boolean _doubleIsInteger(double d) {
        // FindBugs reports "Test for floating point equality", which
        // may be ignored here because we really want to know if
        // the double is equal to the floor of the double.
        return d == Math.floor(d) && d <= Integer.MAX_VALUE
                && d >= Integer.MIN_VALUE;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Debug statements are sent to stdout if non-zero. If 1, only
    // this class sends debug statements, if 2 then ptmatlab.dll also
    // sends debug statements.
    private byte debug = 0;
}
