/* ASTPtMethodCallNode represents method calls on other Tokens and functional
   if-then else (?:) constructs.

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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.Token;
import ptolemy.data.DoubleToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.ArrayToken;
import ptolemy.data.FixToken;
import ptolemy.data.FixMatrixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.LongToken;
import ptolemy.data.LongMatrixToken;
import ptolemy.data.StringToken;

import ptolemy.data.BooleanToken;
import ptolemy.math.Complex;
import ptolemy.math.ComplexMatrixMath;
import ptolemy.math.DoubleMatrixMath;
import ptolemy.math.IntegerMatrixMath;
import ptolemy.math.FixPoint;

import ptolemy.kernel.util.*;
import java.lang.reflect.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtMethodCallNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents method call nodes
in the parse tree.
<p>
To allow extension of the parser capabilities without modifying
the kernel code, method calls on Tokens are supported with the following
syntax  (token).methodName(comma separated arguments).
<p>
Method arguments are processed as described in
{@link ASTPtFunctionNode}. However, to allow element-by-element method
calls on ArrayTokens, the following sequence is followed here to find
a method to execute:
<ul>
<li>Look for a method with tokens as supplied by PtParser.</li>
<li>If that fails, convert all instances of ArrayToken to Token[] and
look again, element-by-element.</li>
<li>If that fails, convert all method arguments to their underlying java
types and try again.</li>
<li>Finally, if the above fails, convert the method object Token to
its underlying java type and try again.</li>
</ul>
<p>

@author Neil Smyth, University of California;
@author Zoltan Kemenczy, Research in Motion Limited
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtMethodCallNode extends ASTPtRootNode {
    /** Need to store the method name of the method call.
     */
    protected String _methodName;

    public ASTPtMethodCallNode(int id) {
        super(id);
    }

    public ASTPtMethodCallNode(PtParser p, int id) {
        super(p, id);
    }

    public void jjtClose() {
        if (_children != null) {
            _children.trimToSize();
        }
        // We cannot assume anything about a method call.
        _isConstant = false;
    }

    public static Node jjtCreate(int id) {
        return new ASTPtMethodCallNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtMethodCallNode(p, id);
    }

    /** Resolves the Token to be stored in the node. When this
     *  method is called by resolveTree, the tokens in each of the children
     *  have been resolved. This method is concerned with evaluating
     *  both the value and type of the ptToken to be stored.
     *  @return The ptolemy.data.Token to be stored in this node.
     *  @exception IllegalArgumentException If an error occurs
     *  trying to evaluate the PtToken type and/or value to be stored.
     */
    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {

        int num = jjtGetNumChildren();

        // The first child is the token on which to invoke the method.
	Class[] argTypes = new Class[num];
	Object[] argValues = new Object[num];

	for (int i = 0; i < num; i++) {
            argValues[i] = (ptolemy.data.Token)_childTokens[i];
            argTypes[i] = argValues[i].getClass();
        }

        // First try to find a signature using argument token values.
        // FIXME: Since most XXXToken methods take Tokens as argument types,
        // "constructed" methods (where actual Array- or MatrixToken
        // arguments are reduced to Scalars element-by-element)
        // will often be first attemted here as "real" methods (due to
        // polymporphicGetMethod() finding Token as a superclass of Array-
        // and MatrixTokens) and will throw
        // InvocationTargetExceptions. For "constructed" methods
        // to have a chance, we catch and ignore IllegalActionExceptions
        // here - see example in javadoc above (zk).
        Object result = null;
	StringBuffer sb = null;         // cache exception messages
        try {
            result = CachedMethod.findAndRunMethod
                (_methodName, argTypes, argValues, CachedMethod.METHOD);
        } catch (IllegalActionException ex) {
            if (sb == null) sb = new StringBuffer();
            sb.append("Failed: "+ex.toString());
        };

        if (result == null) {
            // Convert ArrayTokens to Token[]s but otherwise
            // keep all other arguments as Tokens
            boolean anyArray = false;
            for (int i = 0; i < num; i++) {
                ptolemy.data.Token child = _childTokens[i];
                if (child instanceof ArrayToken) {
                    anyArray = true;
                    argValues[i] = ((ArrayToken)child).arrayValue();
                    argTypes[i] = argValues[i].getClass();
                }
            }
            if (anyArray) {
                try {
                    result = CachedMethod.findAndRunMethod
                        (_methodName, argTypes, argValues,
                         CachedMethod.METHOD);
                } catch (IllegalActionException ex) {
                    if (sb == null) sb = new StringBuffer();
                    sb.append("Failed: "+ex.toString());
                }
            }
        }
        if (result == null) {
            // Convert token types to corresponding java types
            // except the destination class (arg 0)
            argValues[0] = _childTokens[0];
            argTypes[0] = argValues[0].getClass();
            for (int i = 1; i < num; i++) {
                ptolemy.data.Token child = _childTokens[i];
                Object[] javaArg = 
                     ASTPtFunctionNode.convertTokenToJavaType(child);
                argValues[i] = javaArg[0];
                argTypes[i] = (Class)javaArg[1];                
            }
            try {
                result = CachedMethod.findAndRunMethod
                    (_methodName, argTypes, argValues, CachedMethod.METHOD);
            } catch (IllegalActionException ex) {
                if (sb == null) sb = new StringBuffer();
                sb.append("Failed: "+ex.toString());
            }
        }

        // If result is still null and arg 0 is an ArrayToken, then try
        // Token[] (give argument dimension reduction a chance with
        // arguments already converted to java types)
        if (result == null && argValues[0] instanceof ArrayToken) {
            argValues[0] = ((ArrayToken)argValues[0]).arrayValue();
            argTypes[0] = argValues[0].getClass();
            result = CachedMethod.findAndRunMethod
                (_methodName, argTypes, argValues, CachedMethod.METHOD);
        }

        // If result is still null, then try converting
        // arg 0 to its java type... 
        if (result == null ) {
            ptolemy.data.Token child = _childTokens[0];
            Object[] javaArg = 
                ASTPtFunctionNode.convertTokenToJavaType(child);
            argValues[0] = javaArg[0];
            argTypes[0] = (Class)javaArg[1];                
            result = CachedMethod.findAndRunMethod
                (_methodName, argTypes, argValues, CachedMethod.METHOD);
        }

        if (result != null) {
            ptolemy.data.Token retval = 
                ASTPtFunctionNode.convertJavaTypeToToken(result);
            if (retval == null) {
                throw new IllegalActionException
                    ("Result of method call is not of a supported type: "
                     + result.getClass().toString() + " - supported types "
                     + "are: boolean, complex, fixpoint, double, int, "
                     + "long, String, and ptolemy.data.Token.");
            }
            return retval;
        }
        // If we reach this point it means the function was not found on
	// the search path.
        if (sb == null) sb = new StringBuffer();
	for (int i = 1; i < num; i++) {
	    if (i == 1) {
		sb.append(argValues[i].toString());
	    } else {
		sb.append(", " + argValues[i].toString());
	    }
	}
	throw new IllegalActionException
            ("No matching method " + _methodName
             + "( " + sb + " ).");
    }
}
