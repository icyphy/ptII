package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;


public class SDFDelay extends SDFAtomicActor {
    public IOPort inputport;
    public IOPort outputport;

    public SDFDelay(CompositeActor container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport=(IOPort)newPort("input");
            inputport.makeInput(true);
            setTokenConsumptionRate(inputport,1);
            outputport=(IOPort)newPort("output");
            outputport.makeOutput(true);
            setTokenProductionRate(outputport,1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFDelay: constructor error");
            e1.printStackTrace();
        }
    }
    
    public void fire() throws IllegalActionException {
        IntToken message;
        
        message=(IntToken)inputport.get(0);
        System.out.print("Delay - ");
        System.out.println(message.getValue());
        outputport.send(0,message);
    }
}






