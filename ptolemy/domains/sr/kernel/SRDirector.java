/* Director for the SR model of computation.

 Copyright (c) 2000-2001 The Regents of the University of California.
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

@ProposedRating Red (pwhitake@eecs.berkeley.edu)
@AcceptedRating Red (pwhitake@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.kernel;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sr.lib.NonStrictActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

import java.util.Iterator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

// FIXME: Won't need this import.
import java.util.Collections;

//////////////////////////////////////////////////////////////////////////
//// SRDirector
/**

FIXME: update.

@author Paul Whitaker
@version $Id$
*/
public class SRDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public SRDirector() {
        super();
        _init();
    }

    /** Construct a director in the given workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace for this object.
     */
    public SRDirector(Workspace workspace) {
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
    public SRDirector(CompositeEntity container, String name)
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

    /** The period of an iteration. The type must be less than or equal to
     *  DoubleToken, and it defaults to 1.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the iterations member.  The new
     *  actor will have the same parameter values as the old.
     *  FIXME: Are the parameters cloned automatically?
     *  FIXME: Are the type constraints cloned automatically?
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SRDirector newObject = (SRDirector)(super.clone(workspace));
        newObject.iterations = (Parameter)newObject.getAttribute("iterations");
        //newObject.iterations.setTypeEquals(BaseType.INT);
        newObject.period = (Parameter)newObject.getAttribute("period");
        //newObject.period.setTypeEquals(BaseType.DOUBLE);
        return newObject;
    }

    /** Iterate every contained actor with calls to prefire(), fire(), and 
     *  postfire().
     *  @exception IllegalActionException If an actor executed by this
     *  director returns false in its prefire().
     */
    public void fire() throws IllegalActionException {

        List actorList = _getActorList();
        // FIXME distinguish between iteration of an actor and iteration
        // through the actors
        Iterator actorIterator;

        _initFiring();

        // Here we need to check for each actor that iteration is allowed
        // (in other words, that the actor has not returned false in 
        //  postfire()).
        
        // _fireActor and _postfireActor are responsible for checking that
        // firing is allowed (in other words, the actor will not call fire()
        // or postfire() on an actor until it returns true in prefire()).

        do {
            actorIterator = actorList.iterator();

            while (actorIterator.hasNext()) {
                Actor actor = (Actor) actorIterator.next();
                if (_isIterationAllowed(actor)) _fireActor(actor);
            }
        } while (!_hasIterationConverged());

        // FIXME is each of these called a phase of actor firings?
        // global replace

        actorIterator = actorList.iterator();

        while (actorIterator.hasNext()) {

            Actor actor = (Actor)actorIterator.next();

            if (_isIterationAllowed(actor)) {

                if (_postfireActor(actor)) {
                    _postfireReturns = true;
                } else {
                    _doNotAllowIterationOf(actor);
                }

            }

        }

    }

    /** Return the number of iterations.
     *  @return The number of iterations.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public int getIterations() throws IllegalActionException {
        return ((IntToken) iterations.getToken()).intValue();
    }

    /** Return the period.
     *  @return The period.
     *  @exception IllegalActionException If the period parameter does
     *   not have a valid token.
     */
    public double getPeriod() throws IllegalActionException {
        return ((DoubleToken) period.getToken()).doubleValue();
    }

    /** Initialize the director by initializing state variables and invoke
     *  the initialize() methods of all actors deeply contained by the
     *  container.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void initialize() throws IllegalActionException {

        _iteration = 0;        
        _doNotIterateActors = null;

        super.initialize();
    }

    /** Return a new receiver consistent with the SR domain.
     *  @return A new SRReceiver.
     */
    public Receiver newReceiver() {
        if (_receivers == null) _receivers = new LinkedList();
        Receiver receiver = new SRReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /** Return false if the system has finished executing, either by
     *  reaching the iteration limit, or having an actor in the model
     *  return false in postfire.
     *  @return True if the execution is not finished.
     *  @exception IllegalActionException If the iterations parameter does
     *   not have a valid token.
     */
    public boolean postfire() throws IllegalActionException {

        _advanceCurrentTime(getPeriod());

        int numberOfIterations = getIterations();
        _iteration++;

        _debug("SRDirector: iteration",
                String.valueOf(_iteration), 
                "is complete.");

        if((numberOfIterations > 0) && (_iteration >= numberOfIterations)) {
            _iteration = 0;
            return false;
        }

        return _postfireReturns;
    }

    /** Enable all actors and invoke the preinitialize() methods of
     *  all actors deeply contained by the container.
     *  FIXME: is this the proper place to start a new receiver list?
     *  @exception IllegalActionException If the superclass throws it.
     */
    public void preinitialize() throws IllegalActionException {

        _receivers = new LinkedList();

        // Call the parent preinitialize method to create the receivers.
        super.preinitialize();
    }

    //FIXME transferInputs
    //FIXME transferOutputs

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The static default SR period is 1 second.
     */
    protected static int _DEFAULT_SR_PERIOD = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to indicate that this director does not
     *  need write access on the workspace during an iteration.
     *  @return False.
     */
    protected boolean _writeAccessRequired() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Advance the current time by the specified amount.
     *  @param amount The amount to advance the current time.
     */
    private void _advanceCurrentTime(double amount) {
        _currentTime = getCurrentTime() + amount;
    }

    /** Do not allow the specified actor to fire.  Typically called when
     *  the prefire method of the actor returns false.
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to allow to fire.
     */
    private void _doAllowFiringOf(Actor actor) {
        if (actor != null) {
            if (_doFireActors == null) {
                _doFireActors = new HashSet();
            }
            //_debug("  Added to _doFireActors:", ((Nameable)actor).getName());
            _doFireActors.add(actor);
        }
    }

    /** Disable the specified actor.  All events destined to this actor
     *  will be ignored. If the argument is null, then do nothing.
     *  @param actor The actor to disallow to iterate.
     */
    private void _doNotAllowIterationOf(Actor actor) {
        if (actor != null) {
            if (_doNotIterateActors == null) {
                _doNotIterateActors = new HashSet();
            }
            _debug("  Added to _doNotIterateActors:",
                    ((Nameable)actor).getName());
            _doNotIterateActors.add(actor);
        }
    }

    /** Fire the specified actor if the prefire() method of this actor has
     *  returned true in the current iteration.  If the prefire() method 
     *  of this actor has not returned true in the current iteration, 
     *  the prefire() method will be called first.
     *  @param actor The actor to fire.
     *  @exception IllegalActionException If the prefire() method or fire()
     *   method of the actor throws it.
     */
    private void _fireActor(Actor actor) throws IllegalActionException {

        if (_isReadyToFire(actor)) {
            if (_isFiringAllowed(actor)) {
                if (!_hasCompletedFiring(actor)) {
                    _debug("    SRDirector is firing",
                            ((Nameable)actor).getName());
                    actor.fire();
                }
            } else {
                _debug("    SRDirector is prefiring",
                        ((Nameable)actor).getName());
                if (actor.prefire()) {
                    _doAllowFiringOf(actor);
                    _fireActor(actor);
                }
            }
        }
    }

    /** Return a list of the actors to be fired by this director.
     *  @return The list of actors to be fired by this director.
     */
    private List _getActorList() {

        CompositeEntity container = (CompositeEntity) getContainer();
        if (container == null) 
            throw new InvalidStateException(this,
                    "fired, but it has no container!");

        // FIXME improve efficiency by caching the list.
        
        // FIXME improve efficiency with partial ordering.
        // Explain in comments that order doesn't affect results.
        // Do something similar to this?  
	// Collections.sort(actorList, new GiottoActorComparator());

        List actorList = container.deepEntityList();

        // FIXME: this shouldn't happen, just for testing
        Collections.shuffle(actorList);

        return actorList;
    }

    /** Return true if the specified actor has completed firing.
     *  @return True if the specified actor has completed firing.
     *  @exception IllegalActionException If it cannot be determined
     *   whether the output ports have known state.
     */
    private boolean _hasCompletedFiring(Actor actor)
            throws IllegalActionException {

        // FIXME: Is this list deep enough?
        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort)outputPorts.next();
            if (!outputPort.isKnown()) return false;
        }

        return true;
    }

    /** Return true if this iteration has converged.  The iteration has
     *  converged if both the number of known receivers has converged and
     *  the number of actors to fire has converged.
     *  @return True if the iteration has converged.
     */
    private boolean _hasIterationConverged() {

        // Get the previous values for local use.
        int previousNumOfActorsToFire = _lastNumOfActorsToFire;
        int previousNumOfKnownReceivers = _lastNumOfKnownReceivers;

        // Get the current values for local use.
        int currentNumOfActorsToFire = _numOfActorsToFire();
        int currentNumOfKnownReceivers = _numOfKnownReceivers();

        // Update the previous values for use the next time this method
        // is called.
        _lastNumOfActorsToFire = currentNumOfActorsToFire;
        _lastNumOfKnownReceivers = currentNumOfKnownReceivers;

        _debug("  previousNumOfActorsToFire is",
                String.valueOf(previousNumOfActorsToFire));
        _debug("  currentNumOfActorsToFire is",
                String.valueOf(currentNumOfActorsToFire));

        _debug("  previousNumOfKnownReceivers is",
                String.valueOf(previousNumOfKnownReceivers));
        _debug("  currentNumOfKnownReceivers is",
                String.valueOf(currentNumOfKnownReceivers));

        _debug("  total number of receivers is",
                String.valueOf(_receivers.size()));

        // No actors to fire, so the iteration has converged.  This
        // check may eliminate one unnecessary phase of actor firings.
        if (currentNumOfActorsToFire == 0) return true;

        // Note that all receivers having known state is not sufficient
        // for convergence.  After the values are present, each actor must
        // have the opportunity to fire before the end of the iteration.

        // This is the first phase of actor firings.
        if (previousNumOfKnownReceivers == -1) return false;

        // The number of actors to fire has not converged, so the
        // iteration has not converged.
        if (previousNumOfActorsToFire != currentNumOfActorsToFire)
            return false;

        // The number of known receivers has not converged, so the
        // iteration has not converged.
        if (previousNumOfKnownReceivers != currentNumOfKnownReceivers)
            return false;

        // The number of actors to fire and the number of known receivers
        // have both converged, so the iteration has converged.
        return true;
    }

    /** Initialize the director by creating parameters.
     */
    private void _init() {
	try {
            //FIXME check that the defaults happen.
	    period = new Parameter(this, "period",
                    new IntToken(_DEFAULT_SR_PERIOD));
            period.setTypeEquals(BaseType.DOUBLE);
	    iterations = new Parameter(this, "iterations", new IntToken(0));
            iterations.setTypeEquals(BaseType.INT);
	    setCurrentTime(0.0);
        } catch (KernelException ex) {
	    throw new InternalErrorException(
                    "Cannot initialize SRDirector: " + ex.getMessage());
        }
    }

    /** Initialize the firing of the director by resetting state variables
     *  and resetting all receivers to have unknown status.
     */
    private void _initFiring() {

        _postfireReturns = false;
        _doFireActors = null;

        _lastNumOfActorsToFire = -1;
        _lastNumOfKnownReceivers = -1;

        _resetAllReceivers();
    }

    /** Return true if the specified actor is allowed to fire, that is,
     *  the prefire method of the actor has returned true.
     *  @return True if the specified actor is allowed to fire.
     */
    private boolean _isFiringAllowed(Actor actor) {
        return (!(_doFireActors == null) && 
                _doFireActors.contains(actor));
    }

    /** Return true if the specified actor is allowed to iterate.
     *  @return True if the specified actor is allowed to iterate.
     */
    private boolean _isIterationAllowed(Actor actor) {
        return (_doNotIterateActors == null || 
                !_doNotIterateActors.contains(actor));
    }

    /** Return true if the specified actor is ready to fire.
     *  @return True if the specified actor is ready to fire.
     *  @exception IllegalActionException If it cannot be determined
     *   whether the input ports have known state.
     */
    private boolean _isReadyToFire(Actor actor) throws IllegalActionException {
        if (actor instanceof NonStrictActor) {
            return true;
        }

        // FIXME: Is this list deep enough?
        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();
            if (!inputPort.isKnown()) return false;
        }

        return true;
    }

    /** Return the number of actors that are allowed to fire.
     *  @return The number of actors that are allowed to fire.
     */
    private int _numOfActorsToFire() {
        if (_doFireActors == null) {
           return 0;
        }
        return _doFireActors.size();
    }

    /** Return the number of receivers with known state.
     *  @return The number of receivers with known state.
     */
    private int _numOfKnownReceivers() {
        if (_receivers == null) {
           return 0;
        }

        int count = 0;
        Iterator i = _receivers.iterator();

        while (i.hasNext()) {
            if (((SRReceiver) i.next()).isKnown()) count++;
        }

        return count;
    }

    /** Return the result of the postfire() method of the specified actor 
     *  if it has been fired in the current iteration.  If this actor has 
     *  not been fired in the current iteration, return true without
     *  calling the postfire() method of the actor.
     *  FIXME: Is this right?  Some actors may never fire or postfire.
     *  @param actor The actor to postfire.
     *  @return True if this actor can execute in the next iteration.
     *  @exception IllegalActionException If the postfire() method of the
     *   actor throws it.
     */
    private boolean _postfireActor(Actor actor)
            throws IllegalActionException {

        if (_isFiringAllowed(actor)) {
            _debug("    SRDirector is postfiring",
                    ((Nameable)actor).getName());
            return actor.postfire();
        }
        return true;
    }

    /** Reset all receivers to allow a new firing of the director.
     */
    private void _resetAllReceivers() {
        Iterator i = _receivers.iterator();
        while (i.hasNext()) {
            ((SRReceiver) i.next()).reset();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The set of actors that have returned false in their postfire() methods.
    // Events destined for these actors are discarded and the actors are
    // never fired.
    private HashSet _doNotIterateActors;

    // The set of actors that have returned true in their prefire() methods
    // on the given iteration.
    private HashSet _doFireActors;

    // The count of iterations executed.
    private int _iteration;

    // The logical or of the values returned by actors' postfire().
    private boolean _postfireReturns;

    // The number of actors that were fired on the last phase of
    // actor firings.
    private int _lastNumOfActorsToFire;

    // The number of receivers with known state on the last phase of
    // actor firings.
    private int _lastNumOfKnownReceivers;

    // List of all receivers this director has created.
    private List _receivers;

}


