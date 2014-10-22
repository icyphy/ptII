/* The default adapter class for ptolemy.actor.lib.gui.Display.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.defaultAdapters.actor.lib.gui;

import java.util.List;

import ptolemy.data.ontologies.lattice.LatticeOntologyAdapter;
import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// Display

/** The default adapter class for ptolemy.actor.lib.gui.Display. This adapter
 *  does not add any constraints, but rather removes the actor's title
 *  attribute since it is only relevant to the graphical output display and
 *  should not be included in the ontology analysis. This is necessary for
 *  running ontology solver regression test models that contain Display actors
 *  that are run with all graphical classes filtered out and replaced with
 *  Discard actors that do not have a title attribute.
 *
 *  @author Charles Shelton
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cshelton)
 *  @Pt.AcceptedRating Red (cshelton)
 */
public class Display extends LatticeOntologyAdapter {

    /** Construct a default lattice ontology adapter for the Display actor.
     *  @param solver The given solver.
     *  @param actor The given Display actor.
     *  @exception IllegalActionException If the adapter cannot be initialized.
     */
    public Display(LatticeOntologySolver solver,
            ptolemy.actor.lib.gui.Display actor) throws IllegalActionException {
        super(solver, actor, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the list of property-able Attributes by calling the super
     *  class method. For the Display actor, remove the title attribute since
     *  by default they should not be evaluated by the ontology solver.
     *  @return The list of property-able Attributes.
     */
    @Override
    protected List<Attribute> _getPropertyableAttributes() {
        List<Attribute> result = super._getPropertyableAttributes();
        ptolemy.actor.lib.gui.Display displayActor = (ptolemy.actor.lib.gui.Display) getComponent();
        result.remove(displayActor.title);
        return result;
    }
}
