/* Ptolemy server which manages the broker, servlet, and simulations.

 Copyright (c) 2011 The Regents of the University of California.
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
 */

package ptserver;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import ptserver.control.IServerManager;
import ptserver.control.ServerManager;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyServer2

/** This class is responsible for launching the message broker, 
 * enabling users to start, pause, and stop simulations through the
 * servlet, and create independently executing simulations upon request.
 * 
 * @author jkillian
 * @version $Id$
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */

public class PtolemyServer2 implements IServerManager {
    /** Creates an instance of the Ptolemy server.  This class is a singleton 
     * so only one instance should ever exist at a time.
     */
    private PtolemyServer2() {
        this._broker = null;
        this._servletHost = null;
        this._threadReference = new ConcurrentHashMap<Ticket, Thread>();
    }

    /**
     * Initialize the server and loop while waiting for requests.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        try {
            // initialize singleton
            PtolemyServer2.getInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////                public methods
    /**
     * Get the singleton instance of the Ptolemy server and initialize it
     * by launching the required processes (MQTT broker & command servlet.
     */
    public static PtolemyServer2 getInstance() {
        if (_instance == null) {
            synchronized (_syncRoot) {
                if (_instance == null) {
                    _instance = new PtolemyServer2();
                    _instance.initialize();
                }
            }
        }

        return _instance;
    }

    /**
     * Open a thread on which to load the provided model URL and wait
     * for the user to request it's execution.
     * 
     * @param url Path to the model file
     * @exception IllegalStateException Failed to load model file. 
     * with the provided ticket.
     */
    public Ticket open(URL url) throws Exception {

        Ticket ticket = new Ticket(url);
        this._threadReference.put(ticket, new Thread());
        //TODO: launch the simulation thread

        return ticket;
    }

    /**
     * Start the execution fo the simulation on the selected thread by
     * activating the Ptolemy manager.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to start simulation thread.
     */
    public void start(Ticket ticket) throws Exception {
        try {
            this._threadReference.get(ticket).start();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid ticket reference.", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unable to modify thread.", e);
        } catch (IllegalThreadStateException e) {
            throw new IllegalStateException("Unable to start thread.", e);
        }
    }

    /**
     * Pause the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to pause simulation thread.
     */
    public void pause(Ticket ticket) throws Exception {
        try {
            this._threadReference.get(ticket).wait();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid ticket reference.", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unable to modify thread.", e);
        } catch (IllegalThreadStateException e) {
            throw new IllegalStateException("Unable to pause thread.", e);
        }
    }

    /**
     * Resume the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to resume simulation thread.
     */
    public void resume(Ticket ticket) throws Exception {
        try {
            this._threadReference.get(ticket).notify();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid ticket reference.", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unable to modify thread.", e);
        } catch (IllegalThreadStateException e) {
            throw new IllegalStateException("Unable to resume thread.", e);
        }
    }

    /**
     * Stop the execution of the simulation on selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to stop simulation thread.
     */
    public void stop(Ticket ticket) throws Exception {
        try {
            this._threadReference.get(ticket).stop();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid ticket reference.", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unable to modify thread.", e);
        } catch (IllegalThreadStateException e) {
            throw new IllegalStateException("Unable to stop thread.", e);
        }
    }

    /**
     * Shutdown the thread associated with the user's ticket.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to destroy simulation thread.
     */
    public void close(Ticket ticket) throws Exception {
        try {
            this._threadReference.get(ticket).destroy();
        } catch (NullPointerException e) {
            throw new IllegalStateException("Invalid ticket reference.", e);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unable to modify thread.", e);
        } catch (IllegalThreadStateException e) {
            throw new IllegalStateException("Unable to destroy thread.", e);
        }
    }

    /**
     * Get a listing of the models available on the server in either the
     * database or the local file system.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to discover available models.
     */
    public String[] getModelListing() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private methods
    /**
     * Initialize child processes for the servlet (synchronous command
     * handler) and the MQTT message broker (asynchronous simulation data).
     * 
     * @exception IllegalStateException Failed to start key processes
     */
    private void initialize() throws IllegalStateException {

        try {
            /** launch the broker **/
            ProcessBuilder builder = new ProcessBuilder(
                    "C:\\Studio\\rsmb_1.2.0\\windows\\broker.exe");
            builder.redirectErrorStream(true);
            this._broker = builder.start();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to spawn MQTT broker process.", e);
        }

        try {
            /** launch the servlet container **/
            this._servletHost = new Server(8080);
            ServletContextHandler context = new ServletContextHandler(
                    this._servletHost, "/", ServletContextHandler.SESSIONS);
            context.addServlet(ServerManager.class, "/ServerManager");
            this._servletHost.setHandler(context);
            this._servletHost.start();
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Unable to spawn servlet container.", e);
        }
    }

    /**
     * Shut down supporting processes and destroy active simulation threads.
     * 
     * @exception Throwable
     */
    private void shutdown() throws Exception {
        if (this._broker != null) {
            try {
                this._broker.destroy();
                this._broker = null;
            } catch (NullPointerException e) {
                this._broker = null;
            }
        }

        if (this._servletHost != null) {
            try {
                this._servletHost.stop();
                this._servletHost.destroy();
                this._servletHost = null;
            } catch (NullPointerException e) {
                this._servletHost = null;
            }
        }

        Enumeration enumeration = this._threadReference.keys();
        while (enumeration.hasMoreElements()) {
            this.stop((Ticket) enumeration.nextElement());
        }

        this._threadReference.clear();
        this._threadReference = null;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables
    private static PtolemyServer2 _instance;
    private static Object _syncRoot = new Object();
    private Process _broker;
    private Server _servletHost;
    private ConcurrentHashMap<Ticket, Thread> _threadReference;
}
