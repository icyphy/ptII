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
import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PNGalaxySieve
/**
@author Mudit Goel
@version $Id$
*/
public class PNGalaxySieve extends AtomicActor {

    /** Constructor  Adds port
     * @exception NameDuplicationException If more than one port
     *  with the same name is added to the star
     */
    public PNGalaxySieve(CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        _output = new IOPort(this, "output", false, true);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one Token from it's input port and writes this token to
     *  it's output ports. Needs to read one token for every output
     *  port.
     */
    public void fire() throws IllegalActionException {
        Token data;
        boolean islargestprime = true;
	while (true) {
	    //System.out.println("Sieve getting data");
	    data = _input.get(0);
	    //System.out.println("Sieve gotten data");
	    if (((IntToken)data).intValue()%_prime != 0) {
		// is it the next prime?
		if (islargestprime) {
		    //System.out.println("Making mutations");
		    // yes - make the mutation for it
		    ChangeRequest m =
                        makeMutation(((IntToken)data).intValue());
		    BasePNDirector director = (BasePNDirector)getDirector();
		    // Queue the new mutation
		    director.requestChange(m);
		    //System.out.println("Queued mutation");
		    islargestprime = false;
		}
		else {
		    _output.broadcast(data);
		    //System.out.println("broadcasting data "+data.stringValue());
		}
	    }
	}
    }

    public void setParam(String name, String valueString)
            throws IllegalActionException {
        if (name.equals("prime")) {
	    IntToken token = new IntToken(valueString);
            _prime = token.intValue();
	    System.out.println("New prime discovered. "+valueString);
        } else {
            throw new IllegalActionException("Unknown parameter: " + name);
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Create and return a new mutation object that adds a new sieve.
     */
    private ChangeRequest makeMutation(final int value) {
        ChangeRequest request = new ChangeRequest(this, "") {

            public void execute() {
                //System.out.println("TopologyRequest event " +
                // "q being constructed!");

                // remember this
		LinkedList listofrels = new LinkedList();

                CompositeActor container =  (CompositeActor)getContainer();
                try {
		    CompositeActor galaxy =
                        new CompositeActor(container, value+"_gal");
		    IOPort galin = (IOPort)galaxy.newPort(value+"_in");
		    IOPort galout = (IOPort)galaxy.newPort(value+"_out");

                    PNGalaxySieve newSieve =
                        new PNGalaxySieve(galaxy, value + "_sieve");
                    newSieve.setParam("prime", Integer.toString(value));

		    //relations = _output.linkedRelations();
		    Iterator rels = _output.linkedRelationList().iterator();
		    while (rels.hasNext()) {
			listofrels.add(rels.next());
		    }
		    rels = listofrels.iterator();
		    while (rels.hasNext()) {
			Relation relation = (Relation)rels.next();
			//listofrels.insertLast(relation);
			//Disconnected
			_output.unlink(relation);
			//Connect PLotter again
			galout.link(relation);
		    }
		    IOPort outport = (IOPort)newSieve.getPort("output");
		    Relation newout =
                        galaxy.connect(galout, outport, value+"outgal");
		    IOPort input = (IOPort)newSieve.getPort("input");
		    Relation newin =
                        galaxy.connect(input, galin, value+"ingal");
		    //_output = new PNOutPort(PNSieve.this, "output");
		    Relation newRelation =
                        container.connect(galin, _output, value+"_queue");
                } catch (NameDuplicationException ex) {
                    throw new InvalidStateException("Cannot create " +
                            "new sieve.");
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException("Cannot create " +
                            "new sieve.");
                }

		// queueEntityAddedEvent(container, galaxy);
                // 		queuePortAddedEvent(galaxy, galin);
                // 		queuePortAddedEvent(galaxy, galout);
                //          queueEntityAddedEvent(galaxy, newSieve);
                // 		Enumeration relations = listofrels.elements();
                //          while (relations.hasMoreElements()) {
                // 		    Relation relation =
                //                       (Relation)relations.nextElement();
                //              queuePortUnlinkedEvent(relation, _output);
                //              queuePortLinkedEvent(relation, galout);
                //          }
                // 		queueRelationAddedEvent(galaxy,
                //                              (ComponentRelation)newin);
                // 		queueRelationAddedEvent(galaxy,
                //                              (ComponentRelation)newout);
                // 		queuePortLinkedEvent(newout, galout);
                // 		queuePortLinkedEvent(newout, outport);
                // 		queuePortLinkedEvent(newin, input);
                // 		queuePortLinkedEvent(newin, galin);
                //          //FIXME: This cast should not be required.
                //          // Mention it to johnr
                //          queueRelationAddedEvent(container,
                //                    (ComponentRelation)newRelation);
                //          queuePortLinkedEvent(newRelation, _output);
                //          queuePortLinkedEvent(newRelation, galin);
            }
        };
        return request;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The input port */
    private IOPort _input;
    /* The output port */
    private IOPort _output;
    private int _prime;
}
