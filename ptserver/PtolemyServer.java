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
import java.lang.Thread.State;
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
import org.python.parser.ast.If;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.control.IServerManager;
import ptserver.control.ServerManager;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/** 
 * Launches the message broker, enabling users to start, pause, and stop 
 * simulations through the servlet, and create independently executing 
 * simulations upon request.
 * 
 * @author jkillian
 * @version $Id$
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */
public class PtolemyServer implements IServerManager {

    //////////////////////////////////////////////////////////////////////
    ////                public variables

    // Access the ResourceBundle containing configuration parameters.
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");

    // Start the Logger used to write messages to the specified log file. 
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
            throw new ExceptionInInitializerError(e);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        LOGGER = logger;
    }

    /**
     * Initialize the server and loop while waiting for requests.  To use,
     * run the command:
     *  java -classpath ptserver.PtolemyServer xxx yyy
     * 
     * @param args Optional command line arguments.
     * -servlet_path /PtolemyServer 
     * -servlet_port 8080 
     * -broker_path /usr/sbin/mosquito
     * -broker_port 1883
     * @exception ExceptionInInitializerError If the server was unable to 
     * start parse the command line configuration, throw an exception.
     */
    public static void main(String[] args) throws ExceptionInInitializerError {
        // Create the singleton.
        _instance = new PtolemyServer();

        try {
            // Set all provided configuration parameters.
            for (int i = 0; i < args.length; i++) {
                if ((args[i].startsWith("-")) && (i + 1 < args.length)) {
                    if (args[i].toLowerCase() == "-servlet_path") {
                        _instance._setServletPath(args[i + 1]);
                    } else if (args[i].toLowerCase() == "-servlet_port") {
                        _instance
                                ._setServletPort(Integer.parseInt(args[i + 1]));
                    } else if (args[i].toLowerCase() == "-broker_path") {
                        _instance._setBrokerPath(args[i + 1]);
                    } else if (args[i].toLowerCase() == "-broker_port") {
                        _instance._setBrokerPort(Integer.parseInt(args[i + 1]));
                    }
                }
            }

            // Launch the servlet container and broker
            _instance.startup();
        } catch (NumberFormatException e) {
            PtolemyServer.LOGGER.log(Level.WARNING,
                    "Port must be a numeric value.");
            throw new ExceptionInInitializerError(
                    "Port must be a numeric value.");
        } catch (IllegalStateException e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to start the servlet or broker.");
            throw new ExceptionInInitializerError(
                    "Unable to start the servlet or broker.");
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////                constructor

    /** 
     * Create an instance of the Ptolemy server.  This class is a singleton 
     * so only one instance should ever exist at a time.  Child process are
     * initialized for the servlet (synchronous command handler) and the 
     * MQTT message broker (asynchronous simulation data).
     * 
     * @exception ExceptionInInitializerError If the server was unable to 
     * start load the default configuration from the resource file, throw
     * an exception.
     */
    public PtolemyServer() {
        try {
            _servletPath = CONFIG.getString("SERVLET_PATH");
            _servletPort = Integer.parseInt(CONFIG.getString("SERVLET_PORT"));
            _brokerPath = CONFIG.getString("BROKER_PATH");
            _brokerPort = Integer.parseInt(CONFIG.getString("BROKER_PORT"));
            _simulations = new ConcurrentHashMap<Ticket, SimulationThread>();
        } catch (NumberFormatException e) {
            PtolemyServer.LOGGER.log(Level.WARNING,
                    "Unable to properly load configuration file.");
            throw new ExceptionInInitializerError(
                    "Unable to properly load configuration file.");
        }
    }

    //////////////////////////////////////////////////////////////////////
    ////                public methods

    /**
     * Get the singleton instance of the Ptolemy server.
     */
    public static PtolemyServer getInstance() {
        if (_instance == null) {
            synchronized (_lock) {
                if (_instance == null) {
                    // Create singleton with default configuration.
                    _instance = new PtolemyServer();

                    // Launch the servlet container and broker.
                    _instance.startup();
                }
            }
        }

        return _instance;
    }

    /**
     * Get the servlet operating port.
     * @return servletPort Port on which to run the servlet container
     */
    public int getServletPort() {
        return this._servletPort;
    }

    /**
     * Get the broker operating port.
     * @return _brokerPort Port on which the MQTT broker operates
     */
    public int getBrokerPort() {
        return this._brokerPort;
    }

    /**
     * Initialize the servlet and broker for use by the Ptolemy server.
     */
    public void startup() {
        // Launch the broker process.
        _broker = null;
        try {
            String[] commands = new String[] { _brokerPath, "-p",
                    Integer.toString(_brokerPort) };

            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);

            _broker = builder.start();
        } catch (IOException e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to spawn MQTT broker process.");
            throw new IllegalStateException(
                    "Unable to spawn MQTT broker process.", e);
        }

        // Launch the Jetty servlet container.
        _servletHost = null;
        try {
            _servletHost = new Server(_servletPort);
            ServletContextHandler context = new ServletContextHandler(
                    _servletHost, "/", ServletContextHandler.SESSIONS);
            ServletHolder container = new ServletHolder(ServerManager.class);
            context.addServlet(container, _servletPath);

            _servletHost.setHandler(context);
            _servletHost.start();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unable to spawn servlet container.");
            throw new IllegalStateException(
                    "Unable to spawn servlet container.", e);
        }
    }

    /**
     * Shut down supporting processes and destroy active simulation threads.
     * 
     * @exception If the servlet container or broker cannot be stopped,
     * throw an exception.
     * for some reason, throw an exception.
     */
    public void shutdown() throws Exception {
        // Shut down the MQTT broker.
        if (_broker != null) {
            _broker.destroy();
            _broker = null;
        }

        // Shutdown the servlet container.
        if (_servletHost != null) {
            _servletHost.stop();
            _servletHost.destroy();
            _servletHost = null;
        }

        // Stop each active thread.
        Enumeration enumeration = _simulations.keys();
        while (enumeration.hasMoreElements()) {
            stop((Ticket) enumeration.nextElement());
        }

        // Dispose of the simulation collection.
        _simulations.clear();
        _simulations = null;
    }

    /**
     * Open a thread on which to load the provided model URL and wait
     * for the user to request the execution of the simulation.
     * 
     * @param url Path to the model file
     * @exception Exception If the model fails to load from the provided 
     * URL, throw an exception.
     * @return Ticket The user's reference to the simulation thread
     */
    public Ticket open(URL url) throws IllegalActionException {
        Ticket ticket = null;

        try {
            // Attempt to generate a unique ticket.
            ticket = Ticket.generateTicket(url);
            while (_simulations.contains(ticket)) {
                ticket = Ticket.generateTicket(url);
            }

            // Enqueue the simulation.
            _simulations.put(ticket, new SimulationThread(ticket));
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }

        return ticket;
    }

    /**
     * Start the execution of the simulation on the selected thread by
     * activating the Ptolemy manager.
     * 
     * @param ticket Ticket reference to the simulation request
     * @exception Exception If the server was unable to start the 
     * simulation on the referenced thread, throw an exception.
     */
    public void start(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_simulations.containsKey(ticket))) {
                throw new Exception("Invalid ticket provided: "
                        + ticket.getTicketID().toString());
                //TODO: create InvalidTicketException
            }

            _simulations.get(ticket).getManager().execute();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /**
     * Pause the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException If the server was unable to 
     * pause the running simulation, throw an exception.
     */
    public void pause(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_simulations.containsKey(ticket))) {
                throw new Exception("Invalid ticket provided: "
                        + ticket.getTicketID().toString());
                //TODO: create InvalidTicketException
            }

            _simulations.get(ticket).getManager().pause();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /**
     * Resume the execution of the simulation on the selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException If the server was unable to 
     * resume the execution of the simulation, throw an exception.
     */
    public void resume(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_simulations.containsKey(ticket))) {
                throw new Exception("Invalid ticket provided: "
                        + ticket.getTicketID().toString());
                //TODO: create InvalidTicketException
            }

            _simulations.get(ticket).getManager().resume();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /**
     * Stop the execution of the simulation on selected thread.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException If the server was unable to 
     * stop the simulation thread, throw an exception.
     */
    public void stop(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_simulations.containsKey(ticket))) {
                throw new Exception("Invalid ticket provided: "
                        + ticket.getTicketID().toString());
                //TODO: create InvalidTicketException
            }

            _simulations.get(ticket).getManager().finish();
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /**
     * Shutdown the thread associated with the user's ticket.
     * 
     * @param ticket Ticket reference to the simulation request.
     * @exception IllegalStateException If the server was unable to 
     * destroy the simulation thread, throw an exception.
     */
    public void close(Ticket ticket) throws IllegalActionException {
        try {
            if ((ticket == null) || (!_simulations.containsKey(ticket))) {
                throw new Exception("Invalid ticket provided: "
                        + ticket.getTicketID().toString());
                //TODO: create InvalidTicketException
            }

            SimulationThread thread = _simulations.get(ticket);
            if (thread.getState() != State.TERMINATED) {
                stop(ticket);
            }

            _simulations.remove(ticket);
        } catch (Exception e) {
            PtolemyServer.LOGGER.log(
                    Level.SEVERE,
                    String.format("%s: %s", ticket.getTicketID().toString(),
                            e.getMessage()));
            throw new IllegalActionException(e.getMessage());
        }
    }

    /**
     * Get a listing of the models available on the server in either the
     * database or the local file system.
     * 
     * @exception IllegalStateException If there was a problem discovering
     * available models, throw an exception.
     * @return Array of URL references to available model files
     */
    public URL[] getModelListing() throws IllegalActionException {
        // TODO Add code to query the local database or current directory
        // in the file system to find available model files.
        return new URL[0];
    }

    //////////////////////////////////////////////////////////////////////
    ////                private methods

    /**
     * Set the servlet virtual directory.
     * @param servletPath Virtual path of the servlet
     */
    private void _setServletPath(String servletPath) {
        this._servletPath = servletPath;
    }

    /**
     * Set the servlet operating port.
     * @param servletPort Port on which to run the servlet container
     */
    private void _setServletPort(int servletPort) {
        this._servletPort = servletPort;
    }

    /**
     * Set the path to the broker executable.
     * @param brokerPath Path to the broker executable
     */
    private void _setBrokerPath(String brokerPath) {
        this._brokerPath = brokerPath;
    }

    /**
     * Set the broker operating port.
     * @param brokerPort Port on which the MQTT broker operates
     */
    private void _setBrokerPort(int brokerPort) {
        this._brokerPort = brokerPort;
    }

    //////////////////////////////////////////////////////////////////////
    ////                private variables

    private static PtolemyServer _instance;
    private static Object _lock = new Object();
    private Process _broker;
    private Server _servletHost;
    private String _brokerPath;
    private int _brokerPort;
    private String _servletPath;
    private int _servletPort;
    private ConcurrentHashMap<Ticket, SimulationThread> _simulations;
}
