/* Base class for code generator adapter.

 Copyright (c) 2005-2009 The Regents of the University of California.
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
package ptolemy.cg.kernel.generic.program;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////////
//// ProgramCodeGeneratorAdapterStrategy

/**
 * The strategy that determines how code should be generated for a certain ProgramCodeGeneratorAdapter.
 * @author Bert Rodiers
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (rodiers)
 * @Pt.AcceptedRating Red (rodiers)
 */
public class ProgramCodeGeneratorAdapterStrategy {

    /** Construct the code generator adapter strategy.
     */
    public ProgramCodeGeneratorAdapterStrategy() {
    }

    /** Set the component for which we are generating code.
     *  @param object The associated component.
     *  @see #getComponent
     */
    public void setComponent(NamedObj object) {
        // FIXME: Why is this a namedObj when the analyzeActor()
        // method requires an Actor?
        _object = object;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the code generator associated with this adapter class.
     *  @return The code generator associated with this adapter class.
     *  @see #setCodeGenerator(ProgramCodeGenerator)
     */
    public ProgramCodeGenerator getCodeGenerator() {
        return _codeGenerator;
    }

    /** Get the component associated with this adapter.
     *  @return The associated component.
     *  @see #setComponent
     */
    public NamedObj getComponent() {
        return (NamedObj) _object;
    }

    /** Get the template parser associated with this strategy.
     *  @return The associated template parser.
     */
    final public TemplateParser getTemplateParser() {
        return _templateParser;
    }

    /** Set the adapter.
     *  @param adapter The given adapter.
     */
    final public void setAdapter(ProgramCodeGeneratorAdapter adapter) {
        _adapter = adapter;
        _createParser();        
    }

    /** Set the associated code generator.
     *  @param codeGenerator The code generator associated with this class.
     *  @see #getCodeGenerator()
     */
    final public void setCodeGenerator(ProgramCodeGenerator codeGenerator) {
        _codeGenerator = codeGenerator;
        if (_templateParser != null) {
            _templateParser.setCodeGenerator(codeGenerator);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected methods.                    ////

    /** Create the template parser.
     */
    protected void _createParser() { 
        _templateParser = new TemplateParser(_object, _adapter);
    }
    
    /**
     * Generate the type conversion statement for the particular offset of
     * the two given channels. This assumes that the offset is the same for
     * both channel. Advancing the offset of one has to advance the offset of
     * the other.
     * @param source The given source channel.
     * @param sink The given sink channel.
     * @param offset The given offset.
     * @return The type convert statement for assigning the converted source
     *  variable to the sink variable with the given offset.
     * @exception IllegalActionException If there is a problem getting the
     * adapters for the ports or if the conversion cannot be handled.
     */
    protected String _generateTypeConvertStatement(ProgramCodeGeneratorAdapter.Channel source,
            ProgramCodeGeneratorAdapter.Channel sink, int offset) throws IllegalActionException {

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
        String sourceRef = ((ProgramCodeGeneratorAdapter) _getAdapter(source.port
                .getContainer())).getReference(sourcePortChannel);

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
                + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = ((ProgramCodeGeneratorAdapter) _getAdapter(sink.port
                .getContainer())).getReference(sinkPortChannel, true);

        // When the sink port is contained by a modal controller, it is
        // possible that the port is both input and output port. we need
        // to pay special attention. Directly calling getReference() will
        // treat it as output port and this is not correct.
        // FIXME: what about offset?
        if (sink.port.getContainer() instanceof ModalController) {
            sinkRef = ProgramCodeGeneratorAdapter.generateName(sink.port);
            if (sink.port.isMultiport()) {
                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
            }
        }

        String result = sourceRef;

        if (!sinkType.equals(sourceType)) {
            if (_codeGenerator.isPrimitive(sinkType)) {
                result = _codeGenerator.codeGenType(sourceType) + "to" + _codeGenerator.codeGenType(sinkType)
                        + "(" + result + ")";

            } else if (_codeGenerator.isPrimitive(sourceType)) {
                result = "$new(" + _codeGenerator.codeGenType(sourceType) + "(" + result
                        + "))";
            }

            if (sinkType != BaseType.SCALAR && sinkType != BaseType.GENERAL
                    && !_codeGenerator.isPrimitive(sinkType)) {
                if (sinkType instanceof ArrayType) {
                    if (_codeGenerator.isPrimitive(sourceType)) {
                        result = "$new(" + _codeGenerator.codeGenType(sinkType) + "(1, 1, "
                                + result + ", TYPE_" + _codeGenerator.codeGenType(sourceType)
                                + "))";
                    }

                    // Deep converting for ArrayType.
                    Type elementType = ((ArrayType) sinkType).getElementType();
                    while (elementType instanceof ArrayType) {
                        elementType = ((ArrayType) elementType)
                                .getElementType();
                    }

                    if (elementType != BaseType.SCALAR
                            && elementType != BaseType.GENERAL) {
                        result = "$typeFunc(TYPE_"
                                + _codeGenerator.codeGenType(sinkType)
                                + "::convert("
                                + result
                                + ", /*CGH*/ TYPE_"
                                + _codeGenerator.codeGenType(((ArrayType) sinkType)
                                        .getElementType()) + "))";
                    }

                } else {
                    result = "$typeFunc(TYPE_" + _codeGenerator.codeGenType(sinkType)
                            + "::convert(" + result + "))";
                }
            }
        }
        return sinkRef + " = " + result + ";" + _eol;
    }

    /** Get the code generator adapter associated with the given component.
     *  @param component The given component.
     *  @return The code generator adapter.
     *  @exception IllegalActionException If the adapter class cannot be found.
     */
    protected ProgramCodeGeneratorAdapter _getAdapter(NamedObj component)
            throws IllegalActionException {
        return (ProgramCodeGeneratorAdapter) _codeGenerator
                .getAdapter(component);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The ProgramCodeGeneratorAdapter.*/
    protected ProgramCodeGeneratorAdapter _adapter;

    /** The code generator that contains this adapter class.
     */
    protected ProgramCodeGenerator _codeGenerator;

    protected TemplateParser _templateParser;

    /** End of line character.  Under Unix: "\n", under Windows: "\n\r".
     *  We use a end of line character so that the files we generate
     *  have the proper end of line character for use by other native tools.
     */
    protected final static String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The associated object. */
    private Object _object;
}
