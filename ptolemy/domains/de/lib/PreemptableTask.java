/* An actor that implements as preemptable task

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.Transformer;
import ptolemy.actor.lib.SequenceActor;
import ptolemy.actor.lib.TimedActor;
import ptolemy.actor.*;
import ptolemy.actor.util.FIFOQueue;

//////////////////////////////////////////////////////////////////////////
//// PreemptableTask
/**
This actor implements a preemptable task.  The <i>executionTime</i>
parameter specifies the time it takes to execute the task.  When a
token is received on the <i>input</i> port, the value is stored in the
actor and the actor is scheduled to fire at <i>executionTime</i> time
units later.  When the <i>interrupt</i> port receives a token, and the
value is true, this task is "preempted."  Later, when the value of the
token on the <i>interrupt</i> port becomes false, the elapsed time is
calculated and added to the execution time of the task.  The actor is
scheduled to fire at this new execution time.  The saved input value
is emitted when the new execution time has passed.

<p>

@author Elaine Cheong and Yang Zhao and Xiaojun Liu
@version $Id$
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
        output.setTypeAtLeast(input);
        interrupt = new TypedIOPort(this, "interrupt", true, false);
        interrupt.setTypeEquals(BaseType.BOOLEAN);

        executionTime = new Parameter(this, "executionTime", new DoubleToken(1.0));

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
    /*
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        //PreemptableTask newObject = (PreemptableTask)super.clone(workspace);
        //newObject._queue = new FIFOQueue();
        //newObject.output.setTypeAtLeast(newObject.input);
        //return newObject;
    }
     */
    
    /**  When a token is received on the <i>input</i> port, the value
     *   is stored in the actor and the actor is scheduled to fire at
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
        double curr_time = ((DEDirector)getDirector()).getCurrentTime();
        DEDirector director = (DEDirector)getDirector();
        
        if(input.hasToken(0)) {
            _savedInputValue = ((DoubleToken)input.get(0)).doubleValue();
            _outputTime =  curr_time + ((DoubleToken)(executionTime.getToken())).doubleValue();
            director.fireAt(this, _outputTime);
            _hasOutput = true;
        } if (interrupt.hasToken(0)) {
            boolean intr = ((BooleanToken)interrupt.get(0)).booleanValue();
            if (intr) {
                _interruptTime = curr_time;
                _interrupted = true;
            } else {
                if (_interrupted && _hasOutput) {
                    _interrupted = false;
                    double delay_time = curr_time - _interruptTime;
                    _outputTime += delay_time;
                    director.fireAt(this, _outputTime);
                }
            }
        }
            
    }

   /**  If the execution time of the task has passed, emit the saved
     *  input value.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        if (_hasOutput) {
            double curr_time = ((DEDirector)getDirector()).getCurrentTime();
            if (curr_time >= _outputTime) {
                DoubleToken newToken = new DoubleToken(_savedInputValue);
                output.send(0, newToken);
                _hasOutput = false;
            }
        }
        return super.postfire();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // If true, the actor received a token on the input port and has not yet
    // emitted the corresponding output.
    boolean _hasOutput = false;

    // Records the time at which a token arrives on the interrupt port
    // (the time at which the task is interrupted).
    double _interruptTime = 0;

    // If true, the task has been interrupted .
    boolean _interrupted = false;

    // Records the time at which the output should be
    // emitted (execution time + any elapsed interrupt time).
    double _outputTime = 0;

    // Saves the value of the input token.
    double _savedInputValue;
}
