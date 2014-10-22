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

import java.awt.Frame;

import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.controller.ModelParameter;
import ptolemy.actor.gt.controller.TransformationAttribute;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.data.BooleanToken;
import ptolemy.domains.ptera.kernel.PteraModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttributeEditorFactory extends EditorFactory {

    public TransformationAttributeEditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    @Override
    public void createEditor(NamedObj object, final Frame parent) {
        TransformationAttribute attribute = (TransformationAttribute) object;
        try {
            if (!((BooleanToken) attribute.condition.getToken()).booleanValue()) {
                return;
            }
        } catch (Throwable t) {
            throw new InternalErrorException(null, t, "Unable to evaluate "
                    + "application condition \""
                    + attribute.condition.getExpression() + "\".");
        }
        BasicGraphFrame frame = (BasicGraphFrame) parent;
        CompositeEntity model = null;
        try {
            model = (CompositeEntity) GTTools.cleanupModel(frame.getModel());
        } catch (IllegalActionException e) {
            throw new InternalErrorException(null, e, "Unable to clean up "
                    + "model.");
        }
        if (model != null) {
            new ExecutionThread(attribute, model, frame).start();
        }
    }

    public static class ExecutionThread extends Thread {

        @Override
        public void run() {
            Manager manager = _attribute.getModelUpdater().getManager();
            manager.addExecutionListener(_listener);
            try {
                manager.execute();
            } catch (Throwable t) {
                MessageHandler.error("Error while executing the "
                        + "transformation model.", t);
            } finally {
                manager.removeExecutionListener(_listener);
            }
        }

        ExecutionThread(TransformationAttribute attribute,
                CompositeEntity model, BasicGraphFrame frame) {
            _attribute = attribute;
            _model = model;
            _frame = frame;
            _listener = new TransformationListener(
                    _attribute.getModelUpdater(), _model, _frame);
        }

        private TransformationAttribute _attribute;

        private BasicGraphFrame _frame;

        private TransformationListener _listener;

        private CompositeEntity _model;
    }

    public static class TransformationListener implements ExecutionListener {

        public TransformationListener(PteraModalModel transformation,
                CompositeEntity model, BasicGraphFrame frame) {
            _transformation = transformation;
            _model = model;
            _frame = frame;
        }

        @Override
        public void executionError(Manager manager, Throwable throwable) {
            _frame.report("");
        }

        @Override
        public void executionFinished(Manager manager) {
            _frame.setModified(true);
            _frame.report("");
            GTFrameTools.changeModel(_frame, _model, true, true);
        }

        @Override
        public void managerStateChanged(Manager manager) {
            if (manager.getState() == Manager.INITIALIZING) {
                _frame.report("Applying model transformation...");
                ModelParameter modelAttribute = (ModelParameter) _transformation
                        .getController().getAttribute("Model");
                modelAttribute.setModel(_model);
            }
        }

        protected BasicGraphFrame _frame;

        protected CompositeEntity _model;

        protected PteraModalModel _transformation;
    }
}
