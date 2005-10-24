/*

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
package ptolemy.copernicus.jhdl.soot;

import soot.Value;

import soot.jimple.Jimple;

import ptolemy.kernel.util.IllegalActionException;


//////////////////////////////////////////////////////////////////////////
//// CompoundAndExpression

/**
 * Represents the "AND" compound Boolean expression.
 *
 * The inverse of this expression is a CompoundOrExpression with each
 * of the operands inverted (i.e. application of DeMorgan's theorem).
 *
 * @see ptolemy.copernicus.jhdl.soot.CompoundOrExpression
 *
 * @author Mike Wirthlin
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class CompoundAndExpression extends AbstractCompoundExpression {
    public CompoundAndExpression(Value op1, Value op2) {
        super(op1, op2);
    }

    public final String getSymbol() {
        return " && ";
    }

    public Object clone() {
        return new CompoundAndExpression(Jimple.cloneIfNecessary(getOp1()),
            Jimple.cloneIfNecessary(getOp2()));
    }

    public CompoundBooleanExpression invert() throws IllegalActionException {
        // Apply demorgan's theorem
        return new CompoundOrExpression(invertValue(getOp1()),
            invertValue(getOp2()));
    }
}
