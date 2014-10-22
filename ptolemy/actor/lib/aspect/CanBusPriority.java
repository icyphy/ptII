/* This actor implements a CAN bus priority filter.

@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor.lib.aspect;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This actor is a {@link ptolemy.actor.lib.Transformer} designed for use with the {@link CanBus}.
 * This actor comes with a parameter <i>CanId</i> that sets the priority of the output port that it 'covers'.
 * To improve visibility this parameter is made visible using a BoxedValueIcon.
 *
 * For more information see: <i>CAN bus simulator using Quantity Manager</i>.
 *
 *  @author D. Marciano, P. Derler
 *  @version $Id$
   @since Ptolemy II 10.0

   @Pt.ProposedRating Red (derler)
   @Pt.AcceptedRating Red (derler)
 */
public class CanBusPriority extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CanBusPriority(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        value = new Parameter(this, "value");
        value.setExpression("1");
        value.setTypeEquals(BaseType.INT);

        // icon description
        _attachText("_iconDescription", "<svg>\n" + "<rect x=\"0\" y=\"0\" "
                + "width=\"60\" height=\"20\" " + "style=\"fill:white\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /**
     *  Value of the CanId parameter.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the parameter changed is <i>CanId</i>, then ensure that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == value) {
            int n = ((IntToken) value.getToken()).intValue();
            if (n <= 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero CanId: " + n);
            }
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the type constraints.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CanBusPriority newObject = (CanBusPriority) super.clone(workspace);
        return newObject;
    }

    /**
     * Take the input (if there is such one) and put it in the output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            output.send(0, input.get(0));
        }
    }
}
