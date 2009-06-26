/*
Below is the copyright agreement for the Ptolemy II system.
Version: $Id$

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

public class TypeSystemCCS extends PropertyConstraintSolver {

    public TypeSystemCCS(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
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
        StringAttribute highlightUnknownProperty = new StringAttribute(
                _momlHandler, "unknown");
        highlightUnknownProperty.setExpression("Unknown");
        ColorAttribute highlightUnknownColor = new ColorAttribute(_momlHandler,
                "unknownHighlightColor");
        highlightUnknownColor.setExpression("{0.0,0.0,0.0,1.0}");
        StringAttribute highlightConflictProperty = new StringAttribute(
                _momlHandler, "conflict");
        highlightConflictProperty.setExpression("Conflict");
        ColorAttribute highlightConflictColor = new ColorAttribute(
                _momlHandler, "conflictHighlightColor");
        highlightConflictColor.setExpression("{1.0,0.0,0.0,1.0}");

        StringAttribute highlightIntProperty = new StringAttribute(
                _momlHandler, "int");
        highlightIntProperty.setExpression("Int");
        ColorAttribute highlightIntColor = new ColorAttribute(_momlHandler,
                "intHighlightColor");
        highlightIntColor.setExpression("{0.4,0.8,1.0,1.0}");
        StringAttribute highlightSInt16Property = new StringAttribute(
                _momlHandler, "sint16");
        highlightSInt16Property.setExpression("SInt16");
        ColorAttribute highlightSInt16Color = new ColorAttribute(_momlHandler,
                "sint16HighlightColor");
        highlightSInt16Color.setExpression("{0.0,0.4,1.0,1.0}");
        StringAttribute highlightSInt32Property = new StringAttribute(
                _momlHandler, "sint32");
        highlightSInt32Property.setExpression("SInt32");
        ColorAttribute highlightSInt32Color = new ColorAttribute(_momlHandler,
                "sint32HighlightColor");
        highlightSInt32Color.setExpression("{0.0,0.2,0.8,1.0}");

        StringAttribute highlightUInt8Property = new StringAttribute(
                _momlHandler, "uint8");
        highlightUInt8Property.setExpression("UInt8");
        ColorAttribute highlightUInt8Color = new ColorAttribute(_momlHandler,
                "uint8HighlightColor");
        highlightUInt8Color.setExpression("{0.6,1.0,0.6,1.0}");
        StringAttribute highlightUInt16Property = new StringAttribute(
                _momlHandler, "uint16");
        highlightUInt16Property.setExpression("UInt16");
        ColorAttribute highlightUInt16Color = new ColorAttribute(_momlHandler,
                "uint16HighlightColor");
        highlightUInt16Color.setExpression("{0.0,0.8,0.0,1.0}");
        StringAttribute highlightUInt32Property = new StringAttribute(
                _momlHandler, "uint32");
        highlightUInt32Property.setExpression("UInt32");
        ColorAttribute highlightUInt32Color = new ColorAttribute(_momlHandler,
                "uint32HighlightColor");
        highlightUInt32Color.setExpression("{0.0,0.6,0.2,1.0}");

        StringAttribute highlightBooleanProperty = new StringAttribute(
                _momlHandler, "boolean");
        highlightBooleanProperty.setExpression("Boolean");
        ColorAttribute highlightBooleanColor = new ColorAttribute(_momlHandler,
                "booleanHighlightColor");
        highlightBooleanColor.setExpression("{1.0,0.6,0.2,1.0}");

        StringAttribute highlightReal32Property = new StringAttribute(
                _momlHandler, "real32");
        highlightReal32Property.setExpression("Real32");
        ColorAttribute highlightReal32Color = new ColorAttribute(_momlHandler,
                "real32HighlightColor");
        highlightReal32Color.setExpression("{0.8,0.6,1.0,1.0}");
        StringAttribute highlightReal64Property = new StringAttribute(
                _momlHandler, "real64");
        highlightReal64Property.setExpression("Real64");
        ColorAttribute highlightReal64Color = new ColorAttribute(_momlHandler,
                "real64HighlightColor");
        highlightReal64Color.setExpression("{0.6,0.0,0.6,1.0}");
    }

}
