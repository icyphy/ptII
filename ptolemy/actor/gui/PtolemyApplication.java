/* A base class for Ptolemy applications.

 Copyright (c) 1999 The Regents of the University of California.
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

@ProposedRating Red (vogel@eecs.berkeley.edu)
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Java imports
import java.lang.System;

// Ptolemy imports
import ptolemy.gui.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// PtolemyApplication
/**
A base class for Ptolemy applications. This is provided
for convenience, in order to promote commonality among certain elements.
It is by no means required in order
to create an application that uses Ptolemy II.
In particular, it creates a manager, a top-level composite
actor; it provides a top-level composite
actor; it provides a mechanism for reporting
errors and exceptions. This class makes no use of any gui features.

@author Brian K. Vogel. This class is based on code written by Edward A. Lee
@version $Id$
*/
public class PtolemyApplication implements ExecutionListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create the model.  In this base class, a manager and
     *  top-level composite actor are created, but nothing more.
     *  Derived classes should extend this method to build the model.
     *  The model is created in a new workspace, where the name of
     *  the workspace is the name of this class.
     */
    public void create() {
        _workspace = new Workspace(getClass().getName());
        try {
            _manager = new Manager(_workspace, "manager");
            _manager.addExecutionListener(this);
            _toplevel = new TypedCompositeActor(_workspace);
            _toplevel.setName("topLevel");
            _toplevel.setManager(_manager);
        } catch (Exception ex) {
            report("Setup of manager and top level actor failed:\n", ex);
        }
    }

    /** Report that an execution error occured.  This is
     *  called by the manager.
     */
    public void executionError(Manager manager, Exception ex) {
        report(ex);
    }

    /** Report that execution of the model has finished.  This is
     *  called by the manager.
     */
    public void executionFinished(Manager manager) {
        System.out.println("Execution finished.");
    }

    /** Report that the manager state has changed.  This is
     *  called by the manager.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newstate = manager.getState();
        if (newstate != _previousState) {
            System.out.println(manager.getState().getDescription());
            _previousState = newstate;
        }
    }

    /** Report an exception.  This prints a message to the standard error
     *  stream, followed by the stack trace.
     */
    public void report(Exception ex) {
        String msg = "Exception thrown by applet.\n" + ex.toString();
        System.err.println(msg);
        ex.printStackTrace();
        System.out.println("Exception occurred.");
        new Message(msg + "\nSee Java console for stack trace.");
    }

    /** Report an exception with an additional message.  Currently
     *  this prints a message to standard error, followed by the stack trace,
     *  although soon it will pop up a message window instead.
     */
    public void report(String message, Exception ex) {
        String msg = "Exception thrown by applet.\n" + message + "\n"
                + ex.toString();
        System.err.println(msg);
        ex.printStackTrace();
        System.out.println("Exception occurred.");
        new Message(msg + "\nSee Java console for stack trace.");
    }

    /** Start execution of the model. This method is called by the
     *  browser or applet viewer to inform this applet that it should
     *  start its execution. It is called after the init method
     *  and each time the applet is revisited in a Web page.
     *  In this base class, this method calls the protected method
     *  _go(), which executes the model.  If a derived class does not
     *  wish to execute the model each time start() is called, it should
     *  override this method with a blank method.
     */
    public void start() {
        try {
            _go();
        } catch (Exception ex) {
            report(ex);
        }
    }

    /** Stop execution of the model. This method is called by the
     *  browser or applet viewer to inform this applet that it should
     *  stop its execution. It is called when the Web page
     *  that contains this applet has been replaced by another page,
     *  and also just before the applet is to be destroyed.
     *  In this base class, this method calls the finish() method
     *  of the manager. If there is no maneger, do nothing.
     */
    public void stop() {
        if(_manager != null) {
            _manager.finish();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Concatenate two parameter info string arrays and return the result.
     *  This is provided to make it easy for derived classes to override
     *  the getParameterInfo() method. The returned array has length equal
     *  to the sums of the lengths of the two arguments, and containing
     *  the arrays contained by the arguments.
     *
     *  @param first The first string array.
     *  @param second The second string array.
     *  @return A concatenated string array.
     */
    protected String[][] _concatStringArrays(
            String[][] first, String[][] second) {
        String[][] newinfo = new String[first.length + second.length][];
        System.arraycopy(first, 0, newinfo, 0, first.length);
        System.arraycopy(second, 0, newinfo, first.length, second.length);
        return newinfo;
    }


    /** Execute the model.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _go() throws IllegalActionException {
        _manager.startRun();
    }

    /** Stop the execution.
     */
    protected void _stop() {
        _manager.finish();
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The workspace that the applet is built in. Each applet has
     *  it own workspace.
     */
    protected Workspace _workspace;

    /** The manager, created in the init() method. */
    protected Manager _manager;

    /** The top-level composite actor, created in the init() method. */
    protected TypedCompositeActor _toplevel;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////


    private Manager.State _previousState;

   
}
