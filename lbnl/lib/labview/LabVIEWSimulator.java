/* Actor that calls a simulation program that interacts with a LabVIEW program.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package lbnl.lib.labview;

import java.io.IOException;

import lbnl.actor.lib.Simulator;
import ptolemy.actor.util.Time;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Actor that works with a LabVIEW simulation program. To use this
 * actor, the LabVIEW program must follow a fixed semantics, which is
 * specified later in this documentation.
 * <p>
 * This actor communicates the LabVIEW simulation program, as well
 * as synchronizes simulated physical time between these two platforms.
 * This actor assumes the Ptolemy program dictates what time to advance
 * to, while the LabVIEW program proposes times to advance to.
 * </p><p>
 * This actor can be invoked in two cases, one, when an input is received
 * from the Ptolemy II simulation; two, when this actor triggers itself to
 * fire at some future time through the use of pure events. In either case,
 * outputs may or may not be produced by this actor. However, since the
 * timeline this actor deals with is the simulated physical time,
 * the outputs produced should not be a response of consumed input at
 * the same time. The LabVIEW program should ensure this behavior.
 * </p><p>
 * At initialization, this actor reads input from the LabVIEW program,
 * which proposes a simulated physical time (hereby referred to as "time",
 * unless otherwise stated) to advance to. This actor then produces a
 * pure event (by calling fireAt() of the director, with this time as
 * its timestamp. Notice this time may or may not be different from
 * the current time of the Ptolemy simulation environment.
 * </p><p>
 * The director will invoke this actor either when a trigger event
 * arrives at this actor's input port, or when the pure event produced
 * earlier triggers this event. In the first case, the input is consumed,
 * and this data is transmitted into the LabVIEW program. The LabVIEW
 * program should then react to this input and propose the next time to
 * advance to, and send it back to the Ptolemy actor. Also, if a previous
 * input has decided to produce an output at the current time, then an output
 * will be produced by the LabVIEW program at the current time, and that
 * output will be produced by this actor.
 * </p><p>
 * The key assumption we make about the LabVIEW program is that it always
 * has information about what is the next time it wants to advance to.
 * Thus at any point in time when the LabVIEW program is invoked, it
 * proposes a new time to advance to, and send that time to the Ptolemy
 * program.
 *
 * @author Jia Zou
 * @version $Id$
 * @since Ptolemy II 10.0
 *
 */
public class LabVIEWSimulator extends Simulator {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LabVIEWSimulator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Send the input token to the client program and send the
     *  output from the client program to the output port. However
     *  if the received proposed time to advance to from LabVIEW
     *  is less than 0, it is interpreted as an indication to
     *  terminate the program. In which case this actor does not
     *  send an event to the output port. Nor do we call fireAt().
     *  Note the other way to stop execution is to set the stop time
     *  of the DE actor to a finite value.
     *
     *  @exception IllegalActionException If the simulation time
     *  between Ptolemy and the client program is not synchronized.
     */
    @Override
    public void fire() throws IllegalActionException {

        // tokTim is the current time of in the Ptolemy world. This time
        // is to be sent to the LabVIEW program to ensure time advances
        // to the same value.
        tokTim = getDirector().getModelTime().getDoubleValue();
        //           if (!firstFire && server.getClientFlag() == 0) {
        if (server.getClientFlag() == 0) {
            // If clientflag is non-zero, do not read anymore

            // this method write the tokTim to the LabVIEW program.
            // Also, if data is present at the input of this actor,
            // that data is sent to the LabVIEW program. If no data
            // is present, then no data is sent to the LabVIEW
            // program.
            _writeToServer();

            // Reading data from the LabVIEW program, which gets
            // the next proposed time to advance to, as well as
            // (possibly) data from the LabVIEW program.
            // After reading, we call fireAt() to ensure this actor
            // fires again at the proposed future time. Also, if data is read
            // from the LabVIEW program, that data is sent to the
            // output port of this actor.
            _readFromServer();
            double[] dblRea = server.getDoubleArray();
            double nextSimulationTime = server
                    .getSimulationTimeReadFromClient();
            // If nextSimulationTime is negative, this implies the program
            // should be stopped. Thus we simply return from fire().
            if (nextSimulationTime < 0) {
                return;
            }
            getDirector().fireAt(this,
                    new Time(getDirector(), nextSimulationTime));
            if (dblRea.length == 1) {
                output.send(0, new DoubleToken(dblRea[0]));
            } else if (dblRea.length != 0) {
                throw new IllegalActionException(
                        this,
                        "Received data from "
                                + "LabVIEW, the only supported data lenght right now is 1.");
            }
        } else { // Either client is down or this is the first time step. Consume token
            input.get(0);
            firstFire = false;
        }
        //////////////////////////////////////////////////////
        // send output token
        //        output.send(0, outTok);
    }

    /** Return true and do not check the inputs.
     *  @return Always return true, indicating that this actor is ready for firing.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // This actor extends Simulator, which extends SDFTransformer.
        // SDFTransformer.prefire() checks the input ports for the
        // required number of tokens.  This actor always calls fire(),
        // so prefire() always returns true.
        if (_debugging) {
            _debug("Called prefire()");
        }

        return true;
    }

    /** Get a double array from the Token.
     *
     * @param t the token which must be a type that can be converted to an ArrayToken
     * @return the double[] array with the elements of the Token
     * @exception IllegalActionException If the base class throws it.
     */
    @Override
    protected double[] _getDoubleArray(ptolemy.data.Token t)
            throws IllegalActionException {

        double[] result;
        if (t == null) {
            result = new double[0];
        } else {
            result = new double[1];
            if (t instanceof DoubleToken) {
                result[0] = ((DoubleToken) t).doubleValue();
            } else {
                throw new IllegalActionException(this, "Data received at the "
                        + "input of this actor must be of type double");
            }
        }
        return result;
    }

    /** Start the simulation program. Currently we do this manually, but
     *  there should be a way to run a labview model through command line.
     *
     *  @exception IllegalActionException If the simulation process arguments
     *                           are invalid.
     */
    @Override
    protected void _startSimulation() throws IllegalActionException {
    }

    /** During initialize, we output one token to startup the co-simulation
     *  between Ptolemy and LabVIEW program.
     */
    @Override
    protected void _outputInitToken() throws IllegalActionException {
        Token token = new DoubleToken(0.0);
        output.send(0, token);
    }

    /** Write the data to the server instance, which will send it to
     * the client program.
     *
     * @exception IllegalActionException If there was an error when
     * writing to the server.
     */
    @Override
    protected void _writeToServer() throws IllegalActionException {
        //////////////////////////////////////////////////////
        // Write data to server
        Token token = null;
        if (input.hasToken(0)) {
            token = input.get(0);
        }
        dblWri = _getDoubleArray(token);

        try {
            //                          Thread.sleep(1000); // in milliseconds
            server.write(0, tokTim, dblWri);
        } catch (IOException e) {
            String em = "Error while writing to client: " + LS + e.getMessage();
            throw new IllegalActionException(this, em);
        }
        // get tokens' time stamp. This time will be written to the
        // client in the next time step, this time step read from the client
        // the output which will be sent to clients in the next time step
        // as inputs
        tokTim = getDirector().getModelTime().getDoubleValue();
        System.out.println("the current time is " + tokTim);
    }
}
