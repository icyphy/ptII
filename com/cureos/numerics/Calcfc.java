/*
 * jcobyla
 *
 * The MIT License
 *
 * Copyright (c) 2012 Anders Gustafsson, Cureos AB.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Remarks:
 *
 * The original Fortran 77 version of this code was by Michael Powell (M.J.D.Powell @ damtp.cam.ac.uk)
 * The Fortran 90 version was by Alan Miller (Alan.Miller @ vic.cmis.csiro.au). Latest revision - 30 October 1998
 */
package com.cureos.numerics;

import ptolemy.kernel.util.IllegalActionException;

/**
 * Interface for calculation of objective function and constraints in COBYLA2 optimization.
 *
 * @author Anders Gustafsson, Cureos AB.
 */
public interface Calcfc {
    /**
     * The objective and constraints function evaluation method used
     * in COBYLA2 minimization.
     * @param n Number of variables.
     * @param m Number of constraints.
     * @param x Variable values to be employed in function and constraints calculation.
     * @param con Calculated function values of the constraints.
     * @param terminate A boolean array that provides the terminate state. Only the first element of the array is read
     * @return Calculated objective function value.
     * @exception IllegalActionException If there is a problem
     * calculating the COBYLA2 minimization.
     */
    double Compute(int n, int m, double[] x, double[] con, boolean[] terminate)
            throws IllegalActionException;

}
