

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
package org.ptolemy.machineLearning.hmm;

import java.util.HashMap;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements a parameter estimator for Hidden Markov Models with Multinomial
Emissions. The base class ParameterEstimator performs the parameter estimation and
the HMMGaussianEstimator class contains density-specific methods for Multinomial emission
calculations and produces the relevant estimates at its output ports.</p>
<p>
The output ports for a Gaussian HMM model are the <i>mean<\i> and the <i>standardDeviation<\i>
vectors of the possible hidden states in addition to the HMM parameters independent
from the emission density: <i>transitionMatrix<\i> .
T
he <i>mean<\i>  is a double array output containing the mean estimates and 
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
If, during iteration, the conditional log-likelihood of the observed 
sequence given the parameter estimates converges to a value within <i>likelihoodThreshold</i>,
the parameter estimation stops iterating and delivers the parameter estimates.

 @author Ilge Akkaya
 @version  
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating 
 */
public class HMMMultinomialEstimator extends ParameterEstimator {
   /** Construct an actor with the given container and name.
    *  @param container The container.
    *  @param name The name of this actor
    *  @exception IllegalActionException If the actor cannot be contained
    *   by the proposed container.
    *  @exception NameDuplicationException If the container already has an
    *   actor with this name.
    */
   public HMMMultinomialEstimator(CompositeEntity container, String name)
           throws NameDuplicationException, IllegalActionException {
       super(container, name);
       
       emissionEstimates = new TypedIOPort(this, "observationProbabilities", false, true);
       emissionEstimates.setTypeEquals(BaseType.DOUBLE_MATRIX);
        
       observationProbabilities = new Parameter(this, "observationProbabilities");
       observationProbabilities.setExpression("[0.6,0.3,0.1;0.1,0.4,0.5]");
       observationProbabilities.setTypeEquals(BaseType.DOUBLE_MATRIX);
       
       nCategories = new Parameter(this, "numberOfCategoriesObserved");
       nCategories.setExpression("3");
       nCategories.setTypeEquals(BaseType.INT);
       _nCategories = 3;
       
       _B = new double[_nStates][_nCategories];
       _B0 = new double[_nStates][_nCategories]; 
   }
   
   public void attributeChanged(Attribute attribute)
           throws IllegalActionException {
       if(attribute == observationProbabilities){
           int nStates = ((MatrixToken) observationProbabilities.getToken()).getRowCount();
           int nCat = ((MatrixToken) observationProbabilities.getToken()).getColumnCount();
           for (int i = 0; i < nStates; i++) {
               for(int j = 0; j< nCat; j++){
                   _B0[i][j] = ((DoubleToken)((MatrixToken) observationProbabilities.getToken())
                           .getElementAsToken(i, j))
                           .doubleValue();
               }
           }
           
       } else if( attribute == nCategories)
       {
           int cat = ((IntToken) nCategories.getToken()).intValue();
           if(cat <= 0){
               throw new IllegalActionException(this, "Number of categories must be positive");
           } else{
               _nCategories = cat;
           }
       }
       else{
           super.attributeChanged(attribute);
       }
   }

   ///////////////////////////////////////////////////////////////////
   ////                         public variables                  ////

   public TypedIOPort emissionEstimates;
   
   public Parameter observationProbabilities;
   
   public Parameter nCategories;
   
   ///////////////////////////////////////////////////////////////////
   ////                         public methods                    ////

   // the function to specifically define the emission probabilities
   public void fire() throws IllegalActionException { 
       super.fire(); 
       
       if( (_nStates != _transitionMatrix.length) || (_nStates != _priors.length) || (_nCategories != _B0[0].length))
       {
           throw new IllegalActionException(this, "Parameter guess vectors cannot have different lengths.");
       } 
       
       HashMap newEstimates = new HashMap(); 
       // reset the initial values to the parameter values 
       _transitionMatrix = _A0;
       _B = _B0;
       
       double[][] A_new = new double[_nStates][_nStates];
       double[][] B_new = new double[_nStates][_nCategories];
          
       for( int iterations = 0; iterations< _nIterations; iterations++){
            newEstimates = HMMAlphaBetaRecursion(_observations, _transitionMatrix, _priors, _nCategories); 
            B_new = (double[][]) newEstimates.get("eta_hat"); 
            A_new = (double[][]) newEstimates.get("A_hat"); 
            
               _transitionMatrix  = A_new; 
               _B = B_new;
               
               double likelihood = (Double) (newEstimates.get("likelihood"));
               
               if( (likelihood - _likelihood) < _likelihoodThreshold || (likelihood - _likelihood) < 0.0 ){
                   break;
               } else{
                   _likelihood = likelihood;
               } 
           }  
           // broadcast best-effort parameter estimates
           
           double[] pi_new = (double[]) newEstimates.get("pi_hat");
           Token[] pTokens = new Token[_nStates];
           for ( int i = 0; i< _nStates; i++){ 
               pTokens[i] = new DoubleToken(pi_new[i]);
           }
           transitionMatrix.send(0, new DoubleMatrixToken(A_new));
           emissionEstimates.send(0, new DoubleMatrixToken(B_new));
           priorEstimates.send(0, new ArrayToken(pTokens)); 
   }
   
   public boolean postfire() throws IllegalActionException {
       _likelihood = 0.0;
       return true;
   }
   protected double emissionProbability(double y, int hiddenState){
       return _B[hiddenState][(int)y];  
   }
   
// emission distributions Bij = P(Yt=j | qt = i)
private double[][] _B;
private double[][] _B0;
private int _nCategories;
}
