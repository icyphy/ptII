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
  The frequency domain spec is made up by two possible categories: edge
  frequencies, and gains at the edge frequencies.  Pass band and stop band ripple
  heights are represented by gain values.
  <p>
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
          mgplot.setBackground(_plotBack);
          mgplot.setForeground(_plotFore);
          mgplot.setView(this);
          mgplot.setXRange(-3.15, 3.15);
          mgplot.setYRange(-0.3, 1.3);
          mgplot.setTitle("Frequency Response: Magnitude");
          mgplot.setNumSets(3);
          mgplot.setSize(300, 300);
          dbplot.setBackground(_plotBack);
          dbplot.setForeground(_plotFore);
          dbplot.setXRange(-3.15, 3.15);
          dbplot.setYRange(-1000, 20);
          dbplot.setTitle("Frequency Response: Magnitude in DB");
          dbplot.setNumSets(1);
          dbplot.setSize(300, 300);
          phplot.setBackground(_plotBack);
          phplot.setForeground(_plotFore);
          phplot.setXRange(-3.15, 3.15);
          phplot.setYRange(-3.15, 3.15);
          phplot.setTitle("Frequency Response: Phase");
          phplot.setNumSets(1);
          phplot.setSize(300, 300);
          _opMode = mode;

          // create card panel to place these plots.
          _viewPanel = new FreqPlotPanel(mgplot, dbplot, phplot);

          // create the frame if in frame mode
          if (_opMode == FilterView.FRAMEMODE){ // frame mode
              String name = new String("");
              if (filter != null) name = filter.getName();

              _frame  = _createViewFrame(name);
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

          // create two hashtables, one for frequencies, one for gains
          _crossref = new Hashtable[2];
          _crossref[0] = new Hashtable();
          _crossref[1] = new Hashtable();

          // get initial data and spec
          if (filter != null){
              Complex [] freqresponse = filter.getFrequency();
              _setViewFreqValue(freqresponse);
              double [] edgefreq = filter.getFreqBand();
              double [] edgegain = filter.getFreqGain();
              _setViewFreqSpec(edgefreq, edgegain);
          }
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
              FilterObj filter = (FilterObj) _observed;
              Complex [] freqresponse = filter.getFrequency();
              _setViewFreqValue(freqresponse);
              double [] edgefreq = filter.getFreqBand();
              double [] edgegain = filter.getFreqGain();
              _setViewFreqSpec(edgefreq, edgegain);
          }
     }

     public void deleteInteractComp(InteractComponent ic){
          return;
     }

     public void selectInteractComp(InteractComponent ic){
          return;

     }

     public void unselectInteractComp(InteractComponent ic){
          return;

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

          // reorganize all the spec, and passes them to filter object.
          _changeFreqSpecValue(ic);

          // get edge frequencies
          double [] edgefreq = _getEdgeFrequencies();
          // get gain at edge frequencies
          double [] edgegain = _getEdgeGains();

          FilterObj jf = (FilterObj) _observed;
          jf.updateFreqSpec(edgefreq, edgegain);
     }

    //////////////////////////////////////////////////////////////////////////
    ////                       protected methods                          ////

     /** Change the underlying spec data with the given InteractComponent.
      * The interact component repsents the critical frequencies, and
      * gain at critical frequencies.  The value that corresponding to
      * that interact component in the hashtable will be changed.
      * @param ic the InteractComponent to be changed
      */
     protected void _changeFreqSpecValue(InteractComponent ic){
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

          }
     }

     /** Retrieve the critical frequencies from the cross reference hashtable.
      *  The edge frequencies are stored in an array of double.
      * @return array of double contains the edge frequencies
      */
     protected double [] _getEdgeFrequencies(){

          double [] edgefreq = new double[_crossref[0].size()];
          int ind = 0;
          Enumeration edgeenum = _crossref[0].keys();
          while (edgeenum.hasMoreElements()){
               InteractComponent edgeic = (InteractComponent) edgeenum.nextElement();
               Double edge = (Double) _crossref[0].get(edgeic);
               edgefreq[edgeic.getDataIndex()] = edge.doubleValue();
          }
          return edgefreq;
     }

     /** Retrieve the critical gains from the cross reference hashtable.
      *  The edge gains are stored in an array of double.
      * @return array of double contains the edge gains.
      */
     protected double [] _getEdgeGains(){

          double [] edgegain = new double[_crossref[1].size()];
          Enumeration gainenum = _crossref[1].keys();
          while (gainenum.hasMoreElements()){
               InteractComponent gainic = (InteractComponent) gainenum.nextElement();
               Double gain = (Double) _crossref[1].get(gainic);
               edgegain[gainic.getDataIndex()] = gain.doubleValue();
          }
          return edgegain;
     }


     /** Get frequency domain data that is obtained from the filter object.
      * Magnitude, Magnitude-db and phase plots will get the new
      * data.
      * @param freq frequency response data.
      */
     protected void _setViewFreqValue(Complex [] freq){

         double stepsize = 2*Math.PI/freq.length;

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

   /**
    * Change the filter spec on the graph.  Interact components will
    * be created for these spec: edge frequencies, gain at edge frequencies.
    * These interact component's references will be
    * store in hastables, _crossref.
    * _crossref[0] : for edge frequencies
    * _crossref[1] : for gains at edge frequencies
    * @param edgefreq array of double contains edge frequencies, the array length = 2 for
    * lowpass/highpass, the array length = 3 for bandstop/bandpass
    * @param edgegain array of double contains edge gains, the array length = 2
    */
    protected void _setViewFreqSpec(double [] edgefreq, double [] edgegain){

        // erase interact components in plot
        ((InteractPlot)_plots[0]).eraseInteractComponents();

        // clear all the data points in plot
        _plots[0].eraseAllPoints(1);
        _plots[0].eraseAllPoints(2);

        // clear hashtable
        _crossref[0].clear();
        _crossref[1].clear();


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

                double minX, maxX;

                if (i == 0) {
                    minX = _minGap;
                    maxX = edgefreq[1]-_minGap;
                } else if (i == edgefreq.length-1){
                    minX = edgefreq[i-1]+_minGap;
                    maxX = Math.PI-_minGap;
                } else {
                    minX = edgefreq[i-1]+_minGap;
                    maxX = edgefreq[i+1]-_minGap;
                }

                ic.setBoundary(minX, maxX, Double.MIN_VALUE, Double.MAX_VALUE);

                ic.setDrawingParam(Color.green, 50, false,
                                   InteractComponent.VERTICALORI);

                ic.setInteractParam(new String("Band"),
                                    new String("Gain"),
                                    InteractComponent.XAXISDEGFREE);

                // add interact component to interact magnitude plot
                InteractPlot iplot = (InteractPlot) _plots[0];
                iplot.addInteractPoint(ic, 1, i, ic.getXValue(), ic.getYValue(), false);

                // set the hashtable entry
                _crossref[0].put(ic, new Double(ic.getXValue()));
            }

            // create interact components for gains at edge frequencies
            for (int i=0;i<edgegain.length;i++){
                InteractComponent ic;
                ic = new InteractComponent("Band Gain",
                                           InteractComponent.LINE, edgefreq[i],
                                           edgegain[i]);

                double minY, maxY;


                if (i == 0) {
                    if (edgegain[0] < edgegain[1]){
                        minY = 0.0;
                        maxY = edgegain[1]-_minGap;
                    } else {
                        minY = edgegain[1]+_minGap;
                        maxY = 1.0;
                    }
                } else {
                    if (edgegain[0] < edgegain[1]){
                        minY = edgegain[0]+_minGap;
                        maxY = 1.0;
                    } else {
                        minY = 0.0;
                        maxY = edgegain[1]-_minGap;
                    }
                }

                ic.setBoundary(Double.MIN_VALUE, Double.MAX_VALUE, minY, maxY);

                ic.setDrawingParam(Color.green, 50, false,
                                   InteractComponent.HORIZONTALORI);

                ic.setInteractParam(new String("Band"), new String("Gain"),
                                    InteractComponent.YAXISDEGFREE);

                // add the interact component to interact plot
                InteractPlot iplot = (InteractPlot) _plots[0];
                iplot.addInteractPoint(ic, 2, i, ic.getXValue(), ic.getYValue(), false);

                // set the hashtable entry
                _crossref[1].put(ic, new Double(ic.getYValue()));
            }
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
            this.setBackground(_plotBack);
            this.setForeground(_plotFore);
            _cp = new Panel();
            _cp.setBackground(_plotBack);
            _cp.setForeground(_plotFore);

            // a choice widget is used to change the card
            _c = new Choice();
            _c.setBackground(_plotBack);
            _c.setForeground(_plotText);
            _c.addItem("Magnitude");
            _c.addItem("Magnitude in dB");
            _c.addItem("Phase");
            _c.addItemListener(this);
            _cp.add(_c);
            this.add("North", _c);

            _mainpanel = new Panel();
            _mainpanel.setLayout(new CardLayout());
            _mainpanel.setSize(300,380);
            _mainpanel.setBackground(_plotBack);
            _mainpanel.setForeground(_plotFore);

            _subpanel1 = new Panel();
            _subpanel1.setLayout(new BorderLayout(15, 15));
            _subpanel1.add("Center", p1);
            _subpanel1.setSize(300,380);
            _subpanel1.setBackground(_plotBack);
            _subpanel1.setForeground(_plotFore);

            _subpanel2 = new Panel();
            _subpanel2.setLayout(new BorderLayout(15, 15));
            _subpanel2.add("Center", p2);
            _subpanel2.setSize(300,380);
            _subpanel2.setBackground(_plotBack);
            _subpanel2.setForeground(_plotFore);

            _subpanel3 = new Panel();
            _subpanel3.setLayout(new BorderLayout(15, 15));
            _subpanel3.add("Center", p3);
            _subpanel3.setSize(300,380);
            _subpanel3.setBackground(_plotBack);
            _subpanel3.setForeground(_plotFore);

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

   private final double _minGap = 0.02;
}
