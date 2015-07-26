/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2015 The Regents of the University of California.
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

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
*/
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
