/* A GR 3D scene viewer.

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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.util.Enumeration;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Background;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.Group;
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3f;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Placeable;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.domains.gr.kernel.ViewScreenInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

///////////////////////////////////////////////////////////////////
//// ViewScreen3D

/** A sink actor that renders the 3D GR geometry into a display screen.

 @author C. Fong, Adam Cataldo, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (acataldo)
 */
public class ViewScreen3D extends GRActor3D implements Placeable,
        ViewScreenInterface {
    // FIXME: Need a reset button to reset the viewpoint to the original.

    /** Construct a ViewScreen in the given container with the given name.
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
    public ViewScreen3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setTypeEquals(SceneGraphToken.TYPE);
        sceneGraphIn.setMultiport(true);

        horizontalResolution = new Parameter(this, "horizontalResolution",
                new IntToken(400));
        horizontalResolution.setTypeEquals(BaseType.INT);

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
    public TypedIOPort sceneGraphIn;

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

    /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor.
     *  @param node The node to be added.
     */
    public void addChild(Node node) {
        _addChild(node);
    }

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
        boolean _hasToken = sceneGraphIn.hasToken(0);

        if (_debugging) {
            _debug("Called fire()");

            //_debug("hasToken = " + _hasToken);
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

        //  _counter = _counter + 1;
    }

    /** Return the root Java 3D rendering group used by this view screen.
     *  @return the root Java 3D rendering group used by this view screen.
     */
    public BranchGroup getBranchGroup() {
        return _branchRoot;
    }

    /** Return the Java 3D canvas used by this view screen.
     *  @return the Java 3D canvas used by this view screen.
     */
    public Canvas3D getCanvas() {
        return _canvas;
    }

    /** Initialize the execution.  Create the ViewScreen frame if
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        //  _counter =1;
        //boolean _hasToken = sceneGraphIn.hasToken(0);
        // Create a frame, if necessary, along with the canvas and
        // simple universe.
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

        //To allow multiple shapes to be added
        /*        _root = new BranchGroup();
         _root.setCapability(BranchGroup.ALLOW_DETACH);
         _root.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
         _root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
         _root.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
         _userTransformation.addChild(_root); */
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

    /** Set the container that this actor should display data in.  If
     * place is not called, then the actor will create its own frame
     * for display.
     */
    @Override
    public void place(Container container) {
        _container = container;

        if (_container == null) {
            return;
        }

        Container c = _container.getParent();

        while (c.getParent() != null) {
            c = c.getParent();
        }

        // If we had created a frame before, then blow it away.
        if (_frame != null) {
            _frame.dispose();
            _frame = null;
        }

        try {
            _createViewScreen();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to create ViewScreen3D.");
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
    ////                         protected methods                 ////

    /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor.
     */
    @Override
    protected void _addChild(Node node) {
        if (_debugging) {
            _debug("Called _addChild(Node node)");
        }

        _userTransformation.addChild(node);

        //_root.addChild(node);
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     *  @exception IllegalActionException If there is a problem reading
     *  a parameter.
     */
    protected void _createViewScreen() throws IllegalActionException {
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

        /* FIXME: experimental code for changing views.
         TransformGroup VPTG = new TransformGroup();
         VPTG = _simpleUniverse.getViewingPlatform()
         .getMultiTransformGroup().getTransformGroup(0);
         Transform3D VPT3D = new Transform3D();
         //VPT3D.lookAt(new Point3d(0.0, 0.0, 10.0),
         //      new Point3d(0.0, 0.0, 0.0),
         //      new Vector3d(0.0, 1.0, 0.0));
         //VPT3D.setTranslation(new Vector3f(0.0f, 0.0f, 10.0f));
         //VPT3D.rotX(Math.PI/2.0);
         VPT3D.rotX(Math.PI/2);
         VPT3D.setTranslation(new Vector3f(0.0f, -10.0f, 0.0f));

         VPTG.setTransform(VPT3D);
         */
    }

    /** Get the number of horizontal pixels in the rendered image.
     *  @return the number of horizontal pixels in the rendered image.
     *  @exception IllegalActionException If thrown while reading the
     *  <i>horizontalPixels</i> parameter.
     */
    protected int _getHorizontalPixels() throws IllegalActionException {
        return ((IntToken) horizontalResolution.getToken()).intValue();
    }

    /** The ViewScreen does not have an associated Java3D node.
     *
     *  @return null
     */
    @Override
    protected Node _getNodeObject() {
        return null;
    }

    /** Get the number of vertical pixels in the rendered image.
     *  @return the number of vertical pixels in the rendered image.
     *  @exception IllegalActionException If thrown while reading the
     *  <i>verticalPixels</i> parameter.
     */
    protected int _getVerticalPixels() throws IllegalActionException {
        return ((IntToken) verticalResolution.getToken()).intValue();
    }

    /** Makes the background for the viewScreen.
     *
     *  @return javax.media.j3d.Background
     *  @exception IllegalActionException If unable to read the color
     * parameter.
     */
    protected Background _makeBackground() throws IllegalActionException {
        Color3f color = new Color3f(backgroundColor.asColor());
        return new Background(color);
    }

    /** Connect each of the channels of the <i>sceneGraphIn</i> port
     *  to the scene.
     *  @exception IllegalActionException If thrown while reading from a
     *  channel.
     */
    protected void _makeNodeConnection() throws IllegalActionException {
        if (_debugging) {
            _debug("Called _makeNodeConnection()");
        }

        int width = sceneGraphIn.getWidth();

        for (int i = 0; i < width; i++) {
            SceneGraphToken objectToken = (SceneGraphToken) sceneGraphIn.get(i);
            Node node = objectToken.getSceneGraphNode();
            _addChild(node);
        }
    }

    /** Setup the scene graph connections of this actor.
     */
    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        if (_debugging) {
            _debug("Called _makeSceneGraphConnection()");
        }

        int width = sceneGraphIn.getWidth();

        for (int i = 0; i < width; i++) {
            SceneGraphToken objectToken = (SceneGraphToken) sceneGraphIn.get(i);

            // node = objectToken.getSceneGraphNode();
            Node node = objectToken.getSceneGraphNode();

            //((BranchGroup) node).detach();
            /* if (_debugging) {
             _debug("Node parent = " + node.getParent());

             }*/
            //System.out.println("Node parent = " + node.getParent());
            _addChild(node);
        }

        _branchRoot.compile();
        _simpleUniverse.addBranchGraph(_branchRoot);
        _isSceneGraphInitialized = true;
    }

    /** Start the internal Java3D renderer.
     */
    protected void _startRenderer() {
        if (_iterationSynchronized) {
            _canvas.startRenderer();
        }
    }

    /** Stop the internal Java3D renderer.
     */
    protected void _stopRenderer() {
        if (_iterationSynchronized) {
            _canvas.stopRenderer();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
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

    private boolean _shouldShowAxes() throws IllegalActionException {
        return ((BooleanToken) showAxes.getToken()).booleanValue();
    }

    private static class MouseRotateView extends MouseRotate {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public MouseRotateView(ViewScreen3D viewContainer) {
            super();
            _viewContainer = viewContainer;
        }

        @Override
        public void processStimulus(java.util.Enumeration criteria) {
            if (stopped != true) {
                _viewContainer._startRenderer();
            }

            /* FIXME: experimental code for changing xforms
             double[] db = new double[16];

             db[0] = 1.0; db[1] = db[2] = db[3] = 0.0;
             db[4] = 0.0; db[5] = 1.0; db[6] = db[7] = 0.0;
             db[8] = db[9] = 0.0; db[10] = 1.0; db[11] = 0.0;
             db[12] = db[13] = db[14] = 0.0; db[15] = 1.0;
             currXform.set(db);
             */
            super.processStimulus(criteria);

            if (stopped != true) {
                _viewContainer._stopRenderer();
            }
        }

        public void stopped() {
            stopped = true;
        }

        /* FIXME experimental code for changing xforms
         public void transformChanged(Transform3D transform) {
         double[] db = new double[16];

         transform.get(db);
         for (int i = 0; i < 16; i++) {
         db[8] = db[9] = 0.0; db[10] = 1.0; db[11] = 0.0;
         db[12] = db[13] = db[14] = 0.0; db[15] = 1.0;
         Transform3D td = new Transform3D();
         td.set(db);
         //currXform.set(db);
         }*/
        boolean stopped = false;

        ViewScreen3D _viewContainer;
    }

    /** The BoundingSphere. */
    protected BoundingSphere _bounds;

    /** The main connection branch that connects to the universe. */
    protected BranchGroup _branchRoot;

    /** The connection branch to which incoming nodes connect. */
    protected BranchGroup _root;

    /** The Java3D canvas component. */
    protected Canvas3D _canvas;

    /** The container set in the place() method, or the content pane of the
     * created frame if place was not called.
     */
    protected Container _container;

    /** The frame containing our canvas, if we created it. */
    protected JFrame _frame;

    /** True for manual rendering, false for default rendering.
     */
    protected boolean _iterationSynchronized = false;

    /** The last transform. */
    protected Transform3D _lastTransform = new Transform3D();

    /** The mouse rotate view. */
    protected MouseRotateView _mouseRotate;

    /** The Java3D universe, displayed inside the canvas. */
    protected SimpleUniverse _simpleUniverse;

    /** The user transformation. */
    protected TransformGroup _userTransformation;

    //  protected int _counter;
    //protected boolean _hasToken;
}
