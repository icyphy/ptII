

/* An FFT.

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
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType; 
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException; 
///////////////////////////////////////////////////////////////////
////FFT
import ptolemy.kernel.util.StringAttribute;

/**
<p>This actor performs Maximum-Likelihood classification of the partially-observed
Bayesian Network models. ClassifyObservations is designed to work with <i>
ExpectationMaximization<\i>, which provides the Maximum-Likelihood model parameters
from which the observations are assumed to be drawn. The output is an integer array 
of labels, representing the maximum-likelihood hidden state sequence of the given
model.

<p>
The user provides a set of parameter estimates as inputs to the model, and 
The <i>mean<\i>  is a double array input containing the mean estimate and 
<i>sigma</i> is a double array input containing standard deviation estimate of 
each mixture component. If the <i>modelType<\i> is HMM, then an additional input, 
<i>transitionMatrix<\i> is provided, which is an estimate of the transition matrix 
governing the Markovian process representing the hidden state evolution. The <i>prior
</i> input is an estimate of the prior state distribution.


 <p>
 <b>References</b>
 <p>[1]
 Jordan, Michael I., et al. <i>An introduction to variational methods for graphical 
 models</i>, Springer Netherlands, 1998.
 <p>[2]
 Bilmes, Jeff A. <i>A gentle tutorial of the EM algorithm and its application 
 to parameter estimation for Gaussian mixture and hidden Markov models.</i> 
 International Computer Science Institute 4.510 (1998): 126.

 @see ptolemy.demo.FaultModels.lib.ExpectationMaximization
 @see ptolemy.demo.FaultModels.HiddenMarkovModelAnalysis

 @author Ilge Akkaya
 @version  
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class ClassifyObservations extends TypedAtomicActor {
   /** Construct an actor with the given container and name.
    *  @param container The container.
    *  @param name The name of this actor
    *  @exception IllegalActionException If the actor cannot be contained
    *   by the proposed container.
    *  @exception NameDuplicationException If the container already has an
    *   actor with this name.
    */
   public ClassifyObservations(CompositeEntity container, String name)
           throws NameDuplicationException, IllegalActionException {
       super(container, name);

       mean =  new PortParameter(this, "mean");
       mean.setExpression("{50E-3,200E-3,300E-3}");
       mean.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       StringAttribute cardinality = new StringAttribute(
               mean.getPort(), "_cardinal");
       cardinality.setExpression("SOUTH");
       
       modelType = new StringParameter(this, "modelType");
       modelType.setExpression("Hidden Markov Model");
       modelType.addChoice("Hidden Markov Model");
       modelType.addChoice("Mixture Model");
       _modelType = _HMM;
       
       standardDeviation =  new PortParameter(this, "standardDeviation"); 
       standardDeviation.setExpression("{10E-3,50E-3,50E-3}");
       standardDeviation.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       cardinality = new StringAttribute(
               standardDeviation.getPort(), "_cardinal");
       cardinality.setExpression("SOUTH");
       
       transitionMatrix = new PortParameter(this, "transitionMatrix"); 
       transitionMatrix.setExpression("[0.3,0.3,0.4;0.3,0.3,0.4;0.3,0.3,0.4]");
       transitionMatrix.setTypeEquals( BaseType.DOUBLE_MATRIX);
       cardinality = new StringAttribute(
               transitionMatrix.getPort(), "_cardinal");
       cardinality.setExpression("SOUTH");
       
       prior = new PortParameter(this, "priorDistribution");
       prior.setExpression("{0.5,0.5,0.0}");
       prior.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       cardinality = new StringAttribute(
               prior.getPort(), "_cardinal");
       cardinality.setExpression("SOUTH");
       
       input = new TypedIOPort(this, "input", true, false);
       input.setTypeEquals(new ArrayType(BaseType.DOUBLE));
       
       output = new TypedIOPort(this, "output", false, true);
       output.setTypeEquals(new ArrayType(BaseType.INT)); 
       
       emissionDistribution = new StringParameter(this, "emissionDistribution");
       emissionDistribution.setExpression("Gaussian");
       //emissionDistribution.addChoice("Rician");
       //emissionDistribution.addChoice("Exponential");
       //emissionDistribution.addChoice("Multinomial");
       _distribution = _GAUSSIAN; 
       
       //_nStates = ((ArrayToken) meanToken).length();
       _nStates = ((ArrayToken)mean.getToken()).length();
       _meanEstimate = new double[_nStates];
       _stDeviationEstimate = new double[_nStates];
       _transitionMatrixEstimate = new double[_nStates][_nStates];
       _priors = new double[_nStates];
       
       
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public variables                  ////

   
   public PortParameter prior;
   
   public StringParameter modelType;
   
   public PortParameter mean;
   
   public PortParameter standardDeviation;
   
   public PortParameter transitionMatrix;
   
   public TypedIOPort output;
   
   public TypedIOPort input;
   
   public StringParameter emissionDistribution;

   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   /** Ensure that the order parameter is positive and recompute the
    *  size of internal buffers.
    *  @param attribute The attribute that has changed.
    *  @exception IllegalActionException If the parameters are out of range.
    */
   public void attributeChanged(Attribute attribute)
           throws IllegalActionException {
       if(attribute == emissionDistribution){
           
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
           
       }else if(attribute == modelType){
           String modelName = modelType.getExpression().trim().toLowerCase();
           if ( modelName.equals("mixture model")){
               _modelType = _MM;
               //transitionMatrix.setTypeEquals( new ArrayType(BaseType.DOUBLE));
           } else if (modelName.equals("hidden markov model")){
               _modelType = _HMM;
               //transitionMatrix.setTypeEquals( BaseType.DOUBLE_MATRIX);
           } else{
               throw new IllegalActionException(this,
                       "Unsupported model: " + modelName
                               + ".  Supported models are 'Hidden Markov Model', 'Mixture Model'");
           }
           
           
       }else if(attribute == mean){
           
           _nStates = ((ArrayToken) mean.getToken()).length();
           _meanEstimate = new double[_nStates];
           for (int i = 0; i < _nStates; i++) {
               _meanEstimate[i] = ((DoubleToken)((ArrayToken) mean.getToken()).getElement(i))
                       .doubleValue();
           }
       }else if(attribute == standardDeviation){
           _nStates = ((ArrayToken) standardDeviation.getToken()).length();
           _stDeviationEstimate = new double[_nStates];
           for (int i = 0; i < _nStates; i++) {
               _stDeviationEstimate[i] = ((DoubleToken)((ArrayToken) standardDeviation.getToken()).getElement(i))
                       .doubleValue();
           }
       }else if(attribute == prior){
           _nStates = ((ArrayToken) standardDeviation.getToken()).length();
           _priors = new double[_nStates];
           for (int i = 0; i < _nStates; i++) {
               _priors[i] = ((DoubleToken)((ArrayToken) prior.getToken()).getElement(i))
                       .doubleValue();
           }
       }else if(attribute == transitionMatrix){
           _nStates = ((ArrayToken) standardDeviation.getToken()).length();
           _transitionMatrixEstimate = new double[_nStates][_nStates];
           for (int i = 0; i < _nStates; i++) {
               for(int j = 0; j< _nStates; j++){
                   _transitionMatrixEstimate[i][j] = ((DoubleToken)((MatrixToken) transitionMatrix.getToken())
                           .getElementAsToken(i, j))
                           .doubleValue();
               }
           }
       }else{
           super.attributeChanged(attribute);
       }
   }
   /** Consume the inputs and produce the outputs of the FFT filter.
    *  @exception IllegalActionException If a runtime type error occurs.
    */
   public void fire() throws IllegalActionException {
       super.fire();
       
       // Read input ports
       populateArrays();
       
       if( (_nStates != _stDeviationEstimate.length) ||(_nStates != _transitionMatrixEstimate.length))
       {
           throw new IllegalActionException(this, "Parameter guess vectors need to have the same length.");
       }
       int[] classifyStates = new int[_observations.length];
       if( _modelType == _HMM){
           classifyStates = gaussianClassifyHMM(0,_observations, _transitionMatrixEstimate, _meanEstimate, _stDeviationEstimate, _priors); 
       } else if( _modelType == _MM){
           classifyStates = gaussianClassifyMM(0,_observations, _meanEstimate, _stDeviationEstimate, _priors); 
       }
           
       IntToken[] _outTokenArray = new IntToken[classifyStates.length];
       for (int i = 0; i < classifyStates.length; i++) {
           
        _outTokenArray[i] = new IntToken(classifyStates[i]);
       }

       output.broadcast(new ArrayToken(BaseType.INT, _outTokenArray));
   }
private void populateArrays() throws IllegalActionException{

       
       //Token meanToken = mean.getToken();
       //Token stdToken = standardDeviation.getToken();
       
       //Token priorToken = prior.getToken();
       Token observationArray= input.get(0);
       
       //_nStates = ((ArrayToken) meanToken).length();
       //_meanEstimate = new double[_nStates];
       //_stDeviationEstimate = new double[_nStates];
       //_transitionMatrixEstimate = new double[_nStates][_nStates];
       //_priors = new double[_nStates];
       
    // observation length is inferred from the input array length. 
       _classificationLength = ((ArrayToken) observationArray).length();
       _observations = new double[_classificationLength];
       
       
       // Get Observation Values as doubles 
       //FIXME: should the observations  allowed to be vectors of doubles, too?
       for (int i = 0; i < _classificationLength; i++) {
           _observations[i] = ((DoubleToken) ((ArrayToken)observationArray).getElement(i))
                   .doubleValue();
       } 
       
//       for (int i = 0; i < _nStates; i++) {
//                   
//                   _stDeviationEstimate[i] = ((DoubleToken)((ArrayToken) stdToken).getElement(i))
//                           .doubleValue();
//                   _meanEstimate[i] = ((DoubleToken)((ArrayToken) meanToken).getElement(i))
//                           .doubleValue();
//                   _priors[i] = ((DoubleToken)((ArrayToken) priorToken).getElement(i))
//                           .doubleValue();
//                   if(_modelType == _HMM){
//                   for(int j = 0; j< _nStates; j++){
//                       _transitionMatrixEstimate[i][j] = ((DoubleToken)((MatrixToken) transToken)
//                               .getElementAsToken(i, j))
//                               .doubleValue();
//                   }
//                   }
//               }
   }

   public static final int[] gaussianClassifyHMM(int startAt, double[] y, double[][] A, double[] mu, double[] sigma, double[] prior)
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
       
       // the maximum gamma determines the classification.
       int[] clusterAssignments = new int[y.length];
       for( int t = 0; t < y.length; t++){
           int maxState = 0;
           for( int j = 1; j < nStates; j++){
               if ( gamma[t][j] > gamma[t][maxState]){
                   maxState = j;
               }
           }
           
           clusterAssignments[t] = maxState;
       }
       return clusterAssignments;
   }
   
   public static final int[] gaussianClassifyMM(int startAt, double[] y, double[] mu, double[] sigma, double[] prior)
   {
       int nStates = mu.length;
       // the soft assignments for the observations  given the parameter estimates
       double[][] tau = new double[y.length][nStates];
       // the classified states of observations
       int[] clusterAssignments = new int[y.length];
       
       for(int t=0; t< y.length ; t++){
           for (int i=0; i < nStates; i++){
               tau[t][i] = prior[i]*gaussian(y[t], mu[i],sigma[i]);
               if(tau[t][i] > tau[t][clusterAssignments[t]]){
                   clusterAssignments[t] = i;
               }
           }
       }
      
       return clusterAssignments;
   }
   
   
   private static final double gaussian(double x, double mu, double sigma){
       
       return 1.0/(Math.sqrt(2*Math.PI)*sigma)*Math.exp(-0.5*Math.pow((x-mu)/sigma, 2));
   }

   ///////////////////////////////////////////////////////////////////
   ////                         private variables                 ////
   
   private int _classificationLength;

   private double[] _observations;
   
   private double[] _meanEstimate;
   
   private int _nStates;

   private double[] _priors;

   private double[] _stDeviationEstimate;
   
   private double[][] _transitionMatrixEstimate;

   protected static final int _GAUSSIAN = 0;
   protected static final int _RICIAN = 1;
   protected static final int _EXPONENTIAL = 2;
   protected static final int _MULTINOMIAL = 3;
   
   protected static final int _HMM = 50;
   protected static final int _MM = 51;
   
   protected int _distribution;
   
   protected int _modelType;
}
