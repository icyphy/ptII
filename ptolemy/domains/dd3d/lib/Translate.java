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


public class Translate extends Transform {

    public Translate(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        xTranslate = new TypedIOPort(this, "xtranslate",true,false);
	    xTranslate.setTypeEquals(BaseType.DOUBLE);
	    yTranslate = new TypedIOPort(this, "ytranslate",true,false);
	    yTranslate.setTypeEquals(BaseType.DOUBLE);
	    zTranslate = new TypedIOPort(this, "ztranslate",true,false);
	    zTranslate.setTypeEquals(BaseType.DOUBLE);
	    
	    
	    initialXTranslation = new Parameter(this, "xTranslation", new DoubleToken(0.0));
  	    initialYTranslation = new Parameter(this, "yTranslation", new DoubleToken(0.0));
  	    initialZTranslation = new Parameter(this, "zTranslation", new DoubleToken(0.0));
    }
    
    public TypedIOPort xTranslate;
    public TypedIOPort yTranslate;
    public TypedIOPort zTranslate;
    
    public Parameter initialXTranslation;
    public Parameter initialYTranslation;
    public Parameter initialZTranslation;
    
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Translate newobj = (Translate)super.clone(workspace);
        newobj.input  = (TypedIOPort) newobj.getPort("input");
        newobj.output = (TypedIOPort) newobj.getPort("output");
        return newobj;
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        _initialXTranslation = ((DoubleToken) initialXTranslation.getToken()).doubleValue();
        _initialYTranslation = ((DoubleToken) initialYTranslation.getToken()).doubleValue();
        _initialZTranslation = ((DoubleToken) initialZTranslation.getToken()).doubleValue();

        Transform3D transform = new Transform3D();
	    transform.setTranslation(new Vector3d(_initialXTranslation,_initialYTranslation,_initialZTranslation));
        obj.setTransform(transform);
    }
    
    public void fire() throws IllegalActionException {
        boolean applyTransform = false;
        double xOffset = _initialXTranslation;
        double yOffset = _initialYTranslation;
        double zOffset = _initialZTranslation;
        
        if (xTranslate.getWidth() != 0) {
            if (xTranslate.hasToken(0)) {
                double in = ((DoubleToken) xTranslate.get(0)).doubleValue();
                applyTransform = true;
                xOffset = in + _initialXTranslation;
            }
        }
        
        if (yTranslate.getWidth() != 0) {
            if (yTranslate.hasToken(0)) {
                double in = ((DoubleToken) yTranslate.get(0)).doubleValue();
                applyTransform = true;
                yOffset = in + _initialYTranslation;
            }
        }
        
        if (zTranslate.getWidth() != 0) {
            if (zTranslate.hasToken(0)) {
                double in = ((DoubleToken) zTranslate.get(0)).doubleValue();
                applyTransform = true;
                zOffset = in + _initialZTranslation;
            }
        }
        
        if (applyTransform) {
            Transform3D transform = new Transform3D();
    	    transform.setTranslation(new Vector3d(xOffset,yOffset,zOffset));
    	    obj.setTransform(transform);
        }
        
    }
    
    private double _initialXTranslation;
    private double _initialYTranslation;
    private double _initialZTranslation;
    
    
}
