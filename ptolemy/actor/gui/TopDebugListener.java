/* A debug listener that displays messages in a top-level window.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import java.awt.Point;
import java.awt.Rectangle;

import ptolemy.gui.TextEditor;
import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;

//////////////////////////////////////////////////////////////////////////
//// TopDebugListener
/**
A debug listener that displays messages in a top-level window.
Users of this class should listen for window closing to unregister
the class as a listener.  This can be done using code like this:
<pre>
        // Listen for window closing events to unregister as a listener.
        topDebugListenerInstance.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                listenee.removeDebugListener(topDebugListenerInstance);
            }
        });
</pre>

@author  Edward A. Lee
@version $Id$
*/
public class TopDebugListener extends TextEditor implements DebugListener {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Create a debug listener that displays messages in a top-level
     *  window.
     */
    public TopDebugListener() {
        super();
        text.setEditable(false);
        text.setColumns(80);
        text.setRows(20);
        pack();

        // We do not expect derived classes, so we can go ahead and
        // make the window visible.
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Display a string representation of the specified event.
     */
    public void event(DebugEvent event) {
	text.append(event.toString() + "\n");
        // Song and dance to scroll to the new line.
        text.scrollRectToVisible(new Rectangle(new Point(0, text.getHeight())));
    }

    /** Display the specified message.
     */
    public void message(String message) {
        text.append(message + "\n");
        // Song and dance to scroll to the new line.
        text.scrollRectToVisible(new Rectangle(new Point(0, text.getHeight())));
    }
}
