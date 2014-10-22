/* Store a model's element representations on multiple contents.

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
import java.util.HashMap;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
////MultiContent

/** Store a model's element representations on multiple contents.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 *
 *  @param <T> The class implementing a ContentPrototype. It represents a tab
 *  that can create one similar to itself using the prototype pattern.
 */
public class MultiContent<T extends ContentPrototype> {

    /** Create a new multi-content frame based on a content prototype.
     *
     *  @param contentPrototype A prototype of the content area. This
     *  prototype is used to create multiple content areas on demand.
     */
    public MultiContent(T contentPrototype) {
        _contentPrototype = contentPrototype;
    }

    /** The constructor takes a model that is parsed for elements with
     *  location attributes. All that should be represented are parsed
     *  and placed in multiple content areas depending on their parameters.
     *
     *  @param model The model to be parsed.
     *  @param contentPrototype A prototype of the content area. This
     *  prototype is used to create multiple content areas on demand.
     *  @exception IllegalActionException If any of the elements can't be places
     *  into a content area.
     *  @exception NameDuplicationException If multiple elements exist with the
     *  same name. Element names within a workspace must be unique.
     */
    public MultiContent(T contentPrototype, CompositeEntity model)
            throws IllegalActionException, NameDuplicationException {
        _contentPrototype = contentPrototype;
        _initializeContents(contentPrototype, model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a new content area to the MultiContent with a specific content.
     *  The new content area will be the last in order.
     *
     *  @param topLevel The container containing all tab definitions. If the
     *  container does not have the attribute that contains all tab definition
     *  information, it will be created.
     *  @param tabTag The tag identifier used for the content area.
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @param content The specific content for the new tab.
     *  @return The tab identifier.
     *  @exception NameDuplicationException If the name coincides with an
     *  attribute already in the container.
     *  @exception IllegalActionException If the attribute is not of an acceptable
     *  class for the container, if the name contains a period, or if a content
     *  area with the same tag already exist.
     */
    public String addTab(ComponentEntity topLevel, String tabTag,
            String tabName, ContentPrototype content)
            throws IllegalActionException, NameDuplicationException {
        if (tabTag != null && _contents.containsKey(tabTag)) {
            throw new IllegalActionException(
                    "A content area with the identifier " + tabTag
                            + " already exists.");
        }

        TabDefinition tabDefinition = new TabDefinition(topLevel, tabTag,
                tabName);
        // Since a new tag might have been generated, return the real tag of the tab.
        String tag = tabDefinition.getTag();

        tabDefinition.setContent(content);
        _contents.put(tag, tabDefinition);
        _order.add(tag);
        return tag;
    }

    /** Add a new content area to the MultiContent. The new content area will
     *  be the last in order.
     *
     *  @param topLevel The container containing all tab definitions. If the
     *  container does not have the attribute that contains all tab definition
     *  information, it will be created.
     *  @param tabTag The tag identifier used for the content area.
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @return The tab identifier.
     *  @exception NameDuplicationException If the name coincides with an
     *  attribute already in the container.
     *  @exception IllegalActionException If the attribute is not of an acceptable
     *  class for the container, if the name contains a period, or if a content
     *  area with the same tag already exist.
     */
    public String addTab(ComponentEntity topLevel, String tabTag, String tabName)
            throws IllegalActionException, NameDuplicationException {
        return addTab(topLevel, tabTag, tabName,
                _contentPrototype.getNewInstance());
    }

    /** Add a new content area to the MultiContent. The new content area will
     *  be the last in order.
     *
     *  @param topLevel The container containing all tab definitions. If the
     *  container does not have the attribute that contains all tab definition
     *  information, it will be created.
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @param content The content to use in the new tab.
     *  @return The newly generated tab identifier.
     *  @exception NameDuplicationException If the name coincides with an
     *  attribute already in the container.
     *  @exception IllegalActionException If the tab could not be created in the model.
     */
    public String addTab(ComponentEntity topLevel, String tabName,
            ContentPrototype content) throws NameDuplicationException,
            IllegalActionException {
        return addTab(topLevel, null, tabName, content);
    }

    /** Add a new content area to the MultiContent. The new content area will
     *  be the last in order.
     *
     *  @param topLevel The container containing all tab definitions. If the
     *  container does not have the attribute that contains all tab definition
     *  information, it will be created.
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @return The newly generated tab identifier.
     *  @exception NameDuplicationException If the name coincides with an
     *  attribute already in the container.
     *  @exception IllegalActionException If the tab could not be created in the model.
     */
    public String addTab(ComponentEntity topLevel, String tabName)
            throws NameDuplicationException, IllegalActionException {
        return addTab(topLevel, tabName, _contentPrototype.getNewInstance());
    }

    /** Add an element to a specific content area.
     *
     *  @param tabTag The tag identifying the content area.
     *  @param element The element to be added to the content area.
     *  @exception IllegalActionException If the content area is not set.
     */
    public void addElement(String tabTag, PositionableElement element)
            throws IllegalActionException {
        _contents.get(tabTag).addContent(element);
    }

    /** Remove an element from all content areas.
     *
     *  @param element The element to be removed.
     */
    public void removeElement(PositionableElement element) {
        for (TabDefinition tab : _contents.values()) {
            if (tab.getContent() == null) {
                continue;
            }

            try {
                tab.removeContent(element);
            } catch (IllegalActionException e) {
                // Tab didn't have that element, moving on.
            }
        }
    }

    /** Get a content area based on a unique tag value.
     *
     *  @param tabTag The tag that identifies a content area.
     *  @return The content area object.
     */
    public Object getContent(String tabTag) {
        return _contents.get(tabTag).getContent();
    }

    /** Get all content areas as a list. This list is ordered based in the
     *  ordering in the original model.
     *
     *  @return The ordered list of content areas.
     */
    public ArrayList<TabDefinition> getAllTabs() {
        ArrayList<TabDefinition> tabs = new ArrayList<TabDefinition>();

        for (String tag : _order) {
            tabs.add(_contents.get(tag));
        }

        return tabs;
    }

    /** Remove a content area from the contents.
     *
     *  @param tag The identifier of the tab to be removed.
     */
    public void removeTab(String tag) {
        try {
            _contents.get(tag).getTabAttribute().setContainer(null);
        } catch (IllegalActionException e) {
            // This can't happen since we are removing the tab.
        } catch (NameDuplicationException e) {
            // This can't happen since we are removing the tab.
        }
        _order.remove(tag);
        _contents.remove(tag);

    }

    /** Rename a content area at a given position.
     *
     *  @param position The position of the tab.
     *  @param text The new title of the tab.
     *  @exception IllegalActionException If the new name is not accepted by the model.
     */
    public void setNameAt(int position, String text)
            throws IllegalActionException {
        if (position >= 0 && position < _order.size()) {
            _contents.get(_order.get(position)).setName(text);
        }
    }

    /** Clear all data.
     */
    public void clear() {
        _contents.clear();
        _order.clear();
    }

    /** Return a positionable element if the named object is in any of the tabs.
     *
     *  @param object The named object to check.
     *  @return The positionable element wrapping the named object if it's contained,
     *  null otherwise.
     */
    public PositionableElement getElement(NamedObj object) {
        for (TabDefinition tab : _contents.values()) {
            for (PositionableElement element : tab.getElements()) {
                if (element.getElement() == object) {
                    return element;
                }
            }
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the content mapping.
     *
     *  @return The mapping of tag identifiers to tab definitions.
     */
    protected HashMap<String, TabDefinition> _getContents() {
        return _contents;
    }

    /** Return the order of the contents.
     *
     *  @return The ordered list of tag identifiers.
     */
    protected ArrayList<String> _getOrder() {
        return _order;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Parse the model and create the multiple content areas. All elements that
     *  have positions defined are placed into a content area defined by the elements
     *  parameters.
     *
     *  @param contentPrototype A prototype of the content area. This
     *  prototype is used to create multiple content areas on demand.
     *  @param model The model to be parsed.
     *  @exception IllegalActionException If any of the elements can't be places
     *  into a content area.
     *  @exception NameDuplicationException If multiple elements exist with the
     *  same name. Element names within a workspace must be unique.
     */
    protected void _initializeContents(ContentPrototype contentPrototype,
            CompositeEntity model) throws IllegalActionException,
            NameDuplicationException {
        LayoutParser parser = new LayoutParser(model);

        // Add each tab to the layout.
        ArrayList<TabDefinition> tabs = null;
        tabs = parser.getTabDefinitions();

        // Create contents for each tab
        for (TabDefinition tab : tabs) {
            // Initialize content
            tab.setContent(contentPrototype.getNewInstance());

            // Store content for switching tabs
            _contents.put(tab.getTag(), tab);

            // Store the order
            _order.add(tab.getTag());
        }

        // Add the sinks to the content area
        for (EntityElement entity : parser.getPositionableEntities()) {
            _contents.get(entity.getTab()).addContent(entity);
        }

        // Add the remote attributes to the content area and attach listeners
        for (AttributeElement attribute : parser.getPositionableAttributes()) {
            _contents.get(attribute.getTab()).addContent(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map of all available identifiers to different contents. Identifiers
     *  should be unique.
     */
    private HashMap<String, TabDefinition> _contents = new HashMap<String, TabDefinition>();

    /** Store the order of the tabs by their identifiers.
     */
    private ArrayList<String> _order = new ArrayList<String>();

    /** A prototype to the content area.
     */
    private T _contentPrototype;
}
