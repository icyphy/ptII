/* The DigitalFilter class, abstract

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

import ptolemy.math.*;
import collections.*;
//////////////////////////////////////////////////////////////////////////
//// DigitalFilter
/** 
The DigitalFilter class is an abstract class from which RealDigitalFilter and 
ComplexDigitalFilter class will be derived.  This class contains abstract 
methods for RealDigitalFilter and ComplexDigitalFilter to share.  These methods
should be overwritten in the derived classes.  DigitalFilter also contains a
IIR filter designing method that will return a RealDigitalFilter with the given
specifications.  

@author  David Teng(davteng@hkn.eecs.berkeley.edu), William Wu(wbwu@eecs.berkeley.edu), Albert Chen

@version %W%	%G%

@see classname
@see full-classname
*/

public abstract class DigitalFilter extends Filter{

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** reset all the states of the factors in the filter to 
     * zero.  This method should be overwritten in the derived classes.
     */
    public abstract void resetState();
   
    public abstract Complex[] getFrequencyResponse();

    public int getFreqStep(){
        return NUMSTEP;
    }
 
    /** Checks if the poles and zeros are still valid.  If not, calls
        _updatePolesZeroes(), then return zeroes.  If yes, just return
        zeroes. This method should be overwritten in the derived classes.
    */
    public abstract Complex[] getZeroes();
    
    /** Checks if the zeroes and poles are still valid.  If not, calls
        _updatePolesZeroes(), then return poles.  If no, just return
        poles.  This method should be overwritten in the derived classes.
    */
    public abstract Complex[] getPoles();

    public abstract void addPoleZero(Complex pole, Complex zero, boolean conj);


    /** Take as parameter a conjugate pair of pole locations and 
     * incorporate them into the current transfer function of the
     * filter.  This method should be overwritten in the derived classes.
     * @param polePair a ConjugateComplex instance containing the
     *                           values of a pair of poles that 
     *                           is to added
     */
    //public abstract void addPolePair(ConjugateComplex zeroPair);
    
    /** Take as parameter a conjugate pair of zero locations and 
     * incorporate them in to the current transfer function of the 
     * filter.  This method should be overwritten in the derived classes.
     * @param zeroPair a ConjugateComplex instance containing the
     *                           values of a pair of zeroes that
     *                           is to be added
     */
    //public abstract void addZeroPair(ConjugateComplex polePair);

    /** Given a pole, deletePole will find the factor associated 
     * with this pole and delete that factor.  This method should be 
     * overwritten in the derived classes.
     * @param pole the pole to be deleted
     */
    public abstract void deletePole(Complex pole);
    
    /** Given a zero, deleteZero will find the factor associated
     * with this zero and delete that factor.  This method should be 
     * overwritten in the derived classes.
     * @param zero the zero to be deleted
     */
    public abstract void deleteZero(Complex zero);
    
    /** Take as parameter a zero and the value for its new location and
     * and updates the value of the zero's location to the new one.  This 
     * method should be overwritten in the derived classes.
     * @param zero the zero that is to be moved
     * @param real real value of the new location
     * @param imag imaginary value of the new location
     */
    public abstract void moveZero(Complex zero, double real, double imag);

    /** Take as parameter a pole and the value for its new location and
     * and updates the value of the pole's location to the new one.  This
     * method should be overwritten in the derived classes.  
     * @param pole the pole that is to be moved
     * @param real real value of the new location
     * @param imag imaginary value of the new location
     */
    public abstract void movePole(Complex zero, double real, double imag);

    /**
     * Design the IIR Filter according to the parameters.  
     * AnalogFilter.designRealIIR will be called to obtained a RealAnalogFilter
     * conforming the given specifications.  Then bilinear transform will be
     * used to transform the RealAnalogFilter into a RealDigitalFilter.
     * Given the RealAnalogFilter:
     *
     *  - The s-domain factors is transfered using specified transfer 
     *    method.  The result is z-domain quadratic factors of numerators 
     *    and denominators, and the gain of the filter (so that the filter 
     *    is unit gain at pass band). 
     *  
     *    @return RealDigitalFilter object that contain designed filter.   
     *    @param appmethod the approximation method used for design analog 
     *                     filter.
     *    @param mapmethod transformation between analog and digital.
     *    @param filttype type of the filter.
     *    @param critfreq the critical frequencies of filter, given at 
     *                    magnitude of PI, i.e. 0.8 means the desired frequency
     *                    is at 0.8 PI radian frequency. 
     *    @param gain gains at various critical frequencies, value is between 
     *                0.0 ~ 1.0
     *    @param stoprippleheight stop band ripple height.
     *    @param passrippleheight pass band ripple height, used for Chebshev 
     *                            and Elliptical design method. 
     *    @param fs sampling frequency
     *
     */
    public static RealDigitalFilter designRealIIR(int mapmethod, int appmethod,
          int filttype, double [] critfreq, double [] gain, double fs){
        
        RealAnalogFilter aFilter = 
            AnalogFilter.designRealIIR(mapmethod, appmethod, filttype, 
                    critfreq, gain, fs);
        System.out.println("number of sfactors = " + 
                aFilter.getNumberOfFactors());
        
        RealSFactor[] factors = aFilter.getFactors();
        double[] numerator;
        double[] denominator;
        for (int i = 0; i < aFilter.getNumberOfFactors(); i++) {
            numerator = factors[i].getNumerator();
            denominator = factors[i].getDenominator();
            System.out.println("factor " + i);
            for (int j = 0; j < numerator.length; j++) {
                System.out.println("numerator = " + 
                        numerator[j]);
            }
            for (int k = 0; k < numerator.length; k++) {
                System.out.println("denominator = " + denominator[k]);
            }
        }
        Complex[] poles = aFilter.getPoles();
        Complex[] zeroes = aFilter.getZeroes();
        for (int i = 0; i<poles.length; i++){
            System.out.println(
                    "poles are = " + poles[i].real+" and "+poles[i].imag);
        }
        for (int i = 0; i<zeroes.length; i++){
            System.out.println(
                    "zeroes are = " + zeroes[i].real+" and "+zeroes[i].imag);
        }
        
        // Bilinear transfer back to z-domain
        RealDigitalFilter dFilter =
               _bilinearQuadTransform(aFilter, fs);

        System.out.println("number of zfactors = " + dFilter.getNumberOfFactors());
        System.out.println("how about?");
        return dFilter;
    }
            
        
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods should not have doc comments, they should
    // have regular C++ comments.

    /** Update the pole and zero locations of all the factors.  If a zero
     * is close to a pole, they will not be returned.  This method should
     * be overwritten in the derived classes.
     */
    private abstract void _updatePolesZeroes();

    
    /**
      * Does a bilinear transformation of all s-domain factors stored in 
      * filter, to z-domain factors and stores result into filter.
      * 
      * @param filter RealAnalogFilter that contain the s-domain factors 
      *               to be transformed.
      * @param fs sampling rate
      */ 
    
    // part of the following code is derived from IIRDEZN.c in 
    // "C Language Algorithms for DSP" P.163
    //
    // == start here
    private static RealDigitalFilter _bilinearQuadTransform(
            RealAnalogFilter filter, double fs){
        
        int iternum;
        if (filter.order%2==0){
            iternum = filter.order/2;
        } else{ 
            iternum = filter.order/2 + 1;
        }
        
        int k;
        k = 0;
        double gain = 1.0;
        double a0, a1, a2, b0, b1, b2;
        
        double znum [] = new double[3];
        double zden [] = new double[3];
        
        RealSFactor[] sFactors = filter.getFactors();
        RealZFactor[] zFactors = new RealZFactor[sFactors.length];

        for (int i = 0; i < zFactors.length; i++) {
            zFactors[i] = new RealZFactor();
        }
        

        for (int iter = 0; iter < iternum; iter++){
            
            // transformation 
            gain *= _bilinear(sFactors[iter], zFactors[iter], fs);
        }
        
        RealDigitalFilter designedFilter = new RealDigitalFilter();
        designedFilter.clearFactors();
        
        // add the gain of DigitalFilter 
        designedFilter.addFactor(new RealZFactor(new double[] {1}, 
        new double[] {1}, gain));
        System.out.println("gain = " + gain);
        
        // put in the transformed z factors
               
        for (int i = 0; i < zFactors.length; i++) {
            designedFilter.addFactor(zFactors[i]);
        }
       
        return designedFilter;
    }


    /**
     * Bilinear transformates of a given bi-quad, in sFactor,  
     * 
     * from:  a0+a1*s+a2*s^2            to:     c0*z^-2+c1*z^-1+z
     *       ----------------, sFactor       G* -----------------, zFactor
     *        b0+b1*s+b2*s^2                    d0*z^-2+d1*z^-1+z
     * 
     * Derivation of c1, c0, d1, d0, and gain can be found by 
     * substitude s=2*fs*(z-1)/(z+1) into s-domain factor.
     *
     * This code is based C codes appeared on P.165 in "C Language
     * Algorithms for Digital Signal Processing" by P.Embree & B. Kimble
     * Prentice Hall, Englewood Cliffs, NJ, 1991  
     *
     * @param sFactor s-domain biquad
     * @param zFactor z-domain biquad
     * @param fs sampling rate
     * @return gain of z-domain bi-quad .
     */  
    private static double _bilinear(RealSFactor sFactor, RealZFactor zFactor,
            double fs){
        double[] snum = sFactor.getNumerator();
        double[] sden = sFactor.getDenominator();
        double a0;
        double a1;
        double a2;
        double b0;
        double b1;
        double b2;
        
        if (snum.length == 3) {
            a2 = snum[0]; 
            a1 = snum[1]; 
            a0 = snum[2]; 
        }
        else if (snum.length == 2 & snum.length == 2) {
            a2 = 0;
            a1 = snum[0];
            a0 = snum[1];
        } else {
            a2 = 0;
            a1 = 0;
            a0 = snum[0];
        }
        
        if (sden.length == 3) {
            b2 = sden[0];
            b1 = sden[1];
            b0 = sden[2];
        } else if (sden.length == 2) {
            b2 = 0;
            b1 = sden[0];
            b0 = sden[1];
        } else {
            b2 = 0;
            b1 = 0;
            b0 = sden[0];
        }

        double gain;
        System.out.println("translate these to bilinear: "+a0+" "+a1+" "+a2+
                "/"+b0+" "+b1+" "+b2);
        
        double ad, bd;
        double c1, c0, d1, d0;
        
        // alpha denominator
               ad = 4.0*a2*fs*fs + 2.0*a1*fs+a0; 
        // beta denominator
               bd = 4.0*b2*fs*fs + 2.0*b1*fs+b0; 
        
        gain = ad/bd;
        
        d1 = (2.0*b0-8.0*b2*fs*fs)/bd;
        d0 = (4.0*b2*fs*fs-2.0*b1*fs+b0)/bd; 
        c1 = (2.0*a0-8.0*a2*fs*fs)/ad;
        c0 = (4.0*a2*fs*fs-2.0*a1*fs+a0)/ad; 
        
        double[] znum = new double[3];
        double[] zden = new double[3];
        
        znum[2] = c0;
        znum[1] = c1;
        znum[0] = 1.0;
        zden[2] = d0;
        zden[1] = d1;
        zden[0] = 1.0;
        
        System.out.println("znum = "+1.0+" "+c1+" "+c0);
        System.out.println("zden = "+1.0+" "+d1+" "+d0);

        zFactor.setNumerator(znum);
        zFactor.setDenominator(zden);
        
        return gain;
    }  
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    protected boolean _transferFnValid;
    protected LinkedList _factors;
    protected int _numberOfFactors;
    protected boolean _polesZeroesValid;

    protected Complex[] _freqResponse; 
  
    protected boolean _freqImpulseValid; 
    protected Complex[] _poles;
    protected Complex[] _zeroes;

    protected int NUMSTEP = 150;
}



