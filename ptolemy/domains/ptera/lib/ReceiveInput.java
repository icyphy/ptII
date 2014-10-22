/* An event that receives user input and allows model time to be advanced.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.ptera.lib;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.domains.ptera.kernel.TimeAdvanceEvent;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ReceiveInput

/**
 An event that receives user input and allows model time to be advanced. The
 user input can be key presses or mouse presses. A tableau is used to receive
 the input. The {@link #timeAdvance} parameter defines the amount of model time
 that is advanced at the same time as the user sends input. For example, if the
 current model time is 5 and timeAdvance parameter is 1 when this event is
 fired, then the user input is considered to be received at model time 6. The
 events scheduled to occur between time 5 and 6 occur in the background, but the
 events scheduled to occur after 6 would be delayed until the user input is
 actually received. Whether the events scheduled exactly at time 5 or 6 are
 processed while the user input is being waited for depends on the LIFO or FIFO
 policy of the model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ReceiveInput extends Event implements TimeAdvanceEvent {

    /** Construct an event with the given name contained by the specified
     *  composite entity. The container argument must not be null, or a
     *  NullPointerException will be thrown. This event will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string.
     *  Increment the version of the workspace.
     *  This constructor write-synchronizes on the workspace.
     *
     *  @param container The container.
     *  @param name The name of the state.
     *  @exception IllegalActionException If the state cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   that of an entity already in the container.
     */
    public ReceiveInput(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        referredTableau = new StringParameter(this, "referredTableau");

        timeAdvance = new Parameter(this, "timeAdvance");
        timeAdvance.setTypeEquals(BaseType.DOUBLE);
        timeAdvance.setExpression("0.0");

        acceptableComponentType = new StringParameter(this,
                "acceptableComponentType");

        receiveKeyPress = new Parameter(this, "receiveKeyPress");
        receiveKeyPress.setTypeEquals(BaseType.BOOLEAN);
        receiveKeyPress.setExpression("true");

        acceptableKeyPattern = new StringParameter(this, "acceptableKeyPattern");

        receiveMousePress = new Parameter(this, "receiveMousePress");
        receiveMousePress.setTypeEquals(BaseType.BOOLEAN);
        receiveMousePress.setExpression("false");

        componentType = new StringParameter(this, "componentType");
        componentType.setPersistent(false);
        componentType.setVisibility(Settable.NOT_EDITABLE);

        keyPressText = new StringParameter(this, "keyPressText");
        keyPressText.setPersistent(false);
        keyPressText.setVisibility(Settable.NOT_EDITABLE);

        mousePressLocation = new Parameter(this, "mousePressLocation");
        mousePressLocation.setTypeEquals(BaseType.INT_MATRIX);
        mousePressLocation.setToken("[-1, -1]");
        mousePressLocation.setPersistent(false);
        mousePressLocation.setVisibility(Settable.NOT_EDITABLE);
    }

    /** Clone the state into the specified workspace. This calls the
     *  base class and then sets the attribute and port public members
     *  to refer to the attributes and ports of the new state.
     *
     *  @param workspace The workspace for the new event.
     *  @return A new event.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ReceiveInput newObject = (ReceiveInput) super.clone(workspace);

        // Findbugs:
        //  [M M IS] Inconsistent synchronization [IS2_INCONSISTENT_SYNC]
        // Actually this is not a problem since the object is
        // being created and hence nobody else has access to it.

        newObject._inputListeners = null;
        return newObject;
    }

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #refire(Token, RefiringData)
     */
    @Override
    public RefiringData fire(Token arguments) throws IllegalActionException {
        super.fire(arguments);

        Pattern keyPattern;
        String keyPatternExpression = acceptableKeyPattern.stringValue();
        if (keyPatternExpression.equals("")) {
            keyPattern = null;
        } else {
            keyPattern = Pattern.compile(keyPatternExpression);
        }

        Class<?> componentClass;
        String componentTypeName = acceptableComponentType.stringValue().trim();
        if (componentTypeName.equals("")) {
            componentClass = null;
        } else {
            try {
                componentClass = Class.forName(componentTypeName);
            } catch (ClassNotFoundException e) {
                throw new IllegalActionException(this, e, "Unable to resolve "
                        + "type " + componentTypeName + ".");
            }
        }

        Tableau tableau = EventUtils.getTableau(this, referredTableau, null);
        JFrame frame = tableau.getFrame();

        InputListener listener = new InputListener(
                ((DoubleToken) timeAdvance.getToken()).doubleValue(), frame,
                componentClass,
                ((BooleanToken) receiveKeyPress.getToken()).booleanValue(),
                keyPattern,
                ((BooleanToken) receiveMousePress.getToken()).booleanValue());
        synchronized (this) {
            if (_inputListeners == null) {
                _inputListeners = new LinkedList<InputListener>();
            }
            _inputListeners.add(listener);
        }
        _addListener(frame, listener);
        frame.addWindowListener(listener);

        return listener;
    }

    /** Return a string that represents the amount of model time advancement.
     *
     *  @return The string.
     */
    @Override
    public String getTimeAdvanceText() {
        return timeAdvance.getExpression();
    }

    /** Continue the processing of this event with the given arguments from the
     *  previous fire() or refire(). The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event, which must be
     *   either an ArrayToken or a RecordToken.
     *  @param data The refiring data structure returned by the previous fire()
     *   or refire().
     *  @return A refiring data structure that contains a non-negative double
     *   number if refire() should be called after that amount of model time, or
     *   null if refire() need not be called.
     *  @exception IllegalActionException If the number of the arguments or
     *   their types do not match, the actions cannot be executed, or any
     *   expression (such as guards and arguments to the next events) cannot be
     *   evaluated.
     *  @see #fire(Token)
     */
    @Override
    public RefiringData refire(Token arguments, RefiringData data)
            throws IllegalActionException {
        InputListener listener = (InputListener) data;

        synchronized (listener) {
            if (!listener._finished) {
                try {
                    workspace().wait(listener);
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }
        _removeListener(listener._frame, listener);
        synchronized (this) {
            if (_inputListeners != null) {
                _inputListeners.remove(listener);
            }
        }
        if (listener._componentType != null) {
            componentType.setExpression(listener._componentType.getName());
        }
        if (listener._keyPressText != null) {
            keyPressText.setExpression(listener._keyPressText);
        }
        if (listener._mousePressLocation != null) {
            mousePressLocation.setExpression("["
                    + listener._mousePressLocation.x + ", "
                    + listener._mousePressLocation.y + "]");
        }
        return null;
    }

    /** Request that the event cease execution altogether.
     */
    @Override
    public void stop() {
        synchronized (this) {
            if (_inputListeners != null) {
                for (InputListener listener : _inputListeners) {
                    synchronized (listener) {
                        listener.notify();
                    }
                }
            }
        }
    }

    /** The name of the class of the component that can accept user input, such
     *  as "java.awt.Button", or empty.
     */
    public StringParameter acceptableComponentType;

    /** A string pattern in the Java regular expression used to match acceptable
     *  key input.
     */
    public StringParameter acceptableKeyPattern;

    /** Class name of the component that actually receives the user input, which
     *  is automatically updated after the input is received.
     */
    public StringParameter componentType;

    /** The text of the key input, which is automatically updated after the
     *  input is received.
     */
    public StringParameter keyPressText;

    /** Screen location of the mouse press, which is automatically updated after
     *  the input is received.
     */
    public Parameter mousePressLocation;

    /** A Boolean parameter to determine whether key presses are accepted.
     */
    public Parameter receiveKeyPress;

    /** A Boolean parameter to determine whether mouse presses are accepted.
     */
    public Parameter receiveMousePress;

    /** The parameter that contains the name of the TableauParameter to be used.
     *  It cannot be empty.
     */
    public StringParameter referredTableau;

    /** The amount (in double) of model time advancement.
     */
    public Parameter timeAdvance;

    /** Add the listener to the component and all components contained by that
     *  component.
     *
     *  @param component The component.
     *  @param listener The listener.
     */
    private void _addListener(Component component, InputListener listener) {
        if (listener._receiveKeyPress) {
            component.addKeyListener(listener);
        }
        if (listener._receiveMousePress) {
            component.addMouseListener(listener);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                _addListener(child, listener);
            }
        }
    }

    /** Remove the listener from the component and all components contained by
     *  that component.
     *
     *  @param component The component.
     *  @param listener The listener.
     */
    private void _removeListener(Component component, InputListener listener) {
        if (listener._receiveKeyPress) {
            component.removeKeyListener(listener);
        }
        if (listener._receiveMousePress) {
            component.removeMouseListener(listener);
        }
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                _removeListener(child, listener);
            }
        }
    }

    /** The list of input listeners. The size can be greater than 1 when this
     *  event is processed multiple times and the previous processing has not
     *  finished.
     */
    private List<InputListener> _inputListeners;

    ///////////////////////////////////////////////////////////////////
    //// InputListener

    /**
     The input listener to listen to the key and mouse input.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class InputListener extends RefiringData implements KeyListener,
            MouseListener, WindowListener {

        /** React to a key press event.
         *
         *  @param e The key press event.
         */
        @Override
        public void keyPressed(KeyEvent e) {
            if (_receiveKeyPress
                    && (_acceptableComponentType == null || _acceptableComponentType
                            .isInstance(e.getComponent()))) {
                String text = KeyEvent.getKeyText(e.getKeyCode());
                if (_keyPattern == null || _keyPattern.matcher(text).matches()) {
                    e.consume();
                    _componentType = e.getComponent().getClass();
                    _keyPressText = text;
                    _finish();
                }
            }
        }

        /** Do nothing.
         *
         *  @param e The key release event.
         */
        @Override
        public void keyReleased(KeyEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The key type event.
         */
        @Override
        public void keyTyped(KeyEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The mouse click event.
         */
        @Override
        public void mouseClicked(MouseEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The mouse enter event.
         */
        @Override
        public void mouseEntered(MouseEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The mouse exit event.
         */
        @Override
        public void mouseExited(MouseEvent e) {
        }

        /** React to a mouse press event.
         *
         *  @param e The mouse press event.
         */
        @Override
        public void mousePressed(MouseEvent e) {
            if (_receiveMousePress
                    && (_acceptableComponentType == null || _acceptableComponentType
                            .isInstance(e.getComponent()))) {
                e.consume();
                _componentType = e.getComponent().getClass();
                _mousePressLocation = new Point(e.getX(), e.getY());
                _finish();
            }
        }

        /** Do nothing.
         *
         *  @param e The mouse release event.
         */
        @Override
        public void mouseReleased(MouseEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The window activation event.
         */
        @Override
        public void windowActivated(WindowEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The window close event.
         */
        @Override
        public void windowClosed(WindowEvent e) {
        }

        /** End the input listening.
         *
         *  @param e The window closing event.
         */
        @Override
        public void windowClosing(WindowEvent e) {
            _finish();
        }

        /** Do nothing.
         *
         *  @param e The window deactivation event.
         */
        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The window deiconification event.
         */
        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The window iconification event.
         */
        @Override
        public void windowIconified(WindowEvent e) {
        }

        /** Do nothing.
         *
         *  @param e The window open event.
         */
        @Override
        public void windowOpened(WindowEvent e) {
        }

        /** Construct an input listener.
         *
         *  @param timeAdvance The amount of time advancement.
         *  @param frame The frame to be used.
         *  @param acceptableComponentType The class of components that can
         *   accept input.
         *  @param receiveKeyPress Whether key presses are acceptable.
         *  @param keyPattern The key pattern.
         *  @param receiveMousePress Whether mouse presses are acceptable.
         */
        InputListener(double timeAdvance, JFrame frame,
                Class<?> acceptableComponentType, boolean receiveKeyPress,
                Pattern keyPattern, boolean receiveMousePress) {
            super(timeAdvance);
            _frame = frame;
            _acceptableComponentType = acceptableComponentType;
            _receiveKeyPress = receiveKeyPress;
            _keyPattern = keyPattern;
            _receiveMousePress = receiveMousePress;
        }

        /** End the input listening.
         */
        private void _finish() {
            synchronized (this) {
                _finished = true;
                notify();
            }
        }

        /** The class of components that can accept input.
         */
        private Class<?> _acceptableComponentType;

        /** Class of the component that actually receives the user input, which
         *  is automatically updated after the input is received.
         */
        private Class<?> _componentType;

        /** Whether input listening is finished.
         */
        private boolean _finished;

        /** The frame to be used.
         */
        private JFrame _frame;

        /** The key pattern.
         */
        private Pattern _keyPattern;

        /** The text of the key input, which is automatically updated after the
         *  input is received.
         */
        private String _keyPressText;

        /** Screen location of the mouse press, which is automatically updated
         *  after the input is received.
         */
        private Point _mousePressLocation;

        /** Whether key presses are acceptable.
         */
        private boolean _receiveKeyPress;

        /** Whether mouse presses are acceptable.
         */
        private boolean _receiveMousePress;
    }
}
