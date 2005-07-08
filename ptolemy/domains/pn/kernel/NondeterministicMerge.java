/* A nondeterministic merge actor for PN.

 Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.domains.pn.kernel;

import java.io.Writer;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

//////////////////////////////////////////////////////////////////////////
//// Merge

/**
 This actor takes any number of input streams and merges them
 nondeterministically.  This actor is intended for use in the
 PN domain. Itis a composite actor that
 creates its own contents.  It contains a PNDirector and one
 actor for each input channel (it creates these actors automatically
 when a connection is created to the input multiport).  The contained
 actors are special actors (implemented as an instance of an inner class)
 that read from the port of this actor and write to the port of
 this actor. They have no ports of their own.  The lifecycle of the
 contained actors (when they are started or stopped) is handled by
 the PNDirector in the usual way.

 @author Edward A. Lee, Haibo Zeng
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class NondeterministicMerge extends TypedCompositeActor {
    /** Construct an actor in the specified container with the specified
     *  name. Create ports and make the input port a multiport.
     *  @param container The container.
     *  @param name The name.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public NondeterministicMerge(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        input.setMultiport(true);
        output.setTypeAtLeast(input);

        channel = new TypedIOPort(this, "channel");
        channel.setOutput(true);
        channel.setTypeEquals(BaseType.INT);

        // Add an attribute to get the port placed on the bottom.
        StringAttribute channelCardinal = new StringAttribute(channel,
                "_cardinal");
        channelCardinal.setExpression("SOUTH");

        _attachText("_iconDescription", "<svg>\n"
                + "<polygon points=\"-10,20 10,10 10,-10, -10,-20\" "
                + "style=\"fill:red\"/>\n" + "</svg>\n");

        /*PNDirector director = */new MergeDirector(this, "director");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.  This base class imposes no type constraints except
     *  that the type of the input cannot be greater than the type of the
     *  output.
     */
    public TypedIOPort input;

    /** The output port. By default, the type of this output is constrained
     *  to be at least that of the input.
     */
    public TypedIOPort output;

    /** Output port used to indicate which input channel the current
     *  output came from. This has type int.
     */
    public TypedIOPort channel;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to adjust the number of contained
     *  actors, if the number is no longer correct.
     *  @param port The port that has connection changes.
     */
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);

        if (port == input) {
            List containedActors = entityList();
            int numberOfContainedActors = containedActors.size();

            // Create the contained actors to handle the inputs.
            int inputWidth = input.getWidth();

            for (int i = 0; i < inputWidth; i++) {
                if (i < numberOfContainedActors) {
                    // Local actor already exists for this channel.
                    // Just wake it up.
                    Object localActor = containedActors.get(i);

                    synchronized (localActor) {
                        localActor.notifyAll();
                    }

                    // ProcessThread associated with the actor might
                    // be blocked on a wait on the director.
                    // So we need to notify on the director also.
                    Director director = getExecutiveDirector();

                    synchronized (director) {
                        director.notifyAll();
                    }
                } else {
                    try {
                        Actor localActor = new ChannelActor(i, this);

                        // Tell the manager to initialize the actor.
                        // NOTE: If the manager is null, then we can't
                        // possibly be executing, so we don't need to do
                        // this.
                        Manager manager = getManager();

                        if ((manager != null)
                                && (manager.getState() != Manager.IDLE)) {
                            manager.requestInitialization(localActor);
                        }

                        // NOTE: Probably don't want this overhead.
                        // ((NamedObj)localActor).addDebugListener(this);
                    } catch (KernelException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Actor to handle an input channel. It has no ports. It uses the
     *  ports of the container.
     */
    private class ChannelActor extends TypedAtomicActor {
        /** Construct an actor in the specified container with the specified
         *  name. The index is set to 0.  This method is used by t 
         *  shallow code generator, which requires a (container, name)
         *  constructor.   
         *  @param container The container.
         *  @param name The name.
         *  @exception NameDuplicationException If an actor
         *   with an identical name already exists in the container.
         *  @exception IllegalActionException If the actor cannot be contained
         *   by the proposed container.
         */
        public ChannelActor(NondeterministicMerge container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            _channelIndex = 0;
            _channelValue = new IntToken(_channelIndex);
        }

        public ChannelActor(int index, NondeterministicMerge container)
                throws IllegalActionException, NameDuplicationException {
            super(container, "ChannelActor" + index);
            _channelIndex = index;
            _channelValue = new IntToken(_channelIndex);
        }

        // Override the base class to not export anything.
        public void exportMoML(Writer output, int depth, String name) {
        }

        public void fire() throws IllegalActionException {
            // If there is no connection, do nothing.
            if (input.getWidth() > _channelIndex) {
                // NOTE: Reading from the input port of the host actor.
                if (!NondeterministicMerge.this._stopRequested
                        && input.hasToken(_channelIndex)) {
                    if (_debugging) {
                        NondeterministicMerge.this
                                ._debug("Waiting for input from channel "
                                        + _channelIndex);
                    }

                    // NOTE: Writing to the port of the host actor.
                    Token result = input.get(_channelIndex);

                    // We require that the send to the two output ports be
                    // atomic so that the channel port gets tokens
                    // in the same order as the output port.
                    // We synchronize on the director because the send()
                    // may call wait() on the director of the container,
                    // so synchronizing on anything else could cause deadlock.
                    synchronized (((NondeterministicMerge) getContainer())
                            .getExecutiveDirector()) {
                        output.send(0, result);
                        channel.send(0, _channelValue);
                    }

                    if (_debugging) {
                        NondeterministicMerge.this._debug("Sent " + result
                                + " from channel " + _channelIndex
                                + " to the output.");
                    }
                }
            } else {
                // Input channel is no longer connected.
                // We don't want to spin lock here, so we
                // wait.
                synchronized (this) {
                    try {
                        workspace().wait(this);
                    } catch (InterruptedException ex) {
                        // Ignore and continue executing.
                    }
                }
            }
        }

        // Override to return the manager associate with the host.
        public Manager getManager() {
            return NondeterministicMerge.this.getManager();
        }

        private int _channelIndex;

        private IntToken _channelValue;
    }

    /** Variant of the PNDirector for the NondeterministicMerge actor.
     */
    private class MergeDirector extends PNDirector {
        public MergeDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        // Return false since this director has nothing to do
        // in its iteration loop, unless pause has been requested,
        // in which case we need to be able to iterate again to
        // wake up paused actors.
        public boolean postfire() {
            // FIXME: The problem with this is that threads do
            // not get re-awakened after a pause because that
            // is apparently in done in prefire() of the
            // director, which will never again be invoked.
            // Can we check here whether postfire() is
            // being called because we are pausing?
            if (_debugging) {
                _debug("Called postfire().");
            }

            if (_stopFireRequested && !_stopRequested) {
                if (_debugging) {
                    _debug("postfire() returns true.");
                }

                return true;
            }

            // FIXME: This is going to return false immediately
            // because fire() returns immediately.
            if (_debugging) {
                _debug("postfire() returns false.");
            }

            return false;
        }

        // Override this to notify the containing director only.
        // This local director does not keep a count of active
        // actors. It delegates this to the enclosing director.
        protected void _actorHasRestarted() {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director)._actorHasRestarted();
            }
        }

        // Override this to notify the containing director as well.
        // This local director does not keep a count of active
        // actors. It delegates this to the enclosing director.
        protected void _actorHasStopped() {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director)._actorHasStopped();
            }
        }

        // Override this to notify the containing director as well.
        // This local director does not keep a count of active
        // actors. It delegates this to the enclosing director.
        protected void _decreaseActiveCount() {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director)._decreaseActiveCount();
            }
        }

        // Override this to notify the containing director as well.
        // This local director does not keep a count of active
        // actors. It delegates this to the enclosing director.
        protected void _increaseActiveCount() {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director)._increaseActiveCount();
            }
        }

        // Override since deadlock cannot ever occur internally.
        protected boolean _resolveDeadlock() {
            if (_debugging) {
                _debug("Deadlock is not real as "
                        + "NondeterministicMerge can't deadlock.");
            }

            return true;
        }
    }
}
