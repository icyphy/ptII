/* A commutator that processes a single token per iteration, used in DDF doamin.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.ddf.lib;

import ptolemy.actor.lib.SingleTokenCommutator;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// DDFSingleTokenCommutator

/**
 The DDFSingleTokenCommutator has a multiport input port and an output
 port.  The types of the ports are undeclared and will be resolved by
 the type resolution mechanism, with the constraint that the output
 type must be greater than or equal to the input type. On each call to
 the fire() method, the actor reads one token from the current input
 channel, and writes the token to the output port. Then in the postfire()
 method, it will update token consumption rate of the input port so that
 it will read token from the next channel in the next iteration.

 @author Gang Zhou
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (zgang)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DDFSingleTokenCommutator extends SingleTokenCommutator {

    // FIXME: This actor extends sr.lib.SingleTokenCommutator which is
    // really wrong because it means that if we ship DDF, then we _must_
    // ship SR.  One solution would be to move this actor elsewhere,
    // say actor/lib.  It would be interesting to see what happens when
    // this actor is used with SDF, perhaps we should throw an error
    // automatically.

    /** Construct an actor in the specified container with the specified name.
     *  @param container The container.
     *  @param name This is the name of this distributor within the container.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public DDFSingleTokenCommutator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        input_tokenConsumptionRate.setTypeEquals(new ArrayType(BaseType.INT));
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** This parameter provides token consumption rate for each input
     *  channel. The type is array of ints.
     */
    public Parameter input_tokenConsumptionRate;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Pre-calculate the rates to be set in the rate parameter of the
     *  <i>input</i> port. Initialize the private variable _rateArray,
     *  each element of which indicates the <i>input</i> port needs to
     *  consume one token from a corresponding channel and no token from
     *  the rest of the channels.
     *  @param port The port that has connection changes.
     */
    @Override
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);

        if (port == input) {
            try {
                _rateArray = new ArrayToken[input.getWidth()];

                IntToken[] rate = new IntToken[input.getWidth()];

                for (int i = 0; i < input.getWidth(); i++) {
                    rate[i] = _zero;
                }

                for (int i = 0; i < input.getWidth(); i++) {
                    rate[i] = _one;
                    _rateArray[i] = new ArrayToken(BaseType.INT, rate);
                    rate[i] = _zero;
                }
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(
                        this,
                        ex,
                        "At this time IllegalActionExceptions are not allowed to happen.\n"
                                + "Width inference should already have been done.");
            }
        }
    }

    /** Begin execution by setting rate parameter indicating it will
     *  read the zeroth input channel.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        input_tokenConsumptionRate.setToken(_rateArray[0]);
    }

    /** Update rate parameter indicating the next input channel.
     *  @return True if execution can continue into the next iteration.
     *  @exception IllegalActionException If any called method throws
     *   IllegalActionException.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // Call postfire first so that current input position is updated.
        boolean postfireReturn = super.postfire();

        input_tokenConsumptionRate
        .setToken(_rateArray[_getCurrentInputPosition()]);

        return postfireReturn;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** A final static IntToken with value 1.
     */
    private final static IntToken _one = new IntToken(1);

    /** A final static IntToken with value 0.
     */
    private final static IntToken _zero = new IntToken(0);

    /** An array of ArrayTokens to be used to set tokenConsumptionRate
     *  of the input port. Each ArrayToken indicates the <i>input</i>
     *  port needs to consume one token from a corresponding channel and
     *  no token from the rest of the channels. The array is initialized
     *  in the method connectionsChanged().
     */
    private ArrayToken[] _rateArray;
}
