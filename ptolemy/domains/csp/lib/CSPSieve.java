/* This constructs a sequence of prime numbers based on Sieve of Erathsenes

 Copyright (c) 1997-1998 The Regents of the University of California.
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

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PNSieve
/** 
@author Neil Smyth
@version @$Id$
*/
public class CSPSieve extends CSPActor {
    
    /** Calls the super class constructor and creates the neccessary ports.
     * @exception NameDuplicationException is thrown if more than one port 
     *  with the same name is added to the star
     */
    public CSPSieve(CompositeActor container, String name, int prime)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _prime = prime;
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
        int lastSeen = 0;
        int limit = 100;
	while (true) {
	    //System.out.println("Sieve getting data");
	    data = _input.get(0);
            lastSeen = ((IntToken)data).intValue();
	    //System.out.println("Sieve gotten data");
	    if (lastSeen % _prime != 0) {
		// is it the next prime? 
		if (islargestprime) {
		    System.out.println(getName() + "Queuing Topology Change.");
		    // yes - make the topologyChange for it 
		    TopologyChangeRequest t = _makeChangeRequest(lastSeen);
		    // Queue the new TopologyChangeRequest
		    getDirector().queueTopologyChangeRequest(t);
                    delay();
		    System.out.println(getName() +":Queued TopologyChange");
		    islargestprime = false;	    
		} 
		else {
		    _output.send(0, data);
                }
	    }
	}
    }

    /** Get the input port for this actor.
     */
    public IOPort getInputPort() {
        return _input;
    }

    /** Create and return a new TopologyChangeRequest object that 
     *  adds a new sieve.
     *  @param value The prime the new filter should sieve.
     */
    private TopologyChangeRequest _makeChangeRequest(final int value) {
        TopologyChangeRequest request = new TopologyChangeRequest(this) {

            public void constructEventQueue() {
                CompositeActor container =  (CompositeActor)getContainer();
                CSPSieve newSieve = null;
                ComponentRelation newRel = null;
                try {
                    newSieve = new CSPSieve(container,value + "_sieve", value);

                    // If we use a 1-1 relation this needs to change.
                    newRel = new ComponentRelation(container, "R" + value);

                } catch (NameDuplicationException ex) {
                    throw new InvalidStateException("Cannot create " +
                            "new sieve.");
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException("Cannot create " +
                            "new sieve.");
                }
        
                queueEntityAddedEvent(container, newSieve);
                queueRelationAddedEvent(container, newRel);
                queuePortLinkedEvent(newRel, _output);
                queuePortLinkedEvent(newRel, newSieve.getInputPort());
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

    // The prime this sieve is filtering out.
    private int _prime;
}


