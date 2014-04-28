
/* Parameter Estimation for Explicit-Duration Hidden Markov Models.

Copyright (c) 1998-2013 The Regents of the University of California.
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
package org.ptolemy.machineLearning.hsmm;

import java.util.HashMap;

import org.ptolemy.machineLearning.hmm.ParameterEstimator;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p> This actor implements the Expectation-Maximization(EM) algorithm for
parameter estimation in a family of graphical stochastic models, known as
the Hidden Semi-Markov Model family.
<p> In addition to estimating the parameters of a hidden markov model, 


 <p>
 <b>References</b>
 <p>[1]
 Jordan, Michael I., et al. <i>An introduction to variational methods for graphical
 models</i>, Springer Netherlands, 1998.
 <p>[2]
 Bilmes, Jeff A. <i>A gentle tutorial of the EM algorithm and its application
 to parameter estimation for Gaussian mixture and hidden Markov models.</i>
 International Computer Science Institute 4.510 (1998): 126.

@see org.ptolemy.machineLearning.hmm.ParameterEstimator


 @author Ilge Akkaya
 @version $Id: ParameterEstimator.java 68631 2014-03-16 10:14:10Z ilgea $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public abstract class HSMMParameterEstimator extends ParameterEstimator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HSMMParameterEstimator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        clusterAssignments = new TypedIOPort(this, "clusterAssignments", false, true);
        clusterAssignments.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        
        maxStateDuration = new Parameter(this, "maxStateDuration");
        maxStateDuration.setTypeEquals(BaseType.INT);
        maxStateDuration.setExpression("100");
        _maxDuration = 100;
 

        priorDurationDistribution = new Parameter(this, "priorDurationDistribution");
        priorDurationDistribution.setExpression("");
        priorDurationDistribution.setTypeEquals(new ArrayType(BaseType.DOUBLE));
        
        durationProbabilities = new Parameter(this, "durationProbabilities");
        durationProbabilities.setTypeEquals(BaseType.DOUBLE_MATRIX); 

        durationEstimates = new TypedIOPort(this, "durationEstimates", false, true);
        durationEstimates.setTypeEquals(BaseType.DOUBLE_MATRIX);
 

        _initializeArrays();

    }

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if(attribute == maxStateDuration)
        {
            _maxDuration = ((IntToken)maxStateDuration.getToken()).intValue();
        } else if( attribute == durationProbabilities){
            int nDurations = ((MatrixToken) durationProbabilities.getToken())
                    .getColumnCount();
            _nStates = ((IntToken)nStates.getToken()).intValue();
            _D0 = new double[_nStates][nDurations];
            for (int i = 0; i < _nStates; i++) {
                for (int j = 0; j < nDurations; j++) {
                    _D0[i][j] = ((DoubleToken) ((MatrixToken) durationProbabilities
                            .getToken()).getElementAsToken(i, j)).doubleValue();
                }
            }
        } else if( attribute == priorDurationDistribution) {
            int nDurations = ((ArrayToken)priorDurationDistribution.getToken()).length();
            _durationPriors = new double[nDurations];               
            for (int j = 0; j < nDurations; j++) {
                _durationPriors[j] = ((DoubleToken)((ArrayToken)priorDurationDistribution.
                        getToken()).getElement(j)).doubleValue();
            }
        }  else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

     
    /* The user-provided initial guess on the prior probability distribution*/
    public Parameter priorDurationDistribution;

    /* The user-provided initial guess on the prior probability distribution*/
    public Parameter durationProbabilities; 

    /* DurationEstimates */
    public TypedIOPort durationEstimates;
    
    /* Cluster Assignments */
    public TypedIOPort clusterAssignments;
    
    /* Maximum Duration */
    public Parameter maxStateDuration;
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        HSMMParameterEstimator newObject = (HSMMParameterEstimator) super
                .clone(workspace); 
        newObject._D0 = new double[_nStates][_maxDuration];
        newObject._durationPriors = new double[_maxDuration];
        newObject._priors = new double[_nStates];
        return newObject;
    }

    public void preinitialize() throws IllegalActionException{


        for (int i = 0; i < _maxDuration; i++) {
            ArrayToken dT = (ArrayToken) priorDurationDistribution.getToken();
            _durationPriors[i] = ((DoubleToken) (dT)
                    .getElement(i)).doubleValue();
        }

        _D0 = new double[_nStates][_maxDuration];
    }
    public void fire() throws IllegalActionException {

        super.fire(); 
        _durationPriors = new double[_maxDuration];   
        for (int i = 0; i < _maxDuration; i++) {
            _durationPriors[i] = ((DoubleToken) ((ArrayToken) priorDurationDistribution.getToken())
                    .getElement(i)).doubleValue();
        }

        _D0 = new double[_nStates][_maxDuration];

        for (int i = 0; i < _nStates; i++) {
            for (int j = 0; j < _maxDuration; j++) {
                _D0[i][j] = ((DoubleToken) ((MatrixToken) durationProbabilities
                        .getToken()).getElementAsToken(i, j)).doubleValue();
            }
        }
    }

    protected boolean _EMParameterEstimation() {

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
            if(iterations > 10){
                // check convergence within likelihoodThreshold
                if ((likelihood - _likelihood) < _likelihoodThreshold) {
                    break;
                }  
            }
            _likelihood = likelihood;
        }

        return success;
    }

    protected abstract double emissionProbability(double y, int hiddenState);

    protected abstract double durationProbability(int y, int hiddenState);

    protected void _initializeArrays() throws IllegalActionException {

        //_observations = new double[_observationLength];
        // infer the number of states from the mean array
        _likelihood = -Double.MAX_VALUE/10.0;
        _nStates = ((IntToken) nStates.getToken()).intValue();
        _transitionMatrix = new double[_nStates][_nStates];
        _A0 = new double[_nStates][_nStates];
        _priors = new double[_nStates];
    }

    protected abstract void _initializeEMParameters();

    protected abstract void _iterateEM();

    protected abstract boolean _checkForConvergence(int i);

    protected abstract void _updateEstimates();

    /* Java implementation of the Baum-Welch algorithm (Alpha-Beta Recursion) for parameter estimation
     * and cluster assignment. This method uses normalized alpha values for computing the conditional
     * probabilities of input sequences, to ensure numerical stability. SEt nCategories to zero for
     * continuous distribution types */ 
    protected HashMap HSMMAlphaBetaRecursion(double[] y, double[][] A,
            double[] prior, int nCategories, double[] dPrior) 
    {

        int nStates = _nStates;
        int nObservations = y.length;

        double[][][] alphas = new double[nObservations][nStates][_maxDuration];
        double[][][] betas = new double[nObservations][nStates][_maxDuration];
        double[][] gamma = new double[nObservations][nStates]; 

        double[][] A_hat = new double[nStates][nStates];
        double[] mu_hat = new double[nStates];
        double[] s_hat = new double[nStates];
        double[] pi_hat = new double[nStates];

        double[][] bStar = new double[nObservations][nStates];

        double[][] epsilon = new double[nObservations][nStates];
        double[][] S = new double[nObservations][nStates];

        double[][] epsilonStar = new double[nObservations][nStates];
        double[][] sStar = new double[nObservations][nStates]; 
        
        double[][] smoothedGamma = new double[nObservations][nStates]; 
        double[][] D_hat = new double[nStates][_maxDuration];
        
        double r = 0;
        double logLikelihood = 0.0;


        // INITIALIZATION 
        // initialize alphas
        for (int m = 0; m < nStates; m++) { 
            for(int d=0; d<_maxDuration; d++){
                alphas[0][m][d] = prior[m]*durationProbability(d,m);
            }
        } 
        for (int m = 0; m < nStates; m++) {  
            for(int d=0;d<_maxDuration; d++){
                gamma[0][m]+= alphas[0][m][d];
            }
        } 
        // initialize r 
        for (int ii = 0; ii < nStates; ii++) {
            r += gamma[0][ii]*emissionProbability(y[0],ii);
        }
        //initialize bStar
        for (int m = 0; m < nStates; m++) { 
            bStar[0][m] = emissionProbability(y[0],m)/r;
        }
        //initialize E
        for (int m = 0; m < nStates; m++) { 
            epsilon[0][m] = alphas[0][m][0]*bStar[0][m];
        }
        // initialize S
        for (int m = 0; m < nStates; m++) { 
            for(int n = 0; n < nStates; n++){
                S[0][m] += epsilon[0][n]*A[n][m];
            }
        }
        // initialize likelihood
        logLikelihood = Math.log(r);


        // FORWARD RECURSION
        for (int t = 1; t < y.length; t++) { 
            for (int m = 0; m < nStates; m++) { 
                for(int d=0;d<_maxDuration-1; d++){
                    // compute S, bStar
                    alphas[t][m][d] = S[t-1][m]*durationProbability(d,m) +
                            bStar[t-1][m]*alphas[t-1][m][d+1];
                }
            }
            for (int m = 0; m < nStates; m++) { 
                //compute gammas
                gamma[t][m] = 0;
                for(int d=0;d<_maxDuration; d++){
                    gamma[t][m]+= alphas[t][m][d];
                }
            } 
            r = 0;
            for (int ii = 0; ii < nStates; ii++) {
                r += gamma[t][ii]*emissionProbability(y[t],ii);
            }
            for (int m = 0; m < nStates; m++) { 
                bStar[t][m] = emissionProbability(y[t],m)/r;
            }
            //Compute epsilon
            for (int m = 0; m < nStates; m++) { 
                epsilon[t][m] = alphas[t][m][0]*bStar[t][m];
            }
            for (int m = 0; m < nStates; m++) { 
                S[t][m] = 0;
                for(int n = 0; n < nStates; n++){
                    S[t][m] += epsilon[t][n]*A[n][m];
                }
            }
            logLikelihood += Math.log(r);
        } 
        // Backward INITIALIZATION
        // initialize beta
        int endIndex = y.length-1;
        for (int m = 0; m < nStates; m++) {
            for(int d=0; d< _maxDuration; d++){
                betas[endIndex][m][d] = bStar[endIndex][m];
            }
        } 
        for (int m = 0; m < nStates; m++) { 
            for(int d=0; d< _maxDuration; d++){
                epsilonStar[endIndex][m] += durationProbability(d,m) * betas[endIndex][m][d];
            }
        }
        for (int m = 0; m < nStates; m++) { 
            for(int n=0; n < nStates; n++){
                sStar[endIndex][m] += A[m][n]*epsilonStar[endIndex][n];
            }
        }  
        
        
        for (int m = 0; m < nStates; m++) { 
            for(int d=0; d< _maxDuration; d++){
                smoothedGamma[endIndex][m] += alphas[endIndex][m][d]*betas[endIndex][m][d];
            } 
            smoothedGamma[endIndex][m] *= bStar[endIndex][m];
        }
        // BACKWARD RECURSION
        for (int t = y.length - 2; t >= 0; t--) {
            for (int m = 0; m < nStates; m++) {
                for(int d=0; d< _maxDuration; d++){ 
                    if(d==0){
                        betas[t][m][d] = sStar[t+1][m]*bStar[t][m];
                    }else{
                        betas[t][m][d] = betas[t+1][m][d-1]*bStar[t][m];
                    } 
                }
            }

            for (int m = 0; m < nStates; m++) { 
                for(int d=0; d< _maxDuration; d++){
                    epsilonStar[t][m] += durationProbability(d,m) * betas[t][m][d];
                }
            }
            for (int m = 0; m < nStates; m++) { 
                for(int n=0; n < nStates; n++){
                    sStar[t][m] += A[m][n]*epsilonStar[t][n];
                }
            }
            for (int m = 0; m < nStates; m++) {  
                    smoothedGamma[t][m] +=  smoothedGamma[t+1][m] + epsilon[t][m]*sStar[t+1][m] - S[t][m]*epsilonStar[t+1][m];
            } 
            
            // transition probability matrix estimate
            if(t > 0){
                for(int m = 0; m < nStates; m++){
                    for(int n=0; n < nStates; n++){
                        A_hat[m][n] += epsilon[t-1][m]*epsilonStar[t][n];
                    }
                    for(int d=0; d< _maxDuration; d++){ 
                        D_hat[m][d] +=  S[t-1][m]*betas[t][m][d];
                    }
                } 
            }
        }
         
        for(int m = 0; m < nStates; m++){
            for(int n=0; n < nStates; n++){
                A_hat[m][n]*=A[m][n];
            }
            for(int d=0; d< _maxDuration; d++){ 
                D_hat[m][d] *= durationProbability(d,m);
            }
        } 
          
        // normalize A
        double[] rowsum = new double[nStates];
        double[] obsSums = new double[nStates];
        for(int m = 0; m < nStates; m++){
            rowsum[m] =0;
            for(int n=0; n < nStates; n++){
                rowsum[m]+=A_hat[m][n];
            }
            for(int j=0; j < nStates; j++){
                A_hat[m][j] /= rowsum[m];
            }
            for(int d=0; d<_maxDuration; d++){ 
                obsSums[m] += D_hat[m][d];
            }
            for(int d=0; d<_maxDuration; d++){ 
                D_hat[m][d] /= obsSums[m];
            }
        }
 
        double[] gammasum = new double[nStates];

        // estimate pi
        double piSum = 0;
        for(int m = 0; m < nStates; m++){
            pi_hat[m] = smoothedGamma[0][m];
            piSum += pi_hat[m];
        }
        for(int m = 0; m < nStates; m++){
            pi_hat[m]/=piSum;
        }

        for (int j = 0; j < nStates; j++) {
            gammasum[j] = 0.0;
            for (int t = 0; t < y.length; t++) {
                mu_hat[j] += smoothedGamma[t][j]* y[t];
                gammasum[j] += smoothedGamma[t][j];
            }

            mu_hat[j] = mu_hat[j] / gammasum[j];

            for (int t = 0; t < y.length; t++) {
                s_hat[j] += (smoothedGamma[t][j] * Math.pow((y[t] - mu_hat[j]), 2));
            }
            s_hat[j] = Math.sqrt(s_hat[j] / gammasum[j]);
        }
        // labels for the multinomial setting

        // do hidden state sequence estimation to compute the log-likelihood, given the current
        // parameter estimates
        int[] clusterAssignments = new int[y.length];
        for (int t = 0; t < y.length; t++) {
            int maxState = 0;
            for (int j = 1; j < nStates; j++) {
                if (smoothedGamma[t][j] > smoothedGamma[t][maxState]) {
                    maxState = j;
                }
            }
            clusterAssignments[t] = maxState;
        } 
        
        HashMap estimates = new HashMap();

        estimates.put("mu_hat", mu_hat);
        estimates.put("s_hat", s_hat);
        estimates.put("gamma", smoothedGamma); //this will not be needed for most of the distributions.
        estimates.put("A_hat", A_hat);
        estimates.put("pi_hat", pi_hat);
        estimates.put("likelihood", logLikelihood);
        estimates.put("D_hat",D_hat);
        estimates.put("clusterAssignments", clusterAssignments);
        return estimates;
    } 

    /* Duration priors - nStates x nDurations*/
    protected double[] _durationPriors; 

    /* new duration distribution */
    protected double[][] D_new = null;

    protected double[][] _D0 = null;

    protected double[][] _D = null;

    protected int _maxDuration; 

}
