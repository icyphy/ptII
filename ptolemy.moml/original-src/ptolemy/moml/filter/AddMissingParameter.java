/* Add a missing parameter.

 Copyright (c) 2012-2014 The Regents of the University of California.
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
import java.util.Map;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// AddMissingParameter

/** Add a missing parameter.

  <p>If a SDFDirector does not have an iterations parameter, then
  add one with the value "0", which is the default for Ptolemy II
  8.0 and earlier.</p>

  <p>FIXME: This class is similar to AddEditorFactory.
  The two classes should be merged by parameterizing.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class AddMissingParameter extends MoMLFilterSimple {
    /**  Identify classes that need to have parameter added.
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
            // Save the name of the for later use if we see a "class"
            _lastNameSeen = attributeValue;

            if (_currentlyProcessingActorThatMayNeedUpdating) {
                if (attributeValue.equals(_addProperty.propertyName)) {
                    _currentlyProcessingActorThatMayNeedUpdating = false;
                    _currentAttributeHasValue = false;
                } else if (attributeValue.equals(_addProperty.onlyAddIfPresent)) {
                    // We only add _editorFactory to parameters that
                    // have _locations
                    _currentAttributeHasValue = true;
                }
            }
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (_namedObjsWithMissingProperties.containsKey(attributeValue)) {
                _currentlyProcessingActorThatMayNeedUpdating = true;

                //System.out.println("AddMissingParameter: " + _lastNameSeen + " " + attributeValue);
                if (container != null) {
                    _currentActorFullName = container.getFullName() + "."
                            + _lastNameSeen;
                } else {
                    _currentActorFullName = "." + _lastNameSeen;
                }
                _addProperty = _namedObjsWithMissingProperties
                        .get(attributeValue);
            } else if (_currentlyProcessingActorThatMayNeedUpdating
                    && container != null
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(
                            _currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes, so
                _currentlyProcessingActorThatMayNeedUpdating = false;
                //System.out.println("AddMissingParameter: " + _lastNameSeen + " " + attributeValue + " setting to _currentlyProcessingActorThatMayNeedUpdating to false");
                _currentAttributeHasValue = false;
                _addProperty = null;
            }
        }

        return attributeValue;
    }

    /** Make modifications to the specified container, which is
     *  defined in a MoML element with the specified name.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @exception Exception if there is a problem substituting
     *  in the new value.
     *  @deprecated Use {@link #filterEndElement(NamedObj, String, StringBuffer, String, MoMLParser)}
     * instead and pass a MoMLParser.
     */
    @Deprecated
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {
        filterEndElement(container, elementName, currentCharData, xmlFile,
                new MoMLParser());
    }

    /** Make modifications to the specified container, which is
     *  defined in a MoML element with the specified name.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *   only in the doc and configure elements
     *  @param xmlFile The file currently being parsed.
     *  @param parser The parser in which MoML is optionally evaluated.
     *  @exception Exception if there is a problem substituting
     *  in the new value.
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile, MoMLParser parser)
                    throws Exception {
        //          System.out.println("AddMissingParameter: filterEndElement: "
        //                  + _currentlyProcessingActorThatMayNeedUpdating
        //                  + " elementName: " + (elementName == null? "null" : elementName)
        //                  + "\n            container: " + (container == null ? "null" : container.getFullName())
        //                  + "\n currentActorFullName: " + _currentActorFullName);

        if (!_currentlyProcessingActorThatMayNeedUpdating) {
            return;
        } else if (_addProperty != null
                && (_addProperty.onlyAddIfPresent != null
                && _currentAttributeHasValue || _addProperty.onlyAddIfPresent == null)
                && elementName != null && elementName.equals("property")
                && container != null
                && container.getFullName().equals(_currentActorFullName)) {

            //System.out.println("AddMissingParameter: filterEndElement: Processing!!!");
            _currentlyProcessingActorThatMayNeedUpdating = false;
            _currentAttributeHasValue = false;

            // We use parse moml here so that we can avoid adding
            // dependencies.  For SDFIterations, we could just
            // add an attribute, but this class should be
            // extended to be more general.

            // setContext calls parser.reset()
            parser.setContext(container);

            String moml = _addProperty.moml;
            _addProperty = null;

            //System.out.println("AddMissingParameter: filterEndElement: moml: " + moml);
            try {
                // Do not call parse(moml) here, since that method
                // will fail if we are in an applet because it tries
                // to read user.dir
                parser.parse(null, moml);
                MoMLParser.setModified(true);
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex, "Failed to parse\n"
                        + moml);
            }
        }
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(
                getClass().getName()
                + ": If a NamedObj is missing a property, then add it.\n"
                + "Optionally, only add the property if another property, "
                + "such as _location is present.\n"
                + "Below are the property names, the optional property and the moml:\n");
        for (Map.Entry<String, AddProperty> entry : _namedObjsWithMissingProperties
                .entrySet()) {
            String namedObjName = entry.getKey();
            AddProperty addProperty = entry.getValue();
            results.append(namedObjName
                    + "\t -> "
                    + addProperty.propertyName
                    + "\t"
                    + (addProperty.onlyAddIfPresent == null ? "null"
                            : addProperty.onlyAddIfPresent) + "\n\t"
                            + addProperty.moml + "\n");
        }
        return results.toString();
    }

    /** A Structure that contains the property name, the moml and
     *  the optional property name that, if non-null, must be present.
     */
    private static class AddProperty {
        AddProperty(String propertyName, String moml, String onlyAddIfPresent) {
            this.propertyName = propertyName;
            this.moml = moml;
            this.onlyAddIfPresent = onlyAddIfPresent;
        }

        /** The property name to be added if it is missing. */
        public String propertyName;
        /** The moml to be added */
        public String moml;
        /** Only add the parameter if the attribute has a parameter
         *  that has the value equal to the value of onlyAddIfPresent.
         *  If this field is null, then the parameter is always added
         *  if propertyName is not present.
         */
        public String onlyAddIfPresent;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map of NamedObject names to an object of
    private static HashMap<String, AddProperty> _namedObjsWithMissingProperties;

    // Cache of parameters name to be added.
    private static AddProperty _addProperty;

    // The the full name of the actor we are currently processing
    private String _currentActorFullName;

    // Set to true if the current attribute has a value that matches.
    // This variable is used to determine whether we need to add an
    // iteration
    private boolean _currentAttributeHasValue = false;

    // Set to true if we are currently processing an actor that may
    // need an update, set to false when we are done.
    private boolean _currentlyProcessingActorThatMayNeedUpdating = false;

    // Last "name" value seen, for use if we see a "class".
    private String _lastNameSeen;

    static {
        _namedObjsWithMissingProperties = new HashMap<String, AddProperty>();

        // SDFDirector
        AddProperty sdfDirectorChanges = new AddProperty("iterations",
                "<property " + "name=\"iterations\" "
                        + "class=\"ptolemy.data.expr.Parameter\" "
                        + "value=\"0\"/>", null);
        _namedObjsWithMissingProperties.put(
                "ptolemy.domains.sdf.kernel.SDFDirector", sdfDirectorChanges);

        // EditorFactory
        AddProperty editorFactoryChanges = new AddProperty("_editorFactory",
                "<property name=\"_editorFactory\""
                        + " class=\"ptolemy.vergil.toolbox."
                        + "VisibleParameterEditorFactory\"/>", "_location");

        _namedObjsWithMissingProperties.put("ptolemy.data.expr.Parameter",
                editorFactoryChanges);
    }
}
