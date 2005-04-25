/* A polymorphic multiplexor with boolean select.

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

(This is similar to Select and BooleanMultiplexor and could be
design/code reviewed at the same time.
*/
package ptolemy.actor.lib;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;


//////////////////////////////////////////////////////////////////////////
//// BooleanSelect

/**
   A type polymorphic select with boolean valued control.  In an
   iteration, if an input token is available at the <i>control</i> input,
   that token is read, and its value is noted.  Its value specifies the
   input port that should be read next. If the <i>control</i> input is
   true, then if an input token is available on the <i>trueInput</i>
   port, then it is is read and sent to the output.  Likewise with a
   false input and the <i>falseInput</i> port.  The token sent to the
   output is determined by the <i>control</i> input, which must be a
   boolean value.  Because tokens are immutable, the same Token is sent
   to the output, rather than a copy.  The <i>trueInput</i> and
   <i>falseInput</i> port may receive Tokens of any type.

   <p> The actor indicates a willingness to fire in its prefire() method
   if there is an input available on the channel specified by the most
   recently seen token on the <i>control</i> port.  If no token has ever
   been received on the <i>control</i> port, then <i>falseInput</i> is
   assumed to be the one to read.

   <p> This actor is similar to the BooleanMultiplexor actor, except that
   it never discards input tokens.  Tokens on channels that are not
   selected are not consumed.

   @author Steve Neuendorffer, Adam Cataldo
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Green (neuendor)
   @Pt.AcceptedRating Red (neuendor)
*/
public class BooleanSelect extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public BooleanSelect(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        trueInput = new TypedIOPort(this, "trueInput", true, false);
        falseInput = new TypedIOPort(this, "falseInput", true, false);
        control = new TypedIOPort(this, "control", true, false);
        control.setTypeEquals(BaseType.BOOLEAN);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeAtLeast(trueInput);
        output.setTypeAtLeast(falseInput);

        // Put the control input on the bottom of the actor.
        StringAttribute controlCardinal = new StringAttribute(control,
                "_cardinal");
        controlCardinal.setExpression("SOUTH");

        /** Make the icon show T, F, and C for trueInput, falseInput
         *  and control.
         */
        _attachText("_iconDescription",
                "<svg>\n" + "<rect x=\"-20\" y=\"-20\" "
                + "width=\"40\" height=\"40\" " + "style=\"fill:white\"/>\n"
                + "<text x=\"-17\" y=\"-3\" " + "style=\"font-size:14\">\n"
                + "T \n" + "</text>\n" + "<text x=\"-17\" y=\"15\" "
                + "style=\"font-size:14\">\n" + "F \n" + "</text>\n"
                + "<text x=\"-5\" y=\"16\" " + "style=\"font-size:14\">\n" + "C \n"
                + "</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for tokens on the true path.  The type can be anything.
     */
    public TypedIOPort trueInput;

    /** Input for tokens on the false path.  The type can be anything.
     */
    public TypedIOPort falseInput;

    /** Input that selects one of the other input ports.  The type is
     *  BooleanToken.
     */
    public TypedIOPort control;

    /** The output port.  The type is at least the type of
     *  <i>trueInput</i> and <i>falseInput</i>
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from each input port.  If the token from the
     *  <i>control</i> input is true, then output the token consumed from the
     *  <i>trueInput</i> port, otherwise output the token from the
     *  <i>falseInput</i> port.
     *
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (_control) {
            // Redo this check in case the control has changed since prefire().
            if (trueInput.hasToken(0)) {
                output.send(0, trueInput.get(0));
            }
        } else {
            if (falseInput.hasToken(0)) {
                output.send(0, falseInput.get(0));
            }
        }
    }

    /** Initialize this actor so that the <i>falseInput</i> is read
     *  from until a token arrives on the <i>control</i> input.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _control = false;
    }

    /** Return false if the control input channel does not have a token,
     *  or if the control input is true, the true input does not have a
     *  token, or if the control input is false, the false input does not
     *  have a token. Otherwise, return whatever the superclass returns.
     *  @return False if there are not enough tokens to fire.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean prefire() throws IllegalActionException {
        if (control.hasToken(0)) {
            _control = ((BooleanToken) control.get(0)).booleanValue();

            if (_control) {
                if (!trueInput.hasToken(0)) {
                    return false;
                }
            } else {
                if (!falseInput.hasToken(0)) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // The most recently read control token.
    private boolean _control = false;
}
