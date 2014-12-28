/* A composite where events can occur inside between the times when
   the outside model invokes the composite.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.SuperdenseTime;
import ptolemy.actor.util.Time;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// CatchUpComposite

/**
This composite allows events to occur in the inside model
between the times when the outside model invokes this composite.
The inside model is required to have a director.
<p>
When this composite actor is fired, it fires the
inside model to process any unprocessed events it may have
at earlier times than the environment time of the firing.
If in any of these "catch up" firings the inside model
attempts to produce outputs, this director will throw an
exception. This director then transfers any available
inputs to the inside model and fires the inside model
one more time. On this "caught up" firing, the inside
model is permitted to produce outputs.
<p>
Note that a firing of this composite results in one
or more complete iterations of the inside model, including
invocations of postfire. Thus, this actor changes state
in the fire() method, and hence only implements the
weak actor semantics. Therefore, it should not be used
inside domains that require a strict actor semantics,
such as Continuous, which requires that fire() not
change state (so that backtracking can occur).
This composite probably can be used in SR, where it
will be treated as a strict as actor and hence will be
fired only once per iteration, and only when all inputs
are known.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 11.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class CatchUpComposite extends MirrorComposite {
    
    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CatchUpComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setClassName("ptolemy.actor.lib.hoc.CatchUpComposite");

        // Create the CatchUpDirector in the proper workspace.
        _director = new CatchUpDirector(workspace());
        _director.setContainer(this);
        _director.setName("CatchUpDirector");
        
        // Create the composite actor for the contents.
        _contents = new MirrorComposite.MirrorCompositeContents(this, "Contents");
        
        // Override the default icon.
        _attachText("_iconDescription", _defaultIcon);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This overrides
     *  the base class to instantiate a new CatchUpDirector.
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CatchUpComposite result = (CatchUpComposite) super.clone(workspace);
        try {
            // Remove the old inner CatchUpDirector(s) that is(are) in the wrong workspace.
            // FIXME: Is this really needed? Following IterateOverArray.
            String catchUpDirectorName = null;
            Iterator catchUpDirectors = result.attributeList(
                    CatchUpDirector.class).iterator();
            while (catchUpDirectors.hasNext()) {
                CatchUpDirector oldCatchUpDirector = (CatchUpDirector) catchUpDirectors
                        .next();
                if (catchUpDirectorName == null) {
                    catchUpDirectorName = oldCatchUpDirector.getName();
                }
                oldCatchUpDirector.setContainer(null);
            }

            // Create a new CatchUpDirector that is in the right workspace.
            CatchUpDirector catchUpDirector = result.new CatchUpDirector(
                    workspace);
            catchUpDirector.setContainer(result);
            catchUpDirector.setName(catchUpDirectorName);
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        return result;
    }

    /** Fire any piggybacked actors and then delegate the firing to the director.
     *  This overrides the base class to not transfer inputs or outputs.
     *  Those operations are handled by the director.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    @Override
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Calling fire()");
        }

        try {
            _workspace.getReadAccess();

            // First invoke piggybacked methods.
            if (_piggybacks != null) {
                // Invoke the fire() method of each piggyback.
                for (Executable piggyback : _piggybacks) {
                    piggyback.fire();
                }
            }
            if (_derivedPiggybacks != null) {
                // Invoke the fire() method of each piggyback.
                for (Executable piggyback : _derivedPiggybacks) {
                    piggyback.fire();
                }
            }
            _director.fire();
        } finally {
            _workspace.doneReading();
        }

        if (_debugging) {
            _debug("Called fire()");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The contained composite actor. */
    private MirrorComposite.MirrorCompositeContents _contents;
    
    /** The default value icon.  This is static so that we avoid doing
     *  string concatenation each time we construct this object.
     */
    private static String _defaultIcon = "<svg>\n"
            + "<rect x=\"-30\" y=\"-20\" width=\"60\" "
            + "height=\"40\" style=\"fill:cyan\"/>\n"
            + "<rect x=\"-28\" y=\"-18\" width=\"56\" "
            + "height=\"36\" style=\"fill:lightgrey\"/>\n"
            + "<rect x=\"-15\" y=\"-10\" width=\"10\" height=\"8\" "
            + "style=\"fill:white\"/>\n"
            + "<rect x=\"-15\" y=\"2\" width=\"10\" height=\"8\" "
            + "style=\"fill:white\"/>\n"
            + "<rect x=\"5\" y=\"-4\" width=\"10\" height=\"8\" "
            + "style=\"fill:white\"/>\n"
            + "<line x1=\"-5\" y1=\"-6\" x2=\"0\" y2=\"-6\"/>"
            + "<line x1=\"-5\" y1=\"6\" x2=\"0\" y2=\"6\"/>"
            + "<line x1=\"0\" y1=\"-6\" x2=\"0\" y2=\"6\"/>"
            + "<line x1=\"0\" y1=\"0\" x2=\"5\" y2=\"0\"/>" + "</svg>\n";
    
    /** The director. */
    private CatchUpDirector _director;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// CatchUpDirector

    /** This is a specialized director that, when fired, fires the
     *  inside model to process any unprocessed events it may have
     *  at earlier times than the environment time of the firing.
     *  If in any of these "catch up" firings the inside model
     *  attempts to produce outputs, this director will throw an
     *  exception. This director then transfers any available
     *  inputs to the inside model and fires the inside model
     *  one more time. On this "caught up" firing, the inside
     *  model is permitted to produce outputs.
     */
    private class CatchUpDirector extends Director implements SuperdenseTimeDirector {
	
        /** Construct an CatchUpDirector in the specified workspace with
         *  no container and an empty string as a name.
         *  @param workspace The workspace.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public CatchUpDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
            setPersistent(false);
        }

        /** Clone the object into the specified workspace.
         *  @param workspace The workspace for the new object.
         *  @return A new NamedObj.
         *  @exception CloneNotSupportedException If any of the attributes
         *   cannot be cloned.
         *  @see #exportMoML(Writer, int, String)
         */
        @Override
        public Object clone(Workspace workspace) throws CloneNotSupportedException {
            CatchUpDirector result = (CatchUpDirector) super.clone(workspace);
            result._pendingFiringTimes = null;
            return result;
        }

        /** Invoke one or more iterations of the contained actor of the
         *  container of this director, allowing it to process any internal
         *  events that it may have at earlier times than the current time
         *  of the enclosing model.  That is, perform zero or more
         *  "catch up" iterations of the contained actor, where the contained
         *  actor will see an earlier time than the current time of
         *  the enclosing model, followed by exactly one "caught up"
         *  iteration.  The inputs of this composite will be visible to
         *  the inside model only on the "caught up" iteration.
         *  @exception IllegalActionException If any called method of
         *   of the contained actor throws it, or if the contained
         *   actor is not opaque.
         */
        @Override
        public void fire() throws IllegalActionException {
            // Don't call "super.fire();" here, this actor contains its
            // own director.
            CompositeActor container = (CompositeActor) getContainer();
            _postfireReturns = true;
            
            Director enclosingDirector = container.getExecutiveDirector();
            if (enclosingDirector == null) {
        	throw new IllegalActionException(container, "No enclosing director!");
            }
            int microstep = 1;
            if (enclosingDirector instanceof SuperdenseTimeDirector) {
        	microstep = ((SuperdenseTimeDirector)enclosingDirector).getIndex();
            }
            SuperdenseTime environmentTime = new SuperdenseTime(getModelTime(), microstep);
                        
            // Perform catch up iterations, if necessary.
            if (_pendingFiringTimes != null) {
        	SuperdenseTime firstPendingFiringTime = _pendingFiringTimes.peek();
        	while (firstPendingFiringTime != null) {
        	    int comparison = firstPendingFiringTime.compareTo(environmentTime);
        	    if (comparison < 0) {
        		// Catch up iteration is needed.
        		_pendingFiringTimes.poll();

        		// Perform catch up firing.
        		try {
        		    _catchUpTime = firstPendingFiringTime.timestamp();
        		    _microstep = firstPendingFiringTime.index();
        		    
                            if (!_fireContents()) {
                        	// Postfire returned false.
                        	break;
                            }
                            // If any output port has tokens, this is an error.
                            List<IOPort> ports = outputPortList();
                            for (IOPort port: ports) {
                        	for (int i = 0; i < port.getWidth(); i++) {
                        	    if (port.hasTokenInside(i)) {
                        		throw new IllegalActionException(port,
                        			"Illegal output during catch up iteration. "
                        			+ "Composite actor is attempting to produce an output at time "
                        			+ _catchUpTime
                        			+ ", but environment time is past that at "
                        			+ environmentTime.timestamp());
                        	    }
                        	}
                            }
        		} finally {
        		    _catchUpTime = null;
        		}
        		
        		// In case another catch up iteration is needed:
        		firstPendingFiringTime = _pendingFiringTimes.peek();
        	    } else if (comparison == 0) {
        		// Caught up firing will satisfy the request.
        		_pendingFiringTimes.poll();
        		break;
        	    } else {
        		// Pending request is in the future.
        		// Proceed directly to caught up firing.
        		break;
        	    }
        	}
            }
        	
            // Perform caught up iteration only if no catch iteration has returned
            // false from postfire().
            if (_postfireReturns == true) {
        	_microstep = microstep;
        	// Transfer inputs.
        	for (Iterator<?> inputPorts = inputPortList().iterator(); inputPorts
        		.hasNext() && !_stopRequested;) {
        	    IOPort p = (IOPort) inputPorts.next();
        	    if (p instanceof ParameterPort) {
        		((ParameterPort) p).getParameter().update();
        	    } else {
        		transferInputs(p);
        	    }
        	}

        	if (!_stopRequested) {
        	    _fireContents();

        	    // Transfer outputs.
        	    if (!_stopRequested) {
        		transferOutputs();
        	    }
        	}
        	
        	// If there is a pending request in the future, delegate
        	// a fireAt() request.
                if (_pendingFiringTimes != null) {
                    SuperdenseTime firstPendingFiringTime = _pendingFiringTimes.peek();
                    if (firstPendingFiringTime != null
                	    && firstPendingFiringTime.compareTo(environmentTime) > 0) {
                	// First argument is ignored, so it can be null.
                	fireAt(null, firstPendingFiringTime.timestamp(), firstPendingFiringTime.index());
                    }
                }
            }
        }

        /** Request a firing of the given actor at the given model
         *  time.  This class delegates the request to the enclosing
         *  director. If the enclosing director returns the same
         *  requested time, then nothing further happens. If the
         *  enclosing director returns a later time, then this method
         *  records the requested time so that on the next firing
         *  a "catch up" iteration will be performed at that time.
         *  If the enclosing director returns an earlier time,
         *  then this method also records the requested time,
         *  and on the next firing, will again attempt to delegate
         *  to the container.
         *  @param actor The actor scheduled to be fired.
         *  @param time The requested time.
         *  @return An instance of Time with the current time value, or
         *   if there is an executive director, the time at which the
         *   container of this director will next be fired
         *   in response to this request.
         *  @see #fireAtCurrentTime(Actor)
         *  @exception IllegalActionException If there is an executive director
         *   and it throws it. Derived classes may choose to throw this
         *   exception for other reasons.
         */
        public Time fireAt(Actor actor, Time time) throws IllegalActionException {
            // Unless the actor specifically requests a particular microstep,
            // we assume it knows nothing about microsteps. We use microstep 1
            // as the default, since this is the default for discrete events.
            // The Continuous domain will specifically request a firing at
            // microstep 0.
            return fireAt(actor, time, 1);
        }

        /** Request a firing of the given actor at the given model
         *  time with the given microstep. This method behaves exactly
         *  like {@link #fireAt(Actor, Time)}, except that it also
         *  passes up to the executive director the microstep, if there
         *  is one.
         *  @param actor The actor scheduled to be fired.
         *  @param time The requested time.
         *  @param microstep The requested microstep.
         *  @return An instance of Time with the current time value, or
         *   if there is an executive director, the time at which the
         *   container of this director will next be fired
         *   in response to this request.
         *  @see #fireAtCurrentTime(Actor)
         *  @see #fireContainerAt(Time)
         *  @exception IllegalActionException If there is an executive director
         *   and it throws it. Derived classes may choose to throw this
         *   exception for other reasons.
         */
        public Time fireAt(Actor actor, Time time, int microstep)
                throws IllegalActionException {
            if (_debugging) {
                _debug("**** Requesting that enclosing director refire me at "
                        + time + " with microstep " + microstep);
            }
            // Translate the local time into an environment time.
            Time environmentTime = localClock
                    .getEnvironmentTimeForLocalTime(time);
            Director director = getExecutiveDirector();

            Time response = director.fireAt(CatchUpComposite.this, environmentTime,
                    microstep);

            int comparison = response.compareTo(time);
            if (comparison == 0) {
        	return time;
            } else {
        	if (_pendingFiringTimes == null) {
        	    _pendingFiringTimes = new PriorityQueue<SuperdenseTime>();
        	}
        	_pendingFiringTimes.add(new SuperdenseTime(time, microstep));
        	return time;
            }
        }

        /** Return a superdense time index for the current time,
         *  where the index is equal to the microstep.
         *  @return A superdense time index.
         *  @see #setIndex(int)
         *  @see ptolemy.actor.SuperdenseTimeDirector
         */
        @Override
        public int getIndex() {
            return _microstep;
        }

        /** If a "catch up" iteration is in progress, then return the
         *  time that the inside model should see. Otherwise, return
         *  the environment time.
         *  @return The current time.
         *  @see #setModelTime(Time)
         */
        public Time getModelTime() {
            if (_catchUpTime == null) {
        	return super.getModelTime();
            }
            return _catchUpTime;
        }

        /** Initialize contained actors
         *  @exception IllegalActionException If the initialize() method of
         *   the super class throws it.
         */
        @Override
        public void initialize() throws IllegalActionException {
            // Initialize the microstep to zero, even though
            // discrete models normally want to run with microstep 1 or higher.
            // During initialization, some contained actors will request
            // firings. One of those might be a Continuous subsystem,
            // which will explicitly request a firing at microstep 0.
            // Others will have their requests automatically set
            // to microstep 1. Thus, with normal discrete models,
            // the only events in the event queue after initialization
            // will all have microstep 1, and hence that is where the
            // simulation will start.
            _microstep = 0;
            // This could be getting re-initialized during execution
            // (e.g., if we are inside a modal model), in which case,
            // if the enclosing director is a superdense time director,
            // we should initialize to its microstep, not to our own.
            // NOTE: Some (weird) directors pretend they are not embedded even
            // if they are (e.g. in Ptides), so we call _isEmbedded() to give
            // the subclass the option of pretending it is not embedded.
            if (isEmbedded()) {
        	Director executiveDirector = getExecutiveDirector();
        	if (executiveDirector instanceof SuperdenseTimeDirector) {
        	    _microstep = ((SuperdenseTimeDirector) executiveDirector).getIndex();
                }
            }
            super.initialize();
        }
        /** Return a new instance of QueueReceiver.
         *  We use a QueueReceiver rather than the base class's MailboxReceiver
         *  so that more than one token can be transferred to the inside
         *  model on each firing.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        @Override
        public Receiver newReceiver() {
            return new QueueReceiver();
        }

        /** Override the base class to return the logical AND of
         *  what the base class postfire() method returns and the
         *  flag set in fire().
         */
        @Override
        public boolean postfire() throws IllegalActionException {
            boolean superReturns = super.postfire();
            return superReturns && _postfireReturns;
        }

        /** Set the superdense time index. This should only be
         *  called by an enclosing director.
         *  @exception IllegalActionException Not thrown in this base class.
         *  @see #getIndex()
         *  @see ptolemy.actor.SuperdenseTimeDirector
         */
        @Override
        public void setIndex(int index) throws IllegalActionException {
            if (_debugging) {
                _debug("Setting superdense time index to " + index);
            }
            _microstep = index;
        }

        //////////////////////////////////////////////////////////////
        ////                   private methods                    ////

	/** Iterate the contents composite.
	 *  @throws IllegalActionException If the composite throws it.
	 *  @return false if postfire() returns false.
	 */
	private boolean _fireContents() throws IllegalActionException {
	    if (_debugging) {
	        _debug(new FiringEvent(this, _contents, FiringEvent.BEFORE_ITERATE, 1));
	    }
	    if (_contents.iterate(1) == Executable.STOP_ITERATING) {
		_postfireReturns = false;
	    }
	    if (_debugging) {
	        _debug(new FiringEvent(this, _contents, FiringEvent.AFTER_ITERATE, 1));
	    }
	    return _postfireReturns;
	}

        //////////////////////////////////////////////////////////////
        ////                   private variables                  ////
        
        /** The time that the inside model should see as the current time
         *  during a "catch up" iteration, or null if there is no catch
         *  up iteration in progress.
         */
        private Time _catchUpTime;
        
        /** The current microstep. */
        protected int _microstep = 1;

        /** A sorted list of times of pending fireAt() requests. */
        private PriorityQueue<SuperdenseTime> _pendingFiringTimes;
        
        /** Indicator that the inside model returned false in postfire. */
        private boolean _postfireReturns = true;
    }
}
