/* A GR Shape consisting of a sphere

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.gr.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Sphere;

//////////////////////////////////////////////////////////////////////////
//// Sphere3D

/** This actor contains the geometry and appearance specifications for a GR
sphere.  The output port is used to connect this actor to the Java3D scene
graph. This actor may be used along with the Scale3D transformer to produce
ellipsoid shapes. This actor will only have meaning in the GR domain.

@author C. Fong
@version $Id$
@since Ptolemy II 1.0
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

        radius = new Parameter(this, "radius", new DoubleToken(0.5));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The radius of the sphere
     *  This parameter should contain a doubleToken
     *  The default value of this parameter is the DoubleToken 0.5
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
        _containedNode = new Sphere(1.0f,
                Sphere.GENERATE_NORMALS, _appearance);
        //Shape3D sphereShape = _containedNode.getShape();
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
