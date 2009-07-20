/*
Below is the copyright agreement for the Ptolemy II system.

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
*/
package ptolemy.data.properties.configuredSolvers.lattice;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public class LogicalOR_Backward_CS extends PropertyConstraintSolver {

    public LogicalOR_Backward_CS(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propertyLattice.setExpression("logicalOrBackward");
        propertyLattice.setVisibility(Settable.NOT_EDITABLE);
        solvingFixedPoint.setExpression("least");
        solvingFixedPoint.setVisibility(Settable.NOT_EDITABLE);
        actorConstraintType.setExpression("in >= out");
        actorConstraintType.setVisibility(Settable.NOT_EDITABLE);
        connectionConstraintType.setExpression("src >= sink");
        connectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        compositeConnectionConstraintType.setExpression("src >= sink");
        compositeConnectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        expressionASTNodeConstraintType.setExpression("child >= parent");
        expressionASTNodeConstraintType.setVisibility(Settable.NOT_EDITABLE);
        fsmConstraintType.setExpression("src >= sink");
        fsmConstraintType.setVisibility(Settable.NOT_EDITABLE);

        // Add default highlight colors
        StringAttribute highlightUnknownProperty = new StringAttribute(
                _momlHandler, "unknown");
        highlightUnknownProperty.setExpression("Unknown");
        ColorAttribute highlightUnknownColor = new ColorAttribute(_momlHandler,
                "unknownHighlightColor");
        highlightUnknownColor.setExpression("{0.0,0.0,0.0,1.0}");

        StringAttribute highlightTrueProperty = new StringAttribute(
                _momlHandler, "true");
        highlightTrueProperty.setExpression("True");
        ColorAttribute highlightTrueColor = new ColorAttribute(_momlHandler,
                "trueHighlightColor");
        highlightTrueColor.setExpression("{0.0,0.8,0.2,1.0}");

        StringAttribute highlightFalseProperty = new StringAttribute(
                _momlHandler, "false");
        highlightFalseProperty.setExpression("False");
        ColorAttribute highlightFalseColor = new ColorAttribute(_momlHandler,
                "falseHighlightColor");
        highlightFalseColor.setExpression("{0.0,0.2,1.0,1.0}");
    }

}
