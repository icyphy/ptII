 /* Filter Object

Below is the copyright agreement for the Ptolemy system.

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

$Id$ %S%
*/

package ptolemy.filter.filtermodel;

import java.util.*;
import ptolemy.math.*;
import ptolemy.filter.controller.*;
import ptolemy.filter.view.*;

//////////////////////////////////////////////////////////////////////////
//// FilterObj 
/** 
 * Filter object, it store different sets of data corresponding 
 * to the filter: 
 *     pole and zero in Z-plane
 *     frequency domain magnitudes
 *     time domain impules 
 *     gain
 *     numerator and denominator coeff
 *
 * Since it is derived from Observable, it keep tracks of all 
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
     *  Manager object is the parameter
     */ 
    public FilterObj(Manager m){
        _bigBoss = m;
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
        return _step;
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
    public double getGain(){
        return _tgain;
    }

    /**
     * Returns the poles  in z-plane 
     */
    public PoleZero [] getPole(){
       return _zpole;
    }

    /**
     * Returns the zeros in z-plane 
     */
    public PoleZero [] getZero(){
       return _zzero;
    }


    /**
     * Returns the frequency domain data 
     */
    public Complex [] getFrequency(){
       return _freqvalue;
    }

    /**
     * Returns the frequency domain magnitude in DB 
     */
    public double [] getFrequencyDB(){
       return _freqDBvalue;
    }

    /**
     * Returns the time impulses 
     */
    public Complex [] getImpulse(){
       return _impulvalue;
    }
  
    public Complex [] getImpulse2(){
       return _impulvalue2;
    }

    /**
     * Returns the numerators 
     */
    public double [] getNumerator(){
       return _numerator;
    }
  
    /**
     * Returns the denominator 
     */
    public double [] getDenominator(){
       return _denominator;
    }

    /**
     * Returns the IIR parameters as a vector 
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
     * Set the init IIR filter parameters.
     * This function is called by Top to set the initial 
     * parameters for IIR filter, like filter type, approximation 
     * method, continous2discrete map method, passband values, 
     * stopband values, sampling frequency. then it calles
     * the math library method to redesign the filter.  
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
        }
 
       // if the band type of the filter is changed, reset the
       // the filter bands to some default value 
        if (btype != _bandtype){
            if (btype == FType.Lowpass){ // lowpass defaults 
                 _band = new double[2];
                 _gain = new double[2];
                 _band[0] = 0.5;
                 _band[1] = 0.6;
                 _gain[0] = 0.8;
                 _gain[1] = 0.2;
            } else if (btype == FType.Highpass){ // highpass defaults
                 _band = new double[2];
                 _gain = new double[2];
                 _band[0] = 0.6;
                 _band[1] = 0.7;
                 _gain[0] = 0.2;
                 _gain[1] = 0.8;
            } else if (btype == FType.Bandpass){ // bandpass defaults
                 _band = new double[3];
                 _gain = new double[2];
                 _band[0] = 0.3;
                 _band[1] = 0.5;
                 _band[2] = 0.8;
                 _gain[0] = 0.2;
                 _gain[1] = 0.8;
            } else if (btype == FType.Bandstop){ // bandstop defaults
                 _band = new double[3];
                 _gain = new double[2];
                 _band[1] = 0.3;
                 _band[0] = 0.5;
                 _band[2] = 0.8;
                 _gain[1] = 0.8;
                 _gain[0] = 0.2;
            } 
        }     
             
        _bandtype = btype;
        _sampleFreq = fs;

        // 
        if (_band != null){
            // design the new filter
            Vector pole=new Vector();
            Vector zero=new Vector();
            double gain = MathWizard.designIIRFilter(_mapmethod, 
             _appmethod, _bandtype, _band, _gain, _lrippleh, _hrippleh,
             _sampleFreq, pole, zero);

            // reset the poles and zeros
            PoleZero [] polev = new PoleZero[pole.size()];
            PoleZero [] zerov = new PoleZero[zero.size()];
            _tgain = gain;
        
            for (int i = 0;i<pole.size();i++){
                PoleZero c = (PoleZero) pole.elementAt(i);
                polev[i] = c;
            }

            for (int i = 0;i<zero.size();i++){
                PoleZero c = (PoleZero) zero.elementAt(i);
                zerov[i] = c;
            }


            updatePoleZero(polev, zerov);

            setChanged();
            notifyObservers("UpdatedFilter");

        }      
   }

   public void updateImpulseValue(Complex [] data){


   }

   /**
    * Update the bands data, and redesign/design the filter.
    * This function is called when the frequency View 
    * changes the passband/stopband value on the view.  
    * After the new band data is stored, the filter is 
    * redesigned by calling <code> MathWizard.designFilter() </code>.
    * It is also called initially from 
    * <code> this.setIIRInitParameter() </code> 
    */
   public void updateBandValue(double [] banddata, double [] gaindata, double pr, double sr){

        Vector pole=new Vector();
        Vector zero=new Vector();

        // banddata.length should be the same as _band
        for (int i=0;i<banddata.length;i++){
             _band[i] = banddata[i];
        }
  
        // gaindata.length should be the same as _band
        for (int i=0;i<gaindata.length;i++){
             _gain[i] = gaindata[i];
        }

        _lrippleh = sr;
        _hrippleh = pr;


        // design the new filter
        double gain = MathWizard.designIIRFilter(_mapmethod, 
             _appmethod, _bandtype, _band, _gain, sr, pr, 
             _sampleFreq, pole, zero);

        // reset the poles and zeros
        PoleZero [] polev = new PoleZero[pole.size()];
        PoleZero [] zerov = new PoleZero[zero.size()];
        _tgain = gain;

        for (int i = 0;i<pole.size();i++){
            PoleZero c = (PoleZero) pole.elementAt(i);
            polev[i] = c;
        }

        for (int i = 0;i<zero.size();i++){
            PoleZero c = (PoleZero) zero.elementAt(i);
            zerov[i] = c;
        }

        updatePoleZero(polev, zerov);
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

   /**
    * Update the poles and zeros to the new value.
    * Poles/zeros are changed from Top or View.  This function reset 
    * to the new poles and zeros and update other data, frequency
    * domain, time domain, and numerator/denominator.
    */  
   public void updatePoleZero(PoleZero [] pole, PoleZero [] zero){
        _zpole = pole;
        _zzero = zero;
  
        // this might not be a good way to find out the order.
        _order = Math.max(_zpole.length, _zzero.length); 
 
        // this function is used to merge the close poles and zeros 
        _organizePoleZero();

        // count the number of poles and zeros
        _poleCount = _zpole.length; 
        _zeroCount = _zzero.length; 

        System.out.println("pole count: "+_poleCount+" zero count: "+_zeroCount);

        // compute the numer/denom
        _updateNumerAndDenom();

        // compute the new frequency domain data 
        Complex [] newf = MathWizard.PoleZeroToFreq(_zpole, _zzero, _tgain, _step);
        _freqvalue = new Complex[newf.length];
        _freqDBvalue = new double[newf.length];

        for (int i=0;i<newf.length;i++){
            _freqvalue[i]=new Complex(newf[i]);

            _freqDBvalue[i]=MathWizard.db(newf[i].abs());
        }

        // compute the new time domain data 
        // old method use an IFFT on the frequency response data
        // turn out it is not very accurate.
        //  
         _impulvalue2 = MathWizard.FreqToImpuls(_freqvalue);

        // compute the new time domain data
        // this method use the time shifting method in Ptolemy IIR star
/*        double [] newi = MathWizard.computeImpulseResponse(
                         _numerator, _denominator, _tgain, 200);
        _impulvalue = new double[newi.length];
        for (int i=0;i<newi.length;i++){
            _impulvalue[i]=newi[i];
        }
*/
        _impulvalue = MathWizard.computeImpulseResponse( _numer, _denom, 
                                                         _tgain, 200);
   
   }



   /**
    * Receives message and data from observers, and process them.
    * The is functions are called by various observers, to notify
    * filter object about changes made in that observer.  Each observer
    * has its own id number, so the filter knows it is talking to.
    * When new changes are in poles/zeros, the updated data is saved,
    * <code> updatePoleZero() </code> is called to update pole/zero, and
    * other data.  
    * When new changes are in band, the updated band is saved, and 
    * <code> updateBandValue() </code> is called to redesign the filter,
    * all other data also gets updated.
    *
    * After all the data settles in, <code> setChanged() </code> & 
    * <code> notifyObservers() </code> are called, so the observer
    * is notified about the newly changed filter, thus observer
    * can request for new data.
    * 
    * This is method is also used when user close the observer (plot)
    * by closing the window.  Thus the observer is removed by calling
    * function in View.
    *  
    * data could be a vector of array, or just a single array
    */
   public void receive(int id, String message, Object data){
        if (message.equals("Update")){
            if (id == 1) { // updated pole-zero

                Vector pz = (Vector) data;
            // store the updates
                PoleZero [] pole = (PoleZero []) pz.elementAt(0);
                PoleZero [] zero = (PoleZero []) pz.elementAt(1);


             // update pole/zero and other data        

                updatePoleZero(pole, zero);

             // notify the observers about new changes
                setChanged();
                notifyObservers("UpdatedFilter");
            } else if (id == 2) { // updated freq
      
                Vector freqp = (Vector) data; 
              // store the updates
                double [] bvalue = (double []) freqp.elementAt(0);
                double [] gvalue = (double []) freqp.elementAt(1);
                double [] rvalue = (double []) freqp.elementAt(2);
            
             // update band value and redesign the filter 
                updateBandValue(bvalue, gvalue, rvalue[0], rvalue[1]);

             // notify the observers about new changes
                setChanged();
                notifyObservers("UpdatedFilter");
            } else if (id == 3) { // updated impulse

              // this might not be needed, since user can't change the 
              // filter at impulse response plot.
                Complex [] value = (Complex []) data;
                updateImpulseValue(value);
                setChanged();
                notifyObservers("UpdatedFilter");
                // need to add impul to polezero update
                // need to add impul to freq update
            } else if (id == 4) { // updated IIR parameters 
                Vector param = (Vector) data;
                int aprox = ((Integer)param.elementAt(0)).intValue();
                int mapm = ((Integer)param.elementAt(1)).intValue();
                int band = ((Integer)param.elementAt(2)).intValue();
                double fs = ((Double)param.elementAt(3)).doubleValue();
                setIIRParameter(aprox, mapm, band, fs);
                setChanged();
                notifyObservers("UpdatedFilter");
            }
       } else if (message.equals("SelfDestruct")){  // user window closing
            if (id == 1){
               PoleZeroView pzv = (PoleZeroView) data;

               // since View contains the plot, it will handle it.
               deleteObserver((Observer) pzv);
      
               // manager need to know about the deletion of observer          
               _bigBoss.setPoleZeroView2NULL();
            } else if (id == 2){
               FreqView fv = (FreqView) data;

               // since View contains the plot, it will handle it.
               deleteObserver((Observer) fv);

               // manager need to know about the deletion of observer          
               _bigBoss.setFreqView2NULL();
            } else {
               ImpulsView iv = (ImpulsView) data;

               // since View contains the plot, it will handle it.
               deleteObserver((Observer) iv);

               // manager need to know about the deletion of observer          
               _bigBoss.setImpulsView2NULL();
            }
       }
   }


   //////////////////////////////////////////////////////////////////////////
   ////                       private methods                            ////

   /**
    * This reorganizes all the poles and zeroes, first it check if
    * there are any pole close to a zero, which will cancel them out,
    * thus remove them, also if pole and / or zero has pair, remove the
    * pair also.  Then it check if there is unequal number of pole and zero
    * if so, it insert appropriate pole and zero at origin.
    */
   private void _organizePoleZero(){

         Vector polearray = new Vector();
         Vector zeroarray = new Vector();

         // put poles, and zeros into Vector, easier
         // to munipulate.        
         for (int i=0;i<_zpole.length;i++){
              polearray.addElement(_zpole[i]); 
         } 

         for (int i=0;i<_zzero.length;i++){
              zeroarray.addElement(_zzero[i]); 
         } 

         // go through the list of poles and zeros check if any pole/zero
         // are close enough to cancel each other.

         // "done" flag used to indicate the completion of checking iteration.
         boolean  done = false;
         boolean found = false;
         PoleZero pole = null;
         PoleZero zero = null;

         while (!done){
              done = true;               
              found = false;

              for (int i=0;i<polearray.size();i++){
                   pole = (PoleZero) polearray.elementAt(i);
                   for (int j=0;j<zeroarray.size();j++){
                        zero = (PoleZero) zeroarray.elementAt(j);
                        double xdiff = Math.abs(pole.re() - zero.re());  
                        double ydiff = Math.abs(pole.im() - zero.im()); 
                        if ((xdiff <= 0.01) && (ydiff <= 0.01)){
                             found = true;
                             break;
                        }
                   }
                   if (found == true) break;                              
              }

              if (found == true){
                   polearray.removeElement((Object) pole);      
                   if (pole.conjugate != null) {
                        polearray.removeElement((Object) pole.conjugate);  
                   }    
                   zeroarray.removeElement((Object) zero);      
                   if (zero.conjugate != null) {
                        zeroarray.removeElement((Object) zero.conjugate);  
                   }
                   done = false;
              }    
         }

         // make sure the number of pole equal to number of zero  
         // and delete any additional pole/zero at origin   
         if (polearray.size() > zeroarray.size()){
             for (int i = 0;i<polearray.size();i++){
                 pole = (PoleZero) polearray.elementAt(i);
                 if ((Math.abs(pole.re())<0.01) && 
                     (Math.abs(pole.im())<0.01)) {
                     polearray.removeElement(pole);
                     if (polearray.size() == zeroarray.size()) break;
                 }
             }       
             _zpole = new PoleZero[polearray.size()]; 
             _zzero = new PoleZero[polearray.size()]; 
         } else if (zeroarray.size() > polearray.size()){
             for (int i = 0;i<zeroarray.size();i++){
                 zero = (PoleZero) zeroarray.elementAt(i);
                 if ((Math.abs(zero.re())<0.01) && 
                     (Math.abs(zero.im())<0.01)) {
                     zeroarray.removeElement(zero);
                     if (polearray.size() == zeroarray.size()) break;
                 }
             }       
             _zpole = new PoleZero[zeroarray.size()]; 
             _zzero = new PoleZero[zeroarray.size()]; 
         } else {
             _zpole = new PoleZero[polearray.size()]; 
             _zzero = new PoleZero[zeroarray.size()]; 
         }

         // if number of zero and pole are not equal, make them equal by adding
         // pole/zero at origin.

         for (int i = 0;i<_zpole.length;i++){
              if (i<polearray.size()){
                   _zpole[i] = (PoleZero) polearray.elementAt(i);
              } else {
                   _zpole[i] = new PoleZero(new Complex(0.0));
              }
         }
       
         for (int i = 0;i<_zzero.length;i++){
              if (i<zeroarray.size()){
                   _zzero[i] = (PoleZero) zeroarray.elementAt(i);
              } else {
                   _zzero[i] = new PoleZero(new Complex(0.0));
              } 
         }       
   }

   /**
    * Update the numerator and denominators.
    * First build array of zeros and poles, and use
    * <code> MathWizard.zeroesToPoly() </code> to find the
    * polynomials coeff.  The array of coeff is arranged like this:
    * [ const a1 a2 a3 a4 ... ]  where a1 is the coeff for z^-1, a2
    * is the coeff for z^-2, so on.
    */
   private void _updateNumerAndDenom(){

        if ((_poleCount == 0) && ( _zeroCount == 0)) return;
 
        // get the polynomials
        _numer = MathWizard.zeroesToPoly(_zzero); 
        _denom = MathWizard.zeroesToPoly(_zpole); 

        // assume all real polynomial
        _numerator = new double[_numer.length];
        _denominator = new double[_denom.length];
        for (int i=0;i<_numer.length;i++){
            if (_numer[i] != null){
                _numerator[i] = _numer[i].re();
            }
        }   
        for (int i=0;i<_denom.length;i++){
            if (_denom[i] != null){
                _denominator[i] = _denom[i].re();
            }
        }
   }



   //////////////////////////////////////////////////////////////////////////
   ////                       private variables                          ////

   private Manager _bigBoss;
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
   private int _order;
   private int _poleCount;
   private int _zeroCount;
  
   private Complex [] _numer; 
   private Complex [] _denom; 
   private double [] _numerator;
   private double [] _denominator;
   private double _tgain=1.0;
   private PoleZero [] _zpole;
   private PoleZero [] _zzero;
 
//   private Vector _polezero;
   private Complex [] _freqvalue;
//   private Vector _freqvalue;
   private Complex [] _impulvalue;
   private Complex [] _impulvalue2;
//   private double [] _impulvalue;
   private double [] _freqDBvalue;
//   private Vector _impulvalue;

   private double _step = Math.PI/150;
}
