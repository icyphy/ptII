/* A DFM Source actor that provides the Frequency spec of a IIR filter. 

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

package ptolemy.domains.dfm.lib;

import ptolemy.actor.*;
import ptolemy.kernel.util.*;
import ptolemy.filter.view.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.math.Complex;
import ptolemy.math.filter.Filter;

import java.util.Observable; 

//////////////////////////////////////////////////////////////////////////
//// DFMFreqActor
/** 
 This DFM actor provides the frequncy spec of a IIR design filter.
 It's input is the magnitude of the frequency reponse, since that 
 will be generated later a feedback delay with a initial value must
 be used on the that input.  It also has input that tells the type
 of bands of the filter (lowpass/highpass/bandpass/bandstop).  It's
 outputs two arrays, one for critical frequency value, one for 
 the gain at these critical frequencies.  It contains a class
 that is derived from ptolemy.filter.view.FreqView.
 <p> 
@author William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.filter.view.FreqView 
*/
public class DFMFreqActor extends DFMActor {
    /** Constructor
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public DFMFreqActor(CompositeActor container, String name)
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _freqin = new IOPort(this, "freqResp", true, false);  
        _bandtypein = new IOPort(this, "bandtype", true, false);  
        _freqout = new IOPort(this, "criticalFreq", false, true);  
        _gainout = new IOPort(this, "criticalGain", false, true);  
  
        _view = new DFMFreqView();
 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Change the frequency reponse spec.  This result in refiring of 
     * system.  Critical frequencies are given in the range 0 - pi in
     * an array of doubles (length = 2 for lowpass and highpass, length
     * = 3 for bandpass and bandstop) while gains are given from 
     * 0 - 1.0, in an array of doubles with length = 2. 
     * @param name name of the parameter, this case it should either
     *   "CriticalFrequencies" or "CriticalGains". 
     * @param arg value of the parameter, in array of doubles
     * @return boolean value indicate if the change parameter is 
     * successful.
     */
     public boolean changeParameter(String name, Object arg){
        DFMDirector dir = (DFMDirector) getDirector();
        if (!dir.isWaitForNextIteration()) return false; 
 
        if (name.equals("CriticalFrequencies")){
            _criticalFreq = (double []) arg;
            _setParamChanged(true);
            dir.dfmResume();
            return true;
        } else if (name.equals("CriticalGains")){
            _criticalGain = (double []) arg;
            _setParamChanged(true);
            dir.dfmResume();
            return true;
        } else {
            // throw new IllegalArgumentException("");
        }
        return false;
     } 

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Produce two outputs, one for critical frequencies, and one
     * for critical gains.  The input token specifies the band
     * type.  If the band type is changed, change the spec also.  
     *
     * default values for spec are: <p>
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
     */	
    protected void _performProcess() {

         DFMToken bandtoken = (DFMToken) _inputTokens.get("bandtype");

         if (bandtoken.getTag().equals("New")){ 
             int bandtype = ((Integer) _getData("bandtype")).intValue();
             // if the the spec changed, use default for spec
             if (bandtype != _bandtype){
                if (bandtype == Filter.LOWPASS){ // lowpass defaults 
                     _criticalFreq = new double[2];
                     _criticalGain = new double[2];
                     _criticalFreq[0] = 0.5*Math.PI;
                     _criticalFreq[1] = 0.6*Math.PI;
                     _criticalGain[0] = 0.8;
                     _criticalGain[1] = 0.2;
                } else if (bandtype == Filter.HIGHPASS){ // highpass defaults
                     _criticalFreq = new double[2];
                     _criticalGain = new double[2];
                     _criticalFreq[0] = 0.5*Math.PI;
                     _criticalFreq[1] = 0.6*Math.PI;
                     _criticalGain[0] = 0.2;
                     _criticalGain[1] = 0.8;
                } else if (bandtype == Filter.BANDPASS){ // highpass defaults
                     _criticalFreq = new double[3];
                     _criticalGain = new double[2];
                     _criticalFreq[0] = 0.3*Math.PI;
                     _criticalFreq[1] = 0.5*Math.PI;
                     _criticalFreq[2] = 0.8*Math.PI;
                     _criticalGain[0] = 0.2;
                     _criticalGain[1] = 0.8;
                } else if (bandtype == Filter.BANDSTOP){ // highpass defaults
                     _criticalFreq = new double[3];
                     _criticalGain = new double[2];
                     _criticalFreq[0] = 0.3*Math.PI;
                     _criticalFreq[1] = 0.5*Math.PI;
                     _criticalFreq[2] = 0.8*Math.PI;
                     _criticalGain[0] = 0.8;
                     _criticalGain[1] = 0.2;
                } else {
                     throw new IllegalArgumentException();
                }
                _view.setFrequencySpec(_criticalFreq, _criticalGain);
             }
         }

         DFMDoubleArrayToken freqout = new DFMDoubleArrayToken("New", _criticalFreq);
         _outputTokens.put("criticalFreq", freqout);
         DFMDoubleArrayToken gainout = new DFMDoubleArrayToken("New", _criticalGain);
         _outputTokens.put("criticalGain", gainout);

    }

    protected void _performAnnotation(){

        DFMToken freqtoken = (DFMToken) _inputTokens.get("freqResp");
        if (freqtoken.getTag().equals("Annotate")){
            _frequencyResponse = (Complex []) freqtoken.getData();
            _view.setFrequencyResponse(_frequencyResponse);
        } else {
System.out.println("Tag: "+freqtoken.getTag());

            throw new IllegalArgumentException();
        } 

    } 

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private Complex [] _frequencyResponse;  
    private int _bandtype; 
    private double [] _criticalFreq;
    private double [] _criticalGain;
    private IOPort _freqout;
    private IOPort _gainout;
    private IOPort _bandtypein;
    private IOPort _freqin;
    private DFMFreqView _view;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // This a derived class from FreqView.  Null is pass for FilterObj
    // thus the derived class must make sure the method that reference
    // to FilterObj is either not used, or overloaded.

    class DFMFreqView extends FreqView {
         public DFMFreqView(){
             super(null, FilterView.FRAMEMODE,"DFM Freq View");
         }

         public void moveInteractComp(InteractComponent ic){
             // reorganize all the spec, and passes them to filter object.
             _changeFreqSpecValue(ic);
       
             // get edge frequencies
             double [] edgefreq = _getEdgeFrequencies(); 
             // get gain at edge frequencies 
             double [] edgegain = _getEdgeGains(); 

             if (ic.getDataSetNum() == 1){
                 while (!changeParameter("CriticalFrequencies", edgefreq)) {} 
             } else {
                 while (!changeParameter("CriticalGains", edgegain)) {}
             }
         }

         public void setFrequencyResponse(Complex [] _freqresp){
             _setViewFreqValue(_freqresp);
         }

         public void setFrequencySpec(double [] freq, double [] gain){
             _setViewFreqSpec(freq, gain);
         }

         public void update(Observable obs, Object arg){
             return;
         }
    }
}
