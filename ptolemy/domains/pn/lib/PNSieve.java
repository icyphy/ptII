/* This constructs a sequence of prime numbers based on Sieve of Erathsenes

 Copyright (c) 1997- The Regents of the University of California.
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

package ptolemy.domains.pn.lib;
import ptolemy.domains.pn.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.mutation.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
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
		    Mutation m = makeMutation(((IntToken)data).intValue());
		    PNDirector director = (PNDirector)getDirector();
		    // Queue the new mutation
		    director.queueMutation(m);
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

    /** Create and return a new mutation object that adds a new sieve.
     */
    private Mutation makeMutation(final int value) {
        Mutation m = new Mutation() {
            // remember this
            PNSieve newSieve = null;
            Relation newRelation = null;
            Relation relation = null;
            IOPort input = null;
            IOPort output = null;
            IOPort outport = null;
            
            // Create the mutation
            public void perform() {
                try {
		    //System.out.println("In perform");
                    CompositeActor container = (CompositeActor)PNSieve.this.getContainer();
		    //System.out.println(Integer.toString(value));
		    //System.out.println("proceeding");
                    newSieve = new PNSieve(container, Integer.toString(value) + "_sieve");
		    //System.out.println("Created new seive");
                    newSieve.setParam("prime", Integer.toString(value));
                    //System.out.println("Created new seive and set param");
                    //Disconnecting the plotter and attaching it to the output
                    //of the new Sieve
                    Enumeration relations = _output.linkedRelations();
		    if (relations.hasMoreElements()) {
			relation = (Relation)relations.nextElement();
			//Disconnected
			_output.unlink(relation);
			//Connect PLotter again
			outport = (IOPort)newSieve.getPort("output");
			outport.link(relation);
			//Connect newsieve
		    }
		    input = (IOPort)newSieve.getPort("input");
		    //_output = new PNOutPort(PNSieve.this, "output");
		    newRelation = container.connect(input, _output, value+"_queue");
		    //}
                } catch (NameDuplicationException e) {
                    System.err.println("Exception: " + e.toString());
                    //This should never be thrown
                }  catch (IllegalActionException e) {
                    //This should never be thrown
                    System.err.println("Exception: " + e.toString());
                }
            }
            // Inform a listener about the mutation
            public void update(MutationListener listener) {
                CompositeActor container = (CompositeActor)PNSieve.this.getContainer();
		if (relation != null) {
		    listener.unlink(relation, _output);
		    listener.addEntity(container, newSieve);
		    listener.link(relation, outport);
		    listener.addRelation(container, newRelation);
		}
		listener.link(newRelation, input);
		listener.link(newRelation, _output);
		listener.done();
		//}
	    }
        };

        return m;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /* The input port */
    private IOPort _input;
    /* The output port */
    private IOPort _output;
    private int _prime;
}


