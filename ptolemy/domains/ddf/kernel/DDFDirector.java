/* Director for the dynamic dataflow model of computation.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
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
   particular actor must be fired in a single iteration.
   <p>
   The algorithm implementing one basic iteration goes like this:
   <pre>
   E = set of enabled actors
   D = set of deferrable enabled actors
   F = set of actors that have fired once already in this one iteration
   </pre>

   One default iteration consists of:
   <pre>
   if (E-D != 0) fire(E-D)
   else if (D != 0) fire minimax(D)
   else deadlocked.
   </pre>

   The function "minimax(D)" returns the one actor with the smallest
   maximum number of tokens on its output paths.

   Based on DDFSimpleSched in Ptolemy Classic, by Edward Lee.

   @author Gang Zhou
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
     *  postfire, and thus the execution can continue forever.
     *  The default value is an IntToken with the value zero.
     */
    public Parameter iterations;

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

            // The variable to store minimax actor.
            Actor minimaxActor = null;
            int minimaxSize = Integer.MAX_VALUE;

            // The List to store actors that are enabled and not deferrable.
            List toBeFiredActors = new LinkedList();

            Iterator actors = _activeActors.iterator();
            while (actors.hasNext()) {
            	
                // Scan all actors to find all enabled and not 
                // deferrable actors.
                Actor actor = (Actor)actors.next();
                int[] flags = (int[])_actorsFlags.get(actor);
                int canFire = flags[_enablingStatus];
                if (canFire == _ENABLED_NOT_DEFERRABLE) {
                    toBeFiredActors.add(actor);
                }

                // Find the minimax actor.
                if (canFire == _ENABLED_DEFERRABLE) {
                    int newSize = flags[_maxNumberOfTokens];
                    if (newSize < minimaxSize) {
                        minimaxSize = newSize;
                        minimaxActor = actor;
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

            // If no actor has been fired, fire the minimax actor.
            if (!_firedOne && minimaxActor != null) {
                _fireActor(minimaxActor);
            }

            // If still no actor has been fired, declare deadlock.
            if(!_firedOne) {
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
                int requiredFirings = flags[_requiredFiringsPerIteration];
                int firingsDone = flags[_numberOfFirings];
                if (firingsDone < requiredFirings) {
                    repeatBasicIteration = true;
                    break;
                }
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
            flags[_enablingStatus] = _actorStatus(actor);

            // Determine required requiredFiringsPerIteration for each actor.
            // The default vaule 0 means no requirement on this actor.
            flags[_requiredFiringsPerIteration] = 0;
            Variable requiredFiringsPerIteration = (Variable)((Entity)actor).
                    getAttribute("requiredFiringsPerIteration");
            if (requiredFiringsPerIteration != null) {
                Token token = requiredFiringsPerIteration.getToken();
                if(token instanceof IntToken) {
                    int value = ((IntToken)token).intValue();
                    if (value > 0)
                        flags[_requiredFiringsPerIteration] = value;
                        _actorsToCheckNumberOfFirings.add(actor);
                } else
                    throw new IllegalActionException(this,
                            (ComponentEntity)actor, "The variable " +
                            "requiredFiringsPerIteration must contain " +
                            "an IntToken.");
            }
        }
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

    /** Initialize numberOfFirings to zero for those actors for which 
     *  positive requiredFiringsPerIteration has been defined.
     *  Assume that the director is always ready to be fired,
     *  so return true because super.prefire() returns true.
     *  @return True.
     *  @exception IllegalActionException Not thrown in this class.
     */
    public boolean prefire() throws IllegalActionException {
    	if (_debugging) {
            _debug("\niterationCount " + _iterationCount); 
        }
        
        Iterator actors = _actorsToCheckNumberOfFirings.iterator();
        while (actors.hasNext()) {
            Actor actor = (Actor)actors.next();
            int[] flags = (int[])_actorsFlags.get(actor);
            flags[_numberOfFirings] = 0;
        }
        return super.prefire();
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
     *  @throws IllegalActionException If any called method throws
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
        flags[_numberOfFirings]++;

        // Update enabling status for each connected actor.
        Iterator ports = ((Entity)actor).portList().iterator();
        while (ports.hasNext()) {
            ComponentPort port = (ComponentPort)ports.next();
            Iterator deepConnectedPorts =
                    port.deepConnectedPortList().iterator();
            while (deepConnectedPorts.hasNext()) {
                Port deepConnectedPort = (Port)deepConnectedPorts.next();
                Actor container = (Actor)deepConnectedPort.getContainer();
                int[] containerFlags = (int[])_actorsFlags.get(container);
                containerFlags[_enablingStatus] = _actorStatus(container);
            }
        }

        // Update enabling status for this actor. 
        flags[_enablingStatus] = _actorStatus(actor);

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
            for (int i=0; i < farReceivers.length; i++)
                for (int j=0; j < farReceivers[i].length; j++) {
                    SDFReceiver farReceiver = (SDFReceiver)farReceivers[i][j];
                    IOPort port = farReceiver.getContainer();

                    // Having a self-loop doesn't make it deferrable.
                    if (port == outputPort)
                        continue;

                    // The defalult vaule for tokenConsumptionRate is 1.
                    int tokenConsumptionRate = 1;
                    Parameter parameter = (Parameter)port.
                                      getAttribute("tokenConsumptionRate");
                    // Ports of opaque SDF composite actors contain 
                    // parameters named "_tokenConsumptionRate" given 
                    // by inside scheduler.
                    if (parameter == null) {
                        parameter = (Parameter)port.
                                getAttribute("_tokenConsumptionRate");
                    }
                    if (parameter != null) {
                        Token token = parameter.getToken();
                        if (token instanceof ArrayToken) {
                            Token[] tokens = ((ArrayToken)token).arrayValue();
                            // Scan the contained receivers of the remote 
                            // port to find out channel index.
                            Receiver[][] portReceivers =
                                    port.getReceivers();
                            int channelIndex = 0;
                            foundChannelIndex:
                            for (int m=0; m < portReceivers.length; m++)
                                for (int n=0; n < portReceivers[m].length; n++)
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
            flags[_maxNumberOfTokens] = maxSize;
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
            Parameter parameter =
                    (Parameter)inputPort.getAttribute("tokenConsumptionRate");
            // Ports of opaque SDF composite actors contain parameters 
            // named "_tokenConsumptionRate" given by inside scheduler.
            if (parameter == null) {
                parameter = (Parameter)inputPort.
                        getAttribute("_tokenConsumptionRate");
            }
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

    /** Initialize the object. In this case, we give the DDFDirector
     *  an iterations parameter.
     */
    private void _init()
	        throws IllegalActionException, NameDuplicationException {
        iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);
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
    private static final int _enablingStatus = 0;
    private static final int _numberOfFirings = 1;
    private static final int _maxNumberOfTokens = 2;
    private static final int _requiredFiringsPerIteration = 3;
}