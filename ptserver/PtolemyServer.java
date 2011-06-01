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
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptserver.control.IServerManager;
import ptserver.control.ServerManager;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/** This class is responsible for launching the message broker, 
 * enabling users to start, pause, and stop simulations through the
 * servlet, and create independently executing simulations upon request.
 * 
 * @author jkillian
 * @version $Id$
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */

public class PtolemyServer implements IServerManager {

    /** The ResourceBundle containing configuration parameters **/
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");
    /** Logger used to write messages to the specified log file. **/
    public static final Logger LOGGER;

    static {
        Logger logger = null;
        FileHandler logFile = null;

        try {
            logger = Logger.getLogger(PtolemyServer.class.getSimpleName());
            logFile = new FileHandler(CONFIG.getString("LOG_FILENAME"), true);
            logFile.setFormatter(new XMLFormatter());

            logger.addHandler(logFile);
            logger.setLevel(Level.ALL);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LOGGER = logger;
    }

    /**
     * Initialize the server and loop while waiting for requests.
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {

        // initialize the singleton
        _instance = new PtolemyServer();

        try {
            if ((args != null) && (args.length % 2 == 0)) {
                for (int i = 0; i < args.length; i += 2) {
                    try {
                        if (args[i].toLowerCase() == "broker_path") {
                            _instance.setBrokerPath(args[i + 1]);
                        } else if (args[i].toLowerCase() == "broker_port") {
                            _instance.setBrokerPort(Integer
                                    .parseInt(args[i + 1]));
                        } else if (args[i].toLowerCase() == "servlet_path") {
                            _instance.setServletPath(args[i + 1]);
                        } else if (args[i].toLowerCase() == "servlet_port") {
                            _instance.setServletPort(Integer
                                    .parseInt(args[i + 1]));
                        }
                    } catch (NumberFormatException e) {
                        PtolemyServer.LOGGER.log(Level.WARNING,
                                "Invalid command line argument provided.");
                    }
                }
            }

            _instance.startup();
        } catch (Throwable e) {
            PtolemyServer.LOGGER.log(Level.SEVERE, e.getMessage());
            e.printStackTrace();
        }
    }

    /** 
     * Creates an instance of the Ptolemy server.  This class is a singleton 
     * so only one instance should ever exist at a time.  Child process are
     * initialized for the servlet (synchronous command handler) and the 
     * MQTT message broker (asynchronous simulation data).
     * 
     * @param brokerPath Path to the MQTT broker executable
     * @param brokerPort Port number to use with the MQTT broker
     * @param servletPath Virtual directory of the servlet
     * @param servletPort Port of the servlet container
     * @exception IllegalStateException Failed to start key processes
     */
    public PtolemyServer() throws IllegalStateException {
        try {
            this._servletPath = CONFIG.getString("SERVLET_PATH");
            this._servletPort = Integer.parseInt(CONFIG
                    .getString("SERVLET_PORT"));
            this._brokerPath = CONFIG.getString("BROKER_PATH");
            this._brokerPort = Integer
                    .parseInt(CONFIG.getString("BROKER_PORT"));
            this._simulations = new ConcurrentHashMap<Ticket, SimulationThread>();
        } catch (Exception e) {
            throw new IllegalStateException("Invalid configuration provided.",
                    e);
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////                public methods
    /**
     * Get the singleton instance of the Ptolemy server.
     */
    public static PtolemyServer getInstance() {
        if (_instance == null) {
            synchronized (_syncRoot) {
                if (_instance == null) {
                    _instance = new PtolemyServer();
                    _instance.startup();
                }
            }
        }

        return _instance;
    }

    /**
     * Initialize the servlet and broker for use by the Ptolemy server.
     */
    public void startup() {
        /** launch the broker **/
        this._broker = null;
        try {
            String[] commands = new String[] { this._brokerPath, "-p",
                    Integer.toString(this._brokerPort) };

            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);

            this._broker = builder.start();
        } catch (IOException e) {
            String errorMessage = "Unable to spawn MQTT broker process.";
            PtolemyServer.LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }

        /** launch the servlet container **/
        this._servletHost = null;
        try {
            this._servletHost = new Server(this._servletPort);
            ServletContextHandler context = new ServletContextHandler(
                    this._servletHost, "/", ServletContextHandler.SESSIONS);
            ServletHolder container = new ServletHolder(ServerManager.class);
            context.addServlet(container, this._servletPath);

            this._servletHost.setHandler(context);
            this._servletHost.start();
        } catch (Exception e) {
            String errorMessage = "Unable to spawn servlet container.";
            PtolemyServer.LOGGER.log(Level.SEVERE, errorMessage);
            throw new IllegalStateException(errorMessage, e);
        }
    }

    /**
     * Shut down supporting processes and destroy active simulation threads.
     * 
     * @exception Throwable
     */
    public void shutdown() throws Exception {
        // shutdown the MQTT broker
        if (this._broker != null) {
            this._broker.destroy();
            this._broker = null;
        }

        // shutdown servlet container
        if (this._servletHost != null) {
            this._servletHost.stop();
            this._servletHost.destroy();
            this._servletHost = null;
        }

        // stop each active thread
        Enumeration enumeration = this._simulations.keys();
        while (enumeration.hasMoreElements()) {
            this.stop((Ticket) enumeration.nextElement());
        }

        // dispose collection
        this._simulations.clear();
        this._simulations = null;
    }

    /**
     * Open a thread on which to load the provided model URL and wait
     * for the user to request it's execution.
     * 
     * @param url Path to the model file
     * @exception Exception Failed to load model file. 
     * with the provided ticket.
     */
    public Ticket open(URL url) throws Exception {
        Ticket ticket = Ticket.generateTicket(url);
        if (ticket != null) {
            this._simulations.put(ticket, new SimulationThread(ticket));
        }

        return ticket;
    }

    /**
     * Start the execution of the simulation on the selected thread by
     * activating the Ptolemy manager.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception Exception Failed to start simulation thread.
     */
    public void start(Ticket ticket) throws IllegalStateException,NullPointerException {
        try {
            SimulationThread activeThread = this._simulations.get(ticket);

            if (activeThread == null) { 
                PtolemyServer.LOGGER.log(
                        Level.SEVERE,
                        String.format("%s: %s", ticket.getTicketID().toString(),
                                "Could not find thread."));
                throw new NullPointerException("Could not find thread.");
            }

            activeThread.getManager().execute();
            
        } catch (IllegalThreadStateException e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalStateException(e.getMessage(), e);
        } catch (IllegalActionException e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalStateException(e.getMessage(), e);
        } catch (KernelException e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Pause the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to pause simulation thread.
     */
    public void pause(Ticket ticket) throws IllegalStateException,NullPointerException {
        SimulationThread activeThread = this._simulations.get(ticket);

        if (activeThread == null) { 
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            "Could not find thread."));
            throw new NullPointerException("Could not find thread.");
        }

        activeThread.getManager().pause();

    }

    /**
     * Resume the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to resume simulation thread.
     */
    public void resume(Ticket ticket) throws NullPointerException {
        SimulationThread activeThread = this._simulations.get(ticket);
        if (activeThread == null) { 
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            "Could not find thread."));
            throw new NullPointerException("Could not find thread.");
        }

        activeThread.getManager().resume();
    }

    /**
     * Stop the execution of the simulation on selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to stop simulation thread.
     */
    public void stop(Ticket ticket) throws IllegalStateException,NullPointerException {
        try {
            SimulationThread activeThread = this._simulations.get(ticket);
            
            if (activeThread == null) { 
                PtolemyServer.LOGGER.log(
                        Level.SEVERE,
                        String.format("%s: %s", ticket.getTicketID().toString(),
                                "Could not find thread."));
                throw new NullPointerException("Could not find thread.");
            }

            activeThread.getManager().finish();

        } catch (IllegalThreadStateException e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    /**
     * Shutdown the thread associated with the user's ticket.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException Failed to destroy simulation thread.
     */
    public void close(Ticket ticket) throws IllegalStateException,NullPointerException {
        try {
            SimulationThread activeThread = this._simulations.get(ticket);
            
            if (activeThread == null) { 
                PtolemyServer.LOGGER.log(
                        Level.SEVERE,
                        String.format("%s: %s", ticket.getTicketID().toString(),
                                "Could not find thread."));
                throw new NullPointerException("Could not find thread.");
            }
            
            this._simulations.remove(ticket);
            activeThread = null;

        } catch (IllegalThreadStateException e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalStateException(e.getMessage(), e);
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
    
    /**
     * Get the number of simulations registered.
     * 
     * @return Number of tickets in the system
     */
    public int getNumberOfSimulationsRunning() {
        return _simulations.size();
    }

    //////////////////////////////////////////////////////////////////////
    ////                private methods
    /**
     * Set the servlet virtual directory.
     * @param _servletPath Virtual path of the servlet
     */
    private void setServletPath(String servletPath) {
        this._servletPath = servletPath;
    }

    /**
     * Set the servlet operating port.
     * @param servletPort Port on which to run the servlet container
     */
    private void setServletPort(int servletPort) {
        this._servletPort = servletPort;
    }

    /**
     * Set the path to the broker executable.
     * @param brokerPath Path to the broker executable
     */
    private void setBrokerPath(String brokerPath) {
        this._brokerPath = brokerPath;
    }

    /**
     * Set the broker operating port
     * @param brokerPort Port on which the MQTT broker operates
     */
    private void setBrokerPort(int brokerPort) {
        this._brokerPort = brokerPort;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables
    private static PtolemyServer _instance;
    private static Object _syncRoot = new Object();
    private Process _broker;
    private Server _servletHost;

    /** configuration variables **/
    private String _brokerPath;
    private int _brokerPort;
    private String _servletPath;
    private int _servletPort;
    private ConcurrentHashMap<Ticket, SimulationThread> _simulations;
}
