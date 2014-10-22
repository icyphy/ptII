/* An actor that outputs the previous event when it receives an event.

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
package ptolemy.domains.de.lib;

import java.util.Set;

import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Previous

/**
 On each iteration, this actor produces the token received on the previous
 iteration. On the first iteration, it produces the token given by the
 <i>initialValue</i> parameter, if such a value has been set.
 <p>
 Although it might be tempting to try, this actor is not very useful
 for breaking precedences in a feedback loop in DE, the way the
 TimedDelay actor in DE or the SampleDelay actor in SDF do.
 Since it does not trigger until there is an input, it will not
 actually break the precedences in a feedback loop.
 <p>
 The output data type is constrained to be at least as general
 as both the input and the <i>initialValue</i> parameter.

 @see ptolemy.domains.de.lib.TimedDelay
 @see ptolemy.domains.sdf.lib.SampleDelay

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Previous extends DETransformer {
    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Previous(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        initialValue = new Parameter(this, "initialValue");
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** The initial output value. If this is set, it specifies the
     *  first output value produced when the first input arrives.
     *  If it is not set, then no output is produced on the first
     *  firing.
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a previous token, then produce it on the output,
     *  and then read the input and record it for the next firing.
     *  @exception IllegalActionException If getting token from input or
     *  sending token to output throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (_previous != null) {
            output.send(0, _previous);
        }

        if (input.hasToken(0)) {
            _tempPrevious = input.get(0);
        }
    }

    /** Initialize so that the initial token will be produced.
     *  @exception IllegalActionException If the super class throws it
     *  or can not get a valid token from the initializeValue parameter.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // Note that this might be null, if it has not been set.
        _previous = initialValue.getToken();
        _tempPrevious = null;
    }

    /** Update the state of the actor.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _previous = _tempPrevious;
        _tempPrevious = null;
        return super.postfire();
    }

    /** Override the method in the base class so that the type
     *  constraint for the <i>initialValue</i> parameter will be set
     *  if it contains a value.
     *  @return a list of Inequality objects.
     *  @see ptolemy.graph.Inequality
     */
    /*    public Set<Inequality> typeConstraints() {
            Set<Inequality> typeConstraints = super.typeConstraints();

            try {
                if (initialValue.getToken() != null) {
                    Inequality ineq = new Inequality(initialValue.getTypeTerm(),
                            output.getTypeTerm());
                    typeConstraints.add(ineq);
                }

                Inequality ineq2 = new Inequality(input.getTypeTerm(),
                        output.getTypeTerm());
                typeConstraints.add(ineq2);
            } catch (IllegalActionException ex) {
                // Errors in the initialValue parameter should
                // already have been caught in getAttribute() method
                // of the base class.
                throw new InternalErrorException("Bad initialValue value!");
            }

            return typeConstraints;
        }
     */

    /**
     * Adds to the set of inequalities returned by the overridden method
     * a constraint that requires the initial value to be less than or
     * equal to the type of the output.
     */
    @Override
    public Set<Inequality> _containedTypeConstraints() {
        Set<Inequality> result = super._containedTypeConstraints();
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

    /** Reset the state variables.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        _tempPrevious = null;
        _previous = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Previous input.
    private Token _previous;

    // Temporary previous input.
    private Token _tempPrevious;
}
