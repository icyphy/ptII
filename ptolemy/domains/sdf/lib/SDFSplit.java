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
public class SDFSplit extends SDFAtomicActor {
    public IOPort inputport;
    public IOPort outputport1;
    public IOPort outputport2;

    public SDFSplit(CompositeActor container, String name) 
        throws IllegalActionException, NameDuplicationException {
        super(container,name);
        try{
            inputport=(IOPort)newPort("input");
            inputport.makeInput(true);
            setTokenConsumptionRate(inputport,2);
            outputport1=(IOPort)newPort("output1");
            outputport1.makeOutput(true);
            setTokenProductionRate(outputport1,1);
            outputport2=(IOPort)newPort("output2");
            outputport2.makeOutput(true);
            setTokenProductionRate(outputport2,1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFSplit: constructor error");
        }
    }
    
    public void fire() throws IllegalActionException {
        IntToken message;
        

        message=(IntToken)inputport.get(0);
        System.out.print("Split1 - ");
        System.out.println(message.getValue());
        outputport1.send(0,message);
        message=(IntToken)inputport.get(0);
        System.out.print("Split2 - ");
        System.out.println(message.getValue());
        outputport2.send(0,message);
    }
}






