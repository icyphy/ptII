/* Add icons to certain actors

 Copyright (c) 2002-2014 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// AddIcon

/** Certain actors have specialized icons that display the value of
 one of the parameters.  This filter adds icons to those actors when
 necessary.

 @author Christopher Hylands, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class AddIcon extends MoMLFilterSimple {
    /**  If the attributeName is "class" and attributeValue names a
     *        class that has had its port names changed between releases,
     *  then substitute in the new port names.
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

            if (_currentlyProcessingActorThatMayNeedAnIcon
                    && attributeValue.equals("_icon")) {
                // We are processing an annotation and it already
                // has _icon
                _currentlyProcessingActorThatMayNeedAnIcon = false;
            }
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (_actorsThatShouldHaveIcons.containsKey(attributeValue)) {
                // We found a class that needs an _icon
                _currentlyProcessingActorThatMayNeedAnIcon = true;

                if (container != null) {
                    _currentActorFullName = container.getFullName() + "."
                            + _lastNameSeen;
                } else {
                    _currentActorFullName = "." + _lastNameSeen;
                }

                _iconMoML = (String) _actorsThatShouldHaveIcons
                        .get(attributeValue);
            } else if (_currentlyProcessingActorThatMayNeedAnIcon
                    && container != null
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(
                            _currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes, so
                _currentlyProcessingActorThatMayNeedAnIcon = false;
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
        if (_currentlyProcessingActorThatMayNeedAnIcon
                && elementName.equals("entity") && container != null
                && container.getFullName().equals(_currentActorFullName)) {
            _currentlyProcessingActorThatMayNeedAnIcon = false;

            // Note that setContext() calls reset() so we don't want
            // to do it on the main parser.
            parser.setContext(container);

            try {
                // Do not call parse(_iconMoML) here, since that method
                // will fail if we are in an applet because it tries
                // to read user.dir
                parser.parse(null, _iconMoML);
                MoMLParser.setModified(true);
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex, "Failed to parse\n"
                        + _iconMoML);
            }
        }
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Add specialized icons that display the value\n"
                + "of one of the parameters.\n" + "The affected actors are:\n");
        Iterator actors = _actorsThatShouldHaveIcons.keySet().iterator();

        while (actors.hasNext()) {
            results.append("\t" + (String) actors.next() + "\n");
        }

        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Map of actors that should have _icon
    private static HashMap _actorsThatShouldHaveIcons;

    // The the full name of the actor we are currently processing
    private String _currentActorFullName;

    // Set to true if we are currently processing an actor that may
    // need _icon added, set to false when we are done.
    private boolean _currentlyProcessingActorThatMayNeedAnIcon = false;

    // The moml that we should substitute in if we need to add
    // an _icon
    private String _iconMoML;

    // Last "name" value seen, for use if we see a "class".
    private String _lastNameSeen;

    static {
        ///////////////////////////////////////////////////////////
        // Actors that should have _icon
        _actorsThatShouldHaveIcons = new HashMap();

        // In alphabetic order by actor class name.
        _actorsThatShouldHaveIcons
                .put("ptolemy.actor.lib.Const",
                        "<property name=\"_icon\" "
                                + "class=\"ptolemy.vergil.icon.BoxedValueIcon\">\n"
                                + "<property name=\"attributeName\" value=\"value\"/>\n"
                                + "<property name=\"displayWidth\" value=\"40\"/>\n"
                                + "</property>\n");

        // In alphabetic order by actor class name.
        _actorsThatShouldHaveIcons
                .put("ptolemy.actor.lib.Expression",
                        "<property name=\"_icon\" "
                                + "class=\"ptolemy.vergil.icon.BoxedValueIcon\">\n"
                                + "<property name=\"attributeName\" value=\"expression\"/>\n"
                                + "<property name=\"displayWidth\" value=\"60\"/>\n"
                                + "</property>\n");

        String functionIcon = "<property name=\"_icon\" "
                + "class=\"ptolemy.vergil.icon.AttributeValueIcon\">\n"
                + "<property name=\"attributeName\" value=\"function\"/>\n"
                + "</property>\n";

        _actorsThatShouldHaveIcons.put("ptolemy.actor.lib.MathFunction",
                functionIcon);

        _actorsThatShouldHaveIcons
                .put("ptolemy.actor.lib.Scale",
                        "<property name=\"_icon\" "
                                + "class=\"ptolemy.vergil.icon.AttributeValueIcon\">\n"
                                + "<property name=\"attributeName\" value=\"factor\"/>\n"
                                + "</property>\n");

        _actorsThatShouldHaveIcons.put("ptolemy.actor.lib.TrigFunction",
                functionIcon);
    }
}
