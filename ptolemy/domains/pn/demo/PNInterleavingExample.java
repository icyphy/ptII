/* An example to demonstrate the PN Domain Scheduler.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.domains.pn.kernel;
import pt.kernel.*;

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
	PNUniverse myUniverse = new PNUniverse();
        myUniverse.setMode(Integer.parseInt(args[0]));
        myUniverse.setNoCycles(Integer.parseInt(args[1]));
        PNInterleave _interleave = new PNInterleave(myUniverse, "interleave");
        _interleave.initialize();
        _interleave.setCycles(Integer.parseInt(args[1]));
        PNAlternate _alternate = new PNAlternate(myUniverse, "alternate");
        _alternate.initialize();
        PNRedirect _redirect0 = new PNRedirect(myUniverse, "redirect0");
        _redirect0.initialize(0);
        PNRedirect _redirect1 = new PNRedirect(myUniverse, "redirect1");
        _redirect1.initialize(1);

        //FIXME: Find a neat way of specifying the queue length of input port!
        //FIXME: Need a nice way of doing the following.
        //Maybe a nice method that set all star parameters and links all ports
        IORelation _queueX = new IORelation(myUniverse, "QX");
        PNPort port = (PNPort)_interleave.getPort("output");
        port.link(_queueX);
        port = (PNPort)_alternate.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueX);
 
        IORelation _queueY = new IORelation(myUniverse, "QY");
        port = (PNPort)_redirect0.getPort("output");
        port.link(_queueY);
        port = (PNPort)_interleave.getPort("input1");
        port.getQueue().setCapacity(1);
        port.link(_queueY);        
 
        IORelation _queueZ = new IORelation(myUniverse, "QZ");
        port = (PNPort)_redirect1.getPort("output");
        port.link(_queueZ);
        port = (PNPort)_interleave.getPort("input2");
        port.getQueue().setCapacity(1);
        port.link(_queueZ);
 
        IORelation _queueT1 = new IORelation(myUniverse, "QT1");
        port = (PNPort)_alternate.getPort("output1");
        port.link(_queueT1);
        port = (PNPort)_redirect0.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueT1);
 
        IORelation _queueT2 = new IORelation(myUniverse, "QT2");
        port = (PNPort)_alternate.getPort("output2");
        port.link(_queueT2);
        port = (PNPort)_redirect1.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueT2);
 
        //FIXME: Should I use connect() rather than all the above stuff??
 	myUniverse.execute();
        System.out.println("Bye World\n");
	return;
    }

}
   

