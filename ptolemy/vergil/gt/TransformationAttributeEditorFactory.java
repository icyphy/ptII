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
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.ToplevelTransformer;
import ptolemy.actor.gt.TransformationAttribute;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.ActorToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.actor.ActorGraphFrame;

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
        ToplevelTransformer transformer =
            attribute.transformer.getTransformer();
        ActorGraphFrame actorFrame = (ActorGraphFrame) parent;
        Configuration configuration = actorFrame.getConfiguration();
        try {
            if (transformer == null) {
                String moml = attribute.transformer.getExpression();
                transformer = _getTransformer(moml);
            }
            Tableau tableau = configuration.openModel(transformer);
            ActorGraphFrame frame = (ActorGraphFrame) tableau.getFrame();

            ActorFrameListener listener = new ActorFrameListener(actorFrame,
                    frame);
            parent.addWindowListener(listener);
            frame.addWindowListener(listener);

            transformer.getChangeListeners().add(new WeakReference(
                    listener._modificationListener));

            Manager manager = transformer.getManager();
            if (manager == null) {
                manager = new Manager(transformer.workspace(), "_manager");
                transformer.setManager(manager);
            }
            manager.addExecutionListener(listener._executionListener);

            attribute.transformer.setTransformer(transformer);
        } catch (Exception e) {
            throw new InternalErrorException(this, e, "Unable to create " +
                    "transformation editor for " + object.getName());
        }
    }

    private ToplevelTransformer _getTransformer(String moml)
    throws IllegalActionException {
        if (moml.equals("")) {
            moml = new ToplevelTransformer().exportMoML();
        }
        MoMLParser parser = new MoMLParser();
        try {
            return (ToplevelTransformer) parser.parse(moml);
        } catch (Exception e) {
            throw new IllegalActionException(this, e,
                    "Unable to parse transformer.");
        }
    }

    private static class ActorFrameListener implements WindowListener {

        public void windowActivated(WindowEvent e) {
        }

        public void windowClosed(WindowEvent e) {
        }

        public void windowClosing(WindowEvent e) {
            if (e.getWindow() == _parent) {
                _parent.removeWindowListener(this);
                _child.removeWindowListener(this);
                _child.close();
            } else if (e.getWindow() == _child) {
                _parent.removeWindowListener(this);
                _child.removeWindowListener(this);
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

        ActorFrameListener(ActorGraphFrame parent, ActorGraphFrame child)
        throws IllegalActionException, NameDuplicationException {
            _parent = parent;
            _child = child;
            _modificationListener = new ModificationListener(parent, child);
            _executionListener = new ExecutionListener(parent, child);
        }

        private ActorGraphFrame _child;

        private ExecutionListener _executionListener;

        private ModificationListener _modificationListener;

        private ActorGraphFrame _parent;
    }

    private static class ExecutionListener
    implements ptolemy.actor.ExecutionListener {

        public ExecutionListener(ActorGraphFrame parent,
                ActorGraphFrame client) {
            _parent = parent;
            _client = client;
        }

        public void executionError(Manager manager, Throwable throwable) {
        }

        public void executionFinished(Manager manager) {
            ActorToken token = ((ToplevelTransformer) _client.getModel())
                    .getOutputToken();
            if (token == null) {
                MessageHandler.message("No output is generated.");
                return;
            }

            CompositeEntity result = (CompositeEntity) token.getEntity();
            try {
                result = (CompositeEntity) new MoMLParser().parse(
                        result.exportMoML());
            } catch (Exception e) {
                throw new InternalErrorException(null, e, "Unable to " +
                        "generate transformation result.");
            }
            GTTools.changeModel(_parent, result);
        }

        public void managerStateChanged(Manager manager) {
            if (manager.getState() == Manager.PREINITIALIZING) {
                try {
                    ((ToplevelTransformer) _client.getModel()).setInputToken(
                            new ActorToken((Entity) _parent.getModel()));
                } catch (IllegalActionException e) {
                    throw new InternalErrorException("Unable to initialize " +
                            "actor token.");
                }
            }
        }

        private ActorGraphFrame _client;

        private ActorGraphFrame _parent;
    }

    private static class ModificationListener implements ChangeListener {

        public ModificationListener(ActorGraphFrame parent,
                ActorGraphFrame client) {
            _parent = parent;
            _child = client;
        }

        public void changeExecuted(ChangeRequest change) {
            if (_child.isModified()) {
                _parent.setModified(true);
                _child.setModified(false);
            }
        }

        public void changeFailed(ChangeRequest change, Exception exception) {
        }

        private ActorGraphFrame _child;

        private ActorGraphFrame _parent;
    }
}
