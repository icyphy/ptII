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
method to designing an IIR filter with Butterworth and Chebychev.  

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
     *  Given the spec(pass band freq and gain, stop band freq and gain), and 
     *  characteristic (Butterworth, Chebyshev, etc) of the filter, an analog
     *  prototype (lowpass, unit cutoff) is designed.  Along with the 
     *  analog cutoff freq of the prototype lowpass.  The prototype is 
     *  a RealAnalogFilter.
     *  
     *  The filter object is returned back.  If specifictions is not yet 
     *  supported, or illeagl, IllegalArgumentException will thrown.
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
     *   
     */
    public static RealAnalogFilter designRealLowpassIIR(int mapmethod, 
            int appmethod, int filttype, double aPassEdgeFreq, 
            double aStopEdgeFreq, double passEdgeGain, double stopEdgeGain) 
            throws IllegalArgumentException {
                               
        // Epsilon factor in Chebyshev
        double epsilon;  
       
        RealAnalogFilter aFilter;  // filter to be designed

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
            throw new IllegalArgumentException(
                    "Only Butterworth and Chebyshev approximations are supported"); 
        }
        
        return aFilter;
    }

    /* Takes a RealAnalogFilter that lowpass and unit cutoff and transform it
     * into the desired filter type.  The parameters inside RealAnalogFilter,
     * analogfc, analogFreqCenter, analogFreqWidth needs to changed one's 
     * desired value for the filter type.
     * @param aFilter a RealAnalogFilter(lowpass) to be transformed
     * @param filttype the desired filter type
     */
    public static void freqTransformation(RealAnalogFilter aFilter, 
            int filttype) {
                
        // transform prototype to desired filter type, then back digital domain
        if (filttype == Filter.LOWPASS){
                    
            // transfer from unit lowpass to spec lowpass
            _toLowPass(aFilter);
            
        } else if (filttype == Filter.HIGHPASS){
        
            // transfer from unit lowpass to spec highpass
            _toHighPass(aFilter);
            
        } else if (filttype == Filter.BANDPASS){
        
            // transfer from unit lowpass to spec bandpass
            _toBandPass(aFilter);
        
        } else if (filttype == Filter.BANDSTOP){

            // transfer from unit lowpass to spec bandstop 
            _toBandStop(aFilter);
            
        } else {
            throw new IllegalArgumentException("Filter type not supported"); 
        }
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

            // update the changes
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
     *   n0*fo^4 + n1*B*fo^2*s + (n2*B^2 + 2*n0*fo^2)*s^2 + n1*B*s^3 + n0*s^4 
     *  --------------------------------------------------------------------
     *   d0*fo^4 + d1*B*fo^2*s + (d2*B^2 + 2*d0*fo^2)*s^2 + d1*B*s^3 + d0*s^4 
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
          
          double den = 2*Math.log(passef/stopef);
          double o = num/den;
         
          // round the order to the nearest int
          int order = (int) Math.round(o);
                    
          
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
              quadterm = order/2;
          } else {                  // odd
              offset = 0;
              quadterm = order/2+1;
          }

          // allocate the space for quadratic terms
          RealSFactor[] newFactors = new RealSFactor[quadterm];
                    
          for (int i = 0; i < newFactors.length; i++) {
              newFactors[i] = new RealSFactor();

          }
          
          double [] numercoeff = {0.0, 0.0, 1.0};
          
          for (int ind=0;ind<quadterm;ind++){
              
              Complex pole = new Complex(Math.cos(Math.PI-offset), 
                                         Math.sin(Math.PI-offset)); 
              offset += Math.PI/((double)order);  
              // numerator equal to 1.0 for all quad terms 
                                                 
              newFactors[ind].setNumerator(numercoeff);
              
              // if pole's imaginary equal to zero then it's the single pole
              // at -1.0+j0.0 
              if ((pole.imag > -0.00001) && (pole.imag < 0.00001)){
                  double [] denomcoeff = {0.0, 1.0, 1.0};
                  newFactors[ind].setDenominator(denomcoeff);     
              } else {
                  // conjugate pair of complex poles form zeroes for this term
                  double [] denomcoeff = 
                     { 1.0, (-2)*(pole.real), pole.magSquared()};
                     newFactors[ind].setDenominator(denomcoeff);     
              }
           
          }
          
          designedFilter.clearFactors();
         
          // update the designed filter
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
          double a = Math.pow((1/stopg)*(1/stopg)-1, 1/((double) 2*order));
          double b = stopef;
          designedFilter.analogfc = b/a;
          return designedFilter;
    }
    
    
    /**
     * Design the pototype filter ( unit cutoff, lowpass) using Chebyshev 
     * technique.  With the order found, the poles can be found using the 
     * knowledge that thers are 2*order of poles lie in a ellipse in s-plane.  
     * Results are then stored in RealAnalogFilter object's factors in 
     * quadratic form.  
     * 
     * Signal Processing Algorithms, Samuel D. Stearns, Ruth A. David
     * Section 7.4, Chebyshev Filter Design
     *  
     * @return a RealAnalogFilter to be designed
     * @param cutoffGain analog pass edge gain 
     * @param stopGain analog stop edge gain 
     * @param cutoffFreq analog pass edge freq 
     * @param stopFreq analog stop edge freq 
     * @param epsilon ripple amplitude 
     */
    private static RealAnalogFilter _designChebychev1(
            double cutoffGain, double stopGain, double cutoffFreq, 
            double stopFreq, 
            double epsilon) {

        // calculate the order
        double order = ExtendedMath.acosh(1/(stopGain*epsilon))/
            ExtendedMath.acosh(stopFreq/cutoffFreq);
        order = Math.ceil(order);
        
        // calculate the constants
        double gamma = Math.pow(((1+Math.sqrt(1+Math.pow(epsilon,2)))/
                epsilon),1/order);
        double sinhPhi = (gamma - Math.pow(gamma, -1))/2;
        double coshPhi = (gamma + Math.pow(gamma, -1))/2;
        
        RealAnalogFilter designedFilter = new RealAnalogFilter();
        designedFilter.analogfc = cutoffFreq;
        
        double mu;
        double sigma;
        double omega;
        double alpha;
        double beta;
        designedFilter.clearFactors();
        
        // counter used for adding poles and zeroes
        int N = (int)Math.ceil(order/2);
        
        // scaling factor for the poles and zeroes, so the transfer function
        // is unit gain
        double factor = 1;
        
        // scaling factor for the even order filter
        double normalizer = Math.sqrt(1.0/(1.0+epsilon*epsilon));
        boolean odd = false;
        
        // add poles and zeroes into the filter to be designed
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
                
                // since the poles and zeroes are in conjugate pairs
                // their magnitude is the squared magnitude of a single
                // pole.
                factor = pole.magSquared();
                
                designedFilter.addPoleZero(pole, zero, factor, true);
            }
        }
        
        if (odd == false ) {
            designedFilter.addFactor(new RealSFactor(new double[] {0,0,1},
            new double[] {0,0,1}, normalizer));
        } 
                
        return designedFilter;
    }
   

    /**
     * Design the pototype filter ( unit cutoff, lowpass) using Chebyshev 
     * technique.  The poles of this filter is simply scaled version of 
     * chebyshev I filter.  The zeroes of this filter lies along the imaginary
     * axis.  Results are then stored in RealAnalogFilter object's 
     * factors in quadratic form.  
     * 
     * Signal Processing Algorithms, Samuel D. Stearns, Ruth A. David
     * Section 7.4, Chebyshev Filter Design
     *  
     * @return a RealAnalogFilter to be designed
     * @param cutoffGain analog pass edge gain 
     * @param stopGain analog stop edge gain 
     * @param cutoffFreq analog pass edge freq 
     * @param stopFreq analog stop edge freq 
     * @param epsilon ripple amplitude 
     */
    private static RealAnalogFilter _designChebychev2(
            double cutoffGain, double stopGain, double cutoffFreq, 
            double stopFreq, 
            double epsilon) {
        
        // calculate the order
        double order = ExtendedMath.acosh(1/(stopGain*epsilon))/
            ExtendedMath.acosh(stopFreq/cutoffFreq);
        double lambda = 1/stopGain;
        order = Math.ceil(order);
        
        // calculate the constants
        double gamma = Math.pow((lambda+Math.sqrt(Math.pow(lambda,2)-1)),
                1/order);
        double sinhPhi = (gamma - Math.pow(gamma, -1))/2;
        double coshPhi = (gamma + Math.pow(gamma, -1))/2;
        
        RealAnalogFilter designedFilter = new RealAnalogFilter();
        designedFilter.analogfc = stopFreq;
        
        double mu;
        double sigma=1;
        double omega=1;
        double alpha;
        double beta;
        designedFilter.clearFactors();
        
        // counter used for adding poles and zeroes
        int N = (int)Math.ceil(order/2);
        
        // scaling factor for the poles and zeroes, so the transfer function
        // is unit gain
        double factor;

        boolean odd = false;
        
        // add the poles and zeroes into the filter
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
                
                // since the poles and zeroes are in conjugate pairs
                // their magnitude is the squared magnitude of the single
                // pole and single zero.
                factor = pole.magSquared()/zero.magSquared();
                designedFilter.addPoleZero(pole, zero, factor, true);
            }
        }
        
        return designedFilter;
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



