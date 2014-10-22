/* An attribute with a reference to a polygon.

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
package ptolemy.vergil.kernel.attributes;

import java.awt.Polygon;
import java.awt.Shape;

import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ResizablePolygonAttribute

/**
 <p>This is an attribute that is rendered as a polygon.  The <i>vertices</i>
 parameter is an array of doubles that specify the vertices of the polygon
 in the form {x1, y1, x2, y2, ... }.
 The <i>width</i> and <i>height</i> parameters, somewhat awkwardly,
 are used to specify the overall width and height. The polygon will be
 scaled to fit the specified width and height.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ResizablePolygonAttribute extends FilledShapeAttribute {
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
    public ResizablePolygonAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        vertices = new Parameter(this, "vertices");

        ArrayType type = new ArrayType(BaseType.DOUBLE);
        vertices.setTypeEquals(type);
        vertices.setExpression("{0.0, 0.0, 50.0, 0.0, 25.0, 50.0, -25.0, 50.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The amount of vertices of the corners.
     *  This is a double that defaults to 0.0, which indicates no vertices.
     *  The default value specifies a rhombus.
     */
    public Parameter vertices;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing
     *  the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == vertices || attribute == width || attribute == height
                && !_inAttributeChanged) {
            // Check that the length of the array is even.
            ArrayToken verticesValue = (ArrayToken) vertices.getToken();
            int length = verticesValue.length();

            if (length / 2 != (length + 1) / 2) {
                throw new IllegalActionException(this,
                        "Length of the vertices array is required to be even.");
            }

            try {
                // Prevent redundant actions here... When we evaluate the
                // _other_ attribute here (whichever one did _not_ trigger
                // this call, it will likely trigger another call to
                // attributeChanged(), which will result in this action
                // being performed twice.
                _inAttributeChanged = true;

                double widthValue = ((DoubleToken) width.getToken())
                        .doubleValue();
                double heightValue = ((DoubleToken) height.getToken())
                        .doubleValue();
                _widthValue = widthValue;
                _heightValue = heightValue;
                _icon.setShape(_newShape());
            } finally {
                _inAttributeChanged = false;
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
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ResizablePolygonAttribute newObject = (ResizablePolygonAttribute) super
                .clone(workspace);

        // The cloned icon ends up referring to the clonee's shape.
        // We need to fix that here. Do not use the _newShape() method
        // of the clone, however, because it may refer to parameters that
        // have not been created yet. Instead, use this object to generate
        // the new shape for the clone.
        newObject._icon.setShape(_newShape());
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the a new polygon with the given vertices.
     *  @return A new shape.
     */
    @Override
    protected Shape _newShape() {
        try {
            ArrayToken verticesValue = (ArrayToken) vertices.getToken();
            int length = verticesValue.length();

            // Keep computations in double as long as possible.
            double[] xPoints = new double[length / 2];
            double[] yPoints = new double[length / 2];
            double xMax = Double.NEGATIVE_INFINITY;
            double xMin = Double.POSITIVE_INFINITY;
            double yMax = Double.NEGATIVE_INFINITY;
            double yMin = Double.POSITIVE_INFINITY;

            // First, read vertex values and find the bounds.
            for (int j = 0; j < length / 2; j++) {
                xPoints[j] = ((DoubleToken) verticesValue.getElement(2 * j))
                        .doubleValue();
                yPoints[j] = ((DoubleToken) verticesValue.getElement(2 * j + 1))
                        .doubleValue();

                if (xPoints[j] > xMax) {
                    xMax = xPoints[j];
                }

                if (xPoints[j] < xMin) {
                    xMin = xPoints[j];
                }

                if (yPoints[j] > yMax) {
                    yMax = yPoints[j];
                }

                if (yPoints[j] < yMin) {
                    yMin = yPoints[j];
                }
            }

            // Next, scale to width and height.
            double scaleX = _widthValue / (xMax - xMin);
            double scaleY = _heightValue / (yMax - yMin);

            for (int j = 0; j < length / 2; j++) {
                xPoints[j] *= scaleX;
                yPoints[j] *= scaleY;
            }

            // Finally, correct if centered, and convert to int.
            int[] xInt = new int[length / 2];
            int[] yInt = new int[length / 2];

            if (_centeredValue) {
                double xOffset = (xMin - xMax) / 2;
                double yOffset = (yMin - yMax) / 2;

                for (int i = 0; i < length / 2; i++) {
                    xInt[i] = (int) Math.rint(xPoints[i] + xOffset);
                    yInt[i] = (int) Math.rint(yPoints[i] + yOffset);
                    ;
                }
            } else {
                for (int i = 0; i < length / 2; i++) {
                    xInt[i] = (int) Math.rint(xPoints[i]);
                    yInt[i] = (int) Math.rint(yPoints[i]);
                    ;
                }
            }

            return new Polygon(xInt, yInt, length / 2);
        } catch (IllegalActionException e) {
            // This should not occur because attributeChanged()
            // has accessed the token of vertices.
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private boolean _inAttributeChanged = false;
}
