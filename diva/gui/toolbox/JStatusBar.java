/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.toolbox;

import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

/**
 * A status bar for displaying at the bottonm of typical applications.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision$
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
    public JStatusBar () {
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
    public void setMessage (String message) {
        _message.setText(message);
        _message.setBorder(_messageBorder);
    }
}


