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
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Timer
/**
   A timer actor extends TimedDelay actor and produces an event with a time 
   delay specified by the input instead by the delay parameter.
   When a timer actor receives an input, if the input value is bigger than 
   0.0, it schedules itself to fire again some time later (introducing a
   delay) to produce an output specified by the value parameter. The delay 
   is specified by the input value. If the input value is 0.0, an output is
   produced immediately. If there is no input token, then no output token 
   is produced.
   @see TimedDelay

   @author Jie Liu, Edward A. Lee, Haiyang Zheng
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (liuj)
*/
public class Timer extends TimedDelay {

    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Timer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        delay.setVisibility(Settable.NONE);
        input.setTypeEquals(BaseType.DOUBLE);
        value = new Parameter(this, "value", new BooleanToken(true));
        output.setTypeSameAs(value);
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
        Timer newObject = (Timer)super.clone(workspace);
        newObject.output.setTypeSameAs(value);
        return newObject;
    }

    /** Read one token from the input and save it so that the
     *  postfire method can produce an output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        _delay = -1.0;
        if (input.hasToken(0)) {
            _currentInput = input.get(0);
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
            _currentOutput = (Token)_delayedTokens.get(
                new Double(currentTime.getTimeValue()));
            if (_currentOutput != null) {
                output.send(0, _currentOutput);
                return;
            } else {
                // no tokens to be produced at the current time.
            }
        }
        if (_delay == 0.0) {
            output.send(0, value.getToken());
            _currentInput = null;
        }
    }

    /** If the token read in the most recent fire() invocation was
     *  nonnegative, then produce an output with a time stamp in
     *  the future.  The time delay is equal to the value of the
     *  input.  If there was no input token, or if its value is
     *  negative, then no output is produced.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
        Time currentTime = getDirector().getModelTime();
        Time delayToTime = currentTime.add(_delay);
        // Remove the token that is scheduled to be sent 
        // at the current time.
        if (_delayedTokens.size() > 0 && 
            _currentOutput != null) {
            _delayedTokens.remove(new Double(currentTime.getTimeValue()));
        }
        // Store the not handled token that is scheduled to 
        // be sent in future.
        if (_currentInput != null && _delay >= 0) {
            _delayedTokens.put(new Double(delayToTime.getTimeValue()), 
                value.getToken());
            getDirector().fireAt(this, delayToTime);
        }
        return super.postfire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(input, output);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Current input.
    private Token _currentInput;
}
