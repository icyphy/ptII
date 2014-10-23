/* An actor that calls remote implementation via CORBA.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 @ProposedRating Red (liuj)
 @AcceptedRating Red (moderator)
 */
package ptolemy.actor.corba;

import java.util.Iterator;
import java.util.StringTokenizer;

import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.corba.util.CorbaActor;
import ptolemy.actor.corba.util.CorbaIllegalActionException;
import ptolemy.actor.corba.util.CorbaIllegalValueException;
import ptolemy.actor.corba.util.CorbaIndexOutofBoundException;
import ptolemy.actor.corba.util.CorbaUnknownPortException;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CorbaActorClient

/**
 This actor delegate all its executions to a remote actor via
 CORBA. The remote actor can be anything that implements the
 CorbaActor interface defined by the CorbaActor.idl.
 <P>
 When constructing, this actor has no input and no output.
 The ports can be added by the addPort() method (or removed
 by the removePort() method.) Local ports and remote ports
 are binded by their names, such that at run time, the tokens
 sent to a local port is transferred to the remote port with
 the same name.
 <P>
 It has three default parameters, one for name server, one for
 its port, and one for the name of the remote actor. Additional
 parameters can be added to the actor, and those parameters
 will be sent to the remote actor at the initilization phase
 of the execution. Parameters are binded by their names.
 <P>
 At the initialize() phase of the execution, the actor will
 try to create the ORB, and connect to the name server.
 If succeed, it will find the remote actor by looking for its
 name. If this succeeds also, the remote actor is called to be
 located. Then at each firing phase (prefire(), fire() and
 postfire()), this actor will first transfer all the current
 input tokens to the remote actor, and call the corresponding
 methods of the remote actor, then transfer the output
 values. If any of these actions failed, an Exception
 will be thrown.
 <P>
 FIXME: In the current implementation, all the values are
 transferred in the form of a string. This is not type save.
 Further improvement is needed.

 @author Jie Liu
 @version $Id$
 */
public class CorbaActorClient extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  It has neither input port, nor output port.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CorbaActorClient(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInit");
        ORBInitProperties.setToken(new StringToken(""));
        remoteActorName = new Parameter(this, "RemoteActorName");
        remoteActorName.setToken(new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The initialization properties of the ORB. The following
     *  system properties are copied from JDK documents.
     *  <P>
     *  "Currently, the following configuration properties are defined
     *  for all ORB implementations:<BR>
     *  <code>org.omg.CORBA.ORBClass</code>
     *  The name of a Java class that implements the org.omg.CORBA.ORB
     *  interface. Applets and applications do not need to
     *  supply this property unless they must have a particular ORB
     *  implementation. The value for the Java IDL ORB is
     *  com.sun.CORBA.iiop.ORB. <BR>
     *  <code>org.omg.CORBA.ORBSingletonClass </code>
     *  The name of a Java class that implements the org.omg.CORBA.ORB
     *  interface. This is the object returned by a call to
     *  orb.init() with no arguments. It is used primarily to create
     *  typecode instances than can be shared across untrusted code
     *  (such as unsigned applets) in a secured environment.
     *  <P>
     *  In addition to the standard properties listed above, Java IDL
     *  also supports the following properties:
     *  <P>
     *  <code>org.omg.CORBA.ORBInitialHost </code>
     *  The host name of a machine running a server or daemon that
     *  provides initial bootstrap services, such as a name service.
     *  The default value for this property is localhost for applications.
     *  For applets it is the applet host, equivalent to
     *  getCodeBase().getHost(). <BR>
     *  <code>org.omg.CORBA.ORBInitialPort </code>
     *  The port the initial naming service listens to. The default
     *  value is 900."
     *  <P>
     *  The parameters should be constructed in one line of String.
     *  For example:<BR>
     *  <code> -ORBInitialHost moog.eecs.berkeley.edu -ORBInitialPort
     *  1005 </code><BR>
     */
    public Parameter ORBInitProperties;

    /** The name of the remote actor. The type of the Parameter
     *  is StringToken.
     */
    public Parameter remoteActorName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to allow arbitrary type changes
     *  for the variables and parameters.
     *  @exception IllegalActionException If thrown by the base class.
     */
    @Override
    public void attributeTypeChanged(Attribute attribute)
            throws IllegalActionException {
        _typesValid = false; // Set flag to invalidate cached type constraints
        super.attributeTypeChanged(attribute);
    }

    /** Setup the link to the remote actor. This includes creating
     *  the ORB, initializing the naming service, and locating the
     *  remote actor.
     *  @exception IllegalActionException If any of the above actions
     *        failted.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        // String tokenize the parameter ORBInitProperties
        StringTokenizer st = new StringTokenizer(
                ((StringToken) ORBInitProperties.getToken()).stringValue());
        String[] args = new String[st.countTokens()];
        int i = 0;

        while (st.hasMoreTokens()) {
            args[i] = st.nextToken();
            _debug("ORB initial argument: " + args[i]);
            i++;
        }

        try {
            try {
                // start the ORB
                ORB orb = ORB.init(args, null);
                _debug(getName(), " ORB initialized");

                //get the root naming context
                org.omg.CORBA.Object objRef = orb
                        .resolve_initial_references("NameService");
                NamingContext ncRef = NamingContextHelper.narrow(objRef);

                if (ncRef != null) {
                    _debug(getName(), "found name service.");
                }

                //resolve the remote actor reference in Naming
                NameComponent namecomp = new NameComponent(
                        ((StringToken) remoteActorName.getToken())
                        .stringValue(),
                        "");
                _debug(getName(), " looking for name: ", remoteActorName
                        .getToken().toString());

                NameComponent[] path = { namecomp };

                // locate the remote actor
                _remoteActor = ptolemy.actor.corba.util.CorbaActorHelper
                        .narrow(ncRef.resolve(path));

                if (_remoteActor == null) {
                    throw new IllegalActionException(this,
                            " can not find the remote actor.");
                }
            } catch (UserException ex) {
                throw new IllegalActionException(this,
                        " initialize ORB failed." + ex.getMessage());
            }

            //check the corespondence of parameters and ports.
            Iterator attributes = attributeList().iterator();

            while (attributes.hasNext()) {
                Attribute att = (Attribute) attributes.next();

                if (att != ORBInitProperties && att != remoteActorName
                        && att instanceof Parameter) {
                    _debug(getName(), " check remote parameter: ",
                            att.getName());

                    if (!_remoteActor.hasParameter(att.getName())) {
                        throw new IllegalActionException(this, "Parameter: "
                                + att.getName()
                                + " not found on the remote side.");
                    }
                }
            }

            Iterator ports = portList().iterator();

            while (ports.hasNext()) {
                IOPort p = (IOPort) ports.next();
                _debug(getName(), " check remote port: ", p.getName());

                if (!_remoteActor.hasPort(p.getName(), p.isInput(),
                        p.isOutput(), p.isMultiport())) {
                    _debug("Port: " + p.getName()
                            + " not found on the remote side"
                            + " or has wrong type.");
                    throw new IllegalActionException(this, "Port: "
                            + p.getName() + " not found on the remote side"
                            + " or has wrong type.");
                }

                try {
                    _remoteActor
                    .setPortWidth(p.getName(), (short) p.getWidth());
                } catch (UserException ex) {
                    _debug("Port: " + p.getName() + " does not support width");
                    throw new IllegalActionException(this, "Port: "
                            + p.getName() + " does not support width "
                            + p.getWidth());
                }
            }
        } catch (SystemException ex) {
            _debug(getName(), " CORBA set up failed " + ex.getMessage());
            throw new IllegalActionException(this, "CORBA set up faliar"
                    + ex.getMessage());
        }

        _debug("Finished initializing " + getName());
    }

    /** Transfer the input tokens to the remote actor, fire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Director dir = getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }

        try {
            _transferInputs();

            try {
                _remoteActor.fire();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            }

            _transferOutputs();
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Communication Failiar."
                    + ex.getMessage());
        }
    }

    /** Transfer the input tokens to the remote actor, postfire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        Director dir = getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }

        // boolean result;
        try {
            _transferInputs();

            try {
                /* result =*/_remoteActor.postfire();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            }

            _transferOutputs();
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Communication Failiar."
                    + ex.getMessage());
        }

        return super.postfire();
    }

    /** Transfer the input tokens to the remote actor, prefire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean superResults = super.prefire();
        Director dir = getDirector();

        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }

        boolean result;

        try {
            _transferInputs();

            try {
                result = _remoteActor.prefire();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            }

            _transferOutputs();
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Communication Failiar."
                    + ex.getMessage());
        }

        return result && superResults;
    }

    /** Wrapup the remote actor.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        try {
            if (_remoteActor != null) {
                // copernicus.kernel.KernelMain.generateCode() calls wrapup()
                // so we need to be sure that _remoteActor is non-null
                _remoteActor.prefire();
            }
        } catch (CorbaIllegalActionException ex) {
            throw new IllegalActionException(this,
                    "remote actor throws IllegalActionException"
                            + ex.getMessage());
        } catch (SystemException ex) {
            throw new InvalidStateException(this, "Communication Failiar."
                    + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Transfer the input tokens to the remote actor.
     *  This is done by converting the local token to String and
     *  transfer the string to the remote actor.
     *  FIXME: This operation is not type-save. Consider better
     *         ways to do it.
     *  @exception IllegalActionException If the port names do not
     *      match, or the tokens values are invalid.
     *  FIXME: How to set width?
     */
    protected void _transferInputs() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();

        while (inputPorts.hasNext()) {
            IOPort port = (IOPort) inputPorts.next();
            String inputName = port.getName();

            for (short i = 0; i < port.getWidth(); i++) {
                if (port.hasToken(i)) {
                    Token inputToken = port.get(0);

                    try {
                        _remoteActor.transferInput(inputName, i,
                                inputToken.toString());
                    } catch (SystemException ex) {
                        throw new InvalidStateException(this,
                                "Communication failiar." + ex.getMessage());
                    } catch (CorbaIllegalActionException ex1) {
                        throw new IllegalActionException(this,
                                "Illegal Action on remote actor. "
                                        + ex1.getMessage());
                    } catch (CorbaUnknownPortException ex2) {
                        throw new IllegalActionException(this,
                                "Wrong port name. " + ex2.getMessage());
                    } catch (CorbaIndexOutofBoundException ex3) {
                        throw new IllegalActionException(this, port,
                                "Channel index out of bound. "
                                        + ex3.getMessage());
                    } catch (CorbaIllegalValueException ex4) {
                        throw new IllegalActionException(port,
                                "contains illegal token value. "
                                        + ex4.getMessage());
                    }
                }
            }
        }
    }

    /** Transfer the output data from the remote actor.
     *  The remote output data is transferred in the form of
     *  String. This method converts the String back to
     *  Token and sends them.
     *  FIXME: This operation is not type-save. Consider better
     *         ways to do it.
     *  @exception IllegalActionException If the port names do not
     *      match, or the tokens values are invalid.
     *  FIXME: how to set width?
     */
    protected void _transferOutputs() throws IllegalActionException {
        Iterator outputPorts = outputPortList().iterator();

        while (outputPorts.hasNext()) {
            IOPort port = (IOPort) outputPorts.next();
            String portName = port.getName();

            for (short i = 0; i < port.getWidth(); i++) {
                try {
                    if (_remoteActor.hasData(portName, i)) {
                        String returndata = _remoteActor.transferOutput(
                                portName, i);

                        //FIXME: type?
                        DoubleToken outputToken = new DoubleToken(returndata);
                        port.send(i, outputToken);
                    }
                } catch (SystemException ex) {
                    throw new InvalidStateException(this,
                            "Communication failiar." + ex.getMessage());
                } catch (CorbaIllegalActionException ex1) {
                    throw new IllegalActionException(this,
                            "Illegal Action on remote actor. "
                                    + ex1.getMessage());
                } catch (CorbaUnknownPortException ex2) {
                    throw new IllegalActionException(this, "Unknow port name"
                            + portName + ex2.getMessage());
                } catch (CorbaIndexOutofBoundException ex3) {
                    throw new IllegalActionException(this, port,
                            "channel index out of bound. " + ex3.getMessage());
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private CorbaActor _remoteActor;
}
