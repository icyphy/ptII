/* Remove graphical classes

 Copyright (c) 1998-2003 The Regents of the University of California.
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

import java.util.HashMap;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// RemoveGraphicalClasses
/** When this class is registered with the MoMLParser.setMoMLFilter()
method, it will cause MoMLParser to filter out graphical classes.

<p>This is very useful for running applets with out requiring files
like diva.jar to be downloaded.  It is also used by the nightly build to
run tests when there is no graphical display present.

@author  Edward A. Lee, Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/

public class RemoveGraphicalClasses implements MoMLFilter {

    /** If the attributeValue is "ptolemy.vergil.icon.ValueIcon",
     *  or "ptolemy.vergil.basic.NodeControllerFactory"
     *  then return "ptolemy.kernel.util.Attribute"; if the attributeValue
     *  is "ptolemy.vergil.icon.AttributeValueIcon" or
     *  "ptolemy.vergil.icon.BoxedValueIcon" then return null, which
     *  will cause the MoMLParser to skip the rest of the element;
     *  otherwise return the original value of the attributeValue.
     *
     *  @param container  The container for this attribute, ignored
     *  in this method.
     *  @param attributeName The name of the attribute, ignored
     *  in this method.
     *  @param attributeValue The value of the attribute.
     *  @return the filtered attributeValue.
     */
    public String filterAttributeValue(NamedObj container,
            String attributeName, String attributeValue) {

        // If the nightly build is failing with messages like:
        // " X connection to foo:0 broken (explicit kill or server shutdown)."
        // Try uncommenting the next lines to see what is being
        // expanding before the error:
        // System.out.println("filterAttributeValue: " + container + "\t"
        //   +  attributeName + "\t" + attributeValue);

        if (attributeValue == null) {
            return null;
        } else if (_graphicalClasses.containsKey(attributeValue)) {
            MoMLParser.setModified(true);
            return (String) _graphicalClasses.get(attributeValue);
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
        return elementName;
    }

    /** Remove a class to be filtered
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @see #put(String, String)
     */
    public void remove(String className) {
        // ptolemy.copernicus.kernel.MakefileGenerator
        // so as to filter out the GeneratorAttribute
        _graphicalClasses.remove(className);
    }

    /** Add a class to be filtered for and its replacement if the class
     *  is found.  If the replacement is null, then the rest of the
     *  attribute is skipped.  Note that if you add a class with
     *  this method, then you must remove it with {@link #remove(String)},
     *  calling 'new RemoveGraphicalClasses' will not remove a class
     *  that was added with this method.
     *  @param className The name of the class to be filtered
     *  out, for example "ptolemy.copernicus.kernel.GeneratorAttribute".
     *  @param replacement The name of the class to be used if
     *  className is found.  If this argument is null then the
     *  rest of the attribute is skipped.
     *  @see #remove(String)
     */
    public void put(String className, String replacement) {
        // ptolemy.copernicus.kernel.KernelMain call this method
        // so as to filter out the GeneratorAttribute
        _graphicalClasses.put(className, replacement);
    }

    /** Return a string that describes what the filter does.
     *  @return the description of the filter that ends with a newline.
     */
    public String toString() {
        StringBuffer results =
            new StringBuffer(getClass().getName()
                    + ": Remove or replace classes that are graphical.\n"
                    + "This filter is used by the nightly build, and\n"
                    + "can be used to run applets so that files like\n"
                    + "diva.jar do not need to be downloaded.\n"
                    + "The following actors are affected:\n"
                             );
        Iterator classNames = _graphicalClasses.keySet().iterator();
        while (classNames.hasNext()) {
            String oldClassName = (String)classNames.next();
            String newClassName = (String) _graphicalClasses.get(oldClassName);
            if (newClassName == null) {
                results.append(oldClassName + " will be removed\n");
            } else {
                results.append(oldClassName + " will be replaced by "
                        + newClassName +"\n");
            }
        }
        return results.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map of actor names a HashMap of graphical classes to their
     *  non-graphical counterparts, usually either
     *  ptolemy.kernel.util.Attribute or null.
     */
    private static HashMap _graphicalClasses;

    static {
        _graphicalClasses = new HashMap();
        // Alphabetical by key class

        // It would be nice if we could substitute in Discard actors
        // when we see the plotter or the display.
        //_graphicalClasses.put("ptolemy.actor.lib.gui.Display",
        //        "ptolemy.actor.lib.Discard");

        // Generated applet from moml/demo/modulation.xml
        // fails to run if substitute Attribute for NodeControllerFactory
        // so we set it to null instead.
        _graphicalClasses.put("ptolemy.vergil.toolbox.AnnotationEditorFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.toolbox.VisibleParameterEditorFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.fsm.modal.HierarchicalStateControllerFactory",
                "ptolemy.kernel.util.Attribute");
        _graphicalClasses.put("ptolemy.vergil.fsm.modal.ModalTableauFactory",
                "ptolemy.kernel.util.Attribute");
        
        // 4/04 BooleanSwitch uses EditorIcon
        _graphicalClasses.put(
                "ptolemy.vergil.icon.EditorIcon",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.EllipseAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.FilledShapeAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.ImageAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.LineAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.ShapeAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.PolygonAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.RectangleAttribute",
                null);

        _graphicalClasses.put(
                "ptolemy.vergil.kernel.attributes.TextAttribute",
                null);

        _graphicalClasses.put("ptolemy.vergil.basic.NodeControllerFactory",
                null);
        _graphicalClasses.put("ptolemy.vergil.icon.AttributeValueIcon",
                null);
        _graphicalClasses.put("ptolemy.vergil.icon.BoxedValueIcon",
                null);
        _graphicalClasses.put("ptolemy.vergil.icon.CopyCatIcon",
                null);
        _graphicalClasses.put("ptolemy.vergil.icon.XMLIcon",
                null);
        // ptolemy/actor/lib/test/auto/StopSDF.xml has a MonitorValue actor,
        // so remove the UpdatedValueIcon.
        _graphicalClasses.put("ptolemy.vergil.icon.UpdatedValueIcon",
                null);
        _graphicalClasses.put("ptolemy.vergil.icon.ValueIcon",
                "ptolemy.kernel.util.Attribute");
        // Generated applet from moml/demo/modulation.xml
        // fails to run if substitute Attribute for AnnotationEditorFactory
        // so we set it to null instead.

        //_graphicalClasses.put("ptolemy.vergil.toolbox.AnnotationEditorFactory",
        //        "ptolemy.kernel.util.Attribute");

        _graphicalClasses.put("ptolemy.vergil.toolbox.AnnotationEditorFactory",
                null);
        _graphicalClasses.put("ptolemy.vergil.toolbox"
                + ".VisibleParameterEditorFactory",
                "ptolemy.kernel.util.Attribute");

        // Shallow CG of actor/lib/test/auto/URLDirectoryReader3.xml fails
        // unless we remove CheckBoxStyle
        _graphicalClasses.put("ptolemy.actor.gui.style.CheckBoxStyle", null);
    }
}

