/* Numerator and Denominator view of the filter

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

$Id$ %S%

*/

package ptolemy.filter.view;

import ptolemy.filter.filtermodel.*;

import java.util.*;
import java.awt.*;
import java.applet.*;

//////////////////////////////////////////////////////////////////////////
//// NumerDenomView
/** 
  A view that describe the filter as a transfer function.  It has
  scrollable panel thas displayed the transfer function
@William Wu
@version 0.1

*/
public class NumerDenomView extends View{
    /** Constructor
     * The panel is created, then added to frame if necessary.
     * Data is requested from filter, and pass to the panel. 
     * 
     * @param filter filter object's reference 
     * @param mode mode of environment: 0 for frame, 1 for applet
     * @param name name of filter. 
     * @return nothing
     * @exception full-classname description
     */	
    public NumerDenomView(FilterObj filter, int mode, String name) {
          _viewPanel = new NumerDenomPanel();
          _opMode = mode;
 
          _viewPanel.resize(300,300);
          if (_opMode == 0){ // frame mode
             //  _frame = new ImpulsPlotFrame(name, plot);
              _frame = new Frame(name);
              _frame.add("Center", _viewPanel);
              _frame.resize(300,300);
              _frame.show();
          }
          _observed = filter;
          update(_observed, "UpdatedFilter");
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Description
     * Update:
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * numerator, denominator, and gain then pass them to the panel.
     *
     * @param o observable object -- the filter 
     * @param arg message from the filter  
     */	
    public void update(Observable o, Object arg) {
          String command = (String)arg;
          if (command.equals("UpdatedFilter")){
              FilterObj jf = (FilterObj) _observed;

              // get numerators
              double [] numer = jf.getNumerator();

              // get numerators
              double [] denom = jf.getDenominator();

              // get gain 
              double gain = jf.getGain();

              if ((numer != null) && (denom != null)) {
                  ((NumerDenomPanel) _viewPanel).updatePanel(gain, numer, denom);         
              }
          }

    }


}
 

class NumerDenomPanel extends Panel {
    
    public NumerDenomPanel(){
         setBackground(Color.white);
    }

    public void updatePanel(double g, double [] num, double [] den){
          
    }

    private String _chopPrec(String input, int proce){

         int pt = input.indexOf(".");
         if ((pt!=-1){
              return  

          
    private StringBuffer _numer;
    private StringBuffer _denom;
    private String _gain;
    private Panel _scribbleP;
    
}

