package ptolemy.domains.gr.lib;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.util.Enumeration;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Material;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

import java.awt.Color;

///////////////////////////////////////////////////////////////////
//// ViewLayers3D

/**
   A sink actor that renders layered structure for High Speed Sintering (HSS)
   3D printing simulation.
   TODO: This is very preliminary version. Still needs a lot of elaboration,
   including information for each layer (shape, color, etc.).

   @author Hokeun Kim
   @version $Id$
   @since Ptolemy II 11.0
   @Pt.ProposedRating Red (eal)
   @Pt.AcceptedRating Red (acataldo)
 */
public class ViewLayers3D extends TypedAtomicActor {
    /** Construct a ViewLayers3D in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this ViewScreen.
     *  @exception IllegalActionException If this actor
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public ViewLayers3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _isSceneGraphInitialized = false;

        layer = new TypedIOPort(this, "layer", true, false);
        layer.setMultiport(true);
        layer.setAutomaticTypeConversion(false);

        horizontalResolution = new Parameter(this, "horizontalResolution",
                new IntToken(400));
        horizontalResolution.setTypeEquals(BaseType.INT);
        
        layerThickness = new Parameter(this, "layerThickness",
                new DoubleToken(0.02));
        layerThickness.setTypeEquals(BaseType.DOUBLE);

        verticalResolution = new Parameter(this, "verticalResolution",
                new IntToken(400));
        verticalResolution.setTypeEquals(BaseType.INT);

        rotatable = new Parameter(this, "rotatable", new BooleanToken(true));
        rotatable.setTypeEquals(BaseType.BOOLEAN);

        scalable = new Parameter(this, "scalable", new BooleanToken(false));
        scalable.setTypeEquals(BaseType.BOOLEAN);

        translatable = new Parameter(this, "translatable", new BooleanToken(
                false));
        translatable.setTypeEquals(BaseType.BOOLEAN);

        showAxes = new Parameter(this, "showAxes", new BooleanToken(false));
        showAxes.setTypeEquals(BaseType.BOOLEAN);

        iterationSynchronized = new Parameter(this, "iterationSynchronized",
                new BooleanToken(false));
        iterationSynchronized.setTypeEquals(BaseType.BOOLEAN);

        backgroundColor = new ColorAttribute(this, "backgroundColor");
        backgroundColor.setExpression("{0.0, 0.0, 0.0, 1.0}");

        _lastTransform = new Transform3D();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The input scene graph. Actors that produce 3D objects
     *  can be connected to this port for rendering.
     *  The type of this port is sceneGraph.
     */
    public TypedIOPort layer;

    /** The background color. Note that the alpha value (the fourth
     *  element of the array), which would normally specify transparency,
     *  is ignored.
     */
    public ColorAttribute backgroundColor;

    // FIXME: Need a backgroundTexture attribute.

    /** The width in pixels of the display screen.
     *  The larger of the vertical or horizontal size will
     *  correspond by default to one unit of distance, so this
     *  parameter determines the horizontal resolution as well as
     *  the size of the display.
     *  This is an int with default 400.
     */
    public Parameter horizontalResolution;

    /** Boolean variable that determines whether screen update is done
     *  once per iteration. This is a boolean with default false.
     */
    public Parameter iterationSynchronized;
    
    /** Floating-point variable for specifying thickness of each layer.
     */
    public Parameter layerThickness;

    /** Boolean variable that determines whether the user can
     *  rotate the model with the mouse.  This is a boolean with
     *  default true.
     */
    public Parameter rotatable;

    /** Boolean variable that determines whether the user can
     *  move the point of view along the z axis using the mouse.
     *  This is a boolean with default false.
     */
    public Parameter scalable;

    /** Boolean variable that determines whether or not axes are shown.
     *  This parameter is a boolean with default false.
     */
    public Parameter showAxes;

    /** Boolean variable that determines whether the user can
     *  translate the model with the mouse.
     *  This is a boolean with default false.
     */
    public Parameter translatable;

    /** The height in pixels of the display screen.
     *  The larger of the vertical or horizontal size will
     *  correspond by default to one unit of distance, so this
     *  parameter determines the horizontal resolution as well as
     *  the size of the display.
     *  This is an integer with default 400.
     */
    public Parameter verticalResolution;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ViewScreen3D newObject = (ViewScreen3D) super.clone(workspace);
        newObject._lastTransform = new Transform3D();
        return newObject;
    }

    /** Fire this actor.*/
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        
        boolean _hasToken = layer.hasToken(0);

        if (_debugging) {
            _debug("Called fire()");
        }

        if (!_isSceneGraphInitialized) {
            _makeSceneGraphConnection();
        } else {
            if (_hasToken != false) {
                _makeNodeConnection();
            }
        }

        if (_iterationSynchronized) {
            _canvas.swap();
            _canvas.startRenderer();
            _canvas.stopRenderer();
        }
    }

    /** Initialize the execution.  Create the ViewScreen frame if
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _isSceneGraphInitialized = false;
        _layerCount = 0;

        _createViewScreen();

        // Make the frame visible.
        if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }

        Enumeration branches = _simpleUniverse.getLocale().getAllBranchGraphs();

        while (branches.hasMoreElements()) {
            BranchGroup branchGroup = (BranchGroup) branches.nextElement();

            if (branchGroup.getCapability(BranchGroup.ALLOW_DETACH)) {
                if (!(branchGroup instanceof com.sun.j3d.utils.universe.ViewingPlatform)) {
                    _simpleUniverse.getLocale().removeBranchGraph(branchGroup);
                }
            }
        }

        _branchRoot = new BranchGroup();
        _branchRoot.setCapability(BranchGroup.ALLOW_DETACH);

        _userTransformation = new TransformGroup(_lastTransform);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        _userTransformation.setCapability(Group.ALLOW_CHILDREN_EXTEND);
        _branchRoot.addChild(_userTransformation);

        _bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);

        Background backg = _makeBackground();
        backg.setApplicationBounds(_bounds);
        _branchRoot.addChild(backg);

        if (_isRotatable()) {
            _mouseRotate = new MouseRotateView(this);
            _mouseRotate.setTransformGroup(_userTransformation);
            _mouseRotate.setSchedulingBounds(_bounds);
            _branchRoot.addChild(_mouseRotate);
        }

        if (_isScalable()) {
            MouseZoom mouseZoom = new MouseZoom();
            mouseZoom.setTransformGroup(_userTransformation);
            mouseZoom.setSchedulingBounds(_bounds);
            _branchRoot.addChild(mouseZoom);
        }

        if (_isTranslatable()) {
            MouseTranslate mouseTranslate = new MouseTranslate();
            mouseTranslate.setTransformGroup(_userTransformation);
            _userTransformation.addChild(mouseTranslate);
            mouseTranslate.setSchedulingBounds(_bounds);
        }

        // FIXME: should implement this so that user can dynamically
        // modify this value during design-time and run-time
        // right now this is only user-changeable during initialization
        if (_isIterationSynchronized()) {
            _iterationSynchronized = true;
        } else {
            _iterationSynchronized = false;
        }

        if (_shouldShowAxes()) {
            Sphere origin = new Sphere((float) 0.05);
            _userTransformation.addChild(origin);

            Cylinder yAxis = new Cylinder((float) 0.01, (float) 6.0);
            _userTransformation.addChild(yAxis);

            Cylinder xAxis = new Cylinder((float) 0.01, (float) 6.0);
            Transform3D rotation = new Transform3D();
            Quat4d quat = new Quat4d();
            quat.set(new AxisAngle4d(0.0, 0.0, 1.0, Math.PI / 2.0));
            rotation.set(quat);

            TransformGroup xAxisGroup = new TransformGroup(rotation);
            xAxisGroup.addChild(xAxis);
            _userTransformation.addChild(xAxisGroup);

            Cylinder zAxis = new Cylinder((float) 0.01, (float) 6.0);
            Transform3D rotation2 = new Transform3D();
            Quat4d quat2 = new Quat4d();
            quat2.set(new AxisAngle4d(1.0, 0.0, 0.0, Math.PI / 2.0));
            rotation2.set(quat2);

            TransformGroup zAxisGroup = new TransformGroup(rotation2);
            zAxisGroup.addChild(zAxis);
            _userTransformation.addChild(zAxisGroup);
        }

        // Setup the lights.
        BranchGroup lightRoot = new BranchGroup();

        AmbientLight lightAmbient = new AmbientLight(new Color3f(0.8f, 0.8f,
                0.8f));
        lightAmbient.setInfluencingBounds(_bounds);
        lightRoot.addChild(lightAmbient);

        DirectionalLight lightDirectional = new DirectionalLight();
        lightDirectional.setInfluencingBounds(_bounds);

        Vector3f direction = new Vector3f(0.0f, -1.0f, -1.0f);
        direction.normalize();
        lightDirectional.setDirection(direction);
        lightDirectional.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        lightRoot.addChild(lightDirectional);

        _simpleUniverse.getViewer().getView().setLocalEyeLightingEnable(true);
        _simpleUniverse.addBranchGraph(lightRoot);

        if (_iterationSynchronized) {
            if (_canvas != null) {
                _canvas.stopRenderer();
            }
        }
    }

    @Override
    public boolean postfire() throws IllegalActionException {
        //boolean _hasToken = sceneGraphIn.hasToken(0);
        if (_debugging) {
            _debug("Called postfire()");
        }

        return !_stopRequested;
    }

    /** Wrapup an execution.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        if (_userTransformation != null) {
            _userTransformation.getTransform(_lastTransform);
        }

        if (_iterationSynchronized) {
            _canvas.stopRenderer();
            _canvas.swap();

            if (_mouseRotate != null) {
                _mouseRotate.stopped();
            }

            _canvas.startRenderer();
        }

        _isSceneGraphInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private classes                   ////

    private static class MouseRotateView extends MouseRotate {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.
    
        public MouseRotateView(ViewLayers3D viewContainer) {
            super();
            _viewContainer = viewContainer;
        }
    
        @Override
        public void processStimulus(java.util.Enumeration criteria) {
            if (stopped != true) {
                _viewContainer._startRenderer();
            }
    
            super.processStimulus(criteria);
    
            if (stopped != true) {
                _viewContainer._stopRenderer();
            }
        }
    
        public void stopped() {
            stopped = true;
        }
    
        boolean stopped = false;
    
        ViewLayers3D _viewContainer;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor.
     */
    private void _addChild(Node node) {
        if (_debugging) {
            _debug("Called _addChild(Node node)");
        }
        _userTransformation.addChild(node);
    }
    
    /**
     * Add a layer to be viewed.
     * @throws IllegalActionException If there is a problem reading
     *  a parameter.
     */
    private void _addLayer() throws IllegalActionException {
    
        int width = layer.getWidth();
    
        int primitiveFlags = Primitive.GENERATE_NORMALS;
        Appearance appearance = new Appearance();
        Material material = new Material();
        float red = 0.7f;
        float green = 0.7f;
        float blue = 0.7f;
        float alpha = 1.0f;
        Color color = new Color(red, green, blue, alpha);
        Color3f color3f = new Color3f(color);
        material.setDiffuseColor(color3f);
        appearance.setMaterial(material);
        DoubleToken thicknessToken = (DoubleToken)layerThickness.getToken();
        
        for (int i = 0; i < width; i++) {
            BranchGroup branchGroup = new BranchGroup();
            DoubleToken layerToken = (DoubleToken) layer.get(i);
            
            float xLen = (float)layerToken.doubleValue();
            float yLen = (float)layerToken.doubleValue();
            float zLen = (float)thicknessToken.doubleValue()/2;
            Node node = new Box(xLen, yLen, zLen, primitiveFlags, appearance);
            
            Transform3D transform = new Transform3D();
            float xOffset = 0.0f;
            float yOffset = 0.0f;
            
            float zOffset = (float)(thicknessToken.doubleValue() * _layerCount);
            _layerCount++;

            transform.setTranslation(new Vector3d(xOffset, yOffset, zOffset));
            TransformGroup transformGroup = new TransformGroup();
            transformGroup.setTransform(transform);
            transformGroup.addChild(node);
            branchGroup.addChild(transformGroup);
            _addChild(branchGroup);
        }
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     *  @exception IllegalActionException If there is a problem reading
     *  a parameter.
     */
    private void _createViewScreen() throws IllegalActionException {
        GraphicsConfiguration config = SimpleUniverse
                .getPreferredConfiguration();

        int horizontalDimension = 400;
        int verticalDimension = 400;

        try {
            horizontalDimension = _getHorizontalPixels();
            verticalDimension = _getVerticalPixels();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to get horizontal " + "or vertical pixels");
        }

        // Create a frame, if placeable was not called.
        if (_container == null) {
            _frame = new JFrame("ViewScreen");
            _frame.setVisible(true);
            _frame.validate();
            _frame.setSize(horizontalDimension + 50, verticalDimension);
            _container = _frame.getContentPane();
        }

        // Set the frame to be visible.
        if (_frame != null) {
            _frame.setVisible(true);
        }

        // Lastly drop the canvas in the frame.
        if (_canvas != null) {
            _container.remove(_canvas);
        }

        _canvas = new Canvas3D(config);

        _container.add("Center", _canvas);
        _canvas.setSize(new Dimension(horizontalDimension, verticalDimension));
        _simpleUniverse = new SimpleUniverse(_canvas);
        _simpleUniverse.getViewingPlatform().setNominalViewingTransform();

        // Must call validate after calling add in case this is called
        // from initialize AND from place.  (This caused some
        // artifacts if the run window was used)
        _container.validate();
    }

    /** Get the number of horizontal pixels in the rendered image.
     *  @return the number of horizontal pixels in the rendered image.
     *  @exception IllegalActionException If thrown while reading the
     *  <i>horizontalPixels</i> parameter.
     */
    private int _getHorizontalPixels() throws IllegalActionException {
        return ((IntToken) horizontalResolution.getToken()).intValue();
    }

    /** Get the number of vertical pixels in the rendered image.
     *  @return the number of vertical pixels in the rendered image.
     *  @exception IllegalActionException If thrown while reading the
     *  <i>verticalPixels</i> parameter.
     */
    private int _getVerticalPixels() throws IllegalActionException {
        return ((IntToken) verticalResolution.getToken()).intValue();
    }

    private boolean _isIterationSynchronized() throws IllegalActionException {
        return ((BooleanToken) iterationSynchronized.getToken()).booleanValue();
    }

    private boolean _isRotatable() throws IllegalActionException {
        return ((BooleanToken) rotatable.getToken()).booleanValue();
    }

    private boolean _isScalable() throws IllegalActionException {
        return ((BooleanToken) scalable.getToken()).booleanValue();
    }

    private boolean _isTranslatable() throws IllegalActionException {
        return ((BooleanToken) translatable.getToken()).booleanValue();
    }

    /** Makes the background for the viewScreen.
     *
     *  @return javax.media.j3d.Background
     *  @exception IllegalActionException If unable to read the color
     * parameter.
     */
    private Background _makeBackground() throws IllegalActionException {
        Color3f color = new Color3f(backgroundColor.asColor());
        return new Background(color);
    }

    /** Connect each of the channels of the <i>sceneGraphIn</i> port
     *  to the scene.
     *  @exception IllegalActionException If thrown while reading from a
     *  channel.
     */
    private void _makeNodeConnection() throws IllegalActionException {
        if (_debugging) {
            _debug("Called _makeNodeConnection()");
        }

        _addLayer();
    }

    /** Setup the scene graph connections of this actor.
     */
    private void _makeSceneGraphConnection() throws IllegalActionException {
        if (_debugging) {
            _debug("Called _makeSceneGraphConnection()");
        }
        
        _addLayer();

        _branchRoot.compile();
        _simpleUniverse.addBranchGraph(_branchRoot);
        _isSceneGraphInitialized = true;
    }

    private boolean _shouldShowAxes() throws IllegalActionException {
        return ((BooleanToken) showAxes.getToken()).booleanValue();
    }

    /** Start the internal Java3D renderer.
     */
    private void _startRenderer() {
        if (_iterationSynchronized) {
            _canvas.startRenderer();
        }
    }

    /** Stop the internal Java3D renderer.
     */
    private void _stopRenderer() {
        if (_iterationSynchronized) {
            _canvas.stopRenderer();
        }
    }
    
    /** The BoundingSphere. */
    private BoundingSphere _bounds;

    /** The main connection branch that connects to the universe. */
    private BranchGroup _branchRoot;

    /** The Java3D canvas component. */
    private Canvas3D _canvas;

    /** The container set in the place() method, or the content pane of the
     * created frame if place was not called.
     */
    private Container _container;

    /** The frame containing our canvas, if we created it. */
    private JFrame _frame;

    /** True for manual rendering, false for default rendering.
     */
    private boolean _iterationSynchronized = false;

    /** The last transform. */
    private Transform3D _lastTransform = new Transform3D();

    /** The number of layers currently in the view. */
    private int _layerCount;

    /** The mouse rotate view. */
    private MouseRotateView _mouseRotate;

    /** The Java3D universe, displayed inside the canvas. */
    private SimpleUniverse _simpleUniverse;

    /** The user transformation. */
    private TransformGroup _userTransformation;

    /** Indicator of whether the scene graph is initialized. */
    private boolean _isSceneGraphInitialized;
}
