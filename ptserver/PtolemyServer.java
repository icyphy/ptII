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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import ptolemy.kernel.util.IllegalActionException;
import ptserver.communication.RemoteModel;
import ptserver.communication.RemoteModel.RemoteModelType;
import ptserver.communication.RemoteModelResponse;
import ptserver.control.IServerManager;
import ptserver.control.ServerManager;
import ptserver.control.Ticket;

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/**
 * Launch the message broker, enabling users to start, pause, resume, and stop
 * simulations through the servlet, and create independently executing
 * simulations upon request.
 * 
 * @author Justin Killian
 * @version $Id$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */
public class PtolemyServer implements IServerManager {

    ///////////////////////////////////////////////////////////////////
    ////                      public variables                     ////

    /**
     * The ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");

    /**
     * Start the Logger used to write messages to the specified log file.
     */
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

    ///////////////////////////////////////////////////////////////////
    ////                  constructor                              ////

    /**
     * Create an instance of the Ptolemy server. This class is a singleton so
     * only one instance should ever exist at a time. Child process are
     * initialized for the servlet (synchronous command handler) and the MQTT
     * message broker (asynchronous simulation data).
     * 
     * @exception Exception  If the server was unable to load the default configuration
     * from the resource file.
     */
    public PtolemyServer() throws Exception {
        try {
            _servletPath = CONFIG.getString("SERVLET_PATH");
            _servletPort = Integer.parseInt(CONFIG.getString("SERVLET_PORT"));
            _modelsDirectory = CONFIG.getString("MODELS_DIRECTORY");
            _brokerPath = CONFIG.getString("BROKER_PATH");
            _brokerPort = Integer.parseInt(CONFIG.getString("BROKER_PORT"));
            _requests = new ConcurrentHashMap<Ticket, SimulationTask>();
            _executor = Executors.newCachedThreadPool();
        } catch (NumberFormatException e) {
            _handleException(
                    "Failed to initialize Ptolemy server with default configuration.",
                    e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                  public methods                           ////

    /**
     * Shut down the thread associated with the user's ticket.
     * 
     * @param ticket  Ticket reference to the simulation request.
     * @exception IllegalActionException If the server was unable to destroy the simulation thread.
     */
    public void close(Ticket ticket) throws IllegalActionException {
        try {
            if (ticket == null) {
                throw new IllegalStateException("Invalid ticket: " + null);
                // TODO: create InvalidTicketException
            }
            if (!_requests.containsKey(ticket)) {
                throw new IllegalStateException("Invalid ticket: "
                        + ticket.getTicketID());
                // TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().finish();
            _requests.remove(ticket);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Return an input stream for the given model file for downloading on the
     * client it.
     * 
     * @param modelName Name for the model xml file.
     * @param exception  The exception that was raised.
     * @exception IllegalActionException If the server encountered an error starting, stopping, or
     * manipulating a simulation request.
     * @return InputStream to the model xml file.
     */

    public InputStream downloadModel(String modelName)
            throws IllegalActionException {

        InputStream inputStream = null;
        try {
            File file = new File(_modelsDirectory + "//" + modelName);
            inputStream = new FileInputStream(file);

        } catch (Exception e) {
            _handleException(e.getMessage().toString(), e);
        }
        return inputStream;

    }

    /**
     * Get the broker operating port.
     * 
     * @return The port on which the MQTT broker operates
     */
    public int getBrokerPort() {
        return this._brokerPort;
    }

    /**
     * Get the singleton instance of the Ptolemy server. If it does not already
     * exist, the singleton will be instantiated using the default
     * configuration.
     * 
     * @return The PtolemyServer singleton.
     * @exception IllegalActionException If the server could not be launched.
     */
    public static PtolemyServer getInstance() throws IllegalActionException {
        if (_instance == null) {
            synchronized (PtolemyServer.class) {
                if (_instance == null) {
                    try {
                        // Create singleton with default configuration.
                        _instance = new PtolemyServer();

                        // Launch the servlet container and broker.
                        _instance.startup();
                    } catch (Exception e) {
                        String message = "Failed to launch Ptolemy server.";
                        PtolemyServer.LOGGER.log(Level.SEVERE, message, e);
                        throw new IllegalActionException(null, e, message);
                    }
                }
            }
        }

        return _instance;
    }

    /**
     * Get a listing of the models available on the server in either the
     * database or the local file system.
     * 
     * @exception IllegalActionException If there was a problem discovering available models.
     * @return The Array of strings containing names of available models.
     */
    public String[] getModelListing() throws IllegalActionException {

        File dir = new File(_modelsDirectory);
        String[] models = dir.list();
        for (String string : models) {
            System.out.println(string);
        }
        return models;
    }

    /**
     * Get the servlet operating port.
     * 
     * @return The port on which to run the servlet container.
     */
    public int getServletPort() {
        return this._servletPort;
    }

    /**
     * Initialize the server, launch the broker and servlet processes, and wait
     * from simulation requests. The following optional command line switches
     * may be used with their accompanying value: -servlet_path, -servlet_port,
     * -broker_path, and -broker_port. The port numbers must integers, the
     * broker path must be the path to the MQTT broker executable on the local
     * machine, and the servlet path is the virtual directory (including the
     * preceding slash) that the Ptolemy servlet will run at.
     * 
     * For example: java -classpath ptserver.PtolemyServer -broker_path
     * /usr/sbin/mosquitto -broker_port 1883
     * 
     * @param args  Optional command line arguments.
     * @exception IllegalActionException If the server could not be launched.
     */
    public static void main(String[] args) throws IllegalActionException {
        try {
            // Create the singleton.
            _instance = new PtolemyServer();

            // Set all provided configuration parameters.
            for (int i = 0; i < args.length; i++) {
                if ((args[i].startsWith("-")) && (i + 1 < args.length)) {
                    if (args[i].toLowerCase().equals("-servlet_path")) {
                        _instance._setServletPath(args[i + 1]);
                    } else if (args[i].toLowerCase().equals("-servlet_port")) {
                        _instance
                                ._setServletPort(Integer.parseInt(args[i + 1]));
                    } else if (args[i].toLowerCase().equals("-broker_path")) {
                        _instance._setBrokerPath(args[i + 1]);
                    } else if (args[i].toLowerCase().equals("-broker_port")) {
                        _instance._setBrokerPort(Integer.parseInt(args[i + 1]));
                    }
                }
            }

            // Launch the servlet container and broker.
            _instance.startup();
        } catch (NumberFormatException e) {
            String message = "Port must be a numeric value.  The default value will be used.";
            PtolemyServer.LOGGER.log(Level.SEVERE, message, e);
            throw new IllegalActionException(null, e, message);
        } catch (Exception e) {
            String message = "Failed to launch Ptolemy server.";
            PtolemyServer.LOGGER.log(Level.SEVERE, message, e);
            throw new IllegalActionException(null, e, message);
        }
    }

    /**
     * Get the number of simulation requests on the server.
     * 
     * @return The size of the hash map of simulation tasks.
     */
    public int numberOfSimulations() {
        if (_requests == null) {
            return 0;
        }

        return _requests.size();
    }

    /**
     * Open a model with the provided model URL and wait for the user to request
     * the execution of the simulation.
     * 
     * @param url  The path to the model file
     * @exception IllegalActionException  If the model fails to load from the provided URL.
     * @return The user's reference to the simulation task
     */
    public RemoteModelResponse open(String url) throws IllegalActionException {
        Ticket ticket = null;
        try {
            // Generate a unique ticket for the request.
            ticket = Ticket.generateTicket(url);
            while (_requests.contains(ticket)) {
                ticket = Ticket.generateTicket(url);
            }

            // Save the simulation request.
            SimulationTask simulationTask = new SimulationTask(ticket);
            RemoteModel clientModel = new RemoteModel(null, null,
                    RemoteModelType.CLIENT);
            clientModel.loadModel(new URL(url));
            RemoteModelResponse response = new RemoteModelResponse();
            response.setTicket(ticket);
            response.setModelTypes(clientModel.getResolvedTypes());
            response.setModelXML(clientModel.getTopLevelActor().exportMoML());
            _requests.put(ticket, simulationTask);
            return response;
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Pause the execution of the selected simulation.
     * 
     * @param ticket  The ticket reference to the simulation request.
     * @exception IllegalActionException If the server was unable to pause the running simulation.
     */
    public void pause(Ticket ticket) throws IllegalActionException {
        try {
            if (ticket == null) {
                throw new IllegalStateException("Invalid ticket: " + null);
                // TODO: create InvalidTicketException
            }
            if (!_requests.containsKey(ticket)) {
                throw new IllegalStateException("Invalid ticket: "
                        + ticket.getTicketID());
                // TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().pause();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Resume the execution of the selected simulation.
     * 
     * @param ticket  The ticket reference to the simulation request.
     * @exception IllegalActionException  If the server was unable to resume the execution of the
     * simulation.
     */
    public void resume(Ticket ticket) throws IllegalActionException {
        try {
            if (ticket == null) {
                throw new IllegalStateException("Invalid ticket: " + null);
                // TODO: create InvalidTicketException
            }
            if (!_requests.containsKey(ticket)) {
                throw new IllegalStateException("Invalid ticket: "
                        + ticket.getTicketID());
                // TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().resume();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Shut down supporting processes and destroy active simulation threads.
     * 
     * @exception IllegalActionException If the servlet, broker, or thread pool cannot be stopped.
     */
    public void shutdown() throws IllegalActionException {
        try {
            // Shut down the MQTT broker.
            if (_broker != null) {
                _broker.destroy();
                _broker = null;
            }
        } catch (Exception e) {
            _handleException("The broker process could not be stopped.", e);
        }

        try {
            // Shut down the servlet.
            if (_servletHost != null) {
                _servletHost.stop();
                _servletHost.destroy();
                _servletHost = null;
            }
        } catch (Exception e) {
            _handleException("The servlet could not be stopped.", e);
        }

        try {
            // Shut down the thread pool and destroy the singleton.
            if (_executor != null) {
                _executor.shutdown();
            }
        } catch (Exception e) {
            _handleException("The thread pool could not be shutdown.", e);
        }

        // Clear all requests in the hash table.
        if (_requests != null) {
            _requests.clear();
            _requests = null;
        }

        _instance = null;
    }

    /**
     * Start the execution of the selected simulation.
     * 
     * @param ticket  The ticket reference to the simulation request.
     * @exception IllegalActionException If the server was unable to start the simulation.
     */
    public void start(Ticket ticket) throws IllegalActionException {
        try {
            if (ticket == null) {
                throw new IllegalStateException("Invalid ticket: " + null);
                // TODO: create InvalidTicketException
            }
            if (!_requests.containsKey(ticket)) {
                throw new IllegalStateException("Invalid ticket: "
                        + ticket.getTicketID());
                // TODO: create InvalidTicketException
            }

            _executor.execute(_requests.get(ticket));
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /**
     * Initialize the servlet and the broker for use in communication with the
     * Ptolemy server.
     * 
     * @exception IllegalActionException  If the broker or servlet cannot be started.
     */
    public void startup() throws IllegalActionException {
        // Launch the broker process.
        _broker = null;
        try {
            String[] commands = new String[] { _brokerPath, "-p",
                    Integer.toString(_brokerPort) };

            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);

            _broker = builder.start();
        } catch (IOException e) {
            _handleException(
                    "Unable to spawn MQTT broker process at '" + _brokerPath
                            + "' on port " + Integer.toString(_brokerPort)
                            + ".", e);
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
            _handleException(
                    "Unable to spawn servlet container at '" + _servletPath
                            + "' on port " + Integer.toString(_servletPort)
                            + ".", e);
        }
    }

    /**
     * Stop the execution of the selected simulation.
     * 
     * @param ticket  The ticket reference to the simulation request.
     * @exception IllegalActionException If the server was unable to stop the simulation.
     */
    public void stop(Ticket ticket) throws IllegalActionException {
        try {
            if (ticket == null) {
                throw new IllegalStateException("Invalid ticket: " + null);
                // TODO: create InvalidTicketException
            }
            if (!_requests.containsKey(ticket)) {
                throw new IllegalStateException("Invalid ticket: "
                        + ticket.getTicketID());
                // TODO: create InvalidTicketException
            }

            _requests.get(ticket).getManager().stop();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private methods                         ////

    /**
     * Log the message and exception into the Ptolemy server log.
     * 
     * @param message  Descriptive message about what caused the error.
     * @param exception The exception that was raised.
     * @exception IllegalActionException If the server encountered an error starting, stopping, or
     *                manipulating a simulation request.
     */
    private void _handleException(String message, Exception exception)
            throws IllegalActionException {
        PtolemyServer.LOGGER.log(Level.SEVERE, message, exception);
        throw new IllegalActionException(null, exception, message);
    }

    /**
     * Set the path to the broker executable.
     * 
     * @param brokerPath Path to the broker executable.
     */
    private void _setBrokerPath(String brokerPath) {
        this._brokerPath = brokerPath;
    }

    /**
     * Set the broker operating port.
     * 
     * @param brokerPort Port on which the MQTT broker operates.
     */
    private void _setBrokerPort(int brokerPort) {
        this._brokerPort = brokerPort;
    }

    /**
     * Set the servlet virtual directory.
     * 
     * @param servletPath Virtual path of the servlet.
     */
    private void _setServletPath(String servletPath) {
        this._servletPath = servletPath;
    }

    /**
     * Set the servlet operating port.
     * 
     * @param servletPort Port on which to run the servlet container.
     */
    private void _setServletPort(int servletPort) {
        this._servletPort = servletPort;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /**
     * The Ptolemy server singleton.
     */
    private static PtolemyServer _instance;
    /**
     * The process reference to the MQTT broker.
     */
    private Process _broker;
    /**
     * The embedded Jetty servlet container that hosts the command servlet.
     */
    private Server _servletHost;
    /**
     * The service that manages the simulation thread pool.
     */
    private ExecutorService _executor;
    /**
     * The server's internal reference to the list of simulation requests.
     */
    private ConcurrentHashMap<Ticket, SimulationTask> _requests;
    private String _brokerPath;
    private int _brokerPort;
    private String _modelsDirectory;
    private String _servletPath;
    private int _servletPort;
}
