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



public class Line3D extends GROActor {

    public Line3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        GLPipelineObject = new TypedIOPort(this, "GLPipelineObject", false, true);
        GLPipelineObject.setTypeEquals(BaseType.OBJECT);
        
        width = new Parameter(this, "width");
        width.setExpression("2.0");

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{1.0, 1.0, 1.0}");

        lineStart = new Parameter(this, "lineStart");
        lineStart.setExpression("{0.0, 0.0, 0.0}");
        
        lineEnd= new Parameter(this, "lineEnd");
        lineEnd.setExpression("{0.0, 0.0, 0.0}");

       
    }
    
    // ports and parameters
    

    public Parameter width;


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
        ArrayToken rgbColorValue = ((ArrayToken) rgbColor.getToken());
        DoubleToken widthValue = (DoubleToken) width.getToken();
        
        GL gl = ((GRODirector) getDirector()).getGL();

        gl.glBegin(GL.GL_LINES);
        gl.glLineWidth((float) widthValue.doubleValue());

        gl.glColor3d(
                ((DoubleToken) rgbColorValue.getElement(0)).doubleValue(), 
                ((DoubleToken) rgbColorValue.getElement(1)).doubleValue(), 
                ((DoubleToken) rgbColorValue.getElement(2)).doubleValue()); 

        // origin of the line
        gl.glVertex3d(
                ((DoubleToken) lineStartToken.getElement(0)).doubleValue(), 
                ((DoubleToken) lineStartToken.getElement(1)).doubleValue(), 
                ((DoubleToken) lineStartToken.getElement(2)).doubleValue()); 
        
        // ending point of the line
        gl.glVertex3d(
                ((DoubleToken) lineEndToken.getElement(0)).doubleValue(), 
                ((DoubleToken) lineEndToken.getElement(1)).doubleValue(), 
                ((DoubleToken) lineEndToken.getElement(2)).doubleValue()); 

        gl.glEnd( );
        
    }

}
