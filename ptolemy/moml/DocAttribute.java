/* An attribute that contains documentation for the container.

 Copyright (c) 1998 The Regents of the University of California.
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

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// DocAttribute
/** 
An attribute that contains documentation for the container.

@author  Edward A. Lee
@version $Id$
*/
public class DocAttribute extends Attribute {

    /** Construct an attribute with the name <i>_doc</i> and the specified
     *  contents for the the specified container.  If the container already
     *  contains an attribute by that name, replace it.  If the container
     *  rejects the attribute with an IllegalActionException, then
     *  the attribute is not forced upon the container, but rather is
     *  created with no container.
     *  @param container The container.
     *  @param value The documentation to attach to the container.
     */	
    public DocAttribute(NamedObj container, String value) {
        super(container.workspace());
        try {
            setName(DOC_ATTRIBUTE_NAME);
            _value = value;
            Attribute previous = container.getAttribute(DOC_ATTRIBUTE_NAME);
            if (previous != null) {
                previous.setContainer(null);
            }
            setContainer(container);
        } catch (NameDuplicationException ex) {
            throw new InternalErrorException(ex.getMessage());
        } catch (IllegalActionException ex) {
            // The container rejects the attribute; create with no container.
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The name of a doc attribute. */
    public static final String DOC_ATTRIBUTE_NAME = "_doc";

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the documentation as a string.
     *  @return The documentation.
     */	
    public String toString() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The string value of the documentation.
    private String _value;
}
