/* A register.

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

import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// Register
/**
   In the fire() method, if there is an event on the <i>trigger</i>
   input port, this actor will produce an output event. The value
   of the output event will be the previously recorded event
   from the <i>input</i> port, or the value of the <i>initialValue</i>
   parameter if there has been no previous input event. If, however,
   <i>initialValue</i> contains no value, then no output will be
   produced. In the postfire() method, if there is an input event on
   the <i>input</i> port, then its value is recorded to be used
   in future firings as the value of the output.
   The inputs can be of any token type, but the <i>output</i> port
   is constrained to be of a type at least that of the <i>input</i>
   port and the <i>initialValue</i> parameter.
   
   <p> Unlike its base class, the Sampler actor, this actor can be used
   to break dependencies in a feedback loop.

   <p> Both the <i>input</i> port and the <i>output</i> port are multiports.
   Generally, their widths should match. Otherwise, if the width of the
   <i>input</i> is greater than the width of the <i>output</i>, the extra
   input tokens will not appear on any output, although they will be
   consumed from the input port. If the width of the <i>output</i> is
   greater than that of the <i>input</i>, then the last few channels of
   the <i>output</i> will never emit tokens.

   <p> Note: If the width of the input changes during execution, then the
   most recent inputs are forgotten, as if the execution of the model
   were starting over.
   
   <p> This class extends Sampler because its interface is identical
   and its behavior is similar.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (eal)
   @see ptolemy.domains.de.lib.Sampler
*/

public class Register extends Sampler {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Register(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n" +
                "<rect x=\"-10\" y=\"-20\" "
                + "width=\"20\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "</svg>\n");
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If there is a token in the <i>trigger</i> port, emit the previously
     *  seen inputs from the <i>input</i> port. If there has been no
     *  previous input tokens, but the <i>initialValue</i> parameter
     *  has been set, emit the value of the <i>initialValue</i> parameter.
     *  Otherwise, emit nothing.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        int inputWidth = input.getWidth();
        int outputWidth = output.getWidth();
        int commonWidth = Math.min(inputWidth, outputWidth);

        // If the <i>initialValue</i> parameter was not set, or if the
        // width of the input has changed.
        if (_lastInputs == null || _lastInputs.length != inputWidth) {
            _lastInputs = new Token[inputWidth];
        }

        // If we have a trigger...
        if (trigger.hasToken(0)) {
            // Consume the trigger token.
            trigger.get(0);
            for (int i = 0; i < commonWidth; i++) {
                if (_lastInputs[i] != null) {
                    output.send(i, _lastInputs[i]);
                }
            }
        }
    }

    /** Record any input values available on the <i>input</i> port.
     *  @exception IllegalActionException If there is no director.
     */
    public boolean postfire() throws IllegalActionException {
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
        return super.postfire();
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    public void pruneDependencies() {
        super.pruneDependencies();
        removeDependency(input, output);
    }
}
