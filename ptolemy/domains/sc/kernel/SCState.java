/* A SCState is a state in the *charts formalism.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

*/

package ptolemy.domains.sc.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.data.*;
import ptolemy.automata.util.*;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SCState
/**
A SCState is a state in the *charts formalism. It can be refined by a
subsystem which is an opaque composite actor.

@author Xiaojun Liu
@version $Id$
*/
public class SCState extends ComponentEntity {

    /** Construct a state in the default workspace with an empty string
     *  as its name. Increment the version number of the workspace.
     *  The state is added to the workspace directory.
     */
    public SCState() {
	super();
    }

    /** Construct a state in the specified workspace with an empty
     *  string as its name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The state is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the state.
     */
    public SCState(Workspace workspace) {
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
    public SCState(CompositeEntity container, String name)
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
     *  @return A new SCState.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        SCState newobj = (SCState)super.clone(ws);
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

    /** Get the composite actor refining this state.
     *  @return The refinement.
     */
    public Actor getRefinement() {
        return _refinement;
    }

    /** Set the composite actor refining this state.
     *  @param refinement The composite actor refining this state.
     */
    public void setRefinement(Actor refinement)
            throws IllegalActionException, NameDuplicationException {
        try {
            workspace().getWriteAccess();
            // remove the current local variable list from the scope of
            // non-preemptive transitions
            if (_localStatusVars != null) {
                _localStatusVars.setContainer(null);
                _localStatusVars = null;
                _localValueVars.setContainer(null);
                _localValueVars = null;
            }
            if (_refinement != null) {
                ((ComponentEntity)_refinement).setContainer(null);
            }
            _refinement = refinement;
            if (_refinement != null) {
                ((ComponentEntity)_refinement).setContainer((CompositeEntity)getContainer().getContainer());
            }
        } finally {
            workspace().doneWriting();
        }
    }

    /** Get an enumeration of outgoing preemptive transitions.
     *  @return An enumeration of outgoing preemptive transitions.
     */
    public Enumeration getPreemptiveTrans() {
        if (workspace().getVersion() == _transVersion) {
            return _preTrans.elements();
        }
        _createTransLists();
        return _preTrans.elements();
    }

    /** Get an enumeration of outgoing non-preemptive transitions.
     *  @return An enumeration of outgoing non-preemptive transitions.
     */
    public Enumeration getNonPreemptiveTrans() {
        if (workspace().getVersion() == _transVersion) {
            return _nonPreTrans.elements();
        }
        _createTransLists();
        return _nonPreTrans.elements();
    }

    /** Set the container of the state. The refinement is lost.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        if (!(container instanceof SCController) && (container != null)) {
            throw new IllegalActionException(container, this,
                    "SCState can only be contained by instances of " +
                    "SCController.");
        }
        if (container == getContainer()) {
            return;
        }
        if (_refinement != null) {
            ((ComponentEntity)_refinement).setContainer(null);
        }
        if (_localStatusVars != null) {
            _localStatusVars.setContainer(null);
            _localStatusVars = null;
            _localValueVars.setContainer(null);
            _localValueVars = null;
        }
        _transVersion = -1;
        super.setContainer(container);
    }

    /** If argument is true, then the subsystem is initialized each time
     *  this state is entered.
     */
    public void setInitEntry(boolean init) {
        _initEntry = init;
    }

    /** Return true if the subsystem is initialized each time this state
     *  is entered.
     */
    public boolean isInitEntry() {
        return _initEntry;
    }

    /** Generate preemptive/non-preemptive transitions lists.
     */
    private void _createTransLists() {
        try {
            workspace().getReadAccess();
            _preTrans = new LinkedList();
            _nonPreTrans = new LinkedList();
            if (_outgoing != null) {
                Enumeration out_trans = _outgoing.linkedRelations();
                SCTransition trans;
                while (out_trans.hasMoreElements()) {
                    trans = (SCTransition)out_trans.nextElement();
                    if (trans.sourceState() != this) {
                        continue;
                    }
                    if (trans.isPreemptive()) {
                        _preTrans.insertFirst(trans);
                    } else {
                        _nonPreTrans.insertFirst(trans);
                    }
                }
            }
            _transVersion = workspace().getVersion();
        } finally {
            workspace().doneReading();
        }
    }

    /** Return the port connecting all incoming transitions.
     */
    protected ComponentPort _incomingPort() {
        if (_incoming == null) {
            try {
                workspace().getWriteAccess();
                _incoming = new ComponentPort(this, INCOMING_PORT);
            } catch (IllegalActionException ex) {
            } catch (NameDuplicationException ex) {
            } finally {
                workspace().doneWriting();
            }
        }
        return _incoming;
    }

    /** Return the port connecting all outgoing transitions.
     */
    protected ComponentPort _outgoingPort() {
        if (_outgoing == null) {
            try {
                workspace().getWriteAccess();
                _outgoing = new ComponentPort(this, OUTGOING_PORT);
            } catch (IllegalActionException ex) {
            } catch (NameDuplicationException ex) {
            } finally {
                workspace().doneWriting();
            }
        }
        return _outgoing;
    }

    /** Create a transition to this state.
     */
    public SCTransition createTransitionFrom(SCState source) {
        SCTransition trans = null;
        try {
            workspace().getWriteAccess();
            SCController cont = (SCController)getContainer();
            trans = new SCTransition(cont, cont._uniqueTransitionName());
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
    public SCTransition createTransitionTo(SCState dest) {
        SCTransition trans = null;
        try {
            workspace().getWriteAccess();
            SCController cont = (SCController)getContainer();
            trans = new SCTransition(cont, cont._uniqueTransitionName());
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

    protected void setupScope() {
        try {
            if (_localStatusVars != null) {
                // remove old variable lists
                _localStatusVars.setContainer(null);
                _localValueVars.setContainer(null);
            }
            if (_refinement != null) {
                // create new variable lists
                _localStatusVars = new VariableList(this, LOCAL_INPUT_STATUS_VAR_LIST);
                _localValueVars = new VariableList(this, LOCAL_INPUT_VALUE_VAR_LIST);
                _localStatusVars.createVariables(_refinement.outputPorts());
                _localValueVars.createVariables(_refinement.outputPorts());
            } else {
                _localStatusVars = null;
                _localValueVars = null;
            }
        } catch (IllegalActionException ex) {
            // this should not happen
        } catch (NameDuplicationException ex) {
            // this should not happen
        }
    }

    /** Set the value of the local input status variables to ABSENT.
     */
    public void resetLocalInputStatus() {
        if (_localStatusVars == null) {
            return;
        }
        try {
            _localStatusVars.setAllVariables(SCController.ABSENT);
        } catch (IllegalArgumentException ex) {
            // this should not happen
        }
    }

    /** Set the value of a local input value variable, and set the
     *  corresponding local input status variable to PRESENT.
     */
    public void setLocalInputVar(String name, Token value)
            throws IllegalArgumentException {
        if (_localValueVars == null) {
            return;
        }
        _localValueVars.setVarValue(name, value);
        _localStatusVars.setVarValue(name, SCController.PRESENT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The name of the outgoing port.
     */
    public static final String OUTGOING_PORT = "Outgoing";

    /** The name of the incoming port.
     */
    public static final String INCOMING_PORT = "Incoming";

    /** The name of the local status variable list.
     */
    public static final String LOCAL_INPUT_STATUS_VAR_LIST = "LocalStatusVars";

    /** The name of the local value variable list.
     */
    public static final String LOCAL_INPUT_VALUE_VAR_LIST = "LocalValueVars";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The actor refining this state.
    Actor _refinement = null;

    // The list of variables corresponding to the status of output
    // of refinement.
    VariableList _localStatusVars = null;

    // The list of variables corresponding to the value of output
    // of refinement.
    VariableList _localValueVars = null;

    // The list of outgoing preemptive transitions.
    LinkedList _preTrans = null;

    // The list of outgoing non-preemptive transitions.
    LinkedList _nonPreTrans = null;

    // The version of the transitions lists.
    long _transVersion = -1;

    // If true, the subsystem refining this state is initialized each
    // time entering this state.
    boolean _initEntry = false;

    // The port connects to all incoming transitions.
    ComponentPort _incoming = null;

    // The port connects to all outgoing transitions.
    ComponentPort _outgoing = null;

}










