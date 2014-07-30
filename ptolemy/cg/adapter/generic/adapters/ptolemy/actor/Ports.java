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
import java.util.Map;

import ptolemy.actor.IOPort;
import ptolemy.cg.kernel.generic.GenericCodeGenerator;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/** A adapter class that allows generating code for ports.
 *  @author Gang Zhou, Contributor: Bert Rodiers, Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Yellow (zgang)
 * @Pt.AcceptedRating Red (eal)
 */
public class Ports {

    /** Create a Ports instance.
     *  @param component The component for which we are doing
     *  extra bookkeeping to generate code.
     *  @param director The director associated with the ports.
     */
    public Ports(NamedObj component, PortDirector director) {
        _component = component;
        _director = director;
    }

    /** Return the code generator associated with the director.
     *  @return the director.
     */
    public GenericCodeGenerator getCodeGenerator() {
        return _director.getCodeGenerator();
    }

    /** Return the component associated with the ports.
     *  @return the component.
     */
    public NamedObj getComponent() {
        return _component;
    }

    /**Generate the expression that represents the offset in the generated
     * code.
     *  @param port The given port.
     * @param offset The specified offset from the user.
     * @param channel The referenced port channel.
     * @param isWrite Whether to generate the write or read offset.
     * @return The expression that represents the offset in the generated code.
     * @exception IllegalActionException If there is problems getting the port
     *  buffer size or the offset in the channel and offset map.
     */
    public String generateOffset(IOPort port, String offset, int channel,
            boolean isWrite) throws IllegalActionException {
        return _getPortInfo(port).generateOffset(offset, channel, isWrite);
    }

    /** Get the buffer size of channel of the port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param port The given port.
     *  @return return The size of the buffer.
     *  @exception IllegalActionException If thrown while getting the port
     *  information or getting the buffer size.
     *  @see #setBufferSize(IOPort, int, int)
     */
    public int getBufferSize(IOPort port, int channelNumber)
            throws IllegalActionException {
        return _getPortInfo(port).getBufferSize(channelNumber);
    }

    /**
     * Return the buffer size of a given port, which is the maximum of
     * the bufferSizes of all channels of the given port.
     * @param port The given port.
     * @return The buffer size of the given port.
     * @exception IllegalActionException If the
     * {@link #getBufferSize(IOPort, int)} method throws it.
     * @see #setBufferSize(IOPort, int, int)
     */
    public int getBufferSize(IOPort port) throws IllegalActionException {
        return _getPortInfo(port).getBufferSize();
    }

    /** Get the read offset in the buffer of a given channel from which a token
     *  should be read. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param inputPort The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel from which a token
     *   should be read.
     *  @exception IllegalActionException Thrown if the adapter class cannot
     *   be found.
     *  @see #setReadOffset(IOPort, int, Object)
     */
    public Object getReadOffset(IOPort inputPort, int channelNumber)
            throws IllegalActionException {
        return _getPortInfo(inputPort).getReadOffset(channelNumber);
    }

    /** Get the write offset in the buffer of a given channel to which a token
     *  should be put. The channel is given by its containing port and
     *  the channel number in that port.
     *  @param port The given port.
     *  @param channelNumber The given channel number.
     *  @return The offset in the buffer of a given channel to which a token
     *   should be put.
     *  @exception IllegalActionException Thrown if the adapter class cannot
     *   be found.
     *  @see #setWriteOffset(IOPort, int, Object)
     */
    public Object getWriteOffset(IOPort port, int channelNumber)
            throws IllegalActionException {
        return _getPortInfo(port).getWriteOffset(channelNumber);
    }

    /** Initialize the offsets.
     *  @param port The given port.
     *  @return The code to initialize the offsets.
     *  @exception IllegalActionException Thrown if offsets can't be initialized.
     */
    public String initializeOffsets(IOPort port) throws IllegalActionException {
        return _getPortInfo(port).initializeOffsets();
    }

    /** Set the buffer size of channel of the port.
     *  @param port The given port.
     *  @param channelNumber The number of the channel that is being set.
     *  @param bufferSize The size of the buffer.
     *  @see #getBufferSize(IOPort, int)
     *  @exception IllegalActionException If thrown while getting the port
     *  information or while setting the buffer size.
     */
    public void setBufferSize(IOPort port, int channelNumber, int bufferSize)
            throws IllegalActionException {
        _getPortInfo(port).setBufferSize(channelNumber, bufferSize);
    }

    /** Set the read offset in a buffer of a given channel from which a token
     *  should be read.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @param readOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException Thrown if the adapter class cannot
     *   be found.
     *  @see #getReadOffset(IOPort, int)
     */
    public void setReadOffset(IOPort port, int channelNumber, Object readOffset)
            throws IllegalActionException {
        _getPortInfo(port).setReadOffset(channelNumber, readOffset);
    }

    /** Set the write offset in a buffer of a given channel to which a token
     *  should be put.
     *  @param port The given port.
     *  @param channelNumber The given channel.
     *  @param writeOffset The offset to be set to the buffer of that channel.
     *  @exception IllegalActionException If
     *   {@link #setWriteOffset(IOPort, int, Object)} method throws it.
     *  @see #getWriteOffset(IOPort, int)
     */
    public void setWriteOffset(IOPort port, int channelNumber,
            Object writeOffset) throws IllegalActionException {
        _getPortInfo(port).setWriteOffset(channelNumber, writeOffset);
    }

    /** Update the offsets of the buffers associated with the ports connected
     *  with the given port in its downstream.
     *
     *  @param port The port whose directly connected downstream actors update
     *   their write offsets.
     *  @return The generated code.
     *  @param rate The rate, which must be greater than or equal to 0.
     *  @exception IllegalActionException If thrown while reading or writing
     *   offsets, or getting the buffer size, or if the rate is less than 0.
     */
    public String updateConnectedPortsOffset(IOPort port, int rate)
            throws IllegalActionException {
        return _getPortInfo(port).updateConnectedPortsOffset(rate);
    }

    /** Update the read offset.
     *  @param port The given port.
     *  @param rate  The rate of the channels.
     *  @return The offset.
     *  @exception IllegalActionException If thrown while getting a token,
     *  adapter, read offset or buffer size.
     */
    public String updateOffset(IOPort port, int rate)
            throws IllegalActionException {
        return _getPortInfo(port).updateOffset(rate);
    }

    /** Return the information necessary to generate
     *  communication code at the given port.
     *  @param port The given port for which we want to retrieve
     *  information to generate code.
     *  @return The information necessary to generate communication code.
     *  @exception IllegalActionException If thrown while getting the adapter.
     */
    private PortInfo _getPortInfo(IOPort port) throws IllegalActionException {
        PortInfo info = null;
        if (!_portInfo.containsKey(port)) {
            NamedObj container = getComponent().getContainer().getContainer();
            // If we don't have portInfo for the port, then go up the hierarchy and look
            // for portInfo elsewhere.  This is very convoluted, but necessary for
            // $PTII/bin/ptcg -language java $PTII/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/domains/sdf/lib/test/auto/SampleDelay5.xml
            if (container != null
                    && getComponent().getContainer() != port.getContainer()
                            .getContainer()
                    && getComponent().getContainer() != port.getContainer()
                    && getComponent()
                            .getContainer()
                            .getFullName()
                            .startsWith(
                                    port.getContainer().getContainer()
                                            .getFullName())) {
                while (container != null) {
                    if (container instanceof CompositeEntity) {
                        List entities = ((CompositeEntity) container)
                                .attributeList(ptolemy.actor.Director.class);
                        if (entities.size() > 0) {
                            Director entity = (Director) getCodeGenerator()
                                    .getAdapter(
                                            entities.get(entities.size() - 1));
                            if (entity instanceof PortDirector) {
                                PortDirector parent = (PortDirector) entity;
                                if (parent.ports._portInfo.containsKey(port)) {
                                    info = parent.ports._portInfo.get(port);
                                }
                                break;
                            }
                        }
                    }
                    container = container.getContainer();
                }
            }
            if (info == null) {
                info = new PortInfo(port, this, _component, _director);
            }
            _portInfo.put(port, info);
        } else {
            info = _portInfo.get(port);
        }
        return info;
    }

    /** A map from IOPort to PortInfo. */
    protected Map<IOPort, PortInfo> _portInfo = new HashMap<IOPort, PortInfo>();

    /** The component associated with the ports. */
    private NamedObj _component;

    /** The director associated with the ports. */
    private PortDirector _director;
}
