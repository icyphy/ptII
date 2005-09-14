/* Base class for all actors that render arbitrary shapes in 3-D.

 Copyright (c) 1999-2005 The Regents of the University of California.
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


import com.sun.j3d.utils.image.TextureLoader;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Geometry;
import javax.media.j3d.LineAttributes;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.RenderingAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransparencyAttributes;


import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.DoubleRangeParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// GRGeometry
/**
   A base class for all actors in the GR domain that render arbitrary 
   shapes in 3-D as 2-D planes.  Its purpose is to define common 
   paramaters and methods for these actors.
   
   @author Tiffany Crawford
   @version $Id$
   @see classname (refer to relevant classes, but not the base class)
   @since Ptolemy II x.x
   @Pt.ProposedRating Red (tsc)
   @Pt.AcceptedRating Red (reviewmoderator)
*/
 
 
abstract public class GRGeometry extends GRActor3D {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRGeometry(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        allowRuntimeChanges = new Parameter(this, "allowRuntimeChanges");
        allowRuntimeChanges.setExpression("false");
        allowRuntimeChanges.setTypeEquals(BaseType.BOOLEAN);
        
        //FIXME How do I use static fields with this expression?
        axis = new Parameter(this, "axis");
        axis.setExpression("1");
        axis.setTypeEquals(BaseType.INT);

        input = new TypedIOPort(this, "input");
        input.setInput(true);
        input.setTypeEquals(BaseType.OBJECT);

        //FIXME Decide whether to use plane spacing or nSlices
        /*nSlices = new Parameter(this, "nSlices");
        nSlices.setExpression("50");
        nSlices.setTypeEquals(BaseType.INT);*/

        planeSpacing = new Parameter(this, "planeSpacing");
        planeSpacing.setExpression(".0125");
        planeSpacing.setTypeEquals(BaseType.DOUBLE);
              
        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(SceneGraphToken.TYPE);

        //texture = new FilePortParameter(this, "texture");
        //texture.setExpression("");

        transparency = new DoubleRangeParameter(this, "transparency");
        transparency.setExpression("1.0");
        transparency.setTypeEquals(BaseType.DOUBLE);

        /*nSlices = new Parameter(this, "nSlices");
         nSlices.setExpression("50");
         nSlices.setTypeEquals(BaseType.INT);*/


    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** If true, then changes to parameter values can be made during
     *  execution of the model. This is a boolean that defaults to false.
     */
    public Parameter allowRuntimeChanges;

    /** The axis in which the volume will be built upon. 
     * Decides the orientation of the volume.
     */ 
    public Parameter axis;

    /** The size of the spacing between the 2-D planes 
     * that comprise the volume being rendered.
     */
    public Parameter planeSpacing;
    //public Parameter nSlices;

    /** The output port for connecting to other GR Actors in
     *  the scene graph. The type is SceneGraphToken.
     */
    public TypedIOPort sceneGraphOut;

    //FIXME Decide whether to define here or in actors themselves
    /** The input port.
     */
    public TypedIOPort input;
    
    /** Texture URL, which if non-empty, specifies an image file
     *  or URL. The image from the file is mapped onto the shape
     *  as a texture.
     */
    //public FilePortParameter texture;
    /** The transparency, where 0.0 means opaque (the default) and 1.0
     *  means fully transparent. The type is double.
     */
    public DoubleRangeParameter transparency;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Do something... (Use the imperative case here, such as:
     *  "Return the most recently recorded event.", not
     *  "Returns the most recently recorded event."
     *  @param parameterName Description of the parameter.
     *  @return Description of the returned value.
     *  @exception ExceptionClass If ... (describe what
     *   causes the exception to be thrown).
     */

    /** Adjust the appearance when an attribute changes if such
     *  an update is supported by the <i>allowRuntimeChanges</i> parameter.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (allowRuntimeChanges != null) {
            if (_changesAllowedNow) {
                if ((attribute == transparency)
                        && (_transparencyAttributes != null)) {
                    float transparent = (float) ((DoubleToken) transparency
                            .getToken()).doubleValue();

                    if (transparent > 0.0) {
                        _transparencyAttributes
                                .setTransparencyMode(TransparencyAttributes.NICEST);
                    } else {
                        _transparencyAttributes
                                .setTransparencyMode(TransparencyAttributes.NONE);
                    }

                    _transparencyAttributes.setTransparency(transparent);
                }
            }
        }
    }

    /** Override the base class to null out private variables.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        GRGeometry newObject = (GRGeometry) super.clone(workspace);
        newObject._appearance = null;
        newObject._polygonAttributes = null;
        newObject._transparencyAttributes = null;
        return newObject;
    }

    /** Initialize variables to parameter values
     * @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // _nSlices = ((IntToken)nSlices.getToken()).intValue();
        _axis = ((IntToken) axis.getToken()).intValue();
        _planeSpacing = ((DoubleToken) planeSpacing.getToken()).doubleValue();
    }

    /** Return false if the scene graph has already been initialized and
     *  there is no token at the input port.
     *  @return False if the scene graph is already initialized.
     *  @exception IllegalActionException Not thrown in this base class
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called prefire()");
            _debug("_isSceneGraphInitialized = " + _isSceneGraphInitialized);
        }
        if (input.hasToken(0)) {
            System.out.println("Has token");
            
            //Set _isSceneGraphInitialized back to false so node
            //can be sent. fire() will set it back to true
            _createModel();
            _isSceneGraphInitialized = false;
            if (_debugging) {
                _debug("Prefire returns true");
            }
            return true;
        } else {
            System.out.println("Does not have token");
            if (_debugging) {
                _debug("Prefire returns false");
            }
            return false;
        }
    }

    /** Override the base class to ensure that material and
     *  appearance objects are created anew.
     *  @exception IllegalActionException If the current director
     *   is not a GRDirector.
     */
    public void preinitialize() throws IllegalActionException {
        _appearance = null;
        _polygonAttributes = null;
        _transparencyAttributes = null;
        _containedNode = null;
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
        _polygonAttributes = null;
        _transparencyAttributes = null;
        _containedNode = null;
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

        // Deal with transparent attributes.
        float transparent = (float) ((DoubleToken) transparency.getToken())
                .doubleValue();

        if ((transparent > 0.0) || allowChanges) {
            //FIXME What mode should be used?  What does NICEST do?
            int mode = TransparencyAttributes.NICEST;

            if (transparent == 0.0) {
                mode = TransparencyAttributes.NONE;
            }

            _transparencyAttributes = new TransparencyAttributes(mode,
                    transparent);
            //_transparencyAttributes = new TransparencyAttributes();
            //_transparencyAttributes.setTransparencyMode(TransparencyAttributes.NICEST);
            _appearance.setTransparencyAttributes(_transparencyAttributes);
        }

        // Default culls back facing polygons, which is weird.
        // We disable that here.
        int mode = PolygonAttributes.POLYGON_FILL;
        _polygonAttributes = new PolygonAttributes(mode,
                PolygonAttributes.CULL_NONE, 0.0f);
        _appearance.setPolygonAttributes(_polygonAttributes);

        // Turn on antialiasing.
        LineAttributes lineAttributes = new LineAttributes(1.0f,
                LineAttributes.PATTERN_SOLID, true);
        _appearance.setLineAttributes(lineAttributes);

        // If runtime changes are allowed, we need to set the
        // appropriate capabilities.
        if (allowChanges) {
            _transparencyAttributes
                    .setCapability(TransparencyAttributes.ALLOW_VALUE_WRITE);
            _transparencyAttributes
                    .setCapability(TransparencyAttributes.ALLOW_MODE_WRITE);
            _material.setCapability(Material.ALLOW_COMPONENT_WRITE);
            _appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);
            _polygonAttributes
                    .setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
        }

        _changesAllowedNow = allowChanges;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    /** Requires all subclasses to define a _createGeometry method */
    abstract protected void _createGeometry() throws IllegalActionException;


    
    /** Create the BranchGroup to be added to the scenegraph 
     *  This has the side effect of setting the color, appearance 
     *  and geometry of this 3D object.
     *  @exception IllegalActionException If a parameter cannot be evaluated.
     */
    protected void _createModel() throws IllegalActionException {

        if (_debugging) {
            _debug("inside of _createModel()");
        }
        _createAppearance();
        _createGeometry();
        _createdNode = new Shape3D(_geometry, _appearance);
        _containedNode = new BranchGroup();
        ((BranchGroup) _containedNode).addChild(_createdNode);

    }

    /** Return the object to be added to the scene graph*/
    protected Node _getNodeObject() {
        return _containedNode;
    }

    /** Send the scene graph token on the output. */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new SceneGraphToken(_getNodeObject()));
        if (_debugging) {
            _debug("Called _makeSceneGraphConnection()");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The appearance of this 3D object. */
    protected Appearance _appearance;

    /** The axis orientation of this 3D object */
    protected int _axis;
    
    /** Indicator that changes are currently allowed. */
    //FIXME In SDF execution ends so changes are not possible.
    protected boolean _changesAllowedNow = false;
    
    /** The coloring attributes, or null if not created. */
    protected ColoringAttributes _coloringAttributes;

    //protected BranchGroup _containedNode;
    
    /** The 3D object in the form that may be rendered */
    protected Node _containedNode;

    
    /** The 3D object */
    protected Shape3D _createdNode;

    
    /** The geometry of this 3D object */
    protected Geometry _geometry;

    /** The material of this 3D object. */
    protected Material _material;

    //protected int _nSlices;

    /** The plane spacing of this 3D object */
    protected double _planeSpacing;
    
    /** Polygon attributes. */
    protected PolygonAttributes _polygonAttributes;

    /** Rendering attributes. */
    protected RenderingAttributes _renderingAttributes;

    /** The transparency attributes, or null if not created. */
    protected TransparencyAttributes _transparencyAttributes;

    
    //FIXME Would like to connect to axis parameter
    protected static int XAXIS = 0;

    protected static int YAXIS = 1;

    protected static int ZAXIS = 2;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected methods.

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables need not have Javadoc comments, although it can
    // be more convenient if they do, since they may at some point
    // become protected variables.


}
