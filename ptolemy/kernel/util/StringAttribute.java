/* An attribute that has a string value.

 Copyright (c) 2000-2003 The Regents of the University of California.
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
@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (cxh@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;
import ptolemy.util.StringUtilities;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// StringAttribute
/**
An attribute that has a string value.
Use setExpression() to define the value, as in for example
<pre>
    attribute.setExpression("xxx");
</pre>
<p>The default value of the string contained by this attribute is the empty
string.

<p>By default, an instance of this class is fully visible in
a user interface.  The visibility is indicated to the user
interface when the user interface calls the getVisibility() method
of this class and the value Settable.FULL is returned to the userInterface.

<p>Note that the string value within StringAttribute cannot reference
other StringAttributes or Parameters, so if an actor has a public
StringAttribute, then one cannot make the value of that attribute
dependent on a value higher in the hierarchy.  Usually, actors have
public ptolemy.data.expr.Parameters instead of public StringAttributes
so that the value can reference other parameters.  The primary reason
to use StringAttribute is if you want a string that will not be parsed
and you do not want to type a leading a trailing double quote.

@see Settable#FULL
@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class StringAttribute extends Attribute implements Settable {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public StringAttribute() {
        super();
        _value = "";
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public StringAttribute(Workspace workspace) {
        super(workspace);
        _value = "";
    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public StringAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _value = "";
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

    /** Write a MoML description of this object, unless it is non-persistent.
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
     *  terminated with a newline.
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
                + _elementName
                + " name=\""
                + name
                + "\" class=\""
                + getClassName()
                + "\""
                + valueTerm
                + ">\n");
        _exportMoMLContents(output, depth + 1);
        output.write(_getIndentPrefix(depth) + "</"
                + _elementName + ">\n");
    }

    /** Get the value that has been set by setExpression(),
     *  or null if there is none.
     *  @return The string value.
     */
    public String getExpression() {
        return _value;
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     *  The visibility is set by default to FULL.
     *  @return The visibility of this attribute.
     */
    public Settable.Visibility getVisibility() {
        return _visibility;
    }

    /** Remove a listener from the list of listeners that is
     *  notified when the value of this attribute changes.  If no such listener
     *  exists, do nothing.
     *  @param listener The listener to remove.
     */
    public void removeValueListener(ValueListener listener) {
        if (_valueListeners != null) {
            _valueListeners.remove(listener);
        }
    }

    /** Set the value of the string attribute and notify the container
     *  of the value of this attribute by calling attributeChanged().
     *  Notify any value listeners of this attribute.
     *  @param expression The value of the string attribute.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void setExpression(String expression)
            throws IllegalActionException {
        if (expression != null && !expression.equals(_value)) {
            // Make sure the new value is exported in MoML.  EAL 12/03.
            setModifiedHeritage(true);
        }

        _value = expression;

        // Notify the container and any value listeners immediately,
        // rather than deferring to validate().
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
    }

    /** Set the visibility of this attribute.  The argument should be one
     *  of the public static instances in Settable.
     *  @param visibility The visibility of this attribute.
     */
    public void setVisibility(Settable.Visibility visibility) {
        _visibility = visibility;
    }

    /** Validate any instances of Settable that this attribute may contain.
     *  There is no need to notify the container or listeners of this
     *  attribute because they have presumably already been notified
     *  in setExpression().
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void validate() throws IllegalActionException {
        Iterator attributes = attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable attribute = (Settable)attributes.next();
            attribute.validate();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The value.
    private String _value;

    // Listeners for changes in value.
    private List _valueListeners;

    // The visibility of this attribute, which defaults to FULL.
    private Settable.Visibility _visibility = Settable.FULL;
}
