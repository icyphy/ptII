/* Frequency domain view object 
 
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

$Id$
 
*/

package ptolemy.filter.view;
 
import ptolemy.math.Complex; 
import ptolemy.filter.filtermodel.*;
import java.util.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// FreqView  
/** 
 * Frequency domain plot observer.  This observer is specificly for the  
 * frequency domain's magnitude, magnitude in dB, and phase plots of a filter.  
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */ 

public class FreqView extends View {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor
     */ 
    public FreqView(){
          InteractPlot mgplot = new InteractPlot();
          Plot dbplot = new Plot();
          Plot phplot = new Plot();
          _plots = new Plot[3];
          _plots[0]= mgplot;
          _plots[1]= dbplot;
          _plots[2]= phplot;
          mgplot.setView(this);
          mgplot.setXRange(-3.15, 3.15);
          mgplot.setYRange(-0.5, 1.5);
          mgplot.setTitle("Frequency Response: Magnitude");
          mgplot.setNumSets(2);
          dbplot.setXRange(-3.15, 3.15);
          dbplot.setYRange(-100, 100);
          dbplot.setTitle("Frequency Response: Magnitude in DB");
          dbplot.setNumSets(1);
          phplot.setXRange(-3.15, 3.15);
          phplot.setYRange(-3.15, 3.15);
          phplot.setTitle("Frequency Response: Phase");
          phplot.setNumSets(1);
          
          _opMode = 0;
          _observed = null;

          _viewPanel = new FreqPlotPanel(mgplot, dbplot, phplot); 
          _frame  = new Frame("");
          _frame.add("Center",_viewPanel);
          _frame.setSize(550,550);
          _frame.show();
     }

     /**
      * Constructor.  Three plots are created, and added to the frame.
      * The data is requested and passed to the three plots.
      */ 
     public FreqView(FilterObj filter, int mode, String name){
          InteractPlot mgplot = new InteractPlot();
          Plot dbplot = new Plot();
          Plot phplot = new Plot();

          _plots = new Plot[3];
          _plots[0]= mgplot;
          _plots[1]= dbplot;
          _plots[2]= phplot;

          mgplot.setView(this);
          mgplot.setXRange(-3.15, 3.15);
          mgplot.setYRange(-0.5, 1.5);
          mgplot.setTitle("Frequency Response: Magnitude");
          mgplot.setNumSets(3);
          mgplot.resize(300,300);
          dbplot.setXRange(-3.15, 3.15);
          dbplot.setYRange(-1000, 20);
     //     dbplot.setYLog(true);
          dbplot.setTitle("Frequency Response: Magnitude in DB");
          dbplot.setNumSets(1);
          dbplot.resize(300,300);
          phplot.setXRange(-3.15, 3.15);
          phplot.setYRange(-3.15, 3.15);
          phplot.setTitle("Frequency Response: Phase");
          phplot.setNumSets(1);
          phplot.resize(300,300);
          _opMode = mode;
          _viewPanel = new FreqPlotPanel(mgplot, dbplot, phplot); 

          if (_opMode == 0){ // frame mode
              _frame  = new Frame(name);
              _frame.add("Center",_viewPanel);
              _frame.resize(360, 370);
              _frame.show();

              phplot.show();
              phplot.init();
              dbplot.show();
              dbplot.init();
              mgplot.show();
              mgplot.init();
          } 

          _observed = filter;
 
          update(_observed, "UpdatedFilter");

     }  
    
    /**
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * frequency response data then pass them to the plots.
     */
     public void update(Observable o, Object arg){
          String command = (String)arg;
          FilterObj jf = (FilterObj) _observed;
          if (command.equals("UpdatedFilter")){

               // get new data
               Complex [] freq = jf.getFrequency();
               double [] freqdb = jf.getFrequencyDB();
               double [] edgefreq = jf.getFreqBand();
               double [] edgegain = jf.getFreqGain();
               double [] rheight = jf.getFreqRippleHeight();
               double step = jf.getStep();
              
               if (freq == null) System.out.println("frequency response is null");  
               if (freq != null){
                   
                   _plots[0].eraseAllPoints(0);
                   _plots[0].repaint();
                   _plots[1].eraseAllPoints(0);
                   _plots[2].eraseAllPoints(0);
                  // update mag plot
                   for (int i=0;i<freq.length;i++){
                       if (i==0){
                            _plots[0].addPoint(0, -Math.PI+step*i, freq[i].abs(), false);
                       } else {
                            _plots[0].addPoint(0, -Math.PI+step*i, freq[i].abs(), true);
                       }
                   }
                   _plots[0].repaint(); 
 
                  // update mag db plot
                   for (int i=0;i<freqdb.length;i++){
                       if (i==0){
                            _plots[1].addPoint(0, -Math.PI+step*i, freqdb[i], false);
                       } else {
                            _plots[1].addPoint(0, -Math.PI+step*i, freqdb[i], true);
                       }
                   }
 
                   _plots[1].repaint();
                  // update pha plot
                   for (int i=0;i<freq.length;i++){
                       if (i==0){
                          _plots[2].addPoint(0, -Math.PI+step*i, freq[i].angle(), false);
                       } else {
                          _plots[2].addPoint(0, -Math.PI+step*i, freq[i].angle(), true);
                       }
                   }

                   _plots[2].repaint();
            
                   _crossref = new InteractObjCrossRef[3][]; 
                   // update the edge frequencies 
                   if (edgefreq != null){
                       _crossref[0] = new InteractObjCrossRef[edgefreq.length]; 
                       _plots[0].eraseAllPoints(1);
                       _plots[0].eraseAllPoints(2);
                       ((InteractPlot)_plots[0]).eraseInteractComponents();
                   
                       for (int i=0;i<edgefreq.length;i++){
                           InteractComponent ic;

                           ic = new InteractComponent("Edge Frequency", 
                                                      InteractComponent.Line);

                           ic.setDrawingParam(Color.blue, 50, false,
                                              InteractComponent.VerticalOri);

                           ic.setInteractParam(new String("Band"), 
                                               new String("Gain"), 
                                               InteractComponent.XaxisDegFree);

                           ic.setDatasetIndex(1,i);
                           ic.xv = edgefreq[i];
                           
                           if (i<edgegain.length){
                               ic.yv = edgegain[i];
                           } else { 
                               ic.yv = edgegain[i-1];
                           }

                           InteractPlot iplot = (InteractPlot) _plots[0];
                           iplot.addInteractPoint(ic, 1, ic.xv, ic.yv, false); 

                           _crossref[0][i] = new InteractObjCrossRef();
                           _crossref[0][i].interactObj = ic;
                           _crossref[0][i].dataObj = (Object) new Double(ic.xv);
                 
                       }

                       _crossref[1] = new InteractObjCrossRef[edgegain.length]; 
                       for (int i=0;i<edgegain.length;i++){
                           InteractComponent ic;
                           ic = new InteractComponent("Band Gain", 
                                                      InteractComponent.Line);

                           ic.setDrawingParam(Color.blue, 50, false,
                                              InteractComponent.HorizontalOri);

                           ic.setInteractParam(new String("Band"), 
                                               new String("Gain"), 
                                               InteractComponent.YaxisDegFree);

                           ic.xv = edgefreq[i];
                           ic.yv = edgegain[i];

                           ic.setDatasetIndex(2,i);

                           InteractPlot iplot = (InteractPlot) _plots[0];
                           iplot.addInteractPoint(ic, 2, ic.xv, ic.yv, false); 
                           _crossref[1][i] = new InteractObjCrossRef();
                           _crossref[1][i].interactObj = ic;
                           _crossref[1][i].dataObj = (Object) new Double(ic.yv);
                       }

                       _crossref[2] = new InteractObjCrossRef[rheight.length]; 
                       if (rheight[0] != -1) {  // pass band ripple, high ripple
                           InteractComponent ic;
                           ic = new InteractComponent("Pass Band Ripple Height", 
                                                       InteractComponent.Line);

                           ic.setDrawingParam(Color.darkGray, 50, false,
                                              InteractComponent.HorizontalOri);

                           ic.setInteractParam(new String(""), 
                                               new String("height"), 
                                               InteractComponent.YaxisDegFree);

                           ic.xv = 0.0;
                           ic.yv = 1.0 - rheight[0];
                           ic.setDatasetIndex(3,0);
                           InteractPlot iplot = (InteractPlot) _plots[0];
                           iplot.addInteractPoint(ic, 3, ic.xv, ic.yv, false); 
                           _crossref[2][0] = new InteractObjCrossRef();
                           _crossref[2][0].interactObj = ic;
                           _crossref[2][0].dataObj = (Object) new Double(ic.yv);
                       }
 
                       if (rheight[1] != -1) { // stop band ripple, low ripple 
                           InteractComponent ic;
                           ic = new InteractComponent("Stop Ripple Height", 
                                     InteractComponent.Line);

                           ic.setDrawingParam(Color.orange, 50, false,
                                     InteractComponent.HorizontalOri);

                           ic.setInteractParam(new String(""), 
                            new String("height"), InteractComponent.YaxisDegFree);

                           ic.xv = 0.0;
                           ic.yv = rheight[1];
                           ic.setDatasetIndex(4,0);
                           InteractPlot iplot = (InteractPlot) _plots[0];
                           iplot.addInteractPoint(ic, 4, ic.xv, ic.yv, false); 
                           _crossref[2][1] = new InteractObjCrossRef();
                           _crossref[2][1].interactObj = ic;
                           _crossref[2][1].dataObj = (Object) new Double(ic.yv);
                       }
 
                       _plots[0].repaint();
                   }    
               }

          }
     }

     public void moveInteractComp(InteractComponent ic){
          double [] edgefreq = new double[_crossref[0].length]; 
          for (int i=0;i<_crossref[0].length;i++){
               if (ic == _crossref[0][i].interactObj){
                     _crossref[0][i].dataObj = (Object) new Double(ic.xv);
                     break;
               }    
               edgefreq[i] = ((Double) _crossref[0][i].dataObj).doubleValue();
          }   

          double [] edgegain = new double[_crossref[1].length]; 
          for (int i=0;i<_crossref[1].length;i++){
               if (ic == _crossref[1][i].interactObj){
                     _crossref[1][i].dataObj = (Object) new Double(ic.yv);
                     break;
               }    
               edgegain[i] = ((Double) _crossref[1][i].dataObj).doubleValue();
          }
 
          double passripple; 
          if (ic == _crossref[2][0].interactObj){
               _crossref[2][0].dataObj = (Object) new Double(1.0-ic.yv);
          }
          passripple = ((Double) _crossref[2][0].dataObj).doubleValue();

          double stopripple; 
          if (ic == _crossref[2][1].interactObj){
               _crossref[2][1].dataObj = (Object) new Double(ic.yv);
          }
          stopripple = ((Double) _crossref[2][1].dataObj).doubleValue();

          FilterObj jf = (FilterObj) _observed;
          jf.updateBandValue(edgefreq, edgegain, passripple, stopripple);
     }
}

// Since the frequency plot requires three plots thus use a 
// special panel that use card layout to change between three
// panels 
class FreqPlotPanel extends Panel {


    public FreqPlotPanel(Plot p1, Plot p2, Plot p3){


      // card panel 
        _cp = new Panel(); 
      // a choice widget is used to change the card
        _c = new Choice();
        _c.addItem("Magnitude");
        _c.addItem("Magnitude in dB");
        _c.addItem("Phase");
        _cp.add(_c);
        this.add("North", _c);
 
        _mainpanel = new Panel();
        _mainpanel.setLayout(new CardLayout());
        _mainpanel.resize(300,350);
 
        _subpanel1 = new Panel();
        _subpanel1.setLayout(new BorderLayout(15, 15));
        _subpanel1.add("Center", p1);
        _subpanel1.resize(300,350);
 
        _subpanel2 = new Panel();
        _subpanel2.setLayout(new BorderLayout(15, 15));
        _subpanel2.add("Center", p2);
        _subpanel2.resize(300,350);
 
        _subpanel3 = new Panel();
        _subpanel3.setLayout(new BorderLayout(15, 15));
        _subpanel3.add("Center", p3);
        _subpanel3.resize(300,350);
 
        _mainpanel.add("Magnitude", _subpanel1);
        _mainpanel.add("Magnitude in dB", _subpanel2);
        _mainpanel.add("Phase", _subpanel3);
        add("Center", _mainpanel);
 
        this.resize(300, 350);
 
    }

   /**
    * Handles the event from menu and choice widget.
    */
   public boolean action(Event event, Object arg){
       if (event.target instanceof Choice) {   // change the card
            ((CardLayout)_mainpanel.getLayout()).show(_mainpanel,(String)arg);
            return true;
        }
       return false;
   }

    

   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////
 
     private Panel _mainpanel;
     private Panel _subpanel1;
     private Panel _subpanel2;
     private Panel _subpanel3;
     private Panel _cp;
     private Choice _c;

}

