/* A non-strict actor that converts tokens into expressions.

@Copyright (c) 1998-2003 The Regents of the University of California.
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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Red (hyzheng@eecs.berkeley.edu)
@AcceptedRating Red (hyzheng@eecs.berkeley.edu)
*/

package ptolemy.domains.sr.lib;

import ptolemy.actor.lib.conversions.TokenToExpression;
import ptolemy.data.StringToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// NonStrictTokenToExpression
/**
This actor is a non-strict version of the TokenToExpression actor.
When its input is unknown, it ouptuts "unknown". When its input is
known but has no token, it outputs "absent". Otherwise, it
does the same thing as TokenToExpression actor does.

@author Haiyang Zheng
@version $Id$
@since Ptolemy II 3.1
@see ptolemy.actor.lib.conversions.TokenToExpression
*/
public class NonStrictTokenToExpression extends TokenToExpression {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonStrictTokenToExpression(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        new Attribute(this, "_nonStrictMarker");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output a string token whose value is an expression representing
     *  the value of the input token. 
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        if (input.isKnown(0)) {
            if (input.hasToken(0)) {
                String string = input.get(0).toString();
                output.broadcast(new StringToken(string));
            } else {
                output.broadcast(new StringToken("absent"));
            }
        } else {
            output.broadcast(new StringToken("unknown"));
        }
    }

    /** Return true.
     *  @exception IllegalActionException If there is no director, or
     *   if no connection has been made to the input.
     */
    public boolean prefire() throws IllegalActionException {
        return input.isKnown(0);
    }

    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() {
        removeDependency(input, output);
    }
}
