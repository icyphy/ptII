/* An attribute that contains documentation for the container.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)

*/

package ptolemy.moml;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Documentation
/**
An attribute that contains documentation for the container.
<p>
The name of a documentation object can often be meaningful.  Many times
the name can be used to specify important information about the type of
documentation.  Unfortunately, all documentation objects are currently
treated the same.

@author  Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
*/
public class Documentation extends StringAttribute {

    /** Construct an attribute with the specified container and name.
     *  The documentation contained by the attribute is initially empty,
     *  but can be set using the setValue() method.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Documentation(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////


    /** Return as a single string all the documentation associated with
     *  the specified object.  Each attribute of type of class Documentation
     *  that the object contains contributes to the documentation.
     *  The text contributed by each such attribute starts on a new line.
     *  If there are no such attributes, then null is returned.
     *  @param object The object to document.
     *  @return The documentation for the object.
     */
    public static String consolidate(NamedObj object) {
        List docList = object.attributeList(Documentation.class);
        if (docList.size() > 0) {
            StringBuffer doc = new StringBuffer();
            Iterator segments = docList.iterator();
            while (segments.hasNext()) {
                Documentation segment = (Documentation)segments.next();
                doc.append(segment.getValue());
                if (segments.hasNext()) doc.append("\n");
            }
            return doc.toString();
        } else {
            return null;
        }
    }

    /** Write a MoML description of this object with the specified
     *  indentation depth.  This class is directly supported by the MoML
     *  "doc" element, so we generate MoML of the form
     *  "&lt;doc&gt;<i>documentation</i>&lt;/doc&gt;", where
     *  <i>documentation</i> is replaced by the string value of this
     *  attribute. If this object is not persistent, then write nothing.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     *  @exception IOException If an I/O error occurs.
     *  @see NamedObj#_exportMoMLContents
     *  @see #isPersistent()
     */
    public void exportMoML(Writer output, int depth, String name)
            throws IOException {
        if (!isPersistent() || isClassElement()) {
            return;
        }
        if (name.equals("_doc")) {
            // Name is the default name.  Omit.
            output.write(_getIndentPrefix(depth)
                    + "<doc>"
                    + getExpression()
                    + "</doc>\n");
        } else {
            // Name is not the default name.
            output.write(_getIndentPrefix(depth)
                    + "<doc name=\""
                    + name
                    + "\">"
                    + getExpression()
                    + "</doc>\n");
        }
    }

    /** Get the documentation as a string.
     *  @return The documentation.
     */
    public String getValue() {
        return getExpression();
    }

    /** Set the documentation string.
     *  @param value The documentation.
     */
    public void setValue(String value) {
        try {
            super.setExpression(value);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }

    /** Get the documentation as a string, with the class name prepended.
     *  @return A string describing the object.
     */
    public String toString() {
        return "(" + getClass().getName() + ", " + getExpression() + ")";
    }
}
