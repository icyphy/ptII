/* An actor that produces tokens through an output channel.

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
import java.util.Random;


//////////////////////////////////////////////////////////////////////////
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

*/

public class CSPSource extends AtomicActor {

    /** Construct a CSPSource in the default workspace with an
     *  empty string as a name.
     *  @exception IllegalActionException If there is an error
     *   with instantiation of the tokenLimit parameter.
     *  @exception NameDuplicationException If there is an error
     *   with instantiation of the tokenLimit parameter.
     */
    public CSPSource() throws IllegalActionException,
            NameDuplicationException {
        super();
        tokenLimit = new Parameter( this, "tokenLimit",
        	(new IntToken(-1)) );
    }

    /** Construct a CSPSource with the sepcified container and the
     *  specified name. The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown.
     *  @param cont The container of this actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the superclass throws it
     *   or if there is an error with instantiation of the tokenLimit
     *   parameter.
     *  @exception NameDuplicationException If the name of the actor
     *   or the tokenLimit parameter is not unique within the
     *   container.
     */
    public CSPSource(CompositeActor cont, String name)
            throws IllegalActionException, NameDuplicationException {
        this(cont, name, -1, 0);
    }

    /** Construct a CSPSource with the sepcified container and the
     *  specified name. The name must be unique within the container
     *  or an exception is thrown. The container argument must not be
     *  null, or a NullPointerException will be thrown. The limit
     *  argument specifies how many tokens will be produced during an
     *  invocation of the fire() method. If the value of limit is set
     *  to -1, then tokens will be produced indefinitely. The initValue
     *  argument specifies the initial integer value of the output
     *  tokens.
     *  @param cont The container of this actor.
     *  @param name The name of this actor.
     *  @param limit The number of tokens that this actor will produce.
     *  @param initValue The initial integer value of the produced
     *   tokens.
     *  @exception IllegalActionException If the superclass throws it
     *   or if there is an error with instantiation of the tokenLimit
     *   parameter.
     *  @exception NameDuplicationException If the name of this actor
     *   or the tokenLimit paramter is not unique within the container.
     */
    public CSPSource(CompositeActor cont, String name, int limit,
    	    int initValue) throws IllegalActionException,
            NameDuplicationException {
        super(cont, name);
        _value = initValue;
        tokenLimit = new Parameter( this, "tokenLimit",
        	(new IntToken(limit)) );
        output = new IOPort(this, "output", false, true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port.
     */
    public IOPort output;

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
     */
    public void fire() {
        try {
            int limit =
                ((IntToken)tokenLimit.getToken()).intValue();
            Random rand = new Random();
            while ( (_value < limit) || (limit < 0) ) {
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
     * @return false Indicating that iteration of this actor should
     *  should not continue.
     */
    public boolean postfire() {
        return false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    private int _value = 0;

}
