/**
 * 
 */
package ptolemy.domains.ptides.lib.targets.luminary;

import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Actor to interface with GPIO port A on the Luminary Micro.
 * This actor will have no effect in model simulation.
 *  
 * @author elgeeko
 *
 */
public class GPOutputDeviceA extends GPOutputDevice {
    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public GPOutputDeviceA(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
            super(container, name);
            pin = new Parameter(this, "pin");
            pin.setExpression("7");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       parameters                          ////

    /** Which pin (0-7) of GPIOA to use
     * FIX: Verify user has set value between 0 and 7
     */
    public Parameter pin;
}
