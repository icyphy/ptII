/* Class providing additional functions in the Ptolemy II expression language.

 Copyright (c) 1998-2003 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data.expr;

import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.util.StringUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.StringUtilities;
import ptolemy.data.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;
import java.util.Properties;
import java.util.StringTokenizer;


//////////////////////////////////////////////////////////////////////////
//// UtilityFunctions
/**
This class provides additional functions for use in the Ptolemy II
expression language.  All of the methods in this class are static
and return an instance of Token.  The expression language identifies
the appropriate method to use by using reflection, matching the
types of the arguments.

@author  Neil Smyth, Christopher Hylands, Bart Kienhuis, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/
public class UtilityFunctions {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a StringToken that contains the names of all the
     *  constants and their values.
     *  @return A token containing the names of all the constants
     *  and their values.
     *  @since Ptolemy II 2.1
     */
    public static StringToken constants() {
        return new StringToken(Constants.constants());
    }

    /** Return a Gaussian random number.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @return An observation of a Gaussian random variable.
     */
    public static DoubleToken gaussian(double mean, double standardDeviation) {
        if (_random == null) _random = new Random();
        double raw = _random.nextGaussian();
        double result = (raw*standardDeviation) + mean;
        return new DoubleToken(result);
    }

    /** Return a matrix of Gaussian random numbers.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @return A matrix of observations of a Gaussian random variable.
     */
    public static DoubleMatrixToken gaussian(
            double mean, double standardDeviation, int rows, int columns) {
        if (_random == null) _random = new Random();
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double raw = _random.nextGaussian();
                result[i][j] = (raw*standardDeviation) + mean;
            }
        }
	try {
            return new DoubleMatrixToken(result);
	} catch (IllegalActionException illegalAction) {
	    // This should not happen since result should not be null.
	    throw new InternalErrorException("UtilityFunction.gaussian: "
		    + "Cannot create the DoubleMatrixToken that contains "
		    + "Gaussian random numbers.");
	}
    }

    /** Find a file. Uses the supplied name and if it does not exist as is,
     *  searches the user directory followed by the current system
     *  java.class.path list and returns the first match or name unchanged.
     *  @param name Relative pathname of file/directory to find.
     *  @return Canonical absolute path if file/directory was found, otherwise
     *   returns unchanged name.
     */
    public static String findFile(String name) {
        File file = new File(name);
        if (!file.exists()) {
            String curDir = StringUtilities.getProperty("user.dir");
            file = new File(curDir, name);
        }
        if (!file.exists()) {
            String cp = System.getProperty("java.class.path");
            StringTokenizer tokens = new StringTokenizer(cp,
                    System.getProperty("path.separator"));
            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();
                file = new File(token, name);
                if (file.exists()) break;
            }
        }
        if (file.exists()) {
            try {
                return file.getCanonicalPath();
            } catch (java.io.IOException ex) {
                return file.getAbsolutePath();
            }
        }
        else
            return name;
    }

    /** Return the approximate number of bytes available for future
     *  object allocation.  Note that requesting a garbage collection
     *  may change this value.
     *  @return The approximate number of bytes available.
     *  @see #totalMemory()
     */
    public static LongToken freeMemory() {
	return new LongToken(Runtime.getRuntime().freeMemory());
    }

    /** Get the specified property from the environment. An empty string
     *  is returned if the argument environment variable does not exist,
     *  though if certain properties are not defined, then we
     *  make various attempts to determine them and then set them.
     *  See the javadoc page for java.util.System.getProperties() for
     *  a list of system properties.
     *  <p>The following properties are handled specially
     *  <dl>
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> vergil usually sets the ptolemy.ptII.dir property to the
     *  value of $PTII.  However, if we are running under Web Start,
     *  then this property might not be set, in which case we look
     *  for "ptolemy/kernel/util/NamedObj.class" and set the
     *  property accordingly.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> Return $PTII as a URL.  For example, if $PTII was c:\ptII,
     *  then return file:/c:/ptII/.
     *  <dt> "user.dir"
     *  <dd> Return the canonical path name to the current working directory.
     *  This is necessary because under JDK1.4.1 System.getProperty()
     *  returns <code><b>c</b>:/<i>foo</i></code>
     *  whereas most of the other methods that operate
     *  on path names return <code><b>C</b>:/<i>foo</i></code>.
     *  </dl>
     *  @param propertyName The name of property.
     *  @return A String containing the string value of the property.
     *  @deprecated Use
     *  {@link ptolemy.util.StringUtilities#getProperty(String)}
     *  instead
     */
    public static String getProperty(String propertyName) {
        return StringUtilities.getProperty(propertyName);
    }

    /** FIXME. Placeholder for a function that will return a model.
     */
    public static ObjectToken model(String classname)
            throws IllegalActionException {
        return new ObjectToken(classname);
    }

    /** Get the specified property from the environment. An empty string
     *  is returned if the argument environment variable does not exist.
     *  See the javadoc page for java.util.System.getProperties() for
     *  a list of system properties.  Example properties include:
     *  <dl>
     *  <dt> "java.version"
     *  <dd> the version of the JDK.
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> vergil usually sets the ptolemy.ptII.dir property to the
     *  value of $PTII.
     *  </dl>
     *
     *  @param propertyName The name of property.
     *  @return A token containing the string value of the property.
     *  @see ptolemy.util.StringUtilities#getProperty(String)
     */
    public static StringToken property(String propertyName) {
        return new StringToken(StringUtilities.getProperty(propertyName));
    }

    /** Get the string text contained in the specified file. For
     *  now this just looks in the directory where the parser
     *  is located, but will eventually (hopefully!) be able
     *  to use environment variables, user names etc. in
     *  creating a file path. An empty string
     *  is returned if the specified file could not be located.
     *  If the contents of the file consists of more than one line,
     *  then \n characters are converted to spaces, and \r characters
     *  are removed.  For example, if a file contains
     *  <pre>
     *  "
     *  foo
     *  "
     *  </pre>
     *  Then <code>eval(readfile("foo.txt"))</code> will return
     *  <code>" foo "</code>.  FIXME: this is a bug, we need to be smarter
     *  here.
     *  <br>Use readFile({@link #findFile}) to specify files relative to the
     *  current user directory or classpath.<p>
     *  A StringToken can be converted to any valid Token it represents
     *  with the Ptolemy II expression language eval() function.
     *  eval() is implemented in ptolemy.data.expr.ASTPtFunctionNode.java.
     *  For example: <code>eval(readFile("taps"))</code><p>
     *
     *  @param filename The file we want to read the text from.
     *  @return StringToken containing the text contained in
     *  the specified file.
     *  @exception IllegalActionException If for the given filename
     *  a file cannot be opened.
     *  @see ptolemy.data.expr.ASTPtFunctionNode
     */
    public static StringToken readFile(String filename)
            throws IllegalActionException {

        File file = new File(filename);
        //System.out.println("Trying to open file: " + file.toString());
        BufferedReader fin = null;
        String line;
        String result = "";
        String newline = System.getProperty("line.separator");
        try {
            if (file.exists()) {
                fin = new BufferedReader(new FileReader(file));
                while (true) {
                    try {
                        line = fin.readLine();
                    } catch (IOException e) {
                        break;
                    }

                    if (line == null) break;
                    result += line + newline;
                    //System.out.println("read in line: \"" +
                    //   line + newline + "\"");
                }
            }
        } catch (FileNotFoundException ex) {
           // what should we do here?
            throw new IllegalActionException(null, ex, "File not found");
        }
        //System.out.println("Contents of file are: " + result);
        return new StringToken(result);
    }

    /** Read a file that contains a matrix of reals in Matlab notation.
     *
     *  @param filename The filename.
     *  @return The matrix defined in the file.
     *  @exception IllegalActionException If the file cannot be opened.
     */
    public static DoubleMatrixToken readMatrix(String filename)
            throws IllegalActionException {

        DoubleMatrixToken returnMatrix = null;

        File file = new File(filename);
        FileReader fin = null;

        // Vector containing the matrix
        Vector k = null;

        // Parameters for the Matrix
        int row = -1;
        int column = -1;

        int rowPosition = 0;
        int columnPosition = 0;
        double[][] mtr = null;

        if (file.exists()) {

            try {
                // Open the matrix file
                fin = new FileReader(file);
            } catch (FileNotFoundException e) {
                throw new IllegalActionException("FIle Not FOUND");
            }


            // Read the file and convert it into a matrix
            if (_matrixParser == null) {
                _matrixParser = new MatrixParser( System.in );
            }

            _matrixParser.ReInit( fin );
            k = _matrixParser.readMatrix( );

            if ( column == -1 ) {
                // The column size of the matrix
                column = k.size();
            }

            Iterator i = k.iterator();
            while ( i.hasNext() ) {
                Vector l = (Vector) i.next();
                if ( row == -1 ) {
                    // the row size.
                    row = l.size();
                    // create a new matrix definition
                    mtr = new double[column][row];
                } else {
                    if ( row != l.size() ) {
                        throw new  IllegalActionException(" The Row" +
                                " size needs to be the same for all" +
                                " rows");
                    }
                }
                Iterator j = l.iterator();
                while ( j.hasNext() ) {
                    Double s = (Double) j.next();
                    mtr[columnPosition][rowPosition++] = s.doubleValue();
                }
                rowPosition = 0;
                columnPosition++;
            }

            // Vectors have now become obsolete, data is stored
            // in double[][].
            k.removeAll(k);
            returnMatrix =  new DoubleMatrixToken(mtr);
        } else {
            throw new IllegalActionException("ReadMatrix: File " +
                    filename + " not Found");
        }

        return returnMatrix;
    }

    /** Get the string text contained in the specified resource.
     *  Resource strings look like filenames without a leading slash
     *  and are always loaded relative to the Java class path.
     *  An empty string is returned if the specified file could
     *  not be located.  If the contents of the file consists of more
     *  than one line, then \n characters are converted to spaces, and
     *  \r characters are removed.  For example, if a file contains
     *  <pre> " foo " </pre> Then
     *  <code>eval(readfile("foo.txt"))</code> will return <code>" foo
     *  "</code>.  FIXME: this is a bug, we need to be smarter here.
     *  <br>Use readFile({@link #findFile}) to specify files relative
     *  to the current user directory or classpath.<p> A StringToken
     *  can be converted to any valid Token it represents with the
     *  Ptolemy II expression language eval() function.  eval() is
     *  implemented in ptolemy.data.expr.ASTPtFunctionNode.java.  For
     *  example: <code>eval(readFile("taps"))</code><p>
     *
     *  @param name The file we want to read the text from.
     *  @return StringToken containing the text contained in
     *  the specified file.
     *  @exception IllegalActionException If for the given filename
     *  a file cannot be opened.
     *  @see ptolemy.data.expr.ASTPtFunctionNode
     */
    public static StringToken readResource(String name)
            throws IllegalActionException {
        URL url = ClassLoader.getSystemResource(name);
        System.out.println("Trying to open url: " + url.toString());
        String result = "";
        try {
            InputStream stream = url.openStream();
            String line;
            String newline = System.getProperty("line.separator");
            BufferedReader fin = new BufferedReader(
                    new InputStreamReader(stream));
            while (true) {
                try {
                    line = fin.readLine();
                } catch (IOException e) {
                    break;
                }

                if (line == null) break;
                result += line + newline;
                //System.out.println("read in line: \"" +
                //   line + newline + "\"");
            }
        } catch (IOException ex) {
           // what should we do here?
            throw new IllegalActionException(null, ex, "File not found");
        }
        //System.out.println("Contents of file are: " + result);
        return new StringToken(result);
    }

    /** Create an array that contains the specified element
     *  repeated the specified number of times.
     *  @param numberOfTimes The number of times to repeat the element.
     *  @param element The element to repeat.
     *  @return A new array containing the specified element repeated the
     *   specified number of times.
     */
    public static ArrayToken repeat(IntToken numberOfTimes, Token element) {
        int length = numberOfTimes.intValue();
        Token[] result = new Token[length];
        for (int i = 0; i < length; i++) {
            result[i] = element;
        }

	ArrayToken arrayToken;
	try {
	    arrayToken = new ArrayToken(result);
	} catch (IllegalActionException illegalAction) {
	    // This should not happen since the elements of the array always
	    // have the same type.
	    throw new InternalErrorException("UtilityFunctions.repeat: "
                    + "Cannot construct ArrayToken. "
                    + illegalAction.getMessage());
	} catch (IllegalArgumentException illegalArgument) {
	    // This should not happen since the elements of the array always
	    // have the same type.
	    throw new InternalErrorException("UtilityFunctions.repeat: "
                    + "Cannot construct ArrayToken. "
                    + illegalArgument.getMessage());
        }
        return arrayToken;
    }

    /** Return the approximate number of bytes used by current objects
     *	and available for future object allocation.
     *  @return The total number of bytes used by the JVM.
     *  @see #freeMemory()
     */
    public static LongToken totalMemory() {
	return new LongToken(Runtime.getRuntime().totalMemory());
    }

    /** Return the zero matrix with the given number of rows and columns.
     *  @return The zero matrix with the given number of rows and columns.
     */
    public static DoubleMatrixToken zeroMatrix(int rows, int columns) {
        double[][] mtr = new double[rows][columns];
	DoubleMatrixToken result = null;
	try {
            result = new DoubleMatrixToken(mtr, DoubleMatrixToken.DO_NOT_COPY);
	} catch (IllegalActionException ex) {
            throw new InternalErrorException("UtilityFunctions.zeroMatrix: "
                    + "Cannot create DoubleMatrixToken. "
                    + ex.getMessage());
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The Matrix Parser. The Matrix parser is recreated for the standard
     *  in. However, we use ReInit for the specific matrix files.
     */
    private static MatrixParser _matrixParser;

    /** The random number generator.
     */
    private static Random _random;
}
