/* A complex number factor type class.

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

package ptolemy.math;

//////////////////////////////////////////////////////////////////////////
//// ComplexZFactor
/** 
 This is the complex number factor class.  It is the component that will be used
 to build complex number LTI transfer function for a digital system.  It store 
 two polynomial of negative power of z with complex coefficients, and a complex
 number gain.  Internal function is usded to factor pole/zero of this factor.  
 Methods are provided to extract and modify these poles and zeroes.  
 When pole/zero are modified the two polynomials are also updated to reflect 
 the changes.  The factor also stores the internal states of the factor.
 state is stored in an array with ordering latest state will be at the end
 of array, while the earliest will be at the beginning.

<p> 
@author  William Wu (wbwu@eecs.berkeley.edu)
@version %W%	%G%
*/

public class ComplexZFactor extends Factor{

    /** Construct a Complex factor of unit one.  The transfer function is :
     * <pre>
     *                1.0 + 0.0j 
     *  (1.0 + 0.0j) -----------
     *                1.0 + 0.0j 
     * </pre>
     *  
     */	
    public ComplexZFactor() {
        _numerator = new Complex[1];
        _numerator[0] = new Complex(1.0);
        _denominator = new Complex[1];
        _denominator[0] = new Complex(1.0); 
        _gain = new Complex(1.0);
        _solvePoleZero();
        _state = new Complex[0];
    }


    /** Construct a complex factor with the given paramerters.  The parameter
     * is two arrays of Complex describe the numerator/denominator polynomial
     * coefficients, and a complex value describe the gain.  The result transfer 
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
     * Poles and zeroes will be calculated.
     * <p>
     * @param numer numerator polynomial coefficients 
     * @param denom denominator polynomial coefficients
     * @param gain gain of this factor
     * @exception IllealArgumentException 
     *                if any input array is null
     */	
    public ComplexZFactor(Complex [] numer, Complex [] denom, Complex gain) 
                      throws IllegalArgumentException{

        if ((numer == null) || (denom == null)) {
            String str = new String("array of coefficients for numerator / denominator is null");
            throw new IllegalArgumentException(str);
        }
        _numerator = new Complex[numer.length];
        _denominator = new Complex[denom.length];
        _gain = new Complex(gain.real, gain.imag);
        for (int i=0;i<_numerator.length;i++){
            _numerator[i] = new Complex(numer[i].real, numer[i].imag);
        }

        for (int i=0;i<_denominator.length;i++){
            _denominator[i] = new Complex(denom[i].real, denom[i].imag);
        }
        _solvePoleZero();
        _state = new Complex[Math.max(numer.length-1, denom.length-1)];
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
        double [] tmpstate = _state;
        // reallocate
        _state = new double[tmpstate.length];
         
        // multiply input by the gain
        input = input * _gain;

        
        // shift the state vector to the left by 1
        System.arraycopy(tmpstate, 1, _state, 0, tmpstate.length-1);
 
        // compute the latest state with this equation
        // w[n] = k=1:N Sum(denom(k)*w[n-k] + input  
        double a = input;
        for (int i=1;i<_denominator.length;i++){
             a -= _denominator[i]*_state[_state.length-1-i];
        }
        _state[_state.length-1] = a;
 
        // compute the output using 
        // y[n] = k=0:M Sum(numer(k)*w[n-k]);
        for (int i=0;i<_numerator.length;i++){
             output += _numerator[i]*_state[_state.length-1-i];
        }
        
        // scale the output by the inverse of constant in denominator
        output = output/denominator[0];
      
        return output;

    }  

    /** Return the gain of this factor.   
     * @return double value equal to the gain 
     */	
    public double getGain(){
        return _gain;
    }

    /** Return the numerator of this factor.   
     * @return array of double values represent the numerator polynomials 
     *    coefficients, with decrease negative power of z ordering.  
     */	
    public double[] getNumerator(){
        return _numerator;
    }

    /** Return the denominator of this factor.   
     * @return array of double values represent the denominator polynomials 
     *    coefficients, with decrease negative power of z ordering.  
     */	
    public double[] getDenominator(){
        return _denominator;
    }

  
    /** Return all the poles of this factor.   
     * @return array of Complex poles
     */	
    public Complex[] getPoles(){
        return _streamlineComplex(_singlePole, _conjugatePole);
    }

    /** Return all the zeroes of this factor. 
     * @return array of Complex zeroes
     */	
    public Complex[] getZeroes(){
        return _streamlineComplex(_singleZero, _conjugateZero);
    }

    /** Return current state of this factor. 
     * @return current state 
     */	
    public double[] getState(){
        return _state;
    }

    /** Return current number of state of this factor. 
     * @return current number of state  
     */	
    public int getNumberOfState(){
        return _state.length;
    }

    
    /** Check if this factor contains the given pole.  
     * @param pole the given pole to be checked on.
     * @return boolean value indicating if the given pole is part of this 
     *         factor.
     */	
    public boolean ifPole(Complex pole){

         boolean found = false;

         for (int i=0;i<_singlePole.length;i++){
             if (_singlePole[i] == pole){
                 found = true;
             }
         }
       
         if (!found) {  
             for (int i=0;i<_conjugatePole.length;i++){                       
                 if ((pole == _conjugatePole[i].getValue()) 
                  || (pole == _conjugatePole[i].getConjValue())){
                       found = true;
                       break;
                   }
               }
          }
          return found; 
    }

    /** Check if this factor contains the given zero. 
     * @param zero the given zero to be checked on.
     * @return boolean value indicating if the given pole is part of this factor
     */	
    public boolean ifZero(Complex zero){

         boolean found = false;

         for (int i=0;i<_singleZero.length;i++){
             if (_singleZero[i] == zero){
                 found = true;
             }
         }
       
         if (!found) {  
             for (int i=0;i<_conjugateZero.length;i++){                       
                 if ((zero == _conjugateZero[i].getValue()) 
                  || (zero == _conjugateZero[i].getConjValue())){
                       found = true;
                       break;
                   }
               }
          }
          return found; 
    }

    /** Move the given pole to the given value.  If the given pole is a single 
     * pole, then only its real value can be changed.  If the given pole is a 
     * part of conjugate pair poles, then the given pole will move to the 
     * desired location, its conjugate will also be changed to the appropriate 
     * value.  The denominator will be updated to 
     * reflect the change.
     * If the given pole can't be found in this factor, a NoSuchElementException
     * is thrown.  Thus this function should only be called when ifZero
     * returns true on the same zero. 
     * <p>
     * @param pole the given pole to be moved
     * @param real destination's real value
     * @param imag destination's imaginary value
     * @exception NoSuchElementException 
     *              if the given pole is not found in this factor.
     */	
    public void movePole(Complex pole, double real, double imag) 
                        throws NoSuchElementException{
         // first check if the given pole is in the single pole array
         boolean found = false;
         for (int i=0;i<_singlePole.length;i++){
             if (_singlePole[i] == pole){
                 // since this is a real factor, change only occurs in the real part
                 pole.real = real;
                 found = true;
             }
         }
         
         if (!found){
              // then check if the given pole is in the conjugate pole array
             for (int i=0;i<_conjugatePole.length;i++){                       
                  if ((pole == _conjugatePole[i].getValue()) 
                   || (pole == _conjugatePole[i].getConjValue())){
                       // update the conjugate pair to the new value
                       pole.real = real;
                       pole.imag = imag;
                       _conjugatePole[i].setValue(pole);
                       found = true;
                       break;
                   }
             }

             if (!found){
                 String str = new String("Given pole can not be found");
                 throw new NoSuchElementException(str);
             }
         } 
         _updateDenominator();
    }

    /** Move the given zero to the given value.  If the given zero is a single 
     * zero, then only its real value can be changed.  If the given zero is a 
     * part of conjugate pair zeros, then the given zero will move to the 
     * desired location, its conjugate will also be changed to the appropriate 
     * value.  The numerator will be updated to reflect the change.
     * If the given zero can't be found in this factor, a NoSuchElementException
     * is thrown.  Thus this function should only be called when ifZero
     * returns true on the same zero. 
     * <p>
     * @param zero the given zero to be moved
     * @param real destination's real value
     * @param imag destination's imaginary value
     * @exception NoSuchElementException 
     *              if the given pole is not found in this factor.
     */	
    public void moveZero(Complex zero, double real, double imag)
                        throws NoSuchElementException {

         // first check if the given zero is in the single pole array
         boolean found = false;
         for (int i=0;i<_singleZero.length;i++){
             if (_singleZero[i] == zero){
                 // since this is a real factor, change only occurs in the real part
                 zero.real = real;
                 found = true;
             }
         }
         
         if (!found){
             // then check if the given zero is in the conjugate pole array
             for (int i=0;i<_conjugateZero.length;i++){                       
                 if ((zero == _conjugateZero[i].getValue()) 
                  || (zero == _conjugateZero[i].getConjValue())){
                       // update the conjugate pair to the new value
                       zero.real = real;
                       zero.imag = imag;
                       _conjugateZero[i].setValue(zero);
                       found = true;
                       break;
                 }
             }

             if (!found){
                 String str = new String("Given zero can not be found");
                 throw new NoSuchElementException(str);
             }
         } 
         _updateNumerator();
    }

    /**
     * Reset the state to array of zero.
     */ 
    public void resetState(){
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
            _state[i] = state[i];
        }
    }      

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Solve the pole/zero of this factor 
    // Currently only the polynomials with order less than 2 can be solved
    // in this function.
    private void _solvePoleZero() throws IllegalActionException { 
         Complex pole, zero; 
 
         Vector singlepole = new Vector();
         Vector singlezero = new Vector();
         Vector conjpole = new Vector();
         Vector conjzero = new Vector();

         if (_numerator.length == 1) {
             // single scalar value, no pole/zero produced
         
         } else if (_numerator.length == 2) {
             // case a + bz^-1
             if (_numerator[0] < TINY) {
                 // case a = 0
                 // produce a pole at origin, and zero at infinity 
                 pole = new Complex(0.0);
                 zero = new Complex(Double.POSITIVE_INFINITY);
             } else {
                 // case a != 0
                 // produce a pole at origin, and zero at -b/a 
                 pole = new Complex(0.0);
                 zero = new Complex(-_numerator[1]/_numerator[0]);
             }
             singlepole.addElement(pole);
             singlezero.addElement(zero);
         } else if (_numerator.length == 3){
             // case a + bz^-1 + cz^-2
             if (_numerator[0] < TINY) {
                 // case a = 0
                 // produce two single poles at origin, and a zero 
                 // at infinity, and a zero at -c/b
                 pole = new Complex(0.0); 
                 zero = new Complex(Double.POSITIVE_INFINITY);
                 singlepole.addElement(pole);
                 singlezero.addElement(zero);
                 pole = new Complex(0.0);
                 zero = new Complex(-_numerator[2]/_numerator[1]);
                 singlepole.addElement(pole);
                 singlezero.addElement(zero);
             } else {
                 // case a != 0, solve this numerator use quadratic eq produce 
                 // two zero.  Since quadratic function solves positive power 
                 // polynomial, thus the "c" in numerator will be "a" in the 
                 // quadratic equation, and "a" in the numerator will be "c" 
                 // in the quadratic equation. 
                 double a = _numerator[2];   
                 double b = _numerator[1];   
                 double c = _numerator[0]; 
 
                 Complex roots = new Complex[2]; 
                 
                 boolean conjugate = MathWizard.realquadraticRoots(a, b, c, roots); 
 
                 if (conjugate){
                     // produce two complex conjugate zeroes, and two
                     // conjugate poles at origin.
                     ConjugateComplex conjz = new ConjugateComplex(roots[0]);
                     conjzero.addElement(conjz);
                     ConjugateComplex conjp = new ConjugateComplex(new Complex(0.0));
                      
                     conjpole.addElement(conjp);
                 } else {
                     // produce two single real zeroes, and two single
                     // poles at origin
                     pole = new Complex(0.0);
                     singlepole.addElement(pole);
                     singlezero.addElement(roots[0]);
                     pole = new Complex(0.0);
                     singlepole.addElement(pole);
                     singlezero.addElement(roots[1]);
                 }
             }
         } else {
            String str = new String("Only order less than or equal to 2 can be factored ");
            throw new IllegalActionException(str);
         }

         if (_denumerator.length == 1) {
             // single scalar value, no pole/zero produced
         
         } else if (_denumerator.length == 2) {
             // case a + bz^-1
             if (_denomerator[0] < TINY) {
                 // case a = 0
                 // produce a zero at origin, and pole at infinity 
                 zero = new Complex(0.0);
                 pole = new Complex(Double.POSITIVE_INFINITY);
             } else {
                 // case a != 0
                 // produce a zero at origin, and pole at -b/a 
                 zero = new Complex(0.0);
                 pole = new Complex(-_denomerator[1]/_denomerator[0]);
             }
             singlepole.addElement(pole);
             singlezero.addElement(zero);
         } else if (_denomerator.length == 3){
             // case a + bz^-1 + cz^-2
             if (_denomerator[0] < TINY) {
                 // case a = 0
                 // produce two single zeroes at origin, and a pole 
                 // at infinity, and a pole at -c/b
                 zero = new Complex(0.0); 
                 pole = new Complex(Double.POSITIVE_INFINITY);
                 singlepole.addElement(pole);
                 singlezero.addElement(zero);
                 zero = new Complex(0.0);
                 pole = new Complex(-_denomerator[2]/_denomerator[1]);
                 singlepole.addElement(pole);
                 singlezero.addElement(zero);
             } else {
                 // case a != 0, solve this denomerator use quadratic eq produce 
                 // two zero.  Since quadratic function solves positive power 
                 // polynomial, thus the "c" in numerator will be "a" in the 
                 // quadratic equation, and "a" in the denomerator will be "c" 
                 // in the quadratic equation. 
                 double a = _denomerator[2];   
                 double b = _denomerator[1];   
                 double c = _denomerator[0]; 
 
                 Complex roots = new Complex[2]; 
                 
                 boolean conjugate = MathWizard.realquadraticRoots(a, b, c, roots); 
 
                 if (conjugate){
                     // produce two complex conjugate poles, and two
                     // conjugate zeroes at origin.
                     ConjugateComplex conjp = new ConjugateComplex(roots[0]);
                     conjpole.addElement(conjp);
                     ConjugateComplex conjz = new ConjugateComplex(new Complex(0.0));
                      
                     conjpole.addElement(conjz);
                 } else {
                     // produce two single real poles, and two single
                     // zeroes at origin
                     zero = new Complex(0.0);
                     singlezero.addElement(zero);
                     singlepole.addElement(roots[0]);
                     zero = new Complex(0.0);
                     singlezero.addElement(pole);
                     singlepole.addElement(roots[1]);
                 }
             }
         } else {
            String str = new String("Only order less than or equal to 2 can be factored ");
            throw new IllegalActionException(str);
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



    //     
    // Convert a linked list of single complex number, and a linked 
    // list of complex conjugate pairs to an array of complex number.
    //       
    // @param single array of single complex number
    // @param conj array of complex conjugate pairs
    // @return array of all complex numbers
    private Complex [] _streamlineComplex(Complex[] single, 
                                          ConjugateComplex[] conj){

       
        int length = single.length + 2*conj.length; 
        Complex [] allComplex = new Complex[length];

        int ind;
        for (ind=0;ind<single.length;ind++){
            allComplex[ind] = single[ind];
        }
                
        for (int i=0;i<conj.length;i++){
            ConjugateComplex conjcomplex = conj[i];
            allComplex[ind++] = conjcomplex.getValue();
            allComplex[ind++] = conjcomplex.getConjValue();
        }

        return allComplex;
    }


    // Update the numerator with the new zero 
    private void _updateNumerator(){

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
    private void _updateDenominator(){
        Complex [] tmpden = MathWizard.zeroesToPoly(getPoles());
        _denominator = new double[tmpden.length];

        // since this is real factor, thus only the real part of is kept 
        for (int i=0;i<tmpnum.length;i++){
            _denominator[i] = tmpden[i].real;
        }

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private double [] _numerator;
    private double [] _denominator;
    private double _gain;

    private double [] _state;

    private Complex [] _singlePole;
    private Complex [] _singleZero;
    private ConjugateComplex[] _conjugatePole;
    private ConjugateComplex[] _conjugateZero;

    private static final double TINY = 1.0e-6;    
}
