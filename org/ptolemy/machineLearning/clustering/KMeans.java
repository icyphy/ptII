/** An observation classifier

Copyright (c) 2013-2015 The Regents of the University of California.
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
package org.ptolemy.machineLearning.clustering;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.math.DoubleArrayMath;

///////////////////////////////////////////////////////////////////
////ObservationClassifier

/**
<p>This actor performs the k-means clustering algorithm to cluster the elements
of a set of observations into k clusters. 

K-Means is a heuristic multi-way classification algorithm that is often used for
exploratory data analysis. 

@author Ilge Akkaya
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class KMeans extends TypedAtomicActor {

    public KMeans(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        input = new TypedIOPort(this,"input",true,false);


        clusters = new TypedIOPort(this, "clusters", false, true);
        clusters.setTypeEquals(new ArrayType(BaseType.INT));

        clusterCenters = new TypedIOPort(this, "clusterCenters", false, true); 

        numClusters = new Parameter(this, "numClusters");
        numClusters.setExpression("2");
        numClusters.setTypeEquals(BaseType.INT);

        distanceMeasure = new StringParameter(this, "distanceMeasure");
        distanceMeasure.addChoice("Euclidean");
        distanceMeasure.setExpression("Euclidean");

        _clusterCenters = new ArrayList();


    }
    /** The sequence of inputs to be classified. */
    public TypedIOPort input;

    /** Cluster indices for each element of the input. */
    public TypedIOPort clusters;

    /** Cluster centers. */
    public TypedIOPort clusterCenters;

    /** Number of clusters (k). */
    public Parameter numClusters;

    /** Distance measure to be used in computation. */
    public Parameter distanceMeasure;

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == numClusters) {
            _numClusters = ((IntToken)numClusters.getToken()).intValue();
        } else if (attribute == distanceMeasure) {
            String distMeasure = ((StringToken) distanceMeasure.getToken()).stringValue();
            switch (distMeasure.toLowerCase()) {
            case "euclidean": 
                _distanceMeasure = Metric.EUCLIDEAN; 
                break;
            default: 
                throw new IllegalActionException(this, 
                        "Selected distance measure not supported.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        KMeans newObject = (KMeans) super
                .clone(workspace);
        newObject._clusterCenters = null;
        newObject._prevClusterAssignment = null;
        newObject._clusterAssignment = null; 
        newObject._trainingData = null;
        return newObject;
    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            ArrayToken inputToken = (ArrayToken) input.get(0);
            if ( inputToken.getElementType().isCompatible(BaseType.DOUBLE)) {
                _trainingData = new double[inputToken.length()][1];
                _featureLength = 1;
                for (int i = 0 ; i < inputToken.length(); i++) {
                    _trainingData[i][0] = ((DoubleToken)inputToken.getElement(i)).doubleValue();
                }
            } else if ( inputToken.getElementType().isCompatible(new ArrayType(BaseType.DOUBLE))) {
                _featureLength = ((ArrayToken)(inputToken.getElement(0))).length();
                _trainingData = new double[inputToken.length()][_featureLength];
                for (int i = 0 ; i < inputToken.length(); i++) {
                    for (int j = 0 ; j < _featureLength; j++) {
                        _trainingData[i][j] = ((DoubleToken)((ArrayToken)
                                inputToken.getElement(i)).getElement(j))
                                .doubleValue();
                    }
                }
            }

            boolean converged = false;

            _initializeClusterCenters();
            int iters = 0;
            while (!converged && iters < MAX_ITER) {
                _assignSamplesToClusters();
                _updateClusterCenters();
                converged = _checkForConvergence();
                iters ++;
            }

            // send the assigned clusters to the clusters port.

            IntToken[] clusterAssignments = new IntToken[_trainingData.length];
            for (int i=0; i< _clusterAssignment.length; i++) {
                clusterAssignments[i] = new IntToken(_clusterAssignment[i]);
            }
            ArrayToken clustersToken = new ArrayToken(clusterAssignments);
            clusters.broadcast(clustersToken);
            System.out.println("K-Means Converged in " + iters + " iterations.");

            // output cluster centers


            if (_featureLength == 1) {
                DoubleToken[] clusterCentersToken  = new DoubleToken[_numClusters];
                for (int i = 0; i < _numClusters; i++) {
                    clusterCentersToken[i] = new DoubleToken((double)_clusterCenters.get(i));
                }
                clusterCenters.send(0, new ArrayToken(clusterCentersToken));
            } else {
                ArrayToken[] clusterCentersToken  = new ArrayToken[_numClusters];
                for (int i = 0; i < _numClusters; i++) {
                    double [] clusterCenter = (double[]) _clusterCenters.get(i);
                    DoubleToken [] cToken = new DoubleToken[_featureLength];
                    for (int k=0; k < _featureLength; k++) {
                        cToken[k] = new DoubleToken(clusterCenter[k]);
                    }
                    clusterCentersToken[i] = new ArrayToken(cToken); 
                }
                clusterCenters.send(0, new ArrayToken(clusterCentersToken)); 
            }

        } 
    }


    private void _assignSamplesToClusters() {
        _clusterAssignment = new int[_trainingData.length];
        double [] minDistance = new double[_trainingData.length];
        Arrays.fill(minDistance, Double.MAX_VALUE);


        for ( int k = 0; k < _numClusters; k++) {
            for (int j = 0; j < _trainingData.length; j++){
                double [] sample = _trainingData[j];
                double distanceToCluster = distance(sample, (double[])_clusterCenters.get(k));
                if (distanceToCluster < minDistance[j]) {
                    minDistance[j] = distanceToCluster;
                    _clusterAssignment[j] = k;
                }
            }
        }
    }
    private double distance(double[] sample, double[] center) {
        switch (_distanceMeasure) {
        case EUCLIDEAN: return Math.sqrt(DoubleArrayMath.
                l2norm(DoubleArrayMath.subtract(sample, center)));
        default: return Double.NaN;
        } 
    }


    private boolean _checkForConvergence() {
        if (_prevClusterAssignment == null) {
            _prevClusterAssignment = new int[_trainingData.length];
            return false;
        } else {
            boolean equal = true;
            for (int i = 0; i < _clusterAssignment.length ; i++) {
                if (_clusterAssignment[i]!=_prevClusterAssignment[i]) {
                    equal = false;
                    break;
                }
            }
            if (equal) {
                return true;
            } else {
                _prevClusterAssignment = Arrays.copyOf(_clusterAssignment,
                        _clusterAssignment.length);
            }
        }
        return false;   
    }


    /**
     * Pick k random cluster centers given the range of input data.
     * This implementation uses the Random Partition method for initialization, 
     * that is, it assigns a cluster at random to each data point, and computes 
     * initial cluster centers. 
     * @throws IllegalActionException 
     *
     */
    private void _initializeClusterCenters() 
            throws IllegalActionException {
        //TODO: Forgy initialization algorithm to be added as an option
        Random clusterIndex = new Random();
        _clusterAssignment = new int[_trainingData.length];
        for (int k = 0; k < _trainingData.length; k++) {
            _clusterAssignment[k] = clusterIndex.nextInt(_numClusters);
        }
        _updateClusterCenters(); 
    }

    /**
     * Compute the new cluster centers, that is, the centroid of the data points
     * that belong to this cluster. For the Euclidean distance measure, this is 
     * simply the average of the points in the cluster.
     * @throws IllegalActionException 
     */
    private void _updateClusterCenters() 
            throws IllegalActionException {
        _clusterCenters.clear();

        switch (_distanceMeasure) {
        case EUCLIDEAN: 
            double [][] newClusterCenters = new double[_numClusters][_featureLength];
            int[] samplesInCluster = new int[_numClusters];
            for (int i=0; i < _trainingData.length; i++) {
                for (int j = 0 ; j < _featureLength; j++) {
                    newClusterCenters[_clusterAssignment[i]][j] += _trainingData[i][j]; 
                }   
                samplesInCluster[_clusterAssignment[i]] ++;
            }

            for (int i=0; i < _numClusters; i++) {
                for (int j = 0 ; j < _featureLength; j++) {
                    if ( samplesInCluster[i] > 0) {
                        newClusterCenters[i][j]/= samplesInCluster[i];
                    }
                }
            }
            for (int i=0; i < _numClusters; i++) {
                if (_featureLength > 1) {
                    _clusterCenters.add(newClusterCenters[i]);
                } else {
                    _clusterCenters.add(newClusterCenters[i][0]);
                } 
            }
            break;
        default: throw new IllegalActionException(this, "Distance measure not supported.");
        }  
    }
    @Override
    public void wrapup() throws IllegalActionException {
        _clusterCenters.clear();
        _clusterAssignment = null;
        _prevClusterAssignment = null;
        _trainingData = null;
        super.wrapup();  
    }
    private double[][] _trainingData;
    private int[] _clusterAssignment;
    private int[] _prevClusterAssignment;
    private int _featureLength;
    private List _clusterCenters;
    private int _numClusters;
    private Metric _distanceMeasure;
    private final static int MAX_ITER = 10000;
    private enum Metric {
        EUCLIDEAN
    };
}
