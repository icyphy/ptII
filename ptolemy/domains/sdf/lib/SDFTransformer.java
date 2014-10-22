/* A base class for SDF actors that transform an input stream into an
 output stream.

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
package ptolemy.domains.sdf.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// SDFTransformer

/**
 This is an abstract base class for actors that transform an input
 stream into an output stream.  It provides an input and an output
 port.

 @author Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class SDFTransformer extends TypedAtomicActor implements SequenceActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SDFTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate");
        input_tokenConsumptionRate.setExpression("1");
        input_tokenConsumptionRate.setVisibility(Settable.NOT_EDITABLE);
        input_tokenConsumptionRate.setTypeEquals(BaseType.INT);
        input_tokenConsumptionRate.setPersistent(false);

        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate");
        output_tokenProductionRate.setExpression("1");
        output_tokenProductionRate.setVisibility(Settable.NOT_EDITABLE);
        output_tokenProductionRate.setTypeEquals(BaseType.INT);
        output_tokenProductionRate.setPersistent(false);

        output_tokenInitProduction = new Parameter(output,
                "tokenInitProduction");
        output_tokenInitProduction.setExpression("0");
        output_tokenInitProduction.setVisibility(Settable.NOT_EDITABLE);
        output_tokenInitProduction.setTypeEquals(BaseType.INT);
        output_tokenInitProduction.setPersistent(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the number of available tokens on the <i>input</i>
     *  port is at least the declared consumption rate for the port.
     *  Otherwise return false.
     *  @exception IllegalActionException If it is thrown accessing the port.
     *  @return True if there are enough tokens.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        Token rateToken = input_tokenConsumptionRate.getToken();
        int required = ((IntToken) rateToken).intValue();

        // Derived classes may convert the input port to a multiport.
        for (int i = 0; i < input.getWidth(); i++) {
            if (!input.hasToken(i, required)) {
                if (_debugging) {
                    _debug("Called prefire(), "
                            + " input tokenConsumptionRate = " + required
                            + ", input.hasToken(" + i + ", " + required
                            + ") is false, prefire() returning false");
                }

                return false;
            }
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The rate parameter for the input port.
     */
    public Parameter input_tokenConsumptionRate;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    /** The rate parameter for the output port.
     */
    public Parameter output_tokenProductionRate;

    /** The rate parameter for the output port that declares the
     *  initial production.
     */
    public Parameter output_tokenInitProduction;
}
