/* This actor implements an AFDX virtual-link parameter which
 * is used to configure a virtual-link.

@Copyright (c) 2010-2011 The Regents of the University of California.
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

package ptolemy.actor.lib.qm;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/** This actor is a {@link ptolemy.actor.lib.Transformer} designed for use with 
 * the {@link AFDXESs}. This actor comes with parameters that configure one 
 * virtual link which belong to one AFDX End-system.
 * To improve visibility this parameter is made visible using a BoxedValueIcon.
 * For more information see: <i>AFDX network simulation in PtolemyII</i>.
 *       
 *  @author G. Lasnier
 *  @version $Id$
   @since Ptolemy II 0.2
 */
public class AFDXVlCfg extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public AFDXVlCfg(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        vlink = new Parameter(this, "vlink");
        vlink.setTypeEquals(BaseType.STRING);       
        vlink.setExpression("\"VL\"");

        bag = new Parameter(this, "bag");
        bag.setDisplayName("bag (ms)");
        bag.setTypeEquals(BaseType.DOUBLE);       
        bag.setExpression("0.0");

        trameSize = new Parameter(this, "trameSize");
        trameSize.setDisplayName("trameSize (bytes)");
        trameSize.setTypeEquals(BaseType.INT);       
        trameSize.setExpression("0");

        schedulerMux = new Parameter(this, "schedulerMux");
        schedulerMux.setTypeEquals(BaseType.STRING);       
        schedulerMux.setExpression("\"Scheduler multiplexor name\"");

        // Icon description.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"0\" y=\"0\" "
                + "width=\"60\" height=\"20\" "
                + "style=\"fill:white\"/>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Value of the virtual link parameter. */
    public Parameter vlink;

    /** Value of the scheduler multiplexor parameter. */
    public Parameter schedulerMux;

    /** Value of the bag parameter. */
    public Parameter bag;

    /** Value of the trameSize parameter. */
    public Parameter trameSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Check constraints on parameter values.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {

        if (attribute == vlink) {
            String value = ((StringToken) vlink.getToken())
                    .stringValue();
            this.setDisplayName(value);

        } else if (attribute == bag) {
            double value = ((DoubleToken) bag.getToken())
                    .doubleValue();
            if (value < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative serviceTime: " + value);
            }
        } else if (attribute == trameSize) {
            int value = ((IntToken) trameSize.getToken()).intValue();
            if (value < 0) {
                throw new IllegalActionException(this,
                        "Cannot have negative or zero size of trames: " + value);
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
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        AFDXVlCfg newObject = (AFDXVlCfg) super.clone(workspace);
        return newObject;
    }

    /** Take the input (if there is such one) and put it in the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {         
            output.send(0, input.get(0));
        }
    }
}
