/* Continuous time actor.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
CTActor is the base class for continuous time actors.
<P>
CTActors can have parameters attached. The parameters can be set by the
setParam() method at any time, but the parameter will not be used
immediately until it is updated by the updateParams() method. When a
parameter is changed, the <code>paramChanged</code> flag is set.
The parameter will be updated in the updateParams() method, if the
<code>paramChanged</code> flag is set.  In this base class,
 the updateParams() happens at the prefire()
stage of the iteration, so that the parameters are keep consistent
during the iteration. 
@author Jie Liu
@version $Id$
@see ptolemy.actor.AtomicActor
*/
public class CTActor extends TypedAtomicActor implements ParameterListener{
    /** Construct a CTActor in the default workspace with an empty string
     *  as its name.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     */
    public CTActor() {
	super();
    }

    /** Construct a CTActor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *
     *  @param workspace The workspace that will list the entity.
     */
    public CTActor(Workspace workspace) {
	super(workspace);
    }

    /** Construct a CTActor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param CTSubSystem The subsystem that this actor is lived in
     *  @param name The actor's name
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException Name coincides with
     *   an entity already in the container.
     */
    public CTActor(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the parameter has changed from the last time it
     *  was updated.
     *
     *  @return true if the parameter has changed.
     */
    public boolean isParamChanged() {
        return _paramChanged;
    }

    /** Set the <code>paramChanged</code> flag to true. The parameter will
     *  be updated when the next time the updateParameters() is called.
     */
    public void parameterChanged(ParameterEvent e) {
        _paramChanged = true;
    }

    /** Responds the parameter removed event. Do nothing in this base class.
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

    /** Set the <code>paramChanged</code> flag if the argument
     *  is true, otherwise reset the flag to false.
     *
     *  @param A boolean illustrate if a parameter has been changed.
     */
    public void resetParamChanged() {
        _paramChanged = false;
    }

    /** Update the parameter values.
     *  @exception IllegalActionException If the parameter is not
     *      in the actor, or the new value is an illegal value of
     *      the parameter.
     */
    public void updateParameters() throws IllegalActionException{}

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Illustrate if the parameter has been changed.
    // default value is TRUE.
    private boolean _paramChanged = true;
}
