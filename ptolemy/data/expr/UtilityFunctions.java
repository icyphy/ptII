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
import ptolemy.data.type.BaseType;
import ptolemy.util.StringUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
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
import java.util.StringTokenizer;


//////////////////////////////////////////////////////////////////////////
//// UtilityFunctions
/**
This class provides additional functions for use in the Ptolemy II
expression language.  All of the methods in this class are static
and return an instance of Token.  The expression language identifies
the appropriate method to use by using reflection, matching the
types of the arguments.

@author  Neil Smyth, Christopher Hylands, Bart Kienhuis, Edward A. Lee, Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class UtilityFunctions {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert the second token to the type of the first.
     *  @exception IllegalActionException If the token cannot be converted.
     */
    public static Token cast(Token token1, Token token2)
            throws IllegalActionException {
        return token1.getType().convert(token2);
    }

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

    /** Return an array of Gaussian random numbers.
     *  @param mean The mean.
     *  @param standardDeviation The standard deviation.
     *  @param length The length of the array.
     *  @return An array of doubles with IID Gaussian random variables.
     */
    public static ArrayToken gaussian(
            double mean, double standardDeviation, int length) {
        if (_random == null) _random = new Random();
        DoubleToken[] result = new DoubleToken[length];
        for (int i = 0; i < length; i++) {
            double raw = _random.nextGaussian();
            result[i] = new DoubleToken((raw*standardDeviation) + mean);
        }
        try {
            return new ArrayToken(result);
        } catch (IllegalActionException illegalAction) {
            // This should not happen since result should not be null.
            throw new InternalErrorException("UtilityFunction.gaussian: "
                    + "Cannot create the array that contains "
                    + "Gaussian random numbers.");
        }
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

    /** Find a file or directory. If the file does not exist as is, then
     *  search the current working directory, the user's home directory,
     *  and finally, the classpath.
     *  @param name Path of a file or directory to find.
     *  @return Canonical absolute path if the file or directory is found,
     *   otherwise the argument is returned unchanged.
     */
    public static String findFile(String name) {
        File file = new File(name);
        if (!file.exists()) {
            String curDir = StringUtilities.getProperty("user.dir");
            file = new File(curDir, name);
        }
        if (!file.exists()) {
            String curDir = StringUtilities.getProperty("user.home");
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

    /** Load a library by first using the default platform dependent
     *  System.loadLibrary() method.  If the library cannot be loaded
     *  using System.loadLibrary(), then search for the library using
     *  {@link #findFile(String)} and if the library is found,
     *  load it using System.load().  If the library is not found
     *  by findFile(), then we through the initial exception.
     *
     *  @param library the name of the library to be loaded.  The name
     *  should not include the platform dependent suffix.
     */
    public static void loadLibrary(String library) {
        try {
            if (library.indexOf(File.separator) == -1) {
                // loadLibrary does not work if the library has a \ or / in it.
                System.loadLibrary(library);
            } else {
                // load() does not work with relative paths.
                System.load(library);
            }
        } catch (UnsatisfiedLinkError ex) {
            String sharedLibrarySuffix = "dll";
            String osName = StringUtilities.getProperty("os.name");
            if (osName.startsWith("SunOS") || osName.startsWith("Linux")) {
                sharedLibrarySuffix = "so";
                // Under Solaris, libraries start with lib, so
                // we find the last /, and if the next chars are not "lib"
                // then we insert "lib".
                int index = library.lastIndexOf("/");
                if (index == -1) {
                    if (!library.startsWith("lib")) {
                        library = "lib" + library;
                    }
                } else {
                    if (!library.substring(index, index + 4).equals("/lib")) {
                        library = library.substring(0, index) + "/lib"
                            + library.substring(index + 1);
                    }
                }
            }
            String libraryWithSuffix =
                library + "." + sharedLibrarySuffix;

            String libraryPath = UtilityFunctions.findFile(libraryWithSuffix);

            if (libraryPath.equals(libraryWithSuffix)) {
                // UnsatisfiedLinkError does not have a (String, Throwable)
                // constructor, so we call initCause().

                String userDir = "<<user.dir unknown>>";
                try {
                    userDir = System.getProperty("user.dir");
                } catch (Throwable throwable) {
                    // Ignore.
                }

                String userHome = "<<user.home unknown>>";
                try {
                    userHome = System.getProperty("user.home");
                } catch (Throwable throwable) {
                    // Ignore.
                }

                String classpath = "<<classpath unknown>>";
                try {
                    classpath = System.getProperty("java.class.path");
                } catch (Throwable throwable) {
                    // Ignore.
                }
                Error error =
                    new UnsatisfiedLinkError("Did not find '"+ library
                            + "' in path, searched "
                            + "user.home (" + userDir
                            + ") user.dir (" + userHome
                            + ") and the classpath for '"
                            + libraryPath + "', but that "
                            + "was not found either.\n"
                            + "classpath was: "
                            + classpath);
                error.initCause(ex);
                throw error;
            }

            // System.loadLibrary() does not handle pathnames with separators.

            // If we get to here and load a library that includes references
            // to libraries not in the PATH or LD_LIBRARY_PATH, then we will
            // get and UnsatisfiedLinkError on the file we depend on.

            // For example, if liba.so uses libb.so and we call this
            // method on a, then libb.so will not be found.

            System.load(libraryPath);
        }
    }

    /** Return the maximum of two unsigned bytes.
     *  @param x An unsigned byte.
     *  @param y An unsigned byte.
     *  @return The maximum of x and y.
     */
    public static UnsignedByteToken max(
            UnsignedByteToken x, UnsignedByteToken y) {
        if (x.intValue() > y.intValue()) {
            return x;
        } else {
            return y;
        }
    }

    /** Return the maximum of the contents of the array.
     *  @param array An array of scalar tokens.
     *  @return The largest element of the array.
     *  @exception IllegalActionException If the array is empty or
     *   it contains tokens that are not scalar or it contains complex tokens.
     */
    public static ScalarToken max(ArrayToken array)
            throws IllegalActionException {
        if (array.length() == 0
                || !BaseType.SCALAR.isCompatible(array.getElementType())) {
            throw new IllegalActionException(
                    "max function can only be applied to arrays of scalars.");
        }
        ScalarToken result = (ScalarToken)array.getElement(0);
        for (int i = 1; i < array.length(); i++) {
            ScalarToken element = (ScalarToken)array.getElement(i);
            if ((element.isGreaterThan(result)).booleanValue()) {
                result = element;
            }
        }
        return result;
    }

    /** Return the minimum of two unsigned bytes.
     *  @param x An unsigned byte.
     *  @param y An unsigned byte.
     *  @return The minimum of x and y.
     */
    public static UnsignedByteToken min(
            UnsignedByteToken x, UnsignedByteToken y) {
        if (x.intValue() < y.intValue()) {
            return x;
        } else {
            return y;
        }
    }

    /** Return the minimum of the contents of the array.
     *  @param array An array of scalar tokens.
     *  @return The largest element of the array.
     *  @exception IllegalActionException If the array is empty or
     *   it contains tokens that are not scalar or it contains complex tokens.
     */
    public static ScalarToken min(ArrayToken array)
            throws IllegalActionException {
        if (array.length() == 0
                || !BaseType.SCALAR.isCompatible(array.getElementType())) {
            throw new IllegalActionException(
                    "min function can only be applied to arrays of scalars.");
        }
        ScalarToken result = (ScalarToken)array.getElement(0);
        for (int i = 1; i < array.length(); i++) {
            ScalarToken element = (ScalarToken)array.getElement(i);
            if ((element.isLessThan(result)).booleanValue()) {
                result = element;
            }
        }
        return result;
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
     *  <dd> The value of $PTII, which is the name of the directory in
     *       which Ptolemy II is installed.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> The value of $PTII as a URL, which is the name of the directory in
     *       which Ptolemy II is installed.
     *  <dt> "user.dir"
     *  <dd> The canonical path name to the current working directory.
     *  </dl>
     *
     *  @param propertyName The name of property.
     *  @return A token containing the string value of the property.
     *  @see ptolemy.util.StringUtilities#getProperty(String)
     */
    public static StringToken property(String propertyName) {
        return new StringToken(StringUtilities.getProperty(propertyName));
    }

    /** Return an array of IID random numbers with value greater than
     *  or equal to 0.0 and less than 1.0.
     *  @param length The length of the array.
     *  @return An array of doubles with IID random variables.
     */
    public static ArrayToken random(int length) {
        DoubleToken[] result = new DoubleToken[length];
        for (int i = 0; i < length; i++) {
            result[i] = new DoubleToken(Math.random());
        }
        try {
            return new ArrayToken(result);
        } catch (IllegalActionException illegalAction) {
            // This should not happen since result should not be null.
            throw new InternalErrorException("UtilityFunction.random: "
                    + "Cannot create the array that contains "
                    + "random numbers.");
        }
    }

    /** Return a matrix of IID random numbers with value greater than
     *  or equal to 0.0 and less than 1.0.
     *  @param rows The number of rows.
     *  @param columns The number of columns.
     *  @return A matrix of IID random variables.
     */
    public static DoubleMatrixToken random(int rows, int columns) {
        double[][] result = new double[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                result[i][j] = Math.random();
            }
        }
        try {
            return new DoubleMatrixToken(result);
        } catch (IllegalActionException illegalAction) {
            // This should not happen since result should not be null.
            throw new InternalErrorException("UtilityFunction.random: "
                    + "Cannot create the DoubleMatrixToken that contains "
                    + "random numbers.");
        }
    }

    /** Get the string text contained in the specified file. The argument
     *  is first interpreted using findFile(), so file names relative to
     *  the current working directory, the user's home directory, or the
     *  classpath are understood. If the file contains text that is a
     *  valid expression in the expression language, then that text can
     *  interpreted using the eval() function in
     *  ptolemy.data.expr.ASTPtFunctionNode.
     *  For example: <code>eval(readFile("<i>filename</i>"))</code><p>
     *
     *  @param filename The name of the file to read from.
     *  @return A StringToken containing the text contained in
     *   the specified file.
     *  @exception IllegalActionException If the file cannot be opened.
     *  @see ptolemy.data.expr.ASTPtFunctionNode
     *  @see #readResource(String)
     */
    public static StringToken readFile(String filename)
            throws IllegalActionException {

        File file = new File(findFile(filename));
        //System.out.println("Trying to open file: " + file.toString());
        BufferedReader fin = null;
        String line;
        StringBuffer result = new StringBuffer("");
        String newline = System.getProperty("line.separator");
        try {
            fin = new BufferedReader(new FileReader(file));
            while (true) {
                try {
                    line = fin.readLine();
                } catch (IOException e) {
                    break;
                }
                if (line == null) break;
                result.append(line + newline);
            }
        } catch (FileNotFoundException ex) {
            throw new IllegalActionException(null, ex, "File not found");
        }
        return new StringToken(result.toString());
    }

    /** Read a file that contains a matrix of reals in Matlab notation.
     *
     *  @param filename The filename.
     *  @return The matrix defined in the file.
     *  @exception IllegalActionException If the file cannot be opened.
     *  @deprecated Use eval(readFile()) instead.
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
            MatrixParser.ReInit( fin );
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

    /** Get the string text contained in the specified resource, which
     *  is a file that is specified relative to the Java classpath.
     *  Resource strings look like filenames without a leading slash.
     *  If the file contains text that is a
     *  valid expression in the expression language, then that text can
     *  interpreted using the eval() function in
     *  ptolemy.data.expr.ASTPtFunctionNode.
     *  For example: <code>eval(readFile("<i>filename</i>"))</code><p>
     *
     *  @param name The name of the resource to read from.
     *  @return A StringToken containing the text contained in
     *   the specified resource.
     *  @exception IllegalActionException If the resource cannot be opened.
     *  @see ptolemy.data.expr.ASTPtFunctionNode
     *  @see #readFile(String)
     */
    public static StringToken readResource(String name)
            throws IllegalActionException {
        URL url = ClassLoader.getSystemResource(name);
        StringBuffer result = new StringBuffer("");
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
                result.append(line + newline);
            }
        } catch (IOException ex) {
            throw new IllegalActionException(null, ex, "File not found");
        }
        return new StringToken(result.toString());
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

    /** Return the sum of the elements in the specified array.
     *  This method is polymorphic in that it can sum any array
     *  whose elements support addition.
     *  @param array An array.
     *  @returns The sum of the elements of the array.
     *  @exception IllegalActionException If the length of the
     * array is zero, or if the array elements do not support
     * addition.
     */
    public static final Token sum(ArrayToken array)
            throws IllegalActionException {
        if (array == null || array.length() < 1) {
            throw new IllegalActionException(
                    "sum() function cannot be applied to an empty array");
        }
        Token result = array.getElement(0);
        for (int i = 1; i < array.length(); i++) {
            result = result.add(array.getElement(i));
        }
        return result;
    }
    
    /** Return the approximate number of bytes used by current objects
     *  and available for future object allocation.
     *  @return The total number of bytes used by the JVM.
     *  @see #freeMemory()
     */
    public static LongToken totalMemory() {
        return new LongToken(Runtime.getRuntime().totalMemory());
    }
    
    /** Evaluate the given string as an expression in the expression
     *  language.  Instead of returning the resulting value, return a
     *  trace of the evaluation, including such useful information as
     *  what registered method is actually invoked.
     *  @param string The string to be parsed and evaluated.
     *  @return A string representing an evaluating trace.
     */
    public static String traceEvaluation(String string) 
            throws IllegalActionException {
        PtParser parser = new PtParser();
        ASTPtRootNode parseTree = parser.generateParseTree(string);
        ParseTreeEvaluator evaluator = new ParseTreeEvaluator();
        return evaluator.traceParseTreeEvaluation(parseTree, null).toString();
    }

    /** Return a double zero matrix with the given number of rows and
     *  columns.
     *  @return The zero matrix with the given number of rows and
     *  columns.
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
