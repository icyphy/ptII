/* Attribute for inserting HTML text into the page exported by Export to Web.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.web;

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
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.TextIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

///////////////////////////////////////////////////////////////////
//// WebContent
/**
 * Base class for attributes defining content for export to web.
 * This class provides parameters that control the size of the text
 * box for editing the content and also a parameter that defines what
 * to display in the model.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public abstract class WebContent extends StringParameter implements
        WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public WebContent(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _icon = new TextIcon(this, "_icon");
        _icon.setTextColor(Color.RED);
        _icon.setIconText("H");

        displayText = new StringParameter(this, "displayText");
        displayText.setExpression("Content for Export to Web");

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
     *  This defaults to "Content for Export to Web".
     */
    public StringParameter displayText;

    /** Parameter specifying the height of the editing box.
     *  This is an int that defaults to 20.
     */
    public Parameter height;

    /** Parameter specifying the width of the editing box.
     *  This is an int that defaults to 60.
     */
    public Parameter width;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to update the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown while setting the
     *  icon text or by the superclass.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == displayText) {
            _icon.setText(displayText.stringValue());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        WebContent newObject = (WebContent) super.clone(workspace);
        try {
            newObject.getAttribute("_icon", TextIcon.class).setContainer(null);
            newObject._icon = new TextIcon(newObject, "_icon");
            newObject._icon.setTextColor(Color.RED);
            newObject._icon.setIconText("H");
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException(getFullName()
                    + ": Failed to clone: " + throwable);
        }
        return newObject;
    }

    /** Provide content to the specified web exporter.
     *  This may include, for example, HTML pages and fragments, Javascript
     *  function definitions and calls, CSS styling, and more. Throw an
     *  IllegalActionException if something is wrong with the web content.
     *
     *  Calls two methods, _provideAttributes() for generating content that
     *  would be an attribute of an element and _provideElements() for
     *  generating standalone elements.  Note that attributes may refer to
     *  other elements; therefore, the provideContent() method always calls both
     *  _provideAttributes() and _provideElements().  For example, a HTML
     *  attribute for a Javascript method call:
     *  <code>onclick="runMethod()"</code>
     *  requires that a
     *  <code>&lt;script&gt; function runMethod() { } &lt;/script&gt;</code> element be defined.
     *  Subclasses should override _provideAttributes() and _provideElements().
     *
     *  @param exporter The web exporter to be used
     *  @exception IllegalActionException If something is wrong with the web
     *  content.
     */
    @Override
    public void provideContent(WebExporter exporter)
            throws IllegalActionException {
        _provideAttributes(exporter);
        _provideElements(exporter);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate attribute web content.  Should call WebExporter's
     * defineAttribute() method.  Subclasses should override. Please also see
     * {@link ptolemy.vergil.basic.export.web.WebAttribute}
     *
     * @param  exporter  The WebExporter to write content to
     * @exception IllegalActionException If there is a problem creating the web
     * content.
     */
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {

    }

    /** Generate element web content.  Should call WebExporter's
     * defineElement() method.  Subclasses should override. Please also see
     * {@link ptolemy.vergil.basic.export.web.WebElement}
     *
     * @param  exporter  The WebExporter to write content to
     * @exception IllegalActionException If there is a problem creating the web
     * content.
     */
    protected void _provideElements(WebExporter exporter)
            throws IllegalActionException {

    }

    // Add createResource() method here that returns a URI?  Not every class
    // would need this.

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Icon. */
    protected TextIcon _icon;
}
