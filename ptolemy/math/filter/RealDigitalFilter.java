/* The RealDigitalFilter class

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

import collections.*;

//////////////////////////////////////////////////////////////////////////
//// RealDigitalFilter
/** 
   The RealDigitalFilter class implements, as the name states, real digital 
   filters.  This class contains a list of factors that represent the transfer
   function of the filter.  Some methods supported are addPole, movePole, 
   addFactor, deletePole, setTransferFn, getOuput, getResponse, methods to set
   the properties of a digital filter.  One can create an instance of 
   RealDigitalFilter given a transfer function or pole/zero location.  On 
   default, a RealDigitalFilter will constructed with a transfer function of 
   one.  To support display engines, a cached/refined version of the filter's
   poles and zeros will kept inside the RealDigitalFilter, so that the display
   engines can access these efficiently.  The poles and zeroes do not have to 
   be retrieved from the factors one by one, if they were never changed from
   the last retrieval.  The poles and zeroes in the cache will still be valid
   for use.  
   
   @author  David Teng (davteng@hkn.eecs.berkeley.edu)
   @version %W%	%G%
*/


public class RealDigitalFilter extends DigitalFilter{
    
    /** Default Constructor
     * Construct a Real Filter with a transfer function
     * of one
     */
    public RealDigitalFilter() {
        _factors = new LinkedList();
        _numberOfFactors = 0;
        addFactor(new RealZFactor());
        _poles = new Complex[0];
        _zeroes = new Complex[0];
        _updatePolesZeroes();
        _updateTransferFn();
        _updateFreqImpResponse();
    }
    
    /** Construct a Real Filter given poles, zeroes, and gain
     * @param poles the locations of the real poles
     * @param zeroes the locations of the real zeroes
     * @param polePairs the locations of the conjugate poles
     * @param zeroPairs the locations of the conjugate zeroes
     * @param gain gain of the real filter
     */
    /*
    public RealDigitalFilter(double[] poles, double[] zeroes, 
            ConjugateComplex[] polePairs, ConjugateComplex[] zeroPairs, 
            double gain) {
        _factors = new LinkedList();
        _poles = new Complex[poles.length + 2*polePairs.length];
        _zeroes = new Complex[zeroes.length + 2*zeroPairs.length];
        int polesCount = 0;
        int zeroesCount = 0;
        _numberOfFactors = 0;
        
        for (int i = 0; i < poles.length; i++) {
            addPoleZero(poles[i], zeroes[i], false);
        }
        
        for (int m = 0; m < polePairs.length; m++) {
            addPoleZero(
        }
        _updatePolesZeroes();
    }
    */
    /** Construct Filter given transfer function
     * The transfer function is in the following form
     * <pre>
     *
     *       numer[0] + numer[1]*x + numer[2]*x^2 + numer[3]*x^3 ...
     * gain* -------------------------------------------------------
     *       denom[0] + denom[1]*x + denom[2]*x^2 + denom[3]*x^3 ...
     *
     * </pre>
     * <p>
     * @param numer numerator polynomial coefficients 
     * @param denom denominator polynomial coefficients
     * @param gain gain of this transfer function
     */
    public RealDigitalFilter(double[] numer, double[] denom,
            double gain) {
        _factors = new LinkedList();
        _numberOfFactors = 0;
        addFactor(new RealZFactor(numer, denom, gain));
        _updatePolesZeroes();
        _updateTransferFn();
        _updateFreqImpResponse();
    }
    
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** clears all the factors
     */
    public void clearFactors() {
        _factors.clear();
        _numberOfFactors = 0;
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }
    
    /** Take as argument a transfer function represented by a 
     * list of factors, f, to replace the existing transfer 
     * function of the filter
     * @param f a list factors representing the new transfer
     *      function to be designed
     */
    public void setTransferFn(RealZFactor[] f) {
        _factors.clear();
        
        // doing insert instead of addFactor because this avoid setting
        // _polesZeroesValid = false  f.length times
        for (int i = 0; i < f.length; i++) {
            _factors.insertFirst(f[i]);
        }

        _numberOfFactors = f.length;
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }
    
    /** Takes parameter f, a factor, and multiply it to the 
     * existing transfer function of the filter
     * @param f a factor to added the transfer function
     */
    public void addFactor(RealZFactor f) {
        _factors.insertFirst(f);
        _numberOfFactors++;
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }

    /** returns the number of factors associated with this RealDigitalFilter
     */
    public int getNumberOfFactors() {
        return _numberOfFactors;
    }

    /** impulse response of the RealDigitalFilter
     */
    public double[] getImpulseResponse() {
        if (_freqImpulseValid == true) {
            return _impulseResponse;
        }
        else {
            _updateFreqImpResponse();
            return _impulseResponse;
        }
    }
    
    /** get the numerator of the filter's transfer function
     */
    public double[] getNumerator() {
        if (_transferFnValid == true) {
            return _numerator;
        }
        else {
            _updateTransferFn();
            return _numerator;
        }
    }
    
    /** get the denominator of the filter's transfer function
     */
    public double[] getDenominator() {
        if (_transferFnValid == true) {
            return _denominator;
        }
        else {
            _updateTransferFn();
            return _denominator;
        }
    }

    /** returns the total gain of the RealDigitalFilter
     */
    public double getGain() {
        if (_transferFnValid == true) {
            return _gain;
        } else {
            _updateTransferFn();
            return _gain;
        }
    }
    
    
    /** Put the _factors LinkedList into an array and return it
     */
    public RealZFactor[] getFactors() {
        RealZFactor[] ZFactors = new RealZFactor[_numberOfFactors];
        for (int i = 0; i<_numberOfFactors; i++) {
            ZFactors[i] = (RealZFactor)_factors.at(i);
        }
        return ZFactors;
    }

    /** Takes parameter state, values of the state of the filter
     * and replaces the current state with the new one
     * @param state an array containing the new state of the 
     *                   filter
     */
    public void setState(RealZFactor f, double[] state) {
        f.setState(state);
    }
    
   
    /** reset all the states of the factors in the filter to 
     * zero
     */
    public void resetState() {
        
        for (int i = 0; i < _numberOfFactors; i++) {
            RealZFactor currentFactor = (RealZFactor)_factors.at(i);
            currentFactor.resetState();
        }
        
    }
     
    /** Takes parameter zero, a zero location, and finds the 
     * factor that contain this zero.  
     * @param zero a zero location
     */
    public RealZFactor getFactorWithZero(Complex zero) 
            throws IllegalArgumentException{
                int j = 0;
                
                while (j < _numberOfFactors) {
                    if (((RealZFactor)_factors.at(j)).ifZero(zero)) {
                        return (RealZFactor)_factors.at(j);
                    }
                    j++;
                }
                
                throw new 
                    IllegalArgumentException(
                            "cannot find the factor with the given zero");
    }
    
    /** Takes parameter pole, a pole location, and finds the 
     * factor that contain this pole.  
     * @param pole a pole location
     */
    public RealZFactor getFactorWithPole(Complex Pole) 
            throws IllegalArgumentException{
                int j = 0;
                
                while (j < _numberOfFactors) {
                    if (((RealZFactor)_factors.at(j)).ifPole(Pole)) {
                        return (RealZFactor)_factors.at(j);
                    }
                    j++;
                }
                
                throw new IllegalArgumentException(
                        "cannot find the factor with the given pole");
    }

    /** Checks if the poles and zeros are still valid.  If not, calls
        _updatePolesZeroes(), then return zeroes.  If yes, just return
        zeroes.
    */
    public Complex[] getZeroes() {

        if (_polesZeroesValid) {
            return _zeroes;
        }
        else {
            _updatePolesZeroes();
            return _zeroes;
        }
    }

    /** Checks if the zeroes and poles are still valid.  If not, calls
        _updatePolesZeroes(), then return poles.  If no, just return
        poles.
    */
    public Complex[] getPoles() {
        
        if (_polesZeroesValid) {
            return _poles;
        }
        else {
            _updatePolesZeroes();
            return _poles;
        }
    }
            
    /** Take as parameter a pole location and incorporate it into the
     * current transfer function of the filter
     * @param pole the value of a real pole's location that is to be 
     *                             added
     */
    public void addPoleZero(Complex pole, Complex zero, boolean conj) {
        
        if (pole.isInfinite() & zero.isInfinite()) {
            return;
        } else if (pole.equals(zero)) {
            return;
        }
        
        if (pole.isInfinite()) {
            double[] denom = {1};
            double[] numer;
            if (conj == false) {
                numer = new double[] {1, -zero.real};
            }
            else {
                Complex[] roots = new Complex[] {
                    zero, zero.conjugate()};
                Complex[] polyTemp = MathWizard.zeroesToPoly(roots);
                numer = new double[polyTemp.length];
                for (int i = 0; i < polyTemp.length; i++) {
                    numer[i] = polyTemp[i].real;
                }
                RealZFactor newFactor = new RealZFactor(numer, denom, 1);
                addFactor(newFactor);
                _polesZeroesValid = false;
                _transferFnValid = false;
                _freqImpulseValid = false;
            }
        } else if (zero.isInfinite()) {
            double[] numer = {1};
            double[] denom;
            if (conj == false) {
                denom = new double[] {1, -pole.real};
            } else {
                Complex[] roots = {pole, pole.conjugate()};
                Complex[] polyTemp = MathWizard.zeroesToPoly(roots);
                denom = new double[polyTemp.length];
                for (int i = 0; i < polyTemp.length; i++) {
                    denom[i] = polyTemp[i].real;
                }
            }
            RealZFactor newFactor = new RealZFactor(numer, denom, 1);
            addFactor(newFactor);
            _polesZeroesValid = false;
            _transferFnValid = false;
            _freqImpulseValid = false;
        }
        else {
            double[] numer;
            double[] denom;
            if (conj == false) {
               numer = new double[] {1, -zero.real};
               denom = new double[] {1, -pole.real};
            } else {
               Complex[] roots = new Complex[] {
                    zero, zero.conjugate()};
                Complex[] polyTemp = MathWizard.zeroesToPoly(roots);
                numer = new double[polyTemp.length];
                for (int i = 0; i < polyTemp.length; i++) {
                    numer[i] = polyTemp[i].real;
                }
                roots = new Complex[] {pole, pole.conjugate()};
                polyTemp = MathWizard.zeroesToPoly(roots);
                denom = new double[polyTemp.length];
                for (int i = 0; i < polyTemp.length; i++) {
                    denom[i] = polyTemp[i].real;
                }
            }
            RealZFactor newFactor = new RealZFactor(numer, denom, 1);
            addFactor(newFactor);
            _polesZeroesValid = false;
            _transferFnValid = false;
            _freqImpulseValid = false;
        }
    }
         
    //don't use for now
    /** Take as parameter a conjugate pair of pole locations and 
     * incorporate them into the current transfer function of the
     * filter
     * @param polePair a ConjugateComplex instance containing the
     *                           values of a pair of poles that 
     *                           is to added
     */
    /*
     public void addPolePair(ConjugateComplex polePair) {
        double[] numer = {1,0,0};
        Complex[] roots = {polePair.getValue(), polePair.getConjValue()};
        Complex[] denomTemp = MathWizard.zeroesToPoly(roots);
        double[] denom = new double[denomTemp.length];
        
        for (int i = 0; i < denomTemp.length; i++) {
            denom[i] = denomTemp[i].real;
        }
        
        RealZFactor newFactor = new RealZFactor(numer, denom, 1);
        addFactor(newFactor);
        _polesZeroesValid = false;
    }
    */
    /** Take as parameter a zero location and incorporate it into the
     * current transfer function of the filter
     * @param zero the value of a real zero's location that is to be
     *                            added
     */
    /*
    public void addZero(double zero) {
        double[] numer = {1,-zero};
        double[] denom = {1,0};
        RealZFactor newFactor = new RealZFactor(numer, denom, 1);
        addFactor(newFactor);
        _polesZeroesValid = false;
    }
    */
    /** Take as parameter a conjugate pair of zero locations and 
     * incorporate them in to the current transfer function of the 
     * filter
     * @param zeroPair a ConjugateComplex instance containing the
     *                           values of a pair of zeroes that
     *                           is to be added
     */
    /*
    public void addZeroPair(ConjugateComplex zeroPair) {
        Complex[] roots = {zeroPair.getValue(), zeroPair.getConjValue()};
        Complex[] numerTemp = MathWizard.zeroesToPoly(roots);
        double[] numer = new double[numerTemp.length];

        for (int i = 0; i < numerTemp.length; i++) {
            numer[i] = numerTemp[i].real;
        }

        double[] denom = {1,0,0};
        RealZFactor newFactor = new RealZFactor(numer, denom, 1);
        addFactor(newFactor);        
        _polesZeroesValid = false;
    }
    */
    /** Given a pole, deletePole will find the factor associated 
     * with this pole and delete that factor
     * @param pole the pole to be deleted
     */
    public void deletePole(Complex pole) {
        _factors.removeOneOf(getFactorWithPole(pole));
        _numberOfFactors--;
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }
    
    /** Given a zero, deleteZero will find the factor associated
     * with this zero and delete that factor
     * @param zero the zero to be deleted
     */
    public void deleteZero(Complex zero) {
        _factors.removeOneOf(getFactorWithZero(zero));
        _numberOfFactors--;
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }
    
    /** Take as parameter a zero and the value for its new location and
     * and updates the value of the zero's location to the new one
     * @param zero the zero that is to be moved
     * @param real real value of the new location
     * @param imag imaginary value of the new location
     */
    public void moveZero(Complex zero, double real, double imag) {
        RealZFactor factorWithGivenZero = getFactorWithZero(zero);
        factorWithGivenZero.moveZero(zero, real, imag);
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }
    
    /** Take as parameter a pole and the value for its new location and
     * and updates the value of the pole's location to the new one
     * @param pole the pole that is to be moved
     * @param real real value of the new location
     * @param imag imaginary value of the new location
     */
    public void movePole(Complex pole, double real, double imag) {
        RealZFactor factorWithGivenZero = getFactorWithPole(pole);
        factorWithGivenZero.movePole(pole, real, imag);
        _polesZeroesValid = false;
        _transferFnValid = false;
        _freqImpulseValid = false;
    }
        
    /** Take one sample of input and returns the output of the filter.
     * The input will go through each factor sequentially.
     *
     * <pre>
     *           ____________                       ____________
     *           |          |  input 1      input n |          |
     * input --->| Factor 1 | --------> * * * ----->| Factor n | ---> output
     *           |          |                       |          |
     *           ------------                       ------------
     * </pre>
     * <p>
     * @param input the input to be filtered 
     */
    public double getOutput(double input) {
        double output = input;
        
        for (int i = 0; i < _numberOfFactors; i++) {
            RealZFactor currentFactor = (RealZFactor)_factors.at(i);
            output = currentFactor.computeOutput(output);
            
        }
        return output;
    }
   
    /** takes a sequence of inputs and put them through the filter
     * @param input an array containing the values of samples
     * @param numSamples number of samples
     */
    public double[] getResponse(double[] input, int numSamples) {
        double[] output = new double[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            output[i] = getOutput(input[i]);
            
        }
        return output;
    }
    
    /** return the frequency response
     */
    public Complex[] getFrequencyResponse() {
        if (_freqImpulseValid == true) {
            return _freqResponse;
        }
        else {
            _updateFreqImpResponse();
            return _freqResponse;
        }
    }
        
        
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Checks if the distance between a pole and a zero is less than the
    // the given parameter distance
    private boolean _comparePoleZero(Complex pole, Complex zero, 
            double distance) {
        if (Double.isInfinite(pole.real)) {
            if (Double.isInfinite(zero.real)) {
                return true;
            }
        }
        else {
            if ((pole.subtract(zero)).mag() < distance) {
                return true;
            }
        }
    
        return false;
    }      

    /** Update the cached pole and zero locations of all the factors.  If 
     * a zero  is close to a pole, they will not be returned.  
    */
    private void _updatePolesZeroes() {
        
        LinkedList zeroesList = new LinkedList();
        LinkedList polesList = new LinkedList();
        RealZFactor currentFactor = new RealZFactor();
        int zeroesListLength = 0;
        int polesListLength = 0;
        
        int previousPolesCounter = 0;
        int checkLength = 0;
        boolean insertNewZero;
        
        // create a list of all the poles
        for (int i = 0; i < _numberOfFactors; i++) {
            currentFactor = (RealZFactor)_factors.at(i);
            Complex[] currentPoles = currentFactor.getPoles();
            for (int j = 0; j < currentPoles.length; j++) {
                polesList.insertFirst(currentPoles[j]);
                polesListLength++;
            }
        }
        
        // check each zero against each poles in polesList, and if a zero
        // and a pole are close, then the zero will not be inserted into 
        // the list, the pole will be removed from the list
        for (int i = 0; i < _numberOfFactors; i++) {
            currentFactor = (RealZFactor)_factors.at(i);
            Complex[] currentZeroes = currentFactor.getZeroes();
            
            for (int j = 0; j < currentZeroes.length; j++) {
                previousPolesCounter = 0;
                checkLength = polesListLength;
                insertNewZero = true;
                
                while (previousPolesCounter < checkLength) {
                    
                    if (_comparePoleZero(
                            (Complex)polesList.at(previousPolesCounter),
                            currentZeroes[j], DELTA)) {
                        polesList.removeAt(previousPolesCounter);
                        polesListLength--;
                        insertNewZero = false;
                        break;
                    }
                    
                    previousPolesCounter++;
                }
                
                // if zero is not close to any of the poles, then it is added
                // to the zeroesList
                if (insertNewZero) {
                    zeroesList.insertFirst(currentZeroes[j]);
                    zeroesListLength++;
                }
            }
        }
        
        // replace the old zeroes and poles with the new ones
        _zeroes = new Complex[zeroesListLength];
        _poles = new Complex[polesListLength];
        for (int i = 0; i < zeroesListLength; i++) {
             _zeroes[i] = (Complex)zeroesList.at(i);
        }
        for (int i = 0; i < polesListLength; i++) {
            _poles[i] = (Complex)polesList.at(i);
        }
        _polesZeroesValid = true;
    }           

    // update the cached version of the RealDigitalFilter's transfer function
    private void _updateTransferFn() {
        Complex[] numerator;
        Complex[] denominator;
        Complex[] tempFn;
        
        double[] partialFn = ((RealZFactor)_factors.at(0)).getNumerator();
        numerator = new Complex[partialFn.length];
        
        // put the first factor's numerator into Complex[] numerator
        for (int i = 0; i < partialFn.length; i++) {
            numerator[i] = new Complex(partialFn[i]);
        } 

        partialFn = ((RealZFactor)_factors.at(0)).getDenominator();
        denominator = new Complex[partialFn.length];
    
        /* put the first factor's denominator into Complex[] denominator
         */
        for (int i = 0; i < partialFn.length; i++) {
            denominator[i] = new Complex(partialFn[i]);
        }

        // multiply the numerators out
        for (int i = 1; i < _numberOfFactors; i++) {
            partialFn = ((RealZFactor)_factors.at(i)).getNumerator();
            tempFn = new Complex[partialFn.length];
            
            for (int j = 0; j < partialFn.length; j++) {
                tempFn[j] = new Complex(partialFn[j]);
            }
            
            numerator = MathWizard.polyMultiply(numerator, tempFn);
        }
    
        // multiply the denominators out
        for (int i = 1; i < _numberOfFactors; i++) {
            partialFn = ((RealZFactor)_factors.at(i)).getDenominator();
            tempFn = new Complex[partialFn.length];
            
            for (int j = 0; j < partialFn.length; j++) {
                tempFn[j] = new Complex(partialFn[j]);
            } 
            
            denominator = MathWizard.polyMultiply(denominator, tempFn);
        }

        _numerator = new double[numerator.length];
        _denominator = new double[denominator.length];

        for (int i = 0; i < numerator.length; i++) {
            _numerator[i] = numerator[i].real;
        }
        
        for (int i = 0; i < denominator.length; i++) {
            _denominator[i] = denominator[i].real;
        }

        _gain = 1;
        for (int i = 0; i < _numberOfFactors; i++) {
            _gain *= ((RealZFactor)_factors.at(i)).getGain();
        } 
    }

    

    private void _updateFreqImpResponse() {
        if (_polesZeroesValid == false) {
            _updatePolesZeroes();
        }
        double[] input = new double[_taps];
        input[0] = 1;
        _impulseResponse = new double[_taps];
        _impulseResponse = getResponse(input, _taps);
       
        for (int i=0;i<_poles.length;i++){
             System.out.println("freq poles : "+_poles[i].real+" "+_poles[i].imag); 
        }

        for (int i=0;i<_zeroes.length;i++){
             System.out.println("freq poles : "+_zeroes[i].real+" "+_zeroes[i].imag); 
        }

        _freqResponse = SignalProcessing.poleZeroToFreq(_poles, _zeroes,
                new Complex(_gain), NUMSTEP);
    }
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


                                     
    // Private variables should not have doc comments, they should
    // have regular C++ comments.
    
    private LinkedList _factors;
    private int _numberOfFactors;
        
    // cached transfer function with numerator and denominator with its 
    // validity indication flags
    private double[] _numerator;
    private double[] _denominator;
    private double _gain;

    // cached frequency and impulse response with its validity indication flags
    private double[] _impulseResponse;
    private int _taps = 50;
    
    
}

