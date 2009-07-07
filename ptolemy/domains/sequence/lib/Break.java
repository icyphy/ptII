package ptolemy.domains.sequence.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

////Break

/**
 <p>An actor that implements a control break.
 If this actor is reached, control returns to the caller.
 This block has no functionality.
  
  Break is a ControlActor, meaning that it keeps a list of
  enabled output ports.  However, the Break actor has no output ports,
  so this list is always empty here.
  
  @author beth
  
 */

public class Break extends ControlActor {

    public Break(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // set name to invisible
        StringAttribute hideName = new StringAttribute(this, "_hideName");
        hideName.setExpression("true");

        // create inports
        input = new TypedIOPort(this, "input", true, false);
        
        // Beth added 12/18/08 - Break input is now also a control input 
        // Beth changed 02/04/09 - Break input changed back to a regular input
        // This is to be able to connect possibly unsequenced upstream actors
        // which do not necessarily output a boolean
        //input.setControl(true);
        
        // set portnames to visible
        StringAttribute inputShowName = new StringAttribute(input, "_showName");
        inputShowName.setExpression("false");
        
        // set direction of ports
        StringAttribute inputCardinal = new StringAttribute(input, "_cardinal");
        inputCardinal.setExpression("WEST");
        
        // set type constraints for ports   
        // The input to the break statement should be a control signal
        // which should be a boolean
        // Beth changed 02/04/09
        // Break input can now be any type
        // This way, can handle Break actors that are introduced because of return 
        // ports with sequence numbers.  
        // The input to the return port will now also be an input to the Break.
        // This way, the unsequenced actors upstream of the original return port
        // will be sequenced correctly.
        //input.setTypeEquals(BaseType.BOOLEAN);
        
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Beth added 02/04/09
     *  
     *  Add a check for an unconnected input port
     *  If the port is unconnected, set the type, so that the type
     *  will not resolve to unknown
     *  Can't use setAtLeast in the constructor, because the input
     *  could be any type, and booleans/integers/reals do not have a 
     *  common base type other than unknown
     *
     *  @exception IllegalActionException Not thrown here
     */
    
    public void preinitialize() throws IllegalActionException {
    
    	super.preinitialize();
    	
    	if (input.connectedPortList().isEmpty())
    	{
    		input.setTypeEquals(BaseType.BOOLEAN);
    	}
    }
    
    
}
