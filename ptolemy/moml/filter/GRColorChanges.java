/* A filter for backward compatibility with 4.0 or earlier GR models.

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

import java.util.HashSet;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// GRColorChanges

/**
 This class filters MoML files for backward compatibility between
 GR models constructed in version 4.0 or earlier. In particular, it
 handles the switch to using ColorAttribute for all colors, plus
 some parameter renaming. It also handles some conversions from
 matrix parameters to arrays.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class GRColorChanges extends MoMLFilterSimple {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Handle parameter name changes.
     *  @param container  The container for XML element.
     *  @param element The XML element name.
     *  @param attributeName The name of the attribute.
     *  @param attributeValue The value of the attribute.
     *  @param xmlFile The file currently being parsed.
     *  @return A new value for the attribute, or the same value
     *   to leave it unchanged, or null to cause the current element
     *   to be ignored (unless the attributeValue argument is null).
     */
    @Override
    public String filterAttributeValue(NamedObj container, String element,
            String attributeName, String attributeValue, String xmlFile) {
        if (attributeValue != null && attributeName != null) {
            if (attributeValue.equals("RGB color")
                    && attributeName.equals("name")
                    && container != null
                    && _actorsWithRGBColor.contains(container.getClass()
                            .getName())) {
                // NOTE: This relies on their being no nested
                // instance of this attribute, which there shouldn't
                // be.
                _foundOne = true;
                MoMLParser.setModified(true);
                return "diffuseColor";
            }
        }

        return attributeValue;
    }

    /** If the container is a property named "diffuseColor" contained
     *  by one of the GR actors, then check the format of its parameter
     *  to change it, if necessary, from matrix format to array format.
     *  Also fix the background color of ViewScreen3D.
     *  If the property name is "polygon" contained by an instance
     *  of PolyCylinder3D, perform a similar change from matrix
     *  to array.
     *  @param container The object defined by the element that this
     *   is the end of.
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
        if (_foundOne) {
            _foundOne = false;

            if (elementName != null
                    && elementName.equals("property")
                    && container.getName().equals("diffuseColor")
                    && ((Settable) container).getExpression().trim()
                    .startsWith("[")) {
                // Found one in matrix format.
                String value = ((Settable) container).getExpression().trim();
                value = value.replace('[', '{');
                value = value.replace(']', '}');
                ((Settable) container).setExpression(value);
                MoMLParser.setModified(true);
            }
        }

        // Fix the background color of the ViewScreen actor.
        // Note that the ViewScreen actor also has a name change.
        if (container != null && container.getName().equals("backgroundColor")) {
            NamedObj actor = container.getContainer();

            if (actor != null
                    && actor.getClass().getName()
                    .startsWith("ptolemy.domains.gr.lib.ViewScreen")) {
                String value = ((Settable) container).getExpression().trim();

                if (value.startsWith("[")) {
                    value = value.replace('[', '{');
                    value = value.replace(']', '}');
                    ((Settable) container).setExpression(value);
                    MoMLParser.setModified(true);
                }
            }
        }

        // Fix the polygon attribute of the PolyCylinder3D actor.
        if (container != null && container.getName().equals("polygon")) {
            NamedObj actor = container.getContainer();

            if (actor != null
                    && actor.getClass().getName()
                    .equals("ptolemy.domains.gr.lib.PolyCylinder3D")) {
                String value = ((Settable) container).getExpression().trim();

                if (value.startsWith("[")) {
                    value = value.replace('[', '{');
                    value = value.replace(']', '}');
                    ((Settable) container).setExpression(value);
                    MoMLParser.setModified(true);
                }
            }
        }

        // Fix the polyline attribute of the CircularSweep3D actor.
        if (container != null && container.getName().equals("polyline")) {
            NamedObj actor = container.getContainer();

            if (actor != null
                    && actor.getClass().getName()
                    .equals("ptolemy.domains.gr.lib.CircularSweep3D")) {
                String value = ((Settable) container).getExpression().trim();

                if (value.startsWith("[")) {
                    value = value.replace('[', '{');
                    value = value.replace(']', '}');
                    ((Settable) container).setExpression(value);
                    MoMLParser.setModified(true);
                }
            }
        }
    }

    /** Return a string that describes what the filter does.
     *  @return A description of the filter (ending with a newline).
     */
    @Override
    public String toString() {
        StringBuffer results = new StringBuffer(getClass().getName()
                + ": Update GR actor with the following changes:\n");
        results.append("\tParameter name \"RGB color\" --> \"diffuseColor\"");
        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Set of actors with parameter named "RGB color" */
    private static HashSet _actorsWithRGBColor = new HashSet();

    static {
        GRColorChanges._actorsWithRGBColor.add("ptolemy.domains.gr.lib.Box3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.CircularSweep3D");
        GRColorChanges._actorsWithRGBColor.add("ptolemy.domains.gr.lib.Cone3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.Cylinder3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.Loader3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.PolyCylinder3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.Sphere3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.TextString3D");
        GRColorChanges._actorsWithRGBColor
        .add("ptolemy.domains.gr.lib.Torus3D");
    }

    /** Flag indicating that we found one whose name needed changing. */
    private boolean _foundOne = false;
}
