/* A named object that represents a graphical view of another ptolemy model.

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
@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.gui.MessageHandler;
import ptolemy.gui.CancelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import javax.swing.JFrame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// View
/**
A named object that represents a graphical view of another ptolemy model.
This is intended to be used in a user interface model, with the ptolemy model
in a separate hierarchy (or even a separate workspace).  

@author Steve Neuendorffer
@version $Id$
*/
public class View extends ComponentEntity {

    /** Construct a view with the given name contained by the specified
     *  ModelDirectory. The view will act on the given model.  
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This entity will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container entity.
     *  @param name The name of the entity.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public View(ModelProxy container,
		String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
    }

    /** Return the component that implements the display of this view.
     */
    public JFrame getFrame () {
	return _frame;
    }
    
    /** Return true if the window associated with this attribute
     *  is a master, which means that if that window is closed, then
     *  all windows associated with the model are closed.
     *  @return True if the window is a master.
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
	    ModelProxy oldContainer = (ModelProxy)getContainer();
	    super.setContainer(container);
	    // Blow away the frame.
	    if(_frame != null) {
		// Note that we call hide instead of dipose..
		// The windowListener below will trigger this to get called.
		_frame.hide();
	    }
	    
            if (_master && oldContainer != null) {
                // Window is a master.  Close the model which will close all
		// other views.
		oldContainer.setContainer(null);
            }
	    // FIXME this should be handled in the model directory.
	    /* List attrList = 
	       oldcontainer.attributeList(WindowAttribute.class);
	       if (attrList.size() == 0) {
	       ModelDirectory.remove(_frame.getKey());
            }
	    */
	} else if(container instanceof ModelProxy) {	
	    super.setContainer(container);
	} else {
	    throw new IllegalActionException(this, container, 
		"The container can only be set to an " + 
		"instance of ModelProxy");
	}
    }

    /** Set the frame associated with this attribute.
     *  @param frame The frame associated with the attribute.
     */
    public void setFrame(JFrame frame) {
        _frame = frame;

        // Set up a listener for window closing events.
        frame.addWindowListener(new WindowAdapter() {
            // This is invoked if the window is closed
            // via the window manager.
            public void windowClosing(WindowEvent e) {
                try {
                    setContainer(null);
                } catch (KernelException ex) {
		    try {
			MessageHandler.warning(
                            "Cannot remove view: " + ex);
                    } catch (CancelException exception) {}
		}
            }
        });
    }

    /** Specify whether the window associated with this attribute
     *  is a master, which means that if that window is closed, then
     *  all windows associated with the model are closed.
     *  @param flag If true, makes the window a master.
     */
    public void setMaster(boolean flag) {
        _master = flag;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The frame that the view is shown in.
     */
    private JFrame _frame;

    /** True if this view is a master view.  Default value is false.
     */
    private boolean _master = false;
}

