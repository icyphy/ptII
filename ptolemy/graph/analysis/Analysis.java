/* A base class for analyses on graphs.

   Copyright (c) 2002 The University of Maryland. All rights reserved.
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

package ptolemy.graph.analysis;

import ptolemy.graph.Graph;

//////////////////////////////////////////////////////////////////////////
//// Analysis
/** A base class for analyses on graphs. To facilitate demand-driven
    and incremental recomputation (e.g., see [1]) of analyses, analysis results
    are cached
    internally (see {@link #_cachedResult()}), and are recomputed only
    when the graph has changed since the last request (via {@link #result()})
    for the analysis result. The internally-cached result (<em>cached result</em>)
    of an analysis is directly accessible only by derived classes; however,
    its status can be queried with the {@link #obsolete()} method to determine
    if a subsequent invocation of {@link #result()} will trigger recomputation
    of the analysis.

    <p> The graph changes tracked by an analysis are restricted to changes in the
    graph topology (the set of nodes and edges). For example, changes to edge/node
    weights that may affect the result of an analysis are not tracked, since
    analyses have no specific knowledge of weights.  In such cases, it is the
    responsibility of the client (or derived analysis class) to invalidate the
    cached result (see {@link #reset()}) when changes to graph weights or other
    non-topology information render the cached result obsolete.  For this reason,
    some caution is generally required when using analyses whose results depend on
    more than just the graph topology.

    <p> [1] G. Ramalingam. <em>Bounded Incremental Computation</em>. PhD thesis,
    University of Wisconsin at Madison, August 1993.

    @author Shuvra S. Bhattacharyya and Mingyung Ko
    @version $Id$
 */

abstract public class Analysis {

    /** Construct an analysis for a given graph.
     *  @param graph The given graph.
     */
    public Analysis(Graph graph) {
        if (compatible(graph)) {
            _graph = graph;
            _graph.addAnalysis(this);
            reset();
        } else {
            throw new IllegalArgumentException(
                    incompatibilityDescription(graph));
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone an instance of this analysis (with the same analysis-specific
     *  options) to operate on a given graph. This method first checks
     *  for compatibility between this analysis and the given graph, and
     *  throws an IllegalArgumentException on incompatibility. Otherwise,
     *  the method returns
     *  a new analysis with the same run-time type as this analysis,
     *  and associates the given graph with the new analysis. The method
     *  should be overridden in derived classes to copy options and
     *  other configuration information associated
     *  with the cloned analysis into the new one (the clone).
     *
     *  @param graph The given graph.
     *  @return A clone of this Analysis.
     *  FIXME: We need better exception handling.
     */
    public Analysis cloneFor(Graph graph) {
        if (!compatible(graph)) {
            throw new IllegalArgumentException("Incompatible graph. "
                    + "A description of the incompatibility follows.\n"
                    + incompatibilityDescription(graph));
        }
        Analysis clone;
        try {
            clone = (Analysis)(getClass().newInstance());
        } catch (Exception exception) {
            throw new RuntimeException("Could not clone this analysis on the"
                    + "given graph. The offending analysis: " + toString());
        }
        clone._graph = graph;
        graph.addAnalysis(clone);
        clone.reset();
        return clone;
    }

    /** Check compatibility for between the analysis and the given
     *  graph. Compatibility may depend on the a variety of graph- and
     *  analysis-specific factors, such as the implementation of certain
     *  interfaces, the satisfaction of certain graph properties, etc.
     *  This method always returns true since there is no issue of
     *  incompatibility with an abstract analysis. It should be overridden in
     *  derived classes in which issues of incompatibility may arise.
     *
     *  @param graph The given graph.
     *  @return True if the analysis is compatible with the given graph.
     */
    public boolean compatible(Graph graph) {
        return true;
    }

    /** Return a description of the configuration of the Analysis.
     *  The configuration can include any analysis-specific options.
     *  For example, a scheduler might have the unfolding factor as one option.
     *  This method in this base class returns an empty string since there is
     *  no configuration information
     *  to report for an abstract analysis. It should be overridden in
     *  derived classes that have configuration information to report.
     *
     *  @return A String describing the configuration of this analysis.
     */
    public String configuration() {
        return "";
    }

    /** The graph associated with the Analysis. This association is made
     *  when the Analysis is constructed.
     *
     *  @return The input graph.
     */
    public Graph graph() {
        return _graph;
    }

    /** Return a string that explains any incompatibilities that the graph
     *  has with respect to the Analysis. Return an empty string if the
     *  graph is compatible. This method in this base class returns an empty
     *  string since there is no issue of incompatibility with an abstract
     *  analysis. The
     *  method should be overridden in derived classes that may have
     *  issues of incompatibility to report.
     *
     *  @param graph The given graph.
     *  @return A description of any incompatibilities.
     */
    public String incompatibilityDescription(Graph graph) {
        return "";
    }

    /** Test whether or not the cached result of the analysis is
     *  obsolete relative to the associated graph. In other words, test if the
     *  graph has changed
     *  since the last time the analysis was performed (i.e., since
     *  the most recent invocation of {@link #result()}). If the cached
     *  result is obsolete, then a subsequent invocation of {@link #result}
     *  will trigger recomputation of the analysis.
     *  @return True if the cached result is obsolete relative to the
     *  associated graph.
     */
    public boolean obsolete() {
        return _lastComputation < graph().changeCount();
    }

    /** Reset the analysis to invalidate any cached value (i.e., to force
     *  recomputation the next time a result of the computation is needed).
     */
    public void reset() {
        _lastComputation = -1;
    }

    /** Return the result (cached value) of the analysis on the associated
     *  graph. The cached value is updated, through recomputation of
     *  the analysis (using {@link #_compute()}), if the graph
     *  has changed since the last time {@link #result()} was invoked.
     *  Otherwise, the cache value is simply returned (in <em>O</em>(1) time).
     *
     *  @return The result of the analysis.
     */
    public final Object result() {
        // Finality of this method is required to ensure analysis computation
        // only happens when the graph changes, as specified in the
        // contract of the method comment.
        if (obsolete()) {
            _cachedResult = _compute();
            _registerComputation();
        }
        return _convertResult();
    }

    /** Return a description of the analysis. This method
     *  simply returns a description of the associated graph.
     *  It should be overridden in derived classes to
     *  include details associated with the associated analyses.
     *
     *  @return A description of the analysis.
     */
    public String toString() {
        return "Analysis for the following graph.\n"
            + graph().toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Perform the graph analysis and return the resulting value.
     *  Upon entry, {@link #_cachedResult()} provides the result of the
     *  previous invocation of the analysis; this value can be
     *  used, for example, to facilitate incremental analyses.
     *  This method just returns null, and will typically be overridden
     *  in each derived class to perform the appropriate graph analysis.
     */
    abstract protected Object _compute();

    /** The result of the most recent computation of the analysis,
     *  as determined by {@link #_compute()}, and without
     *  conversion by {@link #_convertResult()}.
     */
    protected final Object _cachedResult() {
        return _cachedResult;
    }

    /** Convert the cached result ({@link #_cachedResult()}) to a form that is
     *  suitable for the client to access (via {@link #result()}).
     *  This base class method just returns a reference to the cached result.
     *  However, it may be appropriate for derived classes to override this
     *  method. For example, if the object returned by the analysis is mutable,
     *  one may wish to override this method to copy the cached
     *  value (or convert it to some other form) before returning it.
     *  Then changes made by the client to the returned value will
     *  not affect the cached value in the analysis (as an example,
     *  see {@link SelfLoopAnalysis#result()}). This consideration is
     *  important for incremental analyses that use the cached value
     *  across successive invocations of the analysis.
     */
    protected Object _convertResult() {
        return _cachedResult();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Notify the analysis that the associated computation has been
     *  performed. This method should be called immediately after
     *  any invocation of the computation.
     */
    private void _registerComputation() {
        _lastComputation = graph().changeCount();
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The result of the most recent computation of the analysis, as determined
    // by _compute(), and without conversion by _convertResult().
    private Object _cachedResult = null;

    // The graph that this analysis is associated with.
    private Graph _graph;

    // The change count of the associated graph that was in effect when the
    // the analysis was last performed.
    private long _lastComputation;

}
