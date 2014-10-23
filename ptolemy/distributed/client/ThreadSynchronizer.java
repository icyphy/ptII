/* A synchronizer for the client threads.

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

import java.util.HashMap;

import ptolemy.kernel.util.KernelException;

///////////////////////////////////////////////////////////////////
////ThreadSynchronizer

/**
 Synchronizes the access to the commandsMap. In order to allow parallel
 execution of commands, the ClientThreads that manage remote actors locally
 in the DistributedSDFDirector have to be able to access the commands
 without blocking the main execution Thread in a synchronized manner.
 Commands are represented by integers. It provides mechanisms to issue
 sets of commands and synchronize the access to those commands by the
 client Threads.
 It is assumed that no new set of commands is issued before the previous
 set of commands has been processed. Every ClientThread is responsible to
 set itself as ready after performing a command.

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 @see ptolemy.distributed.client.ClientThread
 */
public class ThreadSynchronizer {
    /** Construct a ThreadSynchronizer.
     */
    public ThreadSynchronizer() {
        super();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Waits until readyMap is empty.
     */
    public synchronized void commandsProcessed() {
        while (!notReadyMap.isEmpty()) {
            try {
                if (VERBOSE) {
                    System.out
                    .println("commandsEmpty: waiting for readyMap to "
                            + "be empty");
                }

                wait();
            } catch (InterruptedException e) {
                KernelException.stackTraceToString(e);
            }
        }

        if (VERBOSE) {
            System.out.println("commandsProcessed!");
        }
    }

    /** Synchronizes access to the commands by the ClientThreads. They will
     *  block waiting for commands to be issued. Every time a command is
     *  fetched, it is removed from the commandsMap and all the waiting
     *  threads are notified.
     *
     *  @param key An object that used as the key in the commands map.
     *  @return An integer representing the command.
     */
    public synchronized int getCommand(Object key) {
        while (commandsMap.get(key) == null) {
            try {
                if (VERBOSE) {
                    System.out.println("getCommand waiting for " + key);
                }

                wait();
            } catch (InterruptedException e) {
                KernelException.stackTraceToString(e);
            }
        }

        int auxCommand = ((Integer) commandsMap.get(key)).intValue();
        commandsMap.remove(key);
        notifyAll();
        return auxCommand;
    }

    /** Issues a new set of commands. All the commands are copied to readyMap
     *  that keeps track of the completed commands.No new set of commands
     *  should be issued before the previous set has been completed. When a
     *  new set of commands is issued all the waiting threads are notified.
     *
     *  @param commands HashMap representing the commands.
     */
    public synchronized void setCommands(HashMap commands) {
        if (true) {
            System.out.println("Commands set!" + commands.size());
        }

        commandsMap.putAll(commands);
        notReadyMap.putAll(commands);
        notifyAll();
    }

    /** Removes a given key from the readyMap. Wakes up all threads that are
     *  waiting on this object's monitor.
     *
     *  @param key The key to be removed.
     */
    public synchronized void setReady(Object key) {
        notReadyMap.remove(key);
        notifyAll();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** It states whether debugging messages should be printed. */
    private boolean VERBOSE = false;

    /** The Map containing the commands to be executed.*/
    private HashMap commandsMap = new HashMap();

    /** The Map containing the Threads that are not ready.*/
    private HashMap notReadyMap = new HashMap();
}
