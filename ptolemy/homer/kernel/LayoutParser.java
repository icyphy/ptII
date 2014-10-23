/* Class that wraps and uses MoMLParser to parse the customized
   user interface layout file.

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

package ptolemy.homer.kernel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptserver.util.ServerUtility;

///////////////////////////////////////////////////////////////////
//// LayoutParser

/** Parse the layout XML file for tabs, screen orientation, and
 *  individual sink actor placement properties.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (jkillian)
 *  @Pt.AcceptedRating Red (jkillian)
 */
public class LayoutParser {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Parse the layout XML file so that the screen display
     *  can be set up according to the user's preferences.
     *  @param topLevelActor Top-level actor of the parsed model file.
     */
    public LayoutParser(CompositeEntity topLevelActor) {
        _topLevelActor = topLevelActor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the orientation of the screen from the parsed layout file.
     *  @return The screen orientation (if defined by the layout).
     */
    public ScreenOrientation getOrientation() {
        // By default set the screen orientation to unspecified.
        ScreenOrientation screenOrientation = DEFAULT_SCREEN_ORIENTATION;

        Attribute orientation = _topLevelActor
                .getAttribute(HomerConstants.ORIENTATION_NODE);

        // If the orientation is not defined, or if doesn't have an expression
        // return default orientation.
        if (orientation == null || !(orientation instanceof Settable)) {
            return DEFAULT_SCREEN_ORIENTATION;
        }

        try {
            screenOrientation = Enum.valueOf(ScreenOrientation.class,
                    ((Settable) orientation).getExpression().trim()
                    .toUpperCase(Locale.getDefault()));
        } catch (IllegalArgumentException e) {
            return DEFAULT_SCREEN_ORIENTATION;
        }

        return screenOrientation;
    }

    /** Get the parsed tabs defined in the layout file.
     *  @return All the tabs parsed, or null if no tabs were defined.
     *  @exception IllegalActionException If a tab description is incorrect
     *  @exception NameDuplicationException If there's an error setting the
     *  style due to naming issues.
     */
    public ArrayList<TabDefinition> getTabDefinitions()
            throws IllegalActionException, NameDuplicationException {
        ArrayList<TabDefinition> tabDefinitions = new ArrayList<TabDefinition>();

        // Check for the tab definition.
        Attribute tabsAttribute = _topLevelActor
                .getAttribute(HomerConstants.TABS_NODE);
        if (tabsAttribute == null) {
            return tabDefinitions;
        }

        // Get the tab attributes in the layout file.
        List<Attribute> tabs = tabsAttribute.attributeList();
        if (tabs == null || tabs.size() == 0) {
            return tabDefinitions;
        }

        // Iterate over each child attribute to get the name of the tab
        // and the corresponding elements.
        for (Attribute tab : tabs) {
            if (!(tab instanceof StringAttribute)) {
                throw new IllegalActionException(_topLevelActor,
                        "Tab definition is incorrect.");
            }

            // Add the new parsed tab to the list.
            TabDefinition tabDefinition = new TabDefinition(_topLevelActor,
                    ((StringAttribute) tab).getName(),
                    ((StringAttribute) tab).getExpression());
            tabDefinitions.add(tabDefinition);
        }

        return tabDefinitions;
    }

    /** Get the entities with positions defined.
     *
     *  @return All the entities with positions parsed.
     *  @exception IllegalActionException If an entity does not
     *  implement {@link ptolemy.actor.injection.PortablePlaceable} or if
     *  the location information is invalid for any of the entities.
     */
    public ArrayList<EntityElement> getPositionableEntities()
            throws IllegalActionException {
        ArrayList<EntityElement> entityDefinitions = new ArrayList<EntityElement>();

        // Note that deepEntityList is recursive, no need to look into composite
        // entities separately.
        List<ComponentEntity> entities = _topLevelActor.deepEntityList();
        for (ComponentEntity entity : entities) {
            // Add to the list of visualizable elements if the position is defined.
            if (isPositionable(entity)) {
                entityDefinitions.add(new EntityElement(entity));
            }
        }

        return entityDefinitions;
    }

    /** Get the attributes with positions defined in the layout file.
     *
     *  @return All the attributes with locations parsed.
     *  @exception IllegalActionException If the location information is invalid
     *  for any of the attributes, or if the attribute is not Settable.
     */
    public ArrayList<AttributeElement> getPositionableAttributes()
            throws IllegalActionException {
        ArrayList<AttributeElement> attributeDefinitions = new ArrayList<AttributeElement>();
        List<ComponentEntity> entities = _topLevelActor.deepEntityList();
        for (ComponentEntity entity : entities) {
            _initPositionableAttributes(entity, attributeDefinitions);
        }
        _initPositionableAttributes(_topLevelActor, attributeDefinitions);

        return attributeDefinitions;
    }

    /** Return all the elements in the model marked as proxies.
     *
     *  @return The set of all named objects marked as proxies.
     */
    public HashSet<NamedObj> getProxyElements() {
        HashSet<NamedObj> container = new HashSet<NamedObj>();
        _getProxyElements(_topLevelActor, container);
        return container;
    }

    /** Return all elements in the model that has its positions defined.
     *
     *  @return The set of all named objects with position information.
     */
    public HashSet<NamedObj> getPositionableElements() {
        HashSet<NamedObj> container = new HashSet<NamedObj>();
        _getPositionableElements(_topLevelActor, container);
        return container;
    }

    /** Get all the elements marked as proxies under the element and add them to
     *  the container.
     *
     *  @param element The element to search for proxy attribute and other elements
     *  that have proxy attributes.
     *  @param container The container to store the elements found.
     */
    private static void _getProxyElements(NamedObj element,
            HashSet<NamedObj> container) {

        // Found the attribute, find the element in the original model
        // and add the attribute to it.
        if (element.getAttribute(ServerUtility.REMOTE_OBJECT_TAG) != null) {
            // Found proxy attribute, add it to the container
            container.add(element);
        }
        // Element did not contain the proxy attribute, let's search the
        // other named objects within the element.
        for (Iterator iterator = element.containedObjectsIterator(); iterator
                .hasNext();) {
            NamedObj namedObj = (NamedObj) iterator.next();
            _getProxyElements(namedObj, container);
        }
    }

    /** Get all the elements that have position defined under the element and add
     *  them to the container.
     *
     *  @param element The element to search for location attribute and other elements
     *  that have location attributes.
     *  @param container The container to store the elements found.
     */
    private static void _getPositionableElements(NamedObj element,
            HashSet<NamedObj> container) {

        // Found the attribute, find the element in the original model
        // and add the attribute to it.
        if (element.getAttribute(HomerConstants.POSITION_NODE) != null) {
            // Found position attribute, add it to the container
            container.add(element);
            // Element did not contain the position attribute, let's search the
            // other named objects within the element.
        }
        for (Iterator iterator = element.containedObjectsIterator(); iterator
                .hasNext();) {
            NamedObj namedObj = (NamedObj) iterator.next();
            _getPositionableElements(namedObj, container);
        }
    }

    /** The different screen orientations possible for the visual
     *  representation.
     */
    public enum ScreenOrientation {

        /** Defines portrait orientation.
         */
        PORTRAIT,

        /** Defines landscape orientation.
         */
        LANDSCAPE,

        /** Defines unknown orientation.
         */
        UNSPECIFIED;

        /** Return the enumeration value in lowercase lettering.
         *  @return The value in lowercase.
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.getDefault());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Find all positionable attributes of the model recursively and
     *  add them to the passed container parsed.
     *
     *  @param container The named object that contains the attributes to
     *  be parsed.
     *  @param attributeContainer Container to hold the parsed positionable
     *  attributes.
     *  @exception IllegalActionException If a found attribute with the
     *  location definition is not settable.
     */
    private void _initPositionableAttributes(NamedObj container,
            ArrayList<AttributeElement> attributeContainer)
                    throws IllegalActionException {
        for (Attribute attribute : ServerUtility.deepAttributeList(container)) {
            if (isPositionable(attribute)) {
                attributeContainer.add(new AttributeElement(attribute));
            }
        }
    }

    /** Return true if the node has location defined.
     *  @param node The named object to check.
     *  @return True if the node has location defined, false otherwise.
     */
    public static boolean isPositionable(NamedObj node) {
        HomerLocation location = (HomerLocation) node
                .getAttribute(HomerConstants.POSITION_NODE);

        // If the location is not set, return false.
        if (location == null) {
            return false;
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The top-level actor of the parsed model file.
     */
    private final CompositeEntity _topLevelActor;

    /** The default screen orientation for the Android device.
     */
    private static final ScreenOrientation DEFAULT_SCREEN_ORIENTATION = ScreenOrientation.UNSPECIFIED;
}
