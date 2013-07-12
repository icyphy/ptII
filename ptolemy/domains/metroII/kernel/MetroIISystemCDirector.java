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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import net.jimblackler.Utils.CollectionAbortedException;
import net.jimblackler.Utils.Collector;
import net.jimblackler.Utils.ResultHandler;
import net.jimblackler.Utils.ThreadedYieldAdapter;
import net.jimblackler.Utils.YieldAdapterIterable;
import ptolemy.actor.Director;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Builder;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.Event.Status;
import ptolemy.domains.metroII.kernel.util.ProtoBuf.metroIIcomm.EventVector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// MetroSystemCDirector

/** 
* MetroIISystemCDirector wraps a Metro-SystemC model as a Metro actor in Ptolemy.
*
* @author Liangpeng Guo
* @version $Id$
* @since Ptolemy II 9.1
* @Pt.ProposedRating Red (glp)
* @Pt.AcceptedRating Red (glp)
*
*/
public class MetroIISystemCDirector extends Director implements
        MetroEventHandler {

    /** Construct a MetroIISystemCDirector with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public MetroIISystemCDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        initialize();
    }

    /** Clone the object into the specified workspace. The new object
     *  is <i>not</i> added to the directory of that workspace (you
     *  must do this yourself if you want it there).
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MetroIISystemCDirector newObject = (MetroIISystemCDirector) super
                .clone(workspace);
        newObject.events = (LinkedList<Event.Builder>) events.clone();
        return newObject;
    }

    /**
     * Push Metro events into the pipe
     * @param events
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
        System.out.println("Pushing events: ");
        for (Builder etb : events) {
            System.out
                    .println(etb.getName() + " " + etb.getStatus().toString());
        }
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
     * Synchronize the status of events from the pipe
     * @param events
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
            }
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
        System.out.println("Sync events: ");
        for (Builder etb : events) {
            System.out
                    .println(etb.getName() + " " + etb.getStatus().toString());
        }
        if (_debugging) {
            for (Builder etb : events) {
                _debug(etb.getName() + " " + etb.getStatus().toString());
            }
        }
    }

    /**
     * Check if at least one event is notified in the event vector
     * @param events Event vector
     * @return Whether there is at least one event notified
     */
    public boolean atLeastOneNotified(Iterable<Event.Builder> events) {
        for (Builder event : events) {
            if (event.getStatus() == Status.NOTIFIED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fire the wrapped Metro-SystemC model
     */
    public void getfire(ResultHandler<Iterable<Event.Builder>> resultHandler)
            throws CollectionAbortedException {
//        if (!createProcess) {
//            new Thread() {
//                public void run() {
//                    String s = null;
//                    try {
//                        // using the Runtime exec method:
//                        Process p = Runtime.getRuntime().exec(
//                                "ptolemy/metroII/single-cpu");
//
//                        BufferedReader stdInput = new BufferedReader(
//                                new InputStreamReader(p.getInputStream()));
//
//                        BufferedReader stdError = new BufferedReader(
//                                new InputStreamReader(p.getErrorStream()));
//
//                        // read the output from the command
//                        System.out
//                                .println("Here is the standard output of the command:\n");
//                        while ((s = stdInput.readLine()) != null) {
//                            System.out.println(s);
//                        }
//
//                        // read any errors from the attempted command
//                        System.out
//                                .println("Here is the standard error of the command (if any):\n");
//                        while ((s = stdError.readLine()) != null) {
//                            System.out.println(s);
//                        }
//
//                        // System.exit(0);
//                    } catch (IOException e) {
//                        System.out
//                                .println("exception happened - here's what I know: ");
//                        e.printStackTrace();
//                        System.exit(-1);
//                    }
//                }
//            }.start();
//            createProcess = true;
//
//        }
//
//        while (!createProcess) {
//
//        }
        syncEvents(events);

        do {
            resultHandler.handleResult(events);
        } while (!atLeastOneNotified(events));

        pushEvents(events);
    }

    /**
     * YieldAdapter interface
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
     * Initialize the pipe connecting to Metro-SystemC
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        path = System.getenv("METRO_TEMP");
        path = path + "/";
        if (path == null) {
            throw new IllegalActionException(
                    "Environment varialble METRO_TEMP is not accessable.");
        }
        events = new LinkedList<Event.Builder>();

//        createProcess = false;

    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    private fields                         ////
    
    /**
     * Current event list
     */
    private LinkedList<Event.Builder> events;

//    /**
//     * Whether a process is created for the wrapped Metro-SystemC model
//     */
//    private boolean createProcess;

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
