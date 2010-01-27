/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 2008-2010 The Regents of the University of California.
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
import ptolemy.data.properties.lattice.DeltaConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public class DimensionSystem_CS extends DeltaConstraintSolver {

    public DimensionSystem_CS(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propertyLattice.setExpression("dimensionSystem");
        propertyLattice.setVisibility(Settable.NOT_EDITABLE);
        solvingFixedPoint.setExpression("least");
        solvingFixedPoint.setVisibility(Settable.NOT_EDITABLE);
        actorConstraintType.setExpression("out == in");
        actorConstraintType.setVisibility(Settable.NOT_EDITABLE);
        connectionConstraintType.setExpression("sink == src");
        connectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        compositeConnectionConstraintType.setExpression("sink == src");
        compositeConnectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        expressionASTNodeConstraintType.setExpression("parent >= child");
        expressionASTNodeConstraintType.setVisibility(Settable.NOT_EDITABLE);
        fsmConstraintType.setExpression("sink == src");
        fsmConstraintType.setVisibility(Settable.NOT_EDITABLE);

        // Add default highlight colors
        StringAttribute highlightSpeedProperty = new StringAttribute(
                _momlHandler, "speed");
        highlightSpeedProperty.setExpression("Speed");
        ColorAttribute highlightSpeedColor = new ColorAttribute(_momlHandler,
                "speedHighlightColor");
        highlightSpeedColor.setExpression("{1.0,1.0,0.4,1.0}");
        
        StringAttribute highlightAccelerationProperty = new StringAttribute(
                _momlHandler, "acceleration");
        highlightAccelerationProperty.setExpression("Acceleration");
        ColorAttribute highlightAccelerationColor = new ColorAttribute(_momlHandler,
                "accelerationHighlightColor");
        highlightAccelerationColor.setExpression("{1.0,0.6,0.6,1.0}");
        
        StringAttribute highlightPositionProperty = new StringAttribute(
                _momlHandler, "position");
        highlightPositionProperty.setExpression("Position");
        ColorAttribute highlightPositionColor = new ColorAttribute(_momlHandler,
                "positionHighlightColor");
        highlightPositionColor.setExpression("{0.4,0.4,1.0,1.0}");
        
        StringAttribute highlightTopProperty = new StringAttribute(
                _momlHandler, "top");
        highlightTopProperty.setExpression("Top");
        ColorAttribute highlightTopColor = new ColorAttribute(_momlHandler,
                "topHighlightColor");
        highlightTopColor.setExpression("{0.6,0.6,0.6,1.0}");
        
        StringAttribute highlightTimeProperty = new StringAttribute(
                _momlHandler, "time");
        highlightTimeProperty.setExpression("Time");
        ColorAttribute highlightTimeColor = new ColorAttribute(_momlHandler,
                "timeHighlightColor");
        highlightTimeColor.setExpression("{0.8,1.0,1.0,1.0}");
        
        StringAttribute highlightUnitlessProperty = new StringAttribute(
                _momlHandler, "unitless");
        highlightUnitlessProperty.setExpression("Unitless");
        ColorAttribute highlightUnitlessColor = new ColorAttribute(_momlHandler,
                "unitlessHighlightColor");
        highlightUnitlessColor.setExpression("{1.0,1.0,1.0,1.0}");
        
        StringAttribute highlightUnknownProperty = new StringAttribute(
                _momlHandler, "unknown");
        highlightUnknownProperty.setExpression("Unknown");
        ColorAttribute highlightUnknownColor = new ColorAttribute(_momlHandler,
                "unknownHighlightColor");
        highlightUnknownColor.setExpression("{0.2,0.2,0.2,1.0}");

    }

}
