/* A pre operator for the SR domain.

Copyright (c) 1997-2005 The Regents of the University of California.
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

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.List;


//////////////////////////////////////////////////////////////////////////
//// Pre

/**
 * When the input is present, the output is the previously received input. When
 * the input is absent, the output is absent. The first time the input is
 * present, the output is given by <i>initialValue </i>, or if <i>initialValue
 * </i> is not given, then the output is absent. The output data type is greater
 * than or equal to the input and the <i>initialValue </i> parameter, if it is
 * given. Note that in contrast to the NonStrictDelay actor, this actor is
 * strict. It cannot fire until the input is known.  While NonStrictDelay
 * delays by one clock tick, regardless of whether the input is present,
 * this actor delays only present values, and produces an output only when
 * the input is present.
 *
 * @see NonStrictDelay
 * @author Edward A. Lee
 * @version $Id$
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
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            if (_currentToken != null) {
                output.send(0, _currentToken);
            } else {
                output.sendClear(0);
            }
        }
    }

    /** Initialize the actor by recording the value of <i>initialValue</i>,
     *  if there is one.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        // Note that this will default to null if there is no initialValue set.
        _currentToken = initialValue.getToken();
        super.initialize();
    }

    /** Update the state of the actor by recording the current input
     *  value, if there is one.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _currentToken = input.get(0);
        }

        return super.postfire();
    }

    /** Override the method in the base class so that the type
     *  constraint for the <i>initialValue</i> parameter will be set
     *  if it contains a value.
     *  @return A list of Inequality objects.
     *  @see ptolemy.graph.Inequality
     */
    public List typeConstraintList() {
        List typeConstraints = super.typeConstraintList();

        try {
            if (initialValue.getToken() != null) {
                Inequality ineq = new Inequality(initialValue.getTypeTerm(),
                        output.getTypeTerm());
                typeConstraints.add(ineq);
            }
        } catch (IllegalActionException ex) {
            // Errors in the initialValue parameter should already
            // have been caught in getAttribute() method of the base
            // class.
            throw new InternalErrorException("Bad initialValue value!");
        }

        return typeConstraints;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recent token received on the current iteration to be
    // output on the next iteration.
    private Token _currentToken;
}
