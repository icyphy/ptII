/* An interactive plotting widget. 
 
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

import java.util.*;
import java.awt.*;
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// InteractPlot 
/**
  An interactive plotting widget.  It extends Plot class from plot package.
  This plot support user input on the plot.  It contain a list of 
  interact component that allow user changed the data graphically.
  Mouse listeners are used to process the user mouse event that 
  drag and drop the interact components.
  After change the data, it knows how to repaint the plot, and 
  recalculate the actual value of the interact components from coordinates.
  <p> 
  The list of interactive components are not ordered, they are not
  catagorized either.  But they have a field <i> dataset </i> that
  tells which dataset the component belongs to.
  <p> 
  For plot like pole-zero plot, the plot need to handle merging of those
  interact components that are very close together.  This option is set
  by <code> setMergeInteractComp() </code>.
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W% %G%
  @date: 3/2/98
 */

public class InteractPlot extends Plot {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Initialize the plot.  Set the frame for user data entry.  And set
     * up the mouse listener for select and dragging the interact objects.
     */ 
    public void init(){
        _cxyframe = new ChangeXY();
        SelectObjectListener selectlistener = new SelectObjectListener();
        this.addMouseListener(selectlistener);
        DragObjectListener draglistener = new DragObjectListener();
        this.addMouseMotionListener(draglistener);
    }
    
    public void setMergeInteractComp(boolean mergeflag){
        _mergeflag = mergeflag;
    }  
    /**
     * set the "view" of the plot, that handles the changes made by plot back
     * to the subject, and vise versa. <p>
     * @param v View object of this plot.
     */
    public void setView(PlotView v){
        _viewer = v;
    }

    /**
     * Delete all interactive components 
     */
    public void eraseInteractComponents(){
        _interactComponents.removeAllElements();
    }

    /**
     * Delete interactive components by dataset.
     * @param datas given data set number where the interact object will
     * delete. 
     */
    public void eraseInteractComp(int datas){
        for (int ind = 0; ind < _interactComponents.size();ind++){
             InteractComponent ic;
             ic  = (InteractComponent) _interactComponents.elementAt(ind);
             if (ic.getDataSetNum() == datas){
                 _interactComponents.removeElementAt(ind);
             }
        }
    } 

    /**
     * Add the interactive point with the given interactive
     * component.  It like <code> addPoint </code> in Plot class.
     * <p> 
     * @param interacomp the interact component to be added
     * @param dataset dataset of this interactcomponent.
     * @param x x-coordinate value
     * @param y y-coordinate value
     * @param connect if the data point will be connected.
     */ 
    public void addInteractPoint(InteractComponent interacomp, int dataset, 
                                 double x, double y, boolean connect){

         if (_interactComponents == null) _interactComponents = new Vector();
         _interactComponents.addElement(interacomp);
         addPoint(dataset, x, y, connect);
    }

    /** 
     * Select the interact component that the coordinate is on.  And draw its
     * value on the upper left hand corner of the plot.
     * <p>
     * @param x The X position of cursor. 
     * @param y The Y position of cursor. 
     */ 
    public synchronized void selectInteractcomp(int x, int y){
        _selected = _findClosest(x, y);
        Graphics graphics = getGraphics();
 
        // got a close one
        if (_selected != null){
            _selected.setSelected(true);
 
            // memerize the old coordinates
            _oldx = x;
            _oldy = y;
 
            // draw the string that represents the actual value of
            // interact component on the upper left side of the plot
            // display upto 5 digit precision

            graphics.setColor(Color.white); 
            _selected.drawValue(graphics, 5, 15, 5);
 
            repaint();
        }
    }
 
    /** 
     * Select the interact component that the coordinate is on for keyboard 
     * entry of the interact component value.  A window for data value entry
     * will be poped out. 
     * <p>
     * @param x The X position of cursor. 
     * @param y The Y position of cursor. 
     */ 
    public synchronized void selectInteractcompAndShowValueWin(int x, int y){

        InteractComponent closeic = _findClosest(x, y);
        if (_selected != null){ // something is already selected
            if (closeic == _selected){
                // selecting twice means deselecting
                _canceldataChange();
            } else {
               return;
            } 

        } else{
 
           // do the selection and pop out the dialog box
 
           _selected = _findClosest(x, y);
 
           if (_selected != null){
 
               _selected.setSelected(true);
 
               _cxyframe.setInteractComponent(_selected);
               _cxyframe.setVisible(true);
               repaint();
           }
        }
    }

    /** 
     * Dragging the interact component.  First it check if the
     * selected component exists.  The movement of dragging is limited
     * by interact component's degree of freedom.
     * The actual value of the selected component is updated as it is 
     * dragged around.  It will be painted on the upper left hand corner
     * of the plot.
     * <p>
     * @param x The X position of cursor. 
     * @param y The Y position of cursor. 
     */ 
    public synchronized void dragInteractcomp(int x, int y){
        if (_selected!=null){ // check if any component is selected

            Graphics graphics = getGraphics();
            // dragging can only occur if there is no window for user
            // to do data value entry
            if (!_cxyframe.isVisible()){

                // limit the movement of the dragging, not outside
                if (x < _ulx+10 || x > _lrx-10 
                 || y < _uly+10 || y > _lry-10) return;

                int xdiff = x - _oldx;
                int ydiff = y - _oldy;
                graphics.setXORMode(_background);
                // _setXORpaintMode();  // set the XOR mode to paint.

                // draw itself to erase the old image
                _selected.drawSelf(graphics);
 
                // update the position 
                _selected.movePosition(xdiff, ydiff);

                // draw itself at a new location
                _selected.drawSelf(graphics);

                graphics.setPaintMode(); 
                // _setNormalpaintMode(); // set back the paint mode
 
                double xv = (((double)(_selected.getXPosition()-_ulx))
                            /((double)(_lrx-_ulx)))*(_xMax-_xMin) + _xMin;
                double yv = _yMax - (((double)(_selected.getYPosition()-_uly))
                            /((double)(_lry-_uly)))*(_yMax-_yMin);
                _selected.changeValue(xv, yv);

                // put that actual value in the upper left hand corner
                // erase the previous value
                graphics.setColor(_background);
                graphics.fillRect(5,5,270,15);
 
                graphics.setColor(Color.white);
                _selected.drawValue(graphics, 5, 15, 5);
                _oldx = x;
                _oldy = y;
 
             }
         }
    }
 
    /* Finishing dragging the interact object.  
     * User finishes dragging the component to the desired location,
     * the selected object is unselected, the final value is calculated.
     * The view object is also notified about the change.
     * <p>
     * @param x The X position of cursor. 
     * @param y The Y position of cursor. 
     */
    public synchronized void finishDragInteractcomp(int x, int y){
         if (_selected != null){
             if (!_cxyframe.isVisible()){
                 
                 Graphics graphics = getGraphics();

                 int xdiff = x - _oldx;
                 int ydiff = y - _oldy;
                 _selected.movePosition(xdiff, ydiff);
 
                 // final actual value
                 double xv = (((double)(_selected.getXPosition()-_ulx))
                             /((double)(_lrx-_ulx)))*(_xMax-_xMin) + _xMin;
                 double yv = _yMax - (((double)(_selected.getYPosition()-_uly))
                             /((double)(_lry-_uly)))*(_yMax-_yMin);
                 _selected.changeValue(xv, yv);
                 _selected.setSelected(false);
                 graphics.setColor(_background);
                 graphics.fillRect(5,5,270,15);
 
                 // redraw the plot
                 repaint();
                 _viewer.moveInteractComp(_selected);
                 _selected = null;
             }
         }
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** 
     * Redraw the plot.  It will draw interactive component by calling
     * their <code> drawSelf() </code>.  But it will only draw the object
     * if it is can fit in the plot window.  Thus the the position coordinates
     * is calculated from the value coordinates of the interact components.
     * <p> 
     * First it checks if the merge flag is set or not, if it is set then 
     * it will merge all the close interact components, set the correct
     * multiplicity.  If ther merge flag is not set, simply draw the 
     * visible interact component. <p>
     * <p>
     * This method is derived from <code> Plot._drawPlot() </code>.  The
     * first this method calls is the super class's <code> _drawPlot </code>.
     * This ensures that the non-interactive parts of the plot is drawn.
     * <p> 
     * @param graphics Graphics element to draw the plot with.
     * @param clearfirst boolean indicates if plot should be cleared before
     *                   redraw.  
     */
    protected synchronized void _drawPlot(Graphics graphics, boolean clearfirst){

        super._drawPlot(graphics, clearfirst);
        Color curColor = graphics.getColor();
        InteractComponent iComp;
 
        Vector drawComp = new Vector();  // list of comp to be drawn
  
        // list of multiplicity to each comp
        int [] multi = new int[_interactComponents.size()];
 
        if (_mergeflag){ 
            // merge the interactcomponents if they are in the same dataset
            // and locate very close to each other. 
            // use this for loop to calculate the location of each components,
            // and check if they need merging
            for (int i=0;i<_interactComponents.size();i++){
                 iComp = (InteractComponent) _interactComponents.elementAt(i);
 
                 // find the coordinates
 
                 int ptx = (int)((iComp.getXValue() - _xMin)
                          /(_xMax-_xMin)*(double)(_lrx-_ulx));
                 int pty = (int)((_yMax - iComp.getYValue())
                          /(_yMax-_yMin)*(double)(_lry-_uly));

                 if (ptx>=0 && pty>=0){

                      // merge and draw only those are visible
                     iComp.setNewPosition(ptx+_ulx, pty+_uly);
 
                     boolean merge = false;
                  
                     // don't merge if the pole/zero is in middle of dragging
                     if (iComp.getSelected() == false){
 
                         // check iComp against all other interact component that
                         // is already in the draw vector
                         for (int j=0;j<drawComp.size();j++){
 
                             // if they are close merge them, increment the
                             // multiplicity count.
                             InteractComponent ic = (InteractComponent) drawComp.elementAt(j);
                             if ((Math.abs(ic.getYPosition()-iComp.getYPosition()) < CLOSE )
                              && (Math.abs(ic.getXPosition()-iComp.getXPosition()) < CLOSE )
                              && (ic.getDataSetNum() == iComp.getDataSetNum())){
                                  multi[j]++;
                                  merge = true;
 
                                  // break out the for loop, since one object can
                                  // only merge withe one other object.
                                  break;
                            }
                        }
                     }
 
                     if (merge == false){
                         // no merge, then add it to the draw vector
                         drawComp.addElement(iComp);
                         multi[drawComp.size()-1]=1;
                     }
                 }
             }
        } else {
             // don't merge at all 
             for (int i=0;i<_interactComponents.size();i++){
                  iComp = (InteractComponent) _interactComponents.elementAt(i);
                  // find the coordinates
 
                  int ptx = (int)((iComp.getXValue() - _xMin)
                            /(_xMax-_xMin)*(double)(_lrx-_ulx));
                  int pty = (int)((_yMax - iComp.getYValue())
                            /(_yMax-_yMin)*(double)(_lry-_uly));
                  if (ptx>=0 && pty>=0){
  
                      // draw only those are visible
                      iComp.setNewPosition(ptx+_ulx, pty+_uly);
                      drawComp.addElement(iComp);
                  }
             }
        }
    
        // now draw the final interact components
        // draw the interact component that is in the draw vector
        // and their multiplicity too.
        for (int i=0;i<drawComp.size();i++){
             InteractComponent iCompdraw = (InteractComponent) drawComp.elementAt(i);
             // seleted for dragging
             if (iCompdraw.getSelected() == true) _setXORpaintMode();  

             iCompdraw.drawSelf(graphics);

             if (multi[i]>1){
                 graphics.setColor(Color.white);
                 graphics.drawString(String.valueOf(multi[i]),
                                   iCompdraw.getXPosition()+iCompdraw.getWidth()/2+2,
                                   iCompdraw.getYPosition()+iCompdraw.getWidth()/2+2);
              }
              if (iCompdraw.getSelected()==true) _setNormalpaintMode();
        }

        graphics.setColor(curColor);
        notify();
    } 

 
    /** 
     * Change the selected interact object data value. 
     * The selected object will get unselected.
     * <p>
     * @param x new x-coordinate double value 
     * @param y new y-coordinate double value 
     */ 
    protected void _dataChange(double x, double y){
         if (_selected != null){
              _cxyframe.setVisible(false);
              _selected.changeValue(x, y);      
              _selected.setSelected(false);        
              repaint();
              _viewer.moveInteractComp(_selected);
              _selected = null;
         }
    }

    /** 
     * Cancel the keyboard entry change of the selected object.
     */ 
    protected void _canceldataChange(){
        if (_selected != null){
             _cxyframe.setVisible(false);
             _selected.setSelected(false);
             repaint();
             _selected = null;
        }
    }

 

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** list of interact components */
    protected Vector _interactComponents = new Vector(); 

    /** the interact component that is currently selected */
    protected InteractComponent _selected;   

    /** the reference to the view that contains this plot */ 
    protected PlotView _viewer;

    /** the permission to for editing on the plot */
    protected boolean _editpermission = true;

    /** permission to merge close interact component */
    protected boolean _mergeflag = false; 

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //
    // set the XOR paint mode, smoothing out the interact object's movement 
    // during dragging.  This allow an object to be erased by redrawing 
    // at the same location. 
    //
    private void _setXORpaintMode(){
        getGraphics().setXORMode(_background);
    }

    // 
    // set the normal paint mode. 
    //
    private void _setNormalpaintMode(){
        getGraphics().setPaintMode();
    }

    // 
    // Given a coordinate find the closest interact object that 
    // the coordinate is in.
    // 
    private InteractComponent _findClosest(int x, int y){
        int mindis = Integer.MAX_VALUE;
        int closest = -1;
        InteractComponent iComp;
        for (int i=0;i<_interactComponents.size();i++){
            iComp = (InteractComponent)_interactComponents.elementAt(i);
            // select the closest interact component.
            if (iComp.ifEntered(x, y) == true){
                int d = Math.min(Math.abs(x-iComp.getXPosition()), 
                                 Math.abs(y-iComp.getYPosition()));

                if (d < mindis) {
                    closest = i;
                    mindis = d;
                }
            }
        }
        if (closest != -1){
            iComp = (InteractComponent)_interactComponents.elementAt(closest);
        } else {
            iComp = null;
        }
        return iComp;
    } 
      
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    private int _oldx;
    private int _oldy; 
    private ChangeXY _cxyframe;
    private final int CLOSE = 3;

   //////////////////////////////////////////////////////////////////////////
   ////                         inner class                              ////

   //
   // Dialog to change the value of the interact components
   // when they are selected by the interactcomponent by control-click it
   // Uses frame instead of dialog, since you can't attach a dialog to a applet
   // without frame 
   //
   class ChangeXY extends Frame implements ActionListener {
 
   /**
    * Constructor.  Setup the widgets for different inputs.
    */
      public ChangeXY(){
   
          super();
          _entry1 = new TextField(10);
     
          _p1 = new Panel(); 
          _p1.setLayout(new FlowLayout(5, 5, 5));
          _p2 = new Panel();
          _p2.setLayout(new FlowLayout(5, 5, 5));
  
          _ok = new Button("OK");
          _ok.setActionCommand("   OK   ");
          _ok.addActionListener(this);
          _cancel = new Button("  CANCEL  ");
          _cancel.setActionCommand("CANCEL");
          _cancel.addActionListener(this);
          this.setLayout(new BorderLayout(5, 5));

      } 

      ////                     public methods                      ////
      void setInteractComponent(InteractComponent ic){
          
          _entry1 = null;
          _entry2 = null;
          String xv = String.valueOf(ic.getXValue());
          String yv = String.valueOf(ic.getXValue());

          setTitle(new String("Set "+ic.getName()+" value"));
          int deg = ic.getDegFreedom();

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
   
          _p1.removeAll(); 
          _p2.removeAll(); 
          this.removeAll(); 
          
          _p1.add("Left", new Label(ic.getxlabelName()));
          if (deg != 2){
              _entry1 = new TextField(10);
              _entry1.setText(xvchop);
              _p1.add("Center", _entry1);
          } else {
              _p1.add("Center", new Label(xvchop));
          }
 
          _p2.add("Left", new Label(ic.getylabelName()));
          if (deg != 1){
              _entry2 = new TextField(10);
              _entry2.setText(yvchop);
              _p2.add("Center", _entry2);
          } else {
              _p2.add("Center", new Label(yvchop));
          }
 
          this.add("North", _p1);
          this.add("Center", _p2);

          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(5, 5, 5));
          buttonpanel.add(_ok);   
          buttonpanel.add(_cancel);   
          this.add("South", buttonpanel);
          this.pack();
       }   
 
    // 
    // Reads the input from the dialog and sends to interactplot.
    //
       public void actionPerformed(ActionEvent evt){

          if (evt.getActionCommand().equals("OK")){
 
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
               this.setVisible(false);
               _dataChange(x, y);
          } else if (evt.getActionCommand().equals("CANCEL")){
               this.setVisible(false);
               _canceldataChange();
          }     
       }
 
       ////         private variables                ////
       private TextField _entry1;
       private TextField _entry2;
       private Panel _p1, _p2; 
       private Button _ok;
       private Button _cancel;

    }

    // 
    // Mouse listener to handle the mouse action in selecting, and 
    // unselecting the interact objects on the plot.
    // 
    public class SelectObjectListener implements MouseListener {
        public void mouseClicked (MouseEvent event) {
            if (event.isControlDown()){
                selectInteractcompAndShowValueWin(event.getX(), event.getY()); 
            }
        }

        public void mouseEntered(MouseEvent event) {
        }

        public void mouseExited(MouseEvent event) {
        }

        public void mousePressed(MouseEvent event) {
            if (event.isMetaDown()){
                InteractPlot.this.selectInteractcomp(event.getX(), event.getY());
            }
        }
        public void mouseReleased(MouseEvent event) {
            if (event.isMetaDown()){
                InteractPlot.this.finishDragInteractcomp(event.getX(), event.getY());
            }
        }

    }

    // 
    // Mouse listener to handle the mouse action in draging the 
    // selected interact objects on the plot.
    // 
    public class DragObjectListener implements MouseMotionListener {
        public void mouseDragged (MouseEvent event) {
            if (event.isMetaDown()){
                // drag the selected the interact object
                dragInteractcomp(event.getX(), event.getY());
            }
        }
        public void mouseMoved(MouseEvent event) {
        }
  
    }
} 
