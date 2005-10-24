/* A composite actor designed for the CT domain.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
 A composite actor designed for the CT domain. This class
 extends TypedCompositeActor and implements the following interfaces:
 CTDynamicActor, CTEventGenerator, CTStatefulActor, CTStepSizeControlActor,
 and CTWaveformGenerator.
 <p>
 In the CT domain, normal opaque composite actors are not fired
 in every iteration. They are only fired in the discrete phase of execution
 and when they have trigger events. On the other hand, CTCompositeActors
 are fired in both discrete and continuous phases of execution in each
 iteration.
 <p>
 The key task of this actor is to implement step-size control methods.
 If the local director of this actor is an instance of CTTransparentDirector,
 then any step-size control methods called on this actor will be delegated
 to the local director. If the local director is not a CTTransparentDirector,
 the implementations of the step-size control methods do not affect
 the current step size.
 <P>
 This composite actor should be used when a CT subsystem needs to transfer
 its step size control information to the outer domain. Typical usage
 includes CT inside CT or CT inside FSM inside CT.

 @author  Jie Liu, Haiyang Zheng
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (hyzheng)
 @Pt.AcceptedRating Green (hyzheng)
 @see ptolemy.domains.ct.kernel.CTDynamicActor
 @see ptolemy.domains.ct.kernel.CTEventGenerator
 @see ptolemy.domains.ct.kernel.CTStatefulActor
 @see ptolemy.domains.ct.kernel.CTStepSizeControlActor
 @see ptolemy.domains.ct.kernel.CTWaveformGenerator
 @see CTTransparentDirector
 */
public class CTCompositeActor extends TypedCompositeActor implements
        CTDynamicActor, CTEventGenerator, CTStatefulActor,
        CTStepSizeControlActor, CTWaveformGenerator {
    /** Construct a CTCompositeActor in the default workspace with
     *  no container and an empty string as its name. Add the actor
     *  to the workspace directory.
     */
    public CTCompositeActor() {
        super();

        // When exporting MoML, set the class name to CTCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    /** Create an CTCompositeActor with a name and a container.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown. This actor will use the workspace of the container
     *  for synchronization and version counts. If the name argument is null,
     *  then the name is set to the empty string. Increment the version of the
     *  workspace. This actor will have no local director initially, and its
     *  executive director will be simply the director of the container.
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

    /** Construct a CTCompositeActor in the specified workspace with no
     *  container and an empty string as a name. If the workspace argument is
     *  null, then use the default workspace. Increment the version number of
     *  the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public CTCompositeActor(Workspace workspace) {
        super(workspace);

        // When exporting MoML, set the class name to CTCompositeActor
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTCompositeActor");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the emitCurrentStates() method of the local director if the
     *  local director is an instance of CTTransparentDirector. Otherwise,
     *  do nothing.
     *  @exception IllegalActionException If tentative states cannot be emitted.
     */
    public void emitCurrentStates() throws IllegalActionException {
        Director director = getDirector();

        // We do not need to check whether director is null, because
        // the preinitialize() method of the CompositeActor ensures this
        // actor to have an director.
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).emitCurrentStates();
        }
    }

    /** Call the goToMarkedState() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise, do nothing.
     *  @exception IllegalActionException If there is no marked state.
     */
    public void goToMarkedState() throws IllegalActionException {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).goToMarkedState();
        }
    }

    /** Call the hasCurrentEvent() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise,
     *  return false, indicating that this composite actor does not
     *  have an event at the current time.
     *  @return True if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).hasCurrentEvent();
        }

        return false;
    }

    /** Call the isOutputAccurate() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise, return
     *  true, which indicates the current step size is accurate w.r.t. outputs.
     *  @return True if the current step size is accurate w.r.t. outputs.
     */
    public boolean isOutputAccurate() {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).isOutputAccurate();
        }

        return true;
    }

    /** Call the isStateAccurate() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise, return
     *  true, which indicates the current step size is accurate w.r.t. the
     *  current states.
     *  @return True if the current step size is accurate w.r.t. the current
     *  states.
     */
    public boolean isStateAccurate() {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).isStateAccurate();
        }

        return true;
    }

    /** Call the markState() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise, do nothing.
     */
    public void markState() {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).markState();
        }
    }

    /** Call the predictedStepSize() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise, return
     *  java.lang.Double.MAX_VALUE.
     *  @return The predicted step size.
     */
    public double predictedStepSize() {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).predictedStepSize();
        }

        return java.lang.Double.MAX_VALUE;
    }

    /** Call the prefireDynamicActors() method of the local director if the
     *  local director is an instance of CTTransparentDirector. Return true
     *  if all dynamic actors are prefired, otherwise, return false. If the
     *  local director is not an instance of CTTransparentDirector, return true
     *  always.
     *  @return True if all dynamic actors are prefired.
     *  @exception IllegalActionException If the local director throws it.
     */
    public boolean prefireDynamicActors() throws IllegalActionException {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).prefireDynamicActors();
        }

        return true;
    }

    /** Call the refinedStepSize() method of the local director if the local
     *  director is an instance of CTTransparentDirector. Otherwise, return
     *  the current step size of the executive director.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        Director director = getDirector();

        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).refinedStepSize();
        }

        return ((CTGeneralDirector) getExecutiveDirector())
                .getCurrentStepSize();
    }
}
