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

/** a demo for the SDF domain.  demonstrates an actor with more than one
 *  token on each port.
 *  @author Steve Neuendorffer
 *  @version $Id$
 */
public class SDFdemo2 {

    public static void main(String args[])
            throws IllegalActionException, NameDuplicationException
        {
            SDFdemo2 demo = new SDFdemo2();
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

            c.setManager(m);
            c.setDirector(d);
            d.setScheduler(s);
            d.setScheduleValid(false);

            SDFPrint print = new SDFPrint(c, "print");
            SDFRamp ramp = new SDFRamp(c, "ramp");
            SDFDelay delay = new SDFDelay(c, "delay");

            IOPort rampoutput = (IOPort)ramp.getPort("output");
            IOPort delayinput = (IOPort)delay.getPort("input");
            IOPort delayoutput = (IOPort)delay.getPort("output");
            IOPort printinput = (IOPort)print.getPort("input");

            delay.setTokenConsumptionRate(delayinput, 2);
            delay.setTokenProductionRate(delayoutput, 2);

            r = (IORelation) c.connect(rampoutput, delayinput, "R1");
            r = (IORelation) c.connect(delayoutput, printinput, "R2");

            Parameter p = (Parameter) d.getAttribute("Iterations");
            p.setToken(new IntToken(6));

            m.run();
        }
}




