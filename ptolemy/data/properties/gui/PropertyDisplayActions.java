/* An attribute that creates an editor to configure and run a code generator.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.data.properties.PropertySolver;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.Query;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

//////////////////////////////////////////////////////////////////////////
//// PropertyHighlighter

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class PropertyDisplayActions extends NodeControllerFactory {

    /** Construct a PropertyHighlighter with the specified container and name.
     *  @param container The container.
     *  @param name The name of the PropertyHighlighter.
     *  @exception IllegalActionException If the PropertyHighlighter is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PropertyDisplayActions(NamedObj container, String name)
    throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Return a new node controller.  This base class returns an
     *  instance of IconController.  Derived
     *  classes can return some other class to customize the
     *  context menu.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    public NamedObjController create(GraphController controller) {
        super.create(controller);
        //return new ConfigureHighlightController(controller);
        return new HighlighterController(controller);
    }

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     *  @param object The object to configure.
     *  @param parent The parent window, or null if there is none.
     */
    public void createEditor(NamedObj object, Frame parent) {

    }

    /** The action for the clear display command to be added
     *  to the context menu.
     */
    private class ClearDisplay extends FigureAction {
        public ClearDisplay() {
            super("Clear Property Display");
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj container = getContainer();
            if (container instanceof PropertySolver) {
                ((PropertySolver) container).getMoMLHandler().clearDisplay();
            }
        }
    }


    /** The action for the clear property command to be added
     *  to the context menu.
     */
    private class ClearProperty extends FigureAction {
        public ClearProperty() {
            super("Clear Property");
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj container = getContainer();
            if (container instanceof PropertySolver) {
                ((PropertySolver) container).getMoMLHandler().clearProperties();
            }
        }
    }


    /** The action for the highlight configure command to be added
     *  to the context menu.
     */
    private class ConfigureHighlightAction extends FigureAction {

        public ConfigureHighlightAction() {
            super("Property Display");
        }

        /**
         * Open the dialog for configuring the highlight color
         * for property values.
         */
        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the look inside action.
            super.actionPerformed(e);

            NamedObj target = ((PropertySolver) PropertyDisplayActions
                    .this.getContainer()).getMoMLHandler();

            // Create a dialog for configuring the object.
            // First, identify the top parent frame.
            Frame parent = getFrame();

            _openDialog(parent, target);
        }


        /** Open an edit parameters dialog.  This is a modal dialog, so
         *  this method returns only after the dialog has been dismissed.
         *  @param parent A frame to serve as a parent for the dialog, or
         *  null if there is none.
         *  @param target The object whose parameters are to be edited.
         *  @param event The action event that triggered this, or null if
         *   none.
         */
        private void _openDialog(Frame parent, NamedObj target) {
            new EditHighlightDialog(parent, target, "Highlight properties");
        }
    }


    /** The edit highlight dialog.
     */
    private static class EditHighlightDialog extends EditParametersDialog  {

        /** Construct a dialog with the specified owner and target.
         *  A "Commit" and a "Cancel" button are added to the dialog.
         *  The dialog is placed relative to the owner.
         *  @param owner The object that, per the user, appears to be
         *   generating the dialog.
         *  @param target The object whose parameters are being edited.
         *  @param label The label for the dialog box.
         */
        public EditHighlightDialog(Frame owner, NamedObj target, String label) {
            super(owner, target, label);
        }

        /** Open a dialog to add a new parameter.
         *  @param message A message to place at the top, or null if none.
         *  @param name The default name.
         *  @param defValue The default value.
         *  @param className The default class name.
         *  @return The dialog that is created.
         */
        protected ComponentDialog _openAddDialog(String message, String name,
                String defValue, String className) {
            // Create a new dialog to add a parameter, then open a new
            // EditParametersDialog.
            _query = new Query();

            if (message != null) {
                _query.setMessage(message);
            }

            _query.addLine("name", "Property Name", name);

            ComponentDialog dialog = new ComponentDialog(_owner,
                    "Add a new parameter to " + _target.getFullName(), _query, null);

            // If the OK button was pressed, then queue a mutation
            // to create the parameter.
            // A blank property name is interpreted as a cancel.
            String newName = _query.getStringValue("name");

            if (dialog.buttonPressed().equals("OK") && !newName.equals("")) {
                String moml = "<property name=\"" + newName + "\" value=\""
                + "property value\" class=\""
                + "ptolemy.kernel.util.StringAttribute" + "\"/>";

                MoMLChangeRequest request = new MoMLChangeRequest(this, _target,
                        moml);
                request.setUndoable(true);
                _target.requestChange(request);

                moml = "<property name=\"" + newName + "HighlightColor\" value=\""
                + "{1.0, 0.0, 0.0, 1.0}\" class=\""
                + "ptolemy.actor.gui.ColorAttribute" + "\"/>";

                _target.addChangeListener(this);

                request = new MoMLChangeRequest(this, _target, moml);
                request.setUndoable(true);
                _target.requestChange(request);
            }

            return dialog;
        }
    }


    /** The action for the highlight property command to be added
     *  to the context menu.
     */
    private class HighlightProperty extends FigureAction {
        public HighlightProperty() {
            super("Highlight Property");
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            NamedObj container = getContainer();
            if (container instanceof PropertySolver) {
                ((PropertySolver) container).getMoMLHandler().highlightProperties();
            }
        }
    }


    /** The controller that adds commands to the context menu.
     */
    private class HighlighterController extends AttributeController {

        /** Create a DependencyController that is associated with a controller.
         *  @param controller The controller.
         */
        public HighlighterController(GraphController controller) {
            super(controller);

            ClearProperty clearProperty = new ClearProperty();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clearProperty));

            ClearDisplay clearDisplay = new ClearDisplay();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clearDisplay));

            ShowProperty showProperty = new ShowProperty();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(showProperty));

            HighlightProperty highlightProperty = new HighlightProperty();
            _menuFactory.addMenuItemFactory(new MenuActionFactory(highlightProperty));

            ConfigureHighlightAction highlight = new ConfigureHighlightAction();
            _configureMenuFactory.addAction(highlight, "Configure");
        }
    }


    /** The action for the show property command to be added
     *  to the context menu.
     */
    private class ShowProperty extends FigureAction {
        public ShowProperty() {
            super("Show Property");
        }

        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);
            NamedObj container = getContainer();
            if (container instanceof PropertySolver) {
                ((PropertySolver) container).getMoMLHandler().showProperties();
            }
        }
    }
}
