/* An attribute that has an arbitrary MoML description, externally given.

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

package ptolemy.moml;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// MoMLAttribute
/**
This attribute has an arbitrary MoML description that is exported
when the exportMoML() methods of the container are called.  Thus,
it serves as a convenient way to attach persistent information
that will not otherwise be exported to MoML.  To specify its
MoML description, call setMoMLDescription().

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class MoMLAttribute extends Attribute {

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public MoMLAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public MoMLAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Append to the MoML description of this object.
     *  @param moml The MoML description of this object.
     */
    public void appendMoMLDescription(String moml) {
        StringTokenizer tokenizer = new StringTokenizer(moml, "\n");
        while (tokenizer.hasMoreTokens()) {
            _momlDescription.add(tokenizer.nextToken());
        }
    }

    /** Write the MoML description of this object, which consists of
     *  whatever has been specified using the appendMoMLDescription() method.
     *  If that method has not been called, then nothing is written.
     *  The written MoML is indented to the specified depth and terminated
     *  with a newline.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     */
    public void writeMoMLDescription(Writer output, int depth)
            throws IOException {
        if (_momlDescription.size() > 0) {
            Iterator strings = _momlDescription.iterator();
            while (strings.hasNext()) {
                String string = (String)strings.next();
                output.write(_getIndentPrefix(depth) + string + "\n");
            }
        }
    }

    /** Write a MoML description of this object, which in this case is
     *  whatever has been specified by the setMoMLDescription() method.
     *  If that method has not been called, then nothing is written.
     *  The written MoML is indented to the specified depth and terminated
     *  with a newline. If this object is not persistent, then nothing
     *  is written.
     *  @param name The name to use instead of the name of this object.
     *   This argument is ignored.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (!isPersistent() || isClassElement()) {
            return;
        }
        writeMoMLDescription(output, depth);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The MoML description as a list of strings.
    private List _momlDescription = new LinkedList();
}
