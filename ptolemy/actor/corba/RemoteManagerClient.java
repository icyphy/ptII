/* An actor that calls remote implementation via CORBA.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (moderator@eecs.berkeley.edu)
*/

package ptolemy.actor.corba;

import ptolemy.actor.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.kernel.Port;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.net.URL;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import ptolemy.actor.corba.RemoteManagerUtil.*;
//import ptolemy.actor.corba.RemoteManagerUtil.RemoteManager;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

//////////////////////////////////////////////////////////////////////////
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
sent to a local port is transfered to the remote port with
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
transfered in the form of a string. This is not type save.
Further improvement is needed.

@author Jie Liu
@version $Id$
*/

public class RemoteManagerClient extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  It has neither input port, nor output port.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RemoteManagerClient(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        trigger = new TypedIOPort(this, "trigger", true, false);
        //start = new TypedIOPort(this, "start", true, false);
        //stop = new TypedIOPort(this, "stop", true, false);
        ORBInitProperties  = new Parameter(this, "ORBInit");
        ORBInitProperties.setToken(new StringToken(""));
        remoteManager = new Parameter(this, "RemoteManager");
        remoteManager.setToken(new StringToken(""));
        controllerURL = new Parameter(this, "controllerURL");
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
     *  <code> -ORBInitialHost bennett.eecs.berkeley.edu -ORBInitialPort
     *  1005 </code><BR>
     */
    public Parameter ORBInitProperties;

    /** The name of the remote actor. The type of the Parameter
     *  is StringToken.
     */
    public Parameter remoteManager;

    public Parameter controllerURL;

    public TypedIOPort trigger = null;

    //public TypedIOPort start = null;

    //public TypedIOPort stop = null;


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change of the _director or other property. */
    public void attributeChanged(Attribute attribute)
        throws IllegalActionException {
        if (attribute == controllerURL) {
            try {
                StringToken URLToken = (StringToken)controllerURL.getToken();
                if (URLToken == null) {
                    System.out.println("### please provide the URL of the model. \n");

                } else {
                    _source = URLToken.stringValue();
                    if (!_source.equals("")) {
                        URL url = new URL(_source);
                        InputStream is = url.openStream();
                        BufferedInputStream in = new BufferedInputStream(is);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        StringBuffer modelText = new StringBuffer();
                        String newline = System.getProperty("line.separator");
                        while (true) {
                            String line = reader.readLine();
                            if (line == null) break;
                            modelText = modelText.append(line);
                            modelText = modelText.append(newline);
                        }
                        _modelString = modelText.toString();
                        //System.out.println("--- the URL of the model file: " + url.toString() + "\n");
                    }
                    else {
                        System.out.println("### please provide the URL of the model. \n");

                }

            }

            } catch (Exception ex) {
                throw new IllegalActionException(this, ex.getMessage());
            }
        }
        super.attributeChanged(attribute);
    }

    /** Override the base class to allow arbitrary type changes
     *  for the variables and parameters.
     */
    public void attributeTypeChanged(Attribute attribute) {
    }

    /** Setup the link to the remote actor. This includes creating
     *  the ORB, initializing the naming service, and locating the
     *  remote actor.
     *  @exception IllegalActionException If any of the above actions
     *        failted.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        this.addDebugListener(new StreamListener());
        // String tokenize the parameter ORBInitProperties
        StringTokenizer st = new StringTokenizer(
                ((StringToken)ORBInitProperties.getToken()).stringValue());
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
                org.omg.CORBA.Object objRef = orb.resolve_initial_references(
                        "NameService");
                NamingContext ncRef = NamingContextHelper.narrow(objRef);
                if (ncRef != null) {
                    _debug(getName(), "found name service.");
                }
                //resolve the remote actor reference in Naming
                NameComponent namecomp = new NameComponent(
                        ((StringToken)remoteManager.getToken()).
                        stringValue(), "");
                _debug(getName(), " looking for name: ",
                        (remoteManager.getToken()).toString());
                NameComponent path[] = {namecomp};
                // locate the remote actor
                _remoteManager =
                    ptolemy.actor.corba.RemoteManagerUtil.RemoteManagerHelper.narrow(
                            ncRef.resolve(path));
                if (_remoteManager == null) {
                    throw new IllegalActionException(this,
                            " can not find the remote actor.");
                }
                _remoteManager.changeModel(_modelString);
            } catch (UserException ex) {
                throw new IllegalActionException(this,
                        " initialize ORB failed." + ex.getMessage());
            }

        } catch (SystemException ex) {
            _debug(getName(), " CORBA set up failed " + ex.getMessage());
            throw new IllegalActionException(this,
                    "CORBA set up faliar"+ex.getMessage());
        }
        _debug("Finished initializing " + getName());
    }

    /** Transfer the input tokens to the remote actor, fire the remote
     *  actor, transfer the output tokens, and broadcast them.
     *  @exception IllegalActionException If any of the above actions
     *  failed or if there is no director.
     */
    public void fire() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this, "No director!");
        }
        trigger.get(0);

            try {
                _remoteManager.startRun();
            } catch (CorbaIllegalActionException ex) {
                throw new IllegalActionException(this,
                        "remote actor throws IllegalActionException"
                        + ex.getMessage());
            }
    }


    /** wrapup the remote actor.
     */
    public void wrapup() throws IllegalActionException {
        try {
            _remoteManager.stop();
        } catch (CorbaIllegalActionException ex) {
            throw new IllegalActionException(this,
                    "remote actor throws IllegalActionException"
                    + ex.getMessage());
        } catch (SystemException ex) {
            throw new InvalidStateException(this,
                    "Comminication Failiar."+ex.getMessage());
        }
    }



    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private  RemoteManager _remoteManager;

    private String _source;

    private String _modelString;
}
