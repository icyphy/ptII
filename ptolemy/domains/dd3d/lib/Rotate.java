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


public class Rotate extends Transform {

    public Rotate(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

	    angle = new TypedIOPort(this, "angle",true,false);
	    angle.setTypeEquals(BaseType.DOUBLE);
	    initialAngle = new Parameter(this, "initialAngle", new DoubleToken(0.0));
	    axisDirectionX = new Parameter(this, "axisDirectionX", new DoubleToken(0.0));
  	    axisDirectionY = new Parameter(this, "axisDirectionY", new DoubleToken(1.0));
  	    axisDirectionZ = new Parameter(this, "axisDirectionZ", new DoubleToken(0.0));
  	    baseX = new Parameter(this, "baseX", new DoubleToken(0.0));
  	    baseY = new Parameter(this, "baseY", new DoubleToken(0.0));
  	    baseZ = new Parameter(this, "baseZ", new DoubleToken(0.0));
    }
    
    public TypedIOPort angle;
   
    public Parameter initialAngle; 
    public Parameter axisDirectionX;
    public Parameter axisDirectionY;
    public Parameter axisDirectionZ;
    
    public Parameter baseX;
    public Parameter baseY;
    public Parameter baseZ;
    
    
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Rotate newobj = (Rotate)super.clone(workspace);
        return newobj;
    }
    
    public void initialize() throws IllegalActionException {
        super.initialize();
        double _xAxis = ((DoubleToken) axisDirectionX.getToken()).doubleValue();
        double _yAxis = ((DoubleToken) axisDirectionY.getToken()).doubleValue();
        double _zAxis = ((DoubleToken) axisDirectionZ.getToken()).doubleValue();
        double originalAngle = ((DoubleToken) initialAngle.getToken()).doubleValue();

        Quat4d quat = new Quat4d();
        quat.set(new AxisAngle4d(_xAxis,_yAxis,_zAxis,originalAngle));
        rotation.set(quat);
        obj.setTransform(rotation);
    }
    
    Transform3D rotation = new Transform3D();
    public void fire() throws IllegalActionException {
        if (angle.getWidth() != 0) {
            if (angle.hasToken(0)) {
                double in = ((DoubleToken)angle.get(0)).doubleValue();
                double originalAngle = ((DoubleToken) initialAngle.getToken()).doubleValue();
                _xAxis = ((DoubleToken) axisDirectionX.getToken()).doubleValue();
                _yAxis = ((DoubleToken) axisDirectionY.getToken()).doubleValue();
                _zAxis = ((DoubleToken) axisDirectionZ.getToken()).doubleValue();
        
                Quat4d quat = new Quat4d();
                quat.set(new AxisAngle4d(_xAxis,_yAxis,_zAxis,in+originalAngle));
                rotation.set(quat);
                obj.setTransform(rotation);
            }
        }
    }
    
    private double _xAxis;
    private double _yAxis;
    private double _zAxis;
    
}
