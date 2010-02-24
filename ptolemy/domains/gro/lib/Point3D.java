package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Point3D extends GROActor {

    public Point3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setOutput(true);

       
        radius = new Parameter(this, "radius");
        radius.setExpression("10.0");

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{1.0, 1.0, 1.0}");

        origin = new Parameter(this, "origin");
        origin.setExpression("{0.0, 0.0, 0.0}");
       
        
      

        GLPipelineObject.setTypeEquals(BaseType.OBJECT);

    }

    public Parameter radius;


    /** The red, green, blue, and alpha components of the line.  This
     *  parameter must contain an array of double values.  The default
     *  value is {0.0, 0.0, 1.0}, corresponding to opaque black.
     */
    public ColorAttribute rgbColor;

    /** The origin coordinate of the point. */
    public Parameter origin;

  
    
public TypedIOPort GLPipelineObject;
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        
        
        ArrayToken originToken = ((ArrayToken) origin.getToken());
        ArrayToken rgbColorValue = ((ArrayToken) rgbColor.getToken());
        DoubleToken radiusValue = (DoubleToken) radius.getToken();
        
        GL gl = ((GRODirector) getDirector()).getGL();
        
        gl.glPointSize((float) radiusValue.doubleValue());
        gl.glBegin(GL.GL_POINTS);
        
        gl.glColor3d(
                ((DoubleToken) rgbColorValue.getElement(0)).doubleValue(), 
                ((DoubleToken) rgbColorValue.getElement(1)).doubleValue(), 
                ((DoubleToken) rgbColorValue.getElement(2)).doubleValue()); 

        // origin of the line
        gl.glVertex3d(
                ((DoubleToken) originToken.getElement(0)).doubleValue(), 
                ((DoubleToken) originToken.getElement(1)).doubleValue(), 
                ((DoubleToken) originToken.getElement(2)).doubleValue()); 
        gl.glEnd( );
      
        
    }

}
