/* Interact Component, components allow user to drag and drop. 

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
//// InteractComponent 
/**
 *  A graphical component that allow user to drag and drop
 *  its location.  This is an abstract class, since it includes
 *  an abstract method that know how to draw itself, since there
 *  is no default component, thus the abstract modifier.  The location
 *  is expressed in variable <bold> x, y </bode>, corresponding
 *  actual value is stored in <bold> xv, yv </bode>.  A bounding
 *  box is used for selecting the component.  The movement of the
 *  component is limited variables <b> degFreedom </b>, orientation of
 *  the component is specify by <b> orientation </b>.  
 *
 *  Author: William Wu
 *  Version:
 *  Date: 3/2/98
 */

 
public class InteractComponent {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Constructor, set name and shape of the component
     */
    public InteractComponent(String name, int shape){
        _type = shape;
        _name = name;
    }     

    /**
     * Specifies which data set and data point index this component belongs
     */

    /**
     * Get the dataset number
     */
    public int getDataSetNum(){
           return _dataset;
    }     

    /**
     * Get the data index number
     */
    public int getDataIndexNum(){
           return _dataindex;
    }     

    /**
     * Set default parameters for drawing 
     */
    public void setDrawingParam(Color color, int width, boolean filled, int ori){
        _color = color;
        _filled = filled;
        _width = width;
        _orientation = ori;
 
        if (_type == Line){
            if (_orientation == HorizontalOri){
                _setboundingbox(_width/2, 10); 
            } else {
                _setboundingbox(10, _width/2); 
            }
        } else {
           _setboundingbox(_width/2, _width/2);
        }
    }


    /**
     * Get the name of the component  
     */
    public String getName(){
        return _name;
    }

    /**
     * Get the width of the component  
     */
    public int getWidth(){

        return _width;
    }

    /**
     * Set the parameters needed for user interaction.
     */
    public void setInteractParam(String xlab, String ylab, int degFreedom){
        _xlabelName = xlab;
        _ylabelName = ylab;
        _degFreedom = degFreedom;
    }

    /**
     * Get the label name of x-axis.
     */
    public String getxlabelName(){
        return _xlabelName;
    }

    /**
     * Get the label name of y-axis.
     */
    public String getylabelName(){
        return _ylabelName;
    }

    /**
     * Get the degree of freedom of this component 
     */
    public int getDegFreedom(){
        return _degFreedom;
    }
 
    /**
     *  Check if the given coordinate falls into the bounding box. 
     *  If it does, then return true.
     */ 
    public boolean ifselect(int xpos, int ypos){
       if ((xpos > x - _boundingwidth) 
        && (xpos < x + _boundingwidth)
        && (ypos > y - _boundingheight) 
        && (ypos < y + _boundingheight))
            return true;
       else return false;
    } 

    /**
     *  Draw the interact component.
     */
    public void drawSelf(Graphics g){
       
       Color saved = g.getColor();

       g.setColor(_color);
       switch (_type) {
       case Circle:
           {
              // circle
              if (_filled == true){
                  g.fillOval(x-_width/2, y-_width/2, _width, _width);
              } else {  
                  g.drawOval(x-_width/2, y-_width/2, _width, _width);
              }
              break;
           }
       case Cross:
           {
              // cross
              int hwidth = _width/2;
              g.drawLine(x-hwidth, y+hwidth, x+hwidth, y-hwidth);
              g.drawLine(x-hwidth, y-hwidth, x+hwidth, y+hwidth);
              break;
           }
       case Triangle:
           {
              // triangle
              int vertx[]={x, x-_width/2, x+_width/2 };
              int verty[]={y-_width/2, y+_width/2, y+_width/2 };
              if (_filled == true){
                  g.fillPolygon(vertx, verty, 3);
              } else {  
                  g.drawPolygon(vertx, verty, 3);
              }
              break;
           }
       case Square:
           {
              // square
              if (_filled == true){
                  g.fillRect(x-_width/2, y-_width/2, _width, _width);
              } else {
                  g.drawRect(x-_width/2, y-_width/2, _width, _width);
              }
              break;
           }
       case Plus:
           {  
              // plus
              g.drawLine(x-_width/2, y, x+_width/2, y);
              g.drawLine(x, y-_width/2, x, y+_width/2);
              break;
           } 
       case Line:
           {
              // line
              int hwidth = _width/2; 
              if (_orientation == HorizontalOri){
                   g.drawLine(x-hwidth, y, x+hwidth, y);
              } else if (_orientation == VerticalOri){
                   g.drawLine(x, y-hwidth, x, y+hwidth);
              }
              break;
            }
       default:
       } 
       g.setColor(saved);        

    }


   /** 
    * Set the link of the interact components
    */
    public void setLink(InteractComponent pair, int type){

          _pairlink = pair;
          _pairtype = type; 
    }

    public InteractComponent getPairIC(){
         return _pairlink;
    }

    public int getPairICType(){
         return _pairtype;
    }

    /**
     * Set the dataset and data index this interact component is corresponding 
     * to.  The dataset number and index number is corrsponding to the dataset
     * and data index in Plot. 
     */ 
    public void setAssociation(int datas, int ind){
       _dataindex = ind;
       _dataset = datas;
    } 

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////
  
    /**
     * Set the bounding box for selection.
     */ 
    protected void _setboundingbox(int x, int y) {
       _boundingwidth = x;
       _boundingheight = y;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         public variable                          ////

    // Constants that describe the shape 
    public final static int Circle = 1;   
    public final static int Cross = 2;   
    public final static int Triangle = 3;   
    public final static int Square = 4;   
    public final static int Plus = 5;   
    public final static int Line = 6;  

    // Constants that desicribe the _orientation
    public final static int SymmetricOri = 0; 
    public final static int HorizontalOri = 1; 
    public final static int VerticalOri = 2; 

    // Constants that desicribe the degree of freedom
    public final static int NoneDegFree = 0; 
    public final static int XaxisDegFree = 1; 
    public final static int YaxisDegFree = 2; 
    public final static int AllDegFree = 3; 

    public final static int YaxisMirrorXaxisSynch = 1; 
 
    public double xv, yv;    // holds value this interactcomponent represent
    public int x, y;  // holds the location of this interact component.
    public boolean selected; // set this flag if the component is seleted

    //////////////////////////////////////////////////////////////////////////
    ////                         private variable                          ////

    private int _dataset;  // tells which group of data this component belongs
    private int _dataindex;

    // used to specify on the plot upper left corner what each coordinates
    // represents.
    private String _xlabelName, _ylabelName;
    private Color _color = Color.black;

    private int _type = Circle ; 
    private String _name; 
    private boolean _filled = false;  
    private Vector _vertexes = null;
    private int _width = 10;
    private int _orientation = SymmetricOri; 
    private int _degFreedom = AllDegFree; 

    private int _boundingwidth, _boundingheight;

    // Variables for pairing interact components together 
    private InteractComponent _pairlink = null;
    private int _pairtype = -1;
}
