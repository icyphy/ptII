/* An actor that send data to a remote coordinator.

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
import ptolemy.actor.lib.Sink;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TaskReporter

/**
 An actor that sends data to a remote coordinator that implements the
 Coordinator inteface defined in Coordinator.idl.

 Specify the ORB initial property with the<i>ORBInitProperties</i>
 paremerter, for example:
 "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
 where "xyz.eecs.berkeley.edu" is the machine runing name server, and
 "1050" is the port for name service.

 Specify the name of the coordinator with <i>coordinatorName</i> that it wants
 to connect to.

 Specify the name of this client application that this actor belongs to with
 <i>thisClientName</i>

 @author Yang Zhao
 @version $ $
 @since Ptolemy II 3.0
 */
public class TaskReporter extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TaskReporter(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        ORBInitProperties = new Parameter(this, "ORBInitProperties");
        ORBInitProperties.setToken(new StringToken(""));
        coordinatorName = new Parameter(this, "coordinatorName");
        coordinatorName.setToken(new StringToken("TaskCoordinator"));
        thisClientName = new Parameter(this, "thisClientName");
        thisClientName.setToken(new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** the ORB initial property. for example:
     * "-ORBInitialHost xyz.eecs.berkeley.edu -ORBInitialPort 1050"
     */
    public Parameter ORBInitProperties;

    /** The name of the remote coordinator that this actor wants
     *  to connect to. The type of the Parameter
     *  is StringToken.
     */
    public Parameter coordinatorName;

    /** The name represents the client application that the instance of
     * this actor belongs to. The type of the Parameter
     *  is StringToken.
     */
    public Parameter thisClientName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Setup the link to the remote coordinator. This includes creating
     *  the ORB, initializing the naming service and locating the
     *  remote coordinator.
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
        _coordinator = null;
        _initORB(args);
        _clientName = ((StringToken) thisClientName.getToken()).stringValue();
        _debug("Finished initializing " + getName());
    }

    /** send data to the remote coordinator.
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

                        if (_debugging) {
                            _debug(getName(), "Publisher writes \n" + data);
                        }
                    } else {
                        data = token.toString();
                    }

                    org.omg.CORBA.Any event = _orb.create_any();
                    event.insert_string(data);

                    if (_coordinator != null) {
                        _coordinator.result(_clientName, event);
                    }

                    if (_debugging) {
                        _debug(getName(), "Publisher writes " + data);
                    }
                }
            } catch (CorbaIllegalActionException e) {
                //e.printStackTrace();
                throw new IllegalActionException(this,
                        " failed to send result back because: "
                                + e.getMessage());
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
                    ((StringToken) coordinatorName.getToken()).stringValue(),
                    "Multi");
            _debug(getName(), " looking for name: ", coordinatorName.getToken()
                    .toString());

            NameComponent[] path = { namecomp };
            _coordinator = ptolemy.actor.corba.CoordinatorUtil.CoordinatorHelper
                    .narrow(ncRef.resolve(path));
        } catch (UserException ex) {
            //ex.printStackTrace();
            throw new IllegalActionException(
                    this,
                    " initialize ORB failed. Please make sure the "
                            + "naming server has already started and the "
                            + "ORBInitProperty parameter and look up names are "
                            + "configured correctly. "
                            + "the error message is: " + ex.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private ORB _orb;

    //the proxy object of the Coordinator.
    private Coordinator _coordinator;

    //the name of the client application that this actor belongs to.
    private String _clientName;
}
