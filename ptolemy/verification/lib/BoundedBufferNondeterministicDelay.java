package ptolemy.verification.lib;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class BoundedBufferNondeterministicDelay extends BoundedBufferTimedDelay {

    /** Construct an actor with the specified container and name.
     *  @param container The composite entity to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public BoundedBufferNondeterministicDelay(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    // FIXME: VariableDelay.delay overrides TimedDelay.delay.
    /** The amount specifying delay. Its default value is 1.0.
     */
    public PortParameter delay;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the delay parameter from the delay port and ensure the delay
     *  is not negative. Call the fire method of super class to consume
     *  inputs and generate outputs.
     *  @exception IllegalActionException If the super class throws it,
     *  or a negative delay is received.
     */
    public void fire() throws IllegalActionException {
        delay.update();
        _delay = ((DoubleToken) delay.getToken()).doubleValue();

        if (_delay < 0) {
            throw new IllegalActionException("Can not have a "
                    + "negative delay: " + _delay + ". "
                    + "Check whether overflow happens.");
        }

        // NOTE: _delay may be 0.0, which may change
        // the causality property of the model.
        // We leave the model designers to decide whether the
        // zero delay is really what they want.
        super.fire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> or <i>delay</i> ports
     *  in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(delay.getPort(), output);
    }

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

}
