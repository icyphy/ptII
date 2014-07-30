/*
 Class containing helper methods used by the ptserver and/or Homer.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptserver.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import ptolemy.data.expr.Parameter;
import ptolemy.homer.kernel.HomerConstants;
import ptolemy.homer.kernel.HomerLocation;
import ptolemy.homer.kernel.LayoutParser;
import ptolemy.homer.kernel.TabDefinition;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptserver.actor.ProxyActor;

///////////////////////////////////////////////////////////////////
//// ServerUtility

/**
 * Class containing helper methods used by the ptserver and/or Homer.
 * @author Anar Huseynov
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
public class ServerUtility {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Create and initialize a new MoMLParser.
     * The parser would have BackwardCompatibility and RemoveGraphicalClasses filters.
     * The RemoteGraphicalClasses would filter out only classes that are known not to be
     * portable to be portable to Android.
     * @return new MoMLParser with BackwardCompatibility and RemoveGraphicalClasses filters.
     */
    public static MoMLParser createMoMLParser() {
        MoMLParser parser = new MoMLParser(new Workspace());
        parser.resetAll();
        // TODO: is this thread safe?
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        // TODO either fork RemoveGraphicalClasses or make its hashmap non-static (?)
        RemoveGraphicalClasses filter = new RemoveGraphicalClasses();
        filter.remove("ptolemy.actor.lib.gui.ArrayPlotter");
        filter.remove("ptolemy.actor.lib.gui.SequencePlotter");
        filter.remove("ptolemy.actor.lib.gui.Display");
        filter.remove("ptolemy.actor.gui.style.CheckBoxStyle");
        filter.remove("ptolemy.actor.gui.style.ChoiceStyle");
        MoMLParser.addMoMLFilter(filter);
        return parser;
    }

    /**
     * Return the deep attribute list of the container in a depth first order.
     * @param container the container to process.
     * @return the deep attribute list of the container.
     */
    public static List<Attribute> deepAttributeList(NamedObj container) {
        ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
        _deepAttributeList(container, attributeList);
        return attributeList;
    }

    /**
     * Find all remote attributes of the model and add them to the
     * remoteAttributeMap.
     * @param attributeList the attribute list to search
     * @param remoteAttributeMap the map where the attributes need to be added.
     */
    public static void findRemoteAttributes(List<Attribute> attributeList,
            HashMap<String, Settable> remoteAttributeMap) {
        for (Attribute attribute : attributeList) {
            if (ServerUtility.isRemoteAttribute(attribute)) {
                remoteAttributeMap.put(attribute.getFullName(),
                        (Settable) attribute);
            }
        }
    }

    /**
     * Return true if the attribute is marked as remote attribute, false otherwise.
     * @param attribute the child attribute of the attribute to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isRemoteAttribute(Attribute attribute) {
        if (attribute instanceof Settable) {
            Attribute isRemoteAttribute = attribute
                    .getAttribute(ServerUtility.REMOTE_OBJECT_TAG);
            if (isRemoteAttribute instanceof Parameter) {
                System.out.println("isRemoteAttribute(): " + attribute + " "
                        + isRemoteAttribute);
                if (((Parameter) isRemoteAttribute).getExpression().equals(
                        ServerUtility.REMOTE_ATTRIBUTE)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Return true if the attribute is marked as remote sink, false otherwise.
     * @param targetEntityAttribute the child attribute the source
     * actor to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isTargetProxySink(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            System.out.print("isTargetProxySource(): " + targetEntityAttribute
                    + " " + parameter + " expression: "
                    + parameter.getExpression() + " ==? "
                    + ServerUtility.PROXY_SOURCE_ATTRIBUTE);

            if (parameter.getExpression().equals(
                    ServerUtility.PROXY_SINK_ATTRIBUTE)) {
                System.out.println(" TRUE");
                return true;
            } else {
                System.out.println(" FALSE");
            }
        }
        System.out.println("isTargetProxySink(): " + targetEntityAttribute
                + " FALSE");
        return false;
    }

    /**
     * Return true if the attribute is marked as remote source, false otherwise.
     * @param targetEntityAttribute the child attribute of the source actor to be checked.
     * @return true if the attribute is marked as remote attribute, false otherwise.
     */
    public static boolean isTargetProxySource(Attribute targetEntityAttribute) {
        if (targetEntityAttribute instanceof Settable) {
            Settable parameter = (Settable) targetEntityAttribute;
            System.out.print("isTargetProxySource(): " + targetEntityAttribute
                    + " " + parameter + " expression: "
                    + parameter.getExpression() + " ==? "
                    + ServerUtility.PROXY_SOURCE_ATTRIBUTE);

            if (parameter.getExpression().equals(
                    ServerUtility.PROXY_SOURCE_ATTRIBUTE)) {
                System.out.println(" TRUE");
                return true;
            } else {
                System.out.println(" TRUE");
            }
        }
        System.out.println("isTargetProxySource(): " + targetEntityAttribute
                + "FALSE");
        return false;
    }

    /**
     * Merge the model with layout.
     * @param model The model to merge.
     * @param layout The layout to merge.
     * @param classesToMerge The classes that need to be merged.
     * @param namedObjectsToMerge The named objects that need to be merged.
     * @return the merged model.
     * @exception IllegalActionException if there is a problem merging the model.
     * @exception NameDuplicationException if there is a problem merging the model.
     * @exception CloneNotSupportedException if there is a problem merging the model.
     */
    public static CompositeEntity mergeModelWithLayout(CompositeEntity model,
            CompositeEntity layout,
            HashSet<Class<? extends Attribute>> classesToMerge,
            HashSet<String> namedObjectsToMerge) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {

        // Traverse all elements in the layout.
        for (ComponentEntity entity : (List<ComponentEntity>) layout
                .deepEntityList()) {
            _mergeElements(entity, model, classesToMerge, namedObjectsToMerge);
        }
        _mergeElements(layout, model, classesToMerge, namedObjectsToMerge);

        return model;
    }

    /**
     * Merge the model with layout.
     * @param modelURL The URL to the model.
     * @param layoutURL The URL to the layout.
     * @param classesToMerge The classes that need to be merged.
     * @param namedObjectsToMerge The named objects that need to be merged.
     * @return the merged model.
     * @exception IllegalActionException if there is a problem merging the model.
     * @exception NameDuplicationException if there is a problem merging the model.
     * @exception CloneNotSupportedException if there is a problem merging the model.
     */
    public static CompositeEntity mergeModelWithLayout(URL modelURL,
            URL layoutURL, HashSet<Class<? extends Attribute>> classesToMerge,
            HashSet<String> namedObjectsToMerge) throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        CompositeEntity model = openModelFile(modelURL);
        CompositeEntity layout = openModelFile(layoutURL);
        return mergeModelWithLayout(model, layout, classesToMerge,
                namedObjectsToMerge);
    }

    /**
     * Merge the model with layout.
     * @param modelURL The URL to the model.
     * @param layoutURL The URL to the layout.
     * @param classesToMerge The classes that need to be merged.
     * @param namedObjectsToMerge The named objects that need to be merged.
     * @return the merged model.
     * @exception MalformedURLException if there is a problem merging the model.
     * @exception IllegalActionException if there is a problem merging the model.
     * @exception NameDuplicationException if there is a problem merging the model.
     * @exception CloneNotSupportedException if there is a problem merging the model.
     */
    public static CompositeEntity mergeModelWithLayout(String modelURL,
            String layoutURL,
            HashSet<Class<? extends Attribute>> classesToMerge,
            HashSet<String> namedObjectsToMerge) throws MalformedURLException,
            IllegalActionException, NameDuplicationException,
            CloneNotSupportedException {
        return mergeModelWithLayout(new URL(modelURL), new URL(layoutURL),
                classesToMerge, namedObjectsToMerge);
    }

    /** Open a MoML file, parse it, and the parsed model.
     *
     *  @param url The url of the model.
     *  @return The parsed model.
     *  @exception IllegalActionException If the parsing failed.
     */
    public static CompositeEntity openModelFile(URL url)
            throws IllegalActionException {
        MoMLParser parser = new MoMLParser(new Workspace());
        parser.resetAll();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        try {
            return (CompositeEntity) parser.parse(null, url);
        } catch (Exception e) {
            throw new IllegalActionException(null, e, "Unable to parse url: "
                    + url);
        }
    }

    /** Strips the first part of a compound element name, including the
     *  "." at the beginning.
     *
     * @param fullName The compound name of an element.
     * @return The stripped name of the element, where the first part of
     * the compound name is removed, including the "." at the beginning.
     */
    public static String stripFullName(String fullName) {
        if (fullName.indexOf(".") == -1 || fullName.length() < 2) {
            return fullName;
        }
        return fullName.substring(fullName.substring(1).indexOf(".") + 2);
    }

    /**
     * Validate the model and layout and return list of errors that might prevent
     * merging.
     * @param modelURL The URL of the model.
     * @param layoutURL The URL of the layout.
     * @return the list of the layout validation errors.
     */
    public LayoutValidationErrors validateModelAndLayout(String modelURL,
            String layoutURL) {
        // Open the two models
        CompositeEntity model = null;
        CompositeEntity layout = null;
        LayoutValidationErrors validation = new LayoutValidationErrors();
        try {
            model = openModelFile(new URL(modelURL));
            layout = openModelFile(new URL(layoutURL));
        } catch (IllegalActionException e) {
            validation.addException(e);
        } catch (MalformedURLException e) {
            validation.addException(e);
        }

        if (validation.haveErrors()) {
            return validation;
        }

        LayoutParser layoutParser = new LayoutParser(layout);

        // Entity or remote attribute in layout that's missing from the model file (except Proxy entities)
        for (NamedObj object : (List<NamedObj>) layout.deepNamedObjList()) {
            if (!model.deepContains(object)) {
                validation.addObjectMissingFromModel(object);
            }
        }

        // Layout validation: Remote entities have proxy entities
        boolean found = false;
        for (NamedObj object : layoutParser.getProxyElements()) {
            found = false;
            // Only sink and source entities have proxy actors
            if (!(object instanceof ComponentEntity)) {
                continue;
            }
            ComponentEntity entityToCheck = (ComponentEntity) object;
            // Check if any of the proxy actors have them as targets and pther proxy
            // properties.
            for (ProxyActor proxy : layout.entityList(ProxyActor.class)) {
                if (proxy.getTargetEntityName().equals(
                        entityToCheck.getFullName())) {
                    found = true;
                    break;
                } else {
                    // Proxy entity's targets are invalid/not in the model.
                    ComponentEntity target = model.getEntity(proxy
                            .getTargetEntityName());
                    if (target == null) {
                        validation.addProxyWithInvalidTarget(proxy);
                    }
                }

                // Proxy entity port target ports are invalid/not in the model
                for (Port port : proxy.portList()) {
                    Attribute targetPort = port.getAttribute("targetPortName");
                    if (targetPort == null || !model.deepContains(targetPort)) {
                        validation.addPortWithNoOrInvalidTarget(port);
                    }
                }

            }

            if (!found) {
                validation.addEntityWithoutProxy(entityToCheck);
            }
        }

        ArrayList<TabDefinition> tabs = null;
        try {
            tabs = layoutParser.getTabDefinitions();
        } catch (IllegalActionException e) {
            validation.addException(e);
        } catch (NameDuplicationException e) {
            validation.addException(e);
        }
        for (NamedObj object : layoutParser.getPositionableElements()) {
            found = false;

            // Layout validation: tab node without specified tab.
            Attribute tab = object.getAttribute(HomerConstants.TAB_NODE);
            if (tab == null) {
                // We accept if tab is undefined. They should be put into a defaut tab.
                found = true;
            } else {
                for (TabDefinition tabDefinition : tabs) {
                    if (tabDefinition.getTag().equals(
                            ((Settable) tab).getExpression())) {
                        found = true;
                        break;
                    }
                }
            }

            if (!found) {
                validation.addPositionableWithInvalidTab(object);
            }

            // Layout validation: invalid position or position out of bounds.
            List<HomerLocation> locations = object
                    .attributeList(HomerLocation.class);
            if (locations.isEmpty()) {
                validation.addPositionableWithInvalidLocation(object);
            } else {
                for (HomerLocation location : locations) {
                    try {
                        location.validateLocation();
                    } catch (IllegalActionException e) {
                        validation.addException(e);
                        validation.addPositionableWithInvalidLocation(object);
                        break;
                    }
                }
            }
        }
        return validation;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    /**
     * Recursively find all attributes of the container.
     * @param container the container to check.
     * @param attributeList the attributeList that would contain attributes.
     */
    private static void _deepAttributeList(NamedObj container,
            List<Attribute> attributeList) {
        for (Object attributeObject : container.attributeList()) {
            Attribute attribute = (Attribute) attributeObject;
            attributeList.add(attribute);
            _deepAttributeList(attribute, attributeList);
        }
    }

    /** Merge the source object and all its deeply contained attributes with the target model.
     *  If an entity does not exists in the target model it will not be added, but each
     *  existing object's attributes that are not in the target model will be added.
     *
     *  @param source The source object will potentially extra attributes that are not contained
     *  in the target model.
     * @param targetModel The target model to be updated.
     * @param classesToMerge Contains the classes of attributes to be included when merging. If
     *  this is null, every attribute not present in the target model will be added.
     * @param namedObjectsToMerge The list of named objects to be merged
     *  @exception IllegalActionException If an attribute could not be added to the target model.
     * @exception CloneNotSupportedException
     */
    private static void _mergeElements(NamedObj source,
            CompositeEntity targetModel,
            HashSet<Class<? extends Attribute>> classesToMerge,
            HashSet<String> namedObjectsToMerge) throws IllegalActionException,
            CloneNotSupportedException {
        //System.out.println("_mergeElement(" + source.getFullName() + ", " + targetModel.getFullName() + ", " + classesToMerge + ", " + namedObjectsToMerge);
        // Check if source and model is available.
        if (source == null || targetModel == null) {
            return;
        }

        // Check if source is either an entity or an attribute. Merging is only done
        // on those two types.
        if (!(source instanceof Entity || source instanceof Attribute)) {
            return;
        }

        // If the source is an entity, but is not originally in the target model, the merge
        // skips it
        // Note: This holds for any child entity but does not hold for the top level container.
        // Added a check to allow merging of top level container's attributes.
        if (source instanceof Entity
                && targetModel.getEntity(stripFullName(source.getFullName())) == null
                && source.getContainer() != null) {
            return;
        }
        System.out.println("_mergeElements() at this point ...");

        // At this point the source is either an entity that is also in the target model
        // or it's an attribute. In both cases the the merge will add all attributes that are
        // not present in the target model.
        List<Attribute> attributeList = ServerUtility.deepAttributeList(source);
        for (Attribute attribute : attributeList) {
            System.out
                    .println("_mergeElements() looping "
                            + attribute.getClass()
                            + " "
                            + attribute.getName()
                            + " "
                            + (classesToMerge == null ? "null" : classesToMerge
                                    .contains(attribute.getClass()))
                            + " "
                            + (namedObjectsToMerge == null ? "null"
                                    : namedObjectsToMerge.contains(attribute
                                            .getName())));
            if (classesToMerge == null
                    || classesToMerge.contains(attribute.getClass())
                    || namedObjectsToMerge == null
                    || namedObjectsToMerge.contains(attribute.getName())) {
                // Insert attribute into the target model. The attribute will no longer be
                // available in the source.
                try {
                    // Get read and write access from the source to the target.
                    source.workspace().getReadAccess();
                    targetModel.workspace().getWriteAccess();

                    Attribute clonedAttribute = (Attribute) attribute
                            .clone(targetModel.workspace());
                    NamedObj targetParent = null;
                    if (attribute.getContainer().getContainer() == null) {
                        targetParent = targetModel;
                    } else if (attribute.getContainer() instanceof ComponentEntity) {
                        targetParent = targetModel
                                .getEntity(stripFullName(attribute
                                        .getContainer().getFullName()));
                    } else if (attribute.getContainer() instanceof Attribute) {
                        targetParent = targetModel
                                .getAttribute(stripFullName(attribute
                                        .getContainer().getFullName()));
                    }

                    if (targetParent != null) {
                        clonedAttribute.setContainer(targetParent);
                    } else if (attribute.getContainer().getFullName()
                            .equals(targetModel.getFullName())) {
                        clonedAttribute.setContainer(targetModel);
                    } else {
                        // TODO should we log this or throw an exception?
                        throw new IllegalActionException(clonedAttribute,
                                "Parent for the object within the targetModel was not found");
                    }
                    clonedAttribute.setPersistent(true);
                    if (clonedAttribute instanceof Settable) {
                        ((Settable) clonedAttribute)
                        .setVisibility(Settable.NONE);
                    }
                } catch (NameDuplicationException e) {
                    // The attribute already exists. Since deepAttributeList returns all deeply
                    // nested attributes too, the merge will look into attributes in lower levels
                    // of the model. No need to do anything here.
                } finally {
                    // Remove the accesses from the workspaces.
                    targetModel.workspace().doneWriting();
                    source.workspace().doneReading();
                }
            }
        }
    }

    /**
     * Attribute value indicating that the parent attribute is a remote attribute -
     * its value needs to synchronized between client and server models.
     */
    public static final String REMOTE_ATTRIBUTE = "attribute";
    /**
     * Attribute name indicating that the named object needs to be handled by the ProxyModelBuilder.
     */
    public static final String REMOTE_OBJECT_TAG = "_remote";
    /**
     * Attribute value indicating that the actor is a source.
     */
    public static final String PROXY_SOURCE_ATTRIBUTE = "source";
    /**
     * Attribute value indicating that the actor is a sink.
     */
    public static final String PROXY_SINK_ATTRIBUTE = "sink";
}
