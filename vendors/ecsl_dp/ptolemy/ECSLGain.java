/* An actor that outputs a scaled version of the input for use with ECSL.

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

package vendors.ecsl_dp.ptolemy;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Scale;
import ptolemy.data.type.BaseType;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// Gain
/**
   An actor that outputs a scaled version of the input for use with ECSL.

   @author Christopher Brooks.
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/

public class ECSLGain extends Scale {

    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ECSLGain(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.DOUBLE);

        output.setMultiport(true);
        output.setTypeEquals(BaseType.DOUBLE);

        // Hide the factor port.
        factor.setVisibility(Settable.EXPERT);
        factor.setTypeEquals(BaseType.DOUBLE);

        Gain = new Parameter(this, "Gain");
        Gain.setTypeEquals(BaseType.DOUBLE);
        Gain.setExpression("1.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////


    /** The Gain.
     */
    public Parameter Gain;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the value of an attribute.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == Gain) {
            try {
                factor.setToken(Gain.getToken());
            } catch (Exception ex) {
                System.out.println("ECSLGain: factor.setToken() failed:" + ex);
            }
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
