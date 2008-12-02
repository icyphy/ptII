/* An actor that consumes a specified amount of real time.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// ExecutionTime

/**
 Read the input token, if there is one, execute an (uninteresting)
 computation to consume a specified amount of real time, and produce
 on the output the actual execution time (in milliseconds). Unlike the
 Sleep actor, which suspends the calling thread for the specified
 amount of time, this actor performs a computation during the
 specified amount of time, thus consuming compute resources.

 @author Edward A. Lee
 @version $Id: ExecutionTime.java 51551 2008-11-11 00:19:48Z rodiers $
 @since Ptolemy II 7.2
 @see Sleep
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ExecutionTime extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ExecutionTime(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        executionTime = new Parameter(this, "executionTime");
        executionTime.setTypeEquals(BaseType.LONG);
        executionTime.setExpression("1000L");
        
        output.setTypeEquals(BaseType.LONG);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** The amount of time (in milliseconds) to consume. This is a long
     *  that defaults to 1000L.
     */
    public Parameter executionTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the input token, consume time, the produce on the
     *  output the actual execution time used.
     *  @exception IllegalActionException If send() throws it.
     */
    public void fire() throws IllegalActionException {
        long start = System.currentTimeMillis();
        super.fire();
        if (input.hasToken(0)) {
            // Read and discard the input.
            input.get(0);
        }
        long executionTimeValue = ((LongToken)executionTime.getToken()).longValue();
        while (System.currentTimeMillis() - start < executionTimeValue) {
            // NOTE: The number here determines the granularity.
            int count = 0;
            for (int i = 0; i < 10000; i++) {
                count++;
            }
        }
        // Produce on the output the actual time consumed, in case because
        // of the granularity above it differs from the specified time.
        Token result = new LongToken(System.currentTimeMillis() - start);
        output.send(0, result);
    }
}
