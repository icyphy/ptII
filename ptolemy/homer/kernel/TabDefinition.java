/* Define tab properties and content. 
 
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

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// TabDefinition

/** Define tab properties and content. 
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class TabDefinition {

    ///////////////////////////////////////////////////////////////////
    ////                constructor                                ////

    /** Tab element to hold the content for visual representation.
     * 
     *  @param tag The tag used to identify this tab.
     *  @param name The name of the tab. This will used in the user
     *  interface. 
     */
    public TabDefinition(String tag, String name) {
        _tag = tag;
        _name = name;
    }

    ///////////////////////////////////////////////////////////////////
    ////                public methods                             ////

    /** Get the tag used to identify the tab.
     *  @return The tag of the tab.
     */
    public String getTag() {
        return _tag;
    }

    /** Get the name of the tab.
     *  @return The name of the tab.
     */
    public String getName() {
        return _name;
    }

    /** Return the content area of the tab.
     * 
     * @return The content area of the tab, or null if it has not been set.
     */
    public Object getContent() {
        if (_content == null) {
            return null;
        }
        return _content.getContent();
    }

    /** Set the content area of the tab.
     * 
     *  @param content The content to be used in the tab.
     */
    public void setContent(ContentPrototype content) {
        _content = content;
    }

    /** Add an element to the contents of this tab. The element has to have
     *  at least the location defined.
     * 
     *  @param element Ptolemy element with location defined.
     *  @throws IllegalActionException If the element cannot be added to the
     *  tab content area, or if the tab content area has not been set.
     */
    public void addContent(PositionableElement element)
            throws IllegalActionException {
        if (_content == null) {
            throw new IllegalActionException("Content are must be set first");
        }
        _elements.add(element);
        // Add representation to the tab contents
        _content.add(element);
    }

    /** Remove an element to the contents of this tab.
     * 
     *  @param element Ptolemy element with location defined.
     *  @throws IllegalActionException If the element cannot be removed from 
     *  the tab content area, or if the tab content area has not been set.
     */
    public void removeContent(PositionableElement element)
            throws IllegalActionException {
        if (_content == null) {
            throw new IllegalActionException("Content are must be set first");
        }
        _elements.remove(element);
        // Remove representation to the tab contents
        _content.remove(element);
    }

    /** Get all the elements on this tab.
     * 
     *  @return List of all elements kept on this tab.
     */
    public ArrayList<PositionableElement> getElements() {
        return _elements;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The tag used to identify the tab.
     */
    private String _tag;

    /** The name of the tab.
     */
    private String _name;

    /** The complete content of the tab.
     */
    private ContentPrototype _content = null;
    
    /** List of elements on the tab. 
     */
    private ArrayList<PositionableElement> _elements = new ArrayList<PositionableElement>();
}
