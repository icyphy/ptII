/*

@Copyright (c) 2008-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.undo.UndoAction;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.BasicGraphPane;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.modal.FSMGraphController;
import ptolemy.vergil.modal.FSMGraphFrame;
import ptolemy.vergil.modal.FSMGraphModel;
import diva.graph.GraphModel;
import diva.graph.GraphPane;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTFrameTools {
    public static void changeModel(BasicGraphFrame frame,
            CompositeEntity model, boolean undoable, boolean delegateUndoStack) {
        changeModel(frame, model, undoable, delegateUndoStack, null);
    }

    public static void changeModel(BasicGraphFrame frame,
            CompositeEntity model, boolean undoable, boolean delegateUndoStack,
            UndoAction undoAction) {
        if (undoable && delegateUndoStack) {
            UndoStackAttribute oldAttribute = UndoStackAttribute
                    .getUndoInfo(frame.getModel());
            try {
                new DelegatedUndoStackAttribute(model, "_undoInfo",
                        oldAttribute);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        }

        ModelChangeRequest request = new ModelChangeRequest(null, frame, model,
                undoAction);
        request.setUndoable(undoable);
        model.requestChange(request);
    }

    public static void executeModelChange(final BasicGraphFrame frame,
            final CompositeEntity model) {
        frame.setModel(model);
        PtolemyEffigy effigy = (PtolemyEffigy) frame.getEffigy();
        effigy.setModel(model);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Point2D center = frame.getCenter();

                if (frame instanceof ActorGraphFrame) {
                    ActorEditorGraphController controller = (ActorEditorGraphController) frame
                            .getJGraph().getGraphPane().getGraphController();
                    ActorGraphModel graphModel = new ActorGraphModel(model);
                    frame.getJGraph().setGraphPane(
                            new BasicGraphPane(controller, graphModel, model));
                } else if (frame instanceof FSMGraphFrame) {
                    FSMGraphController controller = (FSMGraphController) frame
                            .getJGraph().getGraphPane().getGraphController();
                    FSMGraphModel graphModel = new FSMGraphModel(model);
                    frame.getJGraph().setGraphPane(
                            new BasicGraphPane(controller, graphModel, model));
                } else if (frame instanceof GTFrame) {
                    RunnableGraphController controller = (RunnableGraphController) frame
                            .getJGraph().getGraphPane().getGraphController();
                    GraphModel graphModel = frame.getJGraph().getGraphPane()
                            .getGraphModel();
                    if (graphModel instanceof FSMGraphModel) {
                        graphModel = new GTFrameController.GTFSMGraphModel(
                                model);
                    } else {
                        graphModel = new GTFrameController.GTActorGraphModel(
                                model);
                    }
                    frame.getJGraph().setGraphPane(
                            new GraphPane(controller, graphModel));
                } else {
                    throw new InternalErrorException("Unable to change the "
                            + "model in frame " + frame.getClass().getName());
                }
                frame.getJGraph().repaint();
                frame.setCenter(center);
                frame.changeExecuted(null);
            }
        });
    }

    public static class DelegatedUndoStackAttribute extends UndoStackAttribute {

        public DelegatedUndoStackAttribute(NamedObj container, String name,
                UndoStackAttribute oldAttribute) throws IllegalActionException,
                NameDuplicationException {
            super(container, name);

            if (oldAttribute instanceof DelegatedUndoStackAttribute) {
                _oldAttribute = ((DelegatedUndoStackAttribute) oldAttribute)._oldAttribute;
            } else {
                _oldAttribute = oldAttribute;
            }
        }

        @Override
        public void mergeTopTwo() {
            _oldAttribute.mergeTopTwo();
        }

        @Override
        public void push(UndoAction action) {
            _oldAttribute.push(action);
        }

        @Override
        public void redo() throws Exception {
            _oldAttribute.redo();
        }

        @Override
        public void undo() throws Exception {
            _oldAttribute.undo();
        }

        private UndoStackAttribute _oldAttribute;
    }

    public static class ModelChangeRequest extends ChangeRequest {

        public ModelChangeRequest(Object originator, BasicGraphFrame frame,
                CompositeEntity model) {
            this(originator, frame, model, null);
        }

        public ModelChangeRequest(Object originator, BasicGraphFrame frame,
                CompositeEntity model, UndoAction undoAction) {
            super(originator, "Change the model in the frame.");
            _frame = frame;
            _model = model;
            _undoAction = undoAction;
        }

        public void setUndoable(boolean undoable) {
            _undoable = undoable;
        }

        @Override
        protected void _execute() throws Exception {
            _oldModel = (CompositeEntity) _frame.getModel();
            if (_undoable) {
                UndoStackAttribute undoInfo = UndoStackAttribute
                        .getUndoInfo(_oldModel);
                if (_undoAction == null) {
                    undoInfo.push(new UndoAction() {
                        @Override
                        public void execute() throws Exception {
                            ModelChangeRequest request = new ModelChangeRequest(
                                    ModelChangeRequest.this, _frame, _oldModel);
                            request.setUndoable(true);
                            request.execute();
                        }
                    });
                } else {
                    undoInfo.push(_undoAction);
                }
            }
            executeModelChange(_frame, _model);
        }

        private BasicGraphFrame _frame;

        private CompositeEntity _model;

        private CompositeEntity _oldModel;

        private UndoAction _undoAction;

        private boolean _undoable = false;
    }
}
