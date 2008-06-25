/*

@Copyright (c) 2007-2008 The Regents of the University of California.
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
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gt.data.CombinedCollection;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTTools {

    public static NamedObj cleanupModel(NamedObj model)
    throws IllegalActionException {
        Workspace workspace = model.workspace();
        try {
            workspace.getReadAccess();
            MoMLParser parser = new MoMLParser(workspace);
            NamedObj newModel = parser.parse(model.exportMoML());
            return newModel;
        } catch (Exception e) {
            throw new IllegalActionException(model, e,
                    "Unable to clean up model.");
        } finally {
            workspace.doneReading();
        }
    }

    public static void deepAddAttributes(NamedObj container,
            Class<? extends Attribute> attributeClass)
    		throws IllegalArgumentException, InstantiationException,
    		IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = attributeClass.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == 2 && types[0].isInstance(container)
                    && types[1].equals(String.class)) {
                constructor.newInstance(container, container.uniqueName("_" +
                        attributeClass.getSimpleName()));
                break;
            }
        }
        for (Object child : getChildren(container, false, true, true, true)) {
            deepAddAttributes((NamedObj) child, attributeClass);
        }
    }

    public static void deepRemoveAttributes(NamedObj container,
            Class<? extends Attribute> attributeClass)
    		throws IllegalActionException, NameDuplicationException {
        List<Object> attributes = new LinkedList<Object>(
                (Collection<?>) container.attributeList(attributeClass));
        for (Object attribute : attributes) {
            ((Attribute) attribute).setContainer(null);
        }
        for (Object child : getChildren(container, false, true, true, true)) {
            deepRemoveAttributes((NamedObj) child, attributeClass);
        }
    }

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

    public static Collection<?> getChildren(NamedObj object,
            boolean includeAttributes, boolean includePorts,
            boolean includeEntities, boolean includeRelations) {
        Collection<Object> collection = new CombinedCollection<Object>();
        if (includeAttributes) {
            collection.addAll((Collection<?>) object.attributeList());
        }
        if (includePorts && object instanceof Entity) {
            Entity entity = (Entity) object;
            collection.addAll((Collection<?>) entity.portList());
        }
        if (object instanceof CompositeEntity) {
            CompositeEntity entity = (CompositeEntity) object;
            if (includeEntities) {
                collection.addAll((Collection<?>) entity.entityList());
            }
            if (includeRelations) {
                collection.addAll((Collection<?>) entity.relationList());
            }
        }
        return collection;
    }

    public static String getCodeFromObject(NamedObj object,
            NamedObj topContainer) {
        String replacementAbbrev = getObjectTypeAbbreviation(object);
        String name = topContainer == null ? object.getName() : object
                .getName(topContainer);
        return replacementAbbrev + name;
    }

    public static CompositeActorMatcher getContainingPatternOrReplacement(
            NamedObj entity) {
        Nameable parent = entity;
        while (parent != null && !(parent instanceof Pattern)
                && !(parent instanceof Replacement)) {
            parent = parent.getContainer();
        }
        return (CompositeActorMatcher) parent;
    }

    public static NamedObj getCorrespondingPatternObject(
            NamedObj replacementObject) {
        if (replacementObject instanceof Replacement) {
            return ((TransformationRule) replacementObject.getContainer())
                    .getPattern();
        }

        PatternObjectAttribute attribute = getPatternObjectAttribute(
                replacementObject);
        if (attribute == null) {
            return null;
        }

        CompositeActorMatcher container = getContainingPatternOrReplacement(
                replacementObject);
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
        if (replacementObject instanceof Entity) {
            return pattern.getEntity(patternObjectName);
        } else if (replacementObject instanceof Relation) {
            return pattern.getRelation(patternObjectName);
        } else {
            return null;
        }
    }

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

    public static NamedObj getObjectFromCode(String code, NamedObj topContainer) {
        String abbreviation = code.substring(0, 2);
        String name = code.substring(2);
        if (abbreviation.equals("A:")) {
            return topContainer.getAttribute(name);
        } else if (abbreviation.equals("E:")
                && topContainer instanceof CompositeEntity) {
            return ((CompositeEntity) topContainer).getEntity(name);
        } else if (abbreviation.equals("P:")
                && topContainer instanceof Entity) {
            return ((Entity) topContainer).getPort(name);
        } else if (abbreviation.equals("R:")
                && topContainer instanceof CompositeEntity) {
            return ((CompositeEntity) topContainer).getRelation(name);
        } else {
            return null;
        }
    }

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

    public static PatternObjectAttribute getPatternObjectAttribute(
            NamedObj object) {
        Attribute attribute = object.getAttribute("patternObject");
        if (attribute != null && attribute instanceof PatternObjectAttribute) {
            return (PatternObjectAttribute) attribute;
        } else {
            return null;
        }
    }

    public static boolean isInPattern(NamedObj entity) {
        CompositeActorMatcher container = getContainingPatternOrReplacement(
                entity);
        return container != null && container instanceof Pattern;
    }

    public static boolean isInReplacement(NamedObj entity) {
        CompositeActorMatcher container = getContainingPatternOrReplacement(
                entity);
        return container != null && container instanceof Replacement;
    }
}
