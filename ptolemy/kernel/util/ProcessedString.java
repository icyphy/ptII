/* A hierarchical library of components specified in MoML.

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

import ptolemy.actor.Configurable;
import java.io.Writer;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// ProcessedString
/**
This class provides a simple way to get a long string into an MoML file. 
Instead of using a Settable attribute (like StringAttribute) to set the
value of the attribute, it uses a configure tag.  Since configure tags 
are often given with processing instructions, this class takes care of 
removing the processing instruction and providing it separately (via the 
<i>getInstruction</i> method.

@author Steve Neuendorffer
@version $Id$
*/

public class ProcessedString
        extends Attribute implements Configurable {

    /** Construct a new attribute with no
     *  container and an empty string as its name. Add the attribute to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public ProcessedString() {
        super();
    }

    /** Construct a a new attribute with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. 
     *  Add the attribute to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public ProcessedString(Workspace workspace) {
	super(workspace);
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ProcessedString(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException  {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Configure the object with data from the specified input source
     *  (a URL) and/or textual data, assumed to be in PlotML format.
     *  If this is called before the plotter has been created
     *  (by calling place() or initialize()), then the configuration
     *  is deferred until the plotter is created.
     *  @param base The base relative to which references within the input
     *   are found, or null if this is not known, or there is none.
     *  @param source The input source, which specifies a URL.
     *  @param text Configuration information given as text.
     *  @exception Exception If the configuration source cannot be read
     *   or if the configuration information is incorrect.
     */
    public void configure(URL base, String source, String text)
            throws Exception {
	// Not sure what to do if a source was given.. ust store it, I guess..
	_source = source;
	// Strip off the processing instruction.
	String trimmed = text.trim();
	if (trimmed.startsWith("<?") && trimmed.endsWith("?>")) {
	    trimmed = trimmed.substring(2, trimmed.length() - 2).trim();
	    int index = 0;
	    // find the first whitespace
	    while(index < trimmed.length() && 
		  !Character.isWhitespace(trimmed.charAt(index))) {
		index ++;
	    }
	    _instruction = trimmed.substring(0, index);
	    _string = trimmed.substring(index, trimmed.length()).trim();
	} else {
	    _instruction = null;
	    _string = trimmed;
	}
    }

    /** Return the processing instruction for the string contained in this
     *  attribute.  If the instruction has not been set, and this
     *  object has not been configured, or if a processing instruction was
     *  not specified in the configure tag (such as if a <pre><![CDATA[</pre>
     *  was used), then return null.
     */
    public String getInstruction() {
	return _instruction;
    }

    /** Return the processed string that this attribute contains,
     *  not including the processing instruction.  If the attribute was 
     *  configured with a <pre><![CDATA[</pre> tag, then this string does not
     *  include the tag.  If no instruction was specified, then this will
     *  contain the entire contents of the configure tag.  If the string
     *  has not been set, or this object has not been configured, then return
     *  null.
     */
    public String getString() {
	return _string;
    }

    /** Set the instruction that should be used to process the string contained
     *  in this attribute.
     */
    public void setInstruction(String instruction) {
	_instruction = instruction;
    }

    /** Set the string contained by this attribute.
     */
    public void setString(String string) {
	_string = string;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write a MoML description of the contents of this object, which
     *  in this class is the configuration information. This method is called
     *  by exportMoML().  Each description is indented according to the
     *  specified depth and terminated with a newline character.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @exception IOException If an I/O error occurs.
     */
    protected void _exportMoMLContents(Writer output, int depth)
            throws IOException {
        super._exportMoMLContents(output, depth);
	if(_instruction == null) {
	    output.write(_getIndentPrefix(depth) + "<configure>\n" 
			 + "<![CDATA[\n"
			 + _string + "]]>\n"
			 + _getIndentPrefix(depth) + "</configure>\n");
	} else {
	    output.write(_getIndentPrefix(depth) + "<configure><?" 
			 + _instruction + "\n"
			 + _string + "?>\n"
			 + _getIndentPrefix(depth) + "</configure>\n");
	}
    }
	
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    private String _instruction = null;
    private String _string = null;
    private String _source = null;
}
