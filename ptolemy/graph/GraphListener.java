/* A class for tracking changes to a graph so that graph properties
are recomputed only when necessary.

 Copyright (c) 2001-2002 The University of Maryland. All rights reserved.
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

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (ssb@eng.umd.edu)
@AcceptedRating Red (ssb@eng.umd.edu)

*/

package ptolemy.graph;

/** A class for tracking changes to a graph so that graph properties can be
recomputed only when necessary. Any given computation
for the graph (e.g., computation of the transitive closure of a directed
graph) can have a graph listener associated with it. If
the {@link #registerComputation()} method is invoked each time the
computation is performed, and results of the computation are cached,
then the {@link #obsolete()} method can be used to determine
whether any changes to the graph have occurred since the time
the cached value was computed.

@author Shuvra S. Bhattacharyya
@version $Id$
@since Ptolemy II 2.0
*/
public class GraphListener {

    /** Construct a new graph listener associated with a given graph, and
     *  add the listener to the set of listeners that the graph broadcasts to.
     *  @param graph The associated graph.
     */
    public GraphListener(Graph graph) {
        _graph = graph;
        _graph.addListener(this);
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the graph associated with this listener.
     *  @return The graph associated with this listener.
     */
    public Graph graph() {
        return _graph;
    }

    /** Test whether or not the graph has changed since the last
     *  time the associated computation was performed (i.e., since
     *  the most recent invocation of {@link #registerComputation()}).
     *  @return True if the graph has changed since the last time
     *  the computation associated with this listener was performed.
     */
    public boolean obsolete() {
        return _lastComputation < _graph.changeCount();
    }

    /** Notify the listener that the associated computation has been
     *  performed. This method should be called immediately after
     *  any invocation of the computation.
     */
    public void registerComputation() {
        _lastComputation = _graph.changeCount();
    }

    /** Reset the listener to invalidate any cached value (i.e., to force
     *  recomputation the next time a result of the computation is needed.
     */
    public void reset() {
        _lastComputation = -1;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The graph that this listener is associated with.
    private Graph _graph;

    // The change count of the associated graph that was in effect when the
    // the computation associated with this listener was last performed.
    private long _lastComputation;

}
