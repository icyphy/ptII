package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.dd3d.kernel.*;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class Transform extends DD3DActor {

    public Transform(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        input = new TypedIOPort(this, "input");
        input.setInput(true);
	    input.setMultiport(true);
	    
	    output = new TypedIOPort(this, "output");
	    output.setOutput(true);
	    output.setTypeEquals(BaseType.GENERAL);
    }
    
    public TypedIOPort input;
    public TypedIOPort output;
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Transform newobj = (Transform)super.clone(workspace);
        newobj.input  = (TypedIOPort) newobj.getPort("input");
        newobj.output = (TypedIOPort) newobj.getPort("output");
        return newobj;
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
  	    obj = new TransformGroup();
	    obj.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    }
    
   
    public void addChild(Node node) {
        obj.addChild(node);
    }
    
    public Node getNodeObject() {
        return (Node) obj;
    }
   
    protected TransformGroup obj;
}
