/* A tableau representing a debug listener window.

 Copyright (c) 2000-2014 The Regents of the University of California.
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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import ptolemy.kernel.util.DebugEvent;
import ptolemy.kernel.util.DebugListener;
import ptolemy.kernel.util.Debuggable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DebugListenerTableau

/**
 A tableau representing a debug listener window. The constructor of this
 class creates the window.  You must then attached any object implementing
 the Debuggable interface using the setDebuggable() method.
 Once attached, the window will display any debug messages produced by
 that object.  The listener window itself is an instance of the inner class
 DebugListenerFrame, which extends TextEditor, and can be
 accessed using the getFrame() method. As with other tableaux,
 this is an entity that is contained by an effigy of the model.
 There can be any number of instances of this class in an effigy.

 @author  Steve Neuendorffer and Edward A. Lee, Elaine Cheong
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Effigy
 */
public class DebugListenerTableau extends Tableau {
    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public DebugListenerTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        final DebugListenerFrame frame = new DebugListenerFrame();
        setFrame(frame);
        frame.setTableau(this);

        // Listen for window closing events to unregister.
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setDebuggable(null);
            }
        });

        if (container instanceof TextEffigy) {
            ((TextEffigy) container).setDocument(frame.text.getDocument());
        }

        frame.setVisible(true);
        frame.pack();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the object that this tableau is listening to, or null
     *  if none has been set.
     *  @return The current debuggable.
     *  @see #setDebuggable(Debuggable)
     */
    public Debuggable getDebuggable() {
        return _debug;
    }

    /** Set the object for this tableau to listen to, or null to stop
     *  listening to the current one.  If this tableau is already
     *  listening to an object, then first stop listening to that
     *  object.  Set the title of the window to show name of object.
     *  @param debuggable The object to listen to.
     *  @see #getDebuggable()
     */
    public void setDebuggable(Debuggable debuggable) {
        if (_debug != null) {
            _debug.removeDebugListener((DebugListenerFrame) getFrame());
        }

        _debug = debuggable;

        if (_debug != null) {
            DebugListenerFrame debugFrame = (DebugListenerFrame) getFrame();
            _debug.addDebugListener(debugFrame);

            debugFrame.setTitle(((NamedObj) _debug).getFullName());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The object this is listening to.
    private Debuggable _debug;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** This class is a top-level window for displaying textual debug
     *  output from an instance of Debuggable.
     */
    @SuppressWarnings("serial")
    public static class DebugListenerFrame extends TextEditor implements
    DebugListener {
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
        @Override
        public void event(DebugEvent event) {
            text.append(event.toString() + "\n");
            scrollToEnd();
        }

        /** Display the specified message.
         */
        @Override
        public void message(String message) {
            text.append(message + "\n");
            scrollToEnd();
        }
    }
}
