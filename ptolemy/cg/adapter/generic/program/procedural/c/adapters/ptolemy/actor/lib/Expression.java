/* A code generation adapter class for actor.lib.Expression
 @Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.actor.lib;

import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.c.CParseTreeCodeGenerator;
import ptolemy.data.DoubleToken;
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
import ptolemy.kernel.util.InternalErrorException;

/**
 * A code generation adapter class for ptolemy.actor.lib.Expression.
 *
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (mankit) Needs 2nd pass for array children of different types
 * @Pt.AcceptedRating Red (mankit)
 */
public class Expression extends NamedProgramCodeGeneratorAdapter {
    /**
     * Constructor method for the Expression adapter.
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
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    protected String _generateFireCode() throws IllegalActionException {
        CParseTreeCodeGenerator parseTreeCG = _cParseTreeCodeGenerator;

        StringBuffer code = new StringBuffer();
        code.append(super._generateFireCode());

        code.append(processCode("$put(output, "
                + parseTreeCG.generateFireCode())
                + ");" + _eol);
        return code.toString();
    }

    /**
     * Generate initialize code.
     * This method reads the <code>initBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    @Override
    public String generateInitializeCode() throws IllegalActionException {
        CParseTreeCodeGenerator parseTreeCG = _cParseTreeCodeGenerator;

        StringBuffer code = new StringBuffer();
        code.append(super.generateInitializeCode());
        code.append(processCode(parseTreeCG.generateInitializeCode()));
        return code.toString();
    }

    /**
     * Generate preinitialize code.
     * This method reads the <code>preinitBlock</code> from Expression.c,
     * replaces macros with their values and returns the processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     * @return The processed code string.
     */
    @Override
    public String generatePreinitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generatePreinitializeCode());

        if (_cParseTreeCodeGenerator == null) {
            // FIXME: why does this need to be done here?
            _cParseTreeCodeGenerator = new CParseTreeCodeGenerator(
                    getCodeGenerator());
        }

        CParseTreeCodeGenerator parseTreeCG = _cParseTreeCodeGenerator;

        ptolemy.actor.lib.Expression actor = (ptolemy.actor.lib.Expression) getComponent();

        try {
            // Note that the parser is NOT retained, since in most
            // cases the expression doesn't change, and the parser
            // requires a large amount of memory.
            PtParser parser = new PtParser();
            ASTPtRootNode parseTree = parser.generateParseTree(actor.expression
                    .getExpression());

            parseTreeCG.evaluateParseTree(parseTree, new VariableScope(actor));
        } catch (IllegalActionException ex) {
            // Chain exceptions to get the actor that threw the exception.
            throw new IllegalActionException(actor, ex, "Expression \""
                    + actor.expression.getExpression() + "\" invalid.");
        }

        code.append(processCode(parseTreeCG.generatePreinitializeCode()));
        return code.toString();
    }

    /**
     * Get shared code.  This method reads the
     * <code>sharedBlock</code> from Expression.c, replaces macros
     * with their values and returns the processed code string.
     * @return A set of strings that are code shared by multiple instances of
     *  the same actor.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    @Override
    public Set getSharedCode() throws IllegalActionException {

        //CParseTreeCodeGenerator parseTreeCG = _cParseTreeCodeGenerator;

        Set codeBlocks = super.getSharedCode();
        //codeBlocks.add(processCode(parseTreeCG.generateSharedCode()));
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
    @Override
    public String generateWrapupCode() throws IllegalActionException {
        CParseTreeCodeGenerator parseTreeCG = _cParseTreeCodeGenerator;

        StringBuffer code = new StringBuffer();
        code.append(super.generateWrapupCode());
        code.append(processCode(parseTreeCG.generateWrapupCode()));

        // Free up memory
        //_cParseTreeCodeGenerator = null;

        return code.toString();
    }

    /**
     * Get the files needed by the code generated for the
     * Expression actor.
     * @return A set of strings that are names of the header files
     *  needed by the code generated for the Expression actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    @Override
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<math.h>");
        files.add("<string.h>");
        return files;
    }

    /** The parse tree code generator. */
    protected CParseTreeCodeGenerator _cParseTreeCodeGenerator;

    /**
     * Variable scope class customized for the CParseTreeCodeGenerator.
     */
    protected static class VariableScope extends ModelScope {
        // Findbugs suggests that this should be static.
        /**
         * Constructor of a VariableScope.
         * @param actor The named ptolemy actor.
         */
        public VariableScope(AtomicActor actor) {
            _actor = actor;
        }

        /** Look up and return the attribute with the specified name.
         *  Return null if such an attribute does not exist.
         *  @param name The name to look up.
         *  @return The attribute with the specified name in the scope.
         */
        @Override
        public Token get(String name) {
            try {
                if (name.equals("time")) {
                    // If the director has the period set to something other
                    // than 0, then return the value of _currentTime
                    // See codegen/kernel/StaticSchedulingCodeGenerator.java.
                    // FIXME: should we check for period being set anywhere
                    // in the hierarchy?
                    Director director = _actor.getDirector();
                    Attribute period = director.getAttribute("period");
                    if (period != null) {
                        Double periodValue = ((DoubleToken) ((Variable) period)
                                .getToken()).doubleValue();
                        if (periodValue != 0.0) {
                            return new ObjectToken("_currentTime");
                        }
                    }
                    return new DoubleToken("0.0");
                } else if (name.equals("iteration")) {
                    return new ObjectToken("$actorSymbol(iterationCount)");
                }

                for (int i = 0; i < _actor.inputPortList().size(); i++) {
                    if (generateSimpleName(
                            (IOPort) _actor.inputPortList().get(i))
                            .equals(name)) {
                        return new ObjectToken("$get(" + name + ")");
                    }
                }

                Attribute attribute = _actor.getAttribute(name);

                if (attribute == null) {
                    attribute = ModelScope
                            .getScopedVariable(null, _actor, name);
                }

                if (attribute != null) {
                    return new ObjectToken("$val(" + name + ")");
                }

                /*
                 for (int i = 0; i < _actor.attributeList().size(); i++) {
                 if (((Attribute) _actor.attributeList().get(i))
                 .getName().equals(name)) {
                 return new ObjectToken("$val(" + name + ")");
                 }
                 }
                 */
            } catch (IllegalActionException ex) {
                // Not thrown here.
                throw new InternalErrorException(ex);
            }

            return null;
        }

        /** Look up and return the type of the attribute with the
         *  specified name in the scope. Return null if such an
         *  attribute does not exist.
         *  @param name The type to look up.  Note that if name
         *  is "time", then the type is BaseType.DOUBLE and if the
         *  name is "iterations", then the type is BaseType.INT.
         *  @return The attribute with the specified name in the scope.
         *  @exception IllegalActionException If thrown whil getting
         *  the port or scoped value.
         */
        @Override
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
         *  @param name The name to look up.
         *  @return The InequalityTerm associated with the given name in
         *  the scope.
         *  @exception IllegalActionException If a value in the scope
         *  exists with the given name, but cannot be evaluated.
         */
        @Override
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
        @Override
        public Set identifierSet() {
            return getAllScopedVariableNames(null, _actor);
        }

        private AtomicActor _actor;
    }
}
