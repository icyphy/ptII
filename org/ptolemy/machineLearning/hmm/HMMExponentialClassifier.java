/* A sequence classifier for Exponential emission HMMs

Copyright (c) 1998-2014 The Regents of the University of California.
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

 */
package org.ptolemy.machineLearning.hmm;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
<p>This actor performs Maximum-Likelihood classification of the partially-observed
Bayesian Network models. ClassifyObservations is designed to work with <i>
ExpectationMaximization</i>, which provides the Maximum-Likelihood model parameters
from which the observations are assumed to be drawn. The output is an integer array
of labels, representing the maximum-likelihood hidden state sequence of the given
model.

<p>
The user provides a set of parameter estimates as inputs to the model, and
The <i>mean</i>  is a double array input containing the mean estimate and
<i>sigma</i> is a double array input containing standard deviation estimate of
each mixture component. If the <i>modelType</i> is HMM, then an additional input,
<i>transitionMatrix</i> is provided, which is an estimate of the transition matrix
governing the Markovian process representing the hidden state evolution. The <i>prior
</i> input is an estimate of the prior state distribution.

 @author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class HMMExponentialClassifier extends ObservationClassifier {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HMMExponentialClassifier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        lambda = new PortParameter(this, "lambda");
        lambda.setExpression("{0.0,3.0}");
        lambda.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        StringAttribute cardinality = new StringAttribute(lambda.getPort(),
                "_cardinal");
        cardinality.setExpression("SOUTH");

        //_nStates = ((ArrayToken) meanToken).length();
        _nStates = ((ArrayToken) lambda.getToken()).length();
        _lambda = new double[_nStates];
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Rate parameter of the exponential distribution.
     */
    public PortParameter lambda;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HMMExponentialClassifier newObject = (HMMExponentialClassifier) super
                .clone(workspace);
        newObject._lambda = new double[_nStates];
        return newObject;
    }

    /** Consume the inputs and produce the outputs of the FFT filter.
     *  @exception IllegalActionException If a runtime type error occurs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        lambda.update();
        transitionMatrix.update();
        prior.update();

        // update array values and lengths
        _nStates = ((ArrayToken) lambda.getToken()).length();

        for (int i = 0; i < _nStates; i++) {
            _priors[i] = ((DoubleToken) ((ArrayToken) prior.getToken())
                    .getElement(i)).doubleValue();
            _lambda[i] = ((DoubleToken) ((ArrayToken) lambda.getToken())
                    .getElement(i)).doubleValue();
            for (int j = 0; j < _nStates; j++) {
                _transitionMatrixEstimate[i][j] = ((DoubleToken) ((MatrixToken) transitionMatrix
                        .getToken()).getElementAsToken(i, j)).doubleValue();
            }
        }
        if ((_nStates != _lambda.length)
                || (_nStates != _transitionMatrixEstimate.length)) {
            throw new IllegalActionException(this,
                    "Parameter guess vectors need to have the same length.");
        }

        int[] classifyStates = new int[_observations.length];

        classifyStates = classifyHMM(_observations, _priors,
                _transitionMatrixEstimate);

        IntToken[] _outTokenArray = new IntToken[classifyStates.length];
        for (int i = 0; i < classifyStates.length; i++) {
            _outTokenArray[i] = new IntToken(classifyStates[i]);
        }

        output.broadcast(new ArrayToken(BaseType.INT, _outTokenArray));
        likelihood.send( 0, new DoubleToken(_likelihood));
    }

    @Override
    protected double emissionProbability(double y, int hiddenState) {
        double m = _lambda[hiddenState];
        return m * Math.exp(-m * y);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Distribution rate parameter */
    private double[] _lambda;
}
