/* View object for a data plotter/changer -- or observer 
 
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

import ptolemy.filter.controller.*;
import ptolemy.filter.filtermodel.*;

import java.util.*;
import java.awt.*;
import java.applet.*;

//////////////////////////////////////////////////////////////////////////
//// DataView 

/** 
 * Filter's observer base class.  This class has the Observer interface
 * that allow it to be added to the observer list in the observable.
 * View contains plots to for displaying the data.  The view can be operated
 * in two mode, one is normal mode, where the plot is displayed in frame.
 * In the other mode, plot is displayed on web page.     
 * The observable can communicate with the observer through <code> update() </code> 
 * In the update function, each viewer know what kind of data it needs, and it
 * request observable specifically for those. 
 * The <code> newChange() </code> is called by the interactive plot that 
 * has changed the values on the plot, so that the observable can be notified
 * about the new changes. 
 * 
 * author: William Wu
 * version:
 * date: 3/2/98
 */ 

/** 
 * This viewer is used to interface with a generical dataset
 */
public class DataView extends View {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor.
     */
    public DataView(){
         _plots = null;  
         _frame = null;
         _opMode = 0;
         _observed = null;
    }


    public void start(){
         DataModel dm = (DataModel) _observed;
         dm.getAllData(); 
    }  

    /**
     * Constructor, builds a default interactive plot, 
     * the observable is passed in.
     */
    public DataView(Observable obs, int mode){
 
         _observed = obs; 
         _plots = new Plot[1];
         InteractPlot ip = new InteractPlot();
         ip.setView(this);
         _plots[0]=ip;
         _opMode = mode; 
         if (_opMode == 0){
              _frame = new Frame("Interact Plot");
              _frame.add("Center", _plots[0]);
              _frame.setSize(600,500);
              _frame.show();
// set the ranges manually, change this later!!
              _plots[0].setXRange(-3, 10);
              _plots[0].setYRange(0, 20);
              _plots[0].init();
         }
         _numset = 0; 
         _lastinteractnum = 0;
         
    }

    // this uses a push style data updating scheme
    public void update(Observable o, Object arg) {
         String command = (String) arg;
         if (command.equals("NewData")){
              _lastinteractnum = 0; 
              DataModel dm = (DataModel) _observed;
              dm.getAllData(); 
         }
    }

    // called by data model to update each dataset, either interact
    // or not interact
    public void dataUpdate(String name, int dataset, double[] x, double[] y, 
                           boolean interact, boolean connect,
                           String xlab, String ylab, int dfree, int ori){
      System.out.println("_numset "+_numset+" dataset "+dataset);
         if ((dataset < _numset) && (_plots.length > 0)){
             
              if (interact == false) {
                   for (int ind =0;ind<x.length;ind++){
                       if (ind == 0){
                           _plots[0].addPoint(dataset, x[ind], y[ind], false);
                       } else {
                           _plots[0].addPoint(dataset, x[ind], y[ind], connect);
                       }
                   }

              } else {
                 if (_histo == true){
                   for (int ind=0;ind<x.length;ind++){
                       InteractComponent ic;
                       ic = new InteractComponent(name, InteractComponent.Line);
                       ic.setDrawingParam(Color.black, 30, false, 
                                          InteractComponent.HorizontalOri);  
                       ic.setInteractParam(xlab, ylab, 
                                           InteractComponent.YaxisDegFree); 

                       ic.setAssociation(dataset, ind);
                       double lx = x[ind]-_barwidth+(dataset - _numset-1)*_offset;
                       double rx = lx + _barwidth;
                       ic.xv = (lx+rx)/2;

                     //  ic.xv = x[ind]; 
                       ic.yv = y[ind]; 

                       if (ind == 0){
                           ((InteractPlot) _plots[0]).addInteractPoint(ic, dataset, x[ind], y[ind], false);
                       } else {
                           ((InteractPlot) _plots[0]).addInteractPoint(ic, dataset, x[ind], y[ind], connect);
                       }
                   }
                 }

                 else {    
                   int comptype = _lastinteractnum;
                   for (int ind=0;ind<x.length;ind++){
                       InteractComponent ic;
                       switch (comptype){
                       case 0:
                           {
                              // circle
                              ic = new InteractComponent(name, InteractComponent.Circle);
                              ic.setDrawingParam(Color.red, 5, false, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);

                              break;
                           }
                       case 1:
                           {
                              // cross
                              ic = new InteractComponent(name, InteractComponent.Cross);
                              ic.setDrawingParam(Color.blue, 10, false, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       case 2:
                           {
                              // triangle 
                              ic = new InteractComponent(name, InteractComponent.Triangle);
                              ic.setDrawingParam(Color.green, 5, false, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       case 3:
                           {
                              // square 
                              ic = new InteractComponent(name, InteractComponent.Square);
                              ic.setDrawingParam(Color.black, 5, false, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       case 4:
                           {
                              // plus 
                              ic = new InteractComponent(name, InteractComponent.Plus);
                              ic.setDrawingParam(Color.black, 5, false, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       case 5:
                           {
                              // filled circle 
                              ic = new InteractComponent(name, InteractComponent.Circle);
                              ic.setDrawingParam(Color.cyan, 5, true, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       case 6:
                           {
                              // filled triangle 
                              ic = new InteractComponent(name, InteractComponent.Triangle);
                              ic.setDrawingParam(Color.orange, 5, true, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       case 7:
                           {
                              // filled square 
                              ic = new InteractComponent(name, InteractComponent.Square);
                              ic.setDrawingParam(Color.pink, 5, true, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                              break;
                           }
                       default:
                           {
                              ic = new InteractComponent(name, InteractComponent.Circle);
                              ic.setDrawingParam(Color.red, 5, false, ori); 
                              ic.setInteractParam(xlab, ylab, dfree);
                           }
                       }

                       ic.setAssociation(dataset, ind); 
                       ic.xv = x[ind]; 
                       ic.yv = y[ind]; 

                       if (ind == 0){
                           ((InteractPlot) _plots[0]).addInteractPoint(ic, dataset, x[ind], y[ind], false);
                       } else {
                           ((InteractPlot) _plots[0]).addInteractPoint(ic, dataset, x[ind], y[ind], connect);
                       }
                  }
                  _lastinteractnum++;
              }
           } 
         }              
    }  

    public void newChange(Vector data){
         super.newChange(data);
         DataModel dm = (DataModel) _observed;
         for (int ind=0; ind<_numset; ind++){

              if ((_xvalue[ind]==null)||(_yvalue[ind]==null)) continue;

              double [] newx;
              double [] newy;
System.out.println(ind);
             
              newx = new double[_xvalue[ind].size()]; 
              newy = new double[_yvalue[ind].size()];
              for (int ind2=0; ind2<newx.length; ind2++){
                  newx[ind2] = ((Double)_xvalue[ind].elementAt(ind2)).doubleValue(); 
                  newy[ind2] = ((Double)_yvalue[ind].elementAt(ind2)).doubleValue(); 
              } 
              dm.dataUpdate(ind, newx, newy);
         }
    }



    public void setBars(double xbin, double offset){
         if (_plots[0]!=null) {
             _plots[0].setBars(xbin, offset);
             _barwidth = xbin;
             _offset = offset;
             _histo = true;
         }
    } 

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////
    protected boolean _histo = false; 
    protected double _barwidth; 
    protected double _offset; 

}
