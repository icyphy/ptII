/* Polezero view object 
 
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
 
import ptolemy.filter.filtermodel.*; 
import ptolemy.math.Complex; 

import java.util.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// PoleZeroView  
/** 
 * Pole-zero plot observer.  This observer is specificly for the pole zero
 * plot of a filter.  
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */ 
 
public class PoleZeroView extends View {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor.  Default operation mode is in frame mode.  
     */  
    public PoleZeroView(){
          PoleZeroPlot plot = new PoleZeroPlot();
          plot.setView(this);
          _plots = new Plot[1];
          _plots[0]=plot;
          _opMode = 0;
          // _frame = new InteractPoleZeroFrame("", plot);
          _frame = new Frame();
    }

    /**
     * Constructor.  Once the plot is created, it will be placed
     * in the frame, (if the View is in frame mode).  
     * Filter's <code> getPoleZero() </code> is called to get
     * the data and passed it too the plot.
     */ 
    public PoleZeroView(Observable filter, int mode, String name){
          PoleZeroPlot plot = new PoleZeroPlot();
          plot.setView(this);
          _plots = new Plot[1];
          _plots[0]=plot;
          _opMode = mode;

          _viewPanel = new Panel();
          _viewPanel.setBackground(Color.black); 
          _addpolezerobutton = new Button("Add Pole/Zero");
          _addpolezerobutton.setForeground(Color.white);
          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(2,2,2));
          buttonpanel.add(_addpolezerobutton);
          _viewPanel.add("North", buttonpanel);
          _viewPanel.add("Center", plot);
          _viewPanel.resize(300,350);
          if (_opMode == 0){ // frame mode
              _frame = new Frame(name);
              _frame.add("Center", _viewPanel);
              _frame.resize(300, 350);
              _frame.show();
              plot.init();
              plot.resize(300,300);
          } 
          _observed = filter;

          // get it all started 
          update(_observed, "UpdatedFilter");
    }  
    


    /**
     * To notify the view about the updated filter.  When 
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * poles and zeros then pass them to the plot.
     * remember dataset 1: pole
     *          dataset 2: zero 
     */ 
    public void update(Observable o, Object arg){
          String command = (String)arg;
          if (command.equals("UpdatedFilter")){

               FilterObj jf = (FilterObj) _observed;
               ((InteractPlot)_plots[0]).eraseInteractComponents();
               _plots[0].eraseAllPoints(1);
               _plots[0].eraseAllPoints(2);
               InteractComponent ic;

               int polecount=0;
               int zerocount=0;

               Complex [] poledata = jf.getPole();
               _crossref = new InteractObjCrossRef[2][];

               if (poledata!=null){

                   _crossref[0] = new InteractObjCrossRef[poledata.length];
 
                   for (int ind = 0;ind<poledata.length;ind++){

                       ic = new InteractComponent("Pole", 
                                                  InteractComponent.Cross);
                       ic.setDrawingParam(Color.green, 6, false,
                                InteractComponent.SymmetricOri);
                       ic.setInteractParam(new String("Pole real"), 
                                           new String("Pole imag"), 
                                           InteractComponent.AllDegFree); 
                    
                       ic.setDatasetIndex(1, ind);
                       ic.xv = poledata[ind].real; 
                       ic.yv = poledata[ind].imag;
                       _crossref[0][ind] = new InteractObjCrossRef();
                       _crossref[0][ind].dataObj = (Object) poledata[ind];
                       _crossref[0][ind].interactObj = ic;
                       ((InteractPlot)_plots[0]).addInteractPoint(ic, 
                                         1, ic.xv, ic.yv, false);
        
                   } 

               }

               Complex [] zerodata = jf.getZero();
               if (zerodata!=null){

                   _crossref[1] = new InteractObjCrossRef[zerodata.length];
                   for (int ind = 0;ind<zerodata.length;ind++){

                       ic = new InteractComponent("Zero", 
                                InteractComponent.Circle);
                       ic.setDrawingParam(Color.cyan, 8, false,
                                InteractComponent.SymmetricOri);
                       ic.setInteractParam(new String("Zero real"), 
                                           new String("Zero imag"), 
                                           InteractComponent.AllDegFree); 

                       ic.xv = zerodata[ind].real; 
                       ic.yv = zerodata[ind].imag; 
                       _crossref[1][ind] = new InteractObjCrossRef();
                       _crossref[1][ind].dataObj = (Object) zerodata[ind];
                       _crossref[1][ind].interactObj = ic;
                       ((InteractPlot)_plots[0]).addInteractPoint(ic, 
                               2, ic.xv, ic.yv, false); 
                   }

               }
               _plots[0].repaint();
          }
     }

    /**
     * process the action created by clicking the add pole/zero
     * button and change the mode of mouse (delete or move)
     * FIXME: fix this for jdk1.1
     */
    public boolean action(Event evt, Object arg){
        if (evt.target == _addpolezerobutton){
             if (_addpolezerobox == null){
                   _addpolezerobox = new NewPoleZeroBox("Add Pole-Zero");
             }
             return true;
        }
        return false;
    }

    public void newpolezero(boolean conjugate, double polereal, double poleimag,
                            double zeroreal, double zeroimag){
        Complex pole = new Complex(polereal, poleimag);
        Complex zero = new Complex(zeroreal, zeroimag);
        FilterObj jf = (FilterObj) _observed;
        jf.addPoleZero(pole, zero, conjugate);
    }

    public void moveInteractComp(InteractComponent interactpolezero){

        boolean polemoved = false;
        for (int i=0;i<_crossref[0].length;i++){
             if (_crossref[0][i].interactObj == interactpolezero){
                 FilterObj jf = (FilterObj) _observed;
                 jf.movePole((Complex) _crossref[0][i].dataObj);
                 polemoved = true;
                 break;
             }
        } 
    
        if (!polemoved) {
            for (int i=0;i<_crossref[1].length;i++){
                if (_crossref[1][i].interactObj == interactpolezero){
                    FilterObj jf = (FilterObj) _observed;
                    jf.moveZero((Complex) _crossref[1][i].dataObj);
                    break;
                }
            }
        } 

    }

    public void deletePoleZero(InteractComponent interactpolezero){

        boolean poledeleted = false;
        for (int i=0;i<_crossref[0].length;i++){
             if (_crossref[0][i].interactObj == interactpolezero){
                 FilterObj jf = (FilterObj) _observed;
                 jf.deletePole((Complex) _crossref[0][i].dataObj);
                 poledeleted = true;
                 break;
             }
        } 

        if (!poledeleted) {
            for (int i=0;i<_crossref[1].length;i++){
                if (_crossref[1][i].interactObj == interactpolezero){
                    FilterObj jf = (FilterObj) _observed;
                    jf.deleteZero((Complex) _crossref[1][i].dataObj);
                    break;
                }
            }
        } 

    }
     
    public void splitConjPoleZero(InteractComponent interactpolezero){

        boolean polesplited = false;
        for (int i=0;i<_crossref[0].length;i++){
             if (_crossref[0][i].interactObj == interactpolezero){
                 FilterObj jf = (FilterObj) _observed;
                 jf.splitConjPole((Complex) _crossref[0][i].dataObj);
                 polesplited = true;
                 break;
             }
        }
 
        if (!polesplited) {
            for (int i=0;i<_crossref[1].length;i++){
                if (_crossref[1][i].interactObj == interactpolezero){
                    FilterObj jf = (FilterObj) _observed;
                    jf.splitConjZero((Complex) _crossref[1][i].dataObj);
                    break;
                }
            }
        } 
    }
     
    public void makePoleZeroConj(InteractComponent interactpolezero){

        boolean makepoleconj = false;
        for (int i=0;i<_crossref[0].length;i++){
             if (_crossref[0][i].interactObj == interactpolezero){
                 FilterObj jf = (FilterObj) _observed;
                 jf.makePoleConj((Complex) _crossref[0][i].dataObj);
                 makepoleconj = true;
                 break;
             }
        }
 
        if (!makepoleconj) {
            for (int i=0;i<_crossref[1].length;i++){
                if (_crossref[1][i].interactObj == interactpolezero){
                    FilterObj jf = (FilterObj) _observed;
                    jf.makeZeroConj((Complex) _crossref[1][i].dataObj);
                    break;
                }
            }
        } 
    }
     
     
     private Button _addpolezerobutton;
     private NewPoleZeroBox _addpolezerobox;


//////////////////////////////////////////////////////////////////////////
////  NewPoleZeroBox
/**
 * Dialog for add new pole/zero to the plot, inner class.
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */
 class NewPoleZeroBox extends Frame {

   //////////////////////////////////////////////////////////////////////////
   ////                         public methods                           ////
   /**
    * Constructor.  Setup the widgets for different inputs.
    */
   public NewPoleZeroBox(String title){
      super(title);
      _polexentry = new TextField(10);
      _polexentry.setText("1.0");
      _poleyentry = new TextField(10);
      _poleyentry.setText("0.0");

      _zeroxentry = new TextField(10);
      _zeroxentry.setText("0.0");
      _zeroyentry = new TextField(10);
      _zeroyentry.setText("0.0");

      // conjugate pair or no conjugate pair
      _ckg = new CheckboxGroup();
      _noConj = new Checkbox("no", _ckg, true);
      _yesConj = new Checkbox("yes", _ckg, false);

      Panel p1 = new Panel();
      p1.add(new Label("Complex Conjugate ?"));
      p1.add(_noConj);
      p1.add(_yesConj);

      Panel p2 = new Panel();
      p2.setLayout(new BorderLayout(5, 5));

      // real and imaginary value of pole

      Panel p3 = new Panel();
      p3.setLayout(new FlowLayout(5, 5, 5));
      p3.add("Left", new Label("Default Real"));
      p3.add("Center", _polexentry);

      Panel p4 = new Panel();
      p4.setLayout(new FlowLayout(5, 5, 5));
      p4.add("Left", new Label("Default Imag"));
      p4.add("Center", _poleyentry);

      p2.add("North", new Label("Enter Default Value for Pole:"));
      p2.add("Center", p3);
      p2.add("South", p4);

      Panel p6 = new Panel();
      p6.setLayout(new BorderLayout(5, 5));

      // real and imaginary value of zero
      Panel p7 = new Panel();
      p7.setLayout(new FlowLayout(5, 5, 5));
      p7.add("Left", new Label("Default Real"));
      p7.add("Center", _zeroxentry);

      Panel p8 = new Panel();
      p8.setLayout(new FlowLayout(5, 5, 5));
      p8.add("Left", new Label("Default Imag"));
      p8.add("Center", _zeroyentry);

      p6.add("North", new Label("Enter Default Value for Zero:"));
      p6.add("Center", p7);
      p6.add("South", p8);

      Panel p9 = new Panel();
      p9.setLayout(new BorderLayout(5, 5));
      p9.add("North", new Label("Enter value for a pair of pole and zero"));
      p9.add("Center", p1);
      p9.add("South", p2);

      this.setLayout(new BorderLayout(15, 15));
      this.add("North", p9);
      this.add("Center", p6);

      _ok = new Button("OK");
      this.add("South", _ok);
      this.pack();
      this.show();
   }

   /**
    * Reads the input from the dialog and sends to polezero plot.
    * Default value will be a pole at origin.
    */
   public boolean action(Event evt, Object arg){

      boolean conjpair;

      if (evt.target == _ok){
          if (_yesConj.getState() == true){
              conjpair = true;
          } else {
              conjpair = false;
          }

          double px, py, zx, zy;

          if (_polexentry.getText().equals("")){
              px = 0.0;
          } else {
              Double a = new Double(_polexentry.getText());
              px = a.doubleValue();
          }

          if (_poleyentry.getText().equals("")){
              py = 0.0;
          } else {
              Double b = new Double(_poleyentry.getText());
              py = b.doubleValue();
          }

          if (_zeroxentry.getText().equals("")){
              zx = 0.0;
          } else {
              Double a = new Double(_zeroxentry.getText());
              zx = a.doubleValue();
          }

          if (_zeroyentry.getText().equals("")){
              zy = 0.0;
          } else {
              Double b = new Double(_zeroyentry.getText());
              zy = b.doubleValue();
          }

          this.hide();
          this.dispose();

          newpolezero(conjpair, px, py, zx, zy);
          return true;
       } else return false;

   }

   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////
   private TextField _polexentry;
   private TextField _poleyentry;
   private TextField _zeroxentry;
   private TextField _zeroyentry;
   private CheckboxGroup _ckg;
   private Checkbox _noConj;
   private Checkbox _yesConj;
   private Button _ok;
 }

         
}
