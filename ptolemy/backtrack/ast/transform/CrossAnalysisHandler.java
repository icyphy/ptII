/* Interface of the cross-analysis handlers called by TypeAnalyzer.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack.ast.transform;

import ptolemy.backtrack.ast.TypeAnalyzerState;

//////////////////////////////////////////////////////////////////////////
//// CrossAnalysisHandler
/**
   Interface of the cross-analysis handlers called by {@link TypeAnalyzer}.
   Users may register cross-analysis handlers (and other kinds of supported
   handlers) to the {@link TypeAnalyzer} used to analyze Java source code.
   When the analyzer detects more cross-analyzed types, it calls back those
   cross-analysis.
   <p>
   Cross-analyzed types are the other types to be refactored at the same time.
   Special care is taken about those types because their instances are
   monitored by checkpoint objects. It is not possible to know all the
   cross-analyzed types at the beginning of the refactoring. It is then
   necessary to fix the refactoring result whenever more cross-analyzed types
   are found as the type analysis goes on.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public interface CrossAnalysisHandler {

    /** Handle new cross-analyzed types recorded in the state of the type
     *  analyzer. This method may be triggered whenever a new cross-analyzed
     *  type is found by the type analyzer. Implementing classes may implement
     *  this method to update the refactoring result.
     * 
     *  @param state The current state of the type analyzer.
     *  @see TypeAnalyzerState#getCrossAnalyzedTypes()
     */
    public void handle(TypeAnalyzerState state);

}
