/* An actor that coornidator a set of clients to work for some tasks.

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

 @ProposedRating Yellow (liuj)
 @AcceptedRating Yellow (janneck)
 */
package ptolemy.actor.corba;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import ptolemy.actor.corba.CoordinatorUtil.Client;
import ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException;
import ptolemy.actor.corba.CoordinatorUtil._CoordinatorImplBase;
import ptolemy.actor.lib.Transformer;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PushSupplier

/**
 An actor that coordinator a set of clients connecting to it to work together
 to finish some tasks.

 It has an inner class that implements the Coordinator interface defined in
 Coordinator.idl. The clients connect to this need to implement the Client
 interface defined in Coordinator.idl also.

 Specify the ORB initial property with the<i>ORBInitProperties</i>
 paremerter, for example:
 "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
 where "xyz.eecs.berkeley.edu" is the machine runing name server, and
 "1050" is the port for name service.

 Specify the name of the coordinator with <i>coordinatorName</i> that it wants
 to register to the name service.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 */
public class TaskCoordinator extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TaskCoordinator(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInitProperties");
        ORBInitProperties.setToken(new StringToken(""));
        coordinatorName = new Parameter(this, "coordinatorName");
        coordinatorName.setToken(new StringToken("TaskCoordinator"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** the ORB initial property. for example:
     * "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
     */
    public Parameter ORBInitProperties;

    /** The name of the coordinator to register with the naming service.
     *  The type of the Parameter is StringToken.
     */
    public Parameter coordinatorName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize this actor. This includes creating
     *  the ORB, initializing the naming service, instantiate and register
     *  the coordinator.
     *  @exception IllegalActionException If any of the above actions
     *  failted.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _clientRefs.clear();
        _availableClients.clear();
        _lock1 = new Object();
        _lock2 = new Object();
        _fireIsWaiting = false;

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

        _orb = null;
        _initORB(args);
        _debug("Finished initializing " + getName());
    }

    /** Read one input token, if there is one, from the input
     *  and send it to the remote TaskReceiver by call the stub method.
     *  If there is no input token, then send received result to the
     *  output port.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < input.getWidth(); i++) {
            try {
                if (input.hasToken(0)) {
                    Token token = input.get(0);
                    String data;

                    if (token instanceof StringToken) {
                        data = ((StringToken) token).stringValue();
                    } else {
                        data = token.toString();
                    }

                    //prepare to send the token to task Receivers.
                    org.omg.CORBA.Any event = _orb.create_any();
                    event.insert_string(data);

                    Client selectedClient;

                    if (_availableClients.size() > 0) {
                        synchronized (_lock2) {
                            selectedClient = (Client) _availableClients
                                    .removeFirst();
                        }

                        selectedClient.push(event);

                        if (_debugging) {
                            _debug(getName(), "coordinator sends new task: "
                                    + data);
                        }
                    } else { //no worker is availabe, so wait.

                        synchronized (_lock1) {
                            if (_debugging) {
                                _debug(getName(), " is waiting.");
                            }

                            _fireIsWaiting = true;
                            _lock1.wait();
                            _fireIsWaiting = false;

                            if (_debugging) {
                                _debug(getName(), " wake up.");
                            }
                        }

                        if (_availableClients.size() > 0) {
                            synchronized (_lock2) {
                                selectedClient = (Client) _availableClients
                                        .removeFirst();
                            }

                            selectedClient.push(event);

                            if (_debugging) {
                                _debug(getName(),
                                        "coordinator sends new task: " + data);
                            }
                        }
                    }
                } else { // we enter fire due to receiving returned result from some client.
                    _debug(getName(),
                            "coordinator send out received task result.");
                    output.send(0, _resultToken);
                }
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                                + ex.getMessage());
            } catch (InterruptedException e) {
                throw new IllegalActionException(this, "blocking interrupted."
                        + e.getMessage());
            }
        }
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible. Wake up the waiting if there is any.
     */
    @Override
    public void stop() {
        if (_fireIsWaiting) {
            synchronized (_lock1) {
                _lock1.notifyAll();
            }

            _fireIsWaiting = false;
        }

        super.stop();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    //use a private method to deal with necessary CORBA operations.
    // @exception IllegalActionException If ORB initialize failed.
    private void _initORB(String[] args) throws IllegalActionException {
        try {
            // start the ORB
            _orb = ORB.init(args, null);
            _debug(getName(), " ORB initialized");

            //get the root naming context
            org.omg.CORBA.Object objRef = _orb
                    .resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);

            if (ncRef != null) {
                _debug(getName(), "found name service.");
            }

            //resolve the remote consumer reference in Naming
            NameComponent namecomp = new NameComponent(
                    ((StringToken) coordinatorName.getToken()).stringValue(),
                    "Multi");
            _debug(getName(), " looking for name: ", coordinatorName.getToken()
                    .toString());

            NameComponent[] path = { namecomp };
            Coordinator _coordinator = new Coordinator();
            _orb.connect(_coordinator);
            ncRef.rebind(path, _coordinator);
        } catch (UserException ex) {
            //ex.printStackTrace();
            throw new IllegalActionException(
                    this,
                    " initialize ORB failed. Please make sure the "
                            + "naming server has already started and the "
                            + "ORBInitProperty parameter is configured correctly. "
                            + "the error message is: " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private ORB _orb;

    private HashMap _clientRefs = new HashMap();

    private LinkedList _availableClients = new LinkedList();

    private boolean _fireIsWaiting;

    private Object _lock1;

    private Object _lock2;

    private Token _resultToken;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    @SuppressWarnings("serial")
    private class Coordinator extends _CoordinatorImplBase {
        /**
         * Construct a Coordinator.
         */
        public Coordinator() {
            super();
        }

        @Override
        public void register(String clientName, Client clientRef)
                throws CorbaIllegalActionException {
            synchronized (_lock2) {
                if (!_clientRefs.containsKey(clientName)) {
                    _clientRefs.put(clientName, clientRef);
                    _availableClients.add(clientRef);

                    if (_fireIsWaiting) {
                        synchronized (_lock1) {
                            _lock1.notifyAll();
                        }
                    }
                }
            }
        }

        @Override
        public void result(String clientName, Any data)
                throws CorbaIllegalActionException {
            //FIXME: this only works for string result data.
            _resultToken = new StringToken(data.extract_string());

            if (_debugging) {
                _debug(getName(), " receive data:\n" + _resultToken.toString());
            }

            synchronized (_lock2) {
                _debug(getName(), "get synchronized object lock2.");

                Client client = (Client) _clientRefs.get(clientName);

                if (client != null) {
                    _availableClients.add(client);
                    _debug(getName(), "free the client back to be available.");

                    if (_fireIsWaiting) {
                        synchronized (_lock1) {
                            _debug(getName(), "get synchronized object lock1.");
                            _lock1.notifyAll();
                        }
                    }
                }
            }

            try {
                getDirector().fireAtCurrentTime(TaskCoordinator.this);
            } catch (IllegalActionException ex) {
                throw new CorbaIllegalActionException(
                        "failed in dealing with director.");
            }
        }

        @Override
        public void unregister(String clientName)
                throws CorbaIllegalActionException {
            synchronized (_lock2) {
                if (_clientRefs.containsKey(clientName)) {
                    Client client = (Client) _clientRefs.get(clientName);
                    _availableClients.remove(client);
                    _clientRefs.remove(clientName);
                }
            }
        }
    }
}
