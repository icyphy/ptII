/* An abstraction for Unit constraints.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

                                    PT_COPYRIGHT_VERSION_3
                                    COPYRIGHTENDKEY
@Pt.ProposedRating Red (rowland@eecs.berkeley.edu)
@Pt.AcceptedRating Red (rowland@eecs.berkeley.edu)
*/

package ptolemy.data.unit;

import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// UnitConstraint
/**
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public abstract class UnitConstraint {

    /**
     * @param string
     */
    public UnitConstraint(String string) {

        // TODO Auto-generated constructor stub
    }

    /**
     * @param lhs
     * @param operator
     * @param rhs
     */
    public UnitConstraint(UnitExpr lhs, String operator, UnitExpr rhs) {
        _lhs = lhs;
        _operator = operator;
        _rhs = rhs;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* (non-Javadoc)
     * @see ptolemy.data.unit.UnitPresentation#commonDesc()
     */
    public String descriptiveForm() {
        return _lhs.descriptiveForm() + _operator + _rhs.descriptiveForm();
    }

    /** Get the left hand side.
     * @return The left hand side.
     */
    public UnitExpr getLhs() {
        return _lhs;
    }

    /**
     * @return The operator.
    */
    public String getOperator() {
        return _operator;
    }

    /** Get the right hand side.
     * @return The right hand side.
     */
    public UnitExpr getRhs() {
        return _rhs;
    }

    /** Get the source of this equation.
     * @return The source of this equation.
     */
    public NamedObj getSource() {
        return _source;
    }

    public void setLhs(UnitExpr expr) {
        _lhs = expr;
    }

    public void setRhs(UnitExpr expr) {
        _rhs = expr;
    }

    public void setSource(NamedObj source) {
        _source = source;
    }

    public String toString() {
        return _lhs.toString() + _operator + _rhs.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    UnitExpr _lhs, _rhs;
    String _operator;
    NamedObj _source = null;
}
