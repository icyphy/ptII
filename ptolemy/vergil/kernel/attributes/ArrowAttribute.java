/* An attribute shown as an arrow.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.vergil.kernel.attributes;

import java.awt.Shape;
import java.awt.geom.AffineTransform;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import diva.util.java2d.Polygon2D;

///////////////////////////////////////////////////////////////////
//// ArrowAttribute

/**
 An attribute shown as an arrow. The length and width of the arrow head can be
 set with the <i>arrowLength</i> and <i>arrowWidth</i> parameters.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ArrowAttribute extends LineAttribute {

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
    public ArrowAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        arrowLength = new Parameter(this, "arrowLength");
        arrowLength.setTypeEquals(BaseType.DOUBLE);
        arrowLength.setExpression("16.0");

        arrowWidth = new Parameter(this, "arrowWidth");
        arrowWidth.setTypeEquals(BaseType.DOUBLE);
        arrowWidth.setExpression("10.0");

        _icon.setFillColor(lineColor.asColor());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Length of the arrow head. Default to 16.0 */
    public Parameter arrowLength;

    /** Width of the arrow head. Default to 10.0. */
    public Parameter arrowWidth;

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
        if (attribute == x || attribute == y || attribute == arrowLength
                || attribute == arrowWidth || attribute == lineColor) {
            double xValue = ((DoubleToken) x.getToken()).doubleValue();
            double yValue = ((DoubleToken) y.getToken()).doubleValue();
            double arrowLengthValue = ((DoubleToken) arrowLength.getToken())
                    .doubleValue();
            double arrowWidthValue = ((DoubleToken) arrowWidth.getToken())
                    .doubleValue();
            _icon.setShape(_createArrow(xValue, yValue, arrowLengthValue,
                    arrowWidthValue));
            _icon.setLineColor(lineColor.asColor());
            _icon.setFillColor(lineColor.asColor());
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ArrowAttribute newObject = (ArrowAttribute) super.clone(workspace);

        // The cloned icon ends up referring to the clonee's shape.
        // We need to fix that here.
        try {
            newObject._icon.attributeChanged(x);
        } catch (IllegalActionException e) {
            // Should not occur.
            throw new CloneNotSupportedException(e.getMessage());
        }
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default arrow.
     *  @return An arrow.
     */
    @Override
    protected Shape _getDefaultShape() {
        return _createArrow(40.0, 40.0, 16.0, 10.0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Create an arrow with the given x, y, arrow length and arrow width
     *  values.
     *  @param x The x extent.
     *  @param y The y extent.
     *  @param arrowLength Length of the arrow head.
     *  @param arrowWidth Width of the arrow head.
     *  @return An arrow with the given x, y, arrow length and arrow width
     *  values.
     */
    private static Shape _createArrow(double x, double y, double arrowLength,
            double arrowWidth) {
        double halfWidth = arrowWidth / 2.0;
        Polygon2D polygon = new Polygon2D.Double();
        polygon.moveTo(0.0, halfWidth);
        polygon.lineTo(arrowLength + 3.0, 0.0);
        polygon.lineTo(arrowLength, halfWidth);
        polygon.lineTo(Math.sqrt(x * x + y * y), halfWidth);
        polygon.lineTo(arrowLength, halfWidth);
        polygon.lineTo(arrowLength + 3.0, arrowWidth);
        polygon.closePath();

        AffineTransform transform = AffineTransform.getRotateInstance(Math
                .atan2(y, x));
        polygon.transform(transform);
        return polygon;
    }
}
