/* Ptolemy II DE FIFO Queue Star

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

package ptolemy.domains.de.lib;

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;
import ptolemy.actor.util.*;

//////////////////////////////////////////////////////////////////////////
//// DEFIFOQueue
/**
Implements a first-in first-out (FIFO) queue with finite or infinite length.
Events on the "demand" input trigger a dequeue on the "outData" port if the
queue is not empty. If the queue is empty, then a "demand" event enables
the next future "inData" particle to pass immediately to "outData". The first
particle to arrive at "inData" is always passed directly to the output,
unless <i>numDemandsPending</i> is initialized to 0. If
<i>consolidateDemands</i> is set to TRUE (the default), then
<i>numDemandsPending</i> is not permitted to rise above one. The size of the
queue is sent to the <i>size</i> output whenever an "inData" or "demand"
event is processed. Input data that doesn't fit in the queue is sent to
the "overflow" output.

@author Lukito Muliadi
@version $Id$
@see Actor
*/
public class DEFIFOQueue extends TypedAtomicActor {
    /** Construct a FIFOQueue star.
     *
     * @param value The initial output event value.
     * @param step The step size by which to increase the output event values.
     * @param container The composite actor that this actor belongs too.
     * @param name The name of this actor.
     *
     * @exception NameDuplicationException Other star already had this name
     * @exception IllegalActionException internal problem
     */
    public DEFIFOQueue(TypedCompositeActor container,
            String name,
            int numDemandsPending,
	    boolean consolidateDemands,
	    int capacity)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        // create an output port
        outData = new TypedIOPort(this, "outData", false, true);
        outData.setDeclaredType(DoubleToken.class);
	overflow = new TypedIOPort(this, "overflow", false, true);
        overflow.setDeclaredType(DoubleToken.class);
	queueSize = new TypedIOPort(this, "queueSize", false, true);
        queueSize.setDeclaredType(DoubleToken.class);

        // create input ports
        inData = new TypedIOPort(this, "inData", true, false);
        inData.setDeclaredType(DoubleToken.class);
        demand = new TypedIOPort(this, "demand", true, false);
        demand.setDeclaredType(DoubleToken.class);

	// set up the parameter

        _initialNumDemandsPending = numDemandsPending;
	_consolidateDemands = consolidateDemands;
	_capacity = capacity;
	_queue.setCapacity(_capacity);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** If there's an event in the clock input port then produce an event,
     *  otherwise just record the value of the input port.
     *
     * @exception IllegalActionException Not thrown in this class.
     */
    public void fire() throws IllegalActionException{

	boolean bugFree = false;

        // Check if there's an event in the inData input
        if (inData.hasToken(0)) {
	    bugFree = true;
	    Token inDataToken = null;
            inDataToken = inData.get(0);

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
            demandToken = demand.get(0);

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
                    "FIFOQueueActor");
	}
    }

    /** FIXME:Describe me!
     *
     *  @exception IllegalActionException Thrown if could not create the
     *   receivers.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // empty the FIFO queue
        while (_queue.size() > 0) {
            _queue.take();
        }
        _numDemandsPending = _initialNumDemandsPending;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // the ports.
    public TypedIOPort outData;
    public TypedIOPort inData;
    public TypedIOPort overflow;
    public TypedIOPort demand;
    public TypedIOPort queueSize;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ptolemy.actor.util.FIFOQueue _queue = new ptolemy.actor.util.FIFOQueue();
    private int _numDemandsPending = 0;
    private int _capacity;
    private boolean _consolidateDemands = true;
    private int _initialNumDemandsPending = 0;
}
