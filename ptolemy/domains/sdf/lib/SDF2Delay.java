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
public class SDF2Delay extends SDFAtomicActor {
    public IOPort inputport;
    public IOPort outputport;

    public SDF2Delay(CompositeActor container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport=(IOPort)newPort("input");
            inputport.makeInput(true);
            setTokenConsumptionRate(inputport,2);
            outputport=(IOPort)newPort("output");
            outputport.makeOutput(true);
            setTokenProductionRate(outputport,2);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFDelay: constructor error");
        }
    }
    
    public void fire() throws IllegalActionException {
        IntToken message;
        

        message=(IntToken)inputport.get(0);
        System.out.print("Delay1 - ");
        System.out.println(message.getValue());
        outputport.send(0,message);
        message=(IntToken)inputport.get(0);
        System.out.print("Delay2 - ");
        System.out.println(message.getValue());
        outputport.send(0,message);
    }
}






