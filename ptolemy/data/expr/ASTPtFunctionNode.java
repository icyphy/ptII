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
import java.lang.Math;                /* Needed for javadoc */
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

}

