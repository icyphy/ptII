/* The class responsible for displaying the content area and managing
   the tab creation, removal, and event propagation.

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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import ptolemy.homer.events.NonVisualContentEvent;
import ptolemy.homer.events.TabEvent;
import ptolemy.homer.events.VisualContentEvent;
import ptolemy.homer.gui.TabScenePanel;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// HomerMultiContent

/** The class responsible for displaying the content area and managing
 *  the tab creation, removal, and event propagation.
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class HomerMultiContent extends MultiContent<TabScenePanel> {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create a content container.
     *  @param contentPrototype A prototype of the content area. This
     *  prototype is used to create multiple content areas on demand.
     */
    public HomerMultiContent(TabScenePanel contentPrototype) {
        super(contentPrototype);
    }

    /** Create a content container.
     *  @param contentPrototype A prototype of the content area.
     *  This prototype is used to create multiple content areas on demand.
     *  @param model The model to be parsed.
     *  @exception IllegalActionException If any of the elements can't be
     *  placed into a content area.
     *  @exception NameDuplicationException If multiple elements exist with
     *  the same name. Element names within a workspace must be unique.
     */
    public HomerMultiContent(TabScenePanel contentPrototype,
            CompositeEntity model) throws IllegalActionException,
            NameDuplicationException {
        super(contentPrototype, model);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a NamedObj element and notify all listeners.
     *  @param element The NamedObj to be added.
     */
    public void add(NamedObj element) {
        _remoteElements.add(element);
        _nofityAllListeners(new NonVisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "add", element));
    }

    /** Add a positionable element to the selected tab.
     *  @param tabTag The tag of the target tab.
     *  @param element The positionable element.
     *  @exception IllegalActionException If the content area is not set.
     */
    @Override
    public void addElement(String tabTag, PositionableElement element)
            throws IllegalActionException {
        super.addElement(tabTag, element);
        add(element.getElement());

        _nofityAllListeners(new VisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "addElement", element));
    }

    /** Add an action listener.
     *  @param listener The listener to add.
     */
    public void addListener(ActionListener listener) {
        _listeners.add(listener);
    }

    /** Add a tab with the provided parameters.
     *  @param topLevel The container containing all tab definitions. If the container
     *  does not have the attribute that contains all tab definition information, it will be created.
     *  @param tag The tag identifier used for the content area.
     *  @param name Name of the tab. Can be used for visualization.
     *  @param content The specific content for the new tab.
     *  @return The tag identifier of the tab.
     *  @exception IllegalActionException If the name coincides with an attribute already in the container.
     *  @exception NameDuplicationException If the attribute is not of an acceptable class for the
     *  container, if the name contains a period, or if a content area with the same tag already exist.
     */
    @Override
    public String addTab(ComponentEntity topLevel, String tag, String name,
            ContentPrototype content) throws IllegalActionException,
            NameDuplicationException {

        String newTag = super.addTab(topLevel, tag, name, content);
        _nofityAllListeners(new TabEvent(this, ActionEvent.ACTION_PERFORMED,
                "addTab", newTag, name, _getOrder().size(), content));

        return newTag;
    }

    /** Clear the contents, but keep all the listeners attached.
     */
    @Override
    public void clear() {
        super.clear();

        _remoteElements.clear();
        _nofityAllListeners(new ActionEvent(this, ActionEvent.ACTION_PERFORMED,
                "clear"));
    }

    /** Determine if the NamedObj already exists.
     *  @param key The NamedObj object to look for.
     *  @return Whether or not the NamedObj is already marked as remote.
     */
    public boolean contains(NamedObj key) {
        return _remoteElements.contains(key);
    }

    /** Get the remote model elements.
     *  @return The hashset of NamedObj elements.
     */
    public HashSet<NamedObj> getRemoteElements() {
        return _remoteElements;
    }

    /** Remove the NamedObj element and notify all listeners.
     *  @param element The NamedObj to be removed.
     */
    public void remove(NamedObj element) {
        _remoteElements.remove(element);
        _nofityAllListeners(new NonVisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "remove", element));
    }

    /** Remove the positionable element and its associated NamedObj.
     *  @param element The positionable element to be removed.
     */
    @Override
    public void removeElement(PositionableElement element) {
        super.removeElement(element);
        remove(element.getElement());

        _nofityAllListeners(new VisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "removeElement", element));
    }

    /** Remove an action listener.
     *  @param listener The listener to remove.
     *  @see #addListener(ActionListener)
     */
    public void removeListener(ActionListener listener) {
        _listeners.remove(listener);
    }

    /** Remove a tab at the provided index.
     *  @param index The ordinal position of the tab to remove.
     */
    public void removeTab(int index) {
        removeTab(_getOrder().get(index));
    }

    /** Remove the tab given its tag.
     *  @param tag The identifier of the tab to be removed.
     */
    @Override
    public void removeTab(String tag) {
        ArrayList<PositionableElement> elements = (ArrayList<PositionableElement>) _getContents()
                .get(tag).getElements().clone();
        for (PositionableElement element : elements) {
            removeElement(element);
        }

        int position = _getOrder().indexOf(tag);
        _nofityAllListeners(new TabEvent(this, ActionEvent.ACTION_PERFORMED,
                "removeTab", tag, _getContents().get(tag).getName(), position,
                null));

        super.removeTab(tag);
    }

    /** Rename a content area at a given position.
     *  @param position The position of the tab.
     *  @param text The new title of the tab.
     *  @exception IllegalActionException If the new name is not accepted by the model.
     */
    @Override
    public void setNameAt(int position, String text)
            throws IllegalActionException {
        super.setNameAt(position, text);

        _nofityAllListeners(new TabEvent(this, ActionEvent.ACTION_PERFORMED,
                "renameTab", _getOrder().get(position), text, position, null));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The hashset of event listeners.
     */
    private HashSet<ActionListener> _listeners = new HashSet<ActionListener>();

    /** Complete list of all named objects executed remotely.
     */
    private HashSet<NamedObj> _remoteElements = new HashSet<NamedObj>();

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Notify all listeners that an event has happened.
     *  @param e The action that has been performed.
     */
    private void _nofityAllListeners(ActionEvent e) {
        for (ActionListener listener : _listeners) {
            listener.actionPerformed(e);
        }
    }
}
