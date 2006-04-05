/* Base class for directors that have fixed point semantics at each iteration.

 Copyright (c) 2000-2006 The Regents of the University of California.
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
package ptolemy.actor.sched;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// FixedPointDirector

/**
 A base class for directors that have fixed point semantics at each
 iteration. An iteration consists of repeated firings of the
 actors controlled by this director until a fixed point is reached.
 An iteration has converged if firing actors will not change signal 
 status any more. 
 <p>
 At the beginning of each iteration, this director resets the status of
 all signals to unknown. Upon firing an actor, the status of its output
 signals may become known.  Once the status of
 a signal becomes known, it cannot be changed back to unknown in the 
 iteration. This monotonicity constraint ensures the existence and
 uniqueness of the fixed point.
 During an iteration, the prefire() and fire() methods of the controlled 
 actors may be repeatedly invoked, but the postfire() method will be 
 invoked exactly once after the fixed point has been found.
 <p>
 Although this director does not require any specific ordering of actor 
 firings, a scheduler is used to choose an efficient ordering. 
 <p>
 By default, actors are <i>strict</i>, which means that all their
 input signals must be known before the actor can be fired. Such actors
 will be fired only once in an iteration. 
 
 An actor is considered <i>ready to fire</i> if sufficient known inputs are
 available.  In a sense, an actor firing is triggered by these known inputs,
 because the director only fires an actor if it is ready to fire.  Unless an
 actor contains an attribute called "_nonStrictMarker", it is assumed to be a
 strict actor, meaning that it requires all of its inputs to be known before
 it is fired.  This is very important since once an actor defines a particular
 output, it is not allowed to change that value in a subsequent firing in the
 course of the iteration.  A nonstrict actor can fire even if no inputs are
 known, and may fire any number of times in the course of an iteration.  Thus,
 a nonstrict actor can be used to produce an initial token in a cyclic graph.
 Since strict actors are only fired if all of their inputs are known, a given
 strict actor is only fired once in a given iteration.  An actor <i>has
 completed firing</i> if it has defined all of its outputs.
 <p>
 An actor is considered <i>allowed to fire</i> if its prefire()
 method has returned true.  An actor is considered <i>allowed to iterate</i>
 if its postfire() method has not returned false.
 <p>
 For an actor to be used under the control of , its prefire() method must be
 monotonic.  In other words, once the prefire() method of an actor returns
 true in a given iteration, this method must not return false if it were to be
 called again in the same iteration.  It is only possible for the number of
 known inputs of an actor to increase in a given iteration, so, for example,
 if the prefire() method of an actor returns true and then more inputs become
 known, the method must return true if it were to be called again.  If this
 were not the case, the behavior of the model would be nondeterministic since
 the execution results would depend on the order of the schedule.
 <p>
 This class is based on the original SRReceiver, written by Paul Whitaker.
 
 @author Haiyang Zheng and Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (hyzheng)
 @Pt.AcceptedRating Red (reviewModerator)
 */
public class FixedPointDirector extends StaticSchedulingDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FixedPointDirector() throws IllegalActionException, 
        NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public FixedPointDirector(Workspace workspace) throws 
        IllegalActionException, NameDuplicationException{
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *   with the specified container.
     *  @exception NameDuplicationException If the name collides with an
     *   attribute in the container.
     */
    public FixedPointDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The number of times that postfire may be called before it
     *  returns false. The type must be IntToken, and the value
     *  defaults to zero. If the value is less than or equal to zero,
     *  then the execution will never return false in postfire, and
     *  thus the execution can continue forever.
     */
    public Parameter iterations;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Keep firing contained actors until the current iteration converges.
     *  An iteration converges when all the receivers have status known.  
     *  This method also calls the prefire() method of an actor before it 
     *  is fired for the first time. 
     *  // FIXME: prefire() may be called each time the fire() method is called?
     *  @exception IllegalActionException If an actor attempts to modify
     *   a known value.
     */
    public void fire() throws IllegalActionException {
        Schedule schedule = getScheduler().getSchedule();

        // we compute the rough cost of the execution in the following way.
        // Assume the cost of one evaluation of an actor is 1,
        // the cost of the execution to reach a fixed point is
        // the number of actors in the schedule times the
        // iterations of the schedule at that instant.
        // The sum of the cost of all instants is the cost of
        // the whole execution.
        int iterationCount = 0;
        int numberOfActors = schedule.size();

        // also, we calculate the real cost, which excludes the
        // overhead caused by not-firing-allowed actors.
        // Here we need to check for each actor that iteration is allowed
        // (in other words, that the actor has not returned false in
        // postfire()).
        // _fireActor and _postfireActor are responsible for checking that
        // firing is allowed (in other words, the director will not call
        // fire() or postfire() on an actor until the actor returns true
        // in prefire()).
        // _fireActor is also responsible for checking that the actor is
        // ready to fire (sufficient known inputs are available) via
        // _isFiringAllowed method.
        
        // Keep firng actors until the iteration converges based on the
        // current inputs.
        do {
            Iterator firingIterator = schedule.firingIterator();
            while (firingIterator.hasNext() && !_stopRequested) {
                Actor actor = ((Firing) firingIterator.next()).getActor();
                if (_actorsNotAllowedToIterate.contains(actor)) {
                    // The postfire() method of this actor returned false in
                    // some previous iteration, so here, for the benefit of
                    // connected actors, we need to explicitly call the
                    // sendClear() method of all of its output ports.
                    // Note that all output ports should be unknown.
                    _sendClearToAllUnknownOutputsOf(actor);
                } else {
                    _fireActor(actor);
                }
            }
            iterationCount++;
        } while (!_hasIterationConverged() && !_stopRequested);

        if (_debugging) {
            _debug("It takes " + iterationCount
                    + " iterations to find a fixed point.");
        }
        _roughCost += (iterationCount * numberOfActors);
    }

    /** Initialize the director and all deeply contained actors by calling
     *  the super.initialize() method. Reset all private variables.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _currentIteration = 0;

        _actorsNotAllowedToIterate = new HashSet();
        _actorsAllowedToFire = new HashSet();
        _actorsFired = new HashSet();
        _actorsPostfired = new HashSet();
        _cachedAllInputsKnown = new HashSet();
        _cachedAllOutputsKnown = new HashSet();

        _resetAllReceivers();

        _cachedFunctionalProperty = true;
        _functionalPropertyVersion = -1;
        _realCost = 0;
        _roughCost = 0;
    }

    /** Return true if all the controlled actors' isFireFunctional()
     *  methods return true. Otherwise, return false.
     *  
     *  @return True if all controlled actors are functional.
     */
    public boolean isFireFunctional() {
        if (workspace().getVersion() == _functionalPropertyVersion) {
            return _cachedFunctionalProperty;
        }

        boolean result = true;

        Iterator actors = ((CompositeActor) getContainer()).
            deepEntityList().iterator();

        while (result && actors.hasNext() && !_stopRequested) {
            Actor actor = (Actor) actors.next();
            result = actor.isFireFunctional() && result;
        }
        
        _cachedFunctionalProperty = result;
        _functionalPropertyVersion = workspace().getVersion();

        return result;
    }

    /** Return false. The transferInputs() method checks whether
     *  the inputs are known before calling hasToken().
     *  Thus this derictor tolerate unknown inputs.
     *  
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** Return a new FPReceiver.
     *  @return A new FPReceiver.
     */
    public Receiver newReceiver() {
        Receiver receiver = new FixedPointReceiver(this);

        if (_receivers == null) {
            _receivers = new LinkedList();
        }

        _receivers.add(receiver);
        return receiver;
    }

    /** Call postfire() on all contained actors.  Return false if the model
     *  has finished executing, either by reaching the iteration limit, or if
     *  no actors in the model return true in postfire(), or if stop has
     *  been requested. This method is called only once for each iteration.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {

        _postfireReturns = false;

        // Actors are postfired here since updating the state of contained
        // actors inherently updates the state of a composite actor.
        // They are postfired in the order specified by the schedule,
        // but only on their first appearance in the schedule. (An actor
        // may appear in the schedule multiple times.)
        // FIXME: this requirement is necessary to avoid duplicate outputs
        // in display? TESTIT.
        
        // Note that the _actorsPostfired set is used to prevent an actor 
        // from being postfired more than once in one iteration, while 
        // the _actorsNotAlledToIterated set is used to provent an actor from
        // being iterated in the rest of EXECUTION if it returns false from its
        // postfire() method. The _actorsPostfired set gets cleared at the 
        // beginning of each iteration, the prefire() method.
        
        Schedule schedule = getScheduler().getSchedule();
        Iterator firingIterator = schedule.firingIterator();
        while (firingIterator.hasNext() && !_stopRequested) {
            Actor actor = ((Firing) firingIterator.next()).getActor();
            if (!_actorsNotAllowedToIterate.contains(actor)) {
                if (!_actorsPostfired.contains(actor)) {
                    _actorsPostfired.add(actor);
                    if (_postfireActor(actor)) {
                        _postfireReturns = true;
                    } else {
                        _actorsNotAllowedToIterate.add(actor);
                    }
                }
            }
        }

        // Question: why not resetting receivers in the prefire() method?
        // All receivers must be reset before any actors are executed in the
        // next iteration.  Since some domains (including SR) might fire one
        // actor before prefiring another actor, resetting the receivers in
        // the prefire() method will not work.  By doing this at the end of
        // each iteration, all receivers are guaranteed to be reset, even in
        // a hierarchical model.
        _resetAllReceivers();
        
        if (_debugging) {
            _debug("FixedPointDirector: Instant " + _currentIteration
                    + " is complete.");
            _debug("So far, the rough cost is " + _roughCost
                    + "; and the real cost is " + _realCost + ".");
        }

        // Check if the current execution has reached its iteration limit. 
        _currentIteration++;
        int numberOfIterations = ((IntToken) iterations.getToken()).intValue();
        if ((numberOfIterations > 0)
                && (_currentIteration >= numberOfIterations)) {
            _currentIteration = 0;
            _postfireReturns = false;
        }

        return super.postfire();
    }

    /** Initialize the firing of the director by resetting state variables.
     *  @return True always.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    public boolean prefire() throws IllegalActionException {
        _actorsAllowedToFire.clear();
        _actorsFired.clear();
        _actorsPostfired.clear();

        _cachedAllInputsKnown.clear();
        _cachedAllOutputsKnown.clear();

        _lastNumberOfActorsAllowedToFire = -1;
        _lastNumberOfKnownReceivers = -1;

        return super.prefire();
    }

    /** Return an array of suggested directors to be used with
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = new String[2];
        defaultSuggestions[1] = "ptolemy.domains.fsm.kernel.NonStrictFSMDirector";
        defaultSuggestions[0] = "ptolemy.domains.fsm.kernel.FSMDirector";
        return defaultSuggestions;
    }

    /** Transfer data from the specified input port of the
     *  container to the ports it is connected to on the inside.
     *  If there is no data on the specified input port, then
     *  set the ports on the inside to absent by calling sendClearInside().
     *  This method delegates the data transfer
     *  operation to the transferInputs method of the super class.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   input port.
     *  @param port The port to transfer tokens from.
     *  @return True.
     */
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        for (int i = 0; i < port.getWidth(); i++) {
            if (port.isKnown(i)) {
                if (port.hasToken(i)) {
                    super.transferInputs(port);
                } else {
                    port.sendClearInside(i);
                }
            }
        }
        // The returned value does not matter. We simply return true.
        return true;
    }

    /** Transfer data from the specified output port of the
     *  container to the ports it is connected to on the outside.
     *  If there is no data on the specified output port, then
     *  set the ports on the outside to absent by calling sendClear().
     *  This method delegates the data transfer
     *  operation to the transferOutputs method of the super class.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True.
     */
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        for (int i = 0; i < port.getWidthInside(); i++) {
            if (port.isKnownInside(i)) {
                if (port.hasTokenInside(i)) {
                    super.transferOutputs(port);
                } else {
                    port.sendClear(i);
                }
            }
        }
        // The returned value does not matter. We simply return true.
        return true; 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** React to the change in receiver status by incrementing the count of
     *  known receivers.
     */
    protected void _receiverChanged() {
        _currentNumberOfKnownReceivers++;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if all the inputs of the specified actor are known.
     */
    private boolean _areAllInputsKnown(Actor actor)
            throws IllegalActionException {

        if (_cachedAllInputsKnown.contains(actor)) {
            return true;
        }

        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            if (!inputPort.isKnown()) {
                return false;
            }
        }

        _cachedAllInputsKnown.add(actor);
        return true;
    }

    /** Return true if all the outputs of the specified actor are known.
     */
    private boolean _areAllOutputsKnown(Actor actor)
            throws IllegalActionException {

        if (_cachedAllOutputsKnown.contains(actor)) {
            return true;
        }

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();

            if (!outputPort.isKnown()) {
                return false;
            }
        }

        _cachedAllOutputsKnown.add(actor);
        return true;
    }

    /** Fire the specified actor if the actor is ready to fire.  If the
     *  prefire() method of this actor has not returned true in the current
     *  iteration, the prefire() method will be called first.
     */
    private void _fireActor(Actor actor) throws IllegalActionException {
        if (_isReadyToFire(actor) && !_stopRequested) {
            // If the actor is nonstrict or all the inputs are known,
            // it is ready to fire.
            if (!_actorsAllowedToFire.contains(actor)) {
                // However, the actor is not in the actorsAllowToFire set.
                // The actor is prefired such that it can be added into
                // the set if its prefire method returns true.
                if (_debugging) {
                    _debug("    FixedPointDirector is prefiring",
                            ((Nameable) actor).getName());
                }
                if (actor.prefire()) {
                    _actorsAllowedToFire.add(actor);
                } else {
                    // Note that if an actor (always) returns false in
                    // its prefire() method, such as the Undefined actor,
                    // the fire() and postfire() methods will never be invoked. 
                    // Consequently, the output of this actor is undefined.
                    return;
                }
            }
            // If an actor has finished firing, meaning that all inputs and 
            // outputs have been resolved, there is no need to fire the actor
            // again. The purpose of this implementation is to reduce the cost
            // for finding a fixed point.
            if (!_isFinishedFiring(actor)) {
                if (_debugging) {
                    _debug("    FixedPointDirector is firing",
                            ((Nameable) actor).getName());
                }

                // Whether all inputs are known must be checked before
                // firing to handle cases with self-loops, because the
                // current firing may change the status of some input
                // receivers from unknown to known.
                boolean allInputsKnownBeforeFiring = _areAllInputsKnown(actor);
                actor.fire();
                // Record the fired actor into the _actorFired set
                _actorsFired.add(actor);

                // If all of the inputs of this actor are known, firing
                // the actor again in the current iteration is not necessary.
                // The actor will not produce any more outputs because
                // it will have no new inputs to react to.  Thus, we
                // can assume that any unknown outputs of this actor
                // are actually absent.
                // Note that even if an opaque composite actor containing a 
                // feedback actor, which nees 2 or more iterations to 
                // resolve all the inputs and outputs, the do-while loop in
                // the fire() method guarantees that inside inputs and outputs
                // of the opaque composite actor are resolved (based on the
                // provided external inputs).
                if (allInputsKnownBeforeFiring) {
                    _sendClearToAllUnknownOutputsOf(actor);
                }

                _realCost++;
            }
        }
    }

    /** Return true if this iteration has converged.  The iteration has
     *  converged if both the number of known receivers has converged and
     *  the number of actors to fire has converged.
     */
    private boolean _hasIterationConverged() {
        // Get the previous values for local use.
        int previousNumberOfActorsAllowedToFire = _lastNumberOfActorsAllowedToFire;
        int previousNumberOfKnownReceivers = _lastNumberOfKnownReceivers;

        // Get the current values for local use.
        int currentNumberOfActorsAllowedToFire = _actorsAllowedToFire.size();
        int currentNumberOfKnownReceivers = _currentNumberOfKnownReceivers;

        // Update the previous values for use the next time this method
        // is called.
        _lastNumberOfActorsAllowedToFire = currentNumberOfActorsAllowedToFire;
        _lastNumberOfKnownReceivers = _currentNumberOfKnownReceivers;

        if (_debugging) {
            _debug("  previousNumberOfActorsAllowedToFire is "
                    + previousNumberOfActorsAllowedToFire);
            _debug("  currentNumberOfActorsAllowedToFire is "
                    + currentNumberOfActorsAllowedToFire);
            _debug("  previousNumberOfKnownReceivers is "
                    + previousNumberOfKnownReceivers);
            _debug("  currentNumberOfKnownReceivers is "
                    + currentNumberOfKnownReceivers);
        }

        // Note that having zero actors to fire is not sufficient for
        // convergence.  Some actors may fire in the next phase if
        // the director calls sendClear() on any output ports.
        // Note that all receivers having known state is not sufficient
        // for convergence.  After the values are present, each actor must
        // have the opportunity to fire before the end of the iteration.
        // This is the first phase of actor firings.
        if (previousNumberOfKnownReceivers == -1) {
            return false;
        }

        // The number of actors to fire has not converged, so the
        // iteration has not converged.
        if (previousNumberOfActorsAllowedToFire != currentNumberOfActorsAllowedToFire) {
            return false;
        }

        // The number of known receivers has not converged, so the
        // iteration has not converged.
        if (previousNumberOfKnownReceivers != currentNumberOfKnownReceivers) {
            return false;
        }

        // The number of actors to fire and the number of known receivers
        // have both converged, so the iteration has converged.
        return true;
    }

    /** Initialize the director by creating the parameters and setting their
     *  values and types.
     */
    private void _init() throws IllegalActionException, NameDuplicationException {
        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);
        
        FixedPointScheduler scheduler = new FixedPointScheduler(this,
                uniqueName("Scheduler"));
        setScheduler(scheduler);
    }

    /** Return true if the specified actor is finished firing.  An actor
     *  has finished firing if it is strict and has defined all of its outputs,
     *  or if it is nonstrict, with all inputs and outputs known, and no
     *  inputs just change from unknown to knonwn.
     */
    private boolean _isFinishedFiring(Actor actor)
            throws IllegalActionException {

        // Actors that have not been fired in this iteration are not finished.
        // The purpose of this check is to reduce the cost for 
        // finding a fixed point.
        if (!_actorsFired.contains(actor)) {
            return false;
        }
        
        if (actor.isStrict()) {
            // For a strict actor, if all outputs have been resolved,
            // then its inputs must have been resolved too and this actor has 
            // finished firing.
            return _areAllOutputsKnown(actor);
        } else {
            // Nonstrict actors should fire in every phase in case more inputs
            // become available (the inputs might be, for example, cached and
            // used in a subsequent iteration).

            // If all the inputs and outputs are known, and
            // the inputs have not just changed from unknown to known,
            // it is unnecessary to fire the actor again,
            // because according to the FP semantics, the
            // the inputs, which are the outputs from other
            // actors, can not change.
            // Check whether the inputs have changed.
            // Note, the _statusChanged method forces the receivers to update
            // the cached information.
            Iterator inputPorts = actor.inputPortList().iterator();
            boolean changed = false;
            
            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                Receiver[][] receivers = inputPort.getReceivers();
                
                for (int i = 0; i < receivers.length; i++) {
                    for (int j = 0; j < receivers[i].length; j++) {
                        changed |= ((FixedPointReceiver) receivers[i][j])
                        ._statusChanged();
                    }
                }
            }
            
            if (_areAllInputsKnown(actor) && _areAllOutputsKnown(actor)) {
                return !changed;
            } else {
                return false;
            }
        }
    }

    /** Return true if the specified actor is ready to fire.  An actor is
     *  ready to fire if sufficient known inputs are available, or the actor
     *  is a nonstrict actor..
     */
    private boolean _isReadyToFire(Actor actor) throws IllegalActionException {
        return !actor.isStrict() || _areAllInputsKnown(actor);
    }

    /** Return the result of the postfire() method of the specified actor
     *  if it is allowed to be fired in the current iteration.  If this actor 
     *  is not to be fired in the current iteration, return true without
     *  calling the postfire() method of the actor.
     */
    private boolean _postfireActor(Actor actor) throws IllegalActionException {
        if (_actorsAllowedToFire.contains(actor)) {
            if (_debugging) {
                _debug("    FixedPointDirector is postfiring",
                        ((Nameable) actor).getName());
            }
            return actor.postfire();
        }
        return true;
    }

    /** Reset all receivers to allow a new firing of the director.
     */
    private void _resetAllReceivers() {
        if (_debugging) {
            _debug("    FixedPointDirector is resetting all receivers");
        }

        _currentNumberOfKnownReceivers = 0;

        if (_receivers == null) {
            _receivers = new LinkedList();
        }

        Iterator receiverIterator = _receivers.iterator();
        while (receiverIterator.hasNext()) {
            ((FixedPointReceiver) receiverIterator.next()).reset();
        }
    }

    /** Call the sendClear() method of each of the output ports of the
     *  specified actor if the actor is strict.
     */
    private void _sendClearToAllUnknownOutputsOf(Actor actor)
            throws IllegalActionException {
        // An actor, if its firing has finished but some of its 
        // outputs are still unknown, clear these outputs.
        // However, there is nothing need to do if this actor has 
        // resolved all of its outputs.
        if (_debugging) {
            _debug("  FixedPointDirector is calling sendClear() on the " +
                    "output ports of " + ((Nameable) actor).getName());
        }
        
        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            for (int j = 0; j < outputPort.getWidth(); j++) {
                if (!outputPort.isKnown(j)) {
                    outputPort.sendClear(j);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private Set _actorsNotAllowedToIterate;

    // The set of actors that have returned true in their prefire() methods
    // on the given iteration.
    private Set _actorsAllowedToFire;

    // The set of actors that have been fired once or more in the given
    // iteration.
    private Set _actorsFired;

    // The set of actors that have been postfired in the given iteration.
    private Set _actorsPostfired;
    
    // The set of actors that have all inputs known in the given iteration.
    private Set _cachedAllInputsKnown;

    // The set of actors that have all outputs known in the given iteration.
    private Set _cachedAllOutputsKnown;

    // The cache of the functional property of the container of this director
    private boolean _cachedFunctionalProperty;
    
    // The current number of receivers with known state.
    private int _currentNumberOfKnownReceivers;

    // The count of iterations executed.
    private int _currentIteration;

    // Version number for the cached functional property
    private transient long _functionalPropertyVersion = -1L;

    // The number of actors that were fired on the last phase of
    // actor firings.
    private int _lastNumberOfActorsAllowedToFire;

    // The number of receivers with known state on the last phase of
    // actor firings.
    private int _lastNumberOfKnownReceivers;

    // List of all receivers this director has created.
    private List _receivers;

    // We calculate the real cost, which excludes the
    // overhead caused by not-firing-allowed actors.
    private int _realCost;

    // we compute the rough cost of the execution in this way.
    // Assume the cost of one evaluation of an actor is 1,
    // the cost of the execution to reach a fixed point is
    // the number of actors in the schedule times the
    // iterations of the schedule at that instant.
    // The sum of the cost of all instants is the cost of
    // the whole execution.
    private int _roughCost;
}
