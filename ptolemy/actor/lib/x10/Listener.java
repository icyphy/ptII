/* Output X10 commands detected on the X10 network.

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.actor.lib.x10;

import x10.Command;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// Listener

/** Monitor the X10 network for any and all commands and output a string
 *  description of the command.
 *
 *  @author Colin Cochran (contributor: Edward A. Lee)
 *  @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Yellow (ptolemy)
*/
public class Listener extends Receiver {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Listener(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Create output port.
        receivedCommand = new TypedIOPort(this, "receivedCommand", false, true);
        receivedCommand.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Port on which to output the command received as a string.
     */
    public TypedIOPort receivedCommand;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output any received command as a string. If no command has been
     *  received, then output an empty string. If there are additional
     *  commands pending, then request another firing at the current
     *  time before returning.
     *  @exception IllegalActionException If super class throws and exception.
     *  @exception InterruptedException If the thread is interupted by another.
     */
    public void fire() throws IllegalActionException {
        super.fire();

        // Check whether a command is ready.
        if (_commandReady()) {
            Command command = _getCommand();
            receivedCommand.send(0, new StringToken(_commandToString(command)));
        } else {
            receivedCommand.send(0, _EMPTY_STRING);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // Empty string token.
    private StringToken _EMPTY_STRING = new StringToken("");
}
