/* An application that executes models specified on the command line.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// PtExecuteApplication
/**
This application executes Ptolemy II models specified on the
command line.  The exact facilities that are available are determined
by the configuration file ptolemy/configs/runConfiguration.xml,
which is loaded before any command-line arguments are processed.
If there are no command-line arguments at all, then this class
does nothing.

@author Edward A. Lee and Steve Neuendorffer
@version $Id$
@since Ptolemy II 1.0
@see ModelFrame
@see RunTableau
*/
public class PtExecuteApplication extends MoMLApplication
    implements ExecutionListener {

    /** Parse the specified command-line arguments, creating models
     *  and running them.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public PtExecuteApplication(String args[]) throws Exception {
        // FIXME: Under JDK1.3.1_06, the MoMLApplication constructor
        // calls setLookAndFeel() which invokes getDefaultToolkit()
        // which may cause PtExecuteApplication to not exit.  See
        // http://developer.java.sun.com/developer/bugParade/bugs/4030718.html
        // However, since we now run with JDK1.4.1, this should not
        // be a problem.
        super(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reduce the count of executing models by one.  If the number of
     *  executing models drops ot zero, then notify threads that might
     *  be waiting for this event.
     *  @param manager The manager calling this method.
     *  @param throwable The throwable being reported.
     */
    public synchronized void executionError(Manager manager,
            Throwable throwable) {
        _activeCount--;
        if (_activeCount == 0) {
            notifyAll();
        }
    }

    /**  Reduce the count of executing models by one.  If the number of
     *  executing models drops ot zero, then notify threads that might
     *  be waiting for this event.
     *  @param manager The manager calling this method.
     */
    public synchronized void executionFinished(Manager manager) {
        _activeCount--;
        if (_activeCount == 0) {
            notifyAll();
        }
    }

    /** Create a new instance of this application, passing it the
     *  command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            PtExecuteApplication application = new PtExecuteApplication(args);
            application.runModels();
        } catch (Exception ex) {
            MessageHandler.error("Command failed", ex);
            System.exit(0);
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

    /** Do nothing.
     *  @param manager The manager calling this method.
     */
    public void managerStateChanged(Manager manager) {
    }

    /** Return a list of the Ptolemy II models that were created by processing
     *  the command-line arguments.
     *  @return A list of instances of NamedObj.
     */
    public List models() {
        LinkedList result = new LinkedList();
        ModelDirectory directory
            = (ModelDirectory)_configuration
            .getEntity(Configuration._DIRECTORY_NAME);
        Iterator effigies = directory.entityList().iterator();
        while (effigies.hasNext()) {
            Effigy effigy = (Effigy)effigies.next();
            if (effigy instanceof PtolemyEffigy) {
                NamedObj model = ((PtolemyEffigy)effigy).getModel();
                result.add(model);
            }
        }
        return result;
    }

    /** Start the models running, each in a new thread, then return.
     *  @exception IllegalActionException If the manager throws it.
     */
    public void runModels() throws IllegalActionException {
        Iterator models = models().iterator();
        while (models.hasNext()) {
            NamedObj model = (NamedObj)models.next();
            if (model instanceof CompositeActor) {
                CompositeActor actor = (CompositeActor)model;
                // Create a manager if necessary.
                Manager manager = actor.getManager();
                if (manager == null) {
                    manager = new Manager(actor.workspace(), "manager");
                    actor.setManager(manager);
                }
                manager.addExecutionListener(this);
                _activeCount++;
                // Run the model in a new thread.
                manager.startRun();
            }
        }
    }

    /** Wait for all executing runs to finish, then return.
     */
    public synchronized void waitForFinish() {
        while (_activeCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a default Configuration, which in this case is given by
     *  the MoML file ptolemy/configuration/runPanelConfiguration.xml.
     *  That configuration supports executing, but not editing,
     *  Ptolemy models.
     *  @return A default configuration.
     *  @exception Exception If the configuration cannot be opened.
     */
    protected Configuration _createDefaultConfiguration() throws Exception {
        URL inURL = specToURL(
                "ptolemy/configs/runConfiguration.xml");
        MoMLParser parser = new MoMLParser();
        _configuration =
            (Configuration)parser.parse(inURL, inURL.openStream());
        return _configuration;
    }

    /** Throw an exception.
     *  @return Does not return.
     *  @exception Exception Always thrown.
     */
    protected Configuration _createEmptyConfiguration() throws Exception {
        throw new Exception("No model specified.");
    }

    /** Parse the command-line arguments. This overrides the base class
     *  only to set the usage information.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected synchronized void _parseArgs(String args[]) throws Exception {
        _commandTemplate = "ptexecute [ options ] file ...";

        super._parseArgs(args);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The configuration.
    private Configuration _configuration;

    // The count of currently executing runs.
    private int _activeCount = 0;
}
