/* Post a Message with a dismiss button

 Copyright (c) 1997 The Regents of the University of California.
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

package plot;

import java.awt.*;
import java.util.*;

/** Class that implements a simple message display with a close button.
 * @author Christopher Hylands
 * @version $Id$
 */
public class Message extends Frame {
    /** Pop up a text widget with a close button.
      */
    public Message(String msg, Color background, Color foreground) {
 	setBackground(background);
 	setForeground(foreground);

	// TextArea.SCROLLABARS_NONE is not in jdk1.0.2
        _txtarea = new TextArea(msg, 12, 40,TextArea.SCROLLBARS_NONE);
        _txtarea.setEditable(false);
        add("Center", _txtarea);

        Button button = new Button("Close");
        Panel panel = new Panel();
        panel.add(button);
        add("South", panel);

    }

//     public static void main(String args[]){
// 	Message message = new Message("A message", Color.white,Color.black);
// 	message.setTitle("A Message");
// 	message.pack();
// 	message.show();
//     }

    public boolean handleEvent(Event event) {
        if (event.id == Event.WINDOW_DESTROY) {
            if (_inapplet) {
                dispose();
            } else {
                //System.exit(0);
            }
        }   
        return super.handleEvent(event);
    }

    public boolean action(Event event, Object arg) {
	dispose();
        return true;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private TextArea _txtarea;
    private boolean _inapplet = true;
}
