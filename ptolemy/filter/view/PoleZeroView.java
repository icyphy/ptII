/* Pole/zero view object 
 
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
 
import ptolemy.filter.controller.Manager; 
import ptolemy.filter.filtermodel.*; 
import ptolemy.math.Complex; 

import java.util.*;
import java.awt.*;
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// PoleZeroView  
/** 
  Observer for filter's pole-zero plot.  This class contain a PoleZeroPlot
  object.  Poles and zeroes will be diplayed on it.  After user click and drag 
  poles and zeroes with mouse, moveInteractComp() is called to notify the
  filter about new change.  Method update() is called when filter changes
  its poles and zeroes, and this object will query the filter for new 
  pole/zero values.  Hashtables _crossref will be used to store the pairing
  between pole/zero and their interact counter parts (one hashtable is for
  pole, the other is for zero).  
  <p>
  PoleZeroView also allow user to add a new pair of pole/zero with a window, 
  NewPoleZeroBox.          
  <p> 
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W%   %G%
  @date: 3/2/98
 */ 
 
public class PoleZeroView extends PlotView implements ActionListener {


    /**
     * Constructor.  Create pole/zero plot, the plot panel, and frame (if
     * the operation mode is in FRAMEMODE).  Method <code> _setViewPoleZero
     * </code>  is called to get the pole/zero data into the plot.
     * <p>
     * @param filter the observable filter object. 
     * @param mode mode of operation: FRAMEMODE or APPLETMODE
     * @param viewname name of this view. 
     */ 
    public PoleZeroView(FilterObj filter, int mode, String viewname){
          super(viewname, filter);

          // create new PoleZeroPlot
          InteractPlot plot = new InteractPlot();
          plot.setBackground(Color.black);
          plot.setForeground(Color.gray);
          plot.setXRange(-1.2, 1.2);
          plot.setYRange(-1.2, 1.2);
          plot.setNumSets(5);
          plot.setMergeInteractComp(true); 
          if (filter.getType() == ptolemy.math.filter.Filter.IIR){
              plot.setEditPermission(false);
          }
          // set the view reference
          plot.setView(this);
          _plots = new Plot[1];
          _plots[0]=plot;

          // saved the mode
          _opMode = mode;

          // create panel for plot
          _viewPanel = new Panel();
          _viewPanel.setBackground(Color.black); 
          _viewPanel.setForeground(Color.white); 

          // create the button for add new pole/zero
          _addpolezerobutton = new Button("Add Pole/Zero");
          _addpolezerobutton.setForeground(Color.white);
          _addpolezerobutton.setActionCommand("Add Pole Zero");
          _addpolezerobutton.addActionListener(this);
          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(2,2,2));
          buttonpanel.add(_addpolezerobutton);

          // layout component in the view panel
          _viewPanel.add("North", buttonpanel);
          _viewPanel.add("Center", plot);
          _viewPanel.setSize(300,380);
         
          // place view panel in frame 
          if (_opMode == Manager.FRAMEMODE){ // frame mode
              _frame  = _createViewFrame(((FilterObj) filter).getName());
              _frame.add("Center", _viewPanel);
              _frame.setSize(300, 390);
              _frame.setLocation(10, 10);
              _frame.setVisible(true);
              plot.init();
              plot.setSize(300,300);

              // set the plot data for a unit circle
              // 180 points decimation
              for (double i = (-1)*Math.PI; i<=Math.PI; i=i+Math.PI/180){
                   double a = Math.cos(i);
                   double b = Math.sin(i);
                   if (i == (-1)*Math.PI){
                       plot.addPoint(0, a, b, false);
                   } else {
                       plot.addPoint(0, a, b, true);
                   }
              }
              _unitcircleadded = true;

          } 
          _observed = filter;

          // create two new hashtable for cross link between interact component
          // and underlying data.  
          _crossref = new Hashtable[2];
          _crossref[0] = new Hashtable();   // for pole
          _crossref[1] = new Hashtable();   // for zero

          // get the initial data 
          _setViewPoleZero();
    }  
    

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

   /**
    * Notifies the view about the updated filter.  When a filter is 
    * modified, the filter calls <code> notifyObservers()
    * </code>, which calls each observer's <code> update() </code>
    * function.  This update will query filter for the new
    * poles and zeros, then create interact components for them, by
    * calling <code> _setViewPoleZero() </code>.  
    * <p> 
    * @param observable filter object
    * @param arg message sent by filter object 
    */ 
   public void update(Observable observable, Object arg){
          String command = (String)arg;

          // "UpdatedFilter" means filter's data is new. 
          if (command.equals("UpdatedFilter")){ 
              _setViewPoleZero();
          }
   }

   /**
    * Process the action created by clicking the add pole/zero button.
    * It will check if the window for add pole/zero exists, if not,
    * then the window will be created.  And the window is shown.
    *
    * @param evt action event created by add pole zero button.
    */
   public void actionPerformed(ActionEvent evt){
        if (evt.getActionCommand().equals("Add Pole Zero")){
             if (_addpolezerobox == null){
                   _addpolezerobox = new NewPoleZeroBox("Add Pole-Zero");
             }
             _addpolezerobox.setVisible(true);
        }
   }

   /**
    * Add a new pair of pole/zero.  If <i> conjugate </i> is set, the pole
    * / zero will have their conjugate pairs.  Filter <code> addPoleZero() 
    * </code> is called to add the newly created pole zero.
    *  
    * @param conjugate boolean value indicating if the given pole/zero have
    *  conjugate pair. 
    * @param polereal the real value part of the new pole  
    * @param poleimag the imaginary value part of the new pole  
    * @param zeroreal the real value part of the new zero  
    * @param zeroimag the imaginary value part of the new zero  
    */
   public void newpolezero(boolean conjugate, double polereal, double poleimag,
                            double zeroreal, double zeroimag){
        Complex pole = new Complex(polereal, poleimag);
        Complex zero = new Complex(zeroreal, zeroimag);
        FilterObj jf = (FilterObj) _observed;
        jf.addPoleZero(pole, zero, conjugate);
   }

   /**
    * Delete the selected pole/zero.  This function calls filter object 
    * <code> deletePole() </code> or <code> deleteZero() </code>.
    * This will delete the factor associate with that pole/zero. 
    * @param interactpolezero to be deleted interact pole/zero 
    */
   public void deleteInteractComp(InteractComponent interactpolezero){
        if (_crossref[0].containsKey(interactpolezero)){
            Complex pole = (Complex) _crossref[0].get(interactpolezero); 
            // notify the filter object about the moved pole.
            FilterObj jf = (FilterObj) _observed;
            jf.deletePole(pole); 
            
        } else {        
            if (_crossref[1].containsKey(interactpolezero)){
                Complex zero = (Complex) _crossref[1].get(interactpolezero); 
                // notify the filter object about the moved zero.
                FilterObj jf = (FilterObj) _observed;
                jf.deleteZero(zero); 
            }
        } 
   }


   /**
    * Move the desired interact pole/zero.  First find the Complex object
    * that represents the pole or zero, then calls FilterObj's updatePoleValue(),
    * updateZeroValue() to change pole/zero in the filter object.
    * <p> 
    * @param interactpolezero changed interact pole/zero
    */  
   public void moveInteractComp(InteractComponent interactpolezero){

        if (_crossref[0].containsKey(interactpolezero)){
            Complex pole = (Complex) _crossref[0].get(interactpolezero); 
            // notify the filter object about the moved pole.
            FilterObj jf = (FilterObj) _observed;
            jf.updatePoleValue(pole, interactpolezero.getXValue(), 
                               interactpolezero.getYValue());
            
        } else {        
            if (_crossref[1].containsKey(interactpolezero)){
                Complex zero = (Complex) _crossref[1].get(interactpolezero); 
                // notify the filter object about the moved zero.
                FilterObj jf = (FilterObj) _observed;
                jf.updateZeroValue(zero, interactpolezero.getXValue(), 
                                   interactpolezero.getYValue());
            }
        } 
   }


   /**
    * Display all the pole-zero that associate with the currently selected
    * pole/zero.  This method will call filter object <code> getFamilyPoleZero()
    * </code> to get all the poles/zero that are in the same factor.
    * @param interactpolezero changed interact pole/zero
    */ 
   public void selectInteractComp(InteractComponent interactpolezero){

        InteractComponent ic;
        Complex [] familypole = null;
        Complex [] familyzero = null;

        if (_crossref[0].containsKey(interactpolezero)){
            Complex pole = (Complex) _crossref[0].get(interactpolezero); 
            FilterObj jf = (FilterObj) _observed;
            familypole = jf.getFamilyPoleWithPole(pole);
            familyzero = jf.getFamilyZeroWithPole(pole);

        } else if (_crossref[1].containsKey(interactpolezero)){
                        
            Complex zero = (Complex) _crossref[1].get(interactpolezero); 
            FilterObj jf = (FilterObj) _observed;
            familypole = jf.getFamilyPoleWithZero(zero);
            familyzero = jf.getFamilyZeroWithZero(zero);
        }
   
        if (familypole != null){
            for (int i=0;i<familypole.length;i++){
                if (!_crossref[0].contains((Object) familypole[i])) {
                      ic = new InteractComponent("Pole", 
                                          InteractComponent.CROSS, 
                                          familypole[i].real,
                                          familypole[i].imag);
                      ic.setDrawingParam(Color.green, 6, false,
                                          InteractComponent.SYMMETRICORI);
                      ((InteractPlot)_plots[0]).addInteractPoint(ic, 3, 
                                                        ic.getXValue(),
                                                        ic.getYValue(),
                                                        false);
                      ic.setHighlighted(true);
                } else { 
                      Enumeration polekeys = _crossref[0].keys();
                      while (polekeys.hasMoreElements()){
                            ic = (InteractComponent) polekeys.nextElement();
                            if (_crossref[0].get(ic) == familypole[i]){
                                 ic.setHighlighted(true);
                                 break;
                            }
                      }
                 } 
            }
        }
 
        if (familyzero != null){
            for (int i=0;i<familyzero.length;i++){
                if (!_crossref[1].contains((Object) familyzero[i])) {
                      ic = new InteractComponent("Zero", 
                                          InteractComponent.CIRCLE, 
                                          familyzero[i].real,
                                          familyzero[i].imag);
                      ic.setDrawingParam(Color.cyan, 6, false,
                                          InteractComponent.SYMMETRICORI);
                      ((InteractPlot)_plots[0]).addInteractPoint(ic, 4, 
                                                         ic.getXValue(),
                                                         ic.getYValue(),
                                                         false);
                      ic.setHighlighted(true);
                } else { 
                      Enumeration zerokeys = _crossref[1].keys();
                      while (zerokeys.hasMoreElements()){
                            ic = (InteractComponent) zerokeys.nextElement();
                            if (_crossref[0].get(ic) == familyzero[i]){
                                 ic.setHighlighted(true);
                                 break;
                            }
                      }
                 } 
            }
        } 
        _plots[0].repaint();
   }

   /**
    * Remove the pole/zero that is in the same factor as the given pole/zero.
    * This is done by simply remove the interact components with data set 3 and 4. 
    * @param interactpolezero unselected interact pole/zero
    */ 
   public void unselectInteractComp(InteractComponent interactpolezero){
         ((InteractPlot)_plots[0]).eraseInteractComponents(3);
         ((InteractPlot)_plots[0]).eraseInteractComponents(4);
         
         Enumeration poleenum = _crossref[0].keys(); 
         Enumeration zeroenum = _crossref[1].keys(); 
  
         InteractComponent ic; 
         while (poleenum.hasMoreElements()){
             ic = (InteractComponent) poleenum.nextElement();
             ic.setHighlighted(false);
         }  
         while (zeroenum.hasMoreElements()){
             ic = (InteractComponent) zeroenum.nextElement();
             ic.setHighlighted(false);
         }  
         _plots[0].repaint();
        
   }
 
   //////////////////////////////////////////////////////////////////////////
   ////                     private methods                              ////

   private void _setViewPoleZero(){

       FilterObj jf = (FilterObj) _observed;
          
       // erase interact components in plot
       ((InteractPlot)_plots[0]).eraseInteractComponents();
      
       _plots[0].eraseAllPoints(1);
       _plots[0].eraseAllPoints(2);
       InteractComponent ic;

       // check if unit circle is added or not 
       if (!_unitcircleadded){

           // unit circle is not added, this happens in applet mode of 
           // PtFilter, since addPoint attemps to draw the on the plot,
           // at that point the applet graphics is not avaliable yet.

           // set the plot data for a unit circle
           // 180 points decimation
           for (double i = (-1)*Math.PI; i<=Math.PI; i=i+Math.PI/180){
                double a = Math.cos(i);
                double b = Math.sin(i);
                if (i == (-1)*Math.PI){
                    _plots[0].addPoint(0, a, b, false);
                } else {
                    _plots[0].addPoint(0, a, b, true);
                }
           }
       }

       Complex [] poledata = jf.getPole();

       // clear hashtable
       _crossref[0].clear();
       _crossref[1].clear();

       if (poledata!=null){

           // add pole to plot
           for (int ind = 0;ind<poledata.length;ind++){

               // create interact component represent pole
               ic = new InteractComponent("Pole", 
                                          InteractComponent.CROSS, poledata[ind].real,
                                          poledata[ind].imag);
               ic.setDrawingParam(Color.green, 6, false,
                                  InteractComponent.SYMMETRICORI);
               ic.setInteractParam(new String("Pole real"), 
                                   new String("Pole imag"), 
                                   InteractComponent.ALLDEGFREE); 
                    
               // add to interact plot 
               ((InteractPlot)_plots[0]).addInteractPoint(ic, 1, ic.getXValue(),
                                                          ic.getYValue(), false);
                        
               // add pole and its interact component to hashtable 
               _crossref[0].put(ic, poledata[ind]);
        
           } 

       }

       Complex [] zerodata = jf.getZero();
       if (zerodata!=null){

           // add zero to plot
           for (int ind = 0;ind<zerodata.length;ind++){

               // create interact component represent zero 
               ic = new InteractComponent("Zero", 
                                          InteractComponent.CIRCLE, zerodata[ind].real,
                                          zerodata[ind].imag);
               ic.setDrawingParam(Color.cyan, 8, false,
                                  InteractComponent.SYMMETRICORI);
               ic.setInteractParam(new String("Zero real"), 
                                   new String("Zero imag"), 
                                   InteractComponent.ALLDEGFREE); 

               ((InteractPlot)_plots[0]).addInteractPoint(ic, 2, ic.getXValue(),
                                                          ic.getYValue(), false);

               // add zero and its interact component to hashtable 
               _crossref[1].put(ic, zerodata[ind]);
           }

       }
       // repaint to show the changes
       _plots[0].repaint();
   }


   //////////////////////////////////////////////////////////////////////////
   ////                     private variables                            ////
  
   // button to create the new pole-zero window
   private Button _addpolezerobutton;

   // window for add new polezero 
   private NewPoleZeroBox _addpolezerobox = null;

   // flag to indicate if the unit circle has been added to the plot
   private boolean _unitcircleadded = false;

   //////////////////////////////////////////////////////////////////////////
   ////                         inner class                              ////
   //  NewPoleZeroBox
   //  Dialog for add new pole/zero to the plot, inner class.
   //
   class NewPoleZeroBox extends Frame implements ActionListener {
   
      // Constructor.  Setup the widgets for different inputs.
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

          _ok = new Button("   OK   ");
          _ok.addActionListener(this);
          _ok.setActionCommand("OK");
          _cancel = new Button("   CANCEL   ");
          _cancel.addActionListener(this);
          _cancel.setActionCommand("CANCEL");
          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(5, 5, 5));
          buttonpanel.add(_ok);
          buttonpanel.add(_cancel);
          this.add("South", buttonpanel);
          this.setSize(250,350);
          this.setVisible(true);
       }

       // Reads the input from the dialog and sends to polezero plot.
       // Default value will be a pole at origin.
       public void actionPerformed(ActionEvent evt){

          boolean conjpair;

          if (evt.getActionCommand().equals("OK")){
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

              this.setVisible(false);

              newpolezero(conjpair, px, py, zx, zy);
           } else if (evt.getActionCommand().equals("CANCEL")){
              this.setVisible(false);
           }   

       }

       // private variables
       private TextField _polexentry;
       private TextField _poleyentry;
       private TextField _zeroxentry;
       private TextField _zeroyentry;
       private CheckboxGroup _ckg;
       private Checkbox _noConj;
       private Checkbox _yesConj;
       private Button _ok;
       private Button _cancel;
   }

         
}
