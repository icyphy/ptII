package ptolemy.domains.sdf.lib;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import java.util.Enumeration;
import ptolemy.domains.sdf.kernel.*;

 

public class SDFJoin extends SDFAtomicActor {
    public IOPort inputport1;
    public IOPort inputport2;
    public IOPort outputport;

    public SDFJoin(CompositeActor container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport1=(IOPort)newPort("input1");
            inputport1.makeInput(true);
            setTokenConsumptionRate(inputport1,1);
            inputport2=(IOPort)newPort("input2");
            inputport2.makeInput(true);
            setTokenConsumptionRate(inputport2,1);
            outputport=(IOPort)newPort("output");
            outputport.makeOutput(true);
            setTokenProductionRate(outputport,2);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFJoin: constructor error");
        }
    }
    
    public void fire() throws IllegalActionException {
        IntToken message;
        
        
        message=(IntToken)inputport1.get(0);
        System.out.print("Join1 - ");
        System.out.println(message.getValue());
        outputport.send(0,message);
        message=(IntToken)inputport2.get(0);
        System.out.print("Join2 - ");
        System.out.println(message.getValue());
        outputport.send(0,message);
            
    }
}






