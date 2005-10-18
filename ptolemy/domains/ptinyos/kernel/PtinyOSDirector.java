/*  A director for generating, compiling, and simulating nesC code from
 TinyOS components.

 Copyright (c) 2005 The Regents of the University of California.
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
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
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
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.CancelException;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// PtinyOSDirector

/**
 A director for generating, compiling, and simulating nesC code from
 TinyOS components.

 <p>NOTE: if target is blank, but simulate is true, then the model will
 assume that the ptII target has already been compiled and will
 attempt to simulate.

 @see ptolemy.actor.lib.io.LineWriter

 @author Elaine Cheong, Yang Zhao, Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class PtinyOSDirector extends Director {
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

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-40\" y=\"-15\" width=\"80\" height=\"30\" "
                + "style=\"fill:blue\"/>" + "<text x=\"-36\" y=\"8\" "
                + "style=\"font-size:20; font-family:SansSerif; fill:white\">"
                + "PtinyOS</text></svg>");

        // Set the code generation output directory to the current
        // working directory.
        destinationDirectory = new FileParameter(this, "destinationDirectory");
        new Parameter(destinationDirectory, "allowFiles", BooleanToken.FALSE);
        new Parameter(destinationDirectory, "allowDirectories",
                BooleanToken.TRUE);
        destinationDirectory.setExpression("$CWD");

        // Set so that user must confirm each file that will be
        // overwritten.
        confirmOverwrite = new Parameter(this, "confirmOverwrite",
                BooleanToken.TRUE);
        confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);

        // Set path to tos directory.
        tosroot = new FileParameter(this, "TOSROOT");
        new Parameter(tosroot, "allowFiles", BooleanToken.FALSE);
        new Parameter(tosroot, "allowDirectories", BooleanToken.TRUE);
        String tosrootProperty = System.getProperty("ptolemy.ptII.tosroot");
        if (tosrootProperty != null) {
            tosroot.setExpression(tosrootProperty);
        } else {
            tosroot.setExpression("$PTII/vendors/ptinyos/tinyos-1.x/tos");
        }

        // Set additional make flags.
        pflags = new StringParameter(this, "pflags");
        pflags.setExpression("-I%T/lib/Counters");

        // Set number of nodes to be simulated to be equal to 1.
        // NOTE: only the top level value of this parameter matters.
        numNodes = new Parameter(this, "numNodes", new IntToken(1));
        numNodes.setTypeEquals(BaseType.INT);

        // Set the boot up time range to the defaul to of 10 sec.
        // NOTE: only the top level value of this parameter matters.
        bootTimeRange = new Parameter(this, "bootTimeRange", new IntToken(10));
        bootTimeRange.setTypeEquals(BaseType.INT);

        // Set compile target platform to ptII.
        // NOTE: only the top level value of this parameter matters.
        target = new StringParameter(this, "target");
        target.setExpression("ptII");

        // Set simulate to true.
        // NOTE: only the top level value of this parameter matters.
        simulate = new Parameter(this, "simulate", BooleanToken.TRUE);
        simulate.setTypeEquals(BaseType.BOOLEAN);

        // Set command and event ports for TOSSIM.
        commandPort = new Parameter(this, "commandPort", new IntToken(10584));
        commandPort.setTypeEquals(BaseType.INT);
        eventPort = new Parameter(this, "eventPort", new IntToken(10585));
        eventPort.setTypeEquals(BaseType.INT);

        // Make timeResolution SharedParameter (from base class) visible.
        timeResolution.setVisibility(Settable.FULL);
        timeResolution.moveToLast();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The directory into which to write the code.
     */
    public FileParameter destinationDirectory;

    /** Path to the TinyOS tree.
     */
    public FileParameter tosroot;

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

    /** Additional include directories for compilation.
     */
    public StringParameter pflags;

    /** Number of nodes to simulate in TOSSIM.
     */
    public Parameter numNodes;

    /** TOSSIM node bootup time range.
     */
    public Parameter bootTimeRange;

    /** Choose what target for which to compile the generated code.
     */
    public StringParameter target;

    /** Choose whether to simulate in ptII.
     */
    public Parameter simulate;

    /** Port for TOSSIM to accept commands
     */
    public Parameter commandPort;

    /** Port for TOSSIM to publish events
     */
    public Parameter eventPort;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** A callback method (from C code) for the application to enqueue
     *  the next event.
     *
     *  newtime is a long long in C
     */
    public void enqueueEvent(String newtime) throws IllegalActionException {
        // Assumes that we already checked in preinitialize() that the
        // container is a CompositeActor.
        CompositeActor container = (CompositeActor) getContainer();

        // Get the executive director.  If there is none, use this instead.
        Director director = container.getExecutiveDirector();

        if (director == null) {
            director = this;
        }

        //System.out.println("PtinyOSDirector.enqueueEvent : " + newtime);
        Time t = new Time(director, Long.parseLong(newtime));
        director.fireAt(container, t);
    }

    /** Process one event in the TOSSIM event queue and run tasks in
     *  task queue.
     *  @exception IllegalActionException If something goes wrong.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }

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
            _loader.processEvent(currentTimeValue);
        }
    }

    /** Get boolean value from PortParameter named param.
     *
     *  NOTE: gets a BooleanToken from port and converts to boolean.
     *  @param param The parameter.
     *  @return FALSE if there is an error.
     */
    public boolean getBooleanParameterValue(String param)
            throws IllegalActionException {
        if (_debugging) {
            _debug("Called getBooleanParameterValue with " + param
                    + " as argument.");
        }

        // FIXME Delete later
        CompositeActor model = (CompositeActor) getContainer();
        Iterator inPorts;

        inPorts = model.inputPortList().iterator();

        while (inPorts.hasNext()) {
            IOPort p = (IOPort) inPorts.next();

            if (p.getName().equals(param) && (p instanceof ParameterPort)) {
                Token t = ((ParameterPort) p).getParameter().getToken();

                if (t != null) {
                    //System.out.println(p.getName() + " " + t);
                    try {
                        BooleanToken dt = BooleanToken.convert(t);
                        return (boolean) dt.booleanValue();
                    } catch (IllegalActionException e) {
                        System.out.println("Couldn't convert to BooleanToken.");

                        // FIXME
                        return false;
                    }
                } else {
                    // FIXME
                    System.out.println("Couldn't get token from ParameterPort "
                            + p.getName());
                }
            }
        }

        // FIXME
        System.out.println("Couldn't find PortParameter " + param);
        return false;
    }

    /** Get a char value from PortParameter named parameter.
     *
     *  NOTE: gets a DoubleToken from port and converts to char.
     *
     *  NOTE: returns 0 if error.
     *  @param parameter The parameter.
     */
    public char getCharParameterValue(String parameter)
            throws IllegalActionException {
        if (_debugging) {
            _debug("Called getCharParameterValue with " + parameter
                    + " as argument.");
        }

        // FIXME Delete later
        CompositeActor model = (CompositeActor) getContainer();
        Iterator inPorts;

        inPorts = model.inputPortList().iterator();

        while (inPorts.hasNext()) {
            IOPort p = (IOPort) inPorts.next();

            if (p.getName().equals(parameter)
                    && (p instanceof ParameterPort)) {
                Token t = ((ParameterPort) p).getParameter().getToken();

                if (t != null) {
                    //System.out.println(p.getName() + " " + t);
                    try {
                        DoubleToken dt = DoubleToken.convert(t);
                        return (char) dt.doubleValue();
                    } catch (IllegalActionException e) {
                        System.out.println("Couldn't convert to DoubleToken.");

                        // FIXME
                        return 0;
                    }
                } else {
                    // FIXME
                    System.out.println("Couldn't get token from ParameterPort "
                            + p.getName());
                }
            }
        }

        // FIXME
        System.out.println("Couldn't find PortParameter " + parameter);
        return 0;
    }

    /** Load TOSSIM library and call main().
     *  @param IllegalActionException If there is a problem initializing
     *  the director, such as a problem loading the JNI loader.   
     */
    public void initialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called initialize()");
        }

        if (((BooleanToken) simulate.getToken()).booleanValue()) {
            NamedObj toplevel = _toplevelNC();
            String toplevelName = _sanitizedFullName(toplevel);

            // Add the destinationDirectory to the classpath.
            String fileSeparator = System.getProperty("file.separator");
            String outputDir = destinationDirectory.stringValue()
                    + fileSeparator + "build" + fileSeparator + "ptII";

            // From: http://javaalmanac.com/egs/java.lang/LoadClass.html
            // Create a File object on the root of the directory
            // containing the class file.
            File file = new File(outputDir);

            try {
                // Convert File to a URL
                URL url = file.toURL(); // file:/c:/myclasses/
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
                            numNodes.getToken().toString() };

                    if (_debugging) {
                        _debug("Done loading '" + className
                                + "', about to load(" + outputDir + ")");
                    }

                    // Load the library with the native methods for TOSSIM.
                    _loader.load(outputDir, this);

                    if (_debugging) {
                        _debug("Done with load(), about to call main("
                                + argsToMain[0] + " " + argsToMain[1]);
                    }
                    if (_loader.main(argsToMain) < 0) {
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

    /** Return true if simulation is requested.
     *  @return The value of the <i>simulate</i> parameter
     */
    public boolean postfire() throws IllegalActionException {
        return ((BooleanToken) simulate.getToken()).booleanValue();
    }

    /** Always return true, indicating that the director is ready to fire.
     *
     *  <p>NOTE: If we return false, the run doesn't terminate on its
     *  own, since in Manager.iterate(), "result" defaults to true.
     *  @exception IllegalActionException Not thrown in this base class.
     *  @return true
     */
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /** Generate nesC code. 
     *  <p>If this director is the top most PtinyOSDirector, and the
     *  {@link #target} parameter is non-empty then
     *  a .java file is created.  The .java file implements   
     *  the {@link ptolemy.domains.ptinyos.kernel.PtinyOSLoader}
     *  interface and is compiled by this method.  
     *  @exception IllegalActionException If the container is not
     *  an instance of CompositeActor, the destination directory
     *  does not exist and cannot be created, or the nesC file
     *  cannot be written.
     */
    public void preinitialize() throws IllegalActionException {
        if (_debugging) {
            _debug("Called preinitialize()");
        }

        if (!(getContainer() instanceof CompositeActor)) {
            throw new IllegalActionException(this,
                    "Requires the container to be an "
                            + "instance of CompositeActor.");
        }

        _version = workspace().getVersion();

        // Open directory, creating it if necessary.
        File directory = destinationDirectory.asFile();

        if (!directory.isDirectory()) {
            if (!MessageHandler.yesNoQuestion("Create directory "
                    + destinationDirectory.getExpression() + "?")) {
                throw new IllegalActionException(this, "No directory named: "
                        + destinationDirectory.getExpression());
            } else {
                if (!directory.mkdir()) {
                    throw new IllegalActionException(this,
                            "Could not create directory " + directory);
                }
            }
        }

        // Generate the code as a string.
        CompositeActor container = (CompositeActor) getContainer();
        String code = _generateCode(container);

        // Create file name relative to the toplevel NCCompositeActor.
        // FIXME working here
        //   if (_isTopLevelNC()) {
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

                String makefileName = _generateMakefile();
                _compile(makefileName);
            }
        }
    }

    /** Notify the loader that a packet has been received.
     *  The {@link ptolemy.domains.ptinyos.kernel.PtinyOSDirector#fire()}
     *  method calls this method with the string value of the input packet.
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

            _loader.receivePacket(currentTimeValue, packet);
        }
    }

    /** 
     *  Send an expression to a port.
     *  <p>The loader class has a method with the same name that calls
     *  this method.  The C code (ptII.c) calls <loader>.sendToPort in
     *  order to send data from the C code to the Java (Ptolemy II)
     *  simulation.
     *
     *  @param portName The name of the port
     *  @param expression The expression
     *  @return 1 if the expression was successfully sent, 0 if the
     *  port is not connected or not found and -1 if the port is
     *  of any type other than Boolean or String.
     */
    public int sendToPort(String portName, String expression)
            throws IllegalActionException {
        CompositeActor model = (CompositeActor) getContainer();
        Iterator outPorts;

        outPorts = model.outputPortList().iterator();

        while (outPorts.hasNext()) {
            TypedIOPort port = (TypedIOPort) outPorts.next();

            if (port.getName().equals(portName)) {
                if (port.getWidth() > 0) {
                    // FIXME always boolean?
                    if (port.getType() == BaseType.BOOLEAN) {
                        port.send(0, new BooleanToken(expression));
                        return 1;
                    } else if (port.getType() == BaseType.STRING) {
                        port.send(0, new StringToken(expression));
                        return 1;
                    } else {
                        // Port does not have correct type.
                        // FIXME
                        System.out.println("error: could not find matching "
                                + "type for sendToPort()");
                        return -1;
                    }
                } else {
                    // Port not connected to anything.
                    return 0;
                }
            }
        }

        // Port not found.
        return 0;
    }

    /** A callback method (from C code) for the application to print a
     *  debug message.
     *
     *  @param debugMode A long long in C (currently unused)
     *  @param message A char * in C
     *  @param nodeNumber is a short in C
     *
     */
    public void tosDebug(String debugMode, String message, String nodeNumber) {
        if (_debugging) {
            // Remove leading and trailing whitespace.
            String trimmedMessage = message.trim();

            if (nodeNumber != null) {
                _debug(nodeNumber + ": " + trimmedMessage);
            } else {
                _debug(trimmedMessage);
            }
        }
    }

    /** Invoke the wrapup method of the super class. Reset the private
     *  state variables.
     *  @exception IllegalActionException If the wrapup() method of
     *   one of the associated actors throws it.
     */
    public void wrapup() throws IllegalActionException {
        if (_debugging) {
            _debug("Called wrapup()");
        }

        if (((BooleanToken) simulate.getToken()).booleanValue()) {
            _loader.wrapup();  // SIGSTOP: man 7 signal
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
        String command[] = {
            "make", "-C",
            destinationDirectory.stringValue().replace('\\', '/'),
            "-f" , makefileName, target.stringValue()
        };

        // Used for error handling.
        StringBuffer commandString = new StringBuffer(command[0]);
        for (int i = 1; i < command.length; i++) {
            commandString.append(" " + command[i]);
        }
        System.out.println(commandString.toString());

        int exitValue = 0;

        try {

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);

            // Connect a thread to the error stream of the cmd process.
            _StreamReaderThread errorGobbler = new _StreamReaderThread(proc
                    .getErrorStream(), "ERROR");

            // Connect a thread to the output stream of the cmd process.
            _StreamReaderThread outputGobbler = new _StreamReaderThread(proc
                    .getInputStream(), "OUTPUT");

            // Start the threads.
            errorGobbler.start();
            outputGobbler.start();

            // Wait for exit and see if there are any errors.
            // make returns non-zero value if there was an error
            exitValue = proc.waitFor();
            
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Could not compile generated code, \"" 
                    + commandString + "\" failed.");
        }
        if (exitValue != 0) {
            throw new IllegalActionException(
                    "Running \"" + commandString +
                    "\" returned a nonzero value.");
        }

    }

    /** Confirm overwrite of file if the confirmOverwrite parameter is
     *  set to true.  Return true if ok to write file, throw
     *  IllegalActionException if the user cancels the ovewrite.
     *  @param file File to be written.
     *  @return True if ok to write file.
     *  @exception IllegalActionException If code generation should be halted.
     */
    private boolean _confirmOverwrite(File file)
            throws IllegalActionException {
        boolean confirmOverwriteValue = ((BooleanToken) confirmOverwrite
                .getToken()).booleanValue();

        if (confirmOverwriteValue && file.exists()) {
            try {
                if (MessageHandler.yesNoCancelQuestion("Overwrite "
                            + file + "?")) {
                    if (!file.delete()) {
                        throw new IllegalActionException(this,
                                "Could not delete file " + file);
                    }
                } else {
                    return false;
                }
            } catch (CancelException ex) {
                throw new IllegalActionException(
                        this, "Cancelled overwrite of " + file);
            }
        }
        return true;
    }

    // FIXME comment
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
        if (!(container instanceof PtinyOSActor)) {
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
     */
    private void _generateLoader() throws IllegalActionException {
        // Use filename relative to toplevel PtinyOSDirector.
        NamedObj toplevel = _toplevelNC();
        String toplevelName = _sanitizedFullName(toplevel);

        // FIXME: why not just use a StringBuffer?
        _CodeString text = new _CodeString();

        text.addLine("import ptolemy.domains.ptinyos.kernel.PtinyOSLoader;");
        text.addLine("import ptolemy.domains.ptinyos.kernel.PtinyOSDirector;");
        text.addLine("import ptolemy.kernel.util.IllegalActionException;");

        text.addLine("public class Loader" + toplevelName
                + " implements PtinyOSLoader {");

        text.addLine("    public void load(String path, PtinyOSDirector director) {");
        text.addLine("        String fileSeparator = System.getProperty(\"file.separator\");");
        text.addLine("        String toBeLoaded = path + fileSeparator + System.mapLibraryName(\""
                        + toplevelName + "\");");
        text.addLine("        toBeLoaded = toBeLoaded.replace('\\\\', '/');");
        text.addLine("        System.out.println(\"" + toplevelName + ".java : about to load \" + toBeLoaded);");

        text.addLine("        System.load(toBeLoaded);");


        text.addLine("        this.director = director;");
        text.addLine("    }");

        text.addLine("    public int main(String argsToMain[]) {");
        text.addLine("        return main" + toplevelName + "(argsToMain);");
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
                .addLine("    public void enqueueEvent(String newTime) throws IllegalActionException {");
        text.addLine("        this.director.enqueueEvent(newTime);");
        text.addLine("    }");

        text.addLine("    public char getCharParameterValue(String param)");
        text.addLine("            throws IllegalActionException {");
        text
                .addLine("        return this.director.getCharParameterValue(param);");
        text.addLine("    }");

        text
                .addLine("    public boolean getBooleanParameterValue(String param)");
        text.addLine("            throws IllegalActionException {");
        text
                .addLine("        return this.director.getBooleanParameterValue(param);");
        text.addLine("    }");

        text
                .addLine("    public int sendToPort(String portName, String expression)");
        text.addLine("            throws IllegalActionException {");
        text
                .addLine("        return this.director.sendToPort(portName, expression);");
        text.addLine("    }");

        text
                .addLine("    public void tosDebug(String debugMode, String message, String nodeNumber) {");
        text.addLine("        this.director.tosDebug(debugMode, message, nodeNumber);");
        text.addLine("    }");

        text.addLine("    private PtinyOSDirector director;");

        text.addLine("    private native int main" + toplevelName
                + "(String argsToMain[]);");
        text.addLine("    private native void wrapup" + toplevelName
                + "();");
        text.addLine("    private native void processEvent" + toplevelName
                + "(long currentTime);");
        text.addLine("    private native void receivePacket" + toplevelName
                + "(long currentTime, String packet);");
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
        // FIXME example
        
TOSROOT=/home/celaine/tinyos/tinyos/tinyos-1.x-scratch
TOSMAKE_PATH += $(TOSROOT)/contrib/ptII/ptinyos/tools/make
COMPONENT=MicaActor3056
PFLAGS += -I%T/lib/Counters
PFLAGS += -DCOMMAND_PORT=10584 -DEVENT_PORT=10585
MY_PTCC_FLAGS += -D_PTII_NODE_NUM=MicaActor3056
PFLAGS += "-I$(TOSROOT)/contrib/ptII/ptinyos/beta/TOSSIM-packet"
include /home/celaine/ptII/mk/ptII.mk
include /home/celaine/tinyos/tinyos/tinyos-1.x-scratch/tools/make/Makerules

     */
    private String _generateMakefile() throws IllegalActionException {
        // Use filename relative to toplevel PtinyOSDirector.
        NamedObj toplevel = _toplevelNC();
        String toplevelName = _sanitizedFullName(toplevel);

        _CodeString text = new _CodeString();
        text.addLine("TOSROOT=" + tosroot.stringValue());

        // path to contrib
        // FIXME use pathseparator?
        // FIXME make sure no trailing / before /../
        // FIXME: this will not work if TOSROOT has spaces in it.
        text.addLine("TOSMAKE_PATH += $(TOSROOT)/contrib/ptII/ptinyos/tools/make");

        text.addLine("COMPONENT=" + toplevelName);

        text.addLine("PFLAGS += " + pflags.stringValue());

        text.addLine("PFLAGS +=" + " -DCOMMAND_PORT=" + commandPort.getToken()
                + " -DEVENT_PORT=" + eventPort.getToken());

        // Turn _ into _1 for JNI compatibility.
        String nativeMethodName = toplevelName.replaceAll("_", "_1");
        text.addLine("MY_PTCC_FLAGS +=" + " -D_PTII_NODE_NUM=" + nativeMethodName);

        String[] targets = target.stringValue().split("\\s");

        for (int i = 0; i < targets.length; i++) {
            if (targets[i].equals("ptII") || targets[i].equals("all")) {
                // FIXME will this work for "all"?
                text.addLine("PFLAGS += \"-I$(TOSROOT)/contrib/ptII/ptinyos/beta/TOSSIM-packet\"");
                // Expand $PTII, substitute / for \, and backslash space for space.
                String ptIImk = StringUtilities.getProperty("ptolemy.ptII.dir") + "/mk/ptII.mk";
                text.addLine("include " + ptIImk.replaceAll(" ", "\\\\ "));
                break;
            }
        }

        // Handle pathnames with spaces: substitute / for \ and backslash space for space.
        text.addLine("include " + tosroot.stringValue().replace('\\', '/').replaceAll(" ", "\\\\ ")
                + "/tools/make/Makerules");

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

            // FIXMe, generate a notice instead

            /*
             if (sourcePortList.size() > 1) {
             throw new IllegalActionException(inPort,
             "Input port (provides) cannot connect to "
             + "multiple output ports (requires) in NC.");
             }*/

            //            if (sourcePortList.size()== 1) {
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
                    && !(container instanceof PtinyOSActor)) {
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
                actorName = "Unnamed";
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
    
    /** Get the sanitized name with workspace version number appended,
     *  or "Unnamed" if no name.
     */
    private String _sanitizedName(NamedObj obj) {
        String objName = obj.getName();
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
    ////                         native methods                    ////
    // Native methods in TOSSIM code.
    //private native int main(String argsToMain[]);
    //private native int mainMicaActor(String argsToMain[]);
    //private native void processEvent();
    //private native void wrapup();
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Java loader for JNI code.
    private PtinyOSLoader _loader;

    // String to rename unnamed objects or files.
    private static String _unnamed = "Unnamed";

    // Workspace version number at preinitialize.
    private long _version;


    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    
    // Class for creating a StringBuffer that represents generated code.
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
         */
        _StreamReaderThread(InputStream inputStream, String name) {
            this(inputStream, name, null);
        }

        /** Create a _StreamReaderThread.
         *  @param inputStream The stream to read from.
         *  @param name The name of this _StreamReaderThread.
         *  @param redirect The name of the output stream to redirect the
            inputStream to.
         */
        _StreamReaderThread(InputStream inputStream, String name,
                OutputStream redirect) {
            _inputStream = inputStream;
            _name = name;
            _outputStream = redirect;
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

        // Stream from which to read.
        InputStream _inputStream;

        // Name of the input stream.
        String _name;

        // Stream to which to write the redirected input.
        OutputStream _outputStream;
    }
}
