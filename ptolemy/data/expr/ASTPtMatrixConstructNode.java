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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

Created : May 1998

*/

package ptolemy.data.expr;

import ptolemy.data.*;
import ptolemy.data.type.*;
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

    protected ptolemy.data.Token _resolveNode() throws IllegalActionException {
        /* first find the type of the matrix token */
        int nChildren = jjtGetNumChildren();
        int i;
        ptolemy.data.Token tok = null;
        Type mtype = _elementType();
        if (_form == 1) {
            if (mtype == BaseType.BOOLEAN) {
                boolean[][] val = new boolean[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    tok = BooleanToken.convert(_childTokens[i]);
                    val[i/_nColumns][i%_nColumns] =
                            ((BooleanToken)tok).booleanValue();
                }
                _ptToken = new BooleanMatrixToken(val);
            } else if (mtype == BaseType.INT) {
                int[][] val = new int[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    tok = IntToken.convert(_childTokens[i]);
                    val[i/_nColumns][i%_nColumns] =
                            ((IntToken)tok).intValue();
                }
                _ptToken = new IntMatrixToken(val);
            } else if (mtype == BaseType.LONG) {
                long[][] val = new long[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    tok = LongToken.convert(_childTokens[i]);
                    val[i/_nColumns][i%_nColumns] =
                            ((LongToken)tok).longValue();
                }
                _ptToken = new LongMatrixToken(val);
            } else if (mtype == BaseType.DOUBLE) {
                double[][] val = new double[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    tok = DoubleToken.convert(_childTokens[i]);
                    val[i/_nColumns][i%_nColumns] =
                            ((DoubleToken)tok).doubleValue();
                }
                _ptToken = new DoubleMatrixToken(val);
            } else if (mtype == BaseType.COMPLEX) {
                Complex[][] val = new Complex[_nRows][_nColumns];
                for (i = 0; i < nChildren; ++i) {
                    tok = ComplexToken.convert(_childTokens[i]);
                    val[i/_nColumns][i%_nColumns] =
                            ((ComplexToken)tok).complexValue();
                }
                _ptToken = new ComplexMatrixToken(val);
            } else {
                /* The resolved type does not have a corresponding
                   matrix type. */
                throw new IllegalActionException("The LUB of the types "
                        + "of the terms of matrix construction does not "
                        + "have a corresponding matrix type.");
            }
        } else if (_form == 2) {
            if (mtype == BaseType.INT) {
                _nColumns = _numIntColumns(_childTokens[0], _childTokens[1],
                        _childTokens[2]);
                // Make sure that all following rows have the same number
                // of columns.
                for (i = 1; i < _nRows; ++i) {
                    if (_nColumns != _numIntColumns(_childTokens[3*i],
                            _childTokens[3*i+1], _childTokens[3*i+2])) {
                        throw new IllegalActionException("Matrix "
                                + "should have the same number of columns "
                                + "for all rows.");
                    }
                }
                int[][] val = new int[_nRows][];
                for (i = 0; i < _nRows; ++i) {
                    val[i] = _createIntRow(_childTokens[3*i],
                            _childTokens[3*i+1]);
                }
                _ptToken = new IntMatrixToken(val);
            } else if (mtype == BaseType.LONG) {
                _nColumns = _numLongColumns(_childTokens[0], _childTokens[1],
                        _childTokens[2]);
                // Make sure that all following rows have the same number
                // of columns.
                for (i = 1; i < _nRows; ++i) {
                    if (_nColumns != _numLongColumns(_childTokens[3*i],
                            _childTokens[3*i+1], _childTokens[3*i+2])) {
                        throw new IllegalActionException("Matrix "
                                + "should have the same number of columns "
                                + "for all rows.");
                    }
                }
                long[][] val = new long[_nRows][];
                for (i = 0; i < _nRows; ++i) {
                    val[i] = _createLongRow(_childTokens[3*i],
                            _childTokens[3*i+1]);
                }
                _ptToken = new LongMatrixToken(val);
            } else if (mtype == BaseType.DOUBLE) {
                _nColumns = _numDoubleColumns(_childTokens[0], _childTokens[1],
                        _childTokens[2]);
            // Make sure that all following rows have the same number
            // of columns.
            for (i = 1; i < _nRows; ++i) {
                    if (_nColumns != _numDoubleColumns(_childTokens[3*i],
                            _childTokens[3*i+1], _childTokens[3*i+2])) {
                        throw new IllegalActionException("Matrix "
                                + "should have the same number of columns "
                                + "for all rows.");
                    }
                }
                double[][] val = new double[_nRows][];
                for (i = 0; i < _nRows; ++i) {
                    val[i] = _createDoubleRow(_childTokens[3*i],
                            _childTokens[3*i+1]);
                }
                _ptToken = new DoubleMatrixToken(val);
            } else {
                /* The resolved type does not have a corresponding
                   matrix type. */
                throw new IllegalActionException("The LUB of the types "
                        + "of the terms of a regularly-spaced-vector matrix "
                        + "construction is not supported: " + mtype);
            }
        }
        return _ptToken;
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

    /* Create a row of a matrix using matlab-style regularly-spaced-vector
     * construction when the elements are integers.
     */
    private int[] _createIntRow(ptolemy.data.Token lb,
            ptolemy.data.Token incr) {
        int[] result = new int[_nColumns];
        try {
            int lbv = ((IntToken)IntToken.convert(lb)).intValue();
            int incrv = ((IntToken)IntToken.convert(incr)).intValue();

            for (int i = 0; i < _nColumns; ++i) {
                result[i] = lbv + i*incrv;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("The caller should guarantee that "
                    + "arguments can be converted losslessly to IntToken: "
                    + ex.getMessage());
        }
        return result;
    }

    /* Create a row of a matrix using matlab-style regularly-spaced-vector
     * construction when the elements are long integers.
     */
    private long[] _createLongRow(ptolemy.data.Token lb,
            ptolemy.data.Token incr) {
        long[] result = new long[_nColumns];
        try {
            long lbv = ((LongToken)LongToken.convert(lb)).longValue();
            long incrv = ((LongToken)LongToken.convert(incr)).longValue();

            for (int i = 0; i < _nColumns; ++i) {
                result[i] = lbv + i*incrv;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("The caller should guarantee that "
                    + "arguments can be converted losslessly to LongToken: "
                    + ex.getMessage());
        }
        return result;
    }

    /* Create a row of a matrix using matlab-style regularly-spaced-vector
     * construction when the elements are doubles.
     */
    private double[] _createDoubleRow(ptolemy.data.Token lb,
            ptolemy.data.Token incr) {
        double[] result = new double[_nColumns];
        try {
            double lbv = ((DoubleToken)DoubleToken.convert(lb)).doubleValue();
            double incrv =
                    ((DoubleToken)DoubleToken.convert(incr)).doubleValue();

            for (int i = 0; i < _nColumns; ++i) {
                result[i] = lbv + i*incrv;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("The caller should guarantee that "
                    + "arguments can be converted losslessly to IntToken: "
                    + ex.getMessage());
        }
        return result;
    }

    /* Determine the element type of the matrix, which is the LUB of the term
     * types.
     * @exception IllegalActionException If the element type of the matrix
     *  cannot be resolved.
     */
    private Type _elementType() throws IllegalActionException {
        int nChildren = jjtGetNumChildren();
        int i;
        Object[] termTypes = new Object[nChildren];
        for (i = 0; i < nChildren; ++i) {
            termTypes[i] = _childTokens[i].getType();
        }

        Type mtype = (Type)TypeLattice.lattice().leastUpperBound(termTypes);
        if (mtype == BaseType.NAT) {
            throw new IllegalActionException("Cannot resolve type for "
                    + "matrix construction: ");
        }
        return mtype;
    }

    /* Determine the number of columns resulting from a matlab-style matrix
     * construction [lb:incr:ub], when lb, incr, and ub are compatible with
     * IntTokens.
     */
    private int _numIntColumns(ptolemy.data.Token lb, ptolemy.data.Token incr,
            ptolemy.data.Token ub) {
        int result = 0;
        try {
            int lbv = ((IntToken)IntToken.convert(lb)).intValue();
            int incrv = ((IntToken)IntToken.convert(incr)).intValue();
            int ubv = ((IntToken)IntToken.convert(ub)).intValue();
            if (incrv == 0) {
                result = 0;
            } else if (incrv > 0 && lbv > ubv) {
                result = 0;
            } else if (incrv < 0 && lbv < ubv) {
                result = 0;
            } else {
                result = (ubv - lbv)/incrv + 1;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("The caller should guarantee that "
                    + "arguments can be converted losslessly to IntToken: "
                    + ex.getMessage());
        }
        return result;
    }

    /* Determine the number of columns resulting from a matlab-style matrix
     * construction [lb:incr:ub], when lb, incr, and ub are compatible with
     * LongTokens.
     */
    private int _numLongColumns(ptolemy.data.Token lb, ptolemy.data.Token incr,
            ptolemy.data.Token ub) {
        int result = 0;
        try {
            long lbv = ((LongToken)LongToken.convert(lb)).longValue();
            long incrv = ((LongToken)LongToken.convert(incr)).longValue();
            long ubv = ((LongToken)LongToken.convert(ub)).longValue();
            if (incrv == 0) {
                result = 0;
            } else if (incrv > 0 && lbv > ubv) {
                result = 0;
            } else if (incrv < 0 && lbv < ubv) {
                result = 0;
            } else {
                result = (int)((ubv - lbv)/incrv + 1);
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("The caller should guarantee that "
                    + "arguments can be converted losslessly to LongToken: "
                    + ex.getMessage());
        }
        return result;
    }

    /* Determine the number of columns resulting from a matlab-style matrix
     * construction [lb:incr:ub], when lb, incr, and ub are compatible with
     * DoubleTokens.
     */
    private int _numDoubleColumns(ptolemy.data.Token lb,
            ptolemy.data.Token incr, ptolemy.data.Token ub) {
        int result = 0;
        try {
            double lbv = ((DoubleToken)DoubleToken.convert(lb)).doubleValue();
            double incrv =
                    ((DoubleToken)DoubleToken.convert(incr)).doubleValue();
            double ubv = ((DoubleToken)DoubleToken.convert(ub)).doubleValue();
            if (incrv == 0) {
                result = 0;
            } else if (incrv > 0 && lbv > ubv) {
                result = 0;
            } else if (incrv < 0 && lbv < ubv) {
                result = 0;
            } else {
                result = (int)((ubv - lbv)/incrv + 1);
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException("The caller should guarantee that "
                    + "arguments can be converted losslessly to IntToken: "
                    + ex.getMessage());
        }
        return result;
    }

}
