/* Codex generator helper for typed composite actor.

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
package ptolemy.codegen.c.actor;

import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.codegen.c.domains.pn.kernel.MpiPNDirector;
import ptolemy.codegen.c.domains.pn.kernel.PNDirector;
import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.codegen.kernel.CodeGeneratorHelper;
import ptolemy.codegen.kernel.PortCodeGenerator;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;

public class IOPort extends CCodeGeneratorHelper implements PortCodeGenerator {

    /** Construct the code generator helper associated
     *  with the given TypedCompositeActor.
     *  @param component The associated component.
     */
    public IOPort(ptolemy.actor.IOPort component) {
        super(component);

    }

    private boolean isMpi() {
        return (null != getCodeGenerator().getAttribute("mpi"));
    }

    public String generateOffset(String offset, int channel, boolean isWrite, 
            Director director) throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        Receiver receiver = _getReceiver(offset, channel, port);

        if (!isMpi() && receiver instanceof PNQueueReceiver) {
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
        if (isMpi()) {
            code.append("MPI_recv();" + _eol);
        }
        return code.toString();
    }


    public String generatePostFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (isMpi()) {
            code.append("MPI_send();" + _eol);
        }
        return code.toString();
    }


    private Receiver _getReceiver(String offset, int channel, ptolemy.actor.IOPort port) {
        Receiver[][] receivers = port.getReceivers();

        Receiver receiver = null;
        try {
            int staticOffset = Integer.parseInt(offset);
            receiver = receivers[channel][staticOffset];

        } catch (Exception ex) {
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


    // Update the write offset of the [multiple] connected ports.
    public String updateConnectedPortsOffset(int rate, Director director) throws IllegalActionException {
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment("\n....................Begin updateConnectedPortsOffset..." + port.getFullName()));

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
            List sinkChannels = helper.getSinkChannels(port, j);

            for (int k = 0; k < sinkChannels.size(); k++) {
                Channel channel = (Channel) sinkChannels.get(k);
                ptolemy.actor.IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                Object offsetObject = helper.getWriteOffset(sinkPort,
                        sinkChannelNumber);

                Receiver receiver = _getReceiver(
                        offsetObject.toString(), sinkChannelNumber, sinkPort);

                if (isMpi() && MpiPNDirector.isMpiSendBuffer(port, j)) {
                    code.append(_generateMPISendCode(j, rate, sinkPort, sinkChannelNumber, director));

                } else if (!isMpi() && receiver instanceof PNQueueReceiver) {

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
        code.append(getCodeGenerator().comment("\n....................End updateConnectedPortsOffset..." + port.getFullName()));
        return code.toString();
    }

    private String _generateMPISendCode(int channelNumber, 
            int rate, ptolemy.actor.IOPort sinkPort,
            int sinkChannelNumber, Director director) throws IllegalActionException {
        ptolemy.actor.TypedIOPort port = (ptolemy.actor.TypedIOPort) getComponent();

        CodeGeneratorHelper sinkHelper = 
            (CodeGeneratorHelper) _getHelper(sinkPort.getContainer());

        StringBuffer code = new StringBuffer();

        code.append("// generateMPISendCode()" + _eol);

        for (int i = 0; i < rate; i++) {

            int sinkRank = MpiPNDirector.getRankNumber((Actor) sinkPort.getContainer());

            code.append("// Initialize send tag value." + _eol);
            code.append("static int " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " = " +
                    MpiPNDirector.getMpiReceiveBufferId(sinkPort, sinkChannelNumber) + ";" + _eol);

            if (MpiPNDirector._DEBUG) {
                int sourceRank = MpiPNDirector.getRankNumber((Actor) port.getContainer());
                code.append("printf(\"" + port.getContainer().getName() + "[" + sourceRank + "] sending msg <" + 
                        sinkRank + ", %d> for " + MpiPNDirector.getBufferLabel(port, channelNumber) +
                        		"\\n\", " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + ");" + _eol);
            }
            
            code.append("MPI_Isend(&");

            String[] channelAndOffset = new String[2];
            channelAndOffset[0] = "" + sinkChannelNumber;
            channelAndOffset[1] = MpiPNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" +
            MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + ".current]";

            String buffer = 
                sinkHelper.generatePortReference(sinkPort, channelAndOffset , false);

            //    sourcHelper.processCode("&$ref(" + port.getName() + "#" + channelNumber + ", " + i + ")");
            //sinkHelper.processCode("&$ref(" + sinkPort.getName() + "#" + sinkChannelNumber + ", " + i + ")");


            code.append(buffer);
            //code.append(buffer.substring(0, buffer.indexOf('=')));
//          generateVariableName(sinkPort) + sinkChannelNumber
//          + "[" + MpiPNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" + 
//          MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + ".current" + (i == 0 ? "" : i) + 
//          "]]" + ", 1, ");

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

            code.append(", " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) +
                    ", " + "comm, &" +
                    MpiPNDirector.generateMpiRequest(sinkPort, sinkChannelNumber) + "[" + 
                    MpiPNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" + 
                    MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                    ".current" + (i == 0 ? "" : i) + "]]" + ");" + _eol);
        }

        // Update the Offset.
        code.append(MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                ".current += " + rate + ";" + _eol);

        MpiPNDirector directorHelper = (MpiPNDirector) _getHelper(director);
        code.append(MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " += " + 
                directorHelper.getNumberOfMpiConnections(true) + ";" + _eol);

        code.append(MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " &= 32767; // 2^15 - 1 which is the max tag value." + _eol);

        return  code + _eol;

    }

    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        ptolemy.actor.TypedIOPort port = (ptolemy.actor.TypedIOPort) getComponent();

        CodeGeneratorHelper helper = 
            (CodeGeneratorHelper) _getHelper(getComponent().getContainer());

        return helper.processCode(code.toString());
    }



    // Updating the read offset.
    public String updateOffset(int rate, Director directorHelper) 
    throws IllegalActionException {

        ptolemy.actor.IOPort port = 
            (ptolemy.actor.IOPort) getComponent();
        Receiver receiver = _getReceiver(null, 0, port);

        String code = this.getCodeGenerator().comment("\n....................Begin updateOffset..." + port.getFullName());

        if (!isMpi() && receiver instanceof PNQueueReceiver) {
            int width = port.getWidth();
            for (int i = 0; i < width; i++) {

                // FIXME: this is kind of hacky.
                PNDirector pnDirector = (PNDirector)//directorHelper;         
                _getHelper(((Actor) port.getContainer()).getExecutiveDirector());

                List<Channel> channels = pnDirector.getReferencedChannels(port, i);

                for (Channel channel : channels) {
                    code += _updatePNOffset(rate, channel.port, 
                            channel.channelNumber, directorHelper, false);
                }
            }
            code += getCodeGenerator().comment("\n....................End updateOffset (PN)..." + port.getFullName());
            return code;
        } else {
            code += _updateOffset(rate);
            code += getCodeGenerator().comment("\n....................End updateOffset..." + port.getFullName());
            return code;
        }
    }

    private String _updatePNOffset(int rate, ptolemy.actor.IOPort port, int channelNumber, Director directorHelper, boolean isWrite)
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

        return incrementFunction + "(" + 
        incrementArg + "&" +
        pnDirector.generatePortHeader(port, channelNumber) + ", &" +
        pnDirector.generateDirectorHeader() + ");" + _eol;
    }

    protected String _updateOffset(int rate) throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = 
            (ptolemy.actor.IOPort) getComponent();
        CodeGeneratorHelper helper = (CodeGeneratorHelper) _getHelper(port
                .getContainer());

        int length = 0;
        if (port.isInput()) {
            length = port.getWidth();
        } else {
            length = port.getWidthInside();
        }

        for (int j = 0; j < length; j++) {
            // Update the offset for each channel.            
            if (helper.getReadOffset(port, j) instanceof Integer) {
                int offset = ((Integer) helper.getReadOffset(port, j))
                .intValue();
                if (helper.getBufferSize(port, j) != 0) {
                    offset = (offset + rate) % helper.getBufferSize(port, j);
                }
                helper.setReadOffset(port, j, Integer.valueOf(offset));
            } else { // If offset is a variable.
                String offsetVariable = (String) helper.getReadOffset(port, j);
                if (padBuffers) {
                    int modulo = helper.getBufferSize(port, j) - 1;
                    code.append(offsetVariable + " = (" + offsetVariable + 
                            " + " + rate + ")&" + modulo + ";" + _eol);
                } else {
                    code.append(offsetVariable + " = (" + offsetVariable + 
                            " + " + rate + ") % " + 
                            helper.getBufferSize(port, j) + ";" + _eol);
                }
            }
        }
        return code.toString();

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

        boolean dynamicReferencesAllowed = ((BooleanToken) _codeGenerator.allowDynamicMultiportReference
                .getToken()).booleanValue();
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        if (MpiPNDirector.isLocalBuffer(port, channel)) {
            int i = 1;
        }


        // When dynamic references are allowed, any input ports require
        // offsets.
        if (dynamicReferencesAllowed && port.isInput()) {
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

//              if (MpiPNDirector.isLocalBuffer(port, channel)) {
//              temp = offsetObject.toString();
//              temp = MpiPNDirector.generateFreeSlots(port, channel) +
//              "[" + MpiPNDirector.generatePortHeader(port, channel) + ".current]";
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

//              if (MpiPNDirector.isLocalBuffer(port, channel)) {
//              result = offsetObject.toString();
//              result = MpiPNDirector.generateFreeSlots(port, channel) +
//              "[" + MpiPNDirector.generatePortHeader(port, channel) + ".current]";                  
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

    public String initializeOffsets() throws IllegalActionException {
        if (isMpi()) {
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
}
