/* An actor that receives data from a remote coordinator.

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

import java.util.StringTokenizer;

import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

import ptolemy.actor.corba.CoordinatorUtil.Coordinator;
import ptolemy.actor.corba.CoordinatorUtil.CorbaIllegalActionException;
import ptolemy.actor.corba.CoordinatorUtil._ClientImplBase;
import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TaskReceiver

/**
 An actor that register itself to a romote data provide that implements the
 Coordinator inteface and receives data from it.

 Specify the ORB initial property with the<i>ORBInitProperties<i>
 paremerter, for example:
 "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
 where "xyz.eecs.berkeley.edu" is the machine runing name server, and
 "1050" is the port for name service.

 Specify the name of the coordinator with <i>coordinatorName<i>, which is
 registered on the name server.

 Specify whether the actor blocks when it haven't receive data with
 the <i>blocking<i> parameter.

 Specify the name of this client with <i>thisClientName<i>.

 See TaskCoordinator.java for the implementation of the coordinator.

 @author Yang Zhao
 @version $$
 @since Ptolemy II 3.0
 */
public class TaskReceiver extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TaskReceiver(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInitProperties");
        ORBInitProperties.setToken(new StringToken(""));
        coordinatorName = new Parameter(this, "coordinatorName");
        coordinatorName.setToken(new StringToken("TaskCoordinator"));
        thisClientName = new Parameter(this, "thisClientName");
        thisClientName.setToken(new StringToken(""));
        blocking = new Parameter(this, "blocking");
        blocking.setTypeEquals(BaseType.BOOLEAN);
        blocking.setExpression("true");
        defaultToken = new Parameter(this, "defaultToken");
        defaultToken.setExpression("0.0");

        //defaultToken.setTypeEquals(BaseType.DOUBLE);
        output.setTypeEquals(defaultToken.getType());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** the ORB initial property. for example:
     * "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
     */
    public Parameter ORBInitProperties;

    /** The name of the coordinator. The type of the Parameter
     *  is StringToken.
     */
    public Parameter coordinatorName;

    /** The name represents the client application that the instance of
     * this actor belongs to. The type of the Parameter
     *  is StringToken.
     */
    public Parameter thisClientName;

    /** Indicate whether the actor blocks when it haven't receive
     *  data. The default value is false of
     *  type BooleanToken.
     */
    public Parameter blocking;

    /** The default token. If the actor is nonblocking
     *  and there is no new token received after the last fire()
     *  method call, then this
     *  token will be output when the fire() method is called.
     *  The default value is 0.0. And the default type is
     *  double. Notice that the type of the output port
     *  is determined by the type of this parameter.
     */
    public Parameter defaultToken;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>blocking</i> update the local
     *  cache of the parameter value, else if the attribute is
     *  <i>defaultToken<i> update the type of the output token to
     *  the be the same type of defaultToken, otherwise pass the
     *  call to the super class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == blocking) {
            _blocking = ((BooleanToken) blocking.getToken()).booleanValue();
        } else if (attribute == defaultToken) {
            output.setTypeEquals(defaultToken.getType());
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Setup the link to the remote coordinator. This includes creating
     *  the ORB, initializing the naming service, and resolve the
     *  coordinator from the naming context.
     *  @exception IllegalActionException If any of the above actions
     *  failted.
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

        _orb = null;
        _client = null;
        _coordinator = null;
        _initORB(args);
        _debug("Finished initializing " + getName());
        _lastReadToken = null;
        _fireIsWaiting = false;
        _defaultToken = defaultToken.getToken();
    }

    /** Read the received data if there is any and send to the output.
     *  If no data received, then wait if <i>blocking<i> is true, or
     *  a default token is sent.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }

        if (_blocking) {
            if (_lastReadToken == null) {
                try {
                    synchronized (_lock) {
                        if (_debugging) {
                            _debug(getName(), " is waiting.");
                        }

                        _fireIsWaiting = true;
                        _lock.wait();
                        _fireIsWaiting = false;

                        if (_debugging) {
                            _debug(getName(), " wake up.");
                        }
                    }
                } catch (InterruptedException e) {
                    throw new IllegalActionException(this,
                            "blocking interrupted." + e.getMessage());
                }
            }

            if (_lastReadToken != null) {
                output.send(0, _lastReadToken);
                _defaultToken = _lastReadToken;
                _lastReadToken = null;
            }
        } else {
            output.send(0, _defaultToken);
        }
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible. Wake up the waiting if there is any and unregister
     *  the Client object from the coordinator.
     */
    @Override
    public void stop() {
        if (_coordinator != null) {
            try {
                _coordinator.unregister(((StringToken) thisClientName
                        .getToken()).stringValue());
            } catch (CorbaIllegalActionException ex) {
                //e.printStackTrace();
                throw new KernelRuntimeException(this,
                        " failed to unregister itself from the remote "
                                + " TaskCoordinator. "
                                + " the error message is: " + ex.getMessage());
            } catch (IllegalActionException e) {
                throw new KernelRuntimeException(this,
                        " gets an error when it tries to get the string value"
                                + " from the thisClientName parameter. "
                                + " the error message is: " + e.getMessage());
            }
        }

        if (_fireIsWaiting) {
            synchronized (_lock) {
                _lock.notifyAll();
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
            _coordinator = ptolemy.actor.corba.CoordinatorUtil.CoordinatorHelper
                    .narrow(ncRef.resolve(path));
            _client = new Client();
            _orb.connect(_client);

            if (_coordinator != null) {
                _coordinator
                .register(((StringToken) thisClientName.getToken())
                        .stringValue(), _client);
            }

            //registe the consumer with the given name
        } catch (UserException ex) {
            throw new IllegalActionException(this,
                    " Initialize ORB failed. Please make sure the "
                            + "naming server has already started and the "
                            + "ORBInitProperty parameter and look up names are"
                            + " configured correctly. "
                            + "the error message is: " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private ORB _orb;

    // the Client objecte to commute with the remote Coordinator object.
    private Client _client;

    // the proxy object of the Coordinator.
    private Coordinator _coordinator;

    // Cached value of whether blocks when no data.
    private boolean _blocking;

    // The indicator for the last received data.
    private Token _lastReadToken;

    //The indicator for the default Token.
    private Token _defaultToken;

    // The lock that monitors the push() method and the fire() method.
    private Object _lock = new Object();

    //The flag indicates wheather the fire() method is waiting.
    private boolean _fireIsWaiting;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////

    /** this inner class implements the Client interface defined in
     * Coordinator.idl.
     */
    @SuppressWarnings("serial")
    private class Client extends _ClientImplBase {
        /**
         * Construct a pushConsumer.
         */
        public Client() {
            super();
        }

        /**
         * Implement the push() method defined in the Client intefece.
         * When the stub method is called, the call is trasfered by ORB to here,
         * and the data is saved in <i>_lastReadToken<i>. If fire() is waiting
         * for new data, then wake up fire(), otherwise call fireAt.
         * //FIXME: need to deal with overwrite if the old data is not consumed.
         */
        @Override
        public void push(org.omg.CORBA.Any data)
                throws CorbaIllegalActionException {
            if (_debugging) {
                _debug("got pushed data");
            }

            //try {
            synchronized (_lock) {
                // Variable variable = new Variable();
                if (_debugging) {
                    _debug("got pushed data:\n" + data.extract_string());
                }

                // variable.setExpression( data.extract_string());
                ////String string = variable.getExpression();
                //_lastReadToken = variable.getToken();
                _lastReadToken = new StringToken(data.extract_string());

                if (_debugging) {
                    _debug(getName(),
                            " receive data:\n" + _lastReadToken.toString());
                }

                if (_fireIsWaiting) {
                    _lock.notifyAll();
                } else {
                    try {
                        getDirector().fireAtCurrentTime(TaskReceiver.this);
                    } catch (IllegalActionException ex) {
                        throw new CorbaIllegalActionException(
                                "failed in dealing with director.");
                    }
                }
            }

            //} catch (IllegalActionException e) {
            //    throw new CorbaIllegalActionException("failed to construct.");
            //}
        }

        /* (non-Javadoc)
         * @see ptolemy.actor.corba.CoordinatorUtil.ClientOperations#start()
         */
        @Override
        public void start() {
            // TODO Auto-generated method stub
        }

        /* (non-Javadoc)
         * @see ptolemy.actor.corba.CoordinatorUtil.ClientOperations#stop()
         */
        @Override
        public void stop() {
        }
    }
}
