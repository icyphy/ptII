/* An analysis for schedules on graphs.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.graph.sched;

import ptolemy.graph.analysis.Analysis;
import ptolemy.graph.analysis.analyzer.Analyzer;

///////////////////////////////////////////////////////////////////
//// ScheduleAnalysis

/**
 An analysis for schedules on graphs. The analyzer associate with this analysis
 which is supposed to implement the ScheduleAnalyzer interface generates a
 Schedule for a given Graph. The analyzer that for a given implementation would
 extend the Strategy ( or CachedStrategy) class contains the scheduling
 algorithm. Scheduling strategies can be dynamically changed, therefore dynamic
 schedulings are also supported.
 <p>
 @see ptolemy.graph.Graph
 @see ptolemy.graph.analysis.Analysis
 @see ptolemy.graph.analysis.strategy.CachedStrategy
 @see ptolemy.graph.sched.ScheduleAnalysis
 @see ptolemy.graph.sched.ScheduleAnalyzer
 @since Ptolemy II 4.0
 @Pt.ProposedRating red (shahrooz)
 @Pt.AcceptedRating red (ssb)
 @author Shahrooz Shahparnia
 */
public class ScheduleAnalysis extends Analysis {
    /** Construct an instance of this class with the given analyzer.
     *
     *  @param analyzer The given analyzer.
     */
    public ScheduleAnalysis(ScheduleAnalyzer analyzer) {
        super(analyzer);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public classes                    ////

    /** Return the schedule computed by the associated analyzer.
     *
     *  @return Return the schedule computed by the associated analyzer.
     */
    public Schedule schedule() {
        return ((ScheduleAnalyzer) analyzer()).schedule();
    }

    /** Check if a given analyzer is compatible with this analysis.
     *  In other words if it is possible to use it to compute the computation
     *  associated with this analysis.
     *  Derived classes should override this method to provide the valid type
     *  of analyzer that they need.
     *
     *  @param analyzer The given analyzer.
     *  @return True if the given analyzer is valid for this analysis.
     */
    @Override
    public boolean validAnalyzerInterface(Analyzer analyzer) {
        return analyzer instanceof ScheduleAnalyzer;
    }
}
