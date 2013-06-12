

/* Parameter Estimation for Graphical Models.

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
package ptolemy.demo.FaultModels.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements the Expectation-Maximization(EM) algorithm for
parameter estimation in graphical stochastic models. Two types of fundamental
types of Bayesian Network models: <i>Mixture Model(MM)<\i> and <i>Hidden Markov 
Model(HMM)<\i> are supported. The input is an array of observations of arbitrary
length and the outputs are the parameter estimates for the chosen model.</p>
<p>
The output ports reflect the parameter estimates of Gaussian MM or HMM. The Mixture
Model is parameterized by <i>M</i> states, each distributed according to a distribution
specified by the <i>emissionDistribution</i> parameter. Currently, the actor supports
Gaussian emissions.
The <i>mean<\i>  is a double array output containing the mean estimates and 
<i>sigma</i> is a double array output containing standard deviation estimates of 
each mixture component. If the <i>modelType<\i> is HMM, then an additional output, 
<i>transitionMatrix<\i> is provided, which is an estimate of the transition matrix 
governing the Markovian process representing the hidden state evolution. 
If the <i>modelType<\i> is MM, this port outputs a double array with the prior 
probability estimates of the mixture components.
</p>
<p>
The user-defined parameters are initial guesses for the model parameters, given by
<i>m0</i>, the mean vector guess, <i>s0</i>, the standard deviation vector guess, 
<i>prior</i>, the prior state distribution guess, <i>A0</i>, the transition 
matrix guess ( only for HMM). <i>iterations</i> is the number of EM iterations 
allowed until convergence. 
The actor iterates over the parameter estimates using the EM algorithm. If, at any
point, the estimates become NaN, the user is notified that the algorithm did not
converge and is given the option to randomize initial guesses to reiterate.


 <p>
 <b>References</b>
 <p>[1]
 Jordan, Michael I., et al. <i>An introduction to variational methods for graphical 
 models</i>, Springer Netherlands, 1998.
 <p>[2]
 Bilmes, Jeff A. <i>A gentle tutorial of the EM algorithm and its application 
 to parameter estimation for Gaussian mixture and hidden Markov models.</i> 
 International Computer Science Institute 4.510 (1998): 126.

 @see ptolemy.demo.FaultModels.lib.ClassifyObservations
 @see ptolemy.demo.FaultModels.HiddenMarkovModelAnalysis

 @author Ilge Akkaya
 @version  
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class ExpectationMaximization extends TypedAtomicActor {
   /** Construct an actor with the given container and name.
    *  @param container The container.
    *  @param name The name of this actor
    *  @exception IllegalActionException If the actor cannot be contained
    *   by the proposed container.
    *  @exception NameDuplicationException If the container already has an
    *   actor with this name.
    */
   public ExpectationMaximization(CompositeEntity container, String name)
           throws NameDuplicationException, IllegalActionException {
       super(container, name);

       mean = new TypedIOPort(this, "mean", false, true);
       mean.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       
       sigma = new TypedIOPort(this, "stdDev", false, true);
       sigma.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       
       transitionMatrix = new TypedIOPort(this, "transitionMatrix", false, true);
       transitionMatrix.setTypeEquals( BaseType.DOUBLE_MATRIX);
       
       modelType = new StringParameter(this, "modelType");
       modelType.setExpression("Hidden Markov Model");
       modelType.addChoice("Hidden Markov Model");
       modelType.addChoice("Mixture Model");
       _modelType = _HMM;
       
       emissionDistribution = new StringParameter(this, "emissionDistribution");
       emissionDistribution.setExpression("Gaussian");
       // The following will also be supported by the actor
       //emissionDistribution.addChoice("Rician");
       //emissionDistribution.addChoice("Exponential");
       //emissionDistribution.addChoice("Multinomial");
       _distribution = _GAUSSIAN;
      
       input = new TypedIOPort(this, "input", true, false);
       input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       //output.setTypeEquals(BaseType.DOUBLE);

       iterations = new Parameter(this, "iterations");
       iterations.setExpression("5");
       iterations.setTypeEquals(BaseType.INT); 
       
       A0 = new Parameter(this, "transitionProbabilityMatrix");
       A0.setExpression("[0.5, 0.5; 0.5, 0.5]");
       A0.setTypeEquals(BaseType.DOUBLE_MATRIX);
        
       m0 = new Parameter(this, "meanVectorGuess");
       m0.setExpression("{0.0, 4.0}");
       m0.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       
       s0 = new Parameter(this, "standardDeviationGuess");
       s0.setExpression("{1.0, 1.0}");
       s0.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       
       prior = new Parameter(this, "priorDistribution");
       prior.setExpression("{0.5,0.5}");
       prior.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       
       
       
       
       //input_tokenConsumptionRate.setExpression(observationLength.getToken().add(classificationLength.getToken()
       //        ).toString());
       //output_tokenProductionRate.setExpression("classificationLength");
       
       
       
       //_nStates = ((IntToken)numberOfStates.getToken()).intValue();
       
       
       populateArrays();
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public variables                  ////

   public TypedIOPort input;

   public TypedIOPort mean;

   public TypedIOPort transitionMatrix;

   public TypedIOPort sigma;

   public Parameter A0;
   
   public Parameter iterations;

   public Parameter m0;

   public Parameter prior;

   public Parameter s0;
   
   public StringParameter emissionDistribution;
   
   public StringParameter modelType;
   
   

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   /** Ensure that the order parameter is positive and recompute the
    *  size of internal buffers.
    *  @param attribute The attribute that has changed.
    *  @exception IllegalActionException If the parameters are out of range.
    */
   public void attributeChanged(Attribute attribute)
           throws IllegalActionException {
       if(attribute == A0){
           
          for (int i = 0; i < _nStates; i++) {
              for(int j = 0; j< _nStates; j++){
                  _A0[i][j] = ((DoubleToken)((MatrixToken) A0.getToken())
                          .getElementAsToken(i, j))
                          .doubleValue();
              }
          }
      }else if(attribute == m0){
           
           for (int i = 0; i < _nStates; i++) {
               _m0[i] = ((DoubleToken)((ArrayToken) m0.getToken()).getElement(i))
                       .doubleValue();
           }
           
       } else if(attribute == s0){ 
           for (int i = 0; i < _nStates; i++) { 
               _s0[i] = ((DoubleToken)((ArrayToken) s0.getToken()).getElement(i))
                       .doubleValue();
           } 
       } else if(attribute == prior){
            
           for (int i = 0; i < _nStates; i++) { 
               _priors[i] = ((DoubleToken)((ArrayToken) prior.getToken()).getElement(i))
                       .doubleValue();
           }
           
       }else if(attribute == emissionDistribution){
           
           String functionName = emissionDistribution.getExpression().trim().toLowerCase();
           if (functionName.equals("gaussian")) {
               _distribution = _GAUSSIAN;
           } else if (functionName.equals("exponential")) {
               _distribution = _EXPONENTIAL;
           } else if (functionName.equals("rician")){
               _distribution = _RICIAN;
           } else if (functionName.equals("multinomial")){
               _distribution = _MULTINOMIAL;
           } else{
               throw new IllegalActionException(this,
                       "Unsupported distribution function: " + functionName
                               + ".  Valid functions are 'Gaussian', 'Exponential', 'Rician', "
                               + "'Multinomial.");
           }
           
       }else if(attribute == iterations){
           _nIterations = (int)((IntToken) iterations.getToken()).intValue();
           
       }else if(attribute == modelType){
           String modelName = modelType.getExpression().trim().toLowerCase();
           if ( modelName.equals("mixture model")){
               _modelType = _MM;
               A0.setVisibility(Settable.NONE);
               transitionMatrix.setTypeEquals( new ArrayType(BaseType.DOUBLE));
           } else if (modelName.equals("hidden markov model")){
               _modelType = _HMM;
               A0.setVisibility(Settable.FULL);
               //A0.setExpression("[0.5,0.5;0.5,0.5]");
               transitionMatrix.setTypeEquals( BaseType.DOUBLE_MATRIX);
           } else{
               throw new IllegalActionException(this,
                       "Unsupported model: " + modelName
                               + ".  Supported models are 'Hidden Markov Model', 'Mixture Model'");
           }
           
           
       } else{
           super.attributeChanged(attribute);
       }
   }
   /** Consume the inputs and produce the outputs of the FFT filter.
    *  @exception IllegalActionException If a runtime type error occurs.
    */
   public void fire() throws IllegalActionException {
       super.fire();
       populateArrays();
       
       if( (_nStates != _s0.length) ||(_nStates != _A0.length) || (_nStates != _priors.length) || (_nStates != _m0.length))
       {
           throw new IllegalActionException(this, "Parameter guess vectors can not have different lengths.");
       }
      
       // get the first N observations and produce parameter estimates
       
       Token observationArray= input.get(0);
       // observation length is inferred from the input array length. 
       _observationLength = ((ArrayToken)observationArray).length();
       _observations = new double[_observationLength];
       if( _observationLength <= 0){
           throw new IllegalActionException(this, "Observation sequence length "
                   + _observationLength + " but must be greater than zero.");
       }
       
       
       // Get Observation Values as doubles 
       //FIXME: should the observations  allowed to be vectors too?
       for (int i = 0; i < _observationLength; i++) {
           _observations[i] = ((DoubleToken)((ArrayToken) observationArray).getElement(i))
                   .doubleValue();
           //System.out.println(_observations[i]);
       } 
       
       
       HashMap newEstimates = new HashMap();
       int nStates = _m0.length;
       
       if( _modelType == _HMM){
       
           double[][] A_new = new double[nStates][nStates];
           double[] m_new = new double[nStates];
           double[] s_new = new double[nStates];
          
           for( int iterations = 0; iterations< _nIterations; iterations++){
               
               newEstimates = gaussianHMM(0,_observations, _A0, _m0, _s0, _priors);
               // get new estimates
               m_new = (double[]) newEstimates.get("mu_hat");
               s_new = (double[]) newEstimates.get("s_hat");
               A_new = (double[][]) newEstimates.get("A_hat");
               
               
               // check for NaN
               if((m_new[0] != m_new[0]) || (s_new[0]!=s_new[0]) || (A_new[0]!=A_new[0])){
                   // if no convergence in 10 iterations, issue warning message.
                   if ( iterations < _nIterations-1){
                       //if((asked == 0) ){
                           //&& !MessageHandler.yesNoQuestion("WARNING: The Expectation Maximization algorithm did not " +
                      // "converge with the given initial parameters. Randomize initial guess?")
                          // break;
                      // }
                      // asked = 1;
                   } else{
                       MessageHandler.message("Expectation Maximization failed to converge");
                   }
                   
                   // randomize means
                   double minO = _observations[0];
                   double maxO = _observations[0];
                   for(int t=0; t<_observations.length; t++){
                       if( _observations[t] < minO){
                           minO = _observations[t];
                       }
                       if( _observations[t] > maxO){
                           maxO = _observations[t];
                       }
                       
                   }
                   double L = maxO - minO;
                   // make new random guess
                   for( int i = 0; i< nStates; i++){
                       m_new[i] = L/nStates*Math.random()  +L*i/nStates + minO;
                       s_new[i] = Math.abs((maxO - minO)*Math.random())/nStates;
                       for ( int j = 0 ; j < nStates; j++){
                           //A_new[i][j] = 1.0/nStates;
                       }
                      
                   }
                   A_new = _Ainit;
                   // sort arrays
                   Arrays.sort(m_new);
                    
               }
               // Converged
               if( (_m0[0] - m_new[0])< mTol){
                //   break;
               }
               _A0 = A_new;
               _s0 = s_new;
               _m0 = m_new;
               
           } 
           Token[] mTokens = new Token[nStates];
           Token[] sTokens = new Token[nStates];
           for ( int i = 0; i< nStates; i++){
               mTokens[i] = new DoubleToken(m_new[i]);
               sTokens[i] = new DoubleToken(s_new[i]);
           }
           // broadcast best-effort parameter estimates
           mean.send(0, new ArrayToken(mTokens));
           sigma.send(0, new ArrayToken(sTokens));
           transitionMatrix.send(0, new DoubleMatrixToken(A_new));
       } else if (_modelType == _MM){
           
           double[] pi_new = new double[nStates];
           double[] m_new = new double[nStates];
           double[] s_new = new double[nStates];
           
           for( int iterations = 0; iterations< _nIterations; iterations++){
               
               newEstimates = gaussianMM(0,_observations, _m0, _s0, _priors);
               // get new estimates
               m_new = (double[]) newEstimates.get("mu_hat");
               s_new = (double[]) newEstimates.get("s_hat");
               pi_new = (double[]) newEstimates.get("pi_hat");
               _priors = pi_new;
               _s0 = s_new;
               _m0 = m_new;
               
           }
           Token[] mTokens = new Token[nStates];
           Token[] sTokens = new Token[nStates];
           Token[] pTokens = new Token[nStates];
           for ( int i = 0; i< nStates; i++){
               mTokens[i] = new DoubleToken(m_new[i]);
               sTokens[i] = new DoubleToken(s_new[i]);
               pTokens[i] = new DoubleToken(pi_new[i]);
           }
           
           mean.send(0, new ArrayToken(mTokens));
           sigma.send(0, new ArrayToken(sTokens));
           transitionMatrix.send(0,new ArrayToken(pTokens));
           
       }
   }
   
   public static final HashMap gaussianHMM(int startAt, double[] y, double[][] A, double[] mu, double[] sigma, double[] prior)
   {
       int nStates = mu.length;
       double[][] alphas = new double[y.length][nStates]; 
       double[][] betas = new double[y.length][nStates]; 
       double[] Py = new double[y.length];
       double[][] gamma = new double[y.length][nStates];
       // do this with org.apache.commons.math3.distribution
       //later NormalDistribution gaussian = ...
       
       for(int t=0; t< y.length ; t++){
           for (int i=0; i < nStates; i++){
               if(t == 0){
                   alphas[t][i] =  prior[i]*gaussian( y[t], mu[i], sigma[i]);
               }else{
                   alphas[t][i] = 0;
                   for(int qt = 0; qt < nStates; qt++){
                       alphas[t][i]+= A[qt][i]*gaussian(y[t],mu[i],sigma[i])*alphas[t-1][qt]; 
                   }
               }
           }
       }
       
       
       for(int t=y.length-1; t>=0 ; t--){
           // initialize py at time t
           Py[t] = 0;
           
           for (int i=0; i < nStates; i++){
               
               gamma[t][i] = 0.0;
               
               if(t == y.length - 1){
                   betas[t][i] =  1;
               }else{
                   betas[t][i] = 0;
                   // reverse-time recursion  (do this recursively later)
                   for(int qtp = 0; qtp < nStates; qtp++){
                       betas[t][i]+= A[i][qtp]*gaussian(y[t+1],mu[qtp],sigma[qtp])*betas[t+1][qtp]; 
                   }
                   //System.out.println(betas[t][i]);
               }
               
               Py[t]+= alphas[t][i]*betas[t][i];
           }
           for( int i =0; i < nStates; i++)
           {
               gamma[t][i] += alphas[t][i]*betas[t][i]/Py[t];
           }
           //System.out.println("Gamma "+ t+ ":" + gamma[t][0] + "  " + gamma[t][1]);
           //System.out.println("Alpha "+ t+ ":" + alphas[t][0] + "  " + alphas[t][1]);
           
       }
       
       // up next, calculate the psis ( for the transition matrix)
       double[][][] psi = new double[y.length-1][nStates][nStates];
       
       double[][] A_hat = new double[nStates][nStates];
       double[]   mu_hat = new double[nStates];
       double[]   s_hat = new double[nStates];
       
       for( int next=0; next < nStates; next++){
           for( int now = 0; now < nStates; now++){
               for(int t = 0; t < (y.length-1); t++){
                   // gamma t or t+1? alphas t or t+1?
                   if(alphas[t+1][next] == 0){
                       psi[t][now][next] = 0;
                   }
                   else{
                       psi[t][now][next] = alphas[t][now]*gaussian(y[t+1], mu[next], sigma[next])
                               *gamma[t+1][next]*A[now][next]/alphas[t+1][next];
                   }
                   A_hat[now][next] += psi[t][now][next];
                   //System.out.print(psi[t][i][j]+ " ");
               }
               
           }
           mu_hat[next] = 0;
           s_hat[next] = 0;
           //System.out.println();
       }
       // Normalize A
       double[] rowsum = new double[nStates];
       double[] gammasum = new double[nStates];
       for(int i = 0 ; i< nStates; i++){
           
           rowsum[i] = 0;
           for(int j = 0; j < nStates; j++){
               rowsum[i] += A_hat[i][j];
           }
           for(int j = 0; j < nStates; j++){
               A_hat[i][j]/= rowsum[i];
           }
           
           gammasum[i]=0.0;
           
       }
       
       for(int j = 0; j < nStates; j++) {
           gammasum[j] = 0.0;
           
           for ( int t = 0; t< y.length; t++){
               gammasum[j] += gamma[t][j];
            // sample means for all states
               mu_hat[j] += gamma[t][j]*y[t];
           }
           mu_hat[j] = mu_hat[j]/gammasum[j];
           
           for ( int t = 0; t< y.length; t++){
               s_hat[j] += (gamma[t][j]*Math.pow((y[t]-mu_hat[j]),2));
           }
           s_hat[j] = Math.sqrt(s_hat[j]/gammasum[j]);
       }
       
       HashMap estimates = new HashMap();
       
       //estimates.put("mu_hat", mu_hat);
       //estimates.put("s_hat", s_hat);
       //estimates.put("A_hat", A_hat);
       
       // Actual tags should also be sorted if we do this. 
       estimates = sortEstimates(mu_hat, s_hat, A_hat);
              
       return estimates;
   
   }
   
   public static final HashMap gaussianMM(int startAt, double[] y, double[] mu, double[] sigma, double[] prior)
   {
       int nStates = mu.length;
       double[][] tau = new double[y.length][nStates]; 
       
       
       
       double[]   pi_hat = new double[nStates];
       double[]   mu_hat = new double[nStates];
       double[]   s_hat = new double[nStates];
       
       
       double[] rowSumTau = new double[y.length];
       double[] colSumTau = new double[nStates];
       
    // soft assignments to clusters
       for(int t=0; t< y.length ; t++){
           for (int i=0; i < nStates; i++){
               tau[t][i] = prior[i]*gaussian(y[t], mu[i],sigma[i]);
           }
       }
       
       for(int i = 0; i < nStates; i++){
           for(int t = 0; t< y.length; t++){
           
           rowSumTau[t] += tau[t][i];
           }
       }
       
       
       for(int t=0; t< y.length ; t++){
           for (int i=0; i < nStates; i++){
               tau[t][i] /= rowSumTau[t];
               mu_hat[i] += tau[t][i]*y[t];
               colSumTau[i] += tau[t][i];
           }
       }
       
       for (int i=0; i < nStates; i++)
       {
           mu_hat[i]/=colSumTau[i];
       }
       
       for(int t=0; t< y.length ; t++){
           for (int i=0; i < nStates; i++){
               s_hat[i]  += tau[t][i]*Math.pow((y[t]-mu_hat[i]),2);
           }
       }
       
       for (int i=0; i < nStates; i++){
           s_hat[i]  = Math.sqrt(s_hat[i]/colSumTau[i]);
           pi_hat[i] = colSumTau[i]/y.length;
       }
       
       
       HashMap estimates = new HashMap();
       
       estimates.put("mu_hat", mu_hat);
       estimates.put("s_hat", s_hat);
       estimates.put("pi_hat", pi_hat);
              
       return estimates;
   
   }
   
 
   
   
   private static final double gaussian(double x, double mu, double sigma){
       
       return 1.0/(Math.sqrt(2*Math.PI)*sigma)*Math.exp(-0.5*Math.pow((x-mu)/sigma, 2));
   }
   
   private static HashMap sortEstimates( double[] mu, double[] sigma, double[][] transition ){
       HashMap sortedEstimates = new HashMap();
       
       
       
       HashMap<Double, Integer> originalOrder = new HashMap();
       for (int i = 0; i < mu.length; ++i) {
           originalOrder.put(mu[i], i);
       }
       
       Arrays.sort(mu);
       double[] orderedSigma = new double[sigma.length];
       double[][] orderedTrans = new double[transition.length][transition.length];
       for (int i = 0; i < mu.length; ++i) {
           int j = originalOrder.get(mu[i]);
           orderedSigma[i] = sigma[j];
           
           for( int k=0; k < mu.length; k++){
               int l = originalOrder.get(mu[k]);
               orderedTrans[i][k] = transition[j][l];
           }
       }
       
       sortedEstimates.put("mu_hat", mu);
       sortedEstimates.put("s_hat", orderedSigma);
       sortedEstimates.put("A_hat", orderedTrans);
       return sortedEstimates;
   }
   

   private void populateArrays() throws IllegalActionException{
       
       //_observations = new double[_observationLength];
       // infer the number of states from the mean array
       _nStates = ((ArrayToken)m0.getToken()).length();
       _m0 = new double[_nStates];
       _s0 = new double[_nStates];
       _A0 = new double[_nStates][_nStates];
       _Ainit = new double[_nStates][_nStates];
       _priors = new double[_nStates];
       
        for (int i = 0; i < _nStates; i++) {
                   
                   _s0[i] = ((DoubleToken)((ArrayToken) s0.getToken()).getElement(i))
                           .doubleValue();
                   _m0[i] = ((DoubleToken)((ArrayToken) m0.getToken()).getElement(i))
                           .doubleValue();
                   _priors[i] = ((DoubleToken)((ArrayToken) prior.getToken()).getElement(i))
                           .doubleValue();
                   if(_modelType == _HMM){ 
                       for(int j = 0; j< _nStates; j++){
                           _A0[i][j] = ((DoubleToken)((MatrixToken) A0.getToken())
                                   .getElementAsToken(i, j))
                                   .doubleValue();
                       }
                   }
                   _Ainit = _A0;
               }
   }
   
   
///////////////////////////////////////////////////////////////////
////                         private variables                 ////

   private double[][] _A0; 
   private double[][] _Ainit; 
   
   protected int _distribution;

   private double[] _m0;
    
   protected int _modelType;
    
   private int _nIterations;
    
   private int _nStates;

   private int _observationLength;

   private double[] _observations;
   
   private double[] _priors;

   private double[] _s0;
   
   /** The convergence tolerance**/
   private static final double mTol = 0.0001;
   
   protected static final int _GAUSSIAN = 0;
   protected static final int _RICIAN = 1;
   protected static final int _EXPONENTIAL = 2;
   protected static final int _MULTINOMIAL = 3;
   
   protected static final int _HMM = 50;
   protected static final int _MM = 51;
}
