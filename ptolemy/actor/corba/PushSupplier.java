/* An actor that sends entries to a Java Space.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (liuj@eecs.berkeley.edu)
@AcceptedRating Yellow (janneck@eecs.berkeley.edu)
*/

package ptolemy.actor.corba;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.LongToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.data.StringToken;
import ptolemy.actor.lib.Sink;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import ptolemy.actor.corba.CorbaIOUtil.*;
import ptolemy.actor.IOPort;

import java.util.StringTokenizer;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Publisher
/**
An actor that publishes instances of TokenEntry to a JavaSpace.  The
JavaSpace that the entries are published to is identified by the
<i>jspaceName</i> parameter. TokenEntries in the JavaSpace has a name,
a serial number, and a Ptolemy II token. This actor has a single input
port.  When the actor is fired, it consumes at most one token from the
input port and publishes an instance of token entry, which contains
the token, to the JavaSpace with the name specified by the
<i>entryName</i> parameter. In this class, the serial number of the
token entry is not used, and is always set to 0. Derived class may use
the serial number to keep track of the order of the published
tokens. If there is already an entry in the JavaSpace with the entry
name, the new token will override the existing one. In theory, an entry
only exists in the JavaSpace for a limited amount of time, denoted
as the <i>lease time</i>. If the lease time expires, the JavaSpace
can freely remote the entry from it. The lease time of an entry
published by this publisher is specified by the <i>leaseTime</i>
parameter in milliseconds. The default value LEASE_FOREVER will
keep the entry as long as the JavaSpace exists.

@see TokenEntry
@author Jie Liu, Yuhong Xiong
@version $Id$
@since Ptolemy II 1.0
*/

public class PushSupplier extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PushSupplier(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

    	ORBInitProperties  = new Parameter(this, "ORBInit");
        ORBInitProperties.setToken(new StringToken(""));
        remoteConsumerName = new Parameter(this, "RemoteConsumerName");
        remoteConsumerName.setToken(new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    public Parameter ORBInitProperties;

    /** The name of the remote actor. The type of the Parameter
     *  is StringToken.
     */
    public Parameter remoteConsumerName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Setup the link to the remote consumer. This includes creating
     *  the ORB, initializing the naming service, and locating the
     *  remote consumer.
     *  @exception IllegalActionException If any of the above actions
     *        failted.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
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
        _orb = null;
        _initORB(args);
        _debug("Finished initializing " + getName());
    }


    /** Read one input token, if there is one, from the input
     *  and publish an entry into the space for the token read.
     *  Do nothing if there's no token in the input port.
     *  @exception IllegalActionException If the publication
     *  action fails due to network problems, transaction errors,
     *  or any remote exceptions.
     */
    public void fire() throws IllegalActionException {
        try {
            if (input.hasToken(0)) {
                Token token = input.get(0);
                String data = token.toString();
                org.omg.CORBA.Any event = _orb.create_any();
                event.insert_string(data);
                _remoteConsumer.push(event);
                if (_debugging) {
                    _debug(getName(), "Publisher writes " + data);
                }
            }
        } catch (CorbaIllegalActionException ex) {
            throw new IllegalActionException(this,
                    "remote actor throws IllegalActionException"
                    + ex.getMessage());
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                    ////
    private void _initORB(String[] args) throws IllegalActionException{
        try {
            // start the ORB
            _orb = ORB.init(args, null);
            _debug(getName(), " ORB initialized");
            //get the root naming context
            org.omg.CORBA.Object objRef = _orb.resolve_initial_references(
                    "NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            if (ncRef != null) {
                _debug(getName(), "found name service.");
            }
            //resolve the remote consumer reference in Naming
            NameComponent namecomp = new NameComponent(
                    ((StringToken)remoteConsumerName.getToken()).
                    stringValue(), "");
            _debug(getName(), " looking for name: ",
                    (remoteConsumerName.getToken()).toString());
            NameComponent path[] = {namecomp};
            // locate the remote actor
            _remoteConsumer =
                ptolemy.actor.corba.CorbaIOUtil.pushConsumerHelper.narrow(
                        ncRef.resolve(path));
            if(_remoteConsumer == null) {
                throw new IllegalActionException(this,
                        " can not find the remote consumer.");
            }
        } catch (UserException ex) {
            throw new IllegalActionException(this,
                    " initialize ORB failed." + ex.getMessage());
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private ORB _orb;

    private pushConsumer _remoteConsumer;

}

