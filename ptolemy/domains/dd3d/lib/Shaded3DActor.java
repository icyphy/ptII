package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.gui.Placeable;
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


public class Shaded3DActor extends DD3DActor {

    public Shaded3DActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        output = new TypedIOPort(this, "output");
        output.setOutput(true);
        output.setTypeEquals(BaseType.GENERAL);
        redComponent   = new Parameter(this,"redComponent", new DoubleToken(0.7));
        greenComponent = new Parameter(this,"greenComponent", new DoubleToken(0.7));
        blueComponent  = new Parameter(this,"blueComponent", new DoubleToken(0.7));
        shininess = new Parameter(this,"shininess",new DoubleToken(20.0));
        
        _color = new Color3f(1.0f,1.0f,1.0f);
        //_material = new Material();
        //_material.setCapability(Material.ALLOW_COMPONENT_WRITE);
        //_appearance = new Appearance();
    }
    
    public TypedIOPort output;
    public Parameter redComponent;
    public Parameter greenComponent;
    public Parameter blueComponent;
    public Parameter shininess;
    
   
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Shaded3DActor newobj = (Shaded3DActor)super.clone(workspace);
        
        newobj.output = (TypedIOPort)newobj.getPort("output");
        newobj.redComponent = (Parameter)newobj.getAttribute("redComponent");
        newobj.greenComponent = (Parameter)newobj.getAttribute("greenComponent");
        newobj.blueComponent = (Parameter)newobj.getAttribute("blueComponent");
        return newobj;
    }
    
    
    protected void _createAppearance() {

        _material = new Material();
        _appearance = new Appearance();
        
        _material.setDiffuseColor(_color);
        if (_shine > 1.0) {
            _material.setSpecularColor(whiteColor);
            _material.setShininess(_shine);
        } 
        _appearance.setMaterial(_material);
    }
    
    protected void _createModel() throws IllegalActionException {
        
        super._createModel();
        _color.x = (float) ((DoubleToken) redComponent.getToken()).doubleValue();
        _color.y = (float) ((DoubleToken) greenComponent.getToken()).doubleValue();
        _color.z = (float) ((DoubleToken) blueComponent.getToken()).doubleValue();
        _shine = (float) ((DoubleToken) shininess.getToken()).doubleValue();
        
        _createAppearance();
    }
    
    protected Color3f _color;
    protected Appearance _appearance;
    protected Material _material;
    protected float _shine;

    protected static final Color3f whiteColor = new Color3f(1.0f,1.0f,1.0f);
    protected static final Color3f blueColor = new Color3f(0.0f,0.0f,1.0f);
}
