/* A DFM Source actor that provides the IIR filter design parameter. 

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
import ptolemy.math.filter.Filter;
import ptolemy.filter.view.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.data.*;

import java.util.Observable;
 
//////////////////////////////////////////////////////////////////////////
//// DFMIIRParamActor
/** 
 This DFM actor provides the IIR filter design parameters.
 It contains a class that is derived from ptolemy.filter.view.IIRFiltSetView.
 <p> 
@author William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.filter.view.IIRFiltSetView 
*/
public class DFMIIRParamActor extends DFMActor {
    /** Constructor
     */	
    public DFMIIRParamActor(CompositeActor container, String name)
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _analogdesignout = new IOPort(this, "analogDesignMethod", false, true);  
        _bandtypeout = new IOPort(this, "bandType", false, true);  
  
        _view = new DFMIIRParamView();
 
        _setSource(); 
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Change the analog filter design method and band type parameters.
     * This will result in refiring of the director and new filter will 
     * be designed.  The parameter value should follow the enum in Filter.
     * @param name name of the parameter, this case it should be "AnalogDesignMethod"
     *       or "BandType".
     * @param arg value of the parameter, this case, it should be type Integer  
     * @return boolean value indicate if the change parameter is 
     * successful.
     */
     public boolean changeParameter(String name, Object arg){
        DFMDirector dir = (DFMDirector) getDirector();
        if (!dir.isWaitForNextIteration()) return false; 
 
        if (name.equals("AnalogDesignMethod")){
            _analogdesign = ((Integer) arg).intValue();
            _setParamChanged(true);
            dir.dfmResume();
            return true;
        } else if (name.equals("BandType")){
            _bandtype = ((Integer) arg).intValue();
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

    /** Produce two outputs, one for analog design method, and one
     * for band type. 
     *
     */	
    protected void _performProcess() {

         DFMIntToken analogdesignout = new DFMIntToken("New", _analogdesign);
         _outputTokens.put("analogDesignMethod", analogdesignout);
         DFMIntToken bandtypeout = new DFMIntToken("New", _bandtype);
         _outputTokens.put("bandType", bandtypeout);

    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private int _analogdesign = Filter.BUTTERWORTH; 
    private int _bandtype = Filter.LOWPASS; 
    private IOPort _analogdesignout;
    private IOPort _bandtypeout;
    private DFMIIRParamView _view;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // This a derived class from IIRFiltSetView.  Null is pass for FilterObj
    // thus the derived class must make sure the method that reference
    // to FilterObj is either not used, or overloaded.

    class DFMIIRParamView extends IIRFiltSetView {
         public DFMIIRParamView(){
             super(null, FilterView.FRAMEMODE, "DFM IIR Parameter View");
         }

         public void update(Observable obs, Object arg){
             return;
         }

        /** 
         * The parameter is modified on the panel.  This method notify the viewer 
         * about the new changes made on the dialog.  
         * The new parameter is sent to FilterObj.
         */
         protected void _newIIRParamChange(int approx, int mapmethod, int bandtype, double fs){
System.out.println("IIR param View: analog: "+approx+" band: "+bandtype); 
             if (approx != _analogdesign){
                 while (!changeParameter("AnalogDesignMethod", new Integer(approx))) {} 
             }

             if (bandtype != _bandtype){
                 while (!changeParameter("BandType", new Integer(bandtype))) {} 
             }
             
         }

    }
}
