/* An actor to execute the input model.

@Copyright (c) 2008-2009 The Regents of the University of California.
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
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
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
    public void fire() throws IllegalActionException {
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
    public void initialize() throws IllegalActionException {
        super.initialize();

        Effigy parentEffigy = EventUtils.findToplevelEffigy(this);
        try {
            parentEffigy.workspace().getWriteAccess();
            _wrapperEffigy = new PtolemyEffigy(parentEffigy, parentEffigy
                    .uniqueName("_wrapperEffigy"));
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
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && actorInput.hasToken(0);
    }

    /** Wrap up this actor and delete the effigy for executing models.
     *
     *  @exception IllegalActionException If the superclass throws it, or if the
     *   effigy cannot be deleted.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();

        try {
            _wrapperEffigy.setContainer(null);
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

    //////////////////////////////////////////////////////////////////////////
    //// Wrapper

    /**
     A wrapper composite actor in which input models are executed.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 8.0
     @Pt.ProposedRating Red (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private class Wrapper extends TypedCompositeActor {

        /** At the end of adding an entity (a model to be executed) in this
         *  wrapper, create and link the ports of this wrapper to those of the
         *  added entity.
         *
         *  @param entity The entity.
         */
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
         @Pt.ProposedRating Red (tfeng)
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
                                // been transfered.
                                for (int i = 0; i < port.getWidthInside(); i++) {
                                    if (port.hasTokenInside(i)) {
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
                            while (port.hasTokenInside(i)) {
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
             *  @return The time at which the actor will be fired.
             *  @exception IllegalActionException Not thrown in this class.
             */
            public Time fireAt(Actor actor, Time time)
                    throws IllegalActionException {
                _eventQueue.add(new TimedEvent(time, actor));
                return time;
            }

            public Time getModelNextIterationTime() {
                Time aFutureTime = Time.POSITIVE_INFINITY;

                if (_eventQueue.size() > 0) {
                    aFutureTime = _eventQueue.peek().timeStamp;
                }

                return aFutureTime;
            }

            public Receiver newReceiver() {
                return new QueueReceiver();
            }

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

            public boolean prefire() throws IllegalActionException {
                return super.prefire()
                        && (_hasToken() || !_eventQueue.isEmpty());
            }

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

            private PriorityQueue<TimedEvent> _eventQueue;
        }
    }

    private class WrapperPort extends TypedIOPort {

        public void broadcast(Token token) throws NoRoomException,
                IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.broadcast(token);
        }

        public void broadcast(Token[] tokenArray, int vectorLength)
                throws NoRoomException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.broadcast(tokenArray, vectorLength);
        }

        public Token get(int channelIndex) throws NoTokenException,
                IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.get(channelIndex);
        }

        public Token[] get(int channelIndex, int vectorLength)
                throws NoTokenException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.get(channelIndex, vectorLength);
        }

        public int getWidth() throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.getWidth();
        }

        public boolean hasToken(int channelIndex) throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.hasToken(channelIndex);
        }

        public boolean hasToken(int channelIndex, int tokens)
                throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            return executorPort.hasToken(channelIndex, tokens);
        }

        public void send(int channelIndex, Token token) throws NoRoomException,
                IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.send(channelIndex, token);
        }

        public void send(int channelIndex, Token[] tokenArray, int vectorLength)
                throws NoRoomException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                    .getPort(getName());
            executorPort.send(channelIndex, tokenArray, vectorLength);
        }

        WrapperPort(Wrapper container, String name, boolean isInput,
                boolean isOutput) throws IllegalActionException,
                NameDuplicationException {
            super(container, name, isInput, isOutput);
        }
    }
}
