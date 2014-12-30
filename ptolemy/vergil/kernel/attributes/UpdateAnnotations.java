/* Update annotations

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.vergil.kernel.attributes;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.MoMLFilterSimple;

///////////////////////////////////////////////////////////////////
//// UpdateAnnotations

/**
 Update the annotations.
 <p>When this class is registered with the MoMLParser.addMoMLFilter()
 method, it will cause MoMLParser to add a update annotations from
 the older style:
 <pre>
 &lt;property name="annotation" class="ptolemy.kernel.util.Attribute"&gt;
    &lt;property name="_hideName" class="ptolemy.kernel.util.SingletonAttribute"&gt;
    &lt;/property&gt;
    &lt;property name="_iconDescription" class="ptolemy.kernel.util.SingletonConfigurableAttribute"&gt;
          &lt;configure&gt;&lt;svg&gt;&lt;text x="20" y="20" style="font-size:14; font-family:SansSerif; fill:blue"&gt;Create a state machine here (and ports, if needed) and
create refinements for the states. Each refinement needs a director.
For hybrid system models, use the CTEmbeddedDirector.&lt;/text&gt;&lt;/svg&gt;&lt;/configure&gt;
    &lt;/property&gt;
    &lt;property name="_location" class="ptolemy.kernel.util.Location" value="75.0, 65.0"&gt;
    &lt;/property&gt;
    &lt;property name="_controllerFactory" class="ptolemy.vergil.basic.NodeControllerFactory"&gt;
    &lt;/property&gt;
    &lt;property name="_editorFactory" class="ptolemy.vergil.toolbox.AnnotationEditorFactory"&gt;
    &lt;/property&gt;
 &lt;/property&gt;
 </pre>
 to
 <pre>

    &lt;property name="annotation" class="ptolemy.vergil.kernel.attributes.TextAttribute"&gt;
        &lt;property name="text" class="ptolemy.kernel.util.StringAttribute" value="Create a state machine here (and ports, if needed) and&amp;#10;create refinements for the states. Each refinement needs a director.&amp;#10;For hybrid system models, use the CTEmbeddedDirector.&lt;/text&gt;&lt;/svg&gt;&lt;/configure&gt;
        &lt;/property&gt;
        &lt;property name="_location" class="ptolemy.kernel.util.Location" value="[75.0, 65.0]"&gt;
        &lt;/property&gt;
    &lt;/property&gt;
 </pre>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class UpdateAnnotations extends MoMLFilterSimple {

    /** Update annotations by removing old annotations and replacing
     *  them with new annotation. If the attributeName is "name" and
     *  attributeValue begins with with "annotation", then replace the
     *  property with a TextAttribute
     *
     *  @param container  The container for this attribute.
     *   in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return the value of the attributeValue argument.
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        //System.out.println("filterAttributeValue: " + container + "\t"
        //       +  attributeName + "\t" + attributeValue);

        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            if (attributeValue.startsWith("annotation")
                    || attributeValue.contains("annotation")
                    && attributeValue.contains(":")) {

                // We found a line like
                // <property name="annotation1"
                //           class="ptolemy.kernel.util.Attribute">
                _currentlyProcessingAnnotation = true;
                _currentAnnotationContainerFullName = container.getFullName();
                _currentAnnotationFullName = container.getFullName() + "."
                        + attributeValue;
                _currentlyProcessingLocation = false;
            } else {
                if (_currentlyProcessingAnnotation) {
                    if (attributeValue.equals("_location")) {
                        // We are processing a location
                        _currentlyProcessingLocation = true;
                    } else {
                        // Saw another name, done processing location
                        _currentlyProcessingLocation = false;
                    }
                }
            }
        } else if (_currentlyProcessingAnnotation) {
            if (attributeName.equals("class")
                    && attributeValue
                    .equals("ptolemy.vergil.kernel.attributes.TextAttribute")
                    && container.getFullName().equals(
                            _currentAnnotationContainerFullName)) {
                // We have an annotation, but it is a TextAttribute, so we are done.
                _reset();
                return attributeValue;
            }
            if (_currentlyProcessingLocation && attributeName.equals("value")) {
                // Found the location
                _currentlyProcessingLocation = false;
            }
        }
        if (_currentlyProcessingAnnotation) {
            if (container != null) {
                if (!container.getFullName().equals(_currentAnnotationFullName)) {
                    if (_currentAnnotationFullName == null
                            || (!_currentAnnotationFullName
                                    .startsWith(container.getFullName()) && !container
                                    .getFullName().startsWith(
                                            _currentAnnotationFullName))) {
                        // We found another class in a different container
                        // while handling an annotation.
                        _reset();
                    }
                }
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
     */
    @Override
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData, String xmlFile) throws Exception {

        if (!_currentlyProcessingAnnotation) {
            return;
        }

        // Useful for debugging
        //         System.out.println("filterEndElement: " + container + "\t"
        //                 +  elementName
        //                           + " container.fn: " +  container.getFullName()
        //                           + " _cafn: " + _currentAnnotationFullName
        //                           + " _cacfn: " + _currentAnnotationContainerFullName
        //                           + "\n" + currentCharData);

        // We have three cases:
        // 1) We have a configure, so we create the TextAttribute and
        //    populate it.
        // 2) We have a Location, so we possibly create the TextAttribute
        //    and we add a Location
        // 3) We are at the end of the annotation, so we delete the
        //    old annotation

        if (elementName.equals("configure")) {
            // 1) We have a configure, so we create the TextAttribute and
            //    populate it.
            Attribute currentAttribute = (Attribute) container;
            NamedObj parentContainer = currentAttribute.getContainer();
            if (currentAttribute.getName().equals("_smallIconDescription")) {
                // Skip the smallIconDescription it is probably -A-
                return;
            }
            if (!(parentContainer instanceof Attribute)) {
                // ptolemy.domains.modal.modal.ModalController cannot be cast to ptolemy.kernel.util.Attribute
                return;
            }

            if (_textAttribute == null) {
                //System.out.println("UpdateAnnotation: create TextAttribute 1");
                NamedObj grandparentContainer = currentAttribute.getContainer()
                        .getContainer();
                //((Attribute)parentContainer).setContainer(null);

                // Use a new name instead of annotation and avoid the HideAnnotationNames filter
                _textAttribute = new TextAttribute(grandparentContainer,
                        grandparentContainer.uniqueName("AnnotationUpdated"));
                //                        grandparentContainer.uniqueName(_currentAnnotationName));
            }

            // Clean up the character data: remove svg and text tags
            String charData = currentCharData.toString().trim();
            if (charData.startsWith("<svg>")) {
                charData = charData.substring(5).trim();
            }
            if (charData.endsWith("</svg>")) {
                charData = charData.substring(0, charData.length() - 6).trim();
            }
            if (charData.endsWith("</text>")) {
                charData = charData.substring(0, charData.length() - 7).trim();
            }

            // Map colors
            if (charData.contains(" fill:")) {
                if (charData.contains(" fill:black")) {
                    _textAttribute.textColor
                    .setExpression("{0.0, 0.0, 0.0, 1.0}");
                }
                if (charData.contains(" fill:darkgray")
                        || charData.contains(" fill:gray")) {
                    _textAttribute.textColor
                    .setExpression("{0.2, 0.2, 0.2, 1.0}");
                }
                if (charData.contains(" fill:green")) {
                    _textAttribute.textColor
                    .setExpression("{0.0, 1.0, 0.0, 1.0}");
                }
                if (charData.contains(" fill:red")) {
                    _textAttribute.textColor
                    .setExpression("{1.0, 0.0, 0.0, 1.0}");
                }
            }

            // Map font sizes
            if (charData.contains("font-size:")) {
                if (charData.contains("font-size:12")) {
                    _textAttribute.textSize.setExpression("12");
                }
                if (charData.contains("font-size:16")) {
                    _textAttribute.textSize.setExpression("16");
                }
                if (charData.contains("font-size:18")) {
                    _textAttribute.textSize.setExpression("18");
                }
            }

            charData = charData.replaceAll("<text.*[^>]>", "");
            //System.out.println("UpdateAnnotation: Setting TextAttribute");
            _textAttribute.text.setExpression(charData);
        }

        if (container instanceof Location) {
            // 2) We have a Location, so we possibly create the TextAttribute
            //    and we add a Location.
            Attribute currentAttribute = (Attribute) container;
            if (_textAttribute == null) {
                //System.out.println("UpdateAnnotation: create TextAttribute 2");
                //NamedObj parentContainer = currentAttribute.getContainer();
                NamedObj grandparentContainer = currentAttribute.getContainer()
                        .getContainer();
                //((Attribute)parentContainer).setContainer(null);
                // Use a new name instead of annotation and avoid the HideAnnotationNames filter
                _textAttribute = new TextAttribute(grandparentContainer,
                        grandparentContainer.uniqueName("AnnotationUpdated"));
            }
            Location location = new Location(_textAttribute, "_location");

            Location oldLocation = (Location) container;
            oldLocation.validate();
            //System.out.println("UpdateAnnotation: setting Location " + oldLocation.getExpression());
            double[] xyLocation = oldLocation.getLocation();

            xyLocation[0] += 15.0;
            location.setLocation(xyLocation);

            location.validate();
        }

        if (container != null
                && container.getFullName().equals(_currentAnnotationFullName)
                && _textAttribute != null) {
            // 3) We are at the end of the annotation, so we delete the
            //    old annotation by setting its container to null.
            //System.out.println("UpdateAnnotation: closing up " + _textAttribute.getContainer().getFullName() + "\n" + _textAttribute.exportMoML());
            //NamedObj top = _textAttribute.toplevel();
            Attribute currentAttribute = (Attribute) container;
            //String name = currentAttribute.getName();
            currentAttribute.setContainer(null);
            MoMLParser.setModified(true);
            _reset();
            //System.out.println("UpdateAnnotation: after reset" + top.exportMoML());
        }
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    @Override
    public String toString() {
        return getClass().getName() + ": Update annotation to new style\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Reset the internal state of the filter. */
    private void _reset() {
        _currentlyProcessingAnnotation = false;
        _currentAnnotationContainerFullName = null;
        _currentAnnotationFullName = null;
        _currentlyProcessingLocation = false;
        _textAttribute = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // True if we are currently processing an annotation.
    private boolean _currentlyProcessingAnnotation = false;

    // The the full name of the container of the annotation we are
    // currently processing.
    private String _currentAnnotationContainerFullName;

    // The the full name of the annotation we are currently processing.
    private String _currentAnnotationFullName;

    // True if we are currently processing a location inside an annotation.
    private boolean _currentlyProcessingLocation = false;

    // The TextAttribute that is being created.
    private TextAttribute _textAttribute;
}
