/* Post a Message with a dismiss button

 Copyright (c) 1997-1998 The Regents of the University of California.
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
@ProposedRating Red
@AcceptedRating Red
*/

package ptolemy.plot;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/** A simple message display with a close button.
 *  This will eventually replaced by a class from JFC.
 *
 *  @author Christopher Hylands
 *  @version $Id$
 */
public class Message extends Frame {

    /** Pop up a text widget with no scroll bar and close button.
     */
    public Message(String msg) {
        this(msg, null, null, 12, 40, TextArea.SCROLLBARS_NONE);
    }

    /** Pop up a text widget with a close button.
     *  If the background parameter is null, then it is ignored.
     *  If the foreground parameter is null, then it is ignored.
     *
     *  @param msg The message to display.
     *  @param background The background Color.
     *  @param foreground The foreground Color.
     *  @param rows The number of rows to display.
     *  @param columns The number of columns to display.
     *  @param int Determines scrollbar visibility, should be one
     *  of TextArea.SCROLLBARS_BOTH, TextArea.SCROLLBARS_NONE,
     *  TextArea.SCROLLBARS_HORIZONTAL_ONLY or
     *  TextArea.SCROLLBARS_VERTICAL_ONLY.
     */
    public Message(String msg, Color background, Color foreground,
            int rows, int columns, int scrollbars) {
        if (background != null) setBackground(background);
        if (foreground != null) setForeground(foreground);

        _txtarea = new TextArea(msg, rows, columns, scrollbars);
        
        _txtarea.setEditable(false);
        add("Center", _txtarea);

        Button button = new Button("Close");
        Panel panel = new Panel();
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        panel.add(button);
        add("South", panel);

        // Closing the window has the same effect as hitting the close button.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        pack();
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private transient TextArea _txtarea;
}
