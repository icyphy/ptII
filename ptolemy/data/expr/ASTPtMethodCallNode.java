/* ASTPtMethodCallNode represents method calls on other Tokens and functional
   if-then else (?:) constructs.

 Copyright (c) 1998 The Regents of the University of California.
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
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
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
    protected String methodName;

    /** Resolves the Token to be stored in the node. When this
     *  method is called by resolveTree, the tokens in each of the children
     *  have been resolved. This method is concerned with evaluating
     *  both the value and type of the ptToken to be stored.
     *  @return The ptolemy.data.Token to be stored in this node.
     *  @exception IllegalArgumentException Thrown when an error occurs
     *  trying to evaluate the PtToken type and/or value to be stored.
     */
    protected ptolemy.data.Token _resolveNode() 
            throws IllegalArgumentException {
        int num = jjtGetNumChildren();
        // if only one child, then must be an enclosing parenthesis
        // i.e. actually no method call.
        if (num == 1) return childTokens[0];

        try {
            if (methodName == null) {
                String msg = "Invalid method call: " + methodName;
                throw new IllegalArgumentException(msg);
            }
            // we have a valid method call!!
            // The first child is the token on which to invoke the method
            // Note that all the arguments to the method call must be
            // ptTokens. Also the argument types used in reflection must
            // correspond EXACTLY to those in the method declaration.
            // thus cannot just do arg.getClass() as this would
            // eg return ptolemy.data.DoubleToken, not ptolemy.data.Token
            Class[] argTypes = new Class[num - 1];
            Object[] argValues = new Object[num - 1];
            try {
                for (int i = 1; i<num; i++) {
                    argValues[i-1] = (ptolemy.data.Token)childTokens[i];
                    if ( !(argValues[i-1] instanceof ptolemy.data.Token) ) {
                        throw new Exception();
                    }
                    argTypes[i-1] = Class.forName("ptolemy.data.Token");
                }
                Class destTokenClass = childTokens[0].getClass();
                Method m = destTokenClass.getMethod(methodName, argTypes);
                Object result = m.invoke(childTokens[0], argValues);
                // Method call can only return ptolemy.data.Token, we want?
                if (result instanceof ptolemy.data.Token) {
                    return (ptolemy.data.Token)result;
                } else {
                    String str = "Method calls on Token must return a Token";
                    throw new IllegalArgumentException(str);
                }
            } catch (Exception ex) {
                StringBuffer sb = new StringBuffer();
                for (int i=0; i<(num-1); i++) {
                    if (i==0) {
                        sb.append(argValues[i].toString());
                    } else {
                        sb.append(", " + argValues[i].toString());
                    }
                }
                String str = "Function " + methodName + "(" + sb + ") cannot";
                str += " be executed with given arguments, on ptTokens of ";
                str += "type " + childTokens[0].getClass().getName() + ": ";
                throw new IllegalArgumentException(str + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }
    
    
    public ASTPtMethodCallNode(int id) {
        super(id);
    }

    public ASTPtMethodCallNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtMethodCallNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtMethodCallNode(p, id);
    }
}
