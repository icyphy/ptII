/* A nonstrict actor that delays tokens by one iteration.

 Copyright (c) 1997-2003 The Regents of the University of California.
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

@ProposedRating Yellow (celaine@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// NonstrictDelay
/**
This actor provides a token delay.  It has one input port and one
output port, both of which are single ports.  A token that is received
on the input port is sent on the output port on the next iteration.
If more than one token is received on the input port in a given
iteration, only the final token is output on the next iteration.  If
no tokens are received on the input port in a given iteration
(regardless of whether the <i>initialValue</i> parameter is set), no
token is output on the next iteration.
<p>
You can specify the value of the token to be emitted in the first
iteration by setting the <i>initialValue</i> parameter.  The token is
emitted in the first iteration, regardless of whether any tokens are
received on the input port.  If the parameter is left empty, then no
token is emitted in the first iteration.
<p>
FIXME: This actor is truly questionable in an SR domain.
It uses a special token, AbsentToken, and delays that as well as
real tokens.  This does not conform with the usual SR behavior.
(EAL)

Compare this actor to other single token delay actors:
@see ptolemy.domains.sdf.lib.SampleDelay
@see ptolemy.domains.de.lib.TimedDelay

@author Paul Whitaker and Elaine Cheong
@version $Id$
@since Ptolemy II 2.0
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
        new Attribute(this, "_nonStrictMarker");

        initialValue = new Parameter(this, "initialValue");
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Initial token value.  Can be of any type.
     *  @see #typeConstraintList()
     */
    public Parameter initialValue;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the input known and there is a token on the input port, 
     *  consume the token from the input port, and store it for output 
     *  on the next iteration. Otherwise, store an AbsentToken for
     *  output on the next iteration.  
     *  If a token was received on the previous iteration, output it to the
     *  recerivers. Otherwise, notify the receivers that there will never be
     *  any token available in the current iteration. 
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.isKnown(0)) {
            if (input.hasToken(0)) {
                _currentToken = input.get(0);
            } else {
                _currentToken = AbsentToken.ABSENT;
            }
        }

        if (_previousToken != null) {
            if (_previousToken == AbsentToken.ABSENT) {
                output.sendClear(0);
            } else {
                output.send(0, _previousToken);
            }
        } else {
            output.sendClear(0);
        }
    }

    /** Initialize the state of the actor.
     *  @exception IllegalActionException If there is no director.
     */
    public void initialize() throws IllegalActionException {
        // Note that this will default to null if there is no initialValue set.
        _previousToken = initialValue.getToken();
        _currentToken = null;
        super.initialize();
    }

    /** Update the state of the actor.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        _previousToken = _currentToken;
        _currentToken = null;

        return super.postfire();
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        removeDependency(input, output);
    }

    /** Override the method in the base class so that the type
     *  constraint for the <i>initialValue</i> parameter will be set
     *  if it contains a value.
     *  @return a list of Inequality objects.
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
    ////                         protected variables               ////

    // The token received on the previous iteration to be output on the
    // current iteration.
    protected Token _previousToken;

    // The most recent token received on the current iteration to be
    // output on the next iteration.
    protected Token _currentToken;

}


