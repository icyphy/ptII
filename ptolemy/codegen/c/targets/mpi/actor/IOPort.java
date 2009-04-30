/* Code generator helper for IOPort.

 Copyright (c) 2005-2008 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.mpi.actor;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.actor.lib.jni.EmbeddedCActor;
import ptolemy.codegen.c.domains.pn.kernel.PNDirector;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////IOPort

/**
Code generator helper for IOPort.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.0
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */

public class IOPort extends ptolemy.codegen.c.actor.IOPort implements PortCodeGenerator {

    /** Construct the code generator helper associated
     *  with the given IOPort.
     *  @param component The associated component.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);
    }

    /////////////////////////////////////////////////////////////////////
    ////                           public methods                    ////

    public String generateCodeForSend(String channel, String dataToken) 
    throws IllegalActionException {
        ptolemy.codegen.actor.Director directorHelper = _getDirectorHelper();
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        int channelNumber = Integer.valueOf(channel);

        return directorHelper.generateCodeForSend(port, channelNumber, dataToken);
    }

    public String generateCodeForGet(String channel) throws IllegalActionException {
        ptolemy.codegen.actor.Director directorHelper = _getDirectorHelper();
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        int channelNumber = Integer.valueOf(channel);

        return directorHelper.generateCodeForGet(port, channelNumber);
    }

    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorHelper helper = (CodeGeneratorHelper) 
        _getHelper(getComponent().getContainer());

        return helper.processCode(code.toString());
    }


    public String generateOffset(String offset, int channel, boolean isWrite, 
            Director director) throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        Receiver receiver = _getReceiver(offset, channel, port);

        if (_isPthread() && receiver instanceof PNQueueReceiver) {
            String result;
            if (offset.length() == 0 || offset.equals("0")) {
                result = (isWrite) ? 
                        "$getWriteOffset(" : "$getReadOffset(";
            } else {
                result = (isWrite) ? 
                        "$getAdvancedWriteOffset(" : "$getAdvancedReadOffset(";
                result += offset + ", ";
            }

            PNDirector pnDirector = (PNDirector) _getHelper(director);
            result += "&" + PNDirector.generatePortHeader(port, channel) + ", ";
            result += "&" + pnDirector.generateDirectorHeader() + ")";
            return "[" + result + "]";
        } else {
            return _generateOffset(offset, channel, isWrite);
        }
    }

    public String generatePreFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_isPthread()) {
            code.append("MPI_recv();" + _eol);
        }
        return code.toString();
    }


    public String generatePostfireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_isPthread()) {
            code.append("MPI_send();" + _eol);
        }
        return code.toString();
    }


    public ptolemy.actor.Director getDirector() {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        Actor actor = (Actor) port.getContainer();

        if (actor instanceof EmbeddedCActor.EmbeddedActor) {
            // ignore the inner SDFDirector.
            actor = (Actor) actor.getContainer();
        }
        Director director = null;

        // FIXME: why are we checking if this is a input port?
        //if (port.isInput() && !port.isOutput() && (actor instanceof CompositeActor)) {
        if (actor instanceof CompositeActor) {
            director = actor.getExecutiveDirector();
        } 

        if (director == null) {
            director = actor.getDirector();
        }
        return director;
    }


    public String initializeOffsets() throws IllegalActionException {
        if (_isPthread()) {
            return "";
        }

        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        CodeGeneratorHelper actorHelper = 
            (CodeGeneratorHelper) _getHelper(port.getContainer());

        StringBuffer code = new StringBuffer();

        for (int i = 0; i < port.getWidth(); i++) {
            Object readOffset = actorHelper.getReadOffset(port, i);
            if (readOffset instanceof Integer) {
                actorHelper.setReadOffset(port, i, Integer.valueOf(0));
            } else {
                code.append(((String) readOffset)
                        + " = 0;" + _eol);
            }
            Object writeOffset = actorHelper.getWriteOffset(port, i);
            if (writeOffset instanceof Integer) {
                actorHelper.setWriteOffset(port, i, Integer.valueOf(0));
            } else {
                code.append(((String) writeOffset)
                        + " = 0;" + _eol);
            }
        }
        return code.toString();
    }


    // Update the write offset of the [multiple] connected ports.
    public String updateConnectedPortsOffset(int rate, Director director) throws IllegalActionException {
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment(_eol + "....Begin updateConnectedPortsOffset...."
					       + CodeGeneratorHelper.generateName(port)));

        if (rate == 0) {
            return "";
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }

        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int length = 0;
        if (port.isInput()) {
            length = port.getWidthInside();
        } else {
            length = port.getWidth();
        }

        for (int j = 0; j < length; j++) {
            List sinkChannels = CodeGeneratorHelper.getSinkChannels(port, j);

            for (int k = 0; k < sinkChannels.size(); k++) {
                Channel channel = (Channel) sinkChannels.get(k);
                ptolemy.actor.IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                Object offsetObject = helper.getWriteOffset(sinkPort,
                        sinkChannelNumber);

                Receiver receiver = _getReceiver(
                        offsetObject.toString(), sinkChannelNumber, sinkPort);

                if (_isPthread() && ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.isMpiReceiveBuffer(sinkPort, sinkChannelNumber)) {
                    code.append(_generateMPISendCode(j, rate, sinkPort, sinkChannelNumber, director));

                } else if (_isPthread() && receiver instanceof PNQueueReceiver) {

                    // PNReceiver.                    
                    code.append(_updatePNOffset(rate, sinkPort, sinkChannelNumber, director, true));
                } else {

                    if (offsetObject instanceof Integer) {
                        int offset = ((Integer) offsetObject).intValue();
                        int bufferSize = helper.getBufferSize(sinkPort,
                                sinkChannelNumber);
                        if (bufferSize != 0) {
                            offset = (offset + rate) % bufferSize;
                        }
                        helper.setWriteOffset(sinkPort, sinkChannelNumber, Integer
                                .valueOf(offset));
                    } else { // If offset is a variable.
                        String offsetVariable = (String) helper.getWriteOffset(
                                sinkPort, sinkChannelNumber);

                        if (padBuffers) {
                            int modulo = helper.getBufferSize(sinkPort,
                                    sinkChannelNumber) - 1;
                            code.append(offsetVariable + " = ("
                                    + offsetVariable + " + " + rate + ")&" + modulo
                                    + ";" + _eol);
                        } else {
                            code.append(offsetVariable + " = ("
                                    + offsetVariable + " + " + rate
                                    + ") % "
                                    + helper.getBufferSize(sinkPort,
                                            sinkChannelNumber) + ";" + _eol);
                        }
                    }
                }
            }
        }
        code.append(getCodeGenerator().comment(_eol + "....End updateConnectedPortsOffset...."
					       + CodeGeneratorHelper.generateName(port)));
        return code.toString();
    }


    // Updating the read offset.
    public String updateOffset(int rate, Director directorHelper) 
    throws IllegalActionException {

        ptolemy.actor.IOPort port = 
            (ptolemy.actor.IOPort) getComponent();
        Receiver receiver = _getReceiver(null, 0, port);

        String code = getCodeGenerator().comment(_eol + "....Begin updateOffset...." 
						 + CodeGeneratorHelper.generateName(port));

        //        int width = 0;
        //        if (port.isInput()) {
        //            width = port.getWidth();
        //        } else {
        //            width = port.getWidthInside();
        //        }

        for (int i = 0; i < port.getWidth(); i++) {
            if (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.isMpiReceiveBuffer(port, i)) {
                // do nothing.
            } else if (_isPthread() && receiver instanceof PNQueueReceiver) {

                // FIXME: this is kind of hacky.
                //PNDirector pnDirector = (PNDirector)//directorHelper;         
                _getHelper(((Actor) port.getContainer()).getExecutiveDirector());

                List<Channel> channels = PNDirector.getReferenceChannels(port, i);

                for (Channel channel : channels) {
                    code += _updatePNOffset(rate, channel.port, 
                            channel.channelNumber, directorHelper, false);
                }
                code += getCodeGenerator().comment(_eol + "....End updateOffset (PN)...."
						   + CodeGeneratorHelper.generateName(port));

            } else {
                code += _updateOffset(i, rate);
                code += getCodeGenerator().comment(_eol + "\n....End updateOffset...."
						   + CodeGeneratorHelper.generateName(port));
            }
        }
        return code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private String _generateMPISendCode(int channelNumber, 
            int rate, ptolemy.actor.IOPort sinkPort,
            int sinkChannelNumber, Director director) throws IllegalActionException {
        ptolemy.actor.TypedIOPort port = (ptolemy.actor.TypedIOPort) getComponent();

        StringBuffer code = new StringBuffer();

        code.append("// generateMPISendCode()" + _eol);

        for (int i = 0; i < rate; i++) {

            int sinkRank = ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getRankNumber((Actor) sinkPort.getContainer());
            int sourceRank = ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getRankNumber((Actor) port.getContainer());

            code.append("// Initialize send tag value." + _eol);
            code.append("static int " + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getSendTag(sinkPort, sinkChannelNumber) + " = " +
                    ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getMpiReceiveBufferId(sinkPort, sinkChannelNumber) + ";" + _eol);

            if (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector._DEBUG) {
                code.append("printf(\"" + generateSimpleName(port.getContainer()) + "[" + sourceRank + "] sending msg <" + 
                        sinkRank + ", %d> for " + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getBufferLabel(port, channelNumber) +
                        "\\n\", " + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getSendTag(sinkPort, sinkChannelNumber) + ");" + _eol);
            }

            code.append("MPI_Isend(&");

            String[] channelAndOffset = new String[2];
            channelAndOffset[0] = "" + sinkChannelNumber;
            channelAndOffset[1] = ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" +
            ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + ".current]";

            String buffer = 
                CodeGeneratorHelper.generatePortReference(sinkPort, channelAndOffset , false);

            code.append(buffer);

            // count.
            code.append(", 1");

            // FIXME: handle different mpi data types.
            if (port.getType() == BaseType.DOUBLE) {
                code.append(", MPI_DOUBLE");
            } else if (port.getType() == BaseType.INT) {
                code.append(", MPI_INT");
            } else {
                assert false;
            }

            code.append(", " + sinkRank);

            code.append(", " + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getSendTag(sinkPort, sinkChannelNumber) +
                    ", " + "comm, &" +
                    ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generateMpiRequest(sinkPort, sinkChannelNumber) + "[" + 
                    ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" + 
                    ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                    ".current" + (i == 0 ? "" : i) + "]]" + ");" + _eol);

            if (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector._DEBUG) {
                code.append("printf(\"" + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getBufferLabel(port, channelNumber) +
                        ", rank[" + sourceRank + "], sended tag[%d]\\n\", " + 
                        ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getSendTag(sinkPort, sinkChannelNumber) + ");" + _eol);
            }            
        }

        // Update the Offset.
        code.append(ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                ".current += " + rate + ";" + _eol);

        ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector directorHelper = (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector) _getHelper(director);
        code.append(ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getSendTag(sinkPort, sinkChannelNumber) + " += " + 
                directorHelper.getNumberOfMpiConnections(true) + ";" + _eol);

        code.append(ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.getSendTag(sinkPort, sinkChannelNumber) + " &= 32767; // 2^15 - 1 which is the max tag value." + _eol);

        return  code + _eol;

    }

    /**
     * Generate the expression that represents the offset in the generated
     * code.
     * @param offsetString The specified offset from the user.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     *  buffer size or the offset in the channel and offset map.
     */
    protected String _generateOffset(String offsetString, int channel, boolean isWrite) 
    throws IllegalActionException {

        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        //if (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.isLocalBuffer(port, channel)) {
        //    int i = 1;
        //}


        // When dynamic references are allowed, any input ports require
        // offsets.
        if (port.isInput()) {
            if (!(port.isMultiport() || getBufferSize(port) > 1)) {
                return "";
            }
        } else {
            if (!(getBufferSize(port) > 1)) {
                return "";
            }
        }

        String result = null;
        Object offsetObject;

        // Get the offset index.
        if (isWrite) {
            offsetObject = getWriteOffset(port, channel);
        } else {
            offsetObject = getReadOffset(port, channel);
        }

        if (!offsetString.equals("")) {
            // Specified offset.

            String temp = "";
            if (offsetObject instanceof Integer && _isInteger(offsetString)) {

                int offset = ((Integer) offsetObject).intValue()
                + (Integer.valueOf(offsetString)).intValue();

                offset %= getBufferSize(port, channel);
                temp = Integer.toString(offset);
                /*
                 int divisor = getBufferSize(sinkPort,
                 sinkChannelNumber);
                 temp = "("
                 + getWriteOffset(sinkPort,
                 sinkChannelNumber) + " + "
                 + channelAndOffset[1] + ")%" + divisor;
                 */

            } else {
                // FIXME: We haven't check if modulo is 0. But this
                // should never happen. For offsets that need to be
                // represented by string expression,
                // getBufferSize(port, channelNumber) will always
                // return a value at least 2.

                //              if (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.isLocalBuffer(port, channel)) {
                //              temp = offsetObject.toString();
                //              temp = ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generateFreeSlots(port, channel) +
                //              "[" + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generatePortHeader(port, channel) + ".current]";
                //              } else 
                if (padBuffers) {
                    int modulo = getBufferSize(port, channel) - 1;
                    temp = "(" + offsetObject.toString() + " + " + offsetString
                    + ")&" + modulo;
                } else {
                    int modulo = getBufferSize(port, channel);
                    temp = "(" + offsetObject.toString() + " + " + offsetString
                    + ")%" + modulo;
                }
            }

            result = "[" + temp + "]";

        } else {
            // Did not specify offset, so the receiver buffer
            // size is 1. This is multiple firing.

            if (offsetObject instanceof Integer) {
                int offset = ((Integer) offsetObject).intValue();

                offset %= getBufferSize(port, channel);

                result = "[" + offset + "]";
            } else {

                //              if (ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.isLocalBuffer(port, channel)) {
                //              result = offsetObject.toString();
                //              result = ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generateFreeSlots(port, channel) +
                //              "[" + ptolemy.codegen.c.targets.mpi.domains.pn.kernel.PNDirector.generatePortHeader(port, channel) + ".current]";                  
                //              } else 
                if (padBuffers) {
                    int modulo = getBufferSize(port, channel) - 1;
                    result = "[" + offsetObject + "&" + modulo + "]";
                } else {
                    result = "[" + offsetObject + "%"
                    + getBufferSize(port, channel) + "]";
                }
            }
        }
        return result;
    }


    private ptolemy.codegen.actor.Director _getDirectorHelper() throws IllegalActionException {
        Director director = getDirector();
        return (ptolemy.codegen.actor.Director) _getHelper(director);
    }


    private Receiver _getReceiver(String offset, int channel, ptolemy.actor.IOPort port) {
        Receiver[][] receivers = port.getReceivers();

	int staticOffset = -1;
        Receiver receiver = null;
	if (offset != null) {
	    try {
		staticOffset = Integer.parseInt(offset);
		receiver = receivers[channel][staticOffset];
	    } catch (Exception ex) {
		staticOffset = -1;
	    }
	}

	if (staticOffset == -1) {
            // FIXME: Assume all receivers are the same type for the channel.
            // However, this may not be true.
            if (receivers.length > 0) {
                receiver = receivers[channel][0];
            } else {
                assert false;
            }
        }
        return receiver;
    }


    /**
     * Return true if the given string can be parse as an integer; otherwise,
     * return false.
     * @param numberString The given number string.
     * @return True if the given string can be parse as an integer; otherwise,
     *  return false.
     */
    private boolean _isInteger(String numberString) {
        try {
            Integer.parseInt(numberString);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }


    private boolean _isPthread() {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        boolean isPN = (((Actor) port.getContainer()).getDirector() 
                instanceof ptolemy.domains.pn.kernel.PNDirector);

        return isPN && 
        (getCodeGenerator().target.getExpression().equals("default") || 
         getCodeGenerator().target.getExpression().equals("posix"));
    }


    protected String _updateOffset(int channel, int rate) throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = 
            (ptolemy.actor.IOPort) getComponent();
        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        // Update the offset for each channel.            
        if (helper.getReadOffset(port, channel) instanceof Integer) {
            int offset = ((Integer) helper.getReadOffset(port, channel))
            .intValue();
            if (helper.getBufferSize(port, channel) != 0) {
                offset = (offset + rate) % helper.getBufferSize(port, channel);
            }
            helper.setReadOffset(port, channel, Integer.valueOf(offset));
        } else { // If offset is a variable.
            String offsetVariable = (String) helper.getReadOffset(port, channel);
            if (padBuffers) {
                int modulo = helper.getBufferSize(port, channel) - 1;
                code.append(offsetVariable + " = (" + offsetVariable + 
                        " + " + rate + ")&" + modulo + ";" + _eol);
            } else {
                code.append(offsetVariable + " = (" + offsetVariable + 
                        " + " + rate + ") % " + 
                        helper.getBufferSize(port, channel) + ";" + _eol);
            }
        }
        return code.toString();

    }


    private String _updatePNOffset(int rate, ptolemy.actor.IOPort port, 
            int channelNumber, Director directorHelper, boolean isWrite)
    throws IllegalActionException {
        // FIXME: this is kind of hacky.
        PNDirector pnDirector = (PNDirector) //directorHelper; 
        _getHelper(((Actor) port.getContainer()).getExecutiveDirector());

        String incrementFunction = (isWrite) ? 
                "$incrementWriteOffset" : "$incrementReadOffset";

        if (rate <= 0) {
            assert false;
        }

        String incrementArg = "";
        if (rate > 1) {
            incrementFunction += "By";

            // Supply the increment argument.
            incrementArg += rate + ", ";
        }

        // FIXME: generate the right buffer reference from
        // both input and output ports.

        return incrementFunction + "(" + incrementArg + "&" +
        PNDirector.generatePortHeader(port, channelNumber) + ", &" +
        pnDirector.generateDirectorHeader() + ");" + _eol;
    }

}
