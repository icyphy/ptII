/* This constructs a sequence of prime numbers based on Sieve of Erathsenes

 Copyright (c) 1997-1999 The Regents of the University of California.
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
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// RadioController
/**
@author Mudit Goel
@version $Id$
*/
public class RadioController extends AtomicActor {

    /** Constructor  Adds port
     * @exception NameDuplicationException is thrown if more than one port
     *  with the same name is added to the star
     */
    public RadioController (CompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _input = new IOPort(this, "input", true, false);
        //_output = new IOPort(this, "output", false, true);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one Token from it's input port and writes this token to
     *  it's output ports. Needs to read one token for every output
     *  port.
     */
    public void fire() throws IllegalActionException {
	//System.out.println("Sieve getting data");
	DoubleToken data = (DoubleToken)_input.get(0);
	//System.out.println("Sieve gotten data");
	if (data.doubleValue() >= 0.95) {
	    // yes - make the mutation for it
	    TopologyChangeRequest m = makeMutation();
	    BasePNDirector director = (BasePNDirector)getDirector();
	    // Queue the new mutation
	    director.queueTopologyChangeRequest(m);
	    //System.out.println("Queued mutation");
	}
    }

    /** Return true. 
     */
    public boolean postfire() throws IllegalActionException {
	_count++;
	return true;
    }

    /** Sets the initial state of the actor 
     *  @param actor The actor to whose output ports the new created plotters
     *  are added.
     *  @param count The seed which acts as a prefix to create the names
     *  of the newly created plotters.
     */
    public void setStation(AtomicActor actor, int count) {
	_station = actor;
	_count = count;
	IOPort port = (IOPort)actor.getPort("output");
	_relation = (IORelation)port.linkedRelations().nextElement();
    }

    /** Create and return a new mutation object that adds a new sieve.
     */
    private TopologyChangeRequest makeMutation() {
        TopologyChangeRequest request = new TopologyChangeRequest(this) {
            public void constructEventQueue() {
                //System.out.println("TopologyRequest event q being constructed!");
                CompositeActor container =  (CompositeActor)getContainer();
                try {
                    PNPlot newplot = new PNPlot (container, _count + "_radio");
		    queueEntityAddedEvent(container, newplot);
		    IOPort radioport = newplot.getPort("input");
		    queuePortLinkedEvent(_relation, port);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /* The input port */
    private IOPort _input;
    //private IORelation _relation;
    /* The output port */
    //private IOPort _output;
    //private int _prime;
    private int _count;
    private AtomicActor _station;
    private IORelation _relation;
}


