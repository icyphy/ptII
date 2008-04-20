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

import java.awt.event.ActionEvent;

import ptolemy.actor.Manager;
import ptolemy.actor.gt.ToplevelTransformer;
import ptolemy.actor.gt.TransformationAttribute;
import ptolemy.data.ActorToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationAttributeController extends AttributeController {

    /**
     * @param controller
     */
    public TransformationAttributeController(GraphController controller) {
        this(controller, FULL);
    }

    /**
     * @param controller
     * @param access
     */
    public TransformationAttributeController(GraphController controller,
            Access access) {
        super(controller, access);

        _menuFactory.addMenuItemFactory(new MenuActionFactory(
                new ApplyTransformationAction()));
    }


    public static class Factory extends NodeControllerFactory {

        public Factory(NamedObj container, String name)
                throws NameDuplicationException, IllegalActionException {
            super(container, name);
        }

        public NamedObjController create(GraphController controller) {
            return new TransformationAttributeController(controller);
        }
    }

    protected static void _applyTransformationResult(
            ToplevelTransformer transformer, BasicGraphFrame frame) {
        ActorToken token = transformer.getOutputToken();
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

        GTFrameTools.changeModel(frame, result, true);
    }

    protected static ToplevelTransformer _getTransformer(
            TransformationAttribute attribute) throws IllegalActionException {
        ToplevelTransformer transformer =
            attribute.transformer.getTransformer();
        if (transformer == null) {
            String moml = attribute.transformer.getExpression();
            if (moml.equals("")) {
                moml = new ToplevelTransformer().exportMoML();
            }
            MoMLParser parser = new MoMLParser();
            try {
                transformer = (ToplevelTransformer) parser.parse(moml);
            } catch (Exception e) {
                throw new IllegalActionException(null, e,
                        "Unable to parse transformer.");
            }
        }

        Manager manager = transformer.getManager();
        if (manager != null) {
            transformer.workspace().remove(manager);
        }
        manager = new Manager(transformer.workspace(), "_manager");
        transformer.setManager(manager);

        return transformer;
    }

    private class ApplyTransformationAction extends FigureAction {

        public ApplyTransformationAction() {
            super("Apply Transformation");
        }

        public void actionPerformed(ActionEvent event) {
            super.actionPerformed(event);

            final TransformationAttribute attribute =
                (TransformationAttribute) getTarget();
            try {
                final ToplevelTransformer transformer =
                    _getTransformer(attribute);
                final Manager manager = transformer.getManager();
                new Thread() {
                    public void run() {
                        BasicGraphFrame frame = (BasicGraphFrame) getFrame();
                        try {
                            frame.report("Applying model transformation: " +
                                    attribute.getName());
                            transformer.setInputToken(new ActorToken((Entity)
                                    frame.getModel()));
                            manager.execute();
                            _applyTransformationResult(transformer, frame);
                        } catch (Throwable t) {
                            MessageHandler.error("Error while executing the " +
                                    "transformation model.", t);
                        } finally {
                            frame.report("");
                        }
                    }
                }.start();
            } catch (IllegalActionException e) {
                throw new InternalErrorException(e);
            }
        }
    }
}
