/* An attribute whose value can be set via the MoML configure tag.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

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
The value of this property, obtained via the getValue() method,
will be whatever text is contained by the referenced URL, followed
by the text "xxx".

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/

public class ConfigurableAttribute
        extends Attribute implements Configurable {

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
     *  @param workspace The workspace that will list the actor.
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

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data.  The input source, if any, is assumed
     *  to be textual data as well.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *   This argument is ignored in this method.
     *  @param source The input source, which specifies a URL.
     *  @param text Configuration information given as text.
     *  @exception Exception If the configuration source cannot be read
     *   or if the configuration information is incorrect.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
        _source = source;
        _text = text;
    }

    /** Return the value given by the configure tag.  This is the text
     *  read from the specified URL (if any), followed by the text
     *  specified in the body of the configure element.
     *  @return The value set in the configure tag.
     *  @exception IOException If the URL given in the configure method
     *   (if any) cannot be read.
     */
    public String value() throws IOException {
        StringBuffer value = new StringBuffer();
	// If a source is given, read its data.
        if (_source != null && !_source.equals("")) {
            URL textFile = new URL(_source);
            InputStream stream = textFile.openStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream));
            String line = reader.readLine();
            while (line != null) {
                value.append(line);
                value.append("\n");
                line = reader.readLine();
            }
            stream.close();
        }
        if (_text != null) {
            value.append(_text);
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
	if(_source != null && !_source.equals("")) {
            sourceSpec = " source=\"" + _source + "\"";
        }
        if (_text != null) {
            output.write(_getIndentPrefix(depth)
                    + "<configure" + sourceSpec + ">"
                    + _text
                    + "</configure>\n");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The URL from which configure data is read.
    private String _source;

    // The text in the body of the configure.
    private String _text;
}
