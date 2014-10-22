/* An actor representing a resource pool with a specified number of resources.

 Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.domains.rendezvous.lib;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.TerminateProcessException;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.domains.rendezvous.kernel.RendezvousDirector;
import ptolemy.domains.rendezvous.kernel.RendezvousReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
////

/**
 This actor manages a pool of resources, where each resource is
 represented by a token with an arbitrary value.  Resources are
 granted on the <i>grant</i> output port and released on the
 <i>release</i> input port. These ports are both multiports,
 so resources can be granted to multiple users of the resources,
 and released by multiple actors.
 <p>
 The initial pool of resources is provided by the <i>initialPool</i>
 parameter, which is an array of arbitrary type.  The <i>grant</i>
 output port and <i>release</i> input port are constrained to have
 compatible types. Specifically, the <i>grant</i> output port must be
 able to send tokens with types that match the elements of this array,
 and it must also be able to send tokens with types that match
 inputs provided at the <i>release</i> input.
 <p>
 This actor is designed for use in the rendezvous domain, where it will
 execute in its own thread. At all times, it is ready to
 rendezvous with any other actor connected to its <i>release</i>
 input port.  When such a rendezvous occurs, the token provided
 at that input is added to the resource pool. In addition,
 whenever the resource pool is non-empty, this actor is ready
 to rendezvous with any actor connected to its <i>grant</i>
 output port. When such a rendezvous occurs, it sends
 the first token in the resource pool to that output port
 and removes that token from the resource pool.
 <p>
 The behavior of this actor is similar to that of the Merge
 actor, except that the Merge actor does no buffering.
 That is, while this actor is always ready to rendezvous with
 any input, the Merge actor is ready to rendezvous with an
 input only after it has delivered the previous input to
 the output.

 @author Edward A. Lee
 @see Merge
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)

 */
public class ResourcePool extends TypedAtomicActor {

    /** Construct an actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ResourcePool(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        grant = new TypedIOPort(this, "grant", false, true);
        grant.setMultiport(true);

        release = new TypedIOPort(this, "release", true, false);
        release.setMultiport(true);

        initialPool = new Parameter(this, "initialPool");
        initialPool.setExpression("{1}");

        // Set type constraints.
        grant.setTypeAtLeast(ArrayType.elementType(initialPool));
        grant.setTypeAtLeast(release);

        // Width default to 1
        grant.setDefaultWidth(1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port through which this actor grants resources.
     *  This port has type equal to the element type
     *  of the <i>initialPool</i> parameter.
     */
    public TypedIOPort grant;

    /** The input port through which other actors release resources.
     *  This port has type equal to the element type
     *  of the <i>initialPool</i> parameter.
     */
    public TypedIOPort release;

    /** The initial resource pool. This is an array with default
     *  value {1} (an integer array with one entry with value 1).
     */
    public Parameter initialPool;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to reset the resource pool to
     *  match the specified initialPool value.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == initialPool) {
            ArrayToken pool = (ArrayToken) initialPool.getToken();
            // Reset the pool.
            _pool.clear();
            // Copy the tokens into the pool.
            for (int i = 0; i < pool.length(); i++) {
                _pool.add(pool.getElement(i));
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to set the type constraints.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ResourcePool actor.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        ResourcePool newObject = (ResourcePool) super.clone(workspace);
        newObject._pool = new LinkedList();
        // set type constraints.
        try {
            newObject.grant.setTypeAtLeast(ArrayType
                    .elementType(newObject.initialPool));
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
        newObject.grant.setTypeAtLeast(newObject.release);

        return newObject;
    }

    /** If the input width is greater than zero and it has not already
     *  been done, start a thread to read a token from the
     *  <i>release</i> input port and store it in the pool.
     *  Then, in the calling thread, if there is at least one
     *  resource in the pool, write the first resource in the pool
     *  to any <i>grant</i> output channel.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     *  @exception TerminateProcessException If the process termination
     *   is requested by the director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        final RendezvousDirector director = (RendezvousDirector) getDirector();
        final Thread writeThread = Thread.currentThread();

        if (!(getDirector() instanceof RendezvousDirector)) {
            throw new IllegalActionException(this,
                    "ResourcePool actor can only be used with RendezvousDirector.");
        }
        _postfireReturns = true;
        if (release.isOutsideConnected() && _readThread == null) {
            _readThread = new Thread(getFullName() + "_readThread") {
                @Override
                public void run() {
                    try {
                        while (!_stopRequested) {
                            // Synchronize on the director since all read/write
                            // operations do.
                            synchronized (director) {
                                if (_debugging) {
                                    _debug("Resources available: " + _pool);
                                }
                                Token resource = RendezvousReceiver.getFromAny(
                                        release.getReceivers(), director);
                                _pool.add(resource);
                                director.threadUnblocked(writeThread, null);
                                director.notifyAll();
                            }
                        }
                    } catch (TerminateProcessException ex) {
                        // OK, just exit
                        _postfireReturns = false;
                    } finally {
                        director.removeThread(_readThread);
                    }
                }
            };
            director.addThread(_readThread);
            _readThread.start();
        } else if (!release.isOutsideConnected() && _readThread != null) {
            // A mutation has eliminated the sources.
            _readThread.interrupt();
        }
        // Synchronize on the director since all read/write
        // operations do.
        synchronized (director) {
            while (_pool.size() == 0) {
                if (_stopRequested || !_postfireReturns) {
                    _postfireReturns = false;
                    return;
                }
                try {
                    director.threadBlocked(writeThread, null);
                    RendezvousReceiver.waitForChange(director);
                } catch (TerminateProcessException ex) {
                    _postfireReturns = false;
                    return;
                } finally {
                    director.threadUnblocked(writeThread, null);
                }
            }
            // There is a token.
            Token token = (Token) _pool.get(0);
            // If this put blocks for any reason, it will block on
            // a director.wait(), so the lock will not be held.
            try {
                RendezvousReceiver.putToAny(token, grant.getRemoteReceivers(),
                        director);
            } catch (TerminateProcessException e) {
                _postfireReturns = false;
                return;
            }
            _pool.remove(0);
        }
    }

    /** Initialize.
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _readThread = null;
        _postfireReturns = true;
        // Force reinitialization of the pool.
        attributeChanged(initialPool);
    }

    /** Return false if it is time to stop the process.
     *  @return False a TerminateProcessException was thrown during I/O.
     */
    @Override
    public boolean postfire() {
        return _postfireReturns;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The current resource pool. */
    private List _pool = new LinkedList();

    /** Flag indicating what postfire should return. */
    private boolean _postfireReturns = true;

    /** The read thread, if it exists. */
    private Thread _readThread = null;
}
