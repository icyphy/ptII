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

/**
 * Enumeration of exit statuses associated with COBYLA2 optimization.
 *
 * @author Anders Gustafsson, Cureos AB.
 */
public enum CobylaExitStatus {
    /**
     * Optimization successfully completed.
     */
    Normal,

    /**
     * Maximum number of iterations (function/constraints evaluations) reached during optimization.
     */
    MaxIterationsReached,

    /**
     * Size of rounding error is becoming damaging, terminating prematurely.
     */
    DivergingRoundingErrors,

    /**
     * Termination requested.
     */
    TerminateRequested
}
