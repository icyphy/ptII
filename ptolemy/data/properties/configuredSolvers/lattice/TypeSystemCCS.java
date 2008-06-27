package ptolemy.data.properties.configuredSolvers.lattice;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.properties.lattice.PropertyConstraintSolver;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;

public class TypeSystemCCS extends PropertyConstraintSolver {

    public TypeSystemCCS(NamedObj container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);
        propertyLattice.setExpression("typeSystem_C");
        propertyLattice.setVisibility(Settable.NOT_EDITABLE);
        solvingFixedPoint.setExpression("least");
        solvingFixedPoint.setVisibility(Settable.NOT_EDITABLE);
        actorConstraintType.setExpression("out >= in");
        actorConstraintType.setVisibility(Settable.NOT_EDITABLE);
        connectionConstraintType.setExpression("sink >= src");
        connectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        compositeConnectionConstraintType.setExpression("sink >= src");
        compositeConnectionConstraintType.setVisibility(Settable.NOT_EDITABLE);
        expressionASTNodeConstraintType.setExpression("parent >= child");
        expressionASTNodeConstraintType.setVisibility(Settable.NOT_EDITABLE);
        fsmConstraintType.setExpression("sink >= src");
        fsmConstraintType.setVisibility(Settable.NOT_EDITABLE);

        // Add default highlight colors
        StringAttribute highlightUnknownProperty = new StringAttribute(_highlighter, "unknown");
        highlightUnknownProperty.setExpression("Unknown");
        ColorAttribute highlightUnknownColor = new ColorAttribute(_highlighter, "unknownHighlightColor");
        highlightUnknownColor.setExpression("{0.0,0.0,0.0,1.0}");
        StringAttribute highlightConflictProperty = new StringAttribute(_highlighter, "conflict");
        highlightConflictProperty.setExpression("Conflict");
        ColorAttribute highlightConflictColor = new ColorAttribute(_highlighter, "conflictHighlightColor");
        highlightConflictColor.setExpression("{1.0,0.0,0.0,1.0}");
        
        StringAttribute highlightIntProperty = new StringAttribute(_highlighter, "int");
        highlightIntProperty.setExpression("Int");
        ColorAttribute highlightIntColor = new ColorAttribute(_highlighter, "intHighlightColor");
        highlightIntColor.setExpression("{0.4,0.8,1.0,1.0}");
        StringAttribute highlightSInt16Property = new StringAttribute(_highlighter, "sint16");
        highlightSInt16Property.setExpression("SInt16");
        ColorAttribute highlightSInt16Color = new ColorAttribute(_highlighter, "sint16HighlightColor");
        highlightSInt16Color.setExpression("{0.0,0.4,1.0,1.0}");
        StringAttribute highlightSInt32Property = new StringAttribute(_highlighter, "sint32");
        highlightSInt32Property.setExpression("SInt32");
        ColorAttribute highlightSInt32Color = new ColorAttribute(_highlighter, "sint32HighlightColor");
        highlightSInt32Color.setExpression("{0.0,0.2,0.8,1.0}");
        
        StringAttribute highlightUInt8Property = new StringAttribute(_highlighter, "uint8");
        highlightUInt8Property.setExpression("UInt8");
        ColorAttribute highlightUInt8Color = new ColorAttribute(_highlighter, "uint8HighlightColor");
        highlightUInt8Color.setExpression("{0.6,1.0,0.6,1.0}");
        StringAttribute highlightUInt16Property = new StringAttribute(_highlighter, "uint16");
        highlightUInt16Property.setExpression("UInt16");
        ColorAttribute highlightUInt16Color = new ColorAttribute(_highlighter, "uint16HighlightColor");
        highlightUInt16Color.setExpression("{0.0,0.8,0.0,1.0}");
        StringAttribute highlightUInt32Property = new StringAttribute(_highlighter, "uint32");
        highlightUInt32Property.setExpression("UInt32");
        ColorAttribute highlightUInt32Color = new ColorAttribute(_highlighter, "uint32HighlightColor");
        highlightUInt32Color.setExpression("{0.0,0.6,0.2,1.0}");

        StringAttribute highlightBooleanProperty = new StringAttribute(_highlighter, "boolean");
        highlightBooleanProperty.setExpression("Boolean");
        ColorAttribute highlightBooleanColor = new ColorAttribute(_highlighter, "booleanHighlightColor");
        highlightBooleanColor.setExpression("{1.0,0.6,0.2,1.0}");

        StringAttribute highlightReal32Property = new StringAttribute(_highlighter, "real32");
        highlightReal32Property.setExpression("Real32");
        ColorAttribute highlightReal32Color = new ColorAttribute(_highlighter, "real32HighlightColor");
        highlightReal32Color.setExpression("{0.8,0.6,1.0,1.0}");
        StringAttribute highlightReal64Property = new StringAttribute(_highlighter, "real64");
        highlightReal64Property.setExpression("Real64");
        ColorAttribute highlightReal64Color = new ColorAttribute(_highlighter, "real64HighlightColor");
        highlightReal64Color.setExpression("{0.6,0.0,0.6,1.0}");
    }        

}
