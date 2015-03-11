/* Hidden Nonhomogeneous Semi-Markov Model Estimator with hourly transition probabilities.

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
package org.ptolemy.machineLearning.hsmm;

import java.util.ArrayList;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DateToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException; 

///////////////////////////////////////////////////////////////////
////ExpectationMaximization

/**
<p>This actor implements a parameter estimator for a Hidden Semi-Markov Model with Multinomial
Emissions. The transition probability matrix is estimated on an hourly basis.

 @author Ilge Akkaya
 @version $Id $
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class HSMMTimeAwareMultinomialEstimator extends HSMMMultinomialEstimator {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public HSMMTimeAwareMultinomialEstimator(CompositeEntity container,
            String name) throws NameDuplicationException,
            IllegalActionException {
        super(container, name);
        timestamps = new TypedIOPort(this, "timestamps", true, false);
        timestamps.setTypeEquals(new ArrayType(BaseType.INT));
        empiricalStartTimes = new TypedIOPort(this, "empiricalStartTimes", false, true);
        empiricalStartTimes.setTypeEquals(new ArrayType(BaseType.DOUBLE_MATRIX)); 

        transitionMatrixEstimationMethod = new StringParameter(this,
                "transitionMatrixEstimationMethod");
        transitionMatrixEstimationMethod.setExpression(INTERPOLATE);
        transitionMatrixEstimationMethod.addChoice(INTERPOLATE);
        transitionMatrixEstimationMethod.addChoice(FORCE_SELF); 
        transitionMatrixEstimationMethod.addChoice(FORCE_ZERO);
        transitionMatrixEstimationMethod.addChoice(NO_ACTION);

    }

    /** Array of observation timestamps as UNIX timestamps */
    public TypedIOPort timestamps;

    /** Transition Matrix partitioning options. 
     * Force self transition asserts a self transition with probability 1 
     * if no information has been learned for the state. Interpolate: Assigns
     * uniform probabilities to any state that has Hamming distance less than 
     * two to the current state. */
    public Parameter transitionMatrixEstimationMethod;

    /** Array of estimated probability transition matrices for each hour. */
    public TypedIOPort empiricalStartTimes;

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (timestamps.hasToken(0)) { 
            Token[] tsTokens = ((ArrayToken)timestamps.get(0)).arrayValue();
            if (tsTokens.length != clusters.length) {
                throw new IllegalActionException("Timestamp array length must be equal"
                        + "to the training sequence length.");
            } 

            // generate a start time distribution given the timestamps.
            int[] hourOfDay = new int[tsTokens.length];
            for ( int i = 0; i < tsTokens.length ; i++) { 
                DateToken dt = new DateToken(((IntToken)tsTokens[i]).intValue(),
                        DateToken.PRECISION_SECOND);
                hourOfDay[i] = dt.getHour();
            }
            // find the transition times in the cluster assignments and build an 
            // empirical distribution for the state transition times.

            // hourly empirical distributions
            At = new double[NUM_CATEGORIES][_nStates][_nStates]; 
            int prevState = clusters[0];
            for (int i = 1 ; i < clusters.length; i++) {
                if (clusters[i] != prevState) {
                    //transition
                    At[hourOfDay[i]][prevState][clusters[i]]++; 
                }
                prevState = clusters[i];
            }
            Token[] Atokens = new Token[NUM_CATEGORIES];
            
            // for each hour, set the learned matrices
            for (int i = 0 ; i < NUM_CATEGORIES; i++) {
                for (int j = 0 ; j < _nStates; j++) { 
                    double sum = 0.0;
                    for (int k=0; k < _nStates; k ++) { 
                        sum += At[i][j][k];
                    }
                    
                    if ( sum > 0 ) {
                        for (int k=0; k < _nStates; k ++) { 
                            At[i][j][k]/= sum;
                        }
                    } else {  
                        String method = transitionMatrixEstimationMethod.getExpression();
                        switch (method) {
                        case INTERPOLATE:
                            ArrayList<Integer> allowedTransitionIndices = new ArrayList<Integer>();
                            for (int b = 0; b < _nStates; b ++) {
                                // bitCount o the xor gives us the hamming distance
                                // i.e., the number of bits that differ among x and b
                                if (_bitCount(j ^ b) <=1) {
                                    allowedTransitionIndices.add(b);
                                }
                            }
                            for (int b : allowedTransitionIndices) { 
                                At[i][j][b] = 1.0/allowedTransitionIndices.size();
                            } 
                            break; 
                        case FORCE_SELF:
                            At[i][j][j] = 1.0;
                            break;
                        case FORCE_ZERO:
                            At[i][j][0] = 1.0;
                            break;
                        case NO_ACTION:
                            break;
                        default:
                            At[i][j][0] = 1.0;
                            break;
                        }  
                    } 
                }
                Atokens[i] = new DoubleMatrixToken(At[i]);
            }
            empiricalStartTimes.send(0, new ArrayToken(Atokens));
        }
    }
    /**
     * MIT HAKMEM Count algorithm.
     * @param u
     * @return
     */
    private int _bitCount( int xor)
    {
        int oneCount = 0; 
        oneCount = xor - ((xor >> 1) & 033333333333) 
                - ((xor >> 2) & 011111111111);
        return ((oneCount + (oneCount >> 3)) & 030707070707) % 63;
    }

    /** Number of partitions in the probability transition matrix. */
    private final int NUM_CATEGORIES = 24;

    private final String INTERPOLATE = "Interpolate";
    private final String FORCE_SELF = "Force self-transition";
    private final String FORCE_ZERO = "Force transition to state 0";
    private final String NO_ACTION = "No action";
    
    /** Time-dependent transition prob .matrix*/
    protected double[][][] At;


}
