/* A GR Shape consisting of a sphere.

Copyright (c) 1998-2004 The Regents of the University of California.
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

//////////////////////////////////////////////////////////////////////////
//// Sphere3D

/** This actor contains the geometry and appearance specifications for a
    sphere.  The output port is used to connect this actor to the Java3D scene
    graph. This actor may be used along with the Scale3D transformer to produce
    ellipsoid shapes. This actor will only have meaning in the GR domain.

    @author C. Fong, Edward A. Lee
    @version $Id$
    @since Ptolemy II 1.0
    @Pt.ProposedRating Red (chf)
    @Pt.AcceptedRating Red (chf)
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


    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == radius) {
            if (scaleTransform != null) {
                double scale = _getRadius();
                scaleTransform.setScale(new Vector3d(scale, scale, scale));
                _scaler.setTransform(scaleTransform);
            }
        }
        super.attributeChanged(attribute);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated sphere
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        int primitiveFlags = Primitive.GENERATE_NORMALS;
        URL textureURL = texture.asURL();
        if (textureURL != null) {
            primitiveFlags = primitiveFlags | Primitive.GENERATE_TEXTURE_COORDS;
        }

        int divisionsValue = ((IntToken)divisions.getToken()).intValue();
        _containedNode = new Sphere(1.0f,
                primitiveFlags,
                divisionsValue,
                _appearance);
        _scaler = new TransformGroup();
        _scaler.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        scaleTransform = new Transform3D();
        double scale = _getRadius();
        scaleTransform.setScale(new Vector3d(scale, scale, scale));
        _scaler.setTransform(scaleTransform);
        _scaler.addChild(_containedNode);
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D sphere.
     *
     *  @return the Java3D Sphere
     */
    protected Node _getNodeObject() {
        return (Node) _scaler;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**  Return the value of the radius parameter
     *  @return the radius of the sphere
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    private double _getRadius() throws IllegalActionException {
        return ((DoubleToken) radius.getToken()).doubleValue();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private Transform3D scaleTransform;
    private TransformGroup _scaler;
    private Sphere _containedNode;
}
