/* An actor that implements a queue of events.

Copyright (c) 1998-2005 The Regents of the University of California.
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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


//////////////////////////////////////////////////////////////////////////
//// QueueWithNextOut

/**
   This actor implements an event queue.  When a token is received on the
   <i>input</i> port, it is stored in the queue.
   When the <i>trigger</i> port receives a token, the oldest element in the
   queue is output.  If there is no element in the queue when a
   token is received on the <i>trigger</i> port, then no output is
   produced.  The inputs can be of any token type, and the output
   is constrained to be of a type at least that of the <i>input</i>. <p>

   An additional output port, <i>nextOut</i>, has been added which allows
   the model to know what's next to come out.  This new output produces a
   token whenever the queue has been empty and a new token is queued.  It
   also produces an output whenever a token is taken from the queue and
   at least one token remains.  Otherwise, no output token is produced at
   <i>nextOut</i>.  The token produced is the oldest token remaining in
   the queue.  This output, also, is constrained to be at least that of
   <i>input</i> <p>

   @author Winthrop Williams
   @version $Id$
   @since Ptolemy II 2.0
   @Pt.ProposedRating Yellow (winthrop)
   @Pt.AcceptedRating Yellow (winthrop)
*/
public class QueueWithNextOut extends Queue {
    //FIXME: make this consistent with the queue from ptolemy classic

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public QueueWithNextOut(CompositeEntity container, String name)
        throws NameDuplicationException, IllegalActionException {
        super(container, name);
        nextOut = new TypedIOPort(this, "nextOut");
        nextOut.setTypeAtLeast(input);
        nextOut.setOutput(true);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The nextOut port, which has type Token.  Gives a preview
     *  of the next token that will come out of the queue.  Produces
     *  an output only when a new token is next up in the queue.
     */
    public TypedIOPort nextOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        QueueWithNextOut newObject = (QueueWithNextOut) super.clone(workspace);
        newObject.nextOut.setTypeAtLeast(newObject.input);
        return newObject;
    }

    /** If there is a token in the <i>trigger</i> port, emit on the
     *  <i>output</i> port the most recent token from the <i>input</i>
     *  port.  If there has been no input token, or there is no token
     *  on the <i>trigger</i> port, emit nothing on the <i>output</i>
     *  port.  If a new token is next to come out, either because the
     *  just received its first event or because one was emitted and
     *  at least one remains, then output a preview of this token at
     *  the <i>nextOut</i> port.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (input.hasToken(0)) {
            _queue.put(input.get(0));

            if (_queue.size() == 1) {
                // Queue was empty, new item is next item.
                // Send it without removing it from the queue.
                nextOut.send(0, (Token) _queue.get(0));
            }
        }

        if (trigger.hasToken(0)) {
            // Consume the trigger token.
            trigger.get(0);

            if (_queue.size() > 0) {
                output.send(0, (Token) _queue.take());
            }

            if (_queue.size() > 0) {
                // If queue still has token(s), send the
                // next token while keeping a copy in the queue.
                nextOut.send(0, (Token) _queue.get(0));
            }
        }
    }
}
