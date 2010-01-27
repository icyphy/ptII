/*
 * An ontology lattice.
 * 
 * Copyright (c) 1997-2010 The Regents of the University of California. All
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
package ptolemy.data.properties.lattice.dimensionSystem;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.LatticeProperty;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.data.properties.lattice.RecordProperty;

///////////////////////////////////////////////////////////////////
//// Lattice

/**
 * The ontology lattice for dimension analysis.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class Lattice extends PropertyLattice {

    /** Construct a new ontology lattice. */
    public Lattice() {
        super();

        addNodeWeight(TOP);
        addNodeWeight(TIME);
        addNodeWeight(ACCELERATION);
        addNodeWeight(SPEED);
        addNodeWeight(POSITION);
        addNodeWeight(UNITLESS);
        addNodeWeight(UNKNOWN);

        // Record Property
        //FIXME: add an artificial top.
        addNodeWeight(RECORD);
        addEdge(UNKNOWN, RECORD);
        addEdge(RECORD, TOP);

        addEdge(UNKNOWN, TIME);
        addEdge(UNKNOWN, UNITLESS);
        addEdge(UNKNOWN, POSITION);
        addEdge(UNKNOWN, SPEED);
        addEdge(UNKNOWN, ACCELERATION);

        addEdge(TIME, TOP);
        addEdge(UNITLESS, TOP);
        addEdge(POSITION, TOP);
        addEdge(SPEED, TOP);
        addEdge(ACCELERATION, TOP);

        if (!isLattice()) {
            throw new AssertionError("This ontology is not a lattice.");

        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final Property RECORD = new RecordProperty(this, new String[0],
            new LatticeProperty[0]).getRepresentative();

    private final Property TOP = new Top(this);

    private final Property TIME = new Time(this);

    private final Property ACCELERATION = new Acceleration(this);

    private final Property SPEED = new Speed(this);

    private final Property POSITION = new Position(this);

    private final Property UNITLESS = new Unitless(this);

    private final Property UNKNOWN = new Unknown(this);
}
