package ptolemy.domains.gro.lib;

import javax.media.opengl.GL;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.gro.kernel.GROActor;
import ptolemy.domains.gro.kernel.GRODirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;



public class Line3D extends GROActor {

    public Line3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject");
        GLPipelineObject.setOutput(true);
        //GLPipelineObject.setTypeEquals(SceneGraphToken.TYPE);
        GLPipelineObject.setMultiport(true);
        
        
        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{0.0, 0.0, 0.0}");

        

        lineStart = new Parameter(this, "lineStart", new DoubleToken(0.0));
        lineStart.setExpression("{0.0, 0.0, 0.0}");
        
        lineEnd= new Parameter(this, "lineEnd", new DoubleToken(0.0));
        lineEnd.setExpression("{0.0, 0.0, 0.0}");

       
    }
    
    // ports and parameters
    
    

    /** The red, green, blue, and alpha components of the line.  This
     *  parameter must contain an array of double values.  The default
     *  value is {0.0, 0.0, 1.0}, corresponding to opaque black.
     */
    public ColorAttribute rgbColor;

    /** The x coordinate of the line's start position in the view screen. */
    public Parameter lineStart;

    /** The y coordinate of the line's start position in the view screen. */
    public Parameter lineEnd;
    
    
    public TypedIOPort GLPipelineObject;
   
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        
        ArrayToken lineStartToken = ((ArrayToken) lineStart.getToken());
        ArrayToken lineEndToken = ((ArrayToken) lineEnd.getToken());
        GL gl = ((GRODirector) getDirector()).getGL();
        gl.glBegin(GL.GL_LINES);
        gl.glColor3f(0.3f, 0.7f, 0.3f); 
        gl.glVertex3f((float) ((DoubleToken) lineStartToken.getElement(0))
                .doubleValue(), (float) ((DoubleToken) lineStartToken.getElement(1))
                .doubleValue(), (float) ((DoubleToken) lineStartToken.getElement(2))
                .doubleValue()); // origin of the line
        gl.glVertex3f((float) ((DoubleToken) lineEndToken.getElement(0))
                .doubleValue(), (float) ((DoubleToken) lineEndToken.getElement(1))
                .doubleValue(), (float) ((DoubleToken) lineEndToken.getElement(2))
                .doubleValue()); // ending point of the line
        gl.glEnd( );
        
    }

}
