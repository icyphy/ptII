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
import ptolemy.kernel.event.*;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JTextArea;

// FIXME: This still doesn't work right.
// If you enter an incorrect value, then hit OK in the ensuing
// dialog, the incorrect value is accepted.

//////////////////////////////////////////////////////////////////////////
//// PtolemyQuery
/**
This class is a query dialog box with various entries for setting
the values of Ptolemy II parameters.  One or more entries are
associated with a parameter so that if the entry is changed, the
parameter value is updated, and if the parameter value changes,
the entry is updated. To change a parameter, this class queues
a change request with a particular composite actor called the <i>change
handler</i>.  The change handler is specified as a constructor
argument.
<p>
It is important to note that it may take
some time before the value of a parameter is actually changed, since it 
is up to the change handler to decide when change requests are processed.
The change handler will typically delegate change requests to the 
Manager, although this is not necessarily the case.
<p>
To use this class, first add an entry to the query using addStyledEntry(),
and then use the attachParameter() method to associate a parameter with
that entry.

@author Brian K. Vogel and Edward A. Lee
@version $Id$
*/
public class PtolemyQuery extends Query
    implements QueryListener, ValueListener, ChangeListener {

    // FIXME: Have to unlisten to ValueListener.
    // No need to unlisten to QueryListener, since that is this.

    /** Construct a panel with no queries in it and with the specified
     *  change handler. When an entry changes, a change request is
     *  queued with the given change handler. The change handler should
     *  normally be a composite actor that deeply contains all parameters
     *  that are attached to query entries.  Otherwise, the change requests
     *  might get queued with a handler that has nothing to do with
     *  the parameters.  The handler is also used to report errors.
     *  @param handler The change handler.
     */
    public PtolemyQuery(CompositeEntity handler) {
	super();
	addQueryListener(this);
	_handler = handler;
        
        if (_handler != null) {
            _handler.addChangeListener(this);
        }
        _varToListOfEntries = new HashMap();

// FIXME: What's below doesn't work.
// I'm going to have to derive from ComponentDialog a special
// Dialog for PtolemyQuery, and then use that...
// Actually, that won't work without rearchitecting the whole
// Configurer architecture...
// Java's UI infrastructure really totally sucks...
/*

        // Well, this totally sucks, but
        // regrettably, Java provides no easy way to attach an event
        // to the closing of the window within which this component is
        // displayed.  For some reason, attaching a ComponentListener
        // and implementing componentHidden() does not work.  Apparently,
        // componentHidden() is not called when this window is closed.
        // This seems like a bug in Java, but we have to live with it.
        // So instead, we have to identify the top-level container,
        // and attach a WindowListener to that.  Unfortunately, we can't
        // identify the toplevel window until the component is made
        // visible, because at construction time it has not yet been
        // added to a parent.  Moreover, implementing addNotify() will
        // not work because it could be added to a parent before the parent
        // has been added to a window.  So we have to do an incredible song
        // and dance just so we can remove listeners when the component
        // is closed.  The lesson: Java's AWT is the full employment act
        // of the third millenium.  Nothing is easy...
        //
        // So, we start by adding a listener for when the component is
        // made visible.  That listener will then find the top-level window
        // and attach a listener to that to detect window closing events.
        // That listener will then remove listeners that this query
        // has scattered about.
        //
        // Wow... I really miss C++ destructors...
        //
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent event) {
                Container parent = getParent();
                // FIXME
                System.out.println("here with original parent: " + parent);
                while (parent != null && !(parent instanceof Window)) {
                    parent = parent.getParent();
                    // FIXME
                    System.out.println("here with new parent: " + parent);
                }
                // If the parent is now null, then somehow this component is
                // not inside an instance of Window.  This should not occur,
                // but if it does, then we just suffer the inefficiency of never
                // removing the listeners.
                // FIXME
                System.out.println("here with parent: " + parent);
                if (parent != null) {
                    Window toplevel = (Window)parent;
                    // FIXME
                    System.out.println("Adding window listener to: ");
                    toplevel.list();
                    toplevel.addWindowListener(new WindowAdapter() {
                        public void windowClosed(WindowEvent event) {
                            // FIXME: This never gets invoked.
                            System.out.println("Window closed");
                            _handler.removeChangeListener(PtolemyQuery.this);
                        }
                        public void windowClosing(WindowEvent event) {
                            // FIXME: This never gets invoked.
                            System.out.println("Window closing");
                            _handler.removeChangeListener(PtolemyQuery.this);
                        }
                    });
                }
            }
        });
*/
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new entry to this query that represents the given parameter.
     *  The name of the entry will be set to the name of the parameter.
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
	    attachParameter(param, param.getName());
	}
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

        // FIXME: How do we remove the listener when this closes?
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
	    // if entryName is in the list. If not, add it.
	    List entryNameList = (List)_varToListOfEntries.get(parameter);
	    Iterator entryNames = entryNameList.iterator();
	    boolean found = false;
	    while (entryNames.hasNext()) {
		// Check if entryName is in the list. If not, add it.
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
                // FIXME be sure to remove the listener when the query closes.
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
        if (_message != null && _message.isVisible()) {
            _message.setText(exception.getMessage()
            + "\n\nPlease enter a new value (click Cancel to revert):");
        } else {
            // Avoid creating a second modal dialog if there is one already.
            if (_query == null || !_query.isVisible()) {
                _query = new PtolemyQuery(_handler);
                Box secondTry = new Box(BoxLayout.Y_AXIS);
                _query._message = new JTextArea(exception.getMessage()
                        + "\n\nPlease enter a new value:");
                JTextArea message = _query._message;
                message.setFont(new Font("SansSerif", Font.PLAIN, 12));
                message.setEditable(false);
                message.setLineWrap(true);
                message.setWrapStyleWord(true);
                message.setBackground(secondTry.getBackground());
                // Left Justify.
                message.setAlignmentX(0.0f);
                secondTry.add(message);
                secondTry.add(secondTry.createVerticalStrut(10));
                // Left Justify.
                _query.setAlignmentX(0.0f);
                _query.setTextWidth(getTextWidth());
                secondTry.add(_query);

                // The name of the entry is the description of the change.
                String entryName = change.getDescription();
                Variable variable = (Variable)_parameters.get(entryName);
                if (variable != null) {
                    _query.addStyledEntry(variable);
                    _query.attachParameter(variable, entryName);
                } else {
                    throw new InternalErrorException(
                        "Expected parameter attached to entry name: "
                        + entryName);
                }
                ComponentDialog dialog
                       = new ComponentDialog(null, "Error", secondTry);
                if (dialog.buttonPressed().equals("Cancel")) {
                    if (_revertValue.containsKey(entryName)) {
                        String revertValue
                                = (String)_revertValue.get(entryName);
                        // FIXME: In the two calls to changedFailed below,
                        // the change object has the wrong originator...
                        // The originator is the original dialog, not the
                        // new one, so the call to changeFailed
                        // is ignored.  Or something...

                        // NOTE: This is happening during a mutation, so we
                        // go ahead and set the value.
                        variable.setExpression(revertValue);
                        // Retrieve the token to force evaluation,
                        // so that listeners are notified.
                        try {
                            variable.getToken();
                        } catch (IllegalActionException ex) {
                            changeFailed(change, ex);
                        }
                    }
                } else if (dialog.buttonPressed().equals("OK")) {
                    try {
                        variable.getToken();
                    } catch (IllegalActionException ex) {
                        changeFailed(change, ex);
                    }
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

        // FIXME: Do we remove the listener when the query closes?

        // Check that the parameter is attached to at least one entry.
        if (_parameters.containsValue(parameter)) {
                        
            // Get the list of entry names that the parameter is attached to.
            List entryNameList = (List)_varToListOfEntries.get(parameter);

            // For each entry name, call set() to update its
            // value with the value of parameter.
            Iterator entryNames = entryNameList.iterator();
            
            while (entryNames.hasNext()) {
                String name = (String)entryNames.next();
                set(name, parameter.stringRepresentation());
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The handler that was specified in the constructors.
    private CompositeEntity _handler;

    // Message box in error handling dialog.
    private JTextArea _message;

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

