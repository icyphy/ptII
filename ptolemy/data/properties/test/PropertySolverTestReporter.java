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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
A extended base abstract class for a property solver.

@author Man-Kit Leung
@version $Id: PropertySolver.java 52224 2009-01-28 19:40:52Z mankit $
@since Ptolemy II 7.2
@Pt.ProposedRating Red (mankit)
@Pt.AcceptedRating Red (mankit)
*/
public class PropertySolverTestReporter {

    public static void main(String args[]) throws Exception {
        testPropertiesAndGenerateReports(args[0]);
    }

    public static void testPropertiesAndGenerateReports(String directoryPath) {
        try {

            Map[] stats;
            stats = _testPropertiesDirectory(directoryPath);
            _printGlobalStats(stats[0]);
            _printLocalStats(stats[1]);

        } catch (Exception ex) {
            // Force the error to show up on console.
            // We may want to direct this an error file.
            ex.printStackTrace(System.out);
        }
    }

    /**
     * @param stats
     * @param key
     * @param entryHeader
     * @param entryValue
     */
    private static void _addLocalStatsEntry(Map<Object, Map> stats, Object key,
            String entryHeader, Object entryValue) {
        _modelStatsHeaders.add(entryHeader);

        Map entry;
        if (stats.containsKey(key)) {
            entry = stats.get(key);
        } else {
            entry = new HashMap();
            stats.put(key, entry);
        }
        entry.put(entryHeader, entryValue);
    }

    private static void _composeOutputs(Map summary, Map intermediateOutputs) {
        for (Object field : intermediateOutputs.keySet()) {
            Object value = intermediateOutputs.get(field);
            if (value instanceof Number) {
                PropertySolver.incrementStats(summary, field, (Number) value);
            } else if (value instanceof Map) {
                summary.put(field, value);
            }
        }
    }

    private static Object _createKey(String filePath, PropertySolver solver,
            PropertySolver invokedSolver) {
        String key = filePath + _separator;

        if (solver != null) {
            key += solver.getName();
        }

        key += _separator;

        if (solver == null && invokedSolver == null) {
            // no solver is invoked.
        } else if (solver == invokedSolver || invokedSolver == null) {
            key += "directly invoked";
        } else {
            key += "dependent for (" + invokedSolver + ")";
        }
        return key;
    }

    /*
     * Get the exception log file for the given test model. The exception log
     * filename reflects whether the test has failed or not. For example, a test
     * model named "model.xml" may have a corresponding exception file named
     * "Failed_errors_model.log". If a file with the same name already existed,
     * a suffix number is attached. This occurs when logs generated by previous
     * runs of the test script are not removed, or there are multiple model
     * files with the same name with under different directories.
     * 
     * @param modelFile The given test model file.
     * 
     * @param failed Indicate whether the test had failed or not.
     * 
     * @return The exception log file that did not previously exist.
     */
    private static File _getExceptionLogFile(File modelFile, boolean failed) {
        int suffixId = 0;

        File errorFile;

        do {
            String exceptionLogFilename = _exceptionLogsDirectory + "/"
                    + (failed ? "Failed_errors_" : "Passed_errors_")
                    + modelFile.getName();

            // Replace the extension.
            exceptionLogFilename = exceptionLogFilename.substring(0,
                    exceptionLogFilename.length() - 4);

            // Duplicate filenames (under different directories) are handled by
            // appending a serial suffix. We assume the file content would
            // specify the path to the model file.
            if (suffixId == 0) {
                errorFile = new File(exceptionLogFilename + ".log");
            } else {
                errorFile = new File(exceptionLogFilename + suffixId + ".log");
            }

            suffixId++;
        } while (errorFile.exists());

        return errorFile;
    }


    private static boolean _isTestableDirectory(File file) {
        if (!file.isDirectory()) {
            return false;
        }

        List directoryPath = Arrays.asList(file.getAbsolutePath().split(
                File.separator.replace("\\", "\\\\")));

        return directoryPath.contains("test") || directoryPath.contains("demo");
    }
    

    private static boolean _isTestableFile(File file) {
        if (!file.getName().endsWith(".xml")) {
            return false;
        }
        return _isTestableDirectory(file.getParentFile());
    }

    private static void _printGlobalStats(Map stats) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                _statsFilename)));
        for (Object field : stats.keySet()) {
            writer.append(field + _separator + stats.get(field));
            writer.newLine();
        }
        writer.close();
    }


    private static void _printLocalStats(Map<Object, Map> stats)
            throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
                _statsFilename), true));

        // Give ordering to the header fields.
        List headers = new LinkedList(_modelStatsHeaders);
        headers.addAll(_solverStatsHeaders);

        // Print the header row.
        writer.append("Filename" + _separator);
        writer.append("Solver" + _separator);
        writer.append("Invocation");
        for (Object header : headers) {
            writer.append(_separator + header.toString());
        }
        writer.newLine();

        // Iterate using triplet keys {testFile, solver, isInvoked}.
        for (Object key : stats.keySet()) {
            Map entry = stats.get(key);
            writer.append(key.toString());

            for (Object header : headers) {
                writer.append(_separator);
                if (entry.containsKey(header)) {
                    writer.append(entry.get(header).toString());
                }
            }
            writer.newLine();
        }
        writer.close();
    }
    


    /*
     * 
     * @param directoryPath
     * 
     * @return
     * 
     * @throws IOException
     */
    private static Map[] _testPropertiesDirectory(String directoryPath)
            throws IOException {
        // Create the log directories.
        new File(_statsDirectory).mkdirs();
        new File(_exceptionLogsDirectory).mkdirs();

        // See MoMLSimpleApplication for similar code
        MoMLParser parser = new MoMLParser();
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses());

        // FIXME: get "stdout", "nondeep" options

        HashMap<Object, Map> localStats = new LinkedHashMap<Object, Map>();
        HashMap globalStats = new LinkedHashMap();

        Map[] summary = new Map[] { globalStats, localStats };

        File directory = new File(directoryPath);

        for (File file : directory.listFiles()) {

            if (file.isDirectory()) {
                Map[] directoryOutputs = _testPropertiesDirectory(file
                        .getAbsolutePath());

                _composeOutputs(summary[0], directoryOutputs[0]);
                _composeOutputs(summary[1], directoryOutputs[1]);

            } else if (_isTestableFile(file)) {
                System.out.println("***isTestable: " + file.getAbsolutePath());

                // Redirect System.err to a byteArrayStream.
                ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
                PrintStream errorStream = new PrintStream(byteArrayStream);
                System.setErr(errorStream);

                // FIXME: Implement option to redirect System.out to a file.
                // String outputFilename =
                // StringUtilities.getProperty("ptolemy.ptII.dir")
                // + "/BOSCH_Logfiles/" + file.getName();
                //                
                // outputFilename = outputFilename.replace(".xml",
                // "_output.txt");
                // File outputFile = new File(outputFilename);
                // outputFile.createNewFile();
                //                
                // FileOutputStream fos2 = new FileOutputStream(outputFilename);
                // PrintStream ps2 = new PrintStream(fos2);
                // System.setOut(ps2);

                boolean failed = false;

                String filePath = file.getAbsolutePath();

                // ==========================================================
                // Record timestamp and memory usage (per testcase).
                System.gc();
                long startTime = System.currentTimeMillis();

                // Format the current time.
                SimpleDateFormat formatter = new SimpleDateFormat(
                        "yyyy.MM.dd G 'at' hh:mm:ss a zzz");
                Date currentTime_1 = new Date(startTime);
                String startTimeString = formatter.format(currentTime_1);

                _addLocalStatsEntry(localStats,
                        _createKey(filePath, null, null), "Start time ",
                        startTimeString);
                _addLocalStatsEntry(localStats,
                        _createKey(filePath, null, null),
                        "Memory usage before", Runtime.getRuntime()
                                .totalMemory());

                // ==========================================================

                try {
                    parser.reset();
                    MoMLParser.purgeModelRecord(filePath);
                    CompositeEntity toplevel = PropertySolverTester._getModel(filePath, parser);

                    // Get all instances of PropertySolver contained in the
                    // model.
                    // FIXME: This only gets solvers in the top-level.
                    List<PropertySolver> solvers = toplevel
                            .attributeList(PropertySolver.class);

                    // There is no PropertySolver in the model.
                    if (solvers.size() == 0) {
                        System.err
                                .println("The model does not contain a solver.");
                    }

                    for (PropertySolver solver : solvers) {
                        if (solver.isTesting()) {
                            // FIXME:
                            // solver._prepareForTesting(options);
                            failed &= solver.invokeSolver();

                            localStats.put(
                                    _createKey(filePath, solver, solver),
                                    solver.getStats());
                            _solverStatsHeaders.addAll(solver.getStats().keySet());

                            for (String solverName : solver
                                    .getDependentSolvers()) {
                                PropertySolver dependentSolver = solver
                                        .findSolver(solverName);

                                localStats.put(_createKey(filePath,
                                        dependentSolver, solver),
                                        dependentSolver.getStats());
                                _solverStatsHeaders
                                        .addAll(dependentSolver.getStats().keySet());
                            }

                            solver.resetAll();

                        } else {
                            System.err
                                    .println("Warning: regression test not performed. "
                                            + solver.getDisplayName()
                                            + " in "
                                            + filePath
                                            + " is set to ["
                                            + solver.action.getExpression()
                                            + "] mode.");

                            failed = true;
                        }
                    }
                } catch (Exception ex) {
                    failed = true;
                    ex.printStackTrace(System.err);
                }

                // ==========================================================
                // Record timestamp and memory usage (per testcase).
                long finishTime = System.currentTimeMillis();
                System.gc();
                _addLocalStatsEntry(localStats,
                        _createKey(filePath, null, null), "Time used (ms)",
                        finishTime - startTime);
                _addLocalStatsEntry(localStats,
                        _createKey(filePath, null, null), "Memory usage after",
                        Runtime.getRuntime().totalMemory());
                _addLocalStatsEntry(localStats,
                        _createKey(filePath, null, null), "Failed?", failed);

                PropertySolver.incrementStats(globalStats, "#Total tests", 1);

                String errors = byteArrayStream.toString();

                if (!failed) {
                    // Should not succeed with errors.
                    assert errors.length() == 0;

                    PropertySolver.incrementStats(globalStats, "#Passed", 1);

                } else {
                    // Should not have a failure without error message.
                    assert errors.length() > 0;

                    PropertySolver.incrementStats(globalStats, "#Failed", 1);

                    File errorFile = _getExceptionLogFile(file, failed);
                    BufferedWriter writer = new BufferedWriter(new FileWriter(
                            errorFile));
                    writer.write(errors);
                    writer.close();
                }
                // ==========================================================
            }

            // statistics log (global, **this is the compare file)
            // comprehensive overview (#failure, #success, #knownFailure,
            // global)
            // - on top
            // stats field identifier (one headline)
            // testcase id (name, no timestamp, per model)
            // - each stats info (separated by [tab])

            // multiple invocations of (intermediate) solver are mapped to the
            // same id
            // - create separate entries for these invocations

        }
        return summary;
    }
    
    public static final String NONDEEP_TEST_OPTION = "-nondeep";

    protected static String _eol = StringUtilities
    .getProperty("line.separator");


    /* The directory path to store the test statistics reports. */
    private static String _statsDirectory = StringUtilities
            .getProperty("ptolemy.ptII.dir")
            + "/propertiesLogfiles";

    /* The directory path to store the exception log files. */
    private static String _exceptionLogsDirectory = _statsDirectory
            + "/exceptionLogs";

    private static LinkedHashSet _modelStatsHeaders = new LinkedHashSet();

    private static LinkedHashSet _solverStatsHeaders = new LinkedHashSet();

    /* The file path for the overview report file. */
    private static String _statsFilename = _statsDirectory
            + "/propertyTestReports.tsv";

    protected static String _separator = "\t";


}
