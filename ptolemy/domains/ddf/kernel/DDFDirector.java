/* Director for the dynamic dataflow model of computation.

Copyright (c) 2001-2004 The Regents of the University of California.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.domains.sdf.kernel.SDFUtilities;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DDFDirector

/**
   Dynamic dataflow (DDF) domain is a superset of the synchronous
   dataflow(SDF) and Boolean dataflow(BDF) domains. In the SDF domain,
   an actor consumes and produces a fixed number of tokens per firing.
   This static information makes possible compile-time scheduling. In the
   DDF domain, there are few constraints on the production and consumption
   behavior of actors and the schedulers make no attempt to construct a
   compile-time schedule. Instead, each actor has a set of firing rules
   and can be fired if one of them is satisfied, i.e. one set of firing
   patterns forms a prefix of sequences of unconsumed tokens at input
   ports.
   <p>
   The scheduler implemented in this director fires all enabled and non
   deferrable actors once in a basic iteration. A deferrable actor is one
   which will not help the downstream actor become enabled because it either
   already has enough data or is waiting for data on another arc. If no actor
   fires, then one deferrable actor which has the smallest maximum number of
   tokens on its output arcs is fired. A user can treat several such basic
   iterations as a single iteration by specifying the number of times a
   particular actor must be fired in a single iteration. If the value of 
   the parameter runUntilDeadlock is a BooleanToken with value true, the 
   scheduler will repeat the basic iteration until deadlock in <i>one 
   iteration</i>.
   <p>
   The algorithm implementing one basic iteration goes like this:
   <pre>
   E = set of enabled actors
   D = set of deferrable enabled actors
   </pre>

   One default iteration consists of:
   <pre>
   if (E-D != 0) fire(E-D)
   else if (D != 0) fire minimax(D)
   else deadlocked.
   </pre>

   The function "minimax(D)" returns set of actors with the smallest
   maximum number of tokens on its output paths.

   Based on DDFSimpleSched in Ptolemy Classic, by Edward Lee.

   @author Gang Zhou
   @version $Id$
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class DDFDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DDFDirector()
            throws IllegalActionException, NameDuplicationException {
        super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public DDFDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
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

    /** A Parameter representing the number of times that postfire may be
     *  called before it returns false.  If the value is less than or
     *  equal to zero, then the execution will never return false in
     *  postfire, and thus the execution can continue forever or until
     *  there are no more active actors or until the model is deadlocked.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;
    
    /** A parameter representing whether one iteration consists of 
     *  repeated basic iteration until deadlock. If this parameter is 
     *  true, the model will be executed until deadlock in one iteration.
     *  The default value is a BooleanToken with the value false.
     */
    public Parameter runUntilDeadlock;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Prior to each basic iteration, scan all active actors to put all
     *  enabled and not deferrable actors in a list and find the minimax
     *  actor. Fire all actors in the list. If no actor has been fired,
     *  fire the minimax actor. If still no actor has been fired, a
     *  deadlock has been detected. This concludes one basic iteration.
     *  If some actor has a parameter named "requiredFiringsPerIteration"
     *  defined, continue to execute basic iterations until the actor has
     *  been fired at least the number of times given in that parameter. If
     *  more than one actor has such a parameter, then the iteration will
     *  continue until all are satisfied.
     *  @exception IllegalActionException If any actor executed by this
     *   actor return false in prefire.
     */
    public void fire() throws IllegalActionException {

        boolean repeatBasicIteration = true;
        while(repeatBasicIteration) {
            if (_debugging) {
                // Add a newline between each basic iteration.
                _debug("");
            }

            // The default value indicates basic iteration will not
            // be repeated until proved otherwise.
            repeatBasicIteration = false;

            // The list to store minimax actors.
            List minimaxActors = new LinkedList();
            int minimaxSize = Integer.MAX_VALUE;

            // The List to store actors that are enabled and not deferrable.
            List toBeFiredActors = new LinkedList();

            Iterator actors = _activeActors.iterator();
            while (actors.hasNext()) {

                // Scan all actors to find all enabled and not
                // deferrable actors.
                Actor actor = (Actor)actors.next();
                int[] flags = (int[])_actorsFlags.get(actor);
                int canFire = flags[_ENABLING_STATUS];
                if (canFire == _ENABLED_NOT_DEFERRABLE) {
                    toBeFiredActors.add(actor);
                }

                // Find set of minimax actors.
                if (canFire == _ENABLED_DEFERRABLE) {
                    int newSize = flags[_MAX_NUMBER_OF_TOKENS];
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
            Iterator  enabledActors = toBeFiredActors.iterator();
            while (enabledActors.hasNext()) {
                Actor actor = (Actor)enabledActors.next();
                _fireActor(actor);
            }

            // If no actor has been fired, fire set of minimax actors.
            if (!_firedOne && minimaxActors.size() != 0) {
                Iterator minimaxActorsIterator = minimaxActors.iterator();
                while (minimaxActorsIterator.hasNext()) {
                    Actor minimaxActor = (Actor)minimaxActorsIterator.next();
                    _fireActor(minimaxActor);
                }
            }

            // If still no actor has been fired, declare deadlock unless
            // the parameter runUntilDeadlock is true.
            if(!_firedOne && !_runUntilDeadlock ) {
                _postfireReturns = false;
                if (_debugging) {
                    _debug("deadlock detected");
                }
                return;
            }

            // Check to see if we need to repeat basic iteration to
            // satisfy requiredFiringsPerIteration for some actors.
            actors = _actorsToCheckNumberOfFirings.iterator();
            while (actors.hasNext()) {
                Actor actor = (Actor)actors.next();
                int[] flags = (int[])_actorsFlags.get(actor);
                int requiredFirings = flags[_REQUIRED_FIRINGS_PER_ITERATION];
                int firingsDone = flags[_NUMBER_OF_FIRINGS];
                if (firingsDone < requiredFirings) {
                    repeatBasicIteration = true;
                    break;
                }
            }
            
            // Repeat 
            if (_runUntilDeadlock) {
                repeatBasicIteration = _firedOne;
            }
        }
    }

    /** Initialize the actors associated with this director and then
     *  set the iteration count to zero.  The order in which the
     *  actors are initialized is arbitrary.
     *  For each actor, dertermine its enabling status:
     *  _NOT_ENABLED, _ENABLED_NOT_DEFERRABLE or _ENABLED_DEFERRABLE.
     *  Determine requiredFiringsPerIteration for each actor.
     *  @exception IllegalActionException If the requiredFiringsPerIteration
     *   parameter does not contain an IntToken.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _runUntilDeadlock = ((BooleanToken)
                runUntilDeadlock.getToken()).booleanValue();
        _postfireReturns = true;
        _iterationCount = 0;
        _actorsToCheckNumberOfFirings.clear();

        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        _activeActors = new LinkedList(container.deepEntityList());
        Iterator actors = _activeActors.iterator();
        while (actors.hasNext()) {

            // Get an array of actor flags from HashMap.
            // Creat it if none found.
            int[] flags;
            Actor actor = (Actor)actors.next();
            if (_actorsFlags.containsKey(actor)) {
                flags = (int[])_actorsFlags.get(actor);
            } else {
                flags = new int[4];
                _actorsFlags.put(actor, flags);
            }

            // Determine actor enabling status.
            flags[_ENABLING_STATUS] = _actorStatus(actor);

            // Determine required requiredFiringsPerIteration for each actor.
            // The default vaule 0 means no requirement on this actor.
            flags[_REQUIRED_FIRINGS_PER_ITERATION] = 0;
            Variable requiredFiringsPerIteration = (Variable)((Entity)actor).
                getAttribute("requiredFiringsPerIteration");
            if (requiredFiringsPerIteration != null) {
                Token token = requiredFiringsPerIteration.getToken();
                if(token instanceof IntToken) {
                    int value = ((IntToken)token).intValue();
                    if (value > 0)
                        flags[_REQUIRED_FIRINGS_PER_ITERATION] = value;
                    _actorsToCheckNumberOfFirings.add(actor);
                } else
                    throw new IllegalActionException(this,
                            (ComponentEntity)actor, "The variable " +
                            "requiredFiringsPerIteration must contain " +
                            "an IntToken.");
            }
        }
    }

    /** Merge another DDFDirector with this director. It can be used
     *  in ActorRecursion which clones a composite actor and then
     *  merges with outside DDF domain.
     *  @param director The DDFDirector to merge with.
     */
    public void merge(DDFDirector director) {
        _activeActors.addAll(director._activeActors);
        _actorsToCheckNumberOfFirings.addAll(director.
                _actorsToCheckNumberOfFirings);
        _actorsFlags.putAll(director._actorsFlags);
    }

    /** Return a new SDFReceiver.
     *  @return A new SDFReceiver.
     */
    public Receiver newReceiver() {
        return new SDFReceiver();
    }

    /** Increment the number of iterations. Return false if the system
     *  has finished executing by reaching the iteration limit, or all
     *  actors have become inactive, or the system is deadlocked.
     *  @return True if the Director wants to be fired again in the future.
     *  @exception IllegalActionException If the iterations parameter
     *   does not contain a legal value.
     */
    public boolean postfire() throws IllegalActionException {
        int iterationsValue = ((IntToken)(iterations.getToken())).intValue();
        _iterationCount++;
        if ((iterationsValue > 0) && (_iterationCount >= iterationsValue)) {
            if (_debugging) {
                _debug("iteration limit reached");
            }
            return false;
        }
        if (_activeActors.size() == 0) {
            if (_debugging) {
                _debug("no more active actors");
            }
            return false;
        }
        return _postfireReturns && super.postfire();
    }

    /** Check the input ports of the container composite actor (if there
     *  are any) to see whether they have enough tokens, and return true
     *  if they do. If there are no input ports, then also return true.
     *  Otherwise, return false. Note that this does not call prefire()
     *  on the contained actors.
     *  Initialize numberOfFirings to zero for those actors for which
     *  positive requiredFiringsPerIteration has been defined.
     *  @return true If all of the input ports of the container of this
     *   director have enough tokens.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("\niterationCount " + _iterationCount);
        }
        super.prefire();

        Actor container = ((Actor)getContainer());
        Iterator inputPorts = container.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();

            // NOTE: If the port is a ParameterPort, then we should not
            // insist on there being an input.
            if (inputPort instanceof ParameterPort) continue;

            int[] rate = _getTokenConsumptionRate(inputPort);
            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (!inputPort.hasToken(i, rate[i])) {
                    if (_debugging) {
                        _debug("Channel " + i + " of port " +
                                inputPort.getFullName()
                                + " does not have enough tokens: "
                                + rate[i]
                                + ". Prefire returns false.");
                    }
                    return false;
                }
            }
        }

        Iterator actors = _actorsToCheckNumberOfFirings.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            int[] flags = (int[])_actorsFlags.get(actor);
            flags[_NUMBER_OF_FIRINGS] = 0;
        }

        return true;
    }

    /** Override the base class method to transfer enough tokens to
     *  complete an internal iteration.  If there are not enough tokens,
     *  then throw an exception. It then updates enabling status for all
     *  inside opaque actors that receive data from this port.
     *  @exception IllegalActionException If the port is not an opaque
     *   input port, or if there are not enough input tokens available.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferInputs(IOPort port)
            throws IllegalActionException {
        if (_debugging) {
            _debug("Calling transferInputs on port: " + port.getFullName());
        }
        if (!port.isInput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "Attempted to transferInputs on a port is not an " +
                    "opaque input port.");
        }
        boolean wasTransferred = false;

        int[] rate = _getTokenConsumptionRate(port);
        for (int i = 0; i < port.getWidth(); i++) {
            try {
                for (int k = 0; k < rate[i]; k++) {
                    if (port.hasToken(i)) {
                        Token t = port.get(i);
                        if (_debugging) _debug(getName(),
                                "transferring input from channel " + i
                                + " of input port "   + port.getName());
                        port.sendInside(i, t);
                        wasTransferred = true;
                    } else {
                        throw new IllegalActionException(this, port,
                                "Channel " + i + "should consume "
                                + rate[i]
                                + " tokens, but there were only "
                                + k
                                + " tokens available.");
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
            IOPort insideSinkPort = (IOPort)insideSinkPorts.next();
            Actor actor = (Actor)insideSinkPort.getContainer();
            // Skip it if the actor to be checked contains this director.
            // In other words, the data directly go to output port instead
            // of any inside actors.
            if (getContainer() != actor) {
                int[] flags = (int[])_actorsFlags.get(actor);
                flags[_ENABLING_STATUS] = _actorStatus(actor);
            }
        }
        return wasTransferred;
    }

    /** Override the base class method to transfer enough tokens to
     *  fulfill the output production rate. If there are not enough
     *  tokens, then throw an exception.
     *  @exception IllegalActionException If the port is not an opaque
     *   output port, or if there are not enough output tokens available.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
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
                for (int k = 0; k < rate[i]; k++) {
                    if (port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        if (_debugging) _debug(getName(),
                                "transferring output from channel " + i
                                + " of port " + port.getName());
                        port.send(i, t);
                        wasTransferred = true;
                    } else {
                        throw new IllegalActionException(this, port,
                                "Channel " + i + " should produce " + rate[i]
                                + " tokens, but there were only "
                                + k + " tokens available.");
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

    /** Check actor enabling status. It could be in one of three statuses:
     *  _NOT_ENABLED, _ENABLED_DEFERRABLE, _ENABLED_NOT_DEFERRABLE.
     *  @param actor The actor to be checked.
     *  @return An int indicating actor enabing status.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    protected int _actorStatus(Actor actor)
            throws IllegalActionException {

        if (!_isEnabled(actor))
            return _NOT_ENABLED;

        if (_isDeferrable(actor))
            return _ENABLED_DEFERRABLE;

        return _ENABLED_NOT_DEFERRABLE;
    }

    /** Iterate the actor once. Increment the firing number for it.
     *  Update the enabling status for each connected actor as well
     *  as itself.
     *  @param actor The actor to be fired.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If any called method throws
     *  IllegalActionException or actor is not ready.
     */
    protected int _fireActor(Actor actor) throws IllegalActionException {
        if (_debugging) {
            _debug(new FiringEvent(this, actor, FiringEvent.BEFORE_ITERATE));
        }
        // Iterate once.
        int returnValue = actor.iterate(1);
        if (_debugging) {
            _debug(new FiringEvent(this, actor, FiringEvent.AFTER_ITERATE));
        }
        if (returnValue == STOP_ITERATING) {
            _activeActors.remove(actor);
            _actorsToCheckNumberOfFirings.remove(actor);
        } else if (returnValue == NOT_READY) {
            throw new IllegalActionException(this,
                    (ComponentEntity)actor, "Actor " +
                    "is not ready to fire.");
        }

        // At least one actor has been fired in this basic iteration.
        _firedOne = true;

        // Increment the firing number.
        int[] flags = (int[])_actorsFlags.get(actor);
        flags[_NUMBER_OF_FIRINGS]++;

        // Update enabling status for each connected actor.
        Iterator ports = ((Entity)actor).portList().iterator();
        while (ports.hasNext()) {
            ComponentPort port = (ComponentPort)ports.next();
            Iterator deepConnectedPorts =
                port.deepConnectedPortList().iterator();
            while (deepConnectedPorts.hasNext()) {
                Port deepConnectedPort = (Port)deepConnectedPorts.next();
                Actor connectedActor = (Actor)deepConnectedPort.getContainer();
                // Skip it if the connectedActor to be checked contains
                // this director.
                if (getContainer() != connectedActor) {
                    int[] containerFlags =
                        (int[])_actorsFlags.get(connectedActor);
                    containerFlags[_ENABLING_STATUS] =
                        _actorStatus(connectedActor);
                }
            }
        }

        // Update enabling status for this actor.
        flags[_ENABLING_STATUS] = _actorStatus(actor);

        return returnValue;
    }

    /** Check each remote receiver to see if the number of tokens in the
     *  receiver is greater than or equal to the tokenConsumptionRate of
     *  the containing port. The actor is deferrable if the above test is
     *  true for any receiver. At the same time, find the maximum number
     *  of tokens in all receivers, which is used to find minimax actor
     *  later on.
     *  @param actor The actor to be checked.
     *  @return true if the actor is deferrable, false if not.
     *  @throws IllegalActionException If any called method throws
     *  IllegalActionException.
     */
    protected boolean _isDeferrable(Actor actor)
            throws IllegalActionException {

        boolean deferrable = false;
        int maxSize = 0;

        Iterator outputPorts = actor.outputPortList().iterator();
        while (outputPorts.hasNext()) {

            IOPort outputPort = (IOPort)outputPorts.next();
            Receiver[][] farReceivers = outputPort.getRemoteReceivers();
            for (int i = 0; i < farReceivers.length; i++)
                for (int j = 0; j < farReceivers[i].length; j++) {
                    SDFReceiver farReceiver = (SDFReceiver)farReceivers[i][j];
                    IOPort port = farReceiver.getContainer();

                    // Having a self-loop doesn't make it deferrable.
                    if (port == outputPort)
                        continue;

                    // The defalult vaule for tokenConsumptionRate is 1.
                    int tokenConsumptionRate = 1;
                    Variable rate = null;
                    if (port.isInput()) {
                        rate = SDFUtilities.getRateVariable(port,
                                "tokenConsumptionRate");
                    }
                    // If DDF domain is inside another domain and the
                    // farReceiver is contained by an opaque output port...
                    if (port.isOutput()) {
                        rate = SDFUtilities.getRateVariable(port,
                                "tokenProductionRate");
                    }
                    if (rate != null) {
                        Token token = rate.getToken();
                        if (token instanceof ArrayToken) {
                            Token[] tokens = ((ArrayToken)token).arrayValue();
                            // Scan the contained receivers of the remote
                            // port to find out channel index.
                            Receiver[][] portReceivers =
                                port.getReceivers();
                            int channelIndex = 0;
                            foundChannelIndex:
                            for (int m = 0; m < portReceivers.length; m++)
                                for (int n = 0; n < portReceivers[m].length;
                                     n++)
                                    if (farReceiver == portReceivers[m][n]) {
                                        channelIndex = m;
                                        break foundChannelIndex;
                                    }
                            tokenConsumptionRate =
                                ((IntToken)tokens[channelIndex]).intValue();
                        } else {
                            tokenConsumptionRate = ((IntToken)token).intValue();
                        }
                    }
                    if (farReceiver.size() >= tokenConsumptionRate) {
                        deferrable = true;
                    }

                    // Here we find the maximum number of tokens in all
                    // receivers at the same time checking deferrability.
                    // The advantage of this is that it only adds a small
                    // additional operation for now. If later on we need
                    // this information, we don't need to do traversing
                    // again. The disadvantage is that 1) we can return
                    // from this method as soon as deferrable = true if we
                    // don't perform this additional operation. 2) We will
                    // not need this information if it turns out not all
                    // enabled actors are deferrable. Therefore another
                    // approach is to perform this operation only when needed,
                    // i.e., when all enabled actor are deferrable.
                    if (farReceiver.size() > maxSize) {
                        maxSize = farReceiver.size();
                    }
                }
        }

        if (deferrable) {
            int[] flags = (int[])_actorsFlags.get(actor);
            flags[_MAX_NUMBER_OF_TOKENS] = maxSize;
        }

        return deferrable;
    }

    /** The actor is enabled if the tokenConsumptionRate on each input port
     *  is satisfied by all receivers contained by this port.
     *  @param actor The actor to be checked.
     *  @return true if the actor is enabled, false if not.
     *  @throws IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    protected boolean _isEnabled(Actor actor)
            throws IllegalActionException {
        Iterator inputPorts = actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();

            //The defalult vaule for tokenConsumptionRate is 1.
            int tokenConsumptionRate = 1;
            Variable parameter = SDFUtilities.getRateVariable(
                    inputPort, "tokenConsumptionRate");

            if (parameter != null) {
                Token token = parameter.getToken();
                // If token is Arraytoken, then each channel has a
                // corresponding tokenConsumptionRate in the array.
                if (token instanceof ArrayToken) {
                    Token[] tokens = ((ArrayToken)token).arrayValue();
                    if (tokens.length  < inputPort.getWidth())
                        throw new IllegalActionException(this,
                                (ComponentEntity)actor, "The length of " +
                                "tokenConsumptionRate array is less than " +
                                "port width.");
                    for (int i = 0; i < inputPort.getWidth(); i++) {
                        int channelRate = ((IntToken)tokens[i]).intValue();
                        if (!inputPort.hasToken(i, channelRate)) {
                            return false;
                        }
                    }
                    return true;

                } else { // All the channels in the port has same
                         // tokenConsumptionRate.
                    tokenConsumptionRate = ((IntToken)token).intValue();
                }
            }

            for (int i = 0; i < inputPort.getWidth(); i++) {
                if (!inputPort.hasToken(i, tokenConsumptionRate)) {
                    return false;
                }
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Get token consumption rate for the given port.
     *  @param port The port to get token consumption rate.
     *  @return An int array of token consumption rate.
     *  @throws IllegalActionException If parameter throws it or the
     *   length of tokenConsumptionRate array is less than port width.
     */
    private int[] _getTokenConsumptionRate(IOPort port)
            throws IllegalActionException {
        int[] rate = new int[port.getWidth()];
        Arrays.fill(rate, 1);
        Variable parameter = SDFUtilities.getRateVariable(port,
                "tokenConsumptionRate");
        if (parameter != null) {
            Token token = parameter.getToken();
            // If token is Arraytoken, then each channel has a
            // corresponding input rate in the array.
            if (token instanceof ArrayToken) {
                Token[] tokens = ((ArrayToken)token).arrayValue();
                if (tokens.length < port.getWidth())
                    throw new IllegalActionException(this, "The length of " +
                            "tokenConsumptionRate array is less than " +
                            "port width.");
                for (int i = 0; i < port.getWidth(); i++)
                    rate[i] = ((IntToken)tokens[i]).intValue();
            } else { // All the channels in the port has same
                     // tokenConsumptionRate.
                Arrays.fill(rate, ((IntToken)token).intValue());
            }
        }
        return rate;
    }


    /** Get token production rate for the given port.
     *  @param port The port to get token production rate.
     *  @return An int array of token production rate.
     *  @throws IllegalActionException If parameter throws it
     *   or the length of tokenProductionRate array is less
     *   than port inside width.
     */
    private int[] _getTokenProductionRate(IOPort port)
            throws IllegalActionException {
        int[] rate = new int[port.getWidthInside()];
        Arrays.fill(rate, 1);
        Variable parameter = SDFUtilities.getRateVariable(port,
                "tokenProductionRate");
        if (parameter != null) {
            Token token = parameter.getToken();
            // If token is Arraytoken, then each channel has a
            // corresponding output rate in the array.
            if (token instanceof ArrayToken) {
                Token[] tokens = ((ArrayToken)token).arrayValue();
                if (tokens.length < port.getWidthInside())
                    throw new IllegalActionException(this, "The length of " +
                            "tokenProductionRate array is less than " +
                            "port inside width.");
                for (int i = 0; i < port.getWidthInside(); i++)
                    rate[i] = ((IntToken)tokens[i]).intValue();
            } else { // All the channels in the port has same
                     // tokenProductionRate.
                Arrays.fill(rate, ((IntToken)token).intValue());
            }
        }
        return rate;
    }

    /** Initialize the object. In this case, we give the DDFDirector
     *  an iterations parameter with default value zero and a 
     *  runUntilDeadlock parameter with default value false.
     */
    private void _init()
            throws IllegalActionException, NameDuplicationException {
        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);
        
        runUntilDeadlock = new Parameter(this, "runUntilDeadlock");
        runUntilDeadlock.setTypeEquals(BaseType.BOOLEAN);
        runUntilDeadlock.setToken(new BooleanToken(false));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A flag indicating whether at least one actor has been fired so far.
    private boolean _firedOne = false;

    // The number of iterations.
    private int _iterationCount = 0;

    // The value that the postfire method will return.
    private boolean _postfireReturns;

    // HashMap containing actor flags. Each actor maps to an array of
    // four integers, representing enablingStatus, numberOfFirings,
    // maxNumberOfTokens and requiredFiringsPerIteration.
    private HashMap _actorsFlags = new HashMap();

    // The list of active actors.
    private LinkedList _activeActors;
    
    //  A boolean initialized with value in the parameter runUntilDeadlock.
    private boolean _runUntilDeadlock;

    // To store those actors for which positive requiredFiringsPerIteration
    // has been defined.
    // We could save more space by making it a HashMap and storing
    // requiredFiringsPerIterationa and numberOfFirings in this
    // variable instead of in _actorsFlags.
    private LinkedList _actorsToCheckNumberOfFirings = new LinkedList();

    // An indicator that the actor is enabled and deferrable.
    private static final int _ENABLED_DEFERRABLE = 2;

    // An indicator that the actor is enabled and not deferrable.
    private static final int _ENABLED_NOT_DEFERRABLE = 1;

    // An indicator that the actor is not enabled.
    private static final int _NOT_ENABLED = 0;

    // Indexes into an array of actor flags contained by a HashMap.
    private static final int _ENABLING_STATUS = 0;
    private static final int _NUMBER_OF_FIRINGS = 1;
    private static final int _MAX_NUMBER_OF_TOKENS = 2;
    private static final int _REQUIRED_FIRINGS_PER_ITERATION = 3;
}
