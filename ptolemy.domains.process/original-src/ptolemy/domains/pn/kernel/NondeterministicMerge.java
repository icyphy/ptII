/* A nondeterministic merge actor for PN.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.process.ProcessReceiver;
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
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// NondeterministicMerge

/**
 This actor takes any number of input streams and merges them
 nondeterministically.  This actor is intended for use in the
 PN domain. It is a composite actor that
 creates its own contents.  It contains an instance of PNDirector and one
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
        _constructor();
    }

    /** Construct a TypedCompositeActor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.  You should set the local director or
     *  executive director before attempting to send data to the actor
     *  or to execute it. Add the actor to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     *  @exception NameDuplicationException If an actor
     *   with an identical name already exists in the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public NondeterministicMerge(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        // Added for the sake of Kepler's KAR handling, which needs this
        // constructor to instantiate composite actors.
        super(workspace);
        _constructor();
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
    @Override
    public void connectionsChanged(Port port) {
        super.connectionsChanged(port);

        if (port == input) {
            /* The reason why we delay the execution of _reinitializeInnerActors:
             *          What happens is that the NondeterministicMerge will call
             *          getWidth in its connectionsChanged method, this will cause
             *          IORelation to request a token from the width Parameter,
             *          which will trigger IORelation.attributeChanged (expressions
             *          are always lazy, which gives unexpected behavior with the
             *          attributeChanged mechanism).
             *          (Before the cached version of the width was being used at
             *          this moment, which resulted in the wrong value being used
             *          (this was a very old bug).)
             *          The call of IORelation.attributeChanged will set the
             *          cached width, which results in IOPort.attributeChanged
             *          being called, which calls NondeterministicMerge.connectionsChanged
             *          (again!). This one sees that the component has not added yet
             *          and does so. Finally the functions all return and we end up
             *          in the first NondeterministicMerge.connectionsChanged
             *          again. At the time it called getWidth it knew it had to add
             *          the component and does so, but in between this was already
             *          done by the second NondeterministicMerge.connectionsChanged,
             *          which results in the exception.
             *          When I move the code that triggers the width and
             *          adds to new actors to the initialize method of the
             *          NondeterministicMerge the model
             *          (ptolemy/domains/pn/demo/BrockAckerman/BrockAckerman.xml) runs
             *          again, but I'm reluctant to do so, since it might mess up the
             *          initialization process.
             *          Moving the code to preinitialize has the disadvantage however
             *          that width inference might happen multiple times (and definitely
             *          will for certain type of models).
             */
            // If the model is running, create new internal actors if needed.
            Manager manager = getManager();
            if (manager != null) {
                Manager.State managerState = manager.getState();
                if (managerState == Manager.ITERATING
                        || managerState == Manager.PAUSED
                        || managerState == Manager.PAUSED_ON_BREAKPOINT) {
                    _reinitializeInnerActors();
                }
            }
        }
    }

    /** Initialize this actor.
     *  @exception IllegalActionException If the parent class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        _reinitializeInnerActors();

        // super.initialize(); will initialize the director of this
        // composite actor (the MergeDirector), which will initialize the
        // newly created actors
        super.initialize();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. This overrides
     *  the base class to set instantiate a new MergeDirector,
     *  @param workspace The workspace for the new object.
     *  @return A new NamedObj.
     *  @exception CloneNotSupportedException If any of the attributes
     *   cannot be cloned.
     *  @see #exportMoML(Writer, int, String)
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        NondeterministicMerge result = (NondeterministicMerge) super
                .clone(workspace);
        try {
            // Remove the old inner MergeDirector(s) that is(are) in the wrong workspace.
            String mergeDirectorName = null;
            Iterator mergeDirectors = result.attributeList(MergeDirector.class)
                    .iterator();
            while (mergeDirectors.hasNext()) {
                MergeDirector oldMergeDirector = (MergeDirector) mergeDirectors
                        .next();
                if (mergeDirectorName == null) {
                    mergeDirectorName = oldMergeDirector.getName();
                }
                oldMergeDirector.setContainer(null);
            }

            // Create a new MergeDirector that is in the right workspace.
            MergeDirector mergeDirector = result.new MergeDirector(workspace);
            mergeDirector.setContainer(result);
            if (mergeDirectorName != null) {
                mergeDirector.setName(mergeDirectorName);
            }
        } catch (Throwable throwable) {
            throw new CloneNotSupportedException("Could not clone: "
                    + throwable);
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Construct a NondeterministicMerge. */
    private void _constructor() throws NameDuplicationException,
    IllegalActionException {

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

        PNDirector director = new MergeDirector(workspace());
        director.setContainer(this);
        director.setName("director");
    }

    /** Create the contained actors to handle the inputs.
     */
    private void _reinitializeInnerActors() {
        List<?> containedActors = entityList();
        int numberOfContainedActors = containedActors.size();

        // Create the contained actors to handle the inputs.
        int inputWidth;
        try {
            inputWidth = input.getWidth();
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "At this time IllegalActionExceptions are not allowed to happen.\n"
                            + "Width inference should already have been done.");
        }

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

                // If there is no director, then the model cannot be running,
                // so there is no need to notify.
                if (director != null) {
                    synchronized (director) {
                        director.notifyAll();
                    }
                }
            } else {
                try {
                    /*Actor localActor =*/new ChannelActor(i, this);

                    // NOTE: Probably don't want this overhead.
                    // ((NamedObj)localActor).addDebugListener(this);
                } catch (KernelException e) {
                    throw new InternalErrorException(e);
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
        public ChannelActor(int index, NondeterministicMerge container)
                throws IllegalActionException, NameDuplicationException {
            super(container, "ChannelActor" + index);
            _channelIndex = index;
            _channelValue = new IntToken(_channelIndex);
        }

        // Override the base class to not export anything.
        @Override
        public void exportMoML(Writer output, int depth, String name) {
        }

        @Override
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
                // NOTE: synchronizing is neither allowed
                // nor necessary here. See workspace().wait(Object).
                // synchronized (this) {
                try {
                    workspace().wait(this);
                } catch (InterruptedException ex) {
                    // Ignore and continue executing.
                }
                // }
            }
        }

        // Override to return the manager associate with the host.
        @Override
        public Manager getManager() {
            return NondeterministicMerge.this.getManager();
        }

        private int _channelIndex;

        private IntToken _channelValue;
    }

    /** Variant of the PNDirector for the NondeterministicMerge actor.
     */
    private class MergeDirector extends PNDirector {
        /** Construct an MergeDirector in the specified workspace with
         *  no container and an empty string as a name. You can then change
         *  the name with setName(). If the workspace argument is null, then
         *  use the default workspace.  You should set the local director or
         *  executive director before attempting to send data to the actor
         *  or to execute it. Add the actor to the workspace directory.
         *  Increment the version number of the workspace.
         *  @param workspace The workspace that will list the actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public MergeDirector(Workspace workspace)
                throws IllegalActionException, NameDuplicationException {
            super(workspace);
            setPersistent(false);
        }

        /** Queue an initialization request with the manager.
         *  The specified actor will be initialized at an appropriate time,
         *  between iterations, by calling its preinitialize() and initialize()
         *  methods. This method is called by CompositeActor when an actor
         *  sets its container to that composite actor.  Typically, that
         *  will occur when a model is first constructed, and during the
         *  execute() method of a ChangeRequest.
         *  We do nothing here in this implementation:
         *  When these actors are added during the initialization phase
         *  setContainer results in the call of this method, which will
         *  requestInitialization, which will normally delegate the action
         *  to the Manager.
         *  super.initialize() in NondeterministicMerge will however
         *  initialize the director of this
         *  composite actor (the MergeDirector), which will initialize the
         *  newly created actors. Hence we don't need to do it again here.
         *  @param actor The actor to initialize.
         */
        @Override
        public void requestInitialization(Actor actor) {
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         */
        @Override
        public synchronized void addThread(Thread thread) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).addThread(thread);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Do nothing.
         */
        @Override
        public void fire() {
            // Do not call super.fire() here because ProcessDirector.fire()
            // waits until a deadlock is detected, which we don't want to do.
        }

        /** Return false since this director has nothing to do.
         *  @return False.
         */
        @Override
        public boolean postfire() {
            return false;
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         */
        @Override
        public synchronized void removeThread(Thread thread) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).removeThread(thread);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         *  @param receiver The receiver handling the I/O operation,
         *   or null if it is not a specific receiver.
         *  @see #threadBlocked(Thread, ProcessReceiver, boolean)
         */
        @Override
        public synchronized void threadBlocked(Thread thread,
                ProcessReceiver receiver) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).threadBlocked(thread, receiver);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         *  @param receiver The receiver handling the I/O operation,
         *   or null if it is not a specific receiver.
         *  @param readOrWrite Either READ_BLOCKED or WRITE_BLOCKED
         *   to indicate whether the thread is blocked on read or write.
         *  @see #threadBlocked(Thread, ProcessReceiver)
         */
        @Override
        public synchronized void threadBlocked(Thread thread,
                ProcessReceiver receiver, boolean readOrWrite) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).threadBlocked(thread, receiver,
                        readOrWrite);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         */
        @Override
        public synchronized void threadHasPaused(Thread thread) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).threadHasPaused(thread);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         */
        @Override
        public synchronized void threadHasResumed(Thread thread) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).threadHasResumed(thread);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         *  @param receiver The receiver handling the I/O operation,
         *   or null if it is not a specific receiver.
         *  @see #threadBlocked(Thread, ProcessReceiver)
         */
        @Override
        public synchronized void threadUnblocked(Thread thread,
                ProcessReceiver receiver) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).threadUnblocked(thread, receiver);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Override the base class to delegate to the executive director.
         *  This director does not keep track of threads.
         *  @param thread The thread.
         *  @param receiver The receiver handling the I/O operation,
         *   or null if it is not a specific receiver.
         *  @param readOrWrite Either READ_BLOCKED or WRITE_BLOCKED
         *   to indicate whether the thread is blocked on read or write.
         *  @see #threadBlocked(Thread, ProcessReceiver, boolean)
         */
        @Override
        public synchronized void threadUnblocked(Thread thread,
                ProcessReceiver receiver, boolean readOrWrite) {
            Director director = getExecutiveDirector();

            if (director instanceof PNDirector) {
                ((PNDirector) director).threadUnblocked(thread, receiver,
                        readOrWrite);
            } else {
                throw new InternalErrorException(
                        "NondeterministicMerge actor can only execute"
                                + " under the control of a PNDirector!");
            }
        }

        /** Do nothing.
         */
        @Override
        public void wrapup() {
        }

        // Override since deadlock cannot ever occur internally.
        @Override
        protected boolean _resolveDeadlock() {
            if (_debugging) {
                _debug("Deadlock is not real as "
                        + "NondeterministicMerge can't deadlock.");
            }

            return true;
        }
    }
}
