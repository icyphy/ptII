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

import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;

//////////////////////////////////////////////////////////////////////////
//// CSPPrimeExample
/** 
This creates an example implementing Sieve of Eratosthenes. It has 
no hierarchy.
<p>
@author Neil Smyth, adapted from a file by Mudit Goel
@version $Id$
*/
public class CSPPrimeExample {

    public static void main(String args[]) throws 
            IllegalStateException, IllegalActionException, 
            NameDuplicationException {
        CompositeActor univ = new CompositeActor();
        univ.setName("Prime_example");
	Manager manager = new Manager("Manager");

	univ.setManager(manager);
	CSPDirector local = new CSPDirector("Local CSPDirector");
	univ.setDirector(local);

        CSPSource source = new CSPSource(univ, "source", 50, 2);
        CSPSieve sieve = new CSPSieve(univ, "2_sieve", 2);

        IOPort portin = (IOPort)sieve.getInputPort();
        IOPort portout = (IOPort)source.output;
        univ.connect(portin, portout, "2_queue");

	//CSPSink sink = new CSPSink(univ, "sink");
	//portout = (IOPort)sieve.getOutputPort();
        //portin = (IOPort)sink.getInputPort();
	//univ.connect(portin, portout, "plot_queue");

        
        //System.out.println(univ.description(pt.kernel.Nameable.LIST_PRETTYPRINT));

        manager.blockingGo();
        System.out.println(univ.getName() + "Finished\n");
        return;
    }
}
