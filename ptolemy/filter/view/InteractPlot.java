/* A plot that allows user interaction. 
 
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

import java.util.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// InteractPlot 
/**
 * This plot support user input on the plot.  It contain a list of 
 * interact component that allow user changed the data graphically.
 * <code> mouseUp(), mouseDrag(), mouseDown() </code> are used to 
 * drag and drop the interact component.
 * After change the data, it knows how to repaint the plot, and 
 * recalculate the actual value of the interact components from coordinates.
 * 
 * The list of interactive components are not ordered, they are not
 * catagorized either.  But they have a field <i> dataset </i> that
 * tells which dataset the component belongs to.
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */
public class InteractPlot extends Plot {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * set the "view" of the plot, that handles the changes made by plot back
     * to the subject, and vise versa.
     */
    public void setView(View v){
        _viewer = v;
    }

    /**
     * Delete all interactive components 
     */
    public void eraseInteractComponents(){
        _interactComponents = null;
    }

    /**
     * Delete interactive components by dataset 
     */
    public void eraseInteractComp(int datas){
        if (_interactComponents != null){
           for (int ind = 0; ind < _interactComponents.size();ind++){
               InteractComponent ic = (InteractComponent) _interactComponents.elementAt(ind);
               if (ic.getDataSetNum() == datas){
                   _interactComponents.removeElementAt(ind);
               }
           }
        }
    } 

    /**
     * set the XOR paint mode, smoothing out the interact object's movement 
     * during dragging.  This allow an object to be erased by redrawing 
     * at the same location. 
     */
    protected void _setXORpaintMode(){
        _graphics.setXORMode(_background);
    }

    /**
     * set the normal paint mode. 
     */
    protected void _setNormalpaintMode(){
        _graphics.setPaintMode();
    }

// calls draw plot in super class
    /**
     * Calls redraw in Plot.  This is a hack to allow PoleZeroPlot.
     * Definitely a FIXME
     */
    public synchronized void olddrawPlot(Graphics g, boolean clearfirst){
        super.drawPlot(g, clearfirst);
    }

    /**
     * Redraw the plot.  It will draw interactive component by calling
     * their <code> drawSelf() </code>.  But it will only draw the object
     * if it is can fit in the plot, by find the coordinates for that object
     */  
    public synchronized void drawPlot(Graphics g, boolean clearfirst){

        super.drawPlot(g, clearfirst);
        if (_interactComponents != null){
          Color curColor = _graphics.getColor();
          for (int i=0;i<_interactComponents.size();i++){
               InteractComponent iComp = (InteractComponent) _interactComponents.elementAt(i);
               if (iComp.selected == true) _setXORpaintMode();  // seleted for dragging 
               // some bugs in finding new location of interactive 
               // objects after zooming, thus old method is commented
               // out... - William Wu
               // find the coordinates
               // int ptx = (int)((Math.abs(iComp.xv - _xMin))/(Math.abs(_xMax-_xMin))*(double)(_lrx-_ulx)); 
      
                // this is the new method, seems working out  
               int ptx = (int)((iComp.xv - _xMin)/(_xMax-_xMin)*(double)(_lrx-_ulx)); 
               // int pty = (int)((Math.abs(_yMax - iComp.yv))/(Math.abs(_yMax-_yMin))*(double)(_lry-_uly)); 

                // this is the new method, seems working out  
               int pty = (int)((_yMax - iComp.yv)/(_yMax-_yMin)*(double)(_lry-_uly)); 

               if (ptx>=0 && pty>=0){      // only update the component 
                                           // location if they are still in the
                                           // plot boundary. 
                                           
                   iComp.x = ptx+_ulx;
                   iComp.y = pty+_uly;
                   iComp.drawSelf(_graphics);
               } else {
                   iComp.x = 0;
                   iComp.y = 0;
               }
               if (iComp.selected==true) _setNormalpaintMode(); // set paintmode back to normal.
          }
          _graphics.setColor(curColor);
       }
       notify();
    } 

    /**
     * Handle the event when user press down on the right mouse button for select 
     * the interact component.  With the given cursor's coordinates
     * it goes through the list of interact components, and find the
     * closest one to the cursor.
     */
    public boolean mouseDown(Event evt, int x, int y){
 
       int closest = -1;
       int mindis = 200; // minimum distance
  
       if (evt.metaDown()==true){  // select only when right button is pressed
          if (_interactComponents == null) return false;
          for (int i=0;i<_interactComponents.size();i++){
              InteractComponent iComp = (InteractComponent)_interactComponents.elementAt(i);
              // if cursor is in more than one bounding box, select the
              // the closest. 
              if (iComp.ifselect(x, y) == true){
                  int d = Math.min(Math.abs(x-iComp.x), Math.abs(y-iComp.y));
                  if (d < mindis) {
                      closest = i;
                      mindis = d;
                  }
              }
          }

          // got a close one 
          if (closest != -1){
              InteractComponent iComp = (InteractComponent)_interactComponents.elementAt(closest);
              iComp.selected = true;
            
              // set this flag, so the plot know a component is seleted
              _selectsome = true;
              _oldx = x;
              _oldy = y;
              _graphics.setColor(Color.black);
           
              // draw the string that represents the actual value of
              // interact component on the upper left side of the plot 
              // display upto 5 digit precision

              String xv = String.valueOf(iComp.xv);
              String yv = String.valueOf(iComp.yv);
              String xvchop, yvchop;
              int pt = xv.indexOf(".");
              if ((pt != -1) && (xv.length() > 7)){
                  xvchop = (xv.substring(0,pt+6)).trim();
              } else {
                  xvchop = xv;
              }
              pt = yv.indexOf(".");
              if ((pt != -1) && (yv.length()>7)){
                  yvchop = (yv.substring(0,pt+6)).trim();
              } else {
                  yvchop = yv;
              }
    
              _oldstr = new String(iComp.getxlabelName()+":  "+xvchop+"  "+iComp.getylabelName()+":  "+yvchop);
              _graphics.drawString(_oldstr, 5, 15);
          }
          return true;
       } else if (evt.controlDown()==true){ // select interactcomponent for change data
          // don't do any thing now, open the dialog box only when button release
          return true; 
       } else { // for zooming
         return super.mouseDown(evt, x, y);
       }
    }


    /**
     * Dragging the interact component.  First it check if the
     * selection flag is set or not, then it limits the movement of dragging.
     * Updates the actual value of the selected component as it is draged around.
     * It also checks for the degree of freedom the component can have, and
     * use XOR mode for repaint the component.
     */    
    public boolean mouseDrag(Event evt, int x, int y){
         if (evt.metaDown()==true){
             if (_selectsome==true){ // check if any component is selected

                // limit the movement of the dragging, not outside
                if (x < _ulx+10 || x > _lrx-10 || y < _uly+10 || y > _lry-10) return false;
                int xdiff = x - _oldx;
                int ydiff = y - _oldy;
                for (int i=0;i<_interactComponents.size();i++){
                    InteractComponent iComp = (InteractComponent)_interactComponents.elementAt(i);
                    if (iComp.selected == true){
                        _setXORpaintMode();  // set the XOR mode to paint.
                        iComp.drawSelf(_graphics); // draw itself to erase the old image
                       
                        // check degree of freedom 
                        if (iComp.getDegFreedom() == InteractComponent.XaxisDegFree){
                            iComp.x = iComp.x + xdiff;
                            ydiff = 0;
                        } else if (iComp.getDegFreedom() == InteractComponent.YaxisDegFree){
                            iComp.y = iComp.y + ydiff;
                            xdiff = 0;
                        } else if (iComp.getDegFreedom() == InteractComponent.AllDegFree){
                            iComp.x = iComp.x + xdiff;
                            iComp.y = iComp.y + ydiff;
                        }

                        iComp.drawSelf(_graphics); // draw itself at a new location
                        _setNormalpaintMode(); // set back the paint mode

                        // figure out the actual value 
                        iComp.xv = (((double)(iComp.x-_ulx))/((double)(_lrx-_ulx)))*(_xMax-_xMin) + _xMin;
                        iComp.yv = _yMax - (((double)(iComp.y-_uly))/((double)(_lry-_uly)))*(_yMax-_yMin);
                        // put that actual value in the upper left hand corner
                        _graphics.setColor(this.getBackground());
                        _graphics.fillRect(5,5,270,15);
                        _graphics.setColor(Color.black);
                        // display upto 5 digit precision

                       String xv = String.valueOf(iComp.xv);
                       String yv = String.valueOf(iComp.yv);
                       String xvchop, yvchop;
                       int pt = xv.indexOf(".");
                       if ((pt != -1) && (xv.length() > 7)){
                          xvchop = (xv.substring(0,pt+6)).trim();
                       } else {
                          xvchop = xv;
                       }
                       pt = yv.indexOf(".");
                       if ((pt != -1) && (yv.length()>7)){
                          yvchop = (yv.substring(0,pt+6)).trim();
                       } else {
                          yvchop = yv;
                       }
                        _oldstr = new String(iComp.getxlabelName()+":  "+xvchop+"  "+iComp.getylabelName()+":   "+yvchop);
                        _graphics.drawString(_oldstr, 5, 15);

                        // process pair's value 
                        InteractComponent pair = iComp.getPairIC();
                        if (pair != null){
                             if (iComp.getPairICType() == InteractComponent.YaxisMirrorXaxisSynch){
                                  pair.xv = iComp.xv; 
                                  pair.yv = -iComp.yv; 
                             }
                        }
  
                    }
                }
                _oldx = x;
                _oldy = y;
            }
            return true;
         } else if (evt.controlDown()==true){ // select interactcomponent for change data
          // don't do any thing now, open the dialog box only when button release
            return true; 
         } else { //for zoom
             return super.mouseDrag(evt, x, y);
         }
    }

    /**
     * Handle the event when user release the mouse button.  When the 
     * user finishes dragging the component to the desired location, 
     * the selected object is unselected, the final value is calculated,
     * and <code> processNewChanges() </code> is called, which will
     * take care of any changes to the dataset, and/or send the new
     * changes to the original data object.  
     */ 
    public boolean mouseUp(Event evt, int x, int y){
       if (evt.metaDown()==true){
          if (_selectsome==true){
             int xdiff = x - _oldx;
             int ydiff = y - _oldy;
             for (int i=0;i<_interactComponents.size();i++){
                 InteractComponent iComp = (InteractComponent)_interactComponents.elementAt(i);
                 if (iComp.selected == true){ // if any selected
  
                        // check for degree of freedom
                        if (iComp.getDegFreedom() == InteractComponent.XaxisDegFree){
                            iComp.x = iComp.x + xdiff;
                        } else if (iComp.getDegFreedom() == InteractComponent.YaxisDegFree){
                            iComp.y = iComp.y + ydiff;
                        } else if (iComp.getDegFreedom() == InteractComponent.AllDegFree) {
                            iComp.x = iComp.x + xdiff;
                            iComp.y = iComp.y + ydiff;
                        }
            
                        // final actual value
                        iComp.xv = (((double)(iComp.x-_ulx))/((double)(_lrx-_ulx)))*(_xMax-_xMin) + _xMin;
                        iComp.yv = _yMax - (((double)(iComp.y-_uly))/((double)(_lry-_uly)))*(_yMax-_yMin);
                        _graphics.setColor(this.getBackground());
                        _graphics.fillRect(5,5,270,15);

                        // process pair's value 
                        InteractComponent pair = iComp.getPairIC();
                        if (pair != null){
                             if (iComp.getPairICType() == InteractComponent.YaxisMirrorXaxisSynch){
                                  pair.xv = iComp.xv; 
                                  pair.yv = -iComp.yv; 
                             }
                        }
                  }
             }

             processNewChanges();

             // redraw the plot
             drawPlot(_graphics, true);
          }
          _selectsome = false;
          return true;
       } else if (evt.controlDown()==true){ // select interactcomponent for change data
          if (_cxyframe != null) return false; // a window is already
                                               // active so can't create
                                               // another 

          // do the selection and pop out the dialog box
          int closest = -1;
          int mindis = 200; // minimum distance
          if (_interactComponents == null) return false;
          for (int i=0;i<_interactComponents.size();i++){
              InteractComponent iComp = (InteractComponent)_interactComponents.elementAt(i);
              // if cursor is in more than one bounding box, select the
              // the closest. 
              if (iComp.ifselect(x, y) == true){
                  int d = Math.min(Math.abs(x-iComp.x), Math.abs(y-iComp.y));
                  if (d < mindis) {
                      closest = i;
                      mindis = d;
                  }
              }
          }
 
          // got a close one 
          if (closest != -1){
              InteractComponent iComp = (InteractComponent)_interactComponents.elementAt(closest);
              iComp.selected = true;
      
              // set this flag, so the plot know a component is seleted
              _selectsome = true;
              _oldx = x;
              _oldy = y;
              String title = new String("Set "+iComp.getName()+" value");
              
              _cxyframe = new ChangeXY(this, title, iComp.xv, iComp.yv,
                       iComp.getxlabelName(), iComp.getylabelName(), 
                       iComp.getDegFreedom());
          }
          return true;

       } else {
         return super.mouseUp(evt, x, y);
       }
    }
 
    /**
     * Set the plot back to original scale.
     */
    public synchronized void fillPlot(){
 
       setXRange(_xLB, _xUB);
       setYRange(_yLB, _yUB);
       paint(_graphics);
    }


    /**
     * Add the interactive point with the given interactive
     * component.
     */ 
    public void addInteractPoint(InteractComponent interacomp, int dataset, 
                                 double x, double y, boolean connect){

         if (_interactComponents == null) _interactComponents = new Vector();
         _interactComponents.addElement(interacomp);
         addPoint(dataset, x, y, connect);
    }
 
    public void processNewChanges(){

         if (_interactComponents!=null){
            for (int ind = 0; ind < _interactComponents.size();ind++){
                InteractComponent ic = (InteractComponent) _interactComponents.elementAt(ind);
                if (ic.selected == true){
                    
                    PlotPoint pp = _getPlotPoint(ic.getDataSetNum(), 
                                                 ic.getDataIndexNum());
                    if (pp != null){
                        if (ic.getDegFreedom() == InteractComponent.XaxisDegFree){
                            pp.x = ic.xv;
                        } else if (ic.getDegFreedom() == InteractComponent.YaxisDegFree){
                            pp.y = ic.yv;
                        } else if (ic.getDegFreedom() == InteractComponent.AllDegFree){
                            pp.x = ic.xv;
                            pp.y = ic.yv;
                        }

                    }
                    ic.selected = false;        
                }
            }
         }
         repaint();
         _viewer.newChange(_interactComponents);
    }

    public void dataChange(double x, double y){
         if (_interactComponents != null){
             for (int ind = 0; ind < _interactComponents.size(); ind++){
                 InteractComponent ic = (InteractComponent) _interactComponents.elementAt(ind);
                 if (ic.selected == true){
                      // update both the plot point and interact component 
                      PlotPoint pp = _getPlotPoint(ic.getDataSetNum(), 
                                                   ic.getDataIndexNum());
                      if (ic.getDegFreedom() == InteractComponent.XaxisDegFree) { // x-axis only
                          ic.xv = x;
                          if (pp!=null) pp.x = ic.xv; 
                      } else if (ic.getDegFreedom() == InteractComponent.YaxisDegFree) { // y-axis only
                          ic.yv = y;
                          if (pp!=null) pp.y = ic.yv; 
                      } else if (ic.getDegFreedom() == InteractComponent.AllDegFree) { // both 
                          ic.xv = x;
                          ic.yv = y;
                          if (pp!=null) { pp.x=ic.xv; pp.y=ic.yv; } 
                      }
                      ic.selected = false;
                 }
             }
             repaint();
        }
        _cxyframe = null;
    }            

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////
    protected Vector _interactComponents; 
    protected double _xLB, _xUB, _yLB, _yUB;
    protected boolean _selectsome; 
    protected int _oldx;
    protected int _oldy;  
    protected String _oldstr;
    protected View _viewer;
    protected ChangeXY _cxyframe;
}

/**
 * Dialog to change the value of the interact components
 * when they are selected by the interactcomponent by control-click it
 * Uses frame instead of dialog, since you can't attach a dialog to a applet
 * without frame 
 */
class ChangeXY extends Frame {
 
   //////////////////////////////////////////////////////////////////////////
   ////                         public methods                           ////
   /**
    * Constructor.  Setup the widgets for different inputs.
    */
   public ChangeXY(InteractPlot p, String title, double v1, double v2, 
                     String message1, String message2, int deg){
      // super(parent, title, false);
      super(title);
      _plot = p;
      _entry1 = new TextField(10);
      _entry2 = new TextField(10);

      Panel p2=null;
      Panel p3=null;
      
      this.setLayout(new BorderLayout(5, 5));
 
      // depends on the messages, if both are null, then there is no point 
      // creating this dialog, if one is null then only one text entry is displayed

      String xv = String.valueOf(v1);
      String yv = String.valueOf(v2);
      String xvchop, yvchop;
      int pt = xv.indexOf(".");
      if ((pt != -1) && (xv.length()>7)){
           xvchop = (xv.substring(0,pt+6)).trim();
      } else {
           xvchop = xv;
      }
      pt = yv.indexOf(".");
      if ((pt != -1) && (yv.length()>7)){
           yvchop = (yv.substring(0,pt+6)).trim();
      } else {
           yvchop = yv;
      }
    

      if (deg == 0) return ; 

      p2 = new Panel();
      p2.setLayout(new FlowLayout(5, 5, 5));

      if (deg != 2){
          p2.add("Left", new Label(message1));
          _entry1.setText(xvchop);
          p2.add("Center", _entry1);
      } else {
          p2.add("Left", new Label(message1));
          p2.add("Center", new Label(xvchop));
      }

      p3 = new Panel();
      p3.setLayout(new FlowLayout(5, 5, 5));
 
      if (deg != 1){
          p3.add("Left", new Label(message2));
          _entry2.setText(yvchop);
          p3.add("Center", _entry2);
      } else {
          p2.add("Left", new Label(message2));
          p2.add("Center", new Label(yvchop));
      }
 
      this.add("North", p2);
      this.add("Center", p3);
 
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
 
 
      if (evt.target == _ok){
 
          double x = 0.0;
          double y = 0.0;

          if (_entry1 != null){ 
              if (_entry1.getText().equals("")){
                  x = 0.0;
              } else {
                  Double a = new Double(_entry1.getText());
                  x = a.doubleValue();
              }
          }
          if (_entry2 != null){ 
              if (_entry2.getText().equals("")){
                  y = 0.0;
              } else {
                  Double b = new Double(_entry2.getText());
                  y = b.doubleValue();
              }
          }
          this.hide();
          this.dispose();
 
          _plot.dataChange(x, y);
          return true;
       } else return false;
 
   }
 
   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////
   private TextField _entry1;
   private TextField _entry2;
   private Button _ok;
   private InteractPlot _plot;
}
 
