/* ASTPtMatrixConstructNode represents matrix construction in the parse tree.

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
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.math.Complex;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtMatrixConstructNode
/**
The parse tree created from the expression string consists of a
hierarchy of node objects. This class represents matrix construction using
Matlab like expressions.

@author Xiaojun Liu
@version $Id$
@see ptolemy.data.expr.ASTPtRootNode
@see ptolemy.data.expr.PtParser
@see ptolemy.data.Token
*/
public class ASTPtMatrixConstructNode extends ASTPtRootNode {

    protected ptolemy.data.Token _resolveNode() {
        /* first find the type of the matrix token */
        int nChildren = jjtGetNumChildren();
        int i;
        Object[] termTypes = new Object[nChildren];
        for (i = 0; i < nChildren; ++i) {
            termTypes[i] = childTokens[i].getClass();
        }
        Class mtype = (Class)TypeLattice.lattice().leastUpperBound(termTypes);
        if (mtype == null || mtype == Void.TYPE) {
            throw new IllegalExpressionException("Cannot resolve type for "
                    + "matrix construction: ");
            // FIXME: should give subexpression here
        }
        try {
            if (mtype == BooleanToken.class) {
                boolean[][] val = new boolean[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    val[i/_nColumns][i%_nColumns] = 
                            ((BooleanToken)BooleanToken.convert(childTokens[i])).booleanValue();
                }
                _ptToken = new BooleanMatrixToken(val);
            } else if (mtype == IntToken.class) {
                int[][] val = new int[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    val[i/_nColumns][i%_nColumns] = ((IntToken)IntToken.convert(childTokens[i])).intValue();
                }
                _ptToken = new IntMatrixToken(val);
            } else if (mtype == LongToken.class) {
                long[][] val = new long[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    val[i/_nColumns][i%_nColumns] = ((LongToken)LongToken.convert(childTokens[i])).longValue();
                }
                _ptToken = new LongMatrixToken(val);
            } else if (mtype == DoubleToken.class) {
                double[][] val = new double[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    val[i%_nColumns][i/_nColumns] = ((DoubleToken)DoubleToken.convert(childTokens[i])).doubleValue();
                }
                _ptToken = new DoubleMatrixToken(val);
            } else if (mtype == ComplexToken.class) {
                Complex[][] val = new Complex[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    val[i/_nColumns][i%_nColumns] = ((ComplexToken)ComplexToken.convert(childTokens[i])).complexValue();
                }
                _ptToken = new ComplexMatrixToken(val);
            } else {
                /* The resolved type does not have a corresponding matrix type. */
                throw new IllegalExpressionException("The LUB of the types of the "                     
                        + " terms of matrix construction does not have a "
                        + " corresponding matrix type.");
            }
        } catch (IllegalActionException ex) {
            /* This is thrown by convert(), but should not be thrown since we
               are converting to a LUB. */
            throw new InternalErrorException("PtParser: error when converting "
                    + "term to the type determined by LUB.");
        }
        return _ptToken;
    }

    public ASTPtMatrixConstructNode(int id) {
        super(id);
    }

    public ASTPtMatrixConstructNode(PtParser p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPtMatrixConstructNode(id);
    }

    public static Node jjtCreate(PtParser p, int id) {
        return new ASTPtMatrixConstructNode(p, id);
    }

    protected int _nRows;
    protected int _nColumns;

}
