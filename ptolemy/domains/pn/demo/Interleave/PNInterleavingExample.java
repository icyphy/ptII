/* An example to demonstrate the PN Domain Scheduler.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (mudit@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.pn.demo.Interleave;
import ptolemy.domains.pn.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// PNInterleavingExample
/**
An example to test the PN domain. This example tests the PN INterleaving
example.
@author Mudit Goel
@version $Id$
*/

class PNInterleavingExample {

    public static void main(String args[]) throws
	    IllegalStateException, IllegalActionException,
            NameDuplicationException {
	CompositeActor myUniverse = new CompositeActor();
	Manager exec = new Manager("exec");
        // FIXME FIXME FIXME
        myUniverse.setManager(exec);
	BasePNDirector local = new BasePNDirector(myUniverse,"Local");
	//myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));
        PNInterleave _interleave = new PNInterleave(myUniverse, "interleave");
        PNAlternate _alternate = new PNAlternate(myUniverse, "alternate");
        PNRedirect _redirect0 = new PNRedirect(myUniverse, "redirect0");
        _redirect0.setParam("Initial Value", "0");
        PNRedirect _redirect1 = new PNRedirect(myUniverse, "redirect1");
        _redirect1.setParam("Initial Value", "1");
        //PNPlot _plot = new PNPlot(myUniverse, "plot");

        //FIXME: Find a neat way of specifying the queue length of input port!
        //FIXME: Need a nice way of doing the following.
        //Maybe a nice method that set all star parameters and links all ports
        IOPort portout = (IOPort)_interleave.getPort("output");
        IOPort portin = (IOPort)_alternate.getPort("input");
        IORelation queue =
            (IORelation)myUniverse.connect(portin, portout, "QX");
        //portin.getQueue().setCapacity(1);

        //portout = (PNOutPort)_interleave.getPort("output");
        //((PNInPort)_plot.getPort("input")).link(queue);

        //queue = (IORelation)myUniverse.connect(portin, portout, "QPlot");

        portout = (IOPort)_redirect0.getPort("output");
        portin = (IOPort)_interleave.getPort("input");
        queue = (IORelation)myUniverse.connect(portin, portout, "QY");
        //portin.getQueue().setCapacity(1);


	//((PNOutPort)_redirect1.getPort("output")).link(queue);
        portout = (IOPort)_redirect1.getPort("output");
        portin = (IOPort)_interleave.getPort("input");
        queue = (IORelation)myUniverse.connect(portin, portout, "QZ");
        //portin.getQueue().setCapacity(1);

        portout = (IOPort)_alternate.getPort("output");
        portin = (IOPort)_redirect0.getPort("input");
        queue = (IORelation)myUniverse.connect(portin, portout, "QT1");
        //portin.getQueue().setCapacity(1);

        portout = (IOPort)_alternate.getPort("output");
        portin = (IOPort)_redirect1.getPort("input");
        queue = (IORelation)myUniverse.connect(portin, portout, "QT2");
        //portin.getQueue().setCapacity(1);

	System.out.println("Connections made");
 	exec.startRun();
        System.out.println("Bye World\n");
	return;
    }

}
