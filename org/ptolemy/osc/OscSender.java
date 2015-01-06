/* A Generic actor that generates OSC Messages via the provided input ports.

Copyright (c) 2013-2014 The Regents of the University of California.
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
package org.ptolemy.osc;

import java.util.HashMap;
import java.util.Map;

import netP5.NetAddress;
import oscP5.OscBundle;
import oscP5.OscMessage;
import oscP5.OscP5;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.IntToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
<p> An actor that generates and sends OSC Messages. Add input ports to this actor
and name the ports to match the desired tag name. Any token received via an input
port will have a tag equal to the name. A tag prefix common to all ports
can be defined by the help of the <i>tagPrefix</i> PortParameter. If specified, this
prefix will be prepended to all the tags, defined by the port names.

<p> Tokens that are received simultaneously at multiple input ports, i.e., that have
the same time stamp, will be bundled into a single OSCBundle object and will be sent
out as a single OSC message.


@see org.ptolemy.osc.OscReceiver

 @author Ilge Akkaya
@version $Id$
@since Ptolemy II 10.0
 @version $Id$
 @Pt.ProposedRating Red (ilgea)
 @Pt.AcceptedRating
 */
public class OscSender extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public OscSender(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        remoteHost = new Parameter(this, "remoteHost");
        remoteHost.setTypeEquals(BaseType.STRING);
        remoteHost.setExpression("\"127.0.0.1\"");

        remotePort = new Parameter(this, "remotePort");
        remotePort.setTypeEquals(BaseType.INT);
        remotePort.setExpression("9999");

        localPort = new Parameter(this, "localPort");
        localPort.setTypeEquals(BaseType.INT);
        localPort.setExpression("56999");

        tagPrefix = new PortParameter(this, "tagPrefix");
        tagPrefix.setTypeEquals(BaseType.STRING);
        tagPrefix.setExpression("\"\"");
    }

    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        OscSender newObject = (OscSender) super.clone(workspace);
        newObject.oscP5 = null;
        return newObject;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * IP Address of the remote host
     */
    public Parameter remoteHost;

    /**
     * Remote port to which the OSC messages will be sent to.
     */
    public Parameter remotePort;
    /**
     * Local port from which the OSC messages will be sent.
     */
    public Parameter localPort;

    /**
     * OSC prefix tag that will be prepended to each port name
     */
    public PortParameter tagPrefix;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == remotePort) {
            _remotePort = (((IntToken) remotePort.getToken()).intValue());
        } else if (attribute == remoteHost) {
            _host = (((StringToken) remoteHost.getToken()).stringValue());
        } else if (attribute == localPort) {
            _localPort = (((IntToken) localPort.getToken()).intValue());
        } else {
            super.attributeChanged(attribute);
        }
    }

    public void initialize() throws IllegalActionException {
        super.initialize();
        _constructOscReceiver(_localPort);
    }

    public void fire() throws IllegalActionException {

        super.fire();
        if (tagPrefix.getPort().isOutsideConnected()) {
            tagPrefix.update();
        }
        // collect tokens and generate OSC Messages

        HashMap<String, Object> tokensToBeTransmitted = new HashMap<String, Object>();
        for (TypedIOPort port : this.inputPortList()) {
            if (!(port instanceof ParameterPort) && port.isInput()
                    && port.hasToken(0)) {
                Token t = port.get(0);
                String portName = port.getName();
                if (t.getType().equals(BaseType.DOUBLE)) {
                    tokensToBeTransmitted.put(portName,
                            ((DoubleToken) t).doubleValue());
                } else if (t.getType().equals(BaseType.INT)) {
                    tokensToBeTransmitted.put(portName,
                            ((IntToken) t).intValue());
                } else if (t.getType().equals(BaseType.STRING)) {
                    tokensToBeTransmitted.put(portName,
                            ((StringToken) t).stringValue());
                } else if (t.getType().equals(BaseType.FLOAT)) {
                    tokensToBeTransmitted.put(portName,
                            ((FloatToken) t).floatValue());
                } else {
                    tokensToBeTransmitted.put(portName,
                            ((ObjectToken) t).getValue());
                }
            }
        }
        _sendOscPackets(tokensToBeTransmitted);

    }

    public void wrapup() throws IllegalActionException {
        super.wrapup();
        oscP5.stop();
    }

    OscP5 oscP5;
    NetAddress myRemoteLocation;

    private void _constructOscReceiver(int port) throws IllegalActionException {
        oscP5 = new OscP5(this, port);// set port if successful
    }

    private void _sendOscPackets(HashMap<String, Object> tokenMap)
            throws IllegalActionException {

        myRemoteLocation = new NetAddress(_host, _remotePort);

        // initialize an OSC bundle
        OscBundle bundle = new OscBundle();
        String prefix = ((StringToken) tagPrefix.getToken()).stringValue();
        if (!prefix.equals("") && !prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }

        for (Map.Entry<String, Object> entry : tokenMap.entrySet()) {

            String label = entry.getKey();
            Object o = entry.getValue();
            OscMessage m = new OscMessage(prefix + "/" + label);

            if (o instanceof Double) {
                m.add(new Float((double)o));
            } else if (o instanceof Integer) {
                m.add((Integer) o);
            } else if (o instanceof String) {
                m.add((String) o);
            } else {
                throw new IllegalActionException(
                        "Invalid OSC input. "
                                + "Currently this OSC Client  Integer, String and Double types");
            }
            bundle.add(m);
        }

        oscP5.send(bundle, myRemoteLocation);

        tokenMap.clear();
    }

    private int _remotePort = 9999;
    private int _localPort = 56999;
    private String _host = "";

}
