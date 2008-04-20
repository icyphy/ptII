/* Property hierarchy.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalAND;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.PropertyLattice;
import ptolemy.graph.DirectedAcyclicGraph;
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

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class Lattice extends PropertyLattice {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public /*static*/ Property TRUE = new True(this);

    public /*static*/ Property FALSE = new False(this);
    
    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    
    // The infinite property lattice
    public Lattice() {
        super();
        _lattice.setBasicLattice(new DirectedAcyclicGraph());

        DirectedAcyclicGraph basicLattice = 
            (DirectedAcyclicGraph) _lattice.basicLattice();
        

        basicLattice.addNodeWeight(TRUE);

        basicLattice.addNodeWeight(FALSE);

        basicLattice.addEdge(TRUE, FALSE);

        // FIXME: Replace this with an assert when we move to 1.5
        if (!basicLattice.isLattice()) {
            throw new InternalErrorException("ThePropertyLattice: The "
                    + "property hierarchy is not a lattice.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
