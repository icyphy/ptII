package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;


/**
 This class serves as a delay for Dataflow Domains which allows schedulers
 like SDF to break a cycle within the topology
 * @version $Id$
 */
public class Delay extends SDFAtomicActor {
    public IOPort inputport;
    public IOPort outputport;

    public Delay(CompositeActor container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport=(IOPort)newPort("input");
            inputport.makeInput(true);
            setTokenConsumptionRate(inputport, 1);
            outputport=(IOPort)newPort("output");
            outputport.makeOutput(true);
            setTokenProductionRate(outputport, 1);
            setTokenInitProduction(outputport, 1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFDelay: constructor error");
            e1.printStackTrace();
        }
    }

    public void initialize() throws IllegalActionException {
        IntToken token = new IntToken();

        System.out.println("Delay, initial token" + token.toString());
        outputport.send(0,token);
    }
    
    public void fire() throws IllegalActionException {
        IntToken message;
        
        message=(IntToken)inputport.get(0);
        System.out.print("Delay - ");
        System.out.println(message.intValue());
        outputport.send(0,message);
    }
}






