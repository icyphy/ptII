/* A view that creates a new run control panel for a ptolemy model.

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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.actor.CompositeActor;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;


//////////////////////////////////////////////////////////////////////////
//// RunView
/**
A view that creates a new run control panel for a ptolemy model, including
placing placeable actors such as plots and toplevel windows.

@author Steve Neuendorffer
@version $Id$
*/
public class RunView extends View {

    /** Create a new run control panel for the given model with the given 
     *  name and make it visible.  This view is managed by the given model
     *  directory.
     */
    public RunView(PtolemyModelProxy container,
		   String name)
            throws IllegalActionException, NameDuplicationException {
	super(container, name);
        NamedObj model = container.getModel();
        if (!(model instanceof CompositeActor)) {
            throw new IllegalActionException(this,
            "Cannot run a model that is not a CompositeActor.");
        }
	ModelFrame frame = new RunFrame((CompositeActor)model);
	frame.setBackground(BACKGROUND_COLOR);
	setFrame(frame);
	frame.setVisible(true);
	frame.pack();
    }

    /** Create a new run control panel for the given model with the given 
     *  name and make it visible.  This view is managed by the given model
     *  directory.
     */
    public RunView(PtolemyModelProxy container,
		   String name,
		   JPanel displayPanel) 
            throws IllegalActionException, NameDuplicationException {
	this(container, name);
	ModelFrame frame = (ModelFrame)getFrame();
	frame.setView(this);
       	if (displayPanel != null) {
	    frame.modelPane().setDisplayPane(displayPanel);
	    
	    // Calculate the size.
	    Dimension frameSize = frame.getPreferredSize();
	    
	    // Swing classes produce a preferred size that is too small...
	    frameSize.height += 30;
	    frameSize.width += 30;
	    frame.setSize(frameSize);
	    frame.validate();
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The frame that is created by this view.
     */
    public class RunFrame extends ModelFrame {	
	/** Construct a frame to control the specified Ptolemy II model.
	 *  After constructing this, it is necessary
	 *  to call setVisible(true) to make the frame appear.
	 *  @param model The model to put in this frame, or null if none.
	 */
	public RunFrame(CompositeActor model) {
	    super(model);

	    // Debug menu
	    JMenuItem[] debugMenuItems = {
		new JMenuItem("Listen to Manager", KeyEvent.VK_M),
		new JMenuItem("Listen to Director", KeyEvent.VK_D),
	    };
	    _debugMenu.setMnemonic(KeyEvent.VK_D);
	    DebugMenuListener sml = new DebugMenuListener();
	    // Set the action command and listener for each menu item.
	    for(int i = 0; i < debugMenuItems.length; i++) {
		debugMenuItems[i].setActionCommand(
		    debugMenuItems[i].getText());
		debugMenuItems[i].addActionListener(sml);
		_debugMenu.add(debugMenuItems[i]);
	    }
	    _menubar.add(_debugMenu);
	}

	/** Listener for debug menu commands. */
	public class DebugMenuListener implements ActionListener {
	    public void actionPerformed(ActionEvent e) {
		JMenuItem target = (JMenuItem)e.getSource();
		String actionCommand = target.getActionCommand();
		try {
		    Debuggable debug;
		    if (actionCommand.equals("Listen to Manager")) {
			debug = getModel().getManager();
		    } else if (actionCommand.equals("Listen to Director")) {
			debug = getModel().getDirector();
		    } else {
			debug = null;
		    }
		    if(debug != null) {
			ModelProxy proxy = (ModelProxy)getContainer();
			new DebugListenerView(proxy,
					      proxy.uniqueName("debugListener"),
					      debug);
		    }
		} catch (KernelException ex) {
		    try {
			MessageHandler.warning("Failed to create debug listener: "
					       + ex);
		    } catch (CancelException exception) {}
		}
	    }
	}
    }

    /** A factory that creates run control panel views for Ptolemy models.
     */
    public class RunViewFactory extends ViewFactory {
	/** Create a view in the default workspace with no name for the 
	 *  given ModelProxy.  The view will created with a new unique name
	 *  in the given model proxy.  If this factory cannot create a view
	 *  for the given proxy (perhaps because the proxy is not of the
	 *  appropriate subclass) then return null.
	 *  @param proxy The model proxy.
	 *  @return A new RunView, if the proxy is a PtolemyModelProxy, or null
	 *  if the proxy is not a PtolemyModelProxy, 
	 *  or creating the view fails.
	 */
	public View createView(ModelProxy proxy) {
	    if(proxy instanceof PtolemyModelProxy) {
		try {
		    return new RunView((PtolemyModelProxy)proxy,
				       proxy.uniqueName("view"));
		} catch (Exception ex) {
		    return null;
		}
	    } else {
		return null;
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** @serial Debug menu for this frame. */
    protected JMenu _debugMenu = new JMenu("Debug");

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
   
    // FIXME: should be somewhere else?
    // Default background color is a light grey.
    private static Color BACKGROUND_COLOR = new Color(0xe5e5e5);
}

