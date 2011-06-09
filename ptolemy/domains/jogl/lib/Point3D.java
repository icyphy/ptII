package ptolemy.domains.jogl.lib;


import javax.media.opengl.GL;

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
 * An actor that is used for drawing 3D point. 
 *
 * @author Yasemin Demir
 * @version $Id: JoglDirector.java 57401 2010-03-03 23:11:41Z ydemir $
 */
public class Point3D extends Sink{
    
    
    /**
     *  Construct a Point3D object in the given container with the given name.
     *  If the container argument is null, a NullPointerException will
     *  be thrown. If the name argument is null, then the name is set
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this Point3D.
     *  @exception IllegalActionException If this actor
     *  is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *  CompositeActor and the name collides with an entity in the container.
     */
    public Point3D(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container,name);
        
        input.setTypeEquals(BaseType.OBJECT);
        

        radius = new Parameter(this, "radius");
        radius.setExpression("1.0");

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{1.0, 0.0, 0.0}");

        origin = new Parameter(this, "origin");
        origin.setExpression("{0.0, 0.0, 0.0}");


    }

    /**Specifies the diameter of the rasterized point.  The initial   
     * value is 1.
     */


    public Parameter radius;

    /** The red, green, blue, and alpha components of the line.  This
     *  parameter must contain an array of double values.  The default
     *  value is {0.0, 0.0, 1.0}, corresponding to opaque black.
     */
    public ColorAttribute rgbColor;

    /** Array specifying the origin position of a point. This is an 
     * array of length 3, where ||(x, y, z)|| = 1, if not, the GL will 
     * normalize this vector. The default value is {0.0, 0.0, 0.0}.           
     */
    public Parameter origin;



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
            
            GL gl = (GL) inputObject;
            ArrayToken originToken = ((ArrayToken) origin.getToken());
            ArrayToken rgbColorValue = ((ArrayToken) rgbColor.getToken());
            DoubleToken radiusValue = (DoubleToken) radius.getToken();


            gl.glPointSize((float) radiusValue.doubleValue());
            gl.glBegin(GL.GL_POINTS);

            gl.glColor3d(((DoubleToken) rgbColorValue.getElement(0)).doubleValue(),
                    ((DoubleToken) rgbColorValue.getElement(1)).doubleValue(),
                    ((DoubleToken) rgbColorValue.getElement(2)).doubleValue());

            // origin of the line
            gl.glVertex3d(((DoubleToken) originToken.getElement(0)).doubleValue(),
                    ((DoubleToken) originToken.getElement(1)).doubleValue(),
                    ((DoubleToken) originToken.getElement(2)).doubleValue());
            gl.glEnd();
            
            

            
        }
    }


}
