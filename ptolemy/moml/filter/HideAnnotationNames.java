/* Filter that adds _hideName to annotations

Copyright (c) 2002-2005 The Regents of the University of California.
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
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;


//////////////////////////////////////////////////////////////////////////
//// HideAnnotationNames

/** When this class is registered with the MoMLParser.addMoMLFilter()
    method, it will cause MoMLParser to add a _hideName property
    property for any annotations.

    @author Christopher Hylands
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class HideAnnotationNames implements MoMLFilter {
    /** If the attributeName is "name" and attributeValue ends
     *  with "annotation", then
     *  <pre>
     *   <property name="_hideName" class="ptolemy.data.expr.SingletonParameter" value="true">
     *   </property>
     *  <pre>
     *  is added if it is not yet present.
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
        if (attributeValue == null) {
            // attributeValue == null is fairly common, so we check for
            // that first
            return null;
        }

        if (attributeName.equals("name")) {
            if (attributeValue.endsWith("annotation1")) {
                // We found a line like
                // <property name="13:0:0:annotation1"
                //          class="ptolemy.kernel.util.Attribute">
                _currentlyProcessingAnnotation = true;
                _currentAnnotationFullName = container.getFullName() + "."
                    + attributeValue;
            } else if (_currentlyProcessingAnnotation
                    && attributeValue.equals("_hideName")) {
                // We are processing an annotation and it already
                // has _hideName
                _currentlyProcessingAnnotation = false;
                _currentAnnotationFullName = null;
            }
        }

        if (_currentlyProcessingAnnotation && (container != null)
                && !container.getFullName().equals(_currentAnnotationFullName)
                && ((_currentAnnotationFullName == null)
                        || ((_currentAnnotationFullName != null)
                                && !_currentAnnotationFullName.startsWith(
                                        container.getFullName())))
                && !container.getFullName().startsWith(_currentAnnotationFullName)) {
            // We found another class in a different container
            // while handling an annotation.
            _currentlyProcessingAnnotation = false;
            _currentAnnotationFullName = null;
        }

        return attributeValue;
    }

    /** Make modifications to the specified container, which is
     *  defined in a MoML element with the specified name.
     *  @param container The object created by this element.
     *  @param elementName The element name.
     */
    public void filterEndElement(NamedObj container, String elementName)
            throws Exception {
        if (!elementName.equals("property")) {
            return;
        }

        if (_currentlyProcessingAnnotation && (container != null)
                && container.getFullName().equals(_currentAnnotationFullName)) {
            _currentlyProcessingAnnotation = false;
            _currentAnnotationFullName = null;

            try {
                SingletonParameter hide = new SingletonParameter(container,
                        "_hideName");
                hide.setToken(BooleanToken.TRUE);
                hide.setVisibility(Settable.EXPERT);

                MoMLParser.setModified(true);
            } catch (NameDuplicationException ex) {
                // Ignore, the container already has a _hideName.
                // The Network model has this problem.
            }
        }
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        return getClass().getName() + ": If an annotation name ends with\n"
            + "'annotation1', then add _hideName if necessary.\n";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // True if we are currently processing an annotation.
    private static boolean _currentlyProcessingAnnotation = false;

    // The the full name of the annotation we are currently processing
    private static String _currentAnnotationFullName;
}
