package ptolemy.domains.dd3d.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.domains.dd3d.kernel.*;
import ptolemy.domains.dt.kernel.DTDebug;


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.awt.GraphicsConfiguration;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.*;
import javax.media.j3d.*;
import javax.vecmath.*;


public class Scale3D extends Transform {

    public Scale3D(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        scaleFactor = new Parameter(this, "scaleFactor",new DoubleToken(1.0));
	    scaleFactor.setTypeEquals(BaseType.DOUBLE);
	    xScale = new Parameter(this, "xScale", new DoubleToken(1.0));
  	    yScale = new Parameter(this, "yScale", new DoubleToken(1.0));
  	    zScale = new Parameter(this, "zScale", new DoubleToken(1.0));
    }
    
    public TypedIOPort input;
    public TypedIOPort output;
    public Parameter scaleFactor;
    
   
    public Parameter xScale;
    public Parameter yScale;
    public Parameter zScale;
    
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Scale3D newobj = (Scale3D) super.clone(workspace);
        newobj.input  = (TypedIOPort) newobj.getPort("input");
        newobj.output = (TypedIOPort) newobj.getPort("output");
        return newobj;
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        Transform3D scaleTransform = new Transform3D();
        //scaleTransform.setScale(_getScale());
        scaleTransform.setScale(new Vector3d(_getScaleX(),_getScaleY(),_getScaleZ()));        
        obj.setTransform(scaleTransform);
    }
  
    private double _getScaleX() throws IllegalActionException {
        double factor = ((DoubleToken) scaleFactor.getToken()).doubleValue();
        double xFactor = ((DoubleToken) xScale.getToken()).doubleValue();
        return factor * xFactor;
    }
    
    private double _getScaleY() throws IllegalActionException {
        double factor = ((DoubleToken) scaleFactor.getToken()).doubleValue();
        double yFactor = ((DoubleToken) yScale.getToken()).doubleValue();
        return factor * yFactor;
    }
    
    private double _getScaleZ() throws IllegalActionException {
        double factor = ((DoubleToken) scaleFactor.getToken()).doubleValue();
        double zFactor = ((DoubleToken) zScale.getToken()).doubleValue();
        return factor * zFactor;
    }
    
    
}
