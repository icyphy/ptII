/* ASTPtProductNode represent product(*,/,%) nodes in the parse tree

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

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtProductNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents product(*,/,%) nodes in
the parse tree.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtProductNode extends ASTPtRootNode {

    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
        int num = jjtGetNumChildren();
        if (num == 1) {
            return _childTokens[0];
        }
        ptolemy.data.Token result = _childTokens[0];
        String op = "";
        int i = 1;
        for (i = 1; i < num; i++) {
            // When start using 1.2 will change this
            // remove from the front, add to the back
            Token x = (Token)_lexicalTokens.removeFirst();
            _lexicalTokens.add(x); // so that tree can be reparsed
            op = x.image;
            if (op.compareTo("*") == 0) {
                result = result.multiply(_childTokens[i]);
            } else if (op.compareTo("/") == 0) {
                result = result.divide(_childTokens[i]);
            } else if (op.compareTo("%") == 0) {
                result = result.modulo(_childTokens[i]);
            } else {
                throw new InternalErrorException(
                        "Invalid concatenator in term() production, " +
                        "check parser");
            }
        }
        return result;
    }

    public ASTPtProductNode(int id) {
        super(id);
    }

    public ASTPtProductNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtProductNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtProductNode(p, id);
    }
}
