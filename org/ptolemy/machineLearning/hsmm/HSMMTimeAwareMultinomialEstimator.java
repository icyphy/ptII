/* Explicit-duration Hidden-Markov Model Estimator with hourly transition matrix
 * estimates.

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
import java.util.HashSet;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import ptolemy.actor.NoRoomException;
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
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException; 

///////////////////////////////////////////////////////////////////
////

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
        transitionMatrixEstimationMethod.addChoice(SELF_AND_ZERO);

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

    public void attributeChanged(Attribute attribute) throws IllegalActionException {
        if (attribute == transitionMatrixEstimationMethod) {
            _method = transitionMatrixEstimationMethod.getExpression();
        } else {
            super.attributeChanged(attribute);
        }
    }
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        TimeZone tz = new SimpleTimeZone(0,"GMT");

        if (timestamps.hasToken(0)) { 
            Token[] tsTokens = ((ArrayToken)timestamps.get(0)).arrayValue();
            if (tsTokens.length != clusters.length) {
                throw new IllegalActionException("Timestamp array length must be equal"
                        + "to the training sequence length.");
            } 

            // generate a start time distribution given the timestamps.
            _hourOfDay = new int[tsTokens.length];
            for ( int i = 0; i < tsTokens.length ; i++) { 
                DateToken dt = new DateToken(((IntToken)tsTokens[i]).intValue(),
                        DateToken.PRECISION_SECOND,tz); 
                _hourOfDay[i] = dt.getHour();
            }
            // find the transition times in the cluster assignments and build an 
            // empirical distribution for the state transition times.

            // hourly empirical distributions

            // learn transitions from data.
            _learnAt();

            At = new double[NUM_CATEGORIES][_nStates][_nStates];
            for (int h = 0; h < NUM_CATEGORIES; h++) {
                for (int i =0; i < _nStates; i++) {
                    for(int j=0; j< _nStates; j++) {
                        At[h][i][j] = Atlearned[h][i][j];
                    }
                }
            }

            for (int i = 0 ; i < NUM_CATEGORIES; i++) {
                _calculateTransitionScheme(_method,i); 
            }

            _sendEmpiricalMatrix();
        }
    }
    
    /**
     * Send the learned matrix to the output.
     * @throws NoRoomException
     * @throws IllegalActionException
     */
    public void _sendEmpiricalMatrix() throws NoRoomException, IllegalActionException {

        Token[] Atokens = new Token[NUM_CATEGORIES];
        for (int i = 0 ; i < NUM_CATEGORIES; i++) { 
            Atokens[i] = new DoubleMatrixToken(At[i]);
        }

        empiricalStartTimes.send(0, new ArrayToken(Atokens));
    }
    
    /**
     * Learn the transition probability matrix for each hour, from timestamped data.
     */
    protected void _learnAt() {
        incompleteCategories = new HashSet<int[]>();
        Atlearned = new double[NUM_CATEGORIES][_nStates][_nStates]; 
        int prevState = clusters[0];
        for (int i = 1 ; i < clusters.length; i++) {
            if (clusters[i] != prevState) {
                //transition
                Atlearned[_hourOfDay[i]][prevState][clusters[i]]++; 
            }
            prevState = clusters[i];
        }

        // for each hour, set the learned matrices
        for (int i = 0 ; i < NUM_CATEGORIES; i++) {
            for (int j = 0 ; j < _nStates; j++) { 
                double sum = 0.0;
                for (int k=0; k < _nStates; k ++) { 
                    sum += Atlearned[i][j][k];
                } 
                if ( Math.abs(sum) > 1E-5) {
                    for (int k=0; k < _nStates; k ++) { 
                        Atlearned[i][j][k]/= sum;
                    }
                } else { 
                    int[] cat = {i,j}; // at category i, from state j, not enough info.
                    incompleteCategories.add(cat); 
                }
            }
        }
    }

    protected void _calculateTransitionScheme(String method, int category) {
        double[][] Asub = At[category];
        for (int[] A : incompleteCategories) {
            if (A[0] == category) { 
                int j = A[1];
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
                        Asub[j][b] = 1.0/allowedTransitionIndices.size();
                    } 
                    break; 
                case FORCE_SELF:
                    Asub[j][j] = 1.0;
                    break;
                case FORCE_ZERO:
                    Asub[j][0] = 1.0;
                    break;
                case NO_ACTION:
                    break;
                case SELF_AND_ZERO:
                    if (j==0) {
                        Asub[j][j] = 1.0;
                    } else {
                        Asub[j][0] = 0.5;
                        Asub[j][j] = 0.5;
                    }
                    break;
                default:
                    Asub[j][0] = 1.0;
                    break;
                }  
            }  
        }
        At[category] = Asub;
    }
    
    /**
     * Count number of 1's in integer's bit representation
     * (MIT HAKMEM Count algorithm.)
     * @param xor the input integer
     * @return Number of 1's in bit representation
     */
    private int _bitCount( int xor)
    {
        int oneCount = 0; 
        oneCount = xor - ((xor >> 1) & 033333333333) 
                - ((xor >> 2) & 011111111111);
        return ((oneCount + (oneCount >> 3)) & 030707070707) % 63;
    }

    /** Number of partitions in the probability transition matrix. */
    protected final int NUM_CATEGORIES = 24;

    /** Completion strategy for A set to interpolation, that is, a uniform distribuition 
     * on all states that have
     * Hamming distance <= 1 to the binary representation of the source state. */
    protected static final String INTERPOLATE = "Interpolate";
    /** Completion strategy for A set to forcing a self-transition. */
    protected static final String FORCE_SELF = "Force self-transition";
    /** Completion strategy for A set to forcing a transition to state 0.*/
    protected static final String FORCE_ZERO = "Force transition to state 0";
    /** No completion strategy. */
    protected static final String NO_ACTION = "No action";
    /** Completion strategy for A set to self and zero transitions with equal probability. */
    protected static final String SELF_AND_ZERO = "Self and Zero";

    /** Time-dependent transition probability matrix*/
    protected double[][][] At;
    
    /** The learned transition probability matrix: before completion strategy is applied. */
    protected double[][][] Atlearned;

    /** Hour categories for which At has not enough information. */
    protected Set<int[]> incompleteCategories; 
    
    /** hour of day for input observations. */
    protected int[] _hourOfDay; 
    String _method;



}
