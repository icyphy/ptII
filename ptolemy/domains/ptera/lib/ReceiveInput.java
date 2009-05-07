/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
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

//////////////////////////////////////////////////////////////////////////
//// ReceiveInput

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ReceiveInput extends Event implements TimeAdvanceEvent {

    /**
     *  @param container
     *  @param name
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
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

        acceptableKeyPattern = new StringParameter(this,
                "acceptableKeyPattern");

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

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ReceiveInput newObject = (ReceiveInput) super.clone(workspace);

        // Findbugs:
        //  [M M IS] Inconsistent synchronization [IS2_INCONSISTENT_SYNC]
        // Actually this is not a problem since the object is
        // being created and hence nobody else has access to it.

        newObject._inputListeners = null;
        return newObject;
    }

    public RefiringData fire(ArrayToken arguments) throws IllegalActionException {
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
                throw new IllegalActionException(this, e, "Unable to resolve " +
                        "type " + componentTypeName + ".");
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

    public String getTimeAdvanceText() {
        return timeAdvance.getExpression();
    }

    public RefiringData refire(ArrayToken arguments, RefiringData data)
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
            mousePressLocation.setExpression("[" +
                    listener._mousePressLocation.x + ", " +
                    listener._mousePressLocation.y + "]");
        }
        return null;
    }

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

    public StringParameter acceptableComponentType;

    public StringParameter acceptableKeyPattern;

    public StringParameter componentType;

    public StringParameter keyPressText;

    public Parameter mousePressLocation;

    public Parameter receiveKeyPress;

    public Parameter receiveMousePress;

    public StringParameter referredTableau;

    public Parameter timeAdvance;

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

    private List<InputListener> _inputListeners;

    private class InputListener extends RefiringData implements KeyListener,
            MouseListener, WindowListener {

        public void keyPressed(KeyEvent e) {
            if (_receiveKeyPress && (_acceptableComponentType == null ||
                    _acceptableComponentType.isInstance(e.getComponent()))) {
                String text = KeyEvent.getKeyText(e.getKeyCode());
                if (_keyPattern == null || _keyPattern.matcher(text)
                        .matches()) {
                    e.consume();
                    _componentType = e.getComponent().getClass();
                    _keyPressText = text;
                    _finish();
                }
            }
        }

        public void keyReleased(KeyEvent e) {
        }

        public void keyTyped(KeyEvent e) {
        }

        public void mouseClicked(MouseEvent e) {
        }

        public void mouseEntered(MouseEvent e) {
        }

        public void mouseExited(MouseEvent e) {
        }

        public void mousePressed(MouseEvent e) {
            if (_receiveMousePress && (_acceptableComponentType == null ||
                    _acceptableComponentType.isInstance(e.getComponent()))) {
                e.consume();
                _componentType = e.getComponent().getClass();
                _mousePressLocation = new Point(e.getX(), e.getY());
                _finish();
            }
        }

        public void mouseReleased(MouseEvent e) {
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            _finish();
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

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

        private void _finish() {
            synchronized (this) {
                _finished = true;
                notify();
            }
        }

        private Class<?> _acceptableComponentType;

        private Class<?> _componentType;

        private boolean _finished;

        private JFrame _frame;

        private Pattern _keyPattern;

        private String _keyPressText;

        private Point _mousePressLocation;

        private boolean _receiveKeyPress;

        private boolean _receiveMousePress;
    }
}
