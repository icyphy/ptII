/* Filter View object -- or observer for the filter object 
 
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

import ptolemy.filter.filtermodel.FilterObj;
import java.awt.event.*;
import java.util.*;
import java.awt.*;
import java.applet.*;

//////////////////////////////////////////////////////////////////////////
//// FilterView 

/** 
  The base class for filter's views.  A view is where certain data of a 
  observed object is diaplayed.  This is a standard "Model-View" design
  pattern found in "Design Pattern" book. This class has the Observer interface
  that allow it to be added to the observer list in the observable.
  The view can be operated in two mode, one is normal mode, where the plot 
  is displayed in frame.  In the other mode, plot is displayed on web page.     
  The observable can communicate with the observer through update()  
  In the update function, each viewer know what kind of data it needs, and it
  request observable specifically for those.    
 
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W%    %G%
  @date: 3/2/98
 */ 


public abstract class FilterView implements Observer {


    /**
     * Constructor.  The name of the view is passed, along 
     * with the observed filter object.
     * <p> 
     * @param name name of this view.
     * @param filter observed filter object.
     */
    public FilterView(String name, FilterObj filter){
         _name = name;
         _observed = filter;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Get the name of this view. 
     * <p>
     * @return name of this view.
     */
    public String getName(){
         return _name;
    }

    /**
     * Notify the view about the newly updated object (observable). 
     * The derived views will then use the "get" methods in FilterObj
     * object, _observed, to query specific information that this view
     * need. <p> 
     * Notice that this method also passes the observable object reference in, 
     * this reference can be used to query data from observed filter.  The 
     * local reference, _observed, is not needed in this case.  However, since
     * the Observable-Observer relation is much more complex than the one
     * provided by Java.  Because the observer can change the data it represent
     * (like in PoleZeroView).  And the change must reach to the Observable.
     * Thus a local reference is kept.
     *  
     * @param observable the observed object
     * @param arg message sent out by observed  
     */
    public abstract void update(Observable observable, Object arg); 

    /**
     * Set view's visibility.  If the view has a frame, set frame to the
     * desired visibility.  If the view's panel is not null set to the same
     * visibility.
     * <p>
     * @param show desired visibility of this view.
     */
    public void setVisible(boolean show){
         if (_frame != null) _frame.setVisible(show);
         if (_viewPanel != null) _viewPanel.setVisible(show);
    }

    public void setViewController(ViewController viewcon){
         _viewcontrol = viewcon;
    }
 
    /**
     * Get the panel that the view is deplayed on.
     * @return view's panel.
     */
    public Panel getPanel(){
         return _viewPanel;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                           public variables                      ////

    public final static int FRAMEMODE = 0;
    public final static int APPLETMODE = 1;

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////
    protected Frame _createViewFrame(String title){
        Frame frame = new Frame(title); 
        frame.addWindowListener(new ViewWinAdapter());    
        return frame;
    }

    protected void _notifyViewController(){
         if (_viewcontrol != null){
             _viewcontrol.setViewVisible(_name, false);
         }
    }
 
    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** Panel that view is displayed */
    protected Panel _viewPanel;   
    /** Frame that view panel is placed if in Frame mode */
    protected Frame _frame;
    /** Observed filter object */
    protected FilterObj _observed; 
    /** Mode for operation, see Operation Mode in this class */
    protected int _opMode;  

    /** View controller.  Controller that handles show/hide of all
      views */
    protected ViewController _viewcontrol;

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                       ////

    // name of the view
    private String _name; 
    
    //////////////////////////////////////////////////////////////////////////
    ////                         inner class                        ////
  
    class ViewWinAdapter extends WindowAdapter {
        public void windowClosing(WindowEvent e){
            System.out.println("Window closing event"); 
            _frame.setVisible(false);
            FilterView.this._notifyViewController();
        }
    }
}
