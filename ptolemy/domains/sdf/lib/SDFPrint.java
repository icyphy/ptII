package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

 

/**
 * @version $Id$
 */
public class SDFPrint extends SDFAtomicActor {
    public IOPort inputport;

    public SDFPrint(CompositeActor container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport=(IOPort)newPort("input");
            inputport.makeInput(true);
            setTokenConsumptionRate(inputport,1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFPrint: Constructor error");
        }
    }
    
    public void fire() throws IllegalActionException {
        IntToken message;
        

        message=(IntToken)inputport.get(0);
        System.out.println(message.intValue());

        
        
    }
}






