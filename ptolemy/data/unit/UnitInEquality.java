/* A Unit inequality which is a prticular type of Unit Constraint.

 Copyright (c) 2003-2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_3
 COPYRIGHTENDKEY
 */
package ptolemy.data.unit;

//////////////////////////////////////////////////////////////////////////
//// UnitInEquality

/** A place holder for the day there is a UnitInEquality

 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
public class UnitInEquality extends UnitConstraint implements UnitPresentation {
    /** Construct a UnitInEquality from the left and right hand sides.
     * @param lhs The left hand side.
     * @param rhs The right hand side.
     */
    public UnitInEquality(UnitExpr lhs, UnitExpr rhs) {
        super(lhs, "<", rhs);
    }
}
