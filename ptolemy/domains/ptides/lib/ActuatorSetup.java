/* ActuatorSetup simulates a hardware device that sends actuation data to the environment.
@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.domains.ptides.lib;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////ActuatorSetup

/** A model of actuator hardware on a target platform.
 *  This base class copies inputs unchanged to the outputs,
 *  but subclasses will model specific properties of physical
 *  hardware on a target platform.  This base class also
 *  imposes no type constraints on the inputs, but subclasses
 *  will constrain the types to those that the physical hardware
 *  can handle.
 *
 *  @author Jia Zou, Slobodan Matic
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating
 */
@Deprecated
public class ActuatorSetup extends OutputDevice {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ActuatorSetup(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this port is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public  variables                 ////
    /** Read one token from the input. Send out an identical token.
     *  @exception IllegalActionException If there is no director, or the
     *  input can not be read, or the output can not be sent.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Token token;
        // consume input
        if (input.hasToken(0)) {
            token = input.get(0);
        } else {
            token = null;
        }

        // produce output
        if (token != null) {
            output.send(0, token);
        }
    }

    /** Perform a check to see if this device is connected to a network
     *  port on the outside. If so, throw an exception. Also call
     *  preinitialize of the super class.
     *  @exception IllegalActionException If there are no outside sink
     *  ports, or if any of the outside sink ports is a network
     *  port.
     */
    @Override
    public void preinitialize() throws IllegalActionException {

        super.preinitialize();

        // Perform port consistency check if the schedulerExecutionTime
        // parameter of the director is 0.0.
        Parameter parameter = (Parameter) getDirector().getAttribute(
                "schedulerExecutionTime");
        if (parameter != null
                && ((DoubleToken) parameter.getToken()).doubleValue() != 0.0) {
            boolean flag = false;
            for (TypedIOPort output : outputPortList()) {
                for (IOPort sinkPort : output.sinkPortList()) {
                    if (sinkPort.getContainer() == getContainer()) {
                        flag = true;
                        break;
                    }
                }
            }
            if (!flag) {
                throw new IllegalActionException(this,
                        "A ActuatorSetup must be connected to a port "
                                + "on the outside.");
            }
        }
    }
}
