/* A HDFFSMTransition connects two HDFFSMStates.

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

@ProposedRating Red (liuxj@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.domains.fsm.kernel;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.util.VariableList;
import java.util.Enumeration;
import java.util.LinkedList;
import ptolemy.domains.fsm.kernel.*;
import ptolemy.domains.fsm.lib.*;

//////////////////////////////////////////////////////////////////////////
//// HDFFSMTransition
/**
A HDFFSMTransition connects two HDFFSMStates. It has a trigger
condition (guard condition). The guard condition is an expression
in the Ptolemy II expression language. Currently, the only variables
allowed in the expression are variables containing tokens transfered
through the input and output ports of the HDF actor refining to
this FSM. The setTriggerCondition() method is used to set this
transition's guard expression.
<p>
A type B firing [1] is defined as final firing of an HDF actor
in an iteration of the HDF diagram. Note that in HDF, a different
schedule is used each time the FSM refining an HDF actor
changes states. The number of firings per iteration can therefore
change when the FSM changes state.
In HDF, transitions can only occurr on type B firings [1]. In the
current HDF implementation, the guard expression is only evaluated
on a type B firing, and if true, a state transition will occurr.
<p>
Nondeterminant transitions (more than one transition with true valued
guard expression) are not allowed.


@author Brian K. Vogel
@version $Id$
*/
public class HDFFSMTransition extends FSMTransition {

    /** Construct a transition in the default workspace with an empty string
     *  as its name. Add the transition to the directory of the workspace.
     */
    public HDFFSMTransition() {
        super();
    }

    /** Construct a transition in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  Add the transition to the workspace directory.
     *  @param workspace The workspace that will list the transition.
     */
    public HDFFSMTransition(Workspace workspace) {
	super(workspace);
    }

    /** Construct a transition with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This transition will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the transition.
     *  @exception IllegalActionException If the container is incompatible
     *   with this transition.
     *  @exception NameDuplicationException If the name coincides with
     *   any relation already in the container.
     */
    public HDFFSMTransition(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new relation with no links and no container.
     *  @param ws The workspace for the cloned object.
     *  @exception CloneNotSupportedException If one or more of the attributes
     *   cannot be cloned.
     *  @return A new ComponentRelation.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        HDFFSMTransition newobj = (HDFFSMTransition)super.clone(ws);
        newobj._stateVersion = -1;
        if (_trigger != null) {
            newobj._trigger = (VariableList)newobj.getAttribute("Trigger");
            newobj._te = (Variable)newobj._trigger.getAttribute("TriggerEvent");
            newobj._tc = (Variable)newobj._trigger.getAttribute("TriggerCondition");
            newobj._actions = (VariableList)newobj.getAttribute("Actions");
            newobj._localVarUpdates = (VariableList)newobj.getAttribute("LocalVarUpdates");
        }
        return newobj;
    }


    /** Return true if this transition is enabled.
     */
    public boolean isEnabled() throws IllegalActionException {

        if (_teSet) {

            if (_debugging) _debug("HDFFSMTransition: isEnabled(): Testing trigger event of " + this.getFullName());

            _te.getToken();
	    /*
              if (((BooleanToken)_te.getToken()).booleanValue() == false) {
              return false;
              }
	    */
        }
        if (_tcSet) {

            if (_debugging) _debug("HDFFSMTransition: isEnabled(): Testing condition of " + this.getFullName());

            _tc.getToken();
            if (((BooleanToken)_tc.getToken()).booleanValue() == false) {
                return false;
            }
        }
	if (_debugging) _debug("HDFFSMTransition: isEnabled(): returning TRUE");
        return true;
    }


    /** Set the trigger event (guard expression) of this transition.
     *  The guard expressions use the Ptolemy II expression language. Currently,
     *  the only variables allowed in the guard expressions are variables
     *  containing tokens transfered through the input and output ports of
     *  the HDF actor. Following the syntax of [1], if the HDF actor contains
     *  an input port called dataIn, then use dataIn$0 in the guard
     *  expression to reference the token most recently transfered through
     *  port dataIn. Use dataIn$1 to reference the next most recent token,
     *  dataIn$2 to reference the next most recent token, and so on. By
     *  default, only the most recently transfered token is allowed.
     *  In order to be able to reference up to the m'th most recently
     *  transfered token (dataIn$m), call the setGuardTokenHistory()
     *  method of the director with m as the parameter.
     */
    public void setTriggerEvent(String te) {
	// _te is a Variable.
	// Create a VariableList and place _te in it.
        if (_te == null) {
            _createVarLists();
        }
        _te.setExpression(te);
        _teSet = true;
    }

    /** Set the trigger condition of this transition.
     */
    public void setTriggerCondition(String tc) {
        if (_tc == null) {
            _createVarLists();
        }
        _tc.setExpression(tc);
        _tcSet = true;
    }




    /** Setup the scope of the guard (trigger condition). The scope
     *  consists of the guard variables. There is a sequence of
     *  guard variables associated with each port contained by the
     *  the local director's container (an opaque composite actor).
     *  For example, if the director's container has an input port
     *  called "dataIn", then the associated sequence of guard
     *  variables is dataIn$0, dataIn$1, dataIn$2, .... Here,
     *  dataIn$0 denotes the most recently read token, dataIn$1
     *  denotes the next most recently read token, and so on.
     *  <p>
     *  The director method setGuardTokenHistory() sets the
     *  number of guard tokens (largest n in dataIn$n).
     */
    public void setupScope() throws NameDuplicationException, IllegalActionException {
	if (_debugging) _debug("HDFFSMTransition: setupScope()");
	HDFFSMController ctrl = (HDFFSMController)getContainer();
	HDFFSMDirector direct = ((HDFFSMDirector)ctrl.getDirector());
	//directorGuard = direct.guardVarArray[0];
	Enumeration dirScopeVars = direct._getTransitionGuardVars();
	/*
          if (dirScopeVars != null) {
          _te.addToScope(dirScopeVars);
          _tc.addToScope(dirScopeVars);
          System.out.println("HDFFSMTransition: setupScope(): added guard vars to scope");
          } else {
          //throw new IllegalActionException((HDFFSMController)getContainer(), this,
          //      "The guard variable is null");
          System.out.println("The guard variable list is null");
          }
	*/
	if (dirScopeVars != null) {
	    while (dirScopeVars.hasMoreElements()) {
		Variable var1 = (Variable)dirScopeVars.nextElement();
		_te.addToScope(var1);
		_tc.addToScope(var1);
		//System.out.println("HDFFSMTransition: setupScope(): Adding "
		//	   + var1.toString());
	    }
        } else {
	    //throw new IllegalActionException((HDFFSMController)getContainer(), this,
	    //      "The guard variable is null");
	    if (_debugging) _debug("The guard variable list is null");
	}
    }

    //////////////////////////////////////////////////////////////
    ///////////        protected methods                   ///////



    ///////////////////////////////////////////////////////////////
    // Variable directorGuard;
}
