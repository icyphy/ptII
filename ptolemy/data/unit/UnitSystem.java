/* A unit system as defined by a set of constants.

 Copyright (c) 2001 The Regents of the University of California.
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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (liuxj@eecs.berkeley.edu)
*/

package ptolemy.data.unit;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.*;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.math.Complex;

import java.util.Hashtable;

//////////////////////////////////////////////////////////////////////////
//// UnitSystem
/**
A unit system as defined by a set of constants.
<p>
The constants represent the various measurement units in a unit system.
The units belong to a number of categories, for example length and time
in the International System of Units (SI). Each category has a base unit,
for example meter in the length category.

FIXME:
In a Vergil configuration, a unit system is defined as. (Give example)

@author Xiaojun Liu
@version $Id$
@see ptolemy.data.expr.Constants
*/

public class UnitSystem extends Attribute {

    /** Construct a unit system with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public UnitSystem(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Record the specified unit category, and the name of its base unit.
     *  If the category is not already recorded, assign a unique index for
     *  the category.
     *  @param categoryName The name of the unit category.
     *  @param baseUnitName The name of the base unit of the category.
     */
    public static void addUnitCategory(String categoryName,
	    String baseUnitName) {

        // System.out.println("Add unit category: " + categoryName
        // + " " + baseUnitName);

	Integer index = (Integer)_indexTable.get(categoryName);
	if (index != null) {
	    _baseNames[index.intValue()] = baseUnitName;
	} else {
	    String[] oldNames = _baseNames;
	    int length = oldNames.length;
	    _baseNames = new String[length + 1];
	    System.arraycopy(oldNames, 0, _baseNames, 0, length);
	    index = new Integer(length);
	    _indexTable.put(categoryName, index);
	    _baseNames[length] = baseUnitName;
	}
    }

    /** If the changed attribute is a parameter, add the parameter as
     *  a constant recognized by the expression parser.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown by the super class.
     */
    public void attributeChanged(Attribute attribute)
	    throws IllegalActionException {
	if (attribute instanceof Parameter) {

            // System.out.println("Add unit constant: " + attribute.getName());

	    Constants.add(attribute.getName(),
		    ((Parameter)attribute).getToken());
	} else {
	    super.attributeChanged(attribute);
	}
    }

    /** Return the name of the base unit of the specified category.
     *  @param categoryIndex The index of the unit category.
     *  @return The name of the base unit of the category.
     */
    public static String getBaseUnitName(int categoryIndex) {
	if (categoryIndex < 0 || categoryIndex > _baseNames.length - 1) {
	    return null;
	} else {
	    return _baseNames[categoryIndex];
	}
    }

    /** Return the index assigned to the specified unit category.
     *  @param categoryName The name of the unit category.
     *  @return The index assigned to the category.
     */
    public static int getUnitCategoryIndex(String categoryName) {
	Integer index = (Integer)_indexTable.get(categoryName);
	if (index == null) {
	    //FIXME: throw an exception?
	    return -1;
	} else {
	    return index.intValue();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The hash table that maps the name of a unit category to its index.
    private static Hashtable _indexTable = new Hashtable();

    // The array that contains the names of the base units of the categories.
    private static String[] _baseNames = new String[0];

}
