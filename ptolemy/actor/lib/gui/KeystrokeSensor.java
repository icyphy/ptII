/* An actor which pops up a keystroke-sensing JFrame.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.actor.lib.gui;

import diva.gui.toolbox.FocusMouseListener;
import ptolemy.actor.Actor;
import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

//////////////////////////////////////////////////////////////////////////
//// KeystrokeSensor

/**
When this actor is preinitialized, it pops up a new JFrame window on
the desktop, usually in the upper left hand corner of the screen.
When this JFrame has the focus (such as when it has been clicked on)
it is capable of sensing keystrokes.  <p>

This actor senses only two keystrokes, control-C (copy) and control-V
(paste).  This actor is designed to work with SystemClipboard.java<p>

This actor contains a private inner class which generated the JFrame.
The frame sets up callbacks which react to the keystrokes.  When called,
these call the director's fireAtCurrentTime() method.  This causes
the director to call fire() on the actor.   The actor then broadcasts
tokens from one or both outputs depending on which keystroke(s) have
occurred since the actor was last fired.  <p>

@author Winthrop Williams
@version $Id$ */
public class KeystrokeSensor extends TypedAtomicActor {

    public KeystrokeSensor(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Outputs

        controlC = new TypedIOPort(this, "controlC");
        controlC.setTypeEquals(BaseType.GENERAL);
        controlC.setOutput(true);

        controlV = new TypedIOPort(this, "controlV");
        controlV.setTypeEquals(BaseType.GENERAL);
        controlV.setOutput(true);
    }

    public TypedIOPort controlV;

    public TypedIOPort controlC;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Fire this actor.
     *  Blah, blah, blah.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) _debug("fire has been called");

	if (_copyKeyPressed) {
	    _copyKeyPressed = false;
	    controlC.broadcast(new Token());
	}

	if (_pasteKeyPressed) {
	    _pasteKeyPressed = false;
	    controlV.broadcast(new Token());
	}

	if (_debugging) _debug("fire has completed");
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

    private class MyFrame extends JFrame {

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

	    // Copy callback
            ActionListener myCopyListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			if (_debugging) _debug("copy callback called");
			_copyKeyPressed = true;
			try {
			    getDirector().fireAtCurrentTime(
                                    KeystrokeSensor.this);
			} catch (IllegalActionException ex) {
			    System.out.println(this
			            + "Ex calling fireAtCurrentTime");
			    throw new RuntimeException("-fireAt* C catch-");
			}
			if (_debugging) _debug("copy callback completed");
		    }
	    };

	    // Paste callback
            ActionListener myPasteListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			if (_debugging) _debug("pasteFrom.. has been called");
			_pasteKeyPressed = true;
			try {
			    getDirector().fireAtCurrentTime(
                                    KeystrokeSensor.this);
			} catch (IllegalActionException ex) {
			    System.out.println("--" + ex.toString() + "--");
			    System.out.println(this
				    + "Exception calling fireAtCurrentTime");
			    throw new RuntimeException("-fireAt* catch-");
			}
			if (_debugging) _debug("pasteFrom.. has completed");
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
    }
}





