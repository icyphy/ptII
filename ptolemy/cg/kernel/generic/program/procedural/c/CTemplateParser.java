/* A class to parse the C template macro constructs in a code generation scope.

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

package ptolemy.cg.kernel.generic.program.procedural.c;

import java.util.Locale;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.ParseTreeCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralTemplateParser;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.modal.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CTemplateParser

/**
A class that allows to parse macros of templates in a code generator
perspective.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
 */

public class CTemplateParser extends ProceduralTemplateParser {

    /** Construct the CTemplateParser associated
     *  with the given component and the given adapter.
     */
    public CTemplateParser() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset.
     * @param alternativeSourceRef The alternative source reference for
     * the port.  If alternativeSourceRef is null, then the adapter for the port
     * of the source channel is used.z
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    @Override
    public String generateTypeConvertStatement(
            ProgramCodeGeneratorAdapter.Channel source,
            ProgramCodeGeneratorAdapter.Channel sink, int offset,
            String alternativeSourceRef) throws IllegalActionException {

        CCodeGenerator codeGenerator = _getCodeGenerator();

        Type sourceType = ((TypedIOPort) source.port).getType();
        Type sinkType = ((TypedIOPort) sink.port).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }

        // The references are associated with their own adapter, so we need
        // to find the associated adapter.
        String sourcePortChannel = source.port.getName() + "#"
                + source.channelNumber + ", " + offset;
        String sourceRef;

        if (alternativeSourceRef == null) {
            sourceRef = ((NamedProgramCodeGeneratorAdapter) codeGenerator
                    .getAdapter(source.port.getContainer())).getReference(
                            sourcePortChannel, false);
        } else {
            sourceRef = alternativeSourceRef;
        }

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
                + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((NamedProgramCodeGeneratorAdapter) codeGenerator
                .getAdapter(sink.port.getContainer())).getReference(
                        sinkPortChannel, true);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = CodeGeneratorAdapter.generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        String sourceCodeGenType = codeGenerator.codeGenType(sourceType);
        String sinkCodeGenType = codeGenerator.codeGenType(sinkType);

        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
            result = "$convert_" + sourceCodeGenType + "_" + sinkCodeGenType
                    + "(" + result + ")";
        }
        return sinkRef + " = " + result + ";"
        + StringUtilities.getProperty("line.separator");
    }

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
        return super.getFunctionInvocation(functionString, isStatic).replace(
                ".type", "->type");
    }

    /** Return a new parse tree code generator to use with expressions.
     *  @return the parse tree code generator to use with expressions.
     */
    @Override
    public ParseTreeCodeGenerator getParseTreeCodeGenerator() {
        // FIXME: We need to create new ParseTreeCodeGenerator each time
        // here or else we get lots of test failures.  It would be better
        // if we could use the same CParseTreeCodeGenerator over and over.
        _parseTreeCodeGenerator = new CParseTreeCodeGenerator(
                _getCodeGenerator());
        return _parseTreeCodeGenerator;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     */
    @Override
    protected CCodeGenerator _getCodeGenerator() {
        return (CCodeGenerator) super._getCodeGenerator();
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
        String result = super._replaceMacro(macro, parameter);

        if (result != null) {
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
            if (_getCodeGenerator().isPrimitive(port.getType())) {
                return ".payload."
                        + _getCodeGenerator().codeGenType(port.getType());
            } else {
                return "";
            }
        } else if (macro.equals("lcCgType")) {
            String cgType = _replaceMacro("cgType", parameter);
            if (cgType.equals("Integer")) {
                return "int";
            }
            return cgType.toLowerCase(Locale.getDefault());
        } else if (macro.equals("ModelName")) {
            return ((CCodeGenerator) super._codeGenerator).getModelName();
        } else if (macro.equals("DirectorName")) {
            return CodeGeneratorAdapter.generateName(((NamedObj) _component))
                    + ".container->director";
        }

        // We will assume that it is a call to a polymorphic
        // functions.
        //String[] call = macro.split("_");
        _getCodeGenerator().markFunctionCalled(macro, this);
        result = macro + "(" + parameter + ")";

        return result;
    }

}
