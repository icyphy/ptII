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
import ptolemy.data.type.*;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sdf.lib.Delay;

import java.util.*;

//////////////////////////////////////////////////////////////////////////
//// DTDirector
/**
 
<h1>DT overview</h1>
The Discrete Time (DT) domain is a timed extension of the Synchronous Dataflow 
(SDF) domain.  Like SDF, it has static scheduling of the dataflow graph 
model. Likewise, DT requires that the rates on the ports of all actors be
known beforehand and fixed. DT handles feedback systems in the same way that 
SDF does, but with additional constraints on initial tokens.
<p>
<h1>Local and Global Time</h1>
Because of the inherent concurency occuring within SDF dataflow graph models,
there are two notions of time in DT -- global time and local time.  Global time
increases steadily as execution progresses.  Moreover, global time increments by 
fixed discrete chunks of time based on the value of the <i>period</i> parameter. 
On the other hand, local time applies to each of the actors in the model. All the
actors have distinct local times as an iteration proceeds. The local time of
an actor during an iteration depends on the global time, period, firing count,
port rates, and the schedule. These local times obey the following constraint:

<center>Global Time  <=  Local Time <= (Global Time + period)</center>
   
The exact way that local time increments during an iteration is described in
detail in the DTReceiver documentation.
<p>.   
<h1>DT Features</h1>
The design of the DT domain is motivated by the following criteria:
<OL>
<LI>) Uniform Token Flow:  The time interval between tokens should be regular
    and unchanging.  This conforms to the idea of having sampled systems 
    with fixed rates. Although the tokens flowing in DT do not keep internal
    time stamps, each actor can query the DT director for its own local time.
    This local time is uniformly increasing by a constant fraction of the
    director's <i>period</I>.  Local time is incremented every time the get() 
    method is called to obtain a token. 
<LI>) Causality: Tokens produced by an actor should only depend on tokens produced
    or consumed in the past. This makes sense because we don't expect an actors to
    produce a token before it can calculate the token's value.  For example,
    if an actor needs three tokens A, B, and C to  compute token D, then the time
    when tokens A, B, and C are consumed should be earlier than than or equal to
    the time when token D is produced.  Note that in DT, time does not get 
    incremented due to computation.
<LI>) SDF-style semantics: Ideally, we want DT to be a timed-superset of SDF with
    compatible token flow and scheduling.  However, we can only approximate
    this behavior. It is not possible to have uniform token flow, causality,
    and SDF-style semantics at the same time.  Causality breaks for non-
    homogeneous actors in a feedback system when fully-compatible SDF-style
    semantics is adopted.  To remedy this situation, every actor in DT that 
    has non-homogeneous input ports should produce initial tokens at each 
    of its output ports.
</OL>
</p>
<p>
<h1> Design Notes</h1>
DT (Discrete Time) is a timed model of computation.  In order 
to benefit from the internal time-keeping mechanism of DT, one should
use actors aware of time. For example, one should use TimedPlotter or
TimedScope instead of SequencePlotter or SequenceScope.  

Top-level DT Directors have a <i>period</i> parameter that can be set by the
user.  Setting the period parameter of a non-top-level DT Director 
under hierarchical DT has no meaning; and hence will be ignored.
<p>  

@see ptolemy.domains.dt.kernel.DTReceiver
@see ptolemy.domains.sdf.kernel.SDFDirector
@see ptolemy.domains.sdf.kernel.SDFReceiver
@see ptolemy.domains.sdf.kernel.SDFScheduler
 
       
 @author C. Fong
 @version  
*/
/* FIXMEs
<p>
<h1> DT and Vergil </h1>

Non-hierarchical DT has been tested to work with Vergil.  However,
there is only limited support for hierarchical DT at the moment.  It is
possible to use hierarchical DT in Vergil by doing the following steps
<OL>
<LI>1.) Create and save the inside model in Vergil. 
<LI>2.) Modify the inside model MOML file with the following modifications:
    <UL>
    <LI> change class= keyword to extends= ; 
    <LI> change <entity> keyword to <class>;
    <LI> provide a name for the <class> which is "" (blank) by default;
    <LI> change the <port> to include <property name="output"/> 
                                or <property name="input"/>
    </UL>
<LI>3.) Modify the palette XML file to include the inside model
   OLD:  <import source="name.xml"/>
         <entity name="name" class=".name">
   NEW:  <input source="name.xml"/>
</OL>
</p>
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
     *  @exception NameDuplicationException If the container is not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public DTDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
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
    
    // FIXME: this function is a test only
    public void fireAt(Actor actor, double time)
            throws IllegalActionException {
        setCurrentTime(time);
    }
    
    
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
        _reset();
        DTDirector newObject = (DTDirector)(super.clone(ws));
        newObject.period = (Parameter)newobj.getAttribute("period");
        return newObject;
    }
    
    
    /** Go through the schedule and iterate every actor with calls to
     *  prefire() , fire() , and postfire().
     *  
     *  @exception IllegalActionException If an actor executed by this
     *  director return false in its prefire().
     */
    public void fire() throws IllegalActionException {
    //  -fire-
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();
        double currentTime;
        
        if (outsideDirector != null) {
            currentTime = outsideDirector.getCurrentTime();
        } else {
            currentTime = getCurrentTime();
        }
        // Some timed directors (such as CT) increment time after prefire() 
        // and during fire(), so time may not be properly updated 
        // before this stage of the execution.
        _checkValidTimeIntervals();
        
        if (! _isFiringAllowed) {
            return;
        }
        
        debug.println("DTDirector fire  "+currentTime);
        if (container == null) {
            throw new InvalidStateException("DTDirector " + getName() +
                    " fired, but it has no container!");
        } else {
                     
            Scheduler s = getScheduler();
            if (s == null)
                throw new IllegalActionException("Attempted to fire " +
                        "DT system with no scheduler");
            Enumeration allactors = s.schedule();
            int i=1;
            while (allactors.hasMoreElements()) {
            	i++;
                
                Actor actor = (Actor)allactors.nextElement();
                
                boolean isFiringNonDTCompositeActor = false;
               
                if (actor instanceof CompositeActor) {
                    CompositeActor compositeActor = (CompositeActor) actor;
		            Director  insideDirector = compositeActor.getDirector();
		            
		            if ( !(insideDirector instanceof DTDirector)) {
		                isFiringNonDTCompositeActor = true;
		                _insideDirector = insideDirector;
		            }
		        }
		        
		        if (isFiringNonDTCompositeActor) {
		            _pseudoTimeEnabled = true;
		        }
		            
		            
                if(!actor.prefire()) {
                    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
                }

                if(_debugging)
                    _debug("Firing " + ((Nameable)actor).getFullName());
               
                actor.fire();
                _postfirereturns = actor.postfire();
                
                if (isFiringNonDTCompositeActor) {
		            _pseudoTimeEnabled = false;
		        }
		        
            }
        }
        if ((outsideDirector != null) && _shouldDoInternalTransferOutputs) {
            _issueTransferOutputs();
        } 
    }

    /** Return the current time.
     *  @return the current time
     */
    public double getCurrentTime() {
    // -getCurrentTime-
        double timeValue;
        if (_pseudoTimeEnabled == true) {
            timeValue = _insideDirector.getCurrentTime();
        } else {
            timeValue = super.getCurrentTime();
        }
        return timeValue;
    }

    
    /** Return the time value of the next iteration.
     *  @return The time of the next iteration.
     */
    public double getNextIterationTime() {
        return Double.MAX_VALUE;
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
    //  -getPeriod-
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
    //  -initialize-
    
        _requestRefireAt(0.0);
        _actorTable = new ArrayList();
        _allActorsTable = new Hashtable();
        _buildActorTable();
        _buildOutputPortTable();
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
               if (rate > 1) dtActor._shouldGenerateInitialTokens = true;
            }
    	}
        _displayActorTable();
        
        // This portion generates the initial tokens for actors with nonhomogeneous outputs
        receiverIterator = _receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            TypedIOPort currentPort = (TypedIOPort) currentReceiver.getContainer();
            Actor toActor = (Actor) currentPort.getContainer();
            TypedIOPort fromPort = currentReceiver.getSourcePort();
            Type fromType = fromPort.getType();
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
                if (dtFromActor._shouldGenerateInitialTokens) {
                    int numberInitialTokens = currentScheduler.getTokenInitProduction(currentPort);
                    debug.prompt("initial port: "+fromType+" to "+currentPort.getType());
                    for(int j=0;j<outrate;j++) {
                        // FIXME:  should check what token basetype 
                        // for the port and generate such. 
                        // move this out of the loop 
                        if (fromType.isEqualTo(BaseType.BOOLEAN)) {
                            currentReceiver.put(new BooleanToken(false));
                        } else if (fromType.isEqualTo(BaseType.DOUBLE)) {
                            currentReceiver.put(new DoubleToken(0.0));
                        } else if (fromType.isEqualTo(BaseType.INT)) {
                            currentReceiver.put(new IntToken(0));
                        }
                    }
                }
            }
        }
        _displayActorTable();
        _displayReceiverTable();
    }
    
    
    /** Process the mutation that occurred.  Reset this director
     *  to an uninitialized state.  Notify parent class about
     *  invalidated schedule.  This method is called when an entity
     *  is instantiated under this director. This method is also 
     *  called when a link is made between ports and/or relations.
     *  see also other mutation methods:
     *    <p><UL>
     *    <LI> void attributeChanged(Attribute)
     *    <LI> void attributeTypeChanged(Attribute)
     *    </UL></p>
     */
    public void invalidateSchedule() {
    //  -invalidateSchedule-
        _reset();
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
    //  -preinitialize-
        
        // FIXME:  creating a new ArrayList() may not be the way to go
        //         for hierarchical topologies.  Maybe this should be 
        //         moved to wrapup()
        // FIXME:  receiverTable reset is currently put here because
        //         Vergil calls newReceiver() when you connect actors
        super.preinitialize();
    }
    
    
    /** Request the outside director to fire this director's container
     *  again for the next period. 
     * 
     *  @return true if the Director wants to be fired again in the
     *  future.
     *  @exception IllegalActionException If the parent class throws
     *  it.
     */
    public boolean postfire() throws IllegalActionException {
    //  -postfire-

        _makeTokensAvailable();
        if (! _isFiringAllowed) {
          return true;
        }
        
        boolean returnValue = super.postfire();
        double timeIncrement = getPeriod();
        
        _requestRefireAt(_formerValidTimeFired + timeIncrement);
        return returnValue;
    }

          
    /** Set the current time of the model under this director.
     *  Setting the time back to the past is allowed in DT.
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) {
        // _currentTime is inherited from base Director
        _currentTime = newTime;
    }
    
    
    /** This is called by the outside director to get tokens 
     *  from the inside director. 
     *  Return true if transfers data from an output port of the
     *  container to the ports it is connected to on the outside.
     *  This method differs from the base class method in that this
     *  method will transfer all available tokens in the receivers,
     *  while the base class method will transfer at most one token.
     *  This behavior is required to handle the case of non-homogeneous
     *  opaque composite actors. The port argument must be an opaque
     *  output port.  If any channel of the output port has no data,
     *  then that channel is ignored.
     *
     *  @exception IllegalActionException If the port is not an opaque
     *   output port.
     *  @param port The port to transfer tokens from.
     *  @return True if data are transferred.
     */
    public boolean transferOutputs(IOPort port)
            throws IllegalActionException {
    //  -transferOutputs-
        if (!port.isOutput() || !port.isOpaque()) {
            throw new IllegalActionException(this, port,
                    "transferOutputs: port argument is not " +
                    "an opaque output port.");
        }
        boolean trans = false;
        Receiver[][] insiderecs = port.getInsideReceivers();
        if (insiderecs != null) {
            for (int i = 0; i < insiderecs.length; i++) {
                if (insiderecs[i] != null) {
                    for (int j = 0; j < insiderecs[i].length; j++) {
			while (insiderecs[i][j].hasToken()) {
                            try {
                                ptolemy.data.Token t = insiderecs[i][j].get();
                                port.send(i, t);
                                trans = true;
                            } catch (NoTokenException ex) {
                                throw new InternalErrorException(
                                        "Director.transferOutputs: " +
                                        "Internal error: " +
                                        ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
        return trans;
    }
    

    
   /** Reset this director to an uninitialized state.
    *  @exception IllegalActionException If the parent class 
    *  throws it
    */
    public void wrapup() throws IllegalActionException {
    //  -wrapup-
        super.wrapup();
        _reset();
    }





    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////
    
    /**Get the number of times an actor repeats in the schedule of an 
     * SDF graph.  If the actor does not exist, throw an exception.
     * @param a The actor whose firing count is needed
     * @exception IllegalActionException If actor does not exist.
     */
    protected int getRepeats(Actor actor) throws IllegalActionException {
        ListIterator actorIterator = _actorTable.listIterator();
        int repeats = 0;
        
        foundRepeatValue:
        while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            if (actor.equals(currentActor._actor)) {
                repeats = currentActor._repeats;
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
            dtActor._repeats++;
            actorsInSchedule++;
        }
        
        // include the container as an actor.  This is needed for TypedCompositeActors
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        _allActorsTable.put(actor, new DTActor((Actor)getContainer()));
        DTActor dtActor = (DTActor) _allActorsTable.get(actor);
        dtActor._repeats = 1;
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
        _displayReceiverTable();
    }
    
    
    private void _buildOutputPortTable() throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        
        Iterator outports = container.outputPortList().iterator();
        while(outports.hasNext()) {
            IOPort port = (IOPort)outports.next();
            
            _outputPortTable.add(new DTIOPort(port));
        }
        
    }
    
    
    private void _checkValidTimeIntervals() throws IllegalActionException {
    //  -checkValidTimeIntervals-
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = _getOutsideDirector();
        debug.println("shouldIgnoreFire subroutine called");
        
        // No need to check if this director is in the top level.
        if (outsideDirector == null) {
            return;
        }
        
        // No need to check if the executive director is also a DTDirector
        if (outsideDirector instanceof DTDirector) {
            return;
        } 
        
        
        double currentTime = outsideDirector.getCurrentTime();
        double currentPeriod = getPeriod();
        double timeElapsed = currentTime - _formerValidTimeFired;
        
        debug.println("DT Director just started fire----------------"+_formerValidTimeFired+" "+currentTime);
        
        
        if ((currentTime != 0) && ((currentTime - _formerTimeFired) < TOLERANCE )) {
            // duplicate firings at the same time should be ignored
            _isFiringAllowed = false;
            _shouldDoInternalTransferOutputs = false;
            _makeTokensUnavailable();
            return;
        }  else {
            _formerTimeFired = currentTime;
        } 
        
        // This case occurs during startup
        if (currentTime == 0) {
            _formerValidTimeFired = currentTime;
            _issuePseudoFire(currentTime);
            _isFiringAllowed = true;
            return;
        }
        
        double iterationTimeElapsed = currentPeriod - timeElapsed;
        
        if (iterationTimeElapsed < -TOLERANCE ) {
            // this case should not occur
            debug.prompt("InternalErrorException time: "+_formerValidTimeFired+" "+currentTime);    
        }
        
        if (iterationTimeElapsed > TOLERANCE) {

            Iterator outputPorts = _outputPortTable.iterator();
            _isFiringAllowed = false;
            while(outputPorts.hasNext()) {
                DTIOPort dtport = (DTIOPort) outputPorts.next();
                Receiver[][] insideReceivers = dtport._port.getInsideReceivers();
                double deltaT = ((DTReceiver)insideReceivers[0][0]).getDeltaT();
                double ratio = timeElapsed/deltaT;
                
                if (Math.abs(Math.round(ratio) - ratio) < TOLERANCE) {
                    // firing at a time when transferOutputs should be called
                    
                    debug.println("*************** fractional fire ratio "+ratio+" should transferOutputs");
                    dtport._shouldTransferOutputs = true;
                    _isFiringAllowed = false;
                    _shouldDoInternalTransferOutputs = true;
                } else {
                    // firing at a time when transferOutputs should not be called

                	for(int i=0;i<dtport._port.getWidth();i++) {
                	    for(int j=0;j<insideReceivers[i].length;j++) {
                            ((DTReceiver) insideReceivers[i][j]).overrideHasToken=true;   	        
                	    }
            	    }     
                    debug.println("************* nonfractional fire ratio "+ratio+" don't transferOutputs"); 
                    dtport._shouldTransferOutputs = false;
                }
            }
        } else {
            // this case occurs during period intervals
            debug.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&UPDATE&&&&&&&&&&&&&&&&&&&&&&&&&&");
            _issuePseudoFire(currentTime);
            _formerValidTimeFired = currentTime;
            _isFiringAllowed = true;
            _shouldDoInternalTransferOutputs = false;
        }
    }


    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException if there is a problem in 
     *   obtaining the number of initial token for delay actors
     */
    private void _displayActorTable() throws IllegalActionException {
         //debug.println("\nACTOR TABLE with "+_actorTable.size()+" unique actors");
         debug.println("---------------------------------------");
         ListIterator actorIterator = _actorTable.listIterator();
         while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            String actorName = ((Nameable) currentActor._actor).getName();
            
            debug.print(actorName+" repeats:"+currentActor._repeats);
            debug.print(" initial_tokens? "+currentActor._shouldGenerateInitialTokens);
            
            if (currentActor._actor instanceof Delay) {
                Delay delay = (Delay) currentActor._actor;
                MatrixToken initialTokens = (MatrixToken)delay.initialOutputs.getToken();
                int delayCount = initialTokens.getColumnCount();
                
                debug.print(" **DELAY** with "+delayCount+" initial tokens");
            }
            
            if ( !((ComponentEntity) currentActor._actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
         }
    }
    
    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void _displayReceiverTable() { 
    //  -displayReceiverTable-
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
    
    private void _displayContainerOutputPorts() throws IllegalActionException {
    
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        
        debug.println("\ndirector container output port list:");
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            debug.println(" ->"+port);
            _displayPortInsideReceivers(port);
        }
        debug.println("\n");
        //debug.prompt("ContainerOutputPorts");
    }
    
    private void _displayPortRemoteReceivers(IOPort port) {
        Receiver[][] remoteReceivers = port.getRemoteReceivers();
    		    
    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<remoteReceivers[i].length;j++) {
    	        debug.println("  -->"+remoteReceivers[i][j]);
    	    }
    	}
    }
    
    private void _displayPortInsideReceivers(IOPort port) throws IllegalActionException {
        Receiver[][] portReceivers = port.getInsideReceivers();
    		    
    	for(int i=0;i<port.getWidth();i++) {
    	    for(int j=0;j<portReceivers[i].length;j++) {
    	        debug.println("  ->"+portReceivers[i][j]);
    	        ((DTReceiver)portReceivers[i][j]).displayReceiverInfo();
    	    }
    	}
    }
    
    private void _issuePseudoFire(double currentTime)
                 throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        Receiver[][] insideReceivers;
        
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            insideReceivers = port.getInsideReceivers();
            DTReceiver receiver = (DTReceiver) insideReceivers[0][0];
            double deltaT = receiver.getDeltaT();
            int periodDivider = receiver.getPeriodDivider();
            debug.println("request pseudo-fire at "+deltaT+" intervals. "+periodDivider);
            for(int n=1;n<periodDivider;n++) {
                _requestRefireAt(currentTime + n*deltaT);
                debug.println(" request pseudo-fire at "+(currentTime + n*deltaT));
            }
        }
    }

    
    private void _issueTransferOutputs() throws IllegalActionException {
        Director outsideDirector = _getOutsideDirector();
        
        Iterator outputPorts = _outputPortTable.iterator();
        while(outputPorts.hasNext()) {
            DTIOPort dtport = (DTIOPort) outputPorts.next();
        
            if (dtport._shouldTransferOutputs) {
                outsideDirector.transferOutputs(dtport._port);
            }
        }
    }
   
    
    private void _makeTokensAvailable() throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            Receiver[][] portReceivers = port.getInsideReceivers();
    		    
    	    for(int i=0;i<port.getWidth();i++) {
    	        for(int j=0;j<portReceivers[i].length;j++) {
                    ((DTReceiver) portReceivers[i][j]).overrideHasToken=false;   	        
        	    }
    	    }  
        }
    }
    
    
    private void _makeTokensUnavailable() throws IllegalActionException {
        List list = ((TypedCompositeActor)getContainer()).outputPortList();
        Iterator listIterator = list.iterator();
        
        while(listIterator.hasNext()) {
            IOPort port = (IOPort) listIterator.next();
            Receiver[][] portReceivers = port.getInsideReceivers();
    		    
    	    for(int i=0;i<port.getWidth();i++) {
    	        for(int j=0;j<portReceivers[i].length;j++) {
                    ((DTReceiver) portReceivers[i][j]).overrideHasToken=true;   	        
        	    }
    	    }  
        }
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
            _reset();
            iterations.setToken(new IntToken(0));
            debug = new DTDebug(false);
    	} catch (Exception e) {
    	    throw new InternalErrorException(
                    "unable to initialize DT Director:\n" +
                    e.getMessage());
    	}
    }
    
    private void _reset() {
        _actorTable = new ArrayList();
        _receiverTable = new ArrayList();
        _outputPortTable = new ArrayList();
        _allActorsTable = new Hashtable(); 
        _currentTime = 0.0;
        _formerTimeFired = 0.0;
        _formerValidTimeFired = 0.0;
        _isFiringAllowed = true;
        _shouldDoInternalTransferOutputs = false;
    }
    
       
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // ArrayList to keep track of all actors scheduled by DTDirector
    private ArrayList _actorTable;
    
    // ArrayList used to cache all receivers managed by DTDirector
    private ArrayList _receiverTable;
    
    // Hashtable for keeping track of actor information
    private Hashtable _allActorsTable;
    
    // The time when the previous valid prefire() was called
    private double _formerValidTimeFired;
    
    // The time when the previous valid or invalid prefire() was called
    private double _formerTimeFired;
    
    // used to keep track of whether firing can be done at current time
    private boolean _isFiringAllowed; 

    // ArrayList to keep track of all container output ports     
    private ArrayList _outputPortTable;
    
    // used to determine whether the director should call transferOutputs() 
    private boolean _shouldDoInternalTransferOutputs;
    
    // display for debugging purposes
    private DTDebug debug;

    private boolean _pseudoTimeEnabled = false;
    private Director _insideDirector;
    
    private static final double TOLERANCE = 0.0000000001;
    

    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    // Inner class to cache important variables for contained actors 
    private class DTActor {
    	private Actor    _actor;
    	private int      _repeats;
        private boolean  _shouldGenerateInitialTokens;
   
    	/* Construct the information on the contained Actor
    	 * @param a The actor  
    	 */	
    	public DTActor(Actor actor) {
    		_actor = actor;
    		_repeats = 0;
            _shouldGenerateInitialTokens = false;
    	}
    }
    
    // Inner class to cache important variables for container output ports 
    private class DTIOPort {
        private IOPort _port;
        private boolean _shouldTransferOutputs;
        
        /*  Construct the information on the output port
         *  @param p The port
         */
        public DTIOPort(IOPort port) {
            _port = port;
            _shouldTransferOutputs = false;
        }
    }
}
