package ptolemy.verification.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.lib.TimedDelay;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class BoundedBufferTimedDelay extends TimedDelay {

    public BoundedBufferTimedDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The size of the buffer. The default for this parameter is 1.
     *  This parameter must contain a DoubleToken
     *  with a non-negative value, or an exception will be thrown when
     *  it is set.
     */
    public Parameter bufferSize;

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == bufferSize) {
            int newBufferSize = ((IntToken) (bufferSize.getToken())).intValue();

            if (newBufferSize < 1) {
                throw new IllegalActionException(this,
                        "Cannot have buffer less than one: " + newBufferSize);
            } else {
                _bufferSize = newBufferSize;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Override the method of the super class to initialize the
     *  parameter values.
     */
    protected void _init() throws NameDuplicationException,
            IllegalActionException {
        delay = new PortParameter(this, "delay");
        delay.setExpression("1.0");
        delay.setTypeEquals(BaseType.DOUBLE);
        bufferSize = new PortParameter(this, "bufferSize");
        bufferSize.setExpression("1");
        bufferSize.setTypeEquals(BaseType.INT);
    }
    

    /** The amount of buffer size.
     */
    protected int _bufferSize;
}
