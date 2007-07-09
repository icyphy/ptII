/* A helper class for ptolemy.data.expr.ASTPtRootNode.

 Copyright (c) 2006 The Regents of the University of California.
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

package ptolemy.data.properties.lattice.staticDynamic.data.expr;

import java.util.Arrays;
import java.util.List;

import ptolemy.data.properties.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.PropertyConstraintSolver;
import ptolemy.data.properties.lattice.staticDynamic.Lattice;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// AddSubtract

/**
 A helper class for ptolemy.data.expr.ASTPtRootNode.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtLeafNode extends PropertyConstraintASTNodeHelper {

    public ASTPtLeafNode(PropertyConstraintSolver solver, 
            ptolemy.data.expr.ASTPtLeafNode node) throws IllegalActionException {
        super(solver, node, false);

        Lattice lattice = (Lattice) solver.getLattice();

        if (variables.contains(node.getName())) {
            setEquals(node, lattice.DYNAMIC);
        } else {
            setEquals(node, lattice.STATIC);
        }
    }
    
    List variables = Arrays.asList( 
            new String[]{ "time", "iteration"
    });    
}
