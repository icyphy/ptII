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

$Id$ %S%
 
*/

package ptolemy.filter.view;

import ptolemy.math.Complex; 
import ptolemy.filter.filtermodel.*;

import java.util.*;
import java.awt.*;
 
//////////////////////////////////////////////////////////////////////////
//// ImpulsView
/**
 * Impulse response plot observer.  This observer is specificly for the impulse 
 * response plot of a filter.
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */

public class ImpulsView extends View {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor.
     */ 
    public ImpulsView(){
          InteractPlot plot = new InteractPlot();
          _plots = new Plot[1];
          _plots[0]=plot;
          _opMode = 0;
          _frame = new Frame("");
    }


    /**
     * Constructor.  Plot is created, then added to the frame.  Data is
     * requested from the filter and passed to the plot.
     */ 
    public ImpulsView(FilterObj filter, int mode, String name){
          InteractPlot plot = new InteractPlot();
          plot.setTitle("Impulse Response");
          plot.setXRange(0, 200);
          plot.setYRange(-0.5, 0.5);
          plot.setNumSets(2);
          plot.setView(this);
          plot.resize(300,300);
          _plots = new Plot[1];
          _plots[0]=plot;
          _opMode = mode;
          _viewPanel = new Panel();
          _viewPanel.resize(300,350);
          _viewPanel.add("Center", plot);
          if (_opMode == 0){ // frame mode
             //  _frame = new ImpulsPlotFrame(name, plot);
              _frame = new Frame(name);
              _frame.add("Center", _viewPanel);
              _frame.resize(300,350);
              _frame.show();
              plot.init();
          } 
          _observed = filter;
          update(_observed, "UpdatedFilter");
     }  



   

     /**
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * impulse response then pass them to the plot.
     */
     public void update(Observable o, Object arg){
          String command = (String)arg;
          if (command.equals("UpdatedFilter")){
              FilterObj jf = (FilterObj) _observed;
              Complex [] data = jf.getImpulse();
              Complex [] data2 = jf.getImpulse2();
              // double [] data = jf.getImpulse();
              if (data != null){
                  _plots[0].eraseAllPoints(0);
                  _plots[0].eraseAllPoints(1);
                  for (int i=0;i<data.length;i++){
                   //   _plots[0].addPoint(0, i, data[i].re(), false); 
                  //    _plots[0].addPoint(1, 2*i+1, data2[i].abs(), false); 
                  //    _plots[0].addPoint(1, 2*i+1, 0, true); 
                  //    _plots[0].addPoint(0, i, data[i], false); 
                      _plots[0].addPoint(1, 2*i+1, data[i].im(), false); 
                      _plots[0].addPoint(1, 2*i+1, 0, true); 
                  //    _plots[0].addPoint(0, 2*i, data[i].abs(), false); 
                      _plots[0].addPoint(0, 2*i, data[i].re(), false); 
                      _plots[0].addPoint(0, 2*i, 0, true); 
                  }
              }
              _plots[0].repaint();            
          }
     }

     /**
      * New changes have been made on the plot.  View
      * passes the new data to the filter.
      * This method might no longer needed, since it is
      * unlikely to modify the impulse response. 
      */
     public void newChange(Vector data){
          FilterObj jf = (FilterObj) _observed;
          jf.receive(3,"Update", data);
     }

}
