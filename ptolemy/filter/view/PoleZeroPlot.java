/* A plot to show and edit pole-zero diagram.
 
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

import java.awt.*;
import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// PoleZeroPlot 
/**
 *  Pole zero plot for a filter.  It allow new pole/zero to be added,
 *  dragged around on the plot.  Since poles and zeros are interact component.
 */ 
public class PoleZeroPlot extends InteractPlot {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Processes the changes when the interactive pole/zero is moved, or
     * deleted. It will set the data point value to the interactive line's value.
     * Finally it notify the observer about the new changes.
     *
     * since in this interact plot, we allow deletion of interactcomponents thus
     * we need to overload this function.
     */
    public void processNewChanges(){

         if ((_mode == 2) && (_interactComponents!=null)){ // delete mode
             for (int i=0;i<_interactComponents.size();i++){
                InteractComponent pz = (InteractComponent)_interactComponents.elementAt(i);
                if (pz.selected == true){
                     _interactComponents.removeElementAt(i);
                     InteractComponent pair = pz.getPairIC();
                     if (pair != null){
                          _interactComponents.removeElement((Object) pair);
                     }
                }
             }
            //  _mode = 1;  // reset to move mode
         } 
         // update the datapoints
         eraseAllPoints(1); 
         eraseAllPoints(2); 
         for (int i=0;i<_interactComponents.size();i++){
            InteractComponent pz = (InteractComponent)_interactComponents.elementAt(i);
            pz.selected = false;    // unselect
            addPoint(pz.getDataSetNum(), pz.xv, pz.yv, false);
            pz.setAssociation(pz.getDataSetNum(), i);
         }

         // notify the observer 
         _viewer.newChange(_interactComponents);
         repaint();
    }


    /**
     * Overload the base drawplot function to incorprate the merging of 
     * poles and zeros.
     *
     * this calls Polt.drawPlot(), since all the interactive components
     * (poles and zeros) are drawn here.  Thus only the non interact 
     * component need to be drawn.
     */
    public synchronized void drawPlot(Graphics g, boolean clearfirst){

        olddrawPlot(g, clearfirst);

        if (_interactComponents == null) return;

        Vector drawComp = new Vector();  // list of comp to be drawn
        int [] multi = new int[_interactComponents.size()]; // list of multiplicity to each comp
        for (int i=0;i<multi.length;i++){
             multi[i]=0;
        }

        if (_interactComponents != null){
        
          Color curColor = getGraphics().getColor();
         
         // use this for loop to calculate the location of each components,
         // and check if they need merging

          for (int i=0;i<_interactComponents.size();i++){
               InteractComponent iComp = (InteractComponent) _interactComponents.elementAt(i);

               // find the coordinates
 
               int ptx = (int)((iComp.xv - _xMin)/(_xMax-_xMin)*(double)(_lrx-_ulx)); 
               int pty = (int)((_yMax - iComp.yv)/(_yMax-_yMin)*(double)(_lry-_uly)); 
               if (ptx>=0 && pty>=0){
                   iComp.x = ptx+_ulx;
                   iComp.y = pty+_uly;
                    
                   boolean merge = false;
                   if (iComp.selected==false){ 
                      for (int j=0;j<drawComp.size();j++){
                          InteractComponent ic = (InteractComponent) drawComp.elementAt(j);
                          if ((Math.abs(ic.yv-iComp.yv) < 0.01 ) 
                            && (Math.abs(ic.xv-iComp.xv) < 0.01 )
                            && (ic.getDataSetNum() == iComp.getDataSetNum())){
                                multi[j]++;
                                merge = true;
                                break;
                          }
                      }
                   }
                   if (merge == false){
                        drawComp.addElement(iComp);
                        multi[drawComp.size()-1]=1;
                   }
               } else {
                   iComp.x = 0;
                   iComp.y = 0;
               }
          }
          for (int i=0;i<drawComp.size();i++){
               InteractComponent iCompdraw = (InteractComponent) drawComp.elementAt(i);
               if (iCompdraw.selected == true) _setXORpaintMode();  // seleted for dragging 
               iCompdraw.drawSelf(getGraphics());
               if (multi[i]>1){
                   getGraphics().drawString(String.valueOf(multi[i]), 
                                     iCompdraw.x+iCompdraw.getWidth()/2+2,     
                                     iCompdraw.y+iCompdraw.getWidth()/2+2);
               }
               if (iCompdraw.selected==true) _setNormalpaintMode();
          }    
          getGraphics().setColor(curColor);
       }
       notify();
    } 

    /**
     * Add the pole-zero that user entered from the dialog box. 
     */
    public void addpolezero(boolean conj, double px, double py, 
                                          double zx, double zy){
       
       InteractComponent ic1, ic2;
       InteractComponent ic3, ic4;

       ic1 = new InteractComponent("Pole", InteractComponent.Cross);
       ic1.setDrawingParam(Color.black,5, false,
                              InteractComponent.SymmetricOri);
       ic1.setInteractParam(new String("Pole real"), new String("Pole imag"),
                               InteractComponent.AllDegFree);

       ic1.xv = px;
       ic1.yv = py;
       ic1.setAssociation(1,-1);  // index number is not important here
       addInteractPoint(ic1, 1, px, py, false); 
       
       ic2 = new InteractComponent("Zero", InteractComponent.Circle);
       ic2.setDrawingParam(Color.black,5, false,
                           InteractComponent.SymmetricOri);
       ic2.setInteractParam(new String("Zero real"), new String("Zero imag"),
                           InteractComponent.AllDegFree);
       ic2.xv = zx;
       ic2.yv = zy;
       ic2.setAssociation(2,-1);  // index number is not important here
       addInteractPoint(ic2, 2, zx, zy, false); 

       if (conj == true){

           // add complex conjugate to pole
           ic3 = new InteractComponent("Pole", InteractComponent.Cross);
           ic3.setDrawingParam(Color.black,5, false,
                              InteractComponent.SymmetricOri);
           ic3.setInteractParam(new String("Pole real"), new String("Pole imag"),
                               InteractComponent.AllDegFree);

           ic3.xv = px;
           ic3.yv = -py;  // complex conjugate

           ic3.setAssociation(1,-1);  // index number is not important here
           addInteractPoint(ic3, 1, px, -py, false);
           ic1.setLink(ic3, InteractComponent.YaxisMirrorXaxisSynch);
           ic3.setLink(ic1, InteractComponent.YaxisMirrorXaxisSynch);
 
           // add complex conjugate to pole
           ic4 = new InteractComponent("Zero", InteractComponent.Circle);
           ic4.setDrawingParam(Color.black,5, false,
                              InteractComponent.SymmetricOri);
           ic4.setInteractParam(new String("Zero real"), new String("Zero imag"),
                               InteractComponent.AllDegFree);
           ic4.xv = zx;
           ic4.yv = -zy;  // complex conjugate
           ic4.setAssociation(2,-1);  // index number is not important here
           addInteractPoint(ic4, 2, zx, -zy, false); 
           ic2.setLink(ic4, InteractComponent.YaxisMirrorXaxisSynch);
           ic4.setLink(ic2, InteractComponent.YaxisMirrorXaxisSynch);
       }
       _polezerobox = null;
       processNewChanges();

    }


   /**
    * Initliazation.  It sets the length of the axises, number of data
    * set, and title of the plot.  For this plot, it creates a unit
    * circle for referencing, it is created as a set of data.
    */
   public void init(){

       super.init();

       addbutton = new Button("Add Pole/Zero");
       _c = new Choice();
       _c.addItem("Move Pole/Zero");
       _c.addItem("Delete Pole/Zero");

       add(addbutton);
       add(_c);

       setTitle("Pole Zero Plot ");
       _xBottom = -1.5;
       _xTop = 1.5;
       _yBottom = -1.5;
       _yTop = 1.5;

       setXRange(_xBottom, _xTop);
       setYRange(_yBottom, _yTop);

       _xLB = _xMin;
       _xUB = _xMax;
       _yLB = _yMin;
       _yUB = _yMax;
      
       setNumSets(5);
 
       // set the plot data for a unit circle
       // 180 points decimation

       for (double i = (-1)*Math.PI; i<=Math.PI; i=i+Math.PI/180){
             double a = Math.cos(i);
             double b = Math.sin(i);
             if (i == (-1)*Math.PI){
                 addPoint(0, a, b, false);
             } else {
                 addPoint(0, a, b, true);
             }
       }
 
       _selectsome = false;
       _oldx = _oldy = 0;
       _mode = 1;
    }


   /**
    * Close the plot window.  Called by frame to destroy the window.
    */

    /**
     * Set the editing mode.  1 for moving, 2 for deleting.
     */
    public void setmode(int m){
       _mode = m;
    }

    /**
     * process the action created by clicking the add pole/zero
     * button and change the mode of mouse (delete or move)
     */
    public boolean action(Event evt, Object arg){
        if (evt.target == addbutton){
             if (_polezerobox == null){
                   _polezerobox = new NewPoleZeroBox("Add Pole-Zero");
             }
             return true;
        } else if (evt.target instanceof Choice){
             if (((String)arg).equals("Move Pole/Zero")){
                  setmode(1);
                  return true;
             } else if (((String)arg).equals("Delete Pole/Zero")){
                  setmode(2);
                  return true;
             } else return false;
        } else return false;
    }

   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////

    private int _mode; // 1 - select mode, 2 - delete mode       
    private NewPoleZeroBox _polezerobox = null;
    private double xrange1 = -1.5;
    private double xrange2 = 1.5;
    private double yrange1 = -1.5;
    private double yrange2 = 1.5;
    private Button addbutton;
    private Choice _c;

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
 
          addpolezero(conjpair, px, py, zx, zy);
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
