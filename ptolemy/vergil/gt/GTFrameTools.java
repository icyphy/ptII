/*

@Copyright (c) 2008 The Regents of the University of California.
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
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.actor.ActorEditorGraphController;
import ptolemy.vergil.actor.ActorGraphFrame;
import ptolemy.vergil.actor.ActorGraphModel;
import ptolemy.vergil.actor.ActorGraphFrame.ActorGraphPane;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.RunnableGraphController;
import ptolemy.vergil.fsm.FSMGraphController;
import ptolemy.vergil.fsm.FSMGraphFrame;
import ptolemy.vergil.fsm.FSMGraphModel;
import ptolemy.vergil.fsm.FSMGraphFrame.FSMGraphPane;
import diva.graph.GraphModel;
import diva.graph.GraphPane;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GTFrameTools {

    public static void changeModel(BasicGraphFrame frame, CompositeEntity model,
            boolean delegateUndoStack) {
        if (delegateUndoStack) {
            UndoStackAttribute oldAttribute = UndoStackAttribute.getUndoInfo(
                        frame.getModel());
            try {
                new DelegatedUndoStackAttribute(model, "_undoInfo",
                        oldAttribute);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        }

        ModelChangeRequest request = new ModelChangeRequest(null, frame, model);
        request.setUndoable(true);
        model.requestChange(request);
    }

    public static class DelegatedUndoStackAttribute extends UndoStackAttribute {

        public DelegatedUndoStackAttribute(NamedObj container, String name,
                UndoStackAttribute oldAttribute)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            if (oldAttribute instanceof DelegatedUndoStackAttribute) {
                _oldAttribute = ((DelegatedUndoStackAttribute) oldAttribute)
                        ._oldAttribute;
            } else {
                _oldAttribute = oldAttribute;
            }
        }

        public void mergeTopTwo() {
            _oldAttribute.mergeTopTwo();
        }

        public void push(UndoAction action) {
            _oldAttribute.push(action);
        }

        public void redo() throws Exception {
            _oldAttribute.redo();
        }

        public void undo() throws Exception {
            _oldAttribute.undo();
        }

        private UndoStackAttribute _oldAttribute;
    }

    public static class ModelChangeRequest extends ChangeRequest {

        public ModelChangeRequest(Object originator, BasicGraphFrame frame,
                CompositeEntity model) {
            super(originator, "Change the model in the frame.");
            _frame = frame;
            _model = model;
        }

        public void setUndoable(boolean undoable) {
            _undoable = undoable;
        }

        protected void _execute() throws Exception {
            _oldModel = (CompositeEntity) _frame.getModel();
            if (_undoable) {
                UndoStackAttribute undoInfo =
                    UndoStackAttribute.getUndoInfo(_oldModel);
                undoInfo.push(new UndoAction() {
                    public void execute() throws Exception {
                        ModelChangeRequest request =
                            new ModelChangeRequest( ModelChangeRequest.this,
                                    _frame, _oldModel);
                        request.setUndoable(true);
                        request.execute();
                    }
                });
            }
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Workspace workspace = _model.workspace();
                    try {
                        workspace.getWriteAccess();
                        Point2D center = _frame.getCenter();
                        _frame.setModel(_model);

                        PtolemyEffigy effigy =
                            (PtolemyEffigy) _frame.getEffigy();
                        effigy.setModel(_model);

                        if (_frame instanceof ActorGraphFrame) {
                            ActorEditorGraphController controller =
                                (ActorEditorGraphController) _frame.getJGraph()
                                .getGraphPane().getGraphController();
                            ActorGraphModel graphModel =
                                new ActorGraphModel(_model);
                            _frame.getJGraph().setGraphPane(new ActorGraphPane(
                                    controller, graphModel, _model));
                        } else if (_frame instanceof FSMGraphFrame) {
                            FSMGraphController controller =
                                (FSMGraphController) _frame.getJGraph()
                                .getGraphPane().getGraphController();
                            FSMGraphModel graphModel = new FSMGraphModel(
                                    (CompositeEntity) _model);
                            _frame.getJGraph().setGraphPane(new FSMGraphPane(
                                    controller, graphModel, _model));
                        } else if (_frame instanceof GTFrame) {
                            RunnableGraphController controller =
                                (RunnableGraphController) _frame.getJGraph()
                                .getGraphPane().getGraphController();
                            GraphModel graphModel = _frame.getJGraph()
                                .getGraphPane().getGraphModel();
                            if (graphModel instanceof FSMGraphModel) {
                                graphModel = new FSMGraphModel(_model);
                            } else {
                                graphModel = new ActorGraphModel(_model);
                            }
                            _frame.getJGraph().setGraphPane(new GraphPane(
                                    controller, graphModel));
                        } else {
                            throw new InternalErrorException("Unable to " +
                                    "change the model in frame " +
                                    _frame.getClass().getName());
                        }
                        _frame.getJGraph().repaint();
                        _frame.setCenter(center);
                        _frame.changeExecuted(null);
                    } finally {
                        workspace.doneWriting();
                    }
                }
            });
        }

        private BasicGraphFrame _frame;

        private CompositeEntity _model;

        private CompositeEntity _oldModel;

        private boolean _undoable = false;
    }
}
