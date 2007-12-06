/* An attribute that produces a custom node controller that highlights
 * downstream actors.

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
package ptolemy.domains.curriculum;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.ColorAttribute;
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
//// DependencyHighlighter

/**
 This is an attribute that produces a custom node controller that adds
 context menu commands to highlight dependents and prerequisites.

 <p>The preferred way to use this attribute is to add it to an
 <code><i>Foo</i>Icon.xml</code> file instead of adding it directly to the
 <code><i>Foo</i>.java</code> file.  The reason is that if this attribute
 is in <code><i>Foo</i>Icon.xml</code>, then the actor can be used in
 a non-graphical context.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (red)
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
    public NamedObjController create(GraphController controller) {
        return new DependencyController(controller);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _addHighlights(NamedObj actor, StringBuffer moml,
            HashSet<NamedObj> visited, boolean forward, boolean clear) {
        if (visited.contains(actor)) {
            return;
        }
        if (actor instanceof Actor) {
            moml.append("<entity name=\"");
            moml.append(actor.getName());
            moml.append("\">");
            if (!clear) {
                moml.append(highlightColor.exportMoML("_highlightColor"));
            } else {
                moml.append("<deleteProperty name=\"_highlightColor\"/>");
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
                    // as the current port.
                    if (port.isInput() && !otherPort.isOutput()
                            || port.isOutput() && !otherPort.isInput()) {
                        continue;
                    }
                    NamedObj higherActor = otherPort.getContainer();
                    _addHighlights(higherActor, moml, visited, forward, clear);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A node controller for dependency highlights.
     */
    public class DependencyController extends AttributeController {

        /** Create a node controller that provideds interaction with
         *  the dependency highlights.
         *  @param controller The associated graph controller.
         */
        public DependencyController(GraphController controller) {
            super(controller);

            HighlightDependents highlight = new HighlightDependents(
                    "Highlight dependents", true, false);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(highlight));

            HighlightDependents clear1 = new HighlightDependents(
                    "Clear dependents", true, true);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clear1));

            HighlightDependents prerequisites = new HighlightDependents(
                    "Highlight prerequisites", false, false);
            _menuFactory
                    .addMenuItemFactory(new MenuActionFactory(prerequisites));

            HighlightDependents clear2 = new HighlightDependents(
                    "Clear prerequisites", false, true);
            _menuFactory.addMenuItemFactory(new MenuActionFactory(clear2));
        }
    }

    private class HighlightDependents extends FigureAction {
        public HighlightDependents(String commandName, boolean forward,
                boolean clear) {
            super(commandName);
            _forward = forward;
            _clear = clear;
        }

        public void actionPerformed(ActionEvent e) {
            // Determine which entity was selected for the create instance action.
            super.actionPerformed(e);

            NamedObj actor = getTarget();
            StringBuffer moml = new StringBuffer("<group>");
            HashSet<NamedObj> visited = new HashSet<NamedObj>();
            _addHighlights(actor, moml, visited, _forward, _clear);
            moml.append("</group>");
            actor.requestChange(new MoMLChangeRequest(this, actor
                    .getContainer(), moml.toString()));
        }

        private boolean _forward, _clear;
    }
}
