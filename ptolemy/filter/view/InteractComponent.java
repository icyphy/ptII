/* Interact Component, components allow user to do "drag and drop". 

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

//FIXME: add limit to where the interact component will be draw to.
//////////////////////////////////////////////////////////////////////////
//// InteractComponent 
/**
   A graphical component that allow user to do "drag and drop" operation
   to change its location.  It include many basic shapes, and provide
   method to draw these shape.  The location of the component reference to 
   the plot is stored in variable <i> _x, _y </i>, corresponding
   actual value is stored in <i> _xv, _yv </i>.  The relation between the two
   sets of data is related to the current plot parameters, like xMax, xMin,
   etc.  Since this class can not get these data values,  it is upto the
   the plot object to keep (x,y) and (xv, yv) in synch.  A bounding
   box is used to determine if the cursor is on the component for selecting.  
   The movement of the component is limited variables <i> degFreedom </i>, 
   Orientation of the component is specify by <i> orientation </i>. i
   <p>
   FIXME: <p>
   Another useful parameter could be an area that the interact component is 
   allowed to be drag around. 
   <p> 
   @author: William Wu (wbwu@eecs.berkeley.edu)
   @version: %W% %G%
   @date: 3/2/98
 */

 
public class InteractComponent {


    /**
     * Constructor. Set name and shape of the component.  Initial value
     * is set at this time.  The location is set by the plot when the component
     * is drawn. 
     * @param name name of this interact component
     * @param shape enumeration integer indicate the shape
     * @param x initial x-coordinate value 
     * @param y initial y-coordinate value 
     */
    public InteractComponent(String name, int shape, double x, double y){
        _type = shape;
        _name = name;
        _xv = x;
        _yv = y;
    }     

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Change the value of this interact component.  The degree of freedom
     * is checked before the value is assigned.
     * <p> 
     * @param newxv new x-axis value
     * @param newyv new y-axis value
     */ 
    public void changeValue(double newxv, double newyv){
        // check degree of freedom
        if (getDegFreedom() == InteractComponent.XAXISDEGFREE){
            _xv = newxv;
        } else if (getDegFreedom() == InteractComponent.YAXISDEGFREE){
            _yv = newyv;
        } else if (getDegFreedom() == InteractComponent.ALLDEGFREE){
            _xv = newxv;
            _yv = newyv;
        }
    }

    /**
     * Set the position of this interact component.  The degree of freedom
     * <b> NOT </b> checked.  Which means the given location is still 
     * corresponding to the data value.  (Change in location but still
     * staying at the same value happens during zoom-in or zoom-out.)
     * <p> 
     * @param newx new location in x axis 
     * @param newy new location in y axis 
     */
    public void setNewPosition(int newx, int newy){
        _x = newx;
        _y = newy;
    }

    /**
     * Move the position of the interact component to a new position
     * with the given offsets.  Degree of freedom is checked.
     * Change to the position only occur on the axis(es) with right
     * permission.  This method is called when user is dragging 
     * the object.<p>
     * @param xdiff x-axis location offset  
     * @param ydiff y-axis location offset  
     */    
    public void movePosition(int xdiff, int ydiff){
        // check degree of freedom 
        if (getDegFreedom() == InteractComponent.XAXISDEGFREE){
            _x = _x + xdiff;
        } else if (getDegFreedom() == InteractComponent.YAXISDEGFREE){
            _y = _y + ydiff;
        } else if (getDegFreedom() == InteractComponent.ALLDEGFREE){
            _x = _x + xdiff;
            _y = _y + ydiff;
        }
    }

    /**
     * Get the dataset number. <p>
     * @return the dataset number of  this object.
     */
    public int getDataSetNum(){
           return _dataset;
    }     

    /**
     * Get the data index number. <p>
     * @return the data index number of this object in the dataset.
     */
    public int getDataIndexNum(){
           return _dataindex;
    }     

    /**
     * Set default drawing parameters.  Set the parameters for the 
     * appearences of this interact object.  They will be used
     * when the object is drawn.  Bounding box is also set in this
     * class.<p>  
     * @param color default color of this object.  It is not advised
     *        to set the color yellow, since it is used when the object
     *        is selected.  (or white, it is used when the object is high
     *        lighted. 
     * @param width width of the object ( or length for lines)
     * @param filled boolean indicate if the object should be filled with color
     *        or empty. 
     * @param ori orientation of the object.  It should be one of the enum
     *        on orientation given in this class    
     */
    public void setDrawingParam(Color color, int width, boolean filled, int ori){
        _color = color;
        _savedcolor = color;
        _filled = filled;
        _width = width;
        _orientation = ori;
 
        if (_type == LINE){
            if (_orientation == HORIZONTALORI){
                _setboundingbox(_width/2, 10); 
            } else {
                _setboundingbox(10, _width/2); 
            }
        } else {
           _setboundingbox(_width/2, _width/2);
        }
    }


    /**
     * Get the name of the component.
     * @return name of the component.  
     */
    public String getName(){
        return _name;
    }

    /**
     * Get the width of the component  
     * @return width of the component.  
     */
    public int getWidth(){
        return _width;
    }

    /**
     * Set the parameters needed for user interaction.  These parameters
     * will be seen when user interacts with the component (like moving
     * or changing value). <p> 
     * @param xlab x-axis label 
     * @param ylab y-axis label 
     * @param degFreedom degree of freedom the user can interact with the
     * object.  It should be one of the enum describing degree of freedom
     * in this file.
     */
    public void setInteractParam(String xlab, String ylab, int degFreedom){
        _xlabelName = xlab;
        _ylabelName = ylab;
        _degFreedom = degFreedom;
    }

    /**
     * Get the label name of x-axis. <p>
     * @return x-axis label.
     */
    public String getxlabelName(){
        return _xlabelName;
    }

    /**
     * Get the label name of y-axis.<p>
     * @return y-axis label.
     */
    public String getylabelName(){
        return _ylabelName;
    }

    /**
     * Get the degree of freedom of this component.<p> 
     * @return degree of freedom of this component. 
     */
    public int getDegFreedom(){
        return _degFreedom;
    }
 
    /**
     * Check if the given coordinate falls into the bounding box. 
     * If it does, then return true, else return false.
     * @param xpos given x-axis coordinate 
     * @param ypos given y-axis coordinate 
     * @return boolean value indicate if the given coordinate is inside
     * the bounding box. 
     */ 
    public boolean ifEntered(int xpos, int ypos){
       if ((xpos > (_x - _boundingwidth)) 
        && (xpos < (_x + _boundingwidth))
        && (ypos > (_y - _boundingheight)) 
        && (ypos < (_y + _boundingheight)))
            return true;
       else return false;
    } 

    /**
     * Set the selected flag of this object.  Drawing color will changed
     * to yellow, if the flag is true.  If not true, then the original
     * color is restored.<p>    
     * @param select select flag value
     */ 
    public void setSelected(boolean select){
       this._selected = select;
       if (select == true){
           _savedcolor = _color;
           _color = Color.yellow;
       } else {
           _color = _savedcolor; 
       }
    }

    /**
     * Get the select flag value. <p>
     * @return select flag value
     */
    public boolean getSelected(){
       return _selected;
    }

    /**
     *  Draw the interact component.  Interact component is drawn according
     *  to the drawing parameters and shape with the given graphics. <p>
     *  @param g Graphics object to be used for drawing 
     */
    public void drawSelf(Graphics g){
       
       Color saved = g.getColor();

       g.setColor(_color);
       switch (_type) {
       case CIRCLE:
           {
              // circle
              if (_filled == true){
                  g.fillOval(_x-_width/2, _y-_width/2, _width, _width);
              } else {  
                  g.drawOval(_x-_width/2, _y-_width/2, _width, _width);
              }
              break;
           }
       case CROSS:
           {
              // cross
              int hwidth = _width/2;
              g.drawLine(_x-hwidth, _y+hwidth, _x+hwidth, _y-hwidth);
              g.drawLine(_x-hwidth, _y-hwidth, _x+hwidth, _y+hwidth);
              break;
           }
       case TRIANGLE:
           {
              // triangle
              int vertx[]={_x, _x-_width/2, _x+_width/2 };
              int verty[]={_y-_width/2, _y+_width/2, _y+_width/2 };
              if (_filled == true){
                  g.fillPolygon(vertx, verty, 3);
              } else {  
                  g.drawPolygon(vertx, verty, 3);
              }
              break;
           }
       case SQUARE:
           {
              // square
              if (_filled == true){
                  g.fillRect(_x-_width/2, _y-_width/2, _width, _width);
              } else {
                  g.drawRect(_x-_width/2, _y-_width/2, _width, _width);
              }
              break;
           }
       case PLUS:
           {  
              // plus
              g.drawLine(_x-_width/2, _y, _x+_width/2, _y);
              g.drawLine(_x, _y-_width/2, _x, _y+_width/2);
              break;
           } 
       case LINE:
           {
              // line
              int hwidth = _width/2; 
              if (_orientation == HORIZONTALORI){
                   g.drawLine(_x-hwidth, _y, _x+hwidth, _y);
              } else if (_orientation == VERTICALORI){
                   g.drawLine(_x, _y-hwidth, _x, _y+hwidth);
              }
              break;
            }
       default:
       } 
       g.setColor(saved);        

    }


    /**
     * Draw the value of this object.  Given the Graphics object, desired
     * location and max number digits of precision after the decimal point,
     * the value will be drawn with these specifications. <p>
     * @param g Graphics object used for drawing.
     * @param x desired location x-axis coordinate, where the value 
     *          will be printed. 
     * @param y desired location x-axis coordinate, where the value 
     *          will be printed. 
     * @param prec number of digit after decimal point the value will be
     *        displaying.
     */
    public void drawValue(Graphics g, int x, int y, int prec){

          String xv = String.valueOf(_xv);
          String yv = String.valueOf(_yv);
          String xvchop, yvchop; 

          // chop the text of the value to the desired precision
          int pt = xv.indexOf(".");
          if ((pt != -1) && (xv.length() > prec+2)){
               xvchop = (xv.substring(0,pt+(prec+1))).trim();
          } else {
               xvchop = xv;
          }

          // chop the text of the value to the desired precision
          pt = yv.indexOf(".");
          if ((pt != -1) && (yv.length() > prec+2)){
               yvchop = (yv.substring(0,pt+(prec+1))).trim();
          } else {
               yvchop = yv;
          }
          String str = new String(getxlabelName()+":  "+xvchop+"  "+getylabelName()+":  "+yvchop);
// System.out.println("drawing the value now: "+str); 
          g.setColor(Color.white);
          g.drawString(str, x, y);
 
    } 

    /**
     * Get component value at x-coordinate.
     * @return component value at x-coordinate.
     */ 
    public double getXValue(){
       return _xv;
    }

    /**
     * Get component value at y-coordinate.
     * @return component value at y-coordinate.
     */ 
    public double getYValue(){
       return _yv;
    }

    /**
     * Get component position at x-coordinate.
     * @return component position at x-coordinate.
     */ 
    public int getXPosition(){
       return _x;
    }

    /**
     * Get component position at y-coordinate.
     * @return component position at y-coordinate.
     */ 
    public int getYPosition(){
       return _y;
    }

    /**
     * Set the dataset and data index this interact component is corresponding 
     * to.  The dataset number and index number is similiar to the dataset
     * and data index in Plot.
     * <p>
     * @param datas data set number.
     * @param ind data index number.
     */ 
    public void setDatasetIndex(int datas, int ind){
       _dataindex = ind;
       _dataset = datas;
    } 



    //////////////////////////////////////////////////////////////////////////
    ////                         public variable                          ////

    // Constants that describe the shape 
    public final static int CIRCLE = 1;   
    public final static int CROSS = 2;   
    public final static int TRIANGLE = 3;   
    public final static int SQUARE = 4;   
    public final static int PLUS = 5;   
    public final static int LINE = 6;  

    // Constants that desicribe the _orientation
    public final static int SYMMETRICORI = 0; 
    public final static int HORIZONTALORI = 1; 
    public final static int VERTICALORI =  2; 

    // Constants that desicribe the degree of freedom
    public final static int NONEDEGFREE = 0; 
    public final static int XAXISDEGFREE = 1; 
    public final static int YAXISDEGFREE = 2; 
    public final static int ALLDEGFREE = 3; 

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                        ////
  
     //
     // Set the bounding box for selection.
     // 
    private void _setboundingbox(int x, int y) {
       _boundingwidth = x;
       _boundingheight = y;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variable                          ////

    private double _xv, _yv;    // holds value this interactcomponent represent

    private double _leftXLimit = Double.NEGATIVE_INFINITY;
    private double _rightXLimit = Double.POSITIVE_INFINITY;
    private double _upYLimit = Double.POSITIVE_INFINITY;
    private double _lowYLimit = Double.NEGATIVE_INFINITY;

    private int _x, _y;  // holds the location of this interact component.
    private int _dataset;  // tells which group of data this component belongs
    private int _dataindex;
    private boolean _selected; // set this flag if the component is seleted

    // used to specify on the plot upper left corner what each coordinates
    // represents.
    private String _xlabelName, _ylabelName;
    private Color _color = Color.black;
    private Color _savedcolor = Color.black;

    private int _type = CIRCLE ; 
    private String _name; 
    private boolean _filled = false;  
    private Vector _vertexes = null;
    private int _width = 10;
    private int _orientation = SYMMETRICORI; 
    private int _degFreedom = ALLDEGFREE; 

    private int _boundingwidth, _boundingheight;
}
