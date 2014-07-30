/* An attribute that produces a custom node controller that highlights
 * downstream actors.

 Copyright (c) 2007-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.event.ActionEvent;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DialogTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorInstanceController;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;
import ptolemy.vergil.kernel.attributes.TextAttribute;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

///////////////////////////////////////////////////////////////////
//// DependencyHighlighter

/**
 This is an attribute that produces a custom node controller that adds
 context menu commands to highlight dependents and prerequisites.
 A dependent is a downstream actor, and a prerequisite is an upstream
 actor. To use this, drop it onto any actor. The context menu (right click
 or command click) acquires four additional commands to highlight or clear
 highlights on dependents or prerequisites.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class DependencyHighlighter extends NodeControllerFactory {
    /** Construct a new attribute with the given container and name.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name.
     */
    public DependencyHighlighter(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        highlightColor = new ColorAttribute(this, "highlightColor");
        // Red default.
        highlightColor.setExpression("{1.0, 0.0, 0.0, 1.0}");

        // Hide the name.
        SingletonParameter _hideName = new SingletonParameter(this, "_hideName");
        _hideName.setToken(BooleanToken.TRUE);
        _hideName.setVisibility(Settable.EXPERT);

        // The icon.
        EditorIcon _icon = new EditorIcon(this, "_icon");
        RectangleAttribute rectangle = new RectangleAttribute(_icon,
                "rectangle");
        rectangle.width.setExpression("175.0");
        rectangle.height.setExpression("20.0");
        rectangle.fillColor.setExpression("{1.0, 0.7, 0.7, 1.0}");

        Location _location = new Location(rectangle, "_location");
        _location.setExpression("-5.0, -15.0");

        TextAttribute text = new TextAttribute(_icon, "text");
        text.text.setExpression("DependencyHighlighter");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The highlight color. */
    public ColorAttribute highlightColor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a new node controller.  This base class returns an
     *  instance of IconController.  Derived
     *  classes can return some other class to customize the
     *  context menu.
     *  @param controller The associated graph controller.
     *  @return A new node controller.
     */
    @Override
    public NamedObjController create(GraphController controller) {
        return new DependencyController(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add MoML for highlights for the specified actor to the specified buffer.
     *  @param actor The actor.
     *  @param moml The string buffer into which to add the MoML for the highlights.
     *  @param visited The set of actors that have been visited.
     *  @param forward True for dependents, false for prerequisites.
     *  @param clear True to clear, false to highlight.
     */
    private void _addHighlights(NamedObj actor, StringBuffer moml,
            HashSet<NamedObj> visited, boolean forward, boolean clear) {
        if (visited.contains(actor)) {
            return;
        }
        if (actor instanceof Actor) {
            moml.append("<entity name=\"");
            moml.append(actor.getFullName());
            moml.append("\">");
            if (!clear) {
                moml.append(highlightColor.exportMoML("_highlightColor"));
            } else {
                if (actor.getAttribute("_highlightColor") != null) {
                    moml.append("<deleteProperty name=\"_highlightColor\"/>");
                }
            }
            moml.append("</entity>");

            visited.add(actor);
            Iterator ports;
            if (forward) {
                ports = ((Actor) actor).outputPortList().iterator();
            } else {
                ports = ((Actor) actor).inputPortList().iterator();
            }
            while (ports.hasNext()) {
                IOPort port = (IOPort) ports.next();
                Iterator connectedPorts = port.connectedPortList().iterator();
                while (connectedPorts.hasNext()) {
                    IOPort otherPort = (IOPort) connectedPorts.next();
                    // Skip ports with the same polarity (input or output)
                    // as the current port, or opposite polarity if the
                    // container of the port is the same as the container
                    // of the actor.
                    if (otherPort.getContainer() == actor.getContainer()) {
                        if (port.isInput() && !otherPort.isInput()
                                || port.isOutput() && !otherPort.isOutput()) {
                            continue;
                        }
                    } else {
                        if (port.isInput() && !otherPort.isOutput()
                                || port.isOutput() && !otherPort.isInput()) {
                            continue;
                        }
                    }
                    NamedObj higherActor = otherPort.getContainer();
                    _addHighlights(higherActor, moml, visited, forward, clear);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** The controller that adds commands to the context menu.
     */
    public/*static*/class DependencyController extends ActorInstanceController {
        // Findbugs suggests making this static, but if this class is static,
        // we can't reference the non-static HighlightDependents class here.

        /** Create a DependencyController that is associated with a controller.
         *  @param controller The controller.
         */
        public DependencyController(GraphController controller) {
            super(controller);

            HighlightDependents highlight = new HighlightDependents(
                    "Highlight dependents", true, false, false);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(highlight));

            // Only one menu choice for listing because the dialog has
            // checkboxes to select between dependencies and prerequisites.
            HighlightDependents listDependents = new HighlightDependents(
                    "List dependents & prereqs.", true, false, true);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(
                    listDependents));

            HighlightDependents clear1 = new HighlightDependents(
                    "Clear dependents", true, true, false);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clear1));

            HighlightDependents prerequisites = new HighlightDependents(
                    "Highlight prerequisites", false, false, false);
            _menuFactory
            .addMenuItemFactory(new MenuActionFactory(prerequisites));

            HighlightDependents clear2 = new HighlightDependents(
                    "Clear prerequisites", false, true, false);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clear2));
        }
    }

    /** The action for the commands added to the context menu.
     */
    @SuppressWarnings("serial")
    private class HighlightDependents extends FigureAction {
        /** Construct a HighlightsDependents action.
         *  @param commandName The name that appears in the menu.
         *  @param forward True if dependents are to be highlighted or listed.
         *  If false, then the the prerequisites are highlighted or listed.
         *  @param clear True if the highlight or prerequisite is to be cleared.
         *  If false, then the dependency or prerequisite is highlighted.
         *  @param list True if a list dialog is to be displayed.  If false,
         *  then the dependents or prerequisites are highlighted.
         */
        public HighlightDependents(String commandName, boolean forward,
                boolean clear, boolean list) {
            super(commandName);
            _forward = forward;
            _clear = clear;
            _list = list;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);

            NamedObj actor = getTarget();
            // If the model has not been preinitialized since the last
            // change to its structure, that must be done now for the result
            // to be accurate. This is because higher-order components
            // and Publisher and Subscriber connections may not have yet
            // been created.
            try {
                BasicGraphFrame frame = BasicGraphFrame
                        .getBasicGraphFrame(actor.toplevel());
                if (frame == null) {
                    throw new NullPointerException("The frame for "
                            + actor.toplevel().getName() + " was null?");
                } else {
                    frame.report("Preinitializing");
                    long startTime = new Date().getTime();
                    Manager.preinitializeThenWrapup((Actor) actor);
                    frame.report("Done Preinitializing: "
                            + Manager.timeAndMemory(startTime));
                }
            } catch (KernelException ex) {
                MessageHandler.error("Preinitialize failed.", ex);
                return;
            }

            if (_list) {
                // List the dependencies or prerequisites in a dialog
                // This is similar to code in BasicGraphFrame for
                // SearchResultDialog.
                Effigy effigy = Configuration.findEffigy(actor.toplevel());
                Configuration configuration = (Configuration) effigy.toplevel();

                DialogTableau dialogTableau = DialogTableau.createDialog(
                        BasicGraphFrame.getBasicGraphFrame(actor.toplevel()),
                        configuration, effigy, DependencyResultsDialog.class,
                        (Entity) actor);

                if (dialogTableau != null) {
                    dialogTableau.show();
                }

            } else {
                // Highlight or clear the dependency or prerequisite.
                StringBuffer moml = new StringBuffer("<group>");
                HashSet<NamedObj> visited = new HashSet<NamedObj>();
                _addHighlights(actor, moml, visited, _forward, _clear);
                moml.append("</group>");
                actor.requestChange(new MoMLChangeRequest(this, actor
                        .getContainer(), moml.toString()));
            }
        }

        private boolean _forward, _clear, _list;
    }
}
