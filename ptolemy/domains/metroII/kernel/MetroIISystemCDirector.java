/* MetroIISystemCDirector wraps a Metro-SystemC model as a Metro actor in Ptolemy.

Copyright (c) 2012-2014 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Director;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.EventVector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.StringBufferExec;

///////////////////////////////////////////////////////////////////
//// MetroSystemCDirector

/**
 * MetroIISystemCDirector wraps a Metro-SystemC model as a Metro actor in
 * Ptolemy.
 *
 * @author Liangpeng Guo
 * @version $Id: MetroIISystemCDirector.java 67896 2013-11-20 02:27:48Z
 *          hudson@moog.eecs.berkeley.edu $
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class MetroIISystemCDirector extends Director implements GetFirable {

    /**
     * Constructs a MetroIISystemCDirector with a name and a container. The
     * container argument must not be null, or a NullPointerException will be
     * thrown.
     *
     * @param container
     *            The container.
     * @param name
     *            The name of this actor.
     * @exception IllegalActionException
     *                If the container is incompatible with this actor.
     * @exception NameDuplicationException
     *                If the name coincides with an actor already in the
     *                container.
     */
    public MetroIISystemCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initializeParameters();
    }

    /**
     * The environmental variable METROII.
     */
    public Parameter metroII;

    /**
     * The executable file name of the MetroII SystemC model.
     */
    public FileParameter modelFileName;

    /**
     * The configuration file name of the MetroII SystemC model.
     */
    public FileParameter configFileName;

    /**
     * Option parameter whether debug info is printed out.
     */
    public Parameter printDebug;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == modelFileName) {
            File modelFile = modelFileName.asFile();
            // modelFile could be null during cloning.
            if (modelFile != null && !modelFile.getName().equals("")
                    && !modelFile.exists()) {
                throw new IllegalActionException(
                        "The modelFileName parameter \""
                                + modelFileName.getExpression()
                                + "\" is not empty yet names a file that does not exist?");
            }

        } else if (attribute == configFileName) {
            //Check if the config is valid.
            File configFile = configFileName.asFile();
            // configFile could be null during cloning.
            if (configFile != null && !configFile.getName().equals("")
                    && !configFile.exists()) {
                throw new IllegalActionException(
                        "The value of the configFileName parameter \""
                                + configFileName.stringValue()
                                + "\" does not exist?");
            }
        } else if (attribute == printDebug) {
            if (((BooleanToken) printDebug.getToken()).booleanValue()) {
                _debugger.turnOnDebugging();
            } else {
                _debugger.turnOffDebugging();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clones the object into the specified workspace. The new object is
     * <i>not</i> added to the directory of that workspace (you must do this
     * yourself if you want it there).
     *
     * @param workspace
     *            The workspace for the cloned object.
     * @exception CloneNotSupportedException
     *                Not thrown in this base class
     * @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIISystemCDirector newObject = (MetroIISystemCDirector) super
                .clone(workspace);
        newObject._events = new LinkedList<Event.Builder>();
        newObject._debugger = _debugger.clone();
        return newObject;
    }

    /**
     * Pushes Metro events into the pipe.
     *
     * @param events
     *            the events to be added into the pipe.
     * @exception IllegalActionException
     */
    public void pushEvents(Iterable<Event.Builder> events)
            throws IllegalActionException {
        if (_debugging) {
            _debug("pushEvents:");
        }

        EventVector.Builder evb = EventVector.newBuilder();
        for (Builder builder : events) {
            evb.addEvent(builder.build());
        }

        EventVector ev = evb.build();
        try {
            FileOutputStream fos = new FileOutputStream(_pipePath
                    + _pipe2server);
            try {
                ev.writeTo(fos);
            } finally {
                fos.close();
            }
        } catch (FileNotFoundException e) {
            throw new IllegalActionException("Unable to find the pipe file: "
                    + _pipePath + _pipe2server);
        } catch (IOException e) {
            throw new IllegalActionException(
                    "I/O exception caused by the pipe file: " + _pipePath
                    + _pipe2server);
        }

        _debugger.printTitle("Pushing events: ");
        _debugger.printMetroEvents(events);

        if (_debugging) {
            for (Builder etb : events) {
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
        if (_debugging) {
            _debug("finished pushEvents");
        }

    }

    /**
     * Synchronizes the status of events from the pipe.
     *
     * @param events
     *            the events to be synchronized from the pipe.
     * @exception IllegalActionException If the pipe file cannot be
     * found or written.
     */
    public void syncEvents(LinkedList<Event.Builder> events)
            throws IllegalActionException {
        if (_debugging) {
            _debug("syncEvents:");
        }
        if (events == null) {
            throw new NullPointerException(
                    "syncEvents(): events argument was null?");
        }
        events.clear();
        EventVector ev = null;
        try {
            FileInputStream fis = new FileInputStream(_pipePath + _pipe2client);
            try {
                ev = EventVector.parseFrom(fis);
            } finally {
                fis.close();
            }

        } catch (FileNotFoundException e) {
            throw new IllegalActionException("Unable to find the pipe file: "
                    + _pipePath + _pipe2client);
        } catch (IOException e) {
            throw new IllegalActionException(
                    "I/O exception caused by the pipe file: " + _pipePath
                    + _pipe2client);
        }

        for (Event e : ev.getEventList()) {
            events.add(e.toBuilder());
        }

        _debugger.printTitle("Sync events: ");
        _debugger.printMetroEvents(events);

        if (_debugging) {
            for (Builder etb : events) {
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
    }

    /**
     * Fires the wrapped Metro-SystemC model.
     */
    @Override
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException, IllegalActionException {
        if (modelFileName != null) {
            if (_process == null) {
                /**
                 * IO thread
                 */
                Thread _ioThread = new Thread() {
                    @Override
                    public void run() {
                        String s = null;
                        try {
                            String command = null;
                            BufferedReader stdInput = null;

                            try {
                                StringToken modelFileNameToken;
                                StringToken configFileNameToken;
                                try {
                                    modelFileNameToken = (StringToken) modelFileName
                                            .getToken();
                                    configFileNameToken = (StringToken) configFileName
                                            .getToken();
                                    File modelFile = new File(
                                            modelFileNameToken.stringValue());
                                    if (!modelFile.exists()) {
                                        throw new IllegalActionException(
                                                "The value of the modelFileName parameter \""
                                                        + modelFileNameToken
                                                        .stringValue()
                                                        + "\" does not exist?");
                                    }

                                    command = modelFileNameToken.stringValue()
                                            + " "
                                            + configFileNameToken.stringValue();
                                    System.out
                                    .println("The MetroII command is: "
                                            + command);
                                    // Using the Runtime exec method:
                                    _process = Runtime.getRuntime().exec(
                                            command);
                                } catch (IllegalActionException e) {
                                    e.printStackTrace();
                                }

                                stdInput = new BufferedReader(
                                        new InputStreamReader(
                                                _process.getInputStream()));

                                // read the output from the command

                                while ((s = stdInput.readLine()) != null) {
                                    System.out.println(s);
                                }

                            } finally {
                                if (stdInput != null) {
                                    try {
                                        stdInput.close();
                                    } catch (IOException ex) {
                                        System.err
                                        .println("Hmm, failed to close the stdInput stream of the subprocess \""
                                                + (command == null ? "null?"
                                                        : command)
                                                        + "\"");
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                _ioThread.start();
            }
        }
        while (!isStopRequested()) {
            if (_events == null) {
                throw new NullPointerException(
                        "MetroIISystemCDirector.getFire(): stop was not requested, but events is null??");
            }
            syncEvents(_events);

            do {
                resultHandler.handleResult(_events);
            } while (!MetroIIEventBuilder.atLeastOneNotified(_events)
                    && !isStopRequested());

            pushEvents(_events);
        }

    }

    /**
     * Returns the iterator for the caller function of getfire().
     *
     * @return iterator the iterator for the caller function of getfire().
     */
    @Override
    public YieldAdapterIterable<Iterable<Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    @Override
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                                    throws CollectionAbortedException,
                                    IllegalActionException {
                        getfire(resultHandler);
                    }
                });
    }

    /**
     * Initializes the pipe connecting to Metro-SystemC.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _pipePath = System.getenv("METRO_TEMP");
        if (_pipePath == null) {
            throw new IllegalActionException(
                    this,
                    "The METRO_TEMP property was not set.  Please set it so that both Ptolemy and the C-based MetroII example can access the same directory.");
        }
        _pipePath = _pipePath + "/";

        String m2event_out_pipe_name = _pipePath + "m2event_ptolemy_buffer";
        String m2event_in_pipe_name = _pipePath + "m2event_metro_buffer";

        _debugger.printText("Waiting for pipes to be created ...");

        List execCommands = new LinkedList();
        execCommands.add("rm -f " + m2event_out_pipe_name);
        execCommands.add("mkfifo " + m2event_out_pipe_name);
        execCommands.add("rm -f " + m2event_in_pipe_name);
        execCommands.add("mkfifo " + m2event_in_pipe_name);

        final StringBufferExec exec = new StringBufferExec();
        exec.setCommands(execCommands);

        _debugger.printText("The following pipes were created successfully:");
        _debugger.printText(m2event_out_pipe_name);
        _debugger.printText(m2event_in_pipe_name);

        exec.start();

        File pipe1 = new File(m2event_out_pipe_name);
        if (!pipe1.exists()) {
            throw new IllegalActionException(this,
                    "Failed to create a pipe named \"" + m2event_out_pipe_name
                    + "\".");
        }
        File pipe2 = new File(m2event_in_pipe_name);
        if (!pipe2.exists()) {
            throw new IllegalActionException(this,
                    "Failed to create a pipe named \"" + m2event_in_pipe_name
                    + "\".");
        }

        _events = new LinkedList<Event.Builder>();

        _process = null;
    }

    /**
     * Stop firing as soon as possible.
     *
     * IMPORTANT: stop() can only be called after getfire() returns.
     */
    @Override
    public void stop() {
        _stopRequested = true;

        Event.Builder builder = MetroIIEventBuilder.newProposedEvent("stop");
        builder.setStatus(Status.NOTIFIED);
        _events.add(builder);

        try {
            pushEvents(_events);
        } catch (IllegalActionException e) {
            // FIXME: Printing the stack trace will not be useful if Ptolemy is invoked from a menu choice.
            e.printStackTrace();
        }

        System.out.println(this.getFullName()
                + " sent stop request to SystemC Model!");

    }

    /**
     * Initializes parameters.
     *
     * @exception IllegalActionException
     * @exception NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
    NameDuplicationException {
        startTime.setVisibility(Settable.NONE);
        stopTime.setVisibility(Settable.NONE);
        localClock.setVisibility(Settable.NONE);

        metroII = new Parameter(this, "METROII");
        metroII.setExpression("\"" + System.getenv("METROII") + "\"");
        metroII.setVisibility(Settable.NOT_EDITABLE);
        modelFileName = new FileParameter(this, "modelFileName");
        configFileName = new FileParameter(this, "configFileName");

        printDebug = new Parameter(this, "printDebug");
        printDebug.setTypeEquals(BaseType.BOOLEAN);
        printDebug.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * Debugger
     */
    private MetroIIDebugger _debugger = new MetroIIDebugger();

    /**
     * Current event list
     */
    private LinkedList<Event.Builder> _events;

    /**
     * The external process running SystemC model
     */
    private Process _process = null;

    /**
     * Path of the pipe
     */
    private String _pipePath;

    /**
     * Name of the incoming pipe
     */
    private final String _pipe2server = "m2event_ptolemy_buffer";

    /**
     * Name of the outcoming pipe
     */
    private final String _pipe2client = "m2event_metro_buffer";

}
