/* An editor for Ptolemy II objects.

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
package ptolemy.actor.gui;

import java.awt.Component;
import java.awt.Window;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.CloseListener;
import ptolemy.kernel.util.Decorator;
import ptolemy.kernel.util.DecoratorAttributes;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// Configurer

/**
 This class is an editor for the user settable attributes of an object.
 It may consist of more than one editor panel.  If the object has
 any attributes that are instances of EditorPaneFactory, then the
 panes made by those factories are stacked vertically in this panel.
 Otherwise, a static method of EditorPaneFactory is
 used to construct a default editor.
 <p>
 The restore() method restores the values of the attributes of the
 object to their values when this object was created.  This can be used
 in a modal dialog to implement a cancel button, which restores
 the attribute values to those before the dialog was opened.
 <p>
 This class is created by an instance of the EditParametersDialog class
 to handle the part of the dialog that edits the parameters.

 @see EditorPaneFactory
 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
@SuppressWarnings("serial")
public class Configurer extends JPanel implements CloseListener {
    /** Construct a configurer for the specified object.  This stores
     *  the current values of any Settable attributes of the given object,
     *  and then defers to any editor pane factories contained by
     *  the given object to populate this panel with widgets that
     *  edit the attributes of the given object.  If there are no
     *  editor pane factories, then a default editor pane is created.
     *  @param object The object to configure.
     */
    public Configurer(final NamedObj object) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //setLayout(new BorderLayout());
        _object = object;

        // Record the original values so a restore can happen later.
        _originalValues = new HashMap<Settable, String>();
        Set<Settable> parameters = _getVisibleSettables(object, true);
        for (Settable parameter : parameters) {
            _originalValues.put(parameter, parameter.getExpression());
        }

        boolean foundOne = false;
        Iterator<?> editors = object.attributeList(EditorPaneFactory.class)
                .iterator();

        while (editors.hasNext()) {
            foundOne = true;

            EditorPaneFactory editor = (EditorPaneFactory) editors.next();
            Component pane = editor.createEditorPane();
            add(pane);

            // Inherit the background color from the container.
            pane.setBackground(null);

            if (pane instanceof CloseListener) {
                _closeListeners.add(pane);
            }
        }

        if (!foundOne) {
            // There is no attribute of class EditorPaneFactory.
            // We cannot create one because that would have to be done
            // as a mutation, something that is very difficult to do
            // while constructing a modal dialog.  Synchronized interactions
            // between the thread in which the manager performs mutations
            // and the event dispatch thread prove to be very tricky,
            // and likely lead to deadlock.  Hence, instead, we use
            // the static method of EditorPaneFactory.
            Component pane = EditorPaneFactory.createEditorPane(object);
            add(pane);//, BorderLayout.CENTER);

            // Inherit the background color from the container.
            pane.setBackground(null);

            if (pane instanceof CloseListener) {
                _closeListeners.add(pane);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the given settable should be visible in a
     *  configurer panel for the specified target. Any settable with
     *  visibility FULL or NOT_EDITABLE will be visible.  If the target
     *  contains an attribute named "_expertMode", then any
     *  attribute with visibility EXPERT will also be visible.
     *  @param target The object to be configured.
     *  @param settable The object whose visibility is returned.
     *  @return True if settable is FULL or NOT_EDITABLE or True
     *  if the target has an _expertMode attribute and the settable
     *  is EXPERT.  Otherwise, return false.
     */
    public static boolean isVisible(NamedObj target, Settable settable) {
        if (settable.getVisibility() == Settable.FULL
                || settable.getVisibility() == Settable.NOT_EDITABLE) {
            return true;
        }

        if (target.getAttribute("_expertMode") != null
                && settable.getVisibility() == Settable.EXPERT) {
            return true;
        }

        return false;
    }

    /** Request restoration of the user settable attribute values to what they
     *  were when this object was created.  The actual restoration
     *  occurs later, in the UI thread, in order to allow all pending
     *  changes to the attribute values to be processed first. If the original
     *  values match the current values, then nothing is done.
     */
    public void restore() {
        // This is done in the UI thread in order to
        // ensure that all pending UI events have been
        // processed.  In particular, some of these events
        // may trigger notification of new attribute values,
        // which must not be allowed to occur after this
        // restore is done.  In particular, the default
        // attribute editor has lines where notification
        // of updates occurs when the line loses focus.
        // That notification occurs some time after the
        // window is destroyed.
        // FIXME: Unfortunately, this gets
        // invoked before that notification occurs if the
        // "X" is used to close the window.  Swing bug?
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // First check for changes.

                // FIXME Currently it is not possible to restore decorated attributes
                //      since they don't show up in moml yet.
                Set<Settable> parameters = _getVisibleSettables(_object, false);
                boolean hasChanges = false;
                StringBuffer buffer = new StringBuffer("<group>\n");

                for (Settable parameter : parameters) {
                    String newValue = parameter.getExpression();
                    String oldValue = _originalValues.get(parameter);

                    if (!newValue.equals(oldValue)) {
                        hasChanges = true;
                        buffer.append("<property name=\"");
                        buffer.append(((NamedObj) parameter).getName(_object));
                        buffer.append("\" value=\"");
                        buffer.append(StringUtilities.escapeForXML(oldValue));
                        buffer.append("\"/>\n");
                    }
                }

                buffer.append("</group>\n");

                // If there are changes, then issue a change request.
                // Use a MoMLChangeRequest so undo works... I.e., you can undo a cancel
                // of a previous change.
                if (hasChanges) {
                    MoMLChangeRequest request = new MoMLChangeRequest(this, // originator
                            _object, // context
                            buffer.toString(), // MoML code
                            null); // base
                    _object.requestChange(request);
                }
            }
        });
    }

    /** Restore parameter values to their defaults.
     */
    public void restoreToDefaults() {
        // This is done in the UI thread in order to
        // ensure that all pending UI events have been
        // processed.  In particular, some of these events
        // may trigger notification of new attribute values,
        // which must not be allowed to occur after this
        // restore is done.  In particular, the default
        // attribute editor has lines where notification
        // of updates occurs when the line loses focus.
        // That notification occurs some time after the
        // window is destroyed.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // FIXME Currently it is not possible to restore decorated attributes
                //      since they don't show up in moml yet.

                Set<Settable> parameters = _getVisibleSettables(_object, false);
                StringBuffer buffer = new StringBuffer("<group>\n");
                final List<Settable> parametersReset = new LinkedList<Settable>();

                for (Settable parameter : parameters) {
                    String newValue = parameter.getExpression();
                    String defaultValue = parameter.getDefaultExpression();

                    if (defaultValue != null && !newValue.equals(defaultValue)) {
                        buffer.append("<property name=\"");
                        buffer.append(((NamedObj) parameter).getName(_object));
                        buffer.append("\" value=\"");
                        buffer.append(StringUtilities
                                .escapeForXML(defaultValue));
                        buffer.append("\"/>\n");
                        parametersReset.add(parameter);
                    }
                }

                buffer.append("</group>\n");

                // If there are changes, then issue a change request.
                // Use a MoMLChangeRequest so undo works... I.e., you can undo a cancel
                // of a previous change.
                if (parametersReset.size() > 0) {
                    MoMLChangeRequest request = new MoMLChangeRequest(this, // originator
                            _object, // context
                            buffer.toString(), // MoML code
                            null) { // base
                        @Override
                        protected void _execute() throws Exception {
                            super._execute();

                            // Reset the derived level, which has the side
                            // effect of marking the object not overridden.
                            Iterator<Settable> parameters = parametersReset
                                    .iterator();

                            while (parameters.hasNext()) {
                                Settable parameter = parameters.next();

                                if (isVisible(_object, parameter)) {
                                    int derivedLevel = ((NamedObj) parameter)
                                            .getDerivedLevel();
                                    // This has the side effect of
                                    // setting to false the flag that
                                    // indicates whether the value of
                                    // this object overrides some
                                    // inherited value.
                                    ((NamedObj) parameter)
                                    .setDerivedLevel(derivedLevel);
                                }
                            }
                        }
                    };

                    _object.requestChange(request);
                }
            }
        });
    }

    /** Notify any panels in this configurer that implement the
     *  CloseListener interface that the specified window has closed.
     *  The second argument, if non-null, gives the name of the button
     *  that was used to close the window.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    @Override
    public void windowClosed(Window window, String button) {
        Iterator<Component> listeners = _closeListeners.iterator();

        while (listeners.hasNext()) {
            CloseListener listener = (CloseListener) listeners.next();
            listener.windowClosed(window, button);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Return the visible Settables of NamedObj object. When
     *  addDecoratorAttributes is true we will also return the
     *  decorated attributes.
     *  In case the passed NamedObj is the top level container, the
     *  parameter enableBackwardTypeInference is added if not present,
     *  with default value false.
     *  @param object The named object for which to show the visible
     *          Settables
     *  @param addDecoratorAttributes A flag that specifies whether
     *          decorated attributes should also be included.
     *  @return The visible attributes.
     */
    static private Set<Settable> _getVisibleSettables(final NamedObj object,
            boolean addDecoratorAttributes) {
        Set<Settable> attributes = new HashSet<Settable>();
        Iterator<?> parameters = object.attributeList(Settable.class)
                .iterator();

        // Add parameter enableBackwardTypeInference to top level container
        if (object.equals(object.toplevel())) {
            try {
                Parameter backwardTypeInf = (Parameter) object.getAttribute(
                        "enableBackwardTypeInference", Parameter.class);
                if (backwardTypeInf == null) {
                    backwardTypeInf = new Parameter(object,
                            "enableBackwardTypeInference");
                    backwardTypeInf.setExpression("false");
                    attributes.add(backwardTypeInf);
                }
                backwardTypeInf.setTypeEquals(BaseType.BOOLEAN);
            } catch (KernelException e) {
                // This should not happen
                throw new InternalErrorException(e);
            }
        }

        while (parameters.hasNext()) {
            Settable parameter = (Settable) parameters.next();

            if (isVisible(object, parameter)) {
                attributes.add(parameter);
            }
        }

        if (addDecoratorAttributes) {
            // Get the decorators that decorate this object, if any.
            Set<Decorator> decorators;
            try {
                decorators = object.decorators();
                for (Decorator decorator : decorators) {
                    // Get the attributes provided by the decorator.
                    DecoratorAttributes decoratorAttributes = object
                            .getDecoratorAttributes(decorator);
                    if (decoratorAttributes != null) {
                        for (Object attribute : decoratorAttributes
                                .attributeList()) {
                            if (attribute instanceof Settable) {
                                Settable settable = (Settable) attribute;
                                if (isVisible(object, settable)) {
                                    attributes.add(settable);
                                }
                            }
                        }
                    }
                }
            } catch (IllegalActionException e) {
                MessageHandler.error("Invalid decorator value", e);
            }
        }
        return attributes;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** A record of the original values. */
    protected HashMap<Settable, String> _originalValues;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A list of panels in this configurer that implement CloseListener,
    // if there are any.
    private List<Component> _closeListeners = new LinkedList<Component>();

    // The object that this configurer configures.
    private NamedObj _object;
}
