/* Manager of the ptfilter.   
 
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
 
package ptolemy.filter.controller;

import java.util.*;
import ptolemy.math.Complex;
import ptolemy.math.PoleZero;
import ptolemy.math.FType;
import ptolemy.filter.filtermodel.*;
import ptolemy.filter.view.*;

//////////////////////////////////////////////////////////////////////////
//// Manager 
/**
 * The manager handle all the creation the filter object
 * and the views.  It also handles the user input from the Tmain
 * and passes them to the filter object.
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */
public class Manager {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
 
    /**
     * Constructor.  _opMode is set for specify which mode the ptfilter is in
     * either as stand alone application or applet on the web.  Mode 0 for frame,
     * Mode 1 for applet.
     */ 
    public Manager(int mode){
       _opMode = mode;
       _fobj = null;
       _pzv = null;
       _fv = null;
       _iv = null; 
       _iirfiltsetv = null; 
    }


    // this method called by TMain to set up a new filter
    // the method below it void setupfilter() is replaced by
    // this one.
    public void newFilter(int type, String name){

       if (_fobj!= null){
          deletefilter();
       } 
       _fobj = new FilterObj(this);
       _fobj.init(name, type);
       addpolezeroview();
       addfreqview();
       addimpulview();
       if (type == FType.IIR){ // IIR
          if (_opMode == FrameMode){ // frame mode, it is ok to start 
                                     // filter design now since all the 
                                     // graphics is taken care internally
              _fobj.setIIRParameter(FType.Butterworth, FType.Bilinear, 
                                    FType.Lowpass, 1.0);
          }
          addfiltparamview(); 
       }
        
    }

    public void start(){
       int type = _fobj.getType();
       if (type==FType.IIR){ //IIR
           _fobj.setIIRParameter(FType.Butterworth, FType.Bilinear, 
                                 FType.Lowpass, 1.0);
       } 
    }
  
    public PoleZeroView getPoleZeroView(){
       return _pzv;
    } 

    public FreqView getFreqView(){
       return _fv;
    }

    public ImpulsView getImpulsView(){
       return _iv;
    }

    public IIRFiltSetView getIIRSetView(){
       return _iirfiltsetv;
    } 
    

    /**
     * Set all parameters of the filter to the given value.  This function
     * will create the filter object then passes all the parameters.
     * Called by TMain when the new parameters are set by the user.
     */ 
    public void setupfilter(int trans, int approx, int f, 
       double g1, double g2, double b1, double b2, double fs, String name){

       double [] band = {b1, b2};
       double [] gain = {g1, g2};
       
       _fobj = new FilterObj(this);
    
       // set the name, and type to be IIR
       _fobj.init(name, 1);
    
       // set the parameters  
       _fobj.setIIRParameter(approx, trans, f, fs);
       _fobj.updateBandValue(band, gain, 0.0, 0.0);

   }

   /** 
    * get the gain of the filter. Called by TMain for 
    * saving the filter to file.
    */
   public double getfilterGain(){
       return _fobj.getGain();
   }


   /** 
    * get the numerator of the filter. Called by TMain for
    * saving the filter to file.
    */
   public double [] getfilterNumerator(){
       return _fobj.getNumerator();
   } 


   /** 
    * get the denominator of the filter. Called by TMain for
    * saving the filter to file.
    */
   public double [] getfilterDenominator(){
       return _fobj.getDenominator();
   } 

 
   /**
    * get the poles/zeros of the filter.  Called by TMain for 
    * saving filter to file.
    */
   public int getfilter(Vector ival1, Vector ival2, Vector ival3, 
                        String name){
      PoleZero [] pole;
      PoleZero [] zero;
      name = null;
      if (_fobj!=null){
           pole = _fobj.getPole();
           for (int i = 0;i<pole.length;i++){
                Double d1 = new Double(pole[i].re());  
                Double d2 = new Double(pole[i].im());  
                Boolean po = new Boolean(true);  
                ival1.addElement(d1); 
                ival2.addElement(d2); 
                ival3.addElement(po); 
           }
           zero = _fobj.getZero();
           for (int i = 0;i<zero.length;i++){
                Double d1 = new Double(zero[i].re());  
                Double d2 = new Double(zero[i].im());  
                Boolean po = new Boolean(false);  
                ival1.addElement(d1); 
                ival2.addElement(d2); 
                ival3.addElement(po); 
           }
           name = _fobj.getName();
           return _fobj.getType(); 
      } 
      return -1;
   }


   /**
    *  Create the filter from the given set of poles and zeros.
    *  Called by TMain when a filter is loaded in as a file that specifies
    *  in poles and zeros.  Also it is called when an empty filter is desired.
    */ 
   public boolean createFilterbyPolesZeros(String name, int type, Vector ival1, 
                            Vector ival2, Vector ival3) {

         if (ival1==null) return false;

         if (_fobj != null){
              deletefilter();
         }
      
         PoleZero [] pole;
         PoleZero [] zero;
         int pcount = 0;
         int zcount = 0;
         int curpole = 0;
         int curzero = 0;
         // count the number of poles and zeros
         for (int i=0;i<ival3.size();i++){
             if (((Boolean)ival3.elementAt(i)).booleanValue()==true){
                 pcount++;
             } else {
                 zcount++;
             }
         } 
 
         pole = new PoleZero[pcount];
         zero = new PoleZero[zcount];

         _fobj = new FilterObj(this);
// need reconstruct vector to feed into filter
         if (type == 1) { // IIR 
              Vector initvalue = new Vector();
              for (int i = 0; i<ival1.size();i++){
                     PoleZero c = new PoleZero(new Complex(((Double)ival1.elementAt(i)).doubleValue(), ((Double)ival2.elementAt(i)).doubleValue()));
                     if(((Boolean)ival3.elementAt(i)).booleanValue()==true){
                         pole[curpole]=c;
                         curpole++;
                     } else {
                         zero[curzero]=c;
                         curzero++;
                     } 
              }
              _fobj.init(name, type);
              _fobj.updatePoleZero(pole, zero);
              addpolezeroview();
              addfreqview();
              addimpulview();
              
              return true;
         }
         return false;
   }

   /**
    * Delete the filter object.  It check if the view objects
    * exists or not, delete them if they do exist.
    */
   public boolean deletefilter() {
        if (_fobj != null){
            if (_pzv != null) {
               removePoleZeroView();
            }
            if (_fv != null) {
               removeFreqView();
            }
            if (_iv != null) {
               removeImpulsView();
            }
            if (_iirfiltsetv != null){
               removeIIRFiltSetView();
            }
            _fobj = null;
            return true;
        } 
        return false;
   }
  
   /**
    * Add a pole-zero view. It calls filter object's <code> addObserver()
    * </code> to add the observer to the observer list.
    * It make sure the polezero view is null, and filter object
    * is not null.
    */ 
   public boolean addpolezeroview(){
        if ((_pzv == null) && (_fobj != null)){
             _pzv = new PoleZeroView(_fobj, _opMode, _fobj.getName()); 
             _fobj.addObserver(_pzv);
             return true;
        }
        return false;
   }

   /**
    * Add a freq view. It calls filter object's <code> addObserver()
    * </code> to add the observer to the observer list.
    * It make sure the polezero view is null, and filter object
    * is not null.
    */ 
   public boolean addfreqview(){
        if ((_fv == null) && (_fobj != null)){
             _fv = new FreqView(_fobj, _opMode, _fobj.getName()); 
             _fobj.addObserver(_fv);
             return true;
        }
        return false;
   }
 
   /**
    * Add a impulse view. It calls filter object's <code> addObserver()
    * </code> to add the observer to the observer list.
    * It make sure the polezero view is null, and filter object
    * is not null.
    */ 
   public boolean addimpulview(){
        if ((_iv == null) && (_fobj != null)){
             _iv = new ImpulsView(_fobj, _opMode, _fobj.getName()); 
             _fobj.addObserver(_iv);
             return true;
        }
        return false;
   }

   /** 
    * add the filter parameter view
    */
   public boolean addfiltparamview(){
        if (_fobj != null){
             if (_fobj.getType() == 1){ // IIR
                 if (_iirfiltsetv == null){ // make sure don't add multiple view
                      _iirfiltsetv = new IIRFiltSetView(_fobj, _opMode, _fobj.getName());
                      _fobj.addObserver(_iirfiltsetv);
                      return true;
                 }
             }
        }
        return false;
   }
   /**
    * Delete the pole-zero view.  This is called from TMain
    * when user killed the view on the menu entry. 
    * calls view's <code> userKill() </code> to dispose
    * the frame and free the plots.
   */
   public void removePoleZeroView(){
       if (_pzv!=null) {
           _fobj.deleteObserver(_pzv);
           _pzv.deleteFrame();
           _pzv = null;
       }
   }
 
   /**
    * Delete the freq view.  This is called from TMain
    * when user killed the view on the menu entry. 
    * calls view's <code> userKill() </code> to dispose
    * the frame and free the plots.
   */
   public void removeFreqView(){
       if (_fv!=null) {
           _fv.deleteFrame();
           _fobj.deleteObserver(_fv);
           _fv = null;
       }
   } 

   /**
    * Delete the impuls view.  This is called from TMain
    * when user killed the view on the menu entry. 
    * calls view's <code> userKill() </code> to dispose
    * the frame and free the plots.
   */
   public void removeImpulsView(){
       if (_iv!= null) {
           _iv.deleteFrame();
           _fobj.deleteObserver(_iv);
           _iv = null;
       }
   } 


   /**
    * this is called to delete the generic filterparam view
    */
   public void removefiltparamview(){
       if ((_fobj!=null) && (_fobj.getType() == 1)){
           removeIIRFiltSetView(); 
       }
   }

   /**
    * Delete the IIR filter set view
    */
   public void removeIIRFiltSetView(){
       if (_iirfiltsetv!= null) {
           _iirfiltsetv.deleteFrame();
           _fobj.deleteObserver(_iirfiltsetv);
           _iirfiltsetv = null;
       } 
   }

   /**
    * Set the pole-zero view to null.  Called by Filter object to notify
    * the manager to delete the pole zero view object.
    */ 
   public void setPoleZeroView2NULL() {
       _pzv = null;
   }

   /**
    * Set the freq view to null.  Called by Filter object to notify
    * the manager to delete the freq view object.
    */ 
   public void setFreqView2NULL() {
       _fv = null;
   } 

   /**
    * Set the impulse view to null.  Called by Filter object to notify
    * the manager to delete the impulse view object.
    */ 
   public void setImpulsView2NULL() {
       _iv = null;
   }
  
   //////////////////////////////////////////////////////////////////////////
   ////                         public variables                        ////
   public final static int FrameMode = 0; 
   public final static int AppletMode = 1; 
   
   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////
 
   private int _opMode;
   private PoleZeroView _pzv;
   private FreqView _fv;
   private ImpulsView _iv;
   private IIRFiltSetView _iirfiltsetv;
   private FilterObj _fobj;  
}
