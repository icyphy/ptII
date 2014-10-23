/* An actor which pops up a keystroke-sensing JFrame.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ArrowKeySensor

/**
 Detect when the user presses or releases an arrow key and produce an
 integer on the corresponding output.

 <p>When this actor is preinitialized, it pops up a new JFrame window on
 the desktop, usually in the upper left hand corner of the screen.
 When this JFrame has the focus (such as when it has been clicked on)
 it is capable of sensing keystrokes.

 <p>This actor senses only the four non-numeric-pad arrow-key
 keystrokes.  This actor responds to key releases as well as key
 presses.  Upon each key press, the integer 1 is broadcast from the
 corresponding output.  Upon each key release, the integer 0 is output.

 <p>This actor contains a private inner class which generated the JFrame.
 The frame sets up call-backs which react to the keystrokes.  When called,
 these call the director's fireAt() method with the current
 time as argument.  This causes the director to call fire() on the actor with
 the first valid time equal to the current time or later.   The actor then broadcasts
 tokens from one or both outputs depending on which keystroke(s) have
 occurred since the actor was last fired. On key pressed a one will be send, on key release
 a zero will be send.

 @author Winthrop Williams, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (winthrop)
 @Pt.AcceptedRating Red (winthrop)
 */
public class ArrowKeySensor extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ArrowKeySensor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Outputs
        upArrow = new TypedIOPort(this, "upArrow");
        upArrow.setTypeEquals(BaseType.INT);
        upArrow.setOutput(true);

        leftArrow = new TypedIOPort(this, "leftArrow");
        leftArrow.setTypeEquals(BaseType.INT);
        leftArrow.setOutput(true);

        rightArrow = new TypedIOPort(this, "rightArrow");
        rightArrow.setTypeEquals(BaseType.INT);
        rightArrow.setOutput(true);

        downArrow = new TypedIOPort(this, "downArrow");
        downArrow.setTypeEquals(BaseType.INT);
        downArrow.setOutput(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Output port, which has type IntToken. */
    public TypedIOPort upArrow;

    /** Output port, which has type IntToken. */
    public TypedIOPort leftArrow;

    /** Output port, which has type IntToken. */
    public TypedIOPort rightArrow;

    /** Output port, which has type IntToken. */
    public TypedIOPort downArrow;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Broadcast the integer value 1 for each key pressed and 0 for
     *  each released.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // Broadcast key presses.
        if (_upKeyPressed) {
            _upKeyPressed = false;
            upArrow.broadcast(new IntToken(1));
        }

        if (_leftKeyPressed) {
            _leftKeyPressed = false;
            leftArrow.broadcast(new IntToken(1));
        }

        if (_rightKeyPressed) {
            _rightKeyPressed = false;
            rightArrow.broadcast(new IntToken(1));
        }

        if (_downKeyPressed) {
            _downKeyPressed = false;
            downArrow.broadcast(new IntToken(1));
        }

        // Broadcast key releases.
        if (_upKeyReleased) {
            _upKeyReleased = false;
            upArrow.broadcast(new IntToken(0));
        }

        if (_leftKeyReleased) {
            _leftKeyReleased = false;
            leftArrow.broadcast(new IntToken(0));
        }

        if (_rightKeyReleased) {
            _rightKeyReleased = false;
            rightArrow.broadcast(new IntToken(0));
        }

        if (_downKeyReleased) {
            _downKeyReleased = false;
            downArrow.broadcast(new IntToken(0));
        }
    }

    /** Create the JFrame window capable of detecting the key-presses.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _myFrame = new MyFrame();
    }

    /** Dispose of the JFrame, causing the window to vanish.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (_myFrame != null) {
            _myFrame.dispose();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A flag indicating if the down arrow key has been pressed
     *  since the last firing of the actor.  <i>Pressed</i> and
     *  <i>Released</i> are are not allowed to both be true for the
     *  same key (Though both may be false).  The most recent action
     *  (press or release) takes precedence.
     */
    protected boolean _downKeyPressed = false;

    /** A flag indicating if the down arrow key has been released
     *  since the last firing of the actor.
     */
    protected boolean _downKeyReleased = false;

    /** A flag indicating if the left arrow key has been released
     *  since the last firing of the actor.
     */
    protected boolean _leftKeyPressed = false;

    /** A flag indicating if the left arrow key has been released
     *  since the last firing of the actor.
     */
    protected boolean _leftKeyReleased = false;

    /** A flag indicating if the right arrow key has been pressed
     *  since the last firing of the actor.
     */
    protected boolean _rightKeyPressed = false;

    /** A flag indicating if the right arrow key has been released
     *  since the last firing of the actor.
     */
    protected boolean _rightKeyReleased = false;

    /** A flag indicating if the up arrow key has been pressed
     *  since the last firing of the actor.
     */
    protected boolean _upKeyPressed = false;

    /** A flag indicating if the up arrow key has been released
     *  since the last firing of the actor.
     */
    protected boolean _upKeyReleased = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The JFrame that contains the arrow keys. */
    private MyFrame _myFrame;

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////
    @SuppressWarnings("serial")
    private class MyFrame extends JFrame {
        /** Construct a frame.  After constructing this, it is
         *  necessary to call setVisible(true) to make the frame
         *  appear.  This is done by calling show() at the end of this
         *  constructor.
         *  @see Tableau#show()
         */
        public MyFrame() {
            // up-arrow call-backs
            ActionListener myUpPressedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _upKeyPressed = true;
                            _upKeyReleased = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            ActionListener myUpReleasedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _upKeyReleased = true;
                            _upKeyPressed = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            // left-arrow call-backs
            ActionListener myLeftPressedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _leftKeyPressed = true;
                            _leftKeyReleased = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            ActionListener myLeftReleasedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _leftKeyReleased = true;
                            _leftKeyPressed = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            // right-arrow call-backs
            ActionListener myRightPressedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _rightKeyPressed = true;
                            _rightKeyReleased = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            ActionListener myRightReleasedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _rightKeyReleased = true;
                            _rightKeyPressed = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            // down-arrow call-backs
            ActionListener myDownPressedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _downKeyPressed = true;
                            _downKeyReleased = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            ActionListener myDownReleasedListener = new ActionListenerExceptionCatcher(
                    new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            _downKeyReleased = true;
                            _downKeyPressed = false;
                            _tryCallingFireAtFirstValidTime();
                        }
                    });

            getContentPane().setLayout(new BorderLayout());

            JLabel label = new JLabel("This window reads Arrow Key strokes. "
                    + "It must have the focus (be on top) to do this");
            getContentPane().add(label);

            // As of jdk1.4, the .registerKeyboardAction() method below is
            // considered obsolete.  Docs recommend using these two methods:
            //  .getInputMap().put(aKeyStroke, aCommand);
            //  .getActionMap().put(aCommand, anAction);
            // with the String aCommand inserted to link them together.
            // See javax.swing.Jcomponent.registerKeyboardAction().
            // Registration of up-arrow call-backs.
            label.registerKeyboardAction(myUpPressedListener, "UpPressed",
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            label.registerKeyboardAction(myUpReleasedListener, "UpReleased",
                    KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            // Registration of left-arrow call-backs.
            label.registerKeyboardAction(myLeftPressedListener, "LeftPressed",
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            label.registerKeyboardAction(myLeftReleasedListener,
                    "LeftReleased",
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            // Registration of right-arrow call-backs.
            label.registerKeyboardAction(myRightPressedListener,
                    "RightPressed",
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            label.registerKeyboardAction(myRightReleasedListener,
                    "RightReleased",
                    KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            // Registration of down-arrow call-backs.
            label.registerKeyboardAction(myDownPressedListener, "DownPressed",
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            label.registerKeyboardAction(myDownReleasedListener,
                    "DownReleased",
                    KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true),
                    JComponent.WHEN_IN_FOCUSED_WINDOW);

            label.setRequestFocusEnabled(true);
            label.addMouseListener(new FocusMouseListener());

            // Set the default size.
            // Note that the location is of the frame, while the size
            // is of the scrollpane.
            pack();
            setVisible(true);
        }

        /* A mouse listener that requests focus for the source of any
         * mouse event it receives.
         */
        private/*static*/class FocusMouseListener implements MouseListener {
            // FindBugs suggests making this class static so as to decrease
            // the size of instances and avoid dangling references.
            // However, Java does allow inner classes of inner classes to be
            // static.

            // This is a copy of diva/gui/toolbox/FocusMouseListener.java
            // because we don't want the dependency on diva.

            /** Invoked when the mouse is released.
             *  Ignored in this listener.
             *  @param event The corresponding event.
             */
            @Override
            public void mouseReleased(MouseEvent event) {
            }

            /** Invoked when the mouse enters a component.
             *  Ignored in this listener.
             *  @param event The corresponding event.
             */
            @Override
            public void mouseEntered(MouseEvent event) {
            }

            /** Invoked when the mouse exits a component.
             *  Ignored in this listener.
             *  @param event The corresponding event.
             */
            @Override
            public void mouseExited(MouseEvent event) {
            }

            /**
             * Grab the keyboard focus when the component that this
             * listener is attached to is clicked on.
             *  @param event The corresponding event.
             */
            @Override
            public void mousePressed(MouseEvent event) {
                Component component = event.getComponent();

                if (!component.hasFocus()) {
                    component.requestFocus();
                }
            }

            /** Invoked when the mouse is clicked (pressed and released).
             *  Ignored in this listener.
             *  @param event The corresponding event.
             */
            @Override
            public void mouseClicked(MouseEvent event) {
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                     private methods                        ////

        /** We schedule a fire as soon as possible.
         */
        private void _tryCallingFireAtFirstValidTime() {
            Director director = getDirector();
            try {
                director.fireAt(ArrowKeySensor.this, director.getModelTime());
            } catch (IllegalActionException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static class ActionListenerExceptionCatcher implements
    ActionListener {
        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Construct an instance that will wrap an ActionListener,
         * catch its exceptions and report it to the Ptolemy
         * Message Handler.
         * @param actionListener The actionListener.
         */
        public ActionListenerExceptionCatcher(ActionListener actionListener) {
            _actionListener = actionListener;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                _actionListener.actionPerformed(e);
            } catch (Throwable ex) {
                ptolemy.util.MessageHandler.error(
                        ptolemy.util.MessageHandler.shortDescription(ex), ex);
            }
        }

        ///////////////////////////////////////////////////////////////////
        ////                         private variables                 ////

        // The runnable.
        private ActionListener _actionListener;
    }

}
