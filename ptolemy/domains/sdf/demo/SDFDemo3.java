/*
@Copyright (c) 1998 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

						PT_COPYRIGHT_VERSION 2
						COPYRIGHTENDKEY
*/
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




