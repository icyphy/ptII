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

/*
 * has parameter period which has default value of 1.0
 * parameter period should only be changed when execution is stopped.
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

    /**  Construct a director in the  workspace with an empty name.
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
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
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
     *  DoubleToken.  Its value defaults to 1.0.
     */
    public Parameter period;
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the director into the specified workspace. This calls the
     *  base class and then copies the parameter of this director.  The new
     *  actor will have the same parameter values as the old.
     *  The period parameter is explicitly cloned in this method.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If one of the attributes
     *   cannot be cloned.
     */
    public Object clone(Workspace ws)
            throws CloneNotSupportedException {
        DTDirector newobj = (DTDirector)(super.clone(ws));
        newobj.period = (Parameter)newobj.getAttribute("period");
        return newobj;
    }

    /** Go through the schedule and iterate every actor with calls to
     *  prefire() , fire() , and postfire().
     *
     */
    public void fire() throws IllegalActionException {
        
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        if (container.getExecutiveDirector() == null) {
            //debug.debug.prompt("toplevel");
        } else {
            // FIXME: resyncLocalTimes for receivers
        }
        debug.println("\nDTDirector "+this.getName()+" fire **************************************************");
       

	
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
                debug.println("{ "+((Nameable)actor).getName()+" "+((Nameable)actor).getFullName()+" start fire--------------------");
                if(!actor.prefire()) {
                    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
                }

                if(_debugging)
                    _debug("Firing " + ((Nameable)actor).getFullName());
		
		
                actor.fire();
                
                _postfirereturns = _postfirereturns && actor.postfire();
                debug.println("} "+"------------------- end fire--------------------");
            }
        }
        debug.println("DTDirector "+this.getName()+" end fire ************************************************\n");
        try {
            //if (_debugOn) java.lang.Thread.sleep(1000);
        } catch(Exception e) {}
    }
    

    public boolean postfire() throws IllegalActionException {
        boolean returnValue = super.postfire();
        
        double timeIncrement = getPeriod();
        requestRefireAt(_lastPrefireTime + timeIncrement);
        return returnValue;
    }

    /** Return a new receiver consistent with the DT domain.
     *  @return A new DTReceiver.
     */
    public Receiver newReceiver() {
    
        DTReceiver currentReceiver = new DTReceiver();
        receiverTable.add(currentReceiver);
        // newReceiver is also called by Vergil during topology change
        //debug.printStackTrace();
        //debug.prompt("newReceiver "+currentReceiver.toString());
        return currentReceiver;
    }

    
    /** prefire() is currently under development to handle hierarchical
     *  compositions of DT with other domains. Please ignore this method for now
     *  @return 
     *  @exception 
     */
    public boolean prefire() throws IllegalActionException {

        // FIXME: This prefire() has bugs in DT inside DT and DE
        boolean prefireReturnValue = true;
        _postfirereturns = true;

        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        
        if (outsideDirector != null) {
            double currentTime = outsideDirector.getCurrentTime();
            double currentPeriod = ((DoubleToken) period.getToken()).doubleValue();
            if ((currentTime - _lastPrefireTime) < currentPeriod) {
                debug.prompt(" prefire: should not fire");
            }
            _lastPrefireTime = currentTime;
        }
        //debug.prompt("prefire "+_lastPrefireTime);
        //TypedCompositeActor container = ((TypedCompositeActor)getContainer());
	    Iterator inputPorts = container.inputPortList().iterator();
	    int inputCount = 0;
	    while(inputPorts.hasNext()) {
	        IOPort inputPort = (IOPort) inputPorts.next();
	        debug.println("IOPort "+inputPort);
	        if (_debugging) _debug("checking input " +
                    inputPort.getFullName());

	        int threshold = SDFScheduler.getTokenConsumptionRate(inputPort);
	        int mymy = getTokenConsumptionRate(inputPort);
	        System.out.println("threshold "+threshold);
	        System.out.println("mymy "+mymy);
	        if (_debugging) _debug("Threshold = " + threshold);
	        Receiver receivers[][] = inputPort.getReceivers();

	        int channel;
	        System.out.println("getWidth ->"+inputPort.getWidth());
	        for(channel = 0; channel < inputPort.getWidth(); channel++) {
		        if(!receivers[channel][0].hasToken(threshold)) {
		            System.out.println(receivers[channel][0]+" "+(receivers[channel][0] instanceof SDFReceiver));
		            if(_debugging) _debug("Channel " + channel + 
					  " does not have enough tokens." +
					  " Prefire returns false on " + 
					  container.getFullName());
		            prefireReturnValue = false;
		            debug.prompt("prefire threshold "+prefireReturnValue+" "+threshold);
                    return prefireReturnValue;
		        } else {
		            System.out.println(receivers[channel][0].hasToken(threshold));
		        }
	        }
	    }
	    if(_debugging) _debug("Prefire returns true on " + container.getFullName());
	    prefireReturnValue = true;
        //debug.prompt("prefire "+prefireReturnValue);
        return prefireReturnValue;
    }
    
    
    /** Set current time to zero, invoke the preinitialize() methods of
     *  all actors deeply contained by the container. 
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration; i.e. every time the GO button is pressed.
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException if the preinitialize() method 
     *   of the container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _currentTime = 0.0;
        
        // FIXME:  creating a new ArrayList() may not be the way to go
        //         for hierarchical topologies.  Maybe this should be 
        //         moved to wrapup()
        // FIXME:  receiverTable reset is currently put here because
        //         Vergil calls newReceiver when you connect actors
        receiverTable = new ArrayList();
        super.preinitialize();
    }
    
    
    

    
    /** Initialize the actors associated with this director.  Create a cached
     *  table of all the actors associated with this director.  Determine which
     *  actors need to generate initial tokens for DT causality. All actors with
     *  nonhomogeneous input ports will need to generate initial tokens. For example,
     *  if actor A has a nonhomogeneous input port and an output port with production
     *  rate 'm' then actor A needs to produce 'm' initial tokens.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {

        requestRefireAt(0.0);
    	debug.println("--> method:DTDirector initialize "+getName());
    	
        actorTable = new ArrayList();

        allActorsTable = new Hashtable();
        buildActorTable();
        debug.println("---  Z INITIAL TOKENS ---");
        if (getContainer().getContainer() == null) {
            super.initialize();
        }
        debug.println("-------------------------\n\n");
        
        // This portion figures out which actors should generate initial tokens
        ListIterator receiverIterator = receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            
            IOPort currentPort = currentReceiver.getContainer();
            int rate = 0;
            Actor actor = (Actor) currentPort.getContainer(); 
            String name = ((Nameable)actor).getFullName();

            DTActor dtActor = (DTActor) allActorsTable.get(actor);
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
            
            debug.println(" ~~~"+name+" potentially produce initial_token? "+dtActor.shouldGenerateInitialTokens);
    	}
        displayActorTable();
        displayArcReceiverTable();
        
        debug.println("--- NONHOMOGENEOUS INITIAL TOKENS ---");
        
        
        // This portion generates the initial tokens for actors with nonhomogeneous outputs
        receiverIterator = receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            TypedIOPort currentPort = (TypedIOPort) currentReceiver.getContainer();
            Actor toActor = (Actor) currentPort.getContainer();
            Actor fromActor = currentReceiver.from;
            TypedIOPort fromPort = (TypedIOPort) currentReceiver.fromPort;
            int outrate = currentReceiver.outrate;
            SDFScheduler currentScheduler = (SDFScheduler) getScheduler();
            
            String name = ((Nameable)fromActor).getFullName();

            DTActor dtFromActor = (DTActor) allActorsTable.get(fromActor);
            
            if (dtFromActor != null) {
                if (dtFromActor.shouldGenerateInitialTokens) {
                    int numberInitialTokens = currentScheduler.getTokenInitProduction(currentPort);
                    debug.println(currentPort.getType());
                    debug.prompt(dtFromActor.actor+" # of init tokens "+ numberInitialTokens);
                    for(int j=0;j<outrate;j++) {
                        // FIXME:  should check what token basetype 
                        // for the port and generate such.
                        currentReceiver.put(new DoubleToken(0.0));
                    }
                }
            }
        }
        debug.println("--------------END---------------------\n");
    	debug.println("---->DTDirector end: initialize");
    }
    

  
    /** Set the current time of the model under this director.
     *  It is okay to set the time back to past for DT.
     *
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) {
        _currentTime = newTime;
    }
    
    
    /** 
     *
     *  @exception IllegalActionException
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        receiverTable = new ArrayList();
    }




    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     */
    protected void requestRefireAt(double time) throws IllegalActionException {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        
        if (outsideDirector != null) {
            outsideDirector.fireAt(container,time);   
        }
    }

   

    /** Initialize the object.   In this case, we give the SDFDirector a
     *  default scheduler of the class SDFScheduler.
     */
    
    /** Create an actor table that caches all the actors directed by this
     *  director.  This method is called once at initialize();
     *  @exception IllegalActionException If the scheduler is null or if
     *  the methods invoked throw it.
     */
    private void buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler== null) 
            throw new IllegalActionException("Attempted to fire " +
                    "DT system with no scheduler");
        Enumeration allActorsScheduled = currentScheduler.schedule();
        
        
        debug.println("\nthis is the schedule");
        int actorsInSchedule = 0;                    
        while (allActorsScheduled.hasMoreElements()) {
            Actor actor = (Actor) allActorsScheduled.nextElement();
            String name = ((Nameable)actor).getFullName();
            debug.println("~~~~~~~~~~~~~~~~~~~~~~~~~"+name);
            DTActor dtActor = (DTActor) allActorsTable.get(actor);
            if (dtActor==null) {
              allActorsTable.put(actor, new DTActor(actor));
              dtActor = (DTActor) allActorsTable.get(actor);
              actorTable.add(dtActor);
            }
            dtActor.repeats++;
            actorsInSchedule++;
        }
        
        String name = getContainer().getFullName();
        Actor actor = (Actor) getContainer();
        debug.println("Composite Container ~~~~~~~"+name);
        debug.println("\n");
        allActorsTable.put(actor, new DTActor((Actor)getContainer()));
        DTActor  dtActor = (DTActor) allActorsTable.get(actor);
        dtActor.repeats = 1;
        actorTable.add(dtActor);
        
        displayActorTable();
        ListIterator receiverIterator = receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            currentReceiver.resyncLocalTime(getCurrentTime());
            currentReceiver.determineEnds(this);
        }
        
        receiverIterator = receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
    	   DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
    	   currentReceiver.calculateArcSamplingRate();
        }
        
        
        debug.println("DTDirector schedule has "+actorsInSchedule+" entities");
        displayActorTable();
        displayArcReceiverTable();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    
    protected final int getRepeats(Actor a) {
        ListIterator actorIterator = actorTable.listIterator();
        
        while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            if (a.equals(currentActor.actor)) {
                return currentActor.repeats;
            }
        }
        
        // fixme: this should be a throw an exception location, but for now I will
        // use a pop-up window for debugging
        debug.prompt("warning should not reach this in getRepeats():  actor name is "+a);
    	return 0;
    }
    
    
    
    /** Get the time increment per iteration.
     *  This is a convenience method for getting the period parameter.
     *
     *  @return The value of the period parameter.
     *  @exception IllegalActionException If the period parameter is
     *  is not of type DoubleToken or IntToken.
     */
    public double getPeriod() throws IllegalActionException {
        Token token;
        	
        token = period.getToken();
        double periodValue = 0.0;
        
        if (token instanceof DoubleToken) {
            periodValue = ((DoubleToken) token).doubleValue();
        } else if (token instanceof IntToken) {
            periodValue = (double) ((IntToken) token).intValue();
        } else {
            throw new IllegalActionException(
                      "Illegal period parameter value");
        }
        
        return periodValue;
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** For debugging purposes.  Display the list of contained actors
     *  and other pertinent information about them.
     *
     *  @exception IllegalActionException if there is a problem in 
     *   obtaining the number of initial token for delay actors
     */
    private void displayActorTable() throws IllegalActionException {
         debug.println("\nACTOR TABLE with "+actorTable.size()+" unique actors");
         debug.println("---------------------------------------");
         ListIterator actorIterator = actorTable.listIterator();
         while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            String actorName = ((Nameable) currentActor.actor).getName();
            
            debug.print(actorName+" repeats:"+currentActor.repeats+" initial_tokens? "+currentActor.shouldGenerateInitialTokens);
            
            if (currentActor.actor instanceof Delay) {
                int delayCount = ((MatrixToken)((Delay)currentActor.actor).initialOutputs.getToken()).getColumnCount();
                
                debug.print(" **DELAY** with "+delayCount+" initial tokens");
            }
            
            if ( !((ComponentEntity) currentActor.actor).isAtomic() ) {
                debug.print(" **COMPOSITE** ");
            }
            debug.println(" ");
            displayAttributesList((NamedObj)currentActor.actor);
         }
    }
    
    /** For debugging purposes.  Display the list of contained receivers
     *  and other pertinent information about them.
     */
    private void displayArcReceiverTable() { 
        System.out.println("\nARC RECEIVER table with "+receiverTable.size()+" unique receivers");
        
        ListIterator receiverIterator = receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
        
            String fromString;
            String toString;
          
            //displayAttributesList(currentReceiver.getContainer());
            if (currentReceiver.from==null) {
                  fromString="0";
            } else {
                  fromString=((Nameable)currentReceiver.from).getName();
            }
          
            if (currentReceiver.to==null) {
                  toString="0";
            } else {
                  toString=((Nameable)currentReceiver.to).getName();
            }
          
            System.out.println(fromString+" "+toString+" "+currentReceiver._deltaT);
          //System.out.println("extra "+receiverTable[i].fromPort+" "+receiverTable[i].toPort);
        }
        System.out.println("\n");
    }
    
    /** 
     */
    private int getTokenConsumptionRate(IOPort ioport) throws IllegalActionException {
        int rate;
        Parameter param = (Parameter) ioport.getAttribute("tokenConsumptionRate");
    	if (param != null) {
            rate = ((IntToken)param.getToken()).intValue();
        } else rate = 0;
        
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
            actorTable = new ArrayList();
            receiverTable = new ArrayList();
            iterations.setToken(new IntToken(30000));
            debug = new DTDebug(false);
    	} catch (Exception e) {
    	    throw new InternalErrorException(
                    "unable to initialize DT Director:\n" +
                    e.getMessage());
    	}
    }
    
    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     */
    private void displayEntityList(CompositeEntity obj) {
    
        List list = obj.entityList();
    	Iterator listIterator = list.iterator();
    	
    	debug.println("\nentity List:");
    	while(listIterator.hasNext()) {
    	    Entity entity = (Entity) listIterator.next();
    	    debug.println(entity);
    	}
    	debug.println("\n");
    }

    /** For debugging purposes.  Display the list of attributes
     *  inside a given named object
     */
    private void displayAttributesList(NamedObj obj)
    {
    	List list = obj.attributeList();
    	Iterator listIterator = list.iterator();
    	
    	debug.println("attribute List:");
    	while(listIterator.hasNext()) {
    	    Attribute attribute = (Attribute) listIterator.next();
    	    debug.println(attribute);
    	}
    }
    
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // ArrayList to keep track of all actors scheduled by DTDirector
    //   also used to keep track of the # of repetitions of each actor in the schedule
    //   also used to keep track of which actor should generate initial tokens
    private ArrayList actorTable;
    
    // ArrayList used to cache all receivers managed by DTDirector
    private ArrayList receiverTable;
    private Hashtable allActorsTable;
    private double _lastPrefireTime;
    private Director _executiveDirector;
    private DTDebug debug;
    
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    // Inner class to cache important variables for contained actors 
    private class DTActor {
    	private Actor    actor;
    	private int      repeats;
        private boolean  shouldGenerateInitialTokens;
    	
    	/* Construct the information on the contained Actor
    	 */	
    	public DTActor(Actor a) {
    		actor = a;
    		repeats = 0;
            shouldGenerateInitialTokens = false;
    	}
    }
    
}
