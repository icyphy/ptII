/* UnitExpr that will contain UnitTerms.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
 */
package ptolemy.moml.unit;

import java.util.Iterator;
import java.util.Vector;

import ptolemy.actor.IOPort;

///////////////////////////////////////////////////////////////////
//// UnitExpr

/** A UnitExpr contains UnitTerms.
 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public class UnitExpr implements UnitPresentation {
    /** Construct an empty (i.e. no UnitTerms) UnitExpr.
     *
     */
    public UnitExpr() {
    }

    /** Construct a UnitTerm from an IOPort.
     * The constructed UnitExpr will have one UnitTerm and it will be
     * a variable with the name being that of the port.
     * @param ioPort The IOPort.
     */
    public UnitExpr(IOPort ioPort) {
        UnitTerm uTerm = new UnitTerm();
        uTerm.setVariable(ioPort.getContainer().getName() + "."
                + ioPort.getName());
        _uTerms.add(uTerm);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a UnitTerm to the expression.
     * @param uTerm The UnitTerm.
     */
    public void addUnitTerm(UnitTerm uTerm) {
        _uTerms.add(uTerm);
    }

    /** Create a copy of this UnitExpr.
     * @return The new UnitExpr.
     */
    public UnitExpr copy() {
        UnitExpr retv = new UnitExpr();
        Vector newUTerms = new Vector();

        for (int i = 0; i < _uTerms.size(); i++) {
            UnitTerm term = (UnitTerm) _uTerms.elementAt(i);
            newUTerms.add(term.copy());
        }

        retv._setUTerms(newUTerms);
        return retv;
    }

    /** The expression of the UnitExpr that is commonly used by humans.
     * @see ptolemy.moml.unit.UnitPresentation#descriptiveForm()
     */
    @Override
    public String descriptiveForm() {
        Iterator iter = _uTerms.iterator();
        StringBuffer retv = new StringBuffer();
        if (iter.hasNext()) {
            String descriptiveForm = ((UnitTerm) iter.next()).descriptiveForm();
            // Coverity: UnitTerm.descriptiveForm() could return null.
            if (descriptiveForm != null) {
                retv.append(descriptiveForm);
            }
        }

        while (iter.hasNext()) {
            retv.append(" " + ((UnitTerm) iter.next()).descriptiveForm());
        }

        return retv.toString();
    }

    /** If this UnitExpr has one term and it is a Unit then return that Unit.
     * @return The Unit if there is a single UnitTerm, and it is a Unit, null
     * otherwise.
     */
    public Unit getSingleUnit() {
        if (_uTerms.size() == 1 && ((UnitTerm) _uTerms.elementAt(0)).isUnit()) {
            return ((UnitTerm) _uTerms.elementAt(0)).getUnit();
        }

        return null;
    }

    /** Get the UnitTerms in this UnitExpr.
     * @return The UnitTerms.
     */
    public Vector getUTerms() {
        return _uTerms;
    }

    /** Create a new UnitExpr that is the inverse of this UnitExpr.
     * @return The inverse of this UnitExpr.
     */
    public UnitExpr invert() {
        UnitExpr retv = new UnitExpr();
        Vector myUTerms = new Vector();

        for (int i = 0; i < _uTerms.size(); i++) {
            myUTerms.add(((UnitTerm) _uTerms.elementAt(i)).invert());
        }

        retv._setUTerms(myUTerms);
        return retv;
    }

    /**
     * Reduce a UnitExpr to produce a UnitExpr that has at most one Unit. Any
     * embedded UnitExpr is first transformed so that all embedded UnitExprs
     * are replaced with Units. This intermediate result is a mixture of Units
     * and variables. The Units are then replaced with their product. The
     * result is a single Unit and all of the original variables.
     * @return The reduced UnitExpr.
     */
    public UnitExpr reduce() {
        _flatten();

        Unit reductionUnit = UnitLibrary.Identity.copy();
        Vector newUTerms = new Vector();

        for (int i = 0; i < _uTerms.size(); i++) {
            UnitTerm unitTerm = (UnitTerm) _uTerms.elementAt(i);

            if (unitTerm.isUnit()) {
                reductionUnit = reductionUnit.multiplyBy(unitTerm.getUnit()
                        .pow(unitTerm.getExponent()));
            } else {
                newUTerms.add(unitTerm);
            }
        }

        newUTerms.add(new UnitTerm(reductionUnit));

        UnitExpr retv = new UnitExpr();
        retv._setUTerms(newUTerms);
        return retv;
    }

    @Override
    public String toString() {
        StringBuffer retv = new StringBuffer("UnitExpr:[");

        if (_uTerms.size() > 0) {
            retv.append(((UnitTerm) _uTerms.elementAt(0)).toString());

            for (int i = 1; i < _uTerms.size(); i++) {
                retv.append(" " + ((UnitTerm) _uTerms.elementAt(i)).toString());
            }
        }

        retv.append("]");
        return retv.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    protected void _setUTerms(Vector uTerms) {
        _uTerms = uTerms;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void _flatten() {
        if (_isFlat) {
            return;
        }

        Vector newUTerms = new Vector();

        for (int i = 0; i < _uTerms.size(); i++) {
            UnitTerm unitTerm = (UnitTerm) _uTerms.elementAt(i);

            if (unitTerm.isUnitExpr()) {
                UnitExpr uExpr = unitTerm.getUnitExpr();

                if (uExpr != null) {
                    uExpr._flatten();
                    newUTerms.addAll(uExpr.getUTerms());
                }
            } else {
                newUTerms.add(unitTerm);
            }
        }

        _isFlat = true;
        _setUTerms(newUTerms);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    boolean _isFlat = false;

    Vector _uTerms = new Vector();
}
