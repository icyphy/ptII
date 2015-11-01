/*
 * Copyright (c) 2010-2014 The Regents of the University of California.
 * All rights reserved.
 *
 * '$Author$'
 * '$Date$'
 * '$Revision$'
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 */

package ptolemy.vergil.basic;

import java.io.Serializable;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import diva.graph.JGraph;

/**
 * This class provides default extensions points which are overridden in the
 * Kepler Comad module.
 *
 * @author Sven Koehler, Christopher Brooks.
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class BasicGraphFrameExtension implements Serializable {

    // From the 2010-11-04 comment that added this class:

    // "Added an extension for a different Drag and Drop behavior.
    // Currently this is only activated in Comad. Once an Actor is
    // dragged from the Library over another actor on the canvas it
    // will replace this actor and Ports are reconeected. Also
    // Parameter values are used for parameters of the new actor if
    // they have the same name.
    //
    // If an actor is dragged over a link this actor will be inserted
    // there. Currently this only works in Comad, where all actors
    // have an "input" and one "output" port."

    private BasicGraphFrameExtension() {
    }

    /** Delete the Ptolemy objects represented by the selection.
     *  This method is typically called by BasicGraphFrame.delete().
     * @param selection The objects to be deleted.
     * @param graphModel The graphical model.
     * @param container The container in which the Ptolemy objects reside
     * @exception IllegalActionException If there is a problem deleting.
     */
    public static void alternateDelete(Object[] selection,
            AbstractBasicGraphModel graphModel, NamedObj container)
                    throws IllegalActionException {
    }

    /** Paste the Ptolemy objects represented by the value of the
     * moml argument into the container.
     * @param container The container in to which the Ptolemy objects are pasted.
     * @param moml The moml used to create the objects.
     * @exception IllegalActionException If there is a problem pasting.
     */
    public static void alternatePasteMomlModification(NamedObj container,
            StringBuffer moml) throws IllegalActionException {
    }

    /** Paste the Ptolemy objects represented by the value of the
     * moml argument into the container.
     * @param container The container in to which the Ptolemy objects are pasted.
     * @param moml The moml used to create the objects.
     * @exception IllegalActionException If there is a problem pasting.
     */
    public static void alternatePaste(NamedObj container, StringBuffer moml)
            throws IllegalActionException {
        // FIXME: How is this different from the alternatePasteMomlModification?
    }

    /** Filter the array of objects selected for deletion.
     * @param graphModel The graphical model.
     * @param selection The objects to be deleted.
     * @return a new array that contains objects to be deleted.
     */
    public static Object[] filterDeletedObjects(
            AbstractBasicGraphModel graphModel, Object[] selection) {
        return selection;
    }

    /** Filter the moml to be deleted.
     * @param graphModel The graphical model.
     * @param selection The objects to be deleted.
     * @param moml The moml to be filtered.
     */
    public static void filterDeleteMoml(AbstractBasicGraphModel graphModel,
            Object[] selection, StringBuffer moml) {
    }

    /** Return the drop target for a JGraph.
     * @param jGraph The Jgraph of interest
     * @return The drop target.
     */
    public static EditorDropTarget getDropTarget(JGraph jGraph) {
        return new EditorDropTarget(jGraph);
    }
}
