/*

@Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.util.Time;
import ptolemy.actor.util.TimedEvent;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
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

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class ModelExecutor extends TypedAtomicActor {

    /**
     * @param container
     * @param name
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public ModelExecutor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        actorInput = new TypedIOPort(this, "actorInput", true, false);
        actorInput.setTypeEquals(ActorToken.TYPE);
        StringAttribute cardinal = new StringAttribute(actorInput, "_cardinal");
        cardinal.setExpression("SOUTH");
    }

    public void fire() throws IllegalActionException {
        Workspace workspace = new Workspace();
        Entity actor = ((ActorToken) actorInput.get(0)).getEntity(workspace);
        if (actor instanceof ComponentEntity) {
            ComponentEntity entity =
                (ComponentEntity) GTTools.cleanupModel(actor);
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

    public void initialize() throws IllegalActionException {
        super.initialize();

        Effigy parentEffigy = Configuration.findEffigy(toplevel());
        try {
            _wrapperEffigy = new PtolemyEffigy(parentEffigy,
                    parentEffigy.uniqueName("_wrapperEffigy"));
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Unable to create an " +
                    "effigy for the model.");
        }
    }

    public boolean prefire() throws IllegalActionException {
        return super.prefire() && actorInput.hasToken(0);
    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();

        try {
            _wrapperEffigy.setContainer(null);
        } catch (NameDuplicationException e) {
            throw new IllegalActionException(this, e, "Unexpected error.");
        }
        _wrapperEffigy = null;
    }

    public TypedIOPort actorInput;

    private PtolemyEffigy _wrapperEffigy;

    private class Wrapper extends TypedCompositeActor {

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
                                && executorPort.isInput()
                                        == entityPort.isInput()
                                && executorPort.isOutput()
                                        == entityPort.isOutput()
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
                            for (Object insideRelationObject
                                    : entityPort.insideRelationList()) {
                                TypedIORelation insideRelation =
                                    (TypedIORelation) insideRelationObject;
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

        Wrapper(Workspace workspace) throws IllegalActionException,
        NameDuplicationException {
            super(workspace);
            new WrapperDirector(this, "_director");
        }

        private class WrapperDirector extends Director {

            public WrapperDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
                super(container, name);
            }

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
                                for (int i = 0; i < port.getWidthInside();
                                        i++) {
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

            public void fireAt(Actor actor, Time time)
            throws IllegalActionException {
                _eventQueue.add(new TimedEvent(time, actor));
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
                return super.prefire() && (_hasToken()
                        || !_eventQueue.isEmpty());
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

            @SuppressWarnings("unchecked")
            private PriorityQueue<TimedEvent> _eventQueue =
                new PriorityQueue<TimedEvent>(1,
                        new TimedEvent.TimeComparator());
        }
    }

    private class WrapperPort extends TypedIOPort {

        public void broadcast(Token token) throws NoRoomException,
        IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            executorPort.broadcast(token);
        }

        public void broadcast(Token[] tokenArray, int vectorLength)
        throws NoRoomException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            executorPort.broadcast(tokenArray, vectorLength);
        }

        public Token get(int channelIndex) throws NoTokenException,
                IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            return executorPort.get(channelIndex);
        }

        public Token[] get(int channelIndex, int vectorLength)
                throws NoTokenException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            return executorPort.get(channelIndex, vectorLength);
        }

        public int getWidth() {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            return executorPort.getWidth();
        }

        public boolean hasToken(int channelIndex) throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            return executorPort.hasToken(channelIndex);
        }

        public boolean hasToken(int channelIndex, int tokens)
                throws IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            return executorPort.hasToken(channelIndex, tokens);
        }

        public void send(int channelIndex, Token token) throws NoRoomException,
        IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            executorPort.send(channelIndex, token);
        }

        public void send(int channelIndex, Token[] tokenArray, int vectorLength)
        throws NoRoomException, IllegalActionException {
            TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this.getPort(
                    getName());
            executorPort.send(channelIndex, tokenArray, vectorLength);
        }

        WrapperPort(Wrapper container, String name, boolean isInput,
                boolean isOutput)
        throws IllegalActionException, NameDuplicationException {
            super(container, name, isInput, isOutput);
        }
    }
}
