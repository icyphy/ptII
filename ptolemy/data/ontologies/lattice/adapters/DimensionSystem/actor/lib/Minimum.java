/* An adapter class for ptolemy.actor.lib.Minimum.

 Copyright (c) 2006-2010 The Regents of the University of California.
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
package ptolemy.data.ontologies.lattice.adapters.DimensionSystem.actor.lib;

import java.util.List;

import ptolemy.data.ontologies.lattice.LatticeOntologySolver;
import ptolemy.data.ontologies.lattice.adapters.DimensionSystem.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Minimum

/**
 An adapter class for ptolemy.actor.lib.Expression.

 @author Man-Kit Leung, Beth
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (beth)
 @Pt.AcceptedRating Red (beth)
*/
public class Minimum extends AtomicActor {

    /**
     * Construct a Minimum adapter for the dimensionSystem lattice.
     * @param solver The given solver.
     * @param actor The given Minimum actor
     * @exception IllegalActionException
     */
    public Minimum(LatticeOntologySolver solver,
            ptolemy.actor.lib.Minimum actor) throws IllegalActionException {

        super(solver, actor, false);
    }

    public List<Inequality> constraintList() throws IllegalActionException {
        ptolemy.actor.lib.Minimum actor = (ptolemy.actor.lib.Minimum) getComponent();

        // Set the minimumValue output to at least the input
        // Set the channelNumber output to Unitless
        setAtLeast(actor.minimumValue, actor.input);
        setAtLeast(actor.channelNumber, _dimensionlessConcept);
        return super.constraintList();
    }
}
