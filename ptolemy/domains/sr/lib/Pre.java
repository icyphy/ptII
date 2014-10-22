/* When the input is present, the output is the previously received input.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Pre

/**
 * When the input is present, the output is the previously received
 * input. When the input is absent, the output is absent. The first
 * time the input is present, the output is given by <i>initialValue
 * </i>, or if <i>initialValue </i> is not given, then the output is
 * absent. The output data type is greater than or equal to the input
 * and the <i>initialValue </i> parameter, if it is given. Note that
 * in contrast to the NonStrictDelay actor, this actor is strict. It
 * cannot fire until the input is known.  While NonStrictDelay delays
 * by one clock tick, regardless of whether the input is present, this
 * actor delays only present values, and produces an output only when
 * the input is present.

 *
 * @see NonStrictDelay
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 4.1
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Pre extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Pre(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        initialValue = new Parameter(this, "initialValue");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Initial token value.  Can be of any type.
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is an input token, the produce the previously read
     *  input token on the output. If there is no previously read
     *  input token, then produce the <i>initialValue</i> token.
     *  If the <i>initialValue</i> has not been set, the produce
     *  absent.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            if (_currentToken != null) {
                output.send(0, _currentToken);
            } else {
                output.send(0, null);
            }
        }
    }

    /** Initialize the actor by recording the value of <i>initialValue</i>,
     *  if there is one.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        // Note that this will default to null if there is no initialValue set.
        _currentToken = initialValue.getToken();
        super.initialize();
    }

    /** Update the state of the actor by recording the current input
     *  value, if there is one.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _currentToken = input.get(0);
        }

        return super.postfire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a constraint that requires the initial value to be less than or
     *  equal to the type of the output.
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
    ////                         private variables                 ////

    /** The most recent token received on the current iteration to be
        output on the next iteration. */
    private Token _currentToken;
}
