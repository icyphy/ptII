/** a demo for the SDF domain.  demonstrates an actor with more than one 
 *  on each port.   
 *  @author Steve Neuendorffer
 *  @version $Id$
 */

package ptolemy.domains.sdf.demo;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import java.util.Enumeration;


/**
 * @version $Id$
 */
public class SDFdemo2 {

    private SDFRamp ramp;
    private SDF2Delay delay;
    private SDFPrint print;
    private Manager m = new Manager();
    private CompositeActor c = new CompositeActor();
    private SDFDirector d = new SDFDirector();
    private SDFScheduler s = new SDFScheduler();
    private IORelation r;

    public static void main(String args[])            
            throws IllegalActionException, NameDuplicationException
         {
             //            DebugListener debugger = new DebugListener();
             //Debug.register(debugger);
        
        SDFdemo2 demo=new SDFdemo2();
        demo.execute();
    }
    public void execute()
            throws IllegalActionException, NameDuplicationException
        {

                c.setManager(m);
                c.setDirector(d);
                d.setScheduler(s);
                d.setScheduleValid(false);

                print=new SDFPrint(c,"print");
                ramp=new SDFRamp(c,"ramp");
                delay=new SDF2Delay(c,"delay");

                r=(IORelation) c.connect(ramp.outputport,delay.inputport,"R1");
                r=(IORelation) c.connect(delay.outputport,print.inputport,"R2");
             
                
      
                m.go(6);
        }
}    
 
    


