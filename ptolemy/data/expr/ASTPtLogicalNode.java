/* ASTPtLogicalNode represent logical operator(&&, ||) nodes in the parse tree

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtLogicalNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents logical operator(&&, ||)
nodes in the parse tree.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtLogicalNode extends ASTPtRootNode {

    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
        int num = jjtGetNumChildren();
        if (num == 1) {
            return _childTokens[0];
        }
        boolean values[] = new boolean[num];
        int i = 0;
        for ( i = 0; i < num; i++ ) {
            if (!(_childTokens[i] instanceof BooleanToken)) {
                throw new IllegalActionException("Cannot perform logical "
                        + "operation on " + _childTokens[i].getClass());
            }
            values[i] =
                    ((ptolemy.data.BooleanToken)_childTokens[i]).booleanValue();
        }
        boolean result = values[0];
        for (i = 0; i < _lexicalTokens.size(); i++) {
            Token x = (Token)_lexicalTokens.removeFirst();
            // need to reinsert at end if want to reParse tree
            _lexicalTokens.add(x);
            if ( x.image.equalsIgnoreCase("&&") ) {
                result = (result && values[i+1]);
            } else if ( x.image.equalsIgnoreCase("||") ) {
                result = (result || values[i+1]);
            } else {
                throw new IllegalActionException("operator on booleans: " +
                        x.image + " are illegal, check parse tree");
            }
        }
        return new ptolemy.data.BooleanToken(result);
    }

    public ASTPtLogicalNode(int id) {
        super(id);
    }

    public ASTPtLogicalNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtLogicalNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtLogicalNode(p, id);
    }
}
