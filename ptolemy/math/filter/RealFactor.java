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
import ptolemy.math.Complex;
 
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
@author  David Teng(davteng@hkn.eecs.berkeley.edu)
@version %W%	%G%
*/

public abstract class RealFactor extends Factor{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** replaces the current numerator with the new numerator in parameter
     * @param numerator the new numerator to replace the old one
     */
    public void setNumerator(double[] numerator) {
        _numerator = new double[numerator.length];
        System.arraycopy(numerator, 0, _numerator, 0, numerator.length);
        _state = new double[Math.max(_numerator.length-1, 
                _denominator.length-1)];
        _solvePoleZero();
    }
    
    /** replaces the current denominator with the new denominator in parameter
     * @param denominator the new denominator to replace the old one
     */
    public void setDenominator(double[] denominator) {
        _denominator = new double[denominator.length];
        System.arraycopy(denominator, 0, _denominator, 0, denominator.length);
        _state = new double[Math.max(_numerator.length-1, 
                _denominator.length-1)];
        _solvePoleZero();
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
     * @return boolean value indicating if the given pole is part of this 
     * factor
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
                System.out.println(
                        "moving the pole to Real: "+real+" Imag: "+imag);
     // first check if the given pole is in the single pole array
     boolean found = false;
     for (int i=0;i<_singlePole.length;i++){
         if (_singlePole[i] == pole){
         // since this is a real factor, change only occurs in the real part
         pole =  new Complex(real, pole.imag);
         found = true;
         }
     }
         
     if (!found){
         // then check if the given pole is in the conjugate pole array
         for (int i=0;i<_conjugatePole.length;i++){                       
             if ((pole == _conjugatePole[i].getValue()) 
                     || (pole == _conjugatePole[i].getConjValue())){
                 // update the conjugate pair to the new value
                 pole = new Complex(real, imag);
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
     * If the given zero can't be found in this factor, a 
     * NoSuchElementException
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

System.out.println("moving the zero to Real: "+real+" Imag: "+imag);
         // first check if the given zero is in the single pole array
         boolean found = false;
         for (int i=0;i<_singleZero.length;i++){
             if (_singleZero[i] == zero){
                 // since this is a real factor, change only occurs in the real part
                 zero = new Complex(real, zero.imag);
                 found = true;
             }
         }
         
         if (!found){
             // then check if the given zero is in the conjugate pole array
             for (int i=0;i<_conjugateZero.length;i++){                       
                 if ((zero == _conjugateZero[i].getValue()) 
                  || (zero == _conjugateZero[i].getConjValue())){
                       // update the conjugate pair to the new value
                       zero = new Complex(real, imag);
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
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                   ////

    // Solve the pole/zero of this factor 
    // Currently only the polynomials with order less than 2 can be solved
    // in this function.
    protected abstract void _solvePoleZero() throws IllegalArgumentException;
            
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
    private int _circularAddition(int position, int incr, int size){
        int result = position;
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
 
    // Update the numerator with the new zero 
    protected abstract void _updateNumerator();
    
    // Update the denominator with new pole
    protected abstract void _updateDenominator();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
      
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


    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    protected double [] _numerator;
    protected double [] _denominator;
    protected double _gain;
    
    // for RealZFactor use
    protected double [] _state;
    protected int _firstState;

    protected Complex [] _singlePole;
    protected Complex [] _singleZero;
    protected ConjugateComplex[] _conjugatePole;
    protected ConjugateComplex[] _conjugateZero;

}
