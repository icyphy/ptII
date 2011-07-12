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

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public class MultiContent {

    /** 
     * Create an empty MultiContent instance.
     */
    public MultiContent() {
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
    public MultiContent(CompositeActor model, ContentPrototype contentPrototype)
            throws IllegalActionException, NameDuplicationException {
        _model = model;
        _initializeContents(contentPrototype);
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /**
     * TODO
     * @param tabName
     * @param prototype
     */
    public void addTab(String tabName, ContentPrototype prototype) {
        TabDefinition tabDefinition = new TabDefinition(tabName, tabName);
        _contents.put(prototype, tabDefinition);
        tabDefinition.setContent(prototype);
        _order.add(tabName);
    }

    /** Get a content area based on a unique tag value.
     * 
     *  @param tag The tag that identifies a content area.
     *  @return The content area oject.
     */
    public Object getContent(String tag) {
        return _contents.get(tag).getContent();
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

    /**
     * TODO
     * @param prototype
     * @return
     */
    public TabDefinition getTabDefinition(ContentPrototype prototype) {
        return _contents.get(prototype);
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
    protected void _initializeContents(ContentPrototype contentPrototype)
            throws IllegalActionException, NameDuplicationException {
        LayoutParser parser = new LayoutParser(_model);

        // Add each tab to the layout.
        ArrayList<TabDefinition> tabs = null;
        tabs = parser.getTabDefinitions();

        // Create contents for each tab
        for (TabDefinition tab : tabs) {
            // Initialize content
            ContentPrototype newInstance = contentPrototype.getNewInstance();
            tab.setContent(newInstance);

            // Store content for switching tabs
            _contents.put(newInstance, tab);

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
    protected HashMap<ContentPrototype, TabDefinition> _contents = new HashMap<ContentPrototype, TabDefinition>();

    /** Store the order of the tabs by their identifiers.
     */
    protected ArrayList<String> _order = new ArrayList<String>();

    /** The model to be parsed and used to create the contents.
     */
    protected CompositeActor _model;
}
