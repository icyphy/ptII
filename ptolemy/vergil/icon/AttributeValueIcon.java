/* An icon that renders the value of an attribute of the container.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.util.StringUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;

///////////////////////////////////////////////////////////////////
//// AttributeValueIcon

/**
 An icon that displays the value of an attribute of the container
 or of some other entity contained by the container.
 The attribute is assumed to be an instance of Settable, and its name
 is given by the parameter <i>attributeName</i>.  The display is not
 automatically updated when the attribute value is updated.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class AttributeValueIcon extends XMLIcon {
    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public AttributeValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        attributeName = new StringAttribute(this, "attributeName");

        displayWidth = new Parameter(this, "displayWidth");
        displayWidth.setExpression("6");
        displayWidth.setTypeEquals(BaseType.INT);

        displayHeight = new Parameter(this, "displayHeight");
        displayHeight.setExpression("1");
        displayHeight.setTypeEquals(BaseType.INT);

        entityName = new StringParameter(this, "entityName");
        entityName.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the attribute of the container whose value to display.
     *  This is a string that by default is empty. An empty string means
     *  that the attribute whose value to display is the container itself,
     *  rather than an attribute contained by the container.
     */
    public StringAttribute attributeName;

    /** The maximum number of lines to display. This is an integer, with
     *  default value 1.
     */
    public Parameter displayHeight;

    /** The number of characters to display. This is an integer, with
     *  default value 6.
     */
    public Parameter displayWidth;

    /** Name of the entity contained by the container whose attribute
     *  this icon will display. This is a string that defaults to the
     *  empty string, which means that the attribute is contained
     *  by the container of this attribute.
     */
    public StringParameter entityName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new Diva figure that visually represents this icon.
     *  The figure will be an instance of LabelFigure that renders the
     *  value of the specified attribute of the container.
     *  @return A new CompositeFigure consisting of the label.
     */
    @Override
    public Figure createFigure() {
        CompositeFigure result = (CompositeFigure) super.createFigure();
        String truncated = _displayString();

        LabelFigure label = new LabelFigure(truncated, _labelFont, 1.0,
                SwingConstants.CENTER);
        Rectangle2D backBounds = result.getBackgroundFigure().getBounds();
        label.translateTo(backBounds.getCenterX(), backBounds.getCenterY());
        result.add(label);

        _addLiveFigure(label);
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the associated attribute. If an {@link #entityName} is given,
     *  then the associated attribute is attribute with name given by
     *  {@link #attributeName} contained by the specified entity.
     *  If no entityName is given, then the associated attribute is
     *  the one contained by the container of this object with the
     *  specified name.  If no attributeName is given either, and the
     *  the container of this object is an Attribute, then the associated
     *  attribute is that container. Otherwise, throw an exception.
     *  @return The associated attribute.
     *  @exception IllegalActionException If no Settable associated attribute
     *   can be found.
     */
    protected Settable _associatedAttribute() throws IllegalActionException {
        NamedObj container = getContainer();
        if (entityName.stringValue().trim().equals("")) {
            String name = attributeName.getExpression();
            if (!name.trim().equals("") && container != null) {
                Attribute candidate = container.getAttribute(name);
                if (candidate instanceof Settable) {
                    return (Settable) candidate;
                }
            } else {
                // No attributeName is given.
                // If the container is an Attribute, use it.
                if (container instanceof Settable) {
                    return (Settable) container;
                }
            }
        } else if (container instanceof CompositeEntity) {
            NamedObj entity = ((CompositeEntity) container)
                    .getEntity(entityName.stringValue());
            if (entity != null) {
                Attribute candidate = entity.getAttribute(attributeName
                        .getExpression());
                if (candidate instanceof Settable) {
                    return (Settable) candidate;
                }
            }
        }
        throw new IllegalActionException(this, "No associated attribute.");
    }

    /** Get the string value of the attribute to render in the icon.
     *  This string is the expression giving the value of the attribute of the
     *  container having the name <i>attributeName</i>. If the string is empty,
     *  then return a string with one space (diva fails on empty strings). This
     *  method will always return the full string value of the attribute and will
     *  not be truncated.
     *  @return The string value of the attribute to display, or a string with
     *   one space if none is found.
     */
    protected String _attributeValueString() {
        try {
            String value = _associatedAttribute().getExpression();
            if (value == null || value.equals("")) {
                value = " ";
            }
            return value;
        } catch (IllegalActionException e) {
            // Ignore and produce a default icon.
            return " ";
        }
    }

    /** Get the string to render in the icon.  This string is the
     *  expression giving the value of the attribute of the container
     *  having the name <i>attributeName</i>, truncated so that it is
     *  no longer than <i>displayWidth</i> characters.  If it is truncated,
     *  then the string has a trailing "...".  If the string is empty,
     *  then return a string with one space (diva fails on empty strings).
     *  @return The string to display, or a string with one space if none is found.
     */
    protected String _displayString() {
        String truncated = _attributeValueString();

        try {
            int width = ((IntToken) displayWidth.getToken()).intValue();
            int height = ((IntToken) displayHeight.getToken()).intValue();
            truncated = StringUtilities
                    .truncateString(truncated, width, height);
        } catch (IllegalActionException ex) {
            // Ignore... use whole string.
        }

        if (truncated.length() == 0) {
            truncated = " ";
        }

        return truncated;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The font used. */
    protected static final Font _labelFont = new Font("SansSerif", Font.PLAIN,
            12);
}
