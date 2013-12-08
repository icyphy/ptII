/* A visitor for parse trees of the expression language.

 Copyright (c) 2006-2013 The Regents of the University of California
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.


 */
package ptolemy.cg.kernel.generic.program.procedural.java;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.data.ArrayToken;
import ptolemy.data.BitwiseOperationToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.FunctionToken;
import ptolemy.data.LongToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.OrderedRecordToken;
import ptolemy.data.RecordToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.ASTPtArrayConstructNode;
import ptolemy.data.expr.ASTPtBitwiseNode;
import ptolemy.data.expr.ASTPtFunctionApplicationNode;
import ptolemy.data.expr.ASTPtFunctionDefinitionNode;
import ptolemy.data.expr.ASTPtFunctionalIfNode;
import ptolemy.data.expr.ASTPtLeafNode;
import ptolemy.data.expr.ASTPtLogicalNode;
import ptolemy.data.expr.ASTPtMatrixConstructNode;
import ptolemy.data.expr.ASTPtMethodCallNode;
import ptolemy.data.expr.ASTPtOrderedRecordConstructNode;
import ptolemy.data.expr.ASTPtPowerNode;
import ptolemy.data.expr.ASTPtProductNode;
import ptolemy.data.expr.ASTPtRecordConstructNode;
import ptolemy.data.expr.ASTPtRelationalNode;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ASTPtShiftNode;
import ptolemy.data.expr.ASTPtSumNode;
import ptolemy.data.expr.ASTPtUnaryNode;
import ptolemy.data.expr.AbstractParseTreeVisitor;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.ExpressionFunction;
import ptolemy.data.expr.ParseTreeSpecializer;
import ptolemy.data.expr.ParseTreeTypeInference;
import ptolemy.data.expr.ParserScope;
import ptolemy.data.expr.PtParserConstants;
import ptolemy.data.expr.Token;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.FunctionType;
import ptolemy.data.type.MatrixType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.math.Complex;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaParseTreeCodeGenerator

/**
 Evaluate a parse tree given a reference to its root node and generate Java code.
 It implements a visitor that visits the parse tree in depth-first order,
 evaluating each node and storing the result as a token in the node.
 Two exceptions are logic nodes and the ternary if node (the ? : construct),
 which do not necessarily evaluate all children nodes.

 <p>This class has the following limitations:
 <ul>
 <li> It is a copy of ParseTreeEvaluator from data/expr and thus
 has lots of code for evaluating expressions, which we don't need
 <li> It is not properly converting types: We need to add logic to
 convert types.
 <li> The .tcl test has known failures involving nulls.
 <li> It does not evaluate constants.
 </ul>

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red
 @Pt.AcceptedRating Red
 @see ptolemy.data.expr.ASTPtRootNode
 */
public class JavaParseTreeCodeGenerator extends ProceduralParseTreeCodeGenerator {

    /**
     * Create a JavaParseTreeCodeGenerator that is used by
     * the given code generator to generate code for expressions.
     * @param generator The given code generator.
     */
    public JavaParseTreeCodeGenerator(ProgramCodeGenerator generator) {
        super(generator);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    static {
        _functionMap.put("matrixToArray", "$matrixToArray");
        _functionMap.put("roundToInt", "(int)");
        _functionMap.put("repeat", "$arrayRepeat");
        _functionMap.put("sum", "$arraySum");

        // Java Specific functions
        _functionMap.put("NaN", "Double.NaN");
        _functionMap.put("abs", "Math.abs");
        _functionMap.put("acos", "Math.acos");
        _functionMap.put("asin", "Math.asin");
        _functionMap.put("atan", "Math.atan");
        _functionMap.put("cbrt", "Math.cbrt");
        _functionMap.put("ceil", "Math.ceil");
        _functionMap.put("cos", "Math.cos");
        _functionMap.put("cosh", "Math.cosh");
        _functionMap.put("exp", "Math.exp");
        _functionMap.put("expm1", "Math.expm1");
        _functionMap.put("floor", "Math.floor");
        _functionMap.put("iterate",
                "ptolemy.data.expr.UtilityFunctions.iterate");
        _functionMap.put("log", "Math.log");
        _functionMap.put("log10", "Math.log10");
        _functionMap.put("log1p", "Math.log1p");
        _functionMap.put("max", "Math.max");
        _functionMap.put("min", "Math.min");
        _functionMap.put("pow", "Math.pow");
        _functionMap.put("rint", "Math.rint");
        _functionMap.put("round", "Math.round");
        _functionMap.put("signum", "Math.signum");
        _functionMap.put("sin", "Math.sin");
        _functionMap.put("sinh", "Math.sinh");
        _functionMap.put("sqrt", "Math.sqrt");
        _functionMap.put("tan", "Math.tan");
        _functionMap.put("tanh", "Math.tanh");
        _functionMap.put("toDegrees", "Math.toDegrees");
        _functionMap.put("toRadians", "Math.toRadians");
        _functionMap.put("ulp", "Math.ulp");
    }
}
