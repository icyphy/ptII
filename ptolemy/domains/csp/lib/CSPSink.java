/* A CSP actor that accepts tokens from a single channel.

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
import ptolemy.actor.NoTokenException;
import ptolemy.actor.lib.Sink;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPSink

/**
 A CSPSink actor accepts tokens from a single input channel.
 Ten tokens can be consumed for every invocation of this
 actor's fire() method. Once ten tokens have been received,
 the fire method returns. The return value of the postfire()
 method defaults to false.

 @author Neil Smyth
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 */
public class CSPSink extends Sink {
    /** Construct a CSPSink with the specified container and the
     *  specified name. The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown.
     *  @param container The container of this actor.
     *  @param name The name of this actor.
     *  @param limit The number of tokens that this actor will produce.
     *  @exception IllegalActionException If the superclass throws it
     *   or if there is an error with instantiation of the tokenLimit
     *   parameter.
     *  @exception NameDuplicationException If the name of the actor
     *   or the tokenLimit parameter is not unique within the
     *   container.
     */
    public CSPSink(CompositeActor container, String name, int limit)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        tokenLimit = new Parameter(this, "tokenLimit", new IntToken(limit));
        input.setMultiport(false);
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

    /** Execute this actor by consuming ten tokens through the
     *  input port.
     *  @exception IllegalActionException If thrown by the superclass.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        int count = 0;

        try {
            int limit = ((IntToken) tokenLimit.getToken()).intValue();

            // FindBugs: [H C IL] An apparent infinite loop [IL_INFINITE_LOOP]
            // If limit < 0 you indeed have an infinite loop,
            // but that's the intend.
            while (count < limit || limit < 0) {
                Token t = input.get(0);
                System.out.println(getName() + " received Token: "
                        + t.toString());
                count++;
            }

            return;
        } catch (IllegalActionException ex) {
            System.out.println("CSPSink invalid get, exiting...");
        } catch (NoTokenException ex) {
            System.out.println("CSPSink invalid get, exiting...");
        }
    }

    /** Return false indicating that this actor should not be
     *  executed in the next iteration of the containing
     *  composite actor.
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
}
