/* The grand math library 
 
Copyright (c) 1997-1998 The Regents of the University of California.
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

@(#)MathWizard.java	1.5\t08/31/98 
*/

package ptolemy.math.filter;

import java.util.*;
import java.lang.reflect.*;
import ptolemy.math.*;

//////////////////////////////////////////////////////////////////////////
//// MathWizard 
/**
 * The math/signal processing library provide methods 
 * to be used on many signal processing tasks, like filter
 * design, FFT.
 *
 * @Author: Albert Chen, William Wu, David Teng(davteng@hkn.eecs.berkeley.edu)
 * @Date: 3/16/98
 * @Version: 
 */

public final class MathWizard {


    /**
       takes an array of zeroes and returns an array of coefficients 
       of the polynomial which is represented by the zeroes
     */
    public static Complex[] zeroesToPoly(Complex[] zeroes) {
        Complex[] result = new Complex[2];
        result[0] = new Complex(1);
        result[1] = new Complex(0);

        if (zeroes.length >= 1) {
            result[1] = zeroes[0].negate();
            if (zeroes.length > 1) {
                for (int i = 1; i < zeroes.length; i++) {
                    Complex[] zero = {new Complex(1), zeroes[i].negate()};
                    result = polyMultiply(result, zero);
                }
            }
        }

        return result;
    }

    /**
    * multiplies two polynomials together, using the basic no-frills technique
    * takes as arguments two arrays of Complex's consisting of the
    * coefficients of the polynomials, in ascending order, i.e. c0, c1x, c2x^2,
    * ...
    */
    public static Complex[] polyMultiply(Complex[] poly1, Complex[] poly2) {
        Complex[] result = new Complex[poly1.length+poly2.length-1];
        for (int i = 0; i<result.length; i++) {
            result[i] = new Complex();
        }

        for (int i = 0; i<poly1.length; i++) {
            for (int j = 0; j<poly2.length; j++) {
                result[i+j] = result[i+j].add(poly1[i].multiply(poly2[j]));
            }
        }

        return result;
    }



 
 
    /**
     *  Factor a 4-order polynomial to two 2-order polynomial.
     *  If the input size doesn't match, return.
     *
     *  @param coeff coefficients of the 4-order polynomial
     *  @param quad1 first factored 2-order polynomial
     *  @param quad2 second factored 2-order polynomial
     */
    public static void factor2quadratic (double [] coeff, double [] quad1, 
            double [] quad2) throws IllegalArgumentException{
 
        if ((coeff.length != 5) || (quad1.length != 3) || (quad2.length != 3)){
            throw new IllegalArgumentException("polynomial given is not 4th order and/or quad1, quad2 are not quadratic: coeff is not a 5 element double[] and/or quad1, quad2 are not 3 element double[]");
        }
 
        double facB [] = {1.0};
        double facC [] = {1.0};

        // factor the numerator into two quadratic polynomial
 
        // factor out the first quadratic in the form of
        // c + b*s + s^2 from numerator
        qroot(coeff, 4, facB, facC, 0.001);

        double quot [] = {facC[0], facB[0], 1.0};
        double [] secquot = new double[5];
        double [] secrem = new double[5];
 
        // factor the second quadratic
        polydiv(coeff, 4, quot, 2, secquot, secrem);

        quad1[0] = facC[0];
        quad1[1] = facB[0];
        quad1[2] = 1.0;

        // Store the second quadratic in denominator array 
        quad2[0] = secquot[0];
        quad2[1] = secquot[1];
        quad2[2] = secquot[2];
        
    }


/*********************************************************
 * following are the functions needed for _toBandpass and _toBandstop,
 * it include some generical useful functions that will be useful
 * later: polynomial division, simplfied polynomial root finding, etc.
 * they should be placed somewhere else.
 *********************************************************/

    /**
     * from the book "Numerical Recipes in C" page 175 
     * Polynomial division
     * Given the n+1 coefficients of a polynomial of degree n in u[0..n], and 
     * the nv+1 coefficients of another polynomial of degree nv in v[0..nv], 
     * divide the polynomial u by the polynomial v ("u"/"v") giveing a quotient
     * polynomial whose coeffients are returned in q[0..n], and a remainder 
     * polynomial whose coeffients are returned in r[0..n].  The elements 
     * r[nv..n] and q[n-nv+1..n] are returned as zero.
     *
     * FIXME:  add another function with the same name that handles polynomial
     *         division with complex coefficients.
     * 
     * @param u dividend polynomial
     * @param n order of dividend polynomial
     * @param v divider polynomial
     * @param nv order of divider polynomial
     * @param q quotient polynomial
     */
    public static void polydiv(double u[], int n, double v[], int nv, 
                               double q[], double r[]){
          int k,j;

          for (j=0;j<=n;j++){
              r[j] = u[j];
              q[j]=0.0;
          }

          for (k=n-nv;k>=0;k--){
              q[k]=r[nv+k]/v[nv];
              for (j=nv+k-1;j>=k;j--) {
                  r[j]-=q[k]*v[j-k];
              }
          }

          for (j=nv;j<=n;j++) {
              r[j]=0.0;
          }
    }


    /**
     *  from the book "Numerical Recipes in C" page 378 
     *  Given n+1 coefficients p[0..n] of a polynomial of degree n, and
     *  trial values for the coefficients of a quadratic factor x*x + b*x + c, 
     *  improve the solution until the coeffients b,c changes by less 
     *  than eps.  The function <code> polydiv </code> is used.  
     *
     *  FIXME:  this method is recommended for root polishing, we might need
     *  something else to factor out a quadratic equation.
     *
     *  @param p input polynomial to be factored
     *  @param n order of input polynomial
     *  @param b guessed coefficient
     *  @param c guessed coefficient
     *  @param eps threshold for stability of b, and c  
     */
    public static void qroot(double p[], int n, double b[], double c[], 
                             double eps){

          int iter;
          double sc,sb,s,rc,rb,r,dv,delc,delb;
          double [] q;
          double [] qq;
          double [] rem;
          double ITMAX=20;
 
          q = new double[n+1];
          qq = new double[n+1];
          rem = new double[n+1];
         
          double [] d = new double[3];
          d[2]=1.0;

          for (iter=1;iter<=ITMAX;iter++){
              d[1]=b[0];
              d[0]=c[0];

              polydiv(p,n,d,2,q,rem); 

              // First division r,s
              s=rem[0];                              
              r=rem[1];

              polydiv(q,n-1,d,2,qq,rem);

              // Second division partial r,s with respect to c.

              sb=-(c[0])*(rc=-rem[1]); 
              rb=-(b[0])*rc+(sc=-rem[0]); 
 
              // solve 2x2 equation
              dv=1.0/(sb*rc-sc*rb);
              delb=(r*sc - s*rc)*dv;
              delc=(-r*sb + s*rb)*dv;
              b[0] += (delb=(r*sc - s*rc)*dv);
              c[0] += (delc=(-r*sb + s*rb)*dv);
              if ((Math.abs(delb) <= eps*Math.abs(b[0]) || Math.abs(b[0])<TINY)
                 && (Math.abs(delc) <= eps*Math.abs(c[0]) 
                 || Math.abs(c[0])<TINY)) {
                    return;
              } 
          }
          System.out.println("WARNING: Exceed interation !!!");
          return;
    }


    
 

    /**
     * solves for roots of ax^2+bx+c=0, with a, b, c all real polynominals.
     * If a quadratic has two unequal real solution, they are seperate roots,
     * the function will return false.
     * If they are two equal real roots, or two complex roots, then they
     * are coupled as complex conjuagte pair, the function will return 
     * true.
     *
     * @param a coefficient for x^2
     * @param b coefficient for x
     * @param c constant 
     * @param result solved roots in array of two Complex   
     * @return boolean value indicate if the roots are complex conjugate pair
     *         or not, true => conjugate pair, false => not conjugate pair     
     */
    public static boolean realquadraticRoots(double a, double b, 
                                           double c, Complex [] roots)
                 throws IllegalArgumentException {
                
        if ((roots == null) || (roots.length < 2)){ 
            String str = new String("Parameter roots must be an array of two Complex");
            throw new IllegalArgumentException(str); 
        }

        // find the absolute values of a, b, and c to see if their values are
        // near zero
        double absOfA = Math.abs(a);
        double absOfB = Math.abs(b);
        double absOfC = Math.abs(c);
        
        boolean conjugate = false;
        
        if (absOfA > TINY & absOfB > TINY & absOfC > TINY) {
            double discrim = b*b-4.0*a*c;
            
            if (discrim < -TINY) {
                // two complex conjugate roots 
                roots[0] = new Complex(-b/(2*a), 
                      Math.pow(-discrim, 0.5)/(2*a));
                roots[1] = new Complex(-b/(2*a), 
                      -Math.pow(-discrim, 0.5)/(2*a));
                conjugate = true;            
            } else if ((discrim >= -TINY) && (discrim <= TINY)){
                // two equal real roots 
                roots[0] = new Complex(-b/(2*a)); 
                roots[1] = new Complex(-b/(2*a));
                conjugate = true;            
            } else { 
            // Adapted from "Numerical Recipes in C: The Art of Scientific Computing" 
            // (ISBN 0-521-43108-5)
            // pgs 183-84
            
            // two unequal real roots 
            double q= -0.5*(b+ExtendedMath.sgn(b)*Math.sqrt(discrim));
            roots[0] = new Complex(q/a);
            roots[1] = new Complex(c/q);
            conjugate = false;            
            }
        } else if (absOfA < TINY & absOfB > TINY & absOfC > TINY) {
            roots[0] = new Complex(-c/b);
            roots[1] = null;
            conjugate = false;
        } else if (absOfA < TINY & absOfB < TINY & absOfC > TINY) {
            roots[0] = null;
            roots[1] = null;
            conjugate = false;
        } else if (absOfA < TINY & absOfB < TINY & absOfC < TINY) {
            roots[0] = null;
            roots[0] = null;
            conjugate = false;
        } else if (absOfA > TINY & absOfB < TINY & absOfC > TINY) {
            if (c < 0) {
                roots[0] = new Complex(Math.pow(-c/a, 0.5));
                roots[1] = new Complex(-Math.pow(-c/a, 0.5));
                conjugate = false;
            } else {
                roots[0] = new Complex(0,Math.pow(c/a, 0.5));
                roots[1] = new Complex(0,-Math.pow(c/a, 0.5));
                conjugate = true;
            }            
        } else if (absOfA < TINY & absOfB > TINY & absOfC < TINY) {
            roots[0] = new Complex(0);
            roots[1] = null;
            conjugate = false;
        } else if (absOfA > TINY & absOfB < TINY & absOfC < TINY) {
            roots[0] = new Complex(0);
            roots[1] = new Complex(0);
            conjugate = true;
        } else if (absOfA > TINY & absOfB > TINY & absOfC < TINY) {
            roots[0] = new Complex(0);
            roots[1] = new Complex(-b/a);
            conjugate = false;
        }
        return conjugate;
    }
        
    /**
     *  array copying.  Return a new double array that is a copy of the 
     *  the one in argument.
     *  FIXME:  this function should be in ArrayMath
     *
     *  @return a copy of input array
     *  @param array input array
     */ 
     public static double [] arraycopy(double [] array){
        double result [] = new double[array.length];
        for (int i=0; i<array.length; i++){
            result[i] = array[i];
        }
        return result;
     } 
    
 

    private static final double _log10scale = 1/Math.log(10);
    private static final double _log2scale = 1/Math.log(2);
    private static final double TINY=1.0e-6;

    private static final int _debug = 1;
}











