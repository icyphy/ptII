/* Find roots of polynomials.

Copyright (c) 2014-2015 The Regents of the University of California.
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


package org.ptolemy.qss.util;


///////////////////////////////////////////////////////////////////
//// PolynomialRoot


/**
 * Find roots of polynomials.
 *
 * <p>Stand-alone utility functions.</p>
 *
 * <p>TODO: Need a unit test for this class.</p>
 *
 * @author David M. Lorenzetti, Contributor: Thierry S. Nouidui
 * @version $id$
 * @since Ptolemy II 10.2  // FIXME: Check version number.
 * @Pt.ProposedRating red (dmlorenzetti)
 * @Pt.AcceptedRating red (reviewmoderator)  // FIXME: Fill in.
 */
public final class PolynomialRoot {


    ///////////////////////////////////////////////////////////////////
    ////                         public methods


    /**
     * Find the smallest positive real root of a second-order (quadratic) equation.
     *
     * <p>Equation is
     * <i>a*x^2 + b*x + c = 0</i>.</p>
     *
     * @param qea Coefficient `a`.
     * @param qeb Coefficient `b`.
     * @param qec Coefficient `c`.
     * @return The smallest positive real root.
     *   Return `Double.POSITIVE_INFINITY` if no root.
     *   Return 0 if only nonpositive roots.
     */
    public final static double findMinimumPositiveRoot2(final double qea, final double qeb, final double qec) {

        double minPosRoot;
        final double disc = qeb*qeb - 4*qea*qec;

        if (qea == 0 ) {
            // Here, don't have a quadratic.
            if (qeb == 0 ) {
                // Here, have a constant.
                minPosRoot = (qec==0) ? 0 : Double.POSITIVE_INFINITY;
            } else {
                // Here, have a line.
                final double lineIntersection = -qec / qeb;
                minPosRoot = (lineIntersection>0) ? lineIntersection : 0;
            }
        } else if (disc < 0 ) {
            // Here, quadratic with no real roots.
            minPosRoot = Double.POSITIVE_INFINITY;
        } else if (disc == 0 ) {
            // Here, quadratic with a repeated real root.
            final double multiRoot = -0.5 * qeb / qea;
            minPosRoot = (multiRoot>0) ? multiRoot : 0;
        } else {
            // Here, quadratic with two distinct real roots.
            //   Note (qea!=0 && disc>0).
            //   Find roots using numerically stable version of quadratic formula.
            // Find ee = (-qeb +/- sqrt{disc})/2.
            //   Choose the +/- to give {ee} the largest magnitude possible.
            double ee = Math.sqrt(disc);
            if (qeb > 0 ) {
                ee = -qeb - ee;
            } else {
                ee = -qeb + ee;
            }
            ee *= 0.5;
            final double root1 = ee / qea;
            final double root2 = qec / ee;

            if (root1 > 0 ) {
                if (root2 > 0 ) {
                    minPosRoot = (root1<root2) ? root1 : root2;
                } else {
                    minPosRoot = root1;
                }
            } else if (root2 > 0) {
                minPosRoot = root2;
            } else {
                minPosRoot = 0;
            }
        }

        // TODO: Consider "tuning up" the root using Newton-Raphson.

        return( minPosRoot );

        }


    /**
     * Find the smallest positive real root of a third-order (cubic) equation.
     *
     * <p>Equation is
     * <i>a*x^3 + b*x^2 + c*x + d = 0</i>.</p>
     *
     * <p>TODO: Switch from bracketing solution, to one based on algebraic
     * solution to cubic equation.
     * Expect to have to tune up the roots it gives, using NR, just to account
     * for numerical issues.
     * But still may be more direct, maybe faster.</p>
     *
     * @param cea Coefficient `a`.
     * @param ceb Coefficient `b`.
     * @param cec Coefficient `c`.
     * @param ced Coefficient `d`.
     * @param absTol The size of residual at which end search.
     * @param relTol The fractional difference in bracket on x, at which end search.
     * @return The smallest positive real root.
     *   Return `Double.POSITIVE_INFINITY` if no root.
     *   Return 0 if only nonpositive roots.
     */
    public final static double findMinimumPositiveRoot3(final double cea, final double ceb, final double cec, final double ced,
        final double absTol, final double relTol) {

        // Check inputs.
        if (absTol <= 0 ) {
            throw new IllegalArgumentException("Require absTol>0; got " +absTol);
        }
        if (relTol < 0 ) {
            throw new IllegalArgumentException("Require relTol>=0; got " +relTol);
        }

        // Catch degenerate cases.
        if (cea == 0 ) {
            // Here, have a quadratic equation:
            // y = b*x^2 + c*x + d = 0
            return( findMinimumPositiveRoot2(ceb, cec, ced) );
        }
        if (ced == 0 ) {
            // Here, know a root is zero.  Divide polynomial by x:
            // y = a*x^2 + b*x + c = 0
            return( findMinimumPositiveRoot2(cea, ceb, cec) );
        }

        // Here, have a cubic.
        //   Also know y{0}!=0.

        // Find stationary points of cubic.
        //   Cubic y = ax^3 +bx^2 +cx +d has stationary points (that is, a min
        // or max) where dy/dx = 0.
        // dy/dx = 3ax^2 +2bx +c
        //   Solve 3ax^2 +2bx +c = 0 by quadratic equation:
        // x*(6a) = -2b +/- sqrt{4b^2 -4*3a*c}
        // x*(3a) = -b +/ sqrt{b^2 -3ac}
        //   Order the stationary points left-to-right.
        //   If cubic has no stationary points, just set them to 0, and expect
        // bracket types to be {NO_SPAN}.
        double xSpL, xSpR;
        xSpL = 0;
        xSpR = 0;
        final double disc = ceb*ceb -3*cea*cec;
        if (disc == 0 ) {
            // Double root at -b/3a.
            xSpL = -ceb/(3*cea);
            xSpR = xSpL;
        } else if (disc > 0 ) {
            // Distinct real roots at (-b +/- sqrt{disc})/3a.
            //   Note that for roots r1, r2, have
            // 3a*(x-r1)*(x-r2) = 3ax^2 +2bx +c
            //   Therefore, equating the constant terms,
            // 3a*r1*r2 = c
            //   Find r1 as root with largest magnitude, then find
            // r2 = c/(3a*r1)
            //   Define ee = 3a*r1.
            double ee = Math.sqrt(disc);
            if (ceb > 0 ) {
                ee = -ceb - ee;
            } else {
                ee = -ceb + ee;
            }
            xSpL = ee/(3*cea);
            xSpR = cec / ee;
            if (xSpL > xSpR ) {
                final double temp = xSpL;
                xSpL = xSpR;
                xSpR = temp;
            }
        }

        // Bracket the smallest positive root, if one exists.
        double xL, xR, yL, yR;
        xL = 0;
        yL = ced;
        // Possible bracket endpoints on right are xSpL and xSpR.  However,
        // neither is guaranteed to be positive.
        xR = (xSpL>0) ? xSpL : xSpR;
        yR = ced + xR*(cec + xR*(ceb + xR*cea));
        _BracketType bracketType = _getBracketType(xL, yL, xR, yR, cea);
        if (_BracketType.NO_SIGN_CHANGE == bracketType ) {
            // Here, cubic has stationary point to right of {xL==0}, but it
            // doesn't establish a bracket.
            //   Continue the search from {xR}.
            xL = xR;
            yL = yR;
            xR = xSpR;
            yR = ced + xR*(cec + xR*(ceb + xR*cea));
            bracketType = _getBracketType(xL, yL, xR, yR, cea);
        }

        // Here, have points (xL, yL) and (xR, yR), and know whether they form
        // a bracket.
        if (_BracketType.YR_ZERO == bracketType ) {
            return( xR );
        } else if (_BracketType.HAVE_BRACKET != bracketType ) {
            // Here, not able to establish a bracket between stationary points.
            //   However, may have a bracket farther to right, or possibly an
            // existing point satisfies the zero test.
            // Make sure that {xR} refers to a non-negative point.
            if (_BracketType.NO_SPAN == bracketType ) {
                xR = xL;
                yR = yL;
                assert( xR >= 0 );
            }
            // Check for ways of finding a zero.
            if ((yR<0 && cea>0)
                ||
                (yR>0 && cea<0) ) {
                // Here, no stationary point to right of {xL}, but if go far
                // enough to right, will get a bracket.
                double step = 1 + xL*1e-4;
                while ( true ) {
                    xR = xL + step;
                    yR = ced + xR*(cec + xR*(ceb + xR*cea));
                    if ((yL<0 && yR>=0) || (yL>0 && yR<=0) ) {
                        break;
                    }
                    step *= 2;
                }
            } else if (xL>0 && Math.abs(yL)<=absTol ) {
                // Here, {yL} is effectively zero.
                // TODO: Consider removing this clause, and the one below.
                // Here, {xL} and {xR} are stationary points.  If they aren't
                // exactly zero, then ought to trust that the sign is correct
                // when evaluate the cubic polynomial.  In other words, these
                // paths out of the root-finder are dodges-- they satisfy the
                // letter of the law (i.e., the formal definition of a zero),
                // but not the spirit of the law (i.e., they are not really
                // zeros).
                return( xL );
            } else if (xR>0 && Math.abs(yR)<=absTol ) {
                return( xR );
            } else {
                return( 0 );
            }
        }

        // Here, have a bracket.
        //   Narrow down on root using bracketed Newton-Raphson method.
        // Newton-Raphson zeros y = f{x} by:
        // xNew = xOld - yOld/dydxOld
        //   Here,
        // y = ax^3 + bx^2 + cx +d
        // dydx = 3ax^2 + 2bx + c
        boolean bisect;
        double xTest = xL;
        final double threeQea = 3*cea;
        final double twoQeb = 2*ceb;
        while ( true ) {

            // Here, assume have a bracket on root.
            assert( xL < xR );
            assert( (yL<0 && yR>0) || (yL>0 && yR<0) );

            // Find test point.
            //   Take NR step from side with smallest residual.
            bisect = false;
            if (Math.abs(yL) < Math.abs(yR) ) {
                final double dydxL = cec + xL*(twoQeb + xL*threeQea);
                if (dydxL != 0 ) {
                    xTest = xL - yL / dydxL;
                } else {
                    bisect = true;
                }
            } else {
                final double dydxR = cec + xR*(twoQeb + xR*threeQea);
                if (dydxR != 0 ) {
                    xTest = xR - yR / dydxR;
                } else {
                    bisect = true;
                }
            }
            if (bisect || xTest<=xL || xTest>=xR ) {
                xTest = xL + 0.5*(xR - xL);
            }

            // Evaluate test point.
            final double yTest = ced + xTest*(cec + xTest*(ceb + xTest*cea));

            // Update bracket.
            if ((yL<0 && yTest<0) || (yL>0 && yTest>0) ) {
                xL = xTest;
                yL = yTest;
            } else {
                xR = xTest;
                yR = yTest;
            }

            // Test for convergence.
            if ((xR-xL)<relTol*Math.abs(xR)
                ||
                Math.abs(yTest)<=absTol ) {
                break;
            }

        }

        // Here, found a root, or near-root.
        final double minPosRoot = (xL>0 && Math.abs(yL)<Math.abs(yR)) ? xL : xR;
        return( minPosRoot );

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods


    /* Check whether a bracket exists between two points of a cubic polynomial.
     *
     * <p>Points are (xL,yL) and (xR,yR).</p>
     *
     * <p>Work to the right.
     * That is, test whether {xR} establishes a bracket relative to the point
     * at {xL}.
     * Therefore if {xR} is not to the right of {xL}, it does not establish
     * a bracket.</p>
     *
     * <p>Treat zero values in a special way.
     * If {yR==0}, it establishes a bracket (provided it is to the right of {xL}.
     * On the other hand, if {yL==0}, it does not establish a bracket (it can
     * get reported, effectively, as {NO_SIGN_CHANGE}.</p>
     */
    private static enum _BracketType {
        NO_SPAN, NO_SIGN_CHANGE, YR_ZERO, HAVE_BRACKET
    }
    private final static _BracketType _getBracketType(final double xL, final double yL,
        final double xR, final double yR, final double cea) {

        _BracketType bracketType = _BracketType.NO_SIGN_CHANGE;

        if (xR <= xL ) {
            bracketType = _BracketType.NO_SPAN;
        } else if (yR == 0 ) {
            bracketType = _BracketType.YR_ZERO;
        } else if (
            (yL<0 && yR>0)
            ||
            (yL>0 && yR<0) ) {
            bracketType = _BracketType.HAVE_BRACKET;
        }

    return( bracketType );

    }


}
