/* A GR Shape consisting of a sphere.

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

import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;

///////////////////////////////////////////////////////////////////
//// Sphere3D

/** This actor contains the geometry and appearance specifications for a
 sphere.  The output port is used to connect this actor to the Java3D scene
 graph. This actor may be used along with the Scale3D transformer to produce
 ellipsoid shapes. This actor will only have meaning in the GR domain.
 Note that most of the parameters are described in the base class documentation.

 @author C. Fong, Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (liuxj)
 */
public class Sphere3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Sphere3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        radius = new Parameter(this, "radius");
        radius.setExpression("0.1");
        radius.setTypeEquals(BaseType.DOUBLE);
        radius.moveToFirst();

        divisions = new Parameter(this, "divisions");
        divisions.setExpression("max(6, roundToInt(radius * 300))");
        divisions.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of divisions in the sphere. This is an integer with
     *  default value "max(6, roundToInt(radius * 300))". This parameter
     *  determines the resolution of the sphere, which is approximated
     *  as a surface composed of triangular facets. Increasing this
     *  value makes the surface smoother, but also increases the cost
     *  of rendering.
     */
    public Parameter divisions;

    /** The radius of the sphere. This is a double with default 0.1.
     */
    public Parameter radius;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is the <i>radius</i>, then modify the
     *  sphere to the new radius. Note that this will take effect
     *  only if the <i>allowRuntimeChanges</i> parameter has value true.
     *  @param attribute The attribute to change.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == radius && _changesAllowedNow) {
            if (_scaleTransform != null) {
                double scale = ((DoubleToken) radius.getToken()).doubleValue();
                _scaleTransform.setScale(new Vector3d(scale, scale, scale));

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

    /** Create the shape and appearance of the sphere.
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained.
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        int primitiveFlags = Primitive.GENERATE_NORMALS;
        URL textureURL = texture.asURL();

        if (textureURL != null || _changesAllowedNow) {
            primitiveFlags = primitiveFlags | Primitive.GENERATE_TEXTURE_COORDS;
        }

        if (_changesAllowedNow) {
            // Sharing the geometry leads to artifacts when changes
            // are made at run time.
            primitiveFlags = primitiveFlags | Primitive.GEOMETRY_NOT_SHARED;
        }

        int divisionsValue = ((IntToken) divisions.getToken()).intValue();
        double radiusValue = ((DoubleToken) radius.getToken()).doubleValue();

        // If changes are not allowed, set the radius of the sphere once
        // and for all. Otherwise, use a transform.
        double scale = radiusValue;

        if (_changesAllowedNow) {
            scale = 1.0;
        }

        _containedNode = new Sphere((float) scale, primitiveFlags,
                divisionsValue, _appearance);

        if (_changesAllowedNow) {
            TransformGroup scaler = new TransformGroup();
            scaler.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
            _scaleTransform = new Transform3D();
            _scaleTransform.setScale(new Vector3d(radiusValue, radiusValue,
                    radiusValue));
            scaler.setTransform(_scaleTransform);
            scaler.addChild(_containedNode);
            _containedNode = scaler;
        } else {
            _scaleTransform = null;
        }
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D sphere.
     *
     *  @return the Java3D Sphere
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

    /** The sphere. */
    private Node _containedNode;
}
