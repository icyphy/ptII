/* Frequency domain view  
 
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
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// FreqView  
/** 
  Frequency domain view of the observed filter object.  This view is 
  specificly for the frequency domain's magnitude, magnitude in dB, 
  and phase plots of observed filter.   For each aspect of the frequency
  domain, there will be a plot associate with it.  The difference is
  that magnitude plot is an interactive plot, while others are regular plots.
  Since user can change the frequency domain spec on the magnitude plot.
  Although there is no reason why the user can't change spec on other plots,
  so this could be improvement in the future.
  <p>    
  The frequency domain spec is made up by four possible categories: edge 
  frequencies, gains at the edge frequencies, pass band and stop band ripple 
  heights.  Not all of them will be used at certain time (ripple heights will 
  not be used when designing Butterworth filter), "null" value will be passed. 
  While other new spec might need to be added later on.  These spec will be 
  represented as interactive components stored as keys in hashtables, 
  _crossref.
  <p> 
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W% %G%
  @date: 3/2/98
 */ 

public class FreqView extends PlotView {

     /**
      * Constructor.  Three plots are created, and added to the frame, 
      * if the operation mode is in FRAME MODE.
      * The three plots will be arrange in a card-layout panel.
      * The data is requested and passed to the three plots.
      * 
      * @param filter observed filter object
      * @param mode operation mode
      * @param viewname name of thie view
      */ 
     public FreqView(FilterObj filter, int mode, String viewname){
          super(viewname, filter);

          // create three plots 
          // interactive magnitude plot
          InteractPlot mgplot = new InteractPlot();   
          // noninteractive magnitude-db plot
          Plot dbplot = new Plot();
          // noninteractive phase plot
          Plot phplot = new Plot();

          _plots = new Plot[3];
          _plots[0]= mgplot;   // interactive magnitude plot
          _plots[1]= dbplot;   // magnitude db plot
          _plots[2]= phplot;   // phase plot

          // initialize these plots
          mgplot.setBackground(Color.black);
          mgplot.setForeground(Color.gray);
          mgplot.setView(this);
          mgplot.setXRange(-3.15, 3.15);
          mgplot.setYRange(-0.3, 1.3);
          mgplot.setTitle("Frequency Response: Magnitude");
          mgplot.setNumSets(5);
          mgplot.setSize(300, 300);
          dbplot.setBackground(Color.black);
          dbplot.setForeground(Color.gray);
          dbplot.setXRange(-3.15, 3.15);
          dbplot.setYRange(-1000, 20);
          dbplot.setTitle("Frequency Response: Magnitude in DB");
          dbplot.setNumSets(1);
          dbplot.setSize(300, 300);
          phplot.setBackground(Color.black);
          phplot.setForeground(Color.gray);
          phplot.setXRange(-3.15, 3.15);
          phplot.setYRange(-3.15, 3.15);
          phplot.setTitle("Frequency Response: Phase");
          phplot.setNumSets(1);
          phplot.setSize(300, 300);
          _opMode = mode;

          // create card panel to place these plots.
          _viewPanel = new FreqPlotPanel(mgplot, dbplot, phplot); 

          // create the frame if in frame mode 
          if (_opMode == Manager.FRAMEMODE){ // frame mode
              _frame  = _createViewFrame(((FilterObj) filter).getName());
              _frame.add("Center",_viewPanel);
              _frame.setSize(360, 480);
              _frame.setLocation(500, 10);
              _frame.setVisible(true);

              phplot.setVisible(true);
              phplot.init();
              dbplot.setVisible(true);
              dbplot.init();
              mgplot.setVisible(true);
              mgplot.init();
          } 

          // create three hashtables
          _crossref = new Hashtable[4];
          _crossref[0] = new Hashtable();
          _crossref[1] = new Hashtable();
          _crossref[2] = new Hashtable();
          _crossref[3] = new Hashtable();

          // get initial data and spec
          _setViewFreqValue();
          _setViewFreqSpec();

    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * frequency response data then pass them to the plots, by
     * calling <code> _setViewFreqValue() </code>.  Frequency specification
     * is also obtained by calling <code> _setViewFreqSpec() </code>.  
     * <p>
     * @param observed observed filter object
     * @param arg message passed by filter object 
     */
     public void update(Observable observed, Object arg){
          String command = (String)arg;

          if (command.equals("UpdatedFilter")){
System.out.println("updating the frequency response value");
               _setViewFreqValue();
               _setViewFreqSpec();
          }  
     }

     /**
      * Process the changing in filter spec.  Interact plot calls this function
      * after the interactive component that represent the filter spec has 
      * been moved by user's click-and-drag.  The moved interact component, ic, 
      * is passed in.  The spec that represents ic is found, and filter object
      * is notified about the changed by calling its <code> updateFreqSpec()
      * </code> method. 
      * <p>
      * @param ic moved interact component.
      */ 
     public void moveInteractComp(InteractComponent ic){
   
          // check and change the underlying spec value represented by ic 
          if (_crossref[0].containsKey(ic)){

              // edge frequencies got changed
              // create new value and replace the old one.
              Double newvalue = new Double(ic.getXValue());
              _crossref[0].put(ic, newvalue);

          } else if (_crossref[1].containsKey(ic)){

              // gain at edge frequencies got changed
              // create new value and replace the old one.
              Double newvalue = new Double(ic.getYValue());
              _crossref[1].put(ic, newvalue);
 
          } else if (_crossref[2].containsKey(ic)){

              // pass band ripple height changed
              // create new value and replace the old one.
              Double newvalue = new Double(1-ic.getYValue());
              _crossref[2].put(ic, newvalue);

          } else if (_crossref[3].containsKey(ic)){

              // pass band ripple height changed
              // create new value and replace the old one.
              Double newvalue = new Double(ic.getYValue());
              _crossref[3].put(ic, newvalue);

          }

          // reorganize all the spec, and passes them to filter object.
         
          double [] edgefreq = new double[_crossref[0].size()]; 
          double [] edgegain = new double[_crossref[1].size()]; 
          double passripple = -1; 
          double stopripple = -1; 
       
          // get edge frequencies 
          Enumeration edgeenum = _crossref[0].keys();
          while (edgeenum.hasMoreElements()){
               InteractComponent edgeic = (InteractComponent) edgeenum.nextElement();
               Double edge = (Double) _crossref[0].get(edgeic);
               edgefreq[edgeic.getDataIndexNum()] = edge.doubleValue();
          }

          // get gain at edge frequencies 
          Enumeration gainenum = _crossref[1].keys();
          while (gainenum.hasMoreElements()){
               InteractComponent gainic = (InteractComponent) gainenum.nextElement();
               Double gain = (Double) _crossref[1].get(gainic);
               edgegain[gainic.getDataIndexNum()] = gain.doubleValue();
          }

          // get pass band ripple height 
          Enumeration passrippleenum = _crossref[2].elements();
          if (passrippleenum.hasMoreElements()){
              passripple = ((Double) passrippleenum.nextElement()).doubleValue();
          }

          // get stop band ripple height 
          Enumeration stoprippleenum = _crossref[3].elements();
          if (stoprippleenum.hasMoreElements()){
              stopripple = ((Double) stoprippleenum.nextElement()).doubleValue();
          }

          // notify the filter object about the new changes. 
          FilterObj jf = (FilterObj) _observed;
          jf.updateFreqSpec(edgefreq, edgegain, passripple, stopripple);
     }

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

     // Get frequency domain data from the filter object.  
     // Magnitude, Magnitude-db and phase plots will get the new
     // data. 
     private void _setViewFreqValue(){
         FilterObj jf = (FilterObj) _observed;

         // get new frequency response data
         Complex [] freq = jf.getFrequency();
         int step = jf.getStep();
         double stepsize = 2*Math.PI/step;
     
         if (freq != null){
               
            // erase all points 
            _plots[0].eraseAllPoints(0);
            _plots[1].eraseAllPoints(0);
            _plots[2].eraseAllPoints(0);

            // update magnitude plot
            for (int i=0;i<freq.length;i++){
                // add points to plot
                if (i==0){
                    _plots[0].addPoint(0, -Math.PI+stepsize*i, freq[i].mag(), false);
                } else {
                    _plots[0].addPoint(0, -Math.PI+stepsize*i, freq[i].mag(), true);
                }
            }

            _plots[0].repaint(); 
 
            // update magnitude db plot
            for (int i=0;i<freq.length;i++){
                if (i==0){
                    _plots[1].addPoint(0, -Math.PI+stepsize*i, 
                        20*Math.log(freq[i].mag())/_log10scale, false);
                } else {
                    _plots[1].addPoint(0, -Math.PI+stepsize*i,
                        20*Math.log(freq[i].mag())/_log10scale, true);
                }
            }

            _plots[1].repaint();

            // update phase plot
            for (int i=0;i<freq.length;i++){
                 if (i==0){
                     _plots[2].addPoint(0, -Math.PI+stepsize*i, freq[i].angle(), false);
                 } else {
                     _plots[2].addPoint(0, -Math.PI+stepsize*i, freq[i].angle(), true);
                 }
            }
            _plots[2].repaint();
        }
    }

    // Get specification from filter object.  Interact components will
    // be created for these spec: edge frequencies, gain at edge frequencies,
    // and ripple heights.  These interact component's references will be
    // store in hastables, _crossref.
    // _crossref[0] : for edge frequencies
    // _crossref[1] : for gains at edge frequencies
    // _crossref[2] : for pass band ripple heights 
    // _crossref[3] : for stop band ripple heights 
    //
    private void _setViewFreqSpec(){

        FilterObj jf = (FilterObj) _observed;
        double [] edgefreq = jf.getFreqBand();
        double [] edgegain = jf.getFreqGain();
        double passrheight = jf.getFreqPassRippleHeight();
        double stoprheight = jf.getFreqStopRippleHeight();

        // erase interact components in plot
        ((InteractPlot)_plots[0]).eraseInteractComponents();

        // clear all the data points in plot
        _plots[0].eraseAllPoints(1);
        _plots[0].eraseAllPoints(2);

        if (_crossref[2].size()>0) _plots[0].eraseAllPoints(3);  
        if (_crossref[3].size()>0) _plots[0].eraseAllPoints(4);
  
        // clear hashtable
        _crossref[0].clear();
        _crossref[1].clear();
        _crossref[2].clear();
        _crossref[3].clear();


        if ((edgefreq != null) && (edgegain != null)){

            // create interact components for edge frequencies 
            for (int i=0;i<edgefreq.length;i++){
                
                InteractComponent ic;

               
                if (i<edgegain.length){
                    // edge frequencies
                    ic = new InteractComponent("Edge Frequency", 
                                            InteractComponent.LINE, edgefreq[i],
                                            edgegain[i]);
               
                } else {
                    // Band width 
                    ic = new InteractComponent("Band Width", 
                                            InteractComponent.LINE, edgefreq[i],
                                            edgegain[i-1]);

                }

                ic.setDrawingParam(Color.green, 50, false,
                                   InteractComponent.VERTICALORI);

                ic.setInteractParam(new String("Band"), 
                                    new String("Gain"), 
                                    InteractComponent.XAXISDEGFREE);

                ic.setDatasetIndex(1,i);

                // add interact component to interact magnitude plot
                InteractPlot iplot = (InteractPlot) _plots[0];
                iplot.addInteractPoint(ic, 1, ic.getXValue(), ic.getYValue(), false); 

                // set the hashtable entry
                _crossref[0].put(ic, new Double(ic.getXValue()));
            }

            // create interact components for gains at edge frequencies 
            for (int i=0;i<edgegain.length;i++){
                InteractComponent ic;
                ic = new InteractComponent("Band Gain", 
                                           InteractComponent.LINE, edgefreq[i],
                                           edgegain[i]);

                ic.setDrawingParam(Color.green, 50, false,
                                   InteractComponent.HORIZONTALORI);

                ic.setInteractParam(new String("Band"), new String("Gain"), 
                                    InteractComponent.YAXISDEGFREE);

                ic.setDatasetIndex(2,i);

                // add the interact component to interact plot
                InteractPlot iplot = (InteractPlot) _plots[0];
                iplot.addInteractPoint(ic, 2, ic.getXValue(), ic.getYValue(), false); 

                // set the hashtable entry
                _crossref[1].put(ic, new Double(ic.getYValue()));
            }
        }

        // -1 is the "null" value, if ripple height is -1, then it
        // doesn't make sense to create it..
        if (passrheight != -1) {  // pass band ripple, high ripple

            _crossref[2].clear(); 
            // create interact component for pass band ripple height 
            InteractComponent ic;

            // since passrheight is actual height, the position of the line
            // is 1-height
            ic = new InteractComponent("Pass Band Ripple Height",  
                                       InteractComponent.LINE, 0.0, 1-passrheight);

            ic.setDrawingParam(Color.green, 50, false,
                               InteractComponent.HORIZONTALORI);

            ic.setInteractParam(new String(""), new String("height"), 
                                InteractComponent.YAXISDEGFREE);

            ic.setDatasetIndex(3,0);
                
                
            // add the interact component to interact plot
            InteractPlot iplot = (InteractPlot) _plots[0];
            iplot.addInteractPoint(ic, 3, ic.getXValue(), ic.getYValue(), false); 

            // set the hashtable entry
            _crossref[2].put(ic, new Double(1.0-ic.getYValue()));
        }
 
        if (stoprheight != -1) { // stop band ripple, low ripple 
            _crossref[3].clear(); 
            InteractComponent ic;
            ic = new InteractComponent("Stop Ripple Height", 
                                       InteractComponent.LINE, 0.0, stoprheight);

            ic.setDrawingParam(Color.green, 50, false,
                               InteractComponent.HORIZONTALORI);

            ic.setInteractParam(new String(""), new String("height"), 
                                InteractComponent.YAXISDEGFREE);

            ic.setDatasetIndex(4,0);

            // add the interact component to interact plot
            InteractPlot iplot = (InteractPlot) _plots[0];
            iplot.addInteractPoint(ic, 4, ic.getXValue(), ic.getYValue(), false); 

            // set the hashtable entry
            _crossref[3].put(ic, new Double(ic.getYValue()));
        }
            
        _plots[0].repaint();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         inner class                              ////

    // Since the frequency plot requires three plots thus use a 
    // special panel that use card layout to change between three
    // panels 
    class FreqPlotPanel extends Panel implements ItemListener {

        public FreqPlotPanel(Plot p1, Plot p2, Plot p3){

            // card panel 
            this.setBackground(Color.black); 
            this.setForeground(Color.white); 
            _cp = new Panel(); 
            _cp.setBackground(Color.black); 
            _cp.setForeground(Color.white); 

            // a choice widget is used to change the card
            _c = new Choice();
            _c.addItem("Magnitude");
            _c.addItem("Magnitude in dB");
            _c.addItem("Phase");
            _c.addItemListener(this);
            _cp.add(_c);
            this.add("North", _c);
 
            _mainpanel = new Panel();
            _mainpanel.setLayout(new CardLayout());
            _mainpanel.setSize(300,380);
            _mainpanel.setBackground(Color.black); 
            _mainpanel.setForeground(Color.white); 
 
            _subpanel1 = new Panel();
            _subpanel1.setLayout(new BorderLayout(15, 15));
            _subpanel1.add("Center", p1);
            _subpanel1.setSize(300,380);
            _subpanel1.setBackground(Color.black); 
            _subpanel1.setForeground(Color.white); 
 
            _subpanel2 = new Panel();
            _subpanel2.setLayout(new BorderLayout(15, 15));
            _subpanel2.add("Center", p2);
            _subpanel2.setSize(300,380);
            _subpanel2.setBackground(Color.black); 
            _subpanel2.setForeground(Color.white); 
 
            _subpanel3 = new Panel();
            _subpanel3.setLayout(new BorderLayout(15, 15));
            _subpanel3.add("Center", p3);
            _subpanel3.setSize(300,380);
            _subpanel3.setBackground(Color.black); 
            _subpanel3.setForeground(Color.white); 
 
            _mainpanel.add("Magnitude", _subpanel1);
            _mainpanel.add("Magnitude in dB", _subpanel2);
            _mainpanel.add("Phase", _subpanel3);
            add("Center", _mainpanel);
 
            this.setSize(300, 380);
 
        }

        //
        // Handles the event choice widget.
        //
        public void itemStateChanged(ItemEvent event){
             ((CardLayout)_mainpanel.getLayout()).show(_mainpanel, _c.getSelectedItem());
        }

    

        //   private variables  //
 
        private Panel _mainpanel;
        private Panel _subpanel1;
        private Panel _subpanel2;
        private Panel _subpanel3;
        private Panel _cp;
        private Choice _c;
   }

   private static final double _log10scale = 1/Math.log(10);

}
