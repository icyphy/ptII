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

@author  David Teng(davteng@hkn.eecs.berkeley.edu), William Wu(wbwu@eecs.berkeley.edu)

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

    public abstract void addPoleZero(Complex pole, Complex zero, 
            double gain, boolean conj);

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
     * AnalogFilter.designLowpassRealIIR will be called to obtained a 
     * RealAnalogFilter conforming the given the prewarped specifications.  
     * Then frequency transformation will be performed to transform the 
     * lowpass filter to the desired filter type.  Bilinear transform will be
     * used to transform the RealAnalogFilter into a RealDigitalFilter.
     * Given the RealAnalogFilter:
     *
     *    The s-domain factors is transformed using specified transform 
     *    method.  The result is z-domain quadratic factors of numerators 
     *    and denominators, and the gain of the filter (so that the filter 
     *    is unit gain at pass band). 
     *  
     *    @return RealDigitalFilter object that contain designed filter.   
     *    @param appmethod the approximation method used for design analog 
     *                     filter: Butterworth, Chebyshev I and II.
     *    @param mapmethod transformation between analog and digital.
     *    @param filttype type of the filter.
     *    @param critfreq the critical frequencies of filter, given at 
     *                    magnitude of PI, i.e. 0.8 means the desired frequency
     *                    is at 0.8 PI radian frequency. 
     *    @param gain gains at various critical frequencies, value is between 
     *                0.0 ~ 1.0
     *    @param fs sampling frequency
     *
     */
    public static RealDigitalFilter designRealIIR(int mapmethod, int appmethod,
            int filttype, double [] critfreq, double [] gain, double fs) throws
            IllegalArgumentException {
        
        // digital and analog protoype pass edge frequencies
        double dPassEdgeFreq, aPassEdgeFreq;  
 
        // prototype pass edge gain
        double passEdgeGain;          

        // digital and analog prototype stop edge frequencies
        double dStopEdgeFreq, aStopEdgeFreq;  

        // prototype stop edge gain 
        double stopEdgeGain;          

        // digital and analog band width
        double dBandwidth, aBandwidth;  

        // digital critical frequency, used for frequency transformation
        double wc;
        
        // get the spec
        // for lowpass and bandstop filter, the pass band/stop band will 
        // be the same as the ones used for designing prototype lowpass.  
        // While highpass and bandpass's pass band/stop band is the 
        // difference between PI and corresponding lowpass prototype's 
        // pass band/stop band. 
        if ((filttype == Filter.LOWPASS) || 
            (filttype == Filter.BANDSTOP)){

            dPassEdgeFreq = critfreq[0];
            dStopEdgeFreq = critfreq[1];
            passEdgeGain = gain[0];
            stopEdgeGain = gain[1];

        } else if ((filttype == Filter.HIGHPASS) || 
                (filttype == Filter.BANDPASS)){               
            dStopEdgeFreq = Math.PI-critfreq[0];
            dPassEdgeFreq = Math.PI-critfreq[1];
            stopEdgeGain = gain[0];
            passEdgeGain = gain[1];
            
        } else {
            throw new IllegalArgumentException("Filter type not supported");
        }
         
        // Prewarp the spec from digital frequency to analog
        if (mapmethod == Filter.BILINEAR){  
            // only bilinear is supported
            aPassEdgeFreq = _bilinearPreWarp(dPassEdgeFreq, fs); 
            aStopEdgeFreq = _bilinearPreWarp(dStopEdgeFreq, fs); 
        } else {
            throw new IllegalArgumentException(
                    "Only Bilinear transform is supported"); 
        }
        
        // first design an analog lowpass filter
        RealAnalogFilter aFilter = 
               AnalogFilter.designRealLowpassIIR(mapmethod, appmethod, 
                       filttype, aPassEdgeFreq, aStopEdgeFreq, passEdgeGain, 
                       stopEdgeGain);
        
        // now do the preparation for frequency transformation that would  
        // transform the designed analog lowpass filter into highpass, 
        // bandpass, or bandstop filter
        if (filttype == Filter.HIGHPASS){
            
            if (mapmethod == Filter.BILINEAR){
                
                // convert the equivalent lowpass
                // analog cutoff frequency to digital 
                // frequency in bilinear, then take its difference with PI, 
                // thus the cutoff digital frequency of the high pass, 
                // then use bilinear to convert it back to analog.
                // frequency transformation is in s domain, 
                // maybe that should be changed.

                // digital lowpass cutoff freq
                double tmp = _revBilinearPreWarp(aFilter.analogfc, fs);

                // digital highpass cutoff freq
                wc = Math.PI - tmp; 
 
                // analog highpass cutoff freq
                aFilter.analogfc = _bilinearPreWarp(wc, fs);
            
            } else { 
                throw new IllegalArgumentException(
                        "Only Bilinear is supported"); 
            }

        } else if (filttype == Filter.BANDPASS){

            if (mapmethod == Filter.BILINEAR){

                // digital lowpass cutoff freq
                double tmp = _revBilinearPreWarp(aFilter.analogfc, fs);

                // digital highpass cutoff freq
                wc = Math.PI - tmp;
       
                // analog highpass cutoff freq
                aFilter.analogfc = _bilinearPreWarp(wc, fs);

                // analog second highpass cutoff, at -3db  
                double fc2 = _bilinearPreWarp(critfreq[2], fs);

                // analog center is the geometric mean of two cut off 
                // frequencies
                aFilter.analogFreqCenter = Math.sqrt(aFilter.analogfc*fc2);
              
                // analog width
                aFilter.analogFreqWidth = fc2 - aFilter.analogfc;
                
            } else { 
                throw new IllegalArgumentException(
                        "Only Bilinear is supported"); 
            }
            
        } else if (filttype == Filter.BANDSTOP){
            
            if (mapmethod == Filter.BILINEAR){
                
                // analog second lowpass cutoff
                double fc2 = _bilinearPreWarp(critfreq[2], fs);

                // analog center is geometrical mean of two cutoff frequencies
                aFilter.analogFreqCenter = Math.sqrt(aFilter.analogfc*fc2);
                
                // analog width
                aFilter.analogFreqWidth = fc2 - aFilter.analogfc;
           
            } else { 
                throw new IllegalArgumentException(
                        "Only Bilinear is supported"); 
            }
            
        }
        
        // Transform the Lowpass analog filter to the appropriate filter type
        // The filter's parameters should be changed to the desired values 
        // before performing the frequency transformation
        AnalogFilter.freqTransformation(aFilter, filttype);
        
        // Bilinear transfer back to z-domain
        RealDigitalFilter dFilter =
               _bilinearQuadTransform(aFilter, fs);

        double[] num = dFilter.getNumerator();
        double[] den = dFilter.getDenominator();
        for(int i = 0; i<num.length; i++) {
           System.out.println("num = "+num[i]);
        }
        for(int j = 0; j<num.length; j++) {
           System.out.println("den = "+den[j]);
        }
        double g = dFilter.getGain();
        System.out.println("gain = "+g);
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
        

        for (int iter = 0; iter < sFactors.length; iter++){
             // transformation 
             _bilinear(sFactors[iter], zFactors[iter], fs);
        }
        
        RealDigitalFilter designedFilter = new RealDigitalFilter();
        designedFilter.clearFactors();
        
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
        double sgain = sFactor.getGain();
        
        double a0;
        double a1;
        double a2;
        double b0;
        double b1;
        double b2;
        
        if (snum.length == 3) {
            a2 = snum[0]*sgain; 
            a1 = snum[1]*sgain; 
            a0 = snum[2]*sgain; 
        }
        else if (snum.length == 2 & snum.length == 2) {
            a2 = 0;
            a1 = snum[0]*sgain;
            a0 = snum[1]*sgain;
        } else {
            a2 = 0;
            a1 = 0;
            a0 = snum[0]*sgain;
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
        
        zFactor.setNumerator(znum);
        zFactor.setDenominator(zden);
        zFactor.setGain(gain);

        return gain;
    }  

         
    /**
     * Does a prewarp that transforms the digital frequencie to 
     * analog one, used in bilinear transformation.
     *
     * @param digitalfreq digital frequency 
     * @param fs sampling rate 
     */ 
    private static double _bilinearPreWarp(double digitalfreq, double fs){
        return Math.tan(digitalfreq/2)*fs*2;
    } 

    /**
     * Does a reverse prewarp that transforms the analog frequencie to 
     * digital one, used in bilinear transformation.
     *
     * @param analogfreq analog frequency 
     * @param fs sampling rate 
     */ 
    private static double _revBilinearPreWarp(double analogfreq, double fs){
        return 2*Math.atan(analogfreq/(2*fs));
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected variables                 ////

    protected boolean _transferFnValid;
    protected LinkedList _factors;
    protected boolean _polesZeroesValid;

    protected Complex[] _freqResponse; 
  
    protected boolean _freqImpulseValid; 
    protected Complex[] _poles;
    protected Complex[] _zeroes;

    protected int NUMSTEP = 150;
}









