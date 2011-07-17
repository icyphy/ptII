/* Store a model's element representations on multiple contents.

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

///////////////////////////////////////////////////////////////////
//// MultiContent

/** Store a model's element representations on multiple contents.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

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
    ////                public methods                             ////

    /** Add a new content area to the MultiContent with a specific content.
     *  The new content area will be the last in order.
     *
     *  @param tabTag The tag identifier used for the content area.
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @param content The specific content for the new tab. 
     *  @exception IllegalActionException If a content area with the same tag
     *  already exist. 
     */
    public void addTab(String tabTag, String tabName, ContentPrototype content)
            throws IllegalActionException {
        if (_contents.containsKey(tabTag)) {
            throw new IllegalActionException(
                    "A content area with the identifier " + tabTag
                            + " already exists.");
        }

        TabDefinition tabDefinition = new TabDefinition(tabTag, tabName);
        tabDefinition.setContent(content);
        _contents.put(tabTag, tabDefinition);
        _order.add(tabTag);
    }

    /** Add a new content area to the MultiContent. The new content area will
     *  be the last in order.
     *
     *  @param tabTag The tag identifier used for the content area.
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @exception IllegalActionException If a content area with the same tag
     *  already exist. 
     */
    public void addTab(String tabTag, String tabName)
            throws IllegalActionException {
        addTab(tabTag, tabName, _contentPrototype.getNewInstance());
    }

    /** Add a new content area to the MultiContent. The new content area will
     *  be the last in order.
     *
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @param content The content to use in the new tab.
     *  @return The newly generated tab identifier.
     */
    public String addTab(String tabName, ContentPrototype content) {
        Random randomGenerator = new Random();
        String tag = null;
        boolean tagGenerated = false;
        while (!tagGenerated) {
            try {
                tag = String.valueOf(randomGenerator.nextLong());
                addTab(String.valueOf(tag), tabName, content);
                tagGenerated = true;
            } catch (IllegalActionException e) {
                // Do nothing, loop will try to generate a new tag.
            }
        }
        return tag;
    }

    /** Add a new content area to the MultiContent. The new content area will
     *  be the last in order.
     *
     *  @param tabName Name of the tab. Can be used for visualization.
     *  @return The newly generated tab identifier.
     */
    public String addTab(String tabName) {
        return addTab(tabName, _contentPrototype.getNewInstance());
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

    /** Removes a content area from the contents.
     * 
     *  @param tag The identifier of the tab to be removed.
     *  @return Contents of the removed content area.
     */
    public void removeTab(String tag) {
        _order.remove(tag);
        _contents.remove(tag);
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
    ////                private methods                            ////

    /** Parse the model and create the multiple content areas. All elements that
     *  have positions defined are placed into a content area defined by the elements
     *  parameters.
     *   
     *  @param contentPrototype A prototype of the content area. This
     *  prototype is used to create multiple content areas on demand.
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
    ////                private variables                          ////

    /** Map of all available identifiers to different contents. Identifiers
     *  should be unique.
     */
    protected HashMap<String, TabDefinition> _contents = new HashMap<String, TabDefinition>();

    /** Store the order of the tabs by their identifiers.
     */
    protected ArrayList<String> _order = new ArrayList<String>();

    /** A prototype to the content area.
     */
    private T _contentPrototype;
}
