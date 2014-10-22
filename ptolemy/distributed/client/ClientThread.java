/* Client thread created at the client side to allow issuing of parallel
 commands to the servers.

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
package ptolemy.distributed.client;

import java.rmi.RemoteException;

import net.jini.core.lookup.ServiceItem;
import ptolemy.distributed.common.DistributedActor;
import ptolemy.kernel.util.KernelException;

///////////////////////////////////////////////////////////////////
////ClientThread

/**
 Thread that manages the interaction with the remote service. It is required
 to allow commands to be issued to the remote services in parallel. This
 threads prevent the main thread of execution to be blocked by the remote
 calls    to the remote services. A synchronization mechanism to issue and
 access commands is provided by ThreadSynchronizer.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)

 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.distributed.client.ThreadSynchronizer
 */
public class ClientThread extends Thread {
    /** Construct a ClientThread with a given ThreadSynchronizer and a given
     *  service.
     *
     *  @param synchr A ThreadSynchronizer.
     *  @param serv The service corresponding to the remote service that the
     *  Client Thread represents.
     */
    public ClientThread(ThreadSynchronizer synchr, ServiceItem serv) {
        super();
        synchronizer = synchr;
        service = serv;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the service that this ClientThread controls.
     *
     *  @return A ServiceItem corresponding to the service this Client Thread
     *  is managing remotely.
     */
    public ServiceItem getService() {
        return service;
    }

    /** Runs the thread. The thread blocks until it gets a command. When a
     *  command is fetched, the service performs remotely the method
     *  corresponding to the given command. Once the task is performed, the
     *  thread is set to ready in the synchronizer. This is performed until
     *  de command is exit.
     */
    @Override
    public void run() {
        super.run();

        if (VERBOSE) {
            System.out.println(service.serviceID + " starts running...");
        }

        DistributedActor distributedActor;
        int command;

        while ((command = synchronizer.getCommand(this)) != EXIT) {
            if (VERBOSE) {
                System.out.println(service.serviceID + " -> Command: "
                        + command);
            }

            distributedActor = (DistributedActor) service.service;

            try {
                switch (command) {
                case INITIALIZE:
                    distributedActor.initialize();
                    break;

                case FIRE:
                    distributedActor.fire();
                    break;

                case ITERATE:
                    distributedActor.iterate(iterationCount);
                    break;
                }
            } catch (RemoteException e) {
                KernelException.stackTraceToString(e);
            }

            synchronizer.setReady(this);
        }

        if (VERBOSE) {
            System.out.println(this + "Exits...");
        }

        synchronizer.setReady(this);
    }

    /**
     *  Specify the number of times that the iteration command is to be
     *  performed. We assume that this will be set correctly before iterate
     *  is called.
     *
     *  @param iterationC Number of times the iteration has to be performed.
     */
    public void setIterationCount(int iterationC) {
        iterationCount = iterationC;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       constants                           ////

    /** Exit command. */
    public static final int EXIT = 1;

    /** Initialize command. */
    public static final int INITIALIZE = 2;

    /** Fire command. */
    public static final int FIRE = 3;

    /** Iterate command. */
    public static final int ITERATE = 4;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** It states whether debugging messages should be printed. */
    private boolean VERBOSE = false;

    /** The ThreadSynchronizer that synchronizes access to the commands.*/
    private ThreadSynchronizer synchronizer;

    /** The ServiceItem managed by this thread. */
    private ServiceItem service;

    /** The number of times the iteration command is to be performed. */
    private int iterationCount = 0;
}
