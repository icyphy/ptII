/* An attribute that represents a location of a node in a schematic.

 Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.kernel.util;
import ptolemy.util.StringUtilities;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// Location
/**
This attribute represents a location of a node in a schematic.
By default, an instance of this class is not visible in a user interface.
This is indicated to the user interface by returning NONE to the
getVisibility() method.  The location is specified by calling
setExpression() with a string that has the form "x,y" or "[x,y]"
or "{x,y}", where x and y can be parsed into doubles.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
@since Ptolemy II 2.1
*/
public class Location extends SingletonAttribute
    implements Locatable {

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Location(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the given name and position.
     *  @param container The container.
     *  @param name The name of the vertex.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Location(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a listener to be notified when the value of this attribute changes.
     *  If the listener is already on the list of listeners, then do nothing.
     *  @param listener The listener to add.
     */
    public void addValueListener(ValueListener listener) {
        if (_valueListeners == null) {
            _valueListeners = new LinkedList();
        }
        if (!_valueListeners.contains(listener)) {
            _valueListeners.add(listener);
        }
    }

    /** Clone the location into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the base class throws it.
     *  @return A new Location.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Location newObject = (Location)super.clone(workspace);
        // Copy the location so that the reference in the new object
        // does not refer to the same array.
        if (_location == null) {
            newObject._location = null;
        } else {
            int length = _location.length;
            newObject._location = new double[length];
            System.arraycopy(_location, 0, newObject._location, 0, length);
        }
        newObject._valueListeners = null;
        return newObject;
    }

    /** Write a MoML description of this object.
     *  MoML is an XML modeling markup language.
     *  In this class, the object is identified by the "property"
     *  element, with "name", "class", and "value" (XML) attributes.
     *  The body of the element, between the "&lt;property&gt;"
     *  and "&lt;/property&gt;", is written using
     *  the _exportMoMLContents() protected method, so that derived classes
     *  can override that method alone to alter only how the contents
     *  of this object are described.
     *  The text that is written is indented according to the specified
     *  depth, with each line (including the last one)
     *  terminated with a newline. If this object is non-persistent,
     *  then nothing is written.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_suppressMoML(depth)) {
            return;
        }
        String value = getExpression();
        String valueTerm = "";
        if (value != null && !value.equals("")) {
            valueTerm = " value=\"" +
                StringUtilities.escapeForXML(value) + "\"";
        }

        output.write(_getIndentPrefix(depth)
                + "<"
                + getMoMLInfo().elementName
                + " name=\""
                + name
                + "\" class=\""
                + getMoMLInfo().className
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + getMoMLInfo().elementName + ">\n");
    }

    /** Get the value of the attribute that has been set by setExpression()
     *  or by setLocation(), whichever was most recently called,
     *  or return an empty string if neither has been called.
     *  @return The expression.
     */
    public String getExpression() {
        if (_expressionSet) {
            return _expression;
        }
        if (_location == null || _location.length == 0) {
            return "";
        }
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < _location.length - 1; i++) {
            result.append(_location[i]);
            result.append(", ");
        }
        result.append(_location[_location.length - 1]);
        return result.toString();
    }

    /** Get the location in some cartesian coordinate system.
     *  @return The location.
     */
    public double[] getLocation() {
        return _location;
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     *  The visibility is set by default to NONE.
     *  @return The visibility of this attribute.
     */
    public Settable.Visibility getVisibility() {
        return _visibility;
    }

    /** Remove a listener from the list of listeners that is
     *  notified when the value of this variable changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Set the value of the attribute by giving some expression.
     *  This expression is not parsed until validate() is called, and
     *  the container and value listeners are not notified until validate()
     *  is called.
     *  @param expression The value of the attribute.
     */
    public void setExpression(String expression) {
        _expression = expression;
        _expressionSet = true;
        // Make sure the new value is exported in MoML.  EAL 12/03.
        setModifiedFromClass(true);
    }

    /** Set the location in some cartesian coordinate system, and notify
     *  the container and any value listeners of the new location. Setting
     *  the location involves maintaining a local copy of the passed 
     *  parameter. No notification is done if the location is the same 
     *  as before.
     *  @param location The location.
     */
    public void setLocation(double[] location)
            throws IllegalActionException {
        boolean changed = _setLocation(location);
        if (changed) {
            // Make sure the new value is exported in MoML.  EAL 12/03.
            // FIXME NOW: We never get here when dragging objects... Why?
            setModifiedFromClass(true);
        }
    }

    /** Set the visibility of this attribute.  The argument should be one
     *  of the public static instances in Settable.
     *  @param visibility The visibility of this attribute.
     */
    public void setVisibility(Settable.Visibility visibility) {
        _visibility = visibility;
    }

    /** Get a description of the class, which is the class name and
     *  the location in parentheses.
     *  @return A string describing the object.
     */
    public String toString() {
        String className = getClass().getName();
        if (_location == null) {
            return "(" + className + ", Location = null)";
        }
        return "(" + className + ", Location = (" + getExpression() + "))";
    }

    /** Parse the location specification given by setExpression(), if there
     *  has been one, and otherwise set the location to 0.0, 0.0.
     *  Notify the container and any value listeners of the new location,
     *  if it has changed.
     *  @exception IllegalActionException If the expression is invalid.
     */
    public void validate() throws IllegalActionException {
        // If the value has not been set via setExpression(), there is
        // nothing to do.
        if (!_expressionSet) {
            return;
        }
        double[] location;
        if (_expression == null) {
            location = new double[2];
            location[0] = 0.0;
            location[1] = 0.0;
        } else {
            // Parse the specification: a comma specified list of doubles,
            // optionally surrounded by square or curley brackets.
            StringTokenizer tokenizer = new StringTokenizer(_expression, ",[]{}");
            location = new double[tokenizer.countTokens()];
            int count = tokenizer.countTokens();
            for (int i = 0; i < count; i++) {
                String next = tokenizer.nextToken().trim();
                location[i] = Double.parseDouble(next);
            }
        }
        // Set and notify.
        _setLocation(location);
        // The above call sets _expressionSet to false, which is incorrect.
        // We know from above that it is true.
        _expressionSet = true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                  ////

    /** Set the location without altering the modified status.
     *  @param location The location.
     *  @return True if the location was modified.
     */
    private boolean _setLocation(double[] location)
            throws IllegalActionException {
        _expressionSet = false;
        // If the location is unchanged, return false.
        if (_location != null
                && location != null
                && _location.length == location.length) {
            boolean match = true;
            for (int i = 0; i < location.length; i++) {
                if (_location[i] != location[i]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return false;
            }
        }
        
        // FIXME NOW: We never get here when dragging!  The location is getting set
        // some other way! (or not getting set????)

        // Copy location array into member array _location.
        // Just referencing _location to location isn't enough, we need
        // to maintain a local copy of the double array.
        for(int i = 0; i < location.length; i++) {
            _location[i] = location[i];
        }

        NamedObj container = (NamedObj)getContainer();
        if (container != null) {
            container.attributeChanged(this);
        }
        if (_valueListeners != null) {
            Iterator listeners = _valueListeners.iterator();
            while (listeners.hasNext()) {
                ValueListener listener = (ValueListener)listeners.next();
                listener.valueChanged(this);
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The expression given in setExpression().
    private String _expression;

    // Indicator that the expression is the most recent spec for the location.
    private boolean _expressionSet = false;

    // The location.
    private double[] _location = {0.0, 0.0};

    // Listeners for changes in value.
    private List _valueListeners;

    // The visibility of this attribute, which defaults to NONE.
    private Settable.Visibility _visibility = Settable.NONE;
}
