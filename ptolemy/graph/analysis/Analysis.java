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
/** A base class for analyses on graphs.

@author Shuvra S. Bhattacharyya and Mingyung Ko
@version $Id$
*/

// FIXME: this should be an abstract class.
public class Analysis {

    /** Construct an analysis for a given graph.
     *  @param graph The given graph.
     */
    public Analysis(Graph graph) {
        _graph = graph;
        _graph.addAnalysis(this);
        reset();
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
     *  derived classes in which issues of incompatability may arise.
     *
     *  @param graph The given graph.
     *  @return True if the anlysis is compatible with the given graph.
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

    /** Test whether or not the graph has changed since the last
     *  time the analysis was performed (i.e., since
     *  the most recent invocation of {@link #registerComputation()}).
     *  @return True if the graph has changed since the last time
     *  the analysis was performed.
     */
    public boolean obsolete() {
        return _lastComputation < graph().changeCount();
    }

    /** Return the result (cached value) of the analysis on the associated 
     *  graph. The cached value is updated (recomputed) if the graph
     *  has changed since the last time the cached value was computed.
     *  If the object returned by the analysis is mutable, 
     *  one may wish to override this method to copy the cached
     *  value (or convert it to some other form) before returning it.
     *  Then changes made by the client to the returned value will
     *  not affect the cached value in the analysis (as an example,
     *  see {@link #SelfLoopAnalysis.result}).
     *
     *  @return The result of the analysis.
     */
    public Object result() {
        if (obsolete()) {
            _compute();
            registerComputation();
        }
        return _cachedResult;
    }

    /** Return a description of the analysis. This method 
     *  simply returns a description of the associated graph.
     *  It should be overridden in derived classes to 
     *  include details associated with the associated analyses.
     *
     *  @return A description of the analysis.
     */
    public String toString() {
        return "Anlaysis for the following graph.\n"
                + graph().toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                       protected methods                   ////

    /** Perform the graph analysis and
     *  set the value of {@link _cachedResult} to the value obtained by this 
     *  invocation of the analysis. This method will typically be overridden
     *  in each derived class to perform the appropriate graph analysis. 
     *  Care should be taken to set the value of {@link _cachedResult},
     *  as described above, before the method returns.
     *  FIXME: this method should be abstract.
     */
    protected void _compute() {
        _cachedResult = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                      protected variables                  ////

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

   /** Notify the analysis that the associated computation has been
    *  performed. This method should be called immediately after
    *  any invocation of the computation.
    *  FIXME: This should become private.
    */
    public void registerComputation() {
        _lastComputation = graph().changeCount();
    }

    /** Reset the analysis to invalidate any cached value (i.e., to force
     *  recomputation the next time a result of the computation is needed).
     *  FIXME: This should become private.
     */
    public void reset() {
        _lastComputation = -1;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The result of the most recent computation of the analysis.
    protected Object _cachedResult;

    // The graph that this analysis is associated with.
    private Graph _graph;

    // The change count of the associated graph that was in effect when the
    // the analysis was last performed.
    private long _lastComputation;

}
