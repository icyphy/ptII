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

/**
<h1>Class comments</h1>
An OptimalScheduleFinder encapsulates an algorithm to find an optimized schedule.
In particular it implements a simple state space exploration algorithm to find a 
minimum buffer size schedule.
<p>
See {@link ptolemy.domains.sdf.optimize.optimizingSDFDirector}, 
{@link ptolemy.domains.sdf.optimize.optimizingSDFScheduler} and 
{@link ptolemy.domains.sdf.optimize.BufferingProfile} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.OptimizingSDFScheduler
@see ptolemy.domains.sdf.optimize.BufferingProfile

@author Marc Geilen
@version $Id: $
@since Ptolemy II 0.2
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
*/

public class OptimalScheduleFinder {

    /**
    * the state of the channel in the channel array has one integer at stateIndex 
    * indicating the number of tokens in the channel and another integer at stateIndex+1
    * indicating how many consumer are still to read the token
    * I need to remember per receiver how many tokens there are for that receiver.
    * If a token is produced, then the value increases for all consumers. When a consumer
    * reads it decreases only for that consumer. 
    */ 
    public class Channel{
        private int stateIndex;
        public int nrConsumers=-1;
        public int initialTokens;
        
        public Channel() {
            nrConsumers = 0;
        }
        // assign stateIndex and returns the index for the next component
        // depending on the required size
        public int assignStateIndex(int idx){
            assert nrConsumers>0;
            stateIndex = idx;
            return idx + nrConsumers;
        }
        public int assignPortIndex(){
            int i = nrConsumers;
            nrConsumers++;
            return i;
        }
        public int tokens(int i, State s){
            return s.channelContent[stateIndex+i];
        }
        public int exclusiveTokens(int i, State s){
            int myTokens = tokens(i, s);
            int max = myTokens;
            for(int j = 0; j<nrConsumers; j++){
                if(j!=i){
                    max = Math.min(max, myTokens - tokens(j,s)); 
                }
            }
            if(max<0) max=0;
            return max;
        }

        public void addTokens(State s, int rate) {
            for(int i=0; i<nrConsumers; i++){
                s.channelContent[stateIndex+i]+=rate;                
            }
        }

        public void removeTokens(State s, int pi, int rate) {
            s.channelContent[stateIndex+pi]+=rate; // rate is negative                
        }
        
        public void setInitialState(State s){
            for(int j = 0; j<nrConsumers; j++){
                s.channelContent[stateIndex+j] = initialTokens;
            }
        }
        
        // return the amount of memory taken by the channel content.
        // which is the maximum number of tokens still to be read by any 
        // consumer
        public int channelSize(State s) {
            int result = 0;
            for(int i = 0; i<nrConsumers; i++){
                result = Math.max(result, s.channelContent[i]);
            }
            return result;
        }
    }
    public class ListOfChannels extends LinkedList {

        public int channelSize(State s) {
            // count the amount of memory taken by the channels 
            Iterator ci = iterator();
            int result = 0;
            while(ci.hasNext()){
                Channel ch = (Channel) ci.next();
                result += ch.channelSize(s);
            }
            return result;
        }
    }
    
    /** 
    * port of an actor is connected to a channel
    * negative rates represent input ports, positive rates output ports 
    */ 
    public class Port {
        public int portIndex;
        public Port(int r, Channel ch){
            rate = r;
            channel = ch;
        }
        private int rate;
        private Channel channel;
        public void assignIndex(){
            if(rate < 0){
                portIndex = channel.assignPortIndex();                
            }
        }
        public boolean isEnabled(State s) {
            if(rate >= 0) return true;
            return channel.tokens(portIndex, s) + rate >= 0; // consumption rates are negative
        }
        public boolean isExclusiveEnabled(State s) {
            if(rate >= 0) return true;
            return channel.exclusiveTokens(portIndex, s) + rate >= 0; // consumption rates are negative
        }
        public void fire(State s) {
            if(rate > 0)
                channel.addTokens(s, rate);
            else
                channel.removeTokens(s, portIndex, rate);
        }
    }
    
    public class ListOfPorts extends LinkedList{
    }
    
    public class Actor {
        public ListOfPorts ports;
        public String name;
        public int repCount;
        private int stateIndex;
        public int sharedBuffers;
        public int exclusiveBuffers;
        public int sharedExecutionTime;
        public int exclusiveExecutionTime;
        public Actor(String newName, int rep, int sb, int eb, int set, int eet) {
            name = newName;
            repCount = rep;
            sharedBuffers = sb;
            exclusiveBuffers = eb;
            sharedExecutionTime = set;
            exclusiveExecutionTime = eet;
            ports = new ListOfPorts();
        }
        // assign stateIndex and returns the index for the next component
        // depending on the required size
        public int assignStateIndex(int idx){
            Iterator ip = ports.iterator();
            while(ip.hasNext()){
                Port p = (Port)ip.next();
                p.assignIndex();
            }
            stateIndex = idx;
            return idx + 1;
        }

        public boolean isEnabled(State s){
            if(s.actorContent[stateIndex] == 0) return false;
            Iterator portIter = ports.iterator();
            while(portIter.hasNext()){
                Port p = (Port) portIter.next();
                if (!p.isEnabled(s)){
                    return false;
                }
            }
            return true;
        }

        public boolean isExclusiveEnabled(State s){
            if(s.actorContent[stateIndex] == 0) return false;
            Iterator portIter = ports.iterator();
            while(portIter.hasNext()){
                Port p = (Port) portIter.next();
                if (!p.isExclusiveEnabled(s)){
                    return false;
                }
            }
            return true;
        }
        
        // adapt state s according to a firing. assumes it is enabled
        public void fire(State s){
            // fire all ports;
            Iterator portIter = ports.iterator();
            while(portIter.hasNext()){
                Port p = (Port) portIter.next();
                p.fire(s);
                }
            s.actorContent[stateIndex]--;
            s.firingActor = this;
            s.firingExclusive = false;
            }

        public void fireExclusive(State s) {
            // fire all ports;
            Iterator portIter = ports.iterator();
            while(portIter.hasNext()){
                Port p = (Port) portIter.next();
                p.fire(s);
                }
            s.actorContent[stateIndex]--;
            s.firingActor = this;
            s.firingExclusive = true;
        }

        public void addPort(Port p) {
            ports.add(p);
        }

        public void setInitialState(State s) {
            s.actorContent[stateIndex] = this.repCount;
        }
    }
 
    public class ListOfActors extends LinkedList{
    }
  
    public class State {
        public boolean firingExclusive;
        public Actor firingActor;
        public State previousState;

        // actual state information 
        public int[] channelContent;
        public int[] actorContent;
        // value to be minimized
        public int value;
        
        public State(State prev){
            previousState = prev;
        }
        public State clone(State s){
            State result = new State(s);
            result.channelContent = channelContent.clone();
            result.actorContent = actorContent.clone();
            result.value = value;
            return result;
        }
        public boolean isEndState() {
            for(int i = 0; i < actorContent.length; i++){
                if(actorContent[i] > 0) return false;
            }
            return true;
        }

    }
    
    public class SetOfStates extends HashSet {       
    }
    
    public class StateComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            State s1 = (State) o1;
            State s2 = (State) o2;
            if(s1.value != s2.value){
                return s1.value-s2.value;
            } else {
                for(int i=0; i<s1.channelContent.length; i++){
                    if(s1.channelContent[i]!=s2.channelContent[i])
                        return s1.channelContent[i]-s2.channelContent[i];
                }
                for(int i=0; i<s1.actorContent.length; i++){
                    if(s1.actorContent[i]!=s2.actorContent[i])
                        return s1.actorContent[i]-s2.actorContent[i];
                }
                // they are equal
                return 0;
            }
        }
    }
    
    public class SortedSetOfStates  {
        private StateComparator comp;
        private TreeSet ts;
        public SortedSetOfStates(){
            comp = new StateComparator();
            ts = new TreeSet(comp);
        }
        public State removeFirstState(){
            State s = (State) ts.first();
            ts.remove(s);
            return s;
        }
        public void add(State state) {
            ts.add(state);
        }
        public boolean isEmpty() {
            return ts.isEmpty();
        }
    }

    public class TwoWayHashMap {
        private HashMap _fw;
        private HashMap _bw;
        public TwoWayHashMap(){
            _fw = new HashMap();
            _bw = new HashMap();
        }
        public void put(Object A, Object B){
            _fw.put(A, B);
            _bw.put(B, A);
        }
        public Object getFW(Object A){
            return _fw.get(A);
        }
        public Object getBW(Object B){
            return _bw.get(B);
        }
    }
    
    private ListOfActors _actors;
    private int _channelSize;
    private int _actorSize;
    private ListOfChannels _channels;
    private OptimizingSDFScheduler _myScheduler;
    private TwoWayHashMap _actorMap;
    private TwoWayHashMap _channelMap;
    private OptimizationCriteria _optimizationCriterion;
    

    
    public OptimalScheduleFinder(OptimizingSDFScheduler s, OptimizationCriteria crit) {
        _myScheduler = s;
        _optimizationCriterion = crit;
    }

    // instantiate the analysis model from the Ptolemy model
    void instantiateAnalysisModel(Map firingVector) throws IllegalActionException {

        _actors = new ListOfActors();
        _actorMap = new TwoWayHashMap();
        _channels = new ListOfChannels();
        _channelMap = new TwoWayHashMap();
        
        // create actors
        Iterator ai = firingVector.keySet().iterator();
        while(ai.hasNext()){
            int sb, eb, set, eet;
            ptolemy.actor.Actor a = (ptolemy.actor.Actor) ai.next();
            if(a instanceof BufferingProfile){
                BufferingProfile bp = (BufferingProfile) a;
                sb  = bp.sharedBuffers();
                eb  = bp.exclusiveBuffers();
                set = bp.sharedExecutionTime();
                eet = bp.exclusiveExecutionTime();
            } else {
                sb  = 0;
                eb  = 0;
                set = 0;
                eet = 0;
            }
            Actor na = new Actor(a.getName(), (Integer)firingVector.get(a), sb, eb, set, eet);
            _actors.add(na);
            _actorMap.put(na, a);            

            // create a channel for every output port
            List pl = a.outputPortList();
            Iterator pi = pl.iterator();
            while(pi.hasNext()){
                TypedIOPort p = (TypedIOPort)pi.next();
                Channel ch = new Channel();
                // initial tokens 
                ch.initialTokens = DFUtilities.getTokenInitProduction(p);
                _channels.add(ch);
                _channelMap.put(p, ch);        
            }
        }
        
        // create actor ports
        ai = firingVector.keySet().iterator();
        while(ai.hasNext()){
            ptolemy.actor.Actor a = (ptolemy.actor.Actor) ai.next();
            Actor na = (Actor) _actorMap.getBW(a);
            List pl = a.outputPortList();
            Iterator pi = pl.iterator();
            while(pi.hasNext()){
                TypedIOPort p = (TypedIOPort)pi.next();
                int rate = DFUtilities.getRate(p);
                Channel ch = (Channel) _channelMap.getFW(p);
                Port np = new Port(rate, ch);
                na.addPort(np);
            }
            pl = a.inputPortList();
            pi = pl.iterator();
            while(pi.hasNext()){
                TypedIOPort p = (TypedIOPort)pi.next();
                int rate = DFUtilities.getRate(p);
                List sp = p.sourcePortList();
                assert sp.size()<=1; // input port should not have more than one source
                // only make a port in this model if the input port is connected
                if(sp.size()>0){
                    Channel ch = (Channel) _channelMap.getFW(sp.get(0));   
                    // if the source port is not related to a channel, then it is an external port
                    // and we do not care about it.
                    if(ch != null){
                        Port np = new Port(-rate, ch);
                        na.addPort(np);
                    }
                }
            }            
        }        
        
        
    }
    
    Schedule makeSchedule(Map firingVector){
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
            toExplore.add(initialState());
            State minBufEndState = null;
            boolean scheduleFound = false;
            
            while((!scheduleFound) && !toExplore.isEmpty()){
                State s = toExplore.removeFirstState();
                if(s.isEndState()){
                    // found !!
                    minBufEndState = s; 
                    scheduleFound = true;
                } 
                else{
                    // for all possible actions (actor firings)
                    Iterator ai = _actors.iterator();
                    while(ai.hasNext()){
                        Actor a = (Actor) ai.next();
                        // check first if a is exclusively enabled to give
                        // preference to exclusive firing
                        // (although perhaps we should not be assuming that from the sorted set)
                        if(a.isExclusiveEnabled(s)){
                            State ns = s.clone(s);
                            a.fireExclusive(ns);
                            if(_optimizationCriterion == OptimizationCriteria.BUFFERS){
                                ns.value = Math.max(_channels.channelSize(ns) + a.exclusiveBuffers, ns.value);
                            } else if(_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME){
                                ns.value = s.value + a.exclusiveExecutionTime;
                            }
                            toExplore.add(ns);
                        }   
                        // try also firing non-exclusive
                        if(a.isEnabled(s)){
                            State ns = s.clone(s);
                            a.fire(ns);
                            if(_optimizationCriterion == OptimizationCriteria.BUFFERS){
                                ns.value = Math.max(_channels.channelSize(ns) + a.sharedBuffers, ns.value);
                            } else if(_optimizationCriterion == OptimizationCriteria.EXECUTIONTIME){
                                ns.value = s.value + a.sharedExecutionTime;
                            }
                            toExplore.add(ns);
                        }
                    }
                }
            }
            if(scheduleFound){
                result = _buildSchedule(minBufEndState);
            }
        
        } catch(IllegalActionException e){
            
        }
        return result;
    }

    private Schedule _buildSchedule(State s) {
        Schedule result = new Schedule();
        // reverse the order of the states
        LinkedList sl = new LinkedList();
        State ss = s;
        while(ss != null){
            if(ss.firingActor != null){
                sl.addFirst(ss);
            }
            ss = ss.previousState;
        }
        
        // build the schedule
        Iterator li = sl.iterator();
        while(li.hasNext()){
            ss = (State) li.next();
            // Add it to the schedule
            ptolemy.actor.Actor pa = (ptolemy.actor.Actor) _actorMap.getFW(ss.firingActor);
            if(pa instanceof BufferingProfile){
                BufferingProfileFiring firing = new BufferingProfileFiring(pa, ss.firingExclusive);
                firing.setIterationCount(1);
                result.add(firing);
                _myScheduler.showDebug("Fire actor " + pa.getFullName() + " exclusive: " + ss.firingExclusive);
            } else {
                Firing firing = new Firing(pa);
                firing.setIterationCount(1);
                result.add(firing);
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

    private State initialState() {
        State result = new State(null);
        result.channelContent = new int[_channelSize];
        result.actorContent = new int[_actorSize];
        Iterator ai = _actors.iterator();
        while(ai.hasNext()){
            Actor a = (Actor) ai.next();
            a.setInitialState(result);
        }
        Iterator ci = _channels.iterator();
        while(ci.hasNext()){
            Channel ch = (Channel) ci.next();
            ch.setInitialState(result);
        }
        return result;
    }

   
}
