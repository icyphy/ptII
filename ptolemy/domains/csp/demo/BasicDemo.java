/* A  two actor simulation executing a simple get and put rendezvous.

 Copyright (c) 1998-1999 The Regents of the University of California.
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


import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.domains.csp.kernel.*;
import ptolemy.domains.csp.lib.*;


//////////////////////////////////////////////////////////////////////////
//// BasicDemo
/**
This demo illustrates basic rendezvous in the CSP domain. The actors 
used here could be any polymorphic actors that wish to send and 
receive. I use the classes below only on an interim basis until the 
polymorphic libraries issues are fully resolved. the source and sink 
fire for a limited number of times after which they terminate.
<p>
Source - Sink
<p>
@author Neil Smyth
@version $Id$
@see ptolemy.domains.csp.kernel.CSPReceiver
@see ptolemy.domains.csp.kernel.CSPDirector
*/
public class BasicDemo {
    /** Nothing to be done.
     */
    public BasicDemo() {}

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct the topology used in this demo.
     *  Here a single level of hierarchy is used, with the 
     *  director controlling the CompositeActor being an 
     *  instance of CSPDirector. Thus the execution of the model 
     *  follows CSP semantics.
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        try {
            CompositeActor univ = new CompositeActor();
            univ.setName( "Universe");
            Manager manager = new Manager("Manager");
            CSPDirector localdir = new CSPDirector("CSPDirector");
            univ.setManager(manager);
            univ.setDirector(localdir);

	    CSPSource source = new CSPSource(univ, "Source");
            CSPSink sink = new CSPSink(univ, "Sink");

            IOPort out = source.output;
            IOPort in = sink.input;

            IORelation rel = (IORelation)univ.connect(out, in, "R1");
            System.out.println(univ.getFullName() + " starting!");
            univ.getManager().startRun();
        } catch (Exception e) {
            System.out.println(e.getMessage() + ": " + e.getClass().getName());
            throw new InvalidStateException("hello" + e.getMessage());
        }
    }
}
