/* An actor which pops up a wormhole frame responsive to copy and paste.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (winthrop@robotics.eecs.berkeley.edu)
@AcceptedRating Red (winthrop@robotics.eecs.berkeley.edu)
*/

package ptolemy.actor.lib.net;

// Imports from ptolemy/vergil/basic/BasicGraphFrame.java (fully pruned)
import diva.gui.toolbox.FocusMouseListener;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
//import java.awt.event.MouseListener;

// Imports from ptolemy/actor/lib/net/DatagramReceiver.java (fully pruned)
//import ptolemy.actor.AtomicActor;
//import ptolemy.actor.IOPort;
  import ptolemy.actor.TypedAtomicActor;
  import ptolemy.actor.TypedIOPort;
  import ptolemy.data.ArrayToken;
//import ptolemy.data.BooleanToken;
  import ptolemy.data.IntToken;
  import ptolemy.data.StringToken;
  import ptolemy.data.Token;
//import ptolemy.data.expr.Parameter;
  import ptolemy.data.type.ArrayType;
  import ptolemy.data.type.BaseType;
//import ptolemy.data.type.Type;
  import ptolemy.kernel.CompositeEntity;
//import ptolemy.kernel.util.Attribute;
  import ptolemy.kernel.util.IllegalActionException;
  import ptolemy.kernel.util.NameDuplicationException;
//import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Wormhole

/**
This actor generates an additional frame.  This may look like a new
window in Windows.  This frame detects copy (control-c) and paste
(control-v) keystrokes provided it has the focus at the time.  When it
is clicked on, it will acquire said focus.  This actor additionally
accesses the system clipboard.  When fired, it copies the input token,
if there is one, to the system clipboard and pastes the system
clipboard to its output, sending a token there.  If both paste and
copy occur, paste is performed first.  The frame and clipboard
portions of this actor are coupled through the director.  The frame
portion, specified by this actor's inner class, calls the director's
fireAtCurrentTime() method.  This causes the director to call fire()
on the clipboard portion. <p>

@author Winthrop Williams
@version $Id$
*/
public class Wormhole extends TypedAtomicActor implements ClipboardOwner {

    public Wormhole(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Outputs
        pasteOutput = new TypedIOPort(this, "pasteOutput");
        pasteOutput.setTypeEquals(BaseType.STRING);
        pasteOutput.setOutput(true);

        copyTrigger = new TypedIOPort(this, "copyTrigger");
        copyTrigger.setTypeEquals(BaseType.GENERAL);
        copyTrigger.setOutput(true);

        // Input
        copyInput = new TypedIOPort(this, "copyInput");
        copyInput.setTypeEquals(BaseType.STRING);
        copyInput.setInput(true);

	/*
         focusTrigger
        */

    }

    public TypedIOPort pasteOutput;

    public TypedIOPort copyTrigger;
    
    public TypedIOPort copyInput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    /** Fire this actor.
     *  Blah, blah, blah.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) _debug("fire has been called");

	if (_pasteKeyPressed) {
	    _pasteKeyPressed = false;
	    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
	    Transferable transferable = clipboard.getContents(_myframe);
	    try{
		pasteOutput.broadcast(new StringToken( (String)transferable
                        .getTransferData(DataFlavor.stringFlavor) ));
	    // NullPointerException also possible //
		// Ignore this for now, allowing exception to go uncaught.
	    } catch (java.io.IOException ex) {
		throw new IllegalActionException(this,
                        " Failed to paste (IO Exception): " + ex);
	    } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
		throw new IllegalActionException(this,
                        " Failed to paste: (Flavor Exception)" + ex);
	    }
	}

	if (copyInput.getWidth()>0 && copyInput.hasToken(0)) {
	    Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                    .getSystemClipboard();
	    String myString = ((StringToken)(copyInput.get(0))).stringValue();

	    /*
	    ArrayToken myArrayToken = (ArrayToken) copyInput.get(0);
	    // (Use only low byte of each integer.)
	    int byteCount = myArrayToken.length();
	    byte[] byteData = new byte[byteCount];
	    for (int j = 0; j < byteCount; j++) {
		IntToken myToken = (IntToken) myArrayToken.getElement(j);
		byteData[j] = (byte) myToken.intValue();
	    }
	    */
     
	    clipboard.setContents(new StringSelection(myString), this);
	}

	if (_copyKeyPressed) {
	    _copyKeyPressed = false;
	    copyTrigger.broadcast(new Token());
	}

	if (_debugging) _debug("fire has completed");
    }

    /** Comply with the ClipboardOwner interface.  It requires a
     *  method exist named <i>lostOwnership</i>.  
     *  
     *  Without this (and having the actor, or something, 
     *  implement ClipboardOwner, I get the following error:
     *  
     *  setContents(java.awt.datatransfer.Transferable,
     *              java.awt.datatransfer.ClipboardOwner) 
     *           in java.awt.datatransfer.Clipboard
     *           cannot be applied to
     *             (java.awt.datatransfer.StringSelection,
     *              ptolemy.actor.lib.net.Wormhole)
     */
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    public void preinitialize() {
        if (_debugging) _debug("frame will be constructed");
        _myframe = new MyFrame();
        if (_debugging) _debug("frame was constructed");
    }

    public void wrapup() {
	_myframe.dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables

    private MyFrame _myframe;
    private boolean _copyKeyPressed = false;
    private boolean _pasteKeyPressed = false;

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    private class MyFrame extends JFrame implements ClipboardOwner {

        /** Construct a frame associated with the specified Ptolemy II
         *  model.  After constructing this, it is necessary to call
         *  setVisible(true) to make the frame appear.  This is
         *  typically done by calling show() on the controlling
         *  tableau.
         *  @see Tableau#show()
         *  @param entity The model to put in this frame.
         *  @param tableau The tableau responsible for this frame.  */
        public MyFrame() {
            if (_debugging) _debug("frame constructor called");

	    // Paste callback
            ActionListener myPasteListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			pasteFromBufferToPasteOutput(); 
		    } 
	    };

	    // Copy callback
            ActionListener myCopyListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			if (_debugging) _debug("copy callback called");
			_copyKeyPressed = true;
			try {
			    getDirector().fireAtCurrentTime(Wormhole.this);
			} catch (IllegalActionException ex) {
			    System.out.println(this
			            + "Ex calling fireAtCurrentTime");
			    throw new RuntimeException("-fireAt* C catch-");
			}
			if (_debugging) _debug("copy callback completed");
		    }
	    };

            getContentPane().setLayout(new BorderLayout());
            JLabel label = new JLabel("Copy and/or Paste here!");
            getContentPane().add(label);

	    // Paste registration of callback.
            label.registerKeyboardAction(myPasteListener, "Paste",
                    KeyStroke.getKeyStroke(
                    KeyEvent.VK_V, java.awt.Event.CTRL_MASK),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

	    // Copy registration of callback.
            label.registerKeyboardAction(myCopyListener, "Copy",
                    KeyStroke.getKeyStroke(
                    KeyEvent.VK_C, java.awt.Event.CTRL_MASK),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            label.setRequestFocusEnabled(true);
            label.addMouseListener(new FocusMouseListener());
            // Set the default size.
            // Note that the location is of the frame, while the size
            // is of the scrollpane.
            pack();
	    show();
            if (_debugging) _debug("frame constructor completes");
        }

        /** Comply with the ClipboardOwner interface.
         *  It requires a method exist named <i>lostOwnership</i>.
	 */
        public void lostOwnership(Clipboard clipboard,
                Transferable contents) {
        }

        /** Assuming the contents of the clipboard is MoML code, paste
         *  it into the current model by issuing a change request.
         */
        public void pasteFromBufferToPasteOutput() {
            if (_debugging) _debug("pasteFrom.. has been called");
            _pasteKeyPressed = true;
	    try {
                getDirector().fireAtCurrentTime(Wormhole.this);
            } catch (IllegalActionException ex) {
		System.out.println("--" + ex.toString() + "--");
                System.out.println(this
                        + "Exception calling fireAtCurrentTime");
                throw new RuntimeException("-fireAt* catch-");
            }
            if (_debugging) _debug("pasteFrom.. has completed");
        }
    }
}





