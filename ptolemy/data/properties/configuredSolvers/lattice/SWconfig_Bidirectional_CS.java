package ptolemy.data.properties.configuredSolvers.lattice;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public class SWconfig_Bidirectional_CS extends PropertyConstraintSolver {

    public SWconfig_Bidirectional_CS(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propertyLattice.setExpression("softwareConfiguration");
        propertyLattice.setVisibility(Settable.NOT_EDITABLE);
        solvingFixedPoint.setExpression("least");
        solvingFixedPoint.setVisibility(Settable.NOT_EDITABLE);
        actorConstraintType.setExpression("out == in");
        actorConstraintType.setVisibility(Settable.NOT_EDITABLE);
        connectionConstraintType.setExpression("sink == src");
        connectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        compositeConnectionConstraintType.setExpression("sink == src");
        compositeConnectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        expressionASTNodeConstraintType.setExpression("parent == child");
        expressionASTNodeConstraintType.setVisibility(Settable.NOT_EDITABLE);
        fsmConstraintType.setExpression("sink == src");
        fsmConstraintType.setVisibility(Settable.NOT_EDITABLE);

        // Add default highlight colors
        StringAttribute highlightNotSpecifiedProperty = new StringAttribute(_momlHandler, "notSpecified");
        highlightNotSpecifiedProperty.setExpression("NotSpecified");
        ColorAttribute highlightNotSpecifiedColor = new ColorAttribute(_momlHandler, "notSpecifiedHighlightColor");
        highlightNotSpecifiedColor.setExpression("{0.4,0.4,0.4,1.0}");
        
        StringAttribute highlightConfiguredProperty = new StringAttribute(_momlHandler, "configured");
        highlightConfiguredProperty.setExpression("Configured");
        ColorAttribute highlightConfiguredColor = new ColorAttribute(_momlHandler, "configuredHighlightColor");
        highlightConfiguredColor.setExpression("{0.0,0.8,0.2,1.0}");
        
        StringAttribute highlightNotConfiguredProperty = new StringAttribute(_momlHandler, "notConfigured");
        highlightNotConfiguredProperty.setExpression("NotConfigured");
        ColorAttribute highlightNotConfiguredColor = new ColorAttribute(_momlHandler, "notConfiguredHighlightColor");
        highlightNotConfiguredColor.setExpression("{0.0,0.2,1.0,1.0}");

        StringAttribute highlightConflictProperty = new StringAttribute(_momlHandler, "conflict");
        highlightConflictProperty.setExpression("Conflict");
        ColorAttribute highlightConflictColor = new ColorAttribute(_momlHandler, "conflictHighlightColor");
        highlightConflictColor.setExpression("{1.0,0.0,0.0,1.0}");
    }        

}
