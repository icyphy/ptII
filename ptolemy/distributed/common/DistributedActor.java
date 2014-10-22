/* Interface for distributed actors.

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
package ptolemy.distributed.common;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// DistributedActor

/**
 An DistributedActor is a distributed executable entity. This interface
 defines the common functionality in DistributedActorWrapper and further
 implementations of distributed Actors. It defines the <i>action methods</i>,
 which determine how an distributed object can be invoked. It should be
 implemented by distributed actors.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.distributed.rmi.DistributedActorWrapper
 @see ptolemy.actor.Actor
 @see ptolemy.actor.Executable
 */
public interface DistributedActor {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire the distributed actor.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void fire() throws java.rmi.RemoteException;

    /** Return the Internet Address where the distributed actor is executing.
     *
     *  @return InetAddress the Internet Address where the distributed actor is
     *  being executed.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public InetAddress getAddress() throws java.rmi.RemoteException;

    /** Begin execution of the actor.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void initialize() throws java.rmi.RemoteException;

    /** Invoke a specified number of iterations of the actor.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public int iterate(int count) throws java.rmi.RemoteException;

    /** Load a moml representation of an actor.
     *
     *  @param moml The moml code representing the actor to be loaded.
     *  @return True if the loading was successful.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public boolean loadMoML(String moml) throws java.rmi.RemoteException;

    /** This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration.
     *
     *  @return True if the execution can continue.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public boolean postfire() throws java.rmi.RemoteException;

    /** This method should be invoked once per iteration, before the first
     *  invocation of fire() in that iteration.
     *
     *  @return True if the iteration can proceed.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public boolean prefire() throws java.rmi.RemoteException;

    /** This method should be invoked exactly once per execution
     *  of a model, before any of these other methods are invoked.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void preinitialize() throws java.rmi.RemoteException;

    /** Puts copies of the token received into the Receivers included in the
     *  IDlist. The data map contains a token and a list of IDs.
     *
     *  @param data contains a token and a list of IDs.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     *  @exception IllegalActionException If the transaction fails (e.g.
     *   the data type is incompatible).
     */
    public void put(HashMap data) throws java.rmi.RemoteException,
            IllegalActionException;

    /** Set the "virtual connections" concerning the wrapped actor.
     *  The connections HashMap contains a list of ports, and for each
     *  of them a mapping depending on the type of port.
     *  - If the port is an input: a list of receiver IDs that are
     *  contained by the port.
     *  i.e.: inputport, (IDa, ..., IDn)
     *  - If it is an output port: a mapping of services to lists of
     *  receivers.
     *  i.e.: outputport, (servicea, (IDi, ..., IDk),
     *                         ...
     *                     servicen, (IDn, ..., IDs))
     *
     *  For the input ports, a new relation of the type is created of the
     *  type DistributedTypepIORelation for every ID that is received. This
     *  is to force a Receiver to be created whenever createReceivers in the
     *  corresponding port is called.
     *
     *  For the output ports, one only relation of the type
     *  DistributedTypepIORelation is created and the mapping of services to
     *  IDs is set into the relation. The relation sets that same structure in
     *  its internal DistributedReceiver that is in charge of token forwarding
     *  whenever send is called on the port.
     *
     *  @param connections a mapping of ports, services and receiver IDs (see
     *  before).
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void setConnections(HashMap connections)
            throws java.rmi.RemoteException;

    /** Set the port types of the wrapped actor.
     *
     *  @param portTypes is a Map of ports to port types.
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void setPortTypes(HashMap portTypes) throws java.rmi.RemoteException;

    /** Request that execution of the wrapped actor to stop as
     *  soon as possible.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void stop() throws java.rmi.RemoteException;

    /** Request that execution of the current iteration of the
     *  wrapped actor complete.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void stopFire() throws java.rmi.RemoteException;

    /** Terminate any currently executing model with extreme prejudice.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void terminate() throws java.rmi.RemoteException;

    /** This method is invoked exactly once per execution
     *  of an application.
     *
     *  @exception RemoteException If a communication-related exception may
     *  occur during the execution of a remote method call.
     */
    public void wrapup() throws java.rmi.RemoteException;
}
