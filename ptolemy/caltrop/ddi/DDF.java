/*
 @Copyright (c) 2006-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.caltrop.ddi;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.util.IllegalActionException;
import caltrop.interpreter.Context;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.AttributeKeys;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.environment.Environment;

///////////////////////////////////////////////////////////////////
//// DDF

/**
 A plugin for the DDF domain. In DDF, a CAL actor is valid if:
 <p>
 <ol>
 <li> The guards of each action do not depend on the inputs.
 <li> The rates of each action do not depend on the inputs.
 </ol>

 This plugin also adds attributes containing rate information to the
 ports of the actor.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class DDF extends DataflowWithRates {
    /**
     * Create an new DDF DDI.
     * @param ptActor The instance of {@link ptolemy.actor.Actor
     * ptolemy.actor.Actor} that the plugin will be associated with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     */
    public DDF(TypedAtomicActor ptActor, Actor actor, Context context,
            Environment env) {
        super(ptActor, actor, context, env);
    }

    /**
     * Initialize the actor and select the first action to fire.
     * Publish rate information for this actor according to the
     * action.
     * @exception IllegalActionException
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        try {
            int action = _selectAction();

            if (action == -1) {
                _currentSignature = _zeroRateSignature;
            } else {
                _currentSignature = _computeActionRates(_actions[action]);
            }

            _annotatePortsWithRates(_ptActor.inputPortList(),
                    _currentSignature.getInputRates(), "tokenConsumptionRate");
            _annotatePortsWithRates(_ptActor.outputPortList(),
                    _currentSignature.getOutputRates(), "tokenProductionRate");
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Error during action selection in actor '"
                            + _actor.getName() + "'");
        }
    }

    /**
     * In DDF, an actor is legal if:
     * <ol>
     * <li> The guards of each action do not depend on the inputs.
     * <li> The rates of each action do not depend on the inputs.
     * </ol>
     * @return True if the actor associated with this <tt>DDI</tt> is
     * a legal DDF actor.
     */
    @Override
    public boolean isLegalActor() {
        try {
            if (_hasInputDependentGuard()) {
                throw new RuntimeException("A guard depends on an input!");
            }

            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the name of this DDI.
     * @return "DDF".
     */
    @Override
    public String getName() {
        return "DDF";
    }

    /**
     * Postfire this actor.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        super.postfire();

        try {
            int action = _selectAction();

            if (action == -1) {
                _currentSignature = _zeroRateSignature;
            } else {
                _currentSignature = _computeActionRates(_actions[action]);
            }

            _annotatePortsWithRates(_ptActor.inputPortList(),
                    _currentSignature.getInputRates(), "tokenConsumptionRate");
            _annotatePortsWithRates(_ptActor.outputPortList(),
                    _currentSignature.getOutputRates(), "tokenProductionRate");
            return true;
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex,
                    "Error during action selection in actor '"
                            + _actor.getName() + "'");
        }
    }

    /**
     * Select a fireable action among the actions of the actor, if possible.
     *
     * @return True, if an action could be selected.
     * @exception IllegalActionException If an error occurred during the
     * action selection.
     *
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        _lastFiredAction = null;

        if (_currentSignature == null) {
            try {
                int action = _selectAction();

                if (action == -1) {
                    _currentSignature = null;
                } else {
                    _currentSignature = _computeActionRates(_actions[action]);

                    _annotatePortsWithRates(_ptActor.inputPortList(),
                            _currentSignature.getInputRates(),
                            "tokenConsumptionRate");
                    _annotatePortsWithRates(_ptActor.outputPortList(),
                            _currentSignature.getOutputRates(),
                            "tokenProductionRate");
                }
            } catch (Exception ex) {
                throw new IllegalActionException(null, ex,
                        "Error during action selection in actor '"
                                + _actor.getName() + "'");
            }

            return false;
        }

        for (Object element : _ptActor.inputPortList()) {
            IOPort port = (IOPort) element;
            Integer integerRate = (Integer) _currentSignature.getInputRates()
                    .get(port.getName());

            if (integerRate != null
                    && !port.hasToken(0, integerRate.intValue())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Setup the actor associated with this <tt>DDI</tt>. Assumes that
     * {@link #isLegalActor() isLegalActor()} is called first.
     * <p>
     * Setup involves attaching attributes with token
     * consumption/production rates to the input and output ports of
     * the actor associated with this <tt>DDI</tt>.
     */
    @Override
    public void setupActor() {
        _selectInitializer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Return true if any action has a guard which depends on input values.
    protected boolean _hasInputDependentGuard() {
        for (Action action : _actions) {
            Expression[] guards = action.getGuards();

            for (Expression guard : guards) {
                List freeVars = (List) guard
                        .getAttribute(AttributeKeys.KEYFREEVAR);

                if (freeVars != null) {
                    for (Iterator iterator = freeVars.iterator(); iterator
                             .hasNext();) {
                        String name = (String) iterator.next();

                        if (_isBoundByPortVar(name, action)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////
    private ActionRateSignature _currentSignature;

    private static ActionRateSignature _zeroRateSignature = new ActionRateSignature();
}
