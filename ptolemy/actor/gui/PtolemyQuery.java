/* A subclass of Query supporting Ptolemy II parameters.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)

*/

package ptolemy.actor.gui;

import ptolemy.data.expr.ValueListener;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.gui.*;
import ptolemy.actor.gui.style.*;
import ptolemy.data.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.Container;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// PtolemyQuery
/**
This class is a query dialog box with various entries for setting
the values of Ptolemy II parameters.  One or more entries are
associated with a parameter so that if the entry is changed, the
parameter value is updated, and if the parameter value changes,
the entry is updated. To change a parameter, this class queues
a change request with a particular object called the <i>change
handler</i>.  The change handler is specified as a constructor
argument.
<p>
It is important to note that it may take
some time before the value of a parameter is actually changed, since it 
is up to the change handler to decide when change requests are processed.
The change handler will typically delegate change requests to the 
Manager, although this is not necessarily the case.
<p>
To use this class, add an entry to the query using addStyledEntry().

@author Brian K. Vogel and Edward A. Lee
@version $Id$
*/
public class PtolemyQuery extends Query
    implements QueryListener, ValueListener, ChangeListener, CloseListener {

    /** Construct a panel with no queries in it and with the specified
     *  change handler. When an entry changes, a change request is
     *  queued with the given change handler. The change handler should
     *  normally be a composite actor that deeply contains all parameters
     *  that are attached to query entries.  Otherwise, the change requests
     *  might get queued with a handler that has nothing to do with
     *  the parameters.  The handler is also used to report errors.
     *  @param handler The change handler.
     */
    public PtolemyQuery(NamedObj handler) {
	super();
	addQueryListener(this);
	_handler = handler;
        
        if (_handler != null) {
            _handler.addChangeListener(this);
        }
        _varToListOfEntries = new HashMap();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new entry to this query that represents the given parameter.
     *  The name of the entry will be set to the name of the parameter,
     *  and the parameter will be attached to the entry, so that if the
     *  parameter is updated, then the entry is updated.
     *  If the parameter contains a parameter style, then use the style to 
     *  create the entry, otherwise just create a new line entry.
     *  Attach the variable to the new entry.
     *  @param param The parameter for which to create an entry.
     */
    public void addStyledEntry(Variable param) {
	// Look for a ParameterEditorStyle.
	Iterator styles
	    = param.attributeList(ParameterEditorStyle.class).iterator();
	boolean foundStyle = false;
	while (styles.hasNext() && !foundStyle) {
	    ParameterEditorStyle style
		= (ParameterEditorStyle)styles.next();
	    try {
		style.addEntry(this);
		foundStyle = true;
	    } catch (IllegalActionException ex) {
		// Ignore failures here, and just present the default dialog.
	    }
	}
	if (!(foundStyle)) {
	    addLine(param.getName(),
		    param.getName(),
		    param.stringRepresentation());
	}
        attachParameter(param, param.getName());
    }

    /** Attach a parameter to an entry with name <i>entryName</i>,
     *  of a Query. This will cause the parameter to be updated whenever
     *  the specified entry changes, and the entry to change whenever
     *  the parameter changes. If the entry has previously been attached
     *  to a parameter, then it is detached first from that parameter.
     *  If the parameter argument is null, this has the effect of detaching
     *  the entry from any parameter.
     *  @param parameter The parameter to attach to an entry.
     *  @param entryName The entry to attach the parameter to.
     */
    public void attachParameter(Variable parameter, String entryName) {
	// Put the parameter in a Map from entryName -> parameter
	_parameters.put(entryName, parameter);

        // Make a record of the parameter value prior to the change,
        // in case a change fails and the user chooses to revert.
        _revertValue.put(entryName, parameter.stringRepresentation());
        parameter.addValueListener(this);
        Attribute tooltipAttribute = parameter.getAttribute("tooltip");
        if (tooltipAttribute != null
                && tooltipAttribute instanceof Documentation) {
            setToolTip(entryName, ((Documentation)tooltipAttribute).getValue());
        } else {
            String tip = Documentation.consolidate(parameter);
            if (tip != null) {
                setToolTip(entryName, tip);
            }
        }
	// Put the parameter in a Map from parameter -> (list of entry names
	// attached to parameter), but only if entryName is not already
	// contained by the list.
	if (_varToListOfEntries.get(parameter) == null) {
	    // No mapping for parameter exists.
	    List entryNameList = new LinkedList();
	    entryNameList.add(entryName);
	    _varToListOfEntries.put(parameter, entryNameList);
	} else {
	    // parameter is mapped to a list of entry names, but need to check
	    // whether entryName is in the list. If not, add it.
	    List entryNameList = (List)_varToListOfEntries.get(parameter);
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
    }

    /** Queue a change request to alter the value of the parameters
     *  attached to the specified entry, if there is one. This method is
     *  called whenever an entry has been changed.
     *  If no parameter is attached to the specified entry, then
     *  do nothing.
     *  @param name The name of the entry that has changed.
     */
    public void changed(final String name) {

	// Check if the entry that changed is in the mapping.
	if (_parameters.containsKey(name)) {
	    final Variable parameter = (Variable)(_parameters.get(name));
            if ( parameter == null ) {
                // No associated parameter.
                return;
            }

            // NOTE: We could use a MoMLChangeRequest, but that
            // would create a dependence on the moml package, so
            // for this simple mutation, it's probably not worth it.
            ChangeRequest request = new ChangeRequest(this, name) {
                protected void _execute() throws IllegalActionException {
                    parameter.setExpression(stringValue(name));
                    // Retrieve the token to force evaluation, so as to
                    // check the validity of the new value.
                    parameter.getToken();
                }
            };
            if(_handler != null) {
                _handler.requestChange(request);
            } else {
                request.execute();
            }
	}
    }

    /** Notify this class that a change has been successfully executed
     *  by the change handler.
     *  @param change The change that has been executed.
     */
    public void changeExecuted(ChangeRequest change) {
        // Ignore if this was not the originator.
        if (change.getOriginator() != this) return;

        String name = change.getDescription();
	if (_parameters.containsKey(name)) {
	    final Variable parameter = (Variable)(_parameters.get(name));

            // Make a record of the successful parameter value change
            // in case some future change fails and the user chooses to revert.
            _revertValue.put(name, parameter.stringRepresentation());
        }
    }

    /** Notify the listener that a change attempted by the change handler
     *  has resulted in an exception.  This method brings up a new dialog
     *  to prompt the user for a corrected entry.  If the user hits the
     *  cancel button, then the parameter is reverted to its original
     *  value.
     *  @param change The change that was attempted.
     *  @param exception The exception that resulted.
     */
    public void changeFailed(ChangeRequest change, Exception exception) {
        // Ignore if this was not the originator.
        if (change.getOriginator() != this) {
            return;
        }
        // If this is already a dialog reporting an error, and is
        // still visible, then just update the message.  Otherwise,
        // create a new dialog to prompt the user for a corrected input.
        if (_isOpenErrorWindow) {
            setMessage(exception.getMessage()
                    + "\n\nPlease enter a new value (or cancel to revert):");
        } else {
            _query = new PtolemyQuery(_handler);
            _query.setTextWidth(getTextWidth());
            _query._isOpenErrorWindow = true;
            _query.setMessage(exception.getMessage()
                    + "\n\nPlease enter a new value:");

            // The name of the entry is the description of the change.
            String entryName = change.getDescription();
            Variable variable = (Variable)_parameters.get(entryName);
            if (variable != null) {
                _query.addStyledEntry(variable);
            } else {
                throw new InternalErrorException(
                       "Expected parameter attached to entry name: "
                        + entryName);
            }
            _dialog = new ComponentDialog(null, "Error", _query, null);

            // The above returns only when the modal dialog is closing.
            // The following will force a new dialog to
            // be created if the value is not valid.
            _query._isOpenErrorWindow = false;

            if (_dialog.buttonPressed().equals("Cancel")) {
                if (_revertValue.containsKey(entryName)) {
                    String revertValue = (String)_revertValue.get(entryName);
                    setAndNotify(variable.getName(), revertValue);
                }
            } else {
                // Force evaluation to check validity of the entry.
                // NOTE: Normally, we would not need to force evaluation
                // because if the value has changed, then listeners are
                // automatically notified.  However, if the value has not
                // changed, then they are not notified.  Since the original
                // value was invalid, it is not acceptable to skip
                // notification in this case.  So we force it.
                try {
                    variable.getToken();
                } catch (IllegalActionException ex) {
                    changeFailed(change, ex);
                }
            }
        }
    }

    /** Notify this query that the value of the specified parameter has
     *  changed.  This is called by an attached parameter when its
     *  value changes. This method updates the displayed value of
     *  all entries that are attached to the parameter.
     *  @param parameter The parameter whose value has changed.
     */
    public void valueChanged(Variable parameter) {

        // Check that the parameter is attached to at least one entry.
        if (_parameters.containsValue(parameter)) {
                        
            // Get the list of entry names that the parameter is attached to.
            List entryNameList = (List)_varToListOfEntries.get(parameter);

            // For each entry name, call set() to update its
            // value with the value of parameter.
            Iterator entryNames = entryNameList.iterator();
            
            while (entryNames.hasNext()) {
                String name = (String)entryNames.next();
                String newValue = parameter.stringRepresentation();

                // Compare value against what is in already to avoid
                // changing it again.
                if (!stringValue(name).equals(newValue)) {
                    set(name, parameter.stringRepresentation());
                }
            }
        }
    }

    /** Notify all listeners of any updated values, and then
     *  unsubscribe as a listener to all objects that we have subscribed to.
     *  @param window The window that closed.
     *  @param button The name of the button that was used to close the window.
     */
    public void windowClosed(Window window, String button) {
        // NOTE: It seems that we need to force notification of
        // all changes before doing the restore!  Otherwise, some
        // random time later, a line in the query might lose the focus,
        // causing it to override a restore.  However, this has the
        // unfortunate side effect of causing an erroneous entry to
        // trigger a dialog even if the cancel button is pressed!
        // No good workaround here.
        // notifyListeners();

        _handler.removeChangeListener(PtolemyQuery.this);

        Iterator parameters = _parameters.values().iterator();
        while(parameters.hasNext()) {
            Variable parameter = (Variable)parameters.next();
            parameter.removeValueListener(this);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Another dialog used to prompt for corrections to errors.
    private ComponentDialog _dialog;

    // The handler that was specified in the constructors.
    private NamedObj _handler;

    // Indicator that this is an open dialog reporting an erroneous entry.
    private boolean _isOpenErrorWindow = false;

    // Maps an entry name to the variable that is attached to it.
    private Map _parameters = new HashMap();

    // A query box for dealing with an erroneous entry.
    private PtolemyQuery _query = null;

    // Maps an entry name to the most recent error-free value.
    private Map _revertValue = new HashMap();

    // Maps a variable name to a list of entry names that the
    // variable is attached to.
    private Map _varToListOfEntries;
}

