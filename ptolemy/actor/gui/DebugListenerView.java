/* An attribute representing a debug listener window for the container.

 Copyright (c) 1999 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JPanel;

//////////////////////////////////////////////////////////////////////////
//// DebugListenerView
/**
An attribute representing a debug listener window for the container model.
There can be any number of such windows associated with the model.
The listener window is an instance of TextEditor, and can be
accessed using the getFrame() method.

@author  Edward A. Lee and Steve Neuendorffer
@version $Id$
*/
public class DebugListenerView extends View {

    /** Construct a new view of the given debuggable object.  All 
     *  debug events published from the given debuggable will be displayed
     *  in the frame.  The log of these messages can be saved to a 
     *  text file separate from the model.
     *  The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the container does not accept
     *   this attribute.
     *  @exception NameDuplicationException If the name coincides with an 
     *   attribute already in the container.
     */
    public DebugListenerView(ModelProxy container, 
			     String name, 
			     final Debuggable debug)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
	_debug = debug;
        final DebugListenerFrame frame = new DebugListenerFrame();
	setFrame(frame);
	frame.setView(this);
	debug.addDebugListener(frame);
	// Listen for window closing events to unregister.
	frame.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		debug.removeDebugListener(frame);
	    }
	});
	frame.setVisible(true);
	frame.pack();
    }

    // FIXME what are we debugging?
    public class DebugListenerFrame extends TextEditor
	implements DebugListener {
	
	///////////////////////////////////////////////////////////////////
	////                         constructors                      ////
	
	/** Create a debug listener that displays messages in a top-level
	 *  window.
	 */
	public DebugListenerFrame() {
	    super();
	    text.setEditable(false);
	    text.setColumns(80);
	    text.setRows(20);
	    pack();
	}
	
	///////////////////////////////////////////////////////////////////
	////                         public methods                    ////
	
	/** Display a string representation of the specified event.
	 */
	public void event(DebugEvent event) {
	    text.append(event.toString() + "\n");
	    scrollToEnd();
	}
	
	/** Display the specified message.
	 */
	public void message(String message) {
	    text.append(message + "\n");
	    scrollToEnd();
	}
    }

    private Debuggable _debug;
}
