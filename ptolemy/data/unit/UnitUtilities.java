/* Abstract base class for tokens that contain a scalar.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@Pt.ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@Pt.AcceptedRating Red (yuhong@eecs.berkeley.edu)

*/

package ptolemy.data.unit;

import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;


//////////////////////////////////////////////////////////////////////////
//// UnitUtilities.
/**
A set of manipulation routines that are useful for factoring most of
the difficulty of dealing with units out of individual token classes.
Furthermore, having these as static methods which do not depend on
token classes can improve generated code.  Generally, the methods in this
class manipulate arrays of integers, where each index in the array
corresponds to a different category of units, and the value of each
element in the array corresponds to the factor in that unit.
Generally, multiplying two tokens adds adds their units, dividing two
tokens subtracts their units, and adding and subtracting tokens assert that
the units are the same.

<p> Note that a null units array is considered to be a 'unitless'
value to reduce memory allocation for tokens that have no units.  In
other words, the exponent associated with each unit category is zero.
In general, the methods in this class return null whenever a unitless
unit array is encountered.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 0.2
*/
public class UnitUtilities {

    /** There are no instances of this class.
     */
    private UnitUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new units array that has the element at the given
     *  index set to one.
     *  @param index The unit category index.
     */
    public static int[] newUnitArrayInCategory(int index) {
        int[] units = new int[index+1];
        // Note: java.util.Arrays not available on Tini Arrays.fill(units, 0);
        for(int i = 0; i < units.length; i++) {
            units[i] = 0;
        }
        units[index] = 1;
        return units;
    }

    /** Return the string representation of the given array of units.
     *  The general format of the returned string is
     *  "(l_1 * l_2 * ... * l_m) / (s_1 * s_2 * ... * s_n)".
     *  For example: "(meter * kilogram) / (second * second)".
     *  If m or n is 1, then the parenthesis above or below "/" is
     *  omited. For example: "meter / second".
     *  If there is no term above "/", the format becomes
     *  "1 / (s_1 * s_2 * ... * s_n)". For example: "1 / meter".
     *  If the unit array is unitless, then return an empty string.
     *  @return A string representation of the given units array.
     */
    public static String unitsString(int[] units) {
        synchronized(_indexTable) {
            // FIXME: use StringBuffer.
            if (isUnitless(units)) {
                return "";
            }

            System.out.println(summarizeUnitCategories());

            String positiveUnits = "";
            String negativeUnits = "";
            boolean justOnePositive = true;
            boolean justOneNegative = true;
            for (int i = 0; i < units.length; i++) {
                int exponent = units[i];
                if (exponent != 0) {
                    String baseString = null;
                    baseString = UnitUtilities.getBaseUnitName(i);
                    if (exponent > 0) {
                        for (int j = 0; j < exponent; j++) {
                            if (positiveUnits.equals("")) {
                                positiveUnits = baseString;
                            } else {
                                positiveUnits += " * " + baseString;
                                justOnePositive = false;
                            }
                        }
                    } else {
                        for (int j = 0; j < -exponent; j++) {
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
            } else if (!justOnePositive) {
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
    }

    /** Return a copy of the given units array. If the given array is
     *  unitless, then return null.
     *  @return An int array that is a copy of the unit category
     *  exponents of this token.
     */
    public static int[] copyUnitsArray(int[] units) {

        if (isUnitless(units)) {
            return null;
        }

        int length = units.length;
        int[] newUnits = new int[length];
        System.arraycopy(units, 0, newUnits, 0, length);
        return newUnits;
    }

    /** Return true if the units of this token are the same as that of the
     *  argument token. If both tokens do not have units, return true.
     *  @param units1 The first array of units.
     *  @param units2 The second array of units.
     *  @return True if the units of this token is the same as that of
     *  the argument token; false otherwise.
     */
    public static boolean areUnitArraysEqual(int[] units1, int[] units2) {
        boolean isUnitless1 = isUnitless(units1);
        boolean isUnitless2 = isUnitless(units2);

        // Either this token, or the argument token, or both have non null
        // exponent arrays.
        if (isUnitless1 && isUnitless2) {
            return true;
        } else if (isUnitless1 || isUnitless2) {
            // one is unitless, the other is not.
            return false;
        } else {
            // both are not unitless.
            int units1Length = units1.length;
            int units2Length = units2.length;
            int shorterLength = Math.min(units1Length, units2Length);
            for (int i = 0; i < shorterLength; i++) {
                if (units1[i] != units2[i]) {
                    return false;
                }
            }
            for (int i = shorterLength; i < units1Length; i++) {
                if (units1[i] != 0) {
                    return false;
                }
            }
            for (int i = shorterLength; i < units2Length; i++) {
                if (units2[i] != 0) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Add the given unit arrays, and return the result in a new
     *  array.  The size of the returned array will be the maximum of
     *  the size of the two input arrays, or null if both input arrays
     *  are unitless.
     */
    public static int[] addUnitsArray(int[] units1, int[] units2) {
        boolean isUnitless1 = isUnitless(units1);
        boolean isUnitless2 = isUnitless(units2);

        if (isUnitless1 && isUnitless2) {
            return null;
        } else if (isUnitless1) {
            // units2 is not unitless.
            return copyUnitsArray(units2);
        } else if (isUnitless2) {
            // units1 is not unitless.
            return copyUnitsArray(units1);
        } else {
            // both have units.
            int units1Length = units1.length;
            int units2Length = units2.length;
            int[] result;
            if (units1Length < units2Length) {
                result = new int[units2Length];
                System.arraycopy(units2, 0, result, 0, units2Length);
                for (int i = 0; i < units1Length; i++) {
                    result[i] += units1[i];
                }
            } else {
                result = new int[units1Length];
                System.arraycopy(units1, 0, result, 0, units1Length);
                for (int i = 0; i < units2Length; i++) {
                    result[i] += units2[i];
                }
            }

            if (isUnitless(result)) {
                return null;
            }
            return result;
        }
    }

    /** Add the exponent array of this token with the argument array,
     *  and return the result in a new array.
     */
    public static int[] subtractUnitsArray(int[] units1, int[] units2) {
        // negate the exponents of the argument token and add to
        // this token.
        int[] negation = null;
        if (!isUnitless(units2)) {
            int length = units2.length;
            negation = new int[length];
            for (int i = 0; i < length; i++) {
                negation[i] = -units2[i];
            }
        }
        return addUnitsArray(units1, negation);
    }

    /** Return true if the given unit array is null, or the exponents for
     *  each index are zero.
     */
    public static boolean isUnitless(int[] exponents) {
        if (exponents != null) {
            for (int i = 0; i < exponents.length; i++) {
                if (exponents[i] != 0) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Register the specified unit category name.
     *  If the category is not already registered, assign a unique index
     *  for the category.
     *  This method is static, so a category added here will be
     *  available throughout the system.
     *  <p>Note that the
     *  {@link UnitCategory#UnitCategory(NamedObj, String)} constructor
     *  calls this method.
     *
     *  @param categoryName The unit categoryName to be registered.
     */
    public static void registerUnitCategory(String categoryName) {
        synchronized(_indexTable) {
            Integer index = (Integer)_indexTable.get(categoryName);
            if (index != null) {
                return;
            } else {
                index = new Integer(_categories);
                _indexTable.put(categoryName, index);
                ++_categories;
                _categoryList.add(categoryName);
            }
        }
    }

    /** Return the name of the base unit of the specified category.
     *  @param categoryIndex The index of the unit category.
     *  @return The name of the base unit of the category.
     */
    public static String getBaseUnitName(int categoryIndex) {
        synchronized(_indexTable) {
            if (categoryIndex < 0 || categoryIndex >= _categories) {
                // FIXME: exception?
                return "unknown";
            } else {
                String categoryName =
                    (String)_categoryList.get(categoryIndex);
                if (categoryName != null) {
                    return categoryName;
                } else {
                    // FIXME: exception?
                    return "unknown";
                }
            }
        }
    }

    /** Return the number of currently registered categories.
     */
    public static int getNumCategories() {
        return _categories;
    }

    /** Return the index assigned to the specified unit category.
     *  @param categoryName The unit category.
     *  @return The index assigned to the category.
     */
    public static int getUnitCategoryIndex(String categoryName) {
        synchronized(_indexTable) {
            Integer index = (Integer)_indexTable.get(categoryName);
            if (index == null) {
                //FIXME: throw an exception?
                return -1;
            } else {
                return index.intValue();
            }
        }
    }

    /** Return a string representation of the UnitSystem.
     *  @return A string representation of the UnitSystem
     */
    public static String summarizeUnitCategories() {
        synchronized(_indexTable) {
            StringBuffer buffer = new StringBuffer(
                    "The registered categories are:");
            buffer.append("" + _categories);
            buffer.append(_categoryList.toString());
            return buffer.toString();
        }
    }

    /** Reset the internal state of the UnitSystem.  This method is
     * only useful for testing, and should not be called under most
     * circumstances, since it will cause any previously created
     * UnitTokens to have incorrect units.
     */
    public static void resetUnitCategories() {
        // This method is necessary for testing.
        // UnitSystem has static state, so it makes it difficult
        // to create multiple small UnitSystems.
        synchronized(_indexTable) {
            _indexTable.clear();
            _categories = 0;
            _categoryList.clear();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The hash table that maps the name of a unit category to its
    // index.  This object also serves as a synchronization object for
    // the three fields of this class.
    private static HashMap _indexTable = new HashMap();

    // The number of registered unit categories.
    private static int _categories = 0;

    // The vector that contains all registered category names ordered
    // by index.
    private static ArrayList _categoryList = new ArrayList();
}
