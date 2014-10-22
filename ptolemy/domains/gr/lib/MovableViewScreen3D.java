/* A GR 3D scene viewer

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
package ptolemy.domains.gr.lib;

import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MovableViewScreen3D

/** A sink actor that renders the GR geometry into a display screen
 with movable viewpoint.

 @author Adam Cataldo
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (acataldo)
 @Pt.AcceptedRating Red (acataldo)
 */
public class MovableViewScreen3D extends ViewScreen3D {
    /** Construct a ViewScreen in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this ViewScreen.
     *  @exception IllegalActionException If this actor
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public MovableViewScreen3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        viewerPosition = new PortParameter(this, "viewerPosition",
                new ArrayToken("{0.0, 0.0, 2.4}"));
        viewerRotationAxis = new PortParameter(this, "viewerRotationAxis",
                new ArrayToken("{0.0, 0.0, -1.0}"));
        viewerRotationAngle = new PortParameter(this, "viwerRoationAngle",
                new DoubleToken("0.0"));

        viewerPosition.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        viewerRotationAxis.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        viewerRotationAngle.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** A double array representing the (x,y,z) position of the view
     *  frame relative to the virtual universe.  By default, this value
     *  is (0,0,2.4).
     */
    PortParameter viewerPosition;

    /** A double array representing an axis of rotation for the view frame.
     *  By default, when you look at the view screen, you are looking
     *  in the (0.0, 0.0, -1.0) direction of the virtual universe.  Your
     *  right is the (1.0, 0.0, 0.0) direction, and up is the (0.0, 1.0, 0.0)
     *  direction.  In this situtation, the frame describing the viewer has
     *  its positive z-axis going from the screen to you.  It's positive
     *  x-axis points right, and it's positve y-axis points up.  When the
     *  viewerRotationAxis and the viewerRotationAngle are provided, the
     *  ViewScreen frame is rotated counterclockwise around the
     *  viewerRotationAxis by the viewerRotationAngle.
     */
    PortParameter viewerRotationAxis;

    /** A double value representing the angle, in radians, of rotation
     *  about the viewerRotationAxis.
     */
    PortParameter viewerRotationAngle;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MovableViewScreen3D newObject = (MovableViewScreen3D) super
                .clone(workspace);
        newObject.viewerPosition = (PortParameter) newObject
                .getAttribute("viewerPosition");
        newObject.viewerRotationAngle = (PortParameter) newObject
                .getAttribute("viwerRoationAngle");
        newObject.viewerRotationAxis = (PortParameter) newObject
                .getAttribute("viewerRotationAxis");
        return newObject;
    }

    /** Call the ViewScreen fire() method, and translate and rotate the
     *  image if needed.
     *
     *  @exception IllegalActionException If the input array has the
     *  wrong size.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        viewerPosition.update();
        viewerRotationAxis.update();
        viewerRotationAngle.update();

        ArrayToken positionToken = (ArrayToken) viewerPosition.getToken();
        ArrayToken axisToken = (ArrayToken) viewerRotationAxis.getToken();
        DoubleToken angleToken = (DoubleToken) viewerRotationAngle.getToken();

        if (positionToken.length() != 3 || axisToken.length() != 3) {
            throw new IllegalActionException(
                    "viewerPosition and viewerRotaionAxis arrays must have length 3.");
        }

        BooleanToken equals = positionToken.isEqualTo(_position);
        equals = equals.and(axisToken.isEqualTo(_axis));
        equals = equals.and(angleToken.isEqualTo(_angle));

        if (!equals.booleanValue()) {
            double xPosition;
            double yPosition;
            double zPosition;
            double axisX;
            double axisY;
            double axisZ;
            double angle;
            xPosition = ((DoubleToken) positionToken.getElement(0))
                    .doubleValue();
            yPosition = ((DoubleToken) positionToken.getElement(1))
                    .doubleValue();
            zPosition = ((DoubleToken) positionToken.getElement(2))
                    .doubleValue();
            axisX = ((DoubleToken) axisToken.getElement(0)).doubleValue();
            axisY = ((DoubleToken) axisToken.getElement(1)).doubleValue();
            axisZ = ((DoubleToken) axisToken.getElement(2)).doubleValue();
            angle = angleToken.doubleValue();

            Quat4d quaternion = new Quat4d();
            quaternion.set(new AxisAngle4d(axisX, axisY, axisZ, angle));

            Vector3d vector = new Vector3d(xPosition, yPosition, zPosition);
            Transform3D t = new Transform3D(quaternion, vector, 1.0);
            TransformGroup group = _simpleUniverse.getViewingPlatform()
                    .getViewPlatformTransform();
            group.setTransform(t);
        }
    }

    /** Call the ViewScreen initialize() method, and set the correct
     *  location of the viewer.
     *
     *  @exception IllegalActionException If the input array has the wrong size.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        double xPosition;
        double yPosition;
        double zPosition;
        double axisX;
        double axisY;
        double axisZ;
        double angle;
        _position = (ArrayToken) viewerPosition.getToken();
        _axis = (ArrayToken) viewerRotationAxis.getToken();
        _angle = (DoubleToken) viewerRotationAngle.getToken();

        if (_position.length() != 3 || _axis.length() != 3) {
            throw new IllegalActionException(
                    "viewerPosition and viewerRotaionAxis arrays must have length 3.");
        }

        xPosition = ((DoubleToken) _position.getElement(0)).doubleValue();
        yPosition = ((DoubleToken) _position.getElement(1)).doubleValue();
        zPosition = ((DoubleToken) _position.getElement(2)).doubleValue();
        axisX = ((DoubleToken) _axis.getElement(0)).doubleValue();
        axisY = ((DoubleToken) _axis.getElement(1)).doubleValue();
        axisZ = ((DoubleToken) _axis.getElement(2)).doubleValue();
        angle = _angle.doubleValue();

        Quat4d quaternion = new Quat4d();
        quaternion.set(new AxisAngle4d(axisX, axisY, axisZ, angle));

        Vector3d vector = new Vector3d(xPosition, yPosition, zPosition);
        Transform3D t = new Transform3D(quaternion, vector, 1.0);
        TransformGroup group = _simpleUniverse.getViewingPlatform()
                .getViewPlatformTransform();
        group.setTransform(t);
        _initialPosition = _position;
        _initialAxis = _axis;
        _initialAngle = _angle;
    }

    /**Update the input values of the state.
     * @return Returns the value from its super class.
     * @exception IllegalActionException Thrown if super class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _position = (ArrayToken) viewerPosition.getToken();
        _axis = (ArrayToken) viewerRotationAxis.getToken();
        _angle = (DoubleToken) viewerRotationAngle.getToken();
        return super.postfire();
    }

    /**Reset the state to the initial values.
     * @exception IllegalActionException Thrown if super class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _position = _initialPosition;
        _axis = _initialAxis;
        _angle = _initialAngle;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ArrayToken _position;

    private ArrayToken _axis;

    private DoubleToken _angle;

    private ArrayToken _initialPosition;

    private ArrayToken _initialAxis;

    private DoubleToken _initialAngle;

}
