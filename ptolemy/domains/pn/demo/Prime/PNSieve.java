/* This constructs a sequence of prime numbers based on Sieve of Eratosthenes

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
*/

package ptolemy.domains.pn.demo.Prime;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNSieve
/**
@author Mudit Goel
@version $Id$
*/
public class PNSieve extends AtomicActor {

    /** Constructor  Adds port
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star
     */
    public PNSieve(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new IOPort(this, "input", true, false);
        output = new IOPort(this, "output", false, true);
        prime = new Parameter(this, "prime");
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one Token from it's input port and writes this token to
     *  it's output ports. Needs to read one token for every output
     *  port.
     */
    public void fire() throws IllegalActionException {
        int primevalue = ((IntToken)prime.getToken()).intValue();
        Token data;
        boolean islargestprime = true;
	while (true) {
	    data = input.get(0);
            int value = ((IntToken)data).intValue();
	    if (value%primevalue != 0) {
		// is it the next prime?
		if (islargestprime) {
		    // yes - make the mutation for it
		    ChangeRequest m = makeMutation(value);
                    System.out.println("Discovered next prime - It is " +
                            value);
		    BasePNDirector director = (BasePNDirector)getDirector();
		    // Queue the new mutation
		    director.requestChange(m);
		    //System.out.println("Queued mutation");
		    islargestprime = false;
		} else {
		    output.broadcast(data);
		}
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /* The input port */
    public IOPort input;
    /* The output port */
    public IOPort output;
    /** The parameter for primes */
    public Parameter prime;


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create and return a new mutation object that adds a new sieve.
     */
    private ChangeRequest makeMutation(final int value) {
        ChangeRequest request = new ChangeRequest(this, "Creating a new " +
                "sieve") {
            public void execute() {
                CompositeActor container =  (CompositeActor)getContainer();
                try {
                    PNSieve newSieve =
                        new PNSieve(container, value + "_sieve");
                    //queueEntityAddedEvent(container, newSieve);
                    Parameter prim = (Parameter)newSieve.getAttribute("prime");
                    prim.setToken(new IntToken(value));
                    Enumeration relations = output.linkedRelations();
		    if (relations.hasMoreElements()) {
			Relation relation = (Relation)relations.nextElement();
			//Disconnected
                        // queuePortUnlinkedEvent(relation, output);
                        output.unlink(relation);
			//Connect PLotter again
			IOPort outport = (IOPort)newSieve.getPort("output");
                        //queuePortLinkedEvent(relation, outport);
                        outport.link(relation);
		    }
		    IOPort inp = (IOPort)newSieve.getPort("input");
		    IORelation newRelation =
                        new IORelation(container, value+"_queue");
                    //newRelation.setName(value+"_queue");
                    //FIXME: This cast should not be required.
                    //Mention it to johnr
                    //queueRelationAddedEvent(container,
                    //(ComponentRelation)newRelation);

                    //queuePortLinkedEvent(newRelation, output);
                    output.link(newRelation);
                    //queuePortLinkedEvent(newRelation, inp);
                    inp.link(newRelation);

                } catch (NameDuplicationException ex) {
                    throw new InvalidStateException("Cannot create " +
                            "new sieve.");
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException("Cannot create " +
                            "new sieve.");
                }
            }
        };
        return request;
    }
}
