/* OptimalScheduleFinder is a strategy object to compute an optimal scheduler
 * for an OptimizedSDFScheduler

 Copyright (c) 1997-2010 The Regents of the University of California.
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

////////////////////////////////////////////////////////////////////////
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
@since Ptolemy II 0.2
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
*/

public class OptimalScheduleFinder {

   
    /**
     * Construct an instance of the OptimalScheduleFinder. Creates an object 
     * associated with the OptimizingSDFSchedule <i>s</i> and using 
     * the optimization criterion <i>crit</i> to find an optimized schedule.
     * @param s scheduler
     * @param crit optimization criterion
     */
    public OptimalScheduleFinder(OptimizingSDFScheduler s, OptimizationCriteria crit) {
        _myScheduler = s;
        _optimizationCriterion = crit;
    }

    /**
     * Instantiate the analysis model from the Ptolemy model
     * @param firingVector contains repetition vector information
     * @throws IllegalActionException if model information inconsistent
     */
    public void instantiateAnalysisModel(Map firingVector) throws IllegalActionException {

        // initialize lists and maps
        _actors = new ListOfActors();
        _actorMap = new TwoWayHashMap();
        _channels = new ListOfChannels();
        _channelMap = new TwoWayHashMap();
        
        // create the actors
        Iterator ai = firingVector.entrySet().iterator();
        while(ai.hasNext()){
            int sb, eb, set, eet;
            Map.Entry pair = (Map.Entry) ai.next();
            ptolemy.actor.Actor a = (ptolemy.actor.Actor) pair.getKey();
            // if it is an actor which implements our BufferingProfile
            if(a instanceof BufferingProfile){
                BufferingProfile bp = (BufferingProfile) a;
                // set the profile accordingly
                sb  = bp.sharedBuffers();
                eb  = bp.exclusiveBuffers();
                set = bp.sharedExecutionTime();
                eet = bp.exclusiveExecutionTime();
            } else {
                // set the profile to default values
                sb  = 0;
                eb  = 0;
                set = 0;
                eet = 0;
            }
            // create a model actor
            Actor na = new Actor(a.getName(), (Integer)pair.getValue(), sb, eb, set, eet);
            // add the actor
            _actors.add(na);
            // remember the link to the real actor
            _actorMap.put(na, a);            

            // create a channel for every output port
            List pl = a.outputPortList();
            Iterator pi = pl.iterator();
            while(pi.hasNext()){
                TypedIOPort p = (TypedIOPort)pi.next();
                Channel ch = new Channel();
                // set the number of initial tokens 
                ch.initialTokens = DFUtilities.getTokenInitProduction(p);
                // add the channel to the list
                _channels.add(ch);
                // remember the link to the IOPort
                _channelMap.put(p, ch);        
            }
        }
        
        // create the actor ports
        ai = firingVector.keySet().iterator();
        while(ai.hasNext()){
            // for every actor...
            ptolemy.actor.Actor a = (ptolemy.actor.Actor) ai.next();
            Actor na = (Actor) _actorMap.getBW(a);
            List pl = a.outputPortList();
            Iterator pi = pl.iterator();
            while(pi.hasNext()){
                // for every output port of the actor
                TypedIOPort p = (TypedIOPort)pi.next();
                // get the rate of the port 
                int rate = DFUtilities.getRate(p);
                // get the channel of this port
                Channel ch = (Channel) _channelMap.getFW(p);
                // create the model port
                Port np = new Port(rate, ch);
                // add the new port to the actor
                na.addPort(np);
            }
            pl = a.inputPortList();
            pi = pl.iterator();
            while(pi.hasNext()){
                // for every input port of the actor
                TypedIOPort p = (TypedIOPort)pi.next();
                // get the rate of the port
                int rate = DFUtilities.getRate(p);
                // get the producing IOPort
                List sp = p.sourcePortList();
                // input port should not have more than one source
                assert sp.size()<=1; 
                // only make a port in this model if the input port is connected
                if(sp.size() > 0){
                    // get the channel of the producing port
                    Channel ch = (Channel) _channelMap.getFW(sp.get(0));   
                    // if the source port is not related to a channel, then it is an external port
                    // and we do not care about it.
                    if(ch != null){
                        // otherwise set the rate (negative, because it is an input port)
                        Port np = new Port(-rate, ch);
                        // add the new port to the actor
                        na.addPort(np);
                    }
                }
            }            
        }        
        
        
    }
    
    /**
     * Make a schedule using a greedy (non-optimizing algorithm)
     * @param firingVector repetition vector
     * @return the computed schedule
     */
    public Schedule makeScheduleGreedy(Map firingVector){
        Schedule result = null;
        try {              
            // instantiate the model
            instantiateAnalysisModel(firingVector);
            
            // determine the state vector indices
            setStateIndices();
            
            // run a greedy exploration
            // keeping toExplore sorted on maximal progress makes it greedy
            SortedSetOfStates toExplore = new SortedSetOfStates(new StateComparatorMaximumProgress());
            // add the initial state
            toExplore.add(initialState());
            // store the end state when found
            State optimalEndState = null;
            // to be set to true as soon as end state is found
            boolean scheduleFound = false;
            
            // continue searching until a schedule has been found
            while((!scheduleFound) && !toExplore.isEmpty()){
                // take the first state to further explore from our sorted list
                State s = toExplore.removeFirstState();
                // test if it is an end state, in which case we are ready.
                if(s.isEndState()){
                    // found !!
                    optimalEndState = s; 
                    scheduleFound = true;
                } 
                else{
                    // try all possible actions (actor firings) enabled from this state
                    Iterator ai = _actors.iterator();
                    while(ai.hasNext()){
                        // for each actor a ...
                        Actor a = (Actor) ai.next();
                        // check first if a is exclusively enabled to give
                        // preference to exclusive firing
                        // (although perhaps we should not be assuming that from the sorted set)
                        if(a.isExclusiveEnabled(s)){
                            // it is enabled. Create a new state accordingly
                            State ns = s.clone(s);
                            // fire the actor
                            a.fireExclusive(ns);
                            // update the value to be optimized depending on the optimization criterion
                            if(_optimizationCriterion == OptimizationCriteria.BUFFERS){
                                ns.value = Math.max(_channels.channelSize(ns) + a.exclusiveBuffers, ns.value);
                            } else if(_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME){
                                ns.value = s.value + a.exclusiveExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(ns);
                        }   
                        // try also firing non-exclusive
                        // check if a is enabled for a shared firing
                        if(a.isEnabled(s)){
                            // it is enabled. Create a new state accordingly
                            State ns = s.clone(s);
                            // fire the actor
                            a.fire(ns);
                            // update the value to be optimized depending on the optimization criterion
                            if(_optimizationCriterion == OptimizationCriteria.BUFFERS){
                                ns.value = Math.max(_channels.channelSize(ns) + a.sharedBuffers, ns.value);
                            } else if(_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME){
                                ns.value = s.value + a.sharedExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(ns);
                        }
                    }
                }
            }
            // Completed the search. Did we find anything?
            if(scheduleFound){
                // yes, then turn it into a schedule.
                result = _buildSchedule(optimalEndState);
            }
        
        } catch(IllegalActionException e){
            // oops, something happened...
        }
        // return the schedule
        return result;
    }

    /**
     * Make a schedule using an exhaustive BFS-like optimizing algorithm
     * @param firingVector repetition vector
     * @return the computed schedule
     */
    public Schedule makeSchedule(Map firingVector){
        Schedule result = null;
        try {              
            // instantiate the model
            instantiateAnalysisModel(firingVector);
            
            // determine the state vector indices
            setStateIndices();
            
            // run a state-space exploration
            // keeping toExplore sorted on memory usage ensures that the minimum memory
            // schedule is found first.
            SortedSetOfStates toExplore = new SortedSetOfStates();
            // add the initial state
            toExplore.add(initialState());
            // store the end state when found
            State optimalEndState = null;
            // to be set to true as soon as end state is found
            boolean scheduleFound = false;

            // continue searching until a schedule has been found
            while((!scheduleFound) && !toExplore.isEmpty()){
                // take the first state to further explore from our sorted list
                State s = toExplore.removeFirstState();
                // test if it is an end state, in which case we are ready.
                if(s.isEndState()){
                    // found !!
                    optimalEndState = s; 
                    scheduleFound = true;
                } 
                else{
                    // try all possible actions (actor firings) enabled from this state
                    Iterator ai = _actors.iterator();
                    while(ai.hasNext()){
                        // for each actor a ...
                        Actor a = (Actor) ai.next();
                        // check first if a is exclusively enabled to give
                        // preference to exclusive firing
                        // (although perhaps we should not be assuming that from the sorted set)
                        if(a.isExclusiveEnabled(s)){
                            // it is enabled. Create a new state accordingly
                            State ns = s.clone(s);
                            // fire the actor
                            a.fireExclusive(ns);
                            // update the value to be optimized depending on the optimization criterion
                            if(_optimizationCriterion == OptimizationCriteria.BUFFERS){
                                ns.value = Math.max(_channels.channelSize(ns) + a.exclusiveBuffers, ns.value);
                            } else if(_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME){
                                ns.value = s.value + a.exclusiveExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(ns);
                        }   
                        // try also firing non-exclusive
                        // check if a is enabled for a shared firing
                        if(a.isEnabled(s)){
                            // it is enabled. Create a new state accordingly
                            State ns = s.clone(s);
                            // fire the actor
                            a.fire(ns);
                            // update the value to be optimized depending on the optimization criterion
                            if(_optimizationCriterion == OptimizationCriteria.BUFFERS){
                                ns.value = Math.max(_channels.channelSize(ns) + a.sharedBuffers, ns.value);
                            } else if(_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME){
                                ns.value = s.value + a.sharedExecutionTime;
                            }
                            // add the new state to the list of states to be explored
                            toExplore.add(ns);
                        }
                    }
                }
            }
            // Completed the search. Did we find anything?
            if(scheduleFound){
                // yes, then turn it into a schedule.
                result = _buildSchedule(optimalEndState);
            }
        
        } catch(IllegalActionException e){
            // oops, something happened...
        }
        // return the schedule
        return result;
    }
    
///////////////////////////////////////////////////////////////////////////
////                 protected fields                                  ////
    
    /**
     * the state of the channel in the channel array has one integer at stateIndex 
     * indicating the number of tokens in the channel and another integer at stateIndex+1
     * indicating how many consumer are still to read the token
     * I need to remember per receiver how many tokens there are for that receiver.
     * If a token is produced, then the value increases for all consumers. When a consumer
     * reads it decreases only for that consumer. 
     */ 
     protected static class Channel{
         
         /**
          * The number of actors consuming from this channel. 
          */
         protected int nrConsumers=-1;
         
         /**
          * The number of initial tokens on the channel.
          */
         protected int initialTokens;
         
         /**
          * Construct a instance of Channel and initialize nrConsumers to 0.
          */
         protected Channel() {
             nrConsumers = 0;
         }
         
         /**
          * Assign stateIndex and returns the next available index to be assigned
          * to the next channel, depending on the number of consumers of this channel.
          * @param idx state index for the channel 
          * @return index for the next component
          */
         protected int assignStateIndex(int idx){
             assert nrConsumers>0;
             _stateIndex = idx;
             return idx + nrConsumers;
         }

         /**
          * Called for any input port to be connected to the channel. Returns an index
          * into the state vector for this port to manage the number of remaining tokens 
          * to be read.
          * @return index for the new port
          */
         protected int assignPortIndex(){
             int i = nrConsumers;
             nrConsumers++;
             return i;
         }
         
         /**
          * Get the number of available tokens to the consuming port with index 'i'
          * in state 's'.
          * @param i port index
          * @param s state
          * @return the number of available tokens in the channel to port with index i
          */
         protected int tokens(int i, State s){
             return s.channelContent[_stateIndex+i];
         }
         
         /**
          * Get the number of exclusively available tokens to the consuming port with 
          * index 'i' in state 's'.
          * @param i port index
          * @param s state
          * @return the number of exclusively available tokens in the channel to port with index i
          */
         protected int exclusiveTokens(int i, State s){
             int myTokens = tokens(i, s);
             int max = myTokens;
             // we can only use them exclusively if all others have already consumed them
             for(int j = 0; j < nrConsumers; j++){
                 if(j != i){
                     max = Math.min(max, myTokens - tokens(j, s)); 
                 }
             }
             if(max < 0) max = 0;
             return max;
         }

         /**
          * Add new tokens into the channel.
          * @param s state to be updated
          * @param rate number of tokens to add 
          */
         protected void addTokens(State s, int rate) {
             // add tokens for all consuming ports
             for(int i=0; i<nrConsumers; i++){
                 s.channelContent[_stateIndex+i]+=rate;                
             }
         }

         /**
          * Remove tokens from the channel for given port.
          * @param s state to be updated
          * @param pi port index
          * @param rate number of tokens to remove 
          */
         protected void removeTokens(State s, int pi, int rate) {
             s.channelContent[_stateIndex+pi]+=rate; // rate is negative                
         }
         
         /**
          * Initialize the state s to this channel's initial state
          * by putting the appropriate number of initial tokens in it.
          * @param s state to be initialized
          */
         protected void setInitialState(State s){
             for(int j = 0; j<nrConsumers; j++){
                 s.channelContent[_stateIndex+j] = initialTokens;
             }
         }
         
         /**
          * return the amount of memory taken by the channel content.
          * which is the maximum number of tokens still to be read by any
          * consumer
          * @param s state
          * @return amount of memory occupied by channel content 
          */
         protected int channelSize(State s) {
             int result = 0;
             for(int i = 0; i < nrConsumers; i++){
                 result = Math.max(result, s.channelContent[i]);
             }
             return result;
         }
         
         private int _stateIndex;         
     }

     /**
      * A list of channels, based on LinkedList.
      */
     protected static class ListOfChannels extends LinkedList {

         /**
          * Count the overall memory taken by channels in the list in state s.
          * @param s state
          * @return total memory size occupied by channels
          */
         protected int channelSize(State s) { 
             Iterator ci = iterator();
             int result = 0;
             // iterate over channels in the list
             while(ci.hasNext()){
                 Channel ch = (Channel) ci.next();
                 // add up channel size
                 result += ch.channelSize(s);
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
     protected static class Port {
         /**
          * The index for this port into the channel's state vector.
          */
         protected int portIndex;
         
         /**
          * Construct an instance of Port with rate <i>r</i> and for channel <i>ch</i>. 
          * @param r port rate (negative for input port)
          * @param ch channel to which it is bound
          */
         protected Port(int r, Channel ch){
             _rate = r;
             _channel = ch;
         }
         
         /**
          * Assign index to the port into the channel state vector
          * only if it is an input port.
          */
         protected void assignIndex(){
             if(_rate < 0){
                 portIndex = _channel.assignPortIndex();                
             }
         }
         
         /**
          * test if the port is enabled, i.e. if the associated channel has enough
          * tokens on this port to fire in state s
          * @param s state
          * @return true if it is enabled
          */
         protected boolean isEnabled(State s) {
             // an output port is always enabled
             if(_rate >= 0) return true;
             // otherwise check tokens, recall that rate is negative
             return _channel.tokens(portIndex, s) + _rate >= 0; 
         }
         
         /**
          * test if the port is enabled for exclusive firing, i.e. if the associated 
          * channel has enough tokens on this port to fire exclusively in state s
          * @param s state
          * @return true if it is enabled
          */
         protected boolean isExclusiveEnabled(State s) {
             // an output port is always enabled
             if(_rate >= 0) return true;
             // otherwise check tokens, recall that rate is negative
             return _channel.exclusiveTokens(portIndex, s) + _rate >= 0; 
         }
         
         /**
          * Fire the port by accounting the numbers of tokens.
          * @param s state to use for firing
          */
         protected void fire(State s) {
             if(_rate > 0)
                 // producing port
                 _channel.addTokens(s, _rate);
             else
                 // consuming port
                 _channel.removeTokens(s, portIndex, _rate);
         }

         ////////// private fields

         /**
          * the port rate, negative if input port
          */
         private int _rate;
         
         /**
          * The channel associated with the port. 
          */
         private Channel _channel;
     }
     
     /**
      * A list of ports, based on LinkeList.
      */
     protected static class ListOfPorts extends LinkedList{
     }

     /**
      * A model of an actor. Containing the firing profile, its count in the repetition 
      * vector and a number of ports. 
      */
     protected static class Actor {

         /**
          * Construct an instance of Actor, providing its name, repetition vector
          * count and profile information.
          * @param newName name for the actor
          * @param rep repetition vector entry of the actor
          * @param sb number of frame buffers needed for shared firing
          * @param eb number of frame buffers needed for exclusive firing
          * @param set execution time needed for share firing
          * @param eet execution time needed for exclusive firing
          */
         protected Actor(String newName, int rep, int sb, int eb, int set, int eet) {
             name = newName;
             repCount = rep;
             sharedBuffers = sb;
             exclusiveBuffers = eb;
             sharedExecutionTime = set;
             exclusiveExecutionTime = eet;
             ports = new ListOfPorts();
         }
         
         /**
          * A list of ports of the actor, both input and output ports.
          */
         protected ListOfPorts ports;
         
         /**
          * The name of the actor.
          */
         protected String name;
         
         /**
          * Count for the actor in the repetition vector of the graph.
          */
         protected int repCount;
                  
         /**
          * The number of frame buffers the actor requires in a shared firing.
          */
         protected int sharedBuffers;

         /**
          * The number of frame buffers the actor requires in an exclusive firing. 
          */
         protected int exclusiveBuffers;
         
         /**
          * Execution time (estimate) for the actor for a shared firing.
          */
         protected int sharedExecutionTime;

         /**
          * Execution time (estimate) for the actor for an exclusive firing.
          */
         protected int exclusiveExecutionTime;
         
         /**
          * Assign stateIndex to actor and its ports and returns the index for 
          * the next component.
          * @param idx index to assign to the actor
          * @return index to assign to next actor
          */
         protected int assignStateIndex(int idx){
             // iterate over ports to assign index to ports in channel state vector
             Iterator ip = ports.iterator();
             while(ip.hasNext()){
                 Port p = (Port)ip.next();
                 p.assignIndex();
             }
             // index for actor
             _stateIndex = idx;
             return idx + 1;
         }

         /**
          * Test whether the actor is enabled for a shared firing in given state <i>s</i>.
          * @param s state
          * @return true if enabled
          */
         protected boolean isEnabled(State s){
             // no more firings needed in iteration
             if(s.actorContent[_stateIndex] == 0) return false;
             // test if all ports are enabled
             Iterator portIter = ports.iterator();
             while(portIter.hasNext()){
                 Port p = (Port) portIter.next();
                 if (!p.isEnabled(s)){
                     return false;
                 }
             }
             // all are enabled, go ahead
             return true;
         }

         /**
          * Test whether the actor is enabled for an exclusive firing in given state <i>s</i>.
          * @param s state
          * @return true if enabled
          */
         protected boolean isExclusiveEnabled(State s){
             // no more firings needed in iteration
             if(s.actorContent[_stateIndex] == 0) return false;
             // test if all ports are enabled
             Iterator portIter = ports.iterator();
             while(portIter.hasNext()){
                 Port p = (Port) portIter.next();
                 if (!p.isExclusiveEnabled(s)){
                     return false;
                 }
             }
             // all are enabled, go ahead
             return true;
         }
         
         /**
          * adapt state s according to a shared firing. Assumes it is enabled.
          * @param s state
          */
         protected void fire(State s){
             // fire all ports
             Iterator portIter = ports.iterator();
             while(portIter.hasNext()){
                 Port p = (Port) portIter.next();
                 p.fire(s);
             }
             // one less firing remaining in iteration
             s.actorContent[_stateIndex]--;
             // remember in the state which actor fired
             s.firingActor = this;
             // mark that the firing is shared, not exclusive
             s.firingExclusive = false;
             }

         /**
          * adapt state s according to an exclusive firing. Assumes it is enabled.
          * @param s state
          */
         protected void fireExclusive(State s) {
             // fire all ports
             Iterator portIter = ports.iterator();
             while(portIter.hasNext()){
                 Port p = (Port) portIter.next();
                 p.fire(s);
                 }
             // one less firing remaining in iteration
             s.actorContent[_stateIndex]--;
             // remember in the state which actor fired
             s.firingActor = this;
             // mark that the firing is shared, not exclusive
             s.firingExclusive = true;
         }

         /**
          * Add a port to the actor.
          * @param p port to add
          */
         protected void addPort(Port p) {
             ports.add(p);
         }

         /**
          * Initialize state of to initial state of the network. Specifically, set
          * the count for this actor to the number of firings in one iteration.
          * @param s state to initialize
          */
         protected void setInitialState(State s) {
             s.actorContent[_stateIndex] = this.repCount;
         }

         /**
          * index for the actor into the global state vector
          */
         private int _stateIndex;
     }
  
     /**
      * A list of actors, derived from LinkedList.
      */
     protected static class ListOfActors extends LinkedList{
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
     protected static class State {
         /**
          * true if the firing to reach the state was exclusive.
          */
         protected boolean firingExclusive;
         
         /**
          * The actor that was fired to reach this state.
          * remains nil for the initial state.
          */
         protected Actor firingActor;
         
         /**
          * Link to the previous state.
          */
         protected State previousState;

         // actual state information
         /**
          * Array to store all channel content.
          * Channels and input ports within those channels are given their unique
          * index into this array.
          */
         protected int[] channelContent;
         
         /**
          * Array to store all actor content, remaining number of firings.
          * Actors are given their unique index into this array.
          */
         protected int[] actorContent;

         /**
          * Value to be optimized, smaller is better.
          */
         protected int value;
         
         /**
          * Construct an instance of State, with a reference to a previous state. 
          * @param prev link to previous state
          */
         protected State(State prev){
             previousState = prev;
         }
         
         /**
          * Create new state by cloning state s and marking
          * s as its predecessor state.
          * @param s state to clone
          * @return the new state
          */
         protected State clone(State s){
             State result = new State(s);
             // copy global graph state
             result.channelContent = channelContent.clone();
             result.actorContent = actorContent.clone();
             // copy value
             result.value = value;
             return result;
         }
         
         /**
          * Test whether this is a valid end state, i.e. whether all actors have 
          * completed their required number of firings.
          * @return true is end state
          */
         protected boolean isEndState() {
             // test all actors
             for(int i = 0; i < actorContent.length; i++){
                 if(actorContent[i] > 0) return false;
             }
             return true;
         }
         
         /**
          * Determine the number of remaining firings.
          * @return number of remaining firings
          */
         protected int getFiringsToCompletion() {
             int result = 0;
             for(int i = 0; i < actorContent.length; i++){
                 result += actorContent[i];
             }            
             return result;
         }

     }
     
     /**
      * A set of states, based on HashSet.
      */
     protected static class SetOfStates extends HashSet {       
     }

     /** 
      * An abstract super class for Comparators to maintain a sorted 
      * list of states.
      */
     protected static abstract class StateComparator implements Comparator, Serializable {
     }
     
     /**
      * A Comparator to maintain a sorted list of states, sorted on their value.
      */
     protected static class StateComparatorLowestValue extends StateComparator {

         /**
          * compare two states on their value. If values tie, then sort
          * on arbitrary other criteria 
          * @param o1 first object to compare
          * @param o2 second object to compare 
          * @return -1 if o1<o2, +1 if o1>o2 0 otherwise
          */
         public int compare(Object o1, Object o2) {
             State s1 = (State) o1;
             State s2 = (State) o2;
             // sort on value
             if(s1.value != s2.value){
                 return s1.value - s2.value;
             } else {
                 // value tie. compare channel state
                 for(int i = 0; i < s1.channelContent.length; i++){
                     if(s1.channelContent[i] != s2.channelContent[i])
                         return s1.channelContent[i] - s2.channelContent[i];
                 }
                 // still no difference, compare actor state
                 for(int i = 0; i < s1.actorContent.length; i++){
                     if(s1.actorContent[i]!= s2.actorContent[i])
                         return s1.actorContent[i]-s2.actorContent[i];
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
     protected static class StateComparatorMaximumProgress extends StateComparator {
        
         /**
          * Construct an instance of StateComparatorMaximumProgress. It creates
          * a secondary comparator based on StateComparatorLowestValue.
          */
         public StateComparatorMaximumProgress(){
             _backupComparator = new StateComparatorLowestValue();
         }
         
         /**
          * Compare the states based on smallest number of remaining firings.
          * @param o1 first object to compare
          * @param o2 second object to compare 
          * @return -1 if o1<o2, +1 if o1>o2 0 otherwise
          */
         public int compare(Object o1, Object o2) {
             State s1 = (State) o1;
             State s2 = (State) o2;
             int p1 = s1.getFiringsToCompletion();
             int p2 = s2.getFiringsToCompletion();
             if(p1 != p2){
                 // least number of firings first
                 return p1 - p2; 
             } else {
                 // tie, invoke backup comparator
                return _backupComparator.compare(s1, s2);
             }
         }

         /**
          * a secondary comparator to break a tie.
          */
         private StateComparator _backupComparator;         
     }
     
     /**
      * A sorted set of states. Internally using a TreeSet.
      */
     protected static class SortedSetOfStates  {
         
         /**
          * Construct an instance of SortedSetOfStates. Creates the default 
          * comparator and TreeSet. Default comparator sorts on lowest value.
          */
         public SortedSetOfStates(){
             _comp = new StateComparatorLowestValue();
             _ts = new TreeSet(_comp);
         }
         
         /**
          * Construct an instance with an explicitly specified comparator.   
          * @param c comparator
          */
         public SortedSetOfStates(StateComparator c){
             _comp = c;
             _ts = new TreeSet(_comp);
         }
         
         /**
          * Removes the first state from the sorted list.
          * @return state
          */
         protected State removeFirstState(){
             State s = (State) _ts.first();
             _ts.remove(s);
             return s;
         }
         
         /**
          * Adds a state to the sorted list.
          * @param state to add
          */
         protected void add(State state) {
             _ts.add(state);
         }
         
         /**
          * Test if list is empty.
          * @return true if empty
          */
         protected boolean isEmpty() {
             return _ts.isEmpty();
         }
         
         /** 
          * The comparator to use for sorting
          */
         private StateComparator _comp;
         
         /**
          * A tree set to store the sorted list.
          */
         private TreeSet _ts;
     }

     /**
      * A two-way hash map provides fast lookup in both directions
      * of a bijective function between objects.
      * Supports only adding.
      * (Didn't know whether Java provides a standard solution.) 
      */
     protected static class TwoWayHashMap {
         
         /**
          * Construct an instance of two-way hash map.
          */
         public TwoWayHashMap(){
             _fw = new HashMap();
             _bw = new HashMap();
         }
         
         /**
          * Put an association between objects A and B in the hash map.
          * @param A object A
          * @param B object B
          */
         protected void put(Object A, Object B){
             _fw.put(A, B);
             _bw.put(B, A);
         }
         
         /**
          * Forward lookup.
          * @param A lookup object associated with object A
          * @return associated object
          */
         protected Object getFW(Object A){
             return _fw.get(A);
         }

         /**
          * Backward lookup.
          * @param B lookup object associated with object B
          * @return associated object
          */
         protected Object getBW(Object B){
             return _bw.get(B);
         }
         
         ////////// Private fields

         /**
          * one-way hash map to make the forward association
          */
         private HashMap _fw;

         /**
          * one-way hash map to make the backward association
          */
         private HashMap _bw;
         
     }    
    
    /**
     * Build the final schedule from the end state found
     * @param s optimal end state
     * @return schedule
     */
    private Schedule _buildSchedule(State s) {
        // create a new schedule
        Schedule result = new Schedule();
        // reverse the order of the states, because they are linked from final
        // state to initial state 
        LinkedList sl = new LinkedList();
        State ss = s;
        while(ss != null){
            // test if it actually represents an actor firing, otherwise forget it
            if(ss.firingActor != null){
                sl.addFirst(ss);
            }
            ss = ss.previousState;
        }
        
        // build the schedule
        Iterator li = sl.iterator();
        while(li.hasNext()){
            ss = (State) li.next();
            // get the real actor from the model actor
            ptolemy.actor.Actor pa = (ptolemy.actor.Actor) _actorMap.getFW(ss.firingActor);
            // check if the actor implements the special BufferingProfile interface
            if(pa instanceof BufferingProfile){
                // if yes, create a BufferingProfileFiring with the right parameters
                BufferingProfileFiring firing = new BufferingProfileFiring(pa, ss.firingExclusive);
                firing.setIterationCount(1);
                // add it to the schedule
                result.add(firing);
                // output the schedule to the debug listener
                _myScheduler.showDebug("Fire actor " + pa.getFullName() + " exclusive: " + ss.firingExclusive);
            } else {
                // if no, create a normal Firing with the right parameters
                Firing firing = new Firing(pa);
                firing.setIterationCount(1);
                // add it to the schedule
                result.add(firing);
                // output the schedule to the debug listener
                _myScheduler.showDebug("Fire actor " + pa.getFullName());
            }
        }        
        return result;
    }

    
    private void setStateIndices() {
        int i = 0;
        Iterator ai = _actors.iterator();
        while(ai.hasNext()){
            Actor a = (Actor) ai.next();
            i=a.assignStateIndex(i);
        }
        _actorSize = i;

        i = 0;
        Iterator ci = _channels.iterator();
        while(ci.hasNext()){
            Channel ch = (Channel) ci.next();
            i = ch.assignStateIndex(i);
        }
        _channelSize = i;
    }

    ///// private fields

    /**
     * Create the initial state
     * @return initial state
     */
    private State initialState() {
        State result = new State(null);
        // initialize state vectors
        result.channelContent = new int[_channelSize];
        result.actorContent = new int[_actorSize];
        // initialize actors
        Iterator ai = _actors.iterator();
        while(ai.hasNext()){
            Actor a = (Actor) ai.next();
            a.setInitialState(result);
        }
        // initialize channels
        Iterator ci = _channels.iterator();
        while(ci.hasNext()){
            Channel ch = (Channel) ci.next();
            ch.setInitialState(result);
        }
        return result;
    }

    /**
     * list of actors in the model to optimize
     */
    private ListOfActors _actors;

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
    private ListOfChannels _channels;
    
    /**
     * The scheduler invoking the service of this object.
     */
    private OptimizingSDFScheduler _myScheduler;
    
    /**
     * Two-way hash map associating actor models and their Ptolemy actors
     */
    private TwoWayHashMap _actorMap;
    
    /**
     * Two-way hash map associating channels with their producing IOPorts
     */
    private TwoWayHashMap _channelMap;
    
    /**
     * The optimization criterion to be used from the OptimizingSDFScheduler
     */
    private OptimizationCriteria _optimizationCriterion;
   
}
