/* A NewFSMDirector governs the execution of a modal model.

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

import ptolemy.actor.Director;

import ptolemy.actor.TypedActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.IllegalActionException;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// NewFSMDirector
/**
A NewFSMDirector governs the execution of a modal model. A modal model is
a TypedCompositeActor with a NewFSMDirector as local director. The mode
control logic is captured by an FSMActor contained in the TypedCompositeActor.
Each state of the FSMActor represents a mode of operation and can be refined
by a TypedActor contained in the same TypedCompositeActor.
<p>

@author Xiaojun Liu
@version $Id$
@see FSMActor
*/
public class NewFSMDirector extends Director {

    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public NewFSMDirector() {
        super();
    }

    /** Construct a director in the  workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public NewFSMDirector(Workspace workspace) {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  If the container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *
     *  @param workspace Object for synchronization and version tracking
     *  @param name Name of this director.
     *  @exception IllegalActionException If the nane has a period in it, or
     *   the director is not compatible with the specified container.
     */
    public NewFSMDirector(CompositeActor container, String name)
            throws IllegalActionException {
        super(container, name);
        container.setDirector(this);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire() throws IllegalActionException {
        Iterator ctrlInPorts = _controller.inputPortList().iterator();
        while (ctrlInPorts.hasNext()) {
            _controller._setInputVariables((TypedIOPort)ctrlInPorts.next());
        }
        State st = _controller.currentState();
        Transition tr =
            _controller._chooseTransition(st.preemptiveTransitionList());
        if (tr != null) {
            return;
        }
        TypedActor ref = st.getRefinement();
        if (ref.prefire()) {
            ref.fire();
            ref.postfire();
            ctrlInPorts = _internalInputPortList().iterator();
            while (ctrlInPorts.hasNext()) {
                _controller._setInputVariables((TypedIOPort)ctrlInPorts.next());
            }
        }
        _controller._chooseTransition(st.nonpreemptiveTransitionList());
        return;
    }

    /** Return the mode controller of this director.
     *  @return The mode controller.
     */
    public FSMActor getController() {
        return _controller;
    }

    /** Set the mode controller of this director. Throw an exception if the
     *  proposed controller does not have the same container as this
     *  director.
     *  @param The proposed mode controller.
     *  @exception IllegalActionException If the proposed controller does not
     *   have the same container as this director.
     */
    public void setController(FSMActor controller)
            throws IllegalActionException {
        if (controller == null) {
            _controller = null;
            return;
        }
        if (controller.getContainer() != getContainer()) {
            throw new IllegalActionException(this, controller,
                    "The controller does not have the same container as "
                    + "the director.");
        }
        _controller = controller;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** List the external input ports to the mode controller. These
     *  inputs come from outside the modal model.
     *  @return A list of ports.
     */
    protected List _externalInputPortList() {
        // To be finished!
        return new LinkedList();
    }

    /** List the internal input ports to the mode controller. These
     *  inputs come from refinements of the states of the mode
     *  controller.
     *  @return A list of ports.
     */
    protected List _internalInputPortList() {
        // To be finished!
        return new LinkedList();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The FSMActor capturing mode control logic.
    private FSMActor _controller = null;
}
