/* An attribute that represents a MoML parser.

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

import java.util.List;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;

//////////////////////////////////////////////////////////////////////////
//// ParserAttribute
/**
This attribute represents a MoML parser.
If it is present in an entity that is the context for a MoMLChangeRequest,
then that change request will use it to parse the changes.
It is not a persistent attribute (exportMoML() writes nothing).
It is a singleton, meaning that if it is inserted into a container
that already contains a singleton attribute with the same name,
then it will replace the previous attribute.
<p>
By default, this attribute is not persistent, so it will not
be present in a MoML representation of its container.

@see MoMLChangeRequest
@author  Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/
public class ParserAttribute extends SingletonAttribute {

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ParserAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the parser.  If none has been set, then return a new one.
     *  @return A MoML parser.
     */
    public MoMLParser getParser() {
        if (_parser == null) {
            _parser = new MoMLParser();
        }
        return _parser;
    }
    
    /** Get a parser for the specified object. This searches up the
     *  hierarchy until it finds a container of the specified object
     *  that contains an instance of ParserAttribute. If none is
     *  found, then a new ParserAttribute is created at the top level.
     *  @param object The object for which to find an associated parser.
     *  @exception NullPointerException If the argument is null.
     */
    public static MoMLParser getParser(NamedObj object) {
        NamedObj container = object;
        while (container != null) {
            List attributes = object.attributeList(ParserAttribute.class);
            if (attributes != null && attributes.size() > 0) {
                // Found one.
                ParserAttribute attribute = (ParserAttribute)attributes.get(0);
                return attribute.getParser();
            }
            container = (NamedObj)container.getContainer();
        }
        // No parser attribute was found.
        NamedObj toplevel = object.toplevel();
        try {
            ParserAttribute attribute = new ParserAttribute(toplevel, "_parser");
            return attribute.getParser();
        } catch (KernelException ex) {
            // This should not occur.
            throw new InternalErrorException(ex);
        }
    }

    /** Set the parser.
     *  @param parser The parser.
     */
    public void setParser(MoMLParser parser) {
        _parser = parser;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The parser.
    private MoMLParser _parser;
}
