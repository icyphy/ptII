/* An event to display a dialog for the user to input parameter values.

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
import ptolemy.data.Token;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.ModelErrorHandler;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// Configure

/**
 An event to display a dialog for the user to input parameter values.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class Configure extends Event {

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
    public Configure(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            _ignoredParameters.add(parameter.getName());
        }
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
        Configure newObject = (Configure) super.clone(workspace);
        newObject._ignoredParameters = new HashSet<String>(_ignoredParameters);
        newObject._oldValues = new HashMap<Settable, String>();
        return newObject;
    }

    /** Process this event with the given arguments. The number of arguments
     *  provided must be equal to the number of formal parameters defined for
     *  this event, and their types must match. The actions of this event are
     *  executed.
     *
     *  @param arguments The arguments used to process this event.
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
        RefiringData data = super.fire(arguments);

        _executeChangeRequests();
        ModelErrorHandler errorHandler = new BasicModelErrorHandler();
        Map<NamedObj, ModelErrorHandler> oldErrorHandlers = new HashMap<NamedObj, ModelErrorHandler>();
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
                            JOptionPane.YES_NO_OPTION, null, new String[] {
                            "Set", "Default" }, "Set");
                } else {
                    options = new JOptionPane(query,
                            JOptionPane.INFORMATION_MESSAGE,
                            JOptionPane.CLOSED_OPTION, null,
                            new String[] { "Close" }, "Close");
                }
                JDialog dialog = new JDialog((Frame) null, getName(), true);
                Listener listener = new Listener(dialog);
                query._addKeyListener(listener);
                options.addPropertyChangeListener(listener);
                dialog.setContentPane(options);
                dialog.pack();
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                int x = (toolkit.getScreenSize().width - dialog.getSize().width) / 2;
                int y = (toolkit.getScreenSize().height - dialog.getSize().height) / 2;
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
            for (Map.Entry<NamedObj, ModelErrorHandler> entry : oldErrorHandlers
                    .entrySet()) {
                entry.getKey().setModelErrorHandler(entry.getValue());
            }
        }

        return data;
    }

    /** Begin execution of the actor.  This is invoked exactly once
     *  after the preinitialization phase.  Since type resolution is done
     *  in the preinitialization phase, along with topology changes that
     *  may be requested by higher-order function actors, an actor
     *  can produce output data and schedule events in the initialize()
     *  method.
     *
     *  @exception IllegalActionException If execution is not permitted.
     */
    @Override
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

    /** This method is invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.  It finalizes an execution, typically closing
     *  files, displaying final results, etc.  When this method is called,
     *  no further execution should occur.
     *
     *  @exception IllegalActionException If wrapup is not permitted.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        for (Map.Entry<Settable, String> entry : _oldValues.entrySet()) {
            entry.getKey().setExpression(entry.getValue());
        }
        _oldValues.clear();
        super.wrapup();
    }

    /** Execute all the change requests on the parameters used to receive user
     *  inputs.
     */
    private void _executeChangeRequests() {
        List<Settable> parameters = attributeList(Settable.class);
        for (Settable parameter : parameters) {
            if (_isVisible(parameter)) {
                ((NamedObj) parameter).executeChangeRequests();
            }
        }
    }

    /** Return true if the parameter is visible in the displayed dialog for user
     *  inputs.
     *
     *  @param settable The parameter.
     *  @return true if the parameter is visible; false otherwise.
     */
    private boolean _isVisible(Settable settable) {
        return !_ignoredParameters.contains(settable.getName())
                && Configurer.isVisible(this, settable);
    }

    /** Set of the names of parameters that should not be listed in the dialog
     *  for user input.
     */
    private Set<String> _ignoredParameters = new HashSet<String>();

    /** Old values of the parameters to receive user input.
     */
    private Map<Settable, String> _oldValues = new HashMap<Settable, String>();

    ///////////////////////////////////////////////////////////////////
    //// Listener

    /**
     Listener for key input and property change.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class Listener extends KeyAdapter implements
    PropertyChangeListener {

        /** React to a key being pressed in the dialog.
         *
         *  @param event The key event.
         */
        @Override
        public void keyPressed(KeyEvent event) {
            if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                event.consume();
                _commit = true;
                _dialog.setVisible(false);
            }
        }

        /** React to property change.
         *
         *  @param event The property change event.
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getPropertyName();
            if (_dialog.isVisible()
                    && event.getSource() instanceof JOptionPane
                    && (property.equals(JOptionPane.VALUE_PROPERTY) || property
                            .equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
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

        /** Construct a listener for the dialog.
         *
         *  @param dialog The dialog.
         */
        Listener(JDialog dialog) {
            _dialog = dialog;
        }

        /** Whether the action to close the dialog is commit (or cancel
         *  otherwise).
         */
        private boolean _commit = false;

        /** The dialog.
         */
        private JDialog _dialog;
    }

    ///////////////////////////////////////////////////////////////////
    //// Query

    /**
     The query to receive user input.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    @SuppressWarnings("serial")
    private class Query extends PtolemyQuery {

        /** Construct a query.
         */
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
                addText(Configure.this.getName()
                        + " has no parameters to configure.", Color.black,
                        SwingConstants.CENTER);
            }
        }

        /** Cancel the changes.
         */
        public void cancel() {
            for (Map.Entry<String, String> entry : _oldValues.entrySet()) {
                set(entry.getKey(), entry.getValue());
                super.changed(entry.getKey());
            }
        }

        /** React to change of a property.
         *
         *  @param name The name of the entry that has changed.
         */
        @Override
        public void changed(String name) {
            // Do not update the variables until the commit action is taken.
        }

        /** Commit the changes.
         *
         *  @exception IllegalActionException If the value of some parameters\
         *   cannot be set.
         */
        public void commit() throws IllegalActionException {
            Set<String> names = _attributes.keySet();
            for (String name : names) {
                super.changed(name);
                ((Settable) _attributes.get(name)).validate();
            }
        }

        /** Return whether the dialog has any input entry.
         *
         *  @return true if the dialog has input entry.
         */
        public boolean hasEntries() {
            return _hasEntries;
        }

        /** Add a key listener to listen to the key presses in the dialog.
         *
         *  @param listener The key listener.
         */
        private void _addKeyListener(KeyListener listener) {
            for (Object entry : _entries.values()) {
                if (entry instanceof Component) {
                    ((Component) entry).addKeyListener(listener);
                }
            }
        }

        /** Whether the dialog has any input entry.
         */
        private boolean _hasEntries;

        /** Old values of the parameters in the query.
         */
        private Map<String, String> _oldValues = new HashMap<String, String>();
    }
}
