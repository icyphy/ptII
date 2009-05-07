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

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import ptolemy.actor.gui.Configurer;
import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.data.ArrayToken;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.ModelErrorHandler;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Configure

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Configure extends Event {

    /**
     *  @param container
     *  @param name
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public Configure(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            _ignoredParameters.add(parameter.getName());
        }
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Configure newObject = (Configure) super.clone(workspace);
        newObject._ignoredParameters = new HashSet<String>(_ignoredParameters);
        newObject._oldValues = new HashMap<Settable, String>();
        return newObject;
    }

    public RefiringData fire(ArrayToken arguments)
            throws IllegalActionException {
        RefiringData data = super.fire(arguments);

        _executeChangeRequests();
        ModelErrorHandler errorHandler = new BasicModelErrorHandler();
        Map<NamedObj, ModelErrorHandler> oldErrorHandlers =
            new HashMap<NamedObj, ModelErrorHandler>();
        List<Settable> settables = attributeList(Settable.class);
        for (Settable settable : settables) {
            if (_isVisible(settable)) {
                NamedObj namedObj = (NamedObj) settable;
                oldErrorHandlers.put(namedObj, namedObj.getModelErrorHandler());
                namedObj.setModelErrorHandler(errorHandler);
            }
        }

        try {
            boolean success = false;
            while (!success) {
                Query query = new Query();
                JOptionPane options;
                if (query.hasEntries()) {
                    options = new JOptionPane(query,
                            JOptionPane.QUESTION_MESSAGE,
                            JOptionPane.YES_NO_OPTION, null,
                            new String[] {"Set", "Default"}, "Set");
                } else {
                    options = new JOptionPane(query,
                            JOptionPane.INFORMATION_MESSAGE,
                            JOptionPane.CLOSED_OPTION, null,
                            new String[] {"Close"}, "Close");
                }
                JDialog dialog = new JDialog((Frame) null, getName(), true);
                Listener listener = new Listener(dialog);
                query._addKeyListener(listener);
                options.addPropertyChangeListener(listener);
                dialog.setContentPane(options);
                dialog.pack();
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                int x = (toolkit.getScreenSize().width -
                        dialog.getSize().width) / 2;
                int y = (toolkit.getScreenSize().height -
                        dialog.getSize().height) / 2;
                dialog.setLocation(x, y);

                success = true;
                listener._commit = false;
                dialog.setVisible(true);

                if (listener._commit) {
                    try {
                        query.commit();
                    } catch (Throwable t) {
                        query.cancel();
                        success = false;
                    }
                } else {
                    query.cancel();
                }
            }
        } finally {
            for (Map.Entry<NamedObj, ModelErrorHandler> entry
                    : oldErrorHandlers.entrySet()) {
                entry.getKey().setModelErrorHandler(entry.getValue());
            }
        }

        return data;
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        List<Settable> parameters = attributeList(Settable.class);
        _oldValues.clear();
        for (Settable parameter : parameters) {
            if (_isVisible(parameter)) {
                _oldValues.put(parameter, parameter.getExpression());
            }
        }
    }

    public void wrapup() throws IllegalActionException {
        for (Map.Entry<Settable, String> entry : _oldValues.entrySet()) {
            entry.getKey().setExpression(entry.getValue());
        }
        _oldValues.clear();
        super.wrapup();
    }

    private void _executeChangeRequests() {
        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            if (_isVisible(parameter)) {
                ((NamedObj) parameter).executeChangeRequests();
            }
        }
    }

    private boolean _isVisible(Settable settable) {
        return !_ignoredParameters.contains(settable.getName()) &&
                Configurer.isVisible(this, settable);
    }

    private Set<String> _ignoredParameters = new HashSet<String>();

    private Map<Settable, String> _oldValues = new HashMap<Settable, String>();

    private static class Listener extends KeyAdapter
            implements PropertyChangeListener {

        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                event.consume();
                _commit = true;
                _dialog.setVisible(false);
            }
        }

        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getPropertyName();
            if (_dialog.isVisible() && event.getSource() instanceof JOptionPane
                    && (property.equals(JOptionPane.VALUE_PROPERTY) ||
                    property.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
                JOptionPane optionPane = (JOptionPane) event.getSource();
                Object value = optionPane.getValue();
                if (value instanceof String) {
                    String string = (String) value;
                    if (string.equals("Set")) {
                        _commit = true;
                        _dialog.setVisible(false);
                    } else if (string.equals("Default")
                            || string.equals("Close")) {
                        _dialog.setVisible(false);
                    }
                }
            }
        }

        Listener(JDialog dialog) {
            _dialog = dialog;
        }

        private boolean _commit = false;

        private JDialog _dialog;
    }

    private class Query extends PtolemyQuery {

        public Query() {
            super(null);

            setTextWidth(40);
            List<Settable> settables = attributeList(Settable.class);
            _hasEntries = false;
            for (Settable settable : settables) {
                if (_isVisible(settable)) {
                    _hasEntries = true;
                    addStyledEntry(settable);

                    String name = settable.getName();
                    _oldValues.put(name, getStringValue(name));
                }
            }

            if (!_hasEntries) {
                addText(Configure.this.getName() +
                        " has no parameters to configure.",
                        Color.black, SwingConstants.CENTER);
            }
        }

        public void cancel() {
            for (Map.Entry<String, String> entry : _oldValues.entrySet()) {
                set(entry.getKey(), entry.getValue());
                super.changed(entry.getKey());
            }
        }

        public void changed(String name) {
            // Do not update the variables until the commit action is taken.
        }

        public void commit() throws IllegalActionException {
            Set<String> names = _attributes.keySet();
            for (String name : names) {
                super.changed(name);
                ((Settable) _attributes.get(name)).validate();
            }
        }

        public boolean hasEntries() {
            return _hasEntries;
        }

        private void _addKeyListener(KeyListener listener) {
            for (Object entry : _entries.values()) {
                if (entry instanceof Component) {
                    ((Component) entry).addKeyListener(listener);
                }
            }
        }

        private boolean _hasEntries;

        private Map<String, String> _oldValues = new HashMap<String, String>();
    }
}
