/* An abstract base class for shaded GR Actors

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import javax.media.j3d.Appearance;
import javax.media.j3d.Material;
import javax.vecmath.Color3f;

//////////////////////////////////////////////////////////////////////////
//// GRShadedShape
/** An abstract base class for GR Actors that have material and color
properties. The parameters <i>redComponent</i>, <i>greenComponent</i>,
<i>blueComponent</i> determine the color of the object.  The parameter
<i>shininess</i> determines the Phong exponent used in calculating
the shininess of the object.

@author C. Fong, Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
abstract public class GRShadedShape extends GRActor3D {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRShadedShape(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(SceneGraphToken.TYPE);

        rgbColor = new Parameter(this,"RGB color",
                new DoubleMatrixToken(new double[][] {{ 0.7, 0.7, 0.7}} ));

        shininess = new Parameter(this,"shininess", new DoubleToken(0.0));
        _color = new Color3f(1.0f, 1.0f, 1.0f);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The output port for connecting to other GR Actors in
     *  the scene graph
     */
    public TypedIOPort sceneGraphOut;


    /** The red, green, and blue color components of the 3D shape
     */
    public Parameter rgbColor;

    /** The shininess of the 3D shape.
     *  This parameter should contain a DoubleToken.
     *  The default value of this parameter is DoubleToken ??FIXME
     */
    public Parameter shininess;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /*  Create the Java3D geometry and appearance for this GR actors
     *
     *  @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _createModel();
    }


    /** Return false if the scene graph is already initialized.
     *
     *  @return false if the scene graph is already initialized.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public boolean prefire() throws IllegalActionException {
        if (_isSceneGraphInitialized) {
            return false;
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the material appearance of the shaded 3D actor
     */
    protected void _createAppearance() {

        _material = new Material();
        _appearance = new Appearance();

        _material.setDiffuseColor(_color);
        if (_shine > 1.0) {
            _material.setSpecularColor(_whiteColor);
            _material.setShininess(_shine);
        } else {
            _material.setSpecularColor(_color);
        }
        _appearance.setMaterial(_material);
    }

    /** Create the color of this shaded GR actor
     *
     *  @exception IllegalActionException If unable to setup the color.
     */
    protected void _createModel() throws IllegalActionException {
        DoubleMatrixToken color = (DoubleMatrixToken) rgbColor.getToken();

        _color.x = (float) color.getElementAt(0, 0);
        _color.y = (float) color.getElementAt(0, 1);
        _color.z = (float) color.getElementAt(0, 2);
        _shine = (float) ((DoubleToken) shininess.getToken()).doubleValue();

        _createAppearance();
    }

    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected Color3f _color;
    protected Appearance _appearance;
    protected Material _material;
    protected float _shine;

    protected static final Color3f _whiteColor = new Color3f(1.0f, 1.0f, 1.0f);
    protected static final Color3f _blueColor = new Color3f(0.0f, 0.0f, 1.0f);
}
