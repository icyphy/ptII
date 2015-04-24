/* An actor that sends data in response to a pull request.

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
import ptolemy.actor.corba.CorbaIOUtil._pullSupplierImplBase;
import ptolemy.actor.lib.Sink;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// PullPublisher

/**
 An actor that send data to a remote consumer when there is pull request.

 Specify the ORB initial property with the<i>ORBInitProperties</i>
 paremerter, for example:
 "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
 where "xyz.eecs.berkeley.edu" is the machine runing name server, and
 "1050" is the port for name service.

 Specify the name of the supplier with <i>SupplierName</i>, which is
 registered on the name server.

 This actor can only be used in the CI domain currently. It is an
 active actor, and has an seperate thread to guard its execution.
 when the manage thread call its prefire, it will wait if there is
 no pull request, otherwise it pull data from its providers, and
 send the data to satisfy the request.
 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 1.0
 */
public class PullSupplier extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PullSupplier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInitProperties");
        ORBInitProperties.setToken(new StringToken(""));
        supplierName = new Parameter(this, "supplierName");
        supplierName.setToken(new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public Parameter ORBInitProperties;

    /** The name of the supplier. The type of the Parameter
     *  is StringToken.
     */
    public Parameter supplierName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Setup the link to the remote consumer. This includes creating
     *  the ORB, initializing the naming service, and register the
     *  supplier.
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
        _pullIsWaiting = false;
        _prefireIsWaiting = false;
    }

    /** Read one input token, if there is one, from the input
     *  and notify the thread that is pulling for data.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            _lastReadToken = input.get(0);

            synchronized (_pullThread) {
                _pullThread.notifyAll();
            }
        }
    }

    /** cause the calling thread to wait if there is
     *  no pull request. Return true if the <i>input</i>
     *  port has token, otherwise return false.
     *  @exception IllegalActionException should never be throwed
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        boolean returnValue = super.prefire();
        if (!_pullIsWaiting) {
            try {
                synchronized (_lock) {
                    if (_debugging) {
                        _debug(getName(), " is waiting.");
                    }

                    _prefireIsWaiting = true;
                    _lock.wait();
                    _prefireIsWaiting = false;

                    if (_debugging) {
                        _debug(getName(), " wake up.");
                    }
                }
            } catch (InterruptedException e) {
                throw new IllegalActionException(this, "blocking interrupted."
                        + e.getMessage());
            }
        }

        if (_debugging) {
            _debug(getName(), "_pullIsWaiting = " + _pullIsWaiting);
        }

        if (_pullIsWaiting) {
            boolean b = input.hasToken(0);

            if (_debugging) {
                _debug(getName(), "hasToken = " + b);
            }

            return b && returnValue;
        }

        return false;
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible. Wake up the waiting if there is any.
     */
    @Override
    public void stop() {
        if (_prefireIsWaiting) {
            synchronized (_lock) {
                _lock.notifyAll();
            }

            _prefireIsWaiting = false;
        }

        if (_pullIsWaiting) {
            synchronized (_pullThread) {
                _pullThread.notifyAll();
            }

            _pullIsWaiting = false;
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

            _supplier = new pullSupplier();
            _orb.connect(_supplier);

            //registe the consumer with the given name
            NameComponent namecomp = new NameComponent(
                    ((StringToken) supplierName.getToken()).stringValue(), "");
            _debug(getName(), " register the consumer with name: ",
                    supplierName.getToken().toString());

            NameComponent[] path = { namecomp };
            ncRef.rebind(path, _supplier);
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

    //An instance of pullSupplier this actor create and registered.
    private pullSupplier _supplier;

    // The flag for indicating whether there is pull request waiting.
    private boolean _pullIsWaiting;

    // The indicator for the last read token from its input.
    private Token _lastReadToken;

    // The lock that monitors the pullinging thread and the
    // prefire() of this actor.
    private Object _lock = new Object();

    // The lock that monitors the pullinging thread and the
    // fire() of this actor.
    private Object _pullThread = new Object();

    //// The flag for indicating whether prefire() is waiting.
    private boolean _prefireIsWaiting;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    @SuppressWarnings("serial")
    private class pullSupplier extends _pullSupplierImplBase {
        /**
         * Construct a pullSupplier.
         */
        public pullSupplier() {
            super();
        }

        /**
         * Implement the pull() method defined in the pullSupplier intefece.
         * When the stub method is called, the call is trasfered by ORB to here,
         * and the pull request is propagated to the data source. The calling
         * thread will wait until there is data returned.
         *
         */
        @Override
        public org.omg.CORBA.Any pull() throws CorbaIllegalActionException {
            try {
                if (_lastReadToken == null) {
                    if (_debugging) {
                        _debug(getName(),
                                "no token to return, so pull will wait.");
                    }

                    synchronized (_pullThread) {
                        if (_debugging) {
                            _debug(getName(), "pull() is waiting.");
                        }

                        _pullIsWaiting = true;

                        if (_prefireIsWaiting) {
                            if (_debugging) {
                                _debug(getName(),
                                        "pull for data and wake up prefire().");
                            }

                            synchronized (_lock) {
                                if (_debugging) {
                                    _debug(getName(), "notify prefire().");
                                }

                                _lock.notifyAll();
                            }
                        }

                        _pullThread.wait();
                        _pullIsWaiting = false;

                        if (_debugging) {
                            _debug(getName(), "pull() wake up.");
                        }
                    }
                }

                if (_lastReadToken != null) {
                    if (_debugging) {
                        _debug(getName(), "return requested data.");
                    }

                    org.omg.CORBA.Any event = _orb.create_any();
                    event.insert_string(_lastReadToken.toString());
                    _lastReadToken = null;
                    return event;
                }

                return null;
            } catch (InterruptedException e) {
                throw new InternalErrorException("pull method interrupted."
                        + e.getMessage());
            }
        }
    }
}
