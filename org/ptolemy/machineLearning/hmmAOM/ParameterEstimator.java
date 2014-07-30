/* This actor implements a Network Bus.

@Copyright (c) 2010-2014 The Regents of the University of California.
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

package org.ptolemy.machineLearning.hmmAOM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CommunicationAspect;
import ptolemy.actor.CommunicationAspectAttributes;
import ptolemy.actor.CommunicationAspectListener.EventType;
import ptolemy.actor.IOPort;
import ptolemy.actor.IntermediateReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.aspect.AtomicCommunicationAspect;
import ptolemy.actor.sched.FixedPointDirector;
import ptolemy.actor.util.FIFOQueue;
import ptolemy.actor.util.Time;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.lib.Server;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.SignalProcessing;

/** This actor is a {@link CommunicationAspect} that, when its
 *  {@link #sendToken(Receiver, Receiver, Token)} method is called, delays
 *  the delivery of the specified token to the specified receiver
 *  according to a service rule. Specifically, if the actor is
 *  not currently servicing a previous token, then it delivers
 *  the token with a delay given by the <i>serviceTimeMultiplicationFactor</i>
 *  parameter multiplied by the <i>messageLength</i> parameter specified in the port.
 *  If the actor is currently servicing a previous token, then it waits
 *  until it has finished servicing that token (and any other pending
 *  tokens), and then delays for an additional amount given by
 *  <i>serviceTimeMultiplicationFactor</i> * <i>messageLength</i>.
 *  In the default case of the <i>messageLength</i> = 1, the behavior is similar to
 *  the {@link Server} actor.
 *  Tokens are processed in FIFO order.
 *  <p>
 *  To use this communication aspect, drag an instance of this Bus
 *  into the model, and (optionally)
 *  assign it a name. Then, on any input port whose communication is to be
 *  mediated by this instance of Bus, open the configuration dialogue,
 *  select the tab with the name of the bus in the title and select the
 *  <i>enable</i> attribute. The message length is by default set to 1
 *  but can be configured in this tab.
 *  <p>
 *  Several Bus communication aspects can be used in sequence. The order in which
 *  Tokens are sent through Buses depends on the order in which these are
 *  enabled via the DecoratorAttributes.
 *  <p>
 *  This actor is tested in continuous and DE.
 *  @author Patricia Derler, Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (derler)
 *  @Pt.AcceptedRating Red (derler)
 */
public abstract class ParameterEstimator extends AtomicCommunicationAspect {

    /** Construct a Bus with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ParameterEstimator(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _tokens = new FIFOQueue();
        _receiversAndTokensToSendTo = new HashMap<Receiver, Token>();
        _tempReceiverQueue = new FIFOQueue();

        transitionMatrix = new TypedIOPort(this, "transitionMatrix", false,
                true);
        transitionMatrix.setTypeEquals(BaseType.DOUBLE_MATRIX);

        priorEstimates = new TypedIOPort(this, "priorEstimates", false, true);
        priorEstimates.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        StringAttribute cardinality = new StringAttribute(priorEstimates,
                "_cardinal");
        cardinality.setExpression("SOUTH");

        serviceTimeMultiplicationFactor = new Parameter(this,
                "serviceTimeMultiplicationFactor");
        serviceTimeMultiplicationFactor.setTypeEquals(BaseType.DOUBLE);
        serviceTimeMultiplicationFactor.setExpression("false");

        randomizeGuessVectors = new Parameter(this, "randomizeGuessVectors");
        randomizeGuessVectors.setTypeEquals(BaseType.BOOLEAN);
        randomizeGuessVectors.setExpression("false");

        likelihoodThreshold = new Parameter(this, "likelihoodThreshold");
        likelihoodThreshold.setExpression("1E-4");
        likelihoodThreshold.setTypeEquals(BaseType.DOUBLE);

        maxIterations = new Parameter(this, "maxIterations");
        maxIterations.setExpression("10");
        maxIterations.setTypeEquals(BaseType.INT);

        A0 = new Parameter(this, "A0");
        A0.setExpression("[0.5, 0.5; 0.5, 0.5]");
        A0.setTypeEquals(BaseType.DOUBLE_MATRIX);
        A0.setDisplayName("Transition Probability Matrix");

        priorDistribution = new Parameter(this, "priorDistribution");
        priorDistribution.setExpression("{0.5,0.5}");
        priorDistribution.setTypeEquals(new ArrayType(BaseType.DOUBLE));

        nStates = new Parameter(this, "nStates");
        nStates.setExpression("2");
        nStates.setTypeEquals(BaseType.INT);
        nStates.setDisplayName("numberOfStates");

        batchSize = new Parameter(this, "batchSize");
        batchSize.setExpression("200");
        batchSize.setTypeEquals(BaseType.INT);

        _initializeArrays();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The service time for the default messageLength of 1. This is a
     *  double with default 0.1.  It is required to be positive.
     */
    public Parameter serviceTimeMultiplicationFactor;

    /** The user-provided initial guess of the transition probability matrix. */
    public Parameter A0;

    /** The user-provided threshold on the minimum desired improvement
     * on likelihood per iteration.
     */
    public Parameter likelihoodThreshold;

    /** The user-provided maximum number of allowed iterations of the
     * Alpha-Beta Recursion.
     */
    public Parameter maxIterations;

    /** The user-provided batch-size to be considered in the estimation. */
    public Parameter batchSize;

    /** Number of states of the HMM. */
    public Parameter nStates;

    /** Boolean that determines whether or not to randomize input
     * guess vectors.
     */
    public Parameter randomizeGuessVectors;

    /** The user-provided initial guess on the prior probability
     * distribution.
     */
    public Parameter priorDistribution;

    /** The input port that provides the sample observations.
     */
    public TypedIOPort input;

    /** The vector estimate for the prior distribution on the set of
     * states.
     */
    public TypedIOPort priorEstimates;

    /** The transition matrix estimate obtained by iterating over the
     * observation set.
     */
    public TypedIOPort transitionMatrix;

    /** If the attribute is <i>serviceTime</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == serviceTimeMultiplicationFactor) {
            double value = ((DoubleToken) serviceTimeMultiplicationFactor
                    .getToken()).doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
        } else if (attribute == A0) {
            int nRow = ((MatrixToken) A0.getToken()).getRowCount();
            int nCol = ((MatrixToken) A0.getToken()).getColumnCount();
            if (nRow != nCol) {
                throw new IllegalActionException(this,
                        "Transition Probability Matrix must be a square matrix.");
            } else {
                _transitionMatrix = new double[nRow][nCol];
                _A0 = new double[nRow][nCol];
                for (int i = 0; i < nRow; i++) {
                    for (int j = 0; j < nCol; j++) {
                        _transitionMatrix[i][j] = ((DoubleToken) ((MatrixToken) A0
                                .getToken()).getElementAsToken(i, j))
                                .doubleValue();
                        if (_transitionMatrix[i][j] < 0.0) {
                            throw new IllegalActionException(this,
                                    "Transition probabilities cannot be negative.");
                        }
                    }
                }
                _A0 = _transitionMatrix;
            }
        } else if (attribute == batchSize) {
            int tb = ((IntToken) batchSize.getToken()).intValue();
            if (tb <= 0) {
                throw new IllegalActionException(this,
                        "Batch Size must be a positive integer.");
            }
            _batchSize = tb;
        } else if (attribute == priorDistribution) {
            int nS = ((ArrayToken) priorDistribution.getToken()).length();
            double[] tempPriors = new double[nS];
            double sum = 0.0;

            for (int i = 0; i < nS; i++) {
                tempPriors[i] = ((DoubleToken) ((ArrayToken) priorDistribution
                        .getToken()).getElement(i)).doubleValue();
                if (tempPriors[i] < 0.0) {
                    throw new IllegalActionException(this,
                            "Priors must be non-negative.");
                }
                sum += tempPriors[i];
            }
            // check if priors is a valid probability vector.
            if (!SignalProcessing.close(sum, 1.0)) {
                throw new IllegalActionException(this, "Priors sum to " + sum
                        + " . The sum must be equal to 1.0.");
            } else {
                _priors = tempPriors;
            }
        } else if (attribute == maxIterations) {
            if (((IntToken) maxIterations.getToken()).intValue() <= 0) {
                throw new IllegalActionException(this,
                        "Number of iterations must be greater than zero.");
            } else {
                _nIterations = ((IntToken) maxIterations.getToken()).intValue();
            }
        } else if (attribute == likelihoodThreshold) {
            double threshold = ((DoubleToken) likelihoodThreshold.getToken())
                    .doubleValue();
            if (threshold > 0.0) {
                _likelihoodThreshold = threshold;
            } else {
                throw new IllegalActionException(this,
                        "Likelihood threshold must be positive.");
            }
        } else if (attribute == nStates) {

            int nS = ((IntToken) nStates.getToken()).intValue();
            if (nS > 0) {
                _nStates = nS;
            } else {
                throw new IllegalActionException(this,
                        "Number of states must be a positive integer");
            }

        } else if (attribute == randomizeGuessVectors) {
            boolean randomize = ((BooleanToken) randomizeGuessVectors
                    .getToken()).booleanValue();
            _randomize = randomize;
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the decorated attributes for the target NamedObj.
     *  If the specified target is not an Actor, return null.
     *  @param target The NamedObj that will be decorated.
     *  @return The decorated attributes for the target NamedObj, or
     *   null if the specified target is not an Actor.
     */
    @Override
    public DecoratorAttributes createDecoratorAttributes(NamedObj target) {
        if (target instanceof IOPort && ((IOPort) target).isInput()) {
            try {
                return new BusAttributes(target, this);
            } catch (KernelException ex) {
                // This should not occur.
                throw new InternalErrorException(ex);
            }
        } else {
            return null;
        }
    }

    /** Create an intermediate receiver that wraps a given receiver.
     *  @param receiver The receiver that is being wrapped.
     *  @return A new intermediate receiver.
     * @exception IllegalActionException Thrown if Bus is used in container different from the container of the bus.
     */
    @Override
    public IntermediateReceiver createIntermediateReceiver(Receiver receiver)
            throws IllegalActionException {
        // Only allow use of Bus on Ports in the same hierarchy level.
        if (receiver.getContainer().getContainer().getContainer() != this
                .getContainer()) {
            throw new IllegalActionException(
                    "This Bus can only be used on Ports in the same"
                            + " container as the Bus.");
        }
        IntermediateReceiver intermediateReceiver = new IntermediateReceiver(
                this, receiver);
        return intermediateReceiver;
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new Bus.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ParameterEstimator newObject = (ParameterEstimator) super
                .clone(workspace);
        newObject._tokens = new FIFOQueue();
        newObject._receiversAndTokensToSendTo = new HashMap<Receiver, Token>();
        newObject._tempReceiverQueue = new FIFOQueue();
        newObject._parameters = new HashMap<IOPort, List<Attribute>>();

        newObject._nextReceiver = null;

        newObject._likelihood = 0.0;
        newObject._transitionMatrix = new double[_nStates][_nStates];
        newObject._A0 = new double[_nStates][_nStates];
        newObject._priors = new double[_nStates];
        return newObject;
    }

    /** Send first token in the queue to the target receiver.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Time currentTime = getDirector().getModelTime();
        // In a continuous domain this actor could be fired before any token has
        // been received; _nextTimeFree could be null.
        // for the hmm concept, this will be: if the number of tokens received == number of batch size,
        // output parameter estimates. and for all other tokens, just pass through.
        if (true) {
            Object[] output = (Object[]) _tokens.get(0);
            Receiver receiver = (Receiver) output[0];
            Token token = (Token) output[1];

            if ((_observedTokens.keySet()).contains(receiver.toString())) {
                List tokensForReceiver = _observedTokens.get(receiver
                        .toString());
                tokensForReceiver.add(((DoubleToken) token).doubleValue());
                List newTokens = new LinkedList<Double>();
                newTokens.addAll(tokensForReceiver);
                _observedTokens.put(receiver.toString(), newTokens);
            } else {
                List tokensForReceiver = new LinkedList<Double>();
                tokensForReceiver.add(((DoubleToken) token).doubleValue());
                _observedTokens.put(receiver.toString(), tokensForReceiver);
            }
            _sendToReceiver(receiver, token);

            if (_debugging) {
                _debug("At time " + currentTime + ", completing send to "
                        + receiver.getContainer().getFullName() + ": " + token);
            }
        }

    }

    /** Initialize the actor.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _receiversAndTokensToSendTo.clear();
        _tempReceiverQueue.clear();
        _tokens.clear();
        _tokenCount = 0;
        _nextReceiver = null;
    }

    /** If there are still tokens in the queue and a token has been produced in the fire,
     *  schedule a refiring.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        getDirector().getModelTime();

        // If a token was actually sent to a delegated receiver
        // by the fire() method, then remove that token from
        // the queue and, if there are still tokens in the queue,
        // request another firing at the time those tokens should
        // be delivered to the delegated receiver.
        if (_tokens.size() > 0) {
            // Discard the token that was sent to the output in fire().
            _tokens.take();
            _tokenCount--;
            sendCommunicationEvent(null, 0, _tokenCount, EventType.SENT);
        }
        // If sendToken() was called in the current iteration,
        // then append the token to the queue. If this is the
        // only token on the queue, then request a firing at
        // the time that token should be delivered to the
        // delegated receiver.
        if ((getDirector() instanceof FixedPointDirector)
                && _receiversAndTokensToSendTo != null) {
            while (_tempReceiverQueue.size() > 0) {
                Receiver receiver = (Receiver) _tempReceiverQueue.take();
                Token token = _receiversAndTokensToSendTo.get(receiver);
                if (token != null) {
                    _tokens.put(new Object[] { receiver, token });
                    _tokenCount++;
                    sendCommunicationEvent((Actor) receiver.getContainer()
                            .getContainer(), 0, _tokenCount, EventType.RECEIVED);
                }
            }
        }
        if (_tokens.size() > 0) {
            _scheduleRefire();
        }
        _receiversAndTokensToSendTo.clear();
        return super.postfire();
    }

    /** Initiate a send of the specified token to the specified
     *  receiver. This method will schedule a refiring of this actor
     *  if there is not one already scheduled.
     *  @param source Sender of the token.
     *  @param receiver The receiver to send to.
     *  @param token The token to send.
     *  @exception IllegalActionException If the refiring request fails.
     */
    @Override
    public void sendToken(Receiver source, Receiver receiver, Token token)
            throws IllegalActionException {
        // If the token is null, then this means there is not actually
        // something to send. Do not take up bus resources for this.
        // FIXME: Is this the right thing to do?
        // Presumably, this is an issue with the Continuous domain.
        if (getDirector() instanceof DEDirector && token == null) {
            return;
        }
        getDirector().getModelTime();
        // Send "absent" if there is nothing to send.
        if (_tokens.size() == 0 || receiver != _nextReceiver) {
            // At the current time, there is no token to send.
            // At least in the Continuous domain, we need to make sure
            // the delegated receiver knows this so that it becomes
            // known and absent.
            if (getDirector() instanceof FixedPointDirector) {
                receiver.put(null);
            }
        }

        // If previously in the current iteration we have
        // sent a token, then we require the token to have the
        // same value. Thus, this Bus can be used only in domains
        // that either call fire() at most once per iteration,
        // or domains that have a fixed-point semantics.
        Token tokenToSend = _receiversAndTokensToSendTo.get(receiver);
        if (tokenToSend != null) {
            if (!tokenToSend.equals(token)) {
                throw new IllegalActionException(this, receiver.getContainer(),
                        "Previously initiated a transmission with value "
                                + tokenToSend
                                + ", but now trying to send value " + token
                                + " in the same iteration.");
            }
        } else {
            // In the Continuous domain, this actor gets fired whether tokens are available
            // or not. In the DE domain we need to schedule a refiring.
            if (token != null) {
                _receiversAndTokensToSendTo.put(receiver, token);
                _tempReceiverQueue.put(receiver);

                if (!(getDirector() instanceof FixedPointDirector)) {
                    _tokens.put(new Object[] { receiver, token });
                    _tokenCount++;
                    sendCommunicationEvent((Actor) source.getContainer()
                            .getContainer(), 0, _tokenCount, EventType.RECEIVED);
                    if (_tokens.size() == 1) { // no refiring has been scheduled
                        _scheduleRefire();
                    }
                    _receiversAndTokensToSendTo.clear();
                }
            }
        }

        if (_debugging) {
            _debug("At time " + getDirector().getModelTime()
                    + ", initiating send to "
                    + receiver.getContainer().getFullName() + ": " + token);
        }
    }

    /** Override the base class to first set the container, then establish
     *  a connection with any decorated objects it finds in scope in the new
     *  container.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     *  @see #getContainer()
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);
        if (container != null) {
            List<NamedObj> decoratedObjects = decoratedObjects();
            for (NamedObj decoratedObject : decoratedObjects) {
                decoratedObject.getDecoratorAttributes(this);

            }
        }
    }

    /**
     * Nothing to do.
     */
    @Override
    public void reset() {
    }

    protected boolean _EMParameterEstimation() {
        // get FIRST stream for now

        boolean success = false;
        _initializeEMParameters();

        for (int iterations = 0; iterations < _nIterations; iterations++) {

            _iterateEM();
            success = _checkForConvergence(iterations);
            // randomization not allowed and convergence was not achieved
            if (!_randomize && !success) {
                break;
            }
            _updateEstimates();

            // check convergence within likelihoodThreshold
            if (Math.abs(likelihood - _likelihood) < _likelihoodThreshold) {
                break;
            } else {
                _likelihood = likelihood;
            }
        }

        return success;

    }

    protected abstract double emissionProbability(double y, int hiddenState);

    protected void _initializeArrays() throws IllegalActionException {

        //_observations = new double[_observationLength];
        // infer the number of states from the mean array
        _likelihood = 0.0;
        _nStates = ((IntToken) nStates.getToken()).intValue();
        _transitionMatrix = new double[_nStates][_nStates];
        _A0 = new double[_nStates][_nStates];
        _priors = new double[_nStates];
        _observedTokens = new HashMap<String, List<Double>>();
    }

    protected abstract void _initializeEMParameters();

    protected abstract void _iterateEM();

    protected abstract boolean _checkForConvergence(int i);

    ///////////////////////////////////////////////////////////////////
    //                          public variables                     //

    /** Schedule a refiring of the actor.
     *  @exception IllegalActionException Thrown if the actor cannot be rescheduled
     */
    protected void _scheduleRefire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        _nextReceiver = (Receiver) ((Object[]) _tokens.get(0))[0];
        //IOPort port = _nextReceiver.getContainer();
        _fireAt(currentTime);
    }

    protected abstract void _updateEstimates();

    /* Java implementation of the Baum-Welch algorithm (Alpha-Beta Recursion) for parameter estimation
     * and cluster assignment. This method uses normalized alpha values for computing the conditional
     * probabilities of input sequences, to ensure numerical stability. SEt nCategories to zero for
     * continuous distribution types */
    protected HashMap HMMAlphaBetaRecursion(double[] y, double[][] A,
            double[] prior, int nCategories)

    {
        boolean multinomial = (nCategories > 0) ? true : false;
        int nStates = _nStates;
        int nObservations = y.length;

        double[][] alphas = new double[nObservations][nStates];
        double[][] gamma = new double[nObservations][nStates];
        double[][][] xi = new double[nObservations - 1][nStates][nStates];

        double[][] A_hat = new double[nStates][nStates];
        double[] mu_hat = new double[nStates];
        double[] s_hat = new double[nStates];
        double[] pi_hat = new double[nStates];
        double[][] eta_hat = new double[nStates][nCategories];

        double[] alphaNormalizers = new double[nObservations];
        double alphaSum = 0;
        for (int t = 0; t < y.length; t++) {
            alphaSum = 0;
            for (int i = 0; i < nStates; i++) {
                alphas[t][i] = 0;
                if (t == 0) {
                    alphas[t][i] = prior[i] * emissionProbability(y[t], i);
                } else {
                    for (int qt = 0; qt < nStates; qt++) {
                        alphas[t][i] += A[qt][i] * emissionProbability(y[t], i)
                                * alphas[t - 1][qt];
                    }
                }
                alphaSum += alphas[t][i];
            }
            // alpha normalization
            for (int i = 0; i < nStates; i++) {
                alphas[t][i] /= alphaSum;
                alphaNormalizers[t] = alphaSum;
            }
        }
        for (int t = y.length - 1; t >= 0; t--) {
            for (int qt = 0; qt < nStates; qt++) {
                if (t == y.length - 1) {
                    gamma[t][qt] = alphas[t][qt];
                } else {
                    gamma[t][qt] = 0;
                    for (int qtp = 0; qtp < nStates; qtp++) {
                        double alphasum = 0;
                        for (int j = 0; j < nStates; j++) {
                            alphasum += alphas[t][j] * A[j][qtp];
                        }
                        gamma[t][qt] += (alphas[t][qt] * A[qt][qtp] * gamma[t + 1][qtp])
                                / alphasum;
                    }
                }
            }
        }

        //next, calculate the xis ( for the transition matrix)
        for (int next = 0; next < nStates; next++) {
            for (int now = 0; now < nStates; now++) {
                for (int t = 0; t < (y.length - 1); t++) {
                    // gamma t or t+1? alphas t or t+1?
                    if (alphas[t + 1][next] == 0) {
                        xi[t][now][next] = 0;
                    } else {
                        xi[t][now][next] = alphas[t][now]
                                * emissionProbability(y[t + 1], next)
                                * gamma[t + 1][next] * A[now][next]
                                        / alphas[t + 1][next]; // MJ Eqn (11.45)
                    }
                    A_hat[now][next] += xi[t][now][next];
                }
            }
            mu_hat[next] = 0;
            s_hat[next] = 0;
        }
        // Normalize A
        double[] rowsum = new double[nStates];
        double[] gammasum = new double[nStates];
        for (int i = 0; i < nStates; i++) {
            rowsum[i] = 0;
            for (int j = 0; j < nStates; j++) {
                rowsum[i] += A_hat[i][j];
            }
            for (int j = 0; j < nStates; j++) {
                A_hat[i][j] /= rowsum[i];
            }
            gammasum[i] = 0.0;
        }
        for (int j = 0; j < nStates; j++) {
            gammasum[j] = 0.0;
            for (int t = 0; t < y.length; t++) {
                gammasum[j] += gamma[t][j];
                mu_hat[j] += gamma[t][j] * y[t];
            }
            mu_hat[j] = mu_hat[j] / gammasum[j];
            for (int t = 0; t < y.length; t++) {
                s_hat[j] += (gamma[t][j] * Math.pow((y[t] - mu_hat[j]), 2));
            }
            s_hat[j] = Math.sqrt(s_hat[j] / gammasum[j]);
            // prior probabilities updated
            pi_hat[j] = gamma[0][j];
        }
        // labels for the multinomial setting
        if (multinomial) {
            for (int i = 0; i < nStates; i++) {
                for (int j = 0; j < nCategories; j++) {
                    for (int t = 0; t < y.length; t++) {
                        eta_hat[i][j] += gamma[t][i] * ((y[t] == j) ? 1 : 0);
                    }
                    eta_hat[i][j] /= gammasum[i]; //normalize for gammas
                }
            }
        }
        // do hidden state sequence estimation to compute the log-likelihood, given the current
        // parameter estimates
        int[] clusterAssignments = new int[y.length];
        for (int t = 0; t < y.length; t++) {
            int maxState = 0;
            for (int j = 1; j < nStates; j++) {
                if (gamma[t][j] > gamma[t][maxState]) {
                    maxState = j;
                }
            }
            clusterAssignments[t] = maxState;
        }
        // compute the log-likelihood P(Y|\theta), where \theta is the set of parameter estimates
        // for the HMM.

        double logLikelihood = 0.0;
        for (int t = 0; t < _observations.length - 1; t++) {
            logLikelihood += emissionProbability(y[t], clusterAssignments[t]);
            logLikelihood += A_hat[clusterAssignments[t]][clusterAssignments[t + 1]];
        }
        // add the emission probability at final time value
        logLikelihood += emissionProbability(y[_observations.length - 1],
                clusterAssignments[_observations.length - 1]);

        HashMap estimates = new HashMap();

        estimates.put("mu_hat", mu_hat);
        estimates.put("s_hat", s_hat);
        estimates.put("gamma", gamma); //this will not be needed for most of the distributions.
        estimates.put("A_hat", A_hat);
        estimates.put("pi_hat", pi_hat);
        estimates.put("eta_hat", eta_hat);
        estimates.put("likelihood", logLikelihood);
        return estimates;
    }

    protected HashMap HMMAlphaBetaRecursionNonNormalized(double[] y,
            double[][] A, double[] prior, int unused) {
        int nStates = _nStates;
        int nObservations = y.length;

        int nCategories = 3; // FIXME

        double[][] alphas = new double[nObservations][nStates];
        double[][] betas = new double[nObservations][nStates];
        double[] Py = new double[nObservations];
        double[][] gamma = new double[nObservations][nStates];
        double[][][] xi = new double[nObservations - 1][nStates][nStates];

        double[][] A_hat = new double[nStates][nStates];
        double[] mu_hat = new double[nStates];
        double[] s_hat = new double[nStates];
        double[] pi_hat = new double[nStates];
        double[][] eta_hat = new double[nStates][nCategories];

        for (int t = 0; t < y.length; t++) {
            for (int i = 0; i < nStates; i++) {
                alphas[t][i] = 0;
                if (t == 0) {
                    alphas[t][i] = prior[i] * emissionProbability(y[t], i);
                } else {
                    for (int qt = 0; qt < nStates; qt++) {
                        alphas[t][i] += A[qt][i] * emissionProbability(y[t], i)
                                * alphas[t - 1][qt];
                    }
                }
            }
        }

        for (int t = y.length - 1; t >= 0; t--) {
            // initialize py at time t
            Py[t] = 0;
            for (int i = 0; i < nStates; i++) {
                gamma[t][i] = 0.0;
                betas[t][i] = 0.0;
                if (t == y.length - 1) {
                    betas[t][i] = 1;
                } else {
                    // reverse-time recursion  (do this recursively later)
                    for (int qtp = 0; qtp < nStates; qtp++) {
                        betas[t][i] += A[i][qtp]
                                * emissionProbability(y[t + 1], qtp)
                                * betas[t + 1][qtp];
                    }
                }
                Py[t] += alphas[t][i] * betas[t][i];
            }
            for (int i = 0; i < nStates; i++) {
                gamma[t][i] += alphas[t][i] * betas[t][i] / Py[t];
            }
        }

        //next, calculate the xis ( for the transition matrix)
        for (int next = 0; next < nStates; next++) {
            for (int now = 0; now < nStates; now++) {
                for (int t = 0; t < (y.length - 1); t++) {
                    // gamma t or t+1? alphas t or t+1?
                    if (alphas[t + 1][next] == 0) {
                        xi[t][now][next] = 0;
                    } else {
                        xi[t][now][next] = alphas[t][now]
                                * emissionProbability(y[t + 1], next)
                                * gamma[t + 1][next] * A[now][next]
                                        / alphas[t + 1][next];
                    }
                    A_hat[now][next] += xi[t][now][next];
                }

            }
            mu_hat[next] = 0;
            s_hat[next] = 0;
        }
        // Normalize A
        double[] rowsum = new double[nStates];
        double[] gammasum = new double[nStates];
        for (int i = 0; i < nStates; i++) {

            rowsum[i] = 0;
            for (int j = 0; j < nStates; j++) {
                rowsum[i] += A_hat[i][j];
            }
            for (int j = 0; j < nStates; j++) {
                A_hat[i][j] /= rowsum[i];
            }
            gammasum[i] = 0.0;
        }

        for (int j = 0; j < nStates; j++) {
            gammasum[j] = 0.0;
            for (int t = 0; t < y.length; t++) {
                gammasum[j] += gamma[t][j];
                mu_hat[j] += gamma[t][j] * y[t];
            }
            mu_hat[j] = mu_hat[j] / gammasum[j];

            for (int t = 0; t < y.length; t++) {
                s_hat[j] += (gamma[t][j] * Math.pow((y[t] - mu_hat[j]), 2));
            }
            s_hat[j] = Math.sqrt(s_hat[j] / gammasum[j]);
            // prior probabilities updated
            pi_hat[j] = gamma[0][j];
        }

        for (int i = 0; i < nStates; i++) {
            for (int j = 0; j < nCategories; j++) {
                for (int t = 0; t < y.length; t++) {
                    eta_hat[i][j] += gamma[t][i] * ((y[t] == j) ? 1 : 0);
                }
                eta_hat[i][j] /= gammasum[i]; //normalize for gammas
            }
        }

        // do hidden state sequence estimation to compute the log-likelihood, given the current
        // parameter estimates
        int[] clusterAssignments = new int[y.length];
        for (int t = 0; t < y.length; t++) {
            int maxState = 0;
            for (int j = 1; j < nStates; j++) {
                if (gamma[t][j] > gamma[t][maxState]) {
                    maxState = j;
                }
            }
            clusterAssignments[t] = maxState;
        }
        // compute the log-likelihood P(Y|\theta), where \theta is the set of parameter estimates
        // for the HMM.

        double logLikelihood = 0.0;

        for (int t = 0; t < _observations.length - 1; t++) {
            logLikelihood += emissionProbability(y[t], clusterAssignments[t]);
            logLikelihood += A_hat[clusterAssignments[t]][clusterAssignments[t + 1]];
        }
        // add the emission probability at final time value
        logLikelihood += emissionProbability(y[_observations.length - 1],
                clusterAssignments[_observations.length - 1]);
        // display the log-likelihood at this iteration
        //System.out.println(logLikelihood);

        HashMap estimates = new HashMap();

        estimates.put("mu_hat", mu_hat);
        estimates.put("s_hat", s_hat);
        estimates.put("gamma", gamma); //this will not be needed for most of the distributions.
        estimates.put("A_hat", A_hat);
        estimates.put("pi_hat", pi_hat);
        estimates.put("eta_hat", eta_hat);
        estimates.put("likelihood", logLikelihood);

        return estimates;

    }

    ///////////////////////////////////////////////////////////////////
    //                           private variables                   //

    /* User-defined initial guess array for the state transition matrix*/
    protected double[][] _A0;

    /* User-defined batch size*/
    protected int _batchSize;
    /* likelihood value of the observations given the current estimates L(x1,....xT | \theta_p)*/
    protected double _likelihood;

    protected double _likelihoodThreshold;

    /* User-defined number of iterations of the alpha-beta recursion*/
    protected int _nIterations;

    /* Number of hidden states in the model*/
    protected int _nStates;

    /* Observation array*/
    protected double[] _observations;

    protected HashMap<String, List<Double>> _observedTokens;

    /* Prior distribution on hidden states*/
    protected double[] _priors;

    /* The prior estimates used in the EM iterations*/
    protected double[] _priorIn;

    /* randomize the initial guess vectors or not*/
    protected boolean _randomize;
    /* Initial guess array for the state transition matrix for the Alpha-Beta Recursion*/
    protected double[][] _transitionMatrix;

    HashMap newEstimates;

    double likelihood;

    /** Next receiver to which the next token to be sent is destined. */
    private Receiver _nextReceiver;

    /** Map of receivers and tokens to which the token provided via
     *  sendToken() should be sent to. This is used with FixedPointDirectors.
     */
    private HashMap<Receiver, Token> _receiversAndTokensToSendTo;

    /** During the fix point iteration keep track of the order of tokens sent to
     *  receivers. The tokens are stored in _receiversAndTokensToSendTo.
     */
    private FIFOQueue _tempReceiverQueue;

    /** Tokens stored for processing. This is used with the DE Director. */
    private FIFOQueue _tokens;

    public static class BusAttributes extends CommunicationAspectAttributes {

        /** Constructor to use when editing a model.
         *  @param target The object being decorated.
         *  @param decorator The decorator.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public BusAttributes(NamedObj target,
                AtomicCommunicationAspect decorator)
                        throws IllegalActionException, NameDuplicationException {
            super(target, decorator);
        }

        /** Constructor to use when parsing a MoML file.
         *  @param target The object being decorated.
         *  @param name The name of this attribute.
         *  @exception IllegalActionException If the superclass throws it.
         *  @exception NameDuplicationException If the superclass throws it.
         */
        public BusAttributes(NamedObj target, String name)
                throws IllegalActionException, NameDuplicationException {
            super(target, name);
        }

    }

}
