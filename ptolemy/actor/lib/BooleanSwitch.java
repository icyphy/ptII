/* A polymorphic switch with boolean select.

Copyright (c) 1997-2005 The Regents of the University of California.
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

This is similar to Switch and could be design/code reviewed at the same time.
*/
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;


//////////////////////////////////////////////////////////////////////////
//// BooleanSwitch

/**
   A type polymorphic switch with boolean valued control.  In an
   iteration, if an input token is available at the <i>control</i> input,
   that token is read, and its value is noted.  Its value specifies the
   input port that should be read next. If the <i>control</i> input is
   true, then if an input token is available on the <i>input</i> port,
   then it is is read and sent to the <i>trueOutput</i>.  Likewise with a
   false input and the <i>falseOutput</i> port.  Because tokens are
   immutable, the same Token is sent to the output, rather than a copy.
   The <i>input</i> port may receive Tokens of any type.

   <p>If no token has ever been received on the <i>control</i> port, then
   <i>falseOutput</i> is assumed to be the one to receive data.

   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Green (neuendor)
   @Pt.AcceptedRating Red (neuendor)
*/
public class BooleanSwitch extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public BooleanSwitch(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);
        trueOutput = new TypedIOPort(this, "trueOutput", false, true);
        falseOutput = new TypedIOPort(this, "falseOutput", false, true);
        trueOutput.setTypeAtLeast(input);
        falseOutput.setTypeAtLeast(input);

        // Put the control input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(control,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input that selects one of the other input ports.  The type is
     *  BooleanToken.
     */
    public TypedIOPort control;

    /** The input port.  The type can be anything.
     */
    public TypedIOPort input;

    /** Output for tokens on the true path.  The type is at least the
     *  type of the input.
     */
    public TypedIOPort trueOutput;

    /** Output for tokens on the false path.  The type is at least the
     *  type of the input.
     */
    public TypedIOPort falseOutput;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from each input port.  If the token from the
     *  <i>control</i> input is true, then output the token consumed from the
     *  <i>input</i> port on the <i>trueOutput</i> port,
     *  otherwise output the token on the <i>falseOutput</i> port.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (control.hasToken(0)) {
            _control = ((BooleanToken) control.get(0)).booleanValue();
        }

        if (input.hasToken(0)) {
            Token token = input.get(0);

            if (_control) {
                trueOutput.send(0, token);
            } else {
                falseOutput.send(0, token);
            }
        }
    }

    /** Initialize this actor so that the <i>falseOutput</i> is written
     *  to until a token arrives on the <i>control</i> input.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _control = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recently read control token.
    private boolean _control = false;
}
