/* This creates an example implementing Sieve of Eratosthenes

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

package ptolemy.domains.pn.demo;
import ptolemy.kernel.*;
import ptolemy.data.*;
import ptolemy.actors.*;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.actors.*;
import java.util.Enumeration;

import gui.DynamicGraphView;

//////////////////////////////////////////////////////////////////////////
//// PNPrimeExample
/** 
This is currently a Universe containing some PN Actors. This might not support
hierarchy currently.
@author  Mudit Goel
@version $Id$
*/
public class PNPrimeExample {

    public static void main(String args[]) throws 
            IllegalStateException, IllegalActionException, 
            NameDuplicationException {
        PNCompositeActor myUniverse = new PNCompositeActor();
        myUniverse.setName("Prime_example");
        myUniverse.setCycles(Integer.parseInt(args[0]));
        PNRamp ramp = new PNRamp(myUniverse, "ramp");
        ramp.setInitState(2);
        PNSieve sieve = new PNSieve(myUniverse, "2_sieve");
        sieve.setInitState(2);
        PNInPort portin = (PNInPort)sieve.getPort("input");
        PNOutPort portout = (PNOutPort)ramp.getPort("output");
        myUniverse.connect(portin, portout, "2_queue");

        PNPlot plot = new PNPlot(myUniverse, "plotter");
        portout = (PNOutPort)sieve.getPort("output");
        portin = (PNInPort)plot.getPort("input");
        myUniverse.connect(portin, portout, "plot_queue");
        //portin.getQueue(portout).setCapacity(1);

        //System.out.println(myUniverse.description(ptolemy.kernel.Nameable.LIST_PRETTYPRINT));

        DynamicGraphView view = DynamicGraphView.makeView(
                "Sieve of Eratosthenes", 800, 600);

        view.loadPtolemyGraph(myUniverse);

        // DebugMutationListener d = new DebugMutationListener();
        // myUniverse.getDirector().addMutationListener(d);
        myUniverse.getDirector().addMutationListener(view);

        myUniverse.start();
        System.out.println("Bye World\n");
        return;
    }
}
