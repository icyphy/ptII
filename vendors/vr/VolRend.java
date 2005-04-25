/*
 *	%Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */
package vendors.vr;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.color.ColorSpace;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.io.*;
import java.util.Enumeration;
import com.sun.j3d.utils.behaviors.mouse.*;
import java.net.*;

/**
 * The base class for VolRend applets and applications.  Sets up the basic
 * scene graph structure and processes changes to the attributes
 */
public class VolRend implements VolRendListener, MouseBehaviorCallback {

    //
    static final int 	POST_AWT_CHANGE = 1;

    // parameters settable by setting corresponding property
    boolean 		timing = false;
    boolean 		debug = false;


    Volume 		volume;
    Renderer 		renderer;
    Annotations         annotations;
    View		view; // primary view for renderers

    Renderer[]		renderers;

    UpdateBehavior	updateBehavior;

    Switch 		coordSwitch;

    TransformGroup 	objectGroup;
    TransformGroup 	centerGroup;
    Transform3D 	centerXform = new Transform3D();
    Vector3d 		centerOffset = new Vector3d(-0.5, -0.5, -0.5);

    Group		staticAttachGroup;
    Group		dynamicAttachGroup;

    Switch              staticBackAnnotationSwitch;
    Switch              dynamicBackAnnotationSwitch;
    Switch              staticFrontAnnotationSwitch;
    Switch              dynamicFrontAnnotationSwitch;

    Canvas3D 		canvas;
    Context 		context;
    StringAttr 		dataFileAttr;
    ToggleAttr 		doubleBufferAttr;
    ToggleAttr 		coordSysAttr;
    ToggleAttr 		annotationsAttr;
    ToggleAttr          plusXBoxAttr;
    ToggleAttr          plusYBoxAttr;
    ToggleAttr          plusZBoxAttr;
    ToggleAttr          minusXBoxAttr;
    ToggleAttr          minusYBoxAttr;
    ToggleAttr          minusZBoxAttr;
    StringAttr          plusXImageAttr;
    StringAttr          plusYImageAttr;
    StringAttr          plusZImageAttr;
    StringAttr          minusXImageAttr;
    StringAttr          minusYImageAttr;
    StringAttr          minusZImageAttr;
    ToggleAttr		perspectiveAttr;
    ColormapChoiceAttr	colorModeAttr;
    ToggleAttr		texColorMapAttr;
    IntAttr		segyMinAlphaAttr;
    IntAttr		segyMaxAlphaAttr;
    ChoiceAttr		rendererAttr;
    VectorAttr		translationAttr;
    QuatAttr		rotationAttr;
    DoubleAttr		scaleAttr;
    CoordAttr		volRefPtAttr;
    ChoiceAttr		displayAxisAttr;
    ToggleAttr		axisDepthWriteAttr;

    boolean 		restorePending;
    int			volEditId = -1;

    ScreenSizeCalculator calculator = new ScreenSizeCalculator();

    public VolRend(boolean timing, boolean debug) {

	if (timing) {
	    canvas = new TimingCanvas3D(
		SimpleUniverse.getPreferredConfiguration(), this);
	} else {
	    canvas = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
	}
    }

    Canvas3D getCanvas() {
	return canvas;
    }

    public View setupScene() {

	// Setup the graphics
	// Create a simple scene and attach it to the virtual universe
	BranchGroup scene = createSceneGraph();
	SimpleUniverse u = new SimpleUniverse(canvas);

        // This will move the ViewPlatform back a bit so the
        // objects in the scene can be viewed.
	u.getViewingPlatform().setNominalViewingTransform();

	// get the primary view
	view = u.getViewer().getView();

    System.out.println("Got View");

	// switch to a parallel projection, which is faster for texture mapping
	view.setProjectionPolicy(View.PARALLEL_PROJECTION);

	u.addBranchGraph(scene);

	canvas.setDoubleBufferEnable(true);

	// setup the renderers
	renderers = new Renderer[4];
	renderers[0] = new Axis2DRenderer(view, context, volume);
	renderers[1] = new Axis3DRenderer(view, context, volume);
	renderers[2] = new SlicePlane3DRenderer(view, context, volume);
	renderers[3] = new SlicePlane2DRenderer(view, context, volume);

	renderer = renderers[rendererAttr.getValue()];

	// Add the volume to the scene
	clearAttach();
	renderer.attach(dynamicAttachGroup, staticAttachGroup);

        // Set up the annotations
        annotations = new Annotations(view, context, volume);
        annotations.attach(dynamicFrontAnnotationSwitch,
					staticFrontAnnotationSwitch);
        annotations.attachBack(dynamicBackAnnotationSwitch,
					staticBackAnnotationSwitch);

        return view;

    }

    void update() {
	updateBehavior.postId(POST_AWT_CHANGE);
    }

    public Context initContext(URL codebase) {
	// Attribute stuff

	context = new Context(codebase);
	dataFileAttr = new StringAttr("Data File", "cubes64.vol");
	context.addAttr(dataFileAttr);

	segyMinAlphaAttr = new IntAttr("Segy Min Alpha", 85);
	context.addAttr(segyMinAlphaAttr);
	segyMaxAlphaAttr = new IntAttr("Segy Max Alpha", 170);
	context.addAttr(segyMaxAlphaAttr);
	String[] colorModes = new String[3];
	colorModes[0] = "Intensity";
	colorModes[1] = "Intensity Cmap";
	colorModes[2] = "Segy Cmap";
	Colormap[] colormaps = new Colormap[3];
	colormaps[0] = null;
	colormaps[1] = new LinearColormap();
	colormaps[2] = new SegyColormap(segyMinAlphaAttr, segyMaxAlphaAttr);
	colorModeAttr = new ColormapChoiceAttr("Color Mode", colorModes,
		colormaps, 0);
	context.addAttr(colorModeAttr);
	texColorMapAttr = new ToggleAttr("Tex Color Map", true);
	context.addAttr(texColorMapAttr);

	String[] rendererNames = new String[4];
	rendererNames[0] = "Axis Volume 2D Textures";
	rendererNames[1] = "Axis Volume 3D Texture";
	rendererNames[2] = "Slice Plane 3D Texture";
	rendererNames[3] = "Slice Plane 2D Textures";
	rendererAttr = new ChoiceAttr("Renderer", rendererNames, 0);
	context.addAttr(rendererAttr);

	doubleBufferAttr = new ToggleAttr("Double Buffer", true);
	context.addAttr(doubleBufferAttr);

	coordSysAttr = new ToggleAttr("Coord Sys", true);
	context.addAttr(coordSysAttr);

	annotationsAttr = new ToggleAttr("Annotations", true);
	context.addAttr(annotationsAttr);

        plusXBoxAttr = new ToggleAttr("Plus X Box", true);
        context.addAttr(plusXBoxAttr);

        plusYBoxAttr = new ToggleAttr("Plus Y Box", true);
        context.addAttr(plusYBoxAttr);

        plusZBoxAttr = new ToggleAttr("Plus Z Box", true);
        context.addAttr(plusZBoxAttr);

        minusXBoxAttr = new ToggleAttr("Minus X Box", true);
        context.addAttr(minusXBoxAttr);

        minusYBoxAttr = new ToggleAttr("Minus Y Box", true);
        context.addAttr(minusYBoxAttr);

        minusZBoxAttr = new ToggleAttr("Minus Z Box", true);
        context.addAttr(minusZBoxAttr);

        plusXImageAttr = new StringAttr("Plus X Image", "");
        context.addAttr(plusXImageAttr);

        plusYImageAttr = new StringAttr("Plus Y Image", "");
        context.addAttr(plusYImageAttr);

        plusZImageAttr = new StringAttr("Plus Z Image", "");
        context.addAttr(plusZImageAttr);

        minusXImageAttr = new StringAttr("Minus X Image", "");
        context.addAttr(minusXImageAttr);

        minusYImageAttr = new StringAttr("Minus Y Image", "");
        context.addAttr(minusYImageAttr);

        minusZImageAttr = new StringAttr("Minus Z Image", "");
        context.addAttr(minusZImageAttr);

	perspectiveAttr = new ToggleAttr("Perspective", false);
	context.addAttr(perspectiveAttr);

	translationAttr = new VectorAttr("Translation", new Vector3d());
	context.addAttr(translationAttr);

	rotationAttr = new QuatAttr("Rotation", new Quat4d());
	context.addAttr(rotationAttr);

	scaleAttr = new DoubleAttr("Scale", 1.0);
	context.addAttr(scaleAttr);

	volRefPtAttr = new CoordAttr("Vol Ref Pt", new Point3d());
	context.addAttr(volRefPtAttr);

	// initialize the volume
	volume = new Volume(context);


    System.out.println("Created VolRend object and set up hashtable");

	// initialize the scene graph
	//setupScene();

	/*if (debug) {
	    String[] displayAxis = new String[7];
	    displayAxis[0] = "Auto";
	    displayAxis[1] = "-X";
	    displayAxis[2] = "+X";
	    displayAxis[3] = "-Y";
	    displayAxis[4] = "+Y";
	    displayAxis[5] = "-Z";
	    displayAxis[6] = "+Z";
	    displayAxisAttr = new ChoiceAttr("Display Axis", displayAxis, 0);
	    context.addAttr(displayAxisAttr);
	    axisDepthWriteAttr = new ToggleAttr("Depth Write", true);
	    context.addAttr(axisDepthWriteAttr);
	}*/

    return context;
    }

    public Volume getVolume()
    {
     return volume;
    }


    private void doUpdate() {
	if (restorePending) {
	    return; // we will get called again after the restore is complete
	}
	canvas.setDoubleBufferEnable(doubleBufferAttr.getValue());
	if (coordSysAttr.getValue()) {
	    coordSwitch.setWhichChild(Switch.CHILD_ALL);
	} else {
	    coordSwitch.setWhichChild(Switch.CHILD_NONE);
	}
	if (annotationsAttr.getValue()) {
	    staticBackAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
	    dynamicBackAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
	    staticFrontAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
	    dynamicFrontAnnotationSwitch.setWhichChild(Switch.CHILD_ALL);
	} else {
	    staticBackAnnotationSwitch.setWhichChild(Switch.CHILD_NONE);
	    dynamicBackAnnotationSwitch.setWhichChild(Switch.CHILD_NONE);
	    staticFrontAnnotationSwitch.setWhichChild(Switch.CHILD_NONE);
	    dynamicFrontAnnotationSwitch.setWhichChild(Switch.CHILD_NONE);
	}
	if (perspectiveAttr.getValue()) {
	    view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
	} else {
	    view.setProjectionPolicy(View.PARALLEL_PROJECTION);
	}
	if (renderer != renderers[rendererAttr.getValue()]) {
	    // TODO: renderer.clear();
	    // TODO: handle gui
	    clearAttach();
	    renderer = renderers[rendererAttr.getValue()];
	    renderer.attach(dynamicAttachGroup, staticAttachGroup);
	}
	renderer.update();
        annotations.update();
	int newVolEditId;
	if ((newVolEditId = volume.update()) != volEditId) {
	    updateCenter(volume.minCoord, volume.maxCoord);
	    newVolEditId = volEditId;
	}
    }


    private class UpdateBehavior extends Behavior {
	WakeupCriterion criterion[] = {
		new WakeupOnBehaviorPost(null, POST_AWT_CHANGE)
		};
	WakeupCondition conditions = new WakeupOr( criterion );

	public void initialize() {
	    wakeupOn(conditions);
	}

	public void processStimulus( Enumeration criteria) {
	    // Do the update
	    doUpdate();

	    wakeupOn(conditions);
	}
    }

    void restoreContext(String filename) {
	restorePending = true;
	context.restore(filename);
	restorePending = false;
	restoreXform();
	update();
    }

    void updateCenter(Point3d minCoord, Point3d maxCoord) {
	centerOffset.x = -(maxCoord.x - minCoord.x)/2.0;
	centerOffset.y = -(maxCoord.y - minCoord.y)/2.0;
	centerOffset.z = -(maxCoord.z - minCoord.z)/2.0;
	centerXform.setTranslation(centerOffset);
	centerGroup.setTransform(centerXform);
    }

    BranchGroup createSceneGraph() {
	Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
	Vector3f lDir1  = new Vector3f(0.0f, 0.0f, 1.0f);
	Color3f alColor = new Color3f(1.0f, 1.0f, 1.0f);

	// Create the root of the branch graph
	BranchGroup objRoot = new BranchGroup();

	// Create a transform group to scale the whole scene
	TransformGroup scaleGroup = new TransformGroup();
	Transform3D scaleXform = new Transform3D();
	double scale = 1.2;
	scaleXform.setScale(scale);
	scaleGroup.setTransform(scaleXform);
	objRoot.addChild(scaleGroup);

	// Create the static ordered group
	OrderedGroup scaleOGroup = new OrderedGroup();
	scaleGroup.addChild(scaleOGroup);

        // Create the static annotation group
	staticBackAnnotationSwitch = new Switch();
	staticBackAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	staticBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
	staticBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
	staticBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	scaleOGroup.addChild(staticBackAnnotationSwitch);

	// Create the static attachment group
	staticAttachGroup = new Group();
	staticAttachGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	staticAttachGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	staticAttachGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	scaleOGroup.addChild(staticAttachGroup);


	// Create a TG at the origin
	objectGroup = new TransformGroup();

	// Enable the TRANSFORM_WRITE capability so that our behavior code
	// can modify it at runtime.  Add it to the root of the subgraph.
	//
	objectGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	objectGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
	scaleOGroup.addChild(objectGroup);

        // Create the static annotation group
	staticFrontAnnotationSwitch = new Switch();
	staticFrontAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	staticFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
	staticFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
	staticFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	scaleOGroup.addChild(staticFrontAnnotationSwitch);
	// added after objectGroup so it shows up in front

	// Create the transform group node and initialize it center the
	// object around the origin
	centerGroup = new TransformGroup();
	updateCenter(new Point3d(0.0, 0.0, 0.0), new Point3d(1.0, 1.0, 1.0));
	centerGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
	objectGroup.addChild(centerGroup);

	// Set up the annotation/volume/annotation sandwitch
	OrderedGroup centerOGroup = new OrderedGroup();
	centerGroup.addChild(centerOGroup);

	// create the back dynamic annotation point
	dynamicBackAnnotationSwitch = new Switch(Switch.CHILD_ALL);
	dynamicBackAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	dynamicBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
	dynamicBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
	dynamicBackAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	centerOGroup.addChild(dynamicBackAnnotationSwitch);

	// create the dynamic attachment point
	dynamicAttachGroup = new Group();
	dynamicAttachGroup.setCapability(Group.ALLOW_CHILDREN_READ);
	dynamicAttachGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
	dynamicAttachGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	centerOGroup.addChild(dynamicAttachGroup);

	// create the front dynamic annotation point
	dynamicFrontAnnotationSwitch = new Switch(Switch.CHILD_ALL);
	dynamicFrontAnnotationSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	dynamicFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_READ);
	dynamicFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_WRITE);
	dynamicFrontAnnotationSwitch.setCapability(Group.ALLOW_CHILDREN_EXTEND);
	centerOGroup.addChild(dynamicFrontAnnotationSwitch);

        // Create the annotations
        Annotations annotations = new Annotations(view, context, volume);
        annotations.attachBack(dynamicBackAnnotationSwitch,
				staticBackAnnotationSwitch);
        annotations.attach(dynamicFrontAnnotationSwitch,
				staticFrontAnnotationSwitch);

	coordSwitch = new Switch(Switch.CHILD_ALL);
	coordSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
	coordSwitch.addChild(
		new CoordSys(0.2, new Vector3d(-0.1, -0.1, -0.1)));
        centerGroup.addChild(coordSwitch);

	BoundingSphere bounds =
	    new BoundingSphere(new Point3d(0.0,0.0,0.0), 100000.0);

	MouseRotate mr = new MouseRotate();
	mr.setupCallback(this);
	mr.setTransformGroup(objectGroup);
	mr.setSchedulingBounds(bounds);
	mr.setFactor(0.007);
	objRoot.addChild(mr);
	/*
	MouseTranslate mt = new MouseTranslate();
	mt.setTransformGroup(object);
	mt.setSchedulingBounds(bounds);
	objRoot.addChild(mt);
	MouseZoom mz = new MouseZoom();
	mz.setTransformGroup(object);
	mz.setSchedulingBounds(bounds);
	objRoot.addChild(mz);
	*/

	FirstFramesBehavior ff = new FirstFramesBehavior(this);
	ff.setSchedulingBounds(bounds);
	objRoot.addChild(ff);

    	updateBehavior = new UpdateBehavior();
	updateBehavior.setSchedulingBounds(bounds);
	objRoot.addChild(updateBehavior);

	ExitKeyBehavior eb = new ExitKeyBehavior();
	eb.setSchedulingBounds(bounds);
	objRoot.addChild(eb);

	return objRoot;
    }

    private void clearAttach() {
	while(staticAttachGroup.numChildren() > 0) {
	    staticAttachGroup.removeChild(0);
	}
	while(dynamicAttachGroup.numChildren() > 0) {
	    dynamicAttachGroup.removeChild(0);
	}
    }

    public void transformChanged(int type, Transform3D xform) {
	renderer.transformChanged(type, xform);
	renderer.eyePtChanged();
        annotations.eyePtChanged();
	scaleAttr.set(xform);
	translationAttr.set(xform);
	rotationAttr.set(xform);
    }

    public void restoreXform() {
	Transform3D xform = new Transform3D(rotationAttr.getValue(),
		translationAttr.getValue(), scaleAttr.getValue());
	objectGroup.setTransform(xform);
	renderer.eyePtChanged();
    }

    public void eyePtChanged() {
	renderer.eyePtChanged();
    }

    public double calcRenderSize() {
	if (!centerGroup.isLive()) {
	    return 0.0;
	} else {
	    return renderer.calcRenderSize(calculator, canvas);
	}
    }

    // Have main starup VolRendEdit for compatiblity
    public static void main(String[] args) {
	VolRendEdit vol = new VolRendEdit(args);
    }
}
