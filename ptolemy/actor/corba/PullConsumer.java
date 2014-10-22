/* An actor that sends request to the data supplier for data.

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
import ptolemy.actor.corba.CorbaIOUtil.pullSupplier;
import ptolemy.actor.lib.Source;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// Publisher

/**
 An actor that sends pull request to a remote publisher and asks for data.

 Specify the ORB initial property with the<i>ORBInitProperties<i>
 paremerter, for example:
 "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
 where "xyz.eecs.berkeley.edu" is the machine runing name server, and
 "1050" is the port for name service.

 Specify the name of the supplier with <i>remoteSupplierName<i>, which is
 the supplier it wants to request data from.

 If the <i>blocking<i> parameter is true, then wait until there is
 token received, otherwise, send a default value specified by the
 <i>defaultToken<i> parameter. Notice that the type of the output port
 is determined by the type of this parameter.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 1.0
 */
public class PullConsumer extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PullConsumer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInitProperties");
        ORBInitProperties.setToken(new StringToken(""));
        remoteSupplierName = new Parameter(this, "remoteSupplierName");
        remoteSupplierName.setToken(new StringToken(""));
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
    public Parameter ORBInitProperties;

    /** The name of the remote supplier. The type of the Parameter
     *  is StringToken.
     */
    public Parameter remoteSupplierName;

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
     *  the ORB, initializing the naming service, and locating the
     *  remote supplier.
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
        _requestData = false;
        _lastReadToken = null;
        _fireIsWaiting = false;
        _defaultToken = defaultToken.getToken();
        _dataReadingThread = new DataReadingThread();
        _dataReadingThread.start();
        _debug("Finished initializing " + getName());
    }

    /** Set the request data flag to be true at each firing. If it
     *  is blocking and now new data received since the last firing,
     *  then wait until there is data coming, otherwise send
     *  the default token.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            synchronized (_lock) {
                _requestData = true;
                _lock.notifyAll();
            }

            for (int i = 0; i < trigger.getWidth(); i++) {
                if (trigger.hasToken(i)) {
                    trigger.get(i);
                }
            }

            if (_blocking) {
                if (_lastReadToken == null) {
                    try {
                        synchronized (_fire) {
                            if (_debugging) {
                                _debug(getName(), " is waiting.");
                            }

                            _fireIsWaiting = true;
                            _fire.wait();
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
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(this,
                    "remote actor throws IllegalActionException"
                            + ex.getMessage());
        }
    }

    /** Request that execution of the current iteration stop as soon
     *  as possible. Wake up the waiting if there is any.
     */
    @Override
    public void stop() {
        if (_fireIsWaiting) {
            synchronized (_fire) {
                _fire.notifyAll();
            }

            _fireIsWaiting = false;
        }

        super.stop();
    }

    /** interrupt the thread that is pulling data from the remote supplier.
     *
     */
    @Override
    public void wrapup() throws IllegalActionException {
        if (_dataReadingThread != null) {
            _dataReadingThread.interrupt();
            _dataReadingThread = null;
        } else {
            if (_debugging) {
                _debug("ReadingThread null at wrapup!?");
            }
        }
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
                    ((StringToken) remoteSupplierName.getToken()).stringValue(),
                    "");
            _debug(getName(), " looking for name: ", remoteSupplierName
                    .getToken().toString());

            NameComponent[] path = { namecomp };

            // locate the remote actor
            _remoteSupplier = ptolemy.actor.corba.CorbaIOUtil.pullSupplierHelper
                    .narrow(ncRef.resolve(path));

            if (_remoteSupplier == null) {
                throw new IllegalActionException(this,
                        " can not find the remote supplier.");
            }
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

    private pullSupplier _remoteSupplier;

    private boolean _requestData;

    // The indicator the last read serial number
    private Token _lastReadToken;

    private DataReadingThread _dataReadingThread;

    private Object _lock = new Object();

    private Object _fire = new Object();

    private boolean _fireIsWaiting;

    private boolean _blocking;

    private Token _defaultToken;

    ///////////////////////////////////////////////////////////////////
    ////                        private inner class                ////
    private class DataReadingThread extends Thread {
        /** Constructor.  Create a new thread pull data from a remote supplier.
         *
         */
        public DataReadingThread() {
        }

        /** Run.  Run the thread.  This begins running when .start()
         *  is called on the thread.
         *  If not data request issued, then wait, otherwise pull data
         *  from the supplier. When receive data and fire is waiting
         *  (blocking), wake up fire to send the data to its output. If
         *  fire is not waiting, then save the data for next firing.
         */
        @Override
        public void run() {
            while (true) {
                try {
                    synchronized (_lock) {
                        if (!_requestData) {
                            _lock.wait();
                        }
                    }

                    if (_requestData) {
                        org.omg.CORBA.Any data = _remoteSupplier.pull();

                        if (data != null) {
                            Variable variable = new Variable();
                            variable.setExpression(data.extract_string());
                            _lastReadToken = variable.getToken();

                            if (_debugging) {
                                _debug(getName(), "gets " + data);
                            }

                            _requestData = false;

                            if (_fireIsWaiting) {
                                synchronized (_fire) {
                                    _fire.notifyAll();
                                }
                            }
                        }
                    }
                } catch (CorbaIllegalActionException e) {
                    //fixme what should it throw here.
                } catch (InterruptedException ex) {
                } catch (IllegalActionException ex) {
                }
            }
        }
    }
}
