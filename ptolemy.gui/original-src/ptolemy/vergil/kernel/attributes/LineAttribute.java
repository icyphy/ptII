/* An attribute with a reference to a line.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
import java.awt.geom.Line2D;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// LineAttribute

/**
 This is an attribute that is rendered as a line.

 <p>
 This contains two parameters, <i>x</i> and <i>y</i>, which control the
 run and rise, respectively, of the line.  Note that the origin is in the
 upper-left hand corner, so positive x values will extend to the right, and
 positive y values will extend downwards on the screen.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class LineAttribute extends ShapeAttribute {
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
    public LineAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        x = new Parameter(this, "x");
        x.setTypeEquals(BaseType.DOUBLE);
        x.setExpression("100.0");

        y = new Parameter(this, "y");
        y.setTypeEquals(BaseType.DOUBLE);
        y.setExpression("0.0");

        // FIXME: controller for resizing.
        // Create a custom controller.
        // new ImageAttributeControllerFactory(this, "_controllerFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The horizontal extent.
     *  This is a double that defaults to 100.0.
     */
    public Parameter x;

    /** The y extent.
     *  This is a double that defaults to 0.0.
     */
    public Parameter y;

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
        if (attribute == x || attribute == y) {
            double xValue = ((DoubleToken) x.getToken()).doubleValue();
            double yValue = ((DoubleToken) y.getToken()).doubleValue();
            _icon.setShape(new Line2D.Double(0.0, 0.0, xValue, yValue));
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
        LineAttribute newObject = (LineAttribute) super.clone(workspace);

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

    /** Return a line.
     *  @return A line.
     */
    @Override
    protected Shape _getDefaultShape() {
        return new Line2D.Double(0.0, 0.0, 20.0, 20.0);
    }
}
