/* An actor that rotates the input 3D shape

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Rotate3D

/** Conceptually, this actor takes 3D geometry in its input and produces
 a rotated version in its output. In reality, this actor encapsulates a
 Java3D TransformGroup which is converted into a node in the resulting
 Java3D scene graph. This actor will only have meaning in the GR domain.

 The parameters <i>axisDirectionX</i>,<i>axisDirectionY</i>, and
 <i>axisDirectionZ</i> determine the direction of the axis of rotation.
 The parameters <i>baseX</i>, <i>baseY</i>, and <i>baseZ</i> determine
 the pivot point for axis of the rotation. The parameter <i>initialAngle</i>
 determines the initial angle of rotation.
 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class Rotate3D extends GRTransform {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Rotate3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        angle = new TypedIOPort(this, "angle", true, false);

        angle.setTypeEquals(BaseType.DOUBLE);
        initialAngle = new Parameter(this, "initialAngle", new DoubleToken(0.0));

        axisDirection = new Parameter(this, "axisDirection",
                new DoubleMatrixToken(new double[][] { { 0.0, 1.0, 0.0 } }));

        pivotLocation = new PortParameter(this, "pivotLocation",
                new DoubleMatrixToken(new double[][] { { 0.0, 0.0, 0.0 } }));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of rotation during firing. If this transform is in
     *  accumulate mode, the angle value is accumulated for each firing.
     */
    public TypedIOPort angle;

    /** The initial angle of rotation.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is 0.0.
     */
    public Parameter initialAngle;

    /** The direction of the axis of rotation.
     *  The parameter should contain a DoubleMatrixToken
     *  The default value of this parameter is [0.0, 1.0, 0.0]
     */
    public Parameter axisDirection;

    /** The pivot location of the axis of rotation.
     *  This parameter should contain a DoubleMatrixToken
     *  The default value of this parameter is [0.0, 0.0, 0.0]
     */
    public PortParameter pivotLocation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Connect other Java3D nodes as children of the encapsulated node in
     *  this actor.
     *
     *  @param node The child Java3D node.
     */
    @Override
    protected void _addChild(Node node) {
        _bottomTranslate.addChild(node);
    }

    /** Change the rotation angle depending on the value given in the
     *  input port.
     *
     *  @exception IllegalActionException If the value of some parameters
     *  can't be obtained.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (angle.isOutsideConnected()) {
            if (angle.hasToken(0)) {
                double in = ((DoubleToken) angle.get(0)).doubleValue();
                double originalAngle = ((DoubleToken) initialAngle.getToken())
                        .doubleValue();

                DoubleMatrixToken axis = (DoubleMatrixToken) axisDirection
                        .getToken();

                _xAxis = (float) axis.getElementAt(0, 0);
                _yAxis = (float) axis.getElementAt(0, 1);
                _zAxis = (float) axis.getElementAt(0, 2);

                Quat4d quat = new Quat4d();

                if (_isAccumulating()) {
                    _accumulatedAngle = in + _accumulatedAngle;
                    quat.set(new AxisAngle4d(_xAxis, _yAxis, _zAxis,
                            _accumulatedAngle));
                } else {
                    quat.set(new AxisAngle4d(_xAxis, _yAxis, _zAxis, in
                            + originalAngle));
                }

                _rotation.set(quat);
                _middleRotate.setTransform(_rotation);
            }
        }
        if (pivotLocation.getPort().isOutsideConnected()) {
            pivotLocation.update();

            DoubleMatrixToken pivot = (DoubleMatrixToken) pivotLocation
                    .getToken();

            _baseX = (float) pivot.getElementAt(0, 0);
            _baseY = (float) pivot.getElementAt(0, 1);
            _baseZ = (float) pivot.getElementAt(0, 2);

            Transform3D bottomTransform = new Transform3D();
            bottomTransform.setTranslation(new Vector3d(-_baseX, -_baseY,
                    -_baseZ));
            _bottomTranslate.setTransform(bottomTransform);
        }
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor TransformGroup
     *
     *  @return the Java3D TransformGroup
     */
    @Override
    protected Node _getNodeObject() {
        return _topTranslate;
    }

    /** Setup the initial rotation.
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    @Override
    public void initialize() throws IllegalActionException {
        DoubleMatrixToken axis = (DoubleMatrixToken) axisDirection.getToken();

        _xAxis = (float) axis.getElementAt(0, 0);
        _yAxis = (float) axis.getElementAt(0, 1);
        _zAxis = (float) axis.getElementAt(0, 2);

        DoubleMatrixToken pivot = (DoubleMatrixToken) pivotLocation.getToken();

        _baseX = (float) pivot.getElementAt(0, 0);
        _baseY = (float) pivot.getElementAt(0, 1);
        _baseZ = (float) pivot.getElementAt(0, 2);

        double originalAngle = ((DoubleToken) initialAngle.getToken())
                .doubleValue();

        _accumulatedAngle = originalAngle;

        _rotation = new Transform3D();

        _topTranslate = new TransformGroup();

        _middleRotate = new TransformGroup();
        _middleRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _bottomTranslate = new TransformGroup();
        _bottomTranslate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _bottomTranslate.setCapability(Group.ALLOW_CHILDREN_WRITE);
        _bottomTranslate.setCapability(Group.ALLOW_CHILDREN_EXTEND);

        Transform3D topTransform = new Transform3D();
        topTransform.setTranslation(new Vector3d(_baseX, _baseY, _baseZ));
        _topTranslate.setTransform(topTransform);

        Quat4d quaternion = new Quat4d();
        quaternion.set(new AxisAngle4d(_xAxis, _yAxis, _zAxis, originalAngle));
        _rotation.set(quaternion);
        _middleRotate.setTransform(_rotation);

        Transform3D bottomTransform = new Transform3D();
        bottomTransform.setTranslation(new Vector3d(-_baseX, -_baseY, -_baseZ));
        _bottomTranslate.setTransform(bottomTransform);
        _topTranslate.addChild(_middleRotate);
        _middleRotate.addChild(_bottomTranslate);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double _xAxis;

    private double _yAxis;

    private double _zAxis;

    private double _baseX;

    private double _baseY;

    private double _baseZ;

    private TransformGroup _topTranslate;

    private TransformGroup _middleRotate;

    private TransformGroup _bottomTranslate;

    private Transform3D _rotation;

    private double _accumulatedAngle;
}
