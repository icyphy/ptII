/*

 Copyright (c) 1997-2008 The Regents of the University of California.
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

package ptolemy.vergil.ptera;

import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gui.Tableau;
import ptolemy.domains.ptera.kernel.Event;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.vergil.fsm.FSMGraphFrame;
import ptolemy.vergil.fsm.FSMGraphModel;
import diva.graph.GraphPane;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PteraGraphFrame extends FSMGraphFrame {

    /**
     * @param entity
     * @param tableau
     */
    public PteraGraphFrame(CompositeEntity entity, Tableau tableau) {
        super(entity, tableau);
    }

    /**
     * @param entity
     * @param tableau
     * @param defaultLibrary
     */
    public PteraGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new PteraGraphController();
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.
        final FSMGraphModel graphModel = new FSMGraphModel(
                (CompositeEntity) entity);
        return new FSMGraphPane(_controller, graphModel, entity);
    }

    /** Return the text to be used in the animation menu item. In this class,
     *  always return "Animate Events".
     *
     *  @return The text for the menu item.
     */
    protected String getAnimationMenuText() {
        return "Animate Events";
    }

    protected String _getDefaultEventMoML() {
        NamedObj child = GTTools.getChild(_topLibrary, "Event", false, false,
                true, false);
        if (child instanceof Event) {
            return child.exportMoML();
        } else {
            return "<entity name=\"Event\" class=\"ptolemy.domains.ptera" +
                    ".kernel.Event\">";
        }
    }
}
