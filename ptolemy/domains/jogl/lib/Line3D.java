package ptolemy.domains.jogl.lib;

import javax.media.opengl.GL;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.lib.Sink;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


/**
 * An actor that is used for drawing 3D line. 
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 */

    
    

public class Line3D extends Sink{
    
    /**
     *  Construct a Line3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Line3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */


    public Line3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        
        input.setTypeEquals(BaseType.OBJECT);        
        
        width = new Parameter(this, "width");
        width.setExpression("2.0");

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{1.0, 1.0, 1.0}");

        lineStart = new Parameter(this, "lineStart");
        lineStart.setExpression("{0.0, 0.0, 0.0}");
        
        lineEnd= new Parameter(this, "lineEnd");
        lineEnd.setExpression("{0.0, 0.0, 0.0}");

       
    }
    
    /** 
     * The width of the line.
     */

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
    
   
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken)input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof GL)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of GL. Got "
                        + inputObject.getClass());
            }
            
            GL gl = (GL)inputObject;
        
        ArrayToken lineStartToken = ((ArrayToken) lineStart.getToken());
        ArrayToken lineEndToken = ((ArrayToken) lineEnd.getToken());
        ArrayToken rgbColorValue = ((ArrayToken) rgbColor.getToken());
        DoubleToken widthValue = (DoubleToken) width.getToken();
        

        gl.glLineWidth((float) widthValue.doubleValue());
        gl.glBegin(GL.GL_LINES);

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

}

