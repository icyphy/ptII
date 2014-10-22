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

package ptolemy.vergil.toolbox;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.MoMLModelAttributeController;
import ptolemy.vergil.basic.NamedObjController;
import ptolemy.vergil.basic.NodeControllerFactory;
import diva.graph.GraphController;

/**
 * A factory attribute that creates MoMLModelAttributeControllers.
 * @author Dai Bui.  Based on code by Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class MoMLModelAttributeControllerFactory extends NodeControllerFactory {

    /**
     * Create a new factory with the specified name and container.
     * @param container The specified container.
     * @param name The specified name.
     * @exception IllegalActionException If the attribute cannot be
     * contained by the proposed container.
     * @exception NameDuplicationException If the container already has an
     * attribute with this name.
     */
    public MoMLModelAttributeControllerFactory(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /**
     * Create a new ModelAttributeController with the specified graph
     * controller.
     * @param controller The specified graph controller.
     * @return A new ModelAttributeController.
     */
    @Override
    public NamedObjController create(GraphController controller) {
        return new MoMLModelAttributeController(controller);
    }
}
