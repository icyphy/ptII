/* Simple DE test system.

 Copyright (c) 1998 The Regents of the University of California.
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
*/

package ptolemy.domains.de.demo;

import ptolemy.domains.de.kernel.*;
import ptolemy.domains.de.lib.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// ClockSystem
/** 
A simple DE test system.

@author Edward A. Lee
@version $Id$
*/
public class ClockSystem {
    /** Constructor
     */	
    public ClockSystem()
            throws IllegalActionException, NameDuplicationException {
        topLevel = new CompositeActor();
        topLevel.setName("Top");

        // Set up the directors
        localDirector = new DECQDirector("DE Director");
        topLevel.setDirector(localDirector);
        executiveDirector = new Director("Executive Director");
        topLevel.setExecutiveDirector(executiveDirector);

        // Set up the actors and connections
        DEClock clock = new DEClock(topLevel, "Clock", 1.0, 1.0);
        DEPoisson poisson = new DEPoisson(topLevel, "Poisson",-1.0,1.0);
        DEPlot plot = new DEPlot(topLevel, "Plot");
        topLevel.connect(clock.output, plot.input);
        topLevel.connect(poisson.output, plot.input);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Run the system for the specified amount of time.
     */	
    public void go(double stopTime) throws CloneNotSupportedException,
            IllegalActionException, NameDuplicationException {
        localDirector.setStopTime(stopTime);
        executiveDirector.run();
    }

    /** Run for 10 time units.
     */
    public static void main(String argv[]) {
        try {
            ClockSystem test = new ClockSystem();
            test.go(10.0);
        } catch (Exception ex) {
            System.out.println("Run failed: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The top-level system.
    private CompositeActor topLevel;
    private DECQDirector localDirector;
    private Director executiveDirector;
}
