/* This creates an example implementing Sieve of Eratosthenes

 Copyright (c) 1998-2000  The Regents of the University of California.
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

package ptolemy.domains.pn.demo.Prime;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import java.util.Enumeration;


//////////////////////////////////////////////////////////////////////////
//// PNPrimeExample
/**
This is currently a Universe containing some PN Actors. This might not support
hierarchy currently.
@author  Mudit Goel
@version $Id$
*/
public class PNPrimeGalaxy {

    public static void main(String args[]) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {
        CompositeActor myUniverse = new CompositeActor();
        myUniverse.setName("Prime_example");
	Manager exec = new Manager("exec");
        // FIXME FIXME FIXME
	myUniverse.setManager(exec);
	PNDirector local = new PNDirector(myUniverse, "Local");
	//myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));
        PNRamp ramp = new PNRamp(myUniverse, "ramp");
        ramp.setParam("Initial Value", "2");

	CompositeActor galaxy2 = new CompositeActor(myUniverse, "galaxy2");
	IOPort galin = (IOPort)galaxy2.newPort("input");
        PNGalaxySieve sieve = new PNGalaxySieve(galaxy2, "2_sieve");
        sieve.setParam("prime", "2");
        IOPort portin = (IOPort)sieve.getPort("input");
	galaxy2.connect(portin, galin, "2_gal");
        IOPort portout = (IOPort)ramp.getPort("output");
        myUniverse.connect(galin, portout, "2_queue");


	IOPort galout = (IOPort)galaxy2.newPort("output");
	portout = (IOPort)sieve.getPort("output");
	galaxy2.connect(galout, portout, "gal_2out");
	PNSink sink = new PNSink(myUniverse, "sink");
        portin = (IOPort)sink.getPort("input");
	myUniverse.connect(portin, galout, "plot_queue");

        //PNPlot plot = new PNPlot(myUniverse, "plotter");
        //portout = (PNOutPort)sieve.getPort("output");
        //portin = (PNInPort)plot.getPort("input");
        //myUniverse.connect(portin, portout, "plot_queue");
        //portin.getQueue(portout).setCapacity(1);

        //System.out.println(
        //     myUniverse.description(pt.kernel.Nameable.LIST_PRETTYPRINT));

        //DynamicGraphView view = DynamicGraphView.makeView(
	//       "Sieve of Eratosthenes", 800, 600);

        //view.loadPtolemyGraph(myUniverse);

        // DebugMutationListener d = new DebugMutationListener();
        // myUniverse.getDirector().addMutationListener(d);
        //myUniverse.getDirector().addMutationListener(view);

        exec.run();
        System.out.println("Bye World\n");
        return;
    }
}
