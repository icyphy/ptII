package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.Sink;
import ptolemy.domains.dd3d.kernel.*;

import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JScrollBar;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import java.util.Enumeration;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;
import com.sun.j3d.utils.behaviors.mouse.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class ViewScreen extends DD3DActor implements Placeable, SequenceActor {

    public ViewScreen(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
       super(container, name);
       
       input = new TypedIOPort(this, "input");
       input.setInput(true);
       input.setTypeEquals(BaseType.GENERAL);
       input.setMultiport(true);
    }
    
    public TypedIOPort input;
    
    
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ViewScreen newobj = (ViewScreen) super.clone(workspace);
        return newobj;
    }
    
    DD3DDebug debug = new DD3DDebug(false);
    
 
    public void place(Container container) {
        GraphicsConfiguration config =
           SimpleUniverse.getPreferredConfiguration();
        
        if (c==null) c= new Canvas3D(config);
        container.add("Center",c);
        c.setSize(new Dimension(400,400));
        if (simpleU == null) simpleU = new SimpleUniverse(c);
        simpleU.getViewingPlatform().setNominalViewingTransform();
        
    }
    
    
    
    public void initialize() throws IllegalActionException {
        
        if (simpleU == null) simpleU = new SimpleUniverse(c);
        Enumeration e = simpleU.getLocale().getAllBranchGraphs();
        
        while (e.hasMoreElements()) {
            BranchGroup bg = (BranchGroup) e.nextElement();
            if (bg.getCapability(BranchGroup.ALLOW_DETACH)) {
                simpleU.getLocale().removeBranchGraph(bg);
            } 
        }
        
        objRoot = new BranchGroup();
        objRoot.setCapability(BranchGroup.ALLOW_DETACH);
        
        objRotate = new TransformGroup();
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objRotate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        objRoot.addChild(objRotate);
        
        BoundingSphere bounds =
	    new BoundingSphere(new Point3d(0.0,0.0,0.0), 100.0);
        
        MouseRotate myMouseRotate = new MouseRotate();
        myMouseRotate.setTransformGroup(objRotate);
        myMouseRotate.setSchedulingBounds(bounds);
        objRoot.addChild(myMouseRotate);
        
        //MouseZoom behavior2 = new MouseZoom();
	    //behavior2.setTransformGroup(objRotate);
	    //behavior2.setSchedulingBounds(bounds);
	    //objRoot.addChild(behavior2);
        
          // Create the translate behavior node
    	//MouseTranslate behavior3 = new MouseTranslate();
	    //behavior3.setTransformGroup(objRotate);
    	//objRotate.addChild(behavior3);
	    //behavior3.setSchedulingBounds(bounds);
        
        BranchGroup lightRoot = new BranchGroup();
        AmbientLight lightA = new AmbientLight();
        lightA.setInfluencingBounds(new BoundingSphere());
        lightRoot.addChild(lightA);

        DirectionalLight lightD1 = new DirectionalLight();
        lightD1.setInfluencingBounds(new BoundingSphere());
        Vector3f direction = new Vector3f(-1.0f, -1.0f, -1.0f);
        direction.normalize();
        lightD1.setDirection(direction);
        lightD1.setColor(new Color3f(1.0f, 1.0f, 1.0f));
        lightRoot.addChild(lightD1);
        
        
        simpleU.getViewer().getView().setLocalEyeLightingEnable(true);
        simpleU.addBranchGraph(lightRoot);
    }
    
    public void makeLive() {
        objRoot.compile();
        simpleU.addBranchGraph(objRoot);
    }
    
    public void addChild(Node node) {
        objRotate.addChild(node);
        
    }
    
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        
    }
    
    public Node getNodeObject() {
        return null;
    }
    
    private Canvas3D c;
    private SimpleUniverse simpleU;
    private BranchGroup objRoot;
    private TransformGroup objRotate;
    
}
