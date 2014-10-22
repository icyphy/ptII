/*
Below is the copyright agreement for the Ptolemy II system.

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
 */
package ptolemy.domains.tdl.kernel;

import ptolemy.actor.Actor;

///////////////////////////////////////////////////////////////////
//// LetTask

/**
 * A TTTask (time triggered task) is an actor with an invocationPeriod and a
 * logical execution time.
 *
 * @author Patricia Derler
 * @version $Id$
 * @since Ptolemy II 8.0
 */
public class LetTask {

    /**
     * Create a new LET task.
     *
     * @param actor
     *            The LET task.
     * @param let
     *            The LET of the task.
     * @param invocationPeriod
     *            The period of invocation of the task.
     * @param start
     *            The start time of the task.
     */
    public LetTask(Actor actor, long let, long invocationPeriod, long start) {
        _invocationPeriod = invocationPeriod;
        _let = let;
        _actor = actor;
        _offset = start;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the invocation period of the task.
     *
     * @return the invocationPeriod.
     */
    public long getInvocationPeriod() {
        return _invocationPeriod;
    }

    /**
     * Return the LET of the task.
     *
     * @return the LET.
     */
    public long getLet() {
        return _let;
    }

    /**
     * Return the actor representing the task.
     *
     * @return the task actor.
     */
    public Actor getActor() {
        return _actor;
    }

    /**
     * Return the offset of the task.
     *
     * @return the offset.
     */
    public long getOffset() {
        return _offset;
    }

    /**
     * The invocation period of a task specifies the amount of time that passes
     * before the task needs to be executed again.
     */
    private long _invocationPeriod;

    /**
     * The logical execution time is the logical time required by the task for
     * execution. At the beginning of the logical execution time, input ports
     * are updated and the task execution is started. At the end of the logical
     * execution time, output ports of the task are updated.
     */
    private long _let;

    /** performs the task execution, */
    private Actor _actor;

    /**
     * the offset of the task specifies the time the task is first invoked.
     * After this first invocation, the task is invoked periodically.
     */
    private long _offset;

}
