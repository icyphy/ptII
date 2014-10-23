/* Code generator adapter class associated with the Director class.

   Copyright (c) 2005-2014 The Regents of the University of California.
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
package ptolemy.cg.adapter.generic.adapters.ptolemy.actor;

import java.util.HashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.DFUtilities;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.cg.kernel.generic.program.NamedProgramCodeGeneratorAdapter;
import ptolemy.cg.kernel.generic.program.ProgramCodeGeneratorAdapter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

/** A class that keeps track of information necessary to
 *  generate communication code at ports inside a StaticScheduled model.
 *
 *  @author Gang Zhou, Contributor: Bert Rodiers, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (zgang)
 *  @Pt.AcceptedRating Red (eal)
 */
public class PortInfo {

    /** Create a PortInfo instance.
     *  @param port The port for which we are doing
     *  extra bookkeeping to generate code.
     *  @param ports The ports the ports
     *  @param component The component for which we are doing
     *  extra bookkeeping to generate code.
     *  @param director The director associated with the ports.
     */
    public PortInfo(IOPort port, Ports ports, NamedObj component,
            PortDirector director) {
        _port = port;
        _ports = ports;
        //_component = component;
        _director = director;
    }

    /** Return the code generator of the director.
     *  @return the code generator.
     */
    public GenericCodeGenerator getCodeGenerator() {
        return _director.getCodeGenerator();
    }

    /** Get the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @return return The size of the buffer.
     *  @see #setBufferSize(int, int)
     *  @exception IllegalActionException If thrown while getting
     *  the channel or rate.
     */
    public int getBufferSize(int channelNumber) throws IllegalActionException {
        ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
        if (_bufferSizes.get(channel) == null) {
            // This should be a special case for doing
            // codegen for a sub-part of a model.
            //FIXME Why is the buffer size of a port its width? Should it be the rate of the port?
            return DFUtilities.getRate(channel.port);
            //return channel.port.getWidth();
        }
        return _bufferSizes.get(channel);
    }

    /**
     * Return the buffer size of the port, which is the maximum of
     * the bufferSizes of all channels the port.
     * @return The buffer size of the port.
     * @exception IllegalActionException If the
     * {@link #getBufferSize(int)} method throws it.
     * @see #setBufferSize(int, int)
     */
    public int getBufferSize() throws IllegalActionException {
        int bufferSize = 1;

        int length = 0;

        if (_port.isInput()) {
            length = _port.getWidth();
        } else {
            length = _port.getWidthInside();
        }

        for (int i = 0; i < length; i++) {
            int channelBufferSize = 1;
            try {
                channelBufferSize = getBufferSize(i);
            } catch (ptolemy.actor.sched.NotSchedulableException ex) {
                // Ignore.  Probably a modal model.
                // $PTII/bin/ptcg -inline true -language java /Users/cxh/ptII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/modal/test/auto/Simple01.xml
            }
            if (channelBufferSize > bufferSize) {
                bufferSize = channelBufferSize;
            }
        }
        return bufferSize;
    }

    /**Generate the expression that represents the offset in the generated
     * code.
     * @param offset The specified offset from the user.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     *  buffer size or the offset in the channel and offset map.
     */
    public String generateOffset(String offset, int channel, boolean isWrite)
            throws IllegalActionException {
        return _generateOffset(offset, channel, isWrite);
    }

    /** Get the read offset of a channel of the port.
     *  @param channelNumber The number of the channel.
     *  @return The read offset.
     *  @exception IllegalActionException If thrown while getting the channel.
     *  @see #setReadOffset(int, Object)
     */
    public Object getReadOffset(int channelNumber)
            throws IllegalActionException {
        ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
        if (_readOffsets.get(channel) == null) {
            throw new IllegalActionException(
                    "Could not find the specified channel in this director");
        }
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
        ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
        if (_writeOffsets.get(channel) == null) {
            throw new IllegalActionException(_port,
                    "Could not write offset for channel " + channelNumber
                            + " in port " + _port.getFullName()
                            + ", there were " + _writeOffsets.size()
                            + " writeOffsets for this port.");
        }
        return _writeOffsets.get(channel);

    }

    /** Initialize the offsets.
     *  @return The code to initialize the offsets.
     *  @exception IllegalActionException Thrown if offsets can't be initialized.
     */
    public String initializeOffsets() throws IllegalActionException {

        /* FIXME: move pthread specific code out-of-here...
           if (_isPthread()) {
           return "";
           }
         */

        StringBuffer code = new StringBuffer();

        for (int i = 0; i < _port.getWidth(); i++) {
            Object readOffset = _ports.getReadOffset(_port, i);
            if (readOffset instanceof Integer) {
                _ports.setReadOffset(_port, i, Integer.valueOf(0));
            } else {
                code.append((String) readOffset + " = 0;" + _eol);
            }
            Object writeOffset = _ports.getWriteOffset(_port, i);
            if (writeOffset instanceof Integer) {
                _ports.setWriteOffset(_port, i, Integer.valueOf(0));
            } else {
                code.append((String) writeOffset + " = 0;" + _eol);
            }
        }
        return code.toString();
    }

    /** Set the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param bufferSize The size of the buffer.
     *  @see #getBufferSize(int)
     */
    public void setBufferSize(int channelNumber, int bufferSize) {
        ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
        _bufferSizes.put(channel, bufferSize);
    }

    /** Set the read offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param readOffset The offset.
     *  @see #getReadOffset(int)
     */
    public void setReadOffset(int channelNumber, Object readOffset) {
        ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
        _readOffsets.put(channel, readOffset);
    }

    /** Set the write offset of a channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param writeOffset The offset.
     *  @see #getWriteOffset(int)
     */
    public void setWriteOffset(int channelNumber, Object writeOffset) {
        ProgramCodeGeneratorAdapter.Channel channel = _getChannel(channelNumber);
        _writeOffsets.put(channel, writeOffset);
    }

    /** Update the offsets of the buffers associated with the ports connected
     *  with the given port in its downstream.
     *
     *  @return The generated code.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    public String updateConnectedPortsOffset(int rate)
            throws IllegalActionException {
        boolean padBuffers = _director.padBuffers();

        StringBuffer code = new StringBuffer();
        code.append(getCodeGenerator().comment(
                "Begin updateConnectedPortsOffset "
                        + /*NamedProgramCodeGeneratorAdapter.*/_director
                                .generatePortName((TypedIOPort) _port)));

        if (rate == 0) {
            return "";
        } else if (rate < 0) {
            throw new IllegalActionException(_port, "the rate: " + rate
                    + " is negative.");
        }

        int length = 0;
        if (_port.isInput()) {
            length = _port.getWidthInside();
        } else {
            length = _port.getWidth();
        }

        for (int j = 0; j < length; j++) {
            List<ProgramCodeGeneratorAdapter.Channel> sinkChannels = NamedProgramCodeGeneratorAdapter
                    .getSinkChannels(_port, j);

            for (int k = 0; k < sinkChannels.size(); k++) {
                ProgramCodeGeneratorAdapter.Channel channel = sinkChannels
                        .get(k);
                ptolemy.actor.IOPort sinkPort = channel.port;
                int sinkChannelNumber = channel.channelNumber;

                Object offsetObject = _ports.getWriteOffset(sinkPort,
                        sinkChannelNumber);

                if (offsetObject instanceof Integer) {
                    int offset = ((Integer) offsetObject).intValue();
                    int bufferSize = _ports.getBufferSize(sinkPort,
                            sinkChannelNumber);
                    if (bufferSize != 0) {
                        offset = (offset + rate) % bufferSize;
                    }
                    _ports.setWriteOffset(sinkPort, sinkChannelNumber,
                            Integer.valueOf(offset));
                } else { // If offset is a variable.
                    String offsetVariable = (String) _ports.getWriteOffset(
                            sinkPort, sinkChannelNumber);

                    if (padBuffers) {
                        int modulo = _ports.getBufferSize(sinkPort,
                                sinkChannelNumber) - 1;
                        code.append(offsetVariable + " = (" + offsetVariable
                                + " + " + rate + ")&" + modulo + ";" + _eol);
                    } else {
                        code.append(offsetVariable
                                + " = ("
                                + offsetVariable
                                + " + "
                                + rate
                                + ") % "
                                + _ports.getBufferSize(sinkPort,
                                        sinkChannelNumber) + ";" + _eol);

                    }
                }
            }
        }
        code.append(getCodeGenerator().comment(
                "End updateConnectedPortsOffset "
                        + /*NamedProgramCodeGeneratorAdapter.*/_director
                                .generatePortName((TypedIOPort) _port)));
        return code.toString();
    }

    /** Update the read offset.
     *  @param rate  The rate of the channels.
     *  @return The offset.
     *  @exception IllegalActionException If thrown while getting a token,
     *  adapter, read offset or buffer size.
     */
    public String updateOffset(int rate) throws IllegalActionException {

        //Receiver receiver = _getReceiver(null, 0, _port);

        if (_director == null) {
            throw new NullPointerException("_director == null?");
        }
        if (getCodeGenerator() == null) {
            throw new NullPointerException("getCodeGenerator() returned null?");
        }
        StringBuffer code = new StringBuffer(getCodeGenerator().comment(
                "Begin updateOffset "
                        + /*NamedProgramCodeGeneratorAdapter.*/_director
                                .generatePortName((TypedIOPort) _port)));

        for (int i = 0; i < _port.getWidth(); i++) {
            code.append(_updateOffset(i, rate)
                    + _eol
                    + getCodeGenerator()
                            .comment(
                                    "End updateOffset "
                                            + /*NamedProgramCodeGeneratorAdapter.*/_director
                                                    .generatePortName((TypedIOPort) _port)));
        }
        return code.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////
    /** The value of the line.separator property. */
    protected static final String _eol;

    static {
        _eol = StringUtilities.getProperty("line.separator");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private ProgramCodeGeneratorAdapter.Channel _getChannel(int channelNumber) {
        return new ProgramCodeGeneratorAdapter.Channel(_port, channelNumber);
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
    private String _generateOffset(String offsetString, int channel,
            boolean isWrite) throws IllegalActionException {
        boolean dynamicReferencesAllowed = _director
                .allowDynamicMultiportReference();
        boolean padBuffers = _director.padBuffers();

        //if (MpiPNDirector.isLocalBuffer(port, channel)) {
        //    int i = 1;
        //}

        // When dynamic references are allowed, any input ports require
        // offsets.
        if (dynamicReferencesAllowed && _port.isInput()) {
            if (!(_port.isMultiport() || getBufferSize() > 1)) {
                return "";
            }
        } else {
            int bufferSize = getBufferSize();
            if (!(bufferSize > 1)) {
                return "";
            }
        }

        String result = "";
        Object offsetObject;

        // Get the offset index.
        if (isWrite) {
            offsetObject = getWriteOffset(channel);
        } else {
            offsetObject = getReadOffset(channel);
        }
        if (!offsetString.equals("")) {
            // Specified offset.

            String temp = "";
            if (offsetObject instanceof Integer && _isInteger(offsetString)) {

                int offset = ((Integer) offsetObject).intValue()
                        + Integer.parseInt(offsetString);

                offset %= getBufferSize(channel);
                temp = Integer.toString(offset);
                /*
                  int divisor = getBufferSize(sinkPort,
                  sinkChannelNumber);
                  temp = "("
                  + getWriteOffset(sinkPort,
                  sinkChannelNumber) + " + "
                  + channelAndOffset[1] + ")%" + divisor;
                 */
                result = "[" + temp + "]";
            } else {
                // FIXME: We haven't check if modulo is 0. But this
                // should never happen. For offsets that need to be
                // represented by string expression,
                // getBufferSize(_port, channelNumber) will always
                // return a value at least 2.

                //              if (MpiPNDirector.isLocalBuffer(_port, channel)) {
                //              temp = offsetObject.toString();
                //              temp = MpiPNDirector.generateFreeSlots(_port, channel) +
                //              "[" + MpiPNDirector.generatePortHeader(_port, channel) + ".current]";
                //              } else
                if (offsetObject == null) {
                    result = getCodeGenerator()
                            .comment(
                                    _port.getFullName()
                                            + " Getting offset for channel "
                                            + channel
                                            + " returned null?"
                                            + "This can happen if there are problems with Profile.firing().");

                } else {
                    if (padBuffers) {
                        int modulo = getBufferSize(channel) - 1;
                        temp = "(" + offsetObject.toString() + " + "
                                + offsetString + ")&" + modulo;
                    } else {
                        int modulo = getBufferSize(channel);
                        temp = "(" + offsetObject.toString() + " + "
                                + offsetString + ")%" + modulo;
                    }
                    result = "[" + temp + "]";
                }
            }

        } else {
            // Did not specify offset, so the receiver buffer
            // size is 1. This is multiple firing.

            if (offsetObject instanceof Integer) {
                int offset = ((Integer) offsetObject).intValue();

                offset %= getBufferSize(channel);

                result = "[" + offset + "]";
            } else if (offsetObject != null) {

                //              if (MpiPNDirector.isLocalBuffer(_port, channel)) {
                //              result = offsetObject.toString();
                //              result = MpiPNDirector.generateFreeSlots(_port, channel) +
                //              "[" + MpiPNDirector.generatePortHeader(_port, channel) + ".current]";
                //              } else
                if (padBuffers) {
                    int modulo = getBufferSize(channel) - 1;
                    result = "[" + offsetObject + "&" + modulo + "]";
                } else {
                    result = "[" + offsetObject + "%" + getBufferSize(channel)
                            + "]";
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

    /** Update the offset of the channel.
     *  @param channel The channel number of the channel to be offset.
     *  @param rate The firing rate of the port.
     *  @return The code that represents the offset to the channel,
     *  @exception IllegalActionException If thrown while getting a token,
     *  adapter, read offset or buffer size.
     */
    private String _updateOffset(int channel, int rate)
            throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        boolean padBuffers = _director.padBuffers();

        // Update the offset for each channel.
        if (getReadOffset(channel) instanceof Integer) {
            int offset = ((Integer) getReadOffset(channel)).intValue();
            if (getBufferSize(channel) != 0) {
                offset = (offset + rate) % getBufferSize(channel);
            }
            setReadOffset(channel, Integer.valueOf(offset));
        } else { // If offset is a variable.
            String offsetVariable = (String) getReadOffset(channel);
            if (padBuffers) {
                int modulo = getBufferSize(channel) - 1;
                code.append(offsetVariable + " = (" + offsetVariable + " + "
                        + rate + ")&" + modulo + ";" + _eol);
            } else {
                code.append(offsetVariable + " = (" + offsetVariable + " + "
                        + rate + ") % " + getBufferSize(channel) + ";" + _eol);
            }
        }
        return code.toString();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** A HashMap that keeps track of the bufferSizes of each channel
     *  of the actor.
     */
    private HashMap<ProgramCodeGeneratorAdapter.Channel, Integer> _bufferSizes = new HashMap<ProgramCodeGeneratorAdapter.Channel, Integer>();

    /** The port for which we are doing extra bookkeeping to generate code.
     */
    private IOPort _port;

    /** A HashMap that keeps track of the read offsets of each input channel of
     *  the actor.
     */
    private HashMap<ProgramCodeGeneratorAdapter.Channel, Object> _readOffsets = new HashMap<ProgramCodeGeneratorAdapter.Channel, Object>();

    /** A HashMap that keeps track of the write offsets of each input channel of
     *  the actor.
     */
    private HashMap<ProgramCodeGeneratorAdapter.Channel, Object> _writeOffsets = new HashMap<ProgramCodeGeneratorAdapter.Channel, Object>();

    /** The meta information about the ports in the container. */
    private Ports _ports;

    //private NamedObj _component;

    private PortDirector _director;
}
