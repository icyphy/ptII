/* The AnalogFilter class

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

//////////////////////////////////////////////////////////////////////////
//// AnalogFilter
/** 
The AnalogFilter class is an abstract class from which RealAnalogFilter and
ComplexAnalogFilter are derived.  This class contains shared methods of 
RealAnalogFilter and ComplexAnalogFilter.  These methods should be overwritten
in RealAnalogFilter and ComplexAnalogFilter.  AnalogFilter class also contains
method to designing an IIR filter with Butterworth and Chebychev.  These filter
design methods were pull out of MathWizard and modified to the new filter 
architecture.


@author David Teng(davteng@hkn.eecs.berkeley.edu), William Wu(wbwu@eecs.berkeley.edu)
@version %W%	%G%

*/
public abstract class AnalogFilter extends Filter {
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Design the IIR Filter according to the parameters.  
     * The design process is based on the technique of designing continous 
     * filter.  
     * Thus it involves in these steps:
     *
     *  - transform the spec from digital to analog with the specified 
     *    transfer method 
     *  - Given the spec(pass band freq and gain, stop band freq and gain), and 
     *    characteristic (Butterworth, Chebyshev, etc) of the filter, an analog
     *    prototype (lowpass, unit cutoff) is designed.  Along with the 
     *    analog cutoff freq of the prototype lowpass.  The prototype is 
     *    a RealAnalogFilter.
     *  - The analog cutoff for lowpass and other third band is translated 
     *    into data needed for frequency transformation.  
     *  - The prototype undergoes analog frequency transform that change 
     *    the unit cutoff lowpass to lowpass, highpass, bandpass or bandstop 
     *    at desired shape.  
     *  
     *    The filter object is returned back.  If specifictions is not yet 
     *    supported, or illeagl, null will be returned.
     *
     *    @return RealAnalogFilter object that contains designed filter.   
     *    @param appmethod the approximation method used for design analog 
     *                     filter.
     *    @param mapmethod transformation between analog and digital.
     *    @param filttype type of the filter.
     *    @param critfreq the critical frequencies of filter, given at 
     *                    magnitude of PI, i.e. 0.8 means the desired frequency
     *                    is at 0.8 PI radian frequency. 
     *    @param gain gains at various critical frequencies, value is between 
     *                0.0 ~ 1.0
     *    @param fs sampling frequency
     */
    public static RealAnalogFilter designRealIIR(int mapmethod, int appmethod,
            int filttype, double[] critfreq, double[] gain, double fs) {
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

        double fc;
        double wc;

        double filterGain;

        int order;

        // Epsilon factor in Chebyshev
        double epsilon;  
       
        RealAnalogFilter aFilter;  // filter to be designed
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

            if (_debug > 0) {
                //System.out.println("LOWPASS or bandstop stopb : "+stopef+
                // " stopg: "+stopg+" passb: "+passef+ "passg: "+passg);
            }
        } else if ((filttype == Filter.HIGHPASS) || 
                   (filttype == Filter.BANDPASS)){               
            dStopEdgeFreq = Math.PI-critfreq[0];
            dPassEdgeFreq = Math.PI-critfreq[1];
            stopEdgeGain = gain[0];
            passEdgeGain = gain[1];

            if (_debug > 0) {
                //System.out.println("highpass or bandpass stopb : "+stopef+
                //" stopg: "+stopg+" passb: "+passef+ "passg: "+passg);
            }
        } else {
            // Fixme, throw an exception here
            System.out.println("error, incorrect filter spec");
            return null;
        }

        // Prewarp the spec from digital frequency to analog
        if (mapmethod == Filter.BILINEAR){  
            // only bilinear is supported
            aPassEdgeFreq = _bilinearPreWarp(dPassEdgeFreq, fs); 
            aStopEdgeFreq = _bilinearPreWarp(dStopEdgeFreq, fs); 
        } else {
            // Fixme, throw an exception here
            System.out.println("Only Bilinear transform is supported"); 
            return null;
        }

        // design the LOWPASS prototype 
        if (appmethod == Filter.BUTTERWORTH){

            // design a analog lowpass unit fc butterworth prototype filter
            aFilter =
                   _designButterworth(passEdgeGain, stopEdgeGain, 
                           aPassEdgeFreq, aStopEdgeFreq); 

        } else if (appmethod == Filter.CHEBYSHEV1) { 

            epsilon = Math.sqrt(1.0/(passEdgeGain*passEdgeGain)-1);

            // design a analog lowpass unit fc Chebyshev prototype filter
            aFilter =
                   _designChebychev1(passEdgeGain, stopEdgeGain,
                           aPassEdgeFreq, aStopEdgeFreq, epsilon);
        } else if (appmethod == Filter.CHEBYSHEV2) {
            epsilon = Math.sqrt(1.0/(passEdgeGain*passEdgeGain)-1);
            aFilter = _designChebychev2(passEdgeGain, stopEdgeGain,
                    aPassEdgeFreq, aStopEdgeFreq,epsilon);
        }
        else { 
            System.out.println("Only Butterworth and Chebyshev approximations are supported"); 
            return null;
        }
            
        // transform prototype to desired filter type, then back digital domain
        if (filttype == Filter.LOWPASS){
                    
        // transfer from unit lowpass to spec lowpass
               _toLowPass(aFilter);
            
        } else if (filttype == Filter.HIGHPASS){
            
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
          
                //System.out.println("hiph pass analog cutoff: "+filter.analogfc); 
                // transfer from unit lowpass to spec highpass
                _toHighPass(aFilter);
                
            } else { 
                System.out.println("Only Bilinear is supported"); 
                return null;
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
           
                // transfer from unit lowpass to spec bandpass
                _toBandPass(aFilter);
                
            } else { 
                System.out.println("Only Bilinear is supported"); 
                return null;
            }

        } else if (filttype == Filter.BANDSTOP){

            if (mapmethod == Filter.BILINEAR){

                // analog second lowpass cutoff
                double fc2 = _bilinearPreWarp(critfreq[2], fs);

                // analog center is geometrical mean of two cutoff frequencies
                aFilter.analogFreqCenter = Math.sqrt(aFilter.analogfc*fc2);
              
                // analog width
                aFilter.analogFreqWidth = fc2 - aFilter.analogfc;
           
                // transfer from unit lowpass to spec bandstop 
                _toBandStop(aFilter);
                
            } else { 
                System.out.println("Only Bilinear is supported"); 
                return null;
            }

        } else {
           System.out.println("Incorrect filter spec"); 
           return null; 
        }

        return aFilter; 
    } 
    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Private methods should not have doc comments, they should
    // have regular C++ comments.
            
    /**
     *  Perform analog frequency transformation that transform the unit cutoff
     *  frequency lowpass filter to lowpass filter with cutoff frequency at 
     *  desired value.  This is done by replace the s in numerator/denominator 
     *  polynomials with s/fc (fc = analog cutoff freq).  So changed values of 
     *  numerator/denominator quadratic coeffients: 
     *
     *    a2 + a1*s + a0*s^2 -> a1 = a1/fc, a0 = a0/(fc*fc). 
     * 
     *  Result is still in the same RealAnalogFilter object.
     *  
     *  @param filter the target of the frequency transformation.
     */
     private static void _toLowPass (RealAnalogFilter filter) {
        // find out the number of iterations 
        int iternum = filter.getNumberOfFactors();
        RealSFactor[] theFactors = filter.getFactors();
    
        double[] squadnum;
        double[] squadden;
        double n0;
        double n1;
        double n2;
        double d0;
        double d1;
        double d2;

        for (int i=0; i<iternum; i++){
            squadnum = theFactors[i].getNumerator();
            squadden = theFactors[i].getDenominator();
            
            if (squadnum.length == 3) {
                
                // transform numerator coef 
                n2 = squadnum[2];
                n1 = squadnum[1]/filter.analogfc;
                n0 = squadnum[0]/(filter.analogfc*filter.analogfc);
            } else if (squadnum.length == 2) {
                n2 = squadnum[1];
                n1 = squadnum[0]/filter.analogfc;
                n0 = 0;
            } else {
                n2 = squadnum[0];
                n1 = 0;
                n0 = 0;
            }
            
            if (squadden.length == 3) {
                // transform denominator coef   
                d2 = squadden[2];
                d1 = squadden[1]/filter.analogfc;
                d0 = squadden[0]/(filter.analogfc*filter.analogfc);
            } else if (squadden.length == 2) {
                d2 = squadden[1];
                d1 = squadden[0]/filter.analogfc;
                d0 = 0;
            } else {
                d2 = squadden[0];
                d1 = 0;
                d0 = 0;
            }
                
                
            // update the changes
            theFactors[i].setNumerator(new double[] {n0, n1, n2});
            theFactors[i].setDenominator(new double[] {d0, d1, d2});
        }      
        
    }

      /**
     *  Perform analog frequency transformation that transform the unit cutoff
     *  frequency lowpass filter to highpass filter with cutoff frequency at 
     *  desired value.  This is done by replace the s in numerator/denominator 
     *  polynomials with fc/s (fc = analog cutoff freq).  After derivation, 
     *  the transform is 
     *
     *  from :                    n0 + n1*s + n2*s^2
     *              old biquad = --------------------
     *                            d0 + d1*s + d2*s^2
     *   
     *  to :                      n2*fc*fc + n1*fc*s + n0*s^2
     *              new biquad = ----------------------------
     *                            d2*fc*fc + d1*fc*s + d0*s^2
     * 
     *      
     *  Result is still in the same RealAnalogFilter object.
     *  
     *  @param filter the target of the frequency transformation.
     */
    private static void _toHighPass(RealAnalogFilter filter){ 

        // find out the number of iterations 
        int iternum = filter.getNumberOfFactors();
        RealSFactor[] theFactors = filter.getFactors();
        double[] squadnum;
        double[] squadden;
        
        double n0,n1,n2,d0,d1,d2;
        for (int i=0;i<iternum;i++){
            squadnum = theFactors[i].getNumerator();
            squadden = theFactors[i].getDenominator();
            
            if (squadnum.length == 3) {
                n0 = squadnum[2];
                n1 = squadnum[1]*filter.analogfc;
                n2 = squadnum[0]*filter.analogfc*filter.analogfc;
            }
            else if (squadnum.length == 2) {
                n0 = squadnum[1];
                n1 = squadnum[0]*filter.analogfc;
                n2 = 0;
            } else {
                n0 = squadnum[0];
                n1 = 0;
                n2 = 0;
            }

            if (squadden.length == 3) {
                d0 = squadden[2];
                d1 = squadden[1]*filter.analogfc;
                d2 = squadden[0]*filter.analogfc*filter.analogfc;
            }
            else if (squadden.length == 2) {
                d0 = squadden[1];
                d1 = squadden[0]*filter.analogfc;
                d2 = 0;
            } else {
                d0 = squadden[0];
                d1 = 0;
                d2 = 0;
            }

            theFactors[i].setNumerator(new double[] {n0, n1, n2});
            theFactors[i].setDenominator(new double[] {d0, d1, d2});
        }
    }

    
    /**
     *  Perform analog frequency transformation that transform the unit cutoff
     *  frequency lowpass filter to bandpass filter with cutoff frequency at 
     *  desired value.  This is done by replace the s in numerator/denominator 
     *  polynomials with (s^2+fo^2)/(B*s) (fo is center frequency, and B is 
     *  the bandwidth of the pass ripple.  After derivation the transform is 
     *  this:  
     *
     *  from :                    n0 + n1*s + n2*s^2
     *              old biquad = --------------------
     *                            d0 + d1*s + d2*s^2
     *   
     *  to :
     *
     *    n2*fo^4 + n1*B*fo^2*s + (n0*B^2 + 2*n2*fo^2)*s^2 + n1*B*s^3 + n2*s^4 
     *new=--------------------------------------------------------------------
     *    d2*fo^4 + d1*B*fo^2*s + (d0*B^2 + 2*d2*fo^2)*s^2 + d1*B*s^3 + d2*s^4 
     *
     *  The new fraction is factored into two biquad and added to the 
     *  RealAnalogFilter. Notice the order of filter is doubled, 
     *  since there are two slopes. 
     *      
     *  Result is still in the same RealAnalogFilter object.
     *  
     *  @param filter the target of the frequency transformation.
     */
    private static void _toBandPass(RealAnalogFilter filter){ 
        
        
        // find out the number of iterations 
        int iternum = filter.getNumberOfFactors();
        
        // cache the old numerator/denominator
        RealSFactor[] oldFactors = filter.getFactors();
        
        // new numerator/denominator 
        RealSFactor[] newFactors = new RealSFactor[oldFactors.length*2];

        for (int i = 0; i < newFactors.length; i++) {
            newFactors[i] = new RealSFactor();
        }
        
        // coefficents for transformed numer/denom polynomials 
        double nn0, nn1, nn2, nn3, nn4;
        double nd0, nd1, nd2, nd3, nd4;
        
        double fc = filter.analogFreqCenter;   // center frequency
        double wid = filter.analogFreqWidth; // ripple band width
        
        double[] oldnumer;
        double[] olddenom;
        double gain;
        
        for (int ind = 0; ind < iternum; ind++){
            oldnumer = oldFactors[ind].getNumerator();
            olddenom = oldFactors[ind].getDenominator();
            gain = oldFactors[ind].getGain();
            
            // get old coefficients
            double d0, n0, d1, n1, d2, n2;
            
            if (olddenom.length == 3) {
                d0 = olddenom[2];
                d1 = olddenom[1]; 
                d2 = olddenom[0]; 
            } else if (olddenom.length == 2) {
                d0 = olddenom[1];
                d1 = olddenom[0];
                d2 = 0;
            } else {
                d0 = olddenom[0];
                d1 = 0;
                d2 = 0;
            }
            
            if (oldnumer.length == 3) {
                n0 = oldnumer[2]*gain; 
                n1 = oldnumer[1]*gain; 
                n2 = oldnumer[0]*gain; 
            } else if (oldnumer.length == 2) {
                n0 = oldnumer[1]*gain;
                n1 = oldnumer[0]*gain;
                n2 = 0;
            } else {
                n0 = oldnumer[0]*gain;
                n1 = 0;
                n2 = 0;
            }
            
             // compute the transformed numerator coefficients
             nd0 = d2*fc*fc*fc*fc;
             nd1 = fc*fc*wid*d1;
             nd2 = 2*d2*fc*fc+d0*wid*wid;
             nd3 = d1*wid;
             nd4 = d2;
             
             // compute the transformed denominator coefficients
             nn0 = n2*fc*fc*fc*fc;
             nn1 = fc*fc*wid*n1;
             nn2 = 2*n2*fc*fc+n0*wid*wid;
             nn3 = n1*wid;
             nn4 = n2;

             // form in arrays of coeffients used by factoring function
             double pnumer [] = {nn4,nn3,nn2,nn1,nn0};
             double pdenom [] = {nd4,nd3,nd2,nd1,nd0};
             double quad1 [] = new double[3]; 
             double quad2 [] = new double[3]; 

             // factor the numerator into two quadratic polynomial
             MathWizard.factor2quadratic(pnumer, quad1, quad2);

             // Store the first quadratic in numerator array 
             newFactors[ind].setNumerator(quad1);
             
             // Store the second quadratic in numerator array 
             newFactors[ind+iternum].setNumerator(quad2);
             
             // factor the denominator into two quadratic polynomial
             MathWizard.factor2quadratic(pdenom, quad1, quad2);

             // Store the first quadratic in denominator array 
             newFactors[ind].setDenominator(quad1);
             
             // Store the second quadratic in denominator array 
             newFactors[ind+iternum].setDenominator(quad2);
             
        }
        
        // delete the old factors
        filter.clearFactors();
             
        // put the new factors into the filter
        for (int i = 0; i < newFactors.length; i++) {
            filter.addFactor(newFactors[i]);
        }
        
        // double the order, since there are two edges
        filter.order = filter.order*2;      
    }

     /**
     *  Perform analog frequency transformation that transform the unit cutoff
     *  frequency lowpass filter to bandstop filter with cutoff frequency at 
     *  desired 
     *  value.  This is done by replace the s in numerator/denominator 
     *  polynomials with (B*s)/(s^2+fo^2) (fo is center frequency, and B is 
     *  the bandwidth of the pass ripple.  After derivation the transform is 
     *  this:  
     *
     *  from :                    n0 + n1*s + n2*s^2
     *              old biquad = --------------------
     *                            d0 + d1*s + d2*s^2
     *   
     *  to :
     *
     *     n0*fo^4 + n1*B*fo^2*s + (n2*B^2 + 2*n0*fo^2)*s^2 + n1*B*s^3 + n0*s^4 
     * new=--------------------------------------------------------------------
     *     d0*fo^4 + d1*B*fo^2*s + (d2*B^2 + 2*d0*fo^2)*s^2 + d1*B*s^3 + d0*s^4 
     *
     *  The new fraction is factored into two biquad and added to the
     *  RealAnalogFilter.  Notice the order of filter is doubled, since there 
     *  are two slopes. 
     *  
     *  Result is still in the same RealAnalogFilter object.
     *  
     *  @param filter the target of the frequency transformation.
     */
    private static void _toBandStop(RealAnalogFilter filter){ 

        // find out the number of iterations 
        int iternum = filter.getNumberOfFactors();

        // cache the old numerator/denominator
        RealSFactor[] oldFactors = filter.getFactors();

        // new numerator/denominator 
        RealSFactor[] newFactors = new RealSFactor[2*oldFactors.length];
        
        for (int i = 0; i < newFactors.length; i++) {
            newFactors[i] = new RealSFactor();
        }

        double nn0, nn1, nn2, nn3, nn4;
        double nd0, nd1, nd2, nd3, nd4;

        double fc = filter.analogFreqCenter;   // center frequency
        double wid = filter.analogFreqWidth; // ripple band width

        double[] oldnumer;
        double[] olddenom;
        double gain;
        
        for (int i=0;i<iternum;i++){
            oldnumer = oldFactors[i].getNumerator();
            olddenom = oldFactors[i].getDenominator();
            gain = oldFactors[i].getGain();
            
            // get old coefficients
            double d0, n0, d1, n1, d2, n2;
            if (olddenom.length == 3) {
                d0 = olddenom[2]; 
                d1 = olddenom[1]; 
                d2 = olddenom[0]; 
            } else if (olddenom.length == 2) {
                d0 = olddenom[1];
                d1 = olddenom[0];
                d2 = 0;
            } else {
                d0 = olddenom[0];
                d1 = 0;
                d2 = 0;
            }

            if (oldnumer.length == 3) {
                n0 = oldnumer[2]*gain; 
                n1 = oldnumer[1]*gain; 
                n2 = oldnumer[0]*gain; 
            } else if (oldnumer.length == 2) {
                n0 = oldnumer[1]*gain;
                n1 = oldnumer[0]*gain;
                n2 = 0;
            } else {
                n0 = oldnumer[0]*gain;
                n1 = 0;
                n2 = 0;
            }
           
            // compute the new numerator polynomial coefficients
            nd0 = d0*fc*fc*fc*fc;
            nd1 = fc*fc*wid*d1;
            nd2 = 2*d0*fc*fc+d2*wid*wid;
            nd3 = d1*wid;
            nd4 = d0;
             
            // compute the new denominator polynomial coefficients
            nn0 = n0*fc*fc*fc*fc;
            nn1 = fc*fc*wid*n1;
            nn2 = 2*n0*fc*fc+n2*wid*wid;
            nn3 = n1*wid;
            nn4 = n0;

            // form in arrays of coeffients used by factoring function
            double pnumer [] = {nn4,nn3,nn2,nn1,nn0};
            double pdenom [] = {nd4,nd3,nd2,nd1,nd0};
            double quad1 [] = new double[3];
            double quad2 [] = new double[3];
            
            // factor the numerator into two quadratic polynomial
            MathWizard.factor2quadratic(pnumer, quad1, quad2);
 
            // Store the first quadratic in numerator array
            newFactors[i].setNumerator(quad1);
 
            // Store the second quadratic in numerator array
            newFactors[i+iternum].setNumerator(quad2);
 
            // factor the denominator into two quadratic polynomial
            MathWizard.factor2quadratic(pdenom, quad1, quad2);
 
            // Store the first quadratic in denominator array
            newFactors[i].setDenominator(quad1);
 
            // Store the second quadratic in denominator array
            newFactors[i+iternum].setDenominator(quad2);
        }
        
        // clear the old factors
        filter.clearFactors();
             
        // put the new factors into the filter
        for(int j = 0; j < newFactors.length; j++) {
            filter.addFactor(newFactors[j]);
        }
        
        // double the order, since there are two edges
        filter.order = filter.order*2;      
    }

    
   

   /**
     * Design the pototype filter ( unit cutoff, lowpass) using Butterworth
     * technique, and calculate the lowpass cutoff frequency.   The order 
     * is determined by the two points specified by (passef, passg) and 
     * (stopef, stopg).  
     * Since order determines the number of poles and their locations on the 
     * s-plane, thus the denominator is found.  Numerator is just equal to 1.0.
     * Finally the cutoff freqency is computed using Butterworh equation.  
     * 
     * @return a RealAnalogFilter to be designed
     * @param passg analog pass edge gain 
     * @param stopg analog stop edge gain 
     * @param passef analog pass edge freq 
     * @param stopef analog stop edge freq 
     */
    private static RealAnalogFilter _designButterworth(double passg, 
            double stopg, double passef, double stopef) { 
        
          // use the equation 7.33 on P.420 of Oppenheim & Schafer 
          //
          //                   1
          // |H(jf)|^2 = -------------
          //                1+(f/fc)^2N
          // 
          // where N is the order. fc is the cutoff frequency (at -3db), 
          // f is frequency then we know 
          //
          //                 1 / H(jf1)^2 - 1
          //                 ---------------- 
          //                 1 / H(jf2)^2 - 1
          //        N = log(---------------------)
          //                   2*log(f1/f2)    
          //
          // f1 and f2 are frequencies, and H(jf1), H(jf2) are
          // gains at these frequencies. 
          // which is similiar to equation 7.35 
          //  
          RealAnalogFilter designedFilter = new RealAnalogFilter();
          double num = Math.log(((1/passg)*(1/passg)-1)/
                                ((1/stopg)*(1/stopg)-1));
          //System.out.println("Butterworth stopef " + stopef+ " passef "+passe          //f);
          double den = 2*Math.log(passef/stopef);
          double o = num/den;
         
          if (_debug > 0){
              System.out.println("order "+o);  
          }

          // round the order to the nearest int
          int order = (int) Math.round(o);
          // if (order%2!=0) order++;
          
          designedFilter.order = order;

          // offset is the angluar difference from PI for each pair 
          // complex conjuage poles  
          double offset;

          // now find the poles for the prototype, 
          // then transfer them to quad formation
          // set the cutoff frequency at 1.0, // since this is the prototype.
          //
          // depend on if the order of the filter is even or odd, the 
          // arrangement of the poles will be different.  While odd order
          // will give a pole at real axis at -1.0, while even order don't. 
          // poles in analog Butterworth filter's square will form a circle 
          // around orgin with radius equal to cutoff frequency, they are 
          // at an angle PI/order apart from each other. The poles on the left
          // side (negative) of real plane will form the poles of the filter.
          // Extract these poles and form them into denominator in quadatric
          // clusters.
 
          int quadterm;             // number of quadratic term
          double [] coeff;          // coefficients of quadratic equation 
          if (order%2==0){          // even
              offset = Math.PI/(2*(double)order);  
              quadterm = designedFilter.order/2;
          } else {                  // odd
              offset = 0;
              quadterm = order/2+1;
          }

          // allocate the space for quadratic terms
          RealSFactor[] newFactors = new RealSFactor[quadterm];
          //System.out.println("quadterm = " + quadterm);
          //System.out.println("newFactors Length = " + newFactors.length);
          
          for (int i = 0; i < newFactors.length; i++) {
              newFactors[i] = new RealSFactor();

          }
          
          double [] numercoeff = {0.0, 0.0, 1.0};
          
          for (int ind=0;ind<quadterm;ind++){
              
              Complex pole = new Complex(Math.cos(Math.PI-offset), 
                                         Math.sin(Math.PI-offset)); 
              System.out.println("SPole "+pole.real+" "+pole.imag);
              offset += Math.PI/((double)order);  
              // numerator equal to 1.0 for all quad terms 
                                                 
              newFactors[ind].setNumerator(numercoeff);
              //System.out.println("ooo");  

              // if pole's imaginary equal to zero then it's the single pole
              // at -1.0+j0.0 
              if ((pole.imag > -0.00001) && (pole.imag < 0.00001)){
                  double [] denomcoeff = {0.0, 1.0, 1.0};
                  newFactors[ind].setDenominator(denomcoeff);     
              } else {
                  // conjugate pair of complex poles form zeroes for this term
                  double [] denomcoeff = 
                     { 1.0, (-2)*(pole.real), pole.magSquared()};
                  System.out.println("denominator: "+ pole.magSquared()+" "+(-2)*(pole.real)+" 1.0");
                  newFactors[ind].setDenominator(denomcoeff);     
              }
           
          }
          //System.out.println("here");
          
          designedFilter.clearFactors();
         
          for (int i = 0; i < newFactors.length; i++) {
              designedFilter.addFactor(newFactors[i]);
          }

          // now find the analog cutoff frequency using the same equation 7.33
          // and solve for cutoff, 
          //
          //                     f
          // fc = -------------------------------
          //               1             1
          //         ( --------- - 1)^(-----)
          //            H(jf)^2         2*N 
          //
          //
          //System.out.println("stopg "+stopg+" stopef"+stopef);
 
          double a = Math.pow((1/stopg)*(1/stopg)-1, 1/((double) 2*order));
          double b = stopef;
          designedFilter.analogfc = b/a;
          System.out.println("analog cutoff "+designedFilter.analogfc);
          return designedFilter;
    }
    
    
    /**
     * Design the pototype filter ( unit cutoff, lowpass) using Chebyshev 
     * technique, and calculate the lowpass cutoff frequency.   The order 
     * is determined by the two points specified by (passef, passg) and (stopef,
     * stopg).  Then use the order and Chebyshev equation to enerate the 
     * polynomials.  With the order found, the poles can be found using the 
     * knowledge that thers are 2*order of poles lie in a ellipse in s-plane.  
     * Results are then stored in RealAnalogFilter object's factors in 
     * quadratic form.  Finally the cutoff frequency is found using 
     * Chebyshev equation.
     *  
     * @return a RealAnalogFilter to be designed
     * @param passg analog pass edge gain 
     * @param stopg analog stop edge gain 
     * @param passef analog pass edge freq 
     * @param stopef analog stop edge freq 
     * @param epsilon ripple amplitude 
     */

     /** Digital Filters and Signal Processing, Third Edition, by Leland B. 
        Jackson
        */
    
    private static RealAnalogFilter _designChebychev1(
            double cutoffGain, double stopGain, double cutoffFreq, 
            double stopFreq, 
            double epsilon) {

        double order = ExtendedMath.acosh(1/(stopGain*epsilon))/
            ExtendedMath.acosh(stopFreq/cutoffFreq);
        order = Math.ceil(order);
        double gamma = Math.pow(((1+Math.sqrt(1+Math.pow(epsilon,2)))/
                epsilon),1/order);
        double sinhPhi = (gamma - Math.pow(gamma, -1))/2;
        double coshPhi = (gamma + Math.pow(gamma, -1))/2;
        
        RealAnalogFilter designedFilter = new RealAnalogFilter();
        designedFilter.order = (int)order;
        designedFilter.analogfc = cutoffFreq;
        
        double mu;
        double sigma;
        double omega;
        double alpha;
        double beta;
        designedFilter.clearFactors();
        int N = (int)Math.ceil(order/2);
        double factor = 1;
        double normalizer = Math.sqrt(1.0/(1.0+epsilon*epsilon));
        boolean odd = false;
        
        double scale = cutoffFreq*stopFreq;
        double reverse = stopFreq/cutoffFreq;
        
        if (order%2 != 0) {
            mu = (2*N-1)*Math.PI/(2*order);
            sigma = -(sinhPhi*Math.sin(mu));
            omega = (coshPhi*Math.cos(mu));
            Complex pole = new Complex(sigma, 0);
            Complex zero = new Complex(Double.POSITIVE_INFINITY);
            
            factor = pole.mag();
            designedFilter.addPoleZero(pole, zero, factor, false);
            odd = true;
        }
        
        if (N > 0 && odd) {
            N = N - 1;
        }
        
        if (N >= 1) {
            for (int i = 1; i <= N; i++) {
                mu = (2*i-1)*Math.PI/(2*order);
                sigma = -(sinhPhi*Math.sin(mu));
                omega = (coshPhi*Math.cos(mu));
                Complex pole = new Complex(sigma,omega);
                Complex zero = new Complex(Double.POSITIVE_INFINITY);
                
                factor = pole.magSquared();
                designedFilter.addPoleZero(pole, zero, factor, true);
            }
        }
        
        if (odd == false ) {
            designedFilter.addFactor(new RealSFactor(new double[] {0,0,1},
            new double[] {0,0,1}, normalizer));
        } else {
            designedFilter.addFactor(new RealSFactor(new double[] {0,0,1},
            new double[] {0,0,1}, -1));
        }
        
        return designedFilter;
    }
   

    /** Digital Filters and Signal Processing, Third Edition, by Leland B. 
        Jackson
        */
    
    private static RealAnalogFilter _designChebychev2(
            double cutoffGain, double stopGain, double cutoffFreq, 
            double stopFreq, 
            double epsilon) {
        
        double order = ExtendedMath.acosh(1/(stopGain*epsilon))/
            ExtendedMath.acosh(stopFreq/cutoffFreq);
        double lambda = 1/stopGain;
        order = Math.ceil(order);
        double gamma = Math.pow((lambda+Math.sqrt(Math.pow(lambda,2)-1)),
                1/order);
        double sinhPhi = (gamma - Math.pow(gamma, -1))/2;
        double coshPhi = (gamma + Math.pow(gamma, -1))/2;
        
        RealAnalogFilter designedFilter = new RealAnalogFilter();
        designedFilter.order = (int)order;
        designedFilter.analogfc = stopFreq;
        
        double mu;
        double sigma=1;
        double omega=1;
        double alpha;
        double beta;
        designedFilter.clearFactors();
        int N = (int)Math.ceil(order/2);
        double factor;
        boolean odd = false;
        
        if (order%2 != 0) {
            mu = (2*N-1)*Math.PI/(2*order);
            sigma = -(sinhPhi*Math.sin(mu));
            omega = (coshPhi*Math.cos(mu));
            alpha = (sigma)/(sigma*sigma+omega*omega);
            Complex pole = new Complex(alpha, 0);
            Complex zero = new Complex(Double.POSITIVE_INFINITY);
            
            factor = pole.mag();
            designedFilter.addPoleZero(pole, zero, factor, false);
            odd = true;
        }
        
        if (N > 0 && odd) {
            N = N - 1;
        }
        
        if (N >= 1) {
            for (int i = 1; i <= N; i++) {
                mu = (2*i-1)*Math.PI/(2*order);
                sigma = -(sinhPhi*Math.sin(mu));
                omega = (coshPhi*Math.cos(mu));
                alpha = (sigma)/(sigma*sigma+omega*omega);
                beta = -(omega)/(sigma*sigma+omega*omega);
                Complex pole = new Complex(alpha, beta);
                Complex zero = new Complex(0, 1/Math.cos(mu));
                
                factor = pole.magSquared()/zero.magSquared();
                designedFilter.addPoleZero(pole, zero, factor, true);
            }
        }
        
        return designedFilter;
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
    ////                         private variables                 ////

    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    private static final double _log10scale = 1/Math.log(10);
    private static final double _log2scale = 1/Math.log(2);
    private static final double TINY=1.0e-6;

    private static final int _debug = 1;
}



