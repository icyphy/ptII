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

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;



//////////////////////////////////////////////////////////////////////////
//// DDFDirector
/**
Based on DDFSimpleSched in Ptolemy Classic, by Edward Lee   
   
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
     *
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
    
    public Parameter iterations;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    public void fire() throws IllegalActionException {
    	
    	_firedOne = false;
    	
    	int minimaxSize = Integer.MAX_VALUE;
    	Actor minimaxActor = null;
    	
    	boolean repeatBasicIteration = true;
    	while(repeatBasicIteration == true) {
    		
    		repeatBasicIteration = false;
    		
    	    TypedCompositeActor container = ((TypedCompositeActor)getContainer());
            Iterator actors = container.deepEntityList().iterator();       
            while (actors.hasNext()) {
        	    Actor actor = (Actor)actors.next();
        	    Variable enablingStatus = 
        		    (Variable)((NamedObj)actor).getAttribute("enablingStatus");
                int canFire = ((IntToken)enablingStatus.getToken()).intValue();
                if (canFire == _ENABLED_NOT_DEFERRABLE) {
            	    if (fire(actor) == STOP_ITERATING) return;            	
                } else if (canFire == _ENABLED_DEFERRABLE) {
                	Variable maxNumberOfTokens = 
            		        (Variable)((NamedObj)actor).getAttribute("maxNumberOfTokens");
                    int newSize = ((IntToken)maxNumberOfTokens.getToken()).intValue();
                    if (newSize < minimaxSize) {
                	    minimaxSize = newSize;
                	    minimaxActor = actor; 
                    }
                }           
            }
        
            if (!_firedOne && minimaxActor != null) {
        	    if (fire(minimaxActor) == STOP_ITERATING) return;
            }
        
            //deadlock
            if(!_firedOne) {
        	    _postfireReturns = false;
        	    return;
            }     
        
            actors = container.deepEntityList().iterator();       
            while (actors.hasNext()) {
        	    Actor actor = (Actor)actors.next();
        	    Variable firingsPerIteration = 
    		        (Variable)((NamedObj)actor).getAttribute("firingsPerIteration");
        	    if (firingsPerIteration != null) {
        		    int requiredFirings = 
        			        ((IntToken)firingsPerIteration.getToken()).intValue();
        		    Variable numberOfFirings = 
        		        (Variable)((NamedObj)actor).getAttribute("numberOfFirings");
        		    int firingsDone = 
        		    	    ((IntToken)numberOfFirings.getToken()).intValue();
        		    if (firingsDone < requiredFirings) {
        		    	repeatBasicIteration = true;
        		    	break;
        		    }
        	    }
            }
    	}
    }
    
    public int fire(Actor actor) throws IllegalActionException {
    	
    	int returnValue = actor.iterate(1);
        if (returnValue == STOP_ITERATING) {
            _postfireReturns = false;
            return returnValue;
        } else if (returnValue == NOT_READY) {
            throw new IllegalActionException(this,
                    (ComponentEntity) actor, "Actor " +
                    "is not ready to fire.");
        }
        
        _firedOne = true;
        
        Variable numberOfFirings = 
        	    (Variable)((NamedObj)actor).getAttribute("numberOfFirings");
        int firings = ((IntToken)numberOfFirings.getToken()).intValue();
        numberOfFirings.setToken(new IntToken(++firings));
        
        Iterator connectedPorts = ((Entity)actor).connectedPortList().iterator();
        while (connectedPorts.hasNext()) {
        	Port connectedPort = (Port)connectedPorts.next();
        	Actor container = (Actor)connectedPort.getContainer();
        	_checkActorStatus(container);
        }
        _checkActorStatus(actor);
        
        return returnValue;
    }
    
    /** Initialize the actors associated with this director and then
     *  set the iteration count to zero.  The order in which the
     *  actors are initialized is arbitrary. For each actor, dertermine
     *  its enabling status: _NOT_ENABLED, _ENABLED_NOT_DEFERRABLE or
     *  _ENABLED_DEFERRABLE. */
    
    public void initialize() throws IllegalActionException {
    	super.initialize();
        _iterationCount = 0;
        
        TypedCompositeActor container = ((TypedCompositeActor)getContainer());
        Iterator actors = container.deepEntityList().iterator();
        while (actors.hasNext()) {
        	Actor actor = (Actor)actors.next();
        	_checkActorStatus(actor);
        	_resetNumberOfFirings(actor);
        }	    
    }
    
    public boolean postfire() throws IllegalActionException {
    	int iterationsValue = ((IntToken)(iterations.getToken())).intValue();
        _iterationCount++;
        if ((iterationsValue > 0) && (_iterationCount >= iterationsValue)) {
            _iterationCount = 0;
            return false;
        }
        return _postfireReturns && super.postfire();
    }
    
    public boolean prefire() throws IllegalActionException {
        _postfireReturns = true;
        return super.prefire();
    }
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////
    
    protected int _checkActorStatus(Actor actor) 
	        throws IllegalActionException {
    	
        //Some functionalities below can be implemented using
		//SDFUtilities, but we need to move that class to another
		//package so that DDF package won't depend on SDF package.
		//Then some protected methods need to become public.
    	
        //SDFUtilities._setOrCreate((NameObj)actor, 
		//        "enablingStatus", new IntToken(_NOT_ENABLED));
		
    	//Create variable to store enablingStatus if it does not 
    	//already exist.
    	Variable enablingStatus = 
    		    (Variable)((NamedObj)actor).getAttribute("enablingStatus");
		if (enablingStatus == null) {
			try {
			    enablingStatus = new Variable((NamedObj)actor, "enablingStatus");
			    enablingStatus.setVisibility(Settable.NOT_EDITABLE);
                enablingStatus.setPersistent(false);
		    } catch (KernelException ex) {
		    	throw new InternalErrorException((NamedObj)actor, ex,
                        "Should not occur");
		    }
		} 
    	
    	if (!_isEnabled(actor)) {
    		enablingStatus.setToken(new IntToken(_NOT_ENABLED));
    		return _NOT_ENABLED;
    	}	
    	
    	if (_isDeferrable(actor)) {
    		enablingStatus.setToken(new IntToken(_ENABLED_DEFERRABLE));
    		return _ENABLED_DEFERRABLE;
    	}	
    		
    	enablingStatus.setToken(new IntToken(_ENABLED_NOT_DEFERRABLE));
    	return _ENABLED_NOT_DEFERRABLE;
    	   	
    }
    
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
    				IOPort container = farReceiver.getContainer();
                    
    				//int tokenConsumptionRate = SDFUtilities.
    				//        getTokenConsumptionRate(container);
    				   				
                    //Having a self-loop doesn't make itself deferrable.
    	    		if (container == outputPort) continue;
    	    		
                    //The defalult vaule for tokenConsumptionRate is 1.
    				int tokenConsumptionRate = 1;
    				Variable variable = 
    					    (Variable)container.getAttribute("tokenConsumptionRate");
    	    		if (variable != null) {
    	    			Token token = variable.getToken();
    	    			tokenConsumptionRate = ((IntToken)token).intValue();      			
    	    		}
    	    		if (farReceiver.size() >= tokenConsumptionRate) {
    	    			deferrable = true;
    	    		} 		
    	    		if (farReceiver.size() >= maxSize) {
    	    		    maxSize = farReceiver.size();	
    	    		}
    			}   			   		
    	}
    	
        //SDFUtilities._setOrCreate((NameObj)actor, 
		//        "maxNumOfTokens", new IntToken(0));
		
    	//Create variable to store maximum number of tokens on the actor's
    	//output arcs if it does not already exist, used to find minimax 
    	//actor if all enabled actors are deferrable.
    	Variable maxNumberOfTokens = 
    		    (Variable)((NamedObj)actor).getAttribute("maxNumberofTokens");
		if (maxNumberOfTokens == null) {
			try {
				maxNumberOfTokens = new Variable((NamedObj)actor, "maxNumberOfTokens");
				maxNumberOfTokens.setVisibility(Settable.NOT_EDITABLE);
				maxNumberOfTokens.setPersistent(false);
		    } catch (KernelException ex) {
		    	throw new InternalErrorException((NamedObj)actor, ex,
                        "Should not occur");
		    }
		}
		maxNumberOfTokens.setToken(new IntToken(maxSize));
		
		return deferrable;
    }
    
    protected boolean _isEnabled(Actor actor) 
	        throws IllegalActionException {   	
    	Iterator inputPorts = actor.inputPortList().iterator();
    	while (inputPorts.hasNext()) {
    		IOPort inputPort = (IOPort)inputPorts.next();
    				   		
    		//int tokenConsumptionRate = SDFUtilities.
			//        getTokenConsumptionRate(inputPort);
    		
            //The defalult vaule for tokenConsumptionRate is 1.
    		int tokenConsumptionRate = 1;
      		Variable variable = 
      			    (Variable)inputPort.getAttribute("tokenConsumptionRate");
    		if (variable != null) {
    			Token token = variable.getToken();
    			tokenConsumptionRate = ((IntToken)token).intValue();      			
    		}
    		
    		for (int i = 0; i < inputPort.getWidth(); i++) {
    			if (!inputPort.hasToken(i, tokenConsumptionRate)) {   				
    				return false;
    			}
    		}
    	}
    	return true;
    }
    
    protected void _resetNumberOfFirings(Actor actor) 
	        throws IllegalActionException {
        //SDFUtilities._setOrCreate((NameObj)actor, 
		//        "numberOfFirings", new IntToken(0));
		
    	//Create variable to store number of firings during each iteration
    	//of the director if it does not already exist, used to compare with 
    	//required number of firings in each iteration for some actors. 
    	Variable numberOfFirings = 
    		    (Variable)((NamedObj)actor).getAttribute("numberOfFirings");
		if (numberOfFirings == null) {
			try {
				numberOfFirings = new Variable((NamedObj)actor, "numberOfFirings");
				numberOfFirings.setVisibility(Settable.NOT_EDITABLE);
				numberOfFirings.setPersistent(false);
		    } catch (KernelException ex) {
		    	throw new InternalErrorException((NamedObj)actor, ex,
                        "Should not occur");
		    }
		}
		numberOfFirings.setToken(new IntToken(0));
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Initialize the object. In this case, we give the DDFDirector
     *  an iterations parameter.
     */
    private void _init() 
	         throws IllegalActionException, NameDuplicationException  {
    	iterations = new Parameter(this, "iterations", new IntToken(0));
        iterations.setTypeEquals(BaseType.INT);
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    private boolean _firedOne;
    private int _iterationCount = 0;
    private boolean _postfireReturns;
        
    private static final int _ENABLED_DEFERRABLE = 2;
    private static final int _ENABLED_NOT_DEFERRABLE = 1; 
    private static final int _NOT_ENABLED = 0;
}