/* An actor that produces a copy of the most recent input each time
   the trigger input receives an event.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Yellow (neuendor@eecs.berkeley.edu)
*/

package ptolemy.domains.de.lib;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// SamplerWithDefault
/**
Output the most recent input token when the <i>trigger</i> port
receives a token.  If no token has been received on the <i>input</i>
port when a token is received on the <i>trigger</i> port, then the
value of the <i>initialValue</i> parameter is produced.  The inputs
can be of any token type, and the output is constrained to be of a
type at least that of the input and the parameter.

<p> Both the <i>input</i> port and the
<i>output</i> port are multiports.  Generally, their widths should
match. Otherwise, if the width of the <i>input</i> is greater than the
width of the <i>output</i>, the extra input tokens will not appear on
any output, although they will be consumed from the input port. If the
width of the <i>output</i> is greater than that of the <i>input</i>,
then the last few channels of the <i>output</i> will never emit
tokens.

<p> Note: If the width of the input changes during
execution, then the most recent inputs are forgotten, as if the
execution of the model were starting over.

<p> This actor differs
from the Sampler actor in how it handles the initial condition.  If
the initial conditions are not too critical (if, for instance, the
first event to this actor is an <i>input</i> rather than a
<i>trigger</i>) then it is easier to use the Sampler actor, since no
properly typed initial value need be specified.

@author Steve Neuendorffer, Jie Liu, Edward A. Lee
@version $Id$
*/

public class SamplerWithDefault extends DETransformer {
    /* FIXME: Ideally, the Sampler should have a parameter that can be
       specified as 'absent' to indicate no initial output.  We should
       figure out how to make this work with the type system and
       expression language.
    */
    /* FIXME2: It would be nice to have a version of this actor
       which is a Register.  The difference is that a register
       ignores the value of its input at the current time until
       after its output has been generated.  A register could be
       used to break feedback loops, whereas this cannot.
    */


    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SamplerWithDefault(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        input.setMultiport(true);
        output.setMultiport(true);
        output.setTypeAtLeast(input);
        trigger = new TypedIOPort(this, "trigger", true, false);
        trigger.setTypeEquals(BaseType.GENERAL);
        initialValue = new Parameter(this, "initialValue");
        initialValue.setExpression("");
        initialValue.setTypeSameAs(input);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The trigger port, which has type Token. If this port
     *  receives a token, then the most recent token from the
     *  <i>input</i> port will be emitted on the <i>output</i> port.
     */
    public TypedIOPort trigger;

    /** The value that is output when no input has yet been received.
     *  The type should be the same as the input port.
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *  has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        SamplerWithDefault newObject =
            (SamplerWithDefault)super.clone(workspace);
        newObject.output.setTypeAtLeast(newObject.input);
        newObject.initialValue.setTypeSameAs(newObject.input);
        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastInputs = null;
        return newObject;
    }

    /** If there is a token in the <i>trigger</i> port,
     *  emit the most recent token from the <i>input</i> port. If there
     *  has been no input token, or there is no token on the <i>trigger</i>
     *  port, emit the value of the <i>initialValue</i> parameter.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int inputWidth = input.getWidth();
        int outputWidth = output.getWidth();
        int commonWidth = Math.min(inputWidth, outputWidth);
        // Consume the inputs we save.
        for (int i = 0; i < commonWidth; i++) {
            while (input.hasToken(i)) {
                _lastInputs[i] = input.get(i);
            }
        }
        // Consume the inputs we don't save.
        for (int i = commonWidth; i < inputWidth; i++) {
            while (input.hasToken(i)) {
                input.get(i);
            }
        }
        // If we have a trigger...
        if (trigger.hasToken(0)) {
            // Consume the trigger token.

	    // This trigger.get() used to be wrapped in a while () statement,
	    // Zoltan Kemenczy of Research In Motion pointed out that
	    // with the while () statement, this actor behaved differently
	    // from Sampler:
	    // "Sampler outputs a token for each token present on the trigger
	    // port (which is what I would expect), but SamplerWithDefault
	    // does not.  If multiple trigger tokens are queued on the
	    // trigger port at the same time instance, only one
	    // output will be produced by SamplerWithDefault."

	    trigger.get(0);

            for (int i = 0; i < commonWidth; i++) {
                // Output the most recent token, assuming
                // the receiver has a FIFO behavior.
                output.send(i, _lastInputs[i]);
            }
        }
    }

    /** If there is no input on the <i>trigger</i> port, return
     *  false, indicating that this actor does not want to fire.
     *  This has the effect of leaving input values in the input
     *  ports, if there are any.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        // If the trigger input is not connected, never fire.
        if (trigger.getWidth() > 0) {
            return (trigger.hasToken(0));
        } else {
            return false;
        }
    }

    /** Clear the cached input tokens.
     *  @exception IllegalActionException If there is no director.
     */
    public void preinitialize() throws IllegalActionException {
        _lastInputs = new Token[input.getWidth()];
        for (int i = 0; i < input.getWidth(); i++) {
            _lastInputs[i] = initialValue.getToken();
        }
        super.preinitialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Token[] _lastInputs;
}
