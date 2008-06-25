package ptolemy.data.properties.configuredSolvers.lattice;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public class LogicalOR_Backward_CS extends PropertyConstraintSolver {

    public LogicalOR_Backward_CS(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
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
        StringAttribute highlightUnknownProperty = new StringAttribute(_highlighter, "unknown");
        highlightUnknownProperty.setExpression("Unknown");
        ColorAttribute highlightUnknownColor = new ColorAttribute(_highlighter, "unknownHighlightColor");
        highlightUnknownColor.setExpression("{0.0,0.0,0.0,1.0}");

        StringAttribute highlightTrueProperty = new StringAttribute(_highlighter, "true");
        highlightTrueProperty.setExpression("True");
        ColorAttribute highlightTrueColor = new ColorAttribute(_highlighter, "trueHighlightColor");
        highlightTrueColor.setExpression("{0.0,0.8,0.2,1.0}");
        
        StringAttribute highlightFalseProperty = new StringAttribute(_highlighter, "false");
        highlightFalseProperty.setExpression("False");
        ColorAttribute highlightFalseColor = new ColorAttribute(_highlighter, "falseHighlightColor");
        highlightFalseColor.setExpression("{0.0,0.2,1.0,1.0}");
    }        

}
