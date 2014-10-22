/* A Ptolemy application that instantiates class names given on the command
 line.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.injection.PortablePlaceable;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;
import ptolemy.moml.MoMLParser;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// CompositeActorSimpleApplication

/**
 This application creates one or more Ptolemy II models given a
 classname on the command line, and then executes those models, each in
 its own thread.  Each specified class should be derived from
 CompositeActor, and should have a constructor that takes a single
 argument, an instance of Workspace.  If the model does not contain
 a manager, then one will be created for it.
 <p>
 The model is not displayed,  models that have actors that extend
 Placeable should instead use
 {@link ptolemy.actor.gui.CompositeActorApplication}.
 </p>
 <p>
 The command-line arguments can also set parameter values for any
 parameter in the models, with the name given relative to the top-level
 entity.  For example, to specify the iteration count in an SDF model,
 you can invoke this on the command line as follows:
 </p>
 <pre>
 java -classpath $PTII ptolemy.actor.gui.CompositeActorSimpleApplication \
 -director.iterations 1000 \
 -class ptolemy.actor.gui.test.TestModel
 </pre>
 <p>
 This assumes that the model given by the specified class name has a director
 named "director" with a parameter named "iterations".  If more than
 one model is given on the command line, then the parameter values will
 be set for all models that have such a parameter.
 </p>

 @see ptolemy.actor.gui.CompositeActorApplication
 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (vogel)
 */
public class CompositeActorSimpleApplication {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new application with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String[] args) {
        CompositeActorSimpleApplication application = new CompositeActorSimpleApplication();
        _run(application, args);
    }

    /** Return the list of models.
     *  @return The list of models passed in as arguments.
     */
    public List<CompositeActor> models() {
        // Used primarily for testing.
        return _models;
    }

    /** Parse the command-line arguments, creating models as specified.
     *  @param args The command-line arguments.
     *  @exception Exception If something goes wrong.
     */
    public void processArgs(String[] args) throws Exception {
        if (args != null) {
            _parseArgs(args);

            // start the models.
            Iterator models = _models.iterator();

            while (models.hasNext()) {
                startRun((CompositeActor) models.next());
            }
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
        System.err.println("Exception thrown:\n" + message + "\n"
                + KernelException.stackTraceToString(ex));
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
     *  @exception IllegalActionException If the model contains Placeables.
     *  or does not have a manager.
     *  @return Always returns null.
     *  @see ptolemy.actor.Manager#startRun()
     */
    public synchronized Object startRun(CompositeActor model)
            throws IllegalActionException {
        // This method is synchronized so that it can atomically modify
        // the count of executing processes.
        // NOTE: If you modify this method, please be sure that it
        // will work for non-graphical models in the nightly test suite.
        // Iterate through the model, looking for something that is Placeable.
        Iterator atomicEntities = model.allAtomicEntityList().iterator();

        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();

            if (object instanceof Placeable
                    || object instanceof PortablePlaceable) {
                throw new IllegalActionException(
                        "CompositeActorSimpleApplication does not support "
                                + "actors that are instances of placeable, "
                                + "object was: " + object);
            }
        }

        Manager manager = model.getManager();

        if (manager != null) {
            try {
                manager.startRun();
            } catch (IllegalActionException ex) {
                // Model is already running.  Ignore.
            }
        } else {
            report("Model " + model.getFullName() + " cannot be executed "
                    + "because it does not have a manager.");
        }
        return null;
    }

    /** If the specified model has a manager and is executing, then
     *  stop execution by calling the stop() method of the manager.
     *  If there is no manager, do nothing.
     *  @param model The model to stop.
     */
    public void stopRun(CompositeActor model) {
        Manager manager = model.getManager();

        if (manager != null) {
            manager.stop();
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
    ////                         protected methods                 ////

    /** Parse a command-line argument.  The recognized arguments, which
     *  result in this method returning true, are summarized below:
     *  <ul>
     *  <li>If the argument is "-class", then attempt to interpret
     *  the next argument as the fully qualified classname of a class
     *  to instantiate as a ptolemy model.  The model will be created,
     *  added to the directory of models, and then executed.
     *  <li>If the argument is "-help", then print a help message.
     *  <li>If the argument is "-test", then set a flag that will
     *  abort execution of any created models after two seconds.
     *  <li>If the argument is "-version", then print a short version message.
     *  <li>If the argument is "", then ignore it.
     *  </ul>
     *  Otherwise, the argument is ignored and false is returned.
     *
     *  @param arg The argument to be parse.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-class")) {
            _expectingClass = true;
        } else if (arg.equals("-help")) {
            System.out.println(_usage());

            // Don't call System.exit(0) here, it will break the test suites
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-version")) {
            System.out
                    .println("Version "
                            + VersionAttribute.CURRENT_VERSION
                            + ", Build $Id$");

            // quit the program if the user asked for the version
            // Don't call System.exit(0) here, it will break the test suites
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else {
            if (_expectingClass) {
                _expectingClass = false;

                MoMLParser parser = new MoMLParser();
                String string = "<entity name=\"toplevel\" class=\"" + arg
                        + "\"/>";
                CompositeActor model = (CompositeActor) parser.parse(string);

                // Temporary hack because cloning doesn't properly clone
                // type constraints.
                CompositeActor modelClass = (CompositeActor) parser
                        .searchForClass(arg, model.getSource());

                if (modelClass != null) {
                    model = modelClass;
                }

                _models.add(model);

                // Create a manager.
                Manager manager = model.getManager();

                if (manager == null) {
                    model.setManager(new Manager(model.workspace(), "manager"));
                    //manager = model.getManager();
                }
            } else {
                // Argument not recognized.
                return false;
            }
        }

        return true;
    }

    /** Parse the command-line arguments.
     *  @param args The arguments to be parsed.
     *  @exception Exception If an argument is not understood or triggers
     *   an error.
     */
    protected void _parseArgs(String[] args) throws Exception {
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
            String name = (String) names.next();
            String value = (String) values.next();

            boolean match = false;
            Iterator models = _models.iterator();

            while (models.hasNext()) {
                CompositeActor model = (CompositeActor) models.next();
                Attribute attribute = model.getAttribute(name);

                if (attribute instanceof Variable) {
                    match = true;
                    ((Variable) attribute).setExpression(value);

                    // Force evaluation so that listeners are notified.
                    ((Variable) attribute).getToken();
                }

                Director director = model.getDirector();

                if (director != null) {
                    attribute = director.getAttribute(name);

                    if (attribute instanceof Variable) {
                        match = true;
                        ((Variable) attribute).setExpression(value);

                        // Force evaluation so that listeners are notified.
                        ((Variable) attribute).getToken();
                    }
                }
            }

            if (!match) {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: " + "-"
                        + name);
            }
        }
    }

    /** Run the application.
     *  @param application The application.
     *  @param args The arguments to be passed to the application.
     */
    protected static void _run(CompositeActorSimpleApplication application,
            String[] args) {
        try {
            application.processArgs(args);
            application.waitForFinish();
        } catch (Exception ex) {
            System.err.println(KernelException.stackTraceToString(ex));
            StringUtilities.exit(0);
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }

            StringUtilities.exit(0);
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        StringBuffer result = new StringBuffer("Usage: " + _commandTemplate
                + "\n\n" + "Options that take values:\n");

        int i;

        for (i = 0; i < _commandOptions.length; i++) {
            result.append(" " + _commandOptions[i][0] + " "
                    + _commandOptions[i][1] + "\n");
        }

        result.append("\nBoolean flags:\n");

        for (i = 0; i < _commandFlags.length; i++) {
            result.append(" " + _commandFlags[i]);
        }

        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The command-line options that are either present or not. */
    protected String[] _commandFlags = { "-help", "-test", "-version", };

    /** The command-line options that take arguments. */
    protected String[][] _commandOptions = { { "-class", "<classname>" },
            { "-<parameter name>", "<parameter value>" }, };

    /** The form of the command line. */
    protected String _commandTemplate = "ptolemy [ options ]";

    /** The list of all the models. */
    protected List<CompositeActor> _models = new LinkedList<CompositeActor>();

    /** The count of currently open windows. */
    protected int _openCount = 0;

    /** If true, then auto exit after a few seconds. */
    protected static boolean _test = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Flag indicating that the previous argument was -class.
    private boolean _expectingClass = false;

    // List of parameter names seen on the command line.
    private List _parameterNames = new LinkedList();

    // List of parameter values seen on the command line.
    private List _parameterValues = new LinkedList();
}
