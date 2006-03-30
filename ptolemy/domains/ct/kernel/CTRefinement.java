/* A version of the Case Refinement actor designed for the CT domain.

 Copyright (c) 2006 The Regents of the University of California.
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
import ptolemy.actor.lib.hoc.Refinement;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CTRefinement

/**
 A version of the Case Refinement actor designed for the CT domain.
 
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class CTRefinement extends Refinement implements CTDynamicActor,
        CTEventGenerator, CTStatefulActor, CTStepSizeControlActor,
        CTWaveformGenerator {

    /** Create an CTRefinement with a name and a container.
     *  The container argument must not be null, or a NullPointerException
     *  will be thrown. This actor will use the workspace of the container
     *  for synchronization and version counts. If the name argument is null,
     *  then the name is set to the empty string. Increment the version of the
     *  workspace.
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CTRefinement(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // When exporting MoML, set the class name to CTRefinement
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTRefinement");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the emitCurrentStates() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, do nothing.
     *  @exception IllegalActionException If tentative states cannot be emitted.
     */
    public void emitCurrentStates() throws IllegalActionException {
        Director director = getDirector();
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).emitCurrentStates();
        }
    }

    /** Call the goToMarkedState() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, do nothing.
     *  @exception IllegalActionException If there is no marked state.
     */
    public void goToMarkedState() throws IllegalActionException {
        Director director = getDirector();
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).goToMarkedState();
        }
    }

    /** Call the hasCurrentEvent() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise,
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

    /** Call the isOutputAccurate() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, return
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

    /** Call the isStateAccurate() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, return
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

    /** Call the markState() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, do nothing.
     */
    public void markState() {
        Director director = getDirector();
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).markState();
        }
    }

    /** Call the predictedStepSize() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, return
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

    /** Call the prefireDynamicActors() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Return true
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

    /** Call the refinedStepSize() method of the this refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, return
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
