/* Code generator adapter for SDFReceiver.

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

package ptolemy.cg.adapter.generic.program.procedural.c.adapters.ptolemy.domains.sdf.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.cg.adapter.generic.program.procedural.adapters.ptolemy.actor.Receiver;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.fsm.modal.ModalController;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////////
////SDFReceiver

/** The adapter for SDF recevier.
*  @author Jia Zou, Man-Kit Leung, Isaac Liu, Bert Rodiers
*  @version $Id$
*  @since Ptolemy II 7.1
*  @Pt.ProposedRating Red (jiazou)
*  @Pt.AcceptedRating Red (jiazou)
*/
public class SDFReceiver extends Receiver {

    /** Construct a SDF receiver.
     */
    public SDFReceiver(ptolemy.domains.sdf.kernel.SDFReceiver receiver)
            throws IllegalActionException {
        super(receiver);
    }

    /** Generates code for getting tokens from the receiver.
     *  @return generate get code.
     *  @throws IllegalActionException
     */
    public String generateGetCode() throws IllegalActionException {
        IOPort port = getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());

        ProgramCodeGeneratorAdapter helper = getAdapter(port.getContainer());

        return helper.processCode("$ref(" + port.getName() + "#" + channel
                + ")");
    }

    /** Generates code to check the receiver has token.
     *  @return generate hasToken code.
     *  @throws IllegalActionException
     */
    public String generateHasTokenCode() throws IllegalActionException {
        return "true"; // Assume "true" is a defined constant.
    }

    /** Generates code for putting tokens from the receiver.
     *  @return generate put code.
     *  @throws IllegalActionException
     */
    public String generatePutCode(String token) throws IllegalActionException {
        IOPort port = getComponent().getContainer();
        int channel = port.getChannelForReceiver(getComponent());

        ProgramCodeGeneratorAdapter helper = getAdapter(port.getContainer());

        return helper.processCode("$ref(" + port.getName() + "#" + channel
                + ")")
                + " = " + token + ";" + _eol;
    }

    
    protected String _generateTypeConvertStatement(Channel source)
            throws IllegalActionException {
        
        Type sourceType = ((TypedIOPort) source.port).getType();
        Type sinkType = ((TypedIOPort) getComponent().getContainer()).getType();

        // In a modal model, a refinement may have an output port which is
        // not connected inside, in this case the type of the port is
        // unknown and there is no need to generate type conversion code
        // because there is no token transferred from the port.
        if (sourceType == BaseType.UNKNOWN) {
            return "";
        }
        // FIXME: what do we do with offset?
//
//        // The references are associated with their own adapter, so we need
//        // to find the associated adapter.
//        String sourcePortChannel = source.port.getName() + "#"
//                + source.channelNumber + ", " + offset;
//        String sourceRef = (_getAdapter(source.port.getContainer()))
//                .getReference(sourcePortChannel);
//
//        String sinkPortChannel = sink.port.getName() + "#" + sink.channelNumber
//                + ", " + offset;
//
//        // For composite actor, generate a variable corresponding to
//        // the inside receiver of an output port.
//        // FIXME: I think checking sink.port.isOutput() is enough here.
//        if (sink.port.getContainer() instanceof CompositeActor
//                && sink.port.isOutput()) {
//            sinkPortChannel = "@" + sinkPortChannel;
//        }
//        String sinkRef = (_getAdapter(sink.port.getContainer())).getReference(
//                sinkPortChannel, true);
//
//        // When the sink port is contained by a modal controller, it is
//        // possible that the port is both input and output port. we need
//        // to pay special attention. Directly calling getReference() will
//        // treat it as output port and this is not correct.
//        // FIXME: what about offset?
//        if (sink.port.getContainer() instanceof ModalController) {
//            sinkRef = ProgramCodeGeneratorAdapter.generateName(sink.port);
//            if (sink.port.isMultiport()) {
//                sinkRef = sinkRef + "[" + sink.channelNumber + "]";
//            }
//        }
//
//        String result = sourceRef;
//
//        String sourceCodeGenType = _codeGenerator.codeGenType(sourceType);
//        String sinkCodeGenType = _codeGenerator.codeGenType(sinkType);
//
//        if (!sinkCodeGenType.equals(sourceCodeGenType)) {
//            result = "$convert_" + sourceCodeGenType + "_" + sinkCodeGenType
//                    + "(" + result + ")";
//        }
//        return sinkRef + " = " + result + ";" + _eol;
//    }

        return null;
    }

    //$send(port#channel) ==> port_channel[writeOffset]
    //$get(port#channel) ==> port_channel[readOffset]
}
