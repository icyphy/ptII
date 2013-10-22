/* An actor that produces no outputs.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
import java.util.Locale;

import ptolemy.actor.lib.Source;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// Undefined

/**
 An actor that produces no outputs or tokens.

 <p>In domains such as SR, the output of this actor will never
 converge to a defined value. This actor is different from the
 {@link ptolemy.domains.sr.lib.Absent} actor, which produces an <i>absent</i>
 value.

 @author Paul Whitaker, Haiyang Zheng
 @deprecated This actor does not work. It returns false in prefire(), but that is interpreted by the director to mean that all outputs are absent.
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (hyzheng)
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
        outputType = new StringAttribute(this, "outputType");
        outputType.setExpression("int");
        attributeChanged(outputType);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The type for the output port.  This is a string-valued attribute
     *  that defaults to "int".
     */
    public StringAttribute outputType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to change the type of the output port.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the type is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == outputType) {
            String typeName = outputType.getExpression().trim().toLowerCase(Locale.getDefault());
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

    /** Do nothing.
     *  @exception IllegalActionException not thrown in this class.
     */
    public void fire() throws IllegalActionException {
        // Do nothing.
        // We could have just used the fire() method of the super class,
        // because it is never called anyway. However, we explicitly
        // override the method with an empty body to illstrate the difference.
    }

    /** Return false. This actor never fires. Note that in the fire()
     *  method of the FixedPointDirector, if an actor returns false in
     *  its prefire() method, the fire() and postfire() methods are never
     *  invoked. Consequently, the output of this actor is undefined.
     *
     *  @return False.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        return false;
    }
}
