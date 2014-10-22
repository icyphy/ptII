/* An implementation of the Receiver interface for distributed
 environments.

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

 IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
 HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 */
package ptolemy.distributed.actor;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import net.jini.core.lookup.ServiceItem;
import ptolemy.actor.AbstractReceiver;
import ptolemy.actor.NoTokenException;
import ptolemy.data.Token;
import ptolemy.distributed.common.DistributedActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;

///////////////////////////////////////////////////////////////////
//// DistributedReceiver

/**
 An implementation of the Receiver interface for distributed environments.
 Basically, its task is to forward tokens to distributed services
 whenever the put method is called.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.actor.AbstractReceiver
 */
public class DistributedReceiver extends AbstractReceiver {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Empty implementation of the inherited abstract method.
     */
    @Override
    public Token get() throws NoTokenException {
        return null;
    }

    /** Empty implementation of the inherited abstract method.
     */
    @Override
    public boolean hasRoom() {
        return false;
    }

    /** Empty implementation of the inherited abstract method.
     */
    @Override
    public boolean hasRoom(int numberOfTokens) {
        return false;
    }

    /** Empty implementation of the inherited abstract method.
     */
    @Override
    public boolean hasToken() {
        return false;
    }

    /** Empty implementation of the inherited abstract method.
     */
    @Override
    public boolean hasToken(int numberOfTokens) {
        return false;
    }

    /** Forward copies of the token to the distributed services
     *  specified in the servicesReceiversListMap.
     *  //TODO:This could be done in parallel. Is it worth the effort?
     *  @param token The token to be forwarded, or null to forward no token.
     *  @exception IllegalActionException If the put fails
     *   (e.g. because of incompatible types).
     */
    @Override
    public void put(Token token) throws IllegalActionException {
        if (token == null) {
            return;
        }
        if (VERBOSE) {
            System.out.println("Forwarding token: " + token.toString());
        }

        for (Iterator services = servicesReceiversListMap.keySet().iterator(); services
                .hasNext();) {
            ServiceItem server = (ServiceItem) services.next();
            LinkedList ids = (LinkedList) servicesReceiversListMap.get(server);
            HashMap hashMap = new HashMap();
            hashMap.put(token, ids);

            DistributedActor distributedActor = (DistributedActor) server.service;

            try {
                distributedActor.put(hashMap);
            } catch (RemoteException e) {
                KernelException.stackTraceToString(e);
            }
        }
    }

    /** Specify the servicesReceiversListMap that contains a sequence of
     *  services and IDs of receivers in the service.
     *  @param servRecListMap The map that contains a sequence of
     *  services and IDs of receivers.
     */
    public void setServicesReceiversListMap(HashMap servRecListMap) {
        if (VERBOSE) {
            System.out.println("> DistributedReceiver."
                    + "setServicesReceiversListMap()");
        }

        servicesReceiversListMap = servRecListMap;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Map containing a set of of services and the receiver's IDs in
     *  those services where the tokens put in the receiver have to be
     *  forwarded to.
     */
    private HashMap servicesReceiversListMap = new HashMap();

    /** When true shows debugging messages. */
    private boolean VERBOSE = false;
}
