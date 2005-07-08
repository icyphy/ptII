/* Exception of computing a function of graph with wrong states.

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
//// GraphStateException

/**
 The exception of computing a function of graph with wrong states.
 In some cases, a proper input graph is required to evaluate functions of
 a graph. Graphs with wrong states lead to invalid results or even making
 functions incomputable. Some examples of the states are: topology,
 connectivity, graph type, node/edge counts ...etc. Our design should make
 it impossible for this exception to ever occur, so occurrence is a bug.

 @author Mingyung Ko
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (myko)
 @Pt.AcceptedRating Red (ssb)
 */
public class GraphStateException extends GraphException {
    /** Constructor with an argument of text description.
     *  @param message The exception message.
     */
    public GraphStateException(String message) {
        super(message);
    }
}
