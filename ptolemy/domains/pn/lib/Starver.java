/* A base class for actors that transform an input stream into an output stream.

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
package ptolemy.domains.pn.lib;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Starver

/**
 On each firing, read at most one input token and send it
 to the output port. When the number of output tokens that
 have been produced reaches the value given by <i>limit</i>,
 then do not produce any more outputs.  Subsequent input tokens
 are consumed and discarded.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class Starver extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Starver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        limit = new Parameter(this, "limit");
        limit.setTypeEquals(BaseType.INT);
        limit.setExpression("1");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The limit on the number of tokens that can be transferred
     *  from the input to the output.  This is an integer that
     *  defaults to 1. If the value is negative, then there is no
     *  limit.
     */
    public Parameter limit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read at most one input token, and if the number of outputs
     *  has not yet exceeded the value given by <i>limit</i>, then
     *  produce that token on the output.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Token token = null;
        if (input.hasToken(0)) {
            token = input.get(0);
        }
        int limitValue = ((IntToken) limit.getToken()).intValue();
        if (token != null && (limitValue < 0 || _count < limitValue)) {
            output.send(0, token);
            _count++;
        }
    }

    /** Initialize this actor by setting the count of outputs to zero.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _count = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** The count of outputs that have been produced. */
    private int _count;
}
