/* A CSP actor that accepts tokens from a single channel.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (nsmyth@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.csp.lib;

import ptolemy.domains.csp.kernel.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.Token;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;

//////////////////////////////////////////////////////////////////////////
//// CSPSink
/**
A CSPSink actor accepts tokens from a single input channel.
Ten tokens can be consumed for every invocation of this
actor's fire() method. Once ten tokens have been received,
the fire method returns. The return value of the postfire()
method defaults to false.

@author Neil Smyth
@version $Id$

 */
public class CSPSink extends AtomicActor {

    /** Construct a CSPSink in the default workspace with an
     *  empty string as a name.
     *  @exception IllegalActionException If there is an error
     *   with instantiation of the tokenLimit parameter.
     *  @exception NameDuplicationException If there is an error
     *   with instantiation of the tokenLimit parameter.
     */
    public CSPSink() throws IllegalActionException,
    	    NameDuplicationException {
        super();
        tokenLimit = new Parameter( this, "tokenLimit",
        	(new IntToken(-1)) );
    }

    /** Construct a CSPSink with the specified container and the
     *  specified name. The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown.
     *  @param cont The container of this actor.
     *  @param name The name of this actor.
     *  @param limit The number of tokens that this actor will produce.
     *  @exception IllegalActionException If the superclass throws it
     *   or if there is an error with instantiation of the tokenLimit
     *   parameter.
     *  @exception NameDuplicationException If the name of the actor
     *   or the tokenLimit parameter is not unique within the
     *   container.
     */
    public CSPSink(CompositeActor cont, String name, int limit)
            throws IllegalActionException, NameDuplicationException {
        super(cont, name);
        input = new IOPort(this, "input", true, false);
        tokenLimit = new Parameter( this, "tokenLimit",
        	(new IntToken(limit)) );
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     */
    public IOPort input;

    /** The number of tokens produced by this actor. If this limit
     *  is set to -1, then produce output tokens indefinitely. The
     *  default value of this parameter is -1.
     */
    public Parameter tokenLimit;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Execute this actor by consuming ten tokens through the
     *  input port.
     */
    public void fire() {
        int count = 0;
        try {
            int limit =
                ((IntToken)tokenLimit.getToken()).intValue();
            while (count < limit || limit < 0 ) {
                Token t = input.get(0);
                System.out.println(getName() + " received Token: " +
                        t.toString());
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
     * @return True indicating that execution of this actor
     *  should not be repeated.
     */
    public boolean postfire() {
        return false;
    }

}
