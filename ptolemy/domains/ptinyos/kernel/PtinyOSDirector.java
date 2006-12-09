/*  A director for generating, compiling, and simulating nesC code from
 TinyOS components.

 Copyright (c) 2005-2006 The Regents of the University of California.
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
package ptolemy.domains.ptinyos.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// PtinyOSDirector

/**
   A director for generating, compiling, and simulating nesC code from
   TinyOS components.

   <p>TinyOS is an event-driven operating system designed for sensor
   network nodes that have very limited resources (e.g., 8K bytes of
   program memory, 512 bytes of RAM).  (More info at
   http://www.tinyos.net).

   <p>nesC is an extension to the C programming language designed to
   embody the structuring concepts and execution model of TinyOS.
   (More info at http://nescc.sourceforge.net).

   <p>TOSSIM is a C-based simulator for homogeneous TinyOS networks,
   where all nodes run the same TinyOS program.  (More info at
   http://www.cs.berkeley.edu/~pal/research/tossim.html).

   This version of the PtinyOSDirector is for use with TinyOS 1.x
   only.  It is not compatible with TinyOS 2.x.
   
   When embedded in a model containing nesC components (component
   descriptions converted to MoML from the TinyOS 1.x library using
   $PTII/ptolemy/domains/ptinyos/util/nc2moml/nc2moml), this director
   can generate the top-level nesC application file (.nc).  This
   director can also compile the nesC application for use with any
   TinyOS 1.x-compatible hardware (e.g., mica), or for simulation
   within Ptolemy II.

   When this director is used in simulation mode, the user sets the
   <i>target</i> parameter to ptII.  This director then generates a
   Java loader file (.java), in addition to the nesC application file
   (.nc) and makefile that it generates by default.  This director
   uses the nesC compiler to compile the .nc file into a pre-processed
   C file (TOSSIM).  It then uses a C compiler (usually gcc, though it
   can use another compiler if $PTCC is modified) to compile the C
   file into a shared object (the TOSSIM shared object).  Then, it
   uses the Java compiler to compile the Java loader file into a
   .class file.  Finally, this director loads the resulting .class,
   which loads the TOSSIM shared object.

   For more information on compilation rules, see:
     $TOSROOT/contrib/ptII/ptinyos/tools/make/ptII/ptII.rules

   TOSSIM contains its own discrete event simulation engine, which
   consists of a main scheduling loop and a discrete event queue.
   Events in this queue are ordered by timestamp, which is a long long
   (a 64-bit integer on most systems; this is a standard type used in
   gcc).  After initializing its data structures and performing other
   initialization routines, TOSSIM creates a boot-up event and places
   it in its event queue.  The version of TOSSIM compiled by this
   director contains additional calls that are not in the original
   version of TOSSIM.  These calls are JNI calls that cause the TOSSIM
   scheduler to communicate all events to the PtinyOSDirector, and
   allow events (and sensor values) generated by Ptolemy II to be
   passed to the TOSSIM scheduler.

   When the nesC compiler generates the pre-processed C file for
   TOSSIM, it automatically generates support for homogeneous networks
   by instrumenting all component state variables with an array.  The
   array stores the state for each node.  Therefore, array index 0
   stores the state for node 0, and so on.  This director only uses
   one node per instance of TOSSIM, and hence, only uses array index 0
   for all variables.  Models containing multiple nodes are created by
   using a separate PtinyOSDirector (and hence a separate instance of
   TOSSIM) for each node.  In TOSSIM, node 0 is the base station,
   which is the sink for routing.  This director overrides the
   built-in id number using the <i>nodeID<i> and <i>isBaseStation</i>
   parameters, and passes these values to the nesC compiler so that
   they are hard coded into TOSSIM.

   TOSSIM uses server sockets attached to network ports for commands
   and events in order to communicate with external tools, such as
   TinyViz, a Java-based visualization tool for TOSSIM.  We retain
   these ports for backwards compatibility with TinyViz and other
   tools.  The port numbers are set in <i>commandPort</i> and
   <i>eventPort</i>.  Because of limitations in the implementation of
   TOSSIM, a separate instance of TinyViz must be attached to each
   instance of TOSSIM.
   
   This director contains several parameters:
   <ol>
   <li><i>destinationDirectory</i>: Output directory for generated code.
   <li><i>confirmOverwrite</i>: Flag to ask for confirmation before overwriting.
   <li><i>TOSROOT</i>: Path to the root of the TinyOS tree.
   <li><i>TOSDIR</i>: Path to the tos directory of the TinyOS tree.
   <li><i>pflags</i>: Additional flags passed to the nesC compiler.
   <li><i>numberOfNodes</i>: Number of nodes to simulate per instance of TOSSIM.
   <li><i>bootTimeRange</i>: TOSSIM setting for the number of seconds over which nodes may boot.
   <li><i>target</i>: Compilation target for the generated nesC code.
   <li><i>simulate</i>: Flag for choosing whether to simulate in ptII.
   <li><i>isBaseStation</i>: Flag for indicating that this node is a base station.
   <li><i>commandPort</i>: Port number for TOSSIM command server socket.
   <li><i>eventPort</i>: Port number for TOSSIM event server socket.
   <li><i>timeResolution</i>: The time precision used by this director (inherited from Director).
   <li><i>nodeID</i>: Node ID of this node.
   </ol>
   
   <p>NOTE: if <i>target</i> is blank, but simulate is true, then the
   model will assume that the ptII target has already been compiled
   and will attempt to simulate.

   @see ptolemy.actor.lib.io.LineWriter

   @author Elaine Cheong, Yang Zhao, Edward A. Lee
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Yellow (celaine)
   @Pt.AcceptedRating Yellow (celaine)
 */
public class PtinyOSDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name. The director is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     */
    public PtinyOSDirector() {
        super();
        _initializeParameters();
    }

    /** Construct a director in the workspace with an empty name.
     *  The director is added to the list of objects in the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace of this object.
     */
    public PtinyOSDirector(Workspace workspace) {
        super(workspace);
        _initializeParameters();
    }

    /** Construct a director with the specified container and name.
     *  @param container The container.
     *  @param name The name of the director.
     *  @exception IllegalActionException If the director is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PtinyOSDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initializeParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Output directory for generated code.  The value defaults to
     *  the current working directory ($CWD).
     */
    public FileParameter destinationDirectory;

    /** Path to the root of the TinyOS tree.  TinyOS can be obtained
     *  from http://www.tinyos.net. The value defaults to
     *  $PTII/vendors/ptinyos/tinyos-1.x
     */
    public FileParameter tosRoot;

    /** Path to the tos directory of the TinyOS tree.  TinyOS can be
     *  obtained from http://www.tinyos.net.  The value defaults to
     *  $PTII/vendors/ptinyos/tinyos-1.x/tos
     */
    public FileParameter tosDir;

    /** Flag to ask for confirmation before overwriting.  If
     *  <i>false</i>, then overwrite the specified file without
     *  asking, if the file exists.  If <i>true</i>, then ask for
     *  confirmation before overwriting, if the file exists.  The
     *  value defaults to false, and is of type BooleanToken.
     */
    public Parameter confirmOverwrite;

    /** Additional flags passed to the nesC compiler.  This can be
     *  used, for example, to include additional compilation
     *  directories.  The value defaults to "-I%T/lib/Counters".
     */
    public StringParameter pflags;

    /** Number of nodes to simulate per instance of TOSSIM.  The value
     *  defaults to 1, is of type IntToken, and is set to be
     *  NOT_EDITABLE.
     */
    public Parameter numberOfNodes;

    /** TOSSIM setting for the number of seconds over which nodes may
     *  boot.  The node(s) boots at a random time within this time
     *  frame (time starts at 0 seconds).  The value defaults to 10
     *  and is of type IntToken.
     */
    public Parameter bootTimeRange;

    /** Compilation target for the generated nesC code.  Target can be
     *  any TinyOS-compatible target, such as mica, mica2, pc, or
     *  ptII.  The value defaults to "ptII".
     */
    public StringParameter target;

    /** Flag for choosing whether to simulate in ptII.  The value
     *  defaults to true, and is of type BooleanToken.
     */
    public Parameter simulate;

    /** Flag for indicating that this node is a base station.  True if
     *  this is a base station.  The value defaults to false, and is
     *  of type BooleanToken.
     */
    public Parameter isBaseStation;

    /** Port number for TOSSIM command server socket.  The value
     *  defaults to 10584.
     */
    public PtinyOSIntegerParameter commandPort;

    /** Port number for TOSSIM event server socket.  This is a public
     *  value so that the port number can be set by hand, if
     *  necessary.  The value defaults to (commandPort + 1).
     */
    public Parameter eventPort;

    /** Node ID of this node.  This is a substitute for
     *  TOS_LOCAL_ADDRESS, which in TOSSIM is normally set to the node
     *  array index.  However, we assume only one node per TOSSIM.  To
     *  avoid all node being set to the same ID, we use this
     *  parameter.  In practice, the range is limited by
     *  <i>numberOfNodes</i>, and should be such that enough network
     *  ports are available for each instance of <i>commandPort</i>
     *  and <i>eventPort</i>, though this is not enforced.  The value
     *  defaults to 1.
     */
    public PtinyOSIntegerParameter nodeID;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** A callback method (from C code) to enqueue the next TOSSIM
     *  event at the specified time.
     *
     *  <p>This is a JNI interface method that gets called by TOSSIM
     *  through the Java loader.
     *
     *  @param newTime The time of the next event, a long long in C (a
     *  64-bit integer on most systems).
     */
    public void enqueueEvent(String newTime) {
        // Assumes that we already checked in preinitialize() that the
        // container is a CompositeActor.
        CompositeActor container = (CompositeActor) getContainer();

        // Get the executive director.  If there is none, use this instead.
        Director director = container.getExecutiveDirector();

        if (director == null) {
            director = this;
        }

        //System.out.println("PtinyOSDirector.enqueueEvent : " + newtime);
        Time t = new Time(director, Long.parseLong(newTime));
        try {
            director.fireAt(container, t);
        } catch (IllegalActionException e) {
            MessageHandler.error(e.toString(), e);
        }
    }

    /** Process one event in the TOSSIM event queue and run tasks in
     *  task queue by invoking TOSSIM through the Java loader.
     *  @exception IllegalActionException If something goes wrong.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire() in PtinyOSDirector.");
        }

        super.fire();
        
        if (((BooleanToken) simulate.getToken()).booleanValue()) {
            CompositeActor container = (CompositeActor) getContainer();

            // Get the executive director.  If there is none, use this instead.
            Director director = container.getExecutiveDirector();

            if (director == null) {
                director = this;
            }

            long currentTimeValue = 0;
            Time currentTime = director.getModelTime();

            if (currentTime != null) {
                // NOTE: this could overflow.
                currentTimeValue = currentTime.getLongValue();
            }

            //System.out.println("PtinyOSDirector.fire: " + currentTimeValue);
            try {
                _loader.processEvent(currentTimeValue);
            } catch (Throwable e) {
                MessageHandler.error("JNI error in fire(): ", e);
            }
        }
    }

    /** Get a DoubleToken from PortParameter and convert it to a char.
     *  We use 0 as the default value if a token cannot be obtained
     *  succesfully.
     *
     *  <p>This is a JNI interface method that gets called by TOSSIM
     *  through the Java loader.
     *
     *  @param parameter The parameter.
     *  @return The parameter value, or 0 if the port is not connected
     *  or not found.
     */
    public char getCharParameterValue(String parameter) {
        if (_debugging) {
            _debug("Called getCharParameterValue with " + parameter
                    + " as argument.");
        }

        CompositeActor model = (CompositeActor) getContainer();
        IOPort port = (IOPort) model.getPort(parameter);
        if (port != null) {
            if (port instanceof ParameterPort) {
                try {
                    Token t = ((ParameterPort) port).getParameter().getToken();
                    if (t != null) {
                        //System.out.println(port.getName() + " " + t);
                        DoubleToken dt = DoubleToken.convert(t);
                        return (char) dt.doubleValue();
                    } else {
                        Exception e = new InternalErrorException(
                                "Could not get token from ParameterPort \""
                                + parameter
                                + "\".");
                        MessageHandler.error(e.toString(), e);
                        return 0;
                    }
                } catch (IllegalActionException e) {
                    MessageHandler.error(e.toString(), e);
                }   
            }
        }

        // Port could not be found.
        return 0;
    }

    /** If the {@link #simulate} parameter is true, then load TOSSIM
     *  shared library and call TOSSIM main(), by using Java loader
     *  calls.
     *
     *  The sequence of calls is as follows (assuming simulate is true):
     *
     *  <ul>
     *  <li> PtinyOSDirector.initialize() 
     *    <ul>
     *    <li> Loads Java PtinyOSLoader class into memory using
     *      ClassLoader.loadClass()
     *    <li> Creates instance of PtinyOSLoader class (_loader),
     *      using Class.newInstance() 
     *    <li> Calls Java _loader.load()
     *    </ul>
     *  <li> Java _loader.load() loads the TOSSIM shared object into
     *    memory, using Java System.load()
     *  <li> PtinyOSDirector.initialize(), continued
     *    <ul>
     *    <li> Calls Java _loader.main()
     *    </ul>
     *  <li> Java _loader.main() calls JNI main_unique_name() native
     *    method.
     *  <li> JNI main_unique_name() calls TOSSIM main(), which starts up
     *    TOSSIM.
     *  </ul>
     *
     *  @exception IllegalActionException If there is a problem initializing
     *  the director, such as a problem loading the JNI loader.
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize() in PtinyOSDirector.");
        }

        super.initialize();

        if (((BooleanToken) simulate.getToken()).booleanValue()) {
            NamedObj toplevel = _toplevelNC();
            String toplevelName = _sanitizedFullName(toplevel);

            // Add the destinationDirectory to the classpath.
            String fileSeparator = StringUtilities
                    .getProperty("file.separator");
            String outputDir = destinationDirectory.stringValue()
                    + fileSeparator + "build" + fileSeparator + "ptII";

            // From: http://javaalmanac.com/egs/java.lang/LoadClass.html
            // Create a File object on the root of the directory
            // containing the class file.
            File file = new File(outputDir);

            try {
                // Convert File to a URL
                // file:/c:/myclasses/
                URI uri = file.toURI();
                URL url = uri.toURL();
                URL[] urls = new URL[] { url };

                // Create a new class loader with the directory
                ClassLoader cl = new URLClassLoader(urls);

                // Load in the class; MyClass.class should be located in
                // the directory file:/c:/myclasses/com/mycompany
                //Class cls = Class.forName("Loader" + toplevelName, true, cl);
                String className = "Loader" + toplevelName;

                if (_debugging) {
                    _debug("About to load '" + className + "'");
                }

                Class cls = cl.loadClass(className);
                Object o = cls.newInstance();

                if (o instanceof PtinyOSLoader) {
                    _loader = (PtinyOSLoader) o;

                    // Call main with the boot up time range and
                    // number of nodes as arguments.
                    String[] argsToMain = {
                            "-b=" + bootTimeRange.getToken().toString(),
                            numberOfNodes.getToken().toString() };

                    if (_debugging) {
                        _debug("Done loading '" + className
                                + "', about to load(" + outputDir + ")");
                    }

                    // Load the library with the native methods for TOSSIM.
                    _loader.load(outputDir, this);

                    if (_debugging) {
                        _debug("Done with load(), about to call main("
                                + argsToMain[0] + ", " + argsToMain[1] + ")");
                    }
                    
                    // Note: For statistical purposes.
                    System.gc();
                    _startTime = (new Date()).getTime();
                    int result = 0;
                    try {
                        result = _loader.main(argsToMain);
                    } catch (Throwable e) {
                        MessageHandler.error("JNI error in main(): ", e);
                    }
                    if (result < 0) {
                        throw new InternalErrorException(
                                "Could not initialize TOSSIM.");
                    }

                    if (_debugging) {
                        _debug("call to main completed");
                    }
                } else {
                    throw new InternalErrorException(
                            "Loader was not instance of PtinyOSLoader.");
                }
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, throwable,
                        "Problem initializing.");
            }
        }
    }

    /** Return true if simulation is requested, so that simulated
     *  event handling can proceed.
     *
     *  Note: We override the postfire() method instead of the
     *  prefire() method because in Manager.iterate(), if prefire()
     *  returns false, true is returned at the end of the iteration
     *  (prefire(), fire(), postfire()) of a model, which is not what
     *  we want if simulation is not requested.
     *
     *  @return The value of the <i>simulate</i> parameter
     *  @exception IllegalActionException If thrown while reading the
     *  <i>simulate</i> parameter.
     */
    public boolean postfire() throws IllegalActionException {
        boolean result = super.postfire();
        return (result &&
                ((BooleanToken) simulate.getToken()).booleanValue());
    }

    /** Generate nesC code (.nc).  If this director is the top most
     *  PtinyOSDirector, and the {@link #target} parameter is
     *  non-empty then a .java loader file and a makefile are created.
     *  This methods then compiles the .nc and .java files by calling
     *  make.  The .java file implements the {@link
     *  ptolemy.domains.ptinyos.kernel.PtinyOSLoader} interface.
     *
     *  The sequence of calls is as follows:
     *
     *  <ul>
     *  <li> Generates nesC code (.nc), makefile, and Java
     *       PtinyOSLoader (.java).
     *  <li> Runs make, using Runtime.exec()
     *  <li> Compiles nesC (.nc) code to a TOSSIM shared object (.so
     *       or .dll), and the Java PtinyOSLoader (.java) to a Java
     *       PtinyOSLoader class (.class).
     *        
     *  @exception IllegalActionException If the container is not
     *  an instance of CompositeActor, the destination directory
     *  does not exist and cannot be created, or the nesC file
     *  cannot be written.
     */
    public void preinitialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called preinitialize() in PtinyOSDirector.");
        }

        super.preinitialize();

        if (!(getContainer() instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Requires the container to be an "
                            + "instance of CompositeActor.");
        }

        // FIXME: old TOSSIM can't be used again.
        //long newVersion = workspace().getVersion();
        _version++;

        // Open directory, creating it if necessary.
        File directory = destinationDirectory.asFile();

        if (!directory.isDirectory()) {
            // Create parent directories as needed.
            if (!directory.mkdirs()) {
                throw new IllegalActionException(this,
                        "Could not create directory " + directory);
            }
        }

        // Generate the code as a string.
        CompositeActor container = (CompositeActor) getContainer();
        String code = _generateCode(container);

        // Create file name relative to the toplevel NCCompositeActor.
        NamedObj toplevel = _toplevelNC(); //container.toplevel();

        String filename = _sanitizedFullName(toplevel);

        if (container != toplevel) {
            filename = filename + "_" + container.getName(toplevel);
            filename = StringUtilities.sanitizeName(filename);
        }

        // Open file for the generated nesC code.
        File writeFile = new File(directory, filename + ".nc");

        if (_confirmOverwrite(writeFile)) {
            // Write the generated code to the file.
            try {
                FileWriter writer = new FileWriter(writeFile);
                writer.write(code);
                writer.close();
            } catch (IOException e) {
                throw new IllegalActionException(this, e,
                        "Failed to open file for writing.");
            }
        }

        // Descend recursively into contained composites.
        Iterator entities = container.entityList(CompositeActor.class)
                .iterator();

        while (entities.hasNext()) {
            CompositeActor contained = (CompositeActor) entities.next();
            contained.preinitialize();
        }

        // If this is the toplevel PtinyOSDirector, generate makefile
        // and compile the generated code.
        if (_isTopLevelNC()) {
            if (target.stringValue() != "") {
                _generateLoader();

                String makefileName = null;
                try {
                    makefileName = _generateMakefile();
                } catch (CancelException ex) {
                    throw new IllegalActionException(this, ex,
                            "Makefile generation cancelled by the user.");
                }
                _compile(makefileName);
            }
        }
    }

    /** Notify the loader that a packet has been received.  The {@link
     *  ptolemy.domains.ptinyos.kernel.PtinyOSCompositeActor#fire()}
     *  method calls this method with the string value of the input
     *  packet.
     *
     *  <p>This is a wrapper for a native method, where the
     *  PtinyOSDirector calls this method (Java) to activate routines
     *  in TOSSIM (C).
     *
     *  @param packet The string value of the input packet.
     *  @exception IllegalActionException If there is a problem reading
     *  the {@link #simulate} parameter.
     */
    public void receivePacket(String packet) throws IllegalActionException {
        if (((BooleanToken) simulate.getToken()).booleanValue()) {
            CompositeActor container = (CompositeActor) getContainer();

            // Get the executive director.  If there is none, use this instead.
            Director director = container.getExecutiveDirector();

            if (director == null) {
                director = this;
            }

            long currentTimeValue = 0;
            Time currentTime = director.getModelTime();

            if (currentTime != null) {
                // NOTE: this could overflow.
                currentTimeValue = currentTime.getLongValue();
            }

            try {
                _loader.receivePacket(currentTimeValue, packet);
            } catch (Throwable e) {
                MessageHandler.error("JNI error in receivePacket(): ", e);
            }
        }
    }

    /** Send an expression to a port.  This is used, for example, to
     *  send LED or packet data from TOSSIM to the rest of the Ptolemy
     *  II model.
     *
     *  <p>This is a JNI interface method that gets called by TOSSIM
     *  through the Java loader.
     *
     *  The sequence of calls is as follows:
     *
     *  <ul>
     *  <li>TOSSIM generates an LED or packet value and calls TOSSIM
     *    ptII_updateLeds() or TOSSIM ptII_send_packet(), both of which
     *    call JNI _loader.sendToPort() with the port name and value as
     *    string arguments.
     *  <li>_loader.sendToPort() calls Java PtinyOSDirector.sendToPort().
     *  <li>PtinyOSDirector.sendToPort() performs type conversion from
     *    a Java string to the appropriate Token type (depending on the
     *    type of the requested port), and sends the token to the
     *    requested port using the Java Ptolemy II Port.send() method.
     *  </ul>
     *
     *  @param portName The name of the port
     *  @param expression The expression
     *  @return true if the expression was successfully sent, false if the
     *  port is not connected or not found.
     */
    public boolean sendToPort(String portName, String expression) {
        CompositeActor model = (CompositeActor) getContainer();
        TypedIOPort port = (TypedIOPort) model.getPort(portName);
        if (port != null) {
            if (port.getWidth() > 0) {
                if (port.getType() == BaseType.BOOLEAN) {
                    try {
                        port.send(0, new BooleanToken(expression));
                    } catch (IllegalActionException e) {
                        MessageHandler.error(e.toString(), e);
                    }
                    return true;
                } else if (port.getType() == BaseType.STRING) {
                    try {
                        port.send(0, new StringToken(expression));
                    } catch (IllegalActionException e) {
                        MessageHandler.error(e.toString(), e);
                    }
                    return true;
                } else {
                    Exception e = new InternalErrorException(
                            "Handler for port \""
                            + portName
                            + "\" with type \""
                            + port.getType()
                            + "\" not implemented.");
                    MessageHandler.error(e.toString(), e);
                }
            } else {
                // Port not connected to anything.
                return false;
            }
        }
        // Port could not be found.
        return false;
    }

    /** A callback method (from C code) for the application to print a
     *  debug message.
     *
     *  <p>This is a JNI interface method that gets called by TOSSIM
     *  through the Java loader.       
     *
     *  @param debugMode A long long in C (currently unused)
     *  @param message A char * in C
     *  @param nodeID A short in C
     */
    public void tosDebug(String debugMode, String message, String nodeID) {
        if (_debugging) {
            // Remove leading and trailing whitespace.
            String trimmedMessage = message.trim();

            if (nodeID != null) {
                _debug(nodeID + ": " + trimmedMessage);
            } else {
                _debug(trimmedMessage);
            }
        }
    }

    /** Call wrapup() in TOSSIM to shut down threads created in initialize().
     *
     *  The sequence of calls is as follows:
     *  <ul>
     *  <li> PtinyOSDirector.wrapup() 
     *       <ul>
     *       <li> Calls Java _loader.wrapup()
     *       </ul>
     *  <li> _loader.wrapup() calls JNI wrapup_unique_name() native method.
     *  <li> JNI wrapup_unique_name() calls TOSSIM shutdownSockets(), which: 
     *       <ul>
     *       <li>Closes sockets.
     *       <li>Joins threads by calling TOSSIM ptII_joinThreads(),
     *           which calls JNI _loader.joinThreads().
     *       </ul>
     *  <li>_loader.joinThreads() calls Java Thread.join() on all
     *      threads created during initialization. Returns Java int value
     *      of 0 upon success.
     *  <li>JNI _loader.joinThreads() returns jint value of 0 upon
     *      success, which is converted into a C int and returned by
     *      ptII_joinThreads(). If successful, TOSSIM execution stops.
     *  </ul>
     *
     *  <p>This is a wrapper for a native method, where the
     *  PtinyOSDirector calls this method (Java) to activate routines
     *  in TOSSIM (C).
     *        
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        
        // Note: For statistical purposes.
        System.err.println(Manager.timeAndMemory(_startTime)
                + " "
                + _sanitizedFullName(this));
        
        if (_debugging) {
            _debug("Called wrapup() in PtinyOSDirector.");
        }

        if (((BooleanToken) simulate.getToken()).booleanValue()) {
            if (_loader != null) {
                try {
                    _loader.wrapup(); // SIGSTOP: man 7 signal
                } catch (Throwable e) {
                    MessageHandler.error("JNI error in wrapup(): ", e);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // Methods for accessing sockets from TOSSIM in
    // $TOSROOT/contrib/ptII/ptinyos/tos/platform/ptII/external_comm.c
    //
    // The port number used is defined in the commandPort and
    // eventPort parameters.
    ///////////////////////////////////////////////////////////////////////

    /** Accept a connection on a ServerSocketChannel.  If serverSocketChannel 
     *  is blocking, this method blocks.
     * 
     * @param serverSocketChannel The ServerSocketChannel on which connections are accepted.
     * @return The SocketChannel for the connection that was accepted.
     */
    public SocketChannel acceptConnection(SelectableChannel serverSocketChannel) {
        if (serverSocketChannel instanceof ServerSocketChannel) {
            SocketChannel socketChannel = null;
            try {
                // Accept the connection request.
                // If serverSocketChannel is blocking, this method blocks.
                // The returned channel is in blocking mode.
                socketChannel = ((ServerSocketChannel) serverSocketChannel)
                        .accept();
            } catch (IOException e) {
                System.out.println(e);
            }
            return socketChannel;
        } else {
            System.out
                    .println("The argument passed to acceptConnection() was not a ServerSocketChannel.");
            return null;
        }
    }

    /** Close the selector.
     * 
     *  @param selector The selector that should be closed.
     */
    public void selectorClose(Selector selector) {
        if (selector != null) {
            try {
                if (_debugging) {
                    _debug("Begin call to selectorClose()");
                }
                // We have to call wakeup() here because of a possible bug in J2SE
                //
                // Note that this also requires a locking machnism to
                // prevent selector.select() from being called again after
                // the selector wakes up but before close() is called.
                // See:
                // http://forum.java.sun.com/thread.jspa?threadID=293213&messageID=2671029
                selector.wakeup();
                selector.close();
                if (_debugging) {
                    _debug("End call to selectorClose()");
                }
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /** Create a selector and register the ServerSocketChannel of the 
     *  ServerSocket with the selector.
     * 
     *  @param serverSocket The ServerSocket whose channel should be registered
     *      with the Selector created.
     *  @param opAccept True if this SelectionKey option that should be enabled 
     *      when registering the ServerSocketChannel to the Selector.
     *  @param opConnect True if this SelectionKey option that should be enabled 
     *      when registering the ServerSocketChannel to the Selector.
     *  @param opRead True if this SelectionKey option that should be enabled 
     *      when registering the ServerSocketChannel to the Selector.
     *  @param opWrite True if this SelectionKey option that should be enabled 
     *      when registering the ServerSocketChannel to the Selector.
     *  @return The Selector created.
     */
    public Selector selectorCreate(ServerSocket serverSocket, boolean opAccept,
            boolean opConnect, boolean opRead, boolean opWrite) {
        Selector selector = null;
        try {
            // Create the selector
            selector = Selector.open();

            if (_debugging) {
                _debug("Created selector.");
            }

            ServerSocketChannel serverSocketChannel = serverSocket.getChannel();
            if (serverSocketChannel != null) {
                // Register channel with selector.
                if (opAccept) {
                    serverSocketChannel.register(selector,
                            SelectionKey.OP_ACCEPT);
                }
                if (opConnect) {
                    serverSocketChannel.register(selector,
                            SelectionKey.OP_CONNECT);
                }
                if (opRead) {
                    serverSocketChannel
                            .register(selector, SelectionKey.OP_READ);
                }
                if (opWrite) {
                    serverSocketChannel.register(selector,
                            SelectionKey.OP_WRITE);
                }
            } else {
                System.out
                        .println("Could not find ServerSocketChannel associated with ServerSocket!");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return selector;
    }

    /** Register channel with selector.
     * 
     *  @param selector The selector to which the channel should be registered.
     *  @param socketChannel The SocketChannel that should be registered.
     *  @param opAccept True if this SelectionKey option that should be enabled 
     *      when registering the SocketChannel to the Selector.
     *  @param opConnect True if this SelectionKey option that should be enabled 
     *      when registering the SocketChannel to the Selector.
     *  @param opRead True if this SelectionKey option that should be enabled 
     *      when registering the SocketChannel to the Selector.
     *  @param opWrite True if this SelectionKey option that should be enabled 
     *      when registering the SocketChannel to the Selector.
     */
    public void selectorRegister(Selector selector,
            SelectableChannel socketChannel, boolean opAccept,
            boolean opConnect, boolean opRead, boolean opWrite) {

        if (selector != null && socketChannel != null) {
            try {
                // Register channel with selector.
                if (opAccept) {
                    socketChannel.register(selector, SelectionKey.OP_ACCEPT);
                }
                if (opConnect) {
                    socketChannel.register(selector, SelectionKey.OP_CONNECT);
                }
                if (opRead) {
                    socketChannel.register(selector, SelectionKey.OP_READ);
                }
                if (opWrite) {
                    socketChannel.register(selector, SelectionKey.OP_WRITE);
                }
            } catch (ClosedChannelException e) {
                System.out.println(e);
            }
        }
    }

    /** Returns a selected channel, or null if none.
     *  
     *  @param selector The channel selector.
     *  @param notNullIfClosing TRUE if returning NULL, otherwise left
     *  as is.  We use notNullIfClosing because of threading issues
     *  discussed in {@link #selectorClose(Selector selector)}.
     *  @param opAccept True if this SelectionKey option that should
     *  be enabled when returning a non-null SelectableChannel.
     *  @param opConnect True if this SelectionKey option that should
     *  be enabled when returning a non-null SelectableChannel.
     *  @param opRead True if this SelectionKey option that should be
     *  enabled when returning a non-null SelectableChannel.
     *  @param opWrite True if this SelectionKey option that should be
     *  enabled when returning a non-null SelectableChannel.
     *  @return The selected channel, or null if none.
     */
    public SelectableChannel selectSocket(Selector selector,
            boolean[] notNullIfClosing, boolean opAccept, boolean opConnect,
            boolean opRead, boolean opWrite) {
        notNullIfClosing[0] = false;
        try {
            // Wait for an event
            if (selector.select() > 0) {
                // Get list of selection keys with pending events
                Iterator iterator = selector.selectedKeys().iterator();

                // Process each key
                if (iterator.hasNext()) {
                    // Get the selection key
                    SelectionKey selKey = (SelectionKey) iterator.next();

                    // Remove it from the list to indicate that it is being processed
                    iterator.remove();

                    // Check if it's a connection request
                    if ((opAccept && selKey.isAcceptable())
                            || (opConnect && selKey.isConnectable())
                            || (opRead && selKey.isReadable())
                            || (opWrite && selKey.isWritable())) {
                        // Get channel with connection request
                        SelectableChannel ssChannel = selKey.channel();
                        return ssChannel;
                    }
                }
            }
        } catch (IOException e) {
            // Handle error with selector.
            System.out.println("Exception in selectSocket(): " + e);
        } catch (CancelledKeyException e) {
            // Handle error with selector.
            System.out.println("Exception in selectSocket(): " + e);
        } catch (ClosedSelectorException e) {
            // Handle error with selector.
            System.out.println("Exception in selectSocket(): " + e);
        }
        // Return null if this method didn't find a channel above.
        // This means the selector is about to close.
        notNullIfClosing[0] = true;
        return null;
    }

    /** Close the ServerSocket.
     * 
     *  @param serverSocket The ServerSocket to be closed.
     */
    public void serverSocketClose(ServerSocket serverSocket) {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                System.out.println(e);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    /** Create a non-blocking server socket and check for connections on the 
     *   port specified by <i>port</i>.
     *   @param port The port on which to create a server socket.
     *   @return The ServerSocket created.
     */
    public ServerSocket serverSocketCreate(short port) {
        ServerSocket serverSocket = null;
        try {
            // Create non-blocking server sockets on port.
            ServerSocketChannel serverSocketChannel = ServerSocketChannel
                    .open();
            serverSocketChannel.configureBlocking(false);
            serverSocket = serverSocketChannel.socket();
            try {
                serverSocket.setReuseAddress(true);
            } catch (SocketException e) {
                System.out.println(e);
            }
            serverSocket.bind(new InetSocketAddress(
                    InetAddress.getByName(null), port));
        } catch (IOException e) {
            System.out.println(e);
        }
        return serverSocket;
    }

    /** Close the SocketChannel.
     * 
     * @param socketChannel The SocketChannel to close.
     */
    public void socketChannelClose(SelectableChannel socketChannel) {
        if (socketChannel instanceof SocketChannel) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        } else {
            System.out
                    .println("The argument passed to socketChannelClose() was not a SocketChannel.");
        }
    }

    /** Read from a SocketChannel into readBuffer.  
     *  Returns number of bytes read.  0 if end of stream reached.  -1 if error.
     * 
     * @param socketChannel SocketChannel from which to read.
     * @param readBuffer The bytes read.
     * @return Number of bytes read.  0 if end of stream reached.  -1 if error.
     */
    public int socketChannelRead(SocketChannel socketChannel, byte[] readBuffer) {
        // Create a direct buffer to get bytes from socket.
        // Direct buffers should be long-lived and be reused as much as possible.
        ByteBuffer buffer = ByteBuffer.wrap(readBuffer);
    
        try {
            // Clear the buffer and read bytes from socket
            buffer.clear();
            int numBytesRead = socketChannel.read(buffer);
    
            if (numBytesRead == -1) {
                // No more bytes can be read from the channel
                socketChannel.close(); // FIXME: is this ok?
                return 0; // Reached end of stream
            } else {
                return numBytesRead;
            }
        } catch (IOException e) {
            // Connection may have been closed
            System.out.println(e);
            return -1;
        }
    }

    /** Write the bytes in writeBuffer to a SocketChannel.
     *  Returns number of bytes written.  -1 if error.
     * 
     *  @param socketChannel The SocketChannel on which to write.
     *  @param writeBuffer The bytes to write.
     *  @return Number of bytes written.  -1 if error.
     */
    public int socketChannelWrite(SocketChannel socketChannel,
            byte[] writeBuffer) {
        // Create a direct buffer to write bytes to socket.
        // Direct buffers should be long-lived and be reused as much as possible.
        ByteBuffer buffer = ByteBuffer.wrap(writeBuffer);

        try {
            // Write bytes
            int numBytesWritten = socketChannel.write(buffer);
            return numBytesWritten;
        } catch (IOException e) {
            // Connection may have been closed
            System.out.println(e);
            return -1;
        }
    }

    /** Compile the generated code by calling make on the generated
     *  file from _generateMakefile().
     *  @param makefileName The name of the makefile.
     *  @exception IllegalActionException If there is a problem accessing
     *  the {@link #destinationDirectory} or {@link #target} parameters
     *  or if the make subprocess fails or returns a non-zero value.
     */
    private void _compile(String makefileName) throws IllegalActionException {
        // The command we run is:
        // make
        // -C : change to the destination directory before reading
        // the makefile (FIXME: This is a GNU make extension)
        // -f : Use makefileName as the makefile
        // Use an array so we can handle strings with spaces
        String[] command = { "make", "-C",
                destinationDirectory.stringValue().replace('\\', '/'), "-f",
                makefileName, target.stringValue() };

        // Used for error handling.
        StringBuffer commandString = new StringBuffer(command[0]);

        for (int i = 1; i < command.length; i++) {
            commandString.append(" " + command[i]);
        }

        System.out.println(commandString.toString());

        int exitValue = 0;

        StringWriter errorWriter = new StringWriter();
        StringWriter outputWriter = new StringWriter();
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);

            // Connect a thread to the error stream of the cmd process.
            _StreamReaderThread errorGobbler = new _StreamReaderThread(proc
                    .getErrorStream(), "ERROR", errorWriter);

            // Connect a thread to the output stream of the cmd process.
            _StreamReaderThread outputGobbler = new _StreamReaderThread(proc
                    .getInputStream(), "OUTPUT", outputWriter);

            // Start the threads.
            errorGobbler.start();
            outputGobbler.start();

            // Wait for exit and see if there are any errors.
            // make returns non-zero value if there was an error
            exitValue = proc.waitFor();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Could not compile generated code, \"" + commandString
                            + "\" failed. " + "Output:\n" + outputWriter
                            + "Error:\n" + errorWriter);
        }

        if (exitValue != 0) {
            throw new IllegalActionException(
                    "Running \""
                            + commandString
                            + "\" returned '"
                            + exitValue
                            + "', which is a nonzero value, which indicates an error.\n"
                            + "Output:\n" + outputWriter + "Error:\n"
                            + errorWriter);
        }
    }

    /** Confirm overwrite of file if the confirmOverwrite parameter is
     *  set to true.  Return true if ok to write file, throw
     *  IllegalActionException if the user cancels the ovewrite.
     *  @param file File to be written.
     *  @return True if ok to write file.
     *  @exception IllegalActionException If code generation should be halted.
     */
    private boolean _confirmOverwrite(File file) throws IllegalActionException {
        boolean confirmOverwriteValue = ((BooleanToken) confirmOverwrite
                .getToken()).booleanValue();

        if (confirmOverwriteValue && file.exists()) {
            try {
                if (MessageHandler.yesNoCancelQuestion("Overwrite " + file
                        + "?")) {
                    if (!file.delete()) {
                        throw new IllegalActionException(this,
                                "Could not delete file " + file);
                    }
                } else {
                    return false;
                }
            } catch (CancelException ex) {
                throw new IllegalActionException(this,
                        "Cancelled overwrite of " + file);
            }
        }

        return true;
    }

    /** Generate NC code for the given model. This does not descend
     *  hierarchically into contained composites. It simply generates
     *  code for the top level of the specified model.
     *  @param model The model for which to generate code.
     *  @return The NC code.
     */
    private String _generateCode(CompositeActor model)
            throws IllegalActionException {
        CompositeActor container = (CompositeActor) getContainer();

        //NamedObj toplevel = _toplevelNC();
        _CodeString generatedCode = new _CodeString();

        String containerName = _sanitizedFullName(model);

        generatedCode.addLine("configuration " + containerName + " {");

        //if (container != toplevel) {
        if (!(container instanceof PtinyOSCompositeActor)) {
            generatedCode.add(_interfaceProvides(model));
            generatedCode.add(_interfaceUses(model));
        }

        generatedCode.addLine("}");
        generatedCode.addLine("implementation {");
        generatedCode.add(_includeModule(model));
        generatedCode.add(_includeConnection(model));
        generatedCode.addLine("}");

        return generatedCode.toString();
    }

    /** Generate loader for shared library

===== Begin example Loader.java (from Dec 4, 2006) =====
import java.net.ServerSocket;
import java.nio.channels.Selector;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import ptolemy.domains.ptinyos.kernel.PtinyOSLoader;
import ptolemy.domains.ptinyos.kernel.PtinyOSDirector;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.MessageHandler;        
public class Loader_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0 implements PtinyOSLoader {
    public void load(String path, PtinyOSDirector director) {
        String fileSeparator = System.getProperty("file.separator");
        String toBeLoaded = path + fileSeparator + System.mapLibraryName("_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0");
        toBeLoaded = toBeLoaded.replace('\\', '/');
        System.out.println("_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0.java : about to load " + toBeLoaded);
        System.load(toBeLoaded);
        this.director = director;
    }
    public int main(String argsToMain[]) throws InternalErrorException {
        return main_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0(argsToMain);
    }
    public void startThreads() {
        try {
            this.eventAcceptThread = new EventAcceptThread();
            this.eventAcceptThread.start();
            this.commandReadThread = new CommandReadThread();
            this.commandReadThread.start();
        } catch (Exception e) {
            MessageHandler.error("Could not join thread.", e);
        }
    }
    public boolean joinThreads() {
        try {
            if (this.commandReadThread != null) {
                this.commandReadThread.join();
            }
            if (this.eventAcceptThread != null) {
                this.eventAcceptThread.join();
            }
            return true;
        } catch (Exception e) {
            MessageHandler.error("Could not join thread.", e);
        }
        return false;
    }
    public void wrapup() {
        wrapup_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0();
    }
    public void processEvent(long currentTime) {
        processEvent_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0(currentTime);
    }
    public void receivePacket(long currentTime, String packet) {
        receivePacket_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0(currentTime, packet);
    }
    public void enqueueEvent(String newTime) {
        this.director.enqueueEvent(newTime);
    }
    public char getCharParameterValue(String param) {
        return this.director.getCharParameterValue(param);
    }
    public boolean sendToPort(String portName, String expression) {
        return this.director.sendToPort(portName, expression);
    }
    public void tosDebug(String debugMode, String message, String nodeID) {
        this.director.tosDebug(debugMode, message, nodeID);
    }
    public ServerSocket serverSocketCreate(short port) {
        return this.director.serverSocketCreate(port);
    }
    public void serverSocketClose(ServerSocket serverSocket) {
        this.director.serverSocketClose(serverSocket);
    }
    public Selector selectorCreate(ServerSocket serverSocket, boolean opAccept, boolean opConnect, boolean opRead, boolean opWrite) {
        return this.director.selectorCreate(serverSocket, opAccept, opConnect, opRead, opWrite);
    }
    public void selectorRegister(Selector selector, SelectableChannel SocketChannel, boolean opAccept, boolean opConnect, boolean opRead, boolean opWrite) {
        this.director.selectorRegister(selector, SocketChannel, opAccept, opConnect, opRead, opWrite);
    }
    public void selectorClose(Selector selector) {
        this.director.selectorClose(selector);
    }
    public SelectableChannel selectSocket(Selector selector, boolean[] notNullIfClosing, boolean opAccept, boolean opConnect, boolean opRead, boolean opWrite) {
        return this.director.selectSocket(selector, notNullIfClosing, opAccept, opConnect, opRead, opWrite);
    }
    public SocketChannel acceptConnection(SelectableChannel serverSocketChannel) {
        return this.director.acceptConnection(serverSocketChannel);
    }
    public void socketChannelClose(SelectableChannel socketChannel) {
        this.director.socketChannelClose(socketChannel);
    }
    public int socketChannelWrite(SocketChannel socketChannel, byte[] writeBuffer) {
        return this.director.socketChannelWrite(socketChannel, writeBuffer);
    }
    public int socketChannelRead(SocketChannel socketChannel, byte[] readBuffer) {
        return this.director.socketChannelRead(socketChannel, readBuffer);
    }
    private PtinyOSDirector director;
    private native int main_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0(String argsToMain[]) throws InternalErrorException ;
    private native void commandReadThread_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0();
    private native void eventAcceptThread_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0();
    private native void wrapup_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0();
    private native void processEvent_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0(long currentTime);
    private native void receivePacket_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0(long currentTime, String packet);
    private CommandReadThread commandReadThread;
    private EventAcceptThread eventAcceptThread;
    class CommandReadThread extends Thread {
         public void run() {
             commandReadThread_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0();
         }
    }
    class EventAcceptThread extends Thread {
         public void run() {
             eventAcceptThread_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor0();
         }
    }
}
===== End example Loader.java =====
        
     */
    private void _generateLoader() throws IllegalActionException {
        // Use filename relative to toplevel PtinyOSDirector.
        NamedObj toplevel = _toplevelNC();
        String toplevelName = _sanitizedFullName(toplevel);

        _CodeString text = new _CodeString();

        text.addLine("import java.net.ServerSocket;");
        text.addLine("import java.nio.channels.Selector;");
        text.addLine("import java.nio.channels.SelectableChannel;");
        text.addLine("import java.nio.channels.SocketChannel;");

        text.addLine("import ptolemy.domains.ptinyos.kernel.PtinyOSLoader;");
        text.addLine("import ptolemy.domains.ptinyos.kernel.PtinyOSDirector;");
        text.addLine("import ptolemy.kernel.util.InternalErrorException;");
        text.addLine("import ptolemy.util.MessageHandler;");

        text.addLine("public class Loader" + toplevelName
                + " implements PtinyOSLoader {");

        text
                .addLine("    public void load(String path, PtinyOSDirector director) {");
        text
                .addLine("        String fileSeparator = System.getProperty(\"file.separator\");");
        text
                .addLine("        String toBeLoaded = path + fileSeparator + System.mapLibraryName(\""
                        + toplevelName + "\");");
        text.addLine("        toBeLoaded = toBeLoaded.replace('\\\\', '/');");
        text.addLine("        System.out.println(\"" + toplevelName
                + ".java : about to load \" + toBeLoaded);");

        text.addLine("        System.load(toBeLoaded);");

        text.addLine("        this.director = director;");
        text.addLine("    }");

        text.addLine("    public int main(String argsToMain[]) throws InternalErrorException {");
        text.addLine("        return main" + toplevelName + "(argsToMain);");
        text.addLine("    }");

        text.addLine("    public void startThreads() {");
        text.addLine("        try {");
        text
                .addLine("        this.eventAcceptThread = new EventAcceptThread();");
        text.addLine("        this.eventAcceptThread.start();");
        text
                .addLine("        this.commandReadThread = new CommandReadThread();");
        text.addLine("        this.commandReadThread.start();");
        text.addLine("        } catch (Exception e) {");
        text.addLine("            MessageHandler.error(\"Could not join thread.\", e);");
        text.addLine("        }");
        text.addLine("    }");

        text.addLine("    public boolean joinThreads() {");
        text.addLine("        try {");
        text.addLine("            if (this.commandReadThread != null) {");
        text.addLine("                this.commandReadThread.join();");
        text.addLine("            }");
        text.addLine("            if (this.eventAcceptThread != null) {");
        text.addLine("                this.eventAcceptThread.join();");
        text.addLine("            }");
        text.addLine("            return true;");
        text.addLine("        } catch (Exception e) {");
        text.addLine("            MessageHandler.error(\"Could not join thread.\", e);");
        text.addLine("        }");
        text.addLine("        return false;");
        text.addLine("    }");

        text.addLine("    public void wrapup() {");
        text.addLine("        wrapup" + toplevelName + "();");
        text.addLine("    }");

        text.addLine("    public void processEvent(long currentTime) {");
        text.addLine("        processEvent" + toplevelName + "(currentTime);");
        text.addLine("    }");

        text
                .addLine("    public void receivePacket(long currentTime, String packet) {");
        text.addLine("        receivePacket" + toplevelName
                + "(currentTime, packet);");
        text.addLine("    }");

        text
                .addLine("    public void enqueueEvent(String newTime) {");
        text.addLine("        this.director.enqueueEvent(newTime);");
        text.addLine("    }");

        text.addLine("    public char getCharParameterValue(String param) {");
        text
                .addLine("        return this.director.getCharParameterValue(param);");
        text.addLine("    }");

        text
                .addLine("    public boolean sendToPort(String portName, String expression) {");
        text
                .addLine("        return this.director.sendToPort(portName, expression);");
        text.addLine("    }");

        text
                .addLine("    public void tosDebug(String debugMode, String message, String nodeID) {");
        text
                .addLine("        this.director.tosDebug(debugMode, message, nodeID);");
        text.addLine("    }");

        //////////////////////// Begin socket methods. ////////////////////
        text
                .addLine("    public ServerSocket serverSocketCreate(short port) {");
        text.addLine("        return this.director.serverSocketCreate(port);");
        text.addLine("    }");

        text
                .addLine("    public void serverSocketClose(ServerSocket serverSocket) {");
        text.addLine("        this.director.serverSocketClose(serverSocket);");
        text.addLine("    }");

        text
                .addLine("    public Selector selectorCreate(ServerSocket serverSocket, boolean opAccept, boolean opConnect, boolean opRead, boolean opWrite) {");
        text
                .addLine("        return this.director.selectorCreate(serverSocket, opAccept, opConnect, opRead, opWrite);");
        text.addLine("    }");

        text
                .addLine("    public void selectorRegister(Selector selector, SelectableChannel SocketChannel, boolean opAccept, boolean opConnect, boolean opRead, boolean opWrite) {");
        text
                .addLine("        this.director.selectorRegister(selector, SocketChannel, opAccept, opConnect, opRead, opWrite);");
        text.addLine("    }");

        text.addLine("    public void selectorClose(Selector selector) {");
        text.addLine("        this.director.selectorClose(selector);");
        text.addLine("    }");

        text
                .addLine("    public SelectableChannel selectSocket(Selector selector, boolean[] notNullIfClosing, boolean opAccept, boolean opConnect, boolean opRead, boolean opWrite) {");
        text
                .addLine("        return this.director.selectSocket(selector, notNullIfClosing, opAccept, opConnect, opRead, opWrite);");
        text.addLine("    }");

        text
                .addLine("    public SocketChannel acceptConnection(SelectableChannel serverSocketChannel) {");
        text
                .addLine("        return this.director.acceptConnection(serverSocketChannel);");
        text.addLine("    }");

        text
                .addLine("    public void socketChannelClose(SelectableChannel socketChannel) {");
        text
                .addLine("        this.director.socketChannelClose(socketChannel);");
        text.addLine("    }");

        text
                .addLine("    public int socketChannelWrite(SocketChannel socketChannel, byte[] writeBuffer) {");
        text
                .addLine("        return this.director.socketChannelWrite(socketChannel, writeBuffer);");
        text.addLine("    }");

        text
                .addLine("    public int socketChannelRead(SocketChannel socketChannel, byte[] readBuffer) {");
        text
                .addLine("        return this.director.socketChannelRead(socketChannel, readBuffer);");
        text.addLine("    }");
        //////////////////////// End socket methods. //////////////////////

        text.addLine("    private PtinyOSDirector director;");

        text.addLine("    private native int main" + toplevelName
                + "(String argsToMain[]) throws InternalErrorException;");
        text.addLine("    private native void commandReadThread" + toplevelName
                + "();");
        text.addLine("    private native void eventAcceptThread" + toplevelName
                + "();");
        text.addLine("    private native void wrapup" + toplevelName + "();");
        text.addLine("    private native void processEvent" + toplevelName
                + "(long currentTime);");
        text.addLine("    private native void receivePacket" + toplevelName
                + "(long currentTime, String packet);");
        text.addLine("    private CommandReadThread commandReadThread;");
        text.addLine("    private EventAcceptThread eventAcceptThread;");

        text.addLine("    class CommandReadThread extends Thread {");
        text.addLine("         public void run() {");
        text.addLine("             commandReadThread" + toplevelName + "();");
        text.addLine("         }");
        text.addLine("    }");
        text.addLine("    class EventAcceptThread extends Thread {");
        text.addLine("         public void run() {");
        text.addLine("             eventAcceptThread" + toplevelName + "();");
        text.addLine("         }");
        text.addLine("    }");
        text.addLine("}");

        String loaderFileName = "Loader" + toplevelName + ".java";
        File directory = destinationDirectory.asFile();

        // Open file for the generated loaderFile.
        File writeFile = new File(directory, loaderFileName);

        if (_confirmOverwrite(writeFile)) {
            // Write the generated code to the file.
            try {
                FileWriter writer = new FileWriter(writeFile);
                writer.write(text.toString());
                writer.close();
            } catch (IOException e) {
                throw new IllegalActionException(this, e,
                        "Failed to open file for writing.");
            }
        }
    }

    /** Generate makefile.
===== Begin example makefile (from Dec 9, 20060) =====        
TOSROOT=/home/celaine/ptII/vendors/ptinyos/tinyos-1.x
TOSDIR=/home/celaine/ptII/vendors/ptinyos/tinyos-1.x/tos
TOSMAKE_PATH += $(TOSROOT)/contrib/ptII/ptinyos/tools/make
COMPONENT=_SenseToLeds_InWireless_MicaBoard_MicaCompositeActor1
PFLAGS += -I%T/lib/Counters
PFLAGS += -DCOMMAND_PORT=10584 -DEVENT_PORT=10585
PFLAGS +=-D_PTII_NODEID=1
MY_PTCC_FLAGS += -D_PTII_NODE_NAME=_1SenseToLeds_1InWireless_1MicaBoard_1MicaCompositeActor1
PFLAGS += "-I$(TOSROOT)/contrib/ptII/ptinyos/beta/TOSSIM-packet"
include /home/celaine/ptII/mk/ptII.mk
include /home/celaine/ptII/vendors/ptinyos/tinyos-1.x/tools/make/Makerules
===== End example makefile (from Dec 9, 20060) =====        
     * @exception CancelException If the directory named by the
     * ptolemy.ptII.tosroot property does not exist.
     */
    private String _generateMakefile() throws IllegalActionException,
            CancelException {

        // Check to make sure that tosRoot exists
        if (tosRoot == null || tosRoot.asFile() == null
                || !tosRoot.asFile().isDirectory()) {
            String fileName = (tosRoot == null ? "null"
                    : (tosRoot.asFile() == null ? "null" : tosRoot.asFile()
                            .toString()));
            String tosRootMessage = "The TOSROOT directory \"" + fileName
                    + "\" does not exist?  Compilation "
                    + "is likely to fail.  The TOSROOT environment "
                    + "variable should be set to the location of the "
                    + "TinyOS tree, typically "
                    + "$PTII/vendors/ptinyos/tinyos-1.x.";
            if (!MessageHandler.yesNoQuestion(tosRootMessage
                    + "\nWould you like to proceed?")) {
                throw new CancelException();
            }
        }

        // Check to make sure that tosDir exists
        if (tosDir == null || tosDir.asFile() == null
                || !tosDir.asFile().isDirectory()) {
            String fileName = (tosDir == null ? "null" : tosDir.asFile()
                    .toString());
            fileName = (fileName == null ? "null" : fileName);
            String tosDirMessage = "The TOSDIR directory \"" + fileName
                    + "\" does not exist?  Compilation "
                    + "is likely to fail.  The TOSDIR environment "
                    + "variable should be set to the location of the "
                    + "TinyOS tree, typically "
                    + "$PTII/vendors/ptinyos/tinyos-1.x/tos.";
            if (!MessageHandler.yesNoQuestion(tosDirMessage
                    + "\nWould you like to proceed?")) {
                throw new CancelException();
            }
        }

        // String containing makefile text to generate.
        _CodeString text = new _CodeString();
        text.addLine("TOSROOT=" + tosRoot.stringValue());
        text.addLine("TOSDIR=" + tosDir.stringValue());

        // Path to ptII make platform.
        // FIXME use pathseparator?
        // FIXME: this will not work if TOSROOT has spaces in it.
        text.addLine("TOSMAKE_PATH += "
                + "$(TOSROOT)/contrib/ptII/ptinyos/tools/make");
        // Use full filename.
        NamedObj toplevel = _toplevelNC();
        String toplevelName = _sanitizedFullName(toplevel);
        text.addLine("COMPONENT=" + toplevelName);

        text.addLine("PFLAGS += " + pflags.stringValue());

        text.addLine("PFLAGS +=" + " -DCOMMAND_PORT=" + commandPort.getToken()
                + " -DEVENT_PORT=" + eventPort.getToken());

        // If isBaseStation is <i>true</i>, then _PTII_NODEID in the
        // makefile is set to 0, which makes this a base station.  A
        // non-zero value of the nodeID parameter will be ignored.  If
        // <i>false</i>, use the regular node ID.
        boolean isBaseStationValue = ((BooleanToken) isBaseStation.getToken())
                .booleanValue();
        if (isBaseStationValue == true) {
            text.addLine("PFLAGS +=" + "-D_PTII_NODEID=" + "0");
        } else {
            text.addLine("PFLAGS +=" + "-D_PTII_NODEID=" + nodeID.getToken());
        }

        // Turn _ into _1 for JNI compatibility.
        String nativeMethodName = toplevelName.replaceAll("_", "_1");
        text.addLine("MY_PTCC_FLAGS +=" + " -D_PTII_NODE_NAME="
                + nativeMethodName);

        // Look for ptII in the target list and add necessary PFLAGS.
        String[] targets = target.stringValue().split("\\s");
        for (int i = 0; i < targets.length; i++) {
            if (targets[i].equals("ptII") || targets[i].equals("all")) {
                // Note: we do not conditionally add this in, so
                // makefile must be regenerated if user wants to use
                // makefile with non-ptII platform.
                text
                        .addLine("PFLAGS += "
                                + "\"-I$(TOSROOT)/contrib/ptII/ptinyos/beta/TOSSIM-packet\"");
                break;
            }
        }

        // Expand $PTII, substitute / for \, and backslash space for space.
        String ptIImk = StringUtilities.getProperty("ptolemy.ptII.dir")
                + "/mk/ptII.mk";
        text.addLine("include " + ptIImk.replaceAll(" ", "\\\\ "));

        // Handle pathnames with spaces: substitute / for \ and
        // backslash space for space.
        text.addLine("include "
                + tosRoot.stringValue().replace('\\', '/').replaceAll(" ",
                        "\\\\ ") + "/tools/make/Makerules");

        // Use .mk so that Emacs will be in the right mode
        String makefileName = toplevelName + ".mk";
        File directory = destinationDirectory.asFile();

        // Open file for the generated makefile.
        File writeFile = new File(directory, makefileName);

        if (_confirmOverwrite(writeFile)) {
            // Write the generated code to the file.
            try {
                FileWriter writer = new FileWriter(writeFile);
                writer.write(text.toString());
                writer.close();
            } catch (IOException e) {
                throw new IllegalActionException(this, e,
                        "Failed to open file for writing.");
            }
        }

        return makefileName;
    }

    /** Generate code for the connections.  The order of ports in
     *  model has effect on the order of driver input parameters.
     *  @return The drivers code.
     */
    private String _includeConnection(CompositeActor model)
            throws IllegalActionException {
        _CodeString codeString = new _CodeString();
        Actor actor;
    
        // Generate "Driver functions" for common actors.
        Iterator actors = model.entityList().iterator();
    
        while (actors.hasNext()) {
            actor = (Actor) actors.next();
    
            if (_needsInputDriver(actor)) {
                codeString.add(_includeConnection(model, actor));
            }
        }
    
        Iterator outPorts = model.outputPortList().iterator();
    
        while (outPorts.hasNext()) {
            IOPort port = (IOPort) outPorts.next();
    
            // FIXME: Assuming ports are either
            // input or output and not both.
            List sourcePortList = port.insidePortList();
    
            //FIXME: can the list be empty?
    
            /*
             if (sourcePortList.size() > 1) {
             throw new IllegalActionException(port, "Input port " +
             "cannot receive data from multiple sources in NC.");
             }*/
            CompositeActor container = (CompositeActor) getContainer();
    
            //NamedObj toplevel = _toplevelNC();
            //            if (sourcePortList != null && (container != toplevel)) {
            if ((sourcePortList != null)
                    && !(container instanceof PtinyOSCompositeActor)) {
                // FIXME: test this code
                for (int i = 0; i < sourcePortList.size(); i++) {
                    IOPort sourcePort = (IOPort) sourcePortList.get(i);
                    String sanitizedOutPortName = StringUtilities
                            .sanitizeName(sourcePort.getName());
                    String sourceActorName = StringUtilities
                            .sanitizeName(sourcePort.getContainer().getName());
                    codeString.addLine(sourceActorName + "."
                            + sanitizedOutPortName + " = " + port.getName()
                            + ";");
                }
            }
        }
    
        return codeString.toString();
    }

    /** Generate code for the connections.
     *  @return The connections code.
     */
    private static String _includeConnection(CompositeActor model, Actor actor)
            throws IllegalActionException {
        _CodeString codeString = new _CodeString();

        String actorName = StringUtilities.sanitizeName(((NamedObj) actor)
                .getName());

        for (Iterator inPorts = actor.inputPortList().iterator(); inPorts
                .hasNext();) {
            IOPort inPort = (IOPort) inPorts.next();
            String sanitizedInPortName = StringUtilities.sanitizeName(inPort
                    .getName());
            String inPortMultiport = "";

            if (inPort.isMultiport()) {
                inPortMultiport = "[unique(\"" + sanitizedInPortName + "\")]";
            }

            List sourcePortList = inPort.sourcePortList();

            // Note: We allow an input port (provides) to connect to
            // multiple output ports (requires).  The nesC compiler
            // will generate a warning about uncombined calls in that
            // case.
            for (int i = 0; i < sourcePortList.size(); i++) {
                IOPort sourcePort = (IOPort) sourcePortList.get(i);
                String sanitizedSourcePortName = StringUtilities
                        .sanitizeName(sourcePort.getName());
                String sourcePortMultiport = "";

                if (sourcePort.isMultiport()) {
                    sourcePortMultiport = "[unique(\""
                            + sanitizedSourcePortName + "\")]";
                }

                String sourceActorName = StringUtilities
                        .sanitizeName(sourcePort.getContainer().getName());

                if (sourcePort.getContainer() == model) {
                    codeString.addLine(sanitizedSourcePortName
                            + sourcePortMultiport + " = " + actorName + "."
                            + sanitizedInPortName + inPortMultiport + ";");
                } else {
                    codeString.addLine(sourceActorName + "."
                            + sanitizedSourcePortName + sourcePortMultiport
                            + " -> " + actorName + "." + sanitizedInPortName
                            + inPortMultiport + ";");
                }
            }
        }

        return codeString.toString();
    }

    /** Generate code for the components used in the model.
     *  @return The code.
     */
    private static String _includeModule(CompositeActor model)
            throws IllegalActionException {
        _CodeString codeString = new _CodeString();

        // Examine each actor in the model.
        Iterator actors = model.entityList().iterator();
        boolean isFirst = true;

        while (actors.hasNext()) {
            // Figure out the actor name.
            Actor actor = (Actor) actors.next();
            String actorName = StringUtilities.sanitizeName(((NamedObj) actor)
                    .getName());

            // Figure out the class name.
            String className = ((NamedObj) actor).getClassName();
            String[] classNameArray = className.split("\\.");
            String shortClassName = classNameArray[classNameArray.length - 1];

            // Rename the actor if it has no name.
            if (actorName.length() == 0) {
                actorName = _unnamed;
            }

            String componentName;

            if (!shortClassName.equals(actorName)) {
                componentName = shortClassName + " as " + actorName;
            } else {
                componentName = actorName;
            }

            if (isFirst) {
                codeString.add("components " + componentName);
                isFirst = false;
            } else {
                codeString.add(", " + componentName);
            }
        }

        codeString.addLine(";");
        return codeString.toString();
    }

    /** Initialize parameters. Set all parameters to their default values.
     */
    private void _initializeParameters() {
        try {
            _attachText(
                    "_iconDescription",
                    "<svg>\n"
                            + "<rect x=\"-40\" y=\"-15\" width=\"80\" height=\"30\" "
                            + "style=\"fill:blue\"/>"
                            + "<text x=\"-36\" y=\"8\" "
                            + "style=\"font-size:20; font-family:SansSerif; fill:white\">"
                            + "PtinyOS</text></svg>");

            // Set the code generation output directory to the current
            // working directory.
            destinationDirectory = new FileParameter(this,
                    "destinationDirectory");
            new Parameter(destinationDirectory, "allowFiles",
                    BooleanToken.FALSE);
            new Parameter(destinationDirectory, "allowDirectories",
                    BooleanToken.TRUE);
            destinationDirectory.setExpression("$CWD");

            // Set so that user must confirm each file that will be
            // overwritten.
            // Note: Default to overwrite w/o asking.
            confirmOverwrite = new Parameter(this, "confirmOverwrite",
                    BooleanToken.FALSE);
            confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);

            // Set path to tinyos-1.x directory.
            tosRoot = new FileParameter(this, "TOSROOT");
            new Parameter(tosRoot, "allowFiles", BooleanToken.FALSE);
            new Parameter(tosRoot, "allowDirectories", BooleanToken.TRUE);
            String tosRootProperty = StringUtilities
                    .getProperty("ptolemy.ptII.tosroot");
            if (tosRootProperty != null) {
                tosRoot.setExpression(tosRootProperty);
            } else {
                tosRoot.setExpression("$PTII/vendors/ptinyos/tinyos-1.x");
            }

            // Set path to tinyos-1.x/tos directory.
            tosDir = new FileParameter(this, "TOSDIR");
            new Parameter(tosDir, "allowFiles", BooleanToken.FALSE);
            new Parameter(tosDir, "allowDirectories", BooleanToken.TRUE);
            String tosDirProperty = StringUtilities
                    .getProperty("ptolemy.ptII.tosdir");
            if (tosDirProperty != null) {
                tosDir.setExpression(tosDirProperty);
            } else {
                tosDir.setExpression("$PTII/vendors/ptinyos/tinyos-1.x/tos");
            }

            // Set additional flags passed to the nesC compiler.
            pflags = new StringParameter(this, "pflags");
            pflags.setExpression("-I%T/lib/Counters");

            // Set number of nodes to be simulated to be equal to 1.
            // NOTE: only the top level value of this parameter matters.
            numberOfNodes = new Parameter(this, "numberOfNodes", new IntToken(1));
            numberOfNodes.setTypeEquals(BaseType.INT);
            numberOfNodes.setVisibility(Settable.NOT_EDITABLE);

            // Set the boot up time range to the defaul to of 10 sec.
            // NOTE: only the top level value of this parameter matters.
            bootTimeRange = new Parameter(this, "bootTimeRange", new IntToken(
                    10));
            bootTimeRange.setTypeEquals(BaseType.INT);

            // Set compile target platform to ptII.
            // NOTE: only the top level value of this parameter matters.
            target = new StringParameter(this, "target");
            target.setExpression("ptII");

            // Set simulate to true.
            // NOTE: only the top level value of this parameter matters.
            simulate = new Parameter(this, "simulate", BooleanToken.TRUE);
            simulate.setTypeEquals(BaseType.BOOLEAN);

            // Set so that this is not a base station.
            isBaseStation = new Parameter(this, "isBaseStation",
                    BooleanToken.FALSE);
            isBaseStation.setTypeEquals(BaseType.BOOLEAN);

            // Set command and event ports for TOSSIM.
            commandPort = new PtinyOSIntegerParameter(this, "commandPort", 2);
            commandPort.setExpression("10584");
            eventPort = new Parameter(this, "eventPort");
            eventPort.setExpression("commandPort + 1");

            // Make timeResolution SharedParameter (from base class) visible.
            timeResolution.setVisibility(Settable.FULL);
            timeResolution.moveToLast();

            // Set node ID to a starting value of 1.
            // PtinyOSIntegerParameter will autoincrement this value
            // depending on how many other nodes are in the model.
            nodeID = new PtinyOSIntegerParameter(this, "nodeID", 1);
            nodeID.setExpression("1");

        } catch (KernelException ex) {
            throw new InternalErrorException(this, ex, "Cannot set parameter");
        }
    }

    /** Generate NC code describing the input ports.  Input ports are
     *  described in NC as interfaces "provided" by this module.
     *  @return The code describing the input ports.
     */
    private static String _interfaceProvides(CompositeActor model)
            throws IllegalActionException {
        _CodeString codeString = new _CodeString();

        Iterator inPorts = model.inputPortList().iterator();

        while (inPorts.hasNext()) {
            IOPort port = (IOPort) inPorts.next();

            if (port.isOutput()) {
                throw new IllegalActionException(port,
                        "Ports that are both inputs and outputs "
                                + "are not allowed.");
            }

            codeString.addLine("provides interface " + port.getName() + ";");
        }

        return codeString.toString();
    }

    /** Generate interface the model uses.
     *  @return The code.
     */
    private static String _interfaceUses(CompositeActor model)
            throws IllegalActionException {
        _CodeString codeString = new _CodeString();

        Iterator outPorts = model.outputPortList().iterator();

        while (outPorts.hasNext()) {
            IOPort port = (IOPort) outPorts.next();

            if (port.isInput()) {
                throw new IllegalActionException(port,
                        "Ports that are both inputs and outputs "
                                + "are not allowed.");
            }

            codeString.addLine("uses interface " + port.getName() + ";");
        }

        return codeString.toString();
    }

    /** Return true if this PtinyOSDirector is not contained by a
     *  opaque composite that is itself controlled by a
     *  PtinyOSDirector.
     */

    // FIXME rename?
    private boolean _isTopLevelNC() {
        NamedObj container = getContainer();

        if (container instanceof CompositeActor) {
            Director director = ((CompositeActor) container)
                    .getExecutiveDirector();

            if (director instanceof PtinyOSDirector) {
                return false;
            } else {
                return true;
            }
        } else {
            throw new InternalErrorException("This director was not "
                    + "inside a CompositeActor.");
        }
    }

    /** Return true if the given actor has at least one input port, which
     *  requires it to have an input driver.
     */
    private static boolean _needsInputDriver(Actor actor) {
        if (actor.inputPortList().size() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /** Get the sanitized full name with workspace version number appended,
     *  or "Unnamed" with version number appended if no name.
     */
    private String _sanitizedFullName(NamedObj obj) {
        String objName = obj.getFullName();
        objName = StringUtilities.sanitizeName(objName);

        if (objName == "") {
            objName = _unnamed;
        }

        objName = objName + _version;

        return objName;
    }

    /** Looks for the topmost container that contains a PtinyOSDirector
     *  director.
     *
     *  NOTE: returns this container if this container is not an
     *  instanceof CompositeActor.
     */

    // FIXME rename?
    private NamedObj _toplevelNC() {
        if (!_isTopLevelNC()) {
            NamedObj container = getContainer();

            if (container instanceof CompositeActor) {
                Director director = ((CompositeActor) container)
                        .getExecutiveDirector();

                if (director instanceof PtinyOSDirector) {
                    return ((PtinyOSDirector) director)._toplevelNC();
                }
            }
        }

        return getContainer();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Java loader for JNI code.
    private PtinyOSLoader _loader;

    // String to rename unnamed objects or files.
    private static String _unnamed = "Unnamed";

    // Workspace version number at preinitialize.
    private long _version = -1;
    
    private long _startTime;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Class for creating a StringBuffer that represents generated
     *  code.
     */
    private static class _CodeString {
        public _CodeString() {
            text = new StringBuffer();
        }

        public String toString() {
            return text.toString();
        }

        public void add(String newtext) {
            text.append(newtext);
        }

        public void addLine(String newline) {
            text.append(newline + _endLine);
        }

        private StringBuffer text;

        private static String _endLine = "\n";
    }

    /** Private class that reads a stream in a thread.
     */
    private class _StreamReaderThread extends Thread {
        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this _StreamReaderThread.
         *  @param stringWriter The StringWriter that is written.
         */
        _StreamReaderThread(InputStream inputStream, String name,
                StringWriter stringWriter) {
            this(inputStream, name, null, stringWriter);
        }

        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this _StreamReaderThread.
         *  @param redirect The name of the output stream to redirect the
         inputStream to.
         *  @param stringWriter The StringWriter that is written.
         */
        _StreamReaderThread(InputStream inputStream, String name,
                OutputStream redirect, StringWriter stringWriter) {
            _inputStream = inputStream;
            _name = name;
            _outputStream = redirect;
            _stringWriter = stringWriter;
        }

        /** Read lines from the input stream and redirect them to the
         *  output stream (if it exists), and to the debugging output.
         */
        public void run() {
            try {
                PrintWriter printWriter = null;

                if (_outputStream != null) {
                    printWriter = new PrintWriter(_outputStream);
                }

                InputStreamReader isr = new InputStreamReader(_inputStream);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                while ((line = br.readLine()) != null) {
                    // Redirect the input.
                    if (printWriter != null) {
                        printWriter.println(line);
                    }

                    if (_stringWriter != null) {
                        _stringWriter.write(line + "\n");
                    }

                    // Create the debug output.
                    if (_debugging) {
                        _debug(_name + ">" + line);
                    }

                    System.out.println(_name + ">" + line);
                }

                if (printWriter != null) {
                    printWriter.flush();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /** Stream from which to read. */
        private InputStream _inputStream;

        /** Name of the input stream. */
        private String _name;

        /** Stream to which to write the redirected input. */
        private OutputStream _outputStream;

        /** StringWriter that is written to */
        private StringWriter _stringWriter;
    }
}
