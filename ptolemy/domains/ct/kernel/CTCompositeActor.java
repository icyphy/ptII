/* Composite Actor in the CT domain.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (chf@eecs.berkeley.edu)

*/

package ptolemy.domains.ct.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CTCompositeActor
/**
Composite actor in the CT domain. This class is derived from
TypedCompositeActor and implements the CTStepSizeControlActor interface.
Normally, in the CT domain, opaque composite actors are not fired
in every iteration. They are only fired in discrete iterations,
when there is a possibility of events. Actors that implement
the CTStepSizeControlActor interface, however, such as this one,
are fired in every iteration.
<p>
The key task of this actor is to implement step-size control methods.
If the director of this composite actor is an instance of
CTTransparentDirector, then the CTStepSizeControlActor calls
will be delegated to its local director. Otherwise, they return
default values.
<P>
This composite actor should be used when a CT subsystem needs to transfer
its step size control information to the outer domain. Typical usage
includes CT inside CT or CT inside FSM inside CT.  If you construct
a modal model, then by default, refinements of the modes are actors
like this one that implement the CTStepSizeControlActor interface.

@author  Jie Liu
@version $Id$
@since Ptolemy II 0.2
@see CTStepSizeControlActor
@see CTTransparentDirector
*/
public class CTCompositeActor extends TypedCompositeActor
    implements CTEventGenerator, CTStepSizeControlActor {

    /** Construct a CTCompositeActor in the default workspace with no container
     *  and an empty string as its name. Add the actor to the workspace
     *  directory.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     */
    public CTCompositeActor() {
        super();
        // When exporting MoML, set the class name to CTCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    /** Construct a CTCompositeActor in the specified workspace with no
     *  container
     *  and an empty string as a name. You can then change the name with
     *  setName(). If the workspace argument is null, then use the default
     *  workspace.
     *  The director should be set before attempting to execute it.
     *  The container should be set before sending data to it.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CTCompositeActor(Workspace workspace) {
        super(workspace);
        // When exporting MoML, set the class name to CTCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    /** Create an CTCompositeActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  The director should be set before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CTCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // When exporting MoML, set the class name to CTCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return false, indicating that this composite actor does not
     *  have an event at the current time.
     *  @return True if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        Director dir = getDirector();
        if ((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).hasCurrentEvent();
        }
        return false;
    }

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return true, indicating that this composite actor does not
     *  perform step size control.
     *  @return True if this step is accurate.
     */
    public boolean isThisStepAccurate() {
        Director dir = getDirector();
        if ((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).isThisStepAccurate();
        }
        return true;
    }

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return java.lang.Double.MAX_VALUE.
     *  @return The predicted step size.
     */
    public double predictedStepSize() {
        Director dir = getDirector();
        if ((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).predictedStepSize();
        }
        return java.lang.Double.MAX_VALUE;
    }

    /** This method is delegated to the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return the current step size of the executive director.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        Director dir = getDirector();
        if ((dir != null) && (dir instanceof CTTransparentDirector)) {
            return ((CTTransparentDirector)dir).refinedStepSize();
        }
        return ((CTDirector)getExecutiveDirector()).getCurrentStepSize();
    }

    /** Create a new IOPort with the specified name. This port is
     *  created with a parameter <i>signalType</i> with default value
     *  <i>CONTINUOUS</i>
     *  The container of the port is set to this actor.
     *
     *  @param name The name for the new port.
     *  @return The new port.
     *  @exception NameDuplicationException If the actor already has a port
     *   with the specified name.

     public Port newPort(String name)
     throws NameDuplicationException {
     Port newPort = super.newPort(name);
     try {
     Parameter type = new Parameter(newPort, "signalType",
     new StringToken("CONTINUOUS"));
     } catch (IllegalActionException ex) {
     // This should never occur.
     throw new InternalErrorException("Fail to add parameter signalType"
     + " to new Port " + newPort.getFullName());
     }
     return newPort;
     }
    */
}
