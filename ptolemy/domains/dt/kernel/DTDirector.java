/* Discrete Time (DT) domain director.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (chf@eecs.berkeley.edu)
@AcceptedRating Red (chf@eecs.berkeley.edu)
*/

package ptolemy.domains.dt.kernel;

import ptolemy.graph.*;
import ptolemy.actor.*;
import ptolemy.actor.sched.*;
import ptolemy.actor.util.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.lib.Delay;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DTDirector
/**
 
<h1>DT overview</h1>
The Discrete Time (DT) domain is a timed extension of the Synchronous Dataflow 
(SDF) domain.  Like the SDF, it has static scheduling of the dataflow graph 
model. Likewise, DT requires that the rates on the ports of all actors be
known before hand and fixed. DT handles feedback systems in the same way as 
SDF does, but may with additional constraints on initial tokens.
<p>
<h1>Local and Global Time</h1>
Because of the inherent concurency occuring within SDF dataflow graph models,
there are two notions of time in DT -- global time and local time.  Global time
increases steadily as execution progresses.  Moreover, global time increments by 
fixed discrete chunks of time based on the value of the 'period' parameter.  On 
the other hand, local time applies to each of the actors in the model. All the
actors have distinct local times as an iteration proceeds. The local time of
an actor during an iteration depend on the global time, period, firing count,
port rates, and the schedule. These local times obey the following constraint:

   Global Time  <=  Local Time <= (Global Time + period)
   
The exact way of how local time increments during an iteration is described in
detail in the DTReceiver documentation.
<p>.   
<h1>DT Features</h1>
The design of the DT domain is motivated by the following criteria:
1.) Uniform Token Flow:  The time interval between tokens should be regular
    and unchanging.  This conforms to the idea of having sampled systems 
    with fixed rates. Although the tokens flowing in DT do not keep internal
    time stamps, each actor can query the DT director for its own local time.
    This local time is regularly increasing by a constant fraction of the
    director's period.  Local time is incremented everytime an actor calls
    the get() method to obtain a token. 
2.) Causality: Tokens produced by an actor should only depend on tokens produced
    or consumed in the past. This makes sense because we don't expect an actors to
    produce a token before it can calculate the token's value.  For example,
    if an actor needs three tokens A, B, and C to  compute token D, then the time
    when tokens A, B, and C are consumed should be earlier than than or equal to
    the time when token D is produced.  Note that in DT, computation does not 
    have to take time.
3.) SDF-style semantics: Ideally, we want DT to be a timed-superset of SDF with
    compatible token flow and scheduling.  However, we can only approximate
    this behavior. It is not possible to have uniform token flow, causality,
    and SDF-style semantics at the same time.  Causality breaks for non-
    homogeneous actors in a feedback system when fully-compatible SDF-style
    semantics is adopted.  To remedy this situation, every actor in DT that 
    has non-homogeneous input ports should produce initial tokens at each 
    of its output ports.
<p>
<h1>DTDirector and other classes</h1>
DTDirector is the class that controls execution of actors under the DT
domain.  It is derived from SDFDirector; and hence, follows the same execution 
semantics as SDF.  Actor scheduling is handled by the SDFScheduler class.
The newReceiver() method creates receivers of type DTReceiver, which is derived
from SDFReceiver.
<p>
<h1> Design Notes</h1>
DT (Discrete Time) is a timed model of computation.  In order 
to benefit from the internal time-keeping mechanism of DT, one should
use actors aware of time. For example, one should use TimedPlotter or
TimedScope instead of SequencePlotter or SequenceScope.  
<p>  
<h1> DT and Vergil </h1>
Non-hierarchical DT has been tested to work with Vergil.  However,
there is only limited support for hierarchical DT at the moment.  It is
possible to use hierarchical DT in Vergil by doing the following steps
1.) Create and save the inside model in Vergil. 
2.) Modify the inside model MOML file with the following modifications:
    - change class= keyword to extends= ; 
    - change <model> keyword to <class>;
    - provide a name for the <class> which is "" (blank) by default;
    - change the <port> to include <property name="output"/> 
                                or <property name="input"/>
3.) Modify the palette XML file to include the inside model
     <import source="name.xml"/>
     <entity name="name" class=".name">  

@see ptolemy.domains.dt.kernel.DTReceiver
@see ptolemy.domains.sdf.kernel.SDFDirector
@see ptolemy.domains.sdf.kernel.SDFReceiver
@see ptolemy.domains.sdf.kernel.SDFScheduler
 
       
 @author C. Fong
 @version  
*/
public class DTDirector extends SDFDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public DTDirector() {
    	super();
        _init();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace of this object.
     */
    public DTDirector(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument is null, a NullPointerException will 
     *  be thrown. If the name argument is null, then the name is set 
     *  to the empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     */
    public DTDirector(TypedCompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The period of the model.  This parameter must contain a
     *  DoubleToken.  Its default value is 1.0 .
     *  For homogeneous hierarchical DT (i.e. DT inside DT) , the period 
     *  of the inside director cannot be set explicitly by the user. 
     *  Instead, it will have a fixed value: "outsidePeriod / repeats ", 
     *  where 'outsidePeriod' is the period of the outside director; and 
     *  'repeats' is the firing count of the composite actor that contains
     *  the inside director.
     *  For heterogeneous hierarchical DT (i.e. DT inside DE or CT), the 
     *  period parameter is used to determine how often the fireAt()
     *  method is called to request firing from the outside director. 
     */
    public Parameter period;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  The period parameter is explicitly cloned in this method.
     *  @param ws The workspace for the new object.
     *  @return A new object.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        DTDirector newobj = (DTDirector)(super.clone(ws));
        newobj.period = (Parameter)newobj.getAttribute("period");
        return newobj;
    }
    
    /** Get the global time increment per iteration for this director.
     *  This is a convenience method for getting the period parameter.
     *  For hierarchical DT (DT inside DT), extra calculation is done
     *  to compute the period as a fraction of the outside period.
     *  @return The value of the period parameter.
     *  @exception IllegalActionException If the period parameter is
     *  is not of type DoubleToken or IntToken.
     */
    public double getPeriod() throws IllegalActionException {
        Token token;
        double periodValue = 0.0;
        Director outsideDirector; 
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        
        outsideDirector = _getOutsideDirector();
        if (outsideDirector instanceof DTDirector) {
            DTDirector outsideDTDirector = (DTDirector) outsideDirector;
            token = outsideDTDirector.period.getToken();
            periodValue = 1.0/outsideDTDirector.getRepeats(container);
        } else {	
            token = period.getToken();
            periodValue = 1.0;
        }
        
        if (token instanceof DoubleToken) {
            periodValue = periodValue * ((DoubleToken) token).doubleValue();
        } else if (token instanceof IntToken) {
            periodValue = periodValue * ((IntToken) token).intValue();
        } else {
            throw new IllegalActionException(
                  "Illegal period parameter value");
        }
        return periodValue;
    }
    
    
    /** Initialize all the actors associated with this director by calling
     *  super.initialize().  Create a cached table of all the actors 
     *  associated with this director.  Determine which actors need to generate
     *  initial tokens for causality. All actors with nonhomogeneous input 
     *  ports will need to generate initial tokens for all its output ports. 
     *  For example, if actor A has a nonhomogeneous input port and an output
     *  port with production rate 'm' then actor A needs to produce 'm' initial
     *  tokens on the output port.  The director will handle the production of 
     *  initial tokens if the actor does not have a parameter 'initialOutputs'
     *  on its output ports. 
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {

        _requestRefireAt(0.0);
        _actorTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _buildActorTable();
        super.initialize();
        
        // This portion figures out which actors should generate initial tokens
        ListIterator receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            
            IOPort currentPort = currentReceiver.getContainer();
            int rate = 0;
            Actor actor = (Actor) currentPort.getContainer(); 
            String name = ((Nameable)actor).getFullName();

            DTActor dtActor = (DTActor) _allActorsTable.get(actor);
            debug.println(dtActor);
            if (dtActor == null) {
                throw new IllegalActionException(
                          "DT internal error: unknown actor");
            }
            
            Parameter param = (Parameter) currentPort.getAttribute("tokenConsumptionRate");
    	    if ((param != null)&&(currentPort.isInput())) {
               rate = ((IntToken)param.getToken()).intValue();
               if (rate > 1) dtActor.shouldGenerateInitialTokens = true;
            }
    	}
        _displayActorTable();
        _displayArcReceiverTable();
        
        
        // This portion generates the initial tokens for actors with nonhomogeneous outputs
        receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            TypedIOPort currentPort = (TypedIOPort) currentReceiver.getContainer();
            Actor toActor = (Actor) currentPort.getContainer();
            TypedIOPort fromPort = currentReceiver.getSourcePort();
            Actor fromActor = (Actor) fromPort.getContainer();
            Parameter param = (Parameter) fromPort.getAttribute("tokenProductionRate");
            int outrate = 0;
            if ((param != null) && (fromPort.isOutput())) {
                outrate = ((IntToken)param.getToken()).intValue();
            }
            SDFScheduler currentScheduler = (SDFScheduler) getScheduler();
            
            String name = ((Nameable)fromActor).getFullName();

            DTActor dtFromActor = (DTActor) _allActorsTable.get(fromActor);
            
            if (dtFromActor != null) {
                if (dtFromActor.shouldGenerateInitialTokens) {
                    int numberInitialTokens = currentScheduler.getTokenInitProduction(currentPort);
                    debug.prompt("initial port "+currentPort.getType());
                    for(int j=0;j<outrate;j++) {
                        // FIXME:  should check what token basetype 
                        // for the port and generate such.
                        currentReceiver.put(new DoubleToken(0.0));
                    }
                }
            }
        }
        _displayActorTable();
        _displayArcReceiverTable();
    }
    
    
    /** Reset the internal cache containing a list of actors and 
     *  receivers under this director. Notify parent class about
     *  invalidated schedule.
     */
    public void invalidateSchedule() {
        _receiverTable = new ArrayList();
        super.invalidateSchedule();
    }
    
    /** Return a new receiver consistent with the DT domain.
     *  This function is called when a connection between an output port
     *  of an actor and an input port of another actor is made in Vergil.
     *  This function is also called during the preinitialize() stage of
     *  a toplevel director.  This function may also be called prior to
     *  the preinitialize() stage of a non-toplevel director.
     *  
     *  @return A new DTReceiver.
     */
    public Receiver newReceiver() {
    
        DTReceiver currentReceiver = new DTReceiver();
        _receiverTable.add(currentReceiver);
        return currentReceiver;
    }
    
    
    /** Set current time to zero. Invoke the preinitialize() methods of
     *  all actors deeply contained by the container by calling
     *  super.preinitialize(). This method is invoked once per execution,
     *  before any iteration; i.e. every time the GO button is pressed.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException If the preinitialize() method 
     *   of the container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _currentTime = 0.0;
        _lastPrefireTime = 0.0;
        
        // FIXME:  creating a new ArrayList() may not be the way to go
        //         for hierarchical topologies.  Maybe this should be 
        //         moved to wrapup()
        // FIXME:  receiverTable reset is currently put here because
        //         Vergil calls newReceiver() when you connect actors
        super.preinitialize();
    }
    
    
    /** Request outside director to fire this director's container
     *  again for the next period.  
     *  @return true if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the parent class throws
     *  it.
     */
    public boolean postfire() throws IllegalActionException {
        boolean returnValue = super.postfire();
        
        double timeIncrement = getPeriod();
        _requestRefireAt(_lastPrefireTime + timeIncrement);
        return returnValue;
    }

      
    /** Set the current time of the model under this director.
     *  Setting the time back to the past is allowed in DT.
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) {
        _currentTime = newTime;
    }
    

    
   /** Clear-up cached tables in the DTDirector so that next
    *  execution can start fresh.
    *  @exception IllegalActionException If the parent class 
    *  throws it
    */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _receiverTable = new ArrayList();
    }





    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
    
    /**Get the number of times an actor repeats in the schedule of an 
     * SDF graph.  If the actor does not exist, throw an exception.
     * @param a The actor whose firing count is needed
     * @exception IllegalActionException If actor does not exist.
     */
    protected int getRepeats(Actor a) throws IllegalActionException {
        ListIterator actorIterator = _actorTable.listIterator();
        int repeats = 0;
        
        foundRepeatValue:
        while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            if (a.equals(currentActor.actor)) {
                repeats = currentActor.repeats;
                break foundRepeatValue;
            }
        }
        
        if (repeats == 0) {
            throw new IllegalActionException(
                      "internal DT error: actor with zero firing count");
        }
    	return repeats;
    }
    
    

    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *  @exception IllegalActionException If the scheduler is null 
     */
    private void _buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler== null) 
            throw new IllegalActionException("Attempted to fire " +
                    "DT system with no scheduler");
        Enumeration allActorsScheduled = currentScheduler.schedule();
        
        
        int actorsInSchedule = 0;                    
        while (allActorsScheduled.hasMoreElements()) {
            Actor actor = (Actor) allActorsScheduled.nextElement();
            String name = ((Nameable)actor).getFullName();
            DTActor dtActor = (DTActor) _allActorsTable.get(actor);
            if (dtActor==null) {
              _allActorsTable.put(actor, new DTActor(actor));
              dtActor = (DTActor) _allActorsTable.get(actor);
              _actorTable.add(dtActor);
            }
            dtActor.repeats++;
            actorsInSchedule++;
        }
        
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor, new DTActor((Actor)getContainer()));
        DTActor  dtActor = (DTActor) _allActorsTable.get(actor);
        dtActor.repeats = 1;
        _actorTable.add(dtActor);
        
        _displayActorTable();
        ListIterator receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver.determineEnds(this);
        }
        
        receiverIterator = _receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
    	   DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
    	   currentReceiver.calculateDeltaT();
        }
        
        
        _displayActorTable();
        _displayArcReceiverTable();
    }


    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException if there is a problem in 
     *   obtaining the number of initial token for delay actors
     */
    private void _displayActorTable() throws IllegalActionException {
         debug.println("\nACTOR TABLE with "+_actorTable.size()+" unique actors");
         debug.println("---------------------------------------");
         ListIterator actorIterator = _actorTable.listIterator();
         while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            String actorName = ((Nameable) currentActor.actor).getName();
            
            debug.print(actorName+" repeats:"+currentActor.repeats);
            debug.print(" initial_tokens? "+currentActor.shouldGenerateInitialTokens);
            
            if (currentActor.actor instanceof Delay) {
                Delay delay = (Delay) currentActor.actor;
                MatrixToken initialTokens = (MatrixToken)delay.initialOutputs.getToken();
                int delayCount = initialTokens.getColumnCount();
                
                debug.print(" **DELAY** with "+delayCount+" initial tokens");
            }
            
            if ( !((ComponentEntity) currentActor.actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
         }
    }
    
    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _displayArcReceiverTable() { 
        debug.print("\nARC RECEIVER table with "+_receiverTable.size());
        debug.println(" unique receivers");
        
        ListIterator receiverIterator = _receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver.displayReceiverInfo();
        }
        debug.println("\n");
    }
 
   /** For debugging purposes.  Display the list of attributes
     *  inside a given named object
     *  @param obj The named object that has a list of attributes
     */
    private void _displayAttributesList(NamedObj obj)
    {
    	List list = obj.attributeList();
    	Iterator listIterator = list.iterator();
    	
    	debug.println("attribute List:");
    	while(listIterator.hasNext()) {
    	    Attribute attribute = (Attribute) listIterator.next();
    	    debug.println(attribute);
    	}
    }
 
 
    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     *  @param obj The composite entity with a list of contained entities.
     */
    private void _displayEntityList(CompositeEntity obj) {
    
        List list = obj.entityList();
    	Iterator listIterator = list.iterator();
    	
    	debug.println("\nentity List:");
    	while(listIterator.hasNext()) {
    	    Entity entity = (Entity) listIterator.next();
    	    debug.println(entity);
    	}
    	debug.println("\n");
    }

    
    /** Convenience method for asking the executive director to fire this
     *  director's container again at a specific time in the future.
     *  @param time The time when this director's container should be fired
     *  @exception IllegalActionException If getting the container or 
     *  executive director fails
     */
    private void _requestRefireAt(double time) throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        
        if (outsideDirector != null) {
            outsideDirector.fireAt(container,time);   
        }
    }
    
    /** Convenience method for getting the director of the container that 
     *  holds this director.  If this director is inside a toplevel 
     *  container, then the returned value is null.
     *  @returns The executive director
     */
    private Director _getOutsideDirector() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        
        return outsideDirector;
    }

    
    /** Convenience method for getting the token consumption rate of a 
     *  specified port. If the port does not have the attribute 
     *  "tokenConsumptionRate" then return a rate of 1.
     *  @param ioport The port to be queried
     *  @returns The token consumption rate of the port.
     *  @exception IllegalActionException If getting an attribute from
     *  this port fails.
     */
    private int _getTokenConsumptionRate(IOPort ioport) throws IllegalActionException {
        int rate;
        Parameter param = (Parameter) ioport.getAttribute("tokenConsumptionRate");
    	if (param != null) {
            rate = ((IntToken)param.getToken()).intValue();
        } else rate = 1;
        
        return rate;
    }
    
   
    /** Most of the constructor initialization is relegated to this method.
     *  Initialization process includes :
     *    - create a new actor table to cache all actors contained
     *    - create a new receiver table to cache all receivers contained
     *    - set default number of iterations
     *    - set period value
     */
    private void _init() {
    	try {
            period = new Parameter(this,"period",new DoubleToken(1.0));
            _actorTable = new ArrayList();
            _receiverTable = new ArrayList();
            iterations.setToken(new IntToken(0));
            debug = new DTDebug(true);
    	} catch (Exception e) {
    	    throw new InternalErrorException(
                    "unable to initialize DT Director:\n" +
                    e.getMessage());
    	}
    }
    
       
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // ArrayList to keep track of all actors scheduled by DTDirector
    private ArrayList _actorTable;
    
    // ArrayList used to cache all receivers managed by DTDirector
    private ArrayList _receiverTable;
    
    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;
    
    // time since the last prefire() call
    private double _lastPrefireTime;
    
    // display for debugging purposes
    private DTDebug debug;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    // Inner class to cache important variables for contained actors 
    private class DTActor {
    	private Actor    actor;
    	private int      repeats;
        private boolean  shouldGenerateInitialTokens;
   
    	/* Construct the information on the contained Actor
    	 * @param a The actor  
    	 */	
    	public DTActor(Actor a) {
    		actor = a;
    		repeats = 0;
            shouldGenerateInitialTokens = false;
    	}
    }
}
