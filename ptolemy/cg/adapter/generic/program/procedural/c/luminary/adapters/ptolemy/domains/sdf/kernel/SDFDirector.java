/*  Adapter for the SDFDirector, targeted to the Luminary platform.

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
package ptolemy.cg.adapter.generic.program.procedural.c.luminary.adapters.ptolemy.domains.sdf.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.TypedCompositeActor;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.CodeStream;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;

/**
   Adapter for the SDFDirector, targeted to the Luminary platform.

   @author Jia Zou
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
 */
public class SDFDirector
        extends
ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel.SDFDirector {

    /** Construct the code generator adapter associated with the given
     *  SDFDirector.
     *  @param sdfDirector The associated
     *  ptolemy.domains.sdf.kernel.SDFDirector
     */
    public SDFDirector(ptolemy.domains.sdf.kernel.SDFDirector sdfDirector) {
        super(sdfDirector);
    }

    /** Generate code for transferring enough tokens to complete an internal
     *  iteration.
     *  @param inputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferInputsCode(IOPort inputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the inside.")));
        int rate = DFUtilities.getTokenConsumptionRate(inputPort);

        CompositeActor container = (CompositeActor) getComponent()
                .getContainer();
        TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
                .getAdapter(container);

        // FindBugs wants this instanceof check.
        if (!(inputPort instanceof TypedIOPort)) {
            throw new InternalErrorException(inputPort, null,
                    " is not an instance of TypedIOPort.");
        }

        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort) getAdapter(inputPort);

        // FIXME: not sure what to do with this offset here.
        String offset = "";

        for (int i = 0; i < inputPort.getWidth(); i++) {
            // the following code is of the form:
            // if ($hasToken(i)) {
            //     input = Event_Head->data;
            // }
            code.append("if (");
            code.append(portAdapter.generateHasTokenCode(Integer.toString(i),
                    offset));
            code.append(") {" + _eol);

            // the input port to transfer the data to was declared earlier, and is of this name:
            StringBuffer inputCode = new StringBuffer();
            boolean dynamicReferencesAllowed = allowDynamicMultiportReference();
            inputCode.append(CodeGeneratorAdapter.generateName(inputPort));
            int bufferSize = ports.getBufferSize(inputPort);
            if (inputPort.isMultiport()) {
                inputCode.append("[" + Integer.toString(i) + "]");
                if (bufferSize > 1 || dynamicReferencesAllowed) {
                    throw new InternalErrorException(
                            "Generation of input transfer code"
                                    + "requires the knowledge of offset in the buffer, this"
                                    + "is not yet supported.");
                    //                        inputCode.append("[" + bufferSize + "]");
                }
            } else {
                if (bufferSize > 1) {
                    throw new InternalErrorException(
                            "Generation of input transfer code"
                                    + "requires the knowledge of offset in the buffer, this"
                                    + "is not yet supported.");
                    //                        inputCode.append("[" + bufferSize + "]");
                }
            }

            code.append(inputCode);
            code.append(" = ");
            code.append(portAdapter.generateGetCode(Integer.toString(i), offset));
            code.append(";" + _eol);
            code.append("}" + _eol);
        }

        // Generate the type conversion code before fire code.
        code.append(compositeActorAdapter.generateTypeConvertFireCode(true));

        // The offset of the input port itself is updated by outside director.
        _updateConnectedPortsOffset(inputPort, code, rate);
    }

    /** Generate code for transferring enough tokens to fulfill the output
     *  production rate.
     *  @param outputPort The port to transfer tokens.
     *  @param code The string buffer that the generated code is appended to.
     *  @exception IllegalActionException If thrown while transferring tokens.
     */
    @Override
    public void generateTransferOutputsCode(IOPort outputPort, StringBuffer code)
            throws IllegalActionException {
        code.append(CodeStream.indent(getCodeGenerator().comment(
                "SDFDirector: " + "Transfer tokens to the outside.")));

        int rate = DFUtilities.getTokenProductionRate(outputPort);

        //CompositeActor container = (CompositeActor) getComponent()
        //    .getContainer();
        //TypedCompositeActor compositeActorAdapter = (TypedCompositeActor) getCodeGenerator()
        //    .getAdapter(container);

        ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort portAdapter = (ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.IOPort) getAdapter(outputPort);

        // FIXME: not sure what to do with this offset here.
        String offset = "";

        for (int i = 0; i < outputPort.getWidth(); i++) {

            StringBuffer outputCode = new StringBuffer();
            outputCode.append(CodeGeneratorAdapter.generateName(outputPort));

            if (outputPort.isMultiport()) {
                outputCode.append("[" + Integer.toString(i) + "]");
            }

            int bufferSize = ports.getBufferSize(outputPort);

            if (bufferSize > 1) {
                throw new InternalErrorException(
                        "Generation of input transfer code"
                                + "requires the knowledge of offset in the buffer, this"
                                + "is not yet supported.");
                //                    outputCode.append("[" + bufferSize + "]");
            }

            code.append(portAdapter.generatePutCode(Integer.toString(i),
                    offset, outputCode.toString()));
        }

        // The offset of the ports connected to the output port is
        // updated by outside director.
        _updatePortOffset(outputPort, code, rate);
    }

}
