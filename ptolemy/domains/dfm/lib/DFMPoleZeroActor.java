/* A DFM  actor that displays pole-zero of a IIR filter. 

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
//// DFMPoleZeroActor
/** 
 This DFM actor provides the pole-zero plot of IIR filter.  
 It has two inputs, one for pole, one for zero.  Both in the array of complex.
 An inner class, DFMPoleZeroView, is derived from ptolemy.filter.view.PoleZeroView.
 <p> 
@author William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
@see ptolemy.filter.view.PoleZeroView 
*/
public class DFMPoleZeroActor extends DFMActor {
    /** Constructor
     */	
    public DFMPoleZeroActor(CompositeActor container, String name)
                      throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _polein = new IOPort(this, "pole", true, false);  
        _zeroin = new IOPort(this, "zero", true, false);  
  
        _view = new DFMPoleZeroView();
 
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /**
     * Send the input poles and zeroes to the plot.
     */	
    protected void _performProcess() {

        _pole = (Complex []) _getData("pole");
        _zero = (Complex []) _getData("zero");
        _view.setPoleZero(_pole, _zero); 

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
 
    private Complex [] _pole;
    private Complex [] _zero;
    private IOPort _polein;
    private IOPort _zeroin;
    DFMPoleZeroView _view;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    // This a derived class from PoleZeroView.  Null is pass for FilterObj
    // thus the derived class must make sure the method that reference
    // to FilterObj is either not used, or overloaded.

    class DFMPoleZeroView extends PoleZeroView {

         public DFMPoleZeroView(){
             super(null, FilterView.FRAMEMODE, "DFM Pole Zero View");
             ((InteractPlot) _plots[0]).setEditPermission(false); 
         }

         public void moveInteractComp(InteractComponent ic){
             return;
         }

         public void selectInteractComp(InteractComponent ic){
             return;
         }

         public void setPoleZero(Complex [] pole, Complex [] zero){
             _setViewPoleZero(pole, zero);
         }

         public void unselectInteractComp(InteractComponent ic){
             return;
         }
    
         public void update(Observable obs, Object arg){
             return;
         }
    
    }
}
