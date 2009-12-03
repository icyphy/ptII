/* An adapter class for ptolemy.data.expr.ASTPtRootNode.

 Copyright (c) 2008-2009 The Regents of the University of California.
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
package ptolemy.data.properties.lattice.logicalOrBackward.data.expr;

import ptolemy.data.properties.lattice.PropertyConstraintASTNodeHelper;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// ASTPtRootNode

/**
 An adapter class for ptolemy.data.expr.ASTPtRootNode.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class ASTPtRootNode extends PropertyConstraintASTNodeHelper {
    /**
     * Construct the ASTPtRootNode property constraint adapter associated
     * with the given component and solver. The constructed adapter
     * implicitly uses the default constraints set by the solver.
     * @param solver The given solver.
     * @param actor The given Scale actor
     * @exception IllegalActionException If the adapter cannot be
     * initialized in the superclass.
     */
    public ASTPtRootNode(PropertyConstraintSolver solver,
            ptolemy.data.expr.ASTPtRootNode actor)
            throws IllegalActionException {

        super(solver, actor);
    }

    /**
     * Construct an ASTPtRootNode adapter for the given
     * property solver and AST node.
     * @param solver The given component.
     * @param actor The given AST node.
     * @param useDefaultConstraints Indicate whether this adapter
     *  uses the default actor constraints.
     * @exception IllegalActionException If the adapter cannot
     *  be initialized.
     */
    public ASTPtRootNode(PropertyConstraintSolver solver,
            ptolemy.data.expr.ASTPtRootNode actor, boolean useDefaultConstraints)
            throws IllegalActionException {
        super(solver, actor, useDefaultConstraints);
    }
}
