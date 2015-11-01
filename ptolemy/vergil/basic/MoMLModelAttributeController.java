/* A controller that provides binding of an attribute and a refinement model.
 *
 * Copyright (c) 2009-2014 The Regents of the University of California. All
 * rights reserved. Permission is hereby granted, without written agreement and
 * without license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies of
 * this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2 COPYRIGHTENDKEY
 *
 */

package ptolemy.vergil.basic;

import ptolemy.actor.gui.Configuration;
import ptolemy.vergil.kernel.AttributeController;
import ptolemy.vergil.toolbox.MenuActionFactory;
import diva.graph.GraphController;
import diva.graph.JGraph;
import diva.gui.GUIUtilities;

/**
 * A controller that provides binding of an attribute and a refinement model.
 *
 * @author Dai Bui
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class MoMLModelAttributeController extends AttributeController {

    /**
     * Create a model attribute controller associated with the specified graph
     * controller.
     * @param controller The specified graph controller.
     */
    public MoMLModelAttributeController(GraphController controller) {
        this(controller, FULL);
    }

    /**
     * Create a model attribute controller associated with the specified graph
     * controller.
     * @param controller The associated graph controller.
     * @param access The access level.
     */
    public MoMLModelAttributeController(GraphController controller,
            Access access) {
        super(controller, access);

        _menuFactory
        .addMenuItemFactory(new MenuActionFactory(_lookInsideAction));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Add hot keys to the look inside action in the given JGraph. It would be
     * better that this method was added higher in the hierarchy.
     * @param jgraph The JGraph to which hot keys are to be added.
     */
    @Override
    public void addHotKeys(JGraph jgraph) {
        super.addHotKeys(jgraph);
        GUIUtilities.addHotKey(jgraph, _lookInsideAction);
    }

    /** Set the configuration for MoMLModelAttributeController. This includes
     *  setting the configuration for its _lookinsideAction menu object.
     *  @param configuration The given configuration object to be used to
     *   set the configuration.
     */
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        _lookInsideAction.setConfiguration(configuration);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The action that handles opening an actor. This is accessed by by
     *  ActorViewerController to create a hot key for the editor. The name
     *  "lookInside" is historical and preserved to keep backward compatibility
     *  with subclasses.
     */
    private LookInsideAction _lookInsideAction = new LookInsideAction(
            "Open Model");
}
