/* An actor that simulates a preemptable task.

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
package ptolemy.domains.de.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.util.LinkedList;


//////////////////////////////////////////////////////////////////////////
//// PreemptableTask

/**
   This actor simulates a preemptable task.  The <i>executionTime</i>
   parameter specifies the time it takes to execute the task.  Tokens
   received on the <i>input</i> port correspond to invocations of the
   task.

   When a token is received on the <i>input</i> port, the token is stored
   in the actor and the actor is scheduled to fire at
   <i>executionTime</i> time units later.  When the <i>interrupt</i> port
   receives a token, and the value is true, this task is "preempted."
   Later, when the value of the token on the <i>interrupt</i> port
   becomes false, the elapsed time is calculated and added to the
   execution time of the task.  The actor is scheduled to fire at this
   new execution time.  The saved token is emitted when the new execution
   time has passed.  Subsequent input tokens are queued until the actor
   is done processing the current input token, in which case the actor is
   scheduled to fire at the current time to respond to the next task
   invocation in the queue.

   @author Elaine Cheong and Yang Zhao and Xiaojun Liu
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Yellow (celaine)
   @Pt.AcceptedRating Yellow (celaine)
*/
public class PreemptableTask extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PreemptableTask(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        interrupt = new TypedIOPort(this, "interrupt", true, false);
        interrupt.setTypeEquals(BaseType.BOOLEAN);
        executionTime = new Parameter(this, "executionTime",
                new DoubleToken(1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The interrupt port, which has type BooleanToken. If this input
     *  port receives a token with the value true, then the task is
     *  "preempted."  When this port receives a token with the value
     *  false, the task resumes execution.
     */
    public TypedIOPort interrupt;

    /** The executionTime parameter indicates the time it takes to
     *  execute the task.
     *
     *  Note: In a future version of this actor, perhaps we want to
     *  use a parameter expression to compute the execution time based
     *  on the token value.
     */
    public Parameter executionTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>executionTime</i>, then check that
     *  the value is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == executionTime) {
            _executionTimeValue = ((DoubleToken) (executionTime.getToken()))
                            .doubleValue();

            if (_executionTimeValue < 0.0) {
                throw new IllegalActionException(this,
                    "Cannot have negative execution time: "
                    + _executionTimeValue);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**  If the task receives a token on the <i>input</i> port, queue
     *   it inside of the actor.
     *
     *   When the actor is in a non-executing and non-interrupted
     *   state, and there is an available input token at the
     *   <i>input</i> port, begin execution of the task by scheduling
     *   the actor to fire at <i>executionTime</i> time units later.
     *   When the <i>interrupt</i> port receives a token, and the
     *   value is true, this task is "preempted."  Later, when the
     *   value of the token on the <i>interrupt</i> port becomes
     *   false, the elapsed time is calculated and added to the
     *   execution time of the task.  The actor is scheduled to fire
     *   at this newly calculated execution time.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        Time currentTime = ((DEDirector) getDirector()).getModelTime();
        DEDirector director = (DEDirector) getDirector();

        // If the task receives a token on the <i>input</i> port, queue
        // it inside of the actor.
        if (input.hasToken(0)) {
            _tokenList.add(input.get(0));
        }

        // If there is an input and actor is in a non-executing and
        // non-interrupted state.
        if ((_tokenList.size() > 0) && !_executing && !_interrupted) {
            _executing = true;
            _outputTime = currentTime.add(_executionTimeValue);
            director.fireAt(this, _outputTime);
        }

        // If there is an interrupt.
        if (interrupt.hasToken(0)) {
            boolean interruptValue = ((BooleanToken) interrupt.get(0))
                            .booleanValue();

            if (interruptValue) {
                _interruptTime = currentTime;
                _interrupted = true;
            } else {
                _interrupted = false;

                if (_executing) {
                    _outputTime = _outputTime.add(currentTime.subtract(
                                _interruptTime));
                    director.fireAt(this, _outputTime);
                }
            }
        }
    }

    /** Indicate that the task in a non-interrupted, non-executing
     *  state.  Also create a new linked list to store input tokens.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _executing = false;
        _interruptTime = new Time(getDirector());
        _interrupted = false;
        _outputTime = new Time(getDirector());

        _tokenList = new LinkedList();
    }

    /**  If the execution time of the task has passed, emit the saved
     *  input token.  If the actor is in a non-executing and
     *  non-interrupted state, and there are more input tokens to
     *  process, schedule the actor to fire again.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        // FIXME: can not be used in SR, dealing with time
        DEDirector director = (DEDirector) getDirector();
        Time currentTime = director.getModelTime();

        if (_executing && !_interrupted) {
            if (currentTime.compareTo(_outputTime) >= 0) {
                output.send(0, (Token) _tokenList.removeFirst());
                _executing = false;
            }
        }

        // We put this in a separate "if" statement to account for the
        // case where we receive an input token while in an
        // interrupted state.
        if (!_executing && !_interrupted) {
            if (_tokenList.size() > 0) {
                director.fireAt(this, currentTime);
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Cache the value of the executionTime parameter.  Defaults to 1.0.
    private double _executionTimeValue = 1.0;

    // If true, the actor received a token on the input port and has not yet
    // emitted the corresponding output.
    private boolean _executing = false;

    // Records the time at which a token arrives on the interrupt port
    // (the time at which the task is interrupted).
    private Time _interruptTime;

    // If true, the task has been interrupted.
    private boolean _interrupted = false;

    // Records the time at which the output should be
    // emitted (execution time + any elapsed interrupt time).
    private Time _outputTime;

    // Queue of saved input tokens.
    private LinkedList _tokenList;
}
