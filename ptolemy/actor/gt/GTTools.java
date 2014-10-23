/* A set of tools for model transformation.

@Copyright (c) 2007-2014 The Regents of the University of California.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

//////////////////////////////////////////////////////////////////////////
//// GTTools

/**
 A set of tools for model transformation.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTTools {

    /** Check the class of the container in which the attribute is to be placed.
     *  If the container is not an intended one, throw an
     *  IllegalActionException.
     *
     *  @param attribute The attribute to check.
     *  @param container The container.
     *  @param containerClass The intended class of container.
     *  @param deep Whether containers of the container should be checked
     *   instead, if the container does not qualify.
     *  @exception IllegalActionException If this attribute cannot be used with
     *   the given container.
     */
    public static void checkContainerClass(Attribute attribute,
            NamedObj container,
            Class<? extends CompositeEntity> containerClass, boolean deep)
                    throws IllegalActionException {
        while (deep && container != null
                && !containerClass.isInstance(container)
                && !(container instanceof EntityLibrary)) {
            container = container.getContainer();
            if (container instanceof EntityLibrary) {
                return;
            }
        }

        if (container == null || !containerClass.isInstance(container)
                && !(container instanceof EntityLibrary)) {
            _delete(attribute);
            throw new IllegalActionException(attribute.getClass()
                    .getSimpleName()
                    + " can only be added to "
                    + containerClass.getSimpleName() + ".");
        }
    }

    /** Check whether the attribute is unique in the given container.
     *
     *  @param attribute The attribute to check.
     *  @param container The container.
     *  @exception IllegalActionException If the container already has an
     *   attribute in the same class.
     */
    public static void checkUniqueness(Attribute attribute, NamedObj container)
            throws IllegalActionException {
        if (container instanceof EntityLibrary) {
            return;
        }

        try {
            container.workspace().getReadAccess();
            List<? extends Attribute> attributeList = container
                    .attributeList(attribute.getClass());
            for (Attribute existingAttribute : attributeList) {
                if (existingAttribute != attribute
                        && existingAttribute.isPersistent()) {
                    _delete(attribute);
                    throw new IllegalActionException("Only 1 "
                            + attribute.getClass().getSimpleName()
                            + " can be used.");
                }
            }
        } finally {
            container.workspace().doneReading();
        }
    }

    /** Create a copy of the given model in a new workspace that is cleaned up
     *  with no execution state left in it.
     *
     *  @param model The model to be copied.
     *  @return A cleaned up copy of the given model.
     *  @exception IllegalActionException If the model cannot be copied.
     */
    public static NamedObj cleanupModel(NamedObj model)
            throws IllegalActionException {
        return cleanupModel(model, new Workspace());
    }

    /** Create a copy of the given model with the given parser that is cleaned
     *  up with no execution state left in it.
     *
     *  @param model The model to be copied.
     *  @param parser The parser.
     *  @return A cleaned up copy of the given model.
     *  @exception IllegalActionException If the model cannot be copied.
     */
    public static NamedObj cleanupModel(NamedObj model, MoMLParser parser)
            throws IllegalActionException {
        try {
            URIAttribute uriAttribute = (URIAttribute) model.getAttribute(
                    "_uri", URIAttribute.class);
            NamedObj newModel;
            if (uriAttribute != null) {
                newModel = parser.parse(uriAttribute.getURL(),
                        model.exportMoML());
            } else {
                newModel = parser.parse(model.exportMoML());
            }
            return newModel;
        } catch (Exception e) {
            throw new IllegalActionException(model, e,
                    "Unable to clean up model.");
        }
    }

    /** Create a copy of the given model in the given workspace that is cleaned
     *  up with no execution state left in it.
     *
     *  @param model The model to be copied.
     *  @param workspace The workspace.
     *  @return A cleaned up copy of the given model.
     *  @exception IllegalActionException If the model cannot be copied.
     */
    public static NamedObj cleanupModel(NamedObj model, Workspace workspace)
            throws IllegalActionException {
        try {
            workspace.getReadAccess();
            return cleanupModel(model, new MoMLParser(workspace));
        } catch (Exception e) {
            throw new IllegalActionException(model, e,
                    "Unable to clean up model.");
        } finally {
            workspace.doneReading();
        }
    }

    /** Add an attribute to the given container and each of its children
     *  including ports, entities and relations, but not including attributes.
     *  The name of the attribute is automatically generated by putting an "_"
     *  before the simple class name of the attribute to be created.
     *
     *  @param container The container.
     *  @param attributeClass The attribute class.
     *  @exception InstantiationException If an attribute cannot be
     *   instantiated.
     *  @exception IllegalAccessException If the constructor of the attribute
     *   class is not accessible.
     *  @exception InvocationTargetException If the arguments to constructor is
     *   not valid.
     */
    public static void deepAddAttributes(NamedObj container,
            Class<? extends Attribute> attributeClass)
                    throws InstantiationException, IllegalAccessException,
                    InvocationTargetException {
        Constructor<?>[] constructors = attributeClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 2 && types[0].isInstance(container)
                    && types[1].equals(String.class)) {
                constructor.newInstance(
                        container,
                        container.uniqueName("_"
                                + attributeClass.getSimpleName()));
                break;
            }
        }
        for (Object child : getChildren(container, false, true, true, true)) {
            deepAddAttributes((NamedObj) child, attributeClass);
        }
    }

    /** Remove all the attributes in the given class from the given container
     *  and all of its children including ports, entities and relations, but not
     *  including attributes.
     *
     *  @param container The container.
     *  @param attributeClass The attribute class.
     *  @exception IllegalActionException If an attribute cannot be removed.
     */
    public static void deepRemoveAttributes(NamedObj container,
            Class<? extends Attribute> attributeClass)
                    throws IllegalActionException {
        List<Object> attributes = new LinkedList<Object>(
                container.attributeList(attributeClass));
        for (Object attribute : attributes) {
            try {
                ((Attribute) attribute).setContainer(null);
            } catch (NameDuplicationException e) {
                // This should not happen.
            }
        }
        for (Object child : getChildren(container, false, true, true, true)) {
            deepRemoveAttributes((NamedObj) child, attributeClass);
        }
    }

    /** Find an attribute in the object or its container (if searchContainers is
     *  true) in the given class.
     *
     *  @param object The object to which the attribute longs.
     *  @param attributeClass The attribute class.
     *  @param searchContainers Whether containers of the object are searched.
     *  @return The attribute if found, or null otherwise.
     */
    public static Attribute findMatchingAttribute(Object object,
            Class<? extends Attribute> attributeClass, boolean searchContainers) {
        if (object instanceof NamedObj) {
            NamedObj namedObj = (NamedObj) object;
            List<?> list = namedObj.attributeList(attributeClass);
            if (!list.isEmpty()) {
                return (Attribute) list.get(0);
            } else if (searchContainers) {
                return findMatchingAttribute(namedObj.getContainer(),
                        attributeClass, searchContainers);
            }
        }
        return null;
    }

    /** Get the child of the given object with the given name. The child is
     *  either an attribute, a port, an entity, or a relation if permitted by
     *  the arguments.
     *
     *  @param object The object.
     *  @param name The name of the child.
     *  @param allowAttribute Whether the child can be an attribute.
     *  @param allowPort Whether the child can be a port.
     *  @param allowEntity Whether the child can be an entity.
     *  @param allowRelation Whether the child can be a relation.
     *  @return The child if found, or null otherwise.
     */
    public static NamedObj getChild(NamedObj object, String name,
            boolean allowAttribute, boolean allowPort, boolean allowEntity,
            boolean allowRelation) {
        NamedObj child = null;
        if (allowAttribute) {
            child = object.getAttribute(name);
        }
        if (child == null && allowPort && object instanceof Entity) {
            child = ((Entity) object).getPort(name);
        }
        if (object instanceof CompositeEntity) {
            if (child == null && allowEntity) {
                child = ((CompositeEntity) object).getEntity(name);
            }
            if (child == null && allowRelation) {
                child = ((CompositeEntity) object).getRelation(name);
            }
        }
        return child;
    }

    /** Get the children of the given object. The children can be either
     *  attributes, ports, entities, or relations if permitted by the arguments.
     *
     *  @param object The object.
     *  @param includeAttributes Whether the children can be attributes.
     *  @param includePorts Whether the children can be ports.
     *  @param includeEntities Whether the children can be entities.
     *  @param includeRelations Whether the children can be relations.
     *  @return The collection of children.
     */
    public static Collection<NamedObj> getChildren(NamedObj object,
            boolean includeAttributes, boolean includePorts,
            boolean includeEntities, boolean includeRelations) {
        Collection<NamedObj> collection = new CombinedCollection<NamedObj>();
        if (includeAttributes) {
            collection.addAll(object.attributeList());
        }
        if (includePorts && object instanceof Entity) {
            Entity entity = (Entity) object;
            collection.addAll(entity.portList());
        }
        if (object instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) object;
            if (includeEntities) {
                collection.addAll(entity.entityList());
            }
            if (includeRelations) {
                collection.addAll(entity.relationList());
            }
        }
        return collection;
    }

    /** Get the unique string description containing the type and name of the\
     *  object within the given container.
     *
     *  @param object The object.
     *  @param topContainer The container used as the top level.
     *  @return The description.
     *  @see #getObjectFromCode(String, NamedObj)
     */
    public static String getCodeFromObject(NamedObj object,
            NamedObj topContainer) {
        String replacementAbbrev = getObjectTypeAbbreviation(object);
        String name = topContainer == null ? object.getName() : object
                .getName(topContainer);
        return replacementAbbrev + name;
    }

    /** Get the pattern or replacement of a transformation rule that contains
     *  the given object.
     *
     *  @param object The object.
     *  @return The pattern (of type {@link Pattern}) or replacement (of type
     *   {@link Replacement}.
     */
    public static CompositeActorMatcher getContainingPatternOrReplacement(
            NamedObj object) {
        Nameable parent = object;
        while (parent != null && !(parent instanceof Pattern)
                && !(parent instanceof Replacement)) {
            parent = parent.getContainer();
        }
        return (CompositeActorMatcher) parent;
    }

    /** Given an object in the replacement, return the corresponding object in
     *  the pattern if any, or null otherwise.
     *
     *  @param replacementObject The object in the replacement.
     *  @return The object in the pattern, or null if not found.
     */
    public static NamedObj getCorrespondingPatternObject(
            NamedObj replacementObject) {
        if (replacementObject instanceof Replacement) {
            return ((TransformationRule) replacementObject.getContainer())
                    .getPattern();
        }

        PatternObjectAttribute attribute;
        try {
            attribute = getPatternObjectAttribute(replacementObject, false);
        } catch (KernelException e) {
            attribute = null;
        }
        if (attribute == null) {
            return null;
        }

        CompositeActorMatcher container = getContainingPatternOrReplacement(replacementObject);
        if (container == null) {
            return null;
        }

        String patternObjectName = attribute.getExpression();
        if (patternObjectName.equals("")) {
            return null;
        }

        TransformationRule transformer = (TransformationRule) container
                .getContainer();
        Pattern pattern = transformer.getPattern();
        if (replacementObject instanceof Attribute) {
            return pattern.getAttribute(patternObjectName);
        } else if (replacementObject instanceof Entity) {
            return pattern.getEntity(patternObjectName);
        } else if (replacementObject instanceof Relation) {
            return pattern.getRelation(patternObjectName);
        } else {
            return null;
        }
    }

    /** Return the change request to delete the given object.
     *
     *  @param originator The originator of the change request.
     *  @param object The object to be deleted.
     *  @return The change request.
     */
    public static MoMLChangeRequest getDeletionChangeRequest(Object originator,
            NamedObj object) {
        String moml;
        if (object instanceof Attribute) {
            moml = "<deleteProperty name=\"" + object.getName() + "\"/>";
        } else if (object instanceof Entity) {
            moml = "<deleteEntity name=\"" + object.getName() + "\"/>";
        } else if (object instanceof Port) {
            moml = "<deletePort name=\"" + object.getName() + "\"/>";
        } else if (object instanceof Relation) {
            moml = "<deleteRelation name=\"" + object.getName() + "\"/>";
        } else {
            return null;
        }
        return new MoMLChangeRequest(originator, object.getContainer(), moml);
    }

    /** Get the object within the given container whose type and name
     *  correspond to the code.
     *
     *  @param code The code.
     *  @param topContainer The container used as the top level.
     *  @return The object, or null if not found.
     *  @see #getCodeFromObject(NamedObj, NamedObj)
     */
    public static NamedObj getObjectFromCode(String code, NamedObj topContainer) {
        String abbreviation = code.substring(0, 2);
        String name = code.substring(2);
        if (abbreviation.equals("A:")) {
            return topContainer.getAttribute(name);
        } else if (abbreviation.equals("E:")
                && topContainer instanceof CompositeEntity) {
            return ((CompositeEntity) topContainer).getEntity(name);
        } else if (abbreviation.equals("P:") && topContainer instanceof Entity) {
            return ((Entity) topContainer).getPort(name);
        } else if (abbreviation.equals("R:")
                && topContainer instanceof CompositeEntity) {
            return ((CompositeEntity) topContainer).getRelation(name);
        } else {
            return null;
        }
    }

    /** Get the abbreviation of the object's type.
     *
     *  @param object The object.
     *  @return The abbreviation.
     */
    public static String getObjectTypeAbbreviation(NamedObj object) {
        if (object instanceof Attribute) {
            return "A:";
        } else if (object instanceof Entity) {
            return "E:";
        } else if (object instanceof Port) {
            return "P:";
        } else if (object instanceof Relation) {
            return "R:";
        } else {
            return null;
        }
    }

    /** Get the {@link PatternObjectAttribute} associated with the object, and
     *  if it is not found, either return null if createNew is false, or create
     *  a new one and return it.
     *
     *  @param object The object.
     *  @param createNew Whether a new attribute should be created if it is not
     *   found.
     *  @return The attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public static PatternObjectAttribute getPatternObjectAttribute(
            NamedObj object, boolean createNew) throws IllegalActionException,
            NameDuplicationException {
        Attribute attribute = object.getAttribute("patternObject");
        if (createNew
                && (attribute == null || !(attribute instanceof PatternObjectAttribute))) {
            attribute = new PatternObjectAttribute(object, "patternObject");
        }
        return (PatternObjectAttribute) attribute;
    }

    /** Return whether the object in the pattern is to be created.
     *
     *  @param object The object in the pattern.
     *  @return true if it is to be created; false otherwise.
     */
    public static boolean isCreated(Object object) {
        return findMatchingAttribute(object, CreationAttribute.class, true) != null;
    }

    /** Return whether the object in the pattern is to be ignored.
     *
     *  @param object The object in the pattern.
     *  @return true if it is to be ignored; false otherwise.
     */
    public static boolean isIgnored(Object object) {
        return findMatchingAttribute(object, IgnoringAttribute.class, true) != null;
    }

    /** Return whether the object is in a pattern.
     *
     *  @param object The object.
     *  @return true if the object is in a pattern; false otherwise.
     *  @see #isInReplacement(NamedObj)
     */
    public static boolean isInPattern(NamedObj object) {
        CompositeActorMatcher container = getContainingPatternOrReplacement(object);
        return container != null && container instanceof Pattern;
    }

    /** Return whether the object is in a replacement.
     *
     *  @param object The object.
     *  @return true if the object is in a replacement; false otherwise.
     *  @see #isInPattern(NamedObj)
     */
    public static boolean isInReplacement(NamedObj object) {
        CompositeActorMatcher container = getContainingPatternOrReplacement(object);
        return container != null && container instanceof Replacement;
    }

    /** Return whether the object in the pattern is to be negated.
     *
     *  @param object The object in the pattern.
     *  @return true if it is to be negated; false otherwise.
     */
    public static boolean isNegated(Object object) {
        return findMatchingAttribute(object, NegationAttribute.class, true) != null;
    }

    /** Return whether the object in the pattern is to be optional.
     *
     *  @param object The object in the pattern.
     *  @return true if it is to be optional; false otherwise.
     */
    public static boolean isOptional(Object object) {
        return findMatchingAttribute(object, OptionAttribute.class, false) != null;
    }

    /** Return whether the object in the pattern is to be preserved.
     *
     *  @param object The object in the pattern.
     *  @return true if it is to be preserved; false otherwise.
     */
    public static boolean isPreserved(Object object) {
        return findMatchingAttribute(object, PreservationAttribute.class, true) != null;
    }

    /** Restore the values of the parameters that implement the {@link
     *  ValueIterator} interface within the root entity using the values
     *  recorded in the given table previously. The values are restored
     *  bottom-up.
     *
     *  @param root The root.
     *  @param records The table with the previously stored values.
     *  @exception IllegalActionException If the values of those parameters cannot
     *  be set.
     */
    public static void restoreValues(ComponentEntity root,
            Hashtable<ValueIterator, Token> records)
                    throws IllegalActionException {
        if (root instanceof CompositeEntity) {
            for (Object entity : ((CompositeEntity) root).entityList()) {
                restoreValues((ComponentEntity) entity, records);
            }
        }
        List<?> iterators = root.attributeList(ValueIterator.class);
        ListIterator<?> listIterator = iterators.listIterator(iterators.size());
        while (listIterator.hasPrevious()) {
            ValueIterator iterator = (ValueIterator) listIterator.previous();
            Token value = records.get(iterator);
            if (value != null) {
                iterator.setToken(value);
                iterator.validate();
            }
        }
    }

    /** Save the values of parameters that implement the {@link ValueIterator}
     *  interface in the given records table, starting from the root entity.
     *
     *  @param root The root.
     *  @param records The table to store the values.
     *  @exception IllegalActionException If the values of those parameters cannot
     *  be obtained.
     */
    public static void saveValues(ComponentEntity root,
            Hashtable<ValueIterator, Token> records)
                    throws IllegalActionException {
        List<?> iterators = root.attributeList(ValueIterator.class);
        for (Object iteratorObject : iterators) {
            ValueIterator iterator = (ValueIterator) iteratorObject;
            records.put(iterator, iterator.getToken());
        }
        if (root instanceof CompositeEntity) {
            for (Object entity : ((CompositeEntity) root).entityList()) {
                saveValues((ComponentEntity) entity, records);
            }
        }
    }

    /** Execute a MoMLChangeRequest to set the icon description of the
     *  attribute.
     *
     *  @param object The attribute.
     *  @param iconDescription The icon description.
     */
    public static void setIconDescription(NamedObj object,
            String iconDescription) {
        String moml = "<property name=\"_iconDescription\" class="
                + "\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "  <configure>" + iconDescription + "</configure>"
                + "</property>";
        MoMLChangeRequest request = new MoMLChangeRequest(object, object, moml);
        request.execute();
    }

    /** Request a MoMLChangeRequest to delete this attribute from its container.
     *
     *  @param attribute The attribute to be deleted.
     */
    private static void _delete(Attribute attribute) {
        String moml = "<deleteProperty name=\"" + attribute.getName() + "\"/>";
        attribute.requestChange(new MoMLChangeRequest(attribute, attribute
                .getContainer(), moml));
    }
}
