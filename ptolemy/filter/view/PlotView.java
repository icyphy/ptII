/* Plot View object, view that use plot to represent data.  
 
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
import java.util.*;
import java.awt.*;
import java.applet.*;

//////////////////////////////////////////////////////////////////////////
//// PlotView 

/** 
  This type of view uses plot (the Ptolemy plot package) to display
  data.  Since there could be more than one plot for a view, thus an array
  of plots is stored in _plots.  The plot could be interactive.  Thus each 
  interactive component will correspond to a data element, by using array of 
  hashtables, _crossref, with interact component as the keys.  
  This is an abstract class.  The derived class must 
  implement moveInteractComp(), to process the change done on plot, and 
  notify the observed filter about the changes.
  <p>
   
  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W%   %G%
  @date: 3/2/98
*/ 


public abstract class PlotView extends FilterView {


    /**
     * Constructor.  
     */
    public PlotView(String viewname, FilterObj filter){
         super(viewname, filter);
    }
 
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    /**
     * Initialize all the plots.  If there is no plot, return.
     * <p>
     */
    public void initPlots(){

        if (_plots == null) return;

        for (int i=0;i<_plots.length;i++){
            _plots[i].init();
        }

    }
 
    /**
     * Process the movement of an interactive component.  The underlying data
     * is changed accordingly and notify the observed filter in the derived  
     * class.
     * <p>
     * @param ic the changed InteractComponent.
     */
    public abstract void moveInteractComp(InteractComponent ic);

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** array of plots */
    protected Plot [] _plots;

    /** These hashtables stores the cross ref link between interact 
        component and underlying data. */
    protected Hashtable [] _crossref; 
}
