/* Abstract base class for tokens that contain a scalar.

 Copyright (c) 1997-2001 The Regents of the University of California.
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

@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (yuhong@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.math.Complex;
import ptolemy.math.FixPoint;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.unit.UnitSystem;

import java.util.Arrays;

//////////////////////////////////////////////////////////////////////////
//// ScalarToken
/**
Abstract base class for tokens that contain a scalar.
This class defines methods for type conversion among different scalar
tokens. The implementation in this base class just throws an exception.
Derived class should override the methods that the corresponding
conversion can be achieved without loss of information.
<p>
Instances of ScalarToken may have units. In the arithmetic methods add() and
subtract(), the two operands must have the same units. Otherwise, an exception
will be thrown. In the methods multiply() and divide(), the units of the
resulting token will be computed automatically.

@author Yuhong Xiong, Mudit Goel
@version $Id$
*/
public abstract class ScalarToken extends Token {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a ScalarToken containing the absolute value of the
     *  value of this token. If this token contains a non-negative
     *  number, it is returned directly; otherwise, a new token is
     *  is return.
     *  @return A ScalarToken of the same type as the argument.
     */
    public abstract ScalarToken absolute();

    /** Return the value of this token as a Complex.
     *  In this base class, we just throw an exception.
     *  @return A Complex
     *  @exception IllegalActionException Always thrown
     */
    public Complex complexValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                    getClass().getName() + " to a Complex losslessly.");
    }

    /** Return the value of this token as a double.
     *  In this base class, we just throw an exception.
     *  @return A double
     *  @exception IllegalActionException Always thrown
     */
    public double doubleValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                    getClass().getName() + " to a double losslessly.");
    }

    /** Return the type of this token.
     *  @return BaseType.SCALAR
     */
    public Type getType() {
        return BaseType.SCALAR;
    }

    /** Return the value of this token as a FixPoint.
     *  In this base class, we just throw an exception.
     *  @return A FixPoint
     *  @exception IllegalActionException Always thrown.
     */
    public FixPoint fixValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                getClass().getName() + " to a FixPoint losslessly.");
    }

    /** Return the value of this token as an int.
     *  In this base class, we just throw an exception.
     *  @return The value of this token.
     *  @exception IllegalActionException Always thrown.
     */
    public int intValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                    getClass().getName() + " to an int losslessly.");
    }

    /** Check whether the value of this token is strictly less than that of the
     *  argument token.
     *  @param token A token to compare against.
     *  @return A BooleanToken with value true if this token is strictly
     *   less than the argument.
     *  @exception IllegalActionException If the type of the argument token
     *   is incomparable with the type of this token.
     */
    public abstract BooleanToken isLessThan(ScalarToken token)
            throws IllegalActionException;

    /** Return a scalar token that contains the value of this token in the
     *  units of the argument token. The unit category of the argument token
     *  must be the same as that of this token, otherwise, an exception will
     *  be thrown. The returned token is unitless.
     *  @param units A scalar token that represents a unit.
     *  @return A scalar token that do not have a unit.
     *  @exception IllegalActionException If the unit category of the
     *   argument token is not the same as that of this one.
     */
    public ScalarToken inUnitsOf(ScalarToken units)
            throws IllegalActionException {
        if ( !_areUnitsEqual(units)) {
            throw new IllegalActionException("ScalarToken.inUnitsOf: "
                    + "The units of this token: " + unitsString()
                    + " are not the same as the units of the argument: "
                    + units.unitsString());
        }
        return (ScalarToken)this.divide(units);
    }

    /** Return the value of this token as a long integer.
     *  In this base class, we just throw an exception.
     *  @return A long
     *  @exception IllegalActionException Always thrown.
     */
    public long longValue() throws IllegalActionException {
        throw new IllegalActionException("Cannot convert the value in " +
                    getClass().getName() + " to a long losslessly.");
    }

    /** Set the unit category this token belongs to.
     *  @param The unit category index.
     */
    public void setUnitCategory(int index) {
        _unitCategoryExponents = new int[index+1];
        Arrays.fill(_unitCategoryExponents, 0);
        _unitCategoryExponents[index] = 1;
    }

    /** Return the string representation of the units of this token.
     *  The general format of the returned string is
     *  "(l_1 * l_2 * ... * l_m) / (s_1 * s_2 * ... * s_n)".
     *  For example: "(meter * kilogram) / (second * second)".
     *  If m or n is 1, then the parenthesis above or below "/" is
     *  omited. For example: "meter / second".
     *  If there is no term above "/", the format becomes
     *  "1 / (s_1 * s_2 * ... * s_n)". For example: "1 / meter".
     *  If this token does not have a unit, return an empty string.
     *  @return A string representation of the units of this token.
     */
    public String unitsString() {
        if (_isUnitless(_unitCategoryExponents)) {
            return "";
        }

        String positiveUnits = "";
        String negativeUnits = "";
        boolean justOnePositive = true;
        boolean justOneNegative = true;
        for (int i=0; i<_unitCategoryExponents.length; i++) {
            int exponent = _unitCategoryExponents[i];
            if (exponent != 0) {
                String baseString = null;
                   baseString = UnitSystem.getBaseUnitName(i);
                   if (exponent > 0) {
                    for (int j=0; j<exponent; j++) {
                        if (positiveUnits.equals("")) {
                            positiveUnits = baseString;
                        } else {
                            positiveUnits += " * " + baseString;
                            justOnePositive = false;
                        }
                    }
                } else {
                    for (int j=0; j<-exponent; j++) {
                        if (negativeUnits.equals("")) {
                            negativeUnits = baseString;
                        } else {
                            negativeUnits += " * " + baseString;
                            justOneNegative = false;
                        }
                    }
                }
            }
        }

        if (positiveUnits.equals("") && negativeUnits.equals("")) {
            return "";
        }

        if (positiveUnits.equals("")) {
            positiveUnits = "1";
        } else if (justOnePositive) {
            positiveUnits = positiveUnits;
        } else {
            positiveUnits = "(" + positiveUnits + ")";
        }

        if (negativeUnits.equals("")) {
            return positiveUnits;
        } else if (justOneNegative) {
            return positiveUnits + " / " + negativeUnits;
        } else {
            return positiveUnits + " / (" + negativeUnits + ")";
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Add the corresponding unit category exponents.
     *  @param token A token whose exponent will be added with the
     *   exponents of this token.
     *  @return An int array containing the addition result, or null
     *   if the result is unitless.
     */
    protected int[] _addCategoryExponents(ScalarToken token) {
        return _addCategoryExponents(token._unitCategoryExponents);
    }

    /** Return a copy of the unit category exponents array. If this
     *  token does not have a unit, return null;
     *  @return An int array that is a copy of the unit category
     *   exponents of this token.
     */
    protected int[] _copyOfCategoryExponents() {
        if (_isUnitless()) {
            return null;
        }

        int length = _unitCategoryExponents.length;
        int[] exponents = new int[length];
        System.arraycopy(_unitCategoryExponents, 0, exponents, 0, length);
        return exponents;
    }

    /** Return true if the units of this token are the same as that of the
     *  argument token. If both tokens do not have units, return true.
     *  @param scalarToken A scalar token.
     *  @return True if the units of this token is the same as that of the
     *   argument token; false otherwise.
     */
    protected boolean _areUnitsEqual(ScalarToken scalarToken) {
        boolean isThisUnitless = _isUnitless(this._unitCategoryExponents);
        boolean isArgumentUnitless =
                _isUnitless(scalarToken._unitCategoryExponents);

        // Either this token, or the argument token, or both have non null
        // exponent arrays.
        if (isThisUnitless && isArgumentUnitless) {
            return true;
        } else if (isThisUnitless || isArgumentUnitless) {
            // one is unitless, the other is not.
            return false;
        } else {
            // both have units.
            int thisLength = _unitCategoryExponents.length;
            int argumentLength = scalarToken._unitCategoryExponents.length;
            int shorterLength = (thisLength <= argumentLength) ? thisLength :
                                argumentLength;
            for (int i=0; i<shorterLength; i++) {
                if (_unitCategoryExponents[i] !=
                    scalarToken._unitCategoryExponents[i]) {
                    return false;
                }
            }

            for (int i=shorterLength; i<thisLength; i++) {
                if (_unitCategoryExponents[i] != 0) {
                    return false;
                }
            }
            for (int i=shorterLength; i<argumentLength; i++) {
                if (scalarToken._unitCategoryExponents[i] != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Return true if this token does not have a unit.
     *  @return True if this token does not have a unit.
     */
    protected boolean _isUnitless() {
        return _isUnitless(_unitCategoryExponents);
    }

    /** Subtract the corresponding unit category exponents of the
     *  argument token from that of this token.
     *  @param token A token whose exponent will be subtracted from the
     *   exponents of this token.
     *  @return An int array containing the addition result, or null
     *   if the result is unitless.
     */
    protected int[] _subtractCategoryExponents(ScalarToken token) {
        // negate the exponents of the argument token and add to
        // this token.
        int[] negation = null;
        if ( !token._isUnitless()) {
            int length = token._unitCategoryExponents.length;
            negation = new int[length];
            for (int i=0; i<length; i++) {
                negation[i] = -token._unitCategoryExponents[i];
            }
        }
        return _addCategoryExponents(negation);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    /** The unit category exponents.
     *  The unit system contains a set of base unit categories and derived
     *  catetories. The base categories are customizable by the user.
     *  For example, the user may choose to use the SI unit system which
     *  has 7 base categories: length, mass, time, electric current,
     *  thermodynamic temperature, amount of substance, and luminous
     *  intensity. The customization is done by defining a MoML file to specify
     *  the categories and the units in each category. Each category has an
     *  index, assigned by the order the category appears in the MoML file.
     *  Derived units are recorded by the exponents of the category. For
     *  example, the category speed, which is length/time, is stored by an
     *  exponent of 1 for the length category, and an exponent of -1 for the
     *  time category.
     *  This array records the exponents of the base categories.
     */
    protected int[] _unitCategoryExponents = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Add the exponent array of this token with the argument array,
    // and return the result in a new array.
    private int[] _addCategoryExponents(int[] exponents) {
        boolean isThisUnitless = _isUnitless(this._unitCategoryExponents);
        boolean isArgumentUnitless = _isUnitless(exponents);

        if (isThisUnitless && isArgumentUnitless) {
            return null;
        }

        // Either this token, or the argument token, or both have non null
        // exponent arrays.
        if (isThisUnitless) {
            // exponents is not unitless.
            int length = exponents.length;
            int[] result = new int[length];
            System.arraycopy(exponents, 0, result, 0, length);
            return result;
        }
        if (isArgumentUnitless) {
            // this._unitCategoryExponents is not unitless.
            int length = this._unitCategoryExponents.length;
            int[] result = new int[length];
            System.arraycopy(_unitCategoryExponents, 0, result, 0, length);
            return result;
        }

        // both have units.
        int thisLength = _unitCategoryExponents.length;
        int argumentLength = exponents.length;
        int[] result;
        if (thisLength <= argumentLength) {
            result = new int[argumentLength];
            System.arraycopy(exponents, 0, result, 0, argumentLength);
            for (int i=0; i<thisLength; i++) {
                result[i] += _unitCategoryExponents[i];
            }
        } else {
            result = new int[thisLength];
            System.arraycopy(_unitCategoryExponents, 0, result, 0, thisLength);
            for (int i=0; i<argumentLength; i++) {
                result[i] += exponents[i];
            }
        }

        if (_isUnitless(result)) {
            return null;
        }
        return result;
    }

    // Return true if this token does not have a unit.
    private boolean _isUnitless(int[] exponents) {
        if (exponents != null) {
            for (int i=0; i<exponents.length; i++) {
                if (exponents[i] != 0) {
                    return false;
                }
            }
        }
        return true;
    }
}
