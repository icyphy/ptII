/* A transition action for initializing a hybrid subsystem.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.sc.lib;

import ptolemy.domains.sc.kernel.*;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Director;
import ptolemy.kernel.util.*;
import ptolemy.domains.ct.kernel.CTBaseIntegrator;
import ptolemy.automata.util.*;
import ptolemy.data.DoubleToken;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// HSInit
/**
A transition action for initializing a hybrid subsystem. It is attached
to an SCTransition whose destination state has a CT subsystem as refinement.

@author Xiaojun Liu
@version $Id$
*/
public class HSInit extends NamedObj implements TransitionAction {

    public HSInit(SCTransition container, String integratorName, 
            String expression) {
        super(container.workspace(), container.getFullName() + ".init."
                + integratorName);
        _container = container;
        _container.addTransitionAction(this);
        _integratorName = integratorName;
        _expression = expression;
        try {
            _valueVar = new Variable(this, "ValueVar", new DoubleToken());
        } catch (KernelException ex) {
            throw new InvalidStateException(this, ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize internal data structures etc. This is called when
     *  the SCController initializes.
     */
    public void initialize() {
        try {
            VariableList vl = null;
            // Add the SCController's input value variable list to ValueVar's
            // scope. 
            SCController ctrl = (SCController)_container.getContainer();
            vl = (VariableList)ctrl.getAttribute(SCController.INPUT_VALUE_VAR_LIST);
            _valueVar.addToScope(vl);
            // Add the SCController's local variable list to ValueVar's 
            // scope.
            vl = (VariableList)ctrl.getAttribute(SCController.LOCAL_VARIABLE_LIST);
            _valueVar.addToScope(vl);
            // Add the source state's local input value variable list to ValueVar's
            // scope.
            SCState st = _container.sourceState(); 
            vl = (VariableList)st.getAttribute(SCState.LOCAL_INPUT_VALUE_VAR_LIST);
            _valueVar.addToScope(vl);
            _valueVar.setExpression(_expression);
        } catch (KernelException ex) {
            throw new InvalidStateException(this, ex.getMessage());
        }
    }

    /** Execute the action.
     */
    public void execute() {
        _valueVar.evaluate();
        SCState dest = _container.destinationState();
        CompositeActor nref = (CompositeActor)dest.getRefinement();
        try {
            Director exec = nref.getExecutiveDirector();
            Director dir = nref.getDirector();
            dir.setCurrentTime(exec.getCurrentTime());
        } catch (IllegalActionException ex) {
            throw new InvalidStateException(this, ex.getMessage());
        }
        CTBaseIntegrator intgr = (CTBaseIntegrator)nref.getEntity(_integratorName);

        System.out.println("Executing transition action: " + this.getFullName());

        intgr.setPotentialState(((DoubleToken)_valueVar.getToken()).doubleValue());
        // set the input of dest ref
        // SCState src = _container.sourceState();
        // CompositeActor cref = (CompositeActor)src.getRefinement();
        SCController ctrl = (SCController)dest.getContainer();
        VariableList vl = (VariableList)ctrl.getAttribute(SCController.INPUT_VALUE_VAR_LIST);
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
    }

    public Nameable getContainer() {
        return _container;
    }

    private String _integratorName;
    private String _expression;
    private SCTransition _container;
    private Variable _valueVar;

}
