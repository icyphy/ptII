/* A GR scene viewer

 Copyright (c) 1998-2000 The Regents of the University of California.
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

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.gui.Placeable;

import ptolemy.domains.gr.kernel.*;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.util.Enumeration;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class ViewScreen extends GRActor implements Placeable {

    public ViewScreen(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setTypeEquals(BaseType.OBJECT);
        sceneGraphIn.setMultiport(true);

        horizontalResolution = new Parameter(this,"horizontal resolution",new IntToken(400));
        verticalResolution = new Parameter(this,"vertical resolution",new IntToken(400));
        scale = new Parameter(this, "scale", new DoubleToken(1.0));
        rotatable = new Parameter(this, "allow model rotation",new BooleanToken(true));
        scalable = new Parameter(this, "allow model zooming",new BooleanToken(false));
        translatable = new Parameter(this, "allow model translation",new BooleanToken(false));
        showAxes = new Parameter(this,"show axes",new BooleanToken(false));

        _lastTransform = new Transform3D();
        _root = this;
        //_userTransformation = new TransformGroup();
        //_userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        //_userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    }

    public TypedIOPort sceneGraphIn;
    public Parameter horizontalResolution;
    public Parameter verticalResolution;
    public Parameter scale;
    public Parameter rotatable;
    public Parameter scalable;
    public Parameter translatable;
    public Parameter showAxes;

    public void place(Container container) {
        GraphicsConfiguration config =
            SimpleUniverse.getPreferredConfiguration();

        int horizontalDimension = 400;
        int verticalDimension = 400;

        try {
            horizontalDimension = _getHorizontalResolution();
            verticalDimension = _getVerticalResolution();
        } catch (Exception e) {
            // FIXME handle this
        }

        if (canvas ==null) canvas = new Canvas3D(config);
        if (container==null) {
            _frame = new JFrame("ViewScreen");
            _frame.getContentPane().add(canvas,BorderLayout.CENTER);
            canvas.setSize(new Dimension(horizontalDimension,verticalDimension));
            _frame.setSize(horizontalDimension+50,verticalDimension);
            if (simpleU == null) simpleU = new SimpleUniverse(canvas);
            simpleU.getViewingPlatform().setNominalViewingTransform();
            _frame.setVisible(true);
        } else {
            container.add("Center",canvas);
            canvas.setSize(new Dimension(horizontalDimension,verticalDimension));
            if (simpleU == null) simpleU = new SimpleUniverse(canvas);
            simpleU.getViewingPlatform().setNominalViewingTransform();
        }
    }

    public void subinitialize() throws IllegalActionException {
        super.initialize();
        if (canvas == null) {
            place(_container);
        }
        if (_frame != null) {
            _frame.setVisible(true);
        }
    }

    public void initialize() throws IllegalActionException {

        super.initialize();
        subinitialize();
        if (simpleU == null) simpleU = new SimpleUniverse(canvas);
        Enumeration e = simpleU.getLocale().getAllBranchGraphs();

        while (e.hasMoreElements()) {
            BranchGroup bg = (BranchGroup) e.nextElement();
            if (bg.getCapability(BranchGroup.ALLOW_DETACH)) {
                simpleU.getLocale().removeBranchGraph(bg);
            }
        }

        branchRoot = new BranchGroup();
        branchRoot.setCapability(BranchGroup.ALLOW_DETACH);

        Transform3D scaleTransform = new Transform3D();
        scaleTransform.setScale(_getScale());
        TransformGroup scaler = new TransformGroup(scaleTransform);
        branchRoot.addChild(scaler);

        _userTransformation = new TransformGroup(_lastTransform);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _userTransformation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        //branchRoot.addChild(_userTransformation);
        scaler.addChild(_userTransformation);

        bounds = new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);

        if (_isRotatable()) {
            MouseRotate mouseRotate = new MouseRotate();
            mouseRotate.setTransformGroup(_userTransformation);
            mouseRotate.setSchedulingBounds(bounds);
            branchRoot.addChild(mouseRotate);
        }

        if (_isScalable()) {
            MouseZoom mouseZoom = new MouseZoom();
            mouseZoom.setTransformGroup(_userTransformation);
            mouseZoom.setSchedulingBounds(bounds);
            branchRoot.addChild(mouseZoom);
        }

        if (_isTranslatable()) {
    	    MouseTranslate mouseTranslate = new MouseTranslate();
            mouseTranslate.setTransformGroup(_userTransformation);
    	    _userTransformation.addChild(mouseTranslate);
            mouseTranslate.setSchedulingBounds(bounds);
        }

        if (_shouldShowAxes()) {
            Sphere origin = new Sphere((float) 0.05);
            _userTransformation.addChild(origin);
            Cylinder yAxis = new Cylinder((float)0.01,(float) 6.0);
            _userTransformation.addChild(yAxis);

            Cylinder xAxis = new Cylinder((float)0.01,(float) 6.0);
            Transform3D rotation = new Transform3D();
            Quat4d quat = new Quat4d();
            quat.set(new AxisAngle4d(0.0,0.0,1.0,Math.PI/2.0));
            rotation.set(quat);
            TransformGroup xAxisGroup = new TransformGroup(rotation);
            xAxisGroup.addChild(xAxis);
            _userTransformation.addChild(xAxisGroup);

            Cylinder zAxis = new Cylinder((float)0.01,(float) 6.0);
            Transform3D rotation2 = new Transform3D();
            Quat4d quat2 = new Quat4d();
            quat2.set(new AxisAngle4d(1.0,0.0,0.0,Math.PI/2.0));
            rotation2.set(quat2);
            TransformGroup zAxisGroup = new TransformGroup(rotation2);
            zAxisGroup.addChild(zAxis);
            _userTransformation.addChild(zAxisGroup);


        }

        BranchGroup lightRoot = new BranchGroup();
        // AmbientLight lightA = new AmbientLight();
        AmbientLight lightA = new AmbientLight(new Color3f(0.8f,0.8f,0.8f));
        lightA.setInfluencingBounds(bounds);
        lightRoot.addChild(lightA);

        DirectionalLight lightD1 = new DirectionalLight();
        lightD1.setInfluencingBounds(bounds);
        Vector3f direction = new Vector3f(0.0f, -1.0f, -1.0f);
        direction.normalize();
        lightD1.setDirection(direction);
        lightD1.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        lightRoot.addChild(lightD1);


        simpleU.getViewer().getView().setLocalEyeLightingEnable(true);
        simpleU.addBranchGraph(lightRoot);
    }

    
    public void addChild(Node node) {
        _userTransformation.addChild(node);
    }


    public void makeSceneGraphConnection() throws IllegalActionException {
        int width = sceneGraphIn.getWidth();
        for(int i=0;i<width;i++) {
            ObjectToken objectToken = (ObjectToken) sceneGraphIn.get(i);
            Node node = (Node) objectToken.getValue();
            addChild(node);
        }
        branchRoot.compile();
        simpleU.addBranchGraph(branchRoot);
    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _userTransformation.getTransform(_lastTransform);
    }

    public Node getNodeObject() {
        return null;
    }

    private int _getHorizontalResolution() throws IllegalActionException {
        return ((IntToken) horizontalResolution.getToken()).intValue();
    }

    private int _getVerticalResolution() throws IllegalActionException {
        return ((IntToken) verticalResolution.getToken()).intValue();
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

    private double _getScale() throws IllegalActionException {
        return ((DoubleToken) scale.getToken()).doubleValue();
    }


    private Canvas3D canvas;
    private SimpleUniverse simpleU;
    private BranchGroup branchRoot;
    private Transform3D _lastTransform = new Transform3D();
    private TransformGroup _userTransformation = new TransformGroup();
    private Container _container;
    private JFrame _frame;
    private BoundingSphere bounds;
    GRDebug debug = new GRDebug(false);
}
