/* Property hierarchy.

 Copyright (c) 1997-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.dimensionSystem;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.kernel.util.InternalErrorException;

//////////////////////////////////////////////////////////////////////////
//// PropertyLattice

/**
 Property hierarchy base class.
 Note that all public methods are synchronized.
 There are more than one instances of a property lattice.
 Although the property lattice is constructed once and then typically
 does not change during execution, the methods need to be synchronized
 because there are various data structures used to cache results that
 are expensive to compute. These data structures do change during
 execution. Multiple threads may be accessing the property lattice
 simultaneously and modifying these data structures. To ensure
 thread safety, the methods need to be synchronized.

 @author Man-Kit Leung
 @version $Id: Lattice.java 53046 2009-04-10 23:04:25Z cxh $
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class Lattice extends PropertyLattice {

    // The finite property lattice
    public Lattice() {
        super();

        addNodeWeight(TOP);
        addNodeWeight(TIME);
        addNodeWeight(ACCELERATION);
        addNodeWeight(SPEED);
        addNodeWeight(POSITION);
        addNodeWeight(UNITLESS);
        addNodeWeight(UNKNOWN);

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

        // FIXME: Replace this with an assert when we move to 1.5
        if (!isLattice()) {
            throw new InternalErrorException("ThePropertyLattice: The "
                    + "property hierarchy is not a lattice.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public fields                    ////

    public final Property TOP = new Top(this);
    
    public final Property TIME = new Time(this);

    public final Property ACCELERATION = new Acceleration(this);
    
    public final Property SPEED = new Speed(this);

    public final Property POSITION = new Position(this);

    public final Property UNITLESS = new Unitless(this);

    public final Property UNKNOWN = new Unknown(this);
    
}
