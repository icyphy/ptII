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
import soot.SootField;
import soot.Unit;

import java.util.Set;


/**
   An analysis that maps each local and field to the set of locals and
   fields that alias that value.  Implementors of this interface determine
   the strength of the analysis.  i.e. must-aliases vs. maybe aliases,
   flow sensitive vs. flow insensitive, etc.

   FIXME: I think we need an augmented interface for
   flow-sensitive analysis?

   @author Stephen Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
*/
public interface AliasAnalysis {
    /** Return the set of other fields and locals that reference
     *  the same object as the given field, at a point before
     *  the given unit.
     */
    public Set getAliasesOfBefore(SootField field, Unit unit);

    /** Return the set of other fields and locals that reference
     *  the same object as the given field, at a point after
     *  the given unit.
     */
    public Set getAliasesOfAfter(SootField field, Unit unit);

    /** Return the set of other fields and locals that reference
     *  the same object as the given local, at a point before
     *  the given unit.
     */
    public Set getAliasesOfBefore(Local local, Unit unit);

    /** Return the set of other fields and locals that maybe reference
     *  the same object as the given field, at a point after
     *  the given unit.
     */
    public Set getAliasesOfAfter(Local local, Unit unit);
}
