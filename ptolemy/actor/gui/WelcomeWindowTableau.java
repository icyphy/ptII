/* A tableau representing an HTML Welcome Window (no menu, has don't show again)
 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// WelcomeWindowTableau

/**
 A tableau representing a rendered HTML view in a toplevel window that
 has no menu choices.

 @author  Christopher Brooks
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @see Effigy
 @see WelcomeWindow
 */
public class WelcomeWindowTableau extends HTMLViewerTableau {

    /** Construct a new tableau for the model represented by the given effigy.
     *  This creates an instance of WelcomeWindow.  It does not make the frame
     *  visible.  To do that, call show().
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public WelcomeWindowTableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        WelcomeWindow frame = new WelcomeWindow();
        setFrame(frame);
        frame.setTableau(this);
    }

    /** Handle cases where the <i>url</i> attribute is changed,
     *  yet there is no frame. If the argument is the <i>url</i> parameter,
     *  and there is no frame, then do nothing.  Otherwise, call the same
     *  method in the super class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Thrown by the parent class
     *  if the URL cannot be opened, or if the base class throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == url) {
            if (getFrame() == null) {
                return;
            }
        }
        super.attributeChanged(attribute);
    }
}
