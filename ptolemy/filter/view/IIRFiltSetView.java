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
$Id$ %S%
 
*/

package ptolemy.filter.view;

import ptolemy.math.FType; 
import ptolemy.filter.filtermodel.*;
import java.util.*;
import java.awt.*;
import java.applet.*;

//////////////////////////////////////////////////////////////////////////
//// IIRFiltSetView 

/** 
 * View for setup the type of IIR filter
 * 
 * author: William Wu
 * version:
 * date: 3/2/98
 */ 


public class IIRFiltSetView extends View {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor.
     */
    public IIRFiltSetView(){

          _opMode = 0;
          _observed = null;
          _viewPanel = new FiltSet(this);
          _frame = new Frame();
          _frame.add(_viewPanel);
          _frame.show();

    }

    /**
     * Default constructor.
     */
    public IIRFiltSetView(FilterObj filter, int mode, String name){


          _viewPanel = new FiltSet(this);
          _observed = filter;
          _opMode = mode;
          if (_opMode == 0){ // frame mode
              _frame = new Frame(name);
              _frame.add("Center", _viewPanel);
              _frame.pack();
              _frame.show();
          } 
 
          // it will start with default value

    }


    public void update(Observable o, Object arg){
         String command = (String)arg;
         if (command.equals("UpdatedFilterParameter")){
               FilterObj jf = (FilterObj) _observed;
               Vector param = jf.getIIRParameter();
               int aprox = ((Integer)param.elementAt(0)).intValue();
               int mapm = ((Integer)param.elementAt(1)).intValue();
               int band = ((Integer)param.elementAt(2)).intValue();
               double fs = ((Double)param.elementAt(3)).doubleValue();
               ((FiltSet) _viewPanel).update(aprox,mapm,band,fs);
         } 
    }

    // called by setup dialog to notify the viewer about the new changes made on the
    // dialog.  parse the list of options 
    public void newChange(int approx, int mapmethod, int bandtype, double fs){

         Integer approxm = new Integer(approx);
         Integer mapm = new Integer(mapmethod);
         Integer band = new Integer(bandtype);
         Double fss = new Double(fs);
         Vector sent = new Vector();
         sent.addElement(approxm);
         sent.addElement(mapm);
         sent.addElement(band);
         sent.addElement(fss);
 
         FilterObj jf = (FilterObj) _observed;
         jf.receive(4,"Update", sent);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

}

//////////////////////////////////////////////////////////////////////////
//// FiltSet 
/**
 * A panel allow user to input the initial parameters for a filter.  These
 * parameters are the filter type, approximation method, analog to digital 
 * mapping method, sampling frequency.  Maybe also the name of the filter. 
 * 
 * author: William Wu
 * version:
 * date: 3/2/98
 */
 
class FiltSet extends Panel {
 
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
 
    /**
     * Constructor.  Set up the panel for user to input the initial parameter
     * for the IIR filter, these parameters is only set at this stage of the
      * design while the bands data can be changed on the plot.
     */
    public FiltSet(IIRFiltSetView view){
 
      _fs = new TextField(5);
      _view = view;
 
      // analog to digital mapping methods selection
      _ttype = new CheckboxGroup();
      _bilinear = new Checkbox("Bilinear", _ttype, true);
      _impulsei = new Checkbox("Impulse Invariant", _ttype, false);
      _matchz = new Checkbox("Matched Z", _ttype, false);
 
      // approximation methods selection
      _mtype = new CheckboxGroup();
      _butterworth = new Checkbox("Butterworth", _mtype, true);
      _chebyshev = new Checkbox("Chebyshev", _mtype, false);
      _ellip = new Checkbox("Elliptic", _mtype, false);
 
      // filter type selection
      _ftype = new CheckboxGroup();
      _lowp = new Checkbox("Lowpass", _ftype, true);
      _highp = new Checkbox("Highpass", _ftype, false);
      _bandp = new Checkbox("Bandpass", _ftype, false);
      _bands = new Checkbox("Bandstop", _ftype, false);
  
      // sampling frequency and name
      _p0 = new Panel();
      _p0.setLayout(new FlowLayout(5, 5, 5));
      _p0.add(new Label("sample frequency"));
      _p0.add(_fs);
 
      _p1 = new Panel();
      _p1.setLayout(new BorderLayout(5, 5));
      _p1.add("North", new Label("Approximation method"));
      _p11 = new Panel();
      _p11.setLayout(new FlowLayout(5,5,5));
      _p11.add(_butterworth);
      _p11.add(_chebyshev);
      _p11.add(_ellip);
      _p1.add("Center", _p11);
 
      _p2 = new Panel();
 
      _p2.setLayout(new BorderLayout(5, 5));
      _p2.add("North", new Label("Filter Type"));
      _p21 = new Panel();
      _p21.setLayout(new FlowLayout(5,5,5));
      _p21.add(_lowp);
      _p21.add(_highp);
      _p21.add(_bandp);
      _p21.add(_bands);
      _p2.add("Center", _p21);
 
      _p3 = new Panel();
      _p3.setLayout(new BorderLayout(5, 5));
      _p3.add("North", new Label("Analog to Digital Transfer Method"));
      _p31 = new Panel();
      _p31.setLayout(new FlowLayout(5,5,5));
      _p31.add(_bilinear);
      _p31.add(_impulsei);
      _p31.add(_matchz);
      _p3.add("Center", _p31);
   
      _p4 = new Panel();
      _p4.setLayout(new BorderLayout(5, 5));
      _p4.add("North", new Label("Set initial Parameters"));
      _p4.add("Center", _p0);
      _p4.add("South", _p1);
 
      _apply = new Button("Apply");
 
      _p5 = new Panel();
      _p5.setLayout(new BorderLayout(5, 5));
      _p5.add("North", _p2);
      _p5.add("Center",_p3);
      _p5.add("South", _apply);
 
 
      this.setLayout(new BorderLayout(15, 15));
      this.add("North", _p4);
      this.add("Center", _p5);
   }
 
   /**
    * Handle the event from the apply button.  Obtain all the information
    * from the widgets and send them to View.
    */
   public boolean action(Event evt, Object arg){
      int mt;
       int ft;
      int tt;
      String name;
      double fss;
 
      if (evt.target == _apply){
          // only butterworth & Chebshev are supported now
          if (_butterworth.getState() == true){
              mt = FType.Butterworth;
          } else if (_chebyshev.getState() == true){
              mt = FType.Chebyshev1;
          } else if (_ellip.getState() == true){
              mt = FType.Butterworth;
              // mt = FType.Elliptical;
          } else mt = 0;
 
          // only bilinear supported now
          if (_bilinear.getState() == true){
              tt = FType.Bilinear;
          } else if (_impulsei.getState() == true){
              tt = FType.Bilinear;
              // tt = FType.ImpulseInvar;
          } else if (_matchz.getState() == true){
              tt = FType.Bilinear;
              // tt = FType.MatchZ;
          } else tt = 0;
 
          if (_lowp.getState() == true){
              ft = FType.Lowpass;
          } else if (_highp.getState() == true){
              ft = FType.Highpass;
          } else if (_bandp.getState() == true){
              ft = FType.Bandpass;
          } else if (_bands.getState() == true){
              ft = FType.Bandstop;
          } else {
              ft = 0;
          }
 
          if (_fs.getText().equals("")){
              fss = 1.0;
          } else {
              Double a = new Double(_fs.getText());
              fss = a.doubleValue();
              if (fss < 0.0) return false;
          }

          _view.newChange(mt, tt, ft, fss);
          return true;
           
       } else return false;
 
   }
 
   public void update(int aprox, int mapm, int band, double fs){
       if (aprox == FType.Butterworth){
           _butterworth.setState(true);
       } else if (aprox == FType.Chebyshev1){
           _chebyshev.setState(true);
       } else if (aprox == FType.Elliptical){
           _ellip.setState(true);
       } 

       if (mapm == FType.Bilinear){
           _bilinear.setState(true);
       } else if (mapm == FType.ImpulseInvar){
           _impulsei.setState(true);
       } else if (mapm == FType.MatchZ){
           _matchz.setState(true);
       } 

       if (band == FType.Lowpass){
           _lowp.setState(true);
       } else if (band == FType.Highpass){
           _highp.setState(true);
       } else if (band == FType.Bandpass){
           _bandp.setState(true);
       } else if (band == FType.Bandstop){
           _bands.setState(true);
       }

       _fs.setText(String.valueOf(fs));
       this.show(); 
   }
 
   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////
 
   private Panel _p0, _p1, _p11, _p2, _p21, _p3, _p31, _p4, _p5;
 
   private TextField _fs;
   private CheckboxGroup _ttype;
   private Checkbox _bilinear, _impulsei, _matchz;
   private CheckboxGroup _mtype;
   private Checkbox _butterworth, _chebyshev, _ellip;
   private CheckboxGroup _ftype;
   private Checkbox _lowp, _highp, _bandp, _bands;
   private Button _apply;
   private IIRFiltSetView _view;
}

