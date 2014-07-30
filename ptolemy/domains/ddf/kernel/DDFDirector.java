/* Director for the dynamic dataflow model of computation.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.domains.ddf.kernel;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DDFDirector

/**
 The dynamic dataflow (DDF) domain is a superset of the synchronous
 dataflow(SDF) and Boolean dataflow(BDF) domains. In the SDF domain,
 an actor consumes and produces a fixed number of tokens per firing.
 This static information makes possible compile-time scheduling. In the
 DDF domain, there are few constraints on the production and consumption
 behavior of actors, and the schedulers make no attempt to construct a
 compile-time schedule. Instead, each actor has a set of firing rules
 (patterns) and can be fired if one of them is satisfied, i.e., one
 particular firing pattern forms a prefix of sequences of unconsumed
 tokens at input ports. The canonical actors in the DDF domain include
 Select and Switch, which consume or produce tokens on different channels
 based on the token received from the control port. You can also use
 the two-channel versions BooleanSelect and BooleanSwitch.
 <p>
 The dynamic scheduler implemented in this director fires all enabled
 and non-deferrable actors once in a basic iteration. A deferrable
 actor is one that will not help one of the downstream actors become
 enabled because that downstream actor either already has enough tokens on
 the channel connecting those two actors or is waiting for tokens on
 another channel. If no actor fires so far, which means there is no
 enabled and non-deferrable actor, then among all enabled and deferrable
 actors, this director fires those which have the smallest maximum number
 of tokens on their output channels which satisfy the demand of destination
 actors. If still no actor fires, then there is no enabled actor. A user
 can treat several such basic iterations as a single iteration by adding
 a parameter with name <i>requiredFiringsPerIteration</i> to an actor
 (which is often a sink actor or an actor directly connected to output port
 of the composite actor) and specifying the number of times this actor must
 be fired in a single iteration. If the value of the parameter
 <i>runUntilDeadlockInOneIteration</i> is a BooleanToken with value true,
 one single iteration consists of repeating the basic iteration until
 deadlock is reached (thus overriding the previous definition of one
 iteration), which is the status of the model where all active
 actors under the control of this director are unable to fire because
 their firing rules are not satisfied. However, they may be able to fire
 again during next iteration when tokens are transferred in from an outside
 domain. Note <i>runUntilDeadlockInOneIteration</i> can be set to true
 only when this director is not on the top level.
 <p>
 The algorithm implementing one basic iteration goes like this:
 <pre>
 E = set of enabled actors
 D = set of deferrable enabled actors
 </pre>
 One basic(default) iteration consists of:
 <pre>
 if (E\D != empty set) {
 fire (E\D)
 } else if (D != empty set) {
 fire minimax(D)
 } else {
 declare deadlock
 }
 </pre>
 The function "minimax(D)" returns a subset of D with the smallest
 maximum number of tokens on their output channels which satisfy the
 demand of destination actors.
 <p>
 Note that any SDF model can be run with a DDF Director. However, the
 notion of iteration is different. One could try to imitate the SDF
 iteration in the DDF domain by controlling the number of firings in one
 iteration for some actors, such as requiring a plotter to plot a fixed
 number of points in each iteration.
 <p>
 In the DDF domain, the firing rule of any actor is specified by the token
 consumption rates of its input ports. A general DDF actor could change
 the consumption rates of its input ports after each firing of this actor.
 For multiports, an array token could be used to specify different rates
 for different channels connected to the same multiport. Note that in SDF,
 all channels connected to the same multiport have the same rate.
 <p>
 Based on DDFSimpleSched in Ptolemy Classic, by Edward Lee.
 See E. A. Lee et al., "The Almagest," documentation for Ptolemy Classic,
 Vol. 1, Chapter 7, 1997.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DDFDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DDFDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DDFDirector(Workspace workspace) throws IllegalActionException,
    NameDuplicationException {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container The container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException Not thrown in this base class.
     *   May be thrown in the derived classes if the director
     *   is not compatible with the specified container.
     *  @exception NameDuplicationException If the name collides with
     *   an attribute that already exists in the given container.
     */
    public DDFDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A Parameter representing the number of times that postfire() may
     *  be called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in
     *  postfire(), and thus the execution can continue forever or until
     *  the model is deadlocked.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

    /** A Parameter representing the maximum capacity of each receiver
     *  controlled by this director. This is an integer that defaults to 0,
     *  which means the queue in each receiver is unbounded. To specify
     *  bounded queues, set this to a positive integer.
     */
    public Parameter maximumReceiverCapacity;

    /** A parameter indicating whether one iteration consists of
     *  repeated basic iterations until deadlock. If this parameter is
     *  true, the model will be executed until deadlock in one iteration.
     *  The default value is a BooleanToken with the value false. It
     *  cannot be set to true if this director is at the top level.
     */
    public Parameter runUntilDeadlockInOneIteration;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute being changed is <i>runUntilDeadlockInOneIteration</i>
     *  and it is set to be true, then verify this director is not at the
     *  top level.
     *  @param attribute The changed parameter.
     *  @exception IllegalActionException If this director is at top
     *   level and <i>runUntilDeadlockInOneIteration</i> is set to be true,
     *   or getToken() throws IllegalActionException.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == runUntilDeadlockInOneIteration) {
            _runUntilDeadlock = ((BooleanToken) runUntilDeadlockInOneIteration
                    .getToken()).booleanValue();

            if (_runUntilDeadlock && _isTopLevel()) {
                // The reason we don't allow this is because we cannot
                // pause the model easily if the whole execution is in
                // one iteration. And the same effect can be achieved
                // by setting the parameter iterations to zero anyway.
                throw new IllegalActionException(
                        this,
                        "Cannot set runUntilDeadlockInOneIteration to be "
                                + "true if this DDFDirector is at top level. "
                                + "Instead you should set the parameter iterations "
                                + "to be zero to achieve the same effect.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the director into the specified workspace. This calls the
     *  base class and then sets the attribute public members to refer
     *  to the attributes of the new director.
     *  @param workspace The workspace for the new director.
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        DDFDirector newObject = (DDFDirector) super.clone(workspace);
        newObject._actorsInfo = new HashMap();
        newObject._actorsToCheckNumberOfFirings = new LinkedList();
        newObject._disabledActors = new HashSet();
        return newObject;
    }

    /** Set the flag indicating whether type resolution is disabled or not.
     *  This method is used in an ActorRecursion actor. When a composite
     *  actor is cloned into an ActorRecursion actor, type compatibility
     *  has already been checked, therefore there is no need to invalidate
     *  resolved types.
     *  @param flag The flag to be set.
     */
    public void disableTypeResolution(boolean flag) {
        _isTypeResolutionDisabled = flag;
    }

    /** Execute the model for one iteration. First scan all active actors
     *  to put all enabled and non-deferrable actors in a list and find the
     *  minimax actors. Fire all actors once in the list. If no actor has been
     *  fired, fire the minimax actors. If still no actor has been fired,
     *  a deadlock has been detected. This concludes one basic iteration,
     *  and by default also one iteration of this director. However,
     *  if some actor has a parameter named <i>requiredFiringsPerIteration</i>
     *  defined, continue to execute basic iterations until the actor has
     *  been fired at least the number of times given in that parameter. If
     *  more than one actor has such a parameter, then the iteration will
     *  continue until all are satisfied. If the parameter
     *  <i>runUntilDeadlockInOneIteration</i> has value true, one iteration
     *  consists of repeatedly executing basic iterations until the actors
     *  under control of this director have reached a deadlock.
     *  @exception IllegalActionException If any actor executed by this
     *   actor returns false in prefire().
     */
    @Override
    public void fire() throws IllegalActionException {
        boolean repeatBasicIteration = false;
        if (_debugging) {
            _debug("DDFDirector.fire()");
        }
        do {
            // The List to store actors that are enabled and not deferrable.
            List toBeFiredActors = new LinkedList();

            // The list to store minimax actors.
            List minimaxActors = new LinkedList();
            int minimaxSize = Integer.MAX_VALUE;

            Iterator actors = ((TypedCompositeActor) getContainer())
                    .deepEntityList().iterator();

            while (actors.hasNext()) {
                // Scan all actors to find all enabled and not
                // deferrable actors.
                Actor actor = (Actor) actors.next();

                if (_disabledActors.contains(actor)) {
                    continue;
                }

                ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
                ActorEnablingStatus status = actorInfo.status;

                if (status == ActorEnablingStatus.ENABLED_NOT_DEFERRABLE) {
                    toBeFiredActors.add(actor);
                }

                // Find set of minimax actors.
                if (status == ActorEnablingStatus.ENABLED_DEFERRABLE) {
                    int newSize = actorInfo.maximumNumberOfTokens;

                    if (newSize < minimaxSize) {
                        minimaxActors.clear();
                        minimaxActors.add(actor);
                        minimaxSize = newSize;
                    } else if (newSize == minimaxSize) {
                        minimaxActors.add(actor);
                    }
                }
            }

            // No actor has been fired at the beginning of the
            // basic iteration.
            _firedOne = false;

            // Fire all enabled and not deferrable actors.
            Iterator enabledActors = toBeFiredActors.iterator();

            while (enabledActors.hasNext()) {
                Actor actor = (Actor) enabledActors.next();
                boolean isActorFired = _fireActor(actor);
                _firedOne = isActorFired || _firedOne;
            }

            // If no actor has been fired, fire the set of minimax actors.
            if (!_firedOne) {
                Iterator minimaxActorsIterator = minimaxActors.iterator();

                while (minimaxActorsIterator.hasNext()) {
                    Actor minimaxActor = (Actor) minimaxActorsIterator.next();
                    boolean isActorFired = _fireActor(minimaxActor);
                    _firedOne = isActorFired || _firedOne;
                }
            }

            if (_runUntilDeadlock) {
                // Repeat basic iteration if at lease one actor
                // has been fired.
                repeatBasicIteration = _firedOne;
            } else if (_firedOne) {
                // Check to see if we need to repeat basic iteration to
                // satisfy requiredFiringsPerIteration for some actors.
                actors = _actorsToCheckNumberOfFirings.iterator();

                repeatBasicIteration = false;

                while (actors.hasNext()) {
                    Actor actor = (Actor) actors.next();

                    // If the actor has been deleted from the topology,
                    // there is no need to check.
                    if (actor.getContainer() == null) {
                        actors.remove();
                        continue;
                    }

                    ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
                    int requiredFirings = actorInfo.requiredFiringsPerIteration;
                    int firingsDone = actorInfo.numberOfFirings;

                    if (firingsDone < requiredFirings) {
                        repeatBasicIteration = true;
                        break;
                    }
                }
            } else {
                // If no actor has been fired, declare deadlock
                if (_debugging) {
                    _debug("deadlock detected");
                }

                repeatBasicIteration = false;
            }
        } while (repeatBasicIteration && !_stopRequested);
    }

    /** Initialize the model controlled by this director. Initialize the
     *  actors associated with this director. Set all the state variables
     *  to the their initial values.  The order in which the actors are
     *  initialized is arbitrary. If actors connected directly to output
     *  ports produce initial tokens, then send those tokens to the outside
     *  of the composite actor.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _iterationCount = 0;
        _runUntilDeadlock = ((BooleanToken) runUntilDeadlockInOneIteration
                .getToken()).booleanValue();
        _actorsToCheckNumberOfFirings.clear();
        _disabledActors.clear();

        super.initialize();

        Iterator outputPorts = ((Actor) getContainer()).outputPortList()
                .iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();

            for (int i = 0; i < outputPort.getWidthInside(); i++) {
                while (outputPort.hasTokenInside(i)) {
                    Token token = outputPort.getInside(i);

                    if (_debugging) {
                        _debug("transferring initial tokens from "
                                + outputPort.getFullName());
                    }

                    outputPort.send(i, token);
                }
            }
        }
        if (_debugging) {
            _debug("DDFDirector.initialize() finished.");
        }
    }

    /** Initialize the given actor. This method is called by the
     *  initialize() method of the director, and by the manager whenever
     *  an actor is added to the executing model as a mutation. It first
     *  calls the actor's initialize() method which may emit initial tokens.
     *  Then it updates the enabling status of the actor and all actors
     *  connected to this actor. Finally it records the value given by
     *  <i>requiredFiringsPerIteration</i> if the actor has such a parameter.
     *  Any change to this parameter during execution will be ignored.
     *  @param actor The actor to be initialized.
     *  @exception IllegalActionException If the
     *   <i>requiredFiringsPerIteration</i> parameter does not contain
     *   an IntToken.
     */
    @Override
    public void initialize(Actor actor) throws IllegalActionException {
        super.initialize(actor);

        // Since an actor may produce initial tokens during initialization,
        // the enabling status of those directly connected actors as well
        // as itself must be updated.
        _updateConnectedActorsStatus(actor);

        // Determine requiredFiringsPerIteration for this actor.
        // The default value 0 means no requirement on this actor.
        ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
        actorInfo.requiredFiringsPerIteration = 0;

        Variable requiredFiringsPerIteration = (Variable) ((Entity) actor)
                .getAttribute("requiredFiringsPerIteration");

        if (requiredFiringsPerIteration != null) {
            Token token = requiredFiringsPerIteration.getToken();

            if (token instanceof IntToken) {
                int value = ((IntToken) token).intValue();

                if (value > 0) {
                    actorInfo.requiredFiringsPerIteration = value;
                }

                _actorsToCheckNumberOfFirings.add(actor);
            } else {
                throw new IllegalActionException(this, actor, "The variable "
                        + "requiredFiringsPerIteration must contain "
                        + "an IntToken.");
            }
        }
    }

    /** Call base class method to invalidate resolved types if the flag to
     *  disable type resolution is set to false. If the flag is true,
     *  override the base class method to skip invalidating resolved types.
     *  This method is used for an ActorRecursion actor. When a composite
     *  actor is cloned into an ActorRecursion actor, type compatibility
     *  has already been checked, therefore there is no need to invalidate
     *  resolved types.
     */
    @Override
    public void invalidateResolvedTypes() {
        if (!_isTypeResolutionDisabled) {
            super.invalidateResolvedTypes();
        }
    }

    /** Merge an opaque composite actor controlled by an inside DDFDirector
     *  with the outside domain controlled by this director. It aggregates
     *  the status variables for the inside actors with the status variables
     *  for the outside actors. This method can be used in ActorRecursion
     *  which clones a composite actor into itself and then merges with the
     *  outside DDF domain.
     *  @param insideDirector The inside DDFDirector to be merged.
     */
    public void merge(DDFDirector insideDirector) {
        _disabledActors.addAll(insideDirector._disabledActors);
        _actorsToCheckNumberOfFirings
        .addAll(insideDirector._actorsToCheckNumberOfFirings);
        _actorsInfo.putAll(insideDirector._actorsInfo);
    }

    /** Return a new QueueReceiver. Set the capacity of the FIFO queue
     *  in the receiver to the value specified by the director parameter
     *  <i>maximumReceiverCapacity</i> if that value is greater than 0.
     *  @return A new QueueReceiver.
     */
    @Override
    public Receiver newReceiver() {
        QueueReceiver receiver = new QueueReceiver();

        try {
            int capacity = ((IntToken) maximumReceiverCapacity.getToken())
                    .intValue();

            if (capacity > 0) {
                receiver.setCapacity(capacity);
            }
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }

        return receiver;
    }

    /** Increment the number of iterations. Return false if the system
     *  has finished executing by reaching the iteration limit or the system
     *  is deadlocked.
     *  @return True if the Director wants to be fired again in the future.
     *  @exception IllegalActionException If the <i>iterations</i> parameter
     *   does not contain a legal value.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        int iterationsValue = ((IntToken) iterations.getToken()).intValue();
        _iterationCount++;

        if (iterationsValue > 0 && _iterationCount >= iterationsValue) {
            if (_debugging) {
                _debug("iteration limit reached");
            }

            return false;
        }

        // The DDF domain is deadlocked if no actor is fired in the last
        // basic iteration. However, if the DDF domain is embedded inside
        // another domain, then we have to check whether transferring
        // more tokens into the DDF domain will break the deadlock.
        boolean isDeadlocked = !_firedOne;

        if (isDeadlocked && isEmbedded()) {
            Iterator inputPorts = ((Actor) getContainer()).inputPortList()
                    .iterator();

            while (inputPorts.hasNext()) {
                IOPort inputPort = (IOPort) inputPorts.next();
                Receiver[][] deepReceivers = inputPort.deepGetReceivers();
                foundNotSatisfiedReceiver: for (int i = 0; i < deepReceivers.length; i++) {
                    for (int j = 0; j < deepReceivers[i].length; j++) {
                        QueueReceiver deepReceiver = (QueueReceiver) deepReceivers[i][j];
                        IOPort port = deepReceiver.getContainer();

                        // We don't consider the weird case where the input
                        // port is directly connected to an output port
                        // of the container.
                        if (port.getContainer() != getContainer()) {
                            int tokenConsumptionRate = _getTokenConsumptionRate(deepReceiver);

                            if (deepReceiver.size() < tokenConsumptionRate) {
                                isDeadlocked = false;
                                break foundNotSatisfiedReceiver;
                            }
                        }
                    }
                }
            }
        }

        return super.postfire() && !isDeadlocked;
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do. If there are no input ports, then also return true.
     *  Otherwise, return false. If an input port does not have a parameter
     *  <i>tokenConsumptionRate</i>, then skip checking on that port because
     *  it will transfer all tokens (if there are any) to the inside. Note
     *  the difference from SDF domain where the default rate is 1. Finally,
     *  initialize numberOfFirings to zero for those actors for which positive
     *  requiredFiringsPerIteration has been defined.
     *  @return true If all of the input ports of the container of this
     *   director have enough tokens.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("DDFDirector.prefire()\niterationCount " + _iterationCount);
        }

        super.prefire();

        Actor container = (Actor) getContainer();
        Iterator inputPorts = container.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();

            // NOTE: If the port is a ParameterPort, then we should not
            // insist on there being an input.
            if (inputPort instanceof ParameterPort) {
                continue;
            }

            int[] rate = _getTokenConsumptionRate(inputPort);

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (rate[i] >= 0 && !inputPort.hasToken(i, rate[i])) {
                    if (_debugging) {
                        _debug("Channel " + i + " of port "
                                + inputPort.getFullName()
                                + " does not have enough tokens: " + rate[i]
                                        + ". Prefire returns false.");
                    }
                    if (_debugging) {
                        _debug("DDFDirector.prefire() returns false.");
                    }
                    return false;
                }
            }
        }

        Iterator actors = _actorsToCheckNumberOfFirings.iterator();

        while (actors.hasNext()) {
            Actor actor = (Actor) actors.next();
            ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
            actorInfo.numberOfFirings = 0;
        }
        if (_debugging) {
            _debug("DDFDirector.prefire() returns true.");
        }
        return true;
    }

    /** Return an array of suggested directors to use with an embedded
     *  ModalModel. Each director is specified by its full class
     *  name.  The first director in the array will be the default
     *  director used by a modal model.
     *  @return An array of suggested directors to be used with ModalModel.
     *  @see ptolemy.actor.Director#suggestedModalModelDirectors()
     */
    @Override
    public String[] suggestedModalModelDirectors() {
        String[] defaultSuggestions = {
                "ptolemy.domains.modal.kernel.MultirateFSMDirector",
                "ptolemy.domains.hdf.kernel.HDFFSMDirector",
                "ptolemy.domains.modal.kernel.FSMDirector",
        "ptolemy.domains.modal.kernel.NonStrictFSMDirector" };
        return defaultSuggestions;
    }

    /** Return true to indicate that a ModalModel under control
     *  of this director supports multirate firing.
     *  @return True indicating a ModalModel under control of this director
     *  supports multirate firing.
     */
    @Override
    public boolean supportMultirateFiring() {
        return true;
    }

    /** Override the base class method to transfer enough tokens to complete
     *  an internal iteration. If the token consumption rate is defined for
     *  the port and there are not enough tokens, throw an exception. If the
     *  token consumption rate is not defined for the port, transfer all tokens
     *  (if there are any) contained by the port. Finally it updates enabling
     *  status for all inside opaque actors that receive data from this port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  input port, or if there are not enough input tokens available.
     */
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferInputs on port: " + port.getFullName());
        }

        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an "
                            + "opaque input port.");
        }

        boolean wasTransferred = false;

        int[] rate = _getTokenConsumptionRate(port);

        for (int i = 0; i < port.getWidth(); i++) {
            try {
                // If the parameter tokenConsumptionRate is defined,
                // _getTokenConsumptionRate(port) returns an array
                // of non-negative int.
                if (rate[i] >= 0) {
                    for (int k = 0; k < rate[i]; k++) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);

                            if (_debugging) {
                                _debug(getName(),
                                        "transferring input from channel " + i
                                        + " of input port "
                                        + port.getName());
                            }

                            port.sendInside(i, t);
                            wasTransferred = true;
                        } else {
                            throw new IllegalActionException(
                                    this,
                                    port,
                                    "Channel "
                                            + i
                                            + "should consume "
                                            + rate[i]
                                                    + " tokens, but there were only "
                                                    + k
                                                    + " tokens available. Maybe the rate"
                                                    + " is set wrong?");
                        }
                    }

                    // If the parameter tokenConsumptionRate is not defined,
                    // _getTokeConsumptionRate(port) returns an array of int
                    // each with vaule -1.
                } else {
                    // If no rate was specified, then we transfer at most
                    // one token.
                    if (port.hasToken(i)) {
                        Token token = port.get(i);

                        if (_debugging) {
                            _debug(getName(),
                                    "transferring input from channel " + i
                                    + " of port " + port.getName());
                        }

                        port.sendInside(i, token);
                        wasTransferred = true;
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        // Update enabling status for all inside opaque actors that receive
        // data from this port.
        Iterator insideSinkPorts = port.insideSinkPortList().iterator();

        while (insideSinkPorts.hasNext()) {
            IOPort insideSinkPort = (IOPort) insideSinkPorts.next();
            Actor actor = (Actor) insideSinkPort.getContainer();

            // Skip it if the actor to be checked contains this director.
            // In other words, the data directly go to output port instead
            // of any inside actors.
            if (getContainer() != actor) {
                ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
                if (actorInfo == null) {
                    actorInfo = new ActorInfo();
                    _actorsInfo.put(actor, actorInfo);
                }
                actorInfo.status = _getActorStatus(actor);
            }
        }

        return wasTransferred;
    }

    /** Override the base class method to transfer enough tokens to
     *  fulfill the output production rate. If the token production rate
     *  is defined for the port and there are not enough tokens, throw an
     *  exception. If the token production rate is not defined for the port,
     *  transfer all tokens (if there are any) contained on the inside by
     *  the port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     *  @exception IllegalActionException If the port is not an opaque
     *  output port, or if there are not enough output tokens available.
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferOutputs on port: " + port.getFullName());
        }

        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferOutputs on a port that "
                            + "is not an opaque output port.");
        }

        boolean wasTransferred = false;

        int[] rate = _getTokenProductionRate(port);

        for (int i = 0; i < port.getWidthInside(); i++) {
            try {
                // If the parameter tokenProductionRate is defined,
                // _getTokenProductionRate(port) returns an array
                // of non-negative int.
                if (rate[i] >= 0) {
                    for (int k = 0; k < rate[i]; k++) {
                        if (port.hasTokenInside(i)) {
                            Token token = port.getInside(i);

                            if (_debugging) {
                                _debug(getName(),
                                        "transferring output from channel " + i
                                        + " of port " + port.getName());
                            }

                            port.send(i, token);
                            wasTransferred = true;
                        } else {
                            throw new IllegalActionException(
                                    this,
                                    port,
                                    "Channel "
                                            + i
                                            + " should produce "
                                            + rate[i]
                                                    + " tokens, but there were only "
                                                    + k
                                                    + " tokens available. Maybe the rate"
                                                    + " is set wrong?");
                        }
                    }

                    // If the parameter tokenProductionRate is not defined,
                    // _getTokenProductionRate(port) returns an array of int
                    // each with value -1.
                } else {
                    while (port.hasTokenInside(i)) {
                        Token token = port.getInside(i);

                        if (_debugging) {
                            _debug(getName(),
                                    "transferring output from channel " + i
                                    + " of port " + port.getName());
                        }

                        port.send(i, token);
                        wasTransferred = true;
                    }
                }
            } catch (NoTokenException ex) {
                // this shouldn't happen.
                throw new InternalErrorException(this, ex, null);
            }
        }

        return wasTransferred;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Iterate the actor once. Increment the firing number for it.
     *  Update the enabling status for each connected actor as well
     *  as itself.
     *  @param actor The actor to be fired.
     *  @return true if the actor is actually fired, false if not.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException or the actor is not ready.
     */
    protected boolean _fireActor(Actor actor) throws IllegalActionException {
        if (_debugging) {
            _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE));
        }

        // Iterate once.
        int returnValue = actor.iterate(1);

        if (_debugging) {
            _debug(new FiringEvent(this, actor, FiringEvent.AFTER_ITERATE));
        }

        _updateConnectedActorsStatus(actor);

        if (returnValue == STOP_ITERATING) {
            if (_debugging) {
                _debug("Actor " + ((NamedObj) actor).getFullName()
                        + " is disabled.");
            }

            _disabledActors.add(actor);
            _actorsToCheckNumberOfFirings.remove(actor);
        }

        // If the returnValue is NOT_READY, this method returns false.
        // Because the token consumption rates of input ports provide only
        // a guideline for firing instead of a contract, we allow an enabled
        // (as determined by the director) actor to return false in its
        // prefire().
        boolean fired = false;

        if (returnValue != NOT_READY) {
            // At least one actor has been fired in this basic iteration.
            fired = true;

            // Increment the firing number.
            if (_actorsToCheckNumberOfFirings.contains(actor)) {
                ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
                actorInfo.numberOfFirings++;
            }
        }

        return fired;
    }

    /** Determine actor enabling status. It must be one of the three:
     *  NOT_ENABLED, ENABLED_DEFERRABLE, ENABLED_NOT_DEFERRABLE.
     *  @param actor The actor to be checked.
     *  @return An int indicating actor enabling status.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    protected ActorEnablingStatus _getActorStatus(Actor actor)
            throws IllegalActionException {
        if (!_isEnabled(actor)) {
            if (_debugging) {
                _debug(((NamedObj) actor).getName() + ": "
                        + ActorEnablingStatus.NOT_ENABLED);
            }

            return ActorEnablingStatus.NOT_ENABLED;
        }

        if (_isDeferrable(actor)) {
            if (_debugging) {
                _debug(((NamedObj) actor).getName() + ": "
                        + ActorEnablingStatus.ENABLED_DEFERRABLE);
            }

            return ActorEnablingStatus.ENABLED_DEFERRABLE;
        }

        if (_debugging) {
            _debug(((NamedObj) actor).getName() + ": "
                    + ActorEnablingStatus.ENABLED_NOT_DEFERRABLE);
        }

        return ActorEnablingStatus.ENABLED_NOT_DEFERRABLE;
    }

    /** Check each remote receiver to see whether the number of tokens
     *  in the receiver is greater than or equal to the
     *  <i>tokenConsumptionRate</i> of the containing port. The actor
     *  is deferrable if the above test is true for any receiver. At
     *  the same time, find the maximum number of tokens in all
     *  receivers, which is used to find minimax actors later on.
     *  @param actor The actor to be checked.
     *  @return true if the actor is deferrable, false if not.
     *  @exception IllegalActionException If any called method throws
     *  IllegalActionException.
     */
    protected boolean _isDeferrable(Actor actor) throws IllegalActionException {
        boolean deferrable = false;
        int maxSize = 0;

        Iterator outputPorts = actor.outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort outputPort = (IOPort) outputPorts.next();
            Receiver[][] farReceivers = outputPort.getRemoteReceivers();

            for (Receiver[] farReceiver2 : farReceivers) {
                if (farReceiver2 == null) {
                    continue;
                }
                for (int j = 0; j < farReceiver2.length; j++) {
                    QueueReceiver farReceiver = (QueueReceiver) farReceiver2[j];
                    IOPort port = farReceiver.getContainer();

                    // Having a self-loop doesn't make it deferrable.
                    if (port.getContainer() == outputPort.getContainer()) {
                        continue;
                    }

                    int tokenConsumptionRate = _getTokenConsumptionRate(farReceiver);

                    if (tokenConsumptionRate >= 0
                            && farReceiver.size() >= tokenConsumptionRate) {
                        deferrable = true;

                        // Here we find the maximum of the token numbers for
                        // the actor's output channels which satisfy the demand
                        // of destination actors while checking deferrability.
                        // The advantage of this is that it only adds a small
                        // additional operation for now. If later on we need
                        // this information, we don't need to do traversing
                        // again. The disadvantage is that 1) we can return
                        // from this method as soon as deferrable == true if we
                        // don't perform this additional operation. 2) We will
                        // not need this information if it turns out not all
                        // enabled actors are deferrable. Therefore another
                        // approach is to perform this operation only when
                        // needed, i.e., when all enabled actor are deferrable.
                        if (farReceiver.size() > maxSize) {
                            maxSize = farReceiver.size();
                        }
                    }
                }
            }
        }

        if (deferrable) {
            ActorInfo actorInfo = (ActorInfo) _actorsInfo.get(actor);
            actorInfo.maximumNumberOfTokens = maxSize;
        }

        return deferrable;
    }

    /** Check to see whether the actor is enabled. It is enabled if the
     *  tokenConsumptionRate on each input port is satisfied by all
     *  receivers contained by this port.
     *  @param actor The actor to be checked.
     *  @return true if the actor is enabled, false if not.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    protected boolean _isEnabled(Actor actor) throws IllegalActionException {
        Iterator inputPorts = actor.inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort) inputPorts.next();
            int[] rate = _getTokenConsumptionRate(inputPort);

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (rate[i] > 0 && !inputPort.hasToken(i, rate[i])) {
                    return false;
                }
            }
        }

        return true;
    }

    /** Update the enabling status of the given actor and all actors
     *  connected to this actor. This method gets called after the given
     *  actor gets initialized or fired.
     * @param actor The actor to te checked.
     * @exception IllegalActionException If _getActorStatus(Actor) throws
     *  IllegalActionException.
     */
    protected void _updateConnectedActorsStatus(Actor actor)
            throws IllegalActionException {
        // Update enabling status for each connected actor.
        Iterator ports = ((Entity) actor).portList().iterator();

        while (ports.hasNext()) {
            ComponentPort port = (ComponentPort) ports.next();
            Iterator deepConnectedPorts = port.deepConnectedPortList()
                    .iterator();

            while (deepConnectedPorts.hasNext()) {
                Port deepConnectedPort = (Port) deepConnectedPorts.next();
                Actor connectedActor = (Actor) deepConnectedPort.getContainer();

                // Skip it if the connectedActor to be checked contains
                // this director.
                if (getContainer() != connectedActor) {
                    // Get an array of actor flags from HashMap.
                    // Create it if none found.
                    ActorInfo actorInfo;

                    if (_actorsInfo.containsKey(connectedActor)) {
                        actorInfo = (ActorInfo) _actorsInfo.get(connectedActor);
                    } else {
                        actorInfo = new ActorInfo();
                        _actorsInfo.put(connectedActor, actorInfo);
                    }

                    actorInfo.status = _getActorStatus(connectedActor);
                }
            }
        }

        // Update enabling status for this actor.
        ActorInfo actorInfo;

        if (_actorsInfo.containsKey(actor)) {
            actorInfo = (ActorInfo) _actorsInfo.get(actor);
        } else {
            actorInfo = new ActorInfo();
            _actorsInfo.put(actor, actorInfo);
        }

        actorInfo.status = _getActorStatus(actor);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get token consumption rate for the given port. If the port is an
     *  input port of an actor controlled by this director, the default
     *  rate is 1 unless explicitly specified by a <i>tokenConsumptionRate</i>
     *  parameter. If the port is an input port of the container of this
     *  director, the default value is -1 unless explicitly specified by
     *  a <i>tokenConsumptionRate</i> parameter. The value -1 means consuming
     *  all tokens (if there are any) contained by the port.
     *  @param port The port to get token consumption rate.
     *  @return An int array of token consumption rates.
     *  @exception IllegalActionException If parameter throws it or the
     *   length of tokenConsumptionRate array is less than port width.
     */
    private int[] _getTokenConsumptionRate(IOPort port)
            throws IllegalActionException {
        int[] rate = new int[port.getWidth()];

        if (port.getContainer() != getContainer()) {
            Arrays.fill(rate, 1);
        } else {
            Arrays.fill(rate, -1);
        }

        Variable rateVariable = DFUtilities.getRateVariable(port,
                "tokenConsumptionRate");

        if (rateVariable != null) {
            Token token = rateVariable.getToken();

            if (token != null) {
                // If token is ArrayToken, then each channel has a
                // corresponding input rate in the array.
                if (token instanceof ArrayToken) {
                    Token[] tokens = ((ArrayToken) token).arrayValue();

                    for (int i = 0; i < port.getWidth(); i++) {
                        if (i < tokens.length) {
                            rate[i] = ((IntToken) tokens[i]).intValue();
                        }
                    }
                } else { // All the channels in the port have same

                    // tokenConsumptionRate.
                    Arrays.fill(rate, ((IntToken) token).intValue());
                }
            }
        }

        return rate;
    }

    /** Get token consumption rate for the given receiver. The port
     *  containing the receiver can be an input port of an actor
     *  controlled by this director or an output port of the container
     *  of this director. In the latter case, it actually returns the
     *  production rate.
     *  @param receiver The receiver to get token consumption rate.
     *  @return The token consumption rate of the given receiver.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    private int _getTokenConsumptionRate(Receiver receiver)
            throws IllegalActionException {
        int tokenConsumptionRate;

        IOPort port = receiver.getContainer();
        Variable rateVariable = null;
        Token token = null;
        Receiver[][] portReceivers = null;

        // If DDF domain is inside another domain and the
        // receiver is contained by an opaque output port...
        // The default production rate is -1 which means all
        // tokens in the receiver are transferred to the outside.
        if (port.isOutput()) {
            rateVariable = DFUtilities.getRateVariable(port,
                    "tokenProductionRate");
            portReceivers = port.getInsideReceivers();

            if (rateVariable == null) {
                tokenConsumptionRate = -1;
                return tokenConsumptionRate;
            } else {
                token = rateVariable.getToken();

                if (token == null) {
                    tokenConsumptionRate = -1;
                    return tokenConsumptionRate;
                }
            }
        }

        if (port.isInput()) {
            rateVariable = DFUtilities.getRateVariable(port,
                    "tokenConsumptionRate");
            portReceivers = port.getReceivers();

            if (rateVariable == null) {
                tokenConsumptionRate = 1;
                return tokenConsumptionRate;
            } else {
                token = rateVariable.getToken();

                if (token == null) {
                    tokenConsumptionRate = 1;
                    return tokenConsumptionRate;
                }
            }
        }

        if (token instanceof ArrayToken) {
            Token[] tokens = ((ArrayToken) token).arrayValue();

            // Scan the contained receivers of the port to find
            // out channel index.
            int channelIndex = 0;
            foundChannelIndex: for (int m = 0; m < portReceivers.length; m++) {
                for (int n = 0; n < portReceivers[m].length; n++) {
                    if (receiver == portReceivers[m][n]) {
                        channelIndex = m;
                        break foundChannelIndex;
                    }
                }
            }

            tokenConsumptionRate = ((IntToken) tokens[channelIndex]).intValue();
        } else {
            tokenConsumptionRate = ((IntToken) token).intValue();
        }

        return tokenConsumptionRate;
    }

    /** Get token production rate for the given port. The port argument
     *  should always be an output port of the container of this director.
     *  The convention is that if a parameter named <i>tokenproductionRate</i>
     *  is defined, return the value in that parameter. Otherwise, return
     *  an array of int each with value -1 which means the director should
     *  transfer all tokens contained on the inside by the port to the outside.
     *  Note the difference from SDF domain where the default rate is 1.
     *  @param port The port to get token production rate.
     *  @return An int array of token production rate.
     *  @exception IllegalActionException If parameter throws it
     *   or the length of tokenProductionRate array is less
     *   than port inside width or the port in the argument is
     *   not an output port of the container of this director.
     */
    private int[] _getTokenProductionRate(IOPort port)
            throws IllegalActionException {
        if (port.getContainer() != getContainer()) {
            throw new IllegalActionException(this, "The port in the "
                    + "argument is not an output port of the container of "
                    + getName());
        }

        int[] rate = new int[port.getWidthInside()];
        Arrays.fill(rate, -1);

        Variable rateVariable = DFUtilities.getRateVariable(port,
                "tokenProductionRate");

        if (rateVariable != null) {
            Token token = rateVariable.getToken();

            if (token != null) {
                // If token is ArrayToken, then each channel has a
                // corresponding output rate in the array.
                if (token instanceof ArrayToken) {
                    Token[] tokens = ((ArrayToken) token).arrayValue();

                    if (tokens.length < port.getWidthInside()) {
                        throw new IllegalActionException(this, "The length of "
                                + "tokenProductionRate array is less than "
                                + "the port inside width.");
                    }

                    for (int i = 0; i < port.getWidthInside(); i++) {
                        if (i < tokens.length) {
                            rate[i] = ((IntToken) tokens[i]).intValue();
                        }
                    }
                } else { // All the channels in the port has same

                    // tokenProductionRate.
                    Arrays.fill(rate, ((IntToken) token).intValue());
                }
            }
        }

        return rate;
    }

    /** Initialize the object. In this case, we give the DDFDirector
     *  an <i>iterations</i> parameter with default value zero,
     *  a <i>maximumReceiverCapacity</i> parameter with default value zero
     *  and a <i>runUntilDeadlockInOneIteration</i> parameter with default
     *  value false.
     */
    private void _init() throws IllegalActionException,
    NameDuplicationException {
        iterations = new Parameter(this, "iterations");
        iterations.setTypeEquals(BaseType.INT);
        iterations.setToken(new IntToken(0));

        maximumReceiverCapacity = new Parameter(this, "maximumReceiverCapacity");
        maximumReceiverCapacity.setTypeEquals(BaseType.INT);
        maximumReceiverCapacity.setToken(new IntToken(0));

        runUntilDeadlockInOneIteration = new Parameter(this,
                "runUntilDeadlockInOneIteration");
        runUntilDeadlockInOneIteration.setTypeEquals(BaseType.BOOLEAN);
        runUntilDeadlockInOneIteration.setToken(new BooleanToken(false));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A flag indicating whether type resolution is disabled.
     */
    private boolean _isTypeResolutionDisabled = false;

    /** A flag indicating whether at least one actor has been fired so far.
     */
    private boolean _firedOne = false;

    /** The number of iterations.
     */
    private int _iterationCount = 0;

    /** A boolean initialized with value in the parameter
     *  runUntilDeadlockInOneIteration.
     */
    private boolean _runUntilDeadlock;

    /** A HashMap containing actors' information. Each actor is mapped to
     *  an ActorInfo object.
     */
    private HashMap _actorsInfo = new HashMap();

    /** A list to store those actors for which positive
     *  requiredFiringsPerIteration has been defined.
     */
    private LinkedList _actorsToCheckNumberOfFirings = new LinkedList();

    /** The set of actors that have returned false in their postfire()
     *  methods and therefore become disabled.
     */
    private Set _disabledActors = new HashSet();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This private class is data structure for recording an actor's
     *  information during the execution.
     */
    private static class ActorInfo {

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** This field records the enabling status of the actor.
         */
        public ActorEnablingStatus status;

        /** This field records the number of firings of the actor.
         *  It is reset to 0 at the beginning of each iteration of
         *  the model the actor is in.
         */
        public int numberOfFirings;

        /** This field records the maximum number of tokens on the actor's
         *  output channels which satisfy the demand of destination actors.
         *  It is used to find minimax actors.
         */
        public int maximumNumberOfTokens;

        /** This field records the actor's required number of firings
         *  per iteration of the model the actor is in.
         */
        public int requiredFiringsPerIteration;
    }
}
