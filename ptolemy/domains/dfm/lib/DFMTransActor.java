/* A DFM actor that displays the transfer function of a IIR filter. 

 Copyright (c) 1998-1999 The Regents of the University of California.
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
import ptolemy.math.Complex;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.data.*;

import java.util.Observable;
 
//////////////////////////////////////////////////////////////////////////
//// DFMTransActor
/** 
 This DFM actor provides the transfer function view of IIR filter.  
 It has three input ports, one for numerator, one for denominator, and one for gain.
 
 An inner class, DFMTransView, is derived from ptolemy.filter.view.TransFunctView.
 <p> 
@author William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.filter.view.TransFunctView 
*/
public class DFMTransActor extends DFMActor {
    /** Constructor
     */	
    public DFMTransActor(CompositeActor container, String name)
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _numerin = new IOPort(this, "Numerator", true, false);  
        _denomin = new IOPort(this, "Denominator", true, false);  
        _gainin = new IOPort(this, "Gain", true, false);  
  
        _view = new DFMTransView();
 
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /**
     * Change the number of precision digit after the decimal point
     * parameter.  The director is check for the safe state of change
     * parameter.  The name of the parameter is "Precision Number".
     *
     * @param name name of the parameter, this case it should either
     *   "Precision Number". 
     * @param arg value of the parameter, in array of doubles
     * @return boolean value indicate if the change parameter is 
     * successful.
     */
    public boolean changeParameter(String name, Object arg){

        DFMDirector dir = (DFMDirector) getDirector();
        if (!dir.isWaitForNextIteration()) return false; 
 
        if (name.equals("Precision Number")){
            _prec = ((Integer) arg).intValue();
            _setParamChanged(true);
            dir.dfmResume();
            return true;
        }
        return false;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /**
     * Send the input poles and zeroes to the plot.
     */	
    protected void _performProcess() {
        _numerator = (double []) _getData("Numerator");
        _denominator = (double []) _getData("Denominator");
        _gain = ((Double) _getData("Gain")).doubleValue();
        _view.setTransferFunct(_numerator, _denominator, _gain); 
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private double [] _numerator;
    private double [] _denominator;
    private double _gain;
    private IOPort _numerin;
    private IOPort _denomin;
    private IOPort _gainin;
    private int _prec = 5;
    private DFMTransView _view;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // This a derived class from TransFunctView.  Null is pass for FilterObj
    // thus the derived class must make sure the method that reference
    // to FilterObj is either not used, or overloaded.

    class DFMTransView extends TransFunctView {

         public DFMTransView(){
             super(null, FilterView.FRAMEMODE, "DFM Tranfer Function View");
         }

         public void setPrec(int prec){
             this._prec = prec;
             while (!changeParameter("Precision Number", new Integer(prec))) {}
         }

         public void setTransferFunct(double [] num, double [] denom, double gain){
             _setViewTransferFunction(null, num, null, denom, null, gain);
         }

         public void update(Observable obs, Object arg){
             return;
         }
    }
}
