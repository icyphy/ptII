/* An abstract attribute with a reference to a shape.

 Copyright (c) 2001-2003 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel.attributes;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.ShapeIcon;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

//////////////////////////////////////////////////////////////////////////
//// ShapeAttribute
/**
This is an abstract attribute that is rendered as a shape.
This base class provides support for a line width and a line color.
The line color can be "none", in which case no line is drawn.
Concrete derived classes provide particular shapes.
<p>
@author Edward A. Lee
@version $Id$
*/
public abstract class ShapeAttribute extends Attribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ShapeAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Hide the name.
        new Attribute(this, "_hideName");

        _icon = new ShapeIcon(this, "_icon", _getDefaultShape());
        _icon.setPersistent(false);

        lineWidth = new Parameter(this, "lineWidth");
        lineWidth.setTypeEquals(BaseType.DOUBLE);
        lineWidth.setExpression("1.0");
        
        lineColor = new ColorAttribute(this, "lineColor");
        lineColor.setExpression("{0.0, 0.0, 0.0, 1.0}");
        
        _none = new Variable(this, "none");
        _none.setExpression("{1.0, 1.0, 1.0, 0.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The line color.  This is a string representing an array with
     *  four elements, red, green, blue, and alpha, where alpha is
     *  transparency. The default is "{0.0, 0.0, 0.0, 1.0}", which
     *  represents an opaque black.
     */
    public ColorAttribute lineColor;
    
    /** The line width.  This is a double that defaults to 1.0.
     */
    public Parameter lineWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing
     *  the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == lineWidth) {
            double lineWidthValue =
                ((DoubleToken) lineWidth.getToken()).doubleValue();
            _icon.setLineWidth((float) lineWidthValue);
        } else if (attribute == lineColor) {
            Color lineColorValue = lineColor.asColor();
            if (lineColorValue.getAlpha() == 0f) {
                // Color is fully transparent, so no line is desired.
                _icon.setLineColor(null);
            } else {
                _icon.setLineColor(lineColorValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
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
        ShapeAttribute newObject = (ShapeAttribute)super.clone(workspace);
        // The base class clones the icon, but since this is a protected
        // member, it doesn't automatically get updated by NamedObj!
        newObject._icon = (ShapeIcon)newObject.getAttribute("_icon");
        newObject._none = (Variable)newObject.getAttribute("_none");
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Return the default shape to use for this icon.
     *  Note that this is called in the constructor, so if you override
     *  it in derived classed, you cannot access any methods or members
     *  of the derived class because they will not have been constructed.
     *  @return The default shape for this attribute.
     */
    protected Shape _getDefaultShape() {
        // NOTE: In an ideal world, this would not be necessary, because
        // setShape() would override the default shape.  Unfortunately,
        // without this, icons are rendered before setShape() has been called,
        // and consequently, all shape attributes are rendered the same in
        // the utilities library.
        return new Rectangle2D.Double(0.0, 0.0, 20.0, 20.0);
    }
                       
    ///////////////////////////////////////////////////////////////////
    ////                       protected members                   ////

    /** The shape icon. */
    protected ShapeIcon _icon;
    
    /** A color parameter whose value is a fully transparent white
     *  (alpha = 0.0), which is interepreted as no color.
     */
    protected Variable _none;
}
