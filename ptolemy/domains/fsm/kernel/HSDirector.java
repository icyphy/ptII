/* An HSDirector governs the execution of the discrete dynamics of a
   hybrid system model.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.kernel;

import ptolemy.domains.ct.kernel.CTTransparentDirector;

import ptolemy.domains.ct.kernel.CTDirector;
import ptolemy.domains.ct.kernel.CTStepSizeControlActor;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Director;
import ptolemy.actor.CompositeActor;

import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// HSDirector
/**
An HSDirector governs the execution of the discrete dynamics of a hybrid
system model.

@author Xiaojun Liu
@version $Id$
*/
public class HSDirector extends FSMDirector implements CTTransparentDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HSDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this director.
     */
    public HSDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of this director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container.
     */
    public HSDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the current integration step is successful.
     *  @return True if the current step is successful.
     */
    public boolean isThisStepSuccessful() {
        Actor ref = null;
        try {
            ref = getController().currentState().getRefinement();
        } catch (IllegalActionException ex) {
            ref = null;
        }

        boolean result = true;
        if (ref instanceof CTStepSizeControlActor) {
            result = ((CTStepSizeControlActor)ref).isThisStepSuccessful();
        }
        return result;
    }

    /** Return the next iteration time obtained from the executive director.
     *  @return The next iteration time.
     */
    public double getNextIterationTime() {
        CompositeActor cont = (CompositeActor)getContainer();
        Director execDir = (Director)cont.getExecutiveDirector();
        return execDir.getNextIterationTime();
    }

    /** Return the predicted next step size if this step is successful.
     *  @return The predicted next step size.
     */
    public double predictedStepSize() {
        Actor ref = null;
        try {
            ref = getController().currentState().getRefinement();
        } catch (IllegalActionException ex) {
            ref = null;
        }

        double result = Double.MAX_VALUE;
        if (ref instanceof CTStepSizeControlActor) {
            result = ((CTStepSizeControlActor)ref).predictedStepSize();
        }
        return result;
    }

    /** Return true if the mode controller wishes to be scheduled for
     *  another iteration. Postfire the refinement of the current state
     *  of the mode controller if it is ready to fire in the current
     *  iteration. Execute the commit actions contained by the last
     *  chosen transition of the mode controller and set its current
     *  state to the destination state of the transition.
     *  @return True if the mode controller wishes to be scheduled for
     *   another iteration.
     *  @exception IllegalActionException If thrown by any commit action
     *   or there is no controller.
     */
    public boolean postfire() throws IllegalActionException {
        FSMActor ctrl = getController();
        Actor ref = ctrl.currentState().getRefinement();
        if (ref != null) {
            ref.postfire();
            // take out event outputs generated in ref.postfire()
            Iterator outports = ref.outputPortList().iterator();
            while (outports.hasNext()) {
                IOPort p = (IOPort)outports.next();
                transferOutputs(p);
            }
            ctrl._setInputsFromRefinement();
        }
        State st = ctrl.currentState();
        Transition tr =
            ctrl._chooseTransition(st.outgoingPort.linkedRelationList());
        if (_debugging && tr != null) {
            _debug(tr.getFullName(), "is chosen.");
        }
        CompositeActor hs = (CompositeActor)getContainer();
        CTDirector dir = (CTDirector)hs.getExecutiveDirector();
        if (tr != null) {
            // update current time so that the destination refinement can
            // schedule its firing at the correct time.
            setCurrentTime(dir.getNextIterationTime());
        }
        return super.postfire();
    }

    /** Return the refined step size if this step is not successful.
     *  @return The refined step size.
     */
    public double refinedStepSize() {
        Actor ref = null;
        try {
            ref = getController().currentState().getRefinement();
        } catch (IllegalActionException ex) {
            ref = null;
        }

        double result = 0.0;
        if (ref instanceof CTStepSizeControlActor) {
            result = ((CTStepSizeControlActor)ref).refinedStepSize();
        } else {
            CTDirector dir =
                (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
            result = dir.getCurrentStepSize();
        }
        return result;
    }

}
