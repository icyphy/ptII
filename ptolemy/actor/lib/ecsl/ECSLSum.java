/* A polymorphic adder/subtractor for use with ECSL

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.ecsl;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// Sum

/**
   A polymorphic adder/subtractor for use with ECSL.

   @author Christopher Brooks.
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public class ECSLSum extends AddSubtract {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ECSLSum(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);

        input.setTypeEquals(BaseType.DOUBLE);
        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);

        // FIXME: hide minus and plus ports
        plus.setTypeEquals(BaseType.DOUBLE);
        minus.setTypeEquals(BaseType.DOUBLE);

        Inputs = new StringParameter(this, "Inputs");

        // FIXME: Expect two connections, which is connected to the
        // plus port, the other to the minus port
        Inputs.setExpression("|+-");

        IconShape = new StringParameter(this, "IconShape");
        IconShape.setExpression("UNKNOWN");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port. */
    public TypedIOPort input;

    /** The string that describes which ports are connected to
     *  plus and which are connected to minus.  "|+-" means the
     *  the first connection to the input port is to be connected
     *  to the plus port of the parent and the second connection
     *  to the input port is to be connected to the minus port.
     */
    public StringParameter Inputs;

    /** The shape of the Icon.  Currently ignored.  The default value
     *  is the string "UNKNOWN";
     */
    public StringParameter IconShape;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the value of an attribute.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == Inputs) {
            if (!Inputs.getExpression().equals("|+-")) {
                throw new IllegalActionException("Sorry, Inputs parameter must"
                        + " be set to \"|+-\", instead it was set to \""
                        + Inputs.getExpression() + "\".");
            }
        } else if (attribute == IconShape) {
            // Ignored
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** FIXME
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    public void fire() throws IllegalActionException {
        if (output.getWidth() > 1) {
            throw new IllegalActionException("Output widths greater than "
                    + "1 not yet supported");
        }

        super.fire();
    }
}
