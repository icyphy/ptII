/* Attribute for inserting HTML text into the page exported by Export to Web.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import java.awt.Color;

import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.vergil.icon.TextIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;


///////////////////////////////////////////////////////////////////
//// HTMLText
/**
 * Attribute for inserting HTML text into the page exported by Export to Web.
 * Drag its icon onto the background of a model, and specify the HTML text to
 * export (double click on the attribute to set the text).
 * By default, this text will be placed before the image for the model,
 * after the title,  * but you can change the position by setting the <i>textPosition</i>
 * parameter.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class HTMLText extends StringParameter implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public HTMLText(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _icon = new TextIcon(this, "_icon");
        _icon.setTextColor(Color.RED);
        _icon.setIconText("H");
        
        displayText = new StringParameter(this, "displayText");
        displayText.setExpression("HTML Text for Export to Web");

        textPosition = new HTMLTextPosition(this, "textPosition");
        
        height = new Parameter(this, "height");
        height.setTypeEquals(BaseType.INT);
        height.setExpression("20");
        
        width = new Parameter(this, "width");
        width.setTypeEquals(BaseType.INT);
        width.setExpression("60");

        TextStyle style = new TextStyle(this, "style");
        style.height.setExpression("height");
        style.width.setExpression("width");

        new SingletonAttribute(this, "_hideName");
        new VisibleParameterEditorFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Parameter giving the text to display in the Ptolemy model.
     *  This defaults to "HTML Text for Export to Web".
     */
    public StringParameter displayText;
    
    /** Parameter specifying the height of the editing box.
     *  This is an int that defaults to 20.
     */
    public Parameter height;

    /** Parameter specifying the position into which to export HTML text.
     * The parameter offers the following possibilities:
     *  <ul>
     *  <li><b>end</b>: Put the text at the end of the HTML file.
     *  <li><b>header</b>: Put the text in the header section.
     *  <li><b>start</b>: Put the text at the start of the body section.
     *  <li><i>anything_else</i>: Put the text in a separate HTML file
     *   named <i>anything_else</i>.
     *  </ul>
     *  The default is "start".
     */
    public HTMLTextPosition textPosition;
    
    /** Parameter specifying the width of the editing box.
     *  This is an int that defaults to 60.
     */
    public Parameter width;

    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to update the icon.
     *  @param attribute The attribute that changed.
     */
    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == displayText) {
            _icon.setText(displayText.stringValue());
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This may include, for example, HTML or header
     *  content, including for example JavaScript definitions that
     *  may be needed by the area attributes.
     *  @throws IllegalActionException If parameters cannot be evaluated.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
        String content = stringValue();
        String position = textPosition.stringValue();
        exporter.addContent(position, false, content);
    }

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class does not provide any such content.
     */
    public void provideOutsideContent(WebExporter exporter) {
        // This class does not provide outside content.
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Icon. */
    private TextIcon _icon;
}
