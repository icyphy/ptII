/* Filter model object in the model/view design pattern 

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

*/

package ptolemy.filter.filtermodel;

import java.util.*;
import collections.*;
import ptolemy.math.*;
import ptolemy.math.filter.*;

//////////////////////////////////////////////////////////////////////////
//// FilterObj 
/** 
  Filter model in the model/view design pattern.  It is uses the existing
  object oriented model/view paradigm in java by be derived from
  Observable class in java.util.  Views are the PoleZeroView, FreqView,
  etc, in the ptolemy.filter.view package.  Views can request info
  about the filter characteristics by calling the various <i> get </i>
  methods in the class.  View can also send changed data or new data
  back to filter object by calling the variouse <i> update </i> methods.  
  <p>
  When a the filter object is changed, (like after redesigning with
  new spec), all the View objects will get notified by <code> notifyObservers()
  </code>.  Then View know what to query on the Filter object. 
         
  This object stores various design parameters that will be used
  to design digital filter.  These parameters include:<p>
  - type of digital filter: IIR, FIR, etc <p>
  for IIR: <p>
  - approximation methods: Butterworth, Chebyshev, Elliptical <p>
  - A/D transformation method: Impulse invariant, Bilinear, Mathch-Z <p>
  - band type: low pass, high pass, band stop, band pass. <p>
  - edge frequencies and gain. <p>
  - ripple heights can be represent by gain. <p>
  <p> 
  FilterObj also uses class DigitalFilter (either ReadDigitalFilter or
  ComplexDigitalFilter) to store different sets of data that represents 
  the designed filter.  Data like pole/zero sets, frequency response, 
  impulse reponse, numerator/denominator, etc.   
  <p> 
  
  @author: William Wu
  @version: %W% %G%
  @date: 3/2/98
 */

public class FilterObj extends Observable {


    /**
     * Constructor
     */ 
    public FilterObj(){
        super();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Initialize the filter with a name and type.
     * Manager calls this function to set up the basic parameter 
     * like name, and type.  For all accepted type refer to 
     * the enum on filter type in ptolemy.math.Filter.
     * Type BLANK will create a ComplexDigitalFilter, while all others
     * will create a RealDigitalFilter. <p>
     * @param name name of the filter
     * @param type type of the filter 
     */ 
    public void init(String name, int type){
         this._type = type;
         _fname = new String(name);
         if (_type == Filter.BLANK){
           //  _filter = new ComplexDigitalFilter();
             _filter = new RealDigitalFilter();
         } else {
             _filter = new RealDigitalFilter();
         }
    }

    /**
     * Returns the name of the filter.
     * @return name of the filter
     */
    public String getName(){
        return _fname;
    }

    /**
     * Return the type of the filter
     * @return type of the filter
     */
    public int getType(){
        return _type;
    }

    public int getStep(){
        if (_filter == null) return 0;
        return _filter.getFreqStep();
    }

    public Complex [] getFamilyPoleWithPole(Complex pole){
        if (_type != Filter.BLANK){
           return ((RealDigitalFilter)_filter).getFactorWithPole(pole).getPoles();
        } else {
           return null;
        } 
        
    }

    public Complex [] getFamilyPoleWithZero(Complex zero){
        if (_type != Filter.BLANK){
           return ((RealDigitalFilter)_filter).getFactorWithZero(zero).getPoles();
        } else {
           return null;
        } 
    }

    public Complex [] getFamilyZeroWithPole(Complex pole){
        if (_type != Filter.BLANK){
           return ((RealDigitalFilter)_filter).getFactorWithPole(pole).getZeroes();
        } else {
           return null;
        } 
    }

    public Complex [] getFamilyZeroWithZero(Complex zero){
        if (_type != Filter.BLANK){
           return ((RealDigitalFilter)_filter).getFactorWithZero(zero).getZeroes();
        } else {
           return null;
        } 
    }


    /**
     * Returns the critical frequency values.
     * For lowpass/highpass filter, there are two critical frequencies that 
     * define the width of transiton band.  While for bandpass/bandstop, 
     * there are three, the first two describe the transition band, while
     * the second and third one describes the width of passband (bandpass 
     * filter), or stopband (bandstop filter).  <p>
     * @return critical frequencies   
     */
    public double [] getFreqBand(){
        return _band;
    }
  
    /**
     * Returns the gains at critical frequencies.
     * For bandpass/bandstop there is no gain value at the third critical
     * frequency.
     * @return the gains at critical frequencies.
     */
    public double [] getFreqGain(){
        return _gain;
    }

    /**
     * Returns the complex gain of the filter.  It is used for complex
     * digital filter.  For real filter use <code> getRealGain() </code>. 
     * @return complex gain of the filter. 
     */
    public Complex getComplexGain(){
        if (_type == Filter.BLANK){ 
         //   return ((ComplexDigitalFilter) _filter).getGain();
          return null;
        } else {
            // throw an exception here..
            return null;
        }
    }

    /**
     * Returns the real gain of the filter.  It is used for real  
     * digital filter.  For complex filter use <code> getComplexGain() </code>. 
     * @return real gain of the filter. 
     */
    public double getRealGain(){
        if (_type != Filter.BLANK){ 
            return ((RealDigitalFilter) _filter).getGain();
        } else {
            // throw an exception here..
            return -1;
        }
    }


    /**
     * Returns the poles in z-plane.
     * @return poles on z-plane  
     */
    public Complex [] getPole(){
        return _filter.getPoles();
    }

    /**
     * Returns the zeros in z-plane 
     * @return zeroes on z-plane  
     */
    public Complex [] getZero(){
        return _filter.getZeroes();
    }

    /**
     * Returns the frequency response data. 
     * @return frequency response data.  
     */
    public Complex [] getFrequency(){
        return _filter.getFrequencyResponse();
    }

    /**
     * Returns the complex impulse response data.  If the 
     * filter is a complex filter, then this is method is used.
     * For real filter use <code> getRealImpulse() </code>.
     * @return complex impulse response data.
     */
    public Complex [] getComplexImpulse(){
        if (_type == Filter.BLANK){ 
       //     return ((ComplexDigitalFilter)_filter).getImpulseResponse();
            return null;
        } else {
            return null;
        }
    }
  
    /**
     * Returns the real impulse response data.  If the 
     * filter is a real filter, then this is method is used.
     * For complex filter use <code> getComplexImpulse() </code>.
     * @return real impulse response data.
     */
    public double [] getRealImpulse(){
        if (_type != Filter.BLANK){ 
            return ((RealDigitalFilter)_filter).getImpulseResponse();
        } else {
            return null;
        }
    }


    /**
     * Returns the complex filter'e numerator.
     * Real filter uses <code> getRealNumerator() </code>.
     * @return complex filter numerator 
     */
    public Complex [] getComplexNumerator(){
        if (_type == Filter.BLANK){ 
       //     return ((ComplexDigitalFilter)_filter).getNumerator();
            return null;
        } else {
            return null;
        }
    }
  
    /**
     * Returns the real filter'e numerator.
     * Complex filter uses <code> getComplexNumerator() </code>.
     * @return real filter numerator 
     */
    public double [] getRealNumerator(){
        if (_type != Filter.BLANK){ 
            return ((RealDigitalFilter)_filter).getNumerator();
        } else {
            return null;
        }
    }


    /**
     * Returns the complex filter's denominator. 
     * Real filter uses <code> getRealDenominator() </code>.
     * @return complex filter denominator 
     */
    public Complex [] getComplexDenominator(){
        if (_type == Filter.BLANK){ 
       //     return ((ComplexDigitalFilter)_filter).getDenominator();
            return null;
        } else {
            return null;
        }
    }

    /**
     * Returns the real filter's denominator. 
     * Real filter uses <code> getComplexDenominator() </code>.
     * @return real filter denominator 
     */
    public double [] getRealDenominator(){
        if (_type != Filter.BLANK){ 
            return ((RealDigitalFilter)_filter).getDenominator();
        } else {
            return null;
        }
    }

    /**
     * Returns the IIR parameters as a Vector.  If the current type
     * is not IIR then null is returned.  The Vector is formed like this: 
     * <p> first element: Integer enum for approximation method. 
     * <p> second element: Integer enum for analog to digital transfer method. 
     * <p> third element: Integer enum for filter band type.
     * <p> fourth element: Double for sampling rate.
     * <p>
     * Refer ptolemy.math.Filter for these enums.
     * <p>
     * @return IIR filter paramters in a Vector. 
     */
    public Vector getIIRParameter(){
        if (_type == Filter.IIR){
            Integer aprox = new Integer(_appmethod);
            Integer mapm = new Integer(_mapmethod);
            Integer bandt = new Integer(_bandtype);
            Double fs = new Double(_sampleFreq);
            Vector sent = new Vector();
            sent.addElement(aprox);
            sent.addElement(mapm);
            sent.addElement(bandt);
            sent.addElement(fs);
            return sent;

        } else return null;
    }

    /**
     * Set the IIR filter parameters.  Enums that represent various
     * IIR design parameters are passed in.  The frequency domain spec
     * is reset according the following manner: <p>
     *
     * * If band type did not changed, then previous frequency spec
     * is still valid.  This allow user to do comparison between different
     * design method with the same frequency spec. <p>
     * * If band type changed: here is default value for each band: <p>
     *    # lowpass : passband edge = 0.5pi at 0.8 gain <p>
     *                stopband edge = 0.6pi at 0.2 gain <p>
     *    # highpass : stopband edge = 0.5pi at 0.2 gain <p>
     *                 passband edge = 0.6pi at 0.8 gain <p>
     *    # bandpass : stopband edge = 0.5pi at 0.2 gain <p>
     *                 passband edge = 0.6pi at 0.8 gain <p>
     *                 second passband edge = 0.8pi  <p>
     *    # bandstop : passband edge = 0.5pi at 0.8 gain <p>
     *                 stopband edge = 0.6pi at 0.2 gain <p>
     *                 second stopband edge = 0.8pi  <p>
     * <p>
     * New real digital filter is designed, and all the observer (views)
     * are notified about the new filter.
     * <p>
     * @param atype IIR filter approximation method  
     * @param mtype analog to digital map method  
     * @param btype filter's band type
     * @param fs sampling frequency
     */ 
   public void setIIRParameter(int atype, int mtype, 
                               int btype, double fs){


        _appmethod = atype;
        _mapmethod = mtype; 
 
       // if the band type of the filter is changed, reset the
       // the filter bands to some default value 
        if (btype != _bandtype){
            if (btype == Filter.LOWPASS){ // lowpass defaults 
                 _band = new double[2];
                 _gain = new double[2];
                 _band[0] = 0.5*Math.PI;
                 _band[1] = 0.6*Math.PI;
                 _gain[0] = 0.8;
                 _gain[1] = 0.2;
            } else if (btype == Filter.HIGHPASS){ // highpass defaults
                 _band = new double[2];
                 _gain = new double[2];
                 _band[0] = 0.5*Math.PI;
                 _band[1] = 0.6*Math.PI;
                 _gain[0] = 0.2;
                 _gain[1] = 0.8;
            } else if (btype == Filter.BANDPASS){ // highpass defaults
                 _band = new double[3];
                 _gain = new double[2];
                 _band[0] = 0.3*Math.PI;
                 _band[1] = 0.5*Math.PI;
                 _band[2] = 0.8*Math.PI;
                 _gain[0] = 0.2;
                 _gain[1] = 0.8;
            } else if (btype == Filter.BANDSTOP){ // highpass defaults
                 _band = new double[3];
                 _gain = new double[2];
                 _band[0] = 0.3*Math.PI;
                 _band[1] = 0.5*Math.PI;
                 _band[2] = 0.8*Math.PI;
                 _gain[0] = 0.8;
                 _gain[1] = 0.2;
            } else {
                 System.out.println("unsupported filter spec"); 
            } 
        }     
             
        _bandtype = btype;
        _sampleFreq = fs;

        // design the new filter
        _filter = DigitalFilter.designRealIIR(_mapmethod, _appmethod, 
                                       _bandtype, _band, _gain, 
                                       _sampleFreq);
 
//        _filter = DigitalFilter.designRealIIR(_mapmethod, _appmethod, 
//                                       _bandtype, _band, _gain, 
//                                       _lrippleh, _hrippleh,
//                                       _sampleFreq);

        setChanged();
      
        notifyObservers("UpdatedFilter");
   }

   public void updateImpulseValue(Complex [] data){


   }


/*
   public void setPoleZeroGain(Complex [] singlepole, Complex [] singlezero,
                               ConjugateComplex [] conjpole,
                               ConjugateComplex [] conjzero, Complex gain){

       _filter.setPoleZeroGain(singlepole, singlezero, conjpole, conjzero, gain);
        setChanged();
      
        notifyObservers("UpdatedFilter");
   
   }
*/

   /**
    * Update the frequency domain spec of the filter.
    * This function is called when the frequency domain observer 
    * changes the passband/stopband values on the view.  
    * After the new band data is stored, the filter is 
    * redesigned by calling <code> DigitalFilter.designIIR () </code>.
    * All views/observers are notified about the change.
    * <p>
    * @param banddata frequency domain critical frequencies. 
    * @param gaindata frequency domain gains at critical frequencies. 
    */
   public void updateFreqSpec(double [] banddata, double [] gaindata){

        // banddata.length should be the same as _band
        for (int i=0;i<banddata.length;i++){
             _band[i] = banddata[i];
        }
  
        // gaindata.length should be the same as _gain
        for (int i=0;i<gaindata.length;i++){
             _gain[i] = gaindata[i];
        }


        // design the new IIR filter
//        _filter = DigitalFilter.designRealIIR(_mapmethod, 
//             _appmethod, _bandtype, _band, _gain, sr, pr, 
//             _sampleFreq);

        _filter = DigitalFilter.designRealIIR(_mapmethod, 
             _appmethod, _bandtype, _band, _gain, _sampleFreq);

        setChanged();
        notifyObservers("UpdatedFilter");

   }

  
   /**  
    * Add new pole/zero pair to filter.  Observers will be notified
    * about the changed filter.
    * <p>
    * @param pole pole to be added. 
    * @param zero zero to be added. 
    * @param conj boolean indicate if the given pole/zero have complex 
    * conjuate pair. 
    */
   public void addPoleZero(Complex pole, Complex zero, boolean conj){
        if (_filter == null) return;
        _filter.addPoleZero(pole, zero, 1.0, conj);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   /**  
    * Update pole's value.  Observers will be notified about the changed filter.
    * <p>
    * @param pole pole object to be changed. 
    * @param x new value's real part.
    * @param y new value's imaginary part.
    */
   public void updatePoleValue(Complex pole, double x, double y){
        if (_filter == null) return;
System.out.println("moving pole in filter object");
        _filter.movePole(pole, x, y);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   /**  
    * Update zero's value.  Observers will be notified about the changed filter.
    * <p>
    * @param zero zero object to be changed. 
    * @param x new value's real part.
    * @param y new value's imaginary part.
    */
   public void updateZeroValue(Complex zero, double x, double y){
        if (_filter == null) return;
        _filter.moveZero(zero, x, y);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   
   /**  
    * Delte the factor that contains the given pole. Observers will be 
    * notified about the changed filter.
    * <p>
    * @param pole pole's factor object to be deleted. 
    */
   public void deletePole(Complex pole){
        if (_filter == null) return;
        _filter.deletePole(pole);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   /**  
    * Delte the factor that contains the given zero. Observers will be 
    * notified about the changed filter.
    * <p>
    * @param zero zero's factor object to be deleted. 
    */
   public void deleteZero(Complex zero){
        if (_filter == null) return;
        _filter.deleteZero(zero);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   //////////////////////////////////////////////////////////////////////////
   ////                       private variables                          ////

   private String _fname;   // filter name
   private int _bandtype;  // see ptolemy.math.Filter enum for band type 
   private int _appmethod;  // see ptolemy.math.Filter enum for approximation type 
   private int _mapmethod; // see ptolemy.math.Filter enum for analog to digital
                           // transformation type 
   private double _sampleFreq;
   private double [] _band = null;
   private double [] _gain = null;
   private int _stat;
   private int _type; // see ptolemy.math.Filter enum for filter type 
  
   private DigitalFilter _filter;

   private double _step = Math.PI/150;
}
