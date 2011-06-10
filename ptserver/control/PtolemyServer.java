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

package ptserver.control;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/** The PtolemyServer class is responsible for fulfilling simulation
 *  requests.  To do this, the server hosts a servlet through the Jetty
 *  servlet container that enables clients to administer control commands 
 *  for starting, pausing, resuming, and stopping simulations.  The server 
 *  also launches a separate process for the MQTT broker which is used
 *  for transmitting tokens between the client and server.  
 *
 * <p>PtolemyServer uses mosquitto from <a href="http://mosquitto.org/download/#in_browser">http://mosquitto.org/download</a>.</p>
 *
 * <p>Under Windows, there is a mosquitto binary.</p>
 * <p>Under Mac OS X:</p>
 * <ol>
 * <li>Install cmake from <a href="http://www.cmake.org/cmake/resources/software.html#in_browser">http://www.cmake.org/cmake/resources/software.html</a> or from
 * <a href="http://mxcl.github.com/homebrew/#in_browser">http://mxcl.github.com/homebrew</a></li>
 * <li>Download and untar the mosquitto sources:
 * <pre>
 * cd $PTII/vendors
 * wget http://mosquitto.org/files/source/mosquitto-0.10.2.tar.gz
 * tar -zxf mosquitto-0.10.2.tar.gz
 * cd mosquitto-0.10.2
 * </pre>
 * </li>
 * <li> Run:
 * <pre>
 * cmake .
 * make
 * sudo make install
 * </pre>
 * </li>
 * </ol>
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

    /** The ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");

    /** The logger that will record Ptolemy errors to the log file.
     */
    public static final Logger LOGGER;

    /** Initialize the logger used for error handling.
     */
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

    /** Create an instance of the Ptolemy server. This class is a singleton so
     *  only one instance should ever exist at a time.  An embedded servlet container
     *  is initialized for the servlet (synchronous command handler) and a separate
     *  process is launched for the MQTT message broker (asynchronous simulation data).
     *  @exception IllegalActionException If the server was unable to load the default 
     *  configuration from the resource file.
     */
    private PtolemyServer() throws IllegalActionException {
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

    /** Shut down the simulation thread by calling the finish() method 
     *  on its Manager and removing the task from the server.
     *  @param ticket  Ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to destroy the simulation thread.
     */
    public synchronized void close(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _requests.get(ticket).getManager().finish();
            _requests.remove(ticket);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Download the selected model to the client.
     *  @param url URL of the model file.
     *  @return Byte array containing the model data.
     *  @exception IllegalActionException If the server encountered an error opening the model file.
     */
    public byte[] downloadModel(String url) throws IllegalActionException {
        byte[] modelData = null;

        try {
            modelData = ptolemy.util.FileUtilities
                    .binaryReadURLToByteArray(new URL(url));
        } catch (Exception e) {
            _handleException("Unable to read the model URL: " + url, e);
        }

        return modelData;
    }

    /** Get the broker operating port.
     *  @return The port on which the MQTT broker operates
     *  @see #setBrokerPort
     */
    public int getBrokerPort() {
        return _brokerPort;
    }

    /** Get the singleton instance of the Ptolemy server. If it does not already
     *  exist, the singleton will be instantiated using the default
     *  configuration.
     *  @return The PtolemyServer singleton.
     *  @exception IllegalActionException If the server could not be launched.
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

    /** Get a listing of the models available on the server in either the
     *  database or the local file system.
     *  @return An array of URLs for the models available on the server.
     *  @exception IllegalActionException If there was a problem discovering available models.
     */
    public String[] getModelListing() throws IllegalActionException {

        FilenameFilter modelFilter = new FilenameFilter() {
            public boolean accept(File file, String filename) {
                return (filename.endsWith(".xml"));
            }
        };

        ArrayList<String> urls = new ArrayList<String>();
        File modelDirectory = new File(_modelsDirectory);

        for (File filterResult : modelDirectory.listFiles(modelFilter)) {
            if (!filterResult.isDirectory()) {
                try {
                    urls.add(filterResult.toURI().toURL().toExternalForm());
                } catch (Exception e) {
                    _handleException(
                            "Unable to construct the URL for model file: "
                                    + filterResult.getName(), e);
                }
            }
        }

        String[] returnItems = new String[urls.size()];
        return urls.toArray(returnItems);
    }

    /** Get the servlet operating port.
     *  @return The port on which to run the servlet container.
     *  @see #setServletPort
     */
    public int getServletPort() {
        return _servletPort;
    }

    /** Initialize the Ptolemy server, launch the broker process, set up the servlet host, 
     *  and wait for simulation requests. The following optional command line switches
     *  may be used with their accompanying value: -servlet_path, -servlet_port,
     *  -broker_path, and -broker_port. The port numbers must be integers, the
     *  broker path must be the path to the MQTT broker executable, and the servlet 
     *  path is the virtual directory (including the preceding slash) at which the Ptolemy 
     *  servlet will hosted.
     *  
     *  For example: java -classpath ptserver.PtolemyServer -broker_path
     *  /usr/sbin/mosquitto -broker_port 1883
     * 
     *  @param args  Optional command line arguments.
     *  @exception IllegalActionException If the server could not be launched.
     */
    public static void main(String[] args) throws IllegalActionException {
        try {
            // Create the singleton.
            _instance = new PtolemyServer();

            // Set all provided configuration parameters.
            for (int i = 0; i < args.length; i++) {
                if ((args[i].startsWith("-")) && (i + 1 < args.length)) {
                    if (args[i].toLowerCase().equals("-servlet_path")) {
                        _instance.setServletPath(args[i + 1]);
                    } else if (args[i].toLowerCase().equals("-servlet_port")) {
                        _instance.setServletPort(Integer.parseInt(args[i + 1]));
                    } else if (args[i].toLowerCase().equals("-broker_path")) {
                        _instance.setBrokerPath(args[i + 1]);
                    } else if (args[i].toLowerCase().equals("-broker_port")) {
                        _instance.setBrokerPort(Integer.parseInt(args[i + 1]));
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

    /** Get the number of simulation on the server.  The current thread pool implementation 
     *  allows for an infinite number of concurrent simulations rather than limiting the server to
     *  finite amount.
     *  @return The number of active simulations as well as the queued requests that have not 
     *  yet been fulfilled.
     */
    public synchronized int numberOfSimulations() {
        if (_requests == null) {
            return 0;
        }

        return _requests.size();
    }

    /** Open a model with the provided model URL and wait for the user to request
     *  the execution of the simulation.
     *  @param url  The path to the model file
     *  @exception IllegalActionException  If the model fails to load from the provided URL.
     *  @return The user's reference to the simulation task
     */
    public synchronized RemoteModelResponse open(String url)
            throws IllegalActionException {
        RemoteModelResponse response = null;
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

            // Populate the response.
            response = new RemoteModelResponse();
            response.setTicket(ticket);
            response.setModelTypes(clientModel.getResolvedTypes());
            response.setModelXML(clientModel.getTopLevelActor().exportMoML());

            _requests.put(ticket, simulationTask);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }

        return response;
    }

    /** Pause the execution of the simulation by calling the pause() method
     *  on its Manager.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to pause the running simulation.
     */
    public synchronized void pause(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _requests.get(ticket).getManager().pause();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Resume the execution of the simulation by calling the resume() method
     *  on its Manager.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException  If the server was unable to resume the execution of the
     *  simulation.
     */
    public synchronized void resume(Ticket ticket)
            throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _requests.get(ticket).getManager().resume();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Set the path to the broker executable.
     *  @param brokerPath Path to the broker executable.
     */
    public void setBrokerPath(String brokerPath) {
        _brokerPath = brokerPath;
    }

    /** Set the broker operating port.
     *  @param brokerPort Port on which the MQTT broker operates.
     *  @see #getBrokerPort
     */
    public void setBrokerPort(int brokerPort) {
        _brokerPort = brokerPort;
    }

    /** Set the servlet virtual directory.
     *  @param servletPath Virtual path of the servlet.
     */
    public void setServletPath(String servletPath) {
        _servletPath = servletPath;
    }

    /** Set the servlet operating port.
     *  @param servletPort Port on which to run the servlet container.
     *  @see #getServletPort
     */
    public void setServletPort(int servletPort) {
        _servletPort = servletPort;
    }

    /** Shut down the broker process and stop all active simulation 
     *  threads by calling their Managers.
     *  @exception IllegalActionException If the servlet, broker, or thread pool cannot be stopped.
     */
    public synchronized void shutdown() throws IllegalActionException {
        //TODO: send ShutdownNotifierToken and sleep(5000)
        //TODO: shutdown active simulations via Manager

        try {
            _broker.destroy();
            _broker = null;
        } catch (Exception e) {
            _handleException("The broker process could not be stopped.", e);
        }

        try {
            _servletHost.stop();
            _servletHost.destroy();
            _servletHost = null;
        } catch (Exception e) {
            _handleException("The servlet could not be stopped.", e);
        }

        try {
            _executor.shutdown();
        } catch (Exception e) {
            _handleException("The thread pool could not be shutdown.", e);
        }

        _requests.clear();
        _requests = null;
        _instance = null;
    }

    /** Start the execution of the simulation by utilizing a
     *  free thread within the pool.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to start the simulation.
     */
    public synchronized void start(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _executor.execute(_requests.get(ticket));
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Initialize the servlet and the broker for use in communication with the
     *  Ptolemy server.
     *  @exception IllegalActionException  If the broker or servlet cannot be started.
     */
    public synchronized void startup() throws IllegalActionException {
        // Launch the broker process.
        _broker = null;
        try {
            String[] commands = new String[] { _brokerPath, "-p",
                    String.valueOf(_brokerPort) };

            ProcessBuilder builder = new ProcessBuilder(commands);
            builder.redirectErrorStream(true);

            _broker = builder.start();
        } catch (IOException e) {
            _handleException("Unable to spawn MQTT broker process at '"
                    + _brokerPath + "' on port " + String.valueOf(_brokerPort)
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
                            + "' on port " + String.valueOf(_servletPort) + ".",
                    e);
        }
    }

    /** Stop the execution of the simulation by calling the finish() method
     *  on its Manager.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to stop the simulation.
     */
    public synchronized void stop(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _requests.get(ticket).getManager().finish();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                   private methods                         ////

    /** Check to ensure that the provided ticket is valid and refers to
     *  a current simulation request on the server.
     *  @param ticket The ticket to be validated.
     *  @exception IllegalStateException If the ticket is null or does not
     *  reference a valid simulation request on the server.
     */
    private void _checkTicket(Ticket ticket) throws IllegalStateException {
        if (ticket == null) {
            throw new IllegalStateException("The ticket was null.");
            // TODO: create InvalidTicketException
        }
        if (!_requests.containsKey(ticket)) {
            throw new IllegalStateException(
                    "The ticket does not reference a simulation: "
                            + ticket.getTicketID());
            // TODO: create InvalidTicketException
        }
    }

    /** Log the message and exception into the Ptolemy server log.
     *  @param message  Descriptive message about what caused the error.
     *  @param error The exception that was raised.
     *  @exception IllegalActionException Always throws the exception in order to propogate
     *  the error through the Ptolemy hierarchy.
     */
    private void _handleException(String message, Exception error)
            throws IllegalActionException {
        PtolemyServer.LOGGER.log(Level.SEVERE, message, error);
        throw new IllegalActionException(null, error, message);
    }

    ///////////////////////////////////////////////////////////////////
    ////                private variables                          ////

    /** The Ptolemy server singleton.
     */
    private static PtolemyServer _instance;

    /** The process reference to the MQTT broker.
     */
    private Process _broker;

    /** The embedded Jetty servlet container that hosts the command servlet.
     */
    private Server _servletHost;

    /** The service that manages the simulation thread pool.
     */
    private ExecutorService _executor;

    /** The server's internal reference to the list of simulation requests.
     */
    private ConcurrentHashMap<Ticket, SimulationTask> _requests;

    /** The absolute path to the broker executable.
     */
    private String _brokerPath;

    /** The port on which the MQTT broker operates.
     */
    private int _brokerPort;

    /** The local directory in which model files are stored.
     */
    private String _modelsDirectory;

    /** The virtual path of the command servlet.
     */
    private String _servletPath;

    /** The port on which the servlet is made available.
     */
    private int _servletPort;
}
