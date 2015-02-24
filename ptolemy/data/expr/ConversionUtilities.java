/* Utilities to convert between java types and Token types

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

 @ProposedRating Yellow (nsmyth)
 @AcceptedRating Red (cxh)

 Created : May 1998
 */
package ptolemy.data.expr;

import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanMatrixToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.FixMatrixToken;
import ptolemy.data.FixToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.ShortToken;
import ptolemy.data.StringToken;
import ptolemy.data.UnsignedByteToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FixType;
import ptolemy.data.type.ObjectType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;

///////////////////////////////////////////////////////////////////
//// ConversionUtilities

/**
 This class contains a series of static methods that facilitate the
 runtime conversion of tokens to and from Java representations that are
 not tokens.  One might call this "marshaling and unmarshaling" of
 tokens.  Primarily this facility is used by the expression language to
 properly type references to Java methods, and later invoke those
 methods during expression evaluation.  Generally speaking this is
 somewhat nasty from an Object-oriented point of view.  The nastiness
 is fairly well encapsulated in this class.  The mapping is summarized
 in the following table:

 <pre>
 Token type               Java type
 ---------------------------------------------------
 IntToken                 int
 DoubleToken              double
 LongToken                long
 StringToken              java.lang.String
 BooleanToken             boolean
 ComplexToken             ptolemy.math.Complex
 FixToken                 ptolemy.math.FixPoint
 FixMatrixToken           ptolemy.math.FixPoint[][]
 IntMatrixToken           int[][]
 DoubleMatrixToken        double[][]
 ComplexMatrixToken       ptolemy.math.Complex[][]
 LongMatrixToken          long[][]
 BooleanMatrixToken       boolean[][]
 ArrayToken(FixToken)     ptolemy.math.FixPoint[]
 ArrayToken(IntToken)     int[]
 ArrayToken(LongToken)    long[]
 ArrayToken(DoubleToken)  double[]
 ArrayToken(ComplexToken) ptolemy.math.Complex[]
 ArrayToken(StringToken)  java.lang.String[]
 ArrayToken(BooleanToken) boolean[]
 ArrayToken  (*)          Token[]
 ---------------------------------------------------
 (*) Only when converting from java to Token types
 </pre>

 @author Neil Smyth, Edward A. Lee, Steve Neuendorffer
 @author Zoltan Kemenczy, Research in Motion Limited
 @version $Id$
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 @see ptolemy.data.expr.UtilityFunctions
 @see java.lang.Math
 */
public class ConversionUtilities {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Convert a java object to a corresponding Token.  This method
     * is called by the expression language to marshal numeric object
     * into tokens.  If the argument is a token, this function returns
     * it.  If the argument is an array, an array token will be
     * returned.  If the argument is an array of array, then a matrix
     * token will be returned.  If the argument is a Java numeric
     * encapsulation object, e.g. java.lang.Double, then the number is
     * extracted and re-encapsulated in a token.  If no other
     * conversion is possible, then this method will simply return an
     * ObjectToken wrapping the object.
     * @param object The object that is a Java type to be converted.
     * The value of this parameter might be null, in which case an 
     * ObjectToken with a null v
     * @exception IllegalActionException If the selected conversion fails.
     * @return A new token.
     */
    public static ptolemy.data.Token convertJavaTypeToToken(Object object)
            throws ptolemy.kernel.util.IllegalActionException {
        ptolemy.data.Token returnValue = null;

        // FIXME: Object could be null. What to do here? 
        // Return null? Throw an exception?
        if (object instanceof ptolemy.data.Token) {
            returnValue = (ptolemy.data.Token) object;
        } else if (object instanceof ptolemy.data.Token[]) {
            returnValue = new ArrayToken((ptolemy.data.Token[]) object);
        } else if (object instanceof Boolean) {
            returnValue = new BooleanToken(((Boolean) object).booleanValue());
        } else if (object instanceof Byte) {
            // Note: This is technically not quite right, because of
            // the sign involved...  In lieu of a signed byte token,
            // we assume that methods that return byte should be
            // interpreted unsigned.
            returnValue = new UnsignedByteToken(((Byte) object).byteValue());
        } else if (object instanceof Short) {
            returnValue = new ShortToken(((Short) object).shortValue());
        } else if (object instanceof Integer) {
            returnValue = new IntToken(((Integer) object).intValue());
        } else if (object instanceof Long) {
            returnValue = new LongToken(((Long) object).longValue());
        } else if (object instanceof Double) {
            returnValue = new DoubleToken(((Double) object).doubleValue());
        } else if (object instanceof Float) {
            returnValue = new FloatToken(((Float) object).floatValue());
        } else if (object instanceof Complex) {
            returnValue = new ComplexToken((Complex) object);
        } else if (object instanceof FixPoint) {
            returnValue = new FixToken((FixPoint) object);
        } else if (object instanceof String) {
            returnValue = new StringToken((String) object);
        } else if (object instanceof boolean[][]) {
            returnValue = new BooleanMatrixToken((boolean[][]) object);
        } else if (object instanceof int[][]) {
            returnValue = new IntMatrixToken((int[][]) object);
        } else if (object instanceof long[][]) {
            returnValue = new LongMatrixToken((long[][]) object);
        } else if (object instanceof double[][]) {
            returnValue = new DoubleMatrixToken((double[][]) object);
        } else if (object instanceof Complex[][]) {
            returnValue = new ComplexMatrixToken((Complex[][]) object);
        } else if (object instanceof FixPoint[][]) {
            returnValue = new FixMatrixToken((FixPoint[][]) object);
        } else if (object instanceof double[]) {
            DoubleToken[] temp = new DoubleToken[((double[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new DoubleToken(((double[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object instanceof Complex[]) {
            ComplexToken[] temp = new ComplexToken[((Complex[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new ComplexToken(((Complex[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object instanceof int[]) {
            IntToken[] temp = new IntToken[((int[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new IntToken(((int[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object instanceof long[]) {
            LongToken[] temp = new LongToken[((long[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new LongToken(((long[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object instanceof boolean[]) {
            BooleanToken[] temp = new BooleanToken[((boolean[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new BooleanToken(((boolean[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object instanceof String[]) {
            StringToken[] temp = new StringToken[((String[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new StringToken(((String[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object instanceof FixPoint[]) {
            // Create back an ArrayToken containing FixTokens
            FixToken[] temp = new FixToken[((FixPoint[]) object).length];

            for (int j = 0; j < temp.length; j++) {
                temp[j] = new FixToken(((FixPoint[]) object)[j]);
            }

            returnValue = new ArrayToken(temp);
        } else if (object != null && object.getClass().isArray()) {
            Class elementClass = object.getClass().getComponentType();
            Type elementType = convertJavaTypeToTokenType(elementClass);

            Object[] array = (Object[]) object;
            ptolemy.data.Token[] tokens = new ptolemy.data.Token[array.length];
            for (int i = 0; i < array.length; i++) {
                tokens[i] = convertJavaTypeToToken(array[i]);
            }

            return new ArrayToken(elementType, tokens);
        } else {
            // Package into an ObjectToken.
            returnValue = new ObjectToken(object);
        }

        return returnValue;
    }

    /** Convert a java class, representing a Java type, to a
     *  corresponding instance of a ptolemy type object, as consistent
     *  with the convertJavaTypeToToken method.
     *  @param tokenClass the java class to be converted.
     *  @return The corresponding Ptolemy type object.
     *  @exception IllegalActionException If the token class is not
     *  recognized, or creating the type fails.
     */
    public static Type convertJavaTypeToTokenType(Class tokenClass)
            throws ptolemy.kernel.util.IllegalActionException {
        try {
            if (tokenClass.equals(ptolemy.data.Token.class)) {
                return BaseType.GENERAL;
            } else if (ptolemy.data.ArrayToken.class
                    .isAssignableFrom(tokenClass)) {
                Type type = new ArrayType(BaseType.GENERAL);
                return type;
            } else if (ptolemy.data.RecordToken.class
                    .isAssignableFrom(tokenClass)) {
                Type type = RecordType.EMPTY_RECORD;
                return type;
            } else if (ptolemy.data.Token.class.isAssignableFrom(tokenClass)) {
                Type type = BaseType.forClassName(tokenClass.getName());

                if (type == null) {
                    throw new IllegalActionException(
                            "Could not find return type for class "
                                    + tokenClass);
                }

                return type;
            } else if (tokenClass.equals(Boolean.class)
                    || tokenClass.equals(Boolean.TYPE)) {
                return BaseType.BOOLEAN;
            } else if (tokenClass.equals(Byte.class)
                    || tokenClass.equals(Byte.TYPE)) {
                return BaseType.UNSIGNED_BYTE;
            } else if (tokenClass.equals(Short.class)
                    || tokenClass.equals(Short.TYPE)) {
                return BaseType.SHORT;
            } else if (tokenClass.equals(Integer.class)
                    || tokenClass.equals(Integer.TYPE)) {
                return BaseType.INT;
            } else if (tokenClass.equals(Long.class)
                    || tokenClass.equals(Long.TYPE)) {
                return BaseType.LONG;
            } else if (tokenClass.equals(Double.class)
                    || tokenClass.equals(Double.TYPE)) {
                return BaseType.DOUBLE;
            } else if (tokenClass.equals(Float.class)
                    || tokenClass.equals(Float.TYPE)) {
                return BaseType.FLOAT;
            } else if (tokenClass.equals(Complex.class)) {
                return BaseType.COMPLEX;
            } else if (tokenClass.equals(FixPoint.class)) {
                return BaseType.UNSIZED_FIX;
            } else if (tokenClass.equals(String.class)) {
                return BaseType.STRING;
            } else if (tokenClass.equals(Class.forName("[[Z"))) {
                return BaseType.BOOLEAN_MATRIX;
            } else if (tokenClass.equals(Class.forName("[[I"))) {
                return BaseType.INT_MATRIX;
            } else if (tokenClass.equals(Class.forName("[[J"))) {
                return BaseType.LONG_MATRIX;
            } else if (tokenClass.equals(Class.forName("[[D"))) {
                return BaseType.DOUBLE_MATRIX;
            } else if (tokenClass.equals(Class
                    .forName("[[Lptolemy.math.Complex;"))) {
                return BaseType.COMPLEX_MATRIX;
            } else if (tokenClass.equals(Class
                    .forName("[[Lptolemy.math.FixPoint;"))) {
                return BaseType.FIX_MATRIX;
            } else if (tokenClass.isArray()) {
                return new ArrayType(
                        convertJavaTypeToTokenType(tokenClass
                                .getComponentType()));
            } else if (java.lang.Object.class.isAssignableFrom(tokenClass)) {
                return new ObjectType(tokenClass);
            } else if (tokenClass.isArray()) {
                Class elementClass = tokenClass.getComponentType();
                Type elementType = convertJavaTypeToTokenType(elementClass);
                return new ArrayType(elementType);
            } else {
                // This should really never happen, since every class
                // should be caught by the isAssignable test above,
                // but I don't like the dangling else if.
                throw new InternalErrorException("type not found: "
                        + tokenClass);
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalActionException(null, ex, "Could not find Class '"
                    + tokenClass + "'");
        }
    }

    /** Convert a Token to a corresponding Java object.  This method
     * is called by the expression language to unmarshal numeric
     * objects from tokens.  If the argument is an array token, this
     * function returns an Java array of the correct type.  If the
     * argument is a matrix token, this function returns a square Java
     * array of arrays.  If the argument is another type of token,
     * this function returns the encapsulated data, rewrapped in a
     * Java numeric encapsulating object, e.g. java.lang.Double, if
     * necessary.  If no conversion is possible, then this method
     * throws an exception.
     * @param token The token to be converted.
     * @exception IllegalActionException If the selected conversion fails.
     * @return An object that is not a ptolemy.data.Token or an array
     * of ptolemy.data.Token.
     */
    public static Object convertTokenToJavaType(ptolemy.data.Token token)
            throws ptolemy.kernel.util.IllegalActionException {
        // Design note: it is arguable that this could be moved to the
        // token interface for better object-oriented design, and to
        // more easily support token types not explicitly listed here.
        // We've opted instead to keep this code together with the
        // previous method, in order to emphasize their complementary
        // nature.  Hopefully this will also keep both conversions
        // consistent as well.
        Object returnValue;

        if (token instanceof DoubleToken) {
            returnValue = Double.valueOf(((DoubleToken) token).doubleValue());
        } else if (token instanceof IntToken) {
            returnValue = Integer.valueOf(((IntToken) token).intValue());
        } else if (token instanceof UnsignedByteToken) {
            returnValue = Byte.valueOf(((UnsignedByteToken) token).byteValue());
        } else if (token instanceof LongToken) {
            returnValue = Long.valueOf(((LongToken) token).longValue());
        } else if (token instanceof StringToken) {
            returnValue = ((StringToken) token).stringValue();
        } else if (token instanceof BooleanToken) {

            // FindBugs: "Creating new instances of java.lang.Boolean
            // wastes memory, since Boolean objects are immutable and
            // there are only two useful values of this type.  Use the
            // Boolean.valueOf() method to create Boolean objects
            // instead."
            returnValue = Boolean
                    .valueOf(((BooleanToken) token).booleanValue());
        } else if (token instanceof ComplexToken) {
            returnValue = ((ComplexToken) token).complexValue();
        } else if (token instanceof FixToken) {
            returnValue = ((FixToken) token).fixValue();
        } else if (token instanceof FloatToken) {
            returnValue = ((FloatToken) token).floatValue();
        } else if (token instanceof ShortToken) {
            returnValue = ((ShortToken) token).shortValue();
        } else if (token instanceof FixMatrixToken) {
            returnValue = ((FixMatrixToken) token).fixMatrix();
        } else if (token instanceof IntMatrixToken) {
            returnValue = ((IntMatrixToken) token).intMatrix();
        } else if (token instanceof DoubleMatrixToken) {
            returnValue = ((DoubleMatrixToken) token).doubleMatrix();
        } else if (token instanceof ComplexMatrixToken) {
            returnValue = ((ComplexMatrixToken) token).complexMatrix();
        } else if (token instanceof LongMatrixToken) {
            returnValue = ((LongMatrixToken) token).longMatrix();
        } else if (token instanceof BooleanMatrixToken) {
            returnValue = ((BooleanMatrixToken) token).booleanMatrix();
        } else if (token instanceof ArrayToken) {
            // This is frustrating... It would be nice if there
            // was a Token.getValue() that would return the
            // token element value in a polymorphic way...
            if (((ArrayToken) token).getElement(0) instanceof FixToken) {
                FixPoint[] array = new FixPoint[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((FixToken) ((ArrayToken) token).getElement(j))
                            .fixValue();
                }

                returnValue = array;
            } else if (((ArrayToken) token).getElement(0) instanceof IntToken) {
                int[] array = new int[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((IntToken) ((ArrayToken) token).getElement(j))
                            .intValue();
                }

                returnValue = array;
            } else if (((ArrayToken) token).getElement(0) instanceof LongToken) {
                long[] array = new long[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((LongToken) ((ArrayToken) token).getElement(j))
                            .longValue();
                }

                returnValue = array;
            } else if (((ArrayToken) token).getElement(0) instanceof DoubleToken) {
                double[] array = new double[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((DoubleToken) ((ArrayToken) token)
                            .getElement(j)).doubleValue();
                }

                returnValue = array;
            } else if (((ArrayToken) token).getElement(0) instanceof ComplexToken) {
                Complex[] array = new Complex[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((ComplexToken) ((ArrayToken) token)
                            .getElement(j)).complexValue();
                }

                returnValue = array;
            } else if (((ArrayToken) token).getElement(0) instanceof StringToken) {
                String[] array = new String[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((StringToken) ((ArrayToken) token)
                            .getElement(j)).stringValue();
                }

                returnValue = array;
            } else if (((ArrayToken) token).getElement(0) instanceof BooleanToken) {
                boolean[] array = new boolean[((ArrayToken) token).length()];

                for (int j = 0; j < array.length; j++) {
                    array[j] = ((BooleanToken) ((ArrayToken) token)
                            .getElement(j)).booleanValue();
                }

                returnValue = array;
            } else {
                throw new InternalErrorException("token type not recognized: "
                        + token);
            }
        } else {
            throw new InternalErrorException("token type not recognized: "
                    + token);
        }

        return returnValue;
    }

    /** Convert the given ptolemy type object to a java class
     * representing a java type.  The conversion is the complement of
     * that provided by the convertJavaTypeToTokenType() method.  Note
     * that, generally speaking the reverse is not true, since ptolemy
     * types represent more information about data types than do java
     * types.
     * @param type the given type.
     * @exception IllegalActionException If the token class is not
     *  recognized, or creating the type fails.
     * @return The class associated with the type.
     */
    public static Class convertTokenTypeToJavaType(Type type)
            throws ptolemy.kernel.util.IllegalActionException {
        try {
            if (type.equals(BaseType.DOUBLE)) {
                return Double.TYPE;
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                return Byte.TYPE;
            } else if (type.equals(BaseType.INT)) {
                return Integer.TYPE;
            } else if (type.equals(BaseType.FLOAT)) {
                return Short.TYPE;
            } else if (type.equals(BaseType.LONG)) {
                return Long.TYPE;
            } else if (type.equals(BaseType.SHORT)) {
                return Short.TYPE;
            } else if (type.equals(BaseType.STRING)) {
                return java.lang.String.class;
            } else if (type.equals(BaseType.BOOLEAN)) {
                return Boolean.TYPE;
            } else if (type.equals(BaseType.COMPLEX)) {
                return ptolemy.math.Complex.class;
            } else if (type.equals(BaseType.UNSIZED_FIX)) {
                return ptolemy.math.FixPoint.class;
            } else if (type instanceof FixType) {
                return ptolemy.math.FixPoint.class;
            } else if (type.equals(BaseType.BOOLEAN)) {
                return Class.forName("[[Lptolemy.math.FixPoint;");
            } else if (type.equals(BaseType.INT_MATRIX)) {
                return Class.forName("[[I");
            } else if (type.equals(BaseType.DOUBLE_MATRIX)) {
                return Class.forName("[[D");
            } else if (type.equals(BaseType.COMPLEX_MATRIX)) {
                return Class.forName("[[Lptolemy.math.Complex;");
            } else if (type.equals(BaseType.LONG_MATRIX)) {
                return Class.forName("[[J");
            } else if (type.equals(BaseType.BOOLEAN_MATRIX)) {
                return Class.forName("[[Z");
            } else if (type instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) type;
                Type elementType = arrayType.getElementType();

                if (elementType.equals(BaseType.DOUBLE)) {
                    return Class.forName("[D");
                } else if (elementType.equals(BaseType.INT)) {
                    return Class.forName("[I");
                } else if (elementType.equals(BaseType.LONG)) {
                    return Class.forName("[J");
                } else if (elementType.equals(BaseType.BOOLEAN)) {
                    return Class.forName("[Z");
                } else {
                    return java.lang.reflect.Array
                            .newInstance(
                                    convertTokenTypeToJavaType(arrayType
                                            .getElementType()),
                                    0).getClass();
                }
            } else {
                // Bailout.  The type is not recognized, so defer to
                // the type for some basic information.
                return type.getTokenClass();
            }
        } catch (ClassNotFoundException ex) {
            throw new IllegalActionException(null, ex, "Could not find Type '"
                    + type + "'");
        }
    }
}
