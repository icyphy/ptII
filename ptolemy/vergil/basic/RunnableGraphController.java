/* The graph controller for models that can be executed.

 Copyright (c) 1999-2014 The Regents of the University of California.
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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.TypeConflictException;
import ptolemy.graph.Inequality;
import ptolemy.graph.InequalityTerm;
import ptolemy.gui.UndeferredGraphicalMessageHandler;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// RunnableGraphController

/**
 A graph controller for models that can be executed.
 This controller provides toolbar buttons for executing
 the model.  If the model being controlled is not a top-level
 model, then execution commands are propagated up to the top level.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public abstract class RunnableGraphController extends WithIconGraphController
implements ExecutionListener {
    /** Create a new controller.
     */
    public RunnableGraphController() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add execution commands to the toolbar.
     *  @param menu The menu to add to, which is ignored.
     *  @param toolbar The toolbar to add to, or null if none.
     */
    @Override
    public void addToMenuAndToolbar(JMenu menu, JToolBar toolbar) {
        super.addToMenuAndToolbar(menu, toolbar);
        GUIUtilities.addToolBarButton(toolbar, _runModelAction);
        GUIUtilities.addToolBarButton(toolbar, _pauseModelAction);
        GUIUtilities.addToolBarButton(toolbar, _stopModelAction);
        ((ButtonFigureAction) _stopModelAction).setSelected(true);
    }

    /** Report that an execution error has occurred.  This method
     *  is called by the specified manager.
     *  @param manager The manager calling this method.
     *  @param throwable The throwable being reported.
     */
    @Override
    public void executionError(Manager manager, Throwable throwable) {
        getFrame().report(throwable);

        if (throwable instanceof KernelException) {
            highlightError(((KernelException) throwable).getNameable1());
            highlightError(((KernelException) throwable).getNameable2());

            // Type conflict errors need to be handled specially.
            if (throwable instanceof TypeConflictException) {
                Iterator<?> inequalities = ((TypeConflictException) throwable)
                        .inequalityList().iterator();
                while (inequalities.hasNext()) {
                    Object item = inequalities.next();
                    if (item instanceof InequalityTerm) {
                        Object object = ((InequalityTerm) item)
                                .getAssociatedObject();
                        if (object instanceof Nameable) {
                            highlightError((Nameable) object);
                        }
                    } else if (item instanceof Inequality) {
                        Inequality inequality = (Inequality) item;
                        InequalityTerm term = inequality.getGreaterTerm();
                        if (term != null) {
                            Object object = term.getAssociatedObject();
                            if (object instanceof Nameable) {
                                highlightError((Nameable) object);
                            }
                        }
                        term = inequality.getLesserTerm();
                        if (term != null) {
                            Object object = term.getAssociatedObject();
                            if (object instanceof Nameable) {
                                highlightError((Nameable) object);
                            }
                        }
                    }
                }
            }
        } else if (throwable instanceof KernelRuntimeException) {
            Iterator<?> causes = ((KernelRuntimeException) throwable)
                    .getNameables().iterator();
            while (causes.hasNext()) {
                highlightError((Nameable) causes.next());
            }
        }
    }

    /** Report that execution of the model has finished.
     *  @param manager The manager calling this method.
     */
    @Override
    public synchronized void executionFinished(Manager manager) {
        // Display the amount of time and memory used.
        // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5571
        // There is similar code in ptolemy/actor/gui/ModelFrame.java
        String statusMessage = manager.getStatusMessage();
        if (!statusMessage.isEmpty()) {
            statusMessage = ": " + statusMessage;
        } else {
            statusMessage = ".";
        }
        getFrame().report("execution finished" + statusMessage);
    }

    /** Report that a manager state has changed.
     *  This method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    @Override
    public void managerStateChanged(Manager manager) {
        Manager.State newState = manager.getState();

        if (newState != _previousState) {
            // In case there were errors, we
            // clear any error reporting highlights that may be present.
            // Do this only if there are actually error highlights because
            // it triggers a repaint.
            // We also request the extra repaint when the new state becomes
            // idle (and the previous one was something else), since we want
            // to update visual effects that might have changed by running the
            // model.
            if (newState == Manager.IDLE || _areThereActiveErrorHighlights()) {
                ChangeRequest request = _getClearAllErrorHighlightsChangeRequest();
                manager.requestChange(request);
            }

            // There is similar code in ptolemy/actor/gui/ModelFrame.java
            String statusMessage = manager.getStatusMessage();
            if (statusMessage.equals(_previousStatusMessage)) {
                _previousStatusMessage = statusMessage;
                statusMessage = "";
            } else {
                _previousStatusMessage = statusMessage;
            }

            if (!statusMessage.isEmpty()) {
                statusMessage = ": " + statusMessage;
            } else {
                statusMessage = ".";
            }
            getFrame().report(
                    manager.getState().getDescription() + statusMessage);
            _previousState = newState;

            if (newState == Manager.INITIALIZING
                    || newState == Manager.ITERATING
                    || newState == Manager.PREINITIALIZING
                    || newState == Manager.RESOLVING_TYPES
                    || newState == Manager.WRAPPING_UP
                    || newState == Manager.EXITING) {
                ((ButtonFigureAction) _runModelAction).setSelected(true);
                ((ButtonFigureAction) _pauseModelAction).setSelected(false);
                ((ButtonFigureAction) _stopModelAction).setSelected(false);
            } else if (newState == Manager.PAUSED) {
                ((ButtonFigureAction) _runModelAction).setSelected(false);
                ((ButtonFigureAction) _pauseModelAction).setSelected(true);
                ((ButtonFigureAction) _stopModelAction).setSelected(false);
            } else {
                ((ButtonFigureAction) _runModelAction).setSelected(false);
                ((ButtonFigureAction) _pauseModelAction).setSelected(false);
                ((ButtonFigureAction) _stopModelAction).setSelected(true);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Add hot keys to the actions in the given JGraph.
     *
     *  @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    protected void _addHotKeys(JGraph jgraph) {
        super._addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _runModelAction);
        GUIUtilities.addHotKey(jgraph, _pauseModelAction);
        GUIUtilities.addHotKey(jgraph, _stopModelAction);
    }

    /** Get the manager for the top-level of the associated model,
     *  if there is one, or create one if there is not.
     *  @return The manager.
     *  @exception IllegalActionException If the associated model is
     *   not a CompositeActor, or if the manager cannot be created.
     */
    protected Manager _getManager() throws IllegalActionException {
        AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) getGraphModel();
        NamedObj toplevel = graphModel.getPtolemyModel().toplevel();

        if (!(toplevel instanceof CompositeActor)) {
            throw new IllegalActionException(toplevel,
                    "Cannot get a manager because the model is not a CompositeActor.");
        }

        Manager manager = ((CompositeActor) toplevel).getManager();

        if (manager == null) {
            try {
                manager = new Manager(toplevel.workspace(), "manager");
                ((CompositeActor) toplevel).setManager(manager);
            } catch (IllegalActionException ex) {
                // Should not occur.
                throw new InternalErrorException(ex);
            }
        }

        if (manager != _manager) {
            // If there was a previous manager, unlisten.
            if (_manager != null) {
                _manager.removeExecutionListener(this);
            }

            manager.addExecutionListener(this);
            _manager = manager;
        }

        return manager;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The manager we are currently listening to. */
    private Manager _manager = null;

    /** Action for pausing the model. */
    private Action _pauseModelAction = new PauseModelAction("Pause the model");

    /** The previous state of the manager, to avoid reporting
     *  it if it hasn't changed. */
    private Manager.State _previousState;

    /** The Manager status message from the previous state.
     */
    private String _previousStatusMessage = "";

    /** Action for running the model. */
    private Action _runModelAction = new RunModelAction(
            "Run or Resume the model");

    /** Action for stopping the model. */
    private Action _stopModelAction = new StopModelAction("Stop the model");

    /** An action to run the model that includes a button. */
    @SuppressWarnings("serial")
    private class ButtonFigureAction extends FigureAction {
        public ButtonFigureAction(String description) {
            super(description);
        }

        public void setSelected(boolean state) {
            JButton button = (JButton) getValue("toolBarButton");
            button.setSelected(state);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    //// RunModelAction

    /** An action to run the model. */
    @SuppressWarnings("serial")
    private class RunModelAction extends ButtonFigureAction {
        /** Run the model without opening a run-control window.
         *  @param description The description used for menu entries and
         *   tooltips.
         */
        public RunModelAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/run.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/basic/img/run_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/basic/img/run_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/basic/img/run_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description + " (Ctrl+R)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_R, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        /** Run the model. */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            try {
                // Formerly, if the user opens up a composite actor and then
                // runs the top level and there is an error, then the composite
                // actor window pops up with the error message.  Instead
                // the current window (the top level) should stay up.
                // The problem is that when the composite actor is opened,
                // Top calls GraphicalMessageHandler.setContext().
                // Instead, if the user runs the model, we should set the
                // context to that window.
                UndeferredGraphicalMessageHandler.setContext(getFrame());
                _getManager().startRun();
            } catch (IllegalActionException ex) {
                // Model may be already running. Attempt to resume.
                try {
                    _getManager().resume();
                } catch (IllegalActionException ex1) {
                    MessageHandler.error("Failed to run/resume.", ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// PauseModelAction

    /** An action to pause the model. */
    @SuppressWarnings("serial")
    private class PauseModelAction extends ButtonFigureAction {
        /** Pause the model if it is running.
         *  @param description The description used for menu entries and
         *   tooltips.
         */
        public PauseModelAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/pause.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/basic/img/pause_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/basic/img/pause_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/basic/img/pause_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description + " (Ctrl+U)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_U, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        /** Pause the model. */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            try {
                _getManager().pause();
            } catch (IllegalActionException ex) {
                MessageHandler.error("failed to pause.", ex);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    //// StopModelAction

    /** An action to stop the model. */
    @SuppressWarnings("serial")
    private class StopModelAction extends ButtonFigureAction {
        /** Stop the model, if it is running.
         *  @param description The description used for menu entries and
         *   tooltips.
         */
        public StopModelAction(String description) {
            super(description);

            // Load the image by using the absolute path to the gif.
            // Using a relative location should work, but it does not.
            // Use the resource locator of the class.
            // For more information, see
            // jdk1.3/docs/guide/resources/resources.html
            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/basic/img/stop.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/basic/img/stop_o.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/basic/img/stop_ov.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/basic/img/stop_on.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", description + " (Ctrl+H)");
            putValue(GUIUtilities.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
                    KeyEvent.VK_H, Toolkit.getDefaultToolkit()
                    .getMenuShortcutKeyMask()));
        }

        /** Stop the model. */
        @Override
        public void actionPerformed(ActionEvent e) {
            super.actionPerformed(e);

            try {
                _getManager().stop();
            } catch (IllegalActionException ex) {
                MessageHandler.error("failed to stop.", ex);
            }
        }
    }
}
