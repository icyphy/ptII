/* An attribute with a reference to an ellipse.

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
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ArcAttribute

/**
 <p>This is an attribute that is rendered as an ellipse.
 Unlike the base class, by default, an ellipse is centered on its origin.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ArcAttribute extends FilledShapeAttribute {
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
    public ArcAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        type = new StringParameter(this, "type");
        type.setExpression("pie");
        type.addChoice("chord");
        type.addChoice("open");
        type.addChoice("pie");

        start = new Parameter(this, "start");
        start.setTypeEquals(BaseType.DOUBLE);
        start.setExpression("0.0");

        extent = new Parameter(this, "extent");
        extent.setTypeEquals(BaseType.DOUBLE);
        extent.setExpression("90.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The angular extent of the angle of the arc in degrees. This is a double
     *  that defaults to 90.0.
     */
    public Parameter extent;

    /** The starting angle of the arc in degrees. This is a double
     *  that defaults to 0.0.
     */
    public Parameter start;

    /** The type of the arc, which should be one of "chord", "open", or "pie".
     *  The default is "pie".
     */
    public StringParameter type;

    ///////////////////////////////////////////////////////////////////
    ////                         public method                     ////

    /** React to changes in attribute values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If thrown while getting the
     *  value of an attribute.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == type) {
            String typeValue = type.stringValue();
            int typeDesignator = Arc2D.PIE;
            if (typeValue.equals("chord")) {
                typeDesignator = Arc2D.CHORD;
            } else if (typeValue.equals("open")) {
                typeDesignator = Arc2D.OPEN;
            }
            if (typeDesignator != _typeDesignator) {
                _typeDesignator = typeDesignator;
                _icon.setShape(_newShape());
            }
        } else if (attribute == start) {
            double startValue = ((DoubleToken) start.getToken()).doubleValue();
            if (startValue != _start) {
                _start = startValue;
                _icon.setShape(_newShape());
            }
        } else if (attribute == extent) {
            double extentValue = ((DoubleToken) extent.getToken())
                    .doubleValue();
            if (extentValue != _extent) {
                _extent = extentValue;
                _icon.setShape(_newShape());
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
        ArcAttribute newObject = (ArcAttribute) super.clone(workspace);

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

    /** Return a circle.
     *  @return A Circle.
     */
    @Override
    protected Shape _getDefaultShape() {
        Rectangle2D bounds = new Rectangle2D.Double(0, 0, _widthValue,
                _heightValue);
        return new Arc2D.Double(bounds, 0, 90, Arc2D.PIE);
    }

    /** Return the a new ellipse given a new width and height.
     *  @return A new shape.
     */
    @Override
    protected Shape _newShape() {
        if (_centeredValue) {
            double halfWidth = _widthValue * 0.5;
            double halfHeight = _heightValue * 0.5;
            // FIXME: There is a very weird bug with the last two
            // arguments here. If those are replaced with a constant, say 100, 100,
            // or with halfWidth, halfHeight, then the actual width and height
            // of the rendered arc becomes nondeterministic!!! It sometimes seem
            // to match the default width and height (26?).  Bizarre!!!!!!
            Rectangle2D bounds = new Rectangle2D.Double(-halfWidth,
                    -halfHeight, _widthValue, _heightValue);
            return new Arc2D.Double(bounds, _start, _extent, _typeDesignator);
        } else {
            Rectangle2D bounds = new Rectangle2D.Double(0, 0, _widthValue,
                    _heightValue);
            return new Arc2D.Double(bounds, _start, _extent, _typeDesignator);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The value of the extent parameter. */
    private double _extent = 90.0;

    /** The value of the type parameter. */
    private double _start = 0.0;

    /** The value of the type parameter. */
    private int _typeDesignator = Arc2D.PIE;
}
