/* This demo implements the  Sieve of Eratosthenes algorithm.

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
//// Eratosthenes
/**
This demo implements the Sieve of Eratosthenes algorithm for obtaining 
prime numbers. It uses changes to the topology and illustrates this working 
in the CSP domain.
<p>
The demo originally consists of a source generating integers, and one 
sieve filtering out all multiples of two. When the end sieve sees a 
number that it cannot filter, it creates a new sieve to filter out all 
multiplies of that number. Thus after the sieve filtering out the number 
two sees the number three, it creates a new sieve that filters out the 
number three. This then continues with the three sieve eventually 
creating a sieve to filter out all multiples of five, and so on. Thus 
after a while there will be a chain of sieves each filtering out a 
different prime number. If any number passes through all the sieves 
and reaches the end with no sieve waiting, it must be another prime 
and so a new sieve is created for it.
<p>
@author Neil Smyth, adapted from a file by Mudit Goel
@version $Id$
*/
public class Eratosthenes {

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

        IOPort portin = (IOPort)sieve.getPort("input");
        IOPort portout = (IOPort)source.getPort("output");
        univ.connect(portin, portout, "2_queue");

	//CSPSink sink = new CSPSink(univ, "sink");
	//portout = (IOPort)sieve.getPort("output");
        //portin = (IOPort)sink.getPort("input");
	//univ.connect(portin, portout, "plot_queue");


        //System.out.println(univ.description(pt.kernel.Nameable.LIST_PRETTYPRINT));

        manager.run();
        System.out.println(univ.getName() + "Finished\n");
        return;
    }
}
