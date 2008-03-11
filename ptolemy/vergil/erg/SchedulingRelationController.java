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

package ptolemy.vergil.erg;

import java.awt.BasicStroke;

import ptolemy.domains.erg.kernel.SchedulingRelation;
import ptolemy.vergil.fsm.Arc;
import ptolemy.vergil.fsm.TransitionController;
import diva.canvas.Site;
import diva.canvas.connector.ArcConnector;
import diva.canvas.connector.Connector;
import diva.graph.GraphController;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class SchedulingRelationController extends TransitionController {

    /**
     * @param controller
     */
    public SchedulingRelationController(GraphController controller) {
        super(controller);
        setEdgeRenderer(new SchedulingRelationRenderer());
    }

    public static class SchedulingRelationRenderer extends LinkRenderer {

        public Connector render(Object edge, Site tailSite, Site headSite) {
            ArcConnector connector =
                (ArcConnector) super.render(edge, tailSite, headSite);
            Arc arc = (Arc) edge;
            SchedulingRelation relation =
                (SchedulingRelation) arc.getRelation();
            if (relation != null && relation.isCanceling()) {
                BasicStroke stroke = new BasicStroke(2.0f,
                        BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f,
                        new float[] {5.0f, 7.0f}, 0.0f);
                connector.setStroke(stroke);
            }
            return connector;
        }
    }
}
