package ptolemy.domains.sdf.demo;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;
import java.util.Enumeration;


/**
 * @version $Id$
 */
public class SDFdemo3 {

    private SDFRamp ramp;
    private SDFRamp ramp2;
    private SDF2Delay delay;
    private SDF2Delay delay2;
    private SDFSplit split;
    private SDFPrint print;
    private SDFPrint print2;
    private SDFJoin join;

    private Manager m = new Manager();
    private CompositeActor c= new CompositeActor();
    private SDFDirector d= new SDFDirector();
    private SDFScheduler s=new SDFScheduler();
    private IORelation r;

    public static void main(String args[])            
            throws IllegalActionException, NameDuplicationException
         {

        
        SDFdemo3 demo=new SDFdemo3();
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
                print2=new SDFPrint(c,"print2");
                ramp=new SDFRamp(c,"ramp");
                ramp2=new SDFRamp(c,"ramp2");
                delay=new SDF2Delay(c,"delay");
                delay2=new SDF2Delay(c,"delay2");
                split= new SDFSplit(c,"split");
                join = new SDFJoin(c,"join");

                r=(IORelation) c.connect(ramp.outputport,split.inputport,"R1");
                r=(IORelation) c.connect(split.outputport1,delay.inputport,"R2");
                r=(IORelation) c.connect(split.outputport2,delay2.inputport,"R3");
                r=(IORelation) c.connect(delay.outputport,print.inputport,"R4");
                r=(IORelation) c.connect(delay2.outputport,join.inputport1,"R5");
                r=(IORelation) c.connect(ramp2.outputport,join.inputport2,"R6");
                r=(IORelation) c.connect(join.outputport,print2.inputport,"R7");

                Parameter p = (Parameter) d.getAttribute("Iterations");
                p.setToken(new IntToken(1));
                m.run();
        }
}    
 
    


