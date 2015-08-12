/* Filter for removing properties.

 Copyright (c) 2004-2014 The Regents of the University of California.
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

 */
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// RemoveProperties

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter out the properties included
 in this classes.

 <p>For example, after Ptolemy II 5.0, the <i>stopTime</i> parameter has a
 default value as Infinity instead of MaxDouble (a macro with value of
 Double.MAX_VALUE). What is more, stopTime is not supposed to be stored in
 the MoML file. Therefore, this filter filters out those stopTime parameters
 with the old default value, MaxDouble. The new default value for the
 stopTime parameter will be created automatically by the Java classes and
 the parameter will not be exported into the MoML file.
 <p>
 A typical usage looks like the following.
 <pre>
 // stopTime after Ptolemy II 5.0
 // The stopTime used to have a default value as the Double.MAX_VALUE.
 // Now the default value is Infinity.
 HashMap removePropertyStopTime = new HashMap();

 // Remove properties whose name is "stopTime" if their
 // class and value properties are in the HashMap.
 _propertiesToBeRemoved.put("stopTime", removePropertyStopTime);

 // The class must be a Parameter for this to be removed.
 removePropertyStopTime.put("ptolemy.data.expr.Parameter", null);

 // The value must be one of the following representations of Double.MAX_VALUE.
 removePropertyStopTime.put("1.7976931348623E308", null);
 removePropertyStopTime.put("1.797693134862316E308", null);
 removePropertyStopTime.put("MaxDouble", null);
 removePropertyStopTime.put(""+Double.MAX_VALUE, null);

 </pre>
 The removePropertyStopTime HashMap stores all possible matches of the name
 and class attributes of the MoML entry for this attribute. Given MoML with
 both a class and a value, then both must match for the property to
 be removed.

 The _propertiesToBeRemoved HashMap contains all the properties
 such as the stopTime parameter that will be removed.

 <p> Note that this filter has a limitation. This filter assumes that the
 property to be removed always has three attributes, name, class, and
 value, and they are always in this order.

 @author Haiyang Zheng
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (hyzheng)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class RemoveProperties extends MoMLFilterSimple {
    /** Return the old attribute value for properties that are not registered
     *  to be removed. Otherwise, return null to remove the property.
     *  @param container  The container for this attribute.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return The value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        //System.out.println("RemoveProperties.filterAttributeValue: " + container + "\t"
        //   +  attributeName + "\t" + attributeValue);
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first.
            return null;
        }

        if (attributeName.equals("name")) {
            if (_propertiesToBeRemoved.containsKey(attributeValue)
                    && element != null && element.equals("property")) {
                _foundPropertyToBeRemoved = true;
                _propertyMap = (HashMap) _propertiesToBeRemoved
                        .get(attributeValue);
            } else {
                _foundPropertyToBeRemoved = false;
            }
        }

        if (attributeName.equals("class") && _foundPropertyToBeRemoved) {
            if (_propertyMap.containsKey(attributeValue)) {
                _propertyToBeRemovedConfirmed = true;
            } else {
                _foundPropertyToBeRemoved = false;
                _propertyToBeRemovedConfirmed = false;
            }
        }

        if (attributeName.equals("value") && _propertyToBeRemovedConfirmed) {
            if (_propertyMap.containsKey(attributeValue)) {
                String newValue = (String) _propertyMap.get(attributeValue);

                if (!attributeValue.equals(newValue)) {
                    MoMLParser.setModified(true);
                }

                _foundPropertyToBeRemoved = false;
                _propertyToBeRemovedConfirmed = false;
                return newValue;
            } else {
                _foundPropertyToBeRemoved = false;
                _propertyToBeRemovedConfirmed = false;
            }
        }

        return attributeValue;
    }

    /** Reset private variables.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception if there is a problem substituting
     *  in the new value.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
        _foundPropertyToBeRemoved = false;
        _propertyToBeRemovedConfirmed = false;
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Remove the properties listed below:");
        Iterator propertiesToBeRemoved = _propertiesToBeRemoved.keySet()
                .iterator();

        while (propertiesToBeRemoved.hasNext()) {
            String propertyToBeRemoved = (String) propertiesToBeRemoved.next();
            results.append("\t" + propertyToBeRemoved + "\n");

            HashMap propertyMap = (HashMap) _propertiesToBeRemoved
                    .get(propertyToBeRemoved);
            Iterator attributeMapEntries = propertyMap.entrySet().iterator();

            while (attributeMapEntries.hasNext()) {
                Map.Entry attributes = (Map.Entry) attributeMapEntries.next();
                String oldAttribute = (String) attributes.getKey();
                String newAttribute = (String) attributes.getValue();
                results.append("\t\t" + oldAttribute + "\t -> " + newAttribute
                        + "\n");
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of the properties to be removed.
    private static HashMap _propertiesToBeRemoved;

    // Flag indicating whether a property to be removed is truly found.
    private boolean _propertyToBeRemovedConfirmed = false;

    // Flag indicating whether a potential property to be removed is found.
    private boolean _foundPropertyToBeRemoved = false;

    // Cache of map from the property to be removed to its detailed information.
    private HashMap _propertyMap;

    static {
        ///////////////////////////////////////////////////////////
        // The properties to be removed.
        _propertiesToBeRemoved = new HashMap();

        // For the stopTime parameter after Ptolemy II 5.0, if the stop time
        // parameter has an old default value, remove it.
        // The stopTime used to have a default value as the Double.MAX_VALUE.
        // Now the default value is Infinity.
        HashMap removePropertyStopTime = new HashMap();

        // Key = attribute name, Value = attribute value
        removePropertyStopTime.put("1.7976931348623E308", null);
        removePropertyStopTime.put("1.797693134862316E308", null);
        removePropertyStopTime.put("MaxDouble", null);
        removePropertyStopTime.put("" + Double.MAX_VALUE, null);

        removePropertyStopTime.put("ptolemy.data.expr.Parameter", null);

        _propertiesToBeRemoved.put("stopTime", removePropertyStopTime);

        // A property named "directorClass" that is a StringAttribute
        // or StringParameter with value "ptolemy.domains.fsm.kernel.HSDirector"
        // will be removed. HSDirector no longer exists, so we revert
        // to the default.
        HashMap removePropertyDirectorClass = new HashMap();

        // Key = attribute name, Value = attribute value
        removePropertyDirectorClass.put(
                "ptolemy.domains.fsm.kernel.HSDirector", null);
        removePropertyDirectorClass.put("ptolemy.kernel.util.StringAttribute",
                null);
        removePropertyDirectorClass.put("ptolemy.data.expr.StringParameter",
                null);

        _propertiesToBeRemoved
        .put("directorClass", removePropertyDirectorClass);
    }
}
