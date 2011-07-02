/* Class that wraps and uses MoMLParser to parse the customized
   user interface layout file.
 
 Copyright (c) 2011 The Regents of the University of California.
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
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// LayoutParser

/** Parse the layout XML file for tabs, screen orientation, and 
 *  individual sink actor placement properties.
 *
 *  @author Justin Killian
 *  @version $Id$
 *  @since Ptolemy II 8.0
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
    public LayoutParser(CompositeActor topLevelActor) {
        _topLevelActor = topLevelActor;
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

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
                            .toUpperCase());
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
            return null;
        }

        // Get the tab attributes in the layout file.
        List<Attribute> tabs = tabsAttribute.attributeList();
        if ((tabs == null) || (tabs.size() == 0)) {
            return null;
        }

        // Iterate over each child attribute to get the name of the tab
        // and the corresponding elements.
        for (Attribute tab : tabs) {
            if (!(tab instanceof StringAttribute)) {
                throw new IllegalActionException(_topLevelActor,
                        "Tab definition is incorrect.");
            }

            // Add the new parsed tab to the list.
            TabDefinition tabDefinition = new TabDefinition(
                    ((StringAttribute) tab).getName(),
                    ((StringAttribute) tab).getExpression());
            tabDefinitions.add(tabDefinition);
        }

        return tabDefinitions;
    }

    /** Get the entities with positions defined.
     * 
     *  @return All the entities with positions parsed.
     *  @exception IllegalActionException If an entity does not implement {@link PortablePlaceabe}
     *  or if the location information is invalid for any of the entities.
     */
    public ArrayList<EntityElement> getPositionableEntities()
            throws IllegalActionException {
        ArrayList<EntityElement> entityDefinitions = new ArrayList<EntityElement>();

        // Note that deepEntityList is recursive, no need to look into composite
        // entities separately.
        List<ComponentEntity> entities = _topLevelActor.deepEntityList();
        for (ComponentEntity entity : entities) {
            // Add to the list of visualizable elements if the position is defined.
            if (_isPositionable(entity)) {
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

        _initPositionableAttributes(_topLevelActor, attributeDefinitions);

        return attributeDefinitions;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private methods                            ////

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
        for (Attribute attribute : (List<Attribute>) container.attributeList()) {
            if (_isPositionable(attribute)) {
                attributeContainer.add(new AttributeElement(attribute));
            } else {
                _initPositionableAttributes(attribute, attributeContainer);
            }
        }
    }

    /** Return true if the node has location defined.
     *  @param node The named object to check.
     *  @return True if the node has location defined, false otherwise.
     */
    private boolean _isPositionable(NamedObj node) {
        HomerLocation location = (HomerLocation) node
                .getAttribute(HomerConstants.POSITION_NODE);

        // If the location is not set, return false.
        if (location == null) {
            return false;
        }

        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The top-level actor of the parsed model file.
     */
    private final CompositeActor _topLevelActor;

    /** The default screen orientation for the Android device.
     */
    private static final ScreenOrientation DEFAULT_SCREEN_ORIENTATION = ScreenOrientation.UNSPECIFIED;

    /** The different screen orientations possible for the visual
     *  representation.
     */
    private enum ScreenOrientation {
        PORTRAIT, LANDSCAPE, UNSPECIFIED
    }
}
