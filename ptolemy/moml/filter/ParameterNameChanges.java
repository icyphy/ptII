/* Filter for Parameter name changes.

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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.moml.filter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// ParameterNameChanges

/** When this class is registered with the MoMLParser.setMoMLFilter()
 method, it will cause MoMLParser to filter so that models from
 earlier releases will run in the current release.

 <p>This class will filter for classes with Parameters where the parameter
 name has changed.

 <p>For example, after Ptolemy II 2.2, the PNDirector
 changed in such a way that the 'Initial_queue_capacity' parameter
 is now 'initialQueueCapacity'.

 <p>To add this
 change to this filter, we add a code to the static section at
 the bottom of the file.
 <pre>
 // PNDirectory: After 2.2, 'Initial_queue_capacity'
 // property is now 'initialQueueCapacity'

 HashMap pnDirectorChanges = new HashMap<String, String>();
 // Key = property name, Value = new class name
 pnDirectorChanges.put("Initial_queue_capacity",
 "initialQueueCapacity");
 </pre>
 The pnDirectorChange HashMap maps property names to the new
 name

 <pre>

 _classesWithParameterNameChanges
 .put("ptolemy.domains.pn.PNDirectory",
 pnDirectorChanges);
 </pre>
 The _classesWithParameterNameChanges HashMap contains all the classes
 such as PNDirector that have changes and each class has a map
 of the Parameter changes that are to be made.

 <p> Conceptually, how the code works is that when we see a class while
 parsing, we check to see if the class is in _classesWithParameterNameChanges.
 If the class was present in the HashMap, then as we go through the
 code, we look for property names that need to have their classes changed.

 <p>NOTE: This class and PortNameChange might conflict if
 a port and parameter have the same name.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ParameterNameChanges extends MoMLFilterSimple {
    /** If the attributeName is "class" and attributeValue names a
     *  class that has had a Parameter names changed between releases,
     *  then substitute in the new Parameter names.
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        // This method gets called many times by the MoMLParser,
        // so we try to be smart about the number of comparisons
        // and we try to group comparisons together so that we
        // are not making the same comparison more than once.
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            // Save the name for later use if we see a "class"
            _lastNameSeen = attributeValue;

            if (_currentlyProcessingActorWithParameterNameChanges) {
                if (_propertyMap != null
                        && _propertyMap.containsKey(attributeValue)) {
                    // We will do the above checks only if we found a
                    // class that had property class changes.
                    _newName = (String) _propertyMap.get(attributeValue);

                    if (!attributeValue.equals(_newName)) {
                        MoMLParser.setModified(true);
                    }

                    return _newName;
                } else {
                    return attributeValue;
                }
            }
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (_classesWithParameterNameChanges.containsKey(attributeValue)) {
                // We found a class with a parameter name change.
                _currentlyProcessingActorWithParameterNameChanges = true;

                // Coverity says that container could be null.
                String containerName = (container == null ? "" : container
                        .getFullName());

                _currentActorFullName = containerName + "." + _lastNameSeen;
                _propertyMap = (HashMap) _classesWithParameterNameChanges
                        .get(attributeValue);
            } else if (_currentlyProcessingActorWithParameterNameChanges
                    && _newName != null) {
                // We found a property class to change, and now we
                // found the class itself that needs changing.
                // Only return the new class once, but we might
                // have other properties that need changing
                //_currentlyProcessingActorWithParameterNameChanges = false;
                //                 String temporaryNewClass = _newName;
                //                 if (!attributeValue.equals(_newName)) {
                //                     MoMLParser.setModified(true);
                //                 }
                _newName = null;
            } else if (_currentlyProcessingActorWithParameterNameChanges
                    && container != null
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(
                            _currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes
                _currentlyProcessingActorWithParameterNameChanges = false;
            }
        }

        return attributeValue;
    }

    /** In this class, do nothing.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception Not thrown in this base class.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update any Parameter names\n"
                + "that have been renamed.\n"
                + "Below are the actors that are affected, along "
                + "with the Parameter name \nand the new name:\n");
        for (Map.Entry<String,HashMap<String,String>> classChange: _classesWithParameterNameChanges.entrySet()) {
            String actor = classChange.getKey();
            results.append("\t" + actor + "\n");

            HashMap<String,String> propertyMap = classChange.getValue();

            Iterator propertiesMapEntries = propertyMap.entrySet().iterator();

            while (propertiesMapEntries.hasNext()) {
                Map.Entry properties = (Map.Entry) propertiesMapEntries.next();
                String oldProperty = (String) properties.getKey();
                String newProperty = (String) properties.getValue();
                results.append("\t\t" + oldProperty + "\t -> " + newProperty
                        + "\n");
            }
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actor names a HashMap of property names to new classes.
    private static HashMap<String, HashMap<String, String>> _classesWithParameterNameChanges;

    // The the full name of the actor we are currently processing
    private String _currentActorFullName;

    // Set to true if we are currently processing an actor with parameter
    // class changes, set to false when we are done.
    private boolean _currentlyProcessingActorWithParameterNameChanges = false;

    // Last "name" value seen, for use if we see a "class".
    private String _lastNameSeen;

    // The new name for the property we are working on.
    private String _newName;

    // Cache of map from old property names to new class names for
    // the actor we are working on.
    private static HashMap _propertyMap;

    static {
        ///////////////////////////////////////////////////////////
        // Actors that have properties that have changed class.
        _classesWithParameterNameChanges = new HashMap<String, HashMap<String, String>>();

        // PNDirectory: After 2.2, 'Initial_queue_capacity'
        // property is now 'initialQueueCapacity'
        HashMap pnDirectorChanges = new HashMap<String, String>();

        // Key = property name, Value = new class name
        pnDirectorChanges.put("Initial_queue_capacity", "initialQueueCapacity");
        _classesWithParameterNameChanges.put(
                "ptolemy.domains.pn.kernel.PNDirector", pnDirectorChanges);

        // VariableDelay: After 4.0, 'defaultDelay'
        // property is now 'delay'
        HashMap variableDelayChanges = new HashMap<String, String>();
        variableDelayChanges.put("defaultDelay", "delay");
        _classesWithParameterNameChanges.put(
                "ptolemy.domains.de.lib.VariableDelay", variableDelayChanges);

        // Server: After 4.1, 'serviceTime'
        // property is now 'newServiceTime'
        // Regrettably, after 5.1, this reverted to serviceTime.
        HashMap serverChanges = new HashMap<String, String>();
        serverChanges.put("newServiceTime", "serviceTime");
        _classesWithParameterNameChanges.put("ptolemy.domains.de.lib.Server",
                serverChanges);

        // CodeGenerator: After 7.2, 'generateJNI'
        // property is now 'generateEmbeddedCode'
        {
            HashMap codegen = new HashMap<String, String>();
            codegen.put("generateJNI", "generateEmbeddedCode");
            _classesWithParameterNameChanges.put(
                    "ptolemy.codegen.kernel.CodeGenerator", codegen);
        }
        {
            HashMap codegen = new HashMap<String, String>();
            codegen.put("generateJNI", "generateEmbeddedCode");
            _classesWithParameterNameChanges.put(
                    "ptolemy.codegen.kernel.StaticSchedulingCodeGenerator",
                    codegen);
        }
        {
            HashMap codegen = new HashMap<String, String>();
            codegen.put("generateJNI", "generateEmbeddedCode");
            _classesWithParameterNameChanges.put(
                    "ptolemy.codegen.c.kernel.CCodeGenerator", codegen);
        }

        HashMap embeddedCodeActorChanges = new HashMap<String, String>();
        embeddedCodeActorChanges.put("embeddedCCode", "embeddedCode");
        _classesWithParameterNameChanges.put(
                "ptolemy.cg.lib.EmbeddedCodeActor", embeddedCodeActorChanges);
    }
}
