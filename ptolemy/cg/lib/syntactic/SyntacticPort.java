/* Syntactic Graph for syntactic representations.

Copyright (c) 2010-2014 The Regents of the University of California.
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

package ptolemy.cg.lib.syntactic;

import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/** Represent ComponentPorts syntactically in SyntacticNodes.
 *  Ports in models that can be interpreted as input or output
 *  ports can be represented referentially by this object. The
 *  semantic information about direction is not retained in the
 *  reference but represented by the context of the reference in
 *  an input or output list in a SyntacticTerm. Multiports are
 *  split into series of SyntacticPorts. Bidirectional ports are
 *  split into pairs of SyntacticPorts.
 *  <p>
 *  @author Chris Shaver
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating red (shaver)
 *  @Pt.AcceptedRating red
 *
 */
public class SyntacticPort extends ComponentPort {

    /** Construct SyntacticPort. */
    public SyntacticPort() {
        _representedPort = null;
        _representedChannel = 0;
        _representedWidth = 0;
        _isEmpty = true;
        _isInput = false;
        _iotype = IOType.none;
    }

    /** Construct SyntacticPort with given workspace.
     *  @param workspace Workspace to add SyntacticPort to.
     */
    public SyntacticPort(Workspace workspace) {
        super(workspace);
        _representedPort = null;
        _representedChannel = 0;
        _representedWidth = 0;
        _isEmpty = true;
        _isInput = false;
        _iotype = IOType.none;
    }

    /** Construct SyntacticPort with given container and name.
     *  The caller can associate the syntactic port with an actual port.
     *  The caller must determine if the port is input or output and
     *  the represented port, if one exist, will be treated inferentially
     *  as such.
     *  @param container SyntacticNode in which this port is added.
     *  @param port Port referred to by this SyntacticPort.
     *  @param direction True if input, false if output.
     *  @param name Name of this port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     *
     */
    public SyntacticPort(SyntacticNode container, Port port, boolean direction,
            String name) throws IllegalActionException,
            NameDuplicationException {
        super(container, name);
        _representedPort = port;
        _representedChannel = 0;

        Integer width = portWidth(port);
        _representedWidth = width == null ? 0 : width;
        _isEmpty = _representedWidth == 0;
        _isInput = direction;
        _iotype = portType(port);
    }

    /** Get the connected port from a given port.
     *  If the graph is not made bijective this gives the first.
     *  If there are no ports or no SyntacticPorts null is returned.
     *  @return An immediately connected port or null.
     */
    public SyntacticPort getConnectedPort() {
        List<Port> rports = connectedPortList();
        if (rports.size() == 0) {
            return null;
        }

        Port rport = rports.get(0);
        if (!(rport instanceof SyntacticPort)) {
            return null;
        }

        return (SyntacticPort) rport;
    }

    /** Get node in which port is contained.
     *  @return node in which port is contained or null if none.
     */
    public SyntacticNode getNode() {
        NamedObj obj = this.getContainer();
        if (obj == null || !(obj instanceof SyntacticNode)) {
            return null;
        }
        return (SyntacticNode) obj;
    }

    /** Set the channel of the represented port.
     *  Each SyntacticPort only represents a single channel of
     *  the represented port.
     *  @param channel The channel of the represented port.
     *  @see #getChannel
     */
    public void setChannel(int channel) {
        _representedChannel = channel >= 0 ? channel : 0;
    }

    /** Get the port represented by the Syntactic Port.
     *  @return represented port.
     *  */
    public Port getRepresentedPort() {
        return _representedPort;
    }

    /** Get the channel of the represented port.
     *  @return represented channel of the port.
     *  @see #setChannel
     */
    public int getChannel() {
        return _representedChannel;
    }

    /** Decide whether the port represents an actual port.
     *  If false, the port is purely syntactic.
     *  @return whether the port is representative.
     */
    public boolean isRepresentative() {
        return _representedPort != null;
    }

    /** Decide whether the port is representationally an input port.
     *  @return whether the port is an input port.
     *  */
    public boolean isInput() {
        return _isInput;
    }

    /** Decide whether the port is representationally an output port.
     *  @return whether the port is an output port.
     *  */
    public boolean isOutput() {
        return !_isInput;
    }

    /** Decide whether the port is disconnected.
     *  @return whether the port is disconnected.
     *  */
    public boolean isEmpty() {
        return _isEmpty;
    }

    /** Gets the IOType of the port.
     *  For ports that represent input/output ports, each SyntacticPort
     *  will be set to the appropriate type for the part of the
     *  port it represents.
     *
     *  @return IOType of the port.
     */
    public IOType getType() {
        return _iotype;
    }

    /** Gets the IOType of a given port.
     *
     *  @param port Port to find the type of.
     *  @return IOType of given port.
     */
    static public IOType portType(Port port) {
        if (!(port instanceof IOPort)) {
            return IOType.none;
        }

        IOPort ioport = (IOPort) port;
        boolean isin = ioport.isInput();
        boolean isout = ioport.isOutput();

        if (isin && isout) {
            return IOType.io;
        } else if (isin) {
            return IOType.in;
        } else if (isout) {
            return IOType.out;
        } else {
            return IOType.none;
        }
    }

    /** Decide whether a port is exterior in the given entity.
     *  This should be passed the model during analysis.
     *
     *  @param port Port to check for exteriority.
     *  @param entity Entity to check inside of.
     *  @return whether port is exterior for the given entity.
     */
    static public boolean isPortExterior(Port port, CompositeEntity entity) {
        return port.getContainer() == entity;
    }

    /** Gets the IOType of a given port with reference to a composite entity.
     *  If a port is an exterior port of the entity, its IOType is reversed
     *  to reflect the role it plays on the inside of the composite.
     *
     *  @param port Port to find the type of.
     *  @param entity Entity to check inside of.
     *  @return IOType of the port.
     */
    static public IOType portType(Port port, CompositeEntity entity) {
        IOType type = portType(port);
        return isPortExterior(port, entity) ? type.reverse() : type;
    }

    /** Gets the width of a Port.
     *
     *  @param port Port to find the width of.
     *  @return the width of the port or null if not a port.
     *  @exception IllegalActionException If thrown while getting the
     *  width.
     */
    static public Integer portWidth(Port port) throws IllegalActionException {
        if (!(port instanceof IOPort)) {
            return null;
        }

        IOPort ioport = (IOPort) port;
        int width = ioport.getWidth();
        return width;
    }

    /** Represent IO type for ports. */
    public enum IOType {
        /** Input port. */
        in,

        /** Output port. */
        out,

        /** Input/Output port. */
        io,

        /** Port with undefined or unclear directionality. */
        none;

        /** Get the reversed IO type.
         *  @return reversed IO type.
         */
        public IOType reverse() {
            return this == in ? out : this == out ? in : this;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Port being represented or null if the port is purely Syntactic. */
    private Port _representedPort;

    /** Channel represented of the represented port. */
    private int _representedChannel;

    /** Total width of the represented port. */
    private int _representedWidth;

    /** True if Syntactic Port is an input port. */
    private boolean _isInput;
    //private boolean _isOutput;

    /** True if Syntactic Port is not pointing to a represented port. */
    private boolean _isEmpty;

    /** Syntactic direction of connection. */
    private IOType _iotype;
}
