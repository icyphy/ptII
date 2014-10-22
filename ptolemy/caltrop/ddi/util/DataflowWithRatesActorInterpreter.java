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
package ptolemy.caltrop.ddi.util;

import java.util.Map;

import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.InputChannel;
import caltrop.interpreter.InputPort;
import caltrop.interpreter.InterpreterException;
import caltrop.interpreter.StmtEvaluator;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.InputPattern;
import caltrop.interpreter.ast.Statement;
import caltrop.interpreter.environment.Environment;

//////////////////////////////////////////////////////////////////////////
//// DataFlowWithRatesActorInterpreter

/**
 This class extends the DataflowActorInterpreter, overriding the
 checking of preconditions to assume that input tokens are present.
 This interpreter is assumed to operate in a DDI where token rates
 are published, such as SDF and DDF.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see caltrop.interpreter.ast.Actor
 @see caltrop.interpreter.ast.Action
 @see #actionSetup
 @see #actionEvaluatePrecondition
 @see #actionStep
 @see #actionComputeOutputs
 @see #actionClear
 */
public class DataflowWithRatesActorInterpreter extends DataflowActorInterpreter {
    /**
     * Defines a new actor interpreter for the specified actor.
     *
     * @param actor The actor.
     * @param context The interpretation context.
     * @param actorEnv  The global environment.
     * @param inputPortMap Map from input port names to channels.
     * @param outputPortMap Map from output port names to channels.
     * @see caltrop.interpreter.InputChannel
     * @see caltrop.interpreter.OutputChannel
     */
    public DataflowWithRatesActorInterpreter(final Actor actor,
            final Context context, final Environment actorEnv,
            final Map inputPortMap, final Map outputPortMap) {
        super(actor, context, actorEnv, inputPortMap, outputPortMap);
    }

    /**
     * Evaluate the preconditions for the action and return its
     * result. If this method returns false, some condition required
     * for the successful completion of the action is not
     * satisfied.
     *
     * @return True, if the action precondition was satisfied.
     * @exception caltrop.interpreter.InterpreterException If the
     * evaluation of the guards could not be successfully completed.
     */
    @Override
    public boolean actionEvaluatePrecondition() {
        if (envAction == null) {
            throw new InterpreterException(
                    "DataflowActorInterpreter: Must call actionSetup() "
                            + "before calling actionEvaluatePrecondition().");
        }

        final Action action = envAction;

        final ExprEvaluator eval = new ExprEvaluator(context, env);
        final Expression[] guards = action.getGuards();

        for (Expression guard : guards) {
            final Object g = eval.evaluate(guard);

            if (!context.booleanValue(g)) {
                // System.out.println("guard not satisfied:" + guards[i]);
                return false;
            }
        }

        return true;
    }

    /**
     *  Execute the action body, potentially changing the value of
     *  actor state variables and action-scope variables.
     *
     * @exception caltrop.interpreter.InterpreterException If the
     * action body could not be executed successfully.
     */
    @Override
    public void actionStep() {
        if (envAction == null) {
            throw new InterpreterException(
                    "DataflowActorInterpreter: Must call actionSetup() "
                            + "before calling actionStep().");
        }

        // First evaluate the action-level thunks, so that their value
        // will not be affected by subsequent assignments to action
        // or actor variables.
        env.freezeLocal();

        final Action action = envAction;

        final InputPattern[] inputPatterns = action.getInputPatterns();

        for (InputPattern inputPattern2 : inputPatterns) {
            final InputPattern inputPattern = inputPattern2;

            // FIXME: handle multiports
            final InputChannel channel = ((InputPort) inputPortMap
                    .get(inputPattern.getPortname())).getChannel(0);

            if (inputPattern.getRepeatExpr() == null) {
                if (!channel.hasAvailable(inputPattern.getVariables().length)) {
                    throw new InterpreterException("Not enough inputs:"
                            + inputPattern.getVariables().length);
                }
            } else {
                int repeatVal = context.intValue(env.get(new EnvironmentKey(
                        inputPattern.getPortname())));

                if (!channel.hasAvailable(inputPattern.getVariables().length
                        * repeatVal)) {
                    throw new InterpreterException(
                            "Not enough repeated inputs:"
                                    + inputPattern.getVariables().length
                                    * repeatVal);
                }
            }
        }

        final StmtEvaluator eval = new StmtEvaluator(context, env);
        final Statement[] body = action.getBody();

        for (Statement element : body) {
            eval.evaluate(element);
        }
    }
}
