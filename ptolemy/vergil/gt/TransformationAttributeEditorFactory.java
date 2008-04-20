/*

 Copyright (c) 1997-2005 The Regents of the University of California.
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

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.lang.ref.WeakReference;

import ptolemy.actor.Manager;
import ptolemy.actor.gt.ToplevelTransformer;
import ptolemy.actor.gt.TransformationAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.ActorToken;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.basic.BasicGraphFrame;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttributeEditorFactory extends EditorFactory {

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public TransformationAttributeEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     *  @param object
     *  @param parent
     */
    @SuppressWarnings("unchecked")
    public void createEditor(NamedObj object, Frame parent) {
        TransformationAttribute attribute = (TransformationAttribute) object;
        BasicGraphFrame actorFrame = (BasicGraphFrame) parent;
        Configuration configuration = actorFrame.getConfiguration();
        try {
            ToplevelTransformer transformer = TransformationAttributeController
                    ._getTransformer(attribute);
            Tableau tableau = configuration.openModel(transformer);
            ActorGraphFrame frame = (ActorGraphFrame) tableau.getFrame();

            ActorFrameListener listener = new ActorFrameListener(actorFrame,
                    frame);
            listener._addListeners();

            attribute.transformer.setTransformer(transformer);

            transformer.setApplicabilityParameter(attribute.applicability);
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Unable to create " +
                    "transformation editor for " + object.getName());
        }
    }

    private static class ActorFrameListener implements WindowListener,
    ptolemy.actor.ExecutionListener, ChangeListener {

        public void changeExecuted(ChangeRequest change) {
            if (_child.isModified()) {
                _parent.setModified(true);
                _child.setModified(false);
            }
        }

        public void changeFailed(ChangeRequest change, Exception exception) {
        }

        public void executionError(Manager manager, Throwable throwable) {
        }

        public void executionFinished(Manager manager) {
            TransformationAttributeController._applyTransformationResult(
                    (ToplevelTransformer) _child.getModel(), _parent);
        }

        public void managerStateChanged(Manager manager) {
            if (manager.getState() == Manager.PREINITIALIZING) {
                try {
                    ((ToplevelTransformer) _child.getModel()).setInputToken(
                            new ActorToken((Entity) _parent.getModel()));
                } catch (IllegalActionException e) {
                    throw new InternalErrorException("Unable to initialize " +
                            "actor token.");
                }
            }
        }

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
            if (e.getWindow() == _child) {
                _removeListeners();
            }
        }

        public void windowClosing(WindowEvent e) {
            if (e.getWindow() == _parent) {
                _child.close();
            }
        }

        public void windowDeactivated(WindowEvent e) {
        }

        public void windowDeiconified(WindowEvent e) {
        }

        public void windowIconified(WindowEvent e) {
        }

        public void windowOpened(WindowEvent e) {
        }

        ActorFrameListener(BasicGraphFrame parent, ActorGraphFrame child)
        throws IllegalActionException, NameDuplicationException {
            _parent = parent;
            _child = child;
        }

        @SuppressWarnings("unchecked")
        private void _addListeners() {
            _parent.addWindowListener(this);
            _child.addWindowListener(this);

            ToplevelTransformer transformer =
                (ToplevelTransformer) _child.getModel();
            transformer.getChangeListeners().add(
                    new WeakReference<ActorFrameListener>(this));
            transformer.getManager().addExecutionListener(this);
        }

        private void _removeListeners() {
            _parent.removeWindowListener(this);
            _child.removeWindowListener(this);

            ToplevelTransformer transformer =
                (ToplevelTransformer) _child.getModel();
            transformer.removeChangeListener(this);
            transformer.getManager().removeExecutionListener(this);
        }

        private ActorGraphFrame _child;

        private BasicGraphFrame _parent;
    }
}
