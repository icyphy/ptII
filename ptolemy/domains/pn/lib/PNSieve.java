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

package pt.domains.pn.stars;
import pt.domains.pn.kernel.*;
import pt.kernel.*;
import pt.actors.*;
import pt.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNSieve
/** 
@author Mudit Goel
@version $Id$
*/
public class PNSieve extends PNActor {
    
    /** Constructor  Adds port   
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     */
    public PNSieve(CompositeActor container, String name)
            throws NameDuplicationException {
        super(container, name);
        _input = newInPort(this, "input");
        //_output = null;
        _output = newOutPort(this, "output");
    }
    

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Initializes the Star. Should be called before execution
     * @param prime is the prime for this sieve
     */	
    public void setInitState(int prime) {
        _prime = prime;
        System.out.println("Next Prime is "+ _prime);
    }
    
    /** Reads one Token from it's input port and writes this token to 
     *  it's output ports. Needs to read one token for every output
     *  port. 
     */
    public void run() {
        Token[] data;
        boolean islargestprime = true;
        try {
            writeTo(_output, new IntToken(_prime));
            for (int i=0; _noOfCycles < 0 || i < _noOfCycles; i++) {
	      Enumeration relations = _input.linkedRelations();
	      while (relations.hasMoreElements()) {
		  IORelation rel = (IORelation)relations.nextElement();
		  data = readFrom(_input, rel);
		  if (((IntToken)data[0]).intValue()%_prime != 0) {
		      // is it the next prime? 
		      if (islargestprime) {

			  // yes - make the mutation for it 
                          Mutation m = makeMutation(((IntToken)data[0]).intValue());

                          PNDirector director = (PNDirector)getDirector();

                          // Queue the new mutation
                          director.queueMutation(m);
                          islargestprime = false;

                          // In PN, we should process the mutations NOW
                          director.processPendingMutations();

                          // In PN, we notify the director so that it
                          // schedules the new actor threads
                          director.startNewActors();

                      } 
                      else {
                          writeTo(_output, data[0]);
                      }
                  }
              }
            }
            ((PNDirector)getDirector()).processStopped();
        } catch (NoSuchItemException e) {
            System.out.println("Terminating "+ this.getName());
            return;
        } 
    }
    
    public void setParam(String name, double value)
        throws IllegalActionException {
        if (name.equals("prime")) {
            _prime = (int) value;
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
            PNInPort input = null;
            PNOutPort output = null;
            PNOutPort outport = null;
            
            // Create the mutation
            public void perform() {
                try {
                    CompositeActor container = (CompositeActor)PNSieve.this.getContainer();

                    newSieve = new PNSieve(container, value + "_sieve");
                    newSieve.setInitState(value);
                    
                    //Disconnecting the plotter and attaching it to the output
                    //of the new Sieve
                    Enumeration relations = _output.linkedRelations();
                    relation = (Relation)relations.nextElement();
                    //Disconnected
                    _output.unlink(relation);
                    //Connect PLotter again
                    outport = (PNOutPort)newSieve.getPort("output");
                    outport.link(relation);

                    //Connect newsieve
                    input = (PNInPort)newSieve.getPort("input");
                    //_output = new PNOutPort(PNSieve.this, "output");
                    newRelation = container.connect(input, _output, value+"_queue");
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
                listener.unlink(relation, _output);
                listener.addEntity(container, newSieve);
                listener.link(relation, outport);
                listener.addRelation(container, newRelation);
                listener.link(newRelation, input);
                listener.link(newRelation, _output);
                listener.done();
            }
        };

        return m;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    
    /* The input port */
    private PNInPort _input;
    /* The output port */
    private PNOutPort _output;
    private int _prime;
}


