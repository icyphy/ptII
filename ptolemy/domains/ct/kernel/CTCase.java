/* A version of the Case actor designed for the CT domain.

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
import ptolemy.actor.lib.hoc.Case;
import ptolemy.actor.lib.hoc.CaseDirector;
import ptolemy.actor.lib.hoc.Refinement;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CTCase

/**
 A version of the Case actor designed for the CT domain.
 
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (hyzheng)
 */
public class CTCase extends Case implements
        CTDynamicActor, CTEventGenerator, CTStatefulActor,
        CTStepSizeControlActor, CTWaveformGenerator {

    /** Create an CTCase with a name and a container.
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
    public CTCase(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // When exporting MoML, set the class name to CTCase
        // instead of the default TypedCompositeActor.
        setClassName("ptolemy.domains.ct.kernel.CTCase");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Call the emitCurrentStates() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, do nothing.
     *  @exception IllegalActionException If tentative states cannot
     *  be emitted.
     */
    public void emitCurrentStates() throws IllegalActionException {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).emitCurrentStates();
        }
    }
    
    /** Call the goToMarkedState() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, do nothing.
     *  @exception IllegalActionException If there is no marked state.
     */
    public void goToMarkedState() throws IllegalActionException {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).goToMarkedState();
        }
    }

    /** Call the hasCurrentEvent() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, return false, indicating
     *  that this composite actor does not have an event at the
     *  current time.
     *  @return True if there is an event at the current time.
     */
    public boolean hasCurrentEvent() {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).hasCurrentEvent();
        }
        return false;
    }

    /** Call the isOutputAccurate() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, return true, which indicates
     *  the current step size is accurate w.r.t. outputs.
     *  @return True if the current step size is accurate w.r.t. outputs.
     */
    public boolean isOutputAccurate() {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).isOutputAccurate();
        }
        return true;
    }

    /** Call the isStateAccurate() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, return true, which indicates
     *  the current step size is accurate w.r.t. the current states.
     *  @return True if the current step size is accurate w.r.t. the current
     *  states.
     */
    public boolean isStateAccurate() {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).isStateAccurate();
        }
        return true;
    }

    /** Call the markState() method of the current refinement director if it
     *  is an instance of CTTransparentDirector. Otherwise, do nothing.
     */
    public void markState() {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            ((CTTransparentDirector) director).markState();
        }
    }

    /** Create a new refinement with the specified name.
     *  @param name The name of the new refinement.
     *  @return The new refinement.
     *  @exception IllegalActionException If the refinement cannot be created.
     *  @exception NameDuplicationException If a refinement already
     *  exists with this name.
     */
    public Refinement newRefinement(String name)
            throws IllegalActionException, NameDuplicationException {
        return new CTRefinement(this, name);
    }

    /** Call the predictedStepSize() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, return
     *  java.lang.Double.MAX_VALUE.
     *  @return The predicted step size.
     */
    public double predictedStepSize() {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).predictedStepSize();
        }
        return java.lang.Double.MAX_VALUE;
    }

    /** Call the prefireDynamicActors() method of the current
     *  refinement director if it is an instance of
     *  CTTransparentDirector. Return true if all dynamic actors are
     *  prefired, otherwise, return false. If the local director is
     *  not an instance of CTTransparentDirector, return true always.
     *  @return True if all dynamic actors are prefired.
     *  @exception IllegalActionException If the local director throws it.
     */
    public boolean prefireDynamicActors() throws IllegalActionException {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).prefireDynamicActors();
        }
        return true;
    }

    /** Call the refinedStepSize() method of the current refinement
     *  director if it is an instance of
     *  CTTransparentDirector. Otherwise, return the current step size
     *  of the executive director.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        Director director = _current.getDirector();
        if (director instanceof CTTransparentDirector) {
            return ((CTTransparentDirector) director).refinedStepSize();
        }
        return ((CTGeneralDirector) getExecutiveDirector())
                .getCurrentStepSize();
    }

    /** Return the class name for refinements that this Case actor
     *  expects to contain.
     *  @return The string "ptolemy.actor.lib.hoc.Refinement".
     */
    public String refinementClassName() {
        return "ptolemy.domains.ct.kernel.CTRefinement";
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a director. This class creates an instance of CTCaseDirector.
     *  @exception IllegalActionException If the director cannot be created.
     *  @exception NameDuplicationException If there is already an
     *  attribute with the name "_director".
     */
    protected CaseDirector _createDirector()
            throws IllegalActionException, NameDuplicationException {
        return new CTCaseDirector(this, "_director");        
    }
}
