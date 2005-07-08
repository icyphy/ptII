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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.util.DFUtilities;
import ptolemy.caltrop.ddi.util.DataflowWithRatesActorInterpreter;
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

//////////////////////////////////////////////////////////////////////////
//// DataflowWithRates

/**
 A base class that provides support for dataflow models that publish
 external rate information.  This includes SDF (where the rates are
 constant) and DDF (where the rates are published once an action is
 selected.
 
 @author Christopher Chang, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class DataflowWithRates extends Dataflow {
    /**
     * Create an new DataflowWithRates DDI.
     * @param ptActor The instance of {@link ptolemy.actor.Actor
     * ptolemy.actor.Actor} that the plugin will be associated with.
     * @param actor The abstract syntax tree of the CAL source.
     * @param context The context that the plugin will use.
     * @param env The environment that the plugin will use.
     */
    public DataflowWithRates(TypedAtomicActor ptActor, Actor actor,
            Context context, Environment env) {
        super(ptActor, actor, context, env);
        _eval = new ExprEvaluator(_context, _env);
        _actorInterpreter = new DataflowWithRatesActorInterpreter(_actor,
                _context, _env, _inputPorts, _outputPorts);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Annotate the given list of TypedIOPorts with rate parameters
     * according to the given map.
     */
    protected void _annotatePortsWithRates(List ports, Map rateMap,
            String varName) {
        for (Iterator iterator = ports.iterator(); iterator.hasNext();) {
            IOPort port = (IOPort) iterator.next();

            try {
                Integer integerRate = (Integer) rateMap.get(port.getName());
                int rate;
                if (integerRate == null) {
                    rate = 0;
                } else {
                    rate = integerRate.intValue();
                }
                //  System.out.println("publishing rate " + rate
                //        + " for port " + port.getFullName());
                DFUtilities.setIfNotDefined(port, varName, rate);
                DFUtilities.showRate(port, true);
            } catch (Exception e) {
                throw new DDIException("Failed to set " + varName + " of port "
                        + port.getFullName());
            }
        }
    }

    // Return true if at least one action does not have a guard.
    protected boolean _atLeastOneUnguardedAction() {
        for (int i = 0; i < _actions.length; i++) {
            Action action = _actions[i];

            if (action.getGuards().length == 0) {
                return true;
            }
        }

        return false;
    }

    // Return true if initializer guards can be computed statically.
    protected boolean _checkInitializerGuards() {
        Action[] initializers = _actor.getInitializers();

        for (int i = 0; i < initializers.length; i++) {
            Action initializer = initializers[i];
            Expression[] guards = initializer.getGuards();

            for (int j = 0; j < guards.length; j++) {
                if (!_isStaticallyComputable(guards[j], initializer)) {
                    return false;
                }
            }
        }

        return true;
    }

    // For each action in the given set of actions, compute its rate
    // signature.  Throw an Exception if any action has a rate which
    // cannot be computed.
    protected ActionRateSignature[] _computeActionRates(Action[] actions)
            throws Exception {
        ActionRateSignature[] signatures = new ActionRateSignature[actions.length];

        for (int i = 0; i < actions.length; i++) {
            signatures[i] = _computeActionRates(actions[i]);
        }

        return signatures;
    }

    // Compute the rate signature of the given action.  Throw an
    // exception if the rate cannot be computed.
    protected ActionRateSignature _computeActionRates(Action action)
            throws Exception {
        ActionRateSignature signature = new ActionRateSignature();

        InputPattern[] inputPatterns = action.getInputPatterns();

        for (int j = 0; j < inputPatterns.length; j++) {
            InputPattern inputPattern = inputPatterns[j];
            Expression repeatExpr = inputPattern.getRepeatExpr();
            int repeatVal = _computeRepeatExpression(repeatExpr, action);
            signature.addInputRate(inputPattern.getPortname(), inputPattern
                    .getVariables().length
                    * repeatVal);
        }

        OutputExpression[] outputexps = action.getOutputExpressions();

        for (int j = 0; j < outputexps.length; j++) {
            OutputExpression outputexp = outputexps[j];
            Expression repeatExpr = outputexp.getRepeatExpr();
            int repeatVal = _computeRepeatExpression(repeatExpr, action);
            signature.addOutputRate(outputexp.getPortname(), outputexp
                    .getExpressions().length
                    * repeatVal);
        }
        return signature;
    }

    // Statically compute the value of the given repeat expression. If
    // this is not possible, throw an exception.  <p>In order for the
    // repeat expression to be statically computable, its value must
    // only depend on global variables or actor parameters. In other
    // words, any free variables in a repeat expression cannot be
    // bound by action state variables, port variables, or actor state
    // variables.
    private int _computeRepeatExpression(Expression repeatExpr, Action action)
            throws Exception {
        if (repeatExpr == null) {
            return 1;
        } else {
            if (!_isStaticallyComputable(repeatExpr, action)) {
                throw new Exception("The expression '" + repeatExpr
                        + "' cannot be statically computed.");
            }

            int value = _context.intValue(_eval.evaluate(repeatExpr));
            return value;
        }
    }

    // Return true if the given expression is statically computable.
    // Assumes free variable annotater has been run on the AST.
    private boolean _isStaticallyComputable(Expression expr, Action action) {
        if (expr == null) {
            return true;
        }

        List freeVars = (List) expr.getAttribute(AttributeKeys.KEYFREEVAR);

        for (Iterator iterator = freeVars.iterator(); iterator.hasNext();) {
            String name = (String) iterator.next();

            if (_isBoundByPortVar(name, action)
                    || _isIn(name, action.getDecls())
                    || _isIn(name, _actor.getStateVars())) {
                return false;
            }
        }

        return true;
    }

    protected static boolean _isBoundByPortVar(String name, Action action) {
        InputPattern[] inputPatterns = action.getInputPatterns();

        for (int i = 0; i < inputPatterns.length; i++) {
            if (_isIn(name, inputPatterns[i].getVariables())) {
                return true;
            }
        }

        return false;
    }

    private static boolean _isIn(String name, String[] names) {
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                return true;
            }
        }

        return false;
    }

    private static boolean _isIn(String name, Decl[] decls) {
        for (int i = 0; i < decls.length; i++) {
            if (name.equals(decls[i].getName())) {
                return true;
            }
        }

        return false;
    }

    protected static boolean _allEqual(Object[] objs) {
        if (objs.length <= 1) {
            return true;
        }

        Object standard = objs[0];

        for (int i = 1; i < objs.length; i++) {
            if (!standard.equals(objs[i])) {
                return false;
            }
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////
    private ExprEvaluator _eval;

    protected static class ActionRateSignature {
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else {
                if (o instanceof ActionRateSignature) {
                    return inputRates
                            .equals(((ActionRateSignature) o).inputRates)
                            && outputRates
                                    .equals(((ActionRateSignature) o).outputRates);
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
