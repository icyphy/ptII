/* A base class for Ptolemy applets.

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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (jorhr@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Java imports
import java.applet.Applet;
import java.lang.System;
import java.awt.*;
import java.awt.event.*;

// Ptolemy imports
import ptolemy.gui.*;
import ptolemy.actor.*;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// PtolemyApplet
/**
A base class for Ptolemy applets.  This is provided for convenience,
in order to promote certain common elements among applets.  It is by
no means required in order to create an applet that uses Ptolemy II.
In particular, it creates a manager and optionally creates on-screen
controls for model execution; it provides a top-level composite
actor; it provides a mechanism for reporting
errors and exceptions; and it provide an applet parameter for
controlling the background color.

@author Edward A. Lee
@version $Id$
*/
public class PtolemyApplet extends Applet implements ExecutionListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

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
        showStatus("Execution finished.");
    }

    /** Return generic applet information.
     *  @return A string giving minimal information about Ptolemy II.
     */
    public String getAppletInfo() {
        return "Ptolemy II applet.\n" +
            "Ptolemy II comes from UC Berkeley, Department of EECS.\n" +
            "See http://ptolemy.eecs.berkeley.edu/ptolemyII";
    }

    /** Describe the applet parameters. Derived classes should override
     *  this and append their own parameters.  The protected method
     *  _concatStringArrays() is provided to make this easy to do.
     *  @return An array describing the applet parameters.
     */
    public String[][] getParameterInfo() {
        String pinfo[][] = {
            {"background",    "#RRGGBB",    "color of the background"},
        };
        return pinfo;
    }

    /** Initialize the applet. This method is called by the browser
     *  or applet viewer to inform this applet that it has been
     *  loaded into the system. It is always called before
     *  the first time that the start() method is called.
     *  In this base class, this method creates a new workspace,
     *  and creates a manager and a top-level composite actor
     *  in the workspace, both of which are accessible
     *  to derived classes via protected members.
     *  It also processes a background color parameter.
     *  If the background color parameter has not been set, then the
     *  background color is set to white.
     */
    public void init() {
        // Process the background parameter.
        _background = Color.white;
        try {
            String colorSpecification = getParameter("background");
            if (colorSpecification != null) {
                _background = Color.decode(colorSpecification);
            }
        } catch (Exception ex) {
            report("Warning: background parameter failed: ", ex);
        }
        setBackground(_background);

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

    /** Report that the manager state has changed.  This is
     *  called by the manager.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newstate = manager.getState();
        if (newstate != _previousState) {
            showStatus(manager.getState().getDescription());
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
        showStatus("Exception occurred.");
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
        showStatus("Exception occurred.");
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

    /** Create run controls in a panel and return that panel.
     *  The argument controls how many buttons are
     *  created.  If its value is greater than zero, then a "Go" button
     *  created.  If its value is greater than one, then a "Stop" button
     *  is also created.  Derived classes may override this method to add
     *  additional controls, or to create a panel with a different layout.
     *  @param numberOfButtons How many buttons to create.
     */
    protected Panel _createRunControls(int numberOfButtons) {
        Panel panel = new Panel();
        if (numberOfButtons > 0) {
            _goButton = new Button("Go");
            panel.add(_goButton);
            _goButton.addActionListener(new GoButtonListener());
        }
        if (numberOfButtons > 1) {
            _stopButton = new Button("Stop");
            panel.add(_stopButton);
            _stopButton.addActionListener(new StopButtonListener());
        }
        return panel;
    }

    /** Get the background color as set by the "background" applet parameter.
     *  This is protected so that derived classes can find out what the
     *  background color is. Derived classes may wish to know the
     *  color so they can match it in some of their components.
     */
    protected Color _getBackground() {
        return _background;
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

    /*  The background color as set by the "background" applet parameter.
     *  This is protected so that derived classes can find out what the
     *  background color is.  The Applet base class does not provide
     *  a getBackground() method.  Derived classes may wish to know the
     *  color so they can match it in some of their components.
     */
    private Color _background;

    private Button _goButton;
    private Button _stopButton;
    private Manager.State _previousState;

    ////////////////////////////////////////////////////////////////////////
    ////                       inner classes                            ////

    private class GoButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            try {
                _go();
            } catch (Exception ex) {
                report(ex);
            }
        }
    }

    private class StopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            _stop();
        }
    }

}
