/* Base class for an Accessor generator adapter.

 Copyright (c) 2009-2016 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.accessor;

import ptolemy.actor.lib.conversions.json.TokenToJSON;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.data.Token;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// AccessorCodeGeneratorAdapter

/**
 * Base class for an Accessor code generator adapter.
 *
 * <p>Subclasses should override generateAccessor().
 *
 * <p>Subclasses should be sure to properly indent the code by
 * either using the code block functionality in methods like
 * _generateBlockCode(String) or by calling
 * {@link ptolemy.cg.kernel.generic.program.CodeStream#indent(String)},
 * for example:
 * <pre>
 *     StringBuffer code = new StringBuffer();
 *     code.append(super.generateWrapupCode());
 *     code.append("// Local wrapup code");
 *     return processCode(CodeStream.indent(code.toString()));
 * </pre>
 *
 * @author Christopher Brooks.  Based on AccessorCodeGeneratorAdapter by Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (rodiers)
 * @Pt.AcceptedRating Yellow (rodiers)
 */
public abstract class AccessorCodeGeneratorAdapter extends CodeGeneratorAdapter {

    /** Construct the code generator adapter associated
     *  with the given component.
     *  @param component The associated component.
     */
    public AccessorCodeGeneratorAdapter(NamedObj component) {
        _component = component;
    }

    /** Generate Accessor code.
     *  @return The generated Accessor
     *  @exception IllegalActionException If there is a problem
     *  reading data from the model while generating Accessor code.
     */
    abstract public String generateAccessor() throws IllegalActionException;

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(GenericCodeGenerator)
     */
    @Override
    public GenericCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /** Given a Ptolemy expression, return an Accessor-compatibile
     *  expression.  For example, strings are quoted, ints and doubles
     *  and JSON are not.  Note that this method incorrectly double
     *  quotes JSON expressions.  To avoid this, use
     *  {@link #targetExpression(Parameter)}.
     *
     *  @param expression the Ptolemy expression.
     *  @param ptType The Ptolemy Type.
     *  @return A JavaScript-compatible expression
     */
    public String targetExpression(String expression, Type ptType) {
        throw new InternalErrorException(_component, null, "Do not invoke "
                + "AccessorCodeGeneratorAdapter.targetExpression(String expression, Typ;e ptType) "
                + "with accessors because it will incorrectly double quote JSON parameters. "
                + "Instead, use "
                + "AccessorCodeGeneratorAdapter.targetExpression(Parameter parameter)");
    }

    /** Given a Ptolemy Parameter, return an Accessor-compatibile
     *  expression.  For example, strings are quoted, ints and doubles
     *  and JSON are not.
     *  @param parameter the Ptolemy expression.
     *  @return A JavaScript-compatible expression
     *  @exception IllegalActionException If thrown will getting the
     *  type or expression of the parameter.
     */
    public String targetExpression(Parameter parameter) throws IllegalActionException{
        // Should not use getExpression() here, because the expression may be
        // referring to other parameters in scope. The code generator needs to
        // juse use the value of the token.
        Token token = parameter.getToken();

        // if (parameter.getName().equals("correctValues")) {
        //     new Exception("ACGA: targetExpression(): parameter: " + parameter + " parameter.getType(): " + parameter.getType()
        //                   + ", parameter.isStringMode(): " + parameter.isStringMode()
        //                   + "\n        token: " + token + "\n        token.getType(): " + token.getType() + ", parameter.getAttribute(\"_JSON\"): " + parameter.getAttribute("_JSON") + ", token instanceof StringToken: " + (token instanceof ptolemy.data.StringToken)).printStackTrace();
        // }

        String returnValue = TokenToJSON.constructJSON(token);

        // FIXME: It turns out that StringTokens that have a _JSON parameter attached to them are special.
        // So, we undo a bunch of the backslashing.  This is not robust, but it will get us moving again.
        // To replicate:
        // $PTII/bin/ptinvoke ptolemy.cg.kernel.generic.accessor.AccessorCodeGenerator -language accessor $PTII/ptolemy/actor/lib/jjs/modules/httpClient/test/auto/TestRESTPut.xml
        if (parameter.getAttribute("_JSON") != null && (token instanceof StringToken)) {

            returnValue = returnValue.replace("\\\"", "\"");
            // System.out.println("ACGA.targetExpression: returnValue0: " + returnValue);
            returnValue = returnValue.replace("\\\\n", "\\n");
            // System.out.println("ACGA.targetExpression: returnValue1: " + returnValue);
            returnValue = returnValue.replace("\\\"", "\"");
            // System.out.println("ACGA.targetExpression: returnValue2: " + returnValue);
            returnValue = returnValue.replace("\\\\\"", "\"");
            // System.out.println("ACGA.targetExpression: returnValue2a: " + returnValue);
            returnValue = returnValue.substring(1, returnValue.length() - 1);
            // System.out.println("ACGA.targetExpression: returnValue3: " + returnValue);
        }

        return returnValue;
    }

    /** Set the code generator associated with this adapter class.
     *  @param codeGenerator The code generator associated with this
     *   adapter class.
     *  @see #getCodeGenerator()
     */
    @Override
    public void setCodeGenerator(GenericCodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
    }

    /**
     * Get the corresponding type in the target language
     * from the given Ptolemy type.
     * @param ptType The given Ptolemy type.
     * @return The target language data type.
     */
    public String targetType(Type ptType) {
        // targetType() is called by
        // ptolemy/cg/adapter/generic/accessor/adapters/ptolemy/actor/TypedCompositeActor.java
        
        String returnValue = null;
        if (ptType == BaseType.INT) {
            returnValue = "int";
        } else if ( ptType == BaseType.STRING) {
            returnValue = "string";
        } else if ( ptType == BaseType.DOUBLE) {
            returnValue = "number";
        } else if ( ptType == BaseType.BOOLEAN) {
            returnValue = "boolean";
        } else if ( ptType == BaseType.LONG) {
            returnValue = "number";
        } else if ( ptType == BaseType.UNSIGNED_BYTE) {
            returnValue = "number";
        } else {
            returnValue = ptType.toString();
        }
        return returnValue;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    /** Level one indent string. */
    protected final static String _INDENT1 = "    ";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The code generator that contains this adapter class.*/
    protected GenericCodeGenerator _codeGenerator;

    /** The associated component. */
    private NamedObj _component;
}
