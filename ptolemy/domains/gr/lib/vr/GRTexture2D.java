/* An actor that performs volume rendering using 2D textures.

Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.domains.gr.lib.vr;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.parameters.DoubleRangeParameter;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.actor.parameters.IntRangeParameter;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.sun.j3d.utils.image.TextureLoader;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.net.URL;

import javax.imageio.stream.FileImageInputStream;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Group;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.OrderedGroup;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Switch;
import javax.media.j3d.TexCoordGeneration;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.View;
import javax.vecmath.Color3f;


//////////////////////////////////////////////////////////////////////////
//// GRTexture2D

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
    shiny. A shiny object reflects the <i>specularColor</i>, unless
    it is black, in which case shininess has no effect.
    <p>
    The <i>texture</i> parameter can be used to specify an image file.
    The specified image will be mapped onto the shape.
    <p>
    The parameter <i>transparency</i> determines the transparency of the
    object. It ranges from 0.0 (the default) to 1.0, meaning opaque to
    fully transparent (which makes the object invisible).
    <p>
    The <i>wireFrame</i> parameter can be used to view only the lines
    that outline the polygons of the object and not the surface.
    The <i>flat</i> parameter can be set to make rendered polygons
    flat rather than rounded at the corners.
    <p>
    The <i>allowRuntimeChanges</i> parameter, if true, specifies
    that changes to parameter values during the execution of the model
    take effect immediately. By default, this parameter is false,
    which means that changes to parameter values take effect only
    on the next run of the model. A value of false yields better
    performance, but less interactivity.  Changing this to true will
    only have an effect on the next run of the model.

    @author Tiffany Crawford
    @version
    @since
    @Pt.ProposedRating Red
    @Pt.AcceptedRating Red
*/
public class GRTexture2D extends GRActor3D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRTexture2D(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        voxelFile = new FilePortParameter(this, "voxelFile");
        voxelFile.setExpression("$CLASSPATH/doc/img/brainMRI.jpg");

        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(SceneGraphToken.TYPE);

        diffuseColor = new ColorAttribute(this, "diffuseColor");
        diffuseColor.setExpression("{0.7, 0.7, 0.7, 1.0}");

        emissiveColor = new ColorAttribute(this, "emissiveColor");
        emissiveColor.setExpression("{0.0, 0.0, 0.0, 1.0}");

        specularColor = new ColorAttribute(this, "specularColor");
        specularColor.setExpression("{1.0, 1.0, 1.0, 1.0}");

        shininess = new DoubleRangeParameter(this, "shininess");
        shininess.min.setExpression("1.0");
        shininess.max.setExpression("128.0");
        shininess.precision.setExpression("128");
        shininess.setExpression("1.0");
        shininess.setTypeEquals(BaseType.DOUBLE);

        transparency = new DoubleRangeParameter(this, "transparency");
        transparency.setExpression("0.0");
        transparency.setTypeEquals(BaseType.DOUBLE);

        wireFrame = new Parameter(this, "wireFrame");
        wireFrame.setExpression("false");
        wireFrame.setTypeEquals(BaseType.BOOLEAN);

        flat = new Parameter(this, "flat");
        flat.setExpression("false");
        flat.setTypeEquals(BaseType.BOOLEAN);

        allowRuntimeChanges = new Parameter(this, "allowRuntimeChanges");
        allowRuntimeChanges.setExpression("false");
        allowRuntimeChanges.setTypeEquals(BaseType.BOOLEAN);

        dim = new IntRangeParameter(this, "sSize");
        dim.setExpression("256");
        dim.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If true, then changes to parameter values can be made during
     *  execution of the model. This is a boolean that defaults to false.
     */
    public Parameter allowRuntimeChanges;

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
     *  the scene graph. The type is SceneGraphToken.
     */
    public TypedIOPort sceneGraphOut;

    /** The shininess of the 3D shape.
     *  This parameter should contain a DoubleToken in the range 1.0
     *  to 128.0, where 1.0 represents not shiny and 128.0 represents
     *  very shiny.  This is a double with default 1.0.
     */
    public DoubleRangeParameter shininess;

    /** The specular color, which is a color of a highlight reflecting
     *  ambient illumination. Note that the alpha value (the fourth
     *  element of the array), which would normally specify transparency,
     *  is ignored. The default color is white, which means that the
     *  illumination is reflected white.
     */
    public ColorAttribute specularColor;

    /* /** Texture URL, which if non-empty, specifies an image file
      *  or URL. The image from the file is mapped onto the shape
      *  as a texture.

    public FileParameter texture; */

    /** The transparency, where 0.0 means opaque (the default) and 1.0
     *  means fully transparent. The type is double.
     */
    public DoubleRangeParameter transparency;

    /** If true, render the shape using a wire frame. This is a boolean
     *  that defaults to false.
     */
    public Parameter wireFrame;

    /** If true, render the facets flat rather than rounded.
     *  This is a boolean that defaults to false.
     */
    public Parameter flat;

    /** This parameter provides a third dimension to the images.  The
     * value is a scaled version of the actual known slice depth.
     */
    public IntRangeParameter dim;

    /** The input port that reads a in a URL to the file holding the
     *  volume to be rendered.
     */
    public FilePortParameter voxelFile;

    /** The input port that reads a in a URL to the file holding the
     *  context.
     */
    public FilePortParameter context;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Adjust the appearance when an attribute changes if such
     *  an update is supported by the <i>allowRuntimeChanges</i> parameter.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        // If allowRuntimeChanges is null, then we are in the
        // constructor, and don't need to do any of this.
        if (allowRuntimeChanges != null) {
            if (_changesAllowedNow) {
                if ((attribute == transparency)
                                && (_transparencyAttributes != null)) {
                    float transparent = (float) ((DoubleToken) transparency
                                    .getToken()).doubleValue();

                    if (transparent > 0.0) {
                        _transparencyAttributes.setTransparencyMode(TransparencyAttributes.NICEST);
                    } else {
                        _transparencyAttributes.setTransparencyMode(TransparencyAttributes.NONE);
                    }

                    _transparencyAttributes.setTransparency(transparent);
                }

                if ((attribute == flat) && (_coloringAttributes != null)) {
                    boolean flatValue = ((BooleanToken) flat.getToken())
                                    .booleanValue();
                    int shadeModel = ColoringAttributes.SHADE_GOURAUD;

                    if (flatValue) {
                        shadeModel = ColoringAttributes.SHADE_FLAT;
                    }

                    _coloringAttributes.setShadeModel(shadeModel);
                }

                if (_material != null) {
                    if (attribute == diffuseColor) {
                        Color3f color = new Color3f(diffuseColor.asColor());
                        _material.setDiffuseColor(color);
                    } else if (attribute == emissiveColor) {
                        Color3f color = new Color3f(emissiveColor.asColor());
                        _material.setEmissiveColor(color);
                    } else if (attribute == specularColor) {
                        Color3f color = new Color3f(specularColor.asColor());
                        _material.setSpecularColor(color);
                    } else if (attribute == shininess) {
                        float shine = (float) ((DoubleToken) shininess.getToken())
                                        .doubleValue();
                        _material.setShininess(shine);
                    }
                }

                /*    if ((attribute == texture) && (_appearance != null)) {
                        URL textureURL = texture.asURL();

                        if ((_viewScreen != null) && (textureURL != null)) {
                            TextureLoader loader;
                            loader = new TextureLoader(textureURL,
                                    _viewScreen.getCanvas());

                            Texture loadedTexture = loader.getTexture();

                            if (loadedTexture != null) {
                                _appearance.setTexture(loadedTexture);
                            }
                        }
                    } */
                if ((attribute == wireFrame) && (_polygonAttributes != null)) {
                    int mode = PolygonAttributes.POLYGON_FILL;

                    if (((BooleanToken) wireFrame.getToken()).booleanValue()) {
                        mode = PolygonAttributes.POLYGON_LINE;
                    }

                    _polygonAttributes.setPolygonMode(mode);
                }
            }
        }

        super.attributeChanged(attribute);
    }

    /** Override the base class to null out private variables.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GRTexture2D newObject = (GRTexture2D) super.clone(workspace);
        newObject._appearance = null;
        newObject._coloringAttributes = null;
        newObject._material = null;
        newObject._polygonAttributes = null;
        newObject._transparencyAttributes = null;
        newObject._textureAttributes = null;
        newObject._texture2D = null;
        return newObject;
    }

    /** Create the Java3D geometry and appearance for this GR actor.
     *  @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _parameterPort = voxelFile.getPort();
        _sSize = (int) ((IntToken) dim.getToken()).intValue();
        _tSize = _sSize;
        _counter = 0;
        _createModel();
    }

    /** Return false if the scene graph is already initialized.
     *  @return False if the scene graph is already initialized.
     *  @exception IllegalActionException Not thrown in this base class
     * @throws
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire()");
        }

        if (_isSceneGraphInitialized) {
            /** Updating the PortParameter, cannot call update()
             * because need to access token for condition
             */
            if ((_parameterPort != null) && (_parameterPort.getWidth() > 0)
                            && _parameterPort.hasToken(0)) {
                _stringToken = (StringToken) (_parameterPort.get(0));
                voxelFile.setCurrentValue(_stringToken);

                if (_debugging) {
                    _debug("Updated parameter value to: " + _stringToken);
                }
            } else {
                if (_debugging) {
                    _debug("Did not update parameter");
                }
            }

            if (_debugging) {
                _debug("Port value = " + _stringToken.stringValue());
            }

            if (_stringToken.stringValue() != null) {
                /** Set _isSceneGraphInitialized back to false so
                 * node can be sent. fire() will set it back to true
                 */
                _createModel();
                _isSceneGraphInitialized = false;

                if (_debugging) {
                    _debug("Prefire returned true");
                    _debug("voxelFile = " + voxelFile);
                }

                return true;
            } else {
                if (_debugging) {
                    _debug("Prefire returned false");
                    _debug("voxelFile = " + voxelFile);
                }

                return false;
            }
        }
        /** Should only reach this code during first prefire()
         * all other times _isSceneGraphInitialized should be true from
         * fire method.
         */
        else {
            if (_debugging) {
                _debug("Prefire returned true");
                _debug("voxelFile = " + voxelFile);
            }

            return true;
        }

        /*        voxelFile.update();
                _fileURL = voxelFile.asURL();

                if (_fileURL != null){
                //    _createModel();
                    return true;
                }
                else {
                 return false;
                } */
    }

    /** Override the base class to ensure that material and
     *  appearance objects are created anew.
     *  @exception IllegalActionException If the current director
     *   is not a GRDirector.
     */
    public void preinitialize() throws IllegalActionException {
        _appearance = null;
        _coloringAttributes = null;
        _material = null;
        _polygonAttributes = null;
        _transparencyAttributes = null;
        super.preinitialize();
    }

    /** Override the base class to set to null the references
     *  to appearance and material. This prevents changes
     *  after the model has finished executing.
     *  @exception IllegalActionException If the current director
     *   is not a GRDirector.
     */
    public void wrapup() throws IllegalActionException {
        _appearance = null;
        _coloringAttributes = null;
        _material = null;
        _polygonAttributes = null;
        _transparencyAttributes = null;
        super.wrapup();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the material appearance of the shaded 3D actor.
     *  This has the side effect of setting the protected variable
     *  _changesAllowedNow so that derived classes can check it.
     *  @exception IllegalActionException If a parameter cannot be evaluated.
     */
    protected void _createAppearance() throws IllegalActionException {
        _material = new Material();
        _appearance = new Appearance();

        boolean allowChanges = ((BooleanToken) allowRuntimeChanges.getToken())
                        .booleanValue();

        Color3f color = new Color3f(emissiveColor.asColor());
        _material.setEmissiveColor(color);

        color = new Color3f(diffuseColor.asColor());
        _material.setDiffuseColor(color);

        color = new Color3f(specularColor.asColor());
        _material.setSpecularColor(color);

        float shine = (float) ((DoubleToken) shininess.getToken()).doubleValue();

        if (shine > 1.0) {
            _material.setShininess(shine);
        }

        _material.setLightingEnable(false);
        _appearance.setMaterial(_material);

        // Deal with transparent attributes.
        //FIXME May not need as a parameter?
        _transparencyAttributes = new TransparencyAttributes();
        _transparencyAttributes.setTransparencyMode(TransparencyAttributes.BLENDED);

        // Deal with flat attribute.
        boolean flatValue = ((BooleanToken) flat.getToken()).booleanValue();

        if (flatValue) {
            _coloringAttributes = new ColoringAttributes(0.7f, 0.7f, 0.7f,
                    ColoringAttributes.SHADE_FLAT);
            _appearance.setColoringAttributes(_coloringAttributes);
        } else if (allowChanges) {
            _coloringAttributes = new ColoringAttributes();
            _appearance.setColoringAttributes(_coloringAttributes);
        }

        // Deal with wireFrame attribute.
        int mode = PolygonAttributes.POLYGON_FILL;

        if (((BooleanToken) wireFrame.getToken()).booleanValue()) {
            mode = PolygonAttributes.POLYGON_LINE;
        }

        // Default culls back facing polygons, which is weird.
        // We disable that here.
        _polygonAttributes = new PolygonAttributes(mode,
                PolygonAttributes.CULL_NONE, 0.0f);

        //FIXME May be a bit repetitive
        _polygonAttributes.setCullFace(PolygonAttributes.CULL_NONE);
        _appearance.setPolygonAttributes(_polygonAttributes);

        // Turn on antialiasing.
        LineAttributes lineAttributes = new LineAttributes(1.0f,
                LineAttributes.PATTERN_SOLID, true);
        _appearance.setLineAttributes(lineAttributes);

        /*if (dbWriteEnable == false) {
        RenderingAttributes r = new RenderingAttributes();
        r.setDepthBufferWriteEnable(dbWriteEnable);
        a.setRenderingAttributes(r);
        } */

        // If runtime changes are allowed, we need to set the
        // appropriate capabilities.
        if (allowChanges) {
            _transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
            _transparencyAttributes.setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
            _material.setCapability(Material.ALLOW_COMPONENT_WRITE);
            _coloringAttributes.setCapability(ColoringAttributes.ALLOW_SHADE_MODEL_WRITE);
            _appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
            _polygonAttributes.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        }

        _changesAllowedNow = allowChanges;
    }

    /** Create the geometry for the Node that will hold the texture.
     */
    protected void _createGeometry(int counter) throws IllegalActionException {
        double scale = .0125;
        double curY = _counter * scale;

        if (_debugging) {
            _debug("counter = " + _counter);
            _debug("curY = " + curY);
            _debug("scale = " + scale);
        }

        //int curY = 0;
        _plane = new QuadArray(4,
                GeometryArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
        _quadCoords = new double[12];
        _texCoords = new float[8];

        //_texCoords = new TexCoord2f[8];

        /** Set coordinates for the plane.  These coordinates assume
         * that the the image's origin is at the lower left and rotates
         * it 90 degrees about the x-axis.
         */

        // lower left
        _quadCoords[0] = -0.5;
        _quadCoords[1] = curY;
        _quadCoords[2] = 0.5;
        _texCoords[0] = 0;
        _texCoords[1] = 0;

        //_texCoords[2]= 0;
        // lower right
        _quadCoords[3] = 0.5;
        _quadCoords[4] = curY;
        _quadCoords[5] = 0.5;
        _texCoords[2] = 1;
        _texCoords[3] = 0;

        //_texCoords[5]= 0;
        // upper right
        _quadCoords[6] = 0.5;
        _quadCoords[7] = curY;
        _quadCoords[8] = -0.5;
        _texCoords[4] = 1;
        _texCoords[5] = 1;

        //_texCoords[8]= 0;
        // upper left
        _quadCoords[9] = -0.5;
        _quadCoords[10] = curY;
        _quadCoords[11] = -0.5;
        _texCoords[6] = 0;
        _texCoords[7] = 1;

        //_texCoords[11]= 0;
        _plane.setCoordinates(0, _quadCoords);
        _plane.setTextureCoordinates(0, 0, _texCoords);
    }

    /** Set the color and appearance of this 3D object.
     *  This has the side effect of setting the protected variable
     *  _changesAllowedNow so that derived classes can check it.
     *  @exception IllegalActionException If a parameter cannot be evaluated.
     */
    protected void _createModel() throws IllegalActionException {
        _readImage();

        _counter++;

        _createAppearance();

        if (_debugging) {
            _debug("Created Appearance");
        }

        if (_isSceneGraphInitialized) {
            _loadTexture();
        }

        _createGeometry(_counter);

        if (_debugging) {
            _debug("Created the geometry");
        }

        BranchGroup branchGroup = new BranchGroup();
        branchGroup.setCapability(BranchGroup.ALLOW_DETACH);

        Shape3D texturedPlane = new Shape3D(_plane, _appearance);
        branchGroup.addChild(texturedPlane);

        _containedNode = branchGroup;
    }

    /** Create the texture used for this 3D object.
     * Define the texture coordinates and textureAttributes.
     * @throws IllegalActionException
     */
    protected void _loadTexture() throws IllegalActionException {
        if (_debugging) {
            _debug("About to loadTexture");
        }

        TextureAttributes attributes = null;

        if (_fileURL != null) {
            TextureLoader loader;
            loader = new TextureLoader(_fileURL, _viewScreen.getCanvas());

            if (_debugging) {
                _debug("Loaded texture");
            }

            Texture loadedTexture = loader.getTexture();

            if (_debugging) {
                _debug("got texture");
            }

            if (loadedTexture != null) {
                attributes = new TextureAttributes();
                attributes.setTextureMode(TextureAttributes.MODULATE);

                _appearance.setTextureAttributes(attributes);

                _appearance.setTexture(loadedTexture);
            }
        }
    }

    /** Return the ??????*/
    protected Node _getNodeObject() {
        return _containedNode;
    }

    //FIXME Make more efficient, put all in one method or in a loop
    protected OrderedGroup _getOrderedGroup() {
        OrderedGroup og = new OrderedGroup();
        og.setCapability(Group.ALLOW_CHILDREN_READ);
        og.setCapability(Group.ALLOW_CHILDREN_WRITE);
        og.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        return og;
    }

    /** Send the scene graph token on the output. */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
    }

    /**Read in file. */
    protected void _readImage() throws IllegalActionException {
        /**Read in image containing data to be mapped*/
        _fileURL = voxelFile.asURL();
        _fileRoot = _fileURL.getFile();
    }

    /** Override the base class to set the texture, if one is specified,
     *  now that the view screen is known.
     *  @exception IllegalActionException If the given actor is not a
     *   ViewScreen3D or if an invalid texture is specified.
     */
    protected void _setViewScreen(GRActor actor) throws IllegalActionException {
        if (_debugging) {
            _debug("Inside of setViewScreen");
        }

        super._setViewScreen(actor);

        if (_debugging) {
            _debug("About to loadTexture");
        }

        TextureAttributes attributes = null;

        if (_fileURL != null) {
            TextureLoader loader;
            loader = new TextureLoader(_fileURL, _viewScreen.getCanvas());

            if (_debugging) {
                _debug("Loaded texture");
            }

            Texture loadedTexture = loader.getTexture();

            if (_debugging) {
                _debug("got texture");
            }

            if (loadedTexture != null) {
                attributes = new TextureAttributes();
                attributes.setTextureMode(TextureAttributes.MODULATE);
                _appearance.setTextureAttributes(attributes);

                _appearance.setTexture(loadedTexture);
            }

            // _textureLoader = new TextureLoader(_fileURL, _viewScreen.getCanvas());
        }

        //Texture2D _texture2D = (Texture2D)(_textureLoader.getTexture());

        /** Set the texture and its attributes */

        /*  _textureAttributes = new TextureAttributes();
          _textureAttributes.setTextureMode(TextureAttributes.REPLACE);
          _textureAttributes.setCapability(TextureAttributes.ALLOW_COLOR_TABLE_WRITE);
          _appearance.setTexture(_texture2D);
          _appearance.setTextureAttributes(_textureAttributes); */
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The appearance of this 3D object. */
    protected Appearance _appearance;

    /** Indicator that changes are currently allowed. */
    protected boolean _changesAllowedNow = false;

    /** The coloring attributes, or null if not created. */
    protected ColoringAttributes _coloringAttributes;

    /** The material of this 3D object. */
    protected Material _material;

    /** Polygon attributes. */
    protected PolygonAttributes _polygonAttributes;

    /** The NodeComponent defining the textureAttributes.  Must be added to the Appearance */
    protected TextureAttributes _textureAttributes;

    /** The transparency attributes, or null if not created. */
    protected TransparencyAttributes _transparencyAttributes;

    /** ?????? */
    protected View _view;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The URL that specifies where the file is located. */
    private URL _fileURL;

    /** The Image. */
    private Node _containedNode;

    /** The NodeComponent defining the texture which must be added to the Appearance */
    private Texture2D _texture2D;

    /** ImageComponent. */
    private ImageComponent2D _imageComponent;

    /** QuadArray. */
    private QuadArray _plane;

    /** Buffer of image data */
    private BufferedImage _bufferedImage;

    /**Defines how to translate the pixels into color and alpha components.*/
    private ColorModel _colorModel;

    /** The ColorSpace that defines the color space of the image */
    private ColorSpace _colorSpace;
    private Shape3D _texturedImage;
    private String _fileRoot;
    private File _file;
    private FileImageInputStream _fileImageInputStream;
    private Texture2D _texture;
    private TexCoordGeneration _texCoordGeneration;
    private WritableRaster _raster;
    private int[] _intData;
    private OrderedGroup _frontGroup;
    private OrderedGroup _backGroup;
    private Switch _axisSwitch;
    private double[] _quadCoords;
    private float[] _texCoords;

    //private TexCoord2f[] _texCoords;
    private TextureLoader _textureLoader;
    private DataBufferInt _dataBufferInt;
    private DataBuffer _dataBuffer;
    private BranchGroup _root;
    private BranchGroup frontShapeGroup;
    private int _sSize;
    private int _tSize;
    private ParameterPort _parameterPort;
    private StringToken _stringToken;
    private int _counter;

    ///////////////////////////////////////////////////////////////////
    ////                         static  variables                 ////
    static final int X_AXIS = 0;
    static final int Y_AXIS = 1;
    static final int Z_AXIS = 2;
    static final int FRONT = 0;
    static final int BACK = 1;
    static final int PLUS_X = 0;
    static final int PLUS_Y = 1;
    static final int PLUS_Z = 2;
    static final int MINUS_X = 3;
    static final int MINUS_Y = 4;
    static final int MINUS_Z = 5;
}
