/* A Ptolemy application specified as an instance of CompositeActor.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Java imports
import java.lang.System;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// CompositeActorApplication
/**
This application creates one or more Ptolemy II models given a classname
on the command line, and then executes those models, each in its own
thread.  Each specified class should be derived from CompositeActor.
Each will be created in its own workspace.
<p>
The command-line arguments can also set parameter values for any
parameter in the models, with the name given relative to the top-level
entity.  For example, to specify the iteration count in an SDF model,
you can invoke this on the command line as follows:
<pre>
    ptolemy -director.iterations 2 model.xml
</pre>
This assumes that the model given in file "model.xml" has a director
named "director" with a parameter named "iterations".
If more than one model is given on the command line, then the
parameter values will be set for all models that have such
a parameter.

@author Edward A. Lee and Brian K. Vogel
@version $Id$
*/
public class CompositeActorApplication implements ExecutionListener {

    /** Parse the command-line arguments, creating models as specified.
     *  Then execute each model that contains a manager.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public CompositeActorApplication(String args[]) throws Exception {
        if (args != null) {
            _parseArgs(args);
            Iterator models = _models.iterator();
            while(models.hasNext()) {
                CompositeActor model = (CompositeActor)models.next();
                startRun(model);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add a model to the application.  If the model has no manager,
     *  then create one.  Then register this object as an execution listener.
     *  @param model The model to add.
     */
    public void add(CompositeActor model) {
        _models.add(model);
        Manager manager = model.getManager();
        if (manager == null) {
            try {
                model.setManager(new Manager(model.workspace(), "manager"));
            } catch (IllegalActionException ex) {
                // Ignore... can't attach a manager.
            }
            manager = model.getManager();
        }
        if (manager != null) {
            manager.addExecutionListener(this);
        }
    }

    /** Report that an execution error has occurred.  This method
     *  is called by the specified manager.
     *  @param manager The manager calling this method.
     *  @param ex The exception being reported.
     */
    public void executionError(Manager manager, Exception ex) {
        report(ex);
    }

    /** Report that execution of the model has finished by printing a
     *  message to stdout. If the number of executing models drops to
     *  zero, then notify threads that might be waiting for this event.
     *  This is method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    public synchronized void executionFinished(Manager manager) {
        report("Execution finished.");
        _runningCount--;
        if (_runningCount == 0) {
            notifyAll();
        }
    }

    /** Create a new application with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            CompositeActorApplication plot =
                new CompositeActorApplication(args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    /** Report that a manager state has changed.
     *  This is method is called by the specified manager.
     *  @param manager The manager calling this method.
     */
    public void managerStateChanged(Manager manager) {
        Manager.State newstate = manager.getState();
        if (newstate != _previousState) {
            report(manager.getState().getDescription());
            _previousState = newstate;
        }
    }

    /** Return an unmodifiable list of the models in this application.
     *  @return A list of CompositeActor objects.
     */
    public List models() {
        return Collections.unmodifiableList(_models);
    }

    /** Remove a model from this application.  If the model has a manager,
     *  then deregister as an execution listener.  If there are no more
     *  models, then exit the application.  If the model is not associated
     *  with this application, then do nothing.
     *  @param model The model to remove.
     */
    public void remove(CompositeActor model) {
        _models.remove(model);
        Manager manager = model.getManager();
        if (manager != null) {
            manager.removeExecutionListener(this);
        }
        if (_models.size() == 0) {
            System.exit(0);
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
        String msg = "Exception thrown.\n" + message + "\n"
            + ex.toString();
            System.err.println(msg);
            ex.printStackTrace();
    }

    /** If the specified model has a manager and is not already running,
     *  then execute the model in a new thread.  Otherwise, do nothing.
     *  @param model The model to execute.
     */
    public synchronized void startRun(CompositeActor model) {
        // This method is synchronized so that it can atomically modify
        // the count of executing processes.
        Manager manager = model.getManager();
        if (manager != null) {
            try {
                manager.startRun();
                _runningCount++;
            } catch (IllegalActionException ex) {
                // Model is already running.  Ignore.
            }
        } else {
            report("Model " + model.getFullName() + " is not executable.");
        }
    }

    /** If the specified model has a manager and is executing, then
     *  stop execution by calling the finish() method of the manager.
     *  If there is no manager, do nothing.
     *  @param model The model to stop.
     */
    public void stopRun(CompositeActor model) {
        Manager manager = model.getManager();
        if(manager != null) {
            manager.finish();
        }
    }

    /** Wait for all executing runs to finish, then return.
     */
    public synchronized void waitForFinish() {
        while (_runningCount > 0) {
            try {
                wait();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @throws Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-class")) {
            _expectingClass = true;
        } else if (arg.equals("-help")) {
            System.out.println(_usage());
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version 1.0, Build $Id$");
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else {
            if (_expectingClass) {
                _expectingClass = false;

                Class newClass = Class.forName(arg);

                // Instantiate the specified class in a new workspace.
                Workspace workspace = new Workspace();

                // Get the constructor that takes a Workspace argument.
                Class[] argTypes = new Class[1];
                argTypes[0] = workspace.getClass();
                Constructor constructor = newClass.getConstructor(argTypes);

                Object args[] = new Object[1];
                args[0] = workspace;
                CompositeActor newModel
                    = (CompositeActor)constructor.newInstance(args);

                add(newModel);
            } else {
                // Argument not recognized.
                return false;
            }
        }
        return true;
    }

    /** Parse the command-line arguments.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(String args[]) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (_parseArg(arg) == false) {
                if (arg.startsWith("-") && i < args.length - 1) {
                    // Save in case this is a parameter name and value.
                    _parameterNames.add(arg.substring(1));
                    _parameterValues.add(args[i + 1]);
                    i++;
                } else {
                    // Unrecognized option.
                    throw new IllegalActionException("Unrecognized option: "
                            + arg);
                }
            }
        }
        if (_expectingClass) {
            throw new IllegalActionException("Missing classname.");
        }
        // Check saved options to see whether any is a parameter.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            String name = (String)names.next();
            String value = (String)values.next();

            boolean match = false;
            Iterator models = _models.iterator();
            while(models.hasNext()) {
                CompositeActor model = (CompositeActor)models.next();
                Attribute attribute = model.getAttribute(name);
                if (attribute instanceof Variable) {
                    match = true;
                    ((Variable)attribute).setExpression(value);
                    // Force evaluation so that listeners are notified.
                    ((Variable)attribute).getToken();
                }
                Director director = model.getDirector();
                if (director != null) {
                    attribute = director.getAttribute(name);
                    if (attribute instanceof Variable) {
                        match = true;
                        ((Variable)attribute).setExpression(value);
                        // Force evaluation so that listeners are notified.
                        ((Variable)attribute).getToken();
                    }
                }
            }
            if (!match) {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: "
                        + "-" + name);
            }
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: " + _commandTemplate + "\n\n"
            + "Options that take values:\n";

        int i;
        for(i = 0; i < _commandOptions.length; i++) {
            result += " " + _commandOptions[i][0] +
                " " + _commandOptions[i][1] + "\n";
        }
        result += "\nBoolean flags:\n";
        for(i = 0; i < _commandFlags.length; i++) {
            result += " " + _commandFlags[i];
        }
        return result;
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected variables                    ////

    /** The command-line options that are either present or not. */
    protected String _commandFlags[] = {
        "-help",
        "-test",
        "-version",
    };

    /** The command-line options that take arguments. */
    protected String _commandOptions[][] = {
        {"-class",  "<classname>"},
        {"-<parameter name>", "<parameter value>"},
    };

    /** The form of the command line. */
    protected String _commandTemplate = "ptolemy [ options ]";

    /** The list of models controlled by this application. */
    protected List _models = new LinkedList();

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // Flag indicating that the previous argument was -class.
    private boolean _expectingClass = false;

    // List of parameter names seen on the command line.
    private List _parameterNames = new LinkedList();

    // List of parameter values seen on the command line.
    private List _parameterValues = new LinkedList();

    // The previous state of the manager, to avoid reporting it if it hasn't
    // changed.
    private Manager.State _previousState;

    // The count of currently executing runs.
    private int _runningCount = 0;
}
