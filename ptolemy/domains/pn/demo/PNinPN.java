/* An example to demonstrate the PN Domain Scheduler.

 Copyright (c) 1997-1999 The Regents of the University of California.
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
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
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

class PNinPN {

    public static void main(String args[]) throws
	    IllegalStateException, IllegalActionException,
            NameDuplicationException {
	CompositeActor myUniverse = new CompositeActor();
	Manager exec = new Manager("exec");
        // FIXME FIXME FIXME
        myUniverse.setManager(exec);
	BasePNDirector local = new BasePNDirector("Local");
        //local.addProcessListener(new DefaultPNListener());
	myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));
        CompositeActor inner = new CompositeActor(myUniverse, "inner");
        BasePNDirector indir = new BasePNDirector("indir");
        inner.setDirector(indir);
        //indir.addProcessListener(new DefaultPNListener());
        IOPort inport1 = (IOPort)inner.newPort("input1");
        inport1.setInput(true);
        IOPort outport1 = (IOPort)inner.newPort("output1");
        outport1.setOutput(true);
        IOPort inport2 = (IOPort)inner.newPort("input2");
        inport2.setInput(true);
        IOPort outport2 = (IOPort)inner.newPort("output2");
        outport2.setOutput(true);

        PNInterleave _interleave = new PNInterleave(inner, "interleave");
        PNAlternate _alternate = new PNAlternate(inner, "alternate");
        PNRedirect _redirect0 = new PNRedirect(myUniverse, "redirect0");
        _redirect0.setParam("Initial Value", "0");
        PNRedirect _redirect1 = new PNRedirect(myUniverse, "redirect1");
        _redirect1.setParam("Initial Value", "1");

        IOPort portout = (IOPort)_interleave.getPort("output");
        IOPort portin = (IOPort)_alternate.getPort("input");
        inner.connect(portin, portout, "QX");

        portout = (IOPort)_redirect0.getPort("output");
        myUniverse.connect(inport1, portout);
        portin = (IOPort)_interleave.getPort("input");
        inner.connect(portin, inport1);
        //queue = (IORelation)myUniverse.connect(portin, portout, "QY");


        portout =(IOPort)_redirect1.getPort("output");
        myUniverse.connect(inport2, portout);
        portin = (IOPort)_interleave.getPort("input");
        inner.connect(portin, inport2);
        //queue = (IORelation)myUniverse.connect(portin, portout, "QZ");
        //portin.getQueue().setCapacity(1);

        portout = (IOPort)_alternate.getPort("output");
        inner.connect(outport1, portout);
        portin = (IOPort)_redirect0.getPort("input");
        myUniverse.connect(portin, outport1, "QT1");


        portout = (IOPort)_alternate.getPort("output");
        inner.connect(outport2, portout);
        portin = (IOPort)_redirect1.getPort("input");
        myUniverse.connect(portin, outport2);
        //portin.getQueue().setCapacity(1);

	System.out.println("Connections made");
 	exec.startRun();
        System.out.println("Bye World\n");
	return;
    }

}


