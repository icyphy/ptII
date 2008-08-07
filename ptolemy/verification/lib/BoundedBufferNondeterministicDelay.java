package ptolemy.verification.lib;

import ptolemy.actor.parameters.PortParameter;
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
 

    /** Throw an IllegalActionException to indicate that this actor
     *  is used for code generation only.
     *  @exception IllegalActionException No simulation
     */
    public void preinitialize() throws IllegalActionException {
        throw new IllegalActionException(this, getName() + " can not run in "
                + "simulation mode.");
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
