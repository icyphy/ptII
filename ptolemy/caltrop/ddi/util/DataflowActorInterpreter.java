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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)


*/
package ptolemy.caltrop.ddi.util;

import caltrop.interpreter.Context;
import caltrop.interpreter.ExprEvaluator;
import caltrop.interpreter.InputChannel;
import caltrop.interpreter.InputPort;
import caltrop.interpreter.InterpreterException;
import caltrop.interpreter.OutputChannel;
import caltrop.interpreter.OutputPort;
import caltrop.interpreter.SimpleThunk;
import caltrop.interpreter.StmtEvaluator;
import caltrop.interpreter.ast.Action;
import caltrop.interpreter.ast.Actor;
import caltrop.interpreter.ast.Decl;
import caltrop.interpreter.ast.Expression;
import caltrop.interpreter.ast.InputPattern;
import caltrop.interpreter.ast.OutputExpression;
import caltrop.interpreter.ast.Statement;
import caltrop.interpreter.environment.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The actor interpreter provides an abstract interface to the execution of an actor. It provides a
 * number of operations on the actions of an actor, allowing clients to test them for firability and execute
 * them.
 * <p>
 * An instance of this class maintains some internal state identifying the action it currently works on. It expects
 * its methods to be called in a particular order, viz.<br>
 * {@link #actionSetup(caltrop.interpreter.ast.Action) actionSetup(action)} -- {@link #actionEvaluatePrecondition() actionEvaluatePrecondition()} --
 * {@link #actionStep() actionStep()} -- {@link #actionComputeOutputs() actionComputeOutputs()}<br>
 * During such a sequence, the methods operate on action <em>n</em>, which was selected by the call to {@link #actionSetup(caltrop.interpreter.ast.Action) actionSetup(action)}.
 * At any point in time, the client may call {@link #actionClear() actionClear()}, which deselects the action and deletes the references
 * to the resources used by the action environment created during setup.
 *
 * @author Jörn W. Janneck <janneck@eecs.berkeley.edu>
 * @version $Id$
 * @since Ptolemy II 3.1
 * @see caltrop.interpreter.ast.Actor
 * @see caltrop.interpreter.ast.Action
 * @see #actionSetup
 * @see #actionEvaluatePrecondition
 * @see #actionStep
 * @see #actionComputeOutputs
 * @see #actionClear
 */

public class DataflowActorInterpreter {

    /**
     * Set up the local environment of the specified action. This method is the only way a new action may be
     * set up for execution. If completed successfully, the action environment and the action are stored
     * in the state of this interpreter, and other methods may operate on them.
     *
     * @param action The action to setup.
     */
    public void  actionSetup(Action action) {
        env = null;
        envAction = null;

        final Environment local = actorEnv.newFrame();

        final InputPattern[] inputPatterns = action.getInputPatterns();
        for (int i = 0; i < inputPatterns.length; i++) {
            final InputPattern inputPattern = inputPatterns[i];
            final String [] vars = inputPattern.getVariables();
            final Expression repExpr = inputPattern.getRepeatExpr();

            if (repExpr == null) {
                for (int j = 0; j < vars.length; j++) {
                    final InputChannel channel = ((InputPort)(inputPortMap.get(inputPattern.getPortname()))).getChannel(0); // FIXME
                    local.bind(vars[j], new SingleTokenReaderThunk(channel, j));
                }
            } else {
                SimpleThunk repExprThunk = new SimpleThunk(repExpr, context, local);
                local.bind(new EnvironmentKey(inputPattern.getPortname()), repExprThunk);
                for (int j = 0; j < vars.length; j++) {
                    final InputChannel channel = ((InputPort)(inputPortMap.get(inputPattern.getPortname()))).getChannel(0); // FIXME
                    local.bind(vars[j], new MultipleTokenReaderThunk(channel, j, vars.length, repExprThunk, context));
                }
            }
        }
        final Decl [] decls = action.getDecls();
        for (int i = 0; i < decls.length; i++) {
            final Expression v = decls[i].getInitialValue();
            if (v == null)
                local.bind(decls[i].getName(), null);
            else
                local.bind(decls[i].getName(), new SimpleThunk(v, context, local));
        }

        env = local;
        envAction = action;
    }

    /**
     * Evaluate the preconditions for the action and return its result. If this method returns false, some condition
     * required for the successful completion of the action is not satisfied. This might be an insufficient number of
     * input tokens, or some other condition depending on the value of the tokens or state variables.
     * <p>
     * Note that the converse need not hold---depending on the model of computation, a true return value does not
     * necessarily imply that the action will successfully execute. It may represent an <em>approximation</em> to
     * a complete precondition, in which case the model of computation is not <em>responsible</em>.
     *
     * @return True, if the action precondition was satisfied.
     * @exception caltrop.interpreter.InterpreterException If the evaluation of the guards could not be successfully completed.
     */

    public boolean      actionEvaluatePrecondition() {
        if (envAction == null)
            throw new InterpreterException("DataflowActorInterpreter: Must call actionSetup() before calling actionEvaluatePrecondition().");
        final Action action = envAction;
        final InputPattern[] inputPatterns = action.getInputPatterns();
        for (int i = 0; i < inputPatterns.length; i++) {
            final InputPattern inputPattern = inputPatterns[i];
            // FIXME: handle multiports
            final InputChannel channel = ((InputPort)(inputPortMap.get(inputPattern.getPortname()))).getChannel(0);
            if (inputPattern.getRepeatExpr() == null) {
                if (!channel.hasAvailable(inputPattern.getVariables().length))
                    return false;
            } else {
                int repeatVal = context.intValue(env.get(new EnvironmentKey(inputPattern.getPortname())));
                if (!channel.hasAvailable(inputPattern.getVariables().length * repeatVal))
                    return false;
            }
        }
        final ExprEvaluator eval = new ExprEvaluator(context, env);
        final Expression [] guards = action.getGuards();
        for (int i = 0; i < guards.length; i++) {
            final Object g = eval.evaluate(guards[i]);
            if (! context.booleanValue(g))  {
                return false;
            }
        }
        return true;
    }

    /**
     *  Execute the action body, potentially changing the value of actor state variables and action-scope variables.
     *
     * @exception caltrop.interpreter.InterpreterException If the action body could not be executed successfully.
     */

    public void         actionStep() {
        if (envAction == null)
            throw new InterpreterException("DataflowActorInterpreter: Must call actionSetup() before calling actionStep().");
        // First evaluate the action-level thunks, so that their value will not be affected by subsequent
        // assignments to action or actor variables.
        env.freezeLocal();
        final Action action = envAction;
        final StmtEvaluator eval = new StmtEvaluator(context, env);
        final Statement [] body = action.getBody();
        for (int i = 0; i < body.length; i++) {
            eval.evaluate(body[i]);
        }
    }

    /**
     * Compute the output tokens and send them to the specified (at construction time) output channels.
     *
     * @see DataflowActorInterpreter
     */

    public void         actionComputeOutputs() {
        if (envAction == null)
            throw new InterpreterException("DataflowActorInterpreter: Must call actionSetup() before calling actionComputeOutputs().");
        final Action action = envAction;
        final ExprEvaluator eval = new ExprEvaluator(context, env);
        final OutputExpression [] outputExpressions = action.getOutputExpressions();
        for (int i = 0; i < outputExpressions.length; i++) {
            final OutputExpression outputExpression = outputExpressions[i];
            final Expression [] expressions = outputExpression.getExpressions();
            final Expression repeatExpr = outputExpression.getRepeatExpr();


            final OutputChannel channel = ((OutputPort)(outputPortMap.get(outputExpression.getPortname()))).getChannel(0);

            // FIXME: handle multiports
            if (repeatExpr != null) {
                int repeatValue = context.intValue(eval.evaluate(repeatExpr));
                List [] lists = new List[expressions.length];

                for (int j = 0; j < lists.length; j++) {
                    lists[j] = context.listValue(eval.evaluate(expressions[j]));
                }

                for (int j = 0; j < repeatValue; j++) {
                    for (int k = 0; k < expressions.length; k++) {
                        channel.put(lists[k].get(j));
                    }
                }
            } else {
                for (int j = 0; j < expressions.length; j++) {
                    channel.put(eval.evaluate(expressions[j]));
                }
            }
        }
    }

    /**
     * Compute an output "profile" for the current action. The profile is a mapping from channels to rates.
     * @return Map[ChannelID -> Integer]
     */
    /*public Map actionComputeOutputProfile() {
        if (envAction < 0)
            throw new InterpreterException("DataflowActorInterpreter: Must call actionSetup() before calling actionComputeOutputs().");
        Map profile = new HashMap();
        final Action action = actor.getActions()[envAction];
        final ExprEvaluator eval = new ExprEvaluator(context, env);
        final OutputExpression [] outputExpressions = action.getOutputExpressions();
        for (int i = 0; i < outputExpressions.length; i++) {
            final OutputExpression outputExpression = outputExpressions[i];
            final Expression [] expressions = outputExpression.getExpressions();
            final Expression repeatExpr = outputExpression.getRepeatExpr();

            int repeatValue = 1;

            // FIXME: handle multiports
            if (repeatExpr != null) {
                repeatValue = context.intValue(eval.evaluate(repeatExpr));
            }
            profile.put(new ChannelID(outputExpression.getPortname(), 0),
                    new Integer(repeatValue * expressions.length));
        }
        return profile;
    }*/
    /**
     * Clear action selection. The reference to the environment is cleared, too, allowing the system to reclaim
     * any resources associated with it.
     */

    public void         actionClear() {
        envAction = null;
        env = null;
    }

    /**
     * Return the current action. This is null if none has been selected.
     *
     * @return The current action, null if none.
     */

    public Action          currentAction() {
        return envAction;
    }

    /**
     * Return the number of actions in this actor.
     * @return Number of actions.
     */

    public int          nActions() {
        return actor.getActions().length;
    }

    /**
     * Return the number of initialziers in this actor.
     * @return Number of initializers.
     */

    public int          nInitializers() {
        return actor.getInitializers().length;
    }

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

    public DataflowActorInterpreter(final Actor actor, final Context context, final Environment actorEnv, final Map inputPortMap, final Map outputPortMap) {
        this.actor = actor;
        this.context = context;
        this.actorEnv = actorEnv;
        this.inputPortMap = inputPortMap;
        this.outputPortMap = outputPortMap;
    }

    private Actor           actor;
    private Context         context;
    private Environment     actorEnv;
    private Environment     env = null;
    private Action          envAction = null;

    private Map  inputPortMap;

    public void setOutputPortMap(Map outputPortMap) {
        this.outputPortMap = outputPortMap;
    }

    private Map  outputPortMap;


    /**
     * A single token reader thunk encapsulates the operation of reading a single token from a specified channel.
     * This allows us to defer the reading operation to the time when the token is actually needed.
     */

    private static class SingleTokenReaderThunk implements Environment.VariableContainer {
        public Object value() {
            if (val == this) {
                val = channel.get(index);
                channel = null;         // release ref to channel
            }
            return val;
        }

        public Object value(final Object[] location) {
            throw new InterpreterException("Indices not yet implemented.");  // FIXME
        }

        public void freeze() {
            if (val == this) {
                val = channel.get(index);
                channel = null;
            }
        }

        public SingleTokenReaderThunk(final InputChannel channel, final int index) {
            this.channel = channel;
            this.index = index;
            val = this;             // this is definitely not a legal value for a token
        }

        private InputChannel channel;
        private int     index;
        private Object  val;
    }


    private static class MultipleTokenReaderThunk implements Environment.VariableContainer {
        public Object value() {
            freeze();
            return val;
        }

        public Object value(final Object[] location) {
            throw new InterpreterException("Indices not yet implemented.");  // FIXME
        }

        public void freeze() {
            if (val == this) {
                Object repeatVal = repeatExpr.value();
                int length = context.intValue(repeatVal);
                List tokens = new ArrayList();
                for (int i = 0; i < length; i++) {
                    tokens.add(channel.get(offset + i*period));
                }
                val = context.createList(tokens);
                channel = null;
            }
        }

        public MultipleTokenReaderThunk(InputChannel channel, int offset, int period, SimpleThunk repeatExpr, Context context) {
            this.channel = channel;
            this.offset = offset;
            this.period = period;
            this.repeatExpr = repeatExpr;
            this.context = context;
            this.val = this;
        }

        private InputChannel channel;
        private int     offset;
        private int     period;
        private SimpleThunk repeatExpr;
        private Object  val;
        private Context context;


    }

    private static class EnvironmentKey {
        public EnvironmentKey(Object thingy) {
            this.thingy = thingy;
        }

        public int hashCode() {
            int n = thingy.hashCode();
            return n * n;
        }

        public boolean equals(Object obj) {
            if (obj instanceof EnvironmentKey) {
                return thingy.equals(((EnvironmentKey)obj).thingy);
            } else {
                return false;
            }
        }

        private Object thingy;
    }
}
