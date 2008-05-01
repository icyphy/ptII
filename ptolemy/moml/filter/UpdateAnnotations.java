/* Update annotations

 Copyright (c) 2008 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Configurable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;
import ptolemy.kernel.CompositeEntity;
import ptolemy.vergil.kernel.attributes.TextAttribute;

//////////////////////////////////////////////////////////////////////////
//// UpdateAnnotations

/** When this class is registered with the MoMLParser.addMoMLFilter()
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
        &lt;property name="text" class="ptolemy.kernel.util.StringAttribute" value="Create a state machine here (and ports, if needed) and&#10;create refinements for the states. Each refinement needs a director.&#10;For hybrid system models, use the CTEmbeddedDirector.&lt;/text&gt;&lt;/svg&gt;&lt;/configure&gt;
        &lt;/property&gt;
        &lt;property name="_location" class="ptolemy.kernel.util.Location" value="[75.0, 65.0]"&gt;
        &lt;/property&gt;
    &lt;/property&gt;
 </pre>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 7.0.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class UpdateAnnotations implements MoMLFilter {
    /** Update annotations by removing old annotations and
     *  replacing them with new annotation
     *  If the attributeName is "name" and attributeValue begins with
     *  with "annotation", and a later attributeName is class and the
     *  attributeValue is "ptolemy.kernel.util.Attribute" then replace
     *  the property with a TextAttribute
     *
     *  @param container  The container for this attribute.
     *  in this method.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @return the value of the attributeValue argument.
     */
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue) {
        //System.out.println("filterAttributeValue: " + container + "\t"
        //       +  attributeName + "\t" + attributeValue);
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) { 
            if (attributeValue.startsWith("annotation")) {
                // We found a line like
                // <property name="annotation1"
                //           class="ptolemy.kernel.util.Attribute">
                _currentlyProcessingAnnotation = true;
                _currentAnnotationName = attributeValue;
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
//             if (attributeName.equals("class")
//                     && ! attributeValue.equals("ptolemy.kernel.util.Attribute")
//                     && container.getFullName().equals(_currentAnnotationContainerFullName)) {
//                 // We have an annotation, but the class is not Attribute, so skip out
//                 _reset();
//             }
            if (_currentlyProcessingLocation
                    && attributeName.equals("value")) {
                // Found the location
                _currentlyProcessingLocation = false;
                _currentLocation = attributeValue;
            }                
        }
        if (_currentlyProcessingAnnotation
                && container != null
                && !container.getFullName().equals(_currentAnnotationFullName)
                && ((_currentAnnotationFullName == null) || ((_currentAnnotationFullName != null) && !_currentAnnotationFullName
                        .startsWith(container.getFullName())))
                && !container.getFullName().startsWith(
                        _currentAnnotationFullName)) {
            // We found another class in a different container
            // while handling an annotation.
            _reset();
        }

        return attributeValue;
    }

    /** Make modifications to the specified container, which is
     *  defined in a MoML element with the specified name.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     *  @param currentCharData The character data, which appears
     *  only in the doc and configure elements
     *  @exception Exception if there is a problem substituting
     *  in the new value.
     */
    public void filterEndElement(NamedObj container, String elementName,
            StringBuffer currentCharData) throws Exception {
        System.out.println("filterEndElement: " + container + "\t"
                +  elementName
                + " container.fn: " +  container.getFullName()
                + " _cafn: " + _currentAnnotationFullName
                + " _cacfn: " + _currentAnnotationContainerFullName
                + "\n" + currentCharData);

        //if (!elementName.equals("configure")) {
        //    return;
        //}

        if (!_currentlyProcessingAnnotation) {
            return;
        }

        if (elementName.equals("configure")) {
            Attribute currentAttribute = (Attribute) container;
            NamedObj parentContainer = currentAttribute.getContainer();
            if ( ! (parentContainer instanceof Attribute)) {
                // ptolemy.domains.fsm.modal.ModalController cannot be cast to ptolemy.kernel.util.Attribute
                return;
            }
            
            if (_textAttribute == null) {
                System.out.println("UpdateAnnotation: creating textAttribute1");
                NamedObj grandparentContainer = currentAttribute.getContainer().getContainer();
                //((Attribute)parentContainer).setContainer(null);

                _textAttribute = new TextAttribute(grandparentContainer,
                        grandparentContainer.uniqueName(_currentAnnotationName));
            }


            // Clean up the character data
            String charData = currentCharData.toString().trim();
            if (charData.startsWith("<svg>")) {
                charData = charData.substring(5).trim();
            }
            if (charData.endsWith("</svg>")) {
                charData = charData.substring(0,charData.length() - 6).trim();
            }
            if (charData.endsWith("</text>")) {
                charData = charData.substring(0,charData.length() - 7).trim();
            }
            charData = charData.replaceAll("<text.*[^>]>", "");
            _textAttribute.text.setExpression(charData);
            _textAttribute.text.validate();
            System.out.println("UpdateAnnotation: setting textAttribute: " + charData);
            System.out.println("UpdateAnnotation: textAttribute is now: " + _textAttribute.text.getExpression());

        }

        if (container instanceof Location) {
            System.out.println("UpdateAnnotation: closing "); 
            Attribute currentAttribute = (Attribute) container;
            if (_textAttribute == null) {
                System.out.println("UpdateAnnotation: creating textAttribute2");
                NamedObj parentContainer = currentAttribute.getContainer();
                NamedObj grandparentContainer = currentAttribute.getContainer().getContainer();
                //((Attribute)parentContainer).setContainer(null);
                _textAttribute = new TextAttribute(grandparentContainer,
                        grandparentContainer.uniqueName(_currentAnnotationName));
            }
            //            TextAttribute textAttribute = (TextAttribute) currentAttribute.getContainer().getAttribute(_currentAnnotationName,
            //                    TextAttribute.class);

            //currentAttribute.setContainer(_textAttribute);
            Location location = new Location(_textAttribute, "_location");
            //            System.out.println("UpdateAnnotation: _currentLocation: " + _currentLocation);
            //            location.setExpression(_currentLocation);
            //System.out.println("UpdateAnnotation: _currentLocation: " + _currentLocation + " " + ((Location)container).getExpression());
            location.setExpression(((Location)container).getExpression());

            location.validate();
        }        

        if ((container != null)
                && container.getFullName().equals(_currentAnnotationFullName)) {
            System.out.println("UpdateAnnotation: removing " + container.getFullName());
            // We are at the end of the old annotation, remove it by
            // setting its container to null
            Attribute currentAttribute = (Attribute) container;
            String name = currentAttribute.getName(); 
            currentAttribute.setContainer(null);
            // Rename to the original name.
            _textAttribute.setName(name);
            //NamedObj parentContainer = currentAttribute.getContainer();
            //((Attribute)parentContainer).setContainer(null);
            _reset();
        }
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        return getClass().getName() + ": Update annotation to new style\n";
    }

    private void _reset() {
        _currentlyProcessingAnnotation = false;
        _currentAnnotationContainerFullName = null;
        _currentAnnotationFullName = null;
        _currentAnnotationName = null;
        _currentlyProcessingLocation = false;
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

    // The the short name (without the container) of the annotation we
    // are currently processing.
    private String _currentAnnotationName;

    // True if we are currently processing a location inside an annotation.
    private boolean _currentlyProcessingLocation = false;

    // The location of the annotation.
    private String _currentLocation;

    // The TextAttribute that is being created.
    private TextAttribute _textAttribute;
}
