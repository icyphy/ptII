/* A polymorphic multiplexor.

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

*/
package ptolemy.actor.lib;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// Multiplexor

/**
   This actor selects from the channels on the
   <i>input</i> port, copying the input from one channel to the output,
   based on the most recently received value on the <i>select</i> input.
   If the selected channel has no token, then no output is produced.
   The <i>select</i> input is required to be an integer between 0 and
   <i>n</i>-1, where <i>n</i> is the width of the <i>input</i> port.
   If no token has been received on the <i>select</i> port, then channel
   0 is sent to the output.  The <i>input</i> port may
   receive Tokens of any type, but all channels must have the same type.
   <p>
   One token is consumed from each input channel that has a token.
   Compare this with the Select actor, which only consumes a token on
   the selected channel.

   @author Jeff Tsay and Edward A. Lee
   @version $Id$
   @since Ptolemy II 1.0
   @Pt.ProposedRating Yellow (ctsay)
   @Pt.AcceptedRating Yellow (cxh)
   @see ptolemy.actor.lib.Select
*/
public class Multiplexor extends Transformer {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Multiplexor(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setMultiport(true);

        select = new TypedIOPort(this, "select", true, false);
        select.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input for the index of the port to select. The type is IntToken. */
    public TypedIOPort select;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read a token from the <i>select</i> port and from each channel
     *  of the <i>input</i> port, and output a token on the selected
     *  channel.  This method will throw a NoTokenException if any
     *  input channel does not have a token.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the <i>select</i> input is out of range.
     */
    public void fire() throws IllegalActionException {
        if (select.hasToken(0)) {
            _channel = ((IntToken) select.get(0)).intValue();
        }

        boolean inRange = false;

        for (int i = 0; i < input.getWidth(); i++) {
            inRange = inRange || (i == _channel);

            if (input.hasToken(i)) {
                Token token = input.get(i);

                if (i == _channel) {
                    output.send(0, token);
                }
            }
        }

        if (!inRange) {
            throw new IllegalActionException(this,
                "Select input is out of range: " + _channel + ".");
        }
    }

    /** Initialize to the default, which is to use channel zero. */
    public void initialize() {
        _channel = 0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The most recently read select input. */
    private int _channel = 0;
}
