/* Code generator adapter for IOPort.

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
package ptolemy.cg.adapter.generic.procedural.java.adapters.ptolemy.actor;

import java.util.HashMap;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.cg.kernel.generic.CodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.PortCodeGenerator;
import ptolemy.cg.kernel.generic.procedural.java.JavaCodeGeneratorAdapter;
import ptolemy.cg.lib.EmbeddedCodeActor;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.pn.kernel.PNQueueReceiver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
////IOPort

/**
Code generator adapter for {@link ptolemy.actor.IOPort}.

@author Man-Kit Leung
@version $Id$
@since Ptolemy II 7.2
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
 */

public class IOPort extends JavaCodeGeneratorAdapter implements PortCodeGenerator {

    /** Construct the code generator adapter associated
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
        ptolemy.codegen.actor.Director directorAdapter = _getDirectorAdapter();
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        int channelNumber = Integer.valueOf(channel);

        return directorAdapter.generateCodeForSend(port, channelNumber, dataToken);
    }

    public String generateCodeForGet(String channel) throws IllegalActionException {
        ptolemy.codegen.actor.Director directorAdapter = _getDirectorAdapter();
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        int channelNumber = Integer.valueOf(channel);

        return directorAdapter.generateCodeForGet(port, channelNumber);
    }

    public String generateInitializeCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();

        CodeGeneratorAdapter adapter = (CodeGeneratorAdapter) 
        _getAdapter(getComponent().getContainer());

        return adapter.processCode(code.toString());
    }


    public String generateOffset(String offset, int channel, boolean isWrite, 
            Director director) throws IllegalActionException {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();

        Receiver receiver = _getReceiver(offset, channel, port);

     // FIXME rodiers: reintroduce PN! (but somewhere else)
        if (false) {
        /*
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

	    // FIXME: why does this depend on PN?            
            PNDirector pnDirector = (PNDirector) _getAdapter(director);
            result += "&" + PNDirector.generatePortHeader(port, channel) + ", ";
            result += "&" + pnDirector.generateDirectorHeader() + ")";
            return "[" + result + "]";
            */
            return "";
        // End FIXME rodiers
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


    public String generatePostFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        if (_isPthread()) {
            code.append("MPI_send();" + _eol);
        }
        return code.toString();
    }


    public ptolemy.actor.Director getDirector() {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        Actor actor = (Actor) port.getContainer();

        if (actor instanceof EmbeddedCodeActor.EmbeddedActor) {
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
        CodeGeneratorAdapter actorAdapter = 
            (CodeGeneratorAdapter) _getAdapter(port.getContainer());

        StringBuffer code = new StringBuffer();

        for (int i = 0; i < port.getWidth(); i++) {
            Object readOffset = actorAdapter.getReadOffset(port, i);
            if (readOffset instanceof Integer) {
                actorAdapter.setReadOffset(port, i, Integer.valueOf(0));
            } else {
                code.append(((String) readOffset)
                        + " = 0;" + _eol);
            }
            Object writeOffset = actorAdapter.getWriteOffset(port, i);
            if (writeOffset instanceof Integer) {
                actorAdapter.setWriteOffset(port, i, Integer.valueOf(0));
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
					       + CodeGeneratorAdapter.generateName(port)));

        if (rate == 0) {
            return "";
        } else if (rate < 0) {
            throw new IllegalActionException(port, "the rate: " + rate
                    + " is negative.");
        }

        CodeGeneratorAdapter adapter = (CodeGeneratorAdapter) _getAdapter(port
                .getContainer());

        int length = 0;
        if (port.isInput()) {
            length = port.getWidthInside();
        } else {
            length = port.getWidth();
        }

        for (int j = 0; j < length; j++) {
            List sinkChannels = CodeGeneratorAdapter.getSinkChannels(port, j);

            for (int k = 0; k < sinkChannels.size(); k++) {
                Channel channel = (Channel) sinkChannels.get(k);
                ptolemy.actor.IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                Object offsetObject = adapter.getWriteOffset(sinkPort,
                        sinkChannelNumber);

                Receiver receiver = _getReceiver(
                        offsetObject.toString(), sinkChannelNumber, sinkPort);

                // FIXME rodiers: reintroduce PN specifics (but somewhere else)
                if (false) {
                /*if (_isPthread() && MpiPNDirector.isMpiReceiveBuffer(sinkPort, sinkChannelNumber)) {
                    code.append(_generateMPISendCode(j, rate, sinkPort, sinkChannelNumber, director));

                } else if (_isPthread() && receiver instanceof PNQueueReceiver) {

                    // PNReceiver.                    
                    code.append(_updatePNOffset(rate, sinkPort, sinkChannelNumber, director, true));
                    */
                 // End FIXME rodiers
                } else {

                    if (offsetObject instanceof Integer) {
                        int offset = ((Integer) offsetObject).intValue();
                        int bufferSize = adapter.getBufferSize(sinkPort,
                                sinkChannelNumber);
                        if (bufferSize != 0) {
                            offset = (offset + rate) % bufferSize;
                        }
                        adapter.setWriteOffset(sinkPort, sinkChannelNumber, Integer
                                .valueOf(offset));
                    } else { // If offset is a variable.
                        String offsetVariable = (String) adapter.getWriteOffset(
                                sinkPort, sinkChannelNumber);

                        if (padBuffers) {
                            int modulo = adapter.getBufferSize(sinkPort,
                                    sinkChannelNumber) - 1;
                            code.append(offsetVariable + " = ("
                                    + offsetVariable + " + " + rate + ")&" + modulo
                                    + ";" + _eol);
                        } else {
                            code.append(offsetVariable + " = ("
                                    + offsetVariable + " + " + rate
                                    + ") % "
                                    + adapter.getBufferSize(sinkPort,
                                            sinkChannelNumber) + ";" + _eol);
                        }
                    }
                }
            }
        }
        code.append(getCodeGenerator().comment(_eol + "....End updateConnectedPortsOffset...."
					       + CodeGeneratorAdapter.generateName(port)));
        return code.toString();
    }


    // Updating the read offset.
    public String updateOffset(int rate, Director directorAdapter) 
    throws IllegalActionException {

        ptolemy.actor.IOPort port = 
            (ptolemy.actor.IOPort) getComponent();
        Receiver receiver = _getReceiver(null, 0, port);

        String code = getCodeGenerator().comment(_eol + "....Begin updateOffset...." 
						 + CodeGeneratorAdapter.generateName(port));

        //        int width = 0;
        //        if (port.isInput()) {
        //            width = port.getWidth();
        //        } else {
        //            width = port.getWidthInside();
        //        }

        for (int i = 0; i < port.getWidth(); i++) {
            // FIXME rodiers: reintroduce PN specifics (but somewhere else)
            if (false) {
            /*
            if (MpiPNDirector.isMpiReceiveBuffer(port, i)) {
                // do nothing.
            } else if (_isPthread() && receiver instanceof PNQueueReceiver) {

                // FIXME: this is kind of hacky.
                //PNDirector pnDirector = (PNDirector)//directorAdapter;         
                _getAdapter(((Actor) port.getContainer()).getExecutiveDirector());

                List<Channel> channels = PNDirector.getReferenceChannels(port, i);

                for (Channel channel : channels) {
                    code += _updatePNOffset(rate, channel.port, 
                            channel.channelNumber, directorAdapter, false);
                }
                code += getCodeGenerator().comment(_eol + "....End updateOffset (PN)...."
						   + CodeGeneratorAdapter.generateName(port));
            */
             // End FIXME rodiers
            } else {
                code += _updateOffset(i, rate);
                code += getCodeGenerator().comment(_eol + "\n....End updateOffset...."
						   + CodeGeneratorAdapter.generateName(port));
            }
        }
        return code;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

 // FIXME rodiers: reintroduce PN specifics (but somewhere else)
    /*
    private String _generateMPISendCode(int channelNumber, 
            int rate, ptolemy.actor.IOPort sinkPort,
            int sinkChannelNumber, Director director) throws IllegalActionException {
        ptolemy.actor.TypedIOPort port = (ptolemy.actor.TypedIOPort) getComponent();

        StringBuffer code = new StringBuffer();

        code.append("// generateMPISendCode()" + _eol);

        for (int i = 0; i < rate; i++) {

            int sinkRank = MpiPNDirector.getRankNumber((Actor) sinkPort.getContainer());
            int sourceRank = MpiPNDirector.getRankNumber((Actor) port.getContainer());

            code.append("// Initialize send tag value." + _eol);
            code.append("static int " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " = " +
                    MpiPNDirector.getMpiReceiveBufferId(sinkPort, sinkChannelNumber) + ";" + _eol);

            if (MpiPNDirector._DEBUG) {
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
                CodeGeneratorAdapter.generatePortReference(sinkPort, channelAndOffset , false);

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

            code.append(", " + MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) +
                    ", " + "comm, &" +
                    MpiPNDirector.generateMpiRequest(sinkPort, sinkChannelNumber) + "[" + 
                    MpiPNDirector.generateFreeSlots(sinkPort, sinkChannelNumber) + "[" + 
                    MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                    ".current" + (i == 0 ? "" : i) + "]]" + ");" + _eol);

            if (MpiPNDirector._DEBUG) {
                code.append("printf(\"" + MpiPNDirector.getBufferLabel(port, channelNumber) +
                        ", rank[" + sourceRank + "], sended tag[%d]\\n\", " + 
                        MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + ");" + _eol);
            }            
        }

        // Update the Offset.
        code.append(MpiPNDirector.generatePortHeader(sinkPort, sinkChannelNumber) + 
                ".current += " + rate + ";" + _eol);

        MpiPNDirector directorAdapter = (MpiPNDirector) _getAdapter(director);
        code.append(MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " += " + 
                directorAdapter.getNumberOfMpiConnections(true) + ";" + _eol);

        code.append(MpiPNDirector.getSendTag(sinkPort, sinkChannelNumber) + " &= 32767; // 2^15 - 1 which is the max tag value." + _eol);

        return  code + _eol;

    }
    //End FIXME rodiers
     */
    
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

        //if (MpiPNDirector.isLocalBuffer(port, channel)) {
        //    int i = 1;
        //}


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


    private ptolemy.codegen.actor.Director _getDirectorAdapter() throws IllegalActionException {
        Director director = getDirector();
        return (ptolemy.codegen.actor.Director) _getAdapter(director);
    }


    private Receiver _getReceiver(String offset, int channel, ptolemy.actor.IOPort port) {
        Receiver[][] receivers = port.getReceivers();
        
        // For output ports getReceivers always returns an empty table.
        if (receivers.length == 0) {
            return null;
        }

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
            assert (receivers.length > 0);
            receiver = receivers[channel][0];
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


//    private boolean _isMpi() {
//        return getCodeGenerator().getAttribute("mpi") != null;
//    }


    private boolean _isPthread() {
        ptolemy.actor.IOPort port = (ptolemy.actor.IOPort) getComponent();
        boolean isPN = (((Actor) port.getContainer()).getDirector() 
                instanceof ptolemy.domains.pn.kernel.PNDirector);

        return isPN && (null == getCodeGenerator().getAttribute("mpi"))
        && (getCodeGenerator().target.getExpression().equals("default") || 
            getCodeGenerator().target.getExpression().equals("posix"));
    }


    /** Update the offset of the channel.
     *  @param channel The channel number of the channel to be offset.
     *  @param rate The firing rate of the port.
     *  @return The code that represents the offset to the channel, 
     *  @exception IllegalActionException If thrown while getting a token,
     *  adapter, read offset or buffer size.
     */
    protected String _updateOffset(int channel, int rate) throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        boolean padBuffers = ((BooleanToken) _codeGenerator.padBuffers
                .getToken()).booleanValue();

        ptolemy.actor.IOPort port = 
            (ptolemy.actor.IOPort) getComponent();
        CodeGeneratorAdapter adapter = (CodeGeneratorAdapter) _getAdapter(port
                .getContainer());

        // Update the offset for each channel.            
        if (adapter.getReadOffset(port, channel) instanceof Integer) {
            int offset = ((Integer) adapter.getReadOffset(port, channel))
            .intValue();
            if (adapter.getBufferSize(port, channel) != 0) {
                offset = (offset + rate) % adapter.getBufferSize(port, channel);
            }
            adapter.setReadOffset(port, channel, Integer.valueOf(offset));
        } else { // If offset is a variable.
            String offsetVariable = (String) adapter.getReadOffset(port, channel);
            if (padBuffers) {
                int modulo = adapter.getBufferSize(port, channel) - 1;
                code.append(offsetVariable + " = (" + offsetVariable + 
                        " + " + rate + ")&" + modulo + ";" + _eol);
            } else {
                code.append(offsetVariable + " = (" + offsetVariable + 
                        " + " + rate + ") % " + 
                        adapter.getBufferSize(port, channel) + ";" + _eol);
            }
        }
        return code.toString();

    }

    // FIXME rodiers: reintroduce PN specifics (but somewhere else)
    /*
    private String _updatePNOffset(int rate, ptolemy.actor.IOPort port, 
            int channelNumber, Director directorAdapter, boolean isWrite)
    throws IllegalActionException {
        // FIXME: this is kind of hacky.
        PNDirector pnDirector = (PNDirector) //directorAdapter; 
        _getAdapter(((Actor) port.getContainer()).getExecutiveDirector());

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
    //end FIXME rodiers
    */

    /** Get the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @return return The size of the buffer.
     *  @see #setBufferSize(int, int)
     *  @exception IllegalActionException If thrown while getting the width
     *  of the channel.
     */
    public int getBufferSize(int channelNumber)
	throws IllegalActionException {
        Channel channel = _getChannel(channelNumber);
        
        if (_bufferSizes.get(channel) == null) {
            // This should be a special case for doing
            // codegen for a sub-part of a model.
            return channel.port.getWidth();            
        }
        
        return _bufferSizes.get(channel);
    }

    
    /** Get the read offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The read offset.
     *  @exception IllegalActionException If thrown while getting the channel.
     *  @see #setReadOffset(int, Object)
     */
    public Object getReadOffset(int channelNumber)
	throws IllegalActionException {
        Channel channel = _getChannel(channelNumber);
        return _readOffsets.get(channel);
        
    }
    
    /** Get the write offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The write offset.
     *  @exception IllegalActionException If thrown while getting the channel.
     *  @see #setWriteOffset(int, Object)
     */
    public Object getWriteOffset(int channelNumber)
    throws IllegalActionException {
        Channel channel = _getChannel(channelNumber);
        return _writeOffsets.get(channel);
        
    }

    /** Set the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param bufferSize The size of the buffer.
     *  @see #getBufferSize(int)
     * 
     */
    public void setBufferSize(int channelNumber, int bufferSize) {
        Channel channel = _getChannel(channelNumber);
        _bufferSizes.put(channel, bufferSize);
    }
    
    /** Set the read offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param readOffset The offset.
     *  @see #getReadOffset(int)
     */
    public void setReadOffset(int channelNumber, Object readOffset) {
        Channel channel = _getChannel(channelNumber);
        _readOffsets.put(channel, readOffset);
    }

    /** Set the write offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param writeOffset The offset.
     *  @see #getWriteOffset(int)
     */
    public void setWriteOffset(int channelNumber, Object writeOffset) {
        Channel channel = _getChannel(channelNumber);
        _writeOffsets.put(channel, writeOffset);
    }
    
    /** A HashMap that keeps track of the bufferSizes of each channel
     *  of the actor.
     */
    protected HashMap<Channel, Integer> _bufferSizes = 
        new HashMap<Channel, Integer>();

    /** A HashMap that keeps track of the read offsets of each input channel of
     *  the actor.
     */
    protected HashMap<Channel, Object> _readOffsets = 
        new HashMap<Channel, Object>();
    
    /** A HashMap that keeps track of the write offsets of each input channel of
     *  the actor.
     */
    protected HashMap<Channel, Object> _writeOffsets = 
        new HashMap<Channel, Object>();    
    
    private Channel _getChannel(int channelNumber) {
        return new Channel((ptolemy.actor.IOPort) 
                getComponent(), channelNumber);
    }

}
