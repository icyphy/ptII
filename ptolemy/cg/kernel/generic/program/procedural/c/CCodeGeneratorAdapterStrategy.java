/* The CCodeGeneratorAdapterStrategy.

 Copyright (c) 2009 The Regents of the University of California.
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

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.procedural.ProceduralCodeGeneratorAdapterStrategy;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.ExecuteCommands;
import ptolemy.util.FileUtilities;
import ptolemy.util.StreamExec;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////CCodeGeneratorAdapterStrategy
/**

The strategy that determines how code should be generated for a certain CCodeGeneratorAdapter.

@author Bert Rodiers
@version $Id$
@since Ptolemy II 8.0
@Pt.ProposedRating Red (rodiers)
@Pt.AcceptedRating Red (rodiers)
*/
public class CCodeGeneratorAdapterStrategy extends ProceduralCodeGeneratorAdapterStrategy {
    /**
     * Create a new instance of the C code generator adapter.
     */
    public CCodeGeneratorAdapterStrategy() {        
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////



    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the template parser.
     */
    protected void _createParser() { 
        _templateParser = new CTemplateParser(getComponent(), _adapter);
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
        String sourceRef = (_getAdapter(source.port.getContainer()))
                .getReference(sourcePortChannel);

        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
                + ", " + offset;

        // For composite actor, generate a variable corresponding to
        // the inside receiver of an output port.
        // FIXME: I think checking sink.port.isOutput() is enough here.
        if (sink.port.getContainer() instanceof CompositeActor
                && sink.port.isOutput()) {
            sinkPortChannel = "@" + sinkPortChannel;
        }
        String sinkRef = (_getAdapter(sink.port.getContainer())).getReference(
                sinkPortChannel, true);

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

        String sourceCodeGenType = _codeGenerator.codeGenType(sourceType);
        String sinkCodeGenType = _codeGenerator.codeGenType(sinkType);

        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
            result = "$convert_" + sourceCodeGenType + "_" + sinkCodeGenType
                    + "(" + result + ")";
        }
        return sinkRef + " = " + result + ";" + _eol;
    }

    /** Return the prototype for fire functions.
     * @return The string"(void)" so as to avoid the avr-gcc 3.4.6
     * warning: "function declaration isn't a prototype"
     */
    protected String _getFireFunctionArguments() {
        return "(void)";
    }
}
