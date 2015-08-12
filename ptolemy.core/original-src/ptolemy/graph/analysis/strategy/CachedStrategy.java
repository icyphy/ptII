/* A base class for cached analyzers on graphs.

 Copyright (c) 2003-2014 The University of Maryland. All rights reserved.
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
package ptolemy.graph.analysis.strategy;

import ptolemy.graph.Graph;
import ptolemy.graph.analysis.analyzer.GraphAnalyzer;

///////////////////////////////////////////////////////////////////
//// CachedStrategy

/**
 A base class for cached analyzers on graphs. To facilitate demand-driven
 and incremental recomputation (e.g., see [1]) of analyzers, analyzer results
 are cached internally (see {@link #getCachedResult()}), and are recomputed only
 when the graph has changed since the last request (via {@link #_result()}) for
 the strategy result.
 The status of the cache content can be queried with the {@link #obsolete()}
 method to determine if a subsequent invocation of {@link #_result()} by the
 derived classes will trigger recomputation of the analysis.
 <p>
 The graph changes tracked by an analyzer are restricted to changes in the
 graph topology (the set of nodes and edges). For example, changes to edge/node
 weights that may affect the result of an analysis are not tracked, since
 analyzers have no specific knowledge of weights.  In such cases, it is the
 responsibility of the client (or derived analyzer class) to invalidate the
 cached result (see {@link #reset()}) when changes to graph weights or other
 non-topology information render the cached result obsolete.  For this reason,
 some caution is generally required when using analyzers whose results depend on
 more than just the graph topology.<p>
 In these cases the client should check for data consistency
 (through the {@link #valid()} method) as well as changes in them and
 calling the {@link #reset()} method in case of data changes.

 <p> [1] G. Ramalingam. <em>Bounded Incremental Computation</em>. PhD thesis,
 University of Wisconsin at Madison, August 1993.
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 @author Shuvra S. Bhattacharyya, Ming Yung Ko and Shahrooz Shahparnia
 @version $Id$
 */
abstract public class CachedStrategy extends Strategy implements GraphAnalyzer {
    /** No instance of this object can be created as it is abstract.
     *  The derived classes will use it to initialize its internal data
     *  structures.
     *
     *  @param graph The graph which the analyzer is using to compute its
     *  result.
     */
    public CachedStrategy(Graph graph) {
        _graph = graph;
        reset();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the caching status of the strategy.
     *
     *  @return True if caching is enabled (default on instantiation)
     */
    public boolean cachingStatus() {
        return _cachingEnabled;
    }

    /** Disable caching.
     *
     */
    public void disableCaching() {
        _cachingEnabled = false;
    }

    /** Enable caching.
     *
     */
    public void enableCaching() {
        _cachingEnabled = true;
    }

    /** The result of the most recent cached computation of the analysis,
     *  as determined by {@link #_compute()}, and without
     *  conversion by {@link #_convertResult(Object)}.
     *  @return The result of the most recent cached computation.
     *  @see #setCachedResult(CachedStrategy)
     */
    public Object getCachedResult() {
        return _cachedResult;
    }

    /** The graph associated with the Strategy. This association is made
     *  when the Strategy is constructed.
     *
     *  @return The input graph.
     */
    @Override
    public Graph graph() {
        return _graph;
    }

    /** Test whether or not the cached result of the analyzer is
     *  obsolete relative to the associated graph. In other words, test if the
     *  graph has changed
     *  since the last time the analyzer was executed with an enabled cache
     *  (i.e., since the most recent invocation of {@link #_result()}).
     *  If the cached result is obsolete and the cache is enabled,
     *  then a subsequent invocation of {@link #_result()}
     *  will trigger recomputation of the analysis.
     *  @return True if the cached result is obsolete relative to the
     *  associated graph.
     */
    public boolean obsolete() {
        return _lastComputation < graph().changeCount();
    }

    /** Reset the analyzer to invalidate any cached value (i.e., to force
     *  recomputation the next time a result of the computation is needed).
     */
    public void reset() {
        _lastComputation = -1;
    }

    /** Set the cached value of this analyzer to the cached value of another
     *  analyzer.
     *  @param cacher The other analyzer.
     *  @see #getCachedResult()
     */
    public void setCachedResult(CachedStrategy cacher) {
        _cachedResult = cacher.getCachedResult();
    }

    /** Return a description of the strategy.
     *
     * @return Return a description of the strategy.
     */
    @Override
    public String toString() {
        return "Cached strategy.";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Perform the graph analysis and return the resulting value.
     *  Upon entry, {@link #getCachedResult()} provides the result of the
     *  previous invocation of the analysis; this value can be
     *  used, for example, to facilitate incremental analyses.
     *  This method just returns null, and will typically be overridden
     *  in each derived class to perform the appropriate graph analysis.
     *  @return The results of the graph analysis.  In this base class,
     *  null is returned.
     */
    protected Object _compute() {
        return null;
    }

    /** Convert the cached result ({@link #getCachedResult()})
     *  to a form that is
     *  suitable for the client to access (via {@link #_result()}).
     *  This base class method just returns a reference to the cached result.
     *  However, it may be appropriate for derived classes to override this
     *  method. For example, if the object returned by the analyzer is mutable,
     *  one may wish to override this method to copy the cached
     *  value (or convert it to some other form) before returning it.
     *  Then changes made by the client to the returned value will
     *  not affect the cached value in the analysis.
     *  This consideration is important for incremental analyzers that
     *  use the cached value across successive invocations of the
     *  analyzer.
     *  @param result The cached result to be converted.
     *  @return The form suitable for access via {@link #_result()}.
     */
    protected Object _convertResult(Object result) {
        return result;
    }

    /** Return the result (cached value) of the analyzer on the associated
     *  graph. The cached value is updated, through recomputation of
     *  the analysis (using {@link #_compute()}), if the graph
     *  has changed since the last time {@link #_result()} was invoked.
     *  Otherwise, the cache value is simply returned (in <em>O</em>(1) time).
     *
     *  @return The result of the analysis.
     */
    protected final Object _result() {
        // Finality of this method is required to ensure analyzer computation
        // only happens when the graph changes, as specified in the
        // contract of the method comment. (when caching is on)
        Object result = null;

        if (_cachingEnabled && obsolete()) {
            _cachedResult = _compute();
            _registerComputation();
            result = _cachedResult;
        } else if (_cachingEnabled) {
            result = _cachedResult;
        } else {
            // Do not call _registerComputation to keep the obsolete status as
            // it is.
            result = _compute();
        }

        return _convertResult(result);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Notify the analyzer that the associated computation has been
     *  performed. This method should be called immediately after
     *  any invocation of the computation, if caching is enabled and the graph
     *  is changed.
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

    private boolean _cachingEnabled = true;
}
