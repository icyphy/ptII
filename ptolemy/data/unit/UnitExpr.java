/*

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

import java.util.Iterator;
import java.util.Vector;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;

//////////////////////////////////////////////////////////////////////////
//// UnitExpr
/**
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class UnitExpr {

    /**
     *
     */
    public UnitExpr() {
    }

    public UnitExpr(IOPort actorPort) {
        UnitTerm uTerm = new UnitTerm();
        uTerm.setVariable(actorPort.getFullName());
        Vector uTerms = new Vector();
        uTerms.add(uTerm);
        setUTerms(uTerms);
    }
    ///////////////////////////////////////////////////////////////////
    //// public methods ////

    /**
     * @param u
     */
    public void add(UnitTerm u) {
        _uTerms.add(u);
    }

    public String commonDesc() {
        Iterator iter = _uTerms.iterator();
        String retv = ((UnitTerm) (iter.next())).commonExpression();
        while (iter.hasNext()) {
            retv += " " + ((UnitTerm) (iter.next())).commonExpression();
        }
        return retv;
    }

    /** Create a shallow copy of this UnitExpr.
     * @return The new UnitExpr.
     */
    public UnitExpr copy() {
        UnitExpr retv = new UnitExpr();
        Vector newUTerms = new Vector();
        for (int i = 0; i < _uTerms.size(); i++) {
            UnitTerm term = (UnitTerm) (_uTerms.elementAt(i));
            newUTerms.add(term.copy());
        }
        retv.setUTerms(newUTerms);
        return retv;
    }

    public Unit eval(Bindings bindings) {
        Iterator iter = _uTerms.iterator();
        Unit retv = new Unit();
        while (iter.hasNext()) {
            UnitTerm term = (UnitTerm) (iter.next());
            Unit unit = term.eval(bindings);
            if (unit == null) {
                return null;
            }
            retv = retv.multiplyBy(unit);
        }
        return retv;
    }

    public void flatten() {
        if (isFlat()) {
            return;
        }
        Vector newUTerms = new Vector();
        for (int i = 0; i < _uTerms.size(); i++) {
            UnitTerm unitTerm = (UnitTerm) (_uTerms.elementAt(i));
            if (unitTerm.isUnitExpr()) {
                UnitExpr uExpr = null;
                try {
                    uExpr = (UnitExpr) (unitTerm.getUnitExpr());
                } catch (IllegalActionException e) {
                    KernelException.stackTraceToString(e);
                }
                uExpr.flatten();
                newUTerms.addAll(uExpr.getUTerms());
            } else {
                newUTerms.add(unitTerm);
            }
        }
        setFlat(true);
        setUTerms(newUTerms);
    }

    public String getEvaledExpression(Bindings bindings) {
        Iterator iter = _uTerms.iterator();
        String retv = ((UnitTerm) (iter.next())).getEvaledExpression(bindings);
        while (iter.hasNext()) {
            retv += " "
                + ((UnitTerm) (iter.next())).getEvaledExpression(bindings);
        }
        return retv;
    }

    public Vector getUTerms() {
        return _uTerms;
    }

    public UnitExpr invert() {
        UnitExpr retv = new UnitExpr();
        Vector myUTerms = new Vector();
        for (int i = 0; i < _uTerms.size(); i++) {
            myUTerms.add(((UnitTerm) (_uTerms.elementAt(i))).invert());
        }
        retv.setUTerms(myUTerms);
        return retv;
    }

    public boolean isFlat() {
        return _isFlat;
    }

    /**
     * Return true if this UnitExpr contains just one UnitTerm, and it is a
     * variable.
     *
     * @return True if there is one UnitTerm and it is a variable.
     */
    public boolean isSingleUnit() {
        if (_uTerms.size() != 1) {
            return false;
        }
        UnitTerm unitTerm = (UnitTerm) (_uTerms.elementAt(0));
        return unitTerm.isUnit();
    }

    public boolean isSingleVariable() {
        if (_uTerms.size() != 1) {
            return false;
        }
        UnitTerm unitTerm = (UnitTerm) (_uTerms.elementAt(0));
        return unitTerm.isVariable();
    }

    /**
     * Reduce a UnitExpr to produce a UnitExpr that has at most one Unit. Any
     * embedded UnitExpr is first transformed so that all embedded UnitExprs
     * are replaced with Units. This intermediate result is a mixture of Units
     * and variables. The Units are then replaced with their product. The
     * result is a single Unit and all of the original variables.
     */
    public void reduce() {
        flatten();
        boolean reductionHasHappened = false;
        Unit unit = new Unit();
        UnitTerm constantUnitTerm = new UnitTerm();
        constantUnitTerm.setUnit(unit);
        Vector newUTerms = new Vector();
        for (int i = 0; i < _uTerms.size(); i++) {
            UnitTerm unitTerm = (UnitTerm) (_uTerms.elementAt(i));
            if (unitTerm.isUnit()) {
                UnitTerm x = unitTerm.reduce();
                try {
                    constantUnitTerm = constantUnitTerm.multiplyBy(x);
                } catch (IllegalActionException e) {
                    KernelException.stackTraceToString(e);
                }
                reductionHasHappened = true;
            } else {
                newUTerms.add(unitTerm);
            }
        }
        if (reductionHasHappened) {
            newUTerms.add(constantUnitTerm);
        }
        setUTerms(newUTerms);
    }

    public void setFlat(boolean b) {
        _isFlat = b;
    }

    public void setUTerms(Vector vector) {
        _uTerms = vector;
    }

    public String toString() {
        String retv = "UnitExpr(" + _uTerms.size() + "):[";
        Iterator iter = _uTerms.iterator();
        retv += ((UnitTerm) (iter.next())).toString();
        while (iter.hasNext()) {
            retv += " " + ((UnitTerm) (iter.next())).toString();
        }
        retv += "]";
        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables                      ////
    boolean _isFlat = false;
    Vector _uTerms = new Vector();
}
