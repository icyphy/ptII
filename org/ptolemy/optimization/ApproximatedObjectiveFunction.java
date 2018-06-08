/*
 Copyright (c) 2015-2018 The Regents of the University of California.
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

import ptolemy.math.DoubleMatrixMath;

/**
 * The class which defines the objective function and constraint functions.
 * This class approximate given objective function as a QP problem.
 * BarrierMethod calls method "calcFuncInternal" during optimization.
 *
@author Shuhei Emoto
@version $Id$
@since Ptolemy II 11.0
@Pt.ProposedRating Red (shuhei)
@Pt.AcceptedRating
 */
/**
 * The class of approximated objective function.
 * In this class, non-linear multivariate function is approximated
 * using hessians and gradients.
 * F0(X+dx) = F0(X) + 1/2 * dxT*H0*dx + g0*dx
 * Fi(X+dx) = Fi(X) + gi*dx
 * @author shuhei emoto
 */
class ApproximatedObjectiveFunction extends ObjectiveFunction {
    public ApproximatedObjectiveFunction(ObjectiveFunction a_source) {
        super(a_source.currentX.length, a_source.fiResults.length);
        _source = a_source;
        for (int i = 0; i < _source.currentX.length; i++) {
            currentX[i] = _source.currentX[i];
        }

        // Copy Hessians
        for (int row = 0; row < f0Hessian.length; row++) {
            for (int col = 0; col < f0Hessian[row].length; col++) {
                f0Hessian[row][col] = _source.f0Hessian[row][col];
            }
        }
        for (int i = 0; i < fiResults.length; i++) {
            for (int row = 0; row < fiHessians[i].length; row++) {
                for (int col = 0; col < fiHessians[i][row].length; col++) {
                    fiHessians[i][row][col] = _source.fiHessians[i][row][col];
                }
            }
        }
        iterationCounter = 0;
    }

    @Override
    public boolean calcFunction(double[] x) {
        return false;
    }

    /**
     * Objective function called in QPSolver.
     * @param x : input variables
     */
    @Override
    public void calcFuncInternal(double[] x) {
        if (_source.stopRequested) {
            stopRequested = true;
            return;
        }
        double[] dx = new double[x.length];
        for (int i = 0; i < x.length; i++) {
            currentX[i] = x[i];
            dx[i] = x[i] - _source.currentX[i];
        }

        /////////////////////////////////////////
        ///// result of objective function///////
        // f(x+dx) = f(x)+(1/2)(dxT*H*dx)+g*dx
        double[] Qx = DoubleMatrixMath.multiply(dx, f0Hessian);
        f0Result = _source.f0Result;
        for (int i = 0; i < Qx.length; i++) {
            f0Result += (0.5 * Qx[i] * dx[i] + _source.f0Gradient[i] * dx[i]);
        }
        // df(x+dx) = df(x)+H*dx
        for (int col = 0; col < f0Gradient.length; col++) {
            f0Gradient[col] = _source.f0Gradient[col] + Qx[col];
        }
        // Hessian(x+dx) = Hessian(x) was already copied in the constructor.

        ///////////////////////////////////////////////////
        ////// Inequality constraints:
        for (int i = 0; i < fiResults.length; i++) {
            // fi(x+dx) = fi(x)+(1/2)(dxT*H*dx)+g*dx
            Qx = DoubleMatrixMath.multiply(dx, fiHessians[i]);
            fiResults[i] = _source.fiResults[i];
            for (int col = 0; col < dx.length; col++) {
                fiResults[i] += (0.5 * Qx[col] * dx[col]
                        + _source.fiGradients[i][col] * dx[col]);
            }
            // dfi(x+dx) = dfi(x)+H*dx
            for (int col = 0; col < f0Gradient.length; col++) {
                fiGradients[i][col] = _source.fiGradients[i][col] + Qx[col];
            }
        }
        // Hessian_i(x+dx) were initialized in the constructor.

        iterationCounter++;
    }

    /*
     * Private variables
     */
    private ObjectiveFunction _source;
}
