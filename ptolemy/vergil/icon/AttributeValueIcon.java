/* An icon that renders the value of an attribute of the container.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.Writer;

import javax.swing.SwingConstants;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;

//////////////////////////////////////////////////////////////////////////
//// AttributeValueIcon
/**
An icon that displays the value of an attribute of the container.
The attribute is assumed to be an instance of Settable, and its name
is given by the parameter <i>attributeName</i>.  The display is not
automatically updated when the attribute value is updated.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class AttributeValueIcon extends XMLIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     */
    public AttributeValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        attributeName = new StringAttribute(this, "attributeName");
        displayWidth = new Parameter(this, "displayWidth");
        displayWidth.setExpression("6");
        displayWidth.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the attribute of the container whose value to display. */
    public StringAttribute attributeName;

    /** The number of characters to display. This is an integer, with
     *  default value 6.
     */
    public Parameter displayWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        AttributeValueIcon newObject = (AttributeValueIcon)super.clone(workspace);
        newObject._background = null;
        newObject._label = null;
        return newObject;
    }

    /** Create a new background figure.  This overrides the base class
     *  to remember the background figure so that it can center the label
     *  over it in createFigure().
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        _background = super.createBackgroundFigure();
        return _background;
    }

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  value of the specified attribute of the container.
     *  @return A new CompositeFigure consisting of the label.
     */
    public Figure createFigure() {
        CompositeFigure result = (CompositeFigure)super.createFigure();
        String truncated = _displayString();
        // If there is no string to display now, then create a string
        // with a single blank.
        if (truncated == null) {
            truncated = " ";
        }
        // NOTE: This violates the Diva MVC architecture!
        // This attribute is part of the model, and should not have
        // a reference to this figure.  By doing so, it precludes the
        // possibility of having multiple views on this model.
        _label = new LabelFigure(truncated,
                _labelFont, 1.0, SwingConstants.CENTER);
        Rectangle2D backBounds = _background.getBounds();
        _label.translateTo(backBounds.getCenterX(), backBounds.getCenterY());
        result.add(_label);
        return result;
    }

    /** Write a MoML description of this object, unless this object is
     *  non-persistent.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name" and "class" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline.  Since the value of this attribute
     *  simply mirrors that of the container, the value field is not
     *  exported.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {

        if (!isPersistent()) {
            return;
        }
        output.write(_getIndentPrefix(depth)
                + "<"
                + getMoMLInfo().elementName
                + " name=\""
                + name
                + "\" class=\""
                + getMoMLInfo().className
                + "\">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Get the string to render in the icon.  This string is the
     *  expression giving the value of the attribute of the container
     *  having the name <i>attributeName</i>, truncated so that it is
     *  no longer than <i>displayWidth</i> characters.  If it is truncated,
     *  then the string has a trailing "...".  If the string is empty,
     *  then return a string with one space (diva fails on empty strings).
     *  @return The string to display, or null if none is found.
     */
    protected String _displayString() {
        NamedObj container = (NamedObj)getContainer();
        if (container != null) {
            Attribute associatedAttribute = container.getAttribute(
                    attributeName.getExpression());
            if (associatedAttribute instanceof Settable) {
                String value = ((Settable)associatedAttribute).getExpression();
                String truncated = value;
                try {
                    int width = ((IntToken)displayWidth.getToken()).intValue();
                    if (value.length() > width) {
                        truncated = value.substring(0, width) + "...";
                    }
                } catch (IllegalActionException ex) {
                    // Ignore... use whole string.
                }
                if (truncated.length() == 0) {
                    truncated = " ";
                }
                return truncated;
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The background figure. */
    protected Figure _background;

    /** The label figure. */
    protected LabelFigure _label;

    /** The font used. */
    protected static Font _labelFont = new Font("SansSerif", Font.PLAIN, 12);
}
