/* A GR scene viewer

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
import ptolemy.actor.gui.Placeable;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor3D;
import ptolemy.domains.gr.kernel.SceneGraphToken;
import ptolemy.domains.gr.kernel.ViewScreenInterface;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

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
import javax.media.j3d.Node;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.JFrame;
import javax.vecmath.AxisAngle4d;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.universe.SimpleUniverse;

//////////////////////////////////////////////////////////////////////////
//// ViewScreen

/** A sink actor that renders the GR geometry into a display screen

@author C. Fong, Adam Cataldo, Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
*/
public class ViewScreen extends GRActor3D
    implements Placeable, ViewScreenInterface {

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
    public ViewScreen(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setTypeEquals(SceneGraphToken.TYPE);
        sceneGraphIn.setMultiport(true);

        horizontalResolution = new Parameter(this,
                "horizontalResolution", new IntToken(400));
        horizontalResolution.setTypeEquals(BaseType.INT);

        verticalResolution = new Parameter(this,
                "verticalResolution", new IntToken(400));
        verticalResolution.setTypeEquals(BaseType.INT);

        rotatable = new Parameter(this,
                "rotatable", new BooleanToken(true));
        rotatable.setTypeEquals(BaseType.BOOLEAN);

        scalable = new Parameter(this,
                "scalable", new BooleanToken(false));
        scalable.setTypeEquals(BaseType.BOOLEAN);

        translatable = new Parameter(this,
                "translatable", new BooleanToken(false));
        translatable.setTypeEquals(BaseType.BOOLEAN);

        showAxes = new Parameter(this,"showAxes", new BooleanToken(false));
        showAxes.setTypeEquals(BaseType.BOOLEAN);

        iterationSynchronized = new Parameter(this,
                "iterationSynchronized", new BooleanToken(false));
        iterationSynchronized.setTypeEquals(BaseType.BOOLEAN);

        backgroundColor = new Parameter(this, "backgroundColor",
                new DoubleMatrixToken(new double[][] {{ 0.0, 0.0, 0.0}} ));
        backgroundColor.setTypeEquals(BaseType.DOUBLE_MATRIX);

        _lastTransform = new Transform3D();
    }


    ///////////////////////////////////////////////////////////////////
    ////                     Ports and Parameters                  ////

    /** The input scene graph.
     */
    public TypedIOPort sceneGraphIn;

    /** The background color, given as a 3-element array representing
     *  rgb color
     */
    public Parameter backgroundColor;

    /** The horizontal resolution of the display screen
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is the IntToken 400
     */
    public Parameter horizontalResolution;

    /*  Boolean variable that determines whether screen update is done
     *   once per iteration
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter iterationSynchronized;

    /** Boolean variable that determines whether the user is allowed to
     *   rotate the model
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken true
     */
    public Parameter rotatable;

    /** Boolean variable that determines whether the user is allowed to
     *   scale the model
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter scalable;

    /** Boolean variable that determines whether or not axes are shown.
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter showAxes;

    /** Boolean variable that determines whether the user is allowed to
     *   translate the model
     *  This parameter should contain a BooleanToken
     *  The default value of this parameter is BooleanToken false
     */
    public Parameter translatable;

    /** The vertical resolution of the display screen
     *  This parameter should contain a IntToken.
     *  The default value of this parameter is IntToken 400
     */
    public Parameter verticalResolution;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor.
     */
    public void addChild(Node node) {
        _addChild(node);
    }

    /** Fire this actor.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_iterationSynchronized)  {
            _canvas.swap();
            _canvas.startRenderer();
            _canvas.stopRenderer();
        }
    }

    /** Return the root Java 3D rendering group used by this view screen.
     */
    public BranchGroup getBranchGroup() {
        return _branchRoot;
    }

    /** Return the Java 3D canvas used by this view screen.
     */
    public Canvas3D getCanvas() {
        return _canvas;
    }

    /** Initialize the execution.  Create the ViewScreen frame if
     *  it hasn't been set using the place() method.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {

        super.initialize();

        // Create a frame, if necessary, along with the canvas and
        // simple universe.
        _createViewScreen();

        // Make the frame visible.
        if (_frame != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }

        Enumeration branches
            = _simpleUniverse.getLocale().getAllBranchGraphs();

        while (branches.hasMoreElements()) {
            BranchGroup branchGroup = (BranchGroup) branches.nextElement();
            if (branchGroup.getCapability(BranchGroup.ALLOW_DETACH)) {
                if (!(branchGroup instanceof
                        com.sun.j3d.utils.universe.ViewingPlatform)) {
                    _simpleUniverse.getLocale().removeBranchGraph(branchGroup);
                }
            }
        }

        _branchRoot = new BranchGroup();
        _branchRoot.setCapability(BranchGroup.ALLOW_DETACH);


        _userTransformation = new TransformGroup(_lastTransform);
        _userTransformation.setCapability(
                TransformGroup.ALLOW_TRANSFORM_WRITE);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
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
            Cylinder yAxis = new Cylinder((float)0.01, (float) 6.0);
            _userTransformation.addChild(yAxis);

            Cylinder xAxis = new Cylinder((float)0.01, (float) 6.0);
            Transform3D rotation = new Transform3D();
            Quat4d quat = new Quat4d();
            quat.set(new AxisAngle4d(0.0, 0.0, 1.0, Math.PI/2.0));
            rotation.set(quat);
            TransformGroup xAxisGroup = new TransformGroup(rotation);
            xAxisGroup.addChild(xAxis);
            _userTransformation.addChild(xAxisGroup);

            Cylinder zAxis = new Cylinder((float)0.01, (float) 6.0);
            Transform3D rotation2 = new Transform3D();
            Quat4d quat2 = new Quat4d();
            quat2.set(new AxisAngle4d(1.0, 0.0, 0.0, Math.PI/2.0));
            rotation2.set(quat2);
            TransformGroup zAxisGroup = new TransformGroup(rotation2);
            zAxisGroup.addChild(zAxis);
            _userTransformation.addChild(zAxisGroup);
        }


        // Setup the lights.
        BranchGroup lightRoot = new BranchGroup();

        AmbientLight lightAmbient
            = new AmbientLight(new Color3f(0.8f, 0.8f, 0.8f));
        lightAmbient.setInfluencingBounds(_bounds);
        lightRoot.addChild(lightAmbient);

        DirectionalLight lightDirectional = new DirectionalLight();
        lightDirectional.setInfluencingBounds(_bounds);
        Vector3f direction = new Vector3f(0.0f, -1.0f, -1.0f);
        direction.normalize();
        lightDirectional.setDirection(direction);
        lightDirectional.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        lightRoot.addChild(lightDirectional);

        _simpleUniverse.getViewer().getView()
            .setLocalEyeLightingEnable(true);
        _simpleUniverse.addBranchGraph(lightRoot);

        if (_iterationSynchronized) {
            if (_canvas != null) _canvas.stopRenderer();
        }
    }


    /** Set the container that this actor should display data in.  If
     * place is not called, then the actor will create its own frame
     * for display.
     */
    public void place(Container container) {
        _container = container;

        if (_container == null) return;
        Container c = _container.getParent();
        while (c.getParent() != null) {
            c = c.getParent();
        }
        // If we had created a frame before, then blow it away.
        if (_frame != null) {
            _frame.dispose();
            _frame = null;
        }
        _createViewScreen();
    }

    /** Wrapup an execution
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _userTransformation.getTransform(_lastTransform);
        if (_iterationSynchronized) {
            _canvas.stopRenderer();
            _canvas.swap();
            if (_mouseRotate != null) _mouseRotate.stopped();
            _canvas.startRenderer();
        }
        _isSceneGraphInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add the node argument as a child to the encapsulated Java3D node
     *  in this actor.
     */
    protected void _addChild(Node node) {
        _userTransformation.addChild(node);
    }

    /** Create the view screen component.  If place() was called with
     * a container, then use the container.  Otherwise, create a new
     * frame and use that.
     */
    protected void _createViewScreen() {
        GraphicsConfiguration config =
            SimpleUniverse.getPreferredConfiguration();

        int horizontalDimension = 400;
        int verticalDimension = 400;

        try {
            horizontalDimension = _getHorizontalPixels();
            verticalDimension = _getVerticalPixels();
        } catch (Exception e) {
            // FIXME handle this
        }

        // Create a frame, if placeable was not called.
        if (_container == null) {
            _frame = new JFrame("ViewScreen");
            _frame.show();
            _frame.validate();
            _frame.setSize(horizontalDimension+50,verticalDimension);
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
        _canvas.setSize(new Dimension(horizontalDimension,
                verticalDimension));
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

    /** The ViewScreen does not have an associated Java3D node
     *
     *  @return null
     */
    protected Node _getNodeObject() {
        return null;
    }

    /** Makes the background for the viewScreen
     *
     *  @return javax.media.j3d.Background
     *  @exception IllegalActionException If unable to read the color
     * parameter.
     */
    protected Background _makeBackground() throws IllegalActionException {
        DoubleMatrixToken colorVector =
            (DoubleMatrixToken) backgroundColor.getToken();
        Color3f color = new Color3f();

        color.x = (float) colorVector.getElementAt(0, 0);
        color.y = (float) colorVector.getElementAt(0, 1);
        color.z = (float) colorVector.getElementAt(0, 2);

        return new Background(color);
    }

    /** Setup the scene graph connections of this actor.
     */
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        int width = sceneGraphIn.getWidth();
        for (int i = 0 ; i < width; i++) {
            SceneGraphToken objectToken = (SceneGraphToken)
                sceneGraphIn.get(i);
            Node node = (Node) objectToken.getSceneGraphNode();
            _addChild(node);
        }
        _branchRoot.compile();
        _simpleUniverse.addBranchGraph(_branchRoot);
    }

    /** Get the number of horizontal pixels in the rendered image.
     */
    protected int _getHorizontalPixels() throws IllegalActionException {
        return ((IntToken) horizontalResolution.getToken()).intValue();
    }

    /** Get the number of vertical pixels in the rendered image.
     */
    protected int _getVerticalPixels() throws IllegalActionException {
        return ((IntToken) verticalResolution.getToken()).intValue();
    }

    /** Start the internal Java3D renderer
     */
    protected void _startRenderer() {
        if (_iterationSynchronized) {
            _canvas.startRenderer();
        }
    }


    /** Stop the internal Java3D renderer
     */
    protected void _stopRenderer() {
        if (_iterationSynchronized) {
            _canvas.stopRenderer();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private boolean _isIterationSynchronized() throws IllegalActionException {
        return ((BooleanToken)
                iterationSynchronized.getToken()).booleanValue();
    }

    private boolean _isRotatable() throws IllegalActionException  {
        return ((BooleanToken) rotatable.getToken()).booleanValue();
    }

    private boolean _isScalable() throws IllegalActionException  {
        return ((BooleanToken) scalable.getToken()).booleanValue();
    }

    private boolean _isTranslatable() throws IllegalActionException  {
        return ((BooleanToken) translatable.getToken()).booleanValue();
    }

    private boolean _shouldShowAxes() throws IllegalActionException {
        return ((BooleanToken) showAxes.getToken()).booleanValue();
    }

    private class MouseRotateView extends MouseRotate {
        public MouseRotateView(ViewScreen viewContainer) {
            super();
            _viewContainer = viewContainer;
        }

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


            if (stopped != true ) {
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
        ViewScreen _viewContainer;
    }

    protected BoundingSphere _bounds;
    // The main connection branch that connects to the universe
    protected BranchGroup _branchRoot;
    // The Java3D canvas component.
    protected Canvas3D _canvas;
    // The container set in the place() method, or the content pane of the
    // created frame if place was not called.
    protected Container _container;
    // The frame containing our canvas, if we created it.
    protected JFrame _frame;
    // True for manual rendering, false for default rendering.
    // Steve doesn't think this is entirely necessary.
    protected boolean _iterationSynchronized = false;

    protected Transform3D _lastTransform = new Transform3D();

    protected MouseRotateView _mouseRotate;
    // The Java3D universe, displayed inside the canvas.
    protected SimpleUniverse _simpleUniverse;

    protected TransformGroup _userTransformation = new TransformGroup();
}

