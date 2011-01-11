/* This abstract class for a quantity manager.

@Copyright (c) 2010 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.actor;

import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.Token;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * An Actor that receives Tokens and assigns quantities to the tokens. 
 * @author Patricia Derler
 */
public abstract class QuantityManager extends TypedAtomicActor {

    /** Construct a QuantityManager in the default workspace with no
     *  container and an empty string as its name. Add the actor to the
     *  workspace directory.  You should set the local director or
     *  executive director before attempting to send data to the actor or
     *  to execute it. Increment the version number of the workspace.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public QuantityManager() throws IllegalActionException, NameDuplicationException {
        super();
    }

    /** Construct a QuantityManager in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     * @throws NameDuplicationException 
     * @throws IllegalActionException 
     */
    public QuantityManager(Workspace workspace) throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }
    
    /** Construct a QuantityManager with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public QuantityManager(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     * Reset the QuantityManager.
     */
    public abstract void reset();

    /**
     * Take the token and assign quantities. The token is later sent to the receiver.
     * @param receiver Final receiver of the token once quantities are assigned.
     * @param token Token that is received.
     * @throws IllegalActionException If token cannot be sent.
     */
    public abstract void sendToken(Receiver receiver, Token token) throws IllegalActionException;
    
    /**
     * Create a wrapper for the given receiver and return the wrapper.
     * @param receiver Receiver to be wrapped
     * @return Wrapper for the receiver.
     */
    public abstract Receiver getReceiver(Receiver receiver);
    
}
