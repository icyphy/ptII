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
public class SDFRamp extends SDFAtomicActor {
    private int value;
    public IOPort outputport;

    public SDFRamp(CompositeActor container, String name) 
            throws IllegalActionException,
            NameDuplicationException {
        super(container,name);
        try{
            outputport=(IOPort) newPort("output");
            outputport.makeOutput(true);
            setTokenProductionRate(outputport,1);
        }
        catch (IllegalActionException e1) {
            System.out.println("SDFRamp: constuctor error");
        }
        value=0;

    }

    public void initialize() {
            value=0;
    }
    
    public boolean prefire() throws IllegalActionException {
        return true;
    }
        

    public void fire() throws IllegalActionException {
    System.out.println("Running Ramp");

        Token message=new IntToken(value);
        value=value+1;

        outputport.send(0,message);
    }
}

