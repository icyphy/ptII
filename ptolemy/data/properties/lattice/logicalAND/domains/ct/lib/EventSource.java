/* A helper class for ptolemy.domains.ct.lib.EventSource.

 Copyright (c) 2006-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalAND.domains.ct.lib;

import java.util.List;

import ptolemy.data.ArrayToken;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.logicalAND.actor.AtomicActor;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// EventSource

/**
 A helper class for ptolemy.actor.lib.EventSource.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
*/
public class EventSource extends AtomicActor {

    /**
     * Construct a EventSource helper for the logicalAND ontology.
     * @param solver The given solver.
     * @param actor The given EventSource actor.
     * @exception IllegalActionException Thrown if the
     *  super class throws it.
     */
    public EventSource(PropertyConstraintSolver solver,
            ptolemy.domains.ct.lib.EventSource actor)
            throws IllegalActionException {

        super(solver, actor, false);
    }

    /**
     * Return the list of constraints that specifies the analysis
     * for the EventSource actor.
     * @return The list of constraints.
     * @exception IllegalActionException Thrown if an error
     *  occurs when getting the elements from the lattice,
     *  reading the values from the values parameter of the EventSource
     *  actor, or the super class throws it.
     */
    public List<Inequality> constraintList() throws IllegalActionException {

        ptolemy.domains.ct.lib.EventSource actor = (ptolemy.domains.ct.lib.EventSource) getComponent();

        ArrayToken valuesToken = (ArrayToken) actor.values.getToken();

        boolean isStatic = true;
        for (int i = 1; i < valuesToken.length(); i++) {
            if (!valuesToken.getElement(i - 1)
                    .equals(valuesToken.getElement(i))) {
                isStatic = false;
                break;
            }
        }

        if (isStatic) {
            setAtLeast(actor.output, _lattice.getElement("TRUE"));
        } else {
            setAtLeast(actor.output, _lattice.getElement("FALSE"));
        }

        return super.constraintList();
    }
}
