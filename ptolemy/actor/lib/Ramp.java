/* An actor that outputs a sequence with a given step in values.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Green (eal@eecs.berkeley.edu)
@AcceptedRating Green (bilung@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;


import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Ramp
/**
Produce an output token on each firing with a value that is
incremented by the specified step each iteration. The
first output is given by the <i>init</i> parameter, and the
increment may be given either by the <i>step</i> parameter or by
the associated <i>step</i> port. Note that the increment will show
up in the output only on the next iteration. If you need it to show
up on the current iteration, use the
{@link ptolemy.actor.lib.Accumulator Accumulator} actor.
The type of the output is determined by the constraint that it must
be greater than or equal to the types of the parameter (and/or the
<i>step</i> port, if it is connected).
Thus, this actor is
polymorphic in the sense that its output data type can be that
of any token type that supports addition.

@see Accumulator
@author Yuhong Xiong, Edward A. Lee
@version $Id$
@since Ptolemy II 0.2
*/

public class Ramp extends SequenceSource {

    /** Construct an actor with the given container and name.
     *  In addition to invoking the base class constructors, construct
     *  the <i>init</i> and <i>step</i> parameter and the <i>step</i>
     *  port. Initialize <i>init</i>
     *  to IntToken with value 0, and <i>step</i> to IntToken with value 1.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Ramp(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        init = new Parameter(this, "init", new IntToken(0));
        step = new PortParameter(this, "step", new IntToken(1));

        // set the type constraints.
        output.setTypeAtLeast(init);
        output.setTypeAtLeast(step);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-20\" "
                + "width=\"60\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polygon points=\"-20,10 20,-10 20,10\" "
                + "style=\"fill:grey\"/>\n"
                + "</svg>\n");
        _resultArray = new Token[1];
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The value produced by the ramp on its first iteration.
     *  The default value of this parameter is the integer 0.
     */
    public Parameter init;

    /** The amount by which the ramp output is incremented on each iteration.
     *  The default value of this parameter is the integer 1.
     */
    public PortParameter step;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>init</code> and <code>step</code>
     *  public members to the parameters of the new actor.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        Ramp newObject = (Ramp)super.clone(workspace);
        // set the type constraints.
        newObject.output.setTypeAtLeast(newObject.init);
        newObject.output.setTypeAtLeast(newObject.step);
        _resultArray = new Token[1];
        return newObject;
    }

    /** Send the current value of the state of this actor to the output.
     *  @exception IllegalActionException If calling send() or super.fire()
     *  throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, _stateToken);
    }

    /** Set the state to equal the value of the <i>init</i> parameter.
     *  The state is incremented by the value of the <i>step</i>
     *  parameter on each iteration (in the postfire() method).
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _stateToken = output.getType().convert(init.getToken());
    }

    /** Invoke a specified number of iterations of this actor. Each
     *  iteration updates the state of the actor by adding the
     *  value of the <i>step</i> parameter to the state and sending
     *  the value of the state to the output. The iteration count
     *  is also incremented by the value of <i>count</i>, and if
     *  the result is greater than or equal to <i>firingCountLimit</i>
     *  then return STOP_ITERATING.
     *  <p>
     *  This method should be called instead of the usual prefire(),
     *  fire(), postfire() methods when this actor is used in a
     *  domain that supports vectorized actors.  This leads to more
     *  efficient execution.
     *  @param count The number of iterations to perform.
     *  @return COMPLETED if the actor was successfully iterated the
     *   specified number of times. Otherwise, if the maximum
     *   iteration count has been reached, return STOP_ITERATING.
     *  @exception IllegalActionException If iterating cannot be
     *  performed.
     */
    public int iterate(int count) throws IllegalActionException {
        // Check whether we need to reallocate the output token array.
        if (count > _resultArray.length) {
            _resultArray = new Token[count];
        }
        for (int i = 0; i < count; i++) {
            _resultArray[i] = _stateToken;
            try {
                step.update();
                _stateToken = _stateToken.add(step.getToken());
            } catch (IllegalActionException ex) {
                throw new InternalErrorException(this, ex,
                        "Should not be thrown because we have already"
                        + "verified that the tokens can be added");
            }
        }
        output.send(0, _resultArray, count);
        if (_firingCountLimit != 0) {
            _iterationCount += count;
            if (_iterationCount >= _firingCountLimit) {
                return STOP_ITERATING;
            }
        }
        return COMPLETED;
    }

    /** Update the state of the actor by adding the value of the
     *  <i>step</i> parameter to the state.  Also, increment the
     *  iteration count, and if the result is equal to
     *  <i>firingCountLimit</i>, then
     *  return false.
     *  @return False if the number of iterations matches the number requested.
     *  @exception IllegalActionException If the firingCountLimit parameter
     *   has an invalid expression.
     */
    public boolean postfire() throws IllegalActionException {
        step.update();
        _stateToken = _stateToken.add(step.getToken());
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token _stateToken = null;
    private Token[] _resultArray;
}
