/* A viewer for Welcome Windows

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;

import ptolemy.data.BooleanToken;
import ptolemy.gui.Top;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// WelcomeWindow

/**
 A toplevel frame that can view HTML documents, but has no menus. 

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class WelcomeWindow extends HTMLViewer {
    /** Construct a blank HTML Viewer with no menu items.
     */
    public WelcomeWindow() {
        super(); 
        _statusBar = null;

        // Add the close panel
        _closePanel = new JPanel();
        JTextField _message = new JTextField(80);
        _message.setEditable(false);
        _message.setAlignmentX(LEFT_ALIGNMENT);
        _message.setText("FIXME: need \"Show this Dialog on Startup\" "
                + "and Close Button"); 

        _closePanel.add(_message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make this window displayable.  The menubar is hidden and
     *  close panel is added.
     */
    public void pack() {
        hideMenuBar();
        getContentPane().add(_closePanel, BorderLayout.SOUTH);
        super.pack();
    }

    /** Show the window if the _hideWelcomeWindow parameter is not set 
     *  or is false.
     */
    public void show() {
        BooleanToken hideWelcomeWindow = 
            ((BooleanToken) PtolemyPreferences.preferenceValue(getDirectory(),
                    "_hideWelcomeWindow"));

        if (hideWelcomeWindow != null && hideWelcomeWindow.booleanValue()) {
            // The _hideWelcomeWindow parameter is true
            System.out.println("Hide the welcome window");
            return;
        }
        super.show();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The panel at the bottom that contains the
     *  "Show this dialog on startup" checkbox and the close button.
     */
    private JPanel _closePanel;
}

