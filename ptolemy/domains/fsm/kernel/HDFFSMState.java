/* A HDFFSMState is a state in an FSM that refines a heterochronous
   dataflow actor.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.util.VariableList;
import java.util.Enumeration;
import java.util.LinkedList;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMState
/**
A HDFFSMState is a state in an FSM that refines a heterochronous
dataflow (HDF) actor. An HDFFSMState must be used instead of an FSMState
when the FSM refines a HDF actor. It must be refined by a subsystem
which is an opaque composite actor. The association between this state
and the opaque composite actor representing the subsystem is established
by calling setRefinement().
<p>
Note that instances of HDFFSMState must be contained by an
HDFFSMController actor and that instances of the associated
refining states (an opaque composite actor) must be contained
by the HDF actor that this FSM refines. A HDF actor refining to an FSM
should therefore contain an HDFFSMController (containing only
instances of HDFFSMState and HDFFSMTransition), an HDFFSMDirector,
and an opaque composite actor (the refining subsystem) for each
state in the FSM. Each refining subsystem must refine to either
another FSM, an SDF diagram, or an HDF diagram. There is no
constraint to the number of levels in the hierarchy.

@author Brian K. Vogel
@version $Id$
*/
public class HDFFSMState extends FSMState {

    /** Construct a state in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The state is added to the workspace directory.
     */
    public HDFFSMState() {
	super();
    }

    /** Construct a state in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The state is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the state.
     */
    public HDFFSMState(Workspace workspace) {
	super(workspace);
    }

    /** Construct a state with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This state will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an state already in the container.
     */
    public HDFFSMState(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the state into the specified workspace. The new state is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new entity with the same ports as the original, but
     *  no transitions connectioned and no refinement.
     *  @param ws The workspace for the cloned state.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned state (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new FSMState.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        HDFFSMState newobj = (HDFFSMState)super.clone(ws);
        newobj._refinement = null;
        newobj._localStatusVars = null;
        newobj._localValueVars = null;
        try {
            VariableList vlist =
                (VariableList)newobj.getAttribute(LOCAL_INPUT_STATUS_VAR_LIST);
            if (vlist != null) {
                vlist.setContainer(null);
            }
            vlist = (VariableList)newobj.getAttribute(LOCAL_INPUT_VALUE_VAR_LIST);
            if (vlist != null) {
                vlist.setContainer(null);
            }
        } catch (IllegalActionException ex) {
            // this should not happen
        } catch (NameDuplicationException ex) {
            // this should not happen
        }
        newobj._preTrans = null;
        newobj._nonPreTrans = null;
        newobj._transVersion = -1;
        newobj._incoming = (ComponentPort)newobj.getPort(INCOMING_PORT);
        newobj._outgoing = (ComponentPort)newobj.getPort(OUTGOING_PORT);
        return newobj;
    }

    /** Create a transition to this state.
     */
    public FSMTransition createTransitionFrom(FSMState source) {
        FSMTransition trans = null;
        try {
            workspace().getWriteAccess();
            FSMController cont = (FSMController)getContainer();
            trans = new FSMTransition(cont, cont.uniqueName("_Trans"));
            _incomingPort().link(trans);
            source._outgoingPort().link(trans);
        } catch (IllegalActionException ex) {
            // should not happen
        } catch (NameDuplicationException ex) {
            // should not happen
        } finally {
            workspace().doneWriting();
        }
        return trans;
    }

    /** Create a transition from this state.
     */
    public FSMTransition createTransitionTo(HDFFSMState dest) {
        FSMTransition trans = null;
        try {
            workspace().getWriteAccess();
            HDFFSMController cont = (HDFFSMController)getContainer();
            trans = new HDFFSMTransition(cont, cont.uniqueName("_Trans"));
            _outgoingPort().link(trans);
            dest._incomingPort().link(trans);
        } catch (IllegalActionException ex) {
            // should not happen
        } catch (NameDuplicationException ex) {
            // should not happen
        } finally {
            workspace().doneWriting();
        }
        return trans;
    }


}
