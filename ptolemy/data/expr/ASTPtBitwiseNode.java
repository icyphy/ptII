/* ASTPtBitwiseNode represent bitwise operator(&, |, ^) nodes in the parse tree

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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating none

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtBitwiseNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents bitwise operator(&, |, ^)
nodes in the parse tree.

@author Neil Smyth
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtBitwiseNode extends ASTPtRootNode {

    protected ptolemy.data.Token _resolveNode() 
            throws IllegalArgumentException {
        int num = jjtGetNumChildren();
        if (num == 1) {
            return childTokens[0];
        }
        if (jjtGetNumChildren() != ( _lexicalTokens.size() +1) ) {
            String str = "Not enough/too many operators for number";
            throw new IllegalArgumentException(str + " of children");
        }
        ptolemy.data.Token result = childTokens[0];
        boolean isBoolean = false;
        if (result instanceof BooleanToken) {
            isBoolean = true;
        }
        String op = "";
        int i = 1;
        try {
            for ( i = 1; i<num; i++ ) {
                // need to take the top object, AND put it back at the
                // end so that the tree can be reparsed
                Object x = _lexicalTokens.take();
                _lexicalTokens.insertLast(x);
                op = ((Token)x).image;
                if (isBoolean) {
                    if ( !(childTokens[i] instanceof BooleanToken) ) {
                        throw new Exception();
                    }
                    boolean arg1 = ((BooleanToken)result).getValue();
                    boolean arg2 = ((BooleanToken)childTokens[i]).getValue();
                    if (op.equals("&")) {
                        result = new BooleanToken(arg1 & arg2);
                    } else if (op.equals("|")) {
                        result = new BooleanToken(arg1 | arg2);
                    } else {
                        throw new Exception();
                    }
                } else {
                    // must be applying bitwise operation between integer types
                    // integer types are long and int
                    if ( !((result instanceof IntToken) ||
                            (childTokens[i] instanceof LongToken)) ) {
                        throw new IllegalArgumentException();
                    }
                    if ( (result instanceof LongToken) ||
                            (childTokens[i] instanceof LongToken) ) {
                        long arg1 = ((ScalarToken)result).longValue();
                        long arg2 = ((ScalarToken)childTokens[i]).longValue();
                        if (op.equals("&")) {
                            result = new LongToken(arg1 & arg2);
                        } else if (op.equals("|")) {
                            result = new LongToken(arg1 | arg2);
                        } else if (op.equals("^")) {
                            result = new LongToken(arg1 ^ arg2);
                        } else {
                            throw new IllegalArgumentException();
                        }
                    } else {
                        int arg1 = ((ScalarToken)result).intValue();
                        int arg2 = ((ScalarToken)childTokens[i]).intValue();
                        if (op.equals("&")) {
                            result = new IntToken(arg1 & arg2);
                        } else if (op.equals("|")) {
                            result = new IntToken(arg1 | arg2);
                        } else if (op.equals("^")) {
                            result = new IntToken(arg1 ^ arg2);
                        } else {
                            throw new Exception();
                        }
                    }
                }
            }
            return result;
        } catch (Exception ex) {
            String str = "Invalid operation " + op + " between ";
            str = str + result.getClass().getName() + " and ";
            str = str + childTokens[i].getClass().getName();
            throw new IllegalArgumentException(str);
        }
    }


    public ASTPtBitwiseNode(int id) {
        super(id);
    }

    public ASTPtBitwiseNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtBitwiseNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtBitwiseNode(p, id);
    }
}
