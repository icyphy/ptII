/* The action to listen to debug messages of a NamedObj.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

import ptolemy.actor.ActorExecutionAspect;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.DebugListenerTableau;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ExecutionAspectPlotterEditorFactory;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TextEffigy;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphController;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.toolbox.FigureAction;

///////////////////////////////////////////////////////////////////
//// ListenToAction

/** An action to listen to debug messages in the NamedObj.
 *  This is static so that other classes can use it.
 *
 *  @author Man-Kit Leung
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (mankit)
 *  @Pt.AcceptedRating Red (mankit)
 */
@SuppressWarnings("serial")
public class ListenToAction extends FigureAction {
    /** Construct an action that listens to NamedObj messages.
     *  @param controller The corresponding controller.
     *  @param componentType A String that names the listened to component.
     */
    public ListenToAction(BasicGraphController controller, String componentType) {
        super("Listen to " + componentType);
        _controller = controller;
    }

    /** Construct an action that listens to NamedObj messages.
     *  @param target The target
     *  @param controller The corresponding controller.
     */
    public ListenToAction(NamedObj target, BasicGraphController controller) {
        super("Listen to " + target.getName());
        _target = target;
        _controller = controller;
    }

    /** Open a TextEffigy that displays debug messages.
     *  @param event The action event, used to determine which entity
     *  was selected for the listen to NamedObj action
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (_configuration == null) {
            MessageHandler
            .error("Cannot listen to component without a configuration.");
            return;
        }

        // Determine which entity was selected for the listen to
        // NamedObj action.
        super.actionPerformed(event);

        NamedObj object = _target;

        if (object == null) {
            object = getTarget();
        }

        try {
            BasicGraphFrame frame = _controller.getFrame();
            Tableau tableau = frame.getTableau();

            // effigy is the whole model.
            Effigy effigy = (Effigy) tableau.getContainer();

            // We want to open a new window that behaves as a
            // child of the model window.  So, we create a new text
            // effigy inside this one.  Specify model's effigy as
            // a container for this new effigy.
            Effigy textEffigy = new TextEffigy(effigy,
                    effigy.uniqueName("debugListener" + object.getName()));

            DebugListenerTableau debugTableau = new DebugListenerTableau(
                    textEffigy, textEffigy.uniqueName("debugListener"
                            + object.getName()));
            debugTableau.setDebuggable(object);

            // If the actor is an ExecutionAspect, open Plot as well.
            if (object instanceof ActorExecutionAspect) {
                //Effigy plotEffigy = new
                ExecutionAspectPlotterEditorFactory factory = new ExecutionAspectPlotterEditorFactory(
                        object, object.uniqueName("_editorFactory"));

                ((ActorExecutionAspect) object).addExecutingListener(factory);
                factory.createEditor(object, this.getFrame());
            }
        } catch (KernelException ex) {
            MessageHandler.error("Failed to create debug listener.", ex);
        }
    }

    /** Set the configuration for use by the help screen.
     *  @param configuration The configuration.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    private Configuration _configuration;

    private BasicGraphController _controller;

    private NamedObj _target;
}
