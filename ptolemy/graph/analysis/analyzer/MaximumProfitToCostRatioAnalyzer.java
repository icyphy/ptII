/* A common interface for all the maximum profit to cost analyzers.

 Copyright (c) 2003-2005 The University of Maryland.
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
package ptolemy.graph.analysis.analyzer;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// MaximumProfitToCostRatioAnalyzer

/**
 A common interface for all the maximum profit to cost analyzers.
 <p>
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (shahrooz)
 @Pt.AcceptedRating Red (ssb)
 @see ptolemy.graph.analysis.MaximumProfitToCostRatioAnalysis
 @author Shahrooz Shahparnia
 @version $Id$
 */
public interface MaximumProfitToCostRatioAnalyzer extends GraphAnalyzer {
    /** Return the nodes on the cycle that corresponds to the maximum profit
     *  to cost ratio as an ordered list. If there is more than one cycle with
     *  the same maximal/minimal cycle, one of them is returned randomly,
     *  but the same cycle is returned by different invocations of the method,
     *  unless the graph changes.
     *
     *  @return Return the nodes on the cycle that corresponds to the maximum
     *  profit to cost ratio as an ordered list.
     */
    public List cycle();

    /** Return the maximum profit to cost ratio.
     *
     *  @return Return the maximum profit to cost ratio.
     */
    public double maximumRatio();
}
