/* An application that executes non-graphical
   models specified on the command line and prints out statistics.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.StreamErrorHandler;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// MoMLSimpleStatisticalApplication
/** A simple application that reads in a .xml file as a command
line argument, runs it and prints out time and memory statistics

<p>MoMLApplication sets the look and feel, which starts up Swing,
so we can't use MoMLApplication for non-graphical simulations.

<p>We implement the ChangeListener interface so that this
class will get exceptions thrown by failed change requests.

<p>Below is an example use of this class:
<pre>
java -classpath $PTII ptolemy.actor.gui.MoMLSimpleStatisticalApplication -iterations 2 ../../../ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml
</pre>

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class MoMLSimpleStatisticalApplication extends MoMLSimpleApplication {
    /** Parse the xml file and run it.
     */
    public MoMLSimpleStatisticalApplication(String args[]) throws Exception {
        _parser = new MoMLParser();

        MoMLParser.setErrorHandler(new StreamErrorHandler());

        // First, we gc and then print the memory stats
        // BTW to get more info about gc,
        // use java -verbose:gc . . .
        System.gc();
        Thread.sleep(1000);

        long startTime = System.currentTimeMillis();

        _parseArgs(args);

        // We use parse(URL, URL) here instead of parseFile(String)
        // because parseFile() works best on relative pathnames and
        // has problems finding resources like files specified in
        // parameters if the xml file was specified as an absolute path.
        //CompositeActor toplevel = (CompositeActor) parser.parse(null,
        //        new File(xmlFilename).toURL());

        Manager manager = new Manager(_toplevel.workspace(),
                "MoMLSimpleStatisticalApplication");
        _toplevel.setManager(manager);
        _toplevel.addChangeListener(this);

        Runtime runtime = Runtime.getRuntime();

        // Get the memory stats before we get the model name
        // just to be sure that getting the model name does
        // not skew are data too much
        long totalMemory1 = runtime.totalMemory()/1024;
        long freeMemory1 = runtime.freeMemory()/1024;

        String modelName = _toplevel.getName();

        System.out.println(modelName +
                ": Stats before execution:    "
                + Manager.timeAndMemory(startTime,
                        totalMemory1, freeMemory1));

        // Second, we run and print memory stats.
        manager.execute();

        long totalMemory2 = runtime.totalMemory()/1024;
        long freeMemory2 = runtime.freeMemory()/1024;
        String standardStats = Manager.timeAndMemory(startTime,
                totalMemory2, freeMemory2);

        System.out.println(modelName +
                ": Execution stats:           "
                + standardStats);

        // Third, we gc and print memory stats.
        System.gc();
        Thread.sleep(1000);

        long totalMemory3 = runtime.totalMemory()/1024;
        long freeMemory3 = runtime.freeMemory()/1024;
        System.out.println(modelName +
                ": After Garbage Collection:  "
                + Manager.timeAndMemory(startTime,
                        totalMemory3, freeMemory3));
        System.out.println(modelName +
                ": construction size:         "
                + totalMemory1 + "K - " + freeMemory1 + "K = "
                + (totalMemory1 - freeMemory1) + "K");
        System.out.println(modelName +
                ": model alloc. while exec. : "
                + freeMemory1 + "K - " + freeMemory3 + "K = "
                + (freeMemory1 - freeMemory3) + "K");
        System.out.println(modelName +
                ": model alloc. runtime data: "
                + freeMemory3 + "K - " + freeMemory2 + "K = "
                + (freeMemory3 - freeMemory2) + "K");

        // Print out the standard stats at the end
        // so as not to break too many scripts
        System.out.println(standardStats
                + " Stat: " + (totalMemory1 - freeMemory1)
                + "K StatRT: " + (freeMemory1 - freeMemory3)
                + "K DynRT: " + (freeMemory3 - freeMemory2)
                + "K");
    }


    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @exception Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (arg.equals("-class")) {
            _expectingClass = true;
        } else if (arg.equals("-help")) {
            System.out.println(_usage());
            // NOTE: This means the test suites cannot test -help
            System.exit(0);
        } else if (arg.equals("-test")) {
            _test = true;
        } else if (arg.equals("-version")) {
            System.out.println("Version "
                    + VersionAttribute.CURRENT_VERSION
                    + ", Build $Id$");
            // NOTE: This means the test suites cannot test -version
            System.exit(0);
        } else if (arg.equals("")) {
            // Ignore blank argument.
        } else {
            if (_expectingClass) {
                _expectingClass = false;

                // Create the class.
                Class newClass = Class.forName(arg);

                // Instantiate the specified class in a new workspace.
                Workspace workspace = new Workspace();

                // Get the constructor that takes a Workspace argument.
                Class[] argTypes = new Class[1];
                argTypes[0] = workspace.getClass();
                Constructor constructor = newClass.getConstructor(argTypes);

                Object args[] = new Object[1];
                args[0] = workspace;
                constructor.newInstance(args);

            } else {
                if (!arg.startsWith("-")) {
                    // Assume the argument is a file name or URL.
                    // Attempt to read it.
                    URL inURL = MoMLApplication.specToURL(arg);

                    // Strangely, the XmlParser does not want as base the
                    // directory containing the file, but rather the
                    // file itself.
                    URL base = inURL;

                    // Assume this is a MoML file, and open it.
                    _parser.reset();
                    _toplevel = (CompositeActor)_parser.parse(base, inURL);
                } else {
                    // Argument not recognized.
                    return false;
                }
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
                if (arg.trim().startsWith("-")) {
                    if (i >= args.length - 1) {
                        throw new IllegalActionException("Cannot set " +
                                "parameter " + arg + " when no value is " +
                                "given.");
                    }
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
        // Check saved options to see whether any is setting an attribute.
        Iterator names = _parameterNames.iterator();
        Iterator values = _parameterValues.iterator();
        while (names.hasNext() && values.hasNext()) {
            String name = (String)names.next();
            String value = (String)values.next();

            boolean match = false;

            NamedObj model = _toplevel;
            System.out.println("model = " + model.getFullName());
            Attribute attribute = model.getAttribute(name);
            if (attribute instanceof Settable) {
                match = true;
                ((Settable)attribute).setExpression(value);
                if (attribute instanceof Variable) {
                    // Force evaluation so that listeners are notified.
                    ((Variable)attribute).getToken();
                }
            }
            if (model instanceof CompositeActor) {
                Director director
                    = ((CompositeActor)model).getDirector();
                if (director != null) {
                    attribute = director.getAttribute(name);
                    if (attribute instanceof Settable) {
                        match = true;
                        ((Settable)attribute).setExpression(value);
                        if (attribute instanceof Variable) {
                            // Force evaluation so that listeners
                            // are notified.
                            ((Variable)attribute).getToken();
                        }
                    }
                }
            }
            if (!match) {
                // Unrecognized option.
                throw new IllegalActionException("Unrecognized option: " +
                        "No parameter exists with name " + name);
            }
        }
    }


    /** Create an instance of a single model and run it
     *  @param args The command-line arguments naming the .xml file to run
     */
    public static void main(String args[]) {
        try {
            new MoMLSimpleStatisticalApplication(args);
        } catch (Exception ex) {
            System.err.println("Command failed: " + ex);
            ex.printStackTrace();
        }
    }

    /** Return a string summarizing the command-line arguments.
     *  @return A usage string.
     */
    protected String _usage() {
        String result = "Usage: " + _commandTemplate + "\n\n"
            + "Options that take values:\n";

        int i;
        for (i = 0; i < _commandOptions.length; i++) {
            result += " " + _commandOptions[i][0] +
                " " + _commandOptions[i][1] + "\n";
        }
        result += "\nBoolean flags:\n";
        for (i = 0; i < _commandFlags.length; i++) {
            result += " " + _commandFlags[i];
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

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
    protected String _commandTemplate = "java -classpath $PTII ptolemy.actor.gui.MoMLSimpleStatisticalApplication [ options ] [file ...]";

    /** The parser used to construct the configuration. */
    protected MoMLParser _parser;

    /** The toplevel, which is usually the model */
    protected CompositeActor _toplevel;

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
