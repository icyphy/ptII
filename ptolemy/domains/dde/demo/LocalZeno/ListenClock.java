/* A clock source.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.dde.demo.LocalZeno;

import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.gui.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// ListenClock
/**
This actor produces a periodic signal, a generalized square wave
that sequences through <i>N</i> levels with arbitrary duty cycles
and period.  It has various uses.  Its simplest use in the DE domain
is to generate a sequence of events at regularly spaced
intervals.  In CT, it can be used to generate a square wave.
In both domains, however, it can also generate more intricate
waveforms that cycle through a set of values.
<p>
At the beginning of each time interval of length given by <i>period</i>,
it initiates a sequence of output events with values given by
<i>values</i> and offset into the period given by <i>offsets</i>.
These latter two parameters must both contain row vectors
(1 by <i>N</i> matrices) of the same length (<i>N</i>), or an
exception will be thrown by the fire() method.
The <i>offsets</i> array must be nondecreasing and nonnegative,
or an exception will be thrown when it is set.
Moreover, its largest entry must be smaller than <i>period</i>
or an exception will be thrown by the fire() method.
<p>
The <i>values</i> parameter by default
contains an IntMatrix with value [1, 0] (one row,
two columns, with values 1 and 0).  The default <i>offsets</i>
vector is [0.0, 1.0].  Thus, the default output will be
alternating 1 and 0 with 50% duty cycle.  The default period
is 2.0.
<p>
The actor uses the fireAt() method of the director to request
firing at the beginning of each period plus each of the offsets.
It may in addition fire at any time in response to a trigger
input.  On such firings, it simply repeats the most recent output.
Thus, the trigger, in effect, asks the actor what its current
output value is. Some domains, such as CT, may also fire the actor at
other times, without requiring a trigger input.  Again, the actor
simply repeats the previous output.
Thus, the output can be viewed as samples of the clock waveform,
where the time of each sample is the time of the firing that
produced it.  If the actor fires before the first offset has
been reached, then a zero token of the same type as those in
the <i>values</i> matrix is produced.
<p>
The clock waveform is a square wave (in the sense that transitions
between levels are discrete and signal is piecewise constant),
with <i>N</i> levels, where <i>N</i> is the length of the <i>values</i>
parameter.  Changes between levels occur at times
<i>nP</i> + <i>o<sub>i < /sub></i> where <i>n</i> is any nonnegative integer,
<i>P</i> is the period, and <i>o<sub>i < /sub></i> is an entry
in the <i>offsets</i> vector.
<p>
The type of the output can be any token type that has a corresponding
matrix token type.  The type is inferred from the type of the
<i>values</i> parameter.

@author Edward A. Lee
@version $Id$
*/

public class ListenClock extends Clock {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ListenClock(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** 
     */
    public void addListeners(ExecEventListener listener) {
        if( _listeners == null ) {
            _listeners = new LinkedList();
        }
        _listeners.insertLast(listener);
    }

    /**
     */
    public void generateEvents(ExecEvent event) {
        if( _listeners == null ) {
            return;
        }
        Enumeration enum = _listeners.elements();
        while( enum.hasMoreElements() ) {
            ExecEventListener newListener =
                    (ExecEventListener)enum.nextElement();
            newListener.stateChanged(event);
        }
    }

    /**
     */
    public boolean prefire() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 1 ) );
	try {
	    Thread.sleep(100);
	} catch(InterruptedException e) {
	    // FIXME
	}
	return super.prefire();
    }

    /**
     */
    public boolean postfire() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 2 ) );
	try {
	    Thread.sleep(100);
	} catch(InterruptedException e) {
	    // FIXME
	}
	return super.postfire();
    }

    /**
     */
    public void wrapup() throws IllegalActionException {
	generateEvents( new ExecEvent( this, 3 ) );
	super.wrapup();
    }

    /**
     */
    public void removeListeners(ExecEventListener listener) {
        if( _listeners == null ) {
            return;
        }
        _listeners.removeOneOf(listener);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                        private variables                       ////

    private LinkedList _listeners;

}
