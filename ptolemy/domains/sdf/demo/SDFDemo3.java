/*
@Copyright (c) 1998-1999 The Regents of the University of California.
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
public class SDFDemo3 {

    public static void main(String args[])
            throws IllegalActionException, NameDuplicationException
        {
            SDFDemo3 demo = new SDFDemo3();
            demo.execute();
        }

    public void execute()
            throws IllegalActionException, NameDuplicationException
        {

            Manager m = new Manager();
            TypedCompositeActor c = new TypedCompositeActor();
            SDFDirector d = new SDFDirector();
            SDFScheduler s = new SDFScheduler();
            TypedIORelation r;

            c.setDirector(d);
            c.setManager(m);
            d.setScheduler(s);
            d.setScheduleValid(false);

            SDFPrint print = new SDFPrint(c, "print");
            TypedIOPort printinput = (TypedIOPort) print.getPort("input");
            SDFPrint print2 = new SDFPrint(c, "print2");
            TypedIOPort print2input = (TypedIOPort) print2.getPort("input");
            SDFRamp ramp = new SDFRamp(c, "ramp");
            TypedIOPort rampoutput = (TypedIOPort) ramp.getPort("output");
            SDFRamp ramp2 = new SDFRamp(c, "ramp2");
            TypedIOPort ramp2output = (TypedIOPort) ramp2.getPort("output");
            SDFDelay delay = new SDFDelay(c, "delay");
            TypedIOPort delayinput = (TypedIOPort) delay.getPort("input");
            TypedIOPort delayoutput = (TypedIOPort) delay.getPort("output");
            delay.setTokenConsumptionRate(delayinput, 2);
            delay.setTokenProductionRate(delayoutput, 2);
            SDFDelay delay2 = new SDFDelay(c, "delay2");
            TypedIOPort delay2input = (TypedIOPort) delay2.getPort("input");
            TypedIOPort delay2output = (TypedIOPort) delay2.getPort("output");
            delay2.setTokenConsumptionRate(delay2input, 2);
            delay2.setTokenProductionRate(delay2output, 2);
            SDFSplit split = new SDFSplit(c, "split");
            TypedIOPort splitinput = (TypedIOPort) split.getPort("input");
            TypedIOPort splitoutput1 = (TypedIOPort) split.getPort("output1");
            TypedIOPort splitoutput2 = (TypedIOPort) split.getPort("output2");
            SDFJoin join = new SDFJoin(c, "join");
            TypedIOPort joininput1 = (TypedIOPort) join.getPort("input1");
            TypedIOPort joininput2 = (TypedIOPort) join.getPort("input2");
            TypedIOPort joinoutput = (TypedIOPort) join.getPort("output");

            r = (TypedIORelation) c.connect(rampoutput, splitinput, "R1");
            r = (TypedIORelation) c.connect(splitoutput1, delayinput, "R2");
            r = (TypedIORelation) c.connect(splitoutput2, delay2input, "R3");
            r = (TypedIORelation) c.connect(delayoutput, printinput, "R4");
            r = (TypedIORelation) c.connect(delay2output, joininput1, "R5");
            r = (TypedIORelation) c.connect(ramp2output, joininput2, "R6");
            r = (TypedIORelation) c.connect(joinoutput, print2input, "R7");

            Parameter p = (Parameter) d.getAttribute("Iterations");
            p.setToken(new IntToken(1));
            m.run();
        }
}




