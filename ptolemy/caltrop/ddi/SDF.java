/*
  @Copyright (c) 2003-2005 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.DFUtilities;

import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.AttributeKeys;
import caltrop.interpreter.ast.Decl;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.InputPattern;
import caltrop.interpreter.ast.OutputExpression;
import caltrop.interpreter.environment.Environment;
import caltrop.interpreter.util.PriorityUtil;

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
   <li> The rates of each action are statically computable, and these
   rates are the same for each action.
   <li> There is at least one unguarded action.
   </ol>

   This plugin also adds attributes containing rate information to the
   ports of the actor.

   @author Christopher Chang, Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class SDF extends Dataflow {

    /**
     * Create an new SDF DDI.
     * @param ptActor The instance of {@link ptolemy.actor.Actor
     * ptolemy.actor.Actor} that the plugin will be associated with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     */
    public SDF(TypedAtomicActor ptActor, Actor actor, Context context,
            Environment env) {
        super(ptActor, actor, context, env);
        _eval = new ExprEvaluator(_context,  _env);
    }

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
        try {
            if (!_atLeastOneUnguardedAction()) {
                throw new RuntimeException("No unguarded action!");
            }

            _actionRates = _computeActionRates(_actions);

            // Check that the rates are actually equal.
            if (!_allEqual(_actionRates)) {
                throw new RuntimeException("Action rates are not equal!");
            }

            _initializerRates = _computeActionRates(_actor.getInitializers());

            if (!_checkInitializerGuards()) {
                throw new RuntimeException("Initializers are guarded!");
            }

            if (PriorityUtil.hasPriorityOrder(_actor)) {
                // FIXME: Strictly speaking, we should allow priority in
                // SDF actors, under certain conditions (i.e the resulting
                // actor should remain an SDF actor, of course). So we
                // need to do the corresponding analysis.  Until then, we
                // simply disallow priority clauses in SDF actors.
                throw new RuntimeException("Has priorities!");
            }
            return true;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Get the name of this DDI.
     * @return "SDF".
     */
    public String getName() {
        return "SDF";
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
        _annotatePortsWithRates(_ptActor.inputPortList(),
                _actionRates[0].getInputRates(),  "tokenConsumptionRate");
        _annotatePortsWithRates(_ptActor.outputPortList(),
                _actionRates[0].getOutputRates(),  "tokenProductionRate");
        int i = _selectInitializer();
        if (i != -1) {
            _annotatePortsWithRates(_ptActor.outputPortList(),
                    _initializerRates[i].getOutputRates(),
                    "tokenInitProduction");
        }
        _ptActor.getDirector().invalidateSchedule();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Annotate the given list of TypedIOPorts with rate parameters
     * according to the given map.
     */
    private void _annotatePortsWithRates(
            List ports, Map rateMap, String varName) {
        for (Iterator iterator = ports.iterator(); iterator.hasNext();) {
            IOPort port = (IOPort) iterator.next();
            try {
                DFUtilities.setIfNotDefined(port, varName,
                        ((Integer) rateMap.get(port.getName())).intValue());
                DFUtilities.showRate(port, true);
            } catch (Exception e) {
                throw new DDIException("Failed to set " + varName +
                        " of port " + port.getFullName());
            }
        }
    }

    // Return true if at least one action does not have a guard.
    private boolean _atLeastOneUnguardedAction() {
        for (int i = 0; i < _actions.length; i++) {
            Action action = _actions[i];
            if (action.getGuards().length == 0)
                return true;
        }
        return false;
    }

    // Return true if initializer guards can be computed statically.
    private boolean _checkInitializerGuards() {
        Action [] initializers = _actor.getInitializers();
        for (int i = 0; i < initializers.length; i++) {
            Action initializer = initializers[i];
            Expression [] guards = initializer.getGuards();
            for (int j = 0; j < guards.length; j++) {
                if (!_isStaticallyComputable(guards[j], initializer))
                    return false;
            }
        }
        return true;
    }

    // For each action in the given set of actions, compute its rate
    // signature.  Throw an Exception if any action has a rate which
    // cannot be statically computed.
    private ActionRateSignature[] _computeActionRates(Action[] actions)
            throws Exception {
        ActionRateSignature[] signatures =
            new ActionRateSignature[actions.length];

        for (int i = 0; i < actions.length; i++) {
            Action action =  actions[i];
            ActionRateSignature ars = new ActionRateSignature();

            InputPattern [] inputPatterns = action.getInputPatterns();
            for (int j = 0; j < inputPatterns.length; j++) {
                InputPattern inputPattern = inputPatterns[j];
                Expression repeatExpr = inputPattern.getRepeatExpr();
                int repeatVal = _computeExpression(repeatExpr, action);
                ars.addInputRate(inputPattern.getPortname(),
                        inputPattern.getVariables().length * repeatVal);
            }

            OutputExpression [] outputexps = action.getOutputExpressions();
            for (int j = 0; j < outputexps.length; j++) {
                OutputExpression outputexp = outputexps[j];
                Expression repeatExpr = outputexp.getRepeatExpr();
                int repeatVal = _computeExpression(repeatExpr, action);
                ars.addOutputRate(outputexp.getPortname(),
                        outputexp.getExpressions().length * repeatVal);
            }
            signatures[i] = ars;
        }
        return signatures;
    }

    // Statically compute the value of the given repeat expression. If
    // this is not possible, throw an exception.  <p>In order for the
    // repeat expression to be statically computable, its value must
    // only depend on global variables or actor parameters. In other
    // words, any free variables in a repeat expression cannot be
    // bound by action state variables, port variables, or actor state
    // variables.
    private int _computeExpression(Expression repeatExpr, Action action)
            throws Exception {
        if (repeatExpr == null) {
            return 1;
        } else {
            if (!_isStaticallyComputable(repeatExpr, action)) {
                throw new Exception(
                        "The expression '" + repeatExpr +
                        "' cannot be statically computed.");
            }

            int value = _context.intValue(_eval.evaluate(repeatExpr));
            return value;
        }
    }

    // Return true if the given expression is statically computable.
    // Assumes free variable annotater has been run on the AST.
    private boolean _isStaticallyComputable(Expression expr, Action action) {
        if (expr == null)
            return true;
        List freeVars = (List) expr.getAttribute(AttributeKeys.KEYFREEVAR);
        for (Iterator iterator = freeVars.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();
            if (_isBoundByPortVar(name, action) ||
                    _isIn(name, action.getDecls()) ||
                    _isIn(name, _actor.getStateVars()))
                return false;
        }
        return true;
    }

    private static boolean _isBoundByPortVar(String name, Action action) {
        InputPattern [] inputPatterns = action.getInputPatterns();
        for (int i = 0; i < inputPatterns.length; i++) {
            if (_isIn(name, inputPatterns[i].getVariables()))
                return true;
        }
        return false;
    }

    private static boolean _isIn(String name, String [] names) {
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                return true;
            }
        }
        return false;
    }

    private static boolean _isIn(String name, Decl [] decls) {
        for (int i = 0; i < decls.length; i++) {
            if (name.equals(decls[i].getName())) {
                return true;
            }
        }
        return false;
    }

    private static boolean _allEqual(Object [] objs) {
        if (objs.length <= 1)
            return true;
        Object standard = objs[0];

        for (int i = 1; i < objs.length; i++) {
            if (!standard.equals(objs[i]))
                return false;
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////

    private ExprEvaluator _eval;
    private ActionRateSignature [] _actionRates;
    private ActionRateSignature [] _initializerRates;

    private static class ActionRateSignature {
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else {
                if (o instanceof ActionRateSignature) {
                    return inputRates.equals(((ActionRateSignature)o)
                            .inputRates) &&
                        outputRates.equals(((ActionRateSignature)o)
                                .outputRates);
                } else {
                    return false;
                }
            }
        }
        public int hashCode() {
            return inputRates.hashCode() * outputRates.hashCode();
        }

        public void addInputRate(String portname, int rate) {
            inputRates.put(portname, new Integer(rate));
        }
        public void addOutputRate(String portname, int rate) {
            outputRates.put(portname, new Integer(rate));
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
