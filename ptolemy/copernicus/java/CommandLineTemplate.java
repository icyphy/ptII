/* A Ptolemy application that instantiates classnames given on the command
   line.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Yellow (cxh@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.copernicus.java;

import java.util.Iterator;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;


/////////////////////////////////////////////////////////////////
//// CommandLineTemplate
/**
This class is similar to CompositeActorApplication, except that it
does not parse command line elements.   It is used as
a template for generating a command line interface for code generated
from a ptolemy model.
<p>
In this case, parsing the command line is not necessary because
parameter values and the class values are fixed by the code generator.

@author Steve Neuendorffer
@version $Id$
@since Ptolemy II 2.0
*/
public class CommandLineTemplate {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new application with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            CommandLineTemplate app = new CommandLineTemplate();
            app.processArgs(args);
            app.waitForFinish();
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();

            //  System.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    /** Parse the command-line arguments, creating models as specified.
     *  @param args The command-line arguments.
     *  @exception Exception If there is a problem processing
     *  the arguments.
     */
    public void processArgs(String args[]) throws Exception {
        if (args != null) {
            for(int i = 0; i < args.length; i++) {
                String arg = args[i];
                if(arg.startsWith("-iterationLimit=")) {
                    String countString = arg.substring(arg.indexOf("=") + 1);
                    _iterationLimit = Integer.parseInt(countString);
                }
            }
        }
        // start the models.
        Iterator models = _models.iterator();
        while (models.hasNext()) {
            
            Runtime runtime = Runtime.getRuntime();
            
            CompositeActor model = (CompositeActor)models.next();
            String modelName = model.getName();
            
            // Allocate string buffers before hand, so that it is
            // not counted as allocated memory.
            StringBuffer buffer1 = new StringBuffer(5000);
            StringBuffer buffer2 = new StringBuffer(5000);
            
            // First, we gc..  This will be recorded in a
            // log file and used to compute memory usage.
            System.gc();
            Thread.sleep(1000);
            
            long startTime = System.currentTimeMillis();
            long totalMemory1 = runtime.totalMemory()/1024;
            long freeMemory1 = runtime.freeMemory()/1024;
            timeAndMemory(startTime,
                    totalMemory1, freeMemory1, buffer1);
            
            System.out.println(modelName +
                    ": Stats before execution:    "
                    + buffer1);
            
            // Second, we run and print memory stats.
            startRun(model);
            
            long totalMemory2 = runtime.totalMemory()/1024;
            long freeMemory2 = runtime.freeMemory()/1024;
            timeAndMemory(startTime,
                    totalMemory2, freeMemory2, buffer2);
            
            System.out.println(modelName +
                    ": Execution stats:           "
                    + buffer2);
            
            // GC, again to the log.
            System.gc();
            Thread.sleep(1000);
            
            long totalMemory3 = runtime.totalMemory()/1024;
            long freeMemory3 = runtime.freeMemory()/1024;
            System.out.println(modelName +
                    ": After Garbage Collection:  "
                    + timeAndMemory(startTime,
                            totalMemory3, freeMemory3));
            
            // Print out the standard stats at the end
            // so as not to break too many scripts
            System.out.println(buffer2.toString());
        }
    }

    /** Report an exception.  This prints a message to the standard error
     *  stream, followed by the stack trace.
     *  @param ex The exception to report.
     */
    public void report(Exception ex) {
        report("", ex);
    }

    /** Report a message to the user.
     *  This prints a message to the standard output stream.
     *  @param message The message to report.
     */
    public void report(String message) {
        System.out.println(message);
    }

    /** Report an exception with an additional message.
     *  This prints a message to standard error, followed by the
     *  stack trace.
     *  @param message The message.
     *  @param ex The exception to report.
     */
    public void report(String message, Exception ex) {
        System.err.println("Exception thrown.\n" + message + "\n"
                + ex.toString());
        ex.printStackTrace();
    }

    /** If the specified model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     *  If the model contains an atomic entity that implements Placeable,
     *  we create create an instance of ModelFrame, if nothing implements
     *  Placeable, then we do not create an instance of ModelFrame.  This
     *  allows us to run non-graphical models on systems that do not have
     *  a display.
     *  <p>
     *  We then start the model running.
     *
     *  @param model The model to execute.
     *  @see ptolemy.actor.Manager#startRun()
     */
    public synchronized void startRun(CompositeActor model) {
        // This method is synchronized so that it can atomically modify
        // the count of executing processes.

        // NOTE: If you modify this method, please be sure that it
        // will work for non-graphical models in the nightly test suite.

        // Iterate through the model, looking for something that is Placeable.
        boolean hasPlaceable = false;
        /* Iterator atomicEntities = model.allAtomicEntityList().iterator();
           while (atomicEntities.hasNext()) {
           Object object = atomicEntities.next();
           if (object instanceof Placeable) {
           hasPlaceable = true;
           break;
           }
           }

           if (hasPlaceable) {
           // The model has an entity that is Placeable, so create a frame.
           try {
           // A model frame with no buttons... just place the
           // placeable actors.
           ModelFrame frame = new ModelFrame(model, null,
           new ModelPane(model, ModelPane.HORIZONTAL, 0));

           _openCount++;
           frame.addWindowListener(new WindowAdapter() {
           public void windowClosed(WindowEvent event) {
           synchronized(CommandLineTemplate.this) {
           _openCount--;
           CommandLineTemplate.this.notifyAll();
           // FIXME: is this right?  We need
           // to exit if all the windows are closed?
           if (_openCount == 0) {
           System.exit(0);
           }
           }
           }
           });
           frame.setBackground(new Color(0xe5e5e5));
           frame.pack();
           frame.centerOnScreen();
           frame.setVisible(true);
           // FIXME: Use a JFrame listener to determine when all windows
           // are closed.
           } catch (Exception ex) {
           ex.printStackTrace();
           System.out.println("startRun: " + ex);
           }
           }
        */

        Manager manager = model.getManager();
        try {
            if (manager == null) {
                model.setManager(new Manager(model.workspace(), "manager"));
                manager = model.getManager();
            }
            long startTime = System.currentTimeMillis();
            manager.startRun();
            System.out.println("Execution stats:");
            System.out.println(timeAndMemory(startTime));

        } catch (IllegalActionException ex) {
            // Model is already running.  Ignore.
            System.out.println("Exception = " + ex);
            ex.printStackTrace();
        }
    }

    // copied from Manager.
    public static String timeAndMemory(long startTime) {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory()/1024;
        long freeMemory = runtime.freeMemory()/1024;
        return timeAndMemory(startTime, totalMemory, freeMemory);
    }

    public static String timeAndMemory(long startTime,
            long totalMemory, long freeMemory) {
        StringBuffer buffer = new StringBuffer();
        timeAndMemory(startTime, totalMemory, freeMemory, buffer);
        return buffer.toString();
    }

    public static void timeAndMemory(long startTime,
            long totalMemory, long freeMemory, StringBuffer buffer) {
        Runtime runtime = Runtime.getRuntime();
        buffer.append(System.currentTimeMillis() - startTime);
        buffer.append(" ms. Memory: ");
        buffer.append(totalMemory);
        buffer.append("K Free: ");
        buffer.append(freeMemory);
        buffer.append("K (");
        buffer.append(Math.round( (((double)freeMemory)/((double)totalMemory))
                * 100.0));
        buffer.append("%)");
    }

    /** If the specified model has a manager and is executing, then
     *  stop execution by calling the finish() method of the manager.
     *  If there is no manager, do nothing.
     *  @param model The model to stop.
     */
    public void stopRun(CompositeActor model) {
        Manager manager = model.getManager();
        if (manager != null) {
            manager.finish();
        }
    }

    /** Wait for all windows to close.
     */
    public synchronized void waitForFinish() {
        while (_openCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    protected int _iterationLimit = Integer.MAX_VALUE;
    /** The list of all the models */
    protected List _models = null; // new LinkedList();

    /** The count of currently open windows. */
    protected int _openCount = 0;

    /** Are we testing? */
    protected static boolean _test = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Flag indicating that the previous argument was -class.
    // Exists to mirror CompositeActorApplication.
    private boolean _expectingClass = false;

}
