/* An actor that produces an event with a time delay specified by the input.

Copyright (c) 1998-2004 The Regents of the University of California.
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

import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// NonInterruptibleTimer
/**
   A NonInterruptibleTimer actor works like a {@link Timer} actor except that 
   new inputs will not be processed unless the timer is off. In other words, 
   when a timer is counting down to the scheduled time out, it can not be 
   interrupted to respond new inputs. Instead, the new inputs will be queued 
   and processed in a first come first serve (FCFS) fashion.
   <p>
   This actor extends the {@link Server} actor. The difference from a server
   actor is that the amount of delay is provided by the input value instead 
   of a port parameter.
   
   @see Server
   @see Timer
   @author Haiyang Zheng
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Red (hyzheng)
   @Pt.AcceptedRating Red (hyzheng)
*/
public class NonInterruptibleTimer extends Server {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonInterruptibleTimer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      ports and parameters                 ////

    /** The value produced at the output.  This can have any type,
     *  and it defaults to a boolean token with value <i>true</i>.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Allow type changes on the <i>value</i> parameter, and notify
     *  the director that resolved types are invalid.
     *  @param attribute The attribute whose type changed.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == value) {
            Director director = getDirector();
            if (director != null) {
                director.invalidateResolvedTypes();
            }
        } else {
            super.attributeTypeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then links the type of the <i>value</i> parameter
     *  to the output.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        NonInterruptibleTimer newObject 
            = (NonInterruptibleTimer)super.clone(workspace);
        newObject.output.setTypeSameAs(value);
        return newObject;
    }

    /** Read one token from the input and produce an output that is
     *  scheduled at the current time.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        _delay = -1.0;
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
            _delayedInputTokensList.addLast(_currentInput);
            double delayValue = ((DoubleToken)_currentInput).doubleValue();
            if (delayValue < 0) {
                throw new IllegalActionException(
                    "Delay can not be negative.");
            } else {
                _delay = delayValue;
            }
        } else {
            _currentInput = null;
        }
        Time currentTime = getDirector().getModelTime();
        _currentOutput = null;
        if (_delayedTokens.size() > 0) {
            _currentOutput = (Token)_delayedTokens.get(currentTime);
            if (_currentOutput != null) {
                output.send(0, value.getToken());
                return;
            } else {
                // no tokens to be produced at the current time.
            }
        }
        if (_delay == 0.0 && _delayedInputTokensList.size() > 0) {
            _delayedInputTokensList.removeFirst();
            output.send(0, value.getToken());
            _currentInput = null;
        }
     }

    /** If there are delayed input events that are not processed and the
     *  timer is ready, begin processing the earliest input event and schedule 
     *  a future firing to produce it.
     *  @exception IllegalActionException If there is no director or can not
     *  schedule future firings to handle delayed input events.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();

        // Remove the curent output token from _delayedTokens.
        // NOTE: In this server class, the _delayedTokens can have
        // at most one token inside (like a processer can execute
        // at most one process at any time.) 
        if (_currentOutput != null) {
            _delayedTokens.remove(currentTime);
        }
        // If the delayedTokensList is not empty, and the delayedTokens
        // is empty (ready for a new service), get the first token
        // and put it into service. Schedule a refiring to wave up 
        // after the service finishes.
        if (_delayedInputTokensList.size() != 0 && _delayedTokens.isEmpty()) {
            // NOTE: the input has a fixed data type as double.
            DoubleToken delayToken = (DoubleToken)_delayedInputTokensList.removeFirst();
            double delay = delayToken.doubleValue();
            _nextTimeFree = currentTime.add(delay);
            _delayedTokens.put(_nextTimeFree, delayToken);
            getDirector().fireAt(this, _nextTimeFree);
        }
        return !_stopRequested;
    }

    /** Override the super class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> port in a firing.
     */
    public void pruneDependencies() {
        removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected method                    ////

    /** Override the method of the super class to initialize parameters 
     *  of this actor.
     */
    protected void _init() 
        throws NameDuplicationException, IllegalActionException  {
        input.setTypeEquals(BaseType.DOUBLE);
        
        value = new Parameter(this, "value", new BooleanToken(true));
        output.setTypeSameAs(value);
        output.setTypeSameAs(input);
    }
}
