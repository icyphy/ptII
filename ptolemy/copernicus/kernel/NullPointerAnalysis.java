/* An analysis for detecting objects that must be aliased to each other.

 Copyright (c) 2001-2005 The Regents of the University of California.
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
package ptolemy.copernicus.kernel;

import soot.Local;
import soot.Unit;
import soot.jimple.toolkits.annotation.nullcheck.BranchedRefVarsAnalysis;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.FlowSet;

/**
 An analysis that can determine, at any point in the code, if
 a local variable points to null.  This is just a nice wrapper class
 for soot's BranchedRefVarsAnalysis.
 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class NullPointerAnalysis extends BranchedRefVarsAnalysis {
    public NullPointerAnalysis(UnitGraph g) {
        super(g);
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given field, at a point before
     *  the given unit.
     */
    public boolean isAlwaysNullBefore(Local local, Unit unit) {
        FlowSet flowSet = (FlowSet) getFlowBefore(unit);
        int info = anyRefInfo(local, flowSet);
        return (info == kNull);
    }

    /** Return the set of other fields and locals that must reference
     *  the same object as the given local, at a point before
     *  the given unit.
     */
    public boolean isNeverNullBefore(Local local, Unit unit) {
        FlowSet flowSet = (FlowSet) getFlowBefore(unit);
        int info = anyRefInfo(local, flowSet);
        return (info == kNonNull);
    }
}
