/* Add a VisibleParameterEditorFactor named _editorFactor to certain Parameters

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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// AddEditorFactory

/** Add a VisibleParameterEditorFactory named _editorFactory to certain
 Parameters.

 @author Christopher Hylands, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @deprecated Use {@link ptolemy.moml.filter.AddMissingParameter} instead because it handles multiple missing parameter at once.
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
@Deprecated
public class AddEditorFactory extends MoMLFilterSimple {
    /**  Identify Parameters that need a VisibleParameterEditorFactory
     *   named _editorFactory added.
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

            if (_currentlyProcessingActorThatMayNeedAnEditorFactory) {
                if (attributeValue.equals("_editorFactory")) {
                    // We are processing a Parameter that already has a
                    // _editorFactory
                    _currentlyProcessingActorThatMayNeedAnEditorFactory = false;
                    _currentAttributeHasLocation = false;
                } else if (attributeValue.equals("_location")) {
                    // We only add _editorFactory to parameters that
                    // have locations
                    _currentAttributeHasLocation = true;
                }
            }
        }

        // If you change this class, you should run before and after
        // timing tests on large moml files, a good command to run
        // is:
        // $PTII/bin/ptolemy -test $PTII/ptolemy/domains/ct/demo/CarTracking/CarTracking.xml
        // which will open up a large xml file and then close after 2 seconds.
        if (attributeName.equals("class")) {
            if (attributeValue.equals("ptolemy.data.expr.Parameter")) {
                _currentlyProcessingActorThatMayNeedAnEditorFactory = true;

                if (container != null) {
                    _currentActorFullName = container.getFullName() + "."
                            + _lastNameSeen;
                } else {
                    _currentActorFullName = "." + _lastNameSeen;
                }
            } else if (_currentlyProcessingActorThatMayNeedAnEditorFactory
                    && container != null
                    && !container.getFullName().equals(_currentActorFullName)
                    && !container.getFullName().startsWith(
                            _currentActorFullName)) {
                // We found another class in a different container
                // while handling a class with port name changes, so
                _currentlyProcessingActorThatMayNeedAnEditorFactory = false;
                _currentAttributeHasLocation = false;
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
        if (!_currentlyProcessingActorThatMayNeedAnEditorFactory) {
            return;
        } else if (_currentAttributeHasLocation && elementName != null
                && elementName.equals("property") && container != null
                && container.getFullName().equals(_currentActorFullName)) {
            _currentlyProcessingActorThatMayNeedAnEditorFactory = false;
            _currentAttributeHasLocation = false;

            // In theory, we could do something like the lines below
            // but that would mean that the moml package would depend
            // on the vergil.toolbox package.
            //
            // VisibleParameterEditorFactor _editorFactory =
            //        new VisibleParameterEditorFactory(container, "_editorFactory");

            // setContext calls parser.reset()
            parser.setContext(container);

            String moml = "<property name=\"_editorFactory\""
                    + "class=\"ptolemy.vergil.toolbox."
                    + "VisibleParameterEditorFactory\">" + "</property>";

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
        return getClass().getName()
                + ": If a parameter has a _location, then\n"
                + "add a VisibleParameterEditorFactory named _editorFactory.\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The the full name of the actor we are currently processing
    private String _currentActorFullName;

    // Set to true if the current attribute has a _location attribute.
    // This variable is used to determine whether we need to add  a
    // _editorFactory.
    private boolean _currentAttributeHasLocation = false;

    // Set to true if we are currently processing an actor that may
    // need _editorFactory added, set to false when we are done.
    private boolean _currentlyProcessingActorThatMayNeedAnEditorFactory = false;

    // Last "name" value seen, for use if we see a "class".
    private String _lastNameSeen;
}
