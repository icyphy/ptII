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

import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.ActorToken;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
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
    }

    public void fire() throws IllegalActionException {
        Entity actor = ((ActorToken) actorInput.get(0)).getEntity();
        if (actor instanceof ComponentEntity) {
            ComponentEntity entity = (ComponentEntity) actor;
            Workspace workspace = actor.workspace();
            try {
                Wrapper wrapper = new Wrapper(workspace);
                Effigy parentEffigy = Configuration.findEffigy(toplevel());
                PtolemyEffigy effigy = new PtolemyEffigy(parentEffigy,
                        parentEffigy.uniqueName("wrapperEffigy"));
                effigy.setModel(wrapper);
                entity.setContainer(wrapper);

                Manager manager = new Manager(workspace, "_manager");
                wrapper.setManager(manager);
                manager.execute();
            } catch (KernelException e) {
                throw new IllegalActionException(this, e, "Execution failed.");
            }
        }
    }

    public boolean prefire() throws IllegalActionException {
        return super.prefire() && actorInput.hasToken(0);
    }

    public TypedIOPort actorInput;

    private class Wrapper extends TypedCompositeActor {

        public void initialize() throws IllegalActionException {
            super.initialize();
            _transferInputTokens();
        }

        public boolean postfire() {
            return false;
        }

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
            new Director(this, "_director");
        }

        private void _transferInputTokens() throws IllegalActionException {
            for (Object portObject : portList()) {
                WrapperPort port = (WrapperPort) portObject;
                TypedIOPort executorPort = (TypedIOPort) ModelExecutor.this
                        .getPort(port.getName());
                for (int i = 0; i < executorPort.getWidth(); i++) {
                    if (port.isInput()) {
                        while (executorPort.hasToken(i)) {
                            port.sendInside(i, executorPort.get(i));
                        }
                    }
                }
            }
        }
    }

    private class WrapperPort extends TypedIOPort {

        public void broadcast(Token token) throws NoRoomException,
        IllegalActionException {
            ((TypedIOPort) ModelExecutor.this.getPort(getName())).broadcast(
                    token);
        }

        public void broadcast(Token[] tokenArray, int vectorLength)
        throws NoRoomException, IllegalActionException {
            ((TypedIOPort) ModelExecutor.this.getPort(getName())).broadcast(
                    tokenArray, vectorLength);
        }

        public void send(int channelIndex, Token token) throws NoRoomException,
        IllegalActionException {
            ((TypedIOPort) ModelExecutor.this.getPort(getName())).send(
                    channelIndex, token);
        }

        public void send(int channelIndex, Token[] tokenArray, int vectorLength)
        throws NoRoomException, IllegalActionException {
            ((TypedIOPort) ModelExecutor.this.getPort(getName())).send(
                    channelIndex, tokenArray, vectorLength);
        }

        WrapperPort(Wrapper container, String name, boolean isInput,
                boolean isOutput)
        throws IllegalActionException, NameDuplicationException {
            super(container, name, isInput, isOutput);
        }
    }
}
