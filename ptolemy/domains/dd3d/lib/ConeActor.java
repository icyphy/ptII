package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
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


public class ConeActor extends Shaded3DActor {

    public ConeActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        radius = new Parameter(this, "radius", new DoubleToken(0.5));
        height = new Parameter(this, "height", new DoubleToken(0.7));
    }
    
    public Parameter radius;
    public Parameter height;
    
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ConeActor newobj = (ConeActor)super.clone(workspace);
        newobj.radius = (Parameter)newobj.getAttribute("radius");
        newobj.height = (Parameter)newobj.getAttribute("height");
        return newobj;
    }
    
    public Node getNodeObject() {
        return (Node) obj;
    }
    
    protected void _createModel() throws IllegalActionException {
        super._createModel();
        obj = new Cone((float)_getRadius(),(float) _getHeight(),Cone.GENERATE_NORMALS,_appearance);
    }


    private double _getRadius() throws IllegalActionException {
        return ((DoubleToken) radius.getToken()).doubleValue();
    }
    private double _getHeight() throws IllegalActionException  {
        return ((DoubleToken) height.getToken()).doubleValue();
    }    
    
    private Cone obj;
}
