/* An attribute with a reference to a filled two-dimensional shape.

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

import java.awt.Color;
import java.awt.Shape;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.ResizableAttributeControllerFactory;

//////////////////////////////////////////////////////////////////////////
//// FilledShapeAttribute
/**
This is an abstract attribute that is rendered as a filled shape.
Concrete subclasses produce particular shapes, such as rectangles
and circles. Derived classes need to react to changes in the
<i>width</i> and <i>height</i> parameters in the attributeChanged()
method by calling setShape() on the protected member _icon.
<p>
@author Edward A. Lee
@version $Id$
*/
public abstract class FilledShapeAttribute extends ShapeAttribute {

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
    public FilledShapeAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        width = new Parameter(this, "width");
        width.setTypeEquals(BaseType.DOUBLE);
        width.setExpression("100.0");

        height = new Parameter(this, "height");
        height.setTypeEquals(BaseType.DOUBLE);
        height.setExpression("100.0");

        centered = new Parameter(this, "centered");
        centered.setTypeEquals(BaseType.BOOLEAN);
        centered.setExpression("false");
                
        fillColor = new ColorAttribute(this, "fillColor");
        fillColor.setExpression("none");
        
        // Create a custom controller.
        new ResizableAttributeControllerFactory(this, "_controllerFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indicator of whether the shape should be centered on the location.
     *  This is a boolean that defaults to false, which means that the
     *  location is the upper left corner.
     */
    public Parameter centered;
    
    /** The line color.  This is a string representing an array with
     *  four elements, red, green, blue, and alpha, where alpha is
     *  transparency. The default is "{0.0, 0.0, 0.0, 1.0}", which
     *  represents an opaque black.
     */
    public ColorAttribute fillColor;
    
    /** The vertical extent.
     *  This is a double that defaults to 100.0.
     */
    public Parameter height;

    /** The horizontal extent.
     *  This is a double that defaults to 100.0.
     */
    public Parameter width;
    
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
        if ((attribute == width || attribute == height) && !_inAttributeChanged) {
            try {
                // Prevent redundant actions here... When we evaluate the
                // _other_ atribute here (whichever one did _not_ trigger
                // this call, it will likely trigger another call to
                // attributeChanged(), which will result in this action
                // being performed twice.
                _inAttributeChanged = true;
                double widthValue = ((DoubleToken) width.getToken()).doubleValue();
                double heightValue =
                        ((DoubleToken) height.getToken()).doubleValue();
                if (widthValue != _widthValue || heightValue != _heightValue) {
                    _widthValue = widthValue;
                    _heightValue = heightValue;
                    _icon.setShape(_newShape());
                }
            } finally {
                _inAttributeChanged = false;
            }
        } else if (attribute == centered) {
            boolean centeredValue
                    = ((BooleanToken)centered.getToken()).booleanValue();
            if (centeredValue != _centeredValue) {
                _centeredValue = centeredValue;
                _icon.setCentered(_centeredValue);
            }
        } else if (attribute == fillColor) {
            Color fillColorValue = fillColor.asColor();
            if (fillColorValue.getAlpha() == 0f) {
                _icon.setFillColor(null);
            } else {
                _icon.setFillColor(fillColorValue);
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
        FilledShapeAttribute newObject
                = (FilledShapeAttribute)super.clone(workspace);
        newObject._centeredValue = false;
        newObject._heightValue = 0.0;
        newObject._inAttributeChanged = false;
        newObject._widthValue = 0.0;
        return newObject;
    }
 
    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
    
    /** Return the a new shape given a new width and height. This class
     *  guarantees that the protected variables _centeredValue, _widthValue,
     *  and _heightValue are up to date when this method is called.
     *  Derived classes should override this to return an appropriate shape.
     *  @return A new shape. 
     */
    protected abstract Shape _newShape();
    
    ///////////////////////////////////////////////////////////////////
    ////                        protected variables                ////

    /** Most recently set value of the centered parameter. */
    protected boolean _centeredValue = false;
    
    /** Most recently set value of the height parameter. */
    protected double _heightValue = 0.0;

    /** Variable used to prevent re-entry into attributeChanged(). */
    protected transient boolean _inAttributeChanged = false;

    /** Most recently set value of the width parameter. */
    protected double _widthValue = 0.0;
}
