/* Representation of a Unit

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

import java.util.Vector;

import ptolemy.data.unit.BaseUnit;
import ptolemy.data.unit.UnitUtilities;

///////////////////////////////////////////////////////////////////
//// Unit

/**
 Class that contains the internal representation of a Unit.
 A Unit has the mathematical notation  <b>S</b>&lt;E1, E2, ..., En&gt;
 where <b>S</b> is the
 <i>scale</i> and &lt;E1, E2, ..., En&gt; is the <i>type</i> of the Unit.
 <p>
 This class also contains methods for operating on Units, such as multiply,
 divide, etc.
 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
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

        _labels.add("noLabel" + _noLabelCounter++);
    }

    /** Create a Unit from a BaseUnit.
     * @param bu BaseUnit that provides the basis for this Unit.
     */
    public Unit(BaseUnit bu) {
        this();

        String name = bu.getName();
        setPrimaryLabel(name);

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

    /** Make a copy of this Unit.
     * @return A new Unit.
     */
    public Unit copy() {
        Unit retv = new Unit();
        retv._setLabels((Vector) getLabels().clone());

        int[] newExponents = retv.getType();

        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            newExponents[i] = _type[i];
        }

        retv.setScale(getScale());
        return retv;
    }

    /** The expression of the Unit that is commonly used by humans.
     * For example, the unit 4.1868E7&lt;2, 1, -3, 0, 0&gt; will produce the
     * common expression "calorie second^-1".
     * @see ptolemy.moml.unit.UnitPresentation#descriptiveForm()
     */
    @Override
    public String descriptiveForm() {
        StringBuffer retv = null;
        Unit unit = UnitLibrary.getUnit(this);

        if (unit != null) {
            return unit.getPrimaryLabel();
        }

        UnitExpr factorization = factor();

        if (factorization != null) {
            Vector numerator = new Vector();
            Vector denominator = new Vector();
            Vector uTerms = factorization.getUTerms();

            for (int i = 0; i < uTerms.size(); i++) {
                UnitTerm uterm = (UnitTerm) uTerms.elementAt(i);

                if (uterm.getExponent() < 0) {
                    denominator.add(uterm.invert());
                } else if (uterm.getExponent() > 0) {
                    numerator.add(uterm);
                }
            }

            if (numerator.size() == 0) {
                retv = new StringBuffer("1");
            } else {
                retv = new StringBuffer(((UnitTerm) numerator.elementAt(0))
                        .getUnit().getPrimaryLabel());

                for (int i = 1; i < numerator.size(); i++) {
                    retv.append(" "
                            + ((UnitTerm) numerator.elementAt(i)).getUnit()
                                    .getPrimaryLabel());
                }
            }

            if (denominator.size() > 0) {
                retv.append("/"
                        + ((UnitTerm) denominator.elementAt(0)).getUnit()
                                .getPrimaryLabel());

                for (int i = 1; i < denominator.size(); i++) {
                    retv.append(" "
                            + ((UnitTerm) denominator.elementAt(i)).getUnit()
                                    .getPrimaryLabel());
                }
            }

            return retv.toString();
        }

        if (_scale == 1.0) {
            int numCats = _type.length;
            StringBuffer desc = new StringBuffer();

            for (int i = 0; i < numCats; i++) {
                if (_type[i] != 0) {
                    Unit baseUnit = UnitLibrary.getBaseUnit(i);

                    // Coverity: getBaseUnit() could return null;
                    if (baseUnit != null) {
                        if (_type[i] == 1) {
                            desc.append(" " + baseUnit.getPrimaryLabel());
                        } else {
                            desc.append(" " + baseUnit.getPrimaryLabel() + "^"
                                    + _type[i]);
                        }
                    }
                }
            }

            return desc.toString().substring(1);
        }

        // End up here if nothing works, so just return the formal description
        return toString();
    }

    /** Divide this Unit by another Unit.
     * @param divisor The divisor unit.
     * @return This Unit divided by the divisor.
     */
    public Unit divideBy(Unit divisor) {
        Unit retv = copy();
        int[] otherExponents = divisor.getType();
        int[] thisExponents = retv.getType();

        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            thisExponents[i] -= otherExponents[i];
        }

        retv.setType(thisExponents);
        retv.setScale(retv.getScale() / divisor.getScale());
        return retv;
    }

    /** Return True if this Unit equals another object
     * @param object The object to be compared against.
     * @return True if this Unit equals the other Unit.  Return false
     * if the other object is null or not an instance of Unit.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        Unit otherUnit = null;
        if (!(object instanceof Unit)) {
            return false;
        } else {
            otherUnit = (Unit) object;
        }

        int[] otherExponents = otherUnit.getType();

        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            if (_type[i] != otherExponents[i]) {
                return false;
            }
        }

        if (_scale != otherUnit.getScale()) {
            return false;
        }

        if (!getLabelsString().equals(otherUnit.getLabelsString())) {
            return false;
        }

        return true;
    }

    /** Factor a Unit into a UnitExpr that has UnitTerms that are in the
     * Library. In general, factorization is a <i>lengthy</i> process, and this
     * method is not a complete factorization algorithm. It only tries some of
     * the more likely combinations.
     * @return UnitExpr that is equivalent to to the Unit.
     */
    public UnitExpr factor() {
        // First see if it is simply an invert
        Unit invert = UnitLibrary.getUnit(invert());

        if (invert != null) {
            UnitExpr retv = new UnitExpr();
            UnitTerm uTerm = new UnitTerm(invert);
            uTerm.setExponent(-1);
            retv.addUnitTerm(uTerm);
            return retv;
        }

        // Second see if this is of the form numerator/denominator
        Vector libraryUnits = UnitLibrary.getLibrary();

        for (int i = 0; i < libraryUnits.size(); i++) {
            Unit factor = (Unit) libraryUnits.elementAt(i);
            Unit numerator = this.multiplyBy(factor);
            Unit xx = UnitLibrary.getUnit(numerator);

            if (xx != null) {
                UnitExpr retv = new UnitExpr();

                if (xx != UnitLibrary.Identity) {
                    UnitTerm uTerm1 = new UnitTerm(xx);
                    retv.addUnitTerm(uTerm1);
                }

                UnitTerm uTerm2 = new UnitTerm(factor);
                uTerm2.setExponent(-1);
                retv.addUnitTerm(uTerm2);
                return retv;
            }
        }

        for (int i = 0; i < libraryUnits.size(); i++) {
            Unit factor = (Unit) libraryUnits.elementAt(i);
            Unit remainder = this.divideBy(factor);
            Unit xx = UnitLibrary.getUnit(remainder);

            if (xx != null && xx != UnitLibrary.Identity) {
                UnitExpr retv = new UnitExpr();
                UnitTerm uTerm = new UnitTerm(factor);
                retv.addUnitTerm(uTerm);
                uTerm = new UnitTerm(xx);
                retv.addUnitTerm(uTerm);
                return retv;
            }
        }

        return null;
    }

    /** Get the labels for a Unit.
     * @see ptolemy.moml.unit.Unit#getPrimaryLabel()
     * @return The labels.
     */
    public Vector getLabels() {
        return _labels;
    }

    /** Create a String that is the concatenation of all the labels.
     * @return The concatenation of the labels.
     */
    public String getLabelsString() {
        StringBuffer retv = null;

        if (_labels.size() > 0) {
            retv = new StringBuffer((String) _labels.elementAt(0));
        } else {
            return "";
        }

        for (int i = 1; i < _labels.size(); i++) {
            retv.append((String) _labels.elementAt(i) + ",");
        }

        return retv.toString();
    }

    /** Get the primary label of a Unit.
     * A Unit can have more than one label. For example, cm, and centimeter are
     * labels for the same Unit. There always exists a label that is primary.
     * @return The primary label.
     */
    public String getPrimaryLabel() {
        return (String) _labels.elementAt(0);
    }

    /** Get the scale.
     * @return Scale.
     */
    public double getScale() {
        return _scale;
    }

    /** Get the type (represented as a int array) of this Unit.
     * @return The type (represented as a int array) of this Unit.
     */
    public int[] getType() {
        return _type;
    }

    /** Return a hash code value for this Unit. This method returns
     *  the bitwise xor of the hashCode of the label String, the
     *  categories and the hashCode() of the scale.
     *  @return A hash code value for this Unit.
     */
    @Override
    public int hashCode() {
        int hashCode = getLabelsString().hashCode();
        for (int i = 0; i < UnitLibrary.getNumCategories(); i++) {
            hashCode >>>= _type[i];
        }

        hashCode >>>= Double.valueOf(_scale).hashCode();

        return hashCode;
    }

    /** Return true if the Unit has the same type as another Unit.
     * @param otherUnit
     * @return True if the Unit has the same type as the argument.
     */
    public boolean hasSameType(Unit otherUnit) {
        int[] otherType = otherUnit.getType();

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
        int[] otherExponents = getType();
        int[] exponents = new int[UnitLibrary.getNumCategories()];

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
        int[] otherExponents = multiplicand.getType();
        int[] thisExponents = retv.getType();

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
        int[] exponents = unit.getType();
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
        _scale = d;
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
    @Override
    public String toString() {
        StringBuffer retv = new StringBuffer("Unit:(" + getLabelsString()
                + ") " + _scale + "*<" + _type[0]);

        for (int i = 1; i < UnitLibrary.getNumCategories(); i++) {
            retv.append(", " + _type[i]);
        }

        retv.append(">");
        return retv.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the labels of the Unit.
     * @param labels The labels.
     */
    protected void _setLabels(Vector labels) {
        _labels = labels;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    Vector _labels = new Vector();

    private static int _noLabelCounter = 0;

    private double _scale = 1.0;

    int[] _type;
}
