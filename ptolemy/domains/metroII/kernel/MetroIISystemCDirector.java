/* MetroIISystemCDirector wraps a Metro-SystemC model as a Metro actor in Ptolemy.

Copyright (c) 2012-2013 The Regents of the University of California.
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
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.EventVector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
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
     * The executable file name of the MetroII SystemC model.
     */
    public FileParameter modelFileName;

    /**
     * The configuration file name of the MetroII SystemC model.
     */
    public FileParameter configFileName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == modelFileName) {
            File modelFile = modelFileName.asFile();
            if (!modelFile.getName().equals("") && !modelFile.exists()) {
                throw new IllegalActionException(
                        "The modelFileName parameter \""
                        + modelFileName.getExpression()
                        + "\" is not empty yet names a file that does not exist?");
            }

        } else if (attribute == configFileName) {
            //Check if the config is valid.
            StringToken configFileNameToken = (StringToken) configFileName
                    .getToken();
            File configFile = new File(configFileNameToken.stringValue());
            if (!configFile.exists()) {
                throw new IllegalActionException(
                        "The value of the configFileName parameter \""
                                + configFileNameToken.stringValue()
                                + "\" does not exist?");
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
        newObject.events = new LinkedList<Event.Builder>();
        return newObject;
    }

    /**
     * Pushes Metro events into the pipe.
     * 
     * @param events
     *            the events to be added into the pipe.
     * @throws IllegalActionException 
     */
    public void pushEvents(Iterable<Event.Builder> events) throws IllegalActionException {
        if (_debugging) {
            _debug("pushEvents:");
        }

        EventVector.Builder evb = EventVector.newBuilder();
        for (Builder builder : events) {
            evb.addEvent(builder.build());
        }

        EventVector ev = evb.build();
        try {
            FileOutputStream fos = new FileOutputStream(path + pipe2server);
            try {
                ev.writeTo(fos);
            } finally {
                fos.close();
            }
        } catch (FileNotFoundException e) {
            throw new IllegalActionException("Unable to find the pipe file: " + path + pipe2server);
        } catch (IOException e) {
            throw new IllegalActionException("I/O exception caused by the pipe file: " + path + pipe2server);
        }
        //                System.out.println("Pushing events: ");
        //                for (Builder etb : events) {
        //                    System.out
        //                            .println(etb.getName() + " " + etb.getStatus().toString());
        //                }
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
     */
    public void syncEvents(LinkedList<Event.Builder> events) throws IllegalActionException {
        if (_debugging) {
            _debug("syncEvents:");
        }
        if (events == null) {
            throw new NullPointerException("syncEvents(): events argument was null?");
        }
        events.clear();
        EventVector ev = null;
        try {
            FileInputStream fis = new FileInputStream(path + pipe2client);
            try {
                ev = EventVector.parseFrom(fis);
            } finally {
                fis.close();
            } 

        } catch (FileNotFoundException e) {
            throw new IllegalActionException("Unable to find the pipe file: " + path + pipe2client);
        } catch (IOException e) {
            throw new IllegalActionException("I/O exception caused by the pipe file: " + path + pipe2client);
        }

        for (Event e : ev.getEventList()) {
            events.add(e.toBuilder());
        }
        //        System.out.println("Sync events: ");
        //        for (Builder etb : events) {
        //            System.out
        //                    .println(etb.getName() + " " + etb.getStatus().toString());
        //        }
        if (_debugging) {
            for (Builder etb : events) {
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
    }

    /**
     * Fires the wrapped Metro-SystemC model.
     */
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException, IllegalActionException {
        if (modelFileName != null) {
            if (!createProcess) {
                _ioThread = new Thread() {
                    public void run() {
                        String s = null;
                        try {
                            String command = null;
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
                                    process = Runtime.getRuntime()
                                            .exec(command);
                                } catch (IllegalActionException e) {
                                    e.printStackTrace();
                                }

                                stdInput = new BufferedReader(
                                        new InputStreamReader(
                                                process.getInputStream()));

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

                createProcess = true;
            }
        }
        while (!isStopRequested()) {
            if (events == null) {
                throw new NullPointerException(
                        "MetroIISystemCDirector.getFire(): stop was not requested, but events is null??");
            }
            syncEvents(events);

            do {
                resultHandler.handleResult(events);
            } while (!MetroIIEventBuilder.atLeastOneNotified(events)
                    && !isStopRequested());

            pushEvents(events);
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

        path = System.getenv("METRO_TEMP");
        if (path == null) {
            throw new IllegalActionException(
                    this,
                    "The METRO_TEMP property was not set.  Please set it so that both Ptolemy and the C-based MetroII example can access the same directory.");
        }
        path = path + "/";

        m2event_out_pipe_name = path + "m2event_ptolemy_buffer";
        m2event_in_pipe_name = path + "m2event_metro_buffer";

        //        System.out
        //                .println("FIXME: MetroIISystemCDirector.initialize(): Not creating the pipe because this breaks the tests.");

        System.out.println("Waiting for pipes to be created ...");

        List execCommands = new LinkedList();
        execCommands.add("rm -f " + m2event_out_pipe_name);
        execCommands.add("mkfifo " + m2event_out_pipe_name);
        execCommands.add("rm -f " + m2event_in_pipe_name);
        execCommands.add("mkfifo " + m2event_in_pipe_name);

        final StringBufferExec exec = new StringBufferExec();
        exec.setCommands(execCommands);

        exec.start();

        //            Runtime.getRuntime().exec("rm -f " + m2event_out_pipe_name);
        //            Process process = Runtime.getRuntime().exec(
        //                    "mkfifo " + m2event_out_pipe_name);
        //            process.waitFor();
        //            Runtime.getRuntime().exec("rm -f " + m2event_in_pipe_name);
        //            process = Runtime.getRuntime().exec(
        //                    "mkfifo " + m2event_in_pipe_name);
        //            process.waitFor();
        //        } catch (IOException ex) {
        //            throw new IllegalActionException(this, ex,
        //                    "Failed to create pipes!");
        //        } catch (InterruptedException ex) {
        //            throw new IllegalActionException(this, ex,
        //                    "Failed to create pipes!");
        //        }

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

        events = new LinkedList<Event.Builder>();

        createProcess = false;
    }

    /**
     * Stop firing as soon as possible.
     * 
     * IMPORTANT: stop() can only be called after getfire() returns.
     * 
     */
    @Override
    public void stop() {
        _stopRequested = true;

        Event.Builder builder = MetroIIEventBuilder.newProposedEvent("stop");
        builder.setStatus(Status.NOTIFIED);
        events.add(builder);

        try {
            pushEvents(events);
        } catch (IllegalActionException e) {
            e.printStackTrace();
        }
        
        System.out.println(this.getFullName()
                + " sent stop request to SystemC Model!");
    }

    /**
     * Initializes parameters.
     * 
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    private void _initializeParameters() throws IllegalActionException,
            NameDuplicationException {
        modelFileName = new FileParameter(this, "modelFileName");
        configFileName = new FileParameter(this, "configFileName");

    }

    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////

    /**
     * Standard input of the SystemC model.
     */
    BufferedReader stdInput;

    /**
     * Standard error of the SystemC model.
     */
    // BufferedReader stdError;

    /**
     * The full name of the pipe to SystemC model.
     */
    String m2event_out_pipe_name;

    /**
     * The full name of the pipe from SystemC model.
     */
    String m2event_in_pipe_name;

    /**
     * The external process.
     */
    Process process = null;

    /**
     * IO thread
     */
    Thread _ioThread = null;

    /**
     * Current event list
     */
    private LinkedList<Event.Builder> events;

    /**
     * Whether a process is created for the wrapped Metro-SystemC model
     */
    private boolean createProcess;

    /**
     * Path of the pipe
     */
    private String path;

    /**
     * Name of the incoming pipe
     */
    private String pipe2server = "m2event_ptolemy_buffer";

    /**
     * Name of the outcoming pipe
     */
    private String pipe2client = "m2event_metro_buffer";

}
