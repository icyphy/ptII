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
//// PNExample
/** 
An example to test the PN domain. This example tests the PN INterleaving 
example.
@author Mudit Goel
@version $Id$
*/

class PNInterExample {

    public static void main(String args[]) throws 
	    IllegalStateException, IllegalActionException, 
            NameDuplicationException {
	PNUniverse myUniverse = new PNUniverse();
        myUniverse.setMode(Integer.parseInt(args[0]));
        myUniverse.setNoCycles(Integer.parseInt(args[1]));
        _interleave = new PNInterleave(myUniverse, "interleave");
        _interleave.initialize();
        _interleave.setCycles(_count);
        _alternate = new PNAlternate(myUniverse, "alternate");
        _alternate.initialize();
        _redirect0 = new PNRedirect(myUniverse, "redirect0");
        _redirect0.initialize(0);
        _redirect1 = new PNRedirect(myUniverse, "redirect1");
        _redirect1.initialize(1);

        //FIXME: Find a neat way of specifying the queue length of input port!
        //FIXME: Need a nice way of doing the following.
        //Maybe a nice method that set all star parameters and links all ports
        _queueX = new IORelation(myUniverse, "QX");
        PNPort port = (PNPort)_interleave.getPort("output");
        port.link(_queueX);
        port = (PNPort)_alternate.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueX);
 
        _queueY = new IORelation(myUniverse, "QY");
        port = (PNPort)_redirect0.getPort("output");
        port.link(_queueY);
        port = (PNPort)_interleave.getPort("input1");
        port.getQueue().setCapacity(1);
        port.link(_queueY);        
 
        _queueZ = new IORelation(PNUniverse, "QZ");
        port = (PNPort)_redirect1.getPort("output");
        port.link(_queueZ);
        port = (PNPort)_interleave.getPort("input2");
        port.getQueue().setCapacity(1);
        port.link(_queueZ);
 
        _queueT1 = new IORelation(myUniverse, "QT1");
        port = (PNPort)_alternate.getPort("output1");
        port.link(_queueT1);
        port = (PNPort)_redirect0.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(_queueT1);
 
        _queueT2 = new IORelation(myUniverse, "QT2");
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

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
 
    private PNAlternate _alternate;
    private PNInterleave _interleave;
    private PNRedirect _redirect0;
    private PNRedirect _redirect1;
    private IORelation _queueX;
    private IORelation _queueY;
    private IORelation _queueZ;
    private IORelation _queueT1;
    private IORelation _queueT2;

}
   

