/* Filter Model in the Model/View pattern 


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

$Id$
*/

package ptolemy.filter.filtermodel;

import java.util.*;
import collections.*;
import ptolemy.math.*;
import ptolemy.filter.view.*;

//////////////////////////////////////////////////////////////////////////
//// FilterObj 
/** 
 * This object stores various design parameters that will be used
 * to design filter.  These parameters include:
 * - type of digital filter: IIR, FIR, etc 
 * for IIR:
 * - approximation methods: Butterworth, Chebyshev, Elliptical
 * - A/D transformation method: Impulse invariant, Bilinear, Mathch-Z
 * - band type: low pass, high pass, band stop, band pass.
 * - edge frequencies and gain.
 * - ripple heights.
 *
 * FilterObj also uses class DigitalFilter to store different sets 
 * of data that represents the designed filter.  Data like pole/zero
 * sets, frequency response, impulse reponse, numerator/denominator,
 * etc.   
 *
 * Since FilterObj is derived from Observable, it keep tracks of all 
 * the observer, the plots, when an update occurs in the plot,
 * the plot (observer) notify the plot, plot update all the changes
 * to make sure all the data is in synch, then calls notifyobserver
 * to let other observers to pick up the new changes. 
 * 
 * author: William Wu
 * version:
 * date: 3/2/98
 */

public class FilterObj extends Observable {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Constructor for the filter object.
     * The Manager object manages the displaying of all gui window.   
     */ 
    public FilterObj(){
        _filter = new DigitalFilter();
    }

    /**
     * Returns the name of the filter
     */
    public String getName(){
        return _fname;
    }

    /**
     * Return the type of the filter
     */
    public int getType(){
        return _type;
    }

    /**
     * Return the step size of decimation in frequency
     * of frequency repsonse.
     */
    public double getStep(){
        return _filter.getStep();
    }

    /**
     * Returns the frequency domain parameters
     */
    public double [] getFreqBand(){
        return _band;
    }
  
    /**
     * Returns the frequency domain parameters
     */
    public double [] getFreqGain(){
        return _gain;
    }

    /**
     * Returns the frequency domain parameters
     */
    public double [] getFreqRippleHeight(){
        double [] ripples = {_hrippleh, _lrippleh};
        return ripples; 
    }
 
    /**
     * Returns the gain 
     */
    public Complex getGain(){
        return _filter.getGain();
    }


    /**
     * Returns the poles in z-plane 
     */
    public Complex [] getPole(){
       return _filter.getPole();
    }

    /**
     * Returns the zeros in z-plane 
     */
    public Complex [] getZero(){
       return _filter.getZero();
    }

    /**
     * Returns the frequency domain data 
     */
    public Complex [] getFrequency(){
       return _filter.getFrequencyResponse();
    }

    /**
     * Returns the frequency domain data in dB 
     */
    public double [] getFrequencyDB(){
       Complex [] freq = _filter.getFrequencyResponse();
       double [] result = new double[freq.length];
       for (int i=0;i<result.length;i++){
           result[i] = SignalProcessing.db(freq[i].abs());
       } 
       return result;
    }

    /**
     * Returns the impulse response 
     */
    public Complex [] getImpulse(){
       return _filter.getImpulseResponse();
    }
  

    /**
     * Returns the numerators 
     */
    public Complex [] getNumerator(){
       return _filter.getNumerator();
    }
  
    /**
     * Returns the denominator 
     */
    public Complex [] getDenominator(){
       return _filter.getDenominator();
    }

    /**
     * Returns the IIR parameters as a Vector 
     */
    public Vector getIIRParameter(){
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
    }

    /**
     * Set the IIR filter parameters.
     * This function is called by Top to set the initial 
     * parameters for IIR filter, like filter type, approximation 
     * method, continous2discrete map method, passband values, 
     * stopband values, sampling frequency. then it calles
     * the math library method to redesign the filter.  
     * It is also called by IIR parameter observer that handles
     * user input from the IIR parameter window.
     */ 
   public void setIIRParameter(int atype, int mtype, 
                               int btype, double fs){

        _appmethod = atype;
        _mapmethod = mtype; 

        if (atype == FType.Butterworth) { // butterworth
            // the ripple to -1
            _hrippleh = -1;
            _lrippleh = -1;
        } else if (atype == FType.Chebyshev1) { // chebshev 
            // the ripple to certain default ripple height 
            _hrippleh = 0.1;
            _lrippleh = -1;
        } else if (atype == FType.Elliptical) { // ellipitical 
            // the ripple to certain default ripple height 
            _hrippleh = 0.1;
            _lrippleh = 0.1;
        } else {
            System.out.println("unsupported filter spec"); 
        }
 
       // if the band type of the filter is changed, reset the
       // the filter bands to some default value 
        if (btype != _bandtype){
            if (btype == FType.Lowpass){ // lowpass defaults 
                 _band = new double[2];
                 _gain = new double[2];
                 _band[0] = 0.5*Math.PI;
                 _band[1] = 0.6*Math.PI;
                 _gain[0] = 0.8;
                 _gain[1] = 0.2;
            } else if (btype == FType.Highpass){ // highpass defaults
                 _band = new double[2];
                 _gain = new double[2];
                 _band[0] = 0.6*Math.PI;
                 _band[1] = 0.7*Math.PI;
                 _gain[0] = 0.2;
                 _gain[1] = 0.8;
            } else if (btype == FType.Bandpass){ // bandpass defaults
                 _band = new double[3];
                 _gain = new double[2];
                 _band[0] = 0.3*Math.PI;
                 _band[1] = 0.5*Math.PI;
                 _band[2] = 0.8*Math.PI;
                 _gain[0] = 0.2;
                 _gain[1] = 0.8;
            } else if (btype == FType.Bandstop){ // bandstop defaults
                 _band = new double[3];
                 _gain = new double[2];
                 _band[1] = 0.3*Math.PI;
                 _band[0] = 0.5*Math.PI;
                 _band[2] = 0.8*Math.PI;
                 _gain[1] = 0.8;
                 _gain[0] = 0.2;
            } else {
                 System.out.println("unsupported filter spec"); 
            } 
        }     
             
        _bandtype = btype;
        _sampleFreq = fs;

        // design the new filter
        _filter = MathWizard.IIRFilter(_mapmethod, _appmethod, 
                                       _bandtype, _band, _gain, 
                                       _lrippleh, _hrippleh,
                                       _sampleFreq);

        setChanged();
      
        notifyObservers("UpdatedFilter");
   }

   public void updateImpulseValue(Complex [] data){


   }


   public void setPoleZeroGain(Complex [] singlepole, Complex [] singlezero,
                               ConjugateComplex [] conjpole,
                               ConjugateComplex [] conjzero, Complex gain){

       _filter.setPoleZeroGain(singlepole, singlezero, conjpole, conjzero, gain);
        setChanged();
      
        notifyObservers("UpdatedFilter");
   
   }

   /**
    * Update the bands data, and redesign/design the filter.
    * This function is called when the frequency domain observer 
    * changes the passband/stopband value on the view.  
    * After the new band data is stored, the filter is 
    * redesigned by calling <code> MathWizard.IIRFilter() </code>.
    */
   public void updateBandValue(double [] banddata, double [] gaindata, 
                               double pr, double sr){


        // banddata.length should be the same as _band
        for (int i=0;i<banddata.length;i++){
             _band[i] = banddata[i];
        }
  
        // gaindata.length should be the same as _gain
        for (int i=0;i<gaindata.length;i++){
             _gain[i] = gaindata[i];
        }

        _lrippleh = sr;
        _hrippleh = pr;


        // design the new filter
        _filter = MathWizard.IIRFilter(_mapmethod, 
             _appmethod, _bandtype, _band, _gain, sr, pr, 
             _sampleFreq);

        setChanged();
        notifyObservers("UpdatedFilter");

   }

   /**
    * Initialize the filter with a name and type.
    * Top calls this function to set up the basic parameter 
    * like name, and type.
    */ 
   public void init(String name, int type){
        this._type = type;
        _fname = new String(name);
   }

   
   public void addPoleZero(Complex pole, Complex zero, boolean conj){
        _filter.addPoleZero(pole, zero, conj);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void movePole(Complex pole){
        _filter.movePole(pole);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void moveZero(Complex zero){
        _filter.moveZero(zero);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void deletePole(Complex pole){
        _filter.deletePole(pole);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void deleteZero(Complex zero){
        _filter.deleteZero(zero);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void splitConjPole(Complex pole){
        _filter.seperateConjugatePole(pole);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void splitConjZero(Complex zero){
        _filter.seperateConjugateZero(zero);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void makePoleConj(Complex pole){
        _filter.makeConjugatePole(pole);
        setChanged();
        notifyObservers("UpdatedFilter");
   }

   public void makeZeroConj(Complex zero){
        _filter.makeConjugateZero(zero);
        setChanged();
        notifyObservers("UpdatedFilter");
   }


   //////////////////////////////////////////////////////////////////////////
   ////                       private variables                          ////

   private String _fname;   // filter name
   private int _bandtype;  // 1. lowpass, 2. high pass, 3. bandpass
   private int _appmethod; // 1. butterworth, 2. cheb 
   private int _mapmethod; // 1. bilinear, 2. impulse invar, 3. match z
   private double _sampleFreq;
   private double [] _band = null;
   private double [] _gain = null;
   private double _lrippleh = -1;
   private double _hrippleh = -1;
   private int _stat;
   private int _type; // 1. IIR, 2. FIR
  
   private DigitalFilter _filter;

   private double _step = Math.PI/150;
}
