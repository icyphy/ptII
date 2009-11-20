/*
 * An ontology lattice for the logicalAND use case.
 * 
 * Copyright (c) 2007-2009 The Regents of the University of California. All
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
package ptolemy.data.properties.lattice.logicalAND;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// Lattice

/**
 * An ontology lattice for the logicalAND use case. The lattice contains three
 * elements: TRUE, FALSE and UNKNOWN. UNKNOWN is the bottom, and FALSE is the
 * top, such that the order of the elements looks like UKNOWN -> TRUE -> FALSE.
 * 
 * @author Thomas Mandl, Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 * 
 */
public class Lattice extends PropertyLattice {

    /**
     * Construct a new ontology lattice.
     * @throws IllegalActionException
     */
    public Lattice() throws IllegalActionException {
        super();

        addNodeWeight(TRUE);
        addNodeWeight(FALSE);
        addNodeWeight(UNKNOWN);

        addEdge(UNKNOWN, TRUE);
        addEdge(TRUE, FALSE);

        addStructuredProperties(RECORD);

        // If you aren't sure you have a lattice, uncomment this code.
        //if (!isLattice()) {
        //    throw new IllegalActionException("This ontology is not a lattice.");
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The lattice element named FALSE.
     */
    // FIXME: PropertyConstraintSolver records too much training information.
    // It has this line:
    //                     + propertyTerm.getClass().getSuperclass().getSimpleName()
    // which has the effect of forcing us to create a False class rather than just
    // using LatticeProperty.  What we want to do is:
    //
    // private final Property FALSE = new LatticeProperty(this, "False");
    //
    // but instead we have to do is below. The fix is to figure out exactly
    // what information SHOULD be exported by PropertConstraintSolver, and then
    // retrain all the tests.
    private final Property FALSE = new False(this);

    /**
     * The lattice element named TRUE.
     */
    // private final Property TRUE = new LatticeProperty(this, "True");
    private final Property TRUE = new True(this);

    /**
     * The lattice element named UNKNOWN.
     */
    private final Property UNKNOWN = new LatticeProperty(this, "Unknown") {
        public boolean isAcceptableSolution() {
            return false;
        }
        public boolean isConstant() {
            return false;
        }
    };
}
