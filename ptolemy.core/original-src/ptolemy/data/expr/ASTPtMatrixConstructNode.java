/* ASTPtMatrixConstructNode represents matrix construction in the parse tree.

 Copyright (c) 1998-2014 The Regents of the University of California and
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


 Created : May 1998

 */
package ptolemy.data.expr;

import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtMatrixConstructNode

/**
 The parse tree created from the expression string consists of a
 hierarchy of node objects. This class represents matrix construction using
 Matlab like expressions.

 @author Xiaojun Liu
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.data.expr.ASTPtRootNode
 @see ptolemy.data.expr.PtParser
 @see ptolemy.data.Token
 */
public class ASTPtMatrixConstructNode extends ASTPtRootNode {
    public ASTPtMatrixConstructNode(int id) {
        super(id);
    }

    public ASTPtMatrixConstructNode(PtParser p, int id) {
        super(p, id);
    }

    public int getColumnCount() {
        return _nColumns;
    }

    public int getForm() {
        return _form;
    }

    public int getRowCount() {
        return _nRows;
    }

    /** Traverse this node with the given visitor.
     */
    @Override
    public void visit(ParseTreeVisitor visitor) throws IllegalActionException {
        visitor.visitMatrixConstructNode(this);
    }

    /** The number of rows of the matrix construction.
     */
    protected int _nRows;

    /** The number of columns of the matrix construction.
     */
    protected int _nColumns;

    /** The form of the matrix construction.
     *  _form is 1 when the matrix construction gives all elements.
     *  _form is 2 when using regularly spaced vector construction.
     */
    protected int _form;
}
