/* A subclass of Query supporting Ptolemy II attributes.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import java.awt.Color;
import java.awt.Window;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

import ptolemy.actor.gui.style.ParameterEditorStyle;
import ptolemy.actor.parameters.IntRangeParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.gui.CloseListener;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.gui.QueryListener;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.ValueListener;
import ptolemy.moml.Documentation;
import ptolemy.moml.ErrorHandler;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// PtolemyQuery
/**
This class is a query dialog box with various entries for setting
the values of Ptolemy II attributes that implement the Settable
interface and have visibility FULL.  One or more entries are
associated with an attribute so that if the entry is changed, the
attribute value is updated, and if the attribute value changes,
the entry is updated. To change an attribute, this class queues
a change request with a particular object called the <i>change
handler</i>.  The change handler is specified as a constructor
argument.
<p>
It is important to note that it may take
some time before the value of a attribute is actually changed, since it
is up to the change handler to decide when change requests are processed.
The change handler will typically delegate change requests to the
Manager, although this is not necessarily the case.
<p>
To use this class, add an entry to the query using addStyledEntry().

@author Brian K. Vogel and Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
*/
public class PtolemyQuery extends Query
    implements QueryListener, ValueListener, ChangeListener, CloseListener {

    /** Construct a panel with no queries in it and with the specified
     *  change handler. When an entry changes, a change request is
     *  queued with the given change handler. The change handler should
     *  normally be a composite actor that deeply contains all attributes
     *  that are attached to query entries.  Otherwise, the change requests
     *  might get queued with a handler that has nothing to do with
     *  the attributes.  The handler is also used to report errors.
     *  @param handler The change handler.
     */
    public PtolemyQuery(NamedObj handler) {
        super();
        addQueryListener(this);
        _handler = handler;

        if (_handler != null) {
            // NOTE: Since we register as a listener to the handler,
            // there is no need to also register as a listner with
            // each change request.  EAL 9/15/02.
            _handler.addChangeListener(this);
        }
        _varToListOfEntries = new HashMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new entry to this query that represents the given attribute.
     *  The name of the entry will be set to the name of the attribute,
     *  and the attribute will be attached to the entry, so that if the
     *  attribute is updated, then the entry is updated. If the attribute
     *  contains an instance of ParameterEditorStyle, then defer to
     *  the style to create the entry, otherwise just create a default entry.
     *  The style used in a default entry depends on the class of the
     *  attribute and on its declared type, but defaults to a one-line
     *  entry if there is no obviously better style.
     *  Only the first style that is found is used to create an entry.
     *  @param attribute The attribute to create an entry for.
     */
    public void addStyledEntry(Settable attribute) {
        // Note: it would be nice to give
        // multiple styles to specify to create more than one
        // entry for a particular parameter.  However, the style configurer
        // doesn't support it and we don't have a good way of representing
        // it in this class.

        // Look for a ParameterEditorStyle.
        boolean foundStyle = false;
        if (attribute instanceof NamedObj) {
            Iterator styles = ((NamedObj)attribute)
                .attributeList(ParameterEditorStyle.class).iterator();
            while (styles.hasNext() && !foundStyle) {
                ParameterEditorStyle style =
                    (ParameterEditorStyle)styles.next();
                try {
                    style.addEntry(this);
                    foundStyle = true;
                } catch (IllegalActionException ex) {
                    // Ignore failures here, and just present
                    // the default dialog.
                }
            }
        }

        if (!foundStyle) {
            // NOTE: Infer the style.
            // This is a regrettable approach, but it keeps
            // dependence on UI issues out of actor definitions.
            // Also, the style code is duplicated here and in the
            // style attributes. However, it won't work to create
            // a style attribute here, because we don't necessarily
            // have write access to the workspace.
            String name = attribute.getName();
            try {
                if (attribute.getVisibility() == Settable.NOT_EDITABLE) {
                    String defaultValue = attribute.getExpression();
                    addDisplay(name, name, defaultValue);
                    attachParameter(attribute, name);
                    foundStyle = true;
                } else if (attribute instanceof IntRangeParameter) {
                    int current = ((IntRangeParameter)attribute)
                        .getCurrentValue();
                    int min = ((IntRangeParameter)attribute).getMinValue();
                    int max = ((IntRangeParameter)attribute).getMaxValue();

                    addSlider(name, name, current, min, max);
                    attachParameter(attribute, name);
                    foundStyle = true;
                } else if (attribute instanceof ColorAttribute) {
                    addColorChooser(name,
                            name,
                            attribute.getExpression());
                    attachParameter(attribute, name);
                    foundStyle = true;
                } else if (attribute instanceof FileParameter){
                    // Specify the directory in which to start browsing
                    // to be the location where the model is defined,
                    // if that is known.
                    URI modelURI = URIAttribute.getModelURI(
                            (NamedObj)attribute);
                    File directory = null;
                    if (modelURI != null) {
                        if (modelURI.getScheme().equals("file")) {
                            File modelFile = new File(modelURI);
                            directory = modelFile.getParentFile();
                        }
                    }
                    URI base = null;
                    if (directory != null) {
                        base = directory.toURI();
                    }
                    // FIXME: Should remember previous browse location?
                    // Last argument is the starting directory.
                    addFileChooser(name,
                            name,
                            attribute.getExpression(),
                            base,
                            directory,
                            preferredBackgroundColor(attribute));
                    attachParameter(attribute, name);
                    foundStyle = true;
                } else if (attribute instanceof Parameter
                        && ((Parameter)attribute).getChoices() != null) {
                    Parameter castAttribute = (Parameter)attribute;
                    Color background = preferredBackgroundColor(attribute);
                    // NOTE: Make this always editable since Parameter
                    // supports a form of expressions for value propagation.
                    addChoice(name,
                            name,
                            castAttribute.getChoices(),
                            castAttribute.getExpression(),
                            true,
                            background);
                    attachParameter(attribute, name);
                    foundStyle = true;
                } else if (attribute instanceof Variable) {
                    Type declaredType = ((Variable)attribute).getDeclaredType();
                    Token current = ((Variable)attribute).getToken();
                    if (declaredType == BaseType.BOOLEAN) {
                        // NOTE: If the expression is something other than
                        // "true" or "false", then this parameter is set
                        // to an expression that evaluates to to a boolean,
                        // and the default Line style should be used.
                        if (attribute.getExpression().equals("true")
                                || attribute.getExpression().equals("false")) {
                            addCheckBox(name,
                                    name,
                                    ((BooleanToken)current).booleanValue());
                            attachParameter(attribute, name);
                            foundStyle = true;
                        }
                    }
               }
                // FIXME: Other attribute classes? TextStyle?
            } catch (IllegalActionException ex) {
                // Ignore and create a line entry.
            }
        }

        String defaultValue = attribute.getExpression();
        if (defaultValue == null) defaultValue = "";
        if (!(foundStyle)) {
            Color background = preferredBackgroundColor(attribute);
            addLine(attribute.getName(),
                    attribute.getName(),
                    defaultValue,
                    background);
            // The style itself does this, so we don't need to do it again.
            attachParameter(attribute, attribute.getName());
        }
    }

    /** Attach an attribute to an entry with name <i>entryName</i>,
     *  of a Query. This will cause the attribute to be updated whenever
     *  the specified entry changes.  In addition, a listener is registered
     *  so that the entry will change whenever
     *  the attribute changes. If the entry has previously been attached
     *  to a attribute, then it is detached first from that attribute.
     *  If the attribute argument is null, this has the effect of detaching
     *  the entry from any attribute.
     *  @param attribute The attribute to attach to an entry.
     *  @param entryName The entry to attach the attribute to.
     */
    public void attachParameter(Settable attribute, String entryName) {
        // Put the attribute in a Map from entryName -> attribute
        _attributes.put(entryName, attribute);

        // Make a record of the attribute value prior to the change,
        // in case a change fails and the user chooses to revert.
        _revertValue.put(entryName, attribute.getExpression());

        // Attach the entry to the attribute by registering a listener.
        attribute.addValueListener(this);

        // Put the attribute in a Map from attribute -> (list of entry names
        // attached to attribute), but only if entryName is not already
        // contained by the list.
        if (_varToListOfEntries.get(attribute) == null) {
            // No mapping for attribute exists.
            List entryNameList = new LinkedList();
            entryNameList.add(entryName);
            _varToListOfEntries.put(attribute, entryNameList);
        } else {
            // attribute is mapped to a list of entry names, but need to
            // check whether entryName is in the list. If not, add it.
            List entryNameList = (List)_varToListOfEntries.get(attribute);
            Iterator entryNames = entryNameList.iterator();
            boolean found = false;
            while (entryNames.hasNext()) {
                // Check whether entryName is in the list. If not, add it.
                String name = (String)entryNames.next();
                if (name == entryName) {
                    found = true;
                }
            }
            if (found == false) {
                // Add entryName to the list.
                entryNameList.add(entryName);
            }
        }
        // Handle tool tips.  This is almost certainly an instance
        // of NamedObj, but check to be sure.
        if (attribute instanceof NamedObj) {
            Attribute tooltipAttribute =
                ((NamedObj)attribute).getAttribute("tooltip");
            if (tooltipAttribute != null
                    && tooltipAttribute instanceof Documentation) {
                setToolTip(entryName,
                        ((Documentation)tooltipAttribute).getValue());
            } else {
                String tip = Documentation.consolidate((NamedObj)attribute);
                if (tip != null) {
                    setToolTip(entryName, tip);
                }
            }
        }
    }

    /** Notify this class that a change has been successfully executed
     *  by the change handler.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this was not the originator.
        if (change != null) {
            if (change.getSource() != this) return;

            // Restore the parser error handler.
            if (_savedErrorHandler != null) {
                MoMLParser.setErrorHandler(_savedErrorHandler);
            }

            String name = change.getDescription();
            if (_attributes.containsKey(name)) {
                final Settable attribute = (Settable)(_attributes.get(name));

                // Make a record of the successful attribute value change
                // in case some future change fails and the user
                // chooses to revert.
                _revertValue.put(name, attribute.getExpression());
            }
        }
    }

    /** Notify the listener that a change attempted by the change handler
     *  has resulted in an exception.  This method brings up a new dialog
     *  to prompt the user for a corrected entry.  If the user hits the
     *  cancel button, then the attribute is reverted to its original
     *  value.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(final ChangeRequest change, Exception exception) {
        // Ignore if this was not the originator, or if the error has already
        // been reported, or if the change request is null.
        if (change == null || change.getSource() != this) {
            return;
        }
        
        // Restore the parser error handler.
        if (_savedErrorHandler != null) {
            MoMLParser.setErrorHandler(_savedErrorHandler);
        }

        // If this is already a dialog reporting an error, and is
        // still visible, then just update the message.  Otherwise,
        // create a new dialog to prompt the user for a corrected input.
        if (_isOpenErrorWindow) {
            setMessage(exception.getMessage()
                    + "\n\nPlease enter a new value (or cancel to revert):");
        } else {
            if (change.isErrorReported()) {
                // Error has already been reported.
                return;
            }
            change.setErrorReported(true);

            _query = new PtolemyQuery(_handler);
            _query.setTextWidth(getTextWidth());
            _query._isOpenErrorWindow = true;
            String description = change.getDescription();
            _query.setMessage(exception.getMessage()
                    + "\n\nPlease enter a new value:");
            /* NOTE: The error message used to be more verbose, as follows.
             * But this is intimidating to users.
             _query.setMessage("Change failed:\n"
             + description
             + "\n" + exception.getMessage()
             + "\n\nPlease enter a new value:");
            */

            // Need to extract the name of the entry from the request.
            // Default value is the description itself.
            // NOTE: This is very fragile... depends on the particular
            // form of the MoML change request.
            String tmpEntryName = description;
            if (description.startsWith("<property name=\"")) {
                int nextQuote = description.indexOf("\"", 16);
                if (nextQuote > 15) {
                    tmpEntryName = description.substring(16, nextQuote);
                }
            }
            final String entryName = tmpEntryName;
            final Settable attribute
                = (Settable)_attributes.get(entryName);
            // NOTE: Do this in the event thread, since this might be invoked
            // in whatever thread is processing mutations.
            SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        if (attribute != null) {
                            _query.addStyledEntry(attribute);
                        } else {
                            throw new InternalErrorException(
                                    "Expected attribute attached to entry name: "
                                    + entryName);
                        }
                        _dialog =
                            new ComponentDialog(
                                    JOptionPane.getFrameForComponent(
                                            PtolemyQuery.this),
                                    "Error", _query, null);

                        // The above returns only when the modal
                        // dialog is closing.  The following will
                        // force a new dialog to be created if the
                        // value is not valid.

                        _query._isOpenErrorWindow = false;

                        if (_dialog.buttonPressed().equals("Cancel")) {
                            if (_revertValue.containsKey(entryName)) {
                                String revertValue = (String)
                                    _revertValue.get(entryName);

                                // NOTE: Do not use setAndNotify() here because
                                // that checks whether the string entry has
                                // changed, and we want to force revert even
                                // if it appears to not have changed.
                                set(((NamedObj)attribute).getName(),
                                        revertValue);
                                changed(entryName);
                            }
                        } else {

                            // Force evaluation to check validity of
                            // the entry.  NOTE: Normally, we would
                            // not need to force evaluation because if
                            // the value has changed, then listeners
                            // are automatically notified.  However,
                            // if the value has not changed, then they
                            // are not notified.  Since the original
                            // value was invalid, it is not acceptable
                            // to skip notification in this case.  So
                            // we force it.

                            try {
                                attribute.validate();
                            } catch (IllegalActionException ex) {
                                change.setErrorReported(false);
                                changeFailed(change, ex);
                            }
                        }
                    }
                });
        }
    }

    /** Queue a change request to alter the value of the attribute
     *  attached to the specified entry, if there is one. This method is
     *  called whenever an entry has been changed.
     *  If no attribute is attached to the specified entry, then
     *  do nothing.
     *  @param name The name of the entry that has changed.
     */
    public void changed(final String name) {
        // Check if the entry that changed is in the mapping.
        if (_attributes.containsKey(name)) {
            final Settable attribute
                = (Settable)(_attributes.get(name));
            if ( attribute == null ) {
                // No associated attribute.
                return;
            }

            ChangeRequest request;

            // NOTE: We must use a MoMLChangeRequest so that changes
            // propagate to any objects that have been instantiating
            // using this one as a class.  This is only an issue if
            // attribute is a NamedObj, so we first check.
            if (attribute instanceof NamedObj) {
                NamedObj castAttribute = (NamedObj)attribute;

                // The context for the MoML should be the first container
                // above this attribute in the hierarchy that defers its
                // MoML definition, or the immediate parent if there is none.
                NamedObj parent = MoMLChangeRequest.getDeferredToParent(
                        castAttribute);
                if (parent == null) {
                    parent = (NamedObj)castAttribute.getContainer();
                }
                String moml = "<property name=\""
                    + castAttribute.getName(parent)
                    + "\" value=\""
                    + StringUtilities.escapeForXML(getStringValue(name))
                    + "\"/>";
                request = new MoMLChangeRequest(
                        this,         // originator
                        parent,       // context
                        moml,         // MoML code
                        null) {       // base
                        protected void _execute() throws Exception {
                            synchronized (PtolemyQuery.this) {
                                try {
                                    _ignoreChangeNotifications = true;
                                    super._execute();
                                } finally {
                                    _ignoreChangeNotifications = false;
                                }
                            }
                        }
                    };
            } else {
                // If the attribute is not a NamedObj, then we
                // set its value directly.
                request = new ChangeRequest(this, name) {
                        protected void _execute()
                                throws IllegalActionException {
                            attribute.setExpression(getStringValue(name));

                            attribute.validate();
                            /* NOTE: Earlier version:
                               // Here, we need to handle instances of Variable
                               // specially.  This is too bad...
                               if (attribute instanceof Variable) {

                               // Will this ever happen?  A
                               // Variable that is not a NamedObj???
                               // Retrieve the token to force
                               // evaluation, so as to check the
                               // validity of the new value.

                               ((Variable)attribute).getToken();
                               }
                            */
                        }
                    };
            }
            // NOTE: This object is never removed as a listener from
            // the change request.  This is OK because this query will
            // be closed at some point, and all references to it will
            // disappear, and thus both it and the change request should
            // become accessible to the garbage collector.  However, I
            // don't quite trust Java to do this right, since it's not
            // completely clear that it releases resources when windows
            // are closed.  It would be better if this listener were
            // a weak reference.
            // NOTE: This appears to be unnecessary, since we register
            // as a change listener on the handler.  This results in
            // two notifications.  EAL 9/15/02.
            if (_handler == null) {
                request.addChangeListener(this);
                request.execute();
            } else {
                if (request instanceof MoMLChangeRequest) {
                    ((MoMLChangeRequest)request).setUndoable(true);
                }

                // Remove the error handler so that this class handles
                // the error through the notification.  Save the previous
                // error handler to restore after this request has been
                // processes.
                _savedErrorHandler = MoMLParser.getErrorHandler();
                MoMLParser.setErrorHandler(null);
                _handler.requestChange(request);
            }
        }
    }
    
    /** Return the preferred background color for editing the specified
     *  object.  The default is Color.white, but if the object is an
     *  instance of Parameter and it is in string mode, then a light
     *  blue is returned.
     *  @param object The object to be edited.
     */
    public static Color preferredBackgroundColor(Object object) {
        Color background = Color.white;
        if (object instanceof Parameter) {
            if (((Parameter)object).isStringMode()) {
                background = _STRING_MODE_BACKGROUND_COLOR;
            }
        }
        return background;
    }

    /** Notify this query that the value of the specified attribute has
     *  changed.  This is called by an attached attribute when its
     *  value changes. This method updates the displayed value of
     *  all entries that are attached to the attribute.
     *  @param attribute The attribute whose value has changed.
     */
    public void valueChanged(final Settable attribute) {
        // If our own change request is the cause of this notification,
        // then ignore it.
        if (_ignoreChangeNotifications) return;

        // Do this in the event thread, since it depends on interacting
        // with the UI.  In particular, there is no assurance that
        // getStringValue() will return the correct value if it is called
        // from another thread.  And this method is called whenever an
        // attribute change has occurred, which can happen in any thread.
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {

                    // Check that the attribute is attached
                    // to at least one entry.
                    if (_attributes.containsValue(attribute)) {

                        // Get the list of entry names that the attribute
                        // is attached to.
                        List entryNameList = (List)
                            _varToListOfEntries.get(attribute);

                        // For each entry name, call set() to update its
                        // value with the value of attribute
                        Iterator entryNames = entryNameList.iterator();

                        while (entryNames.hasNext()) {
                            String name = (String)entryNames.next();
                            String newValue = attribute.getExpression();

                            // Compare value against what is in
                            // already to avoid changing it again.
                            if (!getStringValue(name).equals(newValue)) {
                                set(name, newValue);
                            }
                        }
                    }
                }
            });
    }

    /** Unsubscribe as a listener to all objects that we have subscribed to.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    public void windowClosed(Window window, String button) {
        // FIXME: It seems that we need to force notification of
        // all changes before doing the restore!  Otherwise, some
        // random time later, a line in the query might lose the focus,
        // causing it to override a restore.  However, this has the
        // unfortunate side effect of causing an erroneous entry to
        // trigger a dialog even if the cancel button is pressed!
        // No good workaround here.
        // notifyListeners();

        _handler.removeChangeListener(PtolemyQuery.this);

        // It's a bit bizarre that we have to remove ourselves as a listener
        // to ourselves, since the window is closing.  But if we don't do
        // this, then somehow we continue to be notified of changes to
        // the attributes.
        removeQueryListener(this);

        Iterator attributes = _attributes.values().iterator();
        while (attributes.hasNext()) {
            Settable attribute = (Settable)attributes.next();
            attribute.removeValueListener(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Another dialog used to prompt for corrections to errors.
    private ComponentDialog _dialog;

    // The handler that was specified in the constructors.
    private NamedObj _handler;

    // Indicator that we are executing a change request, so we can safely
    // ignore change notifications.
    private boolean _ignoreChangeNotifications = false;

    // Indicator that this is an open dialog reporting an erroneous entry.
    private boolean _isOpenErrorWindow = false;

    // Maps an entry name to the attribute that is attached to it.
    private Map _attributes = new HashMap();

    // A query box for dealing with an erroneous entry.
    private PtolemyQuery _query = null;

    // Maps an entry name to the most recent error-free value.
    private Map _revertValue = new HashMap();

    // Saved error handler to restore after change.
    private ErrorHandler _savedErrorHandler = null;
    
    // Background color for string mode edit boxes.
    private static Color _STRING_MODE_BACKGROUND_COLOR
            = new Color(230, 255, 255, 255);

    // Maps an attribute name to a list of entry names that the
    // attribute is attached to.
    private Map _varToListOfEntries;
}
