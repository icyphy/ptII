/* An event notifying about changed in the tabs.

Copyright (c) 2000-2010 The Regents of the University of California.
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
package ptolemy.homer.events;

import java.awt.event.ActionEvent;

import ptolemy.homer.kernel.ContentPrototype;

/**
 * An event notifying about changed in the tabs.
 * @author Peter Foldes
 * @version $Id$ 
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (pdf)
 * @Pt.AcceptedRating Red (pdf)
 */
public class TabEvent extends ActionEvent {

    /**
     * Create a new instance of the event.
     * @param source The soruce of the event.
     * @param id The id of the event.
     * @param command The command of the event.
     * @param tag The tag of the tab.
     * @param name The name of the tab.
     * @param position The position of the tab
     * @param content The content of the tab.
     */
    public TabEvent(Object source, int id, String command, String tag,
            String name, int position, ContentPrototype content) {
        super(source, id, command);
        _tag = tag;
        _name = name;
        _position = position;
        _content = content;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Return the tag of the tab.
     * @return the tag of the tab.
     */
    public String getTag() {
        return _tag;
    }

    /**
     * Return the name of the tab.
     * @return The name of the tab.
     */
    public String getName() {
        return _name;
    }

    /**
     * Return the position of the tab.
     * @return the position of the tab.
     */
    public int getPosition() {
        return _position;
    }

    /**
     * Return the content of the tab.
     * @return the content of the tab.
     */
    public ContentPrototype getContent() {
        return _content;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /**
     * The tag of the tab.
     */
    private final String _tag;
    /**
     * The name of the tab.
     */
    private final String _name;
    /**
     * The position of the tab.
     */
    private final int _position;
    /**
     * The content of the tab.
     */
    private final ContentPrototype _content;

}
