/* An abstract base class for shaded GR Actors

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

import javax.media.j3d.Appearance;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.j3d.utils.image.TextureLoader;

//////////////////////////////////////////////////////////////////////////
//// GRShadedShape

/** An abstract base class for GR Actors that have material and color
    properties.
    <p>
    The parameter <i>diffuseColor</i> determines
    the color of the object in the usual sense that it determines the
    color of light reflected off the object. The default is gray.
    The parameter <i>emissiveColor</i> specifies a color that is
    emitted by the object, and hence does not depend on illumination.
    It is black by default, which means that the object does not emit
    any light and will be invisible without illumination. The
    parameter <i>specularColor</i> determines the color of highlights
    that are reflected by the object if the object is set to be shiny.
    <p>
    The parameter <i>shininess</i> determines the shininess of the object.
    It ranges from 1.0 (the default) to 128.0, meaning not shiny to very
    shiny.
    <p>
    The parameter <i>transparency</i> determines the transparency of the
    object. It ranges from 0.0 (the default) to 1.0, meaning opaque to
    fully transparent.
    <p>
    The <i>wireFrame</i> parameter can be used to view only the lines
    that outline the polygons of the object and not the surface.

    @author C. Fong, Steve Neuendorffer, Edward A. Lee
    @version $Id$
    @since Ptolemy II 1.0
    @Pt.ProposedRating Red (chf)
    @Pt.AcceptedRating Red (chf)
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

        diffuseColor = new ColorAttribute(this, "diffuseColor");
        diffuseColor.setExpression("{0.7, 0.7, 0.7, 1.0}");

        emissiveColor = new ColorAttribute(this, "emissiveColor");
        emissiveColor.setExpression("{0.0, 0.0, 0.0, 1.0}");

        specularColor = new ColorAttribute(this, "specularColor");
        specularColor.setExpression("{1.0, 1.0, 1.0, 1.0}");

        texture = new FileParameter(this, "texture");

        shininess = new Parameter(this,"shininess");
        shininess.setExpression("1.0");
        shininess.setTypeEquals(BaseType.DOUBLE);

        transparency = new Parameter(this,"transparency");
        transparency.setExpression("0.0");
        transparency.setTypeEquals(BaseType.DOUBLE);

        wireFrame = new Parameter(this,"wireFrame");
        wireFrame.setExpression("false");
        wireFrame.setTypeEquals(BaseType.BOOLEAN);

        flat = new Parameter(this,"flat");
        flat.setExpression("false");
        flat.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The diffuse color, which is the color of the object reflecting
     *  illumination. Note that the alpha value (the fourth
     *  element of the array), which would normally specify transparency,
     *  is ignored. The default color is grey.
     */
    public ColorAttribute diffuseColor;

    /** The emissive color, which is a color that does not depend on
     *  ambient illumination. Note that the alpha value (the fourth
     *  element of the array), which would normally specify transparency,
     *  is ignored. The default color is black, which means that there
     *  is no emissive color (illumination is required).
     */
    public ColorAttribute emissiveColor;

    /** The output port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphOut;

    /** The shininess of the 3D shape.
     *  This parameter should contain a DoubleToken in the range 1.0
     *  to 128.0, where 1.0 represents not shiny and 128.0 represents
     *  very shiny.  This is a double with default 1.0.
     */
    public Parameter shininess;

    /** The specular color, which is a color of a highlight reflecting
     *  ambient illumination. Note that the alpha value (the fourth
     *  element of the array), which would normally specify transparency,
     *  is ignored. The default color is white, which means that the
     *  illumination is reflected white.
     */
    public ColorAttribute specularColor;

    /** Texture URL. FIXME.
     *
     */
    public FileParameter texture;

    /** The transparency, where 0.0 means opaque (the default) and 1.0
     *  means fully transparent. The type is double.
     */
    public Parameter transparency;

    /** If true, render the shape using a wire frame. This is a boolean
     *  that defaults to false.
     */
    public Parameter wireFrame;

    /** If true, render the facets flat rather than rounded.
     *  This is a boolean that defaults to false.
     */
    public Parameter flat;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /*  Create the Java3D geometry and appearance for this GR actors.
     *  @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _createModel();
    }

    /** Return false if the scene graph is already initialized.
     *  @return False if the scene graph is already initialized.
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

    /** Create the material appearance of the shaded 3D actor.
     *  @exception IllegalActionException If a parameter cannot be evaluated.
     */
    protected void _createAppearance() throws IllegalActionException {

        _material = new Material();
        _appearance = new Appearance();

        Color3f color = new Color3f(emissiveColor.asColor());
        _material.setEmissiveColor(color);

        color = new Color3f(diffuseColor.asColor());
        _material.setDiffuseColor(color);

        float shine = (float)
            ((DoubleToken)shininess.getToken()).doubleValue();
        if (shine > 1.0) {
            color = new Color3f(specularColor.asColor());
            _material.setSpecularColor(color);
            _material.setShininess(shine);
        } else {
            _material.setSpecularColor(color);
        }
        _appearance.setMaterial(_material);

        // Deal with transparent attributes.
        float transparent = (float)
            ((DoubleToken)transparency.getToken()).doubleValue();
        if (transparent > 0.0) {
            TransparencyAttributes attributes
                = new TransparencyAttributes(
                        TransparencyAttributes.NICEST, transparent);
            _appearance.setTransparencyAttributes(attributes);
        }

        // Deal with flat attribute.
        if (((BooleanToken)flat.getToken()).booleanValue()) {
            ColoringAttributes coloring
                = new ColoringAttributes(0.7f, 0.7f, 0.7f,
                        ColoringAttributes.SHADE_FLAT);
            _appearance.setColoringAttributes(coloring);
        }

        // Deal with wireFrame attribute.
        int mode = PolygonAttributes.POLYGON_FILL;
        if (((BooleanToken)wireFrame.getToken()).booleanValue()) {
            mode = PolygonAttributes.POLYGON_LINE;
        }
        // Default culls back facing polygons, which is weird.
        // We disable that here.
        PolygonAttributes polygonAttributes = new PolygonAttributes(
                mode,
                PolygonAttributes.CULL_NONE,
                0.0f);
        _appearance.setPolygonAttributes(polygonAttributes);

        // Turn on antialiasing.
        // FIXME: Doesn't seem to work.
        LineAttributes lineAttributes = new LineAttributes(
                1.0f,
                LineAttributes.PATTERN_SOLID,
                true);
        _appearance.setLineAttributes(lineAttributes);
    }

    /** Set the color and appearance of this 3D object.
     *  @exception IllegalActionException If a parameter cannot be evaluated.
     */
    protected void _createModel() throws IllegalActionException {
        _createAppearance();
    }

    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
    }

    /** Override the base class to set the texture, if one is specified,
     *  now that the view screen is known.
     *  @exception IllegalActionException If the given actor is not a
     *   ViewScreen3D or if an invalid texture is specified.
     */
    protected void _setViewScreen(GRActor actor)
            throws IllegalActionException {
        super._setViewScreen(actor);
        URL textureURL = texture.asURL();
        if (textureURL != null) {
            TextureLoader loader;
            loader = new TextureLoader(textureURL, _viewScreen.getCanvas());
            Texture loadedTexture = loader.getTexture();

            if (loadedTexture != null) {

                TextureAttributes attributes = new TextureAttributes();
                attributes.setTextureMode(TextureAttributes.MODULATE);
                _appearance.setTextureAttributes(attributes);

                _appearance.setTexture(loadedTexture);
            }
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The appearance of this 3D object. */
    protected Appearance _appearance;

    /** The material of this 3D object. */
    protected Material _material;
}
