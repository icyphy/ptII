package ptolemy.domains.jogl.lib;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import ptolemy.actor.lib.Transformer;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class Translate3D extends Transformer{
    public Translate3D(CompositeEntity container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);

        output.setTypeEquals(BaseType.OBJECT);
        translation = new PortParameter(this, "translation");
        translation.setExpression("{0.0, 0.0, 0.0}");

    }

    public PortParameter translation;

    public void fire() throws IllegalActionException {
        translation.update();
        if (_debugging) {
            _debug("Called fire()");
        }

        if (input.hasToken(0)) {
            ObjectToken inputToken = (ObjectToken)input.get(0);
            Object inputObject = inputToken.getValue();
            if (!(inputObject instanceof GLAutoDrawable)) {
                throw new IllegalActionException(this,
                        "Input is required to be an instance of GL. Got "
                        + inputObject.getClass());
            }

            GL gl = ((GLAutoDrawable)inputObject).getGL();

            ArrayToken translationValue = ((ArrayToken) translation.getToken());
            
            gl.glTranslated(
                    ((DoubleToken) translationValue.getElement(0)).doubleValue(), 
                    ((DoubleToken) translationValue.getElement(1)).doubleValue(), 
                    ((DoubleToken) translationValue.getElement(2)).doubleValue() 
            ); 
            output.send(0, new ObjectToken(gl));
        }

    }
}
