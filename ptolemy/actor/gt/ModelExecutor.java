/* An actor to execute the input model.

@Copyright (c) 2008-2014 The Regents of the University of California.
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

package ptolemy.actor.gt;

import java.util.List;
import java.util.PriorityQueue;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.domains.ptera.lib.EventUtils;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;

/**
 An actor to execute the input model.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelExecutor extends TypedAtomicActor {

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ModelExecutor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        actorInput = new TypedIOPort(this, "actorInput", true, false);
        actorInput.setTypeEquals(ActorToken.TYPE);
        StringAttribute cardinal = new StringAttribute(actorInput, "_cardinal");
        cardinal.setExpression("SOUTH");
    }

    /** Read the input token as a model, and execute it to completion.
     *
     *  @exception IllegalActionException If the input token cannot be read, or
     *   execution of the model throws an exception.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Workspace workspace = new Workspace();
        Entity actor = ((ActorToken) actorInput.get(0)).getEntity(workspace);
        if (actor instanceof ComponentEntity) {
            ComponentEntity entity = (ComponentEntity) GTTools.cleanupModel(
                    actor, workspace);
            workspace.remove(actor);
            try {
                Wrapper wrapper = new Wrapper(workspace);
                entity.setContainer(wrapper);
                _wrapperEffigy.setModel(wrapper);

                Manager manager = new Manager(workspace, "_manager");
                wrapper.setManager(manager);
                manager.execute();

                workspace.remove(wrapper);
            } catch (KernelException e) {
                throw new IllegalActionException(this, e, "Execution failed.");
            }
        }
    }

    /** Initialize this actor and create an effigy for executing models.
     *
     *  @exception IllegalActionException If thrown when creating the effigy or
     *   thrown by the superclass.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        Effigy parentEffigy = EventUtils.findToplevelEffigy(this);
        try {
            parentEffigy.workspace().getWriteAccess();
            _wrapperEffigy = new PtolemyEffigy(parentEffigy,
                    parentEffigy.uniqueName("_wrapperEffigy"));
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Unable to create an "
                    + "effigy for the model.");
        } finally {
            parentEffigy.workspace().doneWriting();
        }
    }

    /** Return true if there is any input token available.
     *
     *  @return true if there is any input token available.
     *  @exception IllegalActionException If availability of input tokens cannot
     *   be tested.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && actorInput.hasToken(0);
    }

    /** Wrap up this actor and delete the effigy for executing models.
     *
     *  @exception IllegalActionException If the superclass throws it, or if the
     *   effigy cannot be deleted.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        try {
            // When creating a JNLP file, wrapup() is called and
            // _wrapperEffigy might be null.
            if (_wrapperEffigy != null) {
                _wrapperEffigy.setContainer(null);
            }
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Unexpected error.");
        }
        _wrapperEffigy = null;
    }

    /** The actorInput port.
     */
    public TypedIOPort actorInput;

    /** The effigy to execute models at run time.
     */
    private PtolemyEffigy _wrapperEffigy;

    ///////////////////////////////////////////////////////////////////
    //// Wrapper

    /**
     A wrapper composite actor in which input models are executed.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class Wrapper extends TypedCompositeActor {

        /** At the end of adding an entity (a model to be executed) in this
         *  wrapper, create and link the ports of this wrapper to those of the
         *  added entity.
         *
         *  @param entity The entity.
         */
        @Override
        protected void _finishedAddEntity(ComponentEntity entity) {
            try {
                _workspace.getWriteAccess();
                List<?> entityPorts = entity.portList();
                List<?> executorPorts = ModelExecutor.this.portList();
                for (Object entityPortObject : entityPorts) {
                    if (!(entityPortObject instanceof TypedIOPort)) {
                        continue;
                    }

                    TypedIOPort entityPort = (TypedIOPort) entityPortObject;
                    TypedIOPort executorPort = null;
                    boolean found = false;
                    for (Object executorPortObject : executorPorts) {
                        if (!(executorPortObject instanceof TypedIOPort)) {
                            continue;
                        }
                        executorPort = (TypedIOPort) executorPortObject;
                        if (executorPort.getName().equals(entityPort.getName())
                                && executorPort.isInput() == entityPort
                                .isInput()
                                && executorPort.isOutput() == entityPort
                                .isOutput()
                                && entityPort.getType().isCompatible(
                                        executorPort.getType())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        continue;
                    }

                    try {
                        WrapperPort wrapperPort = new WrapperPort(this,
                                entityPort.getName(), entityPort.isInput(),
                                entityPort.isOutput());
                        wrapperPort.setTypeEquals(executorPort.getType());
                        TypedIORelation relation = new TypedIORelation(this,
                                uniqueName("relation"));
                        boolean multiport = entityPort.isMultiport();
                        wrapperPort.setMultiport(multiport);
                        if (multiport) {
                            int insideWidth = 0;
                            for (Object insideRelationObject : entityPort
                                    .insideRelationList()) {
                                TypedIORelation insideRelation = (TypedIORelation) insideRelationObject;
                                insideWidth += insideRelation.getWidth();
                            }
                            relation.setWidth(insideWidth);
                        }
                        wrapperPort.link(relation);
                        entityPort.link(relation);
                    } catch (KernelException e) {
                        throw new InternalErrorException(e);
                    }
                }
            } finally {
                _workspace.doneWriting();
            }
        }

        /** Construct a wrapper composite actor in the specified workspace with
         *  no container and an empty string as a name. You can then change
         *  the name with setName(). If the workspace argument is null, then
         *  use the default workspace.  You should set the local director or
         *  executive director before attempting to send data to the actor
         *  or to execute it. Add the actor to the workspace directory.
         *  Increment the version number of the workspace.
         *
         *  @param workspace The workspace that will list the actor.
         *  @exception IllegalActionException If the name has a period in it, or
         *   the director is not compatible with the specified container, or if
         *   the time resolution parameter is malformed.
         *  @exception NameDuplicationException If the container already contains
         *   an entity with the specified name.
         */
        Wrapper(Workspace workspace) throws IllegalActionException,
        NameDuplicationException {
            super(workspace);
            new WrapperDirector(this, "_director");
        }

        //////////////////////////////////////////////////////////////////////////
        //// WrapperDirector

        /**
         The director to be used in the wrapper that handles requests from the
         directors of the models to be executed.

         @author Thomas Huining Feng
         @version $Id$
         @since Ptolemy II 8.0
         @Pt.ProposedRating Yellow (tfeng)
         @Pt.AcceptedRating Red (tfeng)
         */
        private class WrapperDirector extends Director {

            /** Construct a director in the given container with the given name.
             *  The container argument must not be null, or a
             *  NullPointerException will be thrown.
             *  If the name argument is null, then the name is set to the
             *  empty string. Increment the version number of the workspace.
             *  Create the timeResolution parameter.
             *
             *  @param container The container.
             *  @param name The name of this director.
             *  @exception IllegalActionException If the name has a period in it, or
             *   the director is not compatible with the specified container, or if
             *   the time resolution parameter is malformed.
             *  @exception NameDuplicationException If the container already contains
             *   an entity with the specified name.
             */
            public WrapperDirector(CompositeEntity container, String name)
                    throws IllegalActionException, NameDuplicationException {
                super(container, name);

                _eventQueue = new PriorityQueue<TimedEvent>(1,
                        new TimedEvent.TimeComparator());
            }

            /** Clone the object into the specified workspace. The new object is
             *  <i>not</i> added to the directory of that workspace (you must do this
             *  yourself if you want it there).
             *  The result is an attribute with no container.
             *  @param workspace The workspace for the cloned object.
             *  @exception CloneNotSupportedException Not thrown in this base class
             *  @return The new Attribute.
             */
            @Override
            public Object clone(Workspace workspace)
                    throws CloneNotSupportedException {
                WrapperDirector director = (WrapperDirector) super
                        .clone(workspace);
                director._eventQueue = new PriorityQueue<TimedEvent>(1,
                        new TimedEvent.TimeComparator());
                return director;
            }

            /** Fire the contained actor if there is any token available or if
             *  there is an request in the event queue. Transfer the outputs of
             *  the actor to the output ports of this wrapper.
             *
             *  @exception IllegalActionException If thrown when the actor is
             *   fired.
             */
            @Override
            public void fire() throws IllegalActionException {
                if (!_hasToken() && !_eventQueue.isEmpty()) {
                    TimedEvent timedEvent = _eventQueue.poll();
                    setModelTime(timedEvent.timeStamp);
                    Actor actor = (Actor) timedEvent.contents;
                    if (actor.prefire()) {
                        actor.fire();
                        actor.postfire();
                    }
                } else {
                    super.fire();
                }

                for (Object entityObject : entityList()) {
                    if (entityObject instanceof CompositeActor) {
                        CompositeActor actor = (CompositeActor) entityObject;
                        if (actor.isOpaque()) {
                            for (Object portObject : actor.outputPortList()) {
                                IOPort port = (IOPort) portObject;
                                // Here, do something similar to:
                                //   Director director = actor.getDirector();
                                //   director.transferOutputs(port);
                                // Cannot use transferOutputs because it raises
                                // exception if the tokens inside have already
                                // been transferred.
                                for (int i = 0; i < port.getWidthInside(); i++) {
                                    if (port.isKnownInside(i)
                                            && port.hasTokenInside(i)) {
                                        port.send(i, port.getInside(i));
                                    }
                                }
                            }
                        }
                    }
                }
                for (Object portObject : portList()) {
                    IOPort port = (IOPort) portObject;
                    if (port.isOutput()) {
                        for (int i = 0; i < port.getWidthInside(); i++) {
                            while (port.hasNewTokenInside(i)) {
                                Token token = port.getInside(i);
                                port.send(i, token);
                            }
                        }
                    }
                }
            }

            /** Handle a fireAt request from the contained actor by recording it
             *  in the event queue.
             *
             *  @param actor The actor that requests to be fired.
             *  @param time The time to fire the actor.
             *  @param microstep The microstep (ignored by this director).
             *  @return The time at which the actor will be fired.
             *  @exception IllegalActionException Not thrown in this class.
             */
            @Override
            public Time fireAt(Actor actor, Time time, int microstep)
                    throws IllegalActionException {
                _eventQueue.add(new TimedEvent(time, actor));
                return time;
            }

            /** Return the next time of interest in the model being executed by
             *  this director or the director of any enclosing model up the
             *  hierarchy. If this director is at the top level, then this
             *  default implementation simply returns the current time, since
             *  this director does not advance time. If this director is not
             *  at the top level, then return whatever the enclosing director
             *  returns.
             *  <p>
             *  This method is useful for domains that perform
             *  speculative execution (such as CT).  Such a domain in a hierarchical
             *  model (i.e. CT inside DE) uses this method to determine how far
             *  into the future to execute.
             *  <p>
             *  Derived classes should override this method to provide an appropriate
             *  value, if possible. For example, the DEDirector class returns the
             *  time value of the next event in the event queue.
             *  @return The time of the next iteration.
             *  @see #getModelTime()
             */
            @Override
            public Time getModelNextIterationTime() {
                Time aFutureTime = Time.POSITIVE_INFINITY;

                if (_eventQueue.size() > 0) {
                    aFutureTime = _eventQueue.peek().timeStamp;
                }

                return aFutureTime;
            }

            /** Return a new receiver of a type compatible with this director.
             *  In this class, this returns an instance of QueueReceiver.
             *  @return A new QueueReceiver.
             */
            @Override
            public Receiver newReceiver() {
                return new QueueReceiver();
            }

            /** Return false if the contained actor has finished executing.
             *
             *  @return false if the contained actor has finished executing;
             *   true otherwise.
             *  @exception IllegalActionException If the superclass throws it.
             */
            @Override
            public boolean postfire() throws IllegalActionException {
                boolean result = super.postfire();
                if (result && ModelExecutor.this._stopRequested) {
                    result = false;
                }
                if (result && !_hasToken() && _eventQueue.isEmpty()) {
                    result = false;
                }
                return result;
            }

            /** Return true if the event queue is not empty or there are input
             *  tokens.
             *
             *  @return true if the event queue is not empty or there are input
             *   tokens.
             *  @exception IllegalActionException If the availability of input
             *   tokens cannot be tested.
             */
            @Override
            public boolean prefire() throws IllegalActionException {
                return super.prefire()
                        && (_hasToken() || !_eventQueue.isEmpty());
            }

            /** Test whether there is an input token in any channel of any input
             *  port.
             *
             *  @return true if there is an input token; false otherwise.
             *  @exception IllegalActionException If the availability of input
             *   tokens cannot be tested.
             */
            private boolean _hasToken() throws IllegalActionException {
                boolean hasToken = false;
                for (Object portObject : portList()) {
                    IOPort port = (IOPort) portObject;
                    if (port.isInput()) {
                        for (int i = 0; i < port.getWidth(); i++) {
                            if (port.hasToken(i)) {
                                hasToken = true;
                                break;
                            }
                        }
                        if (hasToken) {
                            break;
                        }
                    }
                }
                return hasToken;
            }

            /** The event queue.
             */
            private PriorityQueue<TimedEvent> _eventQueue;
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// WrapperPort

    /**
     The port of the wrapper. The get and send methods delegate to the port with
     the same name of the model executor.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class WrapperPort extends TypedIOPort {

        /** Send a token to all connected receivers.
         *  Tokens are in general immutable, so each receiver is given a
         *  reference to the same token and no clones are made.
         *  The transfer is accomplished by calling getRemoteReceivers()
         *  to determine the number of channels with valid receivers and
         *  then calling send() on the appropriate channels.
         *  It would probably be faster to call put() directly on the receivers.
         *  If there are no destination receivers, then nothing is sent.
         *  If the port is not connected to anything, or receivers have not been
         *  created in the remote port, then just return.
         *  <p>
         *  Some of this method is read-synchronized on the workspace.
         *  Since it is possible for a thread to block while executing a put,
         *  it is important that the thread does not hold read access on
         *  the workspace when it is blocked. Thus this method releases
         *  read access on the workspace before calling put.
         *
         *  @param token The token to send
         *  @exception IllegalActionException If the token to be sent cannot
         *   be converted to the type of this port
         *  @exception NoRoomException If a send to one of the channels throws
         *     it.
         */
        @Override
        public void broadcast(Token token) throws NoRoomException,
        IllegalActionException {
            // super.broadcast() is not called because we want to send on
            // the executorPort, not on this port.

            // FIXME: This method does not call super.broadcast(),
            // which means that the port event listeners are not
            // notified for this port.
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.broadcast(token);
        }

        /** Send the specified portion of a token array to all receivers connected
         *  to this port. The first <i>vectorLength</i> tokens
         *  of the token array are sent.
         *  <p>
         *  Tokens are in general immutable, so each receiver
         *  is given a reference to the same token and no clones are made.
         *  If the port is not connected to anything, or receivers have not been
         *  created in the remote port, or the channel index is out of
         *  range, or the port is not an output port,
         *  then just silently return.  This behavior makes it
         *  easy to leave output ports unconnected when you are not interested
         *  in the output.  The transfer is accomplished
         *  by calling the vectorized put() method of the remote receivers.
         *  If the port is not connected to anything, or receivers have not been
         *  created in the remote port, then just return.
         *  <p>
         *  Some of this method is read-synchronized on the workspace.
         *  Since it is possible for a thread to block while executing a put,
         *  it is important that the thread does not hold read access on
         *  the workspace when it is blocked. Thus this method releases
         *  read access on the workspace before calling put.
         *
         *  @param tokenArray The token array to send
         *  @param vectorLength The number of elements of the token
         *   array to send.
         *  @exception NoRoomException If there is no room in the receiver.
         *  @exception IllegalActionException If the tokens to be sent cannot
         *   be converted to the type of this port
         */
        @Override
        public void broadcast(Token[] tokenArray, int vectorLength)
                throws NoRoomException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.broadcast(tokenArray, vectorLength);
        }

        /** Get a token from the specified channel.
         *  If the channel has a group with more than one receiver (something
         *  that is possible if this is a transparent port), then this method
         *  calls get() on all receivers, but returns only the first non-null
         *  token returned by these calls.
         *  Normally this method is not used on transparent ports.
         *  If there is no token to return, then throw an exception.
         *  <p>
         *  Some of this method is read-synchronized on the workspace.
         *  Since it is possible for a thread to block while executing a get,
         *  it is important that the thread does not hold read access on
         *  the workspace when it is blocked. Thus this method releases
         *  read access on the workspace before calling get().
         *
         *  @param channelIndex The channel index.
         *  @return A token from the specified channel.
         *  @exception NoTokenException If there is no token.
         *  @exception IllegalActionException If there is no director, and hence
         *   no receivers have been created, if the port is not an input port, or
         *   if the channel index is out of range.
         */
        @Override
        public Token get(int channelIndex) throws NoTokenException,
        IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.get(channelIndex);
        }

        /** Get an array of tokens from the specified channel. The
         *  parameter <i>channelIndex</i> specifies the channel and
         *  the parameter <i>vectorLength</i> specifies the number of
         *  valid tokens to get in the returned array. The length of
         *  the returned array will be equal to <i>vectorLength</i>.
         *  <p>
         *  If the channel has a group with more than one receiver (something
         *  that is possible if this is a transparent port), then this method
         *  calls get() on all receivers, but returns only the result from
         *  the first in the group.
         *  Normally this method is not used on transparent ports.
         *  If there are not enough tokens to fill the array, then throw
         *  an exception.
         *  <p>
         *  Some of this method is read-synchronized on the workspace.
         *  Since it is possible for a thread to block while executing a get,
         *  it is important that the thread does not hold read access on
         *  the workspace when it is blocked. Thus this method releases
         *  read access on the workspace before calling get.
         *
         *  @param channelIndex The channel index.
         *  @param vectorLength The number of valid tokens to get in the
         *   returned array.
         *  @return A token array from the specified channel containing
         *   <i>vectorLength</i> valid tokens.
         *  @exception NoTokenException If there is no array of tokens.
         *  @exception IllegalActionException If there is no director, and hence
         *   no receivers have been created, if the port is not an input port, or
         *   if the channel index is out of range.
         */
        @Override
        public Token[] get(int channelIndex, int vectorLength)
                throws NoTokenException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.get(channelIndex, vectorLength);
        }

        /** Return the width of the port.  The width is the sum of the
         *  widths of the relations that the port is linked to (on the outside).
         *  Note that this method cannot be used to determine whether a port
         *  is connected (deeply) to another port that can either supply it with
         *  data or consume data it produces.  The correct methods to use to
         *  determine that are numberOfSinks() and numberOfSources().
         *  This method is read-synchronized on the workspace.
         *  This method will trigger the width inference algorithm if necessary.
         *  @see #numberOfSinks()
         *  @see #numberOfSources()
         *  @return The width of the port.
         *  @exception IllegalActionException If the width cannot be determined.
         */
        @Override
        public int getWidth() throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.getWidth();
        }

        /** Return true if the specified channel has a token to deliver
         *  via the get() method.  If this port is not an input, or if the
         *  channel index is out of range, then throw an exception.
         *  Note that this does not report any tokens in inside receivers
         *  of an output port. Those are accessible only through
         *  getInsideReceivers().
         *
         *  @param channelIndex The channel index.
         *  @return True if there is a token in the channel.
         *  @exception IllegalActionException If the receivers do not support
         *   this query, if there is no director, and hence no receivers,
         *   if the port is not an input port, or if the channel index is out
         *   of range.
         */
        @Override
        public boolean hasToken(int channelIndex) throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.hasToken(channelIndex);
        }

        /** Return true if the specified channel has the specified number
         *  of tokens to deliver via the get() method.
         *  If this port is not an input, or if the
         *  channel index is out of range, then throw an exception.
         *  Note that this does not report any tokens in inside receivers
         *  of an output port. Those are accessible only through
         *  getInsideReceivers().
         *
         *  @param channelIndex The channel index.
         *  @param tokens The number of tokens to query the channel for.
         *  @return True if there is a token in the channel.
         *  @exception IllegalActionException If the receivers do not support
         *   this query, if there is no director, and hence no receivers,
         *   if the port is not an input port, or if the channel index is out
         *   of range.
         */
        @Override
        public boolean hasToken(int channelIndex, int tokens)
                throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.hasToken(channelIndex, tokens);
        }

        /** Send a token to the specified channel, checking the type
         *  and converting the token if necessary.
         *  If the port is not connected to anything, or receivers have not been
         *  created in the remote port, or the channel index is out of
         *  range, or the port is not an output port,
         *  then just silently return.  This behavior makes it
         *  easy to leave output ports unconnected when you are not interested
         *  in the output.
         *  If the type of the specified token is the type of this
         *  port, or the token can be converted to that type
         *  losslessly, the token is sent to all receivers connected to the
         *  specified channel. Otherwise, IllegalActionException is thrown.
         *  Before putting the token into the destination receivers, this
         *  method also checks the type of the remote input port,
         *  and converts the token if necessary.
         *  The conversion is done by calling the
         *  convert() method of the type of the remote input port.
         *  <p>
         *  Some of this method is read-synchronized on the workspace.
         *  Since it is possible for a thread to block while executing a put,
         *  it is important that the thread does not hold read access on
         *  the workspace when it is blocked. Thus this method releases
         *  read access on the workspace before calling put.
         *
         *  @param channelIndex The index of the channel, from 0 to width-1.
         *  @param token The token to send.
         *  @exception IllegalActionException If the token to be sent cannot
         *   be converted to the type of this port, or if the token is null.
         *  @exception NoRoomException If there is no room in the receiver.
         */
        @Override
        public void send(int channelIndex, Token token) throws NoRoomException,
        IllegalActionException {
            // super.send() is not called because we want to send on
            // the executorPort, not on this port.

            // FIXME: This method does not call super.send(),
            // which means that the port event listeners are not
            // notified for this port.
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.send(channelIndex, token);
        }

        /** Send the specified portion of a token array to all receivers
         *  connected to the specified channel, checking the type
         *  and converting the token if necessary. The first
         *  <i>vectorLength</i> tokens of the token array are sent.
         *  If the port is not connected to anything, or receivers have not been
         *  created in the remote port, or the channel index is out of
         *  range, or the port is not an output port,
         *  then just silently return.  This behavior makes it
         *  easy to leave output ports unconnected when you are not interested
         *  in the output.
         *  <p>
         *  If the type of the tokens in the specified portion of the
         *  token array is the type of this
         *  port, or the tokens in the specified portion of the
         *  token array can be converted to that type
         *  losslessly, the tokens in the specified portion of the
         *  token array are sent to all receivers connected to the
         *  specified channel. Otherwise, IllegalActionException is thrown.
         *  Before putting the tokens in the specified portion of the
         *  token array into the destination receivers, this
         *  method also checks the type of the remote input port,
         *  and converts the tokens if necessary.
         *  The conversion is done by calling the
         *  convert() method of the type of the remote input port.
         *  <p>
         *  Some of this method is read-synchronized on the workspace.
         *  Since it is possible for a thread to block while executing a put,
         *  it is important that the thread does not hold read access on
         *  the workspace when it is blocked. Thus this method releases
         *  read access on the workspace before calling put.
         *
         *  @param channelIndex The index of the channel, from 0 to width-1
         *  @param tokenArray The token array to send
         *  @param vectorLength The number of elements of the token
         *   array to send.
         *  @exception NoRoomException If there is no room in the receiver.
         *  @exception IllegalActionException If the tokens to be sent cannot
         *   be converted to the type of this port, or if the <i>vectorLength</i>
         *   argument is greater than the length of the <i>tokenArray</i>
         *   argument.
         */
        @Override
        public void send(int channelIndex, Token[] tokenArray, int vectorLength)
                throws NoRoomException, IllegalActionException {
            // super.send() is not called because we want to send on
            // the executorPort, not on this port.

            // FIXME: This method does not call super.send(),
            // which means that the port event listeners are not
            // notified for this port.
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.send(channelIndex, tokenArray, vectorLength);
        }

        /** Construct a TypedIOPort with a container and a name that is
         *  either an input, an output, or both, depending on the third
         *  and fourth arguments. The specified container must implement
         *  the TypedActor interface or an exception will be thrown.
         *
         *  @param container The container actor.
         *  @param name The name of the port.
         *  @param isInput True if this is to be an input port.
         *  @param isOutput True if this is to be an output port.
         *  @exception IllegalActionException If the port is not of an acceptable
         *   class for the container, or if the container does not implement the
         *   TypedActor interface.
         *  @exception NameDuplicationException If the name coincides with
         *   a port already in the container.
         */
        WrapperPort(Wrapper container, String name, boolean isInput,
                boolean isOutput) throws IllegalActionException,
                NameDuplicationException {
            super(container, name, isInput, isOutput);
        }
    }
}
