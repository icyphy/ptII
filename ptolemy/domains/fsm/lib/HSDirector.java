/* A HSDirector governs the execution of the discrete dynamics of a hybrid system model.

 Copyright (c) The Regents of the University of California.
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

package ptolemy.domains.fsm.lib;

import ptolemy.graph.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.event.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.fsm.kernel.FSMDirector;
import ptolemy.domains.ct.kernel.*;

import collections.LinkedList;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// FSMDirector
/**
An FSMDirector governs the execution of a *charts model.

@author Xiaojun Liu
@version: $Id$
*/
public class HSDirector extends FSMDirector implements CTEmbeddedDirector {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public HSDirector() {
        super();
    }

    /** Construct a director in the default workspace with the given name.
     *  If the name argument is null, then the name is set to the empty
     *  string. The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param name Name of this object.
     */
    public HSDirector(String name) {
        super(name);
    }

    /** Construct a director in the given workspace with the given name.
     *  If the workspace argument is null, use the default workspace.
     *  The director is added to the list of objects in the workspace.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     */
    public HSDirector(Workspace workspace, String name) {
        super(workspace, name);
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
            CTDirector dir = (CTDirector)(((Actor)getContainer()).getExecutiveDirector());
            return dir.getCurrentStepSize();
        }
    }

    private boolean _first = true;

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
            Enumeration outports = refine.outputPorts();
            while(outports.hasMoreElements()) {
                IOPort p = (IOPort)outports.nextElement();
                transferOutputs(p);
            }
        }
        return _controller.postfire();
    }
}


