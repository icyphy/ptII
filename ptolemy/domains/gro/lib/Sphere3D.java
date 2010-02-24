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

public class Sphere3D extends GROActor {

    public Sphere3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setOutput(true);
       

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{1.0, 1.0, 1.0}");

        origin = new Parameter(this, "origin");
        origin.setExpression("{0.0, 0.0, 0.0}");
        
        lineEnd= new Parameter(this, "lineEnd");
        lineEnd.setExpression("{0.0, 0.0, 0.0}");
        //GLPipelineObject.setTypeEquals(SceneGraphToken.TYPE);
        GLPipelineObject.setTypeEquals(BaseType.OBJECT);

    }
    public ColorAttribute rgbColor;

    /** The x coordinate of the line's start position in the view screen. */
    public Parameter origin;

    /** The y coordinate of the line's start position in the view screen. */
    public Parameter lineEnd;
    public TypedIOPort GLPipelineObject;
    
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        
        ArrayToken originToken = ((ArrayToken) origin.getToken());
        ArrayToken lineEndToken = ((ArrayToken) lineEnd.getToken());
        ArrayToken rgbColorValue = ((ArrayToken) rgbColor.getToken());
        
        GL gl = ((GRODirector) getDirector()).getGL();
        gl.glBegin(GL.GL_LINES);
        gl.glColor3d(
                ((DoubleToken) rgbColorValue.getElement(0)).doubleValue(), 
                ((DoubleToken) rgbColorValue.getElement(1)).doubleValue(), 
                ((DoubleToken) rgbColorValue.getElement(2)).doubleValue()); 

        // origin of the line
        gl.glVertex3d(
                ((DoubleToken) originToken.getElement(0)).doubleValue(), 
                ((DoubleToken) originToken.getElement(1)).doubleValue(), 
                ((DoubleToken) originToken.getElement(2)).doubleValue()); 
        
        // ending point of the line
        gl.glVertex3d(
                ((DoubleToken) lineEndToken.getElement(0)).doubleValue(), 
                ((DoubleToken) lineEndToken.getElement(1)).doubleValue(), 
                ((DoubleToken) lineEndToken.getElement(2)).doubleValue()); 
        gl.glEnd( );
        
    }
    
}
