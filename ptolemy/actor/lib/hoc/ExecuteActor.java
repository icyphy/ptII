/* A composite actor that executes a submodel in fire().

 Copyright (c) 2003-2013 The Regents of the University of California.
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
package ptolemy.actor.lib.hoc;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.ActorToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.lib.EventUtils;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// ExecuteActor

/**
 This is a composite actor that can execute the contained model
 completely, as if it were a top-level model, on each firing.
 This can be used to define an actor whose firing behavior
 is given by a complete execution of a submodel.
 <p>
 An instance of this actor can have ports added to it.  If it has
 input ports, then on each firing, before executing the referenced
 model, this actor will read an input token from the input port, if
 there is one, and use it to set the value of a top-level parameter
 in the contained model that has the same name as the port, if there
 is one.  The simplest way to ensure that there is a matching parameter
 is to use a PortParameter for inputs.  However, this actor will work
 also for ordinary ports. In this case, if this actor has a
 parameter with the same name as the port, and it is an instance
 of Variable (or its derived class Parameter), then the token
 read at the input is moved into it using its setToken() method.
 Otherwise, if it is an instance of Settable, then a string representation
 of the token is copied using the setExpression() method.
 Input ports should not be multiports, and if they are, then
 all but the first channel will be ignored.
 <p>
 If this actor has output ports and the contained model is executed,
 then upon completion of that execution, if this actor has parameters
 whose names match those of the output ports, then the final value of
 those parameters is sent to the output ports. If such a parameter is an
 instance of Variable (or its derived class Parameter), then its
 contained token is sent to the output token. Otherwise, if it is an
 instance of Settable, then a string token is produced on the output
 with its value equal to that returned by getExpression() of the
 Settable. Output ports should not be multiports. If they are,
 then all but the first channel will be ignored.
 A typical use of this actor will use the SetVariable actor
 inside to define the value of the output port.
 <p>
 In preinitialize(), type constraints are set up so that input
 and output ports with (name) matching parameters are constrained
 to have compatible types. Note that if the ports or parameters
 are changed during execution, then it will be necessary to set
 up matching type constraints by hand.  Since this isn't possible
 to do from Vergil, the ports and parameters of this actor
 should not be changed using Vergil during execution.
 <p>
 This actor also overrides the requestChange() method and the
 executeChangeRequests() method to execute the given change. It does not
 delegate the change request to the container, but executes the request
 immediately or records it, depending on whether setDeferringChangeRequests()
 has been called with a true argument.

 @author Edward A. Lee
 @version $Id: RunCompositeActor.java 67792 2013-10-26 19:36:54Z cxh $
 @since Ptolemy II 10.0
 @see ModelReference
 @see ptolemy.actor.lib.SetVariable
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ExecuteActor extends RunCompositeActor {

    /** Construct a RunCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ExecuteActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        actor = new TypedIOPort(this, "actor", true, false);
        actor.setTypeEquals(BaseType.ACTOR);
        StringAttribute cardinal = new StringAttribute(actor, "_cardinal");
        cardinal.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Input port on which to receive an actor. This has type actor.
     */
    public TypedIOPort actor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make this actor opaque. */
    @Override
    public boolean isOpaque() {
    	return true;
    }
    
    /** Override the base class to not set type constraints between the
     *  output ports and parameters of the actor.
     *  This actor cannot manage type inference across independent models,
     *  so the types have to be set by hand.
     *  @exception IllegalActionException If there is no director, or if
     *   the director's preinitialize() method throws it, or if this actor
     *   is not opaque.
     */
    public void preinitialize() throws IllegalActionException {
    	// Do not call super.preinitialize().
        _stopRequested = false;
        
        Effigy parentEffigy = EventUtils.findToplevelEffigy(this);
        if (parentEffigy != null) {
        	try {
        		parentEffigy.workspace().getWriteAccess();
        		_wrapperEffigy = new PtolemyEffigy(parentEffigy,
        				parentEffigy.uniqueName("_wrapperEffigy"));
        	} catch (NameDuplicationException e) {
        		throw new IllegalActionException(this, e, "Unable to create an "
        				+ "effigy for the model.");
        	} finally {
        		parentEffigy.workspace().doneWriting();
        	}
        }

        if (_debugging) {
            _debug("Called preinitialize()");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Run a complete execution of the model provided on the actor port.
     *  @exception IllegalActionException If the execution throws it.
     *  @return One of COMPLETED or STOP_ITERATING.
     */
    protected int _executeInsideModel() throws IllegalActionException {
        if (actor.hasToken(0)) {
        	Entity entity = ((ActorToken)actor.get(0)).getEntity();
        	if (!(entity instanceof TypedCompositeActor)) {
        		throw new IllegalActionException(this, "actor input has to specify a TypedCompositeActor.");
        	}
        	_model = (TypedCompositeActor)entity;
        	
            _wrapperEffigy.setModel(_model);

            Manager manager = new Manager("_manager");
            _model.setManager(manager);
            
            _readInputs();
            try {
				manager.execute();
			} catch (KernelException e) {
				throw new IllegalActionException(this, e, "Execution failed.");
			}
            _writeOutputs();

            if (_debugging) {
                _debug("---- Firing is complete.");
            }
        }
        return COMPLETED;
    }
    
    /** Return the actor whose life cycle is being managed by this actor,
     *  which is the most recently received actor on the actor input port.
     *  @return This.
     */
    protected TypedCompositeActor _getManagedActor() {
    	return _model;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The most recently received actor on the actor input port. */
    private TypedCompositeActor _model;
    
    /** The effigy to execute models at run time. */
    private PtolemyEffigy _wrapperEffigy;
}
