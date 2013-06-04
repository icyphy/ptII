/* A nonstrict actor that delays tokens by one iteration.

 Copyright (c) 1997-2013 The Regents of the University of California.
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
package ptolemy.domains.sr.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.AbsentToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.sr.kernel.SRDirector;
//import ptolemy.domains.sr.kernel.SRDirector;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// NonstrictDelay

/**
 A nonstrict actor that delays tokens by one iteration.
 <p>
 This actor provides a one-tick delay.  On each firing, it produces
 on the output port whatever value it read on the input port in the
 previous tick of the clock. If the input was absent on the previous
 tick of the clock, then the output will be absent. On the first tick,
 the output is <i>initialValue</i> if it is given, and absent otherwise.
 In contrast to the Pre actor, this actor is non-strict, and hence can
 break causality loops.  Whereas Pre provides a one-step delay of
 non-absent values, this actor simply delays by one clock tick.

 @see Pre
 @see ptolemy.domains.sdf.lib.SampleDelay
 @see ptolemy.domains.de.lib.TimedDelay

 @author Paul Whitaker, Elaine Cheong, and Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (celaine)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class NonStrictDelay extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public NonStrictDelay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        initialValue = new Parameter(this, "initialValue");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Initial token value.  This defaults to no value, which results
     *  in the first output being absent. If a value is given, then
     *  output will be constrained to have at least the type of the value
     *  (as well as at least the type of the input).
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Declare that the output does not depend on the input in a firing but
     *  has a delay of one period of the SRDirector.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     *  @see #getCausalityInterface()
     */
    public void declareDelayDependency() throws IllegalActionException {
        if (getDirector() instanceof SRDirector) {
            _declareDelayDependency(input, output, ((SRDirector)getDirector()).periodValue());
        }
    }
    
    
    
    /** Send to the output the previous token received. If no token
     *  was received on the previous tick,
     *  then assert that the output is absent. If this is
     *  the first tick, then produce on the output the value
     *  provided by <i>initialValue</i>, or there was none, then
     *  assert that the output is empty.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        if (_previousToken != null) {
            if (_previousToken == AbsentToken.ABSENT) {
                output.send(0, null);
                if (_debugging) {
                    _debug("Output is absent.");
                }
            } else {
                output.send(0, _previousToken);
                if (_debugging) {
                    _debug("Output is " + _previousToken);
                }
            }
        } else {
            output.send(0, null);
            if (_debugging) {
                _debug("Output is absent.");
            }
        }
    }

    /** Initialize the state of the actor.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        // Note that this will default to null if there is no initialValue set.
        _previousToken = initialValue.getToken();
        super.initialize();
    }

    /** Return false. This actor can produce some output event the input
     *  receiver has status unknown.
     *  @return False.
     */
    public boolean isStrict() {
        return false;
    }

    /** If the input is known, then read it and record
     *  it for the next tick. Otherwise, throw an exception.
     *  @exception IllegalActionException If the input is not
     *   known, or if there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.isKnown(0)) {
            if (input.hasToken(0)) {
                _previousToken = input.get(0);
            } else {
                _previousToken = AbsentToken.ABSENT;
            }
        } else {
            throw new IllegalActionException(this, "Input is unknown.");
        }
        return super.postfire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the a type constraint that requires the initial value to
     *  be less than or equal to the type of the output.
     */
    @Override
    protected Set<Inequality> _customTypeConstraints() {
        Set<Inequality> result = new HashSet<Inequality>();
        try {
            // type of initialValue <= type of output
            if (initialValue.getToken() != null) {
                result.add(new Inequality(initialValue.getTypeTerm(), output
                        .getTypeTerm()));
            }
        } catch (IllegalActionException ex) {
            // Errors in the initialValue parameter should already
            // have been caught in getAttribute() method of the base
            // class.
            throw new InternalErrorException("Bad initialValue value!");
        }

        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The token received on the previous iteration to be output on the
     *  current iteration.
     */
    protected Token _previousToken;
}
