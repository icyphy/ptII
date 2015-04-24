/* An actor that receive data from a remote publisher.

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

import ptolemy.actor.corba.CorbaIOUtil.CorbaIllegalActionException;
import ptolemy.actor.corba.CorbaIOUtil._pushConsumerImplBase;
import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PushConsumer

/**
 An actor that receives data from a remote publisher.
 Specify the ORB initial property with the<i>ORBInitProperties</i>
 paremerter, for example:
 "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
 where "xyz.eecs.berkeley.edu" is the machine runing name server, and
 "1050" is the port for name service.

 Specify the name of the consumer with <i>ConsumerName</i>, which is
 registered on the name server.

 If the <i>blocking</i> paremerter is true, then wait until there is
 token received, otherwise, send a default value specified by the
 <i>defaultToken</i> paremerter. Notice that the type of the output port
 is determined by the type of this parameter.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 1.0
 */
public class PushConsumer extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PushConsumer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInitProperties");
        ORBInitProperties.setToken(new StringToken(""));
        consumerName = new Parameter(this, "consumerName");
        consumerName.setToken(new StringToken(""));
        blocking = new Parameter(this, "blocking");
        blocking.setTypeEquals(BaseType.BOOLEAN);
        blocking.setExpression("false");
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

    /** The name of the consumer. The type of the Parameter
     *  is StringToken.
     */
    public Parameter consumerName;

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
     *  cache of the parameter value, otherwise pass the call to
     *  the super class.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == blocking) {
            _blocking = ((BooleanToken) blocking.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Setup the link to the remote consumer. This includes creating
     *  the ORB, initializing the naming service, and register the
     *  consumer.
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
        _initORB(args);
        _debug("Finished initializing " + getName());
        _lastReadToken = null;
        _fireIsWaiting = false;
        _defaultToken = defaultToken.getToken();
    }

    /** Read the received data if there is any and send to the output.
     *  If no data received, then wait if <i>blocking</i> is true, or
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
     *  as possible. Wake up the waiting if there is any.
     */
    @Override
    public void stop() {
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

            _consumer = new pushConsumer();
            _orb.connect(_consumer);

            //registe the consumer with the given name
            NameComponent namecomp = new NameComponent(
                    ((StringToken) consumerName.getToken()).stringValue(), "");
            _debug(getName(), " register the consumer with name: ",
                    consumerName.getToken().toString());

            NameComponent[] path = { namecomp };
            ncRef.rebind(path, _consumer);
        } catch (UserException ex) {
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

    //An instance of pushConsumer this actor create and registered.
    private pushConsumer _consumer;

    // Cached value of whether blocks when no data.
    private boolean _blocking;

    // The indicator for the last received data.
    private Token _lastReadToken;

    //The indicator for the default Token.
    private Token _defaultToken;

    // The lock that monitors the reading thread and the fire() thread.
    private Object _lock = new Object();

    //The flag indicates wheather the fire() method is waiting.
    private boolean _fireIsWaiting;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    @SuppressWarnings("serial")
    private class pushConsumer extends _pushConsumerImplBase {
        /**
         * Construct a pushConsumer.
         */
        public pushConsumer() {
            super();
        }

        /**
         * Implement the push() method defined in the pushConsumer intefece.
         * When the stub method is called, the call is trasfered by ORB to here,
         * and the data is saved in <i>_lastReadToken</i>. If fire() is waiting
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

                //FIXME: This was designed to be able to receive
                //differnt kinds of tokens. But after some change was
                //made in StringUtil, the string seems not substituted
                //properly, and it gets error when call variable.getToken.
                //So I modified it here to only deal with sting tokens
                //and use it to receive moml strings for the mobile model.
                //variable.setExpression( data.extract_string());
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
                        getDirector().fireAtCurrentTime(PushConsumer.this);
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
    }
}
