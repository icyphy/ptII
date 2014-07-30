/* A class to parse the java template macro constructs in a code generation scope.

Copyright (c) 2009-2014 The Regents of the University of California.
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

package ptolemy.cg.kernel.generic.program.procedural.java;

import java.util.Locale;

import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.ProgramCodeGenerator;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralTemplateParser;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////Java TemplateParser

/**
A class that allows to parse macros of templates in a code generator
perspective.


@author Bert Rodiers
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
 */

public class JavaTemplateParser extends ProceduralTemplateParser {

    /** Construct the JavaTemplateParser associated
     *  with the given component and the given adapter.
     */
    public JavaTemplateParser() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the translated token instance function invocation string.
     *  @param functionString The string within the $tokenFunc() macro.
     *  @param isStatic True if the method is static.
     *  @return The translated type function invocation string.
     *  @exception IllegalActionException The given function string is
     *   not well-formed.
     */
    @Override
    public String getFunctionInvocation(String functionString, boolean isStatic)
            throws IllegalActionException {
        //System.out.println("JTP.getFunctionInvocation: " + functionString + " " + isStatic);
        // Record the referenced type function in the infoTable.
        super.getFunctionInvocation(functionString, isStatic);

        // FIXME: lots of duplicated code from superclass here.
        functionString = processCode(functionString);

        // i.e. "$tokenFunc(token::add(arg1, arg2, ...))"
        // this transforms to ==>
        // "functionTable[token.type][FUNC_add] (token, arg1, arg2, ...)"
        // FIXME: we need to do some more smart parsing to find the following
        // indexes.
        int commaIndex = functionString.indexOf("::");
        int openFuncParenIndex = functionString.indexOf('(', commaIndex);
        int closeFuncParenIndex = functionString.lastIndexOf(')');

        // Syntax checking.
        if (commaIndex == -1 || openFuncParenIndex == -1
                || closeFuncParenIndex != functionString.length() - 1) {
            throw new IllegalActionException(
                    "Bad Syntax with the $tokenFunc / $typeFunc macro. "
                            + "[i.e. -- $tokenFunc(typeOrToken::func(arg1, ...))].  "
                            + "String was:\n:" + functionString);
        }

        String typeOrToken = functionString.substring(0, commaIndex).trim();
        String functionName = functionString.substring(commaIndex + 2,
                openFuncParenIndex).trim();

        String argumentList = functionString.substring(openFuncParenIndex + 1)
                .trim();

        // addFunctionUsed dereferences the parent class _codeGenerator.
        if (_getCodeGenerator() == null) {
            throw new NullPointerException(
                    "Call TemplateParser.setCodeGenerator() "
                            + "before calling getFunctionInvocation()");
        }
        //         if (functionName.indexOf("Complex") != -1
        //                 || functionName.indexOf("convert") != -1) {
        //             throw new RuntimeException("FIXME! missing if block?");
        //         }
        // Record the referenced type function in _typeFuncUsed
        addFunctionUsed(functionName);
        int underbar = typeOrToken.indexOf("_");
        if (underbar != -1) {
            String type = typeOrToken.substring(underbar + 1,
                    typeOrToken.length());
            if (_getCodeGenerator().isPrimitive(type) || type.equals("Complex")
                    || type.equals("Object")) {
                addNewTypesUsed(type);
            }
        }

        if (isStatic) {
            if (argumentList.length() == 0) {
                throw new IllegalActionException(
                        "Static type function requires at least one argument(s).");
            }

            //return "functionTable[(int)" + typeOrToken + "][FUNC_"
            //+ functionName + "](" + argumentList;

            String methodType = typeOrToken
                    .substring(typeOrToken.indexOf('_') + 1);
            return methodType + "_" + functionName + "(" + argumentList;

        } else {
            // if it is more than just a closing paren
            if (argumentList.length() > 1) {
                argumentList = ", " + argumentList;
            }

            //return "functionTable[(int)" + typeOrToken + ".type][FUNC_"
            //+ functionName + "](" + typeOrToken + argumentList;
            //String methodType = typeOrToken.substring(typeOrToken.indexOf('_') + 1);
            _getCodeGenerator().markFunctionCalled(
                    functionName + "_Token_Token", this);
            return functionName + "_Token_Token(" + typeOrToken + argumentList;
        }
    }

    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    @Override
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        // FIXME: We need to create new ParseTreeCodeGenerator each time
        // here or else we get lots of test failures.  It would be better
        // if we could use the same JavaParseTreeCodeGenerator over and over.
        _parseTreeCodeGenerator = new JavaParseTreeCodeGenerator(
                _getCodeGenerator());
        return _parseTreeCodeGenerator;
    }

    /** Return the translated new constructor invocation string. Keep the types
     *  referenced in the info table of this adapter. The kernel will retrieve
     *  this information to determine the total number of referenced types in
     *  the model.
     *  @param constructorString The string within the $new() macro.
     *  @return The translated new constructor invocation string.
     *  @exception IllegalActionException The given constructor string is
     *   not well-formed.
     */
    @Override
    public String getNewInvocation(String constructorString)
            throws IllegalActionException {
        addFunctionUsed("new");
        return super.getNewInvocation(constructorString);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     */
    @Override
    protected JavaCodeGenerator _getCodeGenerator() {
        return (JavaCodeGenerator) super._getCodeGenerator();
    }

    /** Return the replacement string of the given macro. Subclass
     * of GenericCodeGenerator may overriding this method to extend or support
     * a different set of macros.
     * @param macro The given macro.
     * @param parameter The given parameter to the macro.
     * @return The replacement string of the given macro.
     * @exception IllegalActionException Thrown if the given macro or
     *  parameter is not valid.
     */
    @Override
    protected String _replaceMacro(String macro, String parameter)
            throws IllegalActionException {
        ProgramCodeGenerator codeGenerator = _getCodeGenerator();
        String result = super._replaceMacro(macro, parameter);

        if (result != null) {
            if (macro.equals("cgType")) {
                return result.replace("Int", "Integer").replace("Integereger",
                        "Integer");
            }
            return result;
        }

        if (macro.equals("include")) {
            _includeFiles.add(parameter);
            return "";
        } else if (macro.equals("refinePrimitiveType")) {
            TypedIOPort port = getPort(parameter);

            if (port == null) {
                throw new IllegalActionException(
                        parameter
                        + " is not a port. $refinePrimitiveType macro takes in a port.");
            }
            if (codeGenerator.isPrimitive(port.getType())) {
                return ".payload/*jcgh*/."
                        + codeGenerator.codeGenType(port.getType());
            } else {
                return "";
            }
        } else if (macro.equals("lcCgType")) {
            String cgType = _replaceMacro("cgType", parameter);
            if (cgType.equals("Integer")) {
                return "int";
            }
            return cgType.toLowerCase(Locale.getDefault());
        }

        // We will assume that it is a call to a polymorphic
        // functions.
        //String[] call = macro.split("_");
        _getCodeGenerator().markFunctionCalled(macro, this);
        result = macro + "(" + parameter + ")";

        return result;
    }

}
