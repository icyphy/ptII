/* IIR Filter Setup View  
 
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

import ptolemy.math.filter.Filter; 
import ptolemy.filter.filtermodel.*;

import java.util.*;
import java.awt.*;
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// IIRFiltSetView 

/** 
  View for setup IIR filter design parameters.  This class of view does
  not contain any plot, thus it is derived from FilterView.  A panel with 
  choice widgets that allow user to select the IIR filter.  
  <p>   
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W%   %G%
  @date: 3/2/98
 */ 

public class IIRFiltSetView extends FilterView {

    /**
     * Constructor.  Panel is created, then added to the frame, if the operation
     * mode is frame mode.  Data is requested from the filter and passed to
     * the plot by calling <code> _setViewIIRParam() </code> method.
     *
     * @param filter observed filter object
     * @param mode operation mode
     * @param viewname name of thie view

     */
    public IIRFiltSetView(FilterObj filter, int mode, String viewname){

          super(viewname, filter);
          _viewPanel = new FiltSet(this);
          _observed = filter;
          _opMode = mode;
          if (_opMode == FilterView.FRAMEMODE){ // frame mode
              String name = new String("");
              if (filter != null) name = filter.getName();
              _frame  = _createViewFrame(name);
              _frame.add("Center", _viewPanel);
              _frame.pack();
              _frame.setLocation(10, 350);
              _frame.setVisible(true);
          } 
 
          // get initial data value 
     
          if (filter != null) _setViewIIRParam();
        
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

     /**
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * IIR parameters then pass them to the panel, by calling <code> 
     * _setViewIIRParam() </code>
     * <p>
     * The filter object must call call notifyObservers() with String 
     * "UpdatedFilterParameter" as argument in order to update the view.
     * <p>
     * @param observed observed filter object
     * @param arg message passed by filter object
     */
    public void update(Observable o, Object arg){
         String command = (String)arg;
         if (command.equals("UpdatedFilterParameter")){
               _setViewIIRParam();
         } 
    }



    //////////////////////////////////////////////////////////////////////////
    ////                       protected methods                          ////

    /**
     * Get the initial IIR design parameter, and set them to the view.
     */
    protected void _setViewIIRParam(){
         Vector param = ((FilterObj)_observed).getIIRParameter();
         int aprox = ((Integer)param.elementAt(0)).intValue();
         int mapm = ((Integer)param.elementAt(1)).intValue();
         int band = ((Integer)param.elementAt(2)).intValue();
         double fs = ((Double)param.elementAt(3)).doubleValue();
         ((FiltSet) _viewPanel).update(aprox,mapm,band,fs);
    }

    /** 
     * The parameter is modified on the panel.  This method notify the viewer 
     * about the new changes made on the dialog.  
     * The new parameter is sent to FilterObj.
     */
    protected void _newIIRParamChange(int approx, int mapmethod, int bandtype, 
                                    double fs){
         FilterObj jf = (FilterObj) _observed;
         jf.setIIRParameter(approx, mapmethod, bandtype, fs);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         inner class                              ////

    // FiltSet 
    // A panel allow user to input the initial parameters for a filter.  These
    // parameters are the filter type, approximation method, analog to digital 
    // mapping method, sampling frequency.  Maybe also the name of the filter. 
 
    class FiltSet extends Panel implements ItemListener  {
 
 
        // Constructor.  Set up the panel for user to input the initial 
        // parameter for the IIR filter, these parameters is only set at 
        // this stage of the design while the bands data can be changed 
        // on the frequency response plot.
    
        public FiltSet(IIRFiltSetView view){
 
            // sampling frequency
            _fs = new TextField(5);
 
            // analog to digital transformation methods selection
            _ttype = new Choice();
            _ttype.addItem("Bilinear");
            _ttype.addItem("Impulse Invariant");
            _ttype.addItem("Matched Z");
            _ttype.addItemListener(this);
            
            // approximation methods selection
            _atype = new Choice();
            _atype.addItem("Butterworth");
            _atype.addItem("Chebyshev I");
            _atype.addItem("Chebyshev II");
            _atype.addItem("Elliptic");
            _atype.addItemListener(this);
 
            // filter type selection
            _ftype = new Choice();
            _ftype.addItem("Lowpass");
            _ftype.addItem("Highpass");
            _ftype.addItem("Bandpass");
            _ftype.addItem("Bandstop");
            _ftype.addItemListener(this);
 
            // Layout the components 
            _p0 = new Panel();
            _p0.setLayout(new FlowLayout(5, 5, 5));
            _p0.add(new Label("sample frequency"));
            _p0.add(_fs);
 
            _p1 = new Panel();
            _p1.setLayout(new FlowLayout(5, 5, 5));
            _p1.add(new Label("Approximation method"));
            _p1.add(_atype);
 
            _p2 = new Panel();
            _p2.setLayout(new FlowLayout(5, 5, 5));
            _p2.add(new Label("Filter Type"));
            _p2.add(_ftype);
 
            _p3 = new Panel();
            _p3.setLayout(new FlowLayout(5, 5, 5));
            _p3.add(new Label("Analog to Digital Transfer Method"));
            _p3.add(_ttype);
   
            _p4 = new Panel();
            _p4.setLayout(new BorderLayout(5, 5));
            _p4.add("North", new Label("Set initial Parameters"));
            _p4.add("Center", _p0);
            _p4.add("South", _p1);
 
            _p5 = new Panel();
            _p5.setLayout(new BorderLayout(5, 5));
            _p5.add("North", _p2);
            _p5.add("Center",_p3);
 
 
            this.setLayout(new BorderLayout(5, 5));
            this.add("North", _p4);
            this.add("Center", _p5);
            this.setSize(350,200);
       }
 
   
       // Handle the event the changing in choices.  Obtain all the information
       // from the widgets and send them to view, by calling <code> 
       // _newIIRParamChange </code> method
    
       public void itemStateChanged(ItemEvent evt){
System.out.println("item changed");
           int at;
           int ft;
           int tt;
           double fss;

           String approxmethod = _atype.getSelectedItem();
           // only butterworth & Chebshev are supported now
           if (approxmethod.equals("Butterworth")){
               at = Filter.BUTTERWORTH;
           } else if (approxmethod.equals("Chebyshev I")){
               at = Filter.CHEBYSHEV1;
           } else if (approxmethod.equals("Chebyshev II")){
               // at = Filter.CHEBYSHEV1;
                at = Filter.CHEBYSHEV2;
           } else if (approxmethod.equals("Elliptic")){
                at = Filter.CHEBYSHEV1;
               // at = Filter.ELLIPTICAL;
           } else at = 0;
 
           String transformmethod = _ttype.getSelectedItem();
           // only bilinear supported now
           if (transformmethod.equals("Bilinear")){
               tt = Filter.BILINEAR;
           } else if (transformmethod.equals("Impulse Invariant")){
               // tt = Filter.IMPULSEINVAR;
               tt = Filter.BILINEAR;
           } else if (transformmethod.equals("Mathched Z")){
               // tt = FilterFType.MATCHZ;
               tt = Filter.BILINEAR;
           } else tt = 0;
 
           String filtertype = _ftype.getSelectedItem();
           // all filter types are supported now
           if (filtertype.equals("Lowpass")){
               ft = Filter.LOWPASS;
           } else if (filtertype.equals("Highpass")){
               ft = Filter.HIGHPASS;
           } else if (filtertype.equals("Bandpass")){
               ft = Filter.BANDPASS;
           } else if (filtertype.equals("Bandstop")){
               ft = Filter.BANDSTOP;
           } else {
               ft = 0;
           }
 
           if (_fs.getText().equals("")){
               fss = 1.0;
           } else {
              Double a = new Double(_fs.getText());
              fss = a.doubleValue();
           }

           _newIIRParamChange(at, tt, ft, fss);
           
       }

       // Called by view to change the parameter as given. 
       public void update(int aprox, int mapm, int band, double fs){
           if (aprox == Filter.BUTTERWORTH){

               _atype.select("Butterworth");

           } else if (aprox == Filter.CHEBYSHEV1){

               _atype.select("Chebyshev I");

           } else if (aprox == Filter.CHEBYSHEV2){

               _atype.select("Chebyshev II");

           } else if (aprox == Filter.ELLIPTICAL){

               _atype.select("Elliptic");

           } 

           if (mapm == Filter.BILINEAR){

               _ttype.select("Bilinear");

           } else if (mapm == Filter.IMPULSEINVAR){

               _ttype.select("Impulse Invariant");

           } else if (mapm == Filter.MATCHZ){

               _ttype.select("Mathched Z");

           } 

           if (band == Filter.LOWPASS){
    
               _ftype.select("Lowpass");

           } else if (band == Filter.HIGHPASS){

               _ftype.select("Highpass");

           } else if (band == Filter.BANDPASS){

               _ftype.select("Bandpass");

           } else if (band == Filter.BANDSTOP){

               _ftype.select("Bandstop");

           }

           _fs.setText(String.valueOf(fs));
           repaint();
       }
 
       //  private variables  //
 
       private Panel _p0, _p1, _p2, _p3, _p4, _p5;
 
       private TextField _fs;
       private Choice _ttype;
       private Choice _atype;
       private Choice _ftype;
    }

}
