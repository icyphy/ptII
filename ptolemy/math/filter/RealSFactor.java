/* The RealSFactor class

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.math.filter;

import java.util.Vector;
import ptolemy.math.*;
//////////////////////////////////////////////////////////////////////////
//// RealSFactor
/** 
The RealSFactor class is used to implement RealAnalogFilter's transfer
function.  Like the RealZFactor, the RealSFactor supports methods like
movePole, moveZero, addZero, addPole, setNumerator, setDenominator, the 
methods to change the factors characteristics.  To support those methods, 
RealSFactor contains two polynomials of decreasing positive powers representing
the transfer function this factor and the zero and pole locations 
corresponding to the transfer function.  This class is derived from RealFactor.
@author  David Teng(davteng@hkn.eecs.berkeley.edu), William Wu(wbwu@eecs.berkeley.edu)
@version %W%	%G%

*/
public class RealSFactor extends RealFactor {
    /** Construct a real factor of unit one.  The transfer function is :
     * <pre>
     *       1.0 
     *  1.0 -----
     *       1.0 
     * </pre>
     *  
     */	
    public RealSFactor() {
        _numerator = new double[1];
        _numerator[0] = 1.0;
        _denominator = new double[1];
        _denominator[0] = 1.0;
        _gain = 1.0;
        _solvePoleZero();
    }

    /** Construct a real factor with the given parameters.  The parameter
     * is two arrays of double describing the numerator/denominator polynomial
     * coefficients, and a double value describing the gain.  The resulting 
     * transfer function is equal to:
     * <pre>
     *
     *       numer[0]*x^n + numer[1]*x^(n-1) + ... + numer[n-1]*x^1 + numer[n]
     * gain* -----------------------------------------------------------------
     *       denom[0]*x^n + denom[1]*x^(n-1) + ... + denom[n-1]*x^1 + denom[n]
     *
     * </pre>
     * <p>
     * This factor is used to describe continuous time systems, so the 
     * polynomials is implemented in positive decreasing powers of s.
     * <p>
     * This function uses <code> arrayCopy() </code> to copy the given 
     * coefficients to the internal container.
     * <p>
     * Poles and zeroes associated with this transfer function are calculated.
     * <p>
     * @param numer numerator polynomial coefficients 
     * @param denom denominator polynomial coefficients
     * @param gain gain of this factor
     * @exception IllealArgumentException 
     *                if any input array is null
     */	
    public RealSFactor(double [] numer, double [] denom, double gain) 
                      throws IllegalArgumentException{

                          
        if ((numer == null) || (denom == null)) {
            String str = new String(
                    "array of coefficients for numerator / denominator is null");
            throw new IllegalArgumentException(str);
        }
        _numerator = new double[numer.length];
        _denominator = new double[denom.length];
        _gain = gain;
        System.arraycopy(numer, 0, _numerator, 0, numer.length);
        System.arraycopy(denom, 0, _denominator, 0, denom.length);
        _solvePoleZero();
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods should not have doc comments, they should
    // have regular C++ comments.
    protected void _solvePoleZero() throws IllegalArgumentException { 
         Complex pole, zero; 
 
         Vector singlepole = new Vector();
         Vector singlezero = new Vector();
         Vector conjpole = new Vector();
         Vector conjzero = new Vector();
         
         System.out.println("in solveNumerator: " + _numerator.length);
         if (_numerator.length == 1) {
             // single scalar value, no pole/zero produced
                   
         } else if (_numerator.length == 2) {
             // case a*s + b
             Complex[] roots = new Complex[2];
             double a = _numerator[0];
             double b = _numerator[1];
             boolean conjugate = MathWizard.realquadraticRoots(0.0,a,b,roots);
             if (roots[0] != null) {
                 pole = new Complex(Double.POSITIVE_INFINITY);
                 singlepole.addElement(pole);
                 singlezero.addElement(roots[0]);
             }
         } else if (_numerator.length == 3) {
              // case a*s^2 + b*s + c
              double a = _numerator[0];   
              double b = _numerator[1];   
              double c = _numerator[2]; 
              
              System.out.println("a b c = "+a+" "+b+" "+c);
              Complex [] roots = new Complex[2]; 
              
              boolean conjugate = 
                  MathWizard.realquadraticRoots(a, b, c, roots); 
              
              if (conjugate){
                  // produce two complex conjugate zeroes, and two
                  // conjugate poles at infinity.
                  ConjugateComplex conjz = new ConjugateComplex(roots[0]);
                  conjzero.addElement(conjz);
                  ConjugateComplex conjp = 
                      new ConjugateComplex(
                              new Complex(Double.POSITIVE_INFINITY));
                 
                  conjpole.addElement(conjp);
                  System.out.println("roots" + roots[0].real);
                  System.out.println("roots" + roots[1].real);
              } else {
                  if (roots[0] == null && roots[1] == null) {
                      System.out.println("num two nulls");
                  } else if (roots[1] == null) {
                      pole = new Complex(Double.POSITIVE_INFINITY);
                      singlepole.addElement(pole);
                      singlezero.addElement(roots[0]);
                  }
                  else {
                      // produce two single real zeroes, and two single
                      // poles at infinity
                            
                      pole = new Complex(Double.POSITIVE_INFINITY);
                      singlepole.addElement(pole);
                      singlezero.addElement(roots[0]);
                      singlepole.addElement(pole);
                      singlezero.addElement(roots[1]);
                      System.out.println("roots" + roots[0].real);
                      System.out.println("roots" + roots[1].real);
                  }
              }
         } else {
              String str = new String("Only order less than or equal to 2 can be factored ");
              throw new IllegalArgumentException(str);
         }
         
System.out.println("in solve denominator: "+_denominator.length);
         if (_denominator.length == 1) {
             // single scalar value, no pole/zero produced
         } else if (_denominator.length == 2) {
             // case a*s + b
             Complex[] roots = new Complex[2];
             double a = _denominator[0];
             double b = _denominator[1];
             boolean conjugate = MathWizard.realquadraticRoots(0.0,a,b,roots);
             if (roots[0] != null) {
                 zero = new Complex(Double.POSITIVE_INFINITY);
                 singlezero.addElement(zero);
                 singlepole.addElement(roots[0]);
             }
         } else if (_denominator.length == 3){
             // case a*s^2 + b*s + c
             double a = _denominator[0];   
             double b = _denominator[1];   
             double c = _denominator[2]; 
             System.out.println("hummm");
             Complex [] roots = new Complex[2]; 
             
             boolean conjugate = 
                 MathWizard.realquadraticRoots(a, b, c, roots); 
             
             if (conjugate){
                 // produce two complex conjugate poles, and two
                 // conjugate zeroes at infinity.
                 ConjugateComplex conjp = new ConjugateComplex(roots[0]);
                 ConjugateComplex conjz = 
                     new ConjugateComplex(
                             new Complex(Double.POSITIVE_INFINITY));
                 
                 conjzero.addElement(conjz);
                 conjpole.addElement(conjp);
                 System.out.println("roots" + roots[0].real);
                 System.out.println("roots" + roots[1].real);
             } else {
                 System.out.println("two nulls");
                 if (roots[0] == null && roots[1] == null) {
                     System.out.println("two nulls again");
                     
                 } else if (roots[1] == null) {
                     zero = new Complex(Double.POSITIVE_INFINITY);
                     singlezero.addElement(zero);
                     singlepole.addElement(roots[0]);
                 }
                 else {
                     // produce two single real zeroes, and two single
                     // poles at infinity
                     zero = new Complex(Double.POSITIVE_INFINITY);
                     singlezero.addElement(zero);
                     singlepole.addElement(roots[0]);
                     singlezero.addElement(zero);
                     singlepole.addElement(roots[1]);
                     
                 }
             }
         } else {
             String str = new String("Only order less than or equal to 2 can be factored ");
             throw new IllegalArgumentException(str);
         }
        
         _singlePole = new Complex[singlepole.size()];
         for (int i=0;i<singlepole.size();i++){
             _singlePole[i] = (Complex) singlepole.elementAt(i);
         } 
         
         _singleZero = new Complex[singlezero.size()];
         for (int i=0;i<singlezero.size();i++){
             _singleZero[i] = (Complex) singlezero.elementAt(i);
         } 
         
         _conjugatePole = new ConjugateComplex[conjpole.size()];
         for (int i=0;i<conjpole.size();i++){
             _conjugatePole[i] = (ConjugateComplex) conjpole.elementAt(i);
         }
         
         _conjugateZero = new ConjugateComplex[conjzero.size()];
         for (int i=0;i<conjzero.size();i++){
             _conjugateZero[i] = (ConjugateComplex) conjzero.elementAt(i);
         }
         for (int i = 0; i < _singlePole.length; i++) {
             System.out.println("singlepole = " + _singlePole[i].real + " " + _singlePole[i].imag);
         } 
         System.out.println("converting vector to array, "+_singlePole.length+" "+_singleZero.length+" "+_conjugatePole.length+" "+_conjugateZero.length); 
    }

    
    // update the numerator with the new pole
    protected void _updateNumerator() { 
        Complex [] tmpnum = MathWizard.zeroesToPoly(getZeroes());
        _numerator = new double[tmpnum.length];

        for (int i=0;i<tmpnum.length;i++){
            _numerator[i] = tmpnum[i].real;
        }
    }

    // update the denominator with the new zero
    protected void _updateDenominator() {
        Complex [] tmpden = MathWizard.zeroesToPoly(getPoles());
        _denominator = new double[tmpden.length];

        // since this is a real factor, thus only the real part of is kept 
        for (int i=0;i<tmpden.length;i++){
            _denominator[i] = tmpden[i].real;
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    
    private static final double TINY = 1.0e-6;   
    
}

