/* Exception thrown due to incorrect graph topology.

 Copyright (c) 2002-2005 The University of Maryland.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF MARYLAND BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF MARYLAND HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF MARYLAND SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 MARYLAND HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.graph;

//////////////////////////////////////////////////////////////////////////
//// GraphTopologyException

/**
 The exception thrown due to incorrect graph topology. This is for
 functions of graphs with required topologies. For example, acyclic property
 is required in {@link DirectedAcyclicGraph} and the topology checking is
 necessary.

 @author Mingyung Ko
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (myko)
 @Pt.AcceptedRating Red (ssb)
 */
public class GraphTopologyException extends GraphException {
    /** Constructor with an argument of text description.
     *  @param message The exception message.
     */
    public GraphTopologyException(String message) {
        super(message);
    }
}
