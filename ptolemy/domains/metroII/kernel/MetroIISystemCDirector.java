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
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.EventVector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
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
     * Construct a MetroIISystemCDirector with a name and a container. The
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

    public FileParameter modelFileName;

    public FileParameter configFileName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == modelFileName) {
            StringToken modelFileNameToken = (StringToken) modelFileName
                    .getToken();
            if (modelFileNameToken == null
                    || modelFileNameToken.stringValue().equals("")) {
                modelFileName = null;
            } else {
                // May check if the executable model is valid.
            }

        } else if (attribute == configFileName) {
            //Check if the config is valid.
        } else {
            super.attributeChanged(attribute);
        }
    }

    /**
     * Clone the object into the specified workspace. The new object is
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
        newObject.events = (LinkedList<Event.Builder>) events.clone();
        return newObject;
    }

    /**
     * Push Metro events into the pipe.
     * 
     * @param events
     *            the events to be added into the pipe.
     */
    public void pushEvents(Iterable<Event.Builder> events) {
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //        System.out.println("Pushing events: ");
        //        for (Builder etb : events) {
        //            System.out
        //                    .println(etb.getName() + " " + etb.getStatus().toString());
        //        }
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
     * Synchronize the status of events from the pipe.
     * 
     * @param events
     *            the events to be synchronized from the pipe.
     */
    public void syncEvents(LinkedList<Event.Builder> events) {
        if (_debugging) {
            _debug("syncEvents:");
        }
        events.clear();
        EventVector ev = null;
        try {
            FileInputStream fis = new FileInputStream(path + pipe2client);
            try {
                ev = EventVector.parseFrom(fis);
            } finally {
                fis.close();
            } // TODO Auto-generated catch block

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
     * Fire the wrapped Metro-SystemC model.
     */
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
        if (modelFileName != null) {
            if (!createProcess) {
                _ioThread = new Thread() {
                    public void run() {
                        String s = null;
                        try {
                            try {
                                StringToken modelFileNameToken;
                                StringToken configFileNameToken;
                                try {
                                    modelFileNameToken = (StringToken) modelFileName
                                            .getToken();
                                    configFileNameToken = (StringToken) configFileName
                                            .getToken();
                                    System.out
                                            .println(modelFileNameToken
                                                    .stringValue()
                                                    + " "
                                                    + configFileNameToken
                                                            .stringValue());
                                    // using the Runtime exec method:
                                    process = Runtime.getRuntime().exec(
                                            modelFileNameToken.stringValue()
                                                    + " "
                                                    + configFileNameToken
                                                            .stringValue());
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
                                stdInput.close();
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
        syncEvents(events);

        do {
            resultHandler.handleResult(events);
        } while (!MetroIIEventBuilder.atLeastOneNotified(events)
                && !isStopRequested());

        if (isStopRequested()) {
            Event.Builder builder = MetroIIEventBuilder
                    .newProposedEvent("stop");
            events.add(builder);
        }

        pushEvents(events);

    }

    /**
     * Return the iterator for the caller function of getfire().
     * 
     * @return iterator the iterator for the caller function of getfire().
     */
    @Override
    public YieldAdapterIterable<Iterable<Builder>> adapter() {
        return new ThreadedYieldAdapter<Iterable<Event.Builder>>()
                .adapt(new Collector<Iterable<Event.Builder>>() {
                    public void collect(
                            ResultHandler<Iterable<Event.Builder>> resultHandler)
                            throws CollectionAbortedException {
                        getfire(resultHandler);
                    }
                });
    }

    /**
     * Initialize the pipe connecting to Metro-SystemC.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        path = System.getenv("METRO_TEMP");
        path = path + "/";
        if (path == null) {
            throw new IllegalActionException(this,
                    "Environment varialble METRO_TEMP is not accessable.");
        }

        m2event_out_pipe_name = path + "m2event_ptolemy_buffer";
        m2event_in_pipe_name = path + "m2event_metro_buffer";

//        System.out
//                .println("FIXME: MetroIISystemCDirector.initialize(): Not creating the pipe because this breaks the tests.");

        // try {
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
        File pipe2 = new File(m2event_in_pipe_name);

        if (!pipe1.exists() || !pipe2.exists()) {
            throw new IllegalActionException(this, "Failed to create pipes!");
        }

        events = new LinkedList<Event.Builder>();

        createProcess = false;

    }

    /**
     * Stop firing as soon as possible.
     */
    @Override
    public void stop() {
        _stopRequested = true;

        //        if (process != null) {
        //            process.destroy();
        //            System.out.println("The SystemC model was stopped.");
        //        }
    }

    private void _initializeParameters() throws IllegalActionException,
            NameDuplicationException {
        modelFileName = new FileParameter(this, "modelFileName");
        configFileName = new FileParameter(this, "configFileName");

        //        DoubleToken stopTimeValue = (DoubleToken) stopTime.getToken();
        //        if (stopTimeValue != null) {
        //            _stopTime = new Time(this, stopTimeValue.doubleValue());
        //        } else {
        //            _stopTime = null;
        //        }

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
