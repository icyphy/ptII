/*
@Copyright (c) 2003 The Regents of the University of California.
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

import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.InputPort;
import caltrop.interpreter.InterpreterException;
import caltrop.interpreter.SingleInputPort;
import caltrop.interpreter.SingleOutputPort;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.AttributeKeys;
import caltrop.interpreter.ast.Decl;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.InputPattern;
import caltrop.interpreter.ast.OutputExpression;
import caltrop.interpreter.ast.PortDecl;
import caltrop.interpreter.environment.Environment;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.caltrop.actors.CalInterpreter;
import ptolemy.caltrop.ddi.util.DataflowActorInterpreter;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// SDFJava
/**
A plugin for the SDF domain. In SDF, a CAL actor is valid if:
<p>
<ol>
<li> The rates of each action are statically computable, and these rates are the same for each action.
<li> There is at least one unguarded action.
</ol>

This plugin also adds attributes containing rate information to the
ports of the actor.

@author Christopher Chang <cbc@eecs.berkeley.edu>
@version $Id$
@since Ptolemy II 3.1
*/
public class SDF extends AbstractDDI implements DDI {

    /**
     * Create an <tt>SDF</tt>
     * @param ptActor The instance of {@link ptolemy.actor.Actor
     * ptolemy.actor.Actor} that the plugin will be associated with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     */
    public SDF(CalInterpreter ptActor, Actor actor, Context context,
            Environment env) {
        _ptActor = ptActor;
        _actor = actor;
        _context = context;
        _env = env;
        _eval = new ExprEvaluator(_context,  _env);
        _actionRates = new ActionRateSignature[_actor.getActions().length];
        _initializerRates =
            new ActionRateSignature[_actor.getInitializers().length];
        _inputPorts = createPortMap(_actor.getInputPorts(), true);
        _outputPorts = createPortMap(_actor.getOutputPorts(), false);
        _actorInterpreter = new DataflowActorInterpreter(_actor, _context,
                _env, _inputPorts, _outputPorts);
    }

    private Map createPortMap(PortDecl [] ports, boolean isInput) {
        Map portMap = new HashMap();
        for (int i = 0; i < ports.length; i++) {
            String name = ports[i].getName();
            TypedIOPort port = (TypedIOPort) _ptActor.getPort(name);
            if (isInput) {
                portMap.put(name, new SingleInputPort(name,
                        new DFInputChannel(port, 0)));
            } else {
                portMap.put(name, new SingleOutputPort(name,
                        new DFOutputChannel(port, 0)));
            }
        }
        return portMap;
    }

    private CalInterpreter _ptActor;
    private Actor _actor;
    private Context _context;
    private Environment _env;
    private ExprEvaluator _eval;
    private ActionRateSignature [] _actionRates;
    private ActionRateSignature [] _initializerRates;
    private DataflowActorInterpreter _actorInterpreter;
    private Map _inputPorts;
    private Map _outputPorts;

    /**
     * In SDF, an actor is legal if:
     * <ol>
     * <li> The rates of each action are statically computable, and
     * these rates are the same for each action.
     * <li> There is at least one unguarded action.
     * <li> The rates and guards of the initializers are statically computable.
     * </ol>
     * @return True if the actor associated with this <tt>DDI</tt> is
     * a legal SDF actor.
     */
    public boolean isLegalActor() {
        if (atLeastOneUnguardedAction()
                && checkActionRates() && checkInitializers())
            return true;
        return false;
    }

    /**
     * Setup the actor associated with this <tt>DDI</tt>. Assumes that
     * {@link #isLegalActor() isLegalActor()} is called first.
     * <p>
     * Setup involves attaching attributes with token
     * consumption/production rates to the input and output ports of
     * the actor associated with this <tt>DDI</tt>.
     */
    public void setupActor() {
        // use the 0th element because the rates are all the same.
        annotatePortsWithRates(_ptActor.inputPortList(), _actionRates[0].getInputRates(),  "tokenConsumptionRate");
        annotatePortsWithRates(_ptActor.outputPortList(), _actionRates[0].getOutputRates(),  "tokenProductionRate");
        annotatePortsWithInitProductionRates();
        _ptActor.getDirector().invalidateSchedule();
    }

    /*
      1. figure out which initializer to fire (if we've gotten this far, rates and guards
      are both statically computable)
      2. annotate ports with the correct rates.
    */
    private void annotatePortsWithInitProductionRates() {
        int i = _selectInitializer();
        if (i != -1)
            annotatePortsWithRates(_ptActor.outputPortList(), _initializerRates[i].getOutputRates(),
                    "tokenInitProduction");
        return;
    }

    /**
     * Get the name of this <tt>SDF</tt>.
     * @return The name of this <tt>SDF</tt>.
     */
    public String getName() {
        return "SDF";
    }

    private void annotatePortsWithRates(List ioPorts, Map rateMap, String varName) {
        for (Iterator iterator = ioPorts.iterator(); iterator.hasNext();) {
            IOPort ioPort = (IOPort) iterator.next();
            Object o = ioPort.getAttribute(varName);

            if (o != null) {
                if (o instanceof Variable)
                    try {
                        ((Variable) o).setContainer(null);
                    } catch (Exception e) {
                        throw new DDIException("In IOPort " + ioPort.getName() + " couldn't clear Variable " +
                                ((Variable) o).getName());
                    }
                else throw new DDIException("IOPort " + ioPort.getName() +
                        " contains An attribute named " + varName + " that is not a Variable...this shouldn't happen.");
            }

            try {
                new Variable(ioPort, varName, new IntToken(((Integer) rateMap.get(ioPort.getName())).intValue()));
            } catch (IllegalActionException e) {
                // this shouldn't happen. ioPort can definitely hold a variable, and it's guaranteed to be non-null.
                throw new DDIException("This shouldn't happen", e);
            } catch (NameDuplicationException e) {
                // this shouldn't happen either, as this method is the only thing attaching Variables to ports.
                throw new DDIException("This shouldn't happen", e);
            }
        }
    }

    private boolean atLeastOneUnguardedAction() {
        Action [] actions = _actor.getActions();

        for (int i = 0; i < actions.length; i++) {
            Action action = actions[i];
            if (action.getGuards().length == 0)
                return true;
        }
        return false;
    }

    private boolean checkActionRates() {
        if (!computeActionRates())
            return false;
        if (!checkEqualRates())
            return false;
        return true;
    }

    private boolean checkInitializers() {
        if (!computeInitializerRates())
            return false;
        if (!checkInitializerGuards())
            return false;
        return true;
    }

    private boolean checkInitializerGuards() {
        Action [] initializers = _actor.getInitializers();
        for (int i = 0; i < initializers.length; i++) {
            Action initializer = initializers[i];
            Expression [] guards = initializer.getGuards();
            for (int j = 0; j < guards.length; j++) {
                if (!isStaticallyComputable(guards[j], initializer))
                    return false;
            }
        }
        return true;
    }

    /**
     * Verify that the rates of each action are statically computable, and if so, compute them, storing the
     * results in _actionRates.
     * @return
     */
    private boolean computeActionRates() {
        Action [] actions = _actor.getActions();

        for (int i = 0; i < actions.length; i++) {
            Action action =  actions[i];
            ActionRateSignature ars = new ActionRateSignature();

            InputPattern [] inputPatterns = action.getInputPatterns();
            for (int j = 0; j < inputPatterns.length; j++) {
                InputPattern inputPattern = inputPatterns[j];
                Expression repeatExpr = inputPattern.getRepeatExpr();
                int repeatVal = computeRepeatExpr(repeatExpr, action);
                if (repeatVal == -1)
                    return false;
                ars.addInputRate(inputPattern.getPortname(), inputPattern.getVariables().length * repeatVal);
            }

            OutputExpression [] outputexps = action.getOutputExpressions();
            for (int j = 0; j < outputexps.length; j++) {
                OutputExpression outputexp = outputexps[j];
                Expression repeatExpr = outputexp.getRepeatExpr();
                int repeatVal = computeRepeatExpr(repeatExpr, action);
                if (repeatVal == -1)
                    return false;
                ars.addOutputRate(outputexp.getPortname(), outputexp.getExpressions().length * repeatVal);
            }
            _actionRates[i] = ars;
        }
        return true;
    }

    /**
     * Verify that the rates of each action are statically computable, and if so, compute them, storing the
     * results in _actionRates.
     * @return
     */
    private boolean computeInitializerRates() {
        Action [] actions = _actor.getInitializers();

        for (int i = 0; i < actions.length; i++) {
            Action action =  actions[i];
            ActionRateSignature ars = new ActionRateSignature();

            InputPattern [] inputPatterns = action.getInputPatterns();
            for (int j = 0; j < inputPatterns.length; j++) {
                InputPattern inputPattern = inputPatterns[j];
                Expression repeatExpr = inputPattern.getRepeatExpr();
                int repeatVal = computeRepeatExpr(repeatExpr, action);
                if (repeatVal == -1)
                    return false;
                ars.addInputRate(inputPattern.getPortname(), inputPattern.getVariables().length * repeatVal);
            }

            OutputExpression [] outputexps = action.getOutputExpressions();
            for (int j = 0; j < outputexps.length; j++) {
                OutputExpression outputexp = outputexps[j];
                Expression repeatExpr = outputexp.getRepeatExpr();
                int repeatVal = computeRepeatExpr(repeatExpr, action);
                if (repeatVal == -1)
                    return false;
                ars.addOutputRate(outputexp.getPortname(), outputexp.getExpressions().length * repeatVal);
            }
            _initializerRates[i] = ars;
        }
        return true;
    }

    /**
     * Compute the value of a repeat expression. If this is not statically possible, return -1.
     *
     * In order for the repeat expression to be statically computable, its value must only depend on global variables
     * or actor parameters. In other words, any free variables in a repeat expression cannot be bound by action state
     * variables, port variables, or actor state variables.
     * @param repeatExpr
     * @param action
     * @return
     */
    private int computeRepeatExpr(Expression repeatExpr, Action action) {
        if (repeatExpr == null) {
            return 1;
        } else {
            if (!isStaticallyComputable(repeatExpr, action))
                return -1;

            int value;
            try {
                value = this._context.intValue(_eval.evaluate(repeatExpr));
            } catch (InterpreterException ie) {
                return -1;
            }
            return value;
        }
    }

    // Assumes free variable annotater has been run on the AST.
    private boolean isStaticallyComputable(Expression expr, Action action) {
        if (expr == null)
            return true;
        List freeVars = (List) expr.getAttribute(AttributeKeys.KEYFREEVAR);
        for (Iterator iterator = freeVars.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            if (isBoundByPortVar(name, action) ||
                    isIn(name, action.getDecls()) ||
                    isIn(name, _actor.getStateVars()))
                return false;
        }
        return true;
    }

    private static boolean isBoundByPortVar(String name, Action action) {
        InputPattern [] inputPatterns = action.getInputPatterns();
        for (int i = 0; i < inputPatterns.length; i++) {
            if (isIn(name, inputPatterns[i].getVariables()))
                return true;
        }
        return false;
    }

    private static boolean isIn(String name, String [] names) {
        for (int i = 0; i < names.length; i++) {
            if (name == names[i])
                return true;
        }
        return false;
    }

    private static boolean isIn(String name, Decl [] decls) {
        for (int i = 0; i < decls.length; i++) {
            if (name == decls[i].getName())
                return true;
        }
        return false;
    }


    private boolean checkEqualRates() {
        return allEqual(_actionRates);
    }

    private static boolean allEqual(Object [] objs) {
        if (objs.length <= 1)
            return true;
        Object standard = objs[0];

        for (int i = 1; i < objs.length; i++) {
            if (!standard.equals(objs[i]))
                return false;
        }
        return true;
    }

    /**
     * Executes the selected action on the first {@link #fire()
     * fire()} call. On successive calls, it rolls back previous state
     * changes, selects a new action and executes it.
     *
     * <p> <b>Note: Is this correct behavior? What is the contract
     * between the result of prefire() and successive calls to
     * fire()?</b>
     *
     * @throws IllegalActionException If an error occurs during the
     * interpretation of the action.
     */
    public void fire() throws IllegalActionException {
        // FIXMELATER: state transition and potentially rollback
        try {
            if (_actorInterpreter.currentAction() == null) {

                // This point is reached iff this is not the first fire()
                // call of this iteration.
                // Hence we could put rollback work here.

                _selectAction();
            }
            if (_actorInterpreter.currentAction() != null) {
                _actorInterpreter.actionStep();
                _actorInterpreter.actionComputeOutputs();
                _actorInterpreter.actionClear();
                _clearInputChannels();
            }
        } catch (Exception e) {
            throw new IllegalActionException("Could not fire CAL actor '" + _actor.getName() + "': " + e.getMessage());
        }
    }

    /**
     * _selectAction picks an action for which the actor interpreter evaluates the guard to true. Note that this does
     * not necessarily mean that <em>all</em> preconditions for firing are satisfied---the amount of "prechecking"
     * depends on the model of computation ddi. (FIXMELATER)
     *
     * @return The action number that was selected, a value <0 if no action was selected.
     */
    private int  _selectAction() {
        Action [] actions = _actor.getActions();
        for (int i = 0; i < actions.length; i++) {
            _actorInterpreter.actionSetup(actions[i]);   // Note: could we perhaps reuse environment?
            if (_actorInterpreter.actionEvaluatePrecondition()) {
                return i;
            } else {
                _actorInterpreter.actionClear();
            }
        }
        return -1;
    }

    /**
     * In SDF, selecting which initializer to fire is already done in preinitialize().
     * @throws IllegalActionException
     */
    public void initialize() throws IllegalActionException {
        _clearInputChannels();
        try {
            if (_actorInterpreter.currentAction() != null) {
                _actorInterpreter.actionStep();
                _actorInterpreter.actionComputeOutputs();
                _actorInterpreter.actionClear();
            }
        }
        catch (Exception e) {
            throw new IllegalActionException("Could not fire initializer in CAL actor '"
                    + _actor.getName() + "': " + e.getMessage());
        }
    }

    private int  _selectInitializer() {
        Action [] actions = _actor.getInitializers();
        for (int i = 0; i < actions.length; i++) {
            _actorInterpreter.actionSetup(actions[i]);   // Note: could we perhaps reuse environment?
            if (_actorInterpreter.actionEvaluatePrecondition()) {
                return i;
            } else {
                _actorInterpreter.actionClear();
            }
        }
        return -1;
    }

    private void  _clearInputChannels() {
        for (Iterator iterator = _inputPorts.values().iterator(); iterator.hasNext();) {
            InputPort inputPort = (InputPort) iterator.next();
            for (int i = 0; i < inputPort.width(); i++) {
                // FIXME (and corresponding in Dataflow)
                DFInputChannel c = (DFInputChannel)inputPort.getChannel(i);
                c.reset();
            }
        }
    }

    public boolean postfire() throws IllegalActionException {
        return false;
    }

    /**
     * Select a firable action among the actions of the actor, if possible.
     *
     * @return True, if an action could be selected.
     * @throws IllegalActionException If an error occurred during the action selection.
     *
     * @see SDF#_selectAction
     */
    public boolean prefire() throws IllegalActionException {
        try {
            _selectAction();
            if (_actorInterpreter.currentAction() != null)
                return true;
            else
                return _ptActor.superPrefire();
        } catch (Exception e) {
            throw new IllegalActionException("Error during action selection in actor '" + _actor.getName() + "': " + e.getMessage());
        }
    }

    public void preinitialize() throws IllegalActionException {
    }

    private static class ActionRateSignature {
        public boolean equals(Object o) {
            if (o == this)
                return true;
            else if (o instanceof ActionRateSignature) {
                return this.inputRates.equals(((ActionRateSignature)o).inputRates) &&
                    this.outputRates.equals(((ActionRateSignature)o).outputRates);
            } else
                return false;
        }
        public int hashCode() {
            return inputRates.hashCode() * outputRates.hashCode();
        }

        public void addInputRate(String portname, int rate) {
            this.inputRates.put(portname, new Integer(rate));
        }
        public void addOutputRate(String portname, int rate) {
            this.outputRates.put(portname, new Integer(rate));
        }

        public Map getInputRates() {
            return inputRates;
        }

        public Map getOutputRates() {
            return outputRates;
        }

        private Map inputRates = new HashMap();
        private Map outputRates = new HashMap();
    }
}
