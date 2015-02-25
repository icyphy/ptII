package org.ptolemy.machineLearning.test.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.ptolemy.machineLearning.Algorithms;

public class AlgorithmsTest {
     
    
    @Test
    public void testBinaryIntervalSearch() {
        double[] A = {0.0, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0}; 
        double[] keysToSearch = {-0.1,0.1,0.55,0.65,0.75,0.83,0.91,2.0};
        int[] keyIndicesExpected = {-1, 0 ,1,2,3,4,5,-1};
        int[] keyIndicesFound = new int[keysToSearch.length];
        for ( int i = 0; i < keysToSearch.length; i++) {
            keyIndicesFound[i] = Algorithms._binaryIntervalSearch(A, 
                    keysToSearch[i]); 
            
            assertEquals(keyIndicesFound[i], keyIndicesExpected[i]);
        }  
    }
    
    @Test
    public void testMvnPdf() {
       double [] y = {1.0,2.0};
       double [] mu = {1.0,1.0};
       double [][] sigma = {{1.0,0.0},{0.0,1.0}}; 
       assertTrue( Math.abs(Algorithms.mvnpdf(y,mu,sigma) - 0.096532352630054) < 1E-4);
    }
    
     

}
