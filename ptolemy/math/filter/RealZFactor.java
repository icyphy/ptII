/* A real factor type class.

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
import java.util.NoSuchElementException;
import ptolemy.math.*;
//////////////////////////////////////////////////////////////////////////
//// RealFactor
/** 
 This is the real factor class.  It is the component that will be used to 
 build real LTI transfer function for a digital system.  It store two 
 polynomial of negative power of z with real coefficients, and a real number 
 gain.  Internal function is usded to factor pole/zero of this factor.  
 Methods are provided to extract and modify these poles and zeroes.  
 When pole/zero are modified the two polynomials are also updated to reflect 
 the changes.  The factor also stores the internal states of the factor.
 state is stored in an array with ordering latest state will be at the end
 of array, while the earliest will be at the beginning.

<p> 
@author  William Wu (wbwu@eecs.berkeley.edu), David Teng(davteng@hkn.eecs.berkeley.edu)
@version %W%	%G%
*/

public class RealZFactor extends RealFactor{

    /** Construct a real factor of unit one.  The transfer function is :
     * <pre>
     *       1.0 
     *  1.0 -----
     *       1.0 
     * </pre>
     *  
     */	
    public RealZFactor() {
        _numerator = new double[1];
        _numerator[0] = 1.0;
        _denominator = new double[1];
        _denominator[0] = 1.0; 
        _gain = 1.0;
        _solvePoleZero();
        _state = new double[0];
        int _firstState = 0;
    }


    /** Construct a real factor with the given paramerters.  The parameter
     * is two arrays of double describe the numerator/denominator polynomial
     * coefficients, and a double value describe the gain.  The result transfer 
     * function equal to:
     * <pre>
     *
     *       numer[0] + numer[1]*x + numer[2]*x^2 + numer[3]*x^3 ...
     * gain* -------------------------------------------------------
     *       denom[0] + denom[1]*x + denom[2]*x^2 + denom[3]*x^3 ...
     *
     * </pre>
     * <p>
     * Since this factor is used to describe a digital system, the polynomials
     * should be in the negative power of z.
     * <p>
     * This function uses <code> arrayCopy() </code> to copy the given 
     * coefficients to the internal container.
     * <p>
     * Poles and zeroes will be calculated.
     * <p>
     * @param numer numerator polynomial coefficients 
     * @param denom denominator polynomial coefficients
     * @param gain gain of this factor
     * @exception IllealArgumentException 
     *                if any input array is null
     */	
    public RealZFactor(double [] numer, double [] denom, double gain) 
                      throws IllegalArgumentException{

                          
        if ((numer == null) || (denom == null)) {
            String str = new String("array of coefficients for numerator / denominator is null");
            throw new IllegalArgumentException(str);
        }
        _numerator = new double[numer.length];
        _denominator = new double[denom.length];
        _gain = gain;
        System.arraycopy(numer, 0, _numerator, 0, numer.length);
        System.arraycopy(denom, 0, _denominator, 0, denom.length);
        _solvePoleZero();
        _state = new double[Math.max(numer.length-1, denom.length-1)];
        int _firstState = 0;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the output, given a input.  This function computes the
     *  output of the factor based on the given input and current states.
     *  It implements a direct form II realizaton of the factor.
     *  <p>
     * @param input value to be inputted  
     * @return output value
     */
     public double computeOutput(double input){
        
        double output = 0.0;
        
        // multiply input by the gain
        input = input * _gain;
        
        // compute the latest state with this equation
        // lateststate = k=1:N Sum(denom(k)*w[n-k] + input  
        double lateststate = input;
        
                
        
        for (int i=1;i<_denominator.length;i++){
            lateststate -= _denominator[i]*
                _state[_circularAddition(_firstState,-i+1,_state.length)];
            
        }
        
        // divide the intermediate state by denom(0) to feed this 
        // toward the output
        lateststate = lateststate / _denominator[0];

        // compute the output using 
        // y[n] = k=1:M Sum(numer(k)*w[n-k]) + lateststate*numer(0);

        output = lateststate * _numerator[0]; 
        
        for (int i=1;i<_numerator.length;i++){
            output += _numerator[i]*
                _state[_circularAddition(_firstState,-i+1,_state.length)];
        }
        
        // shift in the latest state 
        _firstState = _circularAddition(_firstState,1,_state.length);
        
        if (_state.length > 0) {
            _state[_firstState] = lateststate;
        }        
        
        return output;

    } 
        
    /**
     * Reset the state to array of zero.
     */ 
    public void resetState(){
        _firstState = 0;
        _state = new double[_state.length];
    }
      
    /**
     * Set the factor's internal state to the given array of doubles.
     * This function throw IllegalArgumentException when
     * <p>
     * @param states state value 
     * @exception IllegalArgumentException 
     *              if the given array is null or has incorrect number
     *              of states 
     */	
    public void setState(double [] states) throws IllegalArgumentException {
        if ((states == null) 
         || (states.length != _state.length)){ 
            String str = new String("The number of states is incorrect");
            throw new IllegalArgumentException(str);
        }
        for (int i=0;i<_state.length;i++){
            _state[i] = states[i];
        }
    }      

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Solve the pole/zero of this factor 
    // Currently only the polynomials with order less than 2 can be solved
    // in this function.
    protected void _solvePoleZero() throws IllegalArgumentException { 
         Complex pole, zero; 
 
         Vector singlepole = new Vector();
         Vector singlezero = new Vector();
         Vector conjpole = new Vector();
         Vector conjzero = new Vector();
         if (_numerator.length == 1) {
             // single scalar value, no pole/zero produced
         
         } else if (_numerator.length == 2) {
             // case a + bz^-1
             Complex[] roots = new Complex[2];
             double a = _numerator[0];
             double b = _numerator[1];
             boolean conjugate = MathWizard.realquadraticRoots(0.0,a,b,roots);
             if (roots[0] != null) {
                 pole = new Complex(0);
                 singlepole.addElement(pole);
                 singlezero.addElement(roots[0]);
             }
         } else if (_numerator.length == 3){
             // case a + bz^-1 + cz^-2
             // solve this numerator use quadratic eq produce 
             // two zero.  Since quadratic function solves positive power 
             // polynomial, thus the "c" in numerator will be "a" in the 
             // quadratic equation, and "a" in the numerator will be "c" 
             // in the quadratic equation. 
             double a = _numerator[0];   
             double b = _numerator[1];   
             double c = _numerator[2]; 
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
                             new Complex(0));
                 
                  conjpole.addElement(conjp);
             } else {
                  if (roots[0] == null && roots[1] == null) {
                  
                  } else if (roots[1] == null) {
                      pole = new Complex(0);
                      singlepole.addElement(pole);
                      singlezero.addElement(roots[0]);
                  }
                  else {
                      // produce two single real zeroes, and two single
                      // poles at infinity
                            
                      pole = new Complex(0);
                      singlepole.addElement(pole);
                      singlezero.addElement(roots[0]);
                      singlepole.addElement(pole);
                      singlezero.addElement(roots[1]);
                  }
              }
         } else {
            String str = new String("Only order less than or equal to 2 can be factored ");
            throw new IllegalArgumentException(str);
         }

         if (_denominator.length == 1) {
             // single scalar value, no pole/zero produced
         
         } else if (_denominator.length == 2) {
             // case a + bz^-1
             Complex[] roots = new Complex[2];
             double a = _denominator[0];
             double b = _denominator[1];
             boolean conjugate = MathWizard.realquadraticRoots(0.0,a,b,roots);
             if (roots[0] != null) {
                 zero = new Complex(0);
                 singlezero.addElement(zero);
                 singlepole.addElement(roots[0]);
             }
         } else if (_denominator.length == 3){
             // case a + bz^-1 + cz^-2
             // solve this denomerator use quadratic eq produce 
             // two zero.  Since quadratic function solves positive power 
             // polynomial, thus the "c" in numerator will be "a" in the 
             // quadratic equation, and "a" in the denomerator will be "c" 
             // in the quadratic equation. 

             double a = _denominator[0];   
             double b = _denominator[1];   
             double c = _denominator[2]; 
             Complex [] roots = new Complex[2]; 
             
             boolean conjugate = 
                 MathWizard.realquadraticRoots(a, b, c, roots); 
             
             if (conjugate){
                 // produce two complex conjugate poles, and two
                 // conjugate zeroes at infinity.
                 ConjugateComplex conjp = new ConjugateComplex(roots[0]);
                 ConjugateComplex conjz = 
                     new ConjugateComplex(new Complex(0));
                 
                 conjzero.addElement(conjz);
                 conjpole.addElement(conjp);
             } else {
                 if (roots[0] == null && roots[1] == null) {
                     
                 } else if (roots[1] == null) {
                     zero = new Complex(0);
                     singlezero.addElement(zero);
                     singlepole.addElement(roots[0]);
                 }
                 else {
                     // produce two single real zeroes, and two single
                     // poles at infinity
                     zero = new Complex(0);
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
    }

    // Update the numerator with the new zero 
    protected void _updateNumerator(){

        Complex [] tmpnum = MathWizard.zeroesToPoly(getZeroes());
        _numerator = new double[tmpnum.length];

        // since this is real factor, thus only the real part of is kept 
        // Note:  zeroesToPoly convert array of roots to a positive power
        // polynomial with highest power coefficient first.  This is ok,
        // one can get the negative power z polynoimal by divide numerator
        // and denominator by the largest power of z.  And it turns out be
        // the same polynomial.  
        for (int i=0;i<tmpnum.length;i++){
            _numerator[i] = tmpnum[i].real;
        }
    }

    // Update the numerator with the new pole 
    protected void _updateDenominator(){
        Complex [] tmpden = MathWizard.zeroesToPoly(getPoles());
        _denominator = new double[tmpden.length];

        // since this is real factor, thus only the real part of is kept 
        for (int i=0;i<tmpden.length;i++){
            _denominator[i] = tmpden[i].real;
        }

    }

    // add or substract the first parameter by the second parameter 
    // depending on whether the second paramater is one or zero.  
    // One is add, and zero means substract.  If the first parameter 
    // is at the beginning of the circular number field, then substraction
    // will take the first parameter to the end of the circular 
    // number field.  If the first parameter is at the end, then
    // addition will take it to the beginning.  This function is 
    // used in the computation of the positions in a circular 
    // array.
    //  @param position the number to be perform add or subtract on
    // @param incr value to add to or substract from position
    // @param size the size of the circular number field
    protected int _circularAddition(int position, int incr, int size){
        int result = position;
        
        if (size == 0) {
            return result;
        }
        
        while (Math.abs(incr) > size) {
            if (incr > 0) {
                incr -= size;
            }
            if (incr < 0) {
                incr += size;
            }
        }
        
        result += incr;
        if (result >= size) {
            result -= size;
            return result;
        }
        else if (result < 0) {
            result += size;
            return result;
        }
        else return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

            
    private static final double TINY = 1.0e-6;    
}


