/* ASTPtMethodCallNode represents method calls on other Tokens and functional
   if-then else (?:) constructs.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.Token;
import ptolemy.data.DoubleToken;
import ptolemy.data.FixToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.BooleanToken;
import ptolemy.math.Complex;
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
Note that the arguments to the method, and its returned value, must all
be of type ptolemy.data.Token
<p>
@author Neil Smyth
@version $Id$
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
        // Note that all the arguments to the method call must be
        // ptTokens. Also the argument types used in reflection must
        // correspond EXACTLY to those in the method declaration.
        // thus cannot just do arg.getClass() as this would
        // e.g. return ptolemy.data.DoubleToken, not ptolemy.data.Token
        Class[] argTypes = new Class[num - 1];
        Object[] argValues = new Object[num - 1];
        try {
            for (int i = 1; i < num; i++) {
                argValues[i-1] = (ptolemy.data.Token)_childTokens[i];
                argTypes[i-1] = Class.forName("ptolemy.data.Token");
            }
            Class destTokenClass = _childTokens[0].getClass();
	    Method m = null;
	    // first try to find a method whose arguments are all of type
	    // ptolemy.data.Token
	    try {
		m = destTokenClass.getMethod(_methodName, argTypes);
	    } catch (java.lang.NoSuchMethodException ex) {
		// ignore
	    }
	    if (m == null) {
		for (int i = 0; i < num - 1; i++) {
		    ptolemy.data.Token child = _childTokens[i + 1];
		    if (child instanceof DoubleToken) {
			argValues[i] =
			        new Double(((DoubleToken)child).doubleValue());
			argTypes[i] = Double.TYPE;
		    } else if (child instanceof IntToken) {
			argValues[i] =
			        new Integer(((IntToken)child).intValue());
			argTypes[i] = Integer.TYPE;
		    } else if (child instanceof LongToken) {
			argValues[i] =
			        new Long(((LongToken)child).longValue());
			argTypes[i] = Long.TYPE;
		    } else if (child instanceof StringToken) {
			argValues[i] =
			        new String(((StringToken)child).stringValue());
			argTypes[i] = argValues[i].getClass();
		    } else if (child instanceof BooleanToken) {
			argValues[i] =
			    new Boolean(((BooleanToken)child).booleanValue());
			argTypes[i] = Boolean.TYPE;
		    } else if (child instanceof ComplexToken) {
			argValues[i] = ((ComplexToken)child).complexValue();
			argTypes[i] = argValues[i].getClass();
		    } else if (child instanceof FixToken) {
			argValues[i] = ((FixToken)child).fixValue();
			argTypes[i] = argValues[i].getClass();
		    } else {
			argValues[i] = child;
			argTypes[i] = argValues[i].getClass();
		    }
		}
		m = destTokenClass.getMethod(_methodName, argTypes);
	    }
            Object result = m.invoke(_childTokens[0], argValues);
            // Method call can only return ptolemy.data.Token, we want?
            if (result instanceof ptolemy.data.Token) {
                return (ptolemy.data.Token)result;
            } else if (result instanceof Double) {
		return new DoubleToken(((Double)result).doubleValue());
	    } else if (result instanceof Integer) {
		return new IntToken(((Integer)result).intValue());
	    } else if (result instanceof Long) {
		return new LongToken(((Long)result).longValue());
	    } else if (result instanceof String) {
		return new StringToken((String)result);
	    } else if (result instanceof Boolean) {
		return new BooleanToken(((Boolean)result).booleanValue());
	    } else if (result instanceof Complex) {
		return new ComplexToken((Complex)result);
	    } else if (result instanceof FixPoint) {
		return new FixToken((FixPoint)result);
	    } else {
                throw new IllegalActionException(
                        "Result of method call is not of a supported type: "
			+ result.getClass().toString() + " - supported types "
			+ "are: boolean, complex, fixpoint, double, int, "
			+ "long, String, and ptolemy.data.Token.");
            }
        } catch (java.lang.NoSuchMethodException ex) {
            // The detailed message of the caught exception is included in
            // the generated exception message.
            throw new IllegalActionException(_generateExceptionMessage(ex));
        } catch (java.lang.IllegalAccessException ex) {
            throw new IllegalActionException(_generateExceptionMessage(ex));
        } catch (java.lang.reflect.InvocationTargetException ex) {
            throw new IllegalActionException(_generateExceptionMessage(ex));
        } catch (java.lang.ClassNotFoundException ex) {
            throw new IllegalActionException(_generateExceptionMessage(ex));
        }
    }

    /* Create a new exception message based on the caught argument exception.
     */
    private String _generateExceptionMessage(Exception ex) {
        StringBuffer sb = new StringBuffer();
        int num = jjtGetNumChildren();
        for (int i = 0; i < (num-1); i++) {
            if (i == 0) {
                sb.append(_childTokens[i].toString());
            } else {
                sb.append(", " + _childTokens[i].toString());
            }
        }
        return "Method " + _methodName + "() cannot" +
            " be executed with given arguments, on ptTokens of " +
            "type " + _childTokens[0].getClass().getName() + ": " +
            ex.getMessage();
    }

}
