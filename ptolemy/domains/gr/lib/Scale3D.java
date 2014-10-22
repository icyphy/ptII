/* An actor that scales the input 3D shape

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

import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Scale3D

/** Conceptually, this actor takes 3D geometry in its input and
 produces a scaled version in its output. In reality, this actor
 encapsulates a Java3D TransformGroup which is converted into a node in
 the resulting Java3D scene graph. This actor will only have meaning in
 the GR domain. Scaling can be done uniformly or non-uniformly.
 Uniform scaling scales the input geometry equally in all
 directions. Uniform scaling is done through modification of the
 <i>scaleFactor</i> parameter. Non-uniform scaling involves
 preferential scaling of the input geometry in a specified Cartesian
 axis.  Non-uniform scaling is done through modification of the
 <i>xScale</i>, <i>yScale</i>, and <i>zScale</i> parameters.

 @author C. Fong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
public class Scale3D extends GRTransform {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Scale3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        scaleInput = new TypedIOPort(this, "scaleInput");
        scaleInput.setTypeEquals(BaseType.DOUBLE);
        scaleInput.setInput(true);

        scaleFactor = new Parameter(this, "scaleFactor", new DoubleToken(1.0));
        scaleFactor.setTypeEquals(BaseType.DOUBLE);
        xScale = new Parameter(this, "xScale", new DoubleToken(1.0));
        yScale = new Parameter(this, "yScale", new DoubleToken(1.0));
        zScale = new Parameter(this, "zScale", new DoubleToken(1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount of rescaling during firing. If this transform is in
     *  accumulate mode, the scaling value is accumulated
     */
    public TypedIOPort scaleInput;

    /** The scale factor.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 1.0
     */
    public Parameter scaleFactor;

    /** The scale factor in the Cartesian x-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 1.0
     */
    public Parameter xScale;

    /** The scale factor in the Cartesian y-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 1.0
     */
    public Parameter yScale;

    /** The scale factor in the Cartesian z-axis.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is the DoubleToken 1.0
     */
    public Parameter zScale;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check the input port for scaling input.
     *
     *  @exception IllegalActionException If the value of some parameters
     *   can't be obtained
     */
    @Override
    public void fire() throws IllegalActionException {
        //  all state changes must be done in postfire()
        super.fire();

        boolean applyTransform = false;
        double xScale = 1.0;
        double yScale = 1.0;
        double zScale = 1.0;

        // read new inputs for offsets if there are any.
        if (scaleInput.isOutsideConnected() && scaleInput.hasToken(0)) {
            double in = ((DoubleToken) scaleInput.get(0)).doubleValue();
            applyTransform = true;
            xScale = in * _getInitialScaleX();
            yScale = in * _getInitialScaleY();
            zScale = in * _getInitialScaleZ();
        } else {
            xScale = _scaleXState;
            yScale = _scaleYState;
            zScale = _scaleZState;
        }

        if (applyTransform) {
            Transform3D scaleTransform = new Transform3D();
            scaleTransform.setScale(new Vector3d(xScale, yScale, zScale));
            _transformNode.setTransform(scaleTransform);
        }

        // use the results as the new states and save them
        // (Should these state changes be moved to postfire?)
        _scaleXState = xScale;
        _scaleYState = yScale;
        _scaleZState = zScale;

    }

    /** Setup the transformation needed for scaling.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _transformNode = new TransformGroup();
        _transformNode.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

        _scaleXState = _getInitialScaleX();
        _scaleYState = _getInitialScaleY();
        _scaleZState = _getInitialScaleZ();

        Transform3D scaleTransform = new Transform3D();
        scaleTransform.setScale(new Vector3d(_scaleXState, _scaleYState,
                _scaleZState));
        _transformNode.setTransform(scaleTransform);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add a scene graph child node to this actor.
     *
     *  @param node The child node to be attached.
     */
    @Override
    protected void _addChild(Node node) {
        _transformNode.addChild(node);
    }

    /** Return the encapsulated Java3D node of this 3D actor. The
     *  encapsulated node for this actor is a TransformGroup.
     *
     *  @return the Java3D TransformGroup.
     */
    @Override
    protected Node _getNodeObject() {
        return _transformNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The encapsulated Java 3D node of this actor. */
    protected TransformGroup _transformNode;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get the scaling factor in the x-axis
     *  @return the scaling factor in the x-axis
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getInitialScaleX() throws IllegalActionException {
        double factor = ((DoubleToken) scaleFactor.getToken()).doubleValue();
        double xFactor = ((DoubleToken) xScale.getToken()).doubleValue();
        return factor * xFactor;
    }

    /** Get the scaling factor in the y-axis
     *  @return the scaling factor in the y-axis
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getInitialScaleY() throws IllegalActionException {
        double factor = ((DoubleToken) scaleFactor.getToken()).doubleValue();
        double yFactor = ((DoubleToken) yScale.getToken()).doubleValue();
        return factor * yFactor;
    }

    /** Get the scaling factor in the z-axis
     *  @return the scaling factor in the z-axis
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getInitialScaleZ() throws IllegalActionException {
        double factor = ((DoubleToken) scaleFactor.getToken()).doubleValue();
        double zFactor = ((DoubleToken) zScale.getToken()).doubleValue();
        return factor * zFactor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private double _scaleXState;

    private double _scaleYState;

    private double _scaleZState;
}
