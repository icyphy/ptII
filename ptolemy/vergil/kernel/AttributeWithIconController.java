/* The node controller for states.

 Copyright (c) 1998-2016 The Regents of the University of California.
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
package ptolemy.vergil.kernel;

import java.awt.event.ActionEvent;
import java.util.List;

import diva.graph.GraphController;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.toolbox.FigureAction;

///////////////////////////////////////////////////////////////////
//// AttributeWithIconController

/**
 This class provides interaction with nodes that represent that can have
 custom icons. It adds context menu items to edit the custom icons.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class AttributeWithIconController extends AttributeController {

    /** Create a controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public AttributeWithIconController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public AttributeWithIconController(GraphController controller, Access access) {
        super(controller, access);
        _appearanceMenuActionFactory.addAction(_editIconAction);
        _appearanceMenuActionFactory.addAction(_removeIconAction);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The edit custom icon action. */
    protected EditIconAction _editIconAction = new EditIconAction();

    /** The remove custom icon action. */
    protected RemoveIconAction _removeIconAction = new RemoveIconAction();

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Customize the icon of the attribute.
     */
    @SuppressWarnings("serial")
    private class EditIconAction extends FigureAction {

        /** Create an action to edit an icon. */
        public EditIconAction() {
            super("Edit Custom Icon");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Process the edit icon command.
         *  @param e The event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            if (_configuration == null) {
                MessageHandler.error("Cannot edit icon without a "
                        + "configuration.");
                return;
            }

            // Determine which entity was selected for the action.
            super.actionPerformed(e);

            final NamedObj object = getTarget();

            // Do this as a change request since it may add a new icon.
            ChangeRequest request = new ChangeRequest(this, "Edit Custom Icon") {
                @Override
                protected void _execute() throws Exception {
                    EditorIcon icon = null;
                    List<EditorIcon> iconList = object
                            .attributeList(EditorIcon.class);
                    for (EditorIcon oldIcon : iconList) {
                        if (oldIcon.getClass().equals(EditorIcon.class)) {
                            icon = oldIcon;
                            break;
                        }
                    }

                    if (icon == null) {
                        icon = new EditorIcon(object, "_icon");
                        Parameter hideName = (Parameter) object
                                .getAttribute("_hideName");
                        if (((BooleanToken) hideName.getToken()).booleanValue()) {
                            hideName.setToken(BooleanToken.FALSE);
                        }
                    }

                    _configuration.openModel(icon);
                }
            };

            object.requestChange(request);
        }
    }

    /** Action to remove a custom icon.
     */
    @SuppressWarnings("serial")
    private static class RemoveIconAction extends FigureAction {

        /** Create an action to remove the custom icon. */
        public RemoveIconAction() {
            super("Remove Custom Icon");
        }

        ///////////////////////////////////////////////////////////////////
        ////                         public methods                    ////

        /** Process the remove icon command.
         *  @param e The event.
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj object = getTarget();
            EditorIcon icon = null;
            List<EditorIcon> iconList = object.attributeList(EditorIcon.class);
            for (EditorIcon oldIcon : iconList) {
                if (oldIcon.getClass().equals(EditorIcon.class)) {
                    icon = oldIcon;
                    break;
                }
            }

            if (icon != null) {
                String moml = "<deleteProperty name=\"" + icon.getName()
                        + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, object,
                        moml);
                object.requestChange(request);
            }
        }
    }
}
