/* An attribute with a reference to a polygon.

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

//////////////////////////////////////////////////////////////////////////
//// PolygonAttribute
/**
This is an attribute that is rendered as a polygon.  The <i>vertices</i>
parameter is an array of doubles that specify the vertices of the polygon
in the form {x1, y1, x2, y2, ... }.
The <i>width</i> and <i>height</i> parameters are percentages, allowing
for easy scaling of the polygon.
<p>
@author Edward A. Lee
@version $Id$
*/
public class PolygonAttribute extends FilledShapeAttribute {

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
    public PolygonAttribute(NamedObj container, String name)
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
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == vertices || attribute == width || attribute == height
                && !_inAttributeChanged) {
            // Check that the length of the array is even.
            ArrayToken verticesValue = (ArrayToken)vertices.getToken();
            int length = verticesValue.length();
            if (length/2 != (length + 1)/2)  {
                throw new IllegalActionException(this,
                "Length of the vertices array is required to be even.");           
            }
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
    
    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
    
    /** Return the a new polygon with the given vertices.
     *  @param width The new width.
     *  @param height The new height.
     *  @return A new shape. 
     */
    protected Shape _newShape() {
        
        try {
            ArrayToken verticesValue = (ArrayToken)vertices.getToken();
            int length = verticesValue.length();
            int[] xPoints = new int[length];
            int[] yPoints = new int[length];
            int xMax = Integer.MIN_VALUE;
            int xMin = Integer.MAX_VALUE;
            int yMax = Integer.MIN_VALUE;
            int yMin = Integer.MAX_VALUE;
            
            // Scaling.
            double width = _widthValue;
            double height = _heightValue;
            
            for (int i = 0; i < length; i = i + 2) {
                double x = ((DoubleToken)verticesValue.getElement(i)).doubleValue();
                double y = ((DoubleToken)verticesValue.getElement(i + 1)).doubleValue();
                int j = i/2;
                xPoints[j] = (int)(Math.round(x * width/100.0));
                yPoints[j] = (int)(Math.round(y * height/100.0));
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
            if (_centeredValue) {
                int xOffset = (xMin - xMax)/2;
                int yOffset = (yMin - yMax)/2;
                for (int i = 0; i < length/2; i++) {
                    xPoints[i] += xOffset;
                    yPoints[i] += yOffset;
                }
            }
            return new Polygon(xPoints, yPoints, length/2);
        } catch (IllegalActionException e) {
            // This should not occur because attributeChanged()
            // has accessed the token of vertices.
            throw new InternalErrorException(e);
        }
    }
}
