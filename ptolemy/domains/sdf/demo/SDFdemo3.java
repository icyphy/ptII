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
@ProposedRating Red
@AcceptedRating Red
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
 * This demo demonstrates a more complex SDF system
 * @author Steve Neuendorffer
 * @version $Id$
 */
public class SDFdemo3 {

    public static void main(String args[])
            throws IllegalActionException, NameDuplicationException
        {
            SDFdemo3 demo = new SDFdemo3();
            demo.execute();
        }

    public void execute()
            throws IllegalActionException, NameDuplicationException
        {

            Manager m = new Manager();
            CompositeActor c = new CompositeActor();
            SDFDirector d = new SDFDirector();
            SDFScheduler s = new SDFScheduler();
            IORelation r;

            c.setDirector(d);
            c.setManager(m);
            d.setScheduler(s);
            d.setScheduleValid(false);

            SDFPrint print = new SDFPrint(c, "print");
            IOPort printinput = (IOPort) print.getPort("input");
            SDFPrint print2 = new SDFPrint(c, "print2");
            IOPort print2input = (IOPort) print2.getPort("input");
            SDFRamp ramp = new SDFRamp(c, "ramp");
            IOPort rampoutput = (IOPort) ramp.getPort("output");
            SDFRamp ramp2 = new SDFRamp(c, "ramp2");
            IOPort ramp2output = (IOPort) ramp2.getPort("output");
            SDFDelay delay = new SDFDelay(c, "delay");
            IOPort delayinput = (IOPort) delay.getPort("input");
            IOPort delayoutput = (IOPort) delay.getPort("output");
            delay.setTokenConsumptionRate(delayinput, 2);
            delay.setTokenProductionRate(delayoutput, 2);
            SDFDelay delay2 = new SDFDelay(c, "delay2");
            IOPort delay2input = (IOPort) delay2.getPort("input");
            IOPort delay2output = (IOPort) delay2.getPort("output");
            delay2.setTokenConsumptionRate(delay2input, 2);
            delay2.setTokenProductionRate(delay2output, 2);
            SDFSplit split = new SDFSplit(c, "split");
            IOPort splitinput = (IOPort) split.getPort("input");
            IOPort splitoutput1 = (IOPort) split.getPort("output1");
            IOPort splitoutput2 = (IOPort) split.getPort("output2");
            SDFJoin join = new SDFJoin(c, "join");
            IOPort joininput1 = (IOPort) join.getPort("input1");
            IOPort joininput2 = (IOPort) join.getPort("input2");
            IOPort joinoutput = (IOPort) join.getPort("output");

            r = (IORelation) c.connect(rampoutput, splitinput, "R1");
            r = (IORelation) c.connect(splitoutput1, delayinput, "R2");
            r = (IORelation) c.connect(splitoutput2, delay2input, "R3");
            r = (IORelation) c.connect(delayoutput, printinput, "R4");
            r = (IORelation) c.connect(delay2output, joininput1, "R5");
            r = (IORelation) c.connect(ramp2output, joininput2, "R6");
            r = (IORelation) c.connect(joinoutput, print2input, "R7");

            Parameter p = (Parameter) d.getAttribute("Iterations");
            p.setToken(new IntToken(1));
            m.run();
        }
}




