/* Representation of a Unit

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

import java.util.Vector;

//////////////////////////////////////////////////////////////////////////
//// Unit
/**
Class used to represent a Unit.
A Unit has the form <b>S</b>&ltE1, E2, ..., En&gt where <b>S</b> is the
<i>scale</i> and &ltE1, E2, ..., En&gt is the <i>type</i> of the Unit.
@author Rowland R Johnson
@version $Id$
@since Ptolemy II 3.1
*/
public class Unit implements UnitPresentation {

    /** Create a Unit with no name and the unitless type.
     *
     */
    public Unit() {
        _type = new int[UnitLibrary.getNumCategories()];
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            _type[i] = 0;
        }
        _labels.add("noName" + _noLabelCounter++);
    }

    /** Create a Unit from a BaseUnit.
     * @param bu BaseUnit that provides the basis for this Unit.
     */
    public Unit(BaseUnit bu) {
        this();
        String name = bu.getName();
        setPrimaryLabel(name);
        String spec = bu.getExpression();
        int index = UnitUtilities.getUnitCategoryIndex(name);
        _type[index] = 1;
    }

    /** Create a Unit with a specified name, and the unitless type.
     * @param name Name of the Unit.
     */
    public Unit(String name) {
        this();
        setPrimaryLabel(name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The expression of the Unit that is commonly used by humans.
     * For example, the unit 4.1868E7&lt2, 1, -3, 0, 0&gt will produce the
     * common expression "calorie second^-1".
     * @see ptolemy.data.unit.UnitPresentation#commonExpression()
     */
    public String commonExpression() {
        String retv = null;
        Unit unit = UnitLibrary.getUnit(this);
        if (unit != null) {
            return unit.getPrimaryLabel();
        }
        // Try some alternates of the form xx/yy
        // First see if it is simply an invert
        Unit invert = UnitLibrary.getUnit(invert());
        if (invert != null)
            return invert.getPrimaryLabel() + "^-1";
        // Second see if this is of the form numerator/denominator
        Vector libraryUnits = UnitLibrary.getLibrary();
        for (int i = 0; i < libraryUnits.size(); i++) {
            Unit factor = (Unit) (libraryUnits.elementAt(i));
            Unit numerator = this.multiplyBy(factor);
            Unit xx = UnitLibrary.getUnit(numerator);
            if (xx != null) {
                if (xx != UnitLibrary.Identity)
                    return xx.getPrimaryLabel()
                        + " "
                        + factor.getPrimaryLabel()
                        + "^-1";
                else
                    return factor.getPrimaryLabel() + "^-1";
            }
        }
        for (int i = 0; i < libraryUnits.size(); i++) {
            Unit factor = (Unit) (libraryUnits.elementAt(i));
            Unit remainder = this.divideBy(factor);
            Unit xx = UnitLibrary.getUnit(remainder);
            if (xx != null && xx != UnitLibrary.Identity)
                return factor.getPrimaryLabel() + " " + xx.getPrimaryLabel();
        }
        if (_scaleToBaseUnit == 1.0) {
            int numCats = _type.length;
            String desc = "";
            for (int i = 0; i < numCats; i++) {
                if (_type[i] != 0) {
                    Unit baseUnit = UnitLibrary.getBaseUnit(i);
                    if (_type[i] == 1) {
                        desc += " " + baseUnit.getPrimaryLabel();
                    } else {
                        desc += " "
                            + baseUnit.getPrimaryLabel()
                            + "^"
                            + _type[i];
                    }
                }
            }
            return desc.substring(1);
        }
        // End up here if nothing works, so just return the formal description
        System.out.println("Unit.commonDesc had to use " + toString());
        return toString();
    }

    /** Make a copy of this Unit.
     * @return A new Unit.
     */
    public Unit copy() {
        Unit retv = new Unit();
        retv.setLabels(getLabels());
        int newExponents[] = retv.getType();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            newExponents[i] = _type[i];
        }
        retv.setScale(getScale());
        return retv;
    }

    /** Divide this Unit by the argument.
     * @param divisor The divisor unit.
     * @return This Unit divided by the argument.
     */
    public Unit divideBy(Unit divisor) {
        Unit retv = copy();
        int otherExponents[] = divisor.getType();
        int thisExponents[] = retv.getType();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            thisExponents[i] -= otherExponents[i];
        }
        retv.setType(thisExponents);
        retv.setScale(retv.getScale() / divisor.getScale());
        return retv;
    }

    /** Return True if this Unit equals another Unit.
     * @param otherUnit The Unit to be compared against.
     * @return True if this Unit equals the other Unit.
     */
    public boolean equals(Unit otherUnit) {
        int otherExponents[] = otherUnit.getType();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            if (_type[i] != otherExponents[i]) {
                return false;
            }
        }
        if (_scaleToBaseUnit != otherUnit.getScale()) {
            return false;
        }
        return true;
    }

    /** Get the labels for a Unit.
     * @see ptolemy.data.unit.Unit#getPrimaryLabel()
     * @return The labels.
     */
    public Vector getLabels() {
        return _labels;
    }

    /** Create a String that is the concatenation of all the labels.
     * @return The cancatenation of the labels.
     */
    public String getLabelsString() {
        String retv = null;
        if (_labels.size() > 0) {
            retv = (String) (_labels.elementAt(0));
        } else {
            return "";
        }
        for (int i = 1; i < _labels.size(); i++) {
            retv += (String) (_labels.elementAt(i)) + ",";
        }
        return retv;
    }

    /** Get the primary label of a Unit.
     * A Unit can have more than one label. For example, cm, and centimeter are
     * labels for the same Unit. There always exists a label that is primary.
     * @return The primary label.
     */
    public String getPrimaryLabel() {
        return (String) (_labels.elementAt(0));
    }

    /** Get the scale.
     * @return Scale.
     */
    public double getScale() {
        return _scaleToBaseUnit;
    }

    /** Get the type (represented as a int array) of this Unit.
     * @return The type (represented as a int array) of this Unit.
     */
    public int[] getType() {
        return _type;
    }

    /** Return true if the Unit has the same type as the argument.
     * @param otherUnit
     * @return True if the Unit has the same type as the argument.
     */
    public boolean hasSameType(Unit otherUnit) {
        int otherType[] = otherUnit.getType();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            if (_type[i] != otherType[i]) {
                return false;
            }
        }
        return true;
    }

    /** Invert this Unit.
     * @return The inverse of this Unit.
     */
    public Unit invert() {
        Unit retv = new Unit();
        int otherExponents[] = getType();
        int exponents[] = new int[UnitLibrary.getNumCategories()];
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            exponents[i] = -otherExponents[i];
        }
        retv.setType(exponents);
        retv.setScale(1.0 / getScale());
        return retv;
    }

    /** Multiply this Unit by another Unit.
     * @param multiplicand
     * @return The product of this Unit multiplied by the argument.
     */
    public Unit multiplyBy(Unit multiplicand) {
        Unit retv = copy();
        int otherExponents[] = multiplicand.getType();
        int thisExponents[] = retv.getType();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            thisExponents[i] += otherExponents[i];
        }
        retv.setType(thisExponents);
        retv.setScale(retv.getScale() * multiplicand.getScale());
        return retv;
    }

    /** Returns of value of this Unit raised to the power of the argument.
     * @param power The exponent.
     * @return This Unit raised to the power of the argument.
     */
    public Unit pow(double power) {
        Unit unit = copy();
        int exponents[] = unit.getType();
        double scale = unit.getScale();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            exponents[i] *= power;
        }
        scale = Math.pow(scale, power);
        unit.setType(exponents);
        unit.setScale(scale);
        return unit;
    }

    /** Set the primary label.
     * @param label The primary label.
     */
    public void setPrimaryLabel(String label) {
        _labels.setElementAt(label, 0);
    }

    /** Set the scale.
     * @param d The scale.
     */
    public void setScale(double d) {
        _scaleToBaseUnit = d;
    }

    /** Set the type.
     * @param type
     */
    public void setType(int[] type) {
        _type = type;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String retv =
            "Unit:(" + getLabelsString() + ") " + _scaleToBaseUnit + "*<";
        retv += _type[0];
        for (int i = 1; i < UnitLibrary.getNumCategories(); i++) {
            retv += ", " + _type[i];
        }
        retv += ">";
        return retv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private void setLabels(Vector labels) {
        _labels = labels;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    Vector _labels = new Vector();
    private static int _noLabelCounter = 0;
    private double _scaleToBaseUnit = 1.0;
    int _type[];
}
