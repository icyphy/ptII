package ptolemy.domains.sdf.demo;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import java.util.Enumeration;

import ptolemy.domains.sdf.kernel.*;

/**
 * @version $Id$
 */
public class SDFdemo {

    private SDFRamp ramp;
    private SDFDelay delay;
    private SDFPrint print;
    private Manager m = new Manager();
    private CompositeActor c= new CompositeActor();
    private SDFDirector d= new SDFDirector();
    private SDFScheduler s=new SDFScheduler();
    private IORelation r;

    public static void main(String args[])            
            throws IllegalActionException, NameDuplicationException
         {
             DebugListener debugger = new DebugListener();
    
        Debug.register(debugger);
        SDFdemo demo=new SDFdemo();
        demo.execute();
    }
    public void execute()
            throws IllegalActionException, NameDuplicationException
        {

                c.setDirector(d);
                c.setManager(m);
                d.setScheduler(s);
                d.setScheduleValid(false);

                print=new SDFPrint(c,"print");
                ramp=new SDFRamp(c,"ramp");
                delay=new SDFDelay(c,"delay");

                r=(IORelation) c.connect(ramp.outputport,delay.inputport,"R1");
                r=(IORelation) c.connect(delay.outputport,print.inputport,"R2");
                             
                m.blockingGo(5);
        }
}    
 
    


