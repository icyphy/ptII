/* A DFM actor that displays Impulse response of a IIR filter. 

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
import ptolemy.math.Complex;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.data.*;

import java.util.Observable;
 
//////////////////////////////////////////////////////////////////////////
//// DFMImpulseActor
/** 
 This DFM actor provides the impulse response plot of IIR filter.  
 It has input port for the impulse response.
 An inner class, DFMImpulseView, is derived from ptolemy.filter.view.ImpulseView.
 <p> 
@author William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.filter.view.ImpulseView 
*/
public class DFMImpulseActor extends DFMActor {
    /** Constructor
     */	
    public DFMImpulseActor(CompositeActor container, String name)
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _impulsein = new IOPort(this, "impulse", true, false);  
  
        _view = new DFMImpulseView();
 
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /**
     * Send the input poles and zeroes to the plot.
     */	
    protected void _performProcess() {
        _impulse = (double []) _getData("impulse");
        _view.setImpulse(_impulse); 
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private double [] _impulse;
    private IOPort _impulsein;
    private DFMImpulseView _view;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // This a derived class from ImpulseView.  Null is pass for FilterObj
    // thus the derived class must make sure the method that reference
    // to FilterObj is either not used, or overloaded.

    class DFMImpulseView extends ImpulseView {

         public DFMImpulseView(){
             super(null, FilterView.FRAMEMODE,"DFM Impulse View");
         }

         public void setImpulse(double [] impulses){
             _setViewImpulse(null, impulses);
         }

         public void update(Observable obs, Object arg){
             return;
         }
    }
}
