/* A code generation helper class for actor.lib.Expression
 @Copyright (c) 2005 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES, 
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN \"AS IS\" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, 
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.codegen.c.actor.lib;

import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A code generation helper class for ptolemy.actor.lib.Expression. 
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit) 
 * @Pt.AcceptedRating Red (mankit)
 */
public class Expression extends CCodeGeneratorHelper {

    /**
     * Constructor method for the Expression helper.
     * @param actor The associated actor.
     */
    public Expression(ptolemy.actor.lib.Expression actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>fireBlock</code> from Expression.c,
     * replaces macros with their values and appends the processed code              
     * block to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void  generateFireCode(StringBuffer code)
        throws IllegalActionException {
        code.append(_parseTreeCodeGenerator.generateFireCode());
   }

    /**
     * Generate initialize code.
     * This method reads the <code>initBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generateInitializeCode()
        throws IllegalActionException {
        super.generateInitializeCode();
        return processCode(_parseTreeCodeGenerator.generateInitializeCode());
    }
   
    /**
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generatePreinitializeCode() 
        throws IllegalActionException {
        super.generatePreinitializeCode();

        ptolemy.actor.lib.Expression actor =
            (ptolemy.actor.lib.Expression) getComponent();
        Token result;

        try {
            // Note that the parser is NOT retained, since in most
            // cases the expression doesn't change, and the parser
            // requires a large amount of memory.
            PtParser parser = new PtParser();
            ASTPtRootNode _parseTree = 
                parser.generateParseTree(actor.expression.getExpression());

            _parseTreeCodeGenerator = new ParseTreeCodeGenerator();

            //VariableScope _scope = new VariableScope();

            result = _parseTreeCodeGenerator.evaluateParseTree(_parseTree);
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(null, ex, "Expression invalid.");
        }

        if (result == null) {
            throw new IllegalActionException(null,
                    "Expression yields a null result: "
                            + actor.expression.getExpression());
        }

        return processCode(
                _parseTreeCodeGenerator.generatePreinitializeCode());
    }

    /**
     * Generate shared code.
     * This method reads the <code>sharedBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    public String generateSharedCode() 
        throws IllegalActionException {
        return processCode(_parseTreeCodeGenerator.generateSharedCode());
    }

    /**
     * Generate wrap up code.
     * This method reads the <code>wrapupBlock</code>
     * from Expression.c, 
     * replaces macros with their values and appends the processed code block
     * to the given code buffer.
     * @param code the given buffer to append the code to.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public void generateWrapupCode(StringBuffer code)
        throws IllegalActionException {
        code.append(processCode(_parseTreeCodeGenerator.generateWrapupCode()));

        // Free up memory
        _parseTreeCodeGenerator = null;
    }

    /**
     * Get the files needed by the code generated for the
     * Expression actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the Expression actor.
     */
    public Set getHeaderFiles() {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }
    
    protected ParseTreeCodeGenerator _parseTreeCodeGenerator;
    
    
    private class VariableScope extends ModelScope {
        /** Look up and return the attribute with the specified name in the
         *  scope. Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) throws IllegalActionException {
            if (name.equals("time")) {
                return new DoubleToken(getDirector().getModelTime()
                        .getDoubleValue());
            } else if (name.equals("iteration")) {
                return new IntToken(_iterationCount);
            }

            Token token = (Token) _tokenMap.get(name);

            if (token != null) {
                return token;
            }

            Variable result = getScopedVariable(null, Expression.this, name);

            if (result != null) {
                return result.getToken();
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Type getType(String name) throws IllegalActionException {
            if (name.equals("time")) {
                return BaseType.DOUBLE;
            } else if (name.equals("iteration")) {
                return BaseType.INT;
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) getPort(name);

            if (port != null) {
                return port.getType();
            }

            Variable result = getScopedVariable(null, Expression.this, name);

            if (result != null) {
                return (Type) result.getTypeTerm().getValue();
            }

            return null;
        }

        /** Look up and return the type term for the specified name
         *  in the scope. Return null if the name is not defined in this
         *  scope, or is a constant type.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        public ptolemy.graph.InequalityTerm getTypeTerm(String name)
                throws IllegalActionException {
            if (name.equals("time")) {
                return new TypeConstant(BaseType.DOUBLE);
            } else if (name.equals("iteration")) {
                return new TypeConstant(BaseType.INT);
            }

            // Check the port names.
            TypedIOPort port = (TypedIOPort) getPort(name);

            if (port != null) {
                return port.getTypeTerm();
            }

            Variable result = getScopedVariable(null, Expression.this, name);

            if (result != null) {
                return result.getTypeTerm();
            }

            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        public Set identifierSet() {
            return getAllScopedVariableNames(null, Expression.this);
        }
    }

}
