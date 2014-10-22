/* A tableau representing a text window in an external text editor.

 Copyright (c) 1998-2014 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.gui;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ExternalTextTableau

/**
 A tableau representing an external text editor (for now emacs only, with
 the gnuserv package installed).<p>

 This is just a demo form. TextEditorTableau should really be made
 abstract and have different implementations depending on user
 preferences. ExternalTextTableau should not really derive from the java
 swing Frame based TextEditorTableau. The interface of the abstract
 TextEditorTableau should support both swing JFrame based editor and
 external text editor classes.

 Note that one could send signals (events) back to this class for
 example if the buffer associated with this "tableau" is deleted or
 updated or "Saved As". This could be achieved by adding special "hooks"
 to emacs' file operation hook lists...

 @author  Zoltan Kemenczy, Research in Motion Limited
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ExternalTextTableau extends TextEditorTableau {
    /** Construct a new tableau for the model represented by the given effigy.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container does not accept
     *   this entity (this should not occur).
     *  @exception NameDuplicationException If the name coincides with an
     *   attribute already in the container.
     */
    public ExternalTextTableau(TextEffigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Make the tableau editable or uneditable.  Notice that this does
     *  not change whether the effigy is modifiable, so other tableaux
     *  on the same effigy may still modify the associated file.
     *  @param flag False to make the tableau uneditable.
     */
    @Override
    public void setEditable(boolean flag) {
        ((ExternalTextEffigy) getContainer()).setModifiable(flag);
    }

    /** Make this tableau visible - by calling show() on the container
     * (ExternalTextEffigy). */
    @Override
    public void show() {
        ((ExternalTextEffigy) getContainer()).show();
    }
}
