/* An actor that produces tokens through an output channel.

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
package ptolemy.domains.csp.lib;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.lib.Source;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPSource

/**
 A CSPSource actor produces tokens through an output channel.
 The tokenLimit parameter specifies how many tokens are
 produced by this actor. If the value of tokenLimit is a
 nonnegative integer, then the actor produces that many tokens.
 If the value is negative, then the actor produces tokens
 indefinitely. The default value of tokenLimit is -1.

 @author Neil Smyth
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Red (cxh)

 */
public class CSPSource extends Source {
    /** Construct a CSPSource with the specified container and the
     *  specified name. The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown. The limit
     *  argument specifies how many tokens will be produced during an
     *  invocation of the fire() method. If the value of limit is set
     *  to -1, then tokens will be produced indefinitely. The initValue
     *  argument specifies the initial integer value of the output
     *  tokens.
     *  @param container The container of this actor.
     *  @param name The name of this actor.
     *  @param limit The number of tokens that this actor will produce.
     *  @param initValue The initial integer value of the produced
     *   tokens.
     *  @exception IllegalActionException If the superclass throws it
     *   or if there is an error with instantiation of the tokenLimit
     *   parameter.
     *  @exception NameDuplicationException If the name of this actor
     *   or the tokenLimit parameter is not unique within the container.
     */
    public CSPSource(CompositeActor container, String name, int limit,
            int initValue) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _value = initValue;
        tokenLimit = new Parameter(this, "tokenLimit", new IntToken(limit));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number of tokens produced by this actor. If this limit
     *  is set to -1, then produce output tokens indefinitely. The
     *  default value of this parameter is -1.
     */
    public Parameter tokenLimit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by producing IntTokens on the output port.
     *  If the tokenCount was not set to a nonnegative, integer value,
     *  then produce output tokens indefinitely. Otherwise, produce
     *  N output tokens for N = tokenCount.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            int limit = ((IntToken) tokenLimit.getToken()).intValue();

            // FindBugs: [H C IL] An apparent infinite loop [IL_INFINITE_LOOP]
            // If limit < 0 you indeed have an infinite loop,
            // but that's the intend.
            while (_value < limit || limit < 0) {
                Token t = new IntToken(_value);
                output.send(0, t);
                _value++;
            }

            return;
        } catch (IllegalActionException ex) {
            System.out.println("CSPSource: illegalActionException, "
                    + "exiting");
        }
    }

    /** Return false indicating that iteration of this actor should
     *  not continue.
     *  @exception IllegalActionException If thrown by the parent
     *  class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // We intentially ignore the return value of super.postfire()
        // here.
        super.postfire();
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    private int _value = 0;
}
