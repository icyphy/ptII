/* ASTPtFunctionalIfNode represents method calls on other Tokens and functional
   if-then else (?:) constructs.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
import ptolemy.data.BooleanToken;
import java.lang.reflect.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtFunctionalIfNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents functional if-then-else
nodes.
<p>
A functional if-then-else if of the form booleanToken ? token : token
The token returned depends on the value of the boolean.
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtFunctionalIfNode extends ASTPtRootNode {

    /** Resolves the Token to be stored in the node. When this
     *  method is called by resolveTree, the tokens in each of the children
     *  have been resolved. This method is concerned with evaluating
     *  both the value and type of the ptToken to be stored.
     *  @return The ptolemy.data.Token to be stored in this node.
     *  @exception IllegalArgumentException If an error occurs
     *  trying to evaluate the PtToken type and/or value to be stored.
     */
    protected ptolemy.data.Token _resolveNode()
            throws IllegalArgumentException {
        int num = jjtGetNumChildren();
        // A functional-if node MUST have three children in parse tree, the
        // first of which is of type BooleanToken.
        ptolemy.data.Token test = childTokens[0];
        if ( !(test instanceof BooleanToken)) {
            throw new IllegalArgumentException(
                    "Functional-If must branch on a boolean: " +
                    test.toString());
        } else if ( num != 3) {
            throw new IllegalArgumentException(
                    "Functional-If must must have three children: " +
                    test.toString());
        }
        // construct appears ok
        boolean value = ((BooleanToken)test).booleanValue();
        if (value) {
            return childTokens[1];
        } else {
            return childTokens[2];
        }
    }

    public ASTPtFunctionalIfNode(int id) {
        super(id);
    }

    public ASTPtFunctionalIfNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtFunctionalIfNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtFunctionalIfNode(p, id);
    }
}
