/* View object -- or observer 
 
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
import java.applet.*;

//////////////////////////////////////////////////////////////////////////
//// View 

/** 
 * Interactplot's observer class.  This class has the Observer interface
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


public abstract class View extends Applet implements Observer {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor.
     */
    public View(){
         _plots = null;  
         _frame = null;
         _opMode = 0;
         _observed = null;
         _numset = 0;
    }

    public void setNumSet(int set){
         _numset = set;
         _interact = new boolean[_numset];
    }

    public void dataSetInit(int dataset, boolean interact){
         if (dataset < _numset){
              _interact[dataset] = interact;
         }
    } 

    public abstract void update(Observable o, Object arg); 

    // called by plot to notify the viewer about the new changes made on the
    // plot.  parse the list of InteractComponent to vectors of doubles 
    public void newChange(Vector data){

         InteractComponent ic;
 
         _xvalue = new Vector[_numset];
         _yvalue = new Vector[_numset];
                  
         for (int ind=0;ind<data.size();ind++){
             ic = (InteractComponent) data.elementAt(ind);
             if (_xvalue[ic.getDataSetNum()] == null){
                _xvalue[ic.getDataSetNum()] = new Vector();
                _yvalue[ic.getDataSetNum()] = new Vector();
             }
             _xvalue[ic.getDataSetNum()].addElement(new Double(ic.xv));
             _yvalue[ic.getDataSetNum()].addElement(new Double(ic.yv));
         }
          
    }

    public void initPlots(){

        if (_plots == null) return;

        for (int i=0;i<_plots.length;i++){
            _plots[i].init();
        }

    }
 
    // called by Manager when deleting the view 
    public void deleteFrame(){
         if (_frame != null){
               // _frame.dispose();
               _frame.setVisible(false);
         }
    }

    public Panel getPanel(){
         return _viewPanel;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    protected Vector [] _xvalue;
    protected Vector [] _yvalue;
    protected boolean [] _interact;
    protected int _numset;
    protected int _lastinteractnum;

    protected Panel _viewPanel; 
    protected Plot [] _plots;
    protected Frame _frame;
    protected Observable _observed; 
    protected int _opMode; // 0. for normal standalone, 1. web applet mode 
}
