/* An attribute that represents an external file reference.

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

import java.io.IOException;
import java.io.Writer;

//////////////////////////////////////////////////////////////////////////
//// ImportAttribute
/**
This attribute represents an external file reference.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
@deprecated The 'import' MoML element is deprecated, use the 'source'
attribute instead.
*/
public class ImportAttribute extends Attribute {

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ImportAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the name of the external file being referenced.
     *  @param source The name of the external file.
     */
    public void setSource(String source) {
        _source = source;
    }

    /** Write a MoML description of this object, which in this case is
     *  an "import" element. If this object is not persistent, then
     *  write nothing.
     *  @param name The name to use instead of the name of this object.
     *   This argument is ignored.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (_suppressMoML(depth)) {
            return;
        }
        String moml = "<import source=\""
            + _source
            + "\"/>";
        output.write(_getIndentPrefix(depth) + moml + "\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The external file name;
    private String _source;
}
