/* An attribute whose value can be set via the MoML configure tag.

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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (janneck@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// ConfigurableAttribute
/**
   This class provides a simple way to get a long string into an attribute.
   It implements Configurable, so its value can be set using a configure MoML
   element.  For example,
   <pre>
   &lt;property name="x" class="ptolemy.moml.ConfigurableAttribute"&gt;
   &lt;configure source="url"&gt;xxx&lt;/configure&gt;
   &lt;/property&gt;
   </pre>
   The value of this property, obtained via the value() method,
   will be whatever text is contained by the referenced URL (which
   is optional), followed by the text "xxx".

   @author Steve Neuendorffer and Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
*/

public class ConfigurableAttribute
    extends Attribute implements Configurable, Settable {

    /** Construct a new attribute with no
     *  container and an empty string as its name. Add the attribute to the
     *  default workspace directory.
     *  Increment the version number of the workspace.
     */
    public ConfigurableAttribute() {
        super();
    }

    /** Construct a new attribute with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  Add the attribute to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public ConfigurableAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Construct a new attribute with the given container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name.
     */
    public ConfigurableAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException  {
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

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The input source, if any, is assumed
     *  to contain textual data as well.  Note that the URL is not read
     *  until the value() method is called.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *   This argument is ignored in this method.
     *  @param source The input source, which specifies a URL.
     *  @param text Configuration information given as text.
     *  @exception Exception Not thrown in this base class.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        _base = base;
        _configureSource = source;
        _configureText = text;
        // FIXME: Do we really want to call this right away?
        validate();
        // Make sure the new value is exported in MoML.  EAL 12/03.
        setModifiedHeritage(true);
    }

    /** Return the base specified in the most recent call to the
     *  configure() method, or null if none.
     *  @return The base with respect to which the relative references
     *   in the source file should be interpreted.
     */
    public URL getBase() {
        return _base;
    }

    /** Return the source specified in the most recent call to the
     *  configure() method, or null if none.
     *  @return A URL specifying an external source for configure
     *   information.
     */
    public String getConfigureSource() {
        return _configureSource;
    }

    /** Return the text specified in the most recent call to the
     *  configure() method, or null if none.
     *  @return Text giving configure information.
     */
    public String getConfigureText() {
        return _configureText;
    }

    /** Return the the result of calling value().
     *  @return The value, or a description of the exception if one is thrown.
     *  @see #value()
     */
    public String getExpression() {
        try {
            return value();
        } catch (Exception ex) {
            return ex.toString();
        }
    }

    /** Get the visibility of this attribute, as set by setVisibility().
     *  The visibility is set by default to NONE.
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
     *  of the value of this attribute by calling attributeChanged(),
     *  and notify any listeners that have
     *  been registered using addValueListener().  This is the same
     *  as calling configure with a null base and source, passing
     *  the argument as text.
     *  @param expression The text to configure the attribute with.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void setExpression(String expression) throws IllegalActionException {
        try {
            configure(null, null, expression);
        } catch (IllegalActionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InternalErrorException("Unexpected exception: " + ex);
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
     *  Notify listeners that the value of this attribute has changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to the container.
     */
    public void validate() throws IllegalActionException {
        // Validate contained attributes, if any.
        Iterator attributes = attributeList(Settable.class).iterator();
        while (attributes.hasNext()) {
            Settable attribute = (Settable)attributes.next();
            attribute.validate();
        }
        // Notify the container that the attribute has changed.
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

    /** Return the value given by the configure tag.  This is the text
     *  read from the specified URL (if any), followed by the text
     *  specified in the body of the configure element.  Note that the
     *  URL given in the configure() method, if any, is read each time
     *  this method is called.
     *  @return The value set in the configure tag.
     *  @exception IOException If the URL given in the configure method
     *   (if any) cannot be read.
     */
    public String value() throws IOException {
        StringBuffer value = new StringBuffer();
        // If a source is given, read its data.
        if (_configureSource != null && !_configureSource.trim().equals("")) {
            URL textFile = new URL(_configureSource);
            InputStream stream = textFile.openStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream));
            String line = reader.readLine();
            while (line != null) {
                value.append(line);
                value.append("\n");
                line = reader.readLine();
            }
            reader.close();
            // NOTE: Do we need to close both?  Java docs don't say.
            stream.close();
        }
        if (_configureText != null) {
            value.append(_configureText);
        }
        return value.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object.
     *  This method is called by exportMoML().  Each description
     *  is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
        String sourceSpec = "";
        if (_configureSource != null && !_configureSource.trim().equals("")) {
            sourceSpec = " source=\"" + _configureSource + "\"";
            if (_configureText == null) {
                output.write(_getIndentPrefix(depth)
                        + "<configure" + sourceSpec + "/>\n");
            }
        }
        if (_configureText != null) {
            output.write(_getIndentPrefix(depth)
                    + "<configure" + sourceSpec + ">"
                    + _configureText
                    + "</configure>\n");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The base specified in the configure() method.
    private URL _base;

    // The URL from which configure data is read.
    private String _configureSource;

    // The text in the body of the configure.
    private String _configureText;

    // Listeners for changes in value.
    private List _valueListeners;

    // The visibility of this attribute, which defaults to NONE;
    private Settable.Visibility _visibility = Settable.NONE;
}
