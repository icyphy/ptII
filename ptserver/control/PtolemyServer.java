/* Ptolemy server which manages the broker, servlet, and simulations.

 Copyright (c) 2011-2014 The Regents of the University of California.
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
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;

import ptolemy.actor.Manager.State;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.actor.injection.PtolemyInjector;
import ptolemy.actor.injection.PtolemyModule;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptserver.communication.ProxyModelAdapter;
import ptserver.communication.ProxyModelInfrastructure;
import ptserver.communication.ProxyModelInfrastructure.ProxyModelListener;
import ptserver.communication.ProxyModelResponse;
import ptserver.data.RemoteEventToken;
import ptserver.data.RemoteEventToken.EventType;
import ptserver.data.TokenParser;
import ptserver.data.TokenParser.HandlerData;

///////////////////////////////////////////////////////////////////
//// PtolemyServer

/** The PtolemyServer class is responsible for fulfilling simulation
 *  requests.  To do this, the server hosts a servlet through the Jetty
 *  servlet container that enables clients to administer control commands
 *  for starting, pausing, resuming, and stopping simulations.  The server
 *  also launches a separate process for the MQTT broker which is used
 *  for transmitting tokens between the client and server.
 *
 * <p>PtolemyServer uses mosquitto from
 * <a href="http://mosquitto.org/download/#in_browser">http://mosquitto.org/download</a>.</p>
 *
 * <p>Under Windows, there is a mosquitto binary available for download.</p>
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
 * <p>This class requires that the mosquitto binary be running.  Under Mac OS X:</p>
 * <pre>
 * /usr/local/sbin/mosquitto &amp;
 * </pre>
 *
 * <p>See the tests in $PTII/ptserver/test/junit for simple code that
 * uses this class.</p>
 *
 *
 * @author Justin Killian
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (jkillian)
 * @Pt.AcceptedRating Red (jkillian)
 */
public final class PtolemyServer implements IServerManager {

    static {
        // FIXME remove PTServerModule after SysOutActor is deleted
        // or create a proper initializer for it
        ArrayList<PtolemyModule> modules = new ArrayList<PtolemyModule>();
        modules.addAll(ActorModuleInitializer.getModules());
        modules.add(new PtolemyModule(ResourceBundle
                .getBundle("ptserver.util.PTServerModule")));
        PtolemyInjector.createInjector(modules);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Shut down the simulation thread by calling the finish() method
     *  on its Manager and removing the task from the server.
     *  @param ticket  Ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to destroy the simulation thread.
     */
    @Override
    public synchronized void close(Ticket ticket) throws IllegalActionException {
        if (ticket == null || _requests.get(ticket) == null) {
            String message = "Ticket " + ticket + " was not found";
            PtolemyServer.LOGGER.log(Level.SEVERE, message);
        } else {
            stop(ticket);
            SimulationTask task = _requests.get(ticket);
            // FindBugs is wrong here since _checkTicket ensures that task is not null
            task.close();

            _requests.remove(ticket);
        }

    }

    /** Create the singleton with non-default configuration values.
     *
     *  <p>If any of the parameters are null, then values in the
     *  ptserver.PtolemyServerConfig resource are checked.  Typically, this file
     *  may be found as $PTII/ptserver/PtolemyServerConfig.properties.</p>
     *
     *  @param servletPort The port on which the servlet operates.
     *  @param brokerPath The path to the broker executable.
     *  @param brokerAddress The host address of the MQTT broker.
     *  @param brokerPort The port of the broker.
     *  @param modelDirectory The root directory of where model files are stored.
     *  If the value of the modelDirectory is not a directory, then a directory relative
     *  to the value of the ptolemy.ptII.dir (aka $PTII) property is checked.
     *  If that directory does not exist, then $PTII/ptserver/demo is used.
     *  @exception IllegalActionException If the server could not be created.
     */
    public static synchronized void createInstance(int servletPort,
            String brokerPath, String brokerAddress, int brokerPort,
            String modelDirectory) throws IllegalActionException {
        _instance = new PtolemyServer(servletPort, brokerPath, brokerAddress,
                brokerPort, modelDirectory);
    }

    /** Download the selected model to the client.
     *  @param url URL of the model file.
     *  @return Byte array containing the model data.
     *  @exception IllegalActionException If the server encountered an
     *  error opening the model file.
     */
    @Override
    public byte[] downloadModel(String url) throws IllegalActionException {
        byte[] modelData = null;
        try {
            modelData = FileUtilities.binaryReadURLToByteArray(new URL(url));
        } catch (Exception e) {
            _handleException("Unable to read the model URL: " + url, e);
        }

        return modelData;
    }

    /** Get the full URL to the message broker.
     *  @return The URL to the servlet including port.
     */
    public String getBrokerUrl() {
        return _brokerUrl;
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
                    createInstance(-1, null, null, -1, null);
                }
            }
        }

        return _instance;
    }

    /** Get a listing of the layouts for a specific model available on the
     *  server in either the database or the local file system.
     *  @param url Address of the model file for which layouts are found.
     *  @return An array of URLs for the layouts available for the model on the server.
     *  @exception IllegalActionException If there was a problem discovering available layouts.
     */
    @Override
    public String[] getLayoutListing(final String url)
            throws IllegalActionException {
        FilenameFilter layoutFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                String modelName = url.substring(url.lastIndexOf("/") + 1,
                        url.lastIndexOf(".xml"));

                return filename.startsWith(modelName + "_")
                        && filename.endsWith(".layout.xml");
            }
        };

        ArrayList<String> urls = new ArrayList<String>();
        File modelDirectory = new File(_modelsDirectory);

        File files[] = modelDirectory.listFiles(layoutFilter);
        if (files == null) {
            String message = "Failed to list files in \"" + _modelsDirectory
                    + "\", listFiles() returned null";
            _handleException(message, new NullPointerException(message));
        }
        for (File filterResult : files) {
            if (!filterResult.isDirectory()) {
                try {
                    urls.add(filterResult.toURI().toURL().toExternalForm());
                } catch (Exception e) {
                    _handleException(
                            "Unable to construct the URL for layout file: "
                                    + filterResult.getName(), e);
                }
            }
        }

        String[] returnItems = new String[urls.size()];
        return urls.toArray(returnItems);
    }

    /** Get a listing of the models available on the server in either the
     *  database or the local file system.
     *  @return An array of URLs for the models available on the server.
     *  @exception IllegalActionException If there was a problem discovering available models.
     */
    @Override
    public String[] getModelListing() throws IllegalActionException {

        FilenameFilter modelFilter = new FilenameFilter() {
            @Override
            public boolean accept(File file, String filename) {
                return filename.endsWith(".xml")
                        && !filename.endsWith(".layout.xml");
            }
        };

        ArrayList<String> urls = new ArrayList<String>();
        File modelDirectory = new File(_modelsDirectory);

        File[] files = modelDirectory.listFiles(modelFilter);
        if (files == null) {
            String message = "Failed to list files in \"" + _modelsDirectory
                    + "\", listFiles() returned null";
            _handleException(message, new NullPointerException(message));
        }
        for (File filterResult : files) {
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

    /** Get the full URL to the servlet application.
     *  @return The URL to the servlet including port.
     */
    public String getServletUrl() {
        return _servletUrl;
    }

    /** Get the simulation task of the provided ticket.
     *  @param ticket The ticket associated with the simulation task.
     *  @return the simulation task associated with the provided ticket.
     *  @exception IllegalActionException if the ticket is invalid.
     */
    public synchronized SimulationTask getSimulationTask(Ticket ticket)
            throws IllegalActionException {
        SimulationTask task = null;
        try {
            task = _requests.get(ticket);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }

        return task;
    }

    /** Get the current state of a specific simulation based on the simulation manager's state.
     *  @param ticket The ticket reference to the simulation request.
     *  @return The state of the queried simulation.
     *  @exception IllegalActionException If the ticket is invalid or the state
     *  of the running situation could not be determined.
     */
    public synchronized State getStateOfSimulation(Ticket ticket)
            throws IllegalActionException {
        State state = null;
        try {
            _checkTicket(ticket);
            SimulationTask task = _requests.get(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            state = task.getManager().getState();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }

        return state;
    }

    /** Get the token handlers loaded on the server so that they can be
     *  set up on the client.
     *  @return The token handler map from the server.
     *  @exception IllegalActionException If the server was unable to get the handler map.
     */
    @Override
    public synchronized LinkedHashMap<String, String> getTokenHandlerMap()
            throws IllegalActionException {
        LinkedHashMap<String, String> tokenHandlerMap = null;
        try {
            tokenHandlerMap = new LinkedHashMap<String, String>();
            for (HandlerData<?> data : TokenParser.getInstance()
                    .getHandlerList()) {
                tokenHandlerMap.put(data.getTokenType().getName(), data
                        .getTokenHandler().getClass().getName());
            }
        } catch (Exception e) {
            _handleException(
                    "Problem sending token handler map: " + e.getMessage(), e);
        }

        return tokenHandlerMap;
    }

    /** Initialize the Ptolemy server, set up the servlet host, and
     * wait for simulation requests.
     *
     * <p>This class requires that the mosquitto binary be running.  See the class comment
     * for information about building, installing and invoking mosquitto.</p>
     *
     *  <p>The following optional command line switches may be used with their accompanying value:
     *  -servlet_port, -broker_path (if launching local broker), -broker_address, -broker_port,
     *  and -model_dir. The port numbers must be integers, the broker path must be the path
     *  to the MQTT broker executable, and the broker address must be the host address.
     *  The default values for the command line switches is read from the values in the
     *  ptserver.PtolemyServerConfig resource.  Typically, this file
     *  may be found as $PTII/ptserver/PtolemyServerConfig.properties.</p>
     *
     *  <p>For example:</p>
     *  <pre>
     *  java -classpath java -classpath $PTII:${PTII}/ptserver/lib/hessian-4.0.7.jar:${PTII}/ptserver/lib/jetty-all-7.4.1.v20110513.jar:${PTII}/ptserver/lib/servlet-api-2.5.jar:${PTII}/ptserver/lib/wmqtt.jar \
     *      ptserver.control.PtolemyServer \
     *      -broker_address 192.168.125.169 -broker_port 1883
     *  </pre>
     *
     *  @param args Optional command line arguments.
     *  @exception IllegalActionException If the server could not be launched.
     */
    public static void main(String[] args) throws IllegalActionException {
        try {
            int servletPort = -1;
            String brokerPath = null;
            String brokerAddress = null;
            int brokerPort = -1;
            String modelDirectory = null;

            // Set all provided configuration parameters.
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-") && i + 1 < args.length) {
                    if (args[i].toLowerCase(Locale.getDefault()).equals(
                            "-servlet_port")) {
                        servletPort = Integer.parseInt(args[i + 1]);
                    } else if (args[i].toLowerCase(Locale.getDefault()).equals(
                            "-broker_path")) {
                        brokerPath = args[i + 1];
                    } else if (args[i].toLowerCase(Locale.getDefault()).equals(
                            "-broker_address")) {
                        brokerAddress = args[i + 1];
                    } else if (args[i].toLowerCase(Locale.getDefault()).equals(
                            "-broker_port")) {
                        brokerPort = Integer.parseInt(args[i + 1]);
                    } else if (args[i].toLowerCase(Locale.getDefault()).equals(
                            "-model_dir")) {
                        modelDirectory = args[i + 1];
                    }
                }
            }

            // Create the singleton.
            createInstance(servletPort, brokerPath, brokerAddress, brokerPort,
                    modelDirectory);
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
     *  @param modelUrl The path to the model file
     *  @param layoutUrl The path to a model's layout file
     *  @exception IllegalActionException  If the model fails to load from the provided URL.
     *  @return The user's reference to the simulation task
     */
    @Override
    public synchronized ProxyModelResponse open(String modelUrl,
            String layoutUrl) throws IllegalActionException {
        ProxyModelResponse response = null;
        Ticket ticket = null;

        try {
            // Generate a unique ticket for the request.
            ticket = Ticket.generateTicket(modelUrl, layoutUrl);
            while (_requests.containsKey(ticket)) {
                ticket = Ticket.generateTicket(modelUrl, layoutUrl);
            }

            // Save the simulation request.
            SimulationTask simulationTask = new SimulationTask(ticket);
            simulationTask.getProxyModelInfrastructure().addProxyModelListener(
                    _remoteModelListener);
            simulationTask.getProxyModelInfrastructure().setUpInfrastructure(
                    ticket, _brokerUrl);

            // Populate the response.
            response = new ProxyModelResponse();
            response.setTicket(ticket);
            response.setModelTypes(simulationTask.getProxyModelInfrastructure()
                    .getModelTypes());
            response.setModelXML(new String(
                    downloadModel(ticket.getLayoutUrl())));
            response.setModelImage(_getModelImage(new URL(modelUrl)));
            response.setBrokerUrl(_brokerUrl);

            _requests.put(ticket, simulationTask);
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }

        return response;
    }

    /** Pause the execution of the simulation by calling the pause() method
     *  on its Manager.
     *  @param ticket The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to pause the running simulation.
     */
    @Override
    public synchronized void pause(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
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
    @Override
    public synchronized void resume(Ticket ticket)
            throws IllegalActionException {
        try {
            _checkTicket(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            _requests.get(ticket).getManager().resume();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Shut down the broker process and stop all active simulation
     *  threads by calling their Managers.
     *  @exception IllegalActionException If the servlet, broker, or thread pool cannot be stopped.
     */
    public synchronized void shutdown() throws IllegalActionException {
        for (SimulationTask task : _requests.values()) {
            try {
                // Notify the client that the server is shutting down.
                task.getProxyModelInfrastructure()
                        .getTokenPublisher()
                        .sendToken(
                                new RemoteEventToken(EventType.SERVER_SHUTDOWN,
                                        "The Ptolemy server you are currently connected is shutting down."),
                                null);

                // Shut down the locally running simulation.
                close(task.getProxyModelInfrastructure().getTicket());
            } catch (Throwable error) {
                PtolemyServer.LOGGER.log(Level.SEVERE,
                        "Failed to close the simulation"
                                + task.getProxyModelInfrastructure()
                                        .getTicket(), error);
            }
        }

        // If running launching broker locally, shut it down.
        if (_broker != null) {
            try {
                _broker.destroy();
                _broker = null;
            } catch (Exception e) {
                _handleException("The broker could not be stopped.", e);
            }
        }

        // Shut down the servlet container.
        try {
            _servletHost.stop();
            _servletHost.destroy();
            _servletHost = null;
        } catch (Exception e) {
            _handleException("The servlet could not be stopped.", e);
        }

        // Shut down the thread pool that's hosting simulations.
        try {
            _executor.shutdown();
        } catch (Exception e) {
            _handleException("The thread pool could not be shutdown.", e);
        }

        // FindBugs is wrong here, it's OK to set the instance to null since the server is shutting down.
        _requests.clear();
        _instance = null;
    }

    /** Start the execution of the simulation by utilizing a
     *  free thread within the pool.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to start the simulation.
     */
    @Override
    public synchronized void start(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);
            _executor.execute(_requests.get(ticket));
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    /** Stop the execution of the simulation by calling the finish() method
     *  on its Manager.
     *  @param ticket  The ticket reference to the simulation request.
     *  @exception IllegalActionException If the server was unable to stop the simulation.
     */
    @Override
    public synchronized void stop(Ticket ticket) throws IllegalActionException {
        try {
            _checkTicket(ticket);

            // FindBugs is wrong here since _checkTicket ensures that task is not null
            _requests.get(ticket).getManager().finish();
        } catch (Exception e) {
            _handleException((ticket != null ? ticket.getTicketID() : null)
                    + ": " + e.getMessage(), e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The ResourceBundle containing configuration parameters.
     */
    public static final ResourceBundle CONFIG = ResourceBundle
            .getBundle("ptserver.PtolemyServerConfig");

    /** The logger that will record Ptolemy errors to the log file.
     */
    public static final Logger LOGGER;

    /** Export format of the model image.
     */
    public static final String IMAGE_FORMAT = "PNG";

    /** Buffer size to be used when exporting the model image.
     */
    public static final int IMAGE_BUFFER_SIZE = 50000;

    /** The virtual path of the command servlet.
     */
    public static final String SERVLET_NAME = "PtolemyServer";

    /** Roles to classify users during basic authentication.
     */
    public static final String SERVLET_ROLE = "user";

    /** Initialize the logger used for error handling.
     */
    static {
        Logger logger = null;
        FileHandler logFile = null;

        String serverConfig = "PtolemyServerConfig.properties";
        try {
            PtolemyServer._lookupPropertyFile(serverConfig);
        } catch (IOException ex) {
            ExceptionInInitializerError exception = new ExceptionInInitializerError(
                    "Failed to find \"" + serverConfig + "\"");
            exception.initCause(ex);
            throw exception;
        }
        //CONFIG = ResourceBundle.getBundle("ptserver.PtolemyServerConfig");

        ArrayList<PtolemyModule> modules = new ArrayList<PtolemyModule>();
        modules.addAll(ActorModuleInitializer.getModules());
        modules.add(new PtolemyModule(ResourceBundle
                .getBundle("ptserver.util.PTServerModule")));
        PtolemyInjector.createInjector(modules);

        try {
            logger = Logger.getLogger(PtolemyServer.class.getSimpleName());
            logFile = new FileHandler(CONFIG.getString("LOG_FILENAME"), true);
            logFile.setFormatter(new XMLFormatter());
            logger.addHandler(logFile);
            logger.setLevel(Level.SEVERE);
        } catch (SecurityException e) {
            throw new ExceptionInInitializerError(e);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        LOGGER = logger;
    }

    ///////////////////////////////////////////////////////////////////
    ////                   constructor                             ////

    /** Create an instance of the Ptolemy server. This class is a singleton so
     *  only one instance should ever exist at a time.  An embedded servlet container
     *  is initialized for the servlet (synchronous command handler).
     *
     *  <p>If any of the parameters are null, then values in the
     *  ptserver.PtolemyServerConfig resource are checked.  Typically, this file
     *  may be found as $PTII/ptserver/PtolemyServerConfig.properties.</p>
     *
     *  @param servletPort The port on which the servlet operates.
     *  @param brokerPath The path to the broker executable.
     *  @param brokerAddress The host address of the MQTT broker.
     *  @param brokerPort The port of the broker.
     *  @param modelDirectory The root directory of where model files are stored.
     *  If the value of the modelDirectory is not a directory, then a directory relative
     *  to the value of the ptolemy.ptII.dir (aka $PTII) property is checked.
     *  If that directory does not exist, then $PTII/ptserver/demo is used.
     *  @exception IllegalActionException If the server was unable to load the default
     *  configuration from the resource file.
     */
    private PtolemyServer(int servletPort, String brokerPath,
            String brokerAddress, int brokerPort, String modelDirectory)
            throws IllegalActionException {
        try {
            // If not passed, attempt to pull from configuration.
            brokerPath = brokerPath != null ? brokerPath : CONFIG
                    .getString("BROKER_PATH");
            brokerAddress = brokerAddress != null ? brokerAddress : CONFIG
                    .getString("BROKER_ADDRESS") != null ? CONFIG
                    .getString("BROKER_ADDRESS") : InetAddress.getLocalHost()
                    .getHostAddress();
            brokerPort = brokerPort > 0 ? brokerPort : Integer.parseInt(CONFIG
                    .getString("BROKER_PORT"));
            servletPort = servletPort > 0 ? servletPort : Integer
                    .parseInt(CONFIG.getString("SERVLET_PORT"));
            modelDirectory = modelDirectory != null ? modelDirectory : CONFIG
                    .getString("MODELS_DIRECTORY");

            // If path is specified, attempt to launch the broker process.
            if (_configureBroker(brokerPath, brokerPort)) {
                brokerAddress = InetAddress.getLocalHost().getHostAddress();
            }

            //MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
            //            _configuration = ConfigurationApplication
            //                    .readConfiguration(ConfigurationApplication
            //                            .specToURL("ptolemy/configs/full/configuration.xml"));
            _brokerUrl = String
                    .format("tcp://%s@%s", brokerAddress, brokerPort);
            _servletUrl = String
                    .format("http://%s:%s/%s", InetAddress.getLocalHost()
                            .getHostAddress(), servletPort, SERVLET_NAME);
            if (modelDirectory.length() == 0) {
                modelDirectory = StringUtilities.getProperty("user.dir");
            }
            _modelsDirectory = modelDirectory;
            _servletHost = new Server(servletPort);
            _servletHost.setHandler(_configureServlet());
            _servletHost.start();
            _executor = Executors.newCachedThreadPool();
            _requests = new ConcurrentHashMap<Ticket, SimulationTask>();

            Timer timer = new Timer("PtolemyServer timer");
            timer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    for (SimulationTask task : _requests.values()) {
                        LOGGER.info(task.getProxyModelInfrastructure()
                                .getTicket()
                                + " latency "
                                + task.getProxyModelInfrastructure()
                                        .getPingPongLatency());
                    }
                }
            }, 1000, 1000);
            File file = new File(_modelsDirectory);
            if (!file.isDirectory()) {
                String ptII = StringUtilities.getProperty("ptolemy.ptII.dir");
                file = new File(ptII, _modelsDirectory);
                if (!file.isDirectory()) {
                    File oldFile = file;
                    file = new File(ptII, "ptserver" + File.separator + "demo");
                    if (!file.isDirectory()) {
                        throw new IllegalArgumentException(
                                "Models directory \"" + _modelsDirectory
                                        + "\" is invalid directory/path."
                                        + " (Also tried \"" + oldFile
                                        + "\" and \"" + file + "\".");
                    } else {
                        _modelsDirectory = file.getCanonicalPath();
                    }
                } else {
                    _modelsDirectory = file.getCanonicalPath();
                }
                System.out.println("models directory is " + _modelsDirectory);
            }
        } catch (Throwable e) {
            _handleException("Unable to initialize Ptolemy server.", e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check to ensure that the provided ticket is valid and refers to
     *  a current simulation request on the server.
     *  @param ticket The ticket to be validated.
     *  @exception IllegalActionException If the ticket is null or does not
     *  reference a valid simulation request on the server.
     */
    private void _checkTicket(Ticket ticket) throws IllegalActionException {
        if (ticket == null) {
            throw new IllegalActionException("The ticket was null.");
            // TODO: create InvalidTicketException
        } else if (_requests.get(ticket) == null) {
            throw new IllegalActionException(
                    "The ticket does not reference a simulation: "
                            + ticket.getTicketID());
            // TODO: create InvalidTicketException
        }
    }

    /** Configure and launch the broker if path has been specified.
     *  @param brokerPath The path to the broker.
     *  @param brokerPort The port of the broker.
     *  @return True if the broker was started locally, false otherwise.
     *  @exception IllegalActionException If the broker could not be started.
     */
    private boolean _configureBroker(String brokerPath, int brokerPort)
            throws IllegalActionException {

        if (brokerPath != null && brokerPath.length() > 0) {
            try {
                _broker = new ProcessBuilder(new String[] { brokerPath, "-p",
                        String.valueOf(brokerPort) }).redirectErrorStream(true)
                        .start();

                return true;
            } catch (IOException e) {
                _handleException(
                        "Unable to spawn MQTT broker process at '" + brokerPath
                                + "' on port " + String.valueOf(brokerPort)
                                + ".", e);
            }
        }

        return false;
    }

    /** Configure the context handler of the servlet.
     *  @return The servlet context handler.
     *  @exception URISyntaxException
     *  @exception IOException
     */
    private ServletContextHandler _configureServlet()
            throws URISyntaxException, IOException {
        // Set up the servlet context and security settings.
        // Optional: use com.eclipse.Util.Password for hashing

        // Define the constraints for this servlet.
        ConstraintMapping mapping = new ConstraintMapping();
        mapping.setConstraint(new Constraint(Constraint.__BASIC_AUTH,
                SERVLET_ROLE));
        mapping.setPathSpec("/" + SERVLET_NAME);
        mapping.getConstraint().setAuthenticate(true);

        File userFile = PtolemyServer
                ._lookupPropertyFile("PtolemyServerUsers.properties");

        // Define the security context.
        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setLoginService(new HashLoginService("PtolemyServer", userFile
                .toURI().toURL().toExternalForm()));
        security.setConstraintMappings(new ConstraintMapping[] { mapping });

        // Assign the servlet container context.
        ServletContextHandler context = new ServletContextHandler(_servletHost,
                "/", ServletContextHandler.SESSIONS);
        context.setSecurityHandler(security);
        context.addServlet(new ServletHolder(ServerManager.class), "/"
                + SERVLET_NAME);

        return context;
    }

    /** Get actor graph image in PNG format.
     *  @param modelUrl The model file to capture an image for.
     *  @return The byte array of the PNG image.
     */
    private byte[] _getModelImage(URL modelUrl) {
        //ByteArrayOutputStream output = new ByteArrayOutputStream(
        //        IMAGE_BUFFER_SIZE);
        try {
            //            // Save existing filters.
            //            List filterList = MoMLParser.getMoMLFilters();
            //
            //            // Load the model file.
            //            CompositeEntity topLevelActor = ServerUtility
            //                    .openModelFile(modelUrl);
            //
            //            // Get the window properties and sizing.
            //            WindowPropertiesAttribute window = (WindowPropertiesAttribute) topLevelActor
            //                    .getAttribute("_windowProperties");
            //            ArrayToken dimensions = (ArrayToken) ((RecordToken) window
            //                    .getToken()).get("bounds");
            //
            //            // Create the dummy controller.
            //            ActorEditorGraphController controller = new ActorEditorGraphController();
            //            controller.setConfiguration(_configuration);
            //
            //            // Configure the graph & export.
            //            JGraph graph = new JGraph(new BasicGraphPane(controller,
            //                    new ActorGraphModel(topLevelActor), topLevelActor));
            //            graph.setVisible(true);
            //            graph.setSize(((IntToken) dimensions.getElement(2)).intValue(),
            //                    ((IntToken) dimensions.getElement(3)).intValue());
            //            graph.exportImage(output, IMAGE_FORMAT);
            //
            //            // Reset back to previous filters.
            //            MoMLParser.setMoMLFilters(filterList);
        } catch (Exception e) {
            MessageHandler.error(e.getMessage(), e);
        }

        return new byte[0];
        //return output.toByteArray();
    }

    /** Log the message and exception into the Ptolemy server log.
     *  @param message  Descriptive message about what caused the error.
     *  @param error The exception that was raised.
     *  @exception IllegalActionException Always throws the exception in order to propogate
     *  the error through the Ptolemy hierarchy.
     */
    private void _handleException(String message, Throwable error)
            throws IllegalActionException {
        PtolemyServer.LOGGER.log(Level.SEVERE, message, error);
        throw new IllegalActionException(null, error, message);
    }

    /** Look up a propertyBaseName in $CLASSPATH/ptserver.
     *  If $PTII/ptserver/propertyBaseName.properties does
     *  not exist, copy $PTII/ptserver/propertyBaseName.properties.default
     *  to that $PTII/ptserver/propertyBaseName.properties.
     *  @param propertyBaseName  The name of properties file without
     *  any directory information.
     *  @return The File named by propertyBaseName
     *  @exception IOException If thrown while looking for the default
     *  file or copying the default file.
     */
    private static File _lookupPropertyFile(String propertyBaseName)
            throws IOException {
        // This method is necessary so that we can copy the *.properties.default files
        // to *.properties automatically.  This makes the tests easier to run
        // without requiring user setup.

        // Find the user file for authentication.
        //String location = ptolemy.util.StringUtilities
        //        .getProperty("ptolemy.ptII.dir")
        //        + File.separator
        //        + "ptserver"
        //        + File.separator + propertyBaseName;

        String location = "$CLASSPATH/ptserver/" + propertyBaseName;

        File userFile = FileUtilities.nameToFile(location, null);
        if (!userFile.exists()) {

            // Attempt to create it using the supplied default file.
            URL defaultFile = FileUtilities.nameToURL(location + ".default",
                    null, null);
            if (!FileUtilities.binaryCopyURLToFile(defaultFile, userFile)) {
                throw new IllegalStateException("The server users file \""
                        + userFile + "\"could not be found and \""
                        + defaultFile + "\" the copy failed.");
            }
        }
        return userFile;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The proxy model listener that is notified of exceptions within
     *  the other threads of the application.
     */
    private final ProxyModelListener _remoteModelListener = new ProxyModelAdapter() {

        /** React to the remote connection expiring.
         *  @param remoteModel The remote model whose connection has expired.
         */
        @Override
        public void modelConnectionExpired(ProxyModelInfrastructure remoteModel) {
            LOGGER.severe("Removing model " + remoteModel.getTicket());
            LOGGER.severe("Last pong was " + remoteModel.getPingPongLatency()
                    + " ms ago");

            try {
                close(remoteModel.getTicket());
            } catch (Throwable e) {
                String message = "The connection expired on ticket "
                        + remoteModel.getTicket().getTicketID()
                        + " and the model could not be closed.";
                PtolemyServer.LOGGER.log(Level.SEVERE, message, e);
            }
        }

        @Override
        public void modelException(
                ProxyModelInfrastructure proxyModelInfrastructure,
                String message, Throwable exception) {
            PtolemyServer.LOGGER.log(Level.SEVERE,
                    "Unhandled exception in model "
                            + proxyModelInfrastructure.getTicket()
                                    .getTicketID()
                            + " that is being propagated to the client",
                    exception);
            try {
                proxyModelInfrastructure.getTokenPublisher().sendToken(
                        new RemoteEventToken(message, exception), null);
            } catch (Throwable e) {
                // In order to prevent infinite recursive calls, just print the stack trace since
                // there is nothing one could do.
                PtolemyServer.LOGGER.log(Level.SEVERE,
                        "Problem sending exception event for "
                                + proxyModelInfrastructure.getTicket()
                                        .getTicketID(), exception);
            } finally {
                try {
                    PtolemyServer.getInstance().close(
                            proxyModelInfrastructure.getTicket());
                } catch (Throwable e) {
                    PtolemyServer.LOGGER.log(Level.SEVERE,
                            "Problem sending exception event for "
                                    + proxyModelInfrastructure.getTicket()
                                            .getTicketID(), exception);
                }
            }
        }

        @Override
        public void onRemoteEvent(
                ProxyModelInfrastructure proxyModelInfrastructure,
                RemoteEventToken event) {
            PtolemyServer.LOGGER.info("Remote event was received for model."
                    + proxyModelInfrastructure.getTicket().getTicketID() + "\n"
                    + event.toString());
        }

    };

    /** The process reference to the MQTT broker.
     */
    private Process _broker;

    /** The full URL of the MQTT message broker.
     */
    private String _brokerUrl;

    //    /** The MOML configuration.
    //     */
    //    private Configuration _configuration;

    /** The service that manages the simulation thread pool.
     */
    private ExecutorService _executor;

    /** The Ptolemy server singleton.
     */
    private static PtolemyServer _instance;

    /** The server's internal reference to the list of simulation requests.
     */
    private ConcurrentHashMap<Ticket, SimulationTask> _requests;

    /** The local directory in which model files are stored.
     */
    private String _modelsDirectory;

    /** The embedded Jetty servlet container that hosts the command servlet.
     */
    private Server _servletHost;

    /** The URL to the servlet application.
     */
    private String _servletUrl;
}
