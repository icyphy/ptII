package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.domains.dt.kernel.DTDebug;
import ptolemy.domains.dd3d.kernel.*;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.*;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class SphereActor extends Shaded3DActor {

    public SphereActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        radius = new Parameter(this, "radius", new DoubleToken(0.5));
    }
    
    public Parameter radius;
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SphereActor newobj = (SphereActor)super.clone(workspace);
        newobj.radius = (Parameter)newobj.getAttribute("radius");
        return newobj;
    }
    
    public void _createModel() throws IllegalActionException {
        super._createModel();
        obj = new Sphere((float)_getRadius(),Sphere.GENERATE_NORMALS,_appearance);
    }
    
    public Node getNodeObject() {
        return (Node) obj;
    }
    
    private double _getRadius() throws IllegalActionException {
        return ((DoubleToken) radius.getToken()).doubleValue();
    }

    private Sphere obj;
}
