/* An actor that models a channel with random delay and drop.

 Copyright (c) 1998-2001 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.fsm.demo.ABP;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.DiscreteRandomSource;
import ptolemy.actor.lib.Switch;
import ptolemy.domains.de.lib.VariableDelay;
import ptolemy.actor.lib.Uniform;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;

public class LossyDelayChannel extends TypedCompositeActor {

    public LossyDelayChannel(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);

        // Parameters.
        minimumDelay = new Parameter(this, "minimumDelay");
        minimumDelay.setExpression("0.0");

        maximumDelay = new Parameter(this, "maximumDelay");
        maximumDelay.setExpression("1.0");

        lossProbability = new Parameter(this, "lossProbability");
        lossProbability.setExpression("0.1");

        // Ports.
        input = new TypedIOPort(this, "input", true, false);
        control = new TypedIOPort(this, "control", true, false);
        output = new TypedIOPort(this, "output", false, true);

        Uniform uniform = new Uniform(this, "uniform");
        uniform.lowerBound.setExpression("minimumDelay");
        uniform.upperBound.setExpression("maximumDelay");

        VariableDelay variableDelay = new VariableDelay(this, "variableDelay");
        variableDelay.defaultDelay.setExpression("1.0");

        Switch loser = new Switch(this, "loser");

        DiscreteRandomSource loss = new DiscreteRandomSource(this, "loss");
        loss.pmf.setExpression("[(1.0 - lossProbability) , lossProbability]");
        loss.values.setExpression("{0, 1}");

        // Connections.
        TypedIORelation relation3 = new TypedIORelation(this, "relation3");
        input.link(relation3);
        uniform.trigger.link(relation3);
        variableDelay.input.link(relation3);
        connect (uniform.output, variableDelay.delay);
        connect (output, loser.output);
        TypedIORelation relation5 = new TypedIORelation(this, "relation5");
        variableDelay.output.link(relation5);
        loss.trigger.link(relation5);
        loser.input.link(relation5);
        connect (loser.control, loss.output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The minimum delay.  This parameter has type double, and defaults
     *  to 0.0.
     */
    public Parameter minimumDelay;

    /** The maximum delay.  This parameter has type double, and defaults
     *  to 1.0.
     */
    public Parameter maximumDelay;

    /** The loss probability.  This parameter has type double, and defaults
     *  to 0.1.
     */
    public Parameter lossProbability;

    /** The data input port, which can have any type.
     */
    public TypedIOPort input;

    /** The data output port, which has the same type as the input.
     */
    public TypedIOPort output;

    /** The control port, which has type int.
     */
    public TypedIOPort control;
}
