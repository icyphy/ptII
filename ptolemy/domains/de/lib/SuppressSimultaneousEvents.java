/* An actor suppresses simultaneous events.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

import ptolemy.actor.util.Time;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SuppressSimultaneousEvents

/**
Output the first token received on the input port, and suppress any other
inputs received with the same physical time index.
 @author Jeff C. Jensen
 @version $Id$
 @since Ptolemy II 8.0
 */
public class SuppressSimultaneousEvents extends DETransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SuppressSimultaneousEvents(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output.setTypeEquals(input.getType());
        output.setWidthEquals(input, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the ports.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SuppressSimultaneousEvents newObject = (SuppressSimultaneousEvents) super
                .clone(workspace);

        newObject.output.setTypeEquals(input.getType());
        newObject.output.setWidthEquals(newObject.input, false);

        // This is not strictly needed (since it is always recreated
        // in preinitialize) but it is safer.
        newObject._lastEventTime = null;

        return newObject;
    }

    /** Consume one token from the input port, and output if it is the first
     *  token seen at this point in physical time.
     * @exception IllegalActionException
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            Time currentTime = getDirector().getModelTime();
            Token currentToken = input.get(0);
            if (_lastEventTime == null || !_lastEventTime.equals(currentTime)) {
                output.broadcast(currentToken);
            }
            _lastEventTime = currentTime;
        }
    }

    /** Reset to indicate that no input has yet been seen.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _lastEventTime = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private Time _lastEventTime;
}
