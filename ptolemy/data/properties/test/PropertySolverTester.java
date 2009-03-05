/*  A class that contains the test functions for the PropertySolver.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.data.properties.test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import ptolemy.data.properties.PropertySolver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
////TestPropertySolver

/**
 * A extended base abstract class for a property solver.
 * 
 * @author Man-Kit Leung
 * @version $Id$
 * @since Ptolemy II 7.2
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class PropertySolverTester {

    public static void main(String args[]) throws Exception {
        testProperties(args);
    }
    
    /*
     * Parse a command-line argument. This method recognized -help and -version
     * command-line arguments, and prints usage or version information. No other
     * command-line arguments are recognized.
     * 
     * @param arg The command-line argument to be parsed.
     * 
     * @return True if the argument is understood, false otherwise.
     * 
     * @exception Exception If something goes wrong.
     */
    public static boolean parseArg(String arg) throws Exception {
        if (arg.equals("-help")) {
            // TODO: _usage()??
            // System.out.println(_usage());

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
        } else if (arg.equals("-version")) {
            System.out
                    .println("Version "
                            + VersionAttribute.CURRENT_VERSION.getExpression()
                            + ", Build $Id$");

            StringUtilities.exit(0);
            // If we are testing, and ptolemy.ptII.exitAfterWrapup is set
            // then StringUtilities.exit(0) might not actually exit.
            return true;
            // } else if (arg.equals("-verbose")) {
            // _verboseMode = true;
        } else if (arg.equals(PropertySolver.NONDEEP_TEST_OPTION)) {
            _options.put(PropertySolver.NONDEEP_TEST_OPTION, true);
            return true;
        }
        // Argument not recognized.
        return false;
    }

    /*
     * Resolve properties for a model.
     * 
     * @param args An array of Strings, each element names a MoML file
     * containing a model.
     * 
     * @return The return value of the last subprocess that was run to compile
     * or run the model. Return -1 if called with no arguments.
     * 
     * @exception Exception If any error occurs.
     */
    public static int testProperties(String[] args) throws Exception {


        if (args.length == 0) {
            System.err.println("Usage: java -classpath $PTII "
                    + "ptolemy.data.properties.PropertySolver model.xml "
                    + "[model.xml . . .]" + _eol
                    + "  The arguments name MoML files containing models");
            return -1;
        }

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        for (int i = 0; i < args.length; i++) {

            if (parseArg(args[i])) {
                continue;
            }

            if (args[i].trim().startsWith("-")) {
                if (i >= (args.length - 1)) {
                    throw new IllegalActionException("Cannot set "
                            + "parameter " + args[i] + " when no value is "
                            + "given.");
                }

                // Save in case this is a parameter name and value.
                // _parameterNames.add(args[i].substring(1));
                // _parameterValues.add(args[i + 1]);
                // i++;
                continue;
            }

            CompositeEntity toplevel = null;
            boolean isDone = false;
            int numberOfSolverTested = 0;

            while (!isDone) {
                long memStart, memEnd;
                PropertySolver solver = null;

                System.gc();
                memStart = Runtime.getRuntime().totalMemory();
                parser.reset();
                MoMLParser.purgeModelRecord(args[i]);
                toplevel = _getModel(args[i], parser);

                // Get all instances of PropertySolver contained in the model.
                // FIXME: This only gets solvers in the top-level.
                List solvers = toplevel.attributeList(PropertySolver.class);

                if (solvers.size() == 0) {
                    // There is no PropertySolver in the model.
                    System.err.println("The model does not contain a solver.");

                } else if (numberOfSolverTested < solvers.size()) {
                    // Get the last PropertySolver in the list, maybe
                    // it was added last?
                    solver = (PropertySolver) solvers
                            .get(numberOfSolverTested++);

                    if (solver.isTesting()) {
                        solver.setOptions(_options);
                        solver.invokeSolver();
                        solver.resetAll();

                    } else {
                        System.err
                                .println("Warning: regression test not performed. "
                                        + solver.getDisplayName()
                                        + " in "
                                        + args[i]
                                        + " is set to ["
                                        + solver.action.getExpression()
                                        + "] mode.");
                    }
                } else {
                    isDone = true;
                }

                // Destroy the top level so that we avoid
                // problems with running the model after generating code
                if (toplevel != null) {
                    toplevel.setContainer(null);
                    toplevel = null;
                }

                // ==========================================================
                System.gc();
                memEnd = Runtime.getRuntime().totalMemory();
                if ((memEnd - memStart) != 0) {
                    // FIXME: throw some sort of memory leak exception?
                    // System.out.println("Memory Usage Before PS: " +
                    // memStart);
                    // System.out.println("Memory Usage After PS: " + memEnd);
                    // System.out.println("Memory diff = : " + (memEnd -
                    // memStart));
                    // ==========================================================

                }
            }

        }
        return 0;
    }

    /*
     * @param path
     * 
     * @param parser
     * 
     * @return
     * 
     * @throws IllegalActionException
     */
    protected static CompositeEntity _getModel(String path, MoMLParser parser)
            throws IllegalActionException {
        // Note: the code below uses explicit try catch blocks
        // so we can provide very clear error messages about what
        // failed to the end user. The alternative is to wrap the
        // entire body in one try/catch block and say
        // "Code generation failed for foo", which is not clear.
        URL modelURL;

        try {
            modelURL = new File(path).toURI().toURL();
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex, "Could not open \""
                    + path + "\"");
        }

        CompositeEntity toplevel = null;

        try {
            toplevel = (CompositeEntity) parser.parse(null, modelURL);
        } catch (Exception ex) {
            throw new IllegalActionException(null, ex, "Failed to parse \""
                    + path + "\"");
        }
        return toplevel;
    }

    protected static final String _eol = StringUtilities
            .getProperty("line.separator");

    private static HashMap _options = new HashMap();
}
