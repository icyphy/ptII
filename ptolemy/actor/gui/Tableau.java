/* An object that represents a graphical view of a ptolemy model.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;

import java.util.Iterator;
import java.util.List;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

//////////////////////////////////////////////////////////////////////////
//// Tableau
/**
A tableau is a visual representation of a Ptolemy II model in a top-level
window.  This class represents such a top level window.  The top-level
is always a frame, which is a window with a border and title bar. The
window itself is specified by the setFrame() method, and accessed by
the getFrame() method.  An instance of this class will be contained
by the instance of Effigy that represents the model that is depicted
in the top-level window.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@see Effigy
*/
public class Tableau extends ComponentEntity {

    /** Construct a tableau with the given name and container.
     *  @param container The container.
     *  @param name The name of the tableau.
     *  @exception IllegalActionException If the tableau cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public Tableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the top-level window that implements the display of
     *  this tableau.
     *  @return A top-level window.
     */
    public JFrame getFrame () {
	return _frame;
    }
    
    /** Return the title of this tableau.  Subclasses can override this to
     *  provide a better description of themselves.  This base class
     *  returns the identifier of the effigy containing this tableau,
     *  or the string "Unnamed Tableau" if there is no such identifier.
     *  The title is used as the title of the top-level window in
     *  the setFrame() method.
     *  @return The title to put on the window.
     */
    public String getTitle() {
        Effigy effigy = (Effigy)getContainer();
        return effigy.identifier.getExpression();
    }

    /** Return true if this tableau is a master, which means that
     *  if that if its window is closed, then all other windows associated 
     *  with the model are also closed.
     *  @return True if the tableau is a master.
     */
    public boolean isMaster() {
        return _master;
    }

    /** Override the base class so that if the argument is null and the
     *  window is a master, then all other windows associated with the
     *  container are closed and the model is removed from the ModelDirectory.
     *  If this window is not a master, but after removing it there are
     *  no more windows associated with the model, then also remove it
     *  from the ModelDirectory.
     *  @param container The container to attach this attribute to..
     *  @exception IllegalActionException If the proposed container is not
     *  an instance of ModelDirectory, if this attribute is not of the
     *   expected class for the container, or it has no name,
     *   or the attribute and container are not in the same workspace, or
     *   the proposed container would result in recursive containment.
     *  @exception NameDuplicationException If the container already has
     *   an attribute with the name of this attribute.
     */
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
	if(container == null) {
	    Effigy oldContainer = (Effigy)getContainer();
	    super.setContainer(container);
	    // Blow away the frame.
	    if(_frame != null) {
		// Note that we call hide instead of dipose..
		// The windowListener set in setFrame()
                // will trigger dispose() to get called.
		_frame.hide();
	    }
	    
            if (isMaster() && oldContainer != null) {
                // Window is a master.  Close the model which will close all
		// other tableaux.
		oldContainer.setContainer(null);
            }
	} else if(container instanceof Effigy) {	
	    super.setContainer(container);
	} else {
	    throw new IllegalActionException(this, container, 
		"The container can only be set to an " + 
		"instance of Effigy");
	}
    }

    /** Set the top-level window associated with this tableau.
     *  @param frame The top-level window associated with the attribute.
     */
    public void setFrame(JFrame frame) {
        _frame = frame;

	frame.setTitle(getTitle());

        // Set up a listener for window closing events.
        frame.addWindowListener(new WindowAdapter() {
            // This is invoked if the window is closed
            // via the close command in the File menu.
            public void windowClosed(WindowEvent e) {
                try {
		    setContainer(null);
                } catch (KernelException ex) {
		    try {
			MessageHandler.warning("Cannot remove tableau: " + ex);
                    } catch (CancelException exception) {}
		}
            }
            // This is invoked if the window is closed
            // via the window manager.
            public void windowClosing(WindowEvent e) {
                try {
		    setContainer(null);
                } catch (KernelException ex) {
		    try {
			MessageHandler.warning("Cannot remove tableau: " + ex);
                    } catch (CancelException exception) {}
		}
            }
        });
    }

    /** Specify whether the window associated with this tableau
     *  is a master, which means that if that window is closed, then
     *  all windows associated with the model are closed.
     *  @param flag If true, makes the window a master.
     */
    public void setMaster(boolean flag) {
        _master = flag;
    }

    /** Make this tableau visible by raising or deiconifying its window.
     */
    public void show() {
        getFrame().toFront();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                  ////

    /** The frame that the tableau is shown in.
     */
    private JFrame _frame;

    /** True if this tableau is a master tableau.  Default value is false.
     */
    private boolean _master = false;
}

