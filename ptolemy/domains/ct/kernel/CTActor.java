/* Continuous time actor.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.kernel;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.data.expr.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// CTActor
/** 
CTActor is the base class for continuous time actors. A CT actor can be 
either dynamic or not, which is set to be blank final.
A dynamic actor, e.g. an integrator, will have an
initial state and will emit this state at the initialize() phase of the
execution. As a result, dynamic actors are scheduled at the end of each
iteration. 
<P>
CTActors can have parameters attached. The parameters can be set by the 
setParam() method at any time, but the parameter will not be used 
immediately until it is updated by the updateParams() method. When a
parameter is changed, the <code>paramChanged</code> flag is set. 
The parameter will be updated in the updateParams() method, if the 
<code>paramChanged</code> flag is set.  In this base class
implementation, the updateParams() happens at the prefire()
stage of the iteration, so that the parameters are keep consistent
during the iteration. Parameters can be get by the getUpdatedParam()
method. 
@author Jie Liu
@version $Id$
@see ptolemy.actor.AtomicActor
*/
public class CTActor extends AtomicActor implements ParameterListener{
    /** Construct a CTActor in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  A CTActor can be either dynamic, or not. It must be set at the 
     *  construction time and can't be changed there after.
     *  A dynamic actor will emit a token at its initialization phase.
     *
     *  @param isDynamic true if the actor is a dynamic actor.
     */
    public CTActor(boolean isDynamic) {
	super();
        _dynamic = isDynamic;
    }

    /** Construct a CTActor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  A CTActor can be either dynamic, or not. It must be set at the 
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     *
     *  @param workspace The workspace that will list the entity.
     *  @param isDynamic true if the actor is a dynamic actor.
     */
    public CTActor(Workspace workspace, boolean isDynamic) {
	super(workspace);
        _dynamic = isDynamic;
    }

    /** Construct a CTActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  A CTActor can be either dynamic, or not.  It must be set at the 
     *  construction time and can't be changed thereafter.
     *  A dynamic actor will produce a token at its initialization phase.
     * 
     *  @param CTSubSystem The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @param isDynamic True if the actor is a dynamic actor
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */	
    public CTActor(CompositeActor container, String name, boolean isDynamic) 
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _dynamic = isDynamic;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the actor is a dynamic actor.
     *  Dynamic is a blank final variable, so can not be set after 
     *  construction.
     *
     *  @return True if the actor is a dynamic actor.
     */	
    public boolean isDynamic() {
        return _dynamic;
    }

    /** Return true if the paramter has changed from the last time it
     *  was updated.
     *
     *  @return true if the parameter has changed.
     */
    public boolean isParamChanged() {
        return _paramChanged;
    }
     
    /** Set the <code>paramChanged</code> flag to true. The paramter will
     *  be updated when the next time the updateParameters() is called.
     */
    public void parameterChanged(ParameterEvent e) {
        _paramChanged = true;
    }

    /** Responds the paramter removed event. Do nothing in this base class.
     */
    public void parameterRemoved(ParameterEvent e) {
    }

    /** The default implementation of the prefire() in an iteration.
     *  In this base class, update the parameters if they are changed.
     *
     * @return True always.
     * @exception IllegalActionException Not throw in this class. May be
     *                         needed by derived classes.
     */
    public boolean prefire() throws IllegalActionException {
        if(_paramChanged) {
            updateParameters();
            //System.out.println(getName());
            resetParamChanged();
        }
        return true;
    }
    
    /** FIXME: For the reason of domain polymorphic, consider not doing 
     *  this
     *  Override setContainer() to insure the container is a CTSubSystem 
     *  or null (for removing a CTActor).
     *
     *  @param a CTSubSystem that contains this actor.
     *  @exception IllegalActionException If the container is not an
     *             instance of CTSubSystem or null.
     *  @exception NameDuplicationException If the container has
     *             already contained a object with the name of this 
     *             actor.
     *
    public void setContainer(CompositeEntity container) 
            throws IllegalActionException, NameDuplicationException {
        if(!(container instanceof CTSubSystem) && (container != null)) {
            throw new IllegalActionException(container, 
                    " is not a CTSubSystem.");
        }
        super.setContainer(container);
    }*/  
    
    /** Set the <code>paramChanged</code> flag if the argument
     *  is true, otherwise reset the flag to false.
     *
     *  @param A boolean illustrate if a parameter has been changed.
     */
    public void resetParamChanged() {
        _paramChanged = false;
    }
    

    /** Update the parameter values.
     */
    public void updateParameters() {}

    /** Default implementation of the wrapup() method in a execution.
     *  This is called at the end of every execution of the actor.
     *  If the actor has input ports, consume all the tokens left.
     *  Set <code>paramChanged</code> to true. This method will return
     *  regardless the occurrence of any ptolemy exceptions.
     *  
     * @exception IllegalActionException Not throw in this class.
     *   May be needed by derived classes.
     */
    public void wrapup() throws IllegalActionException {
        try{
            Enumeration inputs = inputPorts();
            while(inputs.hasMoreElements()) {
                IOPort in = (IOPort) inputs.nextElement();
                //cleanup the receivers.
                in.get(0);
                // set the paramChanged flag, so that next time the
                // all the parameter will be updated.
                parameterChanged(null);
            }
        } catch(NoTokenException e) {
            //ignore.
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
  
    // Illustrate if the CTActor is dynamic. 
    // 'final' means this property can not be changed at run time.
    private final boolean _dynamic;

    // Illustrate if the parameter has been changed.
    // default value is TRUE.
    private boolean _paramChanged = true;
}
