/* My first 'big' DE star.

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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;
import ptolemy.actor.util.*;

//////////////////////////////////////////////////////////////////////////
//// DEFIFOQueue
/** Describe me!

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEFIFOQueue extends AtomicActor {
    /** Construct a DEFIFOQueue star.
     *
     * @param value The initial output event value.
     * @param step The step size by which to increase the output event values.
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DEFIFOQueue(int numDemandsPending,
	    boolean consolidateDemands,
	    int capacity,
	    CompositeActor container,
            String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        outData = new IOPort(this, "outData", false, true);
	overflow = new IOPort(this, "overflow", false, true);
	queueSize = new IOPort(this, "queueSize", false, true);

        // create input ports
        inData = new IOPort(this, "inData", true, false);
        demand = new IOPort(this, "demand", true, false);

	// set up the parameter
	_numDemandsPending = numDemandsPending;
	_consolidateDemands = consolidateDemands;
	_capacity = capacity;
	_queue.setCapacity(_capacity);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the clock input port then produce an event,
     *  otherwise just record the value of the input port.
     *
     * @exception CloneNotSupportedException Error when cloning event.
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire()
            throws CloneNotSupportedException, IllegalActionException{
 
	boolean bugFree = false;

        // Check if there's an event in the inData input
        if (inData.hasToken(0)) {
	    bugFree = true;
	    Token inDataToken = null;
	    try {
		inDataToken = inData.get(0);
	    } catch (NoSuchItemException e) {
		throw new InvalidStateException("bug in DEFifoQueue.java (1)");
	    }
	    // check if the queue is full.
	    if (_queue.size() == _capacity) {
		// put the token into the overflow
		overflow.broadcast(inDataToken);
	    } else if (_numDemandsPending > 0) {
		outData.broadcast(inDataToken);
		_numDemandsPending--;
	    } else {
		//put the token into the queue.
		_queue.put(inDataToken);
	    }
	    queueSize.broadcast(new DoubleToken((double)_queue.size()));
	}

	// Check if there's an event in the demand input
	if (demand.hasToken(0)) {
	    bugFree = true;
	    Token demandToken = null;
	    try {
		demandToken = demand.get(0);
	    } catch (NoSuchItemException e) {
		throw new InvalidStateException("bug in DEFIFOQueue.java (2)");
	    }
	    // check if the queue is empty
	    if (_queue.size() == 0) {
		// queue is empty, so increment numDemandsPending.
		_numDemandsPending++;
		if (_consolidateDemands) {
		    _numDemandsPending = 1;
		}
	    } else {
		// queue is not empty
		outData.broadcast((Token)_queue.take());
	    }
	    queueSize.broadcast(new DoubleToken((double)_queue.size()));
	}

	if (!bugFree) {
	    throw new InvalidStateException("Bug in scheduler, look at "+
                    "DEFIFOQueue");
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private FIFOQueue _queue = new FIFOQueue();
    private int _numDemandsPending = 0;
    private int _capacity;
    private boolean _consolidateDemands = true;

    // the ports.
    public IOPort outData;
    public IOPort inData;
    public IOPort overflow;
    public IOPort demand;
    public IOPort queueSize;
}








