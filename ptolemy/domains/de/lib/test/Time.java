/* A simple test case for timing purposes

 Copyright (c) 1999-2000 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib.test;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.domains.de.lib.Merge;

//////////////////////////////////////////////////////////////////////////
//// Time
/* A simple test case for timing purposes
@author Edward A. Lee, Lukito Muliadi
@version $Id$
*/

public class Time {

    public static void main(String arg[]) throws IllegalActionException,
            NameDuplicationException {
        Workspace w = new Workspace("w");
        TypedCompositeActor toplevel = new TypedCompositeActor(w);
        toplevel.setName("toplevel");
        DEDirector director = new DEDirector(toplevel, "director");
        // director.addDebugListener(new StreamListener());
        Manager manager = new Manager(w, "manager");
        toplevel.setManager(manager);

        Merge merge = new Merge(toplevel, "merge");

        // Create 20 clocks.
        for (int i = 1; i < 20; i++) {
            Clock clock = new Clock(toplevel, "clock" + i);
            clock.period.setExpression("" + (i*2));
            clock.values.setExpression("[" + i + ", " + i + "]");
            toplevel.connect(clock.output, merge.input);
        }

        Recorder recorder = new Recorder(toplevel, "recorder");
        recorder.capacity.setExpression("0");
        // TimedPlotter recorder = new TimedPlotter(toplevel, "recorder");
        // recorder.setPanel(null);
        // recorder.plot.setMarksStyle("dots");
        // recorder.plot.setConnected(false);

        toplevel.connect(merge.output, recorder.input);

        director.setStopTime(1000.0);
        manager.run();
        manager.run();
        System.out.println("Total number of events seen by Recorder: "
                + recorder.getCount());
    }
}
