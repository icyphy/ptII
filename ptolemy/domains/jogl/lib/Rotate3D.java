package ptolemy.domains.jogl.lib;
import javax.media.opengl.GL;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Rotate3D extends Transformer{
    public Rotate3D(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);

        output.setTypeEquals(BaseType.OBJECT);

        angle = new PortParameter(this, "angle");
        angle.setTypeEquals(BaseType.DOUBLE);
        angle.setExpression("0.0");
        
        axis = new PortParameter(this, "axis");
        axis.setTypeEquals(new ArrayType(BaseType.DOUBLE, 3));
        axis.setExpression("{1.0, 0.0, 0.0}");
    }

    /** Angle of rotation, where 0.0
     *  corresponds to zero degrees and 1.0 corresponds to 360 degrees.
     *  The rotation is around the axis specified by <i>axis</i> according
     *  to the right-hand rule. This is a double with default value 0.0.
     */
    public PortParameter angle;

    /** Array specifying the axis of rotation to be 
     *  applied to the input. This is an array of length 3, where
     *  ||(x, y, z)|| = 1 (if not, the GL will normalize this vector). The three
     *  elements specify a vector with respect to x (horizontal),
     *  y (vertical), and z (depth, out of the screen), respectively.
     *  This is an array of doubles of length 3, with default value
     *  {1.0, 0.0, 0.0}.
     */
    public PortParameter axis;


    public void fire() throws IllegalActionException {
        angle.update();
        axis.update();
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

            GL gl = ((GL) inputObject);
            gl.glLoadIdentity();
            double angleValue = ((DoubleToken)angle.getToken()).doubleValue();
            ArrayToken axisValue = ((ArrayToken) axis.getToken());
            
            gl.glRotated(
                    angleValue, 
                    ((DoubleToken) axisValue.getElement(1)).doubleValue(), 
                    ((DoubleToken) axisValue.getElement(2)).doubleValue(), 
                    ((DoubleToken) axisValue.getElement(3)).doubleValue()
            ); 
            output.send(0, new ObjectToken(gl));
        }

    }
}
