/* An actor that implements as preemptable task

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.util.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PreemptableTask
/**
This actor implements a preemptable task.  The <i>executionTime</i>
parameter specifies the time it takes to execute the task.  When a
token is received on the <i>input</i> port, the token is stored in the
actor and the actor is scheduled to fire at <i>executionTime</i> time
units later.  When the <i>interrupt</i> port receives a token, and the
value is true, this task is "preempted."  Later, when the value of the
token on the <i>interrupt</i> port becomes false, the elapsed time is
calculated and added to the execution time of the task.  The actor is
scheduled to fire at this new execution time.  The saved token is
emitted when the new execution time has passed.  Subsequent input
tokens are queued until the actor is done processing the current input
token.

<p>This method of queueing tokens inside of the actor results in a
cleaner design that queuing tokens in the receiver.  Queueing tokens
in the receiver requires keeping track of the many states that result
from the interaction between tokens on the input ports and whether the
actor is currently executing a task or is currently interrupted.

@author Elaine Cheong and Yang Zhao and Xiaojun Liu
@version $Id$
@since Ptolemy II 2.0
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
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        interrupt = new TypedIOPort(this, "interrupt", true, false);
        interrupt.setTypeEquals(BaseType.BOOLEAN);
        executionTime =
            new Parameter(this, "executionTime", new DoubleToken(1.0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The interrupt port, which has type BooleanToken. If this port
     *  receives a token with the value true, then the task is
     *  preempted.  If this port receives a token with the value
     *  false, then the task resumes execution.
     */
    public TypedIOPort interrupt;

    /** The executionTime parameter indicates the time it takes to
     *  execute the task.
     */
    public Parameter executionTime;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>executionTime</i>, then check that
     *  the value is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the service time is negative.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == executionTime) {
            double time =
                ((DoubleToken)(executionTime.getToken())).doubleValue();
            if (time < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative execution time: " + time);
            }
        } else {
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        // FIXME: Is this clone method necessary?
        PreemptableTask newObject = (PreemptableTask)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /**  When the actor is in a non-executing and non-interrupted
     *   state, and there is an available input token, begin execution
     *   of the task by scheduling the actor to fire at
     *   <i>executionTime</i> time units later.  When the
     *   <i>interrupt</i> port receives a token, and the value is
     *   true, this task is "preempted."  Later, when the value of the
     *   token on the <i>interrupt</i> port becomes false, the elapsed
     *   time is calculated and added to the execution time of the
     *   task.  The actor is scheduled to fire at this new execution
     *   time.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        double currentTime = ((DEDirector)getDirector()).getCurrentTime();
        DEDirector director = (DEDirector)getDirector();

        // If there is an input and actor is in a non-executing and
        // non-interrupted state.
        if (_tokenList.size() > 0 && !_executing && !_interrupted) {
            _executing = true;
            _outputTime = currentTime
                + ((DoubleToken)(executionTime.getToken())).doubleValue();
            director.fireAt(this, _outputTime);
        }

        // If there is an interrupt.
        if (interrupt.hasToken(0)) {
            boolean interruptValue =
                ((BooleanToken)interrupt.get(0)).booleanValue();
            if (interruptValue) {
                _interruptTime = currentTime;
                _interrupted = true;
            } else {
                _interrupted = false;
                if (_executing) {
                    double delay_time = currentTime - _interruptTime;
                    _outputTime += delay_time;
                    director.fireAt(this, _outputTime);
                }
            }
        }
    }

    /** Indicate that the task in non-interrupted, non-executing
     *  state.  Also create a new linked list to store input tokens.
     *  @exception IllegalActionException If the base class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        _executing = false;
        _interruptTime = 0.0;
        _interrupted = false;
        _outputTime = 0.0;

        _tokenList = new LinkedList();
    }

    /** If the task receives a token on the <i>input</i> port, queue
     *  it inside of the actor.  Return true.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        DEDirector director = (DEDirector)getDirector();
        double currentTime = director.getCurrentTime();

        if (input.hasToken(0)) {
            _tokenList.add(input.get(0));
        }
        return true;
    }

    /**  If the execution time of the task has passed, emit the saved
     *  input value.  If the actor is in a non-executing and
     *  non-interrupted state, and there are more input tokens to
     *  process, schedule the actor to fire again.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        DEDirector director = (DEDirector)getDirector();
        double currentTime = director.getCurrentTime();

        if (_executing && !_interrupted) {
            if (currentTime >= _outputTime) {
                output.send(0, (Token)_tokenList.removeFirst());
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

    // If true, the actor received a token on the input port and has not yet
    // emitted the corresponding output.
    private boolean _executing = false;

    // Records the time at which a token arrives on the interrupt port
    // (the time at which the task is interrupted).
    private double _interruptTime = 0.0;

    // If true, the task has been interrupted.
    private boolean _interrupted = false;

    // Records the time at which the output should be
    // emitted (execution time + any elapsed interrupt time).
    private double _outputTime = 0.0;

    // Queue of saved input tokens.
    private LinkedList _tokenList;
}
