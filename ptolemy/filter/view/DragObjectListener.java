/*  The drag object listener for interact plot.

 Copyright (c) 1998 The Regents of the University of California.
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
 
                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

*/

package ptolemy.filter.view;

import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// DragObjectListener
/** 
   Drag object listener for filter interact plot.  It was an inner class
   of InteractPlot, but it complains during compilation, but class
   files still builds ok, and it run fine...  So it is in its
   seperate java file.
   <p>
   This class is meant be used with  
@author wbwu@eecs.berkeley.edu 
@version $id$ 
@see InteractPlot 
*/
public class DragObjectListener implements MouseMotionListener {
    /** Constructor
     * @param interactPlot the interact plot that this drag object 
     * listener is in. 
     */	
    public DragObjectListener(InteractPlot interactPlot) {
        _interactPlot = interactPlot;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calls InteractPlot's <code> dragInteractcomp() </code>
     * @param event the mouse event 
     */	
    public void mouseDragged (MouseEvent event) {
         if (event.isMetaDown()){
             if (_interactPlot.getEditPermission()){
                // drag the selected the interact object
                _interactPlot.dragInteractcomp(event.getX(), event.getY());
             }
         }
    }

    public void mouseMoved(MouseEvent event) {
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private InteractPlot _interactPlot;
}

