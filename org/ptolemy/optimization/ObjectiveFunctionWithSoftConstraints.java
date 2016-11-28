/*
 Copyright (c) 2015-2016 The Regents of the University of California.
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

package org.ptolemy.optimization;

/**
 * The objective function class which is used in Phase 1 of the interior point method.
 * In the interior point method, a starting point X must be a feasible point that satisfies all
 * constraints. If the feasible point couldn't be found, the interior point method solve
 * the problem using this class treating the hard constraints as soft constraints.
 *
@author Shuhei Emoto
@version $Id$
@since Ptolemy II 11.0
@Pt.ProposedRating Red (shuhei)
@Pt.AcceptedRating
 */

public class ObjectiveFunctionWithSoftConstraints extends ObjectiveFunction {
    public ObjectiveFunctionWithSoftConstraints(ObjectiveFunction a_source) {
        super(a_source.currentX.length, 0);
        _source = a_source;

        for (int i=0; i<_source.currentX.length; i++) {
            currentX[i] = _source.currentX[i];
        }
    }

    @Override
    public boolean calcFunction(double[] x) {
        return false;
    }
    /**
     * Objective function called in optimization class.
     * @param x input variables
     */
    public void calcFuncInternal(double[] x) {
        if (_source.stopRequested) {
            stopRequested = true;
            return;
        }
        for (int i=0; i<currentX.length; i++) {
            currentX[i] = x[i];
        }
        _source.calcFuncInternal(x);
        iterationCounter = _source.iterationCounter;
        //objective function: f(X,S) = sum(conditional_fi^2)
        f0Result = 0;
        for (int i=0; i<_source.fiResults.length; i++) {
            if (_source.fiResults[i] <= 0.0) continue;
            f0Result += (_source.fiResults[i] * _source.fiResults[i]);
        }

        //df(X,S) = sum(2 * conditional_fi * dfi/dx)
        for (int col=0; col<f0Gradient.length; col++) {
            f0Gradient[col] = 0;
        }
        for (int i=0; i<_source.fiResults.length; i++) {
            if (_source.fiResults[i]<=0.0) continue;
            double scaler = 2.0 * _source.fiResults[i];
            for (int col=0; col<f0Gradient.length; col++) {
                f0Gradient[col] += scaler * _source.fiGradients[i][col];
            }
        }

        // Hessian = sum(2 * dfi/dxT * dfi/dx + 2 * conditional_fi * Hess_i)
        clearMatrix(f0Hessian);
        for (int i=0; i<_source.fiResults.length; i++) {
            if (_source.fiResults[i]<=0.0) continue;
            double scaler = 2.0 * _source.fiResults[i];
            for (int row=0; row < f0Hessian.length; row++) {
                for (int col=0; col < f0Hessian[0].length; col++) {
                    f0Hessian[row][col] += 2.0 * _source.fiGradients[i][row] * _source.fiGradients[i][col];
                    f0Hessian[row][col] += scaler * _source.fiHessians[i][row][col];
                }
            }
        }
    }


    /*
     * Private variables
     */
    private ObjectiveFunction _source;
}
