/* ASTPtFunctionNode represents function nodes or array references in the parse tree

 Copyright (c) 1998-2003 The Regents of the University of California and
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998
*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.kernel.util.*;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;
import ptolemy.math.IntegerMatrixMath;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.ComplexMatrixMath;
import ptolemy.matlab.Engine;
import java.lang.Math;		/* Needed for javadoc */
import java.util.Iterator;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents function nodes in the
parse tree.  The first child of this node is the child node for the
name.  The remaining children are node representing the arguments of
the function.

<p> A function node is created when a function call is parsed. This node
will search for the function, using reflection, in the classes
registered for this purpose with the parser. Thus to add to the list
of functions available to the expression, it is only necessary to
create a new class with the functions defined in it and register
it with the parser. By default only java.lang.Math and
ptolemy.data.expr.UtilityFunctions are searched for a given function.
<p>
The one exception to the above rule is a recursive call to the parser.
The function eval() takes as an argument a StringToken, and parses
and evaluates the contained String by re-invoking the parser. The
scope for the re-evaluation (i.e. the Parameters it can refer to
by name) is the same as the main expression in which this function
call is embedded. Note that the parse tree as it is returned from
the parser will contain a node representing this function. Then
when the tree is evaluated, the call to eval() with both create
and evaluate the parse tree for the expression argument to obtain
the Token to be stored in this node.
<p>
The arguments to a function and its return types can be either
Java primitive types (double, boolean, etc.), String types,
or Token types.
Argument type polymorphism is supported. That is, one can define a
function foo(IntToken) and a different function foo(DoubleToken),
and the correct function will be invoked.<p>
This class first attempts to find a static function signature
among the registered classes using token argument types.
If this fails, the token argument values supplied by the expression
parser are mapped to java types according to the following table:
<pre>
     Token type               Java type
     ---------------------------------------------------
     IntToken                 int
     DoubleToken              double
     LongToken                long
     StringToken              String
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
     ArrayToken(StringToken)  String[]
     ArrayToken(BooleanToken) boolean[]
     ArrayToken  (*)          Token[]
     ---------------------------------------------------
     (*) Only when converting from java to Token types
</pre>
That is, static functions using java types will be matched if all
arguments are one of the types (or subclasses of) java types
listed in the table above.<p>
The function result type is subject to the same rules.<p>
If the above fails and at least one argument is an
array type, the dimensions of the argument types are reduced by
one and the registered function classes are searched again. This
process is repeated until all arguments are scalars or a function
signature match is found. If a match is found, the function is
iterated over the argument array and the results are aggregated
into a result array which is returned.<p>
For example, the "fix([0.5, 0.1; 0.4, 0.3], 16, 1)" expression
performs the argument dimension reduction technique twice until
the fix(double,int,int) signature is found in the
ptolemy.data.expr.FixPointFunctions class. This function is
iterated over the elements of rows, returning rows of FixPoint
results, and then the rows are combined into a FixPoint matrix
FixPoint[][] which is converted to a FixMatrixToken result
according to the above table.<p>
If you have matlab installed on your system, you may use an
expression in the form of <em>matlab("expression",arg1,arg2,...)</em>,
where <em>arg1,arg2,...</em>is a list of arguments appearing in
<em>"expression"</em>. Note that this form of invoking matlab
is limited to returning only the first return value of a matlab
function. If you need multiple return values, use the matlab
{@link ptolemy.matlab.Expression} actor. If a "packageDirectories"
Parameter is in the scope of this expression, it's value is
added to the matlab path while the expression is being executed
(like {@link ptolemy.matlab.Expression}).
<p>
@author Neil Smyth, Edward A. Lee, Steve Neuendorffer
@author Zoltan Kemenczy, Research in Motion Limited
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
@see ptolemy.data.expr.UtilityFunctions
@see java.lang.Math
*/
public class ASTPtFunctionNode extends ASTPtRootNode {

    public ASTPtFunctionNode(int id) {
        super(id);
    }

    public ASTPtFunctionNode(PtParser p, int id) {
        super(p, id);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public String getFunctionName() {
        Node n = jjtGetChild(0);
        if (!(n instanceof ASTPtLeafNode))
            return null;
        else
            return ((ASTPtLeafNode)n).getName();
    }
    
    public void jjtClose() {
        super.jjtClose();
        // We cannot assume that the result of a function call is
        // constant, even when the arguments to the function are.
        _isConstant = false;
    }

    /** Traverse this node with the given visitor.
     */
    public void visit(ParseTreeVisitor visitor)
            throws IllegalActionException {
        visitor.visitFunctionNode(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // Convert a token to its underlying java type and return its value
    // (Object) and type (Class).
    // @deprecated use separate convert methods for type and arg.
    public static Object[] convertTokenToJavaType(ptolemy.data.Token token)
            throws ptolemy.kernel.util.IllegalActionException {
        Object[] retval = new Object[2];
        if (token instanceof DoubleToken) {
            // Note: Java makes a distinction between the class objects
            // for double & Double...
            retval[0] = new Double(((DoubleToken)token).doubleValue());
            retval[1] = Double.TYPE;
        } else if (token instanceof UnsignedByteToken) {
            retval[0] = new Byte(((UnsignedByteToken)token).byteValue());
            retval[1] = Byte.TYPE;
        } else if (token instanceof IntToken) {
            retval[0] = new Integer(((IntToken)token).intValue());
            retval[1] = Integer.TYPE;
        } else if (token instanceof LongToken) {
            retval[0] = new Long(((LongToken)token).longValue());
            retval[1] = Long.TYPE;
        } else if (token instanceof StringToken) {
            retval[0] = new String(((StringToken)token).stringValue());
            retval[1] = retval[0].getClass();
        } else if (token instanceof BooleanToken) {
            retval[0] =
                new Boolean(((BooleanToken)token).booleanValue());
            retval[1] = Boolean.TYPE;
        } else if (token instanceof ComplexToken) {
            retval[0] = ((ComplexToken)token).complexValue();
            retval[1] = retval[0].getClass();
        } else if (token instanceof FixToken) {
            retval[0] = ((FixToken)token).fixValue();
            retval[1] = retval[0].getClass();
        } else if (token instanceof FixMatrixToken) {
            retval[0] = ((FixMatrixToken)token).fixMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof IntMatrixToken) {
            retval[0] = ((IntMatrixToken)token).intMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof DoubleMatrixToken) {
            retval[0] = ((DoubleMatrixToken)token).doubleMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof ComplexMatrixToken) {
            retval[0] = ((ComplexMatrixToken)token).complexMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof LongMatrixToken) {
            retval[0] = ((LongMatrixToken)token).longMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof BooleanMatrixToken) {
            retval[0] = ((BooleanMatrixToken)token).booleanMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof ArrayToken) {
            // This is frustrating... It would be nice if there
            // was a Token.getValue() that would return the
            // token element value in a polymorphic way...
            if (((ArrayToken)token).getElement(0)
                instanceof FixToken) {
                FixPoint[] array = new FixPoint
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((FixToken)((ArrayToken)token)
                                .getElement(j)).fixValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof IntToken) {
                int[] array = new int[((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((IntToken)((ArrayToken)token)
                                .getElement(j)).intValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof LongToken) {
                long[] array = new long[((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((LongToken)((ArrayToken)token)
                                .getElement(j)).longValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof DoubleToken) {
                double[] array = new double
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((DoubleToken)((ArrayToken)token)
                                .getElement(j)).doubleValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof ComplexToken) {
                Complex[] array = new Complex
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((ComplexToken)((ArrayToken)token)
                                .getElement(j)).complexValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof StringToken) {
                String[] array = new String
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((StringToken)((ArrayToken)token)
                                .getElement(j)).stringValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof BooleanToken) {
                boolean[] array = new boolean
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((BooleanToken)((ArrayToken)token)
                                .getElement(j)).booleanValue();
                }
                retval[0] = array;
            } else {
                // Bailout if we don't recognize the argument.
                retval[0] = token;
            }
            retval[1] = retval[0].getClass();
        } else {
            // Bailout if we don't recognize the argument.
            retval[0] = token;
            retval[1] = retval[0].getClass();
        }
        return retval;
    }

    // Convert a token to its underlying java type and return its value
    // (Object) and type (Class).
    // @deprecated Use separate convert methods for type and arg.
    public static Object convertTokenToJava(ptolemy.data.Token token)
            throws ptolemy.kernel.util.IllegalActionException {
        Object[] retval = new Object[2];
        if (token instanceof DoubleToken) {
            // Note: Java makes a distinction between the class objects
            // for double & Double...
            retval[0] = new Double(((DoubleToken)token).doubleValue());
            retval[1] = Double.TYPE;
        } else if (token instanceof IntToken) {
            retval[0] = new Integer(((IntToken)token).intValue());
            retval[1] = Integer.TYPE;
        } else if (token instanceof UnsignedByteToken) {
            retval[0] = new Byte(((UnsignedByteToken)token).byteValue());
            retval[1] = Byte.TYPE;
        } else if (token instanceof LongToken) {
            retval[0] = new Long(((LongToken)token).longValue());
            retval[1] = Long.TYPE;
        } else if (token instanceof StringToken) {
            retval[0] = new String(((StringToken)token).stringValue());
            retval[1] = retval[0].getClass();
        } else if (token instanceof BooleanToken) {
            retval[0] =
                new Boolean(((BooleanToken)token).booleanValue());
            retval[1] = Boolean.TYPE;
        } else if (token instanceof ComplexToken) {
            retval[0] = ((ComplexToken)token).complexValue();
            retval[1] = retval[0].getClass();
        } else if (token instanceof FixToken) {
            retval[0] = ((FixToken)token).fixValue();
            retval[1] = retval[0].getClass();
        } else if (token instanceof FixMatrixToken) {
            retval[0] = ((FixMatrixToken)token).fixMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof IntMatrixToken) {
            retval[0] = ((IntMatrixToken)token).intMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof DoubleMatrixToken) {
            retval[0] = ((DoubleMatrixToken)token).doubleMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof ComplexMatrixToken) {
            retval[0] = ((ComplexMatrixToken)token).complexMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof LongMatrixToken) {
            retval[0] = ((LongMatrixToken)token).longMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof BooleanMatrixToken) {
            retval[0] = ((BooleanMatrixToken)token).booleanMatrix();
            retval[1] = retval[0].getClass();
        } else if (token instanceof ArrayToken) {
            // This is frustrating... It would be nice if there
            // was a Token.getValue() that would return the
            // token element value in a polymorphic way...
            if (((ArrayToken)token).getElement(0)
                instanceof FixToken) {
                FixPoint[] array = new FixPoint
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((FixToken)((ArrayToken)token)
                                .getElement(j)).fixValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof IntToken) {
                int[] array = new int[((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((IntToken)((ArrayToken)token)
                                .getElement(j)).intValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof LongToken) {
                long[] array = new long[((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((LongToken)((ArrayToken)token)
                                .getElement(j)).longValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof DoubleToken) {
                double[] array = new double
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((DoubleToken)((ArrayToken)token)
                                .getElement(j)).doubleValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof ComplexToken) {
                Complex[] array = new Complex
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((ComplexToken)((ArrayToken)token)
                                .getElement(j)).complexValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof StringToken) {
                String[] array = new String
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((StringToken)((ArrayToken)token)
                                .getElement(j)).stringValue();
                }
                retval[0] = array;
            } else if (((ArrayToken)token).getElement(0)
                       instanceof BooleanToken) {
                boolean[] array = new boolean
                    [((ArrayToken)token).length()];
                for (int j = 0; j < array.length; j++) {
                    array[j] = ((BooleanToken)((ArrayToken)token)
                                .getElement(j)).booleanValue();
                }
                retval[0] = array;
            } else {
                // Bailout if we don't recognize the argument.
                retval[0] = token;
            }
            retval[1] = retval[0].getClass();
        } else {
            // Bailout if we don't recognize the argument.
            retval[0] = token;
            retval[1] = retval[0].getClass();
        }
        return retval;
    }

    public static Class convertTokenTypeToJavaType(Type type)
            throws ptolemy.kernel.util.IllegalActionException {
        try {
            if (type.equals(BaseType.DOUBLE)) {
                return Double.TYPE;
            } else if (type.equals(BaseType.UNSIGNED_BYTE)) {
                return Byte.TYPE;
            } else  if (type.equals(BaseType.INT)) {
                return Integer.TYPE;
            } else if (type.equals(BaseType.LONG)) {
                return Long.TYPE;
            } else if (type.equals(BaseType.STRING)) {
                return java.lang.String.class;
            } else if (type.equals(BaseType.BOOLEAN)) {
                return Boolean.TYPE;
            } else if (type.equals(BaseType.COMPLEX)) {
                return ptolemy.math.Complex.class;
            } else if (type.equals(BaseType.FIX)) {
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
                ArrayType arrayType = (ArrayType)type;
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
                    return java.lang.reflect.Array.newInstance(
                            convertTokenTypeToJavaType(
                                    arrayType.getElementType()),
                            0).getClass();
                }
            } else {
                // Bailout.
                return type.getTokenClass();
            }
        } catch(ClassNotFoundException ex) {
            throw new IllegalActionException(null, ex, ex.getMessage());
        }
    }

    // Convert a java class to a corresponding token class.
    public static Type convertJavaTypeToTokenType(Class tokenClass)
        throws ptolemy.kernel.util.IllegalActionException {
        try {
            if (tokenClass.equals(ptolemy.data.Token.class)) {
                return BaseType.GENERAL;
            } else if (ptolemy.data.Token.class.isAssignableFrom(tokenClass)) {
                Type type = BaseType.forClassName(tokenClass.getName());
                if(type == null) {
                    throw new IllegalActionException(
                            "Could not return type for class " + tokenClass);
                }
                return type;
            } else if (tokenClass.equals(Boolean.class) ||
                    tokenClass.equals(Boolean.TYPE)) {
                return BaseType.BOOLEAN;
            } else if (tokenClass.equals(Byte.class) ||
                    tokenClass.equals(Byte.TYPE)) {
                return BaseType.UNSIGNED_BYTE;
            } else if (tokenClass.equals(Integer.class) ||
                    tokenClass.equals(Integer.TYPE)) {
                return BaseType.INT;
            } else if (tokenClass.equals(Long.class) ||
                    tokenClass.equals(Long.TYPE)) {
                return BaseType.LONG;
            } else if (tokenClass.equals(Double.class) ||
                    tokenClass.equals(Double.TYPE)) {
                return BaseType.DOUBLE;
            } else if (tokenClass.equals(Complex.class)) {
                return BaseType.COMPLEX;
            } else if (tokenClass.equals(FixPoint.class)) {
                return BaseType.FIX;
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
            } else if (tokenClass.equals(
                               Class.forName("[[Lptolemy.math.Complex;"))) {
                return BaseType.COMPLEX_MATRIX;
            }  else if (tokenClass.equals(
                                Class.forName("[[Lptolemy.math.FixPoint;"))) {
                return BaseType.FIX_MATRIX;
            } else if (tokenClass.isArray()) {
                return new ArrayType(convertJavaTypeToTokenType(
                                             tokenClass.getComponentType()));
                
            } else if (java.lang.Object.class.isAssignableFrom(tokenClass)) {
                return BaseType.OBJECT;
            } else {
                throw new IllegalActionException(
                        "type not found: " + tokenClass);
            }
        } catch(ClassNotFoundException ex) {
            throw new IllegalActionException(null, ex, ex.getMessage());
        }
    }

    // Convert a java object to its corresponding Token.
    public static ptolemy.data.Token convertJavaTypeToToken(Object object)
        throws ptolemy.kernel.util.IllegalActionException {
        ptolemy.data.Token retval = null;
        if (object instanceof ptolemy.data.Token) {
            retval = (ptolemy.data.Token)object;
        } else if (object instanceof ptolemy.data.Token[]) {
            retval = new ArrayToken((ptolemy.data.Token[])object);
        } else if (object instanceof Double) {
            retval = new DoubleToken(((Double)object).doubleValue());
        } else if (object instanceof Byte) {
            // FIXME: not quite right? Sign?
            retval = new UnsignedByteToken(((Byte)object).byteValue());
        } else if (object instanceof Integer) {
            retval = new IntToken(((Integer)object).intValue());
        } else if (object instanceof Long) {
            retval = new LongToken(((Long)object).longValue());
        } else if (object instanceof String) {
            retval = new StringToken((String)object);
        } else if (object instanceof Boolean) {
            retval = new BooleanToken(((Boolean)object).booleanValue());
        } else if (object instanceof Complex) {
            retval = new ComplexToken((Complex)object);
        } else if (object instanceof FixPoint) {
            retval = new FixToken((FixPoint)object);
        } else if (object instanceof int[][]) {
            retval = new IntMatrixToken((int[][])object);
        } else if (object instanceof double[][]) {
            retval = new DoubleMatrixToken((double[][])object);
        } else if (object instanceof Complex[][]) {
            retval = new ComplexMatrixToken((Complex[][])object);
        } else if (object instanceof long[][]) {
            retval = new LongMatrixToken((long[][])object);
        } else if (object instanceof FixPoint[][]) {
            retval = new FixMatrixToken((FixPoint[][])object);
        } else if (object instanceof double[]) {
            DoubleToken[] temp = new DoubleToken
                [((double[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new DoubleToken(((double[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if (object instanceof Complex[]) {
            ComplexToken[] temp = new ComplexToken
                [((Complex[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new ComplexToken(((Complex[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if (object instanceof int[]) {
            IntToken[] temp = new IntToken[((int[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new IntToken(((int[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if (object instanceof long[]) {
            LongToken[] temp = new LongToken[((long[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new LongToken(((long[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if (object instanceof boolean[]) {
            BooleanToken[] temp = new BooleanToken[((long[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new BooleanToken(((boolean[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if (object instanceof String[]) {
            StringToken[] temp = new StringToken[((String[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new StringToken(((String[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if (object instanceof FixPoint[]) {
            // Create back an ArrayToken containing FixTokens
            FixToken[] temp = new FixToken[((FixPoint[])object).length];
            for (int j = 0; j < temp.length; j++) {
                temp[j] = new FixToken((FixPoint)((FixPoint[])object)[j]);
            }
            retval = new ArrayToken(temp);
        } else if(object instanceof Object) {
            // Package into an ObjectToken.
            retval = new ObjectToken(object);
        }
        return retval;
    }
}

