/*

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.vergil.gt;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.controller.TransformationAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.gt.TransformationAttributeEditorFactory.TransformationListener;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.ptera.PteraGraphFrame;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttributeController extends AttributeController {

    public TransformationAttributeController(GraphController controller) {
        this(controller, FULL);
    }

    public TransformationAttributeController(GraphController controller,
            Access access) {
        super(controller, access);

        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                new LookInsideAction()));
    }

    public static class Factory extends NodeControllerFactory {

        public Factory(NamedObj container, String name)
                throws NameDuplicationException, IllegalActionException {
            super(container, name);
        }

        @Override
        public NamedObjController create(GraphController controller) {
            return new TransformationAttributeController(controller);
        }
    }

    private static class Listener extends TransformationListener implements
    ChangeListener, WindowListener {

        @Override
        public void changeExecuted(ChangeRequest change) {
            if (_child.isModified()) {
                _parent.setModified(true);
                _child.setModified(false);
            }
        }

        @Override
        public void changeFailed(ChangeRequest change, Exception exception) {
        }

        @Override
        public void managerStateChanged(Manager manager) {
            if (manager.getState() == Manager.PREINITIALIZING) {
                try {
                    _model = (CompositeEntity) GTTools.cleanupModel(_parent
                            .getModel());
                } catch (IllegalActionException e) {
                    throw new InternalErrorException(null, e, "Unable to "
                            + "clean up model.");
                }
            }
            super.managerStateChanged(manager);
        }

        @Override
        public void windowActivated(WindowEvent e) {
        }

        @Override
        public void windowClosed(WindowEvent e) {
            if (e.getWindow() == _child) {
                _removeListeners();
            }
        }

        @Override
        public void windowClosing(WindowEvent e) {
            if (e.getWindow() == _parent) {
                _child.close();
            }
        }

        @Override
        public void windowDeactivated(WindowEvent e) {
        }

        @Override
        public void windowDeiconified(WindowEvent e) {
        }

        @Override
        public void windowIconified(WindowEvent e) {
        }

        @Override
        public void windowOpened(WindowEvent e) {
        }

        Listener(PteraModalModel transformation, BasicGraphFrame parent,
                PteraGraphFrame child) throws NameDuplicationException {
            super(transformation, null, parent);
            _parent = parent;
            _child = child;
        }

        @SuppressWarnings("unchecked")
        private void _addListeners() {
            _parent.addWindowListener(this);
            _child.addWindowListener(this);

            CompositeActor toplevel = (CompositeActor) _child.getModel()
                    .toplevel();
            List<Object> changeListeners = toplevel.getChangeListeners();
            if (changeListeners == null) {
                toplevel.addChangeListener(this);
            } else {
                changeListeners.add(new WeakReference<Listener>(this));
            }
            toplevel.getManager().addExecutionListener(this);
        }

        private void _removeListeners() {
            _parent.removeWindowListener(this);
            _child.removeWindowListener(this);

            CompositeActor toplevel = (CompositeActor) _child.getModel()
                    .toplevel();
            toplevel.removeChangeListener(this);
            toplevel.getManager().removeExecutionListener(this);
        }

        private PteraGraphFrame _child;

        private BasicGraphFrame _parent;
    }

    @SuppressWarnings("serial")
    private static class LookInsideAction extends FigureAction {

        public LookInsideAction() {
            super("Open Transformation Controller");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            super.actionPerformed(event);

            TransformationAttribute attribute = (TransformationAttribute) getTarget();
            BasicGraphFrame actorFrame = (BasicGraphFrame) getFrame();
            Configuration configuration = actorFrame.getConfiguration();
            try {
                PteraModalModel modelUpdater = attribute.getModelUpdater();
                Tableau tableau = configuration.openInstance(modelUpdater);
                PteraGraphFrame frame = (PteraGraphFrame) tableau.getFrame();

                Listener listener = new Listener(modelUpdater, actorFrame,
                        frame);
                listener._addListeners();
            } catch (Exception e) {
                throw new InternalErrorException(null, e, "Unable to create "
                        + "transformation editor for " + attribute.getName());
            }
        }
    }
}
