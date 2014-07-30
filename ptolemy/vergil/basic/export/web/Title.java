/* Attribute specifying a title for a model or component in a model.

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
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Collection;

import javax.swing.SwingConstants;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.TextIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

///////////////////////////////////////////////////////////////////
//// Title
/**
 * Attribute specifying a title for a model or a component in a model.
 * This attribute provides a visual title in the model,
 * rendered more suitably for a title than a normal annotation.
 * Moreover, if you export to web, this is used as the title for the
 * containing component and for any exported web page.
 * By default, the title is not shown on the web page except
 * as part of the image of the model. If you wish for the title
 * to also be shown in the HTML text before the image, then set
 * the <i>showTitleInHTML</i> parameter to true.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Title extends StringParameter implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public Title(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Unfortunately, much of this class is copied from AbstractTextAttribute,
        // but we have no choice because we need it to be a StringParameter.

        _icon = new TextIcon(this, "_icon");
        _icon.setPersistent(false);
        _icon.setIconText("T");

        showTitleInHTML = new Parameter(this, "showTitleInHTML");
        showTitleInHTML.setExpression("false");
        showTitleInHTML.setTypeEquals(BaseType.BOOLEAN);

        textSize = new Parameter(this, "textSize");
        textSize.setExpression("24");
        textSize.setTypeEquals(BaseType.INT);
        textSize.addChoice("9");
        textSize.addChoice("10");
        textSize.addChoice("11");
        textSize.addChoice("12");
        textSize.addChoice("14");
        textSize.addChoice("18");
        textSize.addChoice("24");
        textSize.addChoice("32");

        textColor = new ColorAttribute(this, "textColor");
        textColor.setExpression("{0.0, 0.0, 0.0, 1.0}");

        // Get font family names from the Font class in Java.
        // This includes logical font names, per Font class in Java:
        // Dialog, DialogInput, Monospaced, Serif, SansSerif, or Symbol.
        fontFamily = new StringParameter(this, "fontFamily");
        fontFamily.setExpression("SansSerif");

        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        for (String familie : families) {
            fontFamily.addChoice(familie);
        }

        bold = new Parameter(this, "bold");
        bold.setExpression("false");
        bold.setTypeEquals(BaseType.BOOLEAN);

        italic = new Parameter(this, "italic");
        italic.setExpression("false");
        italic.setTypeEquals(BaseType.BOOLEAN);

        center = new Parameter(this, "center");
        center.setToken(BooleanToken.FALSE);
        center.setTypeEquals(BaseType.BOOLEAN);

        // Hide the name.
        SingletonParameter hide = new SingletonParameter(this, "_hideName");
        hide.setToken(BooleanToken.TRUE);
        hide.setVisibility(Settable.EXPERT);

        // No need to display any parameters when the "_showParameters"
        // preference asks for such display because presumably all the
        // parameters are reflected in the visual display already.
        Parameter hideAllParameters = new Parameter(this, "_hideAllParameters");
        hideAllParameters.setVisibility(Settable.EXPERT);
        hideAllParameters.setExpression("true");

        // The following ensures that double click edits the text of the title.
        new VisibleParameterEditorFactory(this, "_editorFactory");

        // Add a small icon.
        ConfigurableAttribute smallIcon = new ConfigurableAttribute(this,
                "_smallIconDescription");
        try {
            smallIcon
            .configure(
                    null,
                    null,
                    "<svg><text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">title</text></svg>");
        } catch (Exception e) {
            // Show exception on the console. Should not occur.
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A boolean indicating whether the font should be bold.
     *  This defaults to false.
     */
    public Parameter bold;

    /** A boolean parameter that controls whether the origin of the text is
     *  center (if true) or north-west.
     */
    public Parameter center;

    /** The font family. This is a string that defaults to "SansSerif".
     */
    public StringParameter fontFamily;

    /** A boolean indicating whether the font should be italic.
     *  This defaults to false.
     */
    public Parameter italic;

    /** If set to true, then the title given by this parameter
     *  will be shown in the HTML prior to the image of the model
     *  (as well as in the image of the model, if it is visible
     *  when the export to web occurs). This is a boolean that
     *  defaults to false.
     */
    public Parameter showTitleInHTML;

    /** The text color.  This is a string representing an array with
     *  four elements, red, green, blue, and alpha, where alpha is
     *  transparency. The default is "{0.0, 0.0, 0.0, 1.0}", which
     *  represents an opaque black.
     */
    public ColorAttribute textColor;

    /** The text size.  This is an int that defaults to 14.
     */
    public Parameter textSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == center) {
            if (((BooleanToken) center.getToken()).booleanValue()) {
                _icon.setAnchor(SwingConstants.CENTER);
            } else {
                _icon.setAnchor(SwingConstants.NORTH_WEST);
            }
        } else if ((attribute == fontFamily || attribute == textSize
                || attribute == bold || attribute == italic)
                && !_inAttributeChanged) {
            try {
                // Prevent redundant actions here... When we evaluate the
                // _other_ attribute here (whichever one did _not_ trigger
                // this call, it will likely trigger another call to
                // attributeChanged(), which will result in this action
                // being performed twice.
                _inAttributeChanged = true;

                int sizeValue = ((IntToken) textSize.getToken()).intValue();
                String familyValue = fontFamily.stringValue();
                int styleValue = Font.PLAIN;

                if (((BooleanToken) bold.getToken()).booleanValue()) {
                    styleValue = styleValue | Font.BOLD;
                }

                if (((BooleanToken) italic.getToken()).booleanValue()) {
                    styleValue = styleValue | Font.ITALIC;
                }

                Font fontValue = new Font(familyValue, styleValue, sizeValue);
                _icon.setFont(fontValue);
            } finally {
                _inAttributeChanged = false;
            }
        } else if (attribute == textColor) {
            Color colorValue = textColor.asColor();
            _icon.setTextColor(colorValue);
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the object into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new AbstractTextAttribute.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Title result = (Title) super.clone(workspace);
        result._icon = (TextIcon) result.getAttribute("_icon");
        return result;
    }

    /** A title is of type text/html.
     *
     * @return The string text/html
     */
    @Override
    public String getMimeType() {
        return "text/html";
    }

    /** Return true, since new title content should overwrite old title content.
     *
     * @return True, since new title content should overwrite old title content.
     */
    @Override
    public boolean isOverwriteable() {
        return true;
    }

    /** Move this object to the first position in the list
     *  of attributes of the container. This overrides the base
     *  class to create  an attribute named "_renderFirst" and to
     *  remove an attribute named "_renderLast", if it is present.
     *  This attribute is recognized by vergil, which then renders this
     *  attribute before entities, connections, and other attributes.
     *  This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToFirst() throws IllegalActionException {
        try {
            new SingletonAttribute(this, "_renderFirst");
            Attribute renderLast = getAttribute("_renderLast");

            if (renderLast != null) {
                renderLast.setContainer(null);
            }
        } catch (NameDuplicationException e) {
            // Ignore.  This will result in a rendering error,
            // but that is better than trashing user data.
        }

        return super.moveToFirst();
    }

    /** Move this object to the last position in the list
     *  of attributes of the container. This overrides the base
     *  class to create  an attribute named "_renderLast" and to
     *  remove an attribute named "_renderFirst" if it is present.
     *  This attribute is recognized by vergil, which then renders this
     *  attribute after entities, connections, and other attributes.
     *  This method gets write access on workspace
     *  and increments the version.
     *  @return The index of the specified object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IllegalActionException If this object has
     *   no container.
     */
    @Override
    public int moveToLast() throws IllegalActionException {
        try {
            new SingletonAttribute(this, "_renderLast");
            Attribute renderFirst = getAttribute("_renderFirst");

            if (renderFirst != null) {
                renderFirst.setContainer(null);
            }
        } catch (NameDuplicationException e) {
            // Ignore.  This will result in a rendering error,
            // but that is better than trashing user data.
        }

        return super.moveToLast();
    }

    /** Return a title for the model.  The title can be returned as an attribute
     *  or an element, but not a document.
     *
     *  @param exporter  The web exporter to which to write content.
     *  @exception IllegalActionException If there is a problem creating
     *  the content or setting the attribute.
     */
    @Override
    public void provideContent(WebExporter exporter)
            throws IllegalActionException {
        // Provide a WebElement containing the title.  Title does not
        // provide any WebAttributes.
        //_provideElements(exporter);
        _provideAttributes(exporter);
    }

    /** Provide a title for this object to the specified web exporter.
     *
     *  @param exporter The WebExporter to add content to
     *  @exception IllegalActionException If something is wrong with the
     *   specification of the content.
     */
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {

        // FIXME:  Refactor so we don't need this method
        exporter.setTitle(stringValue(),
                ((BooleanToken) showTitleInHTML.getToken()).booleanValue());

        // Create a WebAttribute for title and add to exporter.
        // Content should only be added once (onceOnly -> true).
        /* title attribute is now used for displaying parameter table.
        WebAttribute webAttribute = WebAttribute.createWebAttribute(
                getContainer(), "titleWebAttribute", "title");
        webAttribute.setExpression(stringValue());
        exporter.defineAttribute(webAttribute, true);
         */
    }

    /** Provide the <title> </title> element to the specified web exporter.
     *  This element should be included in the <head> section.
     *
     *  @param exporter The WebExporter to add content to
     *  @exception IllegalActionException If something is wrong with the
     *   specification of the content.
     */

    /*
    protected void _provideElements(WebExporter exporter)
        throws IllegalActionException {

        // Create a WebElement for title and add to exporter.
        // Content should only be added once (onceOnly -> true).
        WebElement webElement = WebElement.
            createWebElement(getContainer(), "titleWebElement", "title");
        webElement.setParent(WebElement.HEAD);
        webElement.setExpression(stringValue());
        exporter.defineElement(webElement, true);
    }
     */

    /** Override the base class to set the text to be displayed
     *  in the icon.
     */
    @Override
    public Collection validate() throws IllegalActionException {
        Collection result = super.validate();
        _icon.setText(stringValue());
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected members                   ////

    /** The text icon. */
    protected TextIcon _icon;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _inAttributeChanged = false;
}
