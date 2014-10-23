/* OptimalScheduleFinder is a strategy object to compute an optimal scheduler
 * for an OptimizedSDFScheduler

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.domains.sdf.optimize;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.Firing;
import ptolemy.actor.sched.Schedule;
import ptolemy.actor.util.DFUtilities;
import ptolemy.domains.sdf.optimize.OptimizingSDFDirector.OptimizationCriteria;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////OptimalScheduleFinder

/**
<h1>Class comments</h1>
An OptimalScheduleFinder encapsulates an algorithm to find an optimized schedule.
In particular it implements a simple state space exploration algorithm to find a
minimum buffer size schedule.
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector},
{@link ptolemy.domains.sdf.optimize.OptimizingSDFScheduler} and
{@link ptolemy.domains.sdf.optimize.BufferingProfile} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.OptimizingSDFScheduler
@see ptolemy.domains.sdf.optimize.BufferingProfile

@author Marc Geilen
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
 */

public class OptimalScheduleFinder {

    /**
     * Construct an instance of the OptimalScheduleFinder. Creates an object
     * associated with the OptimizingSDFSchedule <i>scheduler</i> and using
     * the optimization criterion <i>criterion</i> to find an optimized schedule.
     * @param scheduler scheduler
     * @param criterion optimization criterion
     */
    public OptimalScheduleFinder(OptimizingSDFScheduler scheduler,
            OptimizationCriteria criterion) {
        _myScheduler = scheduler;
        _optimizationCriterion = criterion;
    }

    /**
     * Make a schedule using a greedy (non-optimizing algorithm).
     * @param firingVector repetition vector
     * @return the computed schedule
     */
    public Schedule makeScheduleGreedy(Map firingVector) {
        Schedule result = null;
        try {
            // instantiate the model
            _instantiateAnalysisModel(firingVector);

            // determine the state vector indices
            _setStateIndices();

            // run a greedy exploration
            // keeping toExplore sorted on maximal progress makes it greedy
            _SortedSetOfStates toExplore = new _SortedSetOfStates(
                    new _StateComparatorMaximumProgress());
            // add the initial state
            toExplore.add(initialState());
            // store the end state when found
            _State optimalEndState = null;
            // to be set to true as soon as end state is found
            boolean scheduleFound = false;

            // continue searching until a schedule has been found
            while (!scheduleFound && !toExplore.isEmpty()) {
                // take the first state to further explore from our sorted list
                _State state = toExplore.removeFirstState();
                // test if it is an end state, in which case we are ready.
                if (state.isEndState()) {
                    // found !!
                    optimalEndState = state;
                    scheduleFound = true;
                } else {
                    // try all possible actions (actor firings) enabled from this state
                    Iterator actorIterator = _actors.iterator();
                    while (actorIterator.hasNext()) {
                        // for each actor a ...
                        _Actor actor = (_Actor) actorIterator.next();
                        // check first if 'actor' is exclusively enabled to give
                        // preference to exclusive firing
                        // (although perhaps we should not be assuming that from the sorted set)
                        if (actor.isExclusiveEnabled(state)) {
                            // it is enabled. Create a new state accordingly
                            _State newState = state.clone(state);
                            // fire the actor
                            actor.fireExclusive(newState);
                            // update the value to be optimized depending on the optimization criterion
                            if (_optimizationCriterion == OptimizationCriteria.BUFFERS) {
                                newState.value = Math.max(
                                        _channels.channelSize(newState)
                                        + actor.exclusiveBuffers,
                                        newState.value);
                            } else if (_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME) {
                                newState.value = state.value
                                        + actor.exclusiveExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(newState);
                        }
                        // try also firing non-exclusive
                        // check if a is enabled for a shared firing
                        if (actor.isEnabled(state)) {
                            // it is enabled. Create a new state accordingly
                            _State newState = state.clone(state);
                            // fire the actor
                            actor.fire(newState);
                            // update the value to be optimized depending on the optimization criterion
                            if (_optimizationCriterion == OptimizationCriteria.BUFFERS) {
                                newState.value = Math.max(
                                        _channels.channelSize(newState)
                                        + actor.sharedBuffers,
                                        newState.value);
                            } else if (_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME) {
                                newState.value = state.value
                                        + actor.sharedExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(newState);
                        }
                    }
                }
            }
            // Completed the search. Did we find anything?
            if (scheduleFound) {
                // yes, then turn it into a schedule.
                result = _buildSchedule(optimalEndState);
            }

        } catch (IllegalActionException exception) {
            // oops, something happened...
        }
        // return the schedule
        return result;
    }

    /**
     * Make a schedule using an exhaustive BFS-like optimizing algorithm.
     * @param firingVector repetition vector
     * @return the computed schedule
     */
    public Schedule makeSchedule(Map firingVector) {
        Schedule result = null;
        try {
            // instantiate the model
            _instantiateAnalysisModel(firingVector);

            // determine the state vector indices
            _setStateIndices();

            // run a state-space exploration
            // keeping toExplore sorted on memory usage ensures that the minimum memory
            // schedule is found first.
            _SortedSetOfStates toExplore = new _SortedSetOfStates();
            // add the initial state
            toExplore.add(initialState());
            // store the end state when found
            _State optimalEndState = null;
            // to be set to true as soon as end state is found
            boolean scheduleFound = false;

            // continue searching until a schedule has been found
            while (!scheduleFound && !toExplore.isEmpty()) {
                // take the first state to further explore from our sorted list
                _State state = toExplore.removeFirstState();
                // test if it is an end state, in which case we are ready.
                if (state.isEndState()) {
                    // found !!
                    optimalEndState = state;
                    scheduleFound = true;
                } else {
                    // try all possible actions (actor firings) enabled from this state
                    Iterator actorIterator = _actors.iterator();
                    while (actorIterator.hasNext()) {
                        // for each actor a ...
                        _Actor actor = (_Actor) actorIterator.next();
                        // check first if a is exclusively enabled to give
                        // preference to exclusive firing
                        // (although perhaps we should not be assuming that from the sorted set)
                        if (actor.isExclusiveEnabled(state)) {
                            // it is enabled. Create a new state accordingly
                            _State newState = state.clone(state);
                            // fire the actor
                            actor.fireExclusive(newState);
                            // update the value to be optimized depending on the optimization criterion
                            if (_optimizationCriterion == OptimizationCriteria.BUFFERS) {
                                newState.value = Math.max(
                                        _channels.channelSize(newState)
                                        + actor.exclusiveBuffers,
                                        newState.value);
                            } else if (_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME) {
                                newState.value = state.value
                                        + actor.exclusiveExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(newState);
                        }
                        // try also firing non-exclusive
                        // check if a is enabled for a shared firing
                        if (actor.isEnabled(state)) {
                            // it is enabled. Create a new state accordingly
                            _State newState = state.clone(state);
                            // fire the actor
                            actor.fire(newState);
                            // update the value to be optimized depending on the optimization criterion
                            if (_optimizationCriterion == OptimizationCriteria.BUFFERS) {
                                newState.value = Math.max(
                                        _channels.channelSize(newState)
                                        + actor.sharedBuffers,
                                        newState.value);
                            } else if (_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME) {
                                newState.value = state.value
                                        + actor.sharedExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(newState);
                        }
                    }
                }
            }
            // Completed the search. Did we find anything?
            if (scheduleFound) {
                // yes, then turn it into a schedule.
                result = _buildSchedule(optimalEndState);
            }

        } catch (IllegalActionException e) {
            // oops, something happened...
        }
        // return the schedule
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                 protected fields                                  ////

    /**
     * Instantiate the analysis model from the core model.
     * @param firingVector contains repetition vector information
     * @exception IllegalActionException if model information inconsistent
     */
    protected void _instantiateAnalysisModel(Map firingVector)
            throws IllegalActionException {

        // initialize lists and maps
        _actors = new _ListOfActors();
        _actorMap = new _TwoWayHashMap();
        _channels = new _ListOfChannels();
        _channelMap = new _TwoWayHashMap();

        // create the actors
        Iterator actorIterator = firingVector.entrySet().iterator();
        while (actorIterator.hasNext()) {
            int sharedBuffers, exclusiveBuffers, sharedExecutionTime, exclusiveExecutionTime;
            Map.Entry pair = (Map.Entry) actorIterator.next();
            ptolemy.actor.Actor actor = (ptolemy.actor.Actor) pair.getKey();
            // if it is an actor which implements our BufferingProfile
            if (actor instanceof BufferingProfile) {
                BufferingProfile actorWithBufferingProfile = (BufferingProfile) actor;
                // set the profile accordingly
                sharedBuffers = actorWithBufferingProfile.sharedBuffers();
                exclusiveBuffers = actorWithBufferingProfile.exclusiveBuffers();
                sharedExecutionTime = actorWithBufferingProfile
                        .sharedExecutionTime();
                exclusiveExecutionTime = actorWithBufferingProfile
                        .exclusiveExecutionTime();
            } else {
                // set the profile to default values
                sharedBuffers = 0;
                exclusiveBuffers = 0;
                sharedExecutionTime = 0;
                exclusiveExecutionTime = 0;
            }
            // create a model actor
            _Actor modelActor = new _Actor(actor.getName(),
                    (Integer) pair.getValue(), sharedBuffers, exclusiveBuffers,
                    sharedExecutionTime, exclusiveExecutionTime);
            // add the actor
            _actors.add(modelActor);
            // remember the link to the real actor
            _actorMap.put(modelActor, actor);

            // create a channel for every output port
            List portList = actor.outputPortList();
            Iterator portIterator = portList.iterator();
            while (portIterator.hasNext()) {
                TypedIOPort port = (TypedIOPort) portIterator.next();
                _Channel channel = new _Channel();
                // set the number of initial tokens
                channel.initialTokens = DFUtilities
                        .getTokenInitProduction(port);
                // add the channel to the list
                _channels.add(channel);
                // remember the link to the IOPort
                _channelMap.put(port, channel);
            }
        }

        // create the actor ports
        actorIterator = firingVector.keySet().iterator();
        while (actorIterator.hasNext()) {
            // for every actor...
            ptolemy.actor.Actor actor = (ptolemy.actor.Actor) actorIterator
                    .next();
            _Actor modelActor = (_Actor) _actorMap.getBW(actor);
            List portList = actor.outputPortList();
            Iterator portIterator = portList.iterator();
            while (portIterator.hasNext()) {
                // for every output port of the actor
                TypedIOPort port = (TypedIOPort) portIterator.next();
                // get the rate of the port
                int rate = DFUtilities.getRate(port);
                // get the channel of this port
                _Channel channel = (_Channel) _channelMap.getFW(port);
                // create the model port
                _Port modelPort = new _Port(rate, channel);
                // add the new port to the actor
                modelActor.addPort(modelPort);
            }
            portList = actor.inputPortList();
            portIterator = portList.iterator();
            while (portIterator.hasNext()) {
                // for every input port of the actor
                TypedIOPort port = (TypedIOPort) portIterator.next();
                // get the rate of the port
                int rate = DFUtilities.getRate(port);
                // get the producing IOPort
                List sourcePortList = port.sourcePortList();
                // input port may have more than one source if it is a multiport
                // make a port in this model for every input port connected
                for (int i = 0; i < sourcePortList.size(); i++) {
                    // get the channel of the producing port
                    _Channel channel = (_Channel) _channelMap
                            .getFW(sourcePortList.get(i));
                    // if the source port is not related to a channel, then it is an external port
                    // and we do not care about it.
                    if (channel != null) {
                        // otherwise set the rate (negative, because it is an input port)
                        _Port modelPort = new _Port(-rate, channel);
                        // add the new port to the actor
                        modelActor.addPort(modelPort);
                    }
                }
            }
        }
    }

    /**
     * the state of the channel in the channel array has one integer at stateIndex
     * indicating the number of tokens in the channel and another integer at stateIndex+1
     * indicating how many consumer are still to read the token
     * I need to remember per receiver how many tokens there are for that receiver.
     * If a token is produced, then the value increases for all consumers. When a consumer
     * reads it decreases only for that consumer.
     */
    protected static class _Channel {

        /**
         * Construct a instance of Channel and initialize nrConsumers to 0.
         */
        _Channel() {
            _numberOfConsumers = 0;
        }

        /**
         * The number of initial tokens on the channel.
         */
        int initialTokens;

        /**
         * Assign stateIndex and returns the next available index to be assigned
         * to the next channel, depending on the number of consumers of this channel.
         * @param index state index for the channel
         * @return index for the next component
         */
        int assignStateIndex(int index) {
            assert _numberOfConsumers > 0;
            _stateIndex = index;
            return index + _numberOfConsumers;
        }

        /**
         * Called for any input port to be connected to the channel. Returns an index
         * into the state vector for this port to manage the number of remaining tokens
         * to be read.
         * @return index for the new port
         */
        int assignPortIndex() {
            int nextIndex = _numberOfConsumers;
            _numberOfConsumers++;
            return nextIndex;
        }

        /**
         * Get the number of available tokens to the consuming port with index 'portIndex'
         * in state 'state'.
         * @param portIndex port index
         * @param state state
         * @return the number of available tokens in the channel to port with index i
         */
        int tokens(int portIndex, _State state) {
            return state.channelContent[_stateIndex + portIndex];
        }

        /**
         * Get the number of exclusively available tokens to the consuming port with
         * index 'portIndex' in state 'state'.
         * @param portIndex port index
         * @param state state
         * @return the number of exclusively available tokens in the channel to port with index i
         */
        int exclusiveTokens(int portIndex, _State state) {
            int myTokens = tokens(portIndex, state);
            int max = myTokens;
            // we can only use them exclusively if all others have already consumed them
            for (int j = 0; j < _numberOfConsumers; j++) {
                if (j != portIndex) {
                    max = Math.min(max, myTokens - tokens(j, state));
                }
            }
            if (max < 0) {
                max = 0;
            }
            return max;
        }

        /**
         * Add new tokens into the channel.
         * @param state state to be updated
         * @param rate number of tokens to add
         */
        void addTokens(_State state, int rate) {
            // add tokens for all consuming ports
            for (int i = 0; i < _numberOfConsumers; i++) {
                state.channelContent[_stateIndex + i] += rate;
            }
        }

        /**
         * Remove tokens from the channel for given port.
         * @param state state to be updated
         * @param portIndex port index
         * @param rate number of tokens to remove
         */
        void removeTokens(_State state, int portIndex, int rate) {
            state.channelContent[_stateIndex + portIndex] += rate; // rate is negative
        }

        /**
         * Initialize the state 'state' to this channel's initial state
         * by putting the appropriate number of initial tokens in it.
         * @param state state to be initialized
         */
        void setInitialState(_State state) {
            for (int j = 0; j < _numberOfConsumers; j++) {
                state.channelContent[_stateIndex + j] = initialTokens;
            }
        }

        /**
         * return the amount of memory taken by the channel content.
         * which is the maximum number of tokens still to be read by any
         * consumer
         * @param state state
         * @return amount of memory occupied by channel content
         */
        int channelSize(_State state) {
            int result = 0;
            for (int i = 0; i < _numberOfConsumers; i++) {
                result = Math.max(result, state.channelContent[i]);
            }
            return result;
        }

        /**
         * The number of actors consuming from this channel.
         */
        int _numberOfConsumers = -1;

        /**
         * Index of this channel into the global state vector.
         */
        int _stateIndex;
    }

    /**
     * A list of channels, based on LinkedList.
     */
    @SuppressWarnings("serial")
    protected static class _ListOfChannels extends LinkedList {

        /**
         * Count the overall memory taken by channels in the list in state 'state'.
         * @param state state
         * @return total memory size occupied by channels
         */
        int channelSize(_State state) {
            Iterator channelIterator = iterator();
            int result = 0;
            // iterate over channels in the list
            while (channelIterator.hasNext()) {
                _Channel channel = (_Channel) channelIterator.next();
                // add up channel size
                result += channel.channelSize(state);
            }
            return result;
        }
    }

    /**
     * A port of an actor, connected to a channel.
     * A port as a rate determining the number of tokens produced/consumed in
     * a firing of its actor.
     * Negative rates represent input ports, positive rates output ports.
     */
    protected static class _Port {

        /**
         * Construct an instance of _Port with rate <i>rate</i> and for channel
         * <i>channel</i>.
         * @param rate port rate (negative for input port)
         * @param channel channel to which it is bound
         */
        _Port(int rate, _Channel channel) {
            _rate = rate;
            _channel = channel;
        }

        /**
         * Assign index to the port into the channel state vector
         * only if it is an input port.
         */
        void assignIndex() {
            if (_rate < 0) {
                _portIndex = _channel.assignPortIndex();
            }
        }

        /**
         * test if the port is enabled, i.e. if the associated channel has enough
         * tokens on this port to fire in state s
         * @param state state
         * @return true if it is enabled
         */
        boolean isEnabled(_State state) {
            // an output port is always enabled
            if (_rate >= 0) {
                return true;
            }
            // otherwise check tokens, recall that rate is negative
            return _channel.tokens(_portIndex, state) + _rate >= 0;
        }

        /**
         * test if the port is enabled for exclusive firing, i.e. if the associated
         * channel has enough tokens on this port to fire exclusively in state s
         * @param state state
         * @return true if it is enabled
         */
        boolean isExclusiveEnabled(_State state) {
            // an output port is always enabled
            if (_rate >= 0) {
                return true;
            }
            // otherwise check tokens, recall that rate is negative
            return _channel.exclusiveTokens(_portIndex, state) + _rate >= 0;
        }

        /**
         * Fire the port by accounting the numbers of tokens.
         * @param state state to use for firing
         */
        void fire(_State state) {
            if (_rate > 0) {
                // producing port
                _channel.addTokens(state, _rate);
            } else {
                // consuming port
                _channel.removeTokens(state, _portIndex, _rate);
            }
        }

        /**
         * The index for this port into the channel's state vector.
         */
        int _portIndex;

        /**
         * the port rate, negative if input port
         */
        int _rate;

        /**
         * The channel associated with the port.
         */
        _Channel _channel;
    }

    /**
     * A list of ports, based on LinkedList.
     */
    @SuppressWarnings("serial")
    protected static class _ListOfPorts extends LinkedList {
    }

    /**
     * A model of an actor. Containing the firing profile, its count in the repetition
     * vector and a number of ports.
     */
    protected static class _Actor {

        /**
         * Construct an instance of Actor, providing its name, repetition vector
         * count and profile information.
         * @param name name for the actor
         * @param repetitionCount repetition vector entry of the actor
         * @param sharedBuffersNeeded number of frame buffers needed for shared firing
         * @param exclusiveBuffersNeeded number of frame buffers needed for exclusive firing
         * @param sharedExecutionTimeNeeded execution time needed for share firing
         * @param exclusiveExecutionTimeNeeded execution time needed for exclusive firing
         */
        protected _Actor(String name, int repetitionCount,
                int sharedBuffersNeeded, int exclusiveBuffersNeeded,
                int sharedExecutionTimeNeeded, int exclusiveExecutionTimeNeeded) {
            _name = name;
            _repetitionCount = repetitionCount;
            sharedBuffers = sharedBuffersNeeded;
            exclusiveBuffers = exclusiveBuffersNeeded;
            sharedExecutionTime = sharedExecutionTimeNeeded;
            exclusiveExecutionTime = exclusiveExecutionTimeNeeded;
            _ports = new _ListOfPorts();
        }

        /**
         * The number of frame buffers the actor requires in a shared firing.
         */
        int sharedBuffers;

        /**
         * The number of frame buffers the actor requires in an exclusive firing.
         */
        int exclusiveBuffers;

        /**
         * Execution time (estimate) for the actor for a shared firing.
         */
        int sharedExecutionTime;

        /**
         * Execution time (estimate) for the actor for an exclusive firing.
         */
        int exclusiveExecutionTime;

        /**
         * Assign stateIndex to actor and its ports and returns the index for
         * the next component.
         * @param index index to assign to the actor
         * @return index to assign to next actor
         */
        int assignStateIndex(int index) {
            // iterate over ports to assign index to ports in channel state vector
            Iterator portIterator = _ports.iterator();
            while (portIterator.hasNext()) {
                _Port port = (_Port) portIterator.next();
                port.assignIndex();
            }
            // index for actor
            _stateIndex = index;
            return index + 1;
        }

        /**
         * Test whether the actor is enabled for a shared firing in given state <i>state</i>.
         * @param state state
         * @return true if enabled
         */
        boolean isEnabled(_State state) {
            // no more firings needed in iteration
            if (state.actorContent[_stateIndex] == 0) {
                return false;
            }
            // test if all ports are enabled
            Iterator portIterator = _ports.iterator();
            while (portIterator.hasNext()) {
                _Port port = (_Port) portIterator.next();
                if (!port.isEnabled(state)) {
                    return false;
                }
            }
            // all are enabled, go ahead
            return true;
        }

        /**
         * Test whether the actor is enabled for an exclusive firing in given state <i>state</i>.
         * @param state state
         * @return true if enabled
         */
        boolean isExclusiveEnabled(_State state) {
            // no more firings needed in iteration
            if (state.actorContent[_stateIndex] == 0) {
                return false;
            }
            // test if all ports are enabled
            Iterator portIterator = _ports.iterator();
            while (portIterator.hasNext()) {
                _Port port = (_Port) portIterator.next();
                if (!port.isExclusiveEnabled(state)) {
                    return false;
                }
            }
            // all are enabled, go ahead
            return true;
        }

        /**
         * adapt state 'state' according to a shared firing. Assumes it is enabled.
         * @param state state
         */
        void fire(_State state) {
            // fire all ports
            Iterator portIterator = _ports.iterator();
            while (portIterator.hasNext()) {
                _Port port = (_Port) portIterator.next();
                port.fire(state);
            }
            // one less firing remaining in iteration
            state.actorContent[_stateIndex]--;
            // remember in the state which actor fired
            state.firingActor = this;
            // mark that the firing is shared, not exclusive
            state.firingExclusive = false;
        }

        /**
         * adapt state 'state' according to an exclusive firing. Assumes it is enabled.
         * @param state state
         */
        void fireExclusive(_State state) {
            // fire all ports
            Iterator portIterator = _ports.iterator();
            while (portIterator.hasNext()) {
                _Port port = (_Port) portIterator.next();
                port.fire(state);
            }
            // one less firing remaining in iteration
            state.actorContent[_stateIndex]--;
            // remember in the state which actor fired
            state.firingActor = this;
            // mark that the firing is shared, not exclusive
            state.firingExclusive = true;
        }

        /**
         * Add a port to the actor.
         * @param port port to add
         */
        void addPort(_Port port) {
            _ports.add(port);
        }

        /**
         * Initialize state of to initial state of the network. Specifically, set
         * the count for this actor to the number of firings in one iteration.
         * @param state state to initialize
         */
        void setInitialState(_State state) {
            state.actorContent[_stateIndex] = _repetitionCount;
        }

        /**
         * index for the actor into the global state vector
         */
        int _stateIndex;

        /**
         * A list of ports of the actor, both input and output ports.
         */
        _ListOfPorts _ports;

        /**
         * The name of the actor.
         */
        String _name;

        /**
         * Count for the actor in the repetition vector of the graph.
         */
        int _repetitionCount;
    }

    /**
     * A list of actors, derived from LinkedList.
     */
    @SuppressWarnings("serial")
    protected static class _ListOfActors extends LinkedList {
    }

    /**
     * State models a global state of the SDF graph and remembers the actor that was
     * fired to reach it.
     * It consists of two integer array containing the channel content and
     * the actor states (remaining number of firings to complete an iteration)
     * respectively.
     * Moreover, it memorizes the firing to reach the state, which actor fired and
     * whether the firing was exclusive. A state is typically first cloned from its
     * predecessor state and then an actor firing is executed on it.
     * It also maintains a link to its predecessor state. This way the path to reach
     * this state can be reconstructed. It also maintain an optimization 'value'
     * associated with the path leading to this state. This value is used for
     * optimization.
     */
    protected static class _State {

        /**
         * Construct an instance of State, with a reference to a previous state.
         * @param thePreviousState link to previous state
         */
        _State(_State thePreviousState) {
            previousState = thePreviousState;
        }

        /**
         * Create new state by cloning state 'state' and marking
         * s as its predecessor state.
         * @param state state to clone
         * @return the new state
         */
        _State clone(_State state) {
            _State result = new _State(state);
            // copy global graph state
            result.channelContent = channelContent.clone();
            result.actorContent = actorContent.clone();
            // copy value
            result.value = value;
            return result;
        }

        /**
         * true if the firing to reach the state was exclusive.
         */
        boolean firingExclusive;

        /**
         * The actor that was fired to reach this state.
         * remains nil for the initial state.
         */
        _Actor firingActor;

        /**
         * Link to the previous state.
         */
        _State previousState;

        // actual state information
        /**
         * Array to store all channel content.
         * Channels and input ports within those channels are given their unique
         * index into this array.
         */
        int[] channelContent;

        /**
         * Array to store all actor content, remaining number of firings.
         * Actors are given their unique index into this array.
         */
        int[] actorContent;

        /**
         * Value to be optimized, smaller is better.
         */
        int value;

        /**
         * Test whether this is a valid end state, i.e. whether all actors have
         * completed their required number of firings.
         * @return true is end state
         */
        boolean isEndState() {
            // test all actors
            for (int element : actorContent) {
                if (element > 0) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Determine the number of remaining firings.
         * @return number of remaining firings
         */
        int getFiringsToCompletion() {
            int result = 0;
            for (int element : actorContent) {
                result += element;
            }
            return result;
        }

    }

    /**
     * A set of states, based on HashSet.
     */
    @SuppressWarnings("serial")
    protected static class _SetOfStates extends HashSet {
    }

    /**
     * An abstract super class for Comparators to maintain a sorted
     * list of states.
     */
    @SuppressWarnings("serial")
    protected static abstract class _StateComparator implements Comparator,
    Serializable {
    }

    /**
     * A Comparator to maintain a sorted list of states, sorted on their value.
     */
    @SuppressWarnings("serial")
    protected static class _StateComparatorLowestValue extends _StateComparator {

        /**
         * compare two states on their value. If values tie, then sort
         * on arbitrary other criteria
         * @param o1 first object to compare
         * @param o2 second object to compare
         * @return -1 if o1<o2, +1 if o1>o2 0 otherwise
         */
        @Override
        public int compare(Object o1, Object o2) {
            _State state1 = (_State) o1;
            _State state2 = (_State) o2;
            // sort on value
            if (state1.value != state2.value) {
                return state1.value - state2.value;
            } else {
                // value tie. compare channel state
                for (int i = 0; i < state1.channelContent.length; i++) {
                    if (state1.channelContent[i] != state2.channelContent[i]) {
                        return state1.channelContent[i]
                                - state2.channelContent[i];
                    }
                }
                // still no difference, compare actor state
                for (int i = 0; i < state1.actorContent.length; i++) {
                    if (state1.actorContent[i] != state2.actorContent[i]) {
                        return state1.actorContent[i] - state2.actorContent[i];
                    }
                }
                // they are really equal
                return 0;
            }
        }
    }

    /**
     * A Comparator to maintain a sorted list of states, sorted on their
     * progress to the final state.
     */
    @SuppressWarnings("serial")
    protected static class _StateComparatorMaximumProgress extends
    _StateComparator {

        /**
         * Construct an instance of StateComparatorMaximumProgress. It creates
         * a secondary comparator based on StateComparatorLowestValue.
         */
        _StateComparatorMaximumProgress() {
            _backupComparator = new _StateComparatorLowestValue();
        }

        /**
         * Compare the states based on smallest number of remaining firings.
         * @param o1 first object to compare
         * @param o2 second object to compare
         * @return -1 if o1<o2, +1 if o1>o2 0 otherwise
         */
        @Override
        public int compare(Object o1, Object o2) {
            _State state1 = (_State) o1;
            _State state2 = (_State) o2;
            int progress1 = state1.getFiringsToCompletion();
            int progress2 = state2.getFiringsToCompletion();
            if (progress1 != progress2) {
                // least number of firings first
                return progress1 - progress2;
            } else {
                // tie, invoke backup comparator
                return _backupComparator.compare(state1, state2);
            }
        }

        /**
         * a secondary comparator to break a tie.
         */
        _StateComparator _backupComparator;
    }

    /**
     * A sorted set of states. Internally using a TreeSet.
     */
    protected static class _SortedSetOfStates {

        /**
         * Construct an instance of SortedSetOfStates. Creates the default
         * comparator and TreeSet. Default comparator sorts on lowest value.
         */
        _SortedSetOfStates() {
            _comparator = new _StateComparatorLowestValue();
            _treeSet = new TreeSet(_comparator);
        }

        /**
         * Construct an instance with an explicitly specified comparator.
         * @param comparator comparator
         */
        _SortedSetOfStates(_StateComparator comparator) {
            _comparator = comparator;
            _treeSet = new TreeSet(_comparator);
        }

        /**
         * Removes the first state from the sorted list.
         * @return state
         */
        _State removeFirstState() {
            _State state = (_State) _treeSet.first();
            _treeSet.remove(state);
            return state;
        }

        /**
         * Adds a state to the sorted list.
         * @param state to add
         */
        void add(_State state) {
            _treeSet.add(state);
        }

        /**
         * Test if list is empty.
         * @return true if empty
         */
        boolean isEmpty() {
            return _treeSet.isEmpty();
        }

        /**
         * The comparator to use for sorting
         */
        _StateComparator _comparator;

        /**
         * A tree set to store the sorted list.
         */
        TreeSet _treeSet;
    }

    /**
     * A two-way hash map provides fast lookup in both directions
     * of a bijective function between objects.
     * Supports only adding.
     * (Didn't know whether Java provides a standard solution.)
     */
    protected static class _TwoWayHashMap {

        /**
         * Construct an instance of two-way hash map.
         */
        _TwoWayHashMap() {
            _forwardMap = new HashMap();
            _backwardMap = new HashMap();
        }

        /**
         * Put an association between objects A and B in the hash map.
         * @param A object A
         * @param B object B
         */
        void put(Object A, Object B) {
            _forwardMap.put(A, B);
            _backwardMap.put(B, A);
        }

        /**
         * Forward lookup.
         * @param A lookup object associated with object A
         * @return associated object
         */
        Object getFW(Object A) {
            return _forwardMap.get(A);
        }

        /**
         * Backward lookup.
         * @param B lookup object associated with object B
         * @return associated object
         */
        Object getBW(Object B) {
            return _backwardMap.get(B);
        }

        /**
         * one-way hash map to make the forward association
         */
        HashMap _forwardMap;

        /**
         * one-way hash map to make the backward association
         */
        HashMap _backwardMap;

    }

    ///////////////////////////////////////////////////////////////////
    ////                   private fields                                  ////

    /**
     * Build the final schedule from the end state found
     * @param state optimal end state
     * @return schedule
     */
    private Schedule _buildSchedule(_State state) {
        // create a new schedule
        Schedule result = new Schedule();
        // reverse the order of the states, because they are linked from final
        // state to initial state
        LinkedList stateList = new LinkedList();
        _State currentState = state;
        while (currentState != null) {
            // test if it actually represents an actor firing, otherwise forget it
            if (currentState.firingActor != null) {
                stateList.addFirst(currentState);
            }
            currentState = currentState.previousState;
        }

        // build the schedule
        Iterator stateListIterator = stateList.iterator();
        while (stateListIterator.hasNext()) {
            currentState = (_State) stateListIterator.next();
            // get the real actor from the model actor
            ptolemy.actor.Actor actor = (ptolemy.actor.Actor) _actorMap
                    .getFW(currentState.firingActor);
            // check if the actor implements the special BufferingProfile interface
            if (actor instanceof BufferingProfile) {
                // if yes, create a BufferingProfileFiring with the right parameters
                BufferingProfileFiring firing = new BufferingProfileFiring(
                        actor, currentState.firingExclusive);
                firing.setIterationCount(1);
                // add it to the schedule
                result.add(firing);
                // output the schedule to the debug listener
                _myScheduler.showDebug("Fire actor " + actor.getFullName()
                        + " exclusive: " + currentState.firingExclusive);
            } else {
                // if no, create a normal Firing with the right parameters
                Firing firing = new Firing(actor);
                firing.setIterationCount(1);
                // add it to the schedule
                result.add(firing);
                // output the schedule to the debug listener
                _myScheduler.showDebug("Fire actor " + actor.getFullName());
            }
        }
        return result;
    }

    /**
     * Assign the state indices into state vector to actors and channels
     * (and recursively to the ports).
     */
    private void _setStateIndices() {
        int i = 0;
        Iterator actorIterator = _actors.iterator();
        while (actorIterator.hasNext()) {
            _Actor actor = (_Actor) actorIterator.next();
            i = actor.assignStateIndex(i);
        }
        _actorSize = i;

        i = 0;
        Iterator channelIterator = _channels.iterator();
        while (channelIterator.hasNext()) {
            _Channel channel = (_Channel) channelIterator.next();
            i = channel.assignStateIndex(i);
        }
        _channelSize = i;
    }

    /**
     * Create the initial state
     * @return initial state
     */
    private _State initialState() {
        _State result = new _State(null);
        // initialize state vectors
        result.channelContent = new int[_channelSize];
        result.actorContent = new int[_actorSize];
        // initialize actors
        Iterator actorIterator = _actors.iterator();
        while (actorIterator.hasNext()) {
            _Actor actor = (_Actor) actorIterator.next();
            actor.setInitialState(result);
        }
        // initialize channels
        Iterator channelIterator = _channels.iterator();
        while (channelIterator.hasNext()) {
            _Channel channel = (_Channel) channelIterator.next();
            channel.setInitialState(result);
        }
        return result;
    }

    /**
     * list of actors in the model to optimize
     */
    private _ListOfActors _actors;

    /**
     * state size occupied by channels
     */
    private int _channelSize;

    /**
     * State size occupied by actors
     */
    private int _actorSize;

    /**
     * a list of channels in the model
     */
    private _ListOfChannels _channels;

    /**
     * The scheduler invoking the service of this object.
     */
    private OptimizingSDFScheduler _myScheduler;

    /**
     * Two-way hash map associating actor models and their Ptolemy actors
     */
    private _TwoWayHashMap _actorMap;

    /**
     * Two-way hash map associating channels with their producing IOPorts
     */
    private _TwoWayHashMap _channelMap;

    /**
     * The optimization criterion to be used from the OptimizingSDFScheduler
     */
    private OptimizationCriteria _optimizationCriterion;

}
