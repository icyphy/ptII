/* Non visual content event notifying about changes in non visual object list.

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

import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// NonVisualContentEvent

/** Non-visual content event notifying about changes in non-visual object list.
 *
 *  @author Peter Folder
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public class NonVisualContentEvent extends ActionEvent {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create a new instance of the event.
     *  @param source The source producing the event.
     *  @param id The id of the event.
     *  @param command The command of the event.
     *  @param element The affected element.
     *  @see ActionEvent
     */
    public NonVisualContentEvent(Object source, int id, String command,
            NamedObj element) {
        super(source, id, command);
        _element = element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the element affected.
     *  @return The element affected.
     */
    public NamedObj getElement() {
        return _element;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The element affected.
     */
    private NamedObj _element;
}
