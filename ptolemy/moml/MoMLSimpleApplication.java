/* An application that executes non-graphical
 models specified on the command line.

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.moml;

import java.io.File;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// MoMLSimpleApplication

/** A simple application that reads in a .xml file as a command
 line argument and runs it.

 <p>MoMLApplication sets the look and feel, which starts up Swing,
 so we can't use MoMLApplication for non-graphical simulations.

 <p>We implement the ChangeListener interface so that this
 class will get exceptions thrown by failed change requests.

 For example to use this class, try:
 <pre>
 java -classpath $PTII ptolemy.actor.gui.MoMLSimpleApplication ../../../ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml
 </pre>

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (eal)
 */
public class MoMLSimpleApplication implements ChangeListener, ExecutionListener {

    /** Instantiate a MoMLSimpleApplication.  This constructor is
     * probably not useful by itself, it is for use by subclasses.
     *
     * <p>The HandSimDroid work in $PTII/ptserver uses dependency
     * injection to determine which implementation actors such as
     * Const and Display to use.  This method reads the
     * ptolemy/actor/ActorModule.properties file.</p>
     *
     *  @exception Exception Not thrown in this base class
     */
    public MoMLSimpleApplication() throws Exception {
        ActorModuleInitializer.initializeInjector();
    }

    /** Parse the xml file and run it.
     *  @param xmlFileName A string that refers to an MoML file that
     *  contains a Ptolemy II model.  The string should be
     *  a relative pathname.
     *  @exception Throwable If there was a problem parsing
     *  or running the model.
     */
    public MoMLSimpleApplication(String xmlFileName) throws Throwable {
        this();
        _workspace = new Workspace("MoMLSimpleApplicationWorkspace");
        _parser = new MoMLParser();

        // The test suite calls MoMLSimpleApplication multiple times,
        // and the list of filters is static, so we reset it each time
        // so as to avoid adding filters every time we run an auto test.
        // We set the list of MoMLFilters to handle Backward Compatibility.
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters(),
                _workspace);

        // Filter out any graphical classes.
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        // If there is a MoML error, then throw the exception as opposed
        // to skipping the error.  If we call StreamErrorHandler instead,
        // then the nightly build may fail to report MoML parse errors
        // as failed tests
        //parser.setErrorHandler(new StreamErrorHandler());
        // We use parse(URL, URL) here instead of parseFile(String)
        // because parseFile() works best on relative pathnames and
        // has problems finding resources like files specified in
        // parameters if the xml file was specified as an absolute path.
        _toplevel = (CompositeActor) _parser.parse(null, new File(xmlFileName)
                .toURI().toURL());

        // If the model is a top level, and a model error handler has not been set,
        // then set a BasicModelErrorHandler.
        // (PtolemyFrame has similar code.)
        if (_toplevel.getContainer() == null) {
            if (_toplevel.getModelErrorHandler() == null) {
                _toplevel.setModelErrorHandler(new BasicModelErrorHandler());
            }
        }

        _manager = new Manager(_toplevel.workspace(), "MoMLSimpleApplication");
        _toplevel.setManager(_manager);
        _toplevel.addChangeListener(this);

        _manager.addExecutionListener(this);
        _activeCount++;

        _manager.startRun();

        Thread waitThread = new UnloadThread();

        // Note that we start the thread here, which could
        // be risky when we subclass, since the thread will be
        // started before the subclass constructor finishes (FindBugs)
        waitThread.start();
        waitThread.join();
        if (_sawThrowable != null) {
            throw _sawThrowable;
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         Public methods                    ////

    /** React to a change request has been successfully executed by
     *  doing nothing. This method is called after a change request
     *  has been executed successfully.  In this class, we
     *  do nothing.
     *  @param change The change that has been executed, or null if
     *   the change was not done via a ChangeRequest.
     */
    @Override
    public void changeExecuted(ChangeRequest change) {
    }

    /** React to a change request that has resulted in an exception.
     *  This method is called after a change request was executed,
     *  but during the execution in an exception was thrown.
     *  This method throws a runtime exception with a description
     *  of the original exception.
     *  @param change The change that was attempted or null if
     *   the change was not done via a ChangeRequest.
     *  @param exception The exception that resulted.
     */
    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        // If we do not implement ChangeListener, then ChangeRequest
        // will print any errors to stdout and continue.
        // This causes no end of trouble with the test suite
        // We can't throw an Exception here because this method in
        // the base class does not throw Exception.
        String description = "";

        if (change != null) {
            description = change.getDescription();
        }

        throw new RuntimeException("MoMLSimplApplication.changeFailed(): "
                + description + " failed: ", exception);
    }

    /** Clean up by freeing memory.  After calling cleanup(), do not call
     *  rerun().
     */
    public void cleanup() {
        // The next line removes the static backward compatibility
        // filters, which is probably not what we want if we
        // want to parse another file.
        //BackwardCompatibility.clear();

        // The next line will remove the static MoMLParser (_filterMoMLParser)
        // used by the filters.  If we add filters, then the _filterMoMLParser
        // is recreated, so this is probably safe.  We need to get rid
        // of _filterMoMLParser so that the _manager is collected.
        MoMLParser.setMoMLFilters(null);

        _parser.resetAll();
        // _parser is a protected variable so setting it to
        // null will (hopefully) cause the garbage
        // collector to collect it.
        _parser = null;

        // _manager is a protected variable so setting it to
        // null will (hopefully) cause the garbage
        // collector to collect it.
        _manager = null;

        // _toplevel and _workspace are protected variables so
        // setting it to null will (hopefully) cause the
        // garbage collector to collect them

        // Set toplevel to null so that the Manager is collected.
        _toplevel = null;

        // Set workspace to null so that the objects contained
        // by the workspace may be collected.
        _workspace = null;
    }

    /** Report an execution failure.   This method will be called
     *  when an exception or error is caught by a manager.
     *  Exceptions are reported this way when the run() or startRun()
     *  methods of the manager are used to perform the execution.
     *  If instead the execute() method is used, then exceptions are
     *  not caught, and are instead just passed up to the caller of
     *  the execute() method.  Those exceptions are not reported
     *  here (unless, of course, the caller of the execute() method does
     *  so).
     *  In this class, we set a flag indicating that execution has finished.
     *
     *  @param manager The manager controlling the execution.
     *  @param throwable The throwable to report.
     */
    @Override
    public synchronized void executionError(Manager manager, Throwable throwable) {
        _sawThrowable = throwable;
        _executionFinishedOrError = true;
        _activeCount--;
        if (_activeCount <= 0) {
            notifyAll();
        }
    }

    /** Report that the current execution has finished and
     *  the wrapup sequence has completed normally. The number of successfully
     *  completed iterations can be obtained by calling getIterationCount()
     *  on the manager.
     *  In this class, we set a flag indicating that execution has finished.
     *  @param manager The manager controlling the execution.
     */
    @Override
    public synchronized void executionFinished(Manager manager) {
        _activeCount--;
        _executionFinishedOrError = true;
        if (_activeCount <= 0) {
            notifyAll();
        }
    }

    /** Report that the manager has changed state.
     *  To access the new state, use the getState() method of Manager.
     *  In this class, do nothing.
     *  @param manager The manager controlling the execution.
     *  @see Manager#getState()
     */
    @Override
    public void managerStateChanged(Manager manager) {
    }

    /** Create an instance of each model file named in the arguments
     *  and run it.
     *  @param args The command-line arguments naming the Ptolemy II
     *  model files (typically .xml files) to be invoked.
     */
    public static void main(String[] args) {
        try {
            for (String arg : args) {
                new MoMLSimpleApplication(arg);
            }
        } catch (Throwable ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
            StringUtilities.exit(1);
        }
    }

    /** Execute the same model again.
     *  @exception Exception if there was a problem rerunning the model.
     */
    public void rerun() throws Exception {
        _manager.execute();
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
    ////                         protected variables               ////

    /** The count of currently executing runs. */
    protected volatile int _activeCount = 0;

    /** A flag that indicates if the execution has finished or thrown
     *  an error.  The code busy waits until executionFinished() or
     *  executionError() is called.  If this variable is true and
     *  _sawThrowable is null then executionFinished() was called.  If
     *  this variable is true and _sawThrowable is non-null then
     *  executionError() was called.
     */
    protected volatile boolean _executionFinishedOrError = false;

    /** The manager of this model. */
    protected Manager _manager = null;

    /** The MoMLParser used to parse the model. */
    protected volatile MoMLParser _parser;

    /** The exception seen by executionError(). */
    protected volatile Throwable _sawThrowable = null;

    /** The toplevel model that is created and then destroyed. */
    protected volatile CompositeActor _toplevel;

    /** The workspace in which the model and Manager are created. */
    protected volatile Workspace _workspace;

    ///////////////////////////////////////////////////////////////////
    ////                         private inner classes             ////

    /** Wait for the run to finish and the unload the model.
     */
    private class UnloadThread extends Thread {
        @Override
        public void run() {
            waitForFinish();
            if (_sawThrowable != null) {
                throw new RuntimeException("Execution failed", _sawThrowable);
            }
        }
    }
}
