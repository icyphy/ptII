/* A DT domain director.

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
import ptolemy.data.expr.*;
import ptolemy.domains.sdf.lib.Delay;

import java.util.*;
import javax.swing.*;

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


    public void fire() throws IllegalActionException {
        
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        if (container.getExecutiveDirector() == null) {
            MB("toplevel");
        } else {
            // FIXME: resyncLocalTimes for receivers
        }
        println("\nDTDirector "+this.getName()+" fire **************************************************");
       

	
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
                println("{ "+((Nameable)actor).getName()+" "+((Nameable)actor).getFullName()+" start fire--------------------");
                if(!actor.prefire()) {
                    throw new IllegalActionException(this,
                            (ComponentEntity) actor, "Actor " +
                            "is not ready to fire.");
                }

                if(_debugging)
                    _debug("Firing " + ((Nameable)actor).getFullName());
		
		
                actor.fire();
                
                _postfirereturns = _postfirereturns && actor.postfire();
                println("} "+"------------------- end fire--------------------");
            }
        }
        println("DTDirector "+this.getName()+" end fire ************************************************\n");
        try {
            //if (_debugOn) java.lang.Thread.sleep(1000);
        } catch(Exception e) {}
    }
    

    public boolean postfire() throws IllegalActionException {
        boolean returnValue = super.postfire();
        
        double timeIncrement = (double) ((DoubleToken) period.getToken()).doubleValue();
        requestRefireAt(_lastPrefireTime + timeIncrement);
        
        return returnValue;
    }

    /** Return a new receiver consistent with the DT domain.
     *  @return A new DTReceiver.
     */
    public Receiver newReceiver() {
    
        DTReceiver currentReceiver = new DTReceiver();
        receiverTable.add(currentReceiver);
        return currentReceiver;
    }

    
    public boolean prefire() throws IllegalActionException {
        //boolean prefireReturnValue = super.prefire();

        
        boolean prefireReturnValue = true;
        _postfirereturns = true;

        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Director outsideDirector = container.getExecutiveDirector();
        
        if (outsideDirector != null) {
            double currentTime = outsideDirector.getCurrentTime();
            double currentPeriod = ((DoubleToken) period.getToken()).doubleValue();
            if ((currentTime - _lastPrefireTime) < currentPeriod) {
                MB(" prefire: should not fire");
            }
            _lastPrefireTime = currentTime;
        }
        MB("prefire "+_lastPrefireTime);
        //TypedCompositeActor container = ((TypedCompositeActor)getContainer());
	    Iterator inputPorts = container.inputPortList().iterator();
	    int inputCount = 0;
	    while(inputPorts.hasNext()) {
	        IOPort inputPort = (IOPort) inputPorts.next();
	        System.out.println("IOPort "+inputPort);
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
		            MB("prefire threshold "+prefireReturnValue+" "+threshold);
                    return prefireReturnValue;
		        } else {
		            System.out.println(receivers[channel][0].hasToken(threshold));
		        }
	        }
	    }
	    if(_debugging) _debug("Prefire returns true on " + container.getFullName());
	    prefireReturnValue = true;
        //MB("prefire "+prefireReturnValue);
        return prefireReturnValue;
    }
    
    
    /** Set current time to zero, invoke the preinitialize() methods of
     *  all actors deeply contained by the container. 
     *  <p>
     *  This method should be invoked once per execution, before any
     *  iteration. 
     *  This method is <i>not</i> synchronized on the workspace, so the
     *  caller should be.
     *
     *  @exception IllegalActionException if the preinitialize() method 
     *   of the container or one of the deeply contained actors throws it.
     */
    public void preinitialize() throws IllegalActionException {
        _currentTime = 0.0;
        super.preinitialize();
    }
    
    
    

    
    /** Initialize the actors associated with this director and
     *  initialize the number of iterations to zero.  The order in which
     *  the actors are initialized is arbitrary.
     *  @exception IllegalActionException If the preinitialize() method of
     *  one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {

        requestRefireAt(0.0);
        iterations.setToken(new IntToken(4000));
    	println("--> method:DTDirector initialize "+getName());
    	
        actorTable = new ArrayList();

        allActorsTable = new Hashtable();
        buildActorTable();
        println("---  Z INITIAL TOKENS ---");
        if (getContainer().getContainer() == null) {
            super.initialize();
        }
        println("-------------------------\n\n");
        
        // figuring which actors should generate initial tokens

        ListIterator receiverIterator = receiverTable.listIterator();
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();
            
            IOPort currentPort = currentReceiver.getContainer();
            int rate = 0;
            Actor actor = (Actor) currentPort.getContainer(); 
            String name = ((Nameable)actor).getFullName();

            DTActor dtActor = (DTActor) allActorsTable.get(actor);
            println(dtActor);
            if (dtActor == null) {
                throw new IllegalActionException("DT internal error: unknown actor");
            }
            
            Parameter param = (Parameter) currentPort.getAttribute("tokenConsumptionRate");
    	    if ((param != null)&&(currentPort.isInput())) {
               rate = ((IntToken)param.getToken()).intValue();
               if (rate > 1) dtActor.shouldGenerateInitialTokens = true;
            }
            
            println(" ~~~"+name+" potentially produce initial_token? "+dtActor.shouldGenerateInitialTokens);
    	}
        displayActorTable();
        displayArcReceiverTable();
        
        println("--- NONHOMOGENEOUS INITIAL TOKENS ---");
        // figuring which actors should generate initial tokens
        
        //ListIterator receiverIterator = receiverTable.listIterator();
        receiverIterator = receiverTable.listIterator();
        
        while(receiverIterator.hasNext()) {
            DTReceiver currentReceiver = (DTReceiver) receiverIterator.next();

            IOPort currentPort = currentReceiver.getContainer();
            Actor toActor = (Actor) currentPort.getContainer();
            Actor fromActor = currentReceiver.from;
            IOPort fromPort = currentReceiver.fromPort;
            int outrate = currentReceiver.outrate;
            
            String name = ((Nameable)fromActor).getFullName();

            DTActor dtFromActor = (DTActor) allActorsTable.get(fromActor);
            
            if (dtFromActor != null) {
                if (dtFromActor.shouldGenerateInitialTokens) {
                    for(int j=0;j<outrate;j++) {
                        currentReceiver.put(new DoubleToken(0.0));
                        //receiverTable[i].put(new IntToken(0));
                        
                    }
                }
            }
        }
        println("--------------END---------------------\n");
    	println("---->DTDirector end: initialize");
        
    }
    

  
    /** Set the current time of the model under this director.
     *  It is okay to set the time back to past for DT.
     *
     *  @param newTime The new current simulation time.
     */
    public void setCurrentTime(double newTime) {
        _currentTime = newTime;
    }
    
    
    
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        receiverTable = new ArrayList();
    }




    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return true if this director requires write access
     *  on the workspace during execution. Most director functions
     *  during execution do not need write access on the workspace.
     *  A director will generally only need write access on the workspace if
     *  it performs mutations locally, instead of queueing them with the
     *  manager.
     *  <p>
     *  In this class, return true, indicating that SDF does not perform local
     *  mutations.
     *
     *  @return false
     */
    protected boolean _writeAccessRequired() {
        return false;
    }
    
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
    
    private void buildActorTable() throws IllegalActionException {
        Scheduler currentScheduler = getScheduler();
        if (currentScheduler== null) 
            throw new IllegalActionException("Attempted to fire " +
                    "DT system with no scheduler");
        Enumeration allActorsScheduled = currentScheduler.schedule();
        
        
        println("\nthis is the schedule");
        int actorsInSchedule = 0;                    
        while (allActorsScheduled.hasMoreElements()) {
            Actor actor = (Actor) allActorsScheduled.nextElement();
            String name = ((Nameable)actor).getFullName();
            println("~~~~~~~~~~~~~~~~~~~~~~~~~"+name);
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
        println("Composite Container ~~~~~~~"+name);
        println("\n");
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
        
        
        println("DTDirector schedule has "+actorsInSchedule+" entities");
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
        MB("warning should not reach this in getRepeats"+a);
    	return 0;
    }
    
    public int getTokenConsumptionRate(IOPort ioport) throws IllegalActionException {
        int rate;
        Parameter param = (Parameter) ioport.getAttribute("tokenConsumptionRate");
    	if (param != null) {
            rate = ((IntToken)param.getToken()).intValue();
        } else rate = 0;
        
        return rate;
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
         println("\nACTOR TABLE with "+actorTable.size()+" unique actors");
         println("---------------------------------------");
         ListIterator actorIterator = actorTable.listIterator();
         while(actorIterator.hasNext()) {
            DTActor currentActor = (DTActor) actorIterator.next();
            String actorName = ((Nameable) currentActor.actor).getName();
            
            print(actorName+" repeats:"+currentActor.repeats+" initial_tokens? "+currentActor.shouldGenerateInitialTokens);
            
            if (currentActor.actor instanceof Delay) {
                int delayCount = ((MatrixToken)((Delay)currentActor.actor).initialOutputs.getToken()).getColumnCount();
                
                print(" **DELAY** with "+delayCount+" initial tokens");
            }
            
            if ( !((ComponentEntity) currentActor.actor).isAtomic() ) {
                print(" **COMPOSITE** ");
            }
            println(" ");
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
   
    
   
    
    private void _init() {
    	try {
            period = new Parameter(this,"period",new DoubleToken(1.0));
            actorTable = new ArrayList();
            receiverTable = new ArrayList();
            iterations.setToken(new IntToken(30000));
            double currentPeriod = ((DoubleToken) period.getToken()).doubleValue();
            println("DT Director has period: " + currentPeriod);
    	} catch (Exception e) {
    		System.out.println("bad parameters");
    	}
    }
    
    /** For debugging purposes.  Display the list of contained entities
     *  inside the composite object
     */
    private void displayEntityList(CompositeEntity obj) {
    
        List list = obj.entityList();
    	Iterator listIterator = list.iterator();
    	
    	println("\nentity List:");
    	while(listIterator.hasNext()) {
    	    Entity entity = (Entity) listIterator.next();
    	    println(entity);
    	}
    	println("\n");
    }

    /** For debugging purposes.  Display the list of attributes
     *  inside the object
     */
    private void displayAttributesList(NamedObj obj)
    {
    	List list = obj.attributeList();
    	Iterator listIterator = list.iterator();
    	
    	println("attribute List:");
    	while(listIterator.hasNext()) {
    	    Attribute attribute = (Attribute) listIterator.next();
    	    println(attribute);
    	}
    }
    
    private boolean _debugOn = true;
    private void println(Object obj) {
        if (_debugOn) {
            System.out.println(obj.toString());
        }
    }
    private void print(Object obj) {
        if (_debugOn) {
            System.out.print(obj.toString());
        }
    }
    
    private void MB(String str) {
        if (_debugOn) {
            JOptionPane.showMessageDialog(null,str,"MessageDialog",JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void printStackTrace() {
        try {
		    throw new Exception("printStackTrace()");
		} catch (Exception e) {
		    e.printStackTrace();
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
    //private double _period;
    
    
    
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
