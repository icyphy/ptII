/* A set of utilities for handling GTEntities.

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

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gt.ingredients.criteria.SubclassCriterion;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.EditorIcon;

/**
 A set of utilities for handling GTEntities (instances of {@link GTEntity}).

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTEntityUtils {

    /** For each port of the given entity, if the port's derived level is
     *  greater than 0 (i.e., it is created automatically by the entity), store
     *  the persistent attributes of the port in a "port" XML element.
     *
     *  @param entity The entity whose ports are looked at.
     *  @param output The output writer.
     *  @param depth The depth for the MoML output.
     *  @exception IOException If the output writer cannot be written to.
     */
    public static void exportPortProperties(GTEntity entity, Writer output,
            int depth) throws IOException {
        if (entity instanceof Entity) {
            Entity ptEntity = (Entity) entity;
            for (Object portObject : ptEntity.portList()) {
                Port port = (Port) portObject;
                if (port.getDerivedLevel() == 0) {
                    continue;
                }
                boolean outputStarted = false;
                for (Object attributeObject : port.attributeList()) {
                    Attribute attribute = (Attribute) attributeObject;
                    if (attribute.isPersistent()) {
                        if (!outputStarted) {
                            output.write(StringUtilities.getIndentPrefix(depth)
                                    + "<port name=\"" + port.getName()
                                    + "\">\n");
                            outputStarted = true;
                        }
                        attribute.exportMoML(output, depth + 1);
                    }
                }
                if (outputStarted) {
                    output.write(StringUtilities.getIndentPrefix(depth)
                            + "</port>\n");
                }
            }
        }
    }

    /** Update the appearance (icons and ports) of the entity with the change in
     *  a GTIngredientAttribute, such as a criterion or an operation.
     *
     *  @param entity The entity.
     *  @param attribute The attribute whose recent change leads to an update of
     *   the entity.
     */
    public static void updateAppearance(final GTEntity entity,
            GTIngredientsAttribute attribute) {

        NamedObj object = (NamedObj) entity;
        Workspace workspace = object.workspace();
        try {
            workspace.getWriteAccess();

            List<?> icons = object.attributeList(EditorIcon.class);
            boolean foundPersistentIcon = false;
            for (Object iconObject : icons) {
                EditorIcon icon = (EditorIcon) iconObject;
                if (icon.isPersistent()) {
                    foundPersistentIcon = true;
                    break;
                }
            }

            Set<String> preservedPortNames = new HashSet<String>();
            boolean isIconSet = false;
            int i = 1;
            GTIngredientList list = attribute.getIngredientList();
            for (GTIngredient ingredient : list) {
                if (ingredient instanceof PortCriterion) {
                    PortCriterion criterion = (PortCriterion) ingredient;
                    String portID = criterion.getPortID(list);
                    preservedPortNames.add(portID);

                    TypedIOPort port = (TypedIOPort) ((ComponentEntity) entity)
                            .getPort(portID);
                    boolean isInput = criterion.isInput();
                    boolean isOutput = criterion.isOutput();
                    boolean isMultiport = !criterion.isMultiportEnabled()
                            || criterion.isMultiport();
                    if (port != null) {
                        if (port instanceof PortMatcher) {
                            port.setInput(isInput);
                            port.setOutput(isOutput);
                        } else {
                            MoMLChangeRequest request = new MoMLChangeRequest(
                                    entity, object, "<deletePort name=\""
                                            + port.getName() + "\"/>");
                            request.setUndoable(true);
                            request.setMergeWithPreviousUndo(true);
                            request.execute();
                            port = new PortMatcher(criterion,
                                    (ComponentEntity) entity, portID, isInput,
                                    isOutput);
                            port.setDerivedLevel(1);
                        }
                    } else {
                        port = new PortMatcher(criterion,
                                (ComponentEntity) entity, portID, isInput,
                                isOutput);
                        port.setDerivedLevel(1);
                    }
                    port.setMultiport(isMultiport);
                } else if (ingredient instanceof SubclassCriterion
                        && !isIconSet && !foundPersistentIcon) {
                    SubclassCriterion criterion = (SubclassCriterion) ingredient;
                    final String superclass = criterion.getSuperclass();
                    object.requestChange(new ChangeRequest(entity,
                            "Deferred load actor icon action.") {
                        @Override
                        protected void _execute() throws Exception {
                            _loadActorIcon(entity, superclass);
                        }
                    });
                    isIconSet = true;
                }
                i++;
            }
            if (!isIconSet && !foundPersistentIcon) {
                object.requestChange(new RestoreAppearanceChangeRequest(entity));
            }

            ComponentEntity component = (ComponentEntity) entity;
            try {
                component.workspace().getReadAccess();
                List<?> portList = new LinkedList<Object>(component.portList());
                for (i = 0; i < portList.size(); i++) {
                    Port port = (Port) portList.get(i);
                    if (port instanceof PortMatcher
                            && !preservedPortNames.contains(port.getName())) {
                        ((PortMatcher) port)._setPortCriterion(null);
                        port.setContainer(null);
                    }
                }
            } finally {
                component.workspace().doneReading();
            }

        } catch (KernelException e) {
            throw new KernelRuntimeException(e, "Cannot update appearance for "
                    + "actor " + entity.getName() + ".");

        } finally {
            workspace.doneWriting();
        }
    }

    /** React to change of a settable contained in the entity and update the
     *  entity's appearance.
     *
     *  @param entity The entity that contains the settable.
     *  @param settable The settable whose value is changed.
     *  @see ptolemy.kernel.util.ValueListener#valueChanged(Settable)
     */
    public static void valueChanged(GTEntity entity, Settable settable) {
        GTIngredientsAttribute criteria = entity.getCriteriaAttribute();

        if (settable == criteria) {
            if (GTTools.isInPattern((NamedObj) entity)) {
                // criteria attribute is used to set the matching criteria for
                // this actor. It is used only for actors in the pattern of
                // a transformation rule. If the actor is in the
                // replacement, this attribute is ignored.
                entity.updateAppearance(criteria);

                // Update the appearance of corresponding entities in the
                // replacement.
                Pattern pattern = (Pattern) GTTools
                        .getContainingPatternOrReplacement((NamedObj) entity);
                NamedObj container = pattern.getContainer();
                if (container instanceof TransformationRule) {
                    Replacement replacement = ((TransformationRule) container)
                            .getReplacement();
                    replacement.updateEntitiesAppearance(criteria);
                }
            }
        }
    }

    /** Load the default icon of the actor specified in the class with name
     *  actorClassName, and set the entity with that icon.
     *
     *  @param entity The entity whose icon is to be set.
     *  @param actorClassName The class name of the actor for loading the
     *   default icon.
     *  @exception Exception If the icon cannot be set for the entity.
     */
    private static void _loadActorIcon(GTEntity entity, String actorClassName)
            throws Exception {
        CompositeActor container = new CompositeActor();
        String moml = "<group><entity name=\"NewActor\" class=\""
                + actorClassName + "\"/></group>";

        boolean isModified = MoMLParser.isModified();
        try {
            new MoMLChangeRequest(entity, container, moml).execute();
            new LoadActorIconChangeRequest(entity, container).execute();
        } catch (Throwable t) {
            if (_removeEditorIcons(entity)) {
                _setIconDescription(entity, entity.getDefaultIconDescription());
            }
        } finally {
            MoMLParser.setModified(isModified);
        }
    }

    /** Remove the EditorIcons defined for the entity.
     *
     *  @param entity The entity.
     *  @return true if at least one EditorIcon is found and removed; false
     *   otherwise.
     *  @exception KernelException If error occurs while removing the
     *   EditorIcons.
     */
    private static boolean _removeEditorIcons(GTEntity entity)
            throws KernelException {
        NamedObj object = (NamedObj) entity;
        boolean foundPersistentIcon = false;
        try {
            object.workspace().getReadAccess();
            for (Object iconObject : object.attributeList(EditorIcon.class)) {
                EditorIcon icon = (EditorIcon) iconObject;
                if (icon.isPersistent()) {
                    foundPersistentIcon = true;
                } else {
                    icon.setContainer(null);
                }
            }
        } finally {
            object.workspace().doneReading();
        }
        return !foundPersistentIcon;
    }

    /** Set the icon description for the entity.
     *
     *  @param entity The entity.
     *  @param iconDescription The icon description.
     *  @exception Exception If an icon description cannot be created, or it
     *   cannot be associated with the entity.
     */
    private static void _setIconDescription(GTEntity entity,
            String iconDescription) throws Exception {
        SingletonConfigurableAttribute description = new SingletonConfigurableAttribute(
                (NamedObj) entity, "_iconDescription");
        description.configure(null, null, iconDescription);
    }

    ///////////////////////////////////////////////////////////////////
    //// LoadActorIconChangeRequest

    /**
     A change request to copy the icon of a newly created entity within a
     container to a given GTEntity.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class LoadActorIconChangeRequest extends ChangeRequest {

        /** Construct a change request to copy the icon of a newly created
         *  entity within a container to a given GTEntity.
         *
         *  @param entity The entity whose icon is to be set.
         *  @param container The container of a single entity inside, which has
         *   the icon to be copied.
         */
        public LoadActorIconChangeRequest(GTEntity entity,
                CompositeEntity container) {
            super(container, "Load the icon of the newly created actor");

            _entity = entity;
            _container = container;
        }

        /** Execute the change request by retrieving the icon description or
         *  EditorIcon of the single entity in the container given in the
         *  constructor and copying it to the entity given in the constructor.
         *
         *  @exception Exception If the icon cannot be retrieved, or it cannot
         *   be associated with the entity.
         */
        @Override
        protected void _execute() throws Exception {
            ComponentEntity actor;
            try {
                _container.workspace().getReadAccess();
                actor = (ComponentEntity) _container.entityList().get(0);
            } finally {
                _container.workspace().doneReading();
            }

            if (!_removeEditorIcons(_entity)) {
                return;
            }

            ConfigurableAttribute actorAttribute = (ConfigurableAttribute) actor
                    .getAttribute("_iconDescription");
            String iconDescription = actorAttribute.getConfigureText();
            _setIconDescription(_entity, iconDescription);

            try {
                actor.workspace().getReadAccess();
                List<?> editorIconList = actor.attributeList(EditorIcon.class);
                for (Object editorIconObject : editorIconList) {
                    EditorIcon editorIcon = (EditorIcon) editorIconObject;
                    EditorIcon icon = (EditorIcon) editorIcon
                            .clone(((NamedObj) _entity).workspace());
                    icon.setName("_icon");
                    EditorIcon oldIcon = (EditorIcon) ((NamedObj) _entity)
                            .getAttribute("_icon");
                    if (oldIcon != null) {
                        oldIcon.setContainer(null);
                    }
                    icon.setContainer((NamedObj) _entity);
                    icon.setPersistent(false);
                    break;
                }
            } finally {
                actor.workspace().doneReading();
            }
        }

        /** The container of a single entity to retrieve the icon.
         */
        private CompositeEntity _container;

        /** The entity to which the icon is to be copied.
         */
        private GTEntity _entity;
    }

    ///////////////////////////////////////////////////////////////////
    //// RestoreAppearanceChangeRequest

    /**
     A change request to restore the default icon of an entity.

     @author Thomas Huining Feng
     @version $Id$
     @since Ptolemy II 7.1
     @Pt.ProposedRating Yellow (tfeng)
     @Pt.AcceptedRating Red (tfeng)
     */
    private static class RestoreAppearanceChangeRequest extends ChangeRequest {

        /** Execute the change request and restore the default icon for the
         *  entity.
         *
         *  @exception Exception The default icon cannot be set for the entity.
         */
        @Override
        protected void _execute() throws Exception {
            if (_removeEditorIcons(_entity)) {
                _setIconDescription(_entity,
                        _entity.getDefaultIconDescription());
            }
        }

        /** Construct a change request to restore the default icon for an
         *  entity.
         *
         *  @param entity The entity whose icon is to be restored.
         */
        RestoreAppearanceChangeRequest(GTEntity entity) {
            super(entity, "Restore the default appearance.");
            _entity = entity;
        }

        /** The entity whose icon is to be restored.
         */
        private GTEntity _entity;
    }
}
