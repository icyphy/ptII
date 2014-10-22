/* A GR Shape consisting of a cylinder with a circular base.

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

import java.net.URL;

import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;

///////////////////////////////////////////////////////////////////
//// Cylinder3D

/** This actor contains the geometry and appearance specifications for a GR
 cylinder.  The output port is used to connect this actor to the Java3D
 scene graph. This actor will only have meaning in the GR domain.
 Note that most of the parameters are described in the base class documentation.

 @author C. Fong, Adam Cataldo, Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (liuxj)
 */
public class Cylinder3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Cylinder3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        radius = new Parameter(this, "radius");
        radius.setExpression("0.5");
        height = new Parameter(this, "height");
        height.setExpression("0.7");

        height.moveToFirst();
        radius.moveToFirst();

        circleDivisions = new Parameter(this, "circleDivisions");
        circleDivisions.setExpression("max(6, roundToInt(radius * 100))");
        circleDivisions.setTypeEquals(BaseType.INT);

        sideDivisions = new Parameter(this, "sideDivisions");
        sideDivisions.setExpression("1");
        sideDivisions.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of divisions in the circles forming the ends of the
     *  cylinder. This is an integer with
     *  default value "max(6, roundToInt(radius * 100))". This parameter
     *  determines the resolution of the cylinder, which is approximated
     *  as a surface composed of rectangular facets. Increasing this
     *  value makes the surface smoother, but also increases the cost
     *  of rendering.
     */
    public Parameter circleDivisions;

    /** The height of the cylinder. This is a double with
     *  default 0.7.
     */
    public Parameter height;

    /** The number of divisions on the side of the cone.
     *  This is an integer with default value "1". This parameter
     *  probably only needs to change when the <i>wireFrame</i> option
     *  is set to true.
     */
    public Parameter sideDivisions;

    /** The radius of the cylinder. This is a double with
     *  default 0.5.
     */
    public Parameter radius;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the dimensions change, then update the box.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Check that a box has been previously created.
        if (attribute == radius || attribute == height) {
            if (_scaleTransform != null) {
                float radiusValue = (float) ((DoubleToken) radius.getToken())
                        .doubleValue();
                float heightValue = (float) ((DoubleToken) height.getToken())
                        .doubleValue();

                _scaleTransform.setScale(new Vector3d(radiusValue, heightValue,
                        radiusValue));

                // The following seems to be needed so the new scale
                // takes effect.
                ((TransformGroup) _containedNode).setTransform(_scaleTransform);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated cylinder.
     *  @exception IllegalActionException If the value of some
     *   parameter can't be obtained.
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        float radiusValue = (float) ((DoubleToken) radius.getToken())
                .doubleValue();
        float heightValue = (float) ((DoubleToken) height.getToken())
                .doubleValue();

        boolean allowChanges = ((BooleanToken) allowRuntimeChanges.getToken())
                .booleanValue();

        int primitiveFlags = Primitive.GENERATE_NORMALS;
        URL textureURL = texture.asURL();

        if (textureURL != null || allowChanges) {
            primitiveFlags = primitiveFlags | Primitive.GENERATE_TEXTURE_COORDS;
        }

        if (allowChanges) {
            // Sharing the geometry leads to artifacts when changes
            // are made at run time.
            primitiveFlags = primitiveFlags | Primitive.GEOMETRY_NOT_SHARED;
        }

        int circleDivisionsValue = ((IntToken) circleDivisions.getToken())
                .intValue();
        int sideDivisionsValue = ((IntToken) sideDivisions.getToken())
                .intValue();

        if (allowChanges) {
            Cylinder cylinder = new Cylinder(1.0f, 1.0f, primitiveFlags,
                    circleDivisionsValue, sideDivisionsValue, _appearance);

            TransformGroup scaler = new TransformGroup();
            scaler.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            _scaleTransform = new Transform3D();
            _scaleTransform.setScale(new Vector3d(radiusValue, heightValue,
                    radiusValue));
            scaler.setTransform(_scaleTransform);
            scaler.addChild(cylinder);
            _containedNode = scaler;
        } else {
            _containedNode = new Cylinder(radiusValue, heightValue,
                    primitiveFlags, circleDivisionsValue, sideDivisionsValue,
                    _appearance);
            _scaleTransform = null;
        }
    }

    /** Return the encapsulated Java3D node of this 3D actor.
     *  The encapsulated node for this actor is a Java3D Cylinder.
     *  @return The Java3D Cylinder.
     */
    @Override
    protected Node _getNodeObject() {
        return _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** If changes to the radius are allowed, this is the transform
     *  that applies them.
     */
    private Transform3D _scaleTransform;

    /** The contained cylinder. */
    private Node _containedNode;
}
