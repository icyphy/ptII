/* Remove certain properties that have and empty value.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.moml.filter;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

import java.util.HashSet;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// RemoveEmptyProperties.
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter so that properties
that have an empty value will be removed.


<p>For example, in diva, if the size attribute is empty, then
loading a MoML file will throw an exception.

@author Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.1
*/
public class RemoveEmptyProperties implements MoMLFilter {

    /**  If the attributeName is "class" and attributeValue names a
     *   class that has had its port names changed between releases,
     *  then substitute in the new port names.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {

        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.

        if (attributeValue == null && !attributeName.equals("value")) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            if (_propertiesThatShouldHaveValues
                    .contains(attributeValue)) {
                _currentlyProcessingPropertyThatShouldHaveAValue = true;
                _foundChange = true;
            } else {
                _currentlyProcessingPropertyThatShouldHaveAValue = false;
                _foundChange = false;
            }
        }
        if (attributeName.equals("value")) {
            if (_currentlyProcessingPropertyThatShouldHaveAValue
                    && attributeValue == null) {
                _foundChange = true;
            } else {
                _foundChange = false;
            }
        }
        return attributeValue;
    }

    /** Given the elementName, perform any filter operations
     *  that are appropriate for the MOMLParser.endElement() method.
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param elementName The element type name.
     *  @return the filtered element name, or null if
     *  MoMLParser.endElement() should immediately return.
     */
    public String filterEndElement(NamedObj container, String elementName)
        throws Exception {
        if (_foundChange) {
            _foundChange = false;
            MoMLParser.setModified(true);
            return null;
        }
        return elementName;
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results =
            new StringBuffer(getClass().getName()
                    + ": Remove properties that are empty (have no value)\n"
                    + "that have been renamed.\n"
                    + "Below are the actors that are affected, along "
                    + "with the port name\nand the new classname:"
                             );
        Iterator properties = _propertiesThatShouldHaveValues.iterator();
        while (properties.hasNext()) {
            String property = (String)properties.next();
            results.append("\t" + property + "\n");
        }
        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Set of properties that should have values
    // (the value should not be null). 
    private static HashSet _propertiesThatShouldHaveValues;

    // The the full name of the actor we are currently processing
    private static String _currentActorFullName;

    // Set to true if we are currently processing a property that should
    // have a non-empty value.
    private static boolean
    _currentlyProcessingPropertyThatShouldHaveAValue  = false;

    // Last "name" value seen, for use if we see a "class".
    private static String _lastNameSeen;

    // The new class name for the property we are working on.
    private static String _newClass;

    // Keep track of whether a change was found.
    private static boolean _foundChange;

    // Cache of map from old property names to new class names for
    // the actor we are working on.
    //private static HashMap _propertyMap;

    static {
        ///////////////////////////////////////////////////////////
        // Actors that have properties that have changed class.
        _propertiesThatShouldHaveValues = new HashSet();

        _propertiesThatShouldHaveValues.add("_vergilSize");
        _propertiesThatShouldHaveValues.add("_vergilLocation");
        _propertiesThatShouldHaveValues.add("_location");
    }
}
