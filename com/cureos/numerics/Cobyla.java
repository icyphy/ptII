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
 * Constrained Optimization BY Linear Approximation in Java.
 *
 * COBYLA2 is an implementation of Powell's nonlinear derivative-free constrained optimization that uses
 * a linear approximation approach. The algorithm is a sequential trust-region algorithm that employs linear
 * approximations to the objective and constraint functions, where the approximations are formed by linear
 * interpolation at n + 1 points in the space of the variables and tries to maintain a regular-shaped simplex
 * over iterations.
 *
 * It solves non-smooth NLP with a moderate number of variables (about 100). Inequality constraints only.
 *
 * The initial point X is taken as one vertex of the initial simplex with zero being another, so, X should
 * not be entered as the zero vector.
 *
 * @author Anders Gustafsson, Cureos AB.
 */
public class Cobyla {
    /**
     * Minimizes the objective function F with respect to a set of inequality constraints CON,
     * and returns the optimal variable array. F and CON may be non-linear, and should preferably be smooth.
     *
     * @param calcfc Interface implementation for calculating objective function and constraints.
     * @param n Number of variables.
     * @param m Number of constraints.
     * @param x On input initial values of the variables (zero-based array). On output
     * optimal values of the variables obtained in the COBYLA minimization.
     * @param rhobeg Initial size of the simplex.
     * @param rhoend Final value of the simplex.
     * @param iprint Print level, 0 &lt;= iprint &lt;= 3, where 0 provides no output and
     * 3 provides full output to the console.
     * @param maxfun Maximum number of function evaluations before terminating.
     * @return Exit status of the COBYLA2 optimization.
     * @exception IllegalActionException
     */
    public static CobylaExitStatus FindMinimum(final Calcfc calcfc, int n,
            int m, double[] x, double rhobeg, double rhoend, int iprint,
            int maxfun, boolean[] terminate) throws IllegalActionException {
        //     This subroutine minimizes an objective function F(X) subject to M
        //     inequality constraints on X, where X is a vector of variables that has
        //     N components.  The algorithm employs linear approximations to the
        //     objective and constraint functions, the approximations being formed by
        //     linear interpolation at N+1 points in the space of the variables.
        //     We regard these interpolation points as vertices of a simplex.  The
        //     parameter RHO controls the size of the simplex and it is reduced
        //     automatically from RHOBEG to RHOEND.  For each RHO the subroutine tries
        //     to achieve a good vector of variables for the current size, and then
        //     RHO is reduced until the value RHOEND is reached.  Therefore RHOBEG and
        //     RHOEND should be set to reasonable initial changes to and the required
        //     accuracy in the variables respectively, but this accuracy should be
        //     viewed as a subject for experimentation because it is not guaranteed.
        //     The subroutine has an advantage over many of its competitors, however,
        //     which is that it treats each constraint individually when calculating
        //     a change to the variables, instead of lumping the constraints together
        //     into a single penalty function.  The name of the subroutine is derived
        //     from the phrase Constrained Optimization BY Linear Approximations.

        //     The user must set the values of N, M, RHOBEG and RHOEND, and must
        //     provide an initial vector of variables in X.  Further, the value of
        //     IPRINT should be set to 0, 1, 2 or 3, which controls the amount of
        //     printing during the calculation. Specifically, there is no output if
        //     IPRINT=0 and there is output only at the end of the calculation if
        //     IPRINT=1.  Otherwise each new value of RHO and SIGMA is printed.
        //     Further, the vector of variables and some function information are
        //     given either when RHO is reduced or when each new value of F(X) is
        //     computed in the cases IPRINT=2 or IPRINT=3 respectively. Here SIGMA
        //     is a penalty parameter, it being assumed that a change to X is an
        //     improvement if it reduces the merit function
        //                F(X)+SIGMA*MAX(0.0, - C1(X), - C2(X),..., - CM(X)),
        //     where C1,C2,...,CM denote the constraint functions that should become
        //     nonnegative eventually, at least to the precision of RHOEND. In the
        //     printed output the displayed term that is multiplied by SIGMA is
        //     called MAXCV, which stands for 'MAXimum Constraint Violation'.  The
        //     argument ITERS is an integer variable that must be set by the user to a
        //     limit on the number of calls of CALCFC, the purpose of this routine being
        //     given below.  The value of ITERS will be altered to the number of calls
        //     of CALCFC that are made.

        //     In order to define the objective and constraint functions, we require
        //     a subroutine that has the name and arguments
        //                SUBROUTINE CALCFC (N,M,X,F,CON)
        //                DIMENSION X(:),CON(:)  .
        //     The values of N and M are fixed and have been defined already, while
        //     X is now the current vector of variables. The subroutine should return
        //     the objective and constraint functions at X in F and CON(1),CON(2),
        //     ...,CON(M).  Note that we are trying to adjust X so that F(X) is as
        //     small as possible subject to the constraint functions being nonnegative.

        // Local variables
        int mpp = m + 2;

        // Internal base-1 X array
        double[] iox = new double[n + 1];
        System.arraycopy(x, 0, iox, 1, n);

        // Internal representation of the objective and constraints calculation method,
        // accounting for that X and CON arrays in the cobylb method are base-1 arrays.
        Calcfc fcalcfc = new Calcfc() {
            /**
             *
             * @param n the value of n
             * @param m the value of m
             * @param x the value of x
             * @param con the value of con
             * @return the double
             */
            @Override
            public double Compute(int n, int m, double[] x, double[] con,
                    boolean[] terminate) throws IllegalActionException {
                double[] ix = new double[n];
                System.arraycopy(x, 1, ix, 0, n);
                double[] ocon = new double[m];
                double f = calcfc.Compute(n, m, ix, ocon, terminate);
                System.arraycopy(ocon, 0, con, 1, m);
                return f;
            }
        };

        CobylaExitStatus status = cobylb(fcalcfc, n, m, mpp, iox, rhobeg,
                rhoend, iprint, maxfun, terminate);
        System.arraycopy(iox, 1, x, 0, n);

        return status;
    }

    private static CobylaExitStatus cobylb(Calcfc calcfc, int n, int m,
            int mpp, double[] x, double rhobeg, double rhoend, int iprint,
            int maxfun, boolean[] terminate) throws IllegalActionException {
        // N.B. Arguments CON, SIM, SIMI, DATMAT, A, VSIG, VETA, SIGBAR, DX, W & IACT
        //      have been removed.

        //     Set the initial values of some parameters. The last column of SIM holds
        //     the optimal vertex of the current simplex, and the preceding N columns
        //     hold the displacements from the optimal vertex to the other vertices.
        //     Further, SIMI holds the inverse of the matrix that is contained in the
        //     first N columns of SIM.

        // Local variables

        CobylaExitStatus status;

        double alpha = 0.25;
        double beta = 2.1;
        double gamma = 0.5;
        double delta = 1.1;

        double f = 0.0;
        double resmax = 0.0;
        double total;

        int np = n + 1;
        int mp = m + 1;
        double rho = rhobeg;
        double parmu = 0.0;

        boolean iflag = false;
        boolean ifull = false;
        double parsig = 0.0;
        double prerec = 0.0;
        double prerem = 0.0;

        double[] con = new double[1 + mpp];
        double[][] sim = new double[1 + n][1 + np];
        double[][] simi = new double[1 + n][1 + n];
        double[][] datmat = new double[1 + mpp][1 + np];
        double[][] a = new double[1 + n][1 + mp];
        double[] vsig = new double[1 + n];
        double[] veta = new double[1 + n];
        double[] sigbar = new double[1 + n];
        double[] dx = new double[1 + n];
        double[] w = new double[1 + n];

        if (iprint >= 2) {
            System.out
                    .format("%nThe initial value of RHO is %13.6f and PARMU is set to zero.%n",
                            rho);
        }

        int nfvals = 0;
        double temp = 1.0 / rho;

        for (int i = 1; i <= n; ++i) {
            sim[i][np] = x[i];
            sim[i][i] = rho;
            simi[i][i] = temp;
        }

        int jdrop = np;
        boolean ibrnch = false;

        //     Make the next call of the user-supplied subroutine CALCFC. These
        //     instructions are also used for calling CALCFC during the iterations of
        //     the algorithm.

        L_40: do {
            if (terminate[0]) {
                status = CobylaExitStatus.TerminateRequested;
                break L_40;
            }
            if (nfvals >= maxfun && nfvals > 0) {
                status = CobylaExitStatus.MaxIterationsReached;
                break L_40;
            }

            ++nfvals;

            f = calcfc.Compute(n, m, x, con, terminate);
            resmax = 0.0;
            for (int k = 1; k <= m; ++k) {
                resmax = Math.max(resmax, -con[k]);
            }

            if (nfvals == iprint - 1 || iprint == 3) {
                PrintIterationResult(nfvals, f, resmax, x, n);
            }

            con[mp] = f;
            con[mpp] = resmax;

            //     Set the recently calculated function values in a column of DATMAT. This
            //     array has a column for each vertex of the current simplex, the entries of
            //     each column being the values of the constraint functions (if any)
            //     followed by the objective function and the greatest constraint violation
            //     at the vertex.

            boolean skipVertexIdent = true;
            if (!ibrnch) {
                skipVertexIdent = false;

                for (int i = 1; i <= mpp; ++i) {
                    datmat[i][jdrop] = con[i];
                }

                if (nfvals <= np) {
                    //     Exchange the new vertex of the initial simplex with the optimal vertex if
                    //     necessary. Then, if the initial simplex is not complete, pick its next
                    //     vertex and calculate the function values there.

                    if (jdrop <= n) {
                        if (datmat[mp][np] <= f) {
                            x[jdrop] = sim[jdrop][np];
                        } else {
                            sim[jdrop][np] = x[jdrop];
                            for (int k = 1; k <= mpp; ++k) {
                                datmat[k][jdrop] = datmat[k][np];
                                datmat[k][np] = con[k];
                            }
                            for (int k = 1; k <= jdrop; ++k) {
                                sim[jdrop][k] = -rho;
                                temp = 0.0;
                                for (int i = k; i <= jdrop; ++i) {
                                    temp -= simi[i][k];
                                }
                                simi[jdrop][k] = temp;
                            }
                        }
                    }
                    if (nfvals <= n) {
                        jdrop = nfvals;
                        x[jdrop] += rho;
                        continue L_40;
                    }
                }

                ibrnch = true;
            }

            L_140: do {
                L_550: do {
                    if (!skipVertexIdent) {
                        //     Identify the optimal vertex of the current simplex.

                        double phimin = datmat[mp][np] + parmu
                                * datmat[mpp][np];
                        int nbest = np;

                        for (int j = 1; j <= n; ++j) {
                            temp = datmat[mp][j] + parmu * datmat[mpp][j];
                            if (temp < phimin) {
                                nbest = j;
                                phimin = temp;
                            } else if (temp == phimin && parmu == 0.0
                                    && datmat[mpp][j] < datmat[mpp][nbest]) {
                                nbest = j;
                            }
                        }

                        //     Switch the best vertex into pole position if it is not there already,
                        //     and also update SIM, SIMI and DATMAT.

                        if (nbest <= n) {
                            for (int i = 1; i <= mpp; ++i) {
                                temp = datmat[i][np];
                                datmat[i][np] = datmat[i][nbest];
                                datmat[i][nbest] = temp;
                            }
                            for (int i = 1; i <= n; ++i) {
                                temp = sim[i][nbest];
                                sim[i][nbest] = 0.0;
                                sim[i][np] += temp;

                                double tempa = 0.0;
                                for (int k = 1; k <= n; ++k) {
                                    sim[i][k] -= temp;
                                    tempa -= simi[k][i];
                                }
                                simi[nbest][i] = tempa;
                            }
                        }

                        //     Make an error return if SIGI is a poor approximation to the inverse of
                        //     the leading N by N submatrix of SIG.

                        double error = 0.0;
                        for (int i = 1; i <= n; ++i) {
                            for (int j = 1; j <= n; ++j) {
                                temp = DOT_PRODUCT(PART(ROW(simi, i), 1, n),
                                        PART(COL(sim, j), 1, n))
                                        - (i == j ? 1.0 : 0.0);
                                error = Math.max(error, Math.abs(temp));
                            }
                        }
                        if (error > 0.1) {
                            status = CobylaExitStatus.DivergingRoundingErrors;
                            break L_40;
                        }

                        //     Calculate the coefficients of the linear approximations to the objective
                        //     and constraint functions, placing minus the objective function gradient
                        //     after the constraint gradients in the array A. The vector W is used for
                        //     working space.

                        for (int k = 1; k <= mp; ++k) {
                            con[k] = -datmat[k][np];
                            for (int j = 1; j <= n; ++j) {
                                w[j] = datmat[k][j] + con[k];
                            }

                            for (int i = 1; i <= n; ++i) {
                                a[i][k] = (k == mp ? -1.0 : 1.0)
                                        * DOT_PRODUCT(PART(w, 1, n),
                                                PART(COL(simi, i), 1, n));
                            }
                        }

                        //     Calculate the values of sigma and eta, and set IFLAG = 0 if the current
                        //     simplex is not acceptable.

                        iflag = true;
                        parsig = alpha * rho;
                        double pareta = beta * rho;

                        for (int j = 1; j <= n; ++j) {
                            double wsig = 0.0;
                            for (int k = 1; k <= n; ++k) {
                                wsig += simi[j][k] * simi[j][k];
                            }
                            double weta = 0.0;
                            for (int k = 1; k <= n; ++k) {
                                weta += sim[k][j] * sim[k][j];
                            }
                            vsig[j] = 1.0 / Math.sqrt(wsig);
                            veta[j] = Math.sqrt(weta);
                            if (vsig[j] < parsig || veta[j] > pareta) {
                                iflag = false;
                            }
                        }

                        //     If a new vertex is needed to improve acceptability, then decide which
                        //     vertex to drop from the simplex.

                        if (!ibrnch && !iflag) {
                            jdrop = 0;
                            temp = pareta;
                            for (int j = 1; j <= n; ++j) {
                                if (veta[j] > temp) {
                                    jdrop = j;
                                    temp = veta[j];
                                }
                            }
                            if (jdrop == 0) {
                                for (int j = 1; j <= n; ++j) {
                                    if (vsig[j] < temp) {
                                        jdrop = j;
                                        temp = vsig[j];
                                    }
                                }
                            }

                            //     Calculate the step to the new vertex and its sign.

                            temp = gamma * rho * vsig[jdrop];
                            for (int k = 1; k <= n; ++k) {
                                dx[k] = temp * simi[jdrop][k];
                            }
                            double cvmaxp = 0.0;
                            double cvmaxm = 0.0;

                            total = 0.0;
                            for (int k = 1; k <= mp; ++k) {
                                total = DOT_PRODUCT(PART(COL(a, k), 1, n),
                                        PART(dx, 1, n));
                                if (k < mp) {
                                    temp = datmat[k][np];
                                    cvmaxp = Math.max(cvmaxp, -total - temp);
                                    cvmaxm = Math.max(cvmaxm, total - temp);
                                }
                            }
                            double dxsign = parmu * (cvmaxp - cvmaxm) > 2.0 * total ? -1.0
                                    : 1.0;

                            //     Update the elements of SIM and SIMI, and set the next X.

                            temp = 0.0;
                            for (int i = 1; i <= n; ++i) {
                                dx[i] = dxsign * dx[i];
                                sim[i][jdrop] = dx[i];
                                temp += simi[jdrop][i] * dx[i];
                            }
                            for (int k = 1; k <= n; ++k) {
                                simi[jdrop][k] /= temp;
                            }

                            for (int j = 1; j <= n; ++j) {
                                if (j != jdrop) {
                                    temp = DOT_PRODUCT(
                                            PART(ROW(simi, j), 1, n),
                                            PART(dx, 1, n));
                                    for (int k = 1; k <= n; ++k) {
                                        simi[j][k] -= temp * simi[jdrop][k];
                                    }
                                }
                                x[j] = sim[j][np] + dx[j];
                            }
                            continue L_40;
                        }

                        //     Calculate DX = x(*)-x(0).
                        //     Branch if the length of DX is less than 0.5*RHO.

                        ifull = trstlp(n, m, a, con, rho, dx);
                        if (!ifull) {
                            temp = 0.0;
                            for (int k = 1; k <= n; ++k) {
                                temp += dx[k] * dx[k];
                            }
                            if (temp < 0.25 * rho * rho) {
                                ibrnch = true;
                                break L_550;
                            }
                        }

                        //     Predict the change to F and the new maximum constraint violation if the
                        //     variables are altered from x(0) to x(0) + DX.

                        total = 0.0;
                        double resnew = 0.0;
                        con[mp] = 0.0;
                        for (int k = 1; k <= mp; ++k) {
                            total = con[k]
                                    - DOT_PRODUCT(PART(COL(a, k), 1, n),
                                            PART(dx, 1, n));
                            if (k < mp) {
                                resnew = Math.max(resnew, total);
                            }
                        }

                        //     Increase PARMU if necessary and branch back if this change alters the
                        //     optimal vertex. Otherwise PREREM and PREREC will be set to the predicted
                        //     reductions in the merit function and the maximum constraint violation
                        //     respectively.

                        prerec = datmat[mpp][np] - resnew;
                        double barmu = prerec > 0.0 ? total / prerec : 0.0;
                        if (parmu < 1.5 * barmu) {
                            parmu = 2.0 * barmu;
                            if (iprint >= 2) {
                                System.out.format(
                                        "%nIncrease in PARMU to %13.6f%n",
                                        parmu);
                            }
                            double phi = datmat[mp][np] + parmu
                                    * datmat[mpp][np];
                            for (int j = 1; j <= n; ++j) {
                                temp = datmat[mp][j] + parmu * datmat[mpp][j];
                                if (temp < phi
                                        || (temp == phi && parmu == 0.0 && datmat[mpp][j] < datmat[mpp][np])) {
                                    continue L_140;
                                }
                            }
                        }
                        prerem = parmu * prerec - total;

                        //     Calculate the constraint and objective functions at x(*).
                        //     Then find the actual reduction in the merit function.

                        for (int k = 1; k <= n; ++k) {
                            x[k] = sim[k][np] + dx[k];
                        }
                        ibrnch = true;
                        continue L_40;
                    }

                    skipVertexIdent = false;
                    double vmold = datmat[mp][np] + parmu * datmat[mpp][np];
                    double vmnew = f + parmu * resmax;
                    double trured = vmold - vmnew;
                    if (parmu == 0.0 && f == datmat[mp][np]) {
                        prerem = prerec;
                        trured = datmat[mpp][np] - resmax;
                    }

                    //     Begin the operations that decide whether x(*) should replace one of the
                    //     vertices of the current simplex, the change being mandatory if TRURED is
                    //     positive. Firstly, JDROP is set to the index of the vertex that is to be
                    //     replaced.

                    double ratio = trured <= 0.0 ? 1.0 : 0.0;
                    jdrop = 0;
                    for (int j = 1; j <= n; ++j) {
                        temp = Math.abs(DOT_PRODUCT(PART(ROW(simi, j), 1, n),
                                PART(dx, 1, n)));
                        if (temp > ratio) {
                            jdrop = j;
                            ratio = temp;
                        }
                        sigbar[j] = temp * vsig[j];
                    }

                    //     Calculate the value of ell.

                    double edgmax = delta * rho;
                    int l = 0;
                    for (int j = 1; j <= n; ++j) {
                        if (sigbar[j] >= parsig || sigbar[j] >= vsig[j]) {
                            temp = veta[j];
                            if (trured > 0.0) {
                                temp = 0.0;
                                for (int k = 1; k <= n; ++k) {
                                    temp += Math.pow(dx[k] - sim[k][j], 2.0);
                                }
                                temp = Math.sqrt(temp);
                            }
                            if (temp > edgmax) {
                                l = j;
                                edgmax = temp;
                            }
                        }
                    }
                    if (l > 0) {
                        jdrop = l;
                    }

                    if (jdrop != 0) {
                        //     Revise the simplex by updating the elements of SIM, SIMI and DATMAT.

                        temp = 0.0;
                        for (int i = 1; i <= n; ++i) {
                            sim[i][jdrop] = dx[i];
                            temp += simi[jdrop][i] * dx[i];
                        }
                        for (int k = 1; k <= n; ++k) {
                            simi[jdrop][k] /= temp;
                        }
                        for (int j = 1; j <= n; ++j) {
                            if (j != jdrop) {
                                temp = DOT_PRODUCT(PART(ROW(simi, j), 1, n),
                                        PART(dx, 1, n));
                                for (int k = 1; k <= n; ++k) {
                                    simi[j][k] -= temp * simi[jdrop][k];
                                }
                            }
                        }
                        for (int k = 1; k <= mpp; ++k) {
                            datmat[k][jdrop] = con[k];
                        }

                        //     Branch back for further iterations with the current RHO.

                        if (trured > 0.0 && trured >= 0.1 * prerem) {
                            continue L_140;
                        }
                    }
                } while (false);

                if (!iflag) {
                ibrnch = false;
                continue L_140;
            }

            if (rho <= rhoend) {
                status = CobylaExitStatus.Normal;
                break L_40;
            }

            //     Otherwise reduce RHO if it is not at its least value and reset PARMU.

            double cmin = 0.0, cmax = 0.0;

            rho *= 0.5;
            if (rho <= 1.5 * rhoend) {
                rho = rhoend;
            }
            if (parmu > 0.0) {
                double denom = 0.0;
                for (int k = 1; k <= mp; ++k) {
                    cmin = datmat[k][np];
                    cmax = cmin;
                    for (int i = 1; i <= n; ++i) {
                        cmin = Math.min(cmin, datmat[k][i]);
                        cmax = Math.max(cmax, datmat[k][i]);
                    }
                    if (k <= m && cmin < 0.5 * cmax) {
                        temp = Math.max(cmax, 0.0) - cmin;
                        denom = denom <= 0.0 ? temp : Math.min(denom, temp);
                    }
                }
                if (denom == 0.0) {
                    parmu = 0.0;
                } else if (cmax - cmin < parmu * denom) {
                    parmu = (cmax - cmin) / denom;
                }
            }
            if (iprint >= 2) {
                System.out
                            .format("%nReduction in RHO to %1$13.6f  and PARMU = %2$13.6f%n",
                                    rho, parmu);
            }
            if (iprint == 2) {
                PrintIterationResult(nfvals, datmat[mp][np],
                            datmat[mpp][np], COL(sim, np), n);
            }

            } while (true);
        } while (true);

        switch (status) {
        case Normal:
            if (iprint >= 1) {
                System.out.format("%nNormal return from subroutine COBYLA%n");
            }
            if (ifull) {
                if (iprint >= 1) {
                    PrintIterationResult(nfvals, f, resmax, x, n);
                }
                return status;
            }
            break;
        case MaxIterationsReached:
            if (iprint >= 1) {
                System.out
                        .format("%nReturn from subroutine COBYLA because the MAXFUN limit has been reached.%n");
            }
            break;
        case DivergingRoundingErrors:
            if (iprint >= 1) {
                System.out
                        .format("%nReturn from subroutine COBYLA because rounding errors are becoming damaging.%n");
            }
            break;
        case TerminateRequested:
            if (iprint >= 1) {
                System.out
                        .format("%nReturn from subroutine COBYLA because termination requested by user.%n");
            }

        }

        for (int k = 1; k <= n; ++k) {
            x[k] = sim[k][np];
        }
        f = datmat[mp][np];
        resmax = datmat[mpp][np];
        if (iprint >= 1) {
            PrintIterationResult(nfvals, f, resmax, x, n);
        }

        return status;
    }

    private static boolean trstlp(int n, int m, double[][] a, double[] b,
            double rho, double[] dx) {
        // N.B. Arguments Z, ZDOTA, VMULTC, SDIRN, DXNEW, VMULTD & IACT have been removed.

        //     This subroutine calculates an N-component vector DX by applying the
        //     following two stages. In the first stage, DX is set to the shortest
        //     vector that minimizes the greatest violation of the constraints
        //       A(1,K)*DX(1)+A(2,K)*DX(2)+...+A(N,K)*DX(N) .GE. B(K), K = 2,3,...,M,
        //     subject to the Euclidean length of DX being at most RHO. If its length is
        //     strictly less than RHO, then we use the resultant freedom in DX to
        //     minimize the objective function
        //              -A(1,M+1)*DX(1) - A(2,M+1)*DX(2) - ... - A(N,M+1)*DX(N)
        //     subject to no increase in any greatest constraint violation. This
        //     notation allows the gradient of the objective function to be regarded as
        //     the gradient of a constraint. Therefore the two stages are distinguished
        //     by MCON .EQ. M and MCON .GT. M respectively. It is possible that a
        //     degeneracy may prevent DX from attaining the target length RHO. Then the
        //     value IFULL = 0 would be set, but usually IFULL = 1 on return.

        //     In general NACT is the number of constraints in the active set and
        //     IACT(1),...,IACT(NACT) are their indices, while the remainder of IACT
        //     contains a permutation of the remaining constraint indices.  Further, Z
        //     is an orthogonal matrix whose first NACT columns can be regarded as the
        //     result of Gram-Schmidt applied to the active constraint gradients.  For
        //     J = 1,2,...,NACT, the number ZDOTA(J) is the scalar product of the J-th
        //     column of Z with the gradient of the J-th active constraint.  DX is the
        //     current vector of variables and here the residuals of the active
        //     constraints should be zero. Further, the active constraints have
        //     nonnegative Lagrange multipliers that are held at the beginning of
        //     VMULTC. The remainder of this vector holds the residuals of the inactive
        //     constraints at DX, the ordering of the components of VMULTC being in
        //     agreement with the permutation of the indices of the constraints that is
        //     in IACT. All these residuals are nonnegative, which is achieved by the
        //     shift RESMAX that makes the least residual zero.

        //     Initialize Z and some other variables. The value of RESMAX will be
        //     appropriate to DX = 0, while ICON will be the index of a most violated
        //     constraint if RESMAX is positive. Usually during the first stage the
        //     vector SDIRN gives a search direction that reduces all the active
        //     constraint violations by one simultaneously.

        // Local variables

        double temp;

        int nactx = 0;
        double resold = 0.0;

        double[][] z = new double[1 + n][1 + n];
        double[] zdota = new double[2 + m];
        double[] vmultc = new double[2 + m];
        double[] sdirn = new double[1 + n];
        double[] dxnew = new double[1 + n];
        double[] vmultd = new double[2 + m];
        int[] iact = new int[2 + m];

        int mcon = m;
        int nact = 0;
        for (int i = 1; i <= n; ++i) {
            z[i][i] = 1.0;
            dx[i] = 0.0;
        }

        int icon = 0;
        double resmax = 0.0;
        if (m >= 1) {
            for (int k = 1; k <= m; ++k) {
                if (b[k] > resmax) {
                    resmax = b[k];
                    icon = k;
                }
            }
            for (int k = 1; k <= m; ++k) {
                iact[k] = k;
                vmultc[k] = resmax - b[k];
            }
        }

        //     End the current stage of the calculation if 3 consecutive iterations
        //     have either failed to reduce the best calculated value of the objective
        //     function or to increase the number of active constraints since the best
        //     value was calculated. This strategy prevents cycling, but there is a
        //     remote possibility that it will cause premature termination.

        boolean first = true;
        do {
            L_60: do {
                if (!first || (first && resmax == 0.0)) {
                    mcon = m + 1;
                    icon = mcon;
                    iact[mcon] = mcon;
                    vmultc[mcon] = 0.0;
                }
                first = false;

                double optold = 0.0;
                int icount = 0;

                double step, stpful;

                L_70: do {
                    double optnew = mcon == m ? resmax : -DOT_PRODUCT(
                            PART(dx, 1, n), PART(COL(a, mcon), 1, n));

                    if (icount == 0 || optnew < optold) {
                        optold = optnew;
                        nactx = nact;
                        icount = 3;
                    } else if (nact > nactx) {
                        nactx = nact;
                        icount = 3;
                    } else {
                        --icount;
                    }
                    if (icount == 0) {
                        break L_60;
                    }

                    //     If ICON exceeds NACT, then we add the constraint with index IACT(ICON) to
                    //     the active set. Apply Givens rotations so that the last N-NACT-1 columns
                    //     of Z are orthogonal to the gradient of the new constraint, a scalar
                    //     product being set to zero if its nonzero value could be due to computer
                    //     rounding errors. The array DXNEW is used for working space.

                    double ratio;
                    if (icon <= nact) {
                        if (icon < nact) {
                            //     Delete the constraint that has the index IACT(ICON) from the active set.

                            int isave = iact[icon];
                            double vsave = vmultc[icon];
                            int k = icon;
                            do {
                                int kp = k + 1;
                                int kk = iact[kp];
                                double sp = DOT_PRODUCT(PART(COL(z, k), 1, n),
                                        PART(COL(a, kk), 1, n));
                                temp = Math.sqrt(sp * sp + zdota[kp]
                                        * zdota[kp]);
                                double alpha = zdota[kp] / temp;
                                double beta = sp / temp;
                                zdota[kp] = alpha * zdota[k];
                                zdota[k] = temp;
                                for (int i = 1; i <= n; ++i) {
                                    temp = alpha * z[i][kp] + beta * z[i][k];
                                    z[i][kp] = alpha * z[i][k] - beta
                                            * z[i][kp];
                                    z[i][k] = temp;
                                }
                                iact[k] = kk;
                                vmultc[k] = vmultc[kp];
                                k = kp;
                            } while (k < nact);

                            iact[k] = isave;
                            vmultc[k] = vsave;
                        }
                        --nact;

                        //     If stage one is in progress, then set SDIRN to the direction of the next
                        //     change to the current vector of variables.

                        if (mcon > m) {
                            //     Pick the next search direction of stage two.

                            temp = 1.0 / zdota[nact];
                            for (int k = 1; k <= n; ++k) {
                                sdirn[k] = temp * z[k][nact];
                            }
                        } else {
                            temp = DOT_PRODUCT(PART(sdirn, 1, n),
                                    PART(COL(z, nact + 1), 1, n));
                            for (int k = 1; k <= n; ++k) {
                                sdirn[k] -= temp * z[k][nact + 1];
                            }
                        }
                    } else {
                        int kk = iact[icon];
                        for (int k = 1; k <= n; ++k) {
                            dxnew[k] = a[k][kk];
                        }
                        double tot = 0.0;

                        {
                            int k = n;
                            while (k > nact) {
                                double sp = 0.0;
                                double spabs = 0.0;
                                for (int i = 1; i <= n; ++i) {
                                    temp = z[i][k] * dxnew[i];
                                    sp += temp;
                                    spabs += Math.abs(temp);
                                }
                                double acca = spabs + 0.1 * Math.abs(sp);
                                double accb = spabs + 0.2 * Math.abs(sp);
                                if (spabs >= acca || acca >= accb) {
                                    sp = 0.0;
                                }
                                if (tot == 0.0) {
                                    tot = sp;
                                } else {
                                    int kp = k + 1;
                                    temp = Math.sqrt(sp * sp + tot * tot);
                                    double alpha = sp / temp;
                                    double beta = tot / temp;
                                    ////////////////////////////////////////////
                                    //When temp is quite small, alpha and beta must be set to approximated value.
                                    if (temp<1.0E-200) {
                                        temp = sp+tot;
                                        alpha = sp/temp;
                                        beta = tot/temp;
                                        double prop = Math.sqrt(alpha*alpha+beta*beta);
                                        if (prop<1.0E-200) {
                                            alpha = 1; beta = 0;
                                        } else {
                                            temp /= prop;
                                            alpha /= prop;
                                            beta /= prop;
                                        }
                                    }
                                    ///////////////////////////////////////////////
                                    tot = temp;
                                    for (int i = 1; i <= n; ++i) {
                                        temp = alpha * z[i][k] + beta
                                                * z[i][kp];
                                        z[i][kp] = alpha * z[i][kp] - beta
                                                * z[i][k];
                                        z[i][k] = temp;
                                    }
                                }
                                --k;
                            }
                        }

                        if (tot == 0.0) {
                            //     The next instruction is reached if a deletion has to be made from the
                            //     active set in order to make room for the new active constraint, because
                            //     the new constraint gradient is a linear combination of the gradients of
                            //     the old active constraints.  Set the elements of VMULTD to the multipliers
                            //     of the linear combination.  Further, set IOUT to the index of the
                            //     constraint to be deleted, but branch if no suitable index can be found.

                            ratio = -1.0;
                            {
                                int k = nact;
                                do {
                                    double zdotv = 0.0;
                                    double zdvabs = 0.0;

                                    for (int i = 1; i <= n; ++i) {
                                        temp = z[i][k] * dxnew[i];
                                        zdotv += temp;
                                        zdvabs += Math.abs(temp);
                                    }
                                    double acca = zdvabs + 0.1
                                            * Math.abs(zdotv);
                                    double accb = zdvabs + 0.2
                                            * Math.abs(zdotv);
                                    if (zdvabs < acca && acca < accb) {
                                        temp = zdotv / zdota[k];
                                        if (temp > 0.0 && iact[k] <= m) {
                                            double tempa = vmultc[k] / temp;
                                            if (ratio < 0.0 || tempa < ratio) {
                                                ratio = tempa;
                                            }
                                        }

                                        if (k >= 2) {
                                            int kw = iact[k];
                                            for (int i = 1; i <= n; ++i) {
                                                dxnew[i] -= temp * a[i][kw];
                                            }
                                        }
                                        vmultd[k] = temp;
                                    } else {
                                        vmultd[k] = 0.0;
                                    }
                                } while (--k > 0);
                            }
                            if (ratio < 0.0) {
                                break L_60;
                            }

                            //     Revise the Lagrange multipliers and reorder the active constraints so
                            //     that the one to be replaced is at the end of the list. Also calculate the
                            //     new value of ZDOTA(NACT) and branch if it is not acceptable.

                            for (int k = 1; k <= nact; ++k) {
                                vmultc[k] = Math.max(0.0, vmultc[k] - ratio
                                        * vmultd[k]);
                            }
                            if (icon < nact) {
                                int isave = iact[icon];
                                double vsave = vmultc[icon];
                                int k = icon;
                                do {
                                    int kp = k + 1;
                                    int kw = iact[kp];
                                    double sp = DOT_PRODUCT(
                                            PART(COL(z, k), 1, n),
                                            PART(COL(a, kw), 1, n));
                                    temp = Math.sqrt(sp * sp + zdota[kp]
                                            * zdota[kp]);
                                    double alpha = zdota[kp] / temp;
                                    double beta = sp / temp;
                                    zdota[kp] = alpha * zdota[k];
                                    zdota[k] = temp;
                                    for (int i = 1; i <= n; ++i) {
                                        temp = alpha * z[i][kp] + beta
                                                * z[i][k];
                                        z[i][kp] = alpha * z[i][k] - beta
                                                * z[i][kp];
                                        z[i][k] = temp;
                                    }
                                    iact[k] = kw;
                                    vmultc[k] = vmultc[kp];
                                    k = kp;
                                } while (k < nact);
                                iact[k] = isave;
                                vmultc[k] = vsave;
                            }
                            temp = DOT_PRODUCT(PART(COL(z, nact), 1, n),
                                    PART(COL(a, kk), 1, n));
                            if (temp == 0.0) {
                                break L_60;
                            }
                            zdota[nact] = temp;
                            vmultc[icon] = 0.0;
                            vmultc[nact] = ratio;
                        } else {
                            //     Add the new constraint if this can be done without a deletion from the
                            //     active set.

                            ++nact;
                            zdota[nact] = tot;
                            vmultc[icon] = vmultc[nact];
                            vmultc[nact] = 0.0;
                        }

                        //     Update IACT and ensure that the objective function continues to be
                        //     treated as the last active constraint when MCON>M.

                        iact[icon] = iact[nact];
                        iact[nact] = kk;
                        if (mcon > m && kk != mcon) {
                            int k = nact - 1;
                            double sp = DOT_PRODUCT(PART(COL(z, k), 1, n),
                                    PART(COL(a, kk), 1, n));
                            temp = Math.sqrt(sp * sp + zdota[nact]
                                    * zdota[nact]);
                            double alpha = zdota[nact] / temp;
                            double beta = sp / temp;
                            zdota[nact] = alpha * zdota[k];
                            zdota[k] = temp;
                            for (int i = 1; i <= n; ++i) {
                                temp = alpha * z[i][nact] + beta * z[i][k];
                                z[i][nact] = alpha * z[i][k] - beta
                                        * z[i][nact];
                                z[i][k] = temp;
                            }
                            iact[nact] = iact[k];
                            iact[k] = kk;
                            temp = vmultc[k];
                            vmultc[k] = vmultc[nact];
                            vmultc[nact] = temp;
                        }

                        //     If stage one is in progress, then set SDIRN to the direction of the next
                        //     change to the current vector of variables.

                        if (mcon > m) {
                            //     Pick the next search direction of stage two.

                            temp = 1.0 / zdota[nact];
                            for (int k = 1; k <= n; ++k) {
                                sdirn[k] = temp * z[k][nact];
                            }
                        } else {
                            kk = iact[nact];
                            temp = (DOT_PRODUCT(PART(sdirn, 1, n),
                                    PART(COL(a, kk), 1, n)) - 1.0)
                                    / zdota[nact];
                            for (int k = 1; k <= n; ++k) {
                                sdirn[k] -= temp * z[k][nact];
                            }
                        }
                    }

                    //     Calculate the step to the boundary of the trust region or take the step
                    //     that reduces RESMAX to zero. The two statements below that include the
                    //     factor 1.0E-6 prevent some harmless underflows that occurred in a test
                    //     calculation. Further, we skip the step if it could be zero within a
                    //     reasonable tolerance for computer rounding errors.

                    double dd = rho * rho;
                    double sd = 0.0;
                    double ss = 0.0;
                    for (int i = 1; i <= n; ++i) {
                        if (Math.abs(dx[i]) >= 1.0E-6 * rho) {
                            dd -= dx[i] * dx[i];
                        }
                        sd += dx[i] * sdirn[i];
                        ss += sdirn[i] * sdirn[i];
                    }
                    if (dd <= 0.0) {
                        break L_60;
                    }
                    temp = Math.sqrt(ss * dd);
                    if (Math.abs(sd) >= 1.0E-6 * temp) {
                        temp = Math.sqrt(ss * dd + sd * sd);
                    }
                    stpful = dd / (temp + sd);
                    step = stpful;
                    if (mcon == m) {
                        double acca = step + 0.1 * resmax;
                        double accb = step + 0.2 * resmax;
                        if (step >= acca || acca >= accb) {
                            break L_70;
                        }
                        step = Math.min(step, resmax);
                    }

                    //     Set DXNEW to the new variables if STEP is the steplength, and reduce
                    //     RESMAX to the corresponding maximum residual if stage one is being done.
                    //     Because DXNEW will be changed during the calculation of some Lagrange
                    //     multipliers, it will be restored to the following value later.

                    for (int k = 1; k <= n; ++k) {
                        dxnew[k] = dx[k] + step * sdirn[k];
                    }
                    if (mcon == m) {
                        resold = resmax;
                        resmax = 0.0;
                        for (int k = 1; k <= nact; ++k) {
                            int kk = iact[k];
                            temp = b[kk]
                                    - DOT_PRODUCT(PART(COL(a, kk), 1, n),
                                            PART(dxnew, 1, n));
                            resmax = Math.max(resmax, temp);
                        }
                    }

                    //     Set VMULTD to the VMULTC vector that would occur if DX became DXNEW. A
                    //     device is included to force VMULTD(K) = 0.0 if deviations from this value
                    //     can be attributed to computer rounding errors. First calculate the new
                    //     Lagrange multipliers.

                    {
                        int k = nact;
                        do {
                            double zdotw = 0.0;
                            double zdwabs = 0.0;
                            for (int i = 1; i <= n; ++i) {
                                temp = z[i][k] * dxnew[i];
                                zdotw += temp;
                                zdwabs += Math.abs(temp);
                            }
                            double acca = zdwabs + 0.1 * Math.abs(zdotw);
                            double accb = zdwabs + 0.2 * Math.abs(zdotw);
                            if (zdwabs >= acca || acca >= accb) {
                                zdotw = 0.0;
                            }
                            vmultd[k] = zdotw / zdota[k];
                            if (k >= 2) {
                                int kk = iact[k];
                                for (int i = 1; i <= n; ++i) {
                                    dxnew[i] -= vmultd[k] * a[i][kk];
                                }
                            }
                        } while (k-- >= 2);
                        if (mcon > m) {
                            vmultd[nact] = Math.max(0.0, vmultd[nact]);
                        }
                    }

                    //     Complete VMULTC by finding the new constraint residuals.

                    for (int k = 1; k <= n; ++k) {
                        dxnew[k] = dx[k] + step * sdirn[k];
                    }
                    if (mcon > nact) {
                        int kl = nact + 1;
                        for (int k = kl; k <= mcon; ++k) {
                            int kk = iact[k];
                            double total = resmax - b[kk];
                            double sumabs = resmax + Math.abs(b[kk]);
                            for (int i = 1; i <= n; ++i) {
                                temp = a[i][kk] * dxnew[i];
                                total += temp;
                                sumabs += Math.abs(temp);
                            }
                            double acca = sumabs + 0.1 * Math.abs(total);
                            double accb = sumabs + 0.2 * Math.abs(total);
                            if (sumabs >= acca || acca >= accb) {
                                total = 0.0;
                            }
                            vmultd[k] = total;
                        }
                    }

                    //     Calculate the fraction of the step from DX to DXNEW that will be taken.

                    ratio = 1.0;
                    icon = 0;
                    for (int k = 1; k <= mcon; ++k) {
                        if (vmultd[k] < 0.0) {
                            temp = vmultc[k] / (vmultc[k] - vmultd[k]);
                            if (temp < ratio) {
                                ratio = temp;
                                icon = k;
                            }
                        }
                    }

                    //     Update DX, VMULTC and RESMAX.

                    temp = 1.0 - ratio;
                    for (int k = 1; k <= n; ++k) {
                        dx[k] = temp * dx[k] + ratio * dxnew[k];
                    }
                    for (int k = 1; k <= mcon; ++k) {
                        vmultc[k] = Math.max(0.0, temp * vmultc[k] + ratio
                                * vmultd[k]);
                    }
                    if (mcon == m) {
                        resmax = resold + ratio * (resmax - resold);
                    }

                    //     If the full step is not acceptable then begin another iteration.
                    //     Otherwise switch to stage two or end the calculation.

                } while (icon > 0);

                if (step == stpful) {
                    return true;
                }

            } while (true);

        //     We employ any freedom that may be available to reduce the objective
        //     function before returning a DX whose length is less than RHO.

        } while (mcon == m);

        return false;
    }

    private static void PrintIterationResult(int nfvals, double f,
            double resmax, double[] x, int n) {
        System.out.format(
                "%nNFVALS = %1$5d   F = %2$13.6f    MAXCV = %3$13.6e%n",
                nfvals, f, resmax);
        System.out.format("X = %s%n", FORMAT(PART(x, 1, n)));
    }

    private static double[] ROW(double[][] src, int rowidx) {
        int cols = src[0].length;
        double[] dest = new double[cols];
        for (int col = 0; col < cols; ++col) {
            dest[col] = src[rowidx][col];
        }
        return dest;
    }

    private static double[] COL(double[][] src, int colidx) {
        int rows = src.length;
        double[] dest = new double[rows];
        for (int row = 0; row < rows; ++row) {
            dest[row] = src[row][colidx];
        }
        return dest;
    }

    private static double[] PART(double[] src, int from, int to) {
        double[] dest = new double[to - from + 1];
        int destidx = 0;
        for (int srcidx = from; srcidx <= to; ++srcidx, ++destidx) {
            dest[destidx] = src[srcidx];
        }
        return dest;
    }

    private static String FORMAT(double[] x) {
        String fmt = "";
        for (int i = 0; i < x.length; ++i) {
            fmt = fmt + String.format("%13.6f", x[i]);
        }
        return fmt;
    }

    private static double DOT_PRODUCT(double[] lhs, double[] rhs) {
        double sum = 0.0;
        for (int i = 0; i < lhs.length; ++i) {
            sum += lhs[i] * rhs[i];
        }
        return sum;
    }
}
