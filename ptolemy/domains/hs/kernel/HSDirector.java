/* A HSDirector governs the execution of the discrete dynamics of a
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.hs.kernel;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.ct.kernel.*;

import java.util.Iterator;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// FSMDirector
/**
An FSMDirector governs the execution of a *charts model.

@author Xiaojun Liu @version $Id$
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
     *  @param workspace The workspace of this object.
     */
    public HSDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the director is not compatible
     *  with the specified container.  May be thrown in derived classes.
     */
    public HSDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the current integration step is successful.
     *  @return True if the current step is successful
     */
    public boolean isThisStepSuccessful() {
        Actor ref = currentRefinement();

        //System.out.println("HSDirector: isThisStepSuccessful is called.");

        if (ref instanceof CTStepSizeControlActor) {

            //System.out.println("HSDirector: get step status from subsys "
            //+ ((ComponentEntity)ref).getFullName());

            return ((CTStepSizeControlActor)ref).isThisStepSuccessful();
        } else {
            return true;
        }
    }

    /** Return the predicted next step size if this step is successful.
     */
    public double predictedStepSize() {
        Actor ref = currentRefinement();
        if (ref instanceof CTStepSizeControlActor) {
            return ((CTStepSizeControlActor)ref).predictedStepSize();
        } else {
            return Double.MAX_VALUE;
        }
    }

    /** Return the refined step size if this step is not successful.
     */
    public double refinedStepSize() {
        Actor ref = currentRefinement();
        if (ref instanceof CTStepSizeControlActor) {
            return ((CTStepSizeControlActor)ref).refinedStepSize();
        } else {
            // FIXME: this implementation does not allow hierarchical FSM
            // embedded in CT
            CTDirector dir =
                (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
            return dir.getCurrentStepSize();
        }
    }

    public void initialize() throws IllegalActionException {
        _first = true;
        super.initialize();
    }

    public boolean postfire() throws IllegalActionException {
        Actor refine = currentRefinement();
	if (_first) {
            _first = false;
	    if (refine != null) {
                return refine.postfire();
            } else {
                return true;
            }
        }
        // elaborate
        if (refine != null) {
            refine.postfire();
            Iterator outports = refine.outputPortList().iterator();
            while(outports.hasNext()) {
                IOPort p = (IOPort)outports.next();
                transferOutputs(p);
            }
        }
        return _controller.postfire();
    }

    /** @serial True if this is the first time through postfire(). */
    private boolean _first = true;

}
