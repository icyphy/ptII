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

$Id$
 
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
                   if (iComp.getSelected() == false){ 
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
               if (iCompdraw.getSelected() == true) _setXORpaintMode();  // seleted for dragging 
               iCompdraw.drawSelf(getGraphics());
               if (multi[i]>1){
                   getGraphics().drawString(String.valueOf(multi[i]), 
                                     iCompdraw.x+iCompdraw.getWidth()/2+2,     
                                     iCompdraw.y+iCompdraw.getWidth()/2+2);
               }
               if (iCompdraw.getSelected()==true) _setNormalpaintMode();
          }    
          getGraphics().setColor(curColor);
       }
       notify();
    } 


   /**
    * Initliazation.  It sets the length of the axises, number of data
    * set, and title of the plot.  For this plot, it creates a unit
    * circle for referencing, it is created as a set of data.
    */
   public void init(){

       _deleteInteractPermission = true;

       _background = Color.black;
       _foreground = Color.white;

       super.init();

       setTitle("Pole Zero Plot ");
       _xBottom = -1.2;
       _xTop = 1.2;
       _yBottom = -1.2;
       _yTop = 1.2;

       setXRange(_xBottom, _xTop);
       setYRange(_yBottom, _yTop);

       _xLB = _xMin;
       _xUB = _xMax;
       _yLB = _yMin;
       _yUB = _yMax;
      
       setNumSets(3);
 
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
    }


   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////

    private int _mode; // 1 - select mode, 2 - delete mode       
    private double xrange1 = -1.5;
    private double xrange2 = 1.5;
    private double yrange1 = -1.5;
    private double yrange2 = 1.5;
    private Button addbutton;
    private Choice _c;

}
 
