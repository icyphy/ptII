/* A transition action for initializing a hybrid subsystem.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

package ptolemy.domains.fsm.lib;

import ptolemy.domains.fsm.kernel.*;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Director;
import ptolemy.kernel.util.*;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.data.expr.Variable;
import ptolemy.domains.fsm.kernel.util.VariableList;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// HSInit
/**
A transition action for initializing a hybrid subsystem. It is attached
to an FSMTransition whose destination state has a CT subsystem as refinement.

@author Xiaojun Liu
@version $Id$
*/
public class HSInit extends NamedObj implements TransitionAction {

    /** FIXME
     *  @exception IllegalActionException If the integrator name has a period.
     */
    public HSInit(FSMTransition container, String integratorName,
            String expression) throws IllegalActionException {
        super(container.workspace(), container.getName() + "_init_"
                + integratorName);
        _container = container;
        _container.addTransitionAction(this);
        _integratorName = integratorName;
        _expression = expression;
        try {
            _valueVar = new Variable(this, "ValueVar", new DoubleToken());
        } catch (IllegalActionException ex) {
	    throw new InvalidStateException(this, ex.getMessage());
	} catch (NameDuplicationException ex) {
            throw new InvalidStateException(this, ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize internal data structures etc. This is called when
     *  the FSMController initializes.
     */
    public void initialize() throws IllegalActionException {
        /*try*/ {
            VariableList vl = null;
            // Add the FSMController's input value variable list to ValueVar's
            // scope.
            FSMController ctrl = (FSMController)_container.getContainer();
            vl = (VariableList)ctrl.getAttribute(FSMController.INPUT_VALUE_VAR_LIST);
            if (vl != null) {
                _valueVar.addToScope(vl.getVariables());
            }
            // Add the FSMController's local variable list to ValueVar's
            // scope.
            vl = (VariableList)ctrl.getAttribute(FSMController.LOCAL_VARIABLE_LIST);
            if (vl != null) {
                _valueVar.addToScope(vl.getVariables());
            }
            // Add the source state's local input value variable list to ValueVar's
            // scope.
            FSMState st = _container.sourceState();
            vl = (VariableList)st.getAttribute(FSMState.LOCAL_INPUT_VALUE_VAR_LIST);
            if (vl != null) {
                _valueVar.addToScope(vl.getVariables());
            }
            _valueVar.setExpression(_expression);
        } /*catch (KernelException ex) {
            throw new InvalidStateException(this, ex.getMessage());
            }*/
    }

    /** Execute the action.
     */
    public void execute() throws IllegalActionException {
        FSMState dest = _container.destinationState();
        CompositeActor nref = (CompositeActor)dest.getRefinement();

        Director exec = nref.getExecutiveDirector();
        Director dir = nref.getDirector();

        CTBaseIntegrator intgr = (CTBaseIntegrator)nref.getEntity(_integratorName);



        //intgr.setPotentialState(((DoubleToken)_valueVar.getToken()).doubleValue());
        Parameter init = (Parameter)intgr.getAttribute("InitialState");
        init.setToken(_valueVar.getToken());

        //System.out.println("Executing transition action: " +
        //this.getFullName() + " " +
        //((DoubleToken)_valueVar.getToken()).doubleValue());

        // set the input of dest ref
        // FSMState src = _container.sourceState();
        // CompositeActor cref = (CompositeActor)src.getRefinement();
        FSMController ctrl = (FSMController)dest.getContainer();
        VariableList vl = (VariableList)ctrl.getAttribute(FSMController.INPUT_VALUE_VAR_LIST);
        Enumeration invars = vl.getVariables();
        while (invars.hasMoreElements()) {
            Variable invar = (Variable)invars.nextElement();
            IOPort np = (IOPort)nref.getPort(invar.getName());
            if (np != null) {
                try {
                    if (np.hasToken(0)) {
                        np.get(0);
                    }
                    np.send(0, invar.getToken());
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException(this, "Error in executing "
                            + "transition action: " + ex.getMessage());
                }
            }
        }
        nref.initialize();
        dir.setCurrentTime(exec.getCurrentTime());

        //System.out.println("transition action set " +
        //dir.getFullName() + " current time " +
        //exec.getCurrentTime());

    }

    public Nameable getContainer() {
        return _container;
    }

    /** @serial Name of the integrator. */
    private String _integratorName;

    /** @serial Expression to process. */
    private String _expression;

    /** @serial Container. */
    private FSMTransition _container;

    /** @serial Value of the variable. */
    private Variable _valueVar;
}
