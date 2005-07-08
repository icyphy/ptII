/* An actor that produces no outputs.

 Copyright (c) 1997-2005 The Regents of the University of California.
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

 */
package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.Source;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Undefined

/**
 This actor outputs no values.  It produces no tokens.  In domains such as
 SR, the output will never converge to a defined value.

 @author Paul Whitaker
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (pwhitake)
 @Pt.AcceptedRating Red (pwhitake)
 */
public class Undefined extends Source {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Undefined(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        new Attribute(this, "_nonStrictMarker");
        outputType = new StringAttribute(this, "outputType");
        outputType.setExpression("int");
        attributeChanged(outputType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to change the type of the output port.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the type is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == outputType) {
            String typeName = outputType.getExpression().trim().toLowerCase();
            Type newType = BaseType.forName(typeName);

            if (newType == null) {
                throw new IllegalActionException(this, "Unrecognized type: "
                        + typeName);
            } else {
                output.setTypeEquals(newType);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type for the output port.  This is a string-valued attribute
     *  that defaults to "int".
     */
    public StringAttribute outputType;
}
