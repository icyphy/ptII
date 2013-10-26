/* A tableau for editing the layout of a customizable run panel.

 Copyright (c) 2007-2013 The Regents of the University of California.
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
package ptolemy.actor.gui.run;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// LayoutTableau

/**
   A run control pane for the model.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (neuendor)
 */
public class LayoutTableau extends Tableau {
    /** Create a new run control panel for the model with the given
     *  effigy.  The tableau is itself an entity contained by the effigy
     *  and having the specified name.  The frame is not made visible
     *  automatically.  You must call show() to make it visible.
     *  @param container The containing effigy.
     *  @param name The name of this tableau within the specified effigy.
     *  @exception IllegalActionException If the tableau is not acceptable
     *   to the specified container.
     *  @param pane The pane whose layout is being edited.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public LayoutTableau(PtolemyEffigy container, String name,
            CustomizableRunPane pane) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        NamedObj model = container.getModel();

        if (!(model instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Cannot run a model that is not a CompositeActor."
                            + " It is: " + model);
        }
        try {
            RunLayoutFrame frame = new RunLayoutFrame((CompositeActor) model,
                    this, pane);
            setFrame(frame);
        } catch (IllegalActionException ex) {
            // Remove this tableau from its container.
            setContainer(null);
            throw ex;
        }
    }
}
