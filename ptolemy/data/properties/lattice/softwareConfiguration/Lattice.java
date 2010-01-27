/* Property hierarchy.

 Copyright (c) 1997-2010 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.softwareConfiguration;

import ptolemy.data.properties.Property;
import ptolemy.data.properties.lattice.PropertyLattice;

///////////////////////////////////////////////////////////////////
//// Lattice

/**
 Lattice representing whether propertyable components are configured or
 not in a model. It has four elements. The bottom is NOT.

 @author Thomas Mandl, Man-Kit Leung, Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 @see ptolemy.graph.CPO
 */
public class Lattice extends PropertyLattice {
    
    // The infinite property lattice
    public Lattice() {
        super();
        
        addNodeWeight(CONFLICT);
        addNodeWeight(CONFIGURED);
        addNodeWeight(NOTCONFIGURED);        
        addNodeWeight(NOTSPECIFIED);
        
        addEdge(NOTSPECIFIED, CONFIGURED);
        addEdge(NOTSPECIFIED, NOTCONFIGURED);
        addEdge(CONFIGURED, CONFLICT);
        addEdge(NOTCONFIGURED, CONFLICT);
        
        // FIXME: Replace this with an assert when we move to 1.5
        if (!isLattice()) {
            throw new AssertionError("ThePropertyLattice: The "
                    + "property hierarchy is not a lattice.");
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Property CONFLICT = new Conflict(this);
    private Property CONFIGURED = new Configured(this);
    private Property NOTCONFIGURED = new NotConfigured(this);
    private Property NOTSPECIFIED = new NotSpecified(this);
}
