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

// Imports from ptolemy/vergil/basic/BasicGraphFrame.java

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.LocationAttribute;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.RunTableau;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.CancelException;
import ptolemy.gui.MessageHandler;
import ptolemy.gui.Top;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.moml.LibraryAttribute;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.tree.EntityTreeModel;
import ptolemy.vergil.tree.PTree;
import ptolemy.vergil.tree.VisibleTreeModel;

import diva.gui.toolbox.FocusMouseListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.tree.TreeModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

//=====================================================================//
// Actor imports

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;


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
*/
public class Wormhole extends TypedAtomicActor {

    public Wormhole(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Output
        pasteOutput = new TypedIOPort(this, "pasteOutput");
        pasteOutput.setTypeEquals(BaseType.GENERAL);
        pasteOutput.setOutput(true);

        /*
        copyInput
        pasteOutput
        copyTrigger
        focusTrigger
        */

    }

    public TypedIOPort pasteOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void fire() throws IllegalActionException {
        if (_debugging) _debug("fire has been called");
        Clipboard clipboard = java.awt.Toolkit.getDefaultToolkit()
                .getSystemClipboard();
        Transferable transferable = clipboard.getContents(_myframe);
        try{
            pasteOutput.broadcast(new StringToken( (String)transferable
                    .getTransferData(DataFlavor.stringFlavor) ));
        // NullPointerException also possible //
            ////////////////////////////////////
        } catch (java.io.IOException ex) {
            throw new IllegalActionException(this,
                    " Failed to paste (IO Exception): " + ex);
        } catch (java.awt.datatransfer.UnsupportedFlavorException ex) {
            throw new IllegalActionException(this,
                    " Failed to paste: (Flavor Exception)" + ex);
        }
        if (_debugging) _debug("fire has completed");
    }

    public void preinitialize() {
        if (_debugging) _debug("frame will be constructed");
        _myframe = new MyFrame();
        if (_debugging) _debug("frame was constructed");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables

    private MyFrame _myframe;

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    private class MyFrame extends JFrame
        implements ClipboardOwner {

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
            ActionListener myPasteListener = new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
			pasteFromBufferToPasteOutput(); 
		    } 
	    };
            getContentPane().setLayout(new BorderLayout());
            JLabel label = new JLabel("Paste here!");
            getContentPane().add(label);
            label.registerKeyboardAction(myPasteListener, "Paste",
                    KeyStroke.getKeyStroke(
                    KeyEvent.VK_V, java.awt.Event.CTRL_MASK),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);
            label.setRequestFocusEnabled(true);
            label.addMouseListener(new FocusMouseListener());
            // Set the default size.
            // Note that the location is of the frame, while the size
            // is of the scrollpane.
            pack();
            if (_debugging) _debug("frame constructor completes");
        }

        /** Get the currently selected objects from this document, if any,
         *  and place them on the clipboard in MoML format.
         */
        /*
        public void copy() {
            String buff = new String("xyz");
            Clipboard clipboard =
                java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(
                        new StringSelection(buff.toString()), this);
            }
            catch (Exception ex) {
                MessageHandler.error("Copy failed", ex);
            }
        }
        */

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
            if (_debugging) _debug("paste* has been called");
            try {
                getDirector().fireAtCurrentTime(Wormhole.this);
            } catch (IllegalActionException ex) {
                System.out.println(this
                        + "Exception calling fireAtCurrentTime");
                throw new RuntimeException("-fireAt* catch-");
            }
            if (_debugging) _debug("paste* has completed");
        }
    }
}





