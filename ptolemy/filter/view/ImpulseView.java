/* Impulse response view object
 
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

package ptolemy.filter.view;

import ptolemy.math.Complex; 
import ptolemy.filter.filtermodel.FilterObj;
import ptolemy.filter.controller.Manager;

import java.util.*;
import java.awt.*;
 
//////////////////////////////////////////////////////////////////////////
//// ImpulseView
/**
  Impulse response view of the observed filter object.  This view 
  is specificly for the impulse response plot of a filter.  Currently
  the impulse response is not interactive, since it doesn't make sense
  to change the impulse response during IIR filter design process (FIR
  is not yet supported).  However the plot is still an InteractivePlot
  class, so later on this class can be modified to design FIR filter.
 
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W% %G%
  @date: 3/2/98
 */

public class ImpulseView extends PlotView {

    /**
     * Constructor.  Plot is created, then added to the frame, if the operation
     * mode is frame mode.  Data is requested from the filter and passed to 
     * the plot by calling <code> update() </code> method.
     *
     * @param filter observed filter object
     * @param mode operation mode
     * @param viewname name of thie view
     */ 
    public ImpulseView(FilterObj filter, int mode, String viewname){
          super(viewname, filter);

          // still interact plot
          InteractPlot plot = new InteractPlot();
          plot.setBackground(Color.black);
          plot.setForeground(Color.gray);
          plot.setTitle("Impulse Response");
          plot.setXRange(0, 200);
          plot.setYRange(-0.5, 0.5);
          plot.setNumSets(2);
          plot.setView(this);
          plot.setSize(300, 300);
          _plots = new Plot[1];
          _plots[0]=plot;

          _opMode = mode;

          // set the panel to place the plot
          _viewPanel = new Panel();
          _viewPanel.setSize(300,350);
          _viewPanel.add("Center", plot);
          _viewPanel.setBackground(Color.black); 
          _viewPanel.setForeground(Color.white); 

          if (_opMode == Manager.FRAMEMODE){ // frame mode
              _frame = new Frame(((FilterObj)filter).getName());
              _frame.add("Center", _viewPanel);
              _frame.setSize(300,350);
              _frame.setLocation(300,10);
              _frame.setVisible(true);
              plot.init();
          }

          // get initial data 
          _setViewImpulse();
     }  


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

     /**
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * impulse response then pass them to the plot.
     * <p>
     * Since the impulse response in digital signal is represented by
     * a sequence of impulses (vertical lines).  So the impulse plots
     * will have vertical line segments.
     * <p>
     * Impulse response could be complex so there will be two impulses for
     * each impulse response data point, one for the real part and one for
     * imaginary part. 
     * <p>
     * @param observed observed filter object
     * @param arg message passed by filter object
     */
     public void update(Observable observed, Object arg){
          String command = (String)arg;
          if (command.equals("UpdatedFilter")){
              _setViewImpulse();
          }
     }

     /**
      * New changes have been made on the plot.  View passes the new 
      * data to the filter.
      * <p>
      * So currently it does not perform any operation.      
      * This method is only needed at FIR filter design, since it is
      * unlikely to modify the impulse response during IIR design. 
      */
     public void moveInteractComp(InteractComponent ic){
          return;
     }

     //////////////////////////////////////////////////////////////////////////
     ////                     private methods                              ////

     private void _setViewImpulse(){
         FilterObj jf = (FilterObj) _observed;
         Complex [] complexdata = null;
         double [] realdata = null;
         if (jf.getType() == ptolemy.math.filter.Filter.BLANK){
             complexdata = jf.getComplexImpulse();
         } else {    
             realdata = jf.getRealImpulse();
         }

         // double [] data = jf.getImpulse();
         if ((complexdata != null) || (realdata != null)){
             _plots[0].eraseAllPoints(0);
             _plots[0].eraseAllPoints(1);
             if (complexdata != null){
                 for (int i=0;i<complexdata.length;i++){
                     // create two vertical line segment, one for real part,
                     // one for imaginary part
                     _plots[0].addPoint(1, 2*i+1, complexdata[i].imag, false); 
                     _plots[0].addPoint(1, 2*i+1, 0, true); 
                     _plots[0].addPoint(0, 2*i, complexdata[i].real, false); 
                     _plots[0].addPoint(0, 2*i, 0, true); 
                 }
             } else {
                 for (int i=0;i<realdata.length;i++){
                     // create two vertical line segment, one for real part,
                     // one for imaginary part
                     _plots[0].addPoint(0, i, realdata[i], false); 
                     _plots[0].addPoint(0, i, 0, true); 
                 }
             }
         }
         _plots[0].repaint();            
     }


}
