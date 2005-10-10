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

import ptolemy.actor.AtomicActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.data.ObjectToken;
import ptolemy.data.Token;
import ptolemy.data.expr.ASTPtRootNode;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.PtParser;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeConstant;
import ptolemy.kernel.util.Attribute;
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
        
        Type portType = ((ptolemy.actor.lib.Expression) 
                this.getComponent()).output.getType();

        // if port type is not primitive, then we use token type
        if (portType != BaseType.DOUBLE && portType != BaseType.INT &&
                portType != BaseType.STRING && portType != BaseType.BOOLEAN) {
        	portType = BaseType.GENERAL;
        }
        code.append(processCode("    $ref(output)." + portType + "Port = ("
                + _parseTreeCodeGenerator.generateFireCode()) + ");\n");
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
            ASTPtRootNode parseTree = 
                parser.generateParseTree(actor.expression.getExpression());

            _parseTreeCodeGenerator = new ParseTreeCodeGenerator();
            
            result = _parseTreeCodeGenerator.evaluateParseTree(
                    parseTree, new VariableScope(actor));
            
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
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public Set generateSharedCode() throws IllegalActionException {
        Set codeBlocks = new HashSet();
        codeBlocks.add(processCode(
                _parseTreeCodeGenerator.generateSharedCode()));
        return codeBlocks;
    }

    /**
     * Generate wrap up code.
     * This method reads the <code>wrapupBlock</code>
     * from Expression.c, 
     * replaces macros with their values and appends the processed code block
     * to the given code buffer.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateWrapupCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();        
        super.generateWrapupCode();
        code.append(processCode(_parseTreeCodeGenerator.generateWrapupCode()));
        
        // Free up memory
        _parseTreeCodeGenerator = null;
        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * Expression actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the Expression actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<math.h>");
        files.add("<string.h>");
        files.add("<time.h>");
        return files;
    }
    
    protected ParseTreeCodeGenerator _parseTreeCodeGenerator;
    
    /**
     * Variable scope class customized for the ParseTreeCodeGenerator.
     */
    private class VariableScope extends ModelScope {
        
        /**
         * Constructor of a VariableScope.
         * @param actor The named ptolemy actor.
         */
        public VariableScope (AtomicActor actor) {
            _actor = actor;    
        }
        
        /** Look up and return the attribute with the specified name. 
         *  Return null if such an attribute does not exist.
         *  @return The attribute with the specified name in the scope.
         */
        public Token get(String name) {
            try {
                if (name.equals("time")) {
                    return new ObjectToken("time(NULL)");
                } else if (name.equals("iteration")) {
                    return new ObjectToken("(iteration + 1)");
                }
    
                for (int i = 0; i < _actor.inputPortList().size(); i++) {
                	if (((IOPort) _actor.inputPortList().get(i))
                            .getName().equals(name)) {
                        return new ObjectToken("$ref(" + name + ")");
                    }
                }
    
                for (int i = 0; i < _actor.attributeList().size(); i++) {
                    if (((Attribute) _actor.attributeList().get(i))
                            .getName().equals(name)) {
                        return new ObjectToken("$val(" + name + ")");
                    }
                }
            } catch (IllegalActionException ex) {
            	// Not thrown here.
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
            TypedIOPort port = (TypedIOPort) _actor.getPort(name);

            if (port != null) {
                return port.getType();
            }

            Variable result = getScopedVariable(null, _actor, name);

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
            TypedIOPort port = (TypedIOPort) _actor.getPort(name);

            if (port != null) {
                return port.getTypeTerm();
            }

            Variable result = getScopedVariable(null, _actor, name);

            if (result != null) {
                return result.getTypeTerm();
            }
            return null;
        }

        /** Return the list of identifiers within the scope.
         *  @return The list of identifiers within the scope.
         */
        public Set identifierSet() {
            return getAllScopedVariableNames(null, _actor);
        }
        
        private AtomicActor _actor;
    }

}
