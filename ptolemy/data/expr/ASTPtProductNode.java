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

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// ASTPtProductNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents product(*,/,%) nodes in
the parse tree.

@author Neil Smyth, Bart Kienhuis
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtProductNode extends ASTPtRootNode {

    private LinkedList _numbers = null;
    private LinkedList _tokens = null;

    protected ptolemy.data.Token _resolveNode()
            throws IllegalActionException {
        int num = jjtGetNumChildren();
        if (num == 1) {
            return _childTokens[0];
        }

        ptolemy.data.Token result = null;
        String op = "";
        String preOp = "";
        int i = 1;

        // Create a linked list from the child tokens
        _numbers = new LinkedList(Arrays.asList(_childTokens));

        // Create a local copy of the _lexicalTokens. This 
        // allows use to manipulate _tokens, while the list
        // _lexicalTokens remains the same, allowing for
        // reevaluation of the expression when needed.
        _tokens = new LinkedList( _lexicalTokens );

        // First, resolve any 'to the power' expressions.
        ListIterator itr = _tokens.listIterator(0);
        int index = 0;
        while( itr.hasNext() ) {
            Token u = (Token)itr.next();
            String opr = u.image;
            if (opr.compareTo("^") == 0) {
                // Yep resolve it.
                itr.remove();
                int times = 1;
                try {
                    times = 
                    ((ptolemy.data.ScalarToken)
                            _numbers.get(index+1)).intValue();
                } catch (Exception e) {
                    throw new IllegalActionException(
                            "Only integral power numbers (e.g. 10^3) " +
                            "are allowed. PLese check expression and use " +
                            "pow(10,3.5) instead to express non-integer " +
                            "powers.");
                }
                ptolemy.data.Token base = (ptolemy.data.Token)
                    _numbers.get(index);
                ptolemy.data.Token multi = base;
                for ( int k = 0; k<times-1; k++ ) {
                    base = base.multiply(multi);
                }
                // Remove base and multiplier from the number list
                _numbers.remove(index+1);
                // and replace it with the new calculated base
                _numbers.set(index,base);
            }
            index++;
        }


        // Check if the expression was only a 'power' expression
        if ( _numbers.size() == 1 ) {             
            return (ptolemy.data.Token) _numbers.get(0);           
        } else {
            
            // Resolve the rest of the expression.
            result = _childTokens[0];
            
            itr = _tokens.listIterator(0);
            index = 1;
            while( itr.hasNext() ) {

                Token x = (Token)itr.next();
                op = x.image;
                        
                ptolemy.data.Token base = (ptolemy.data.Token)
                    _numbers.get(index);
                
                if (op.compareTo("*") == 0) {
                    result = result.multiply(base);
                } else if (op.compareTo("/") == 0) {
                    result = result.divide(base);
                } else if (op.compareTo("%") == 0) {
                    result = result.modulo(base);
                } else {
                    throw new InternalErrorException(
                            "Invalid concatenator in term() production, " +
                            "check parser");
                }
                index++;
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

    /** Debug function. */
    private void _show() {

        System.out.println(" ----------------------------- " );
        Iterator itr = _tokens.iterator();
        while( itr.hasNext() ) {
            Token u = (Token)itr.next();
            String opr = u.image;
            System.out.print(" " + opr );
        }

        itr = _numbers.iterator();
        while( itr.hasNext() ) {
            ptolemy.data.Token n = (ptolemy.data.Token)itr.next();
            System.out.print(" " + n.toString() );
        }
        
        System.out.println(" \n============================= " );
    }
}
