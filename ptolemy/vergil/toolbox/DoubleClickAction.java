/* An action for double clicking a component.

 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.List;

import ptolemy.actor.gui.DoubleClickFactory;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// DoubleClickAction

/**
 An action that will handle double clicking on the current object.
 If that object contains an attribute that is an instance of EditorFactory,
 then that instance is used to create the dialog (or whatever) to configure
 the object.  Otherwise, an instance of EditParametersDialog is created.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class DoubleClickAction extends FigureAction {
    /** Construct a new configure action.
     *  @param description A description.
     */
    public DoubleClickAction(String description) {
        super(description);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a dialog to edit the target.
     *  @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
        try {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);

            NamedObj target = getTarget();

            if (target == null) {
                return;
            }

            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            Frame parent = getFrame();
            _invoke(parent, target, e);
        } catch (Throwable throwable) {
            // Giotto code generator on giotto/demo/Hierarchy/Hierarchy.xml
            // was throwing an exception here that was not being displayed
            // in the UI.
            MessageHandler.error(
                    "Failed to invoke double-clicking for the target.",
                    throwable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Execute command for double clicking the target. This may either
     *  open up a configure dialog box or run the DoubleClickFactory.invoke()
     *  method.
     *  @param parent A frame to serve as a parent for the dialog, or
     *  null if there is none.
     *  @param target The object whose parameters are to be edited.
     *  @param event The action event that triggered this, or null if
     *   none.
     */
    private void _invoke(Frame parent, NamedObj target, ActionEvent event) {
        // Use the DoubleClickFactory only if the alt key is not pressed.
        boolean altKeyPressed = false;
        if (event != null) {
            altKeyPressed = (event.getModifiers() & ActionEvent.ALT_MASK) != 0;
        }

        List attributeList = target.attributeList(DoubleClickFactory.class);
        if (!altKeyPressed && attributeList.size() > 0) {
            DoubleClickFactory factory = (DoubleClickFactory) attributeList
                    .get(0);
            factory.invoke(target, parent);
        } else {
            List editorList = target.attributeList(EditorFactory.class);

            // Open up the user-customized editor if either Alt key
            // is not pressed, or there is already a double-click
            // factory (whether Alt is pressed or not).
            if (editorList.size() > 0
                    && (!altKeyPressed || attributeList.size() > 0)) {

                EditorFactory factory = (EditorFactory) editorList.get(0);
                factory.createEditor(target, parent);
            } else {
                new EditParametersDialog(parent, target);
            }
        }
    }
}
