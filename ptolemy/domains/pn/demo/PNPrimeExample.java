/* This creates an example implementing Sieve of Erasthenes

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
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNPrimeExample
/** 
This is currently a Universe containing some PNStars. This might not support
hierarchy currently.
@author  Mudit Goel
@version $Id$
*/
public class PNPrimeExample {

    public static void main(String args[]) throws 
            IllegalStateException, IllegalActionException, 
            NameDuplicationException {
        PNUniverse myUniverse = new PNUniverse();
        myUniverse.setMode(Integer.parseInt(args[0]));
        myUniverse.setNoCycles(Integer.parseInt(args[1]));
        PNRamp ramp = new PNRamp(myUniverse, "ramp");
        ramp.initialize(2);
        ramp.setCycles(Integer.parseInt(args[1]));
        PNSieve sieve = new PNSieve(myUniverse, "2_sieve");
        sieve.initialize(2);
        IORelation queue = new IORelation(myUniverse, "2_queue");
        PNPort port = (PNPort)sieve.getPort("input");
        port.getQueue().setCapacity(1);
        port.link(queue);
        port = (PNPort)ramp.getPort("output");
        port.link(queue);

        //FIXME: Should I use connect() rather than all the above stuff??
        myUniverse.execute();
        System.out.println("Bye World\n");
        return;
    }
}
   





