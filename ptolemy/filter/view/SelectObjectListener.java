/* This is the select object listener for InteractPlot. 

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
//// SelectObjectListener
/** 
  The select object listener for InteractPlot.  It used be an inner
class in InteractPlot.  But it complains during compilation, but class
files still builds ok, and it runs fine...  So it is in its
seperate java file.

@author  wbwu@eecs.berkeley.edu 
@version $id$ 
@see InteractPlot 
*/
public class SelectObjectListener implements MouseListener {
    /** Constructor
     * @param interactPlot the interact plot that this drag object
     * listener is in.
     */
    public SelectObjectListener(InteractPlot interactPlot) {
         _interactPlot = interactPlot;
    }
   

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Processing mouse click event.  For a mouse click with a control
     * down, the interact plot pop a window that shows value of the
     * InteractComponent.  
     * @param event the click mouse event
     */
    public void mouseClicked (MouseEvent event) {
        if (event.isControlDown()){
            if (_interactPlot.getEditPermission()){
                _interactPlot.selectInteractcompAndShowValueWin(event.getX(), event.getY());
            }
        }
    }
 
    public void mouseEntered(MouseEvent event) {
    }

    public void mouseExited(MouseEvent event) {
    }

    /**
     * Process the mouse down event when the meta key is also down.
     * This is for select interact component on the plot.
     * @param event mouse down event.
     */ 
    public void mousePressed(MouseEvent event) {
        if (event.isMetaDown()){
            _interactPlot.selectInteractcomp(event.getX(), event.getY());
        }
    }

    /**
     * Process the mouse up event when the meta key is also down.
     * This is for unselect interact component on the plot.
     * @param event mouse up event.
     */ 
    public void mouseReleased(MouseEvent event) {
        if (event.isMetaDown()){
            _interactPlot.finishDragInteractcomp(event.getX(), event.getY());
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private InteractPlot _interactPlot;
}
