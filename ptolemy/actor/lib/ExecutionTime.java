/* An actor that consumes a specified amount of real time.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

import ptolemy.data.BooleanToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ExecutionTime

/**
 Read the input token, if there is one, execute an (uninteresting)
 computation to consume a specified amount of real time or to execute
 it a fixed number of times, and produce
 on the output the actual execution time (in milliseconds). Unlike the
 Sleep actor, which suspends the calling thread for the specified
 amount of time, this actor performs a computation during the
 specified amount of time, thus consuming compute resources.
 If <i>realTime</i> is true, then the number of computations it
 performs is not fixed, but rather depends on what the thread
 scheduler does. If it is false, then the amount of computation
 done is fixed. The default is false.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @see Sleep
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class ExecutionTime extends LimitedFiringSource {

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

        realTime = new Parameter(this, "realTime");
        realTime.setTypeEquals(BaseType.BOOLEAN);
        realTime.setExpression("false");

        granularity = new Parameter(this, "granularity");
        granularity.setTypeEquals(BaseType.LONG);
        granularity.setExpression("400000L");

        output.setTypeEquals(BaseType.LONG);

        // Show the firingCountLimit parameter last.
        firingCountLimit.moveToLast();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The amount of time to consume. This is either in milliseconds,
     *  if the realTime parameter is set to true, or in the number of
     *  iterations of a fixed computation, if the realTime parameter
     *  is set to false. This is a long
     *  that defaults to 1000L.
     */
    public Parameter executionTime;

    /** The granularity of the computation. This parameter specifies
     *  the number of additions performed in each invocation of the
     *  (uninteresting) computation. This is a long, which defaults
     *  to 400000, which yields a computation time granularity
     *  of approximately 1msec on a MacBook Pro.
     */
    public Parameter granularity;

    /** If true, then the executionTime parameter is
     *  interpreted as milliseconds. If it is false (the default), then the
     *  executionTime parameter is interpreted to specify the number
     *  of cycles of a fixed computation.  Use false to specify
     *  a fixed computational load, and use true to specify an
     *  amount of real time to consume. When this is true,
     *  if the thread executing the fire() method is preempted
     *  during its run, then less computation is done.
     */
    public Parameter realTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the input token, consume time, the produce on the
     *  output the actual execution time used.
     *  @exception IllegalActionException If send() throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        long start = System.currentTimeMillis();
        super.fire();
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                // Read and discard the input.
                trigger.get(i);
            }
        }
        long executionTimeValue = ((LongToken) executionTime.getToken())
                .longValue();
        //long granularityValue = ((LongToken) granularity.getToken())
        //        .longValue();
        boolean realTimeValue = ((BooleanToken) realTime.getToken())
                .booleanValue();
        boolean moreToDo = true;
        long count = 0L;
        while (moreToDo) {
            // NOTE: The number here determines the granularity.
            //int dummy = 0;
            //for (int i = 0; i < granularityValue; i++) {
            //    dummy++;
            //}
            if (realTimeValue) {
                moreToDo = System.currentTimeMillis() - start < executionTimeValue;
            } else {
                moreToDo = count < executionTimeValue;
            }
            count++;
        }
        // Produce on the output the actual time consumed, in case because
        // of the granularity above it differs from the specified time.
        Token result = new LongToken(System.currentTimeMillis() - start);
        output.send(0, result);
    }
}
