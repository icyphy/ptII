/*
 Copyright (c) 1998-2005 The Regents of the University of California
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
 PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package diva.gui.toolbox;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

/**
 * A status bar for displaying at the bottom of typical applications.
 *
 * @author John Reekie
 * @version $Id$
 */
public class JStatusBar extends JPanel {
    /** The progress bar associated with this status bar
     */
    JProgressBar _progress;

    /** The label that displays the status message.
     */
    JLabel _message;

    /** The border around the message
     */
    Border _messageBorder;

    /** Create a new JStatusBar
     */
    public JStatusBar() {
        _message = new JLabel("");
        add(_message);

        _messageBorder = new BevelBorder(BevelBorder.LOWERED);
        _message.setBorder(_messageBorder);

        // ??!?!? How do you make a 1-pixel width border???
        // System.out.println(_messageBorder.getBorderInsets(_message));
        _progress = new JProgressBar();
        _progress.setMinimum(0);
        _progress.setMaximum(100);
        _progress.setValue(00);
        add(_progress);
    }

    /** Return the progress bar associated with this status bar
     */
    public JProgressBar getProgressBar() {
        return _progress;
    }

    /** Set the message displayed in the status bar. Set this
     * to null to clear the message.
     */
    public void setMessage(String message) {
        _message.setText(message);
        _message.setBorder(_messageBorder);
    }
}
