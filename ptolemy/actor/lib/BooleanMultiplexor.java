/* A polymorphic multiplexor with boolean select.

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
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// BooleanMultiplexor

/**
 A multiplexor with a boolean-valued select control signal.
 This actor conditionally routes input values from the
 {@link #trueInput} and {@link #falseInput} ports to the
 output port, depending on the value of the {@link #select} input.
 <p>
 Upon firing, this actor reads the value at the {@link #select} input,
 if there is one, and records its value (true or false).
 If it has a recorded select value (from this firing or a previous
 one), then it reads at most one token from both the
 {@link #trueInput} and the {@link #falseInput}, chooses one
 of those tokens depending on the recorded select value,
 and produces that token on the output.
 Because tokens are immutable, the same Token
 is sent to the output, rather than a copy.
 <p>
 This actor is non strict. Specifically, if either
 {@link #trueInput} or {@link #falseInput} is unknown, it may
 nonetheless be able to produce an output. Hence, this actor can
 be used in domains with fixed-point semantics, such as SR and Continuous.
 <p>
 In dataflow domains (SDF, DDF, and PN), normally all inputs will be
 known and present when the actor fires. It consumes all inputs
 and produces one output token. Thus, the actor behaves like an SDF
 actor, producing and consuming a single token on all ports.
 <p>
 In DE, the actor will only consume those inputs that are available.
 It does not even require a new {@link #select} input on each firing.
 A value provided at the {@link #select} input will persist
 and will be used in subsequent firings until a new value is provided.
 If no value has ever been provided, then this actor will produce no output.
 <p>
 This actor is different from the {@link BooleanSelect} actor, which consumes
 one token from the control input in one firing, and
 then in the next firing consumes a token from either the
 trueInput or the falseInput, depending on the value of the control input.
 It is also different from the {@link Select} actor, which consumes
 one input from the control input and, in the same firing, one token
 from the input channel given by the value of the control input.
 </p>

 @author Steve Neuendorffer, Stavros Tripakis, Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Green (neuendor)
 @Pt.AcceptedRating Yellow (neuendor)
 */
public class BooleanMultiplexor extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public BooleanMultiplexor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        trueInput = new TypedIOPort(this, "trueInput", true, false);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        select = new TypedIOPort(this, "select", true, false);
        select.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);

        new StringAttribute(select, "_cardinal").setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens on the true path.  The type can be anything.
     */
    public TypedIOPort trueInput;

    /** Input for tokens on the false path.  The type can be anything.
     */
    public TypedIOPort falseInput;

    /** Input that selects one of the other input ports.  The type is
     *  boolean.
     */
    public TypedIOPort select;

    /** The output port.  The type is at least the type of
     *  <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read all inputs that are known, and if the <i>select</i> input
     *  is true, then output the token consumed from the
     *  <i>trueInput</i> port, otherwise output the token from the
     *  <i>falseInput</i> port.
     *  If the required input is unknown, then the output will
     *  remain unknown.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        // Be sure to not use _selectValue if the select input
        // is not known. That would be non-monotonic.
        if (select.isKnown(0)) {
            if (select.hasToken(0)) {
                _selectValue = (BooleanToken) select.get(0);
            }
            // Be sure to read all inputs that are present, even
            // if they aren't required in order to produce output.
            // Tokens need to be consumed in dataflow and DE domains.
            if (trueInput.isKnown(0)) {
                Token trueToken = null;
                if (trueInput.hasToken(0)) {
                    trueToken = trueInput.get(0);
                }
                if (_selectValue != null && _selectValue.booleanValue()) {
                    // Note that if the input is known to be absent,
                    // then the following sends null. Dataflow receivers
                    // interpret this as sending nothing (nothing is queued).
                    // Fixed-point receivers (SR and Continuous) interpret
                    // this as an assertion that the output is absent.
                    output.send(0, trueToken);
                }
            }
            if (falseInput.isKnown(0)) {
                Token falseToken = null;
                if (falseInput.hasToken(0)) {
                    falseToken = falseInput.get(0);
                }
                if (_selectValue != null && !_selectValue.booleanValue()) {
                    // Note that if the input is known to be absent,
                    // then the following sends null. Dataflow receivers
                    // interpret this as sending nothing (nothing is queued).
                    // Fixed-point receivers (SR and Continuous) interpret
                    // this as an assertion that the output is absent.
                    output.send(0, falseToken);
                }
            }
            // If no select value has been seen, then we can
            // assert that the output is empty. Note that this is only
            // safe if the select input is known.
            if (_selectValue == null) {
                output.send(0, null);
            }
        }
    }

    /** Initialize this actor to the state where no select
     *  input has been read.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _selectValue = null;
    }

    /** Return false.
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Most recently read select input. */
    private BooleanToken _selectValue;
}
