/* Base class to invoke a Functional Mock-up Unit (.fmu) file as
   either co-simulation or model exchange.

   Copyright (c) 2012-2014 The Regents of the University of California.
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
package org.ptolemy.fmi.driver;

import java.io.IOException;

import org.ptolemy.fmi.FMILibrary;
import org.ptolemy.fmi.FMIModelDescription;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;

///////////////////////////////////////////////////////////////////
//// FMUDriver

/** Base class to invoke a Functional Mock-up Unit (.fmu) file as
 *  either co-simulation or model exchange.
 *
 *  <p>Derived classes should implement the simulate(...) method and
 *  create a static main(String args) method that invokes
 *  _processArgs(args) and them simulate(...).</p>
 *
 *  @author Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public abstract class FMUDriver {

    /** Return a function by name.
     *  @param name The name of the function.  The value of the
     *  modelIdentifier is prepended to the value of this parameter to
     *  yield the function name.
     *  @return the function.
     *  @deprecated Call FMIModelDescription.getFmiFunction() instead.
     */
    @Deprecated
    public Function getFunction(String name) {
        // This is syntactic sugar.
        if (_enableLogging) {
            System.out.println("About to get the " + name + " function.");
        }
        return _nativeLibrary.getFunction(_modelIdentifier + name);
    }

    /** Invoke a function that returns an integer representing the
     *  FMIStatus return value.
     *  @param fmiModelDescription The FMIModelDescription used to
     *  look up the function by name.
     *  @param name The name of the function.
     *  @param arguments The arguments to be passed to the function.
     *  @param message The error message to be used if there is a problem.
     *  The message should end with ": " because the return value
     *  of the function will be printed after the error message.
     *  @exception UnsatisfiedLinkError If the function is not found using either format.
     *  @exception IOException If the native library cannot be found.
     */
    public void invoke(FMIModelDescription fmiModelDescription, String name,
            Object[] arguments, String message) throws UnsatisfiedLinkError,
            IOException {
        Function function = fmiModelDescription.getFmiFunction(name);
        invoke(function, arguments, message);
    }

    /** Invoke a function that returns an integer representing the
     *  FMIStatus return value.
     *  @param name The name of the function.
     *  @param arguments The arguments to be passed to the function.
     *  @param message The error message to be used if there is a problem.
     *  The message should end with ": " because the return value
     *  of the function will be printed after the error message.
     */
    public void invoke(String name, Object[] arguments, String message) {
        Function function = getFunction(name);
        invoke(function, arguments, message);
    }

    /** Invoke a function that returns an integer representing the
     *  FMIStatus return value.
     *  @param function The function to be invoked.
     *  @param arguments The arguments to be passed to the function.
     *  @param message The error message to be used if there is a problem.
     *  The message should end with ": " because the return value
     *  of the function will be printed after the error message.
     */
    public void invoke(Function function, Object[] arguments, String message) {
        // if (_enableLogging) {
        //    System.out.println("About to call " + function.getName());
        //}
        int fmiFlag = ((Integer) function.invoke(Integer.class, arguments))
                .intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new RuntimeException(message + ": "
                    + FMIModelDescription.fmiStatusDescription(fmiFlag));
        }
    }

    /** Perform co-simulation or model exchange using the named
     * Functional Mock-up Unit (FMU) file.
     *
     *  <p>Derived classes should implement this method.</p>
     *
     *  @param fmuFileName The pathname of the co-simulation .fmu file
     *  @param endTime The ending time in seconds.
     *  @param stepSize The step size in seconds.
     *  @param enableLogging True if logging is enabled.
     *  @param csvSeparator The character used for separating fields.
     *  Note that sometimes the decimal point in floats is converted to ','.
     *  @param outputFileName The output file.
     *  @exception Exception If there is a problem parsing the .fmu file or invoking
     *  the methods in the shared library.
     */
    public abstract void simulate(String fmuFileName, double endTime,
            double stepSize, boolean enableLogging, char csvSeparator,
            String outputFileName) throws Exception;

    ///////////////////////////////////////////////////////////////////
    ////                      protected fields                     ////

    /** Process command line arguments for co-simulation or model exchange of
     *  Functional Mock-up Unit (.fmu) files.
     *
     *  <p>The command line arguments have the following meaning:</p>
     *  <dl>
     *  <dt>file.fmu</dt>
     *  <dd>The co-simulation or model exchange Functional Mock-up
     *  Unit (FMU) file.  In FMI-1.0, co-simulation fmu files contain
     *  a modelDescription.xml file that has an &lt;Implementation&gt;
     *  element.  Model exchange fmu files do not have this
     *  element.</dd>
     *  <dt>endTime</dt>
     *  <dd>The endTime in seconds, defaults to 1.0.</dd>
     *  <dt>stepTime</dt>
     *  <dd>The time between steps in seconds, defaults to 0.1.</dd>
     *  <dt>enableLogging</dt>
     *  <dd>If "true", then enable logging.  The default is false.</dd>
     *  <dt>outputFile</dt>
     *  <dd>The name of the output file.  The default is results.csv</dd>
     *  </dl>
     *
     *  <p>The format of the arguments is based on the fmusim command from the fmusdk
     *  by QTronic Gmbh.</p>
     *
     *  @param args The arguments: file.fmu [endTime] [stepTime]
     *  [loggingOn] [csvSeparator] [outputFile]
     *  @exception Exception If there is a problem parsing the .fmu file or invoking
     *  the methods in the shared library.
     */
    protected void _processArgs(String[] args) throws Exception {
        if (args == null || args.length < 1) {
            throw new Exception("Incorrect arguments, missing a .fmu file.\n"
                    + "Usage: file.fmu [endTime] [stepTime] [loggingOn] [csvSeparator] [outputFile]"
                    + "For example: java -classpath $PTII/lib/jna-4.0.0-variadic.jar:${PTII} org.ptolemy.fmi.driver.FMUModelExchange $PTII/org/ptolemy/fmi/fmu/me/bouncingBallME1.fmu 4.0 0.01 true c result.csv"
        }
        _fmuFileName = args[0];
        if (args.length >= 2) {
            _endTime = Double.valueOf(args[1]);
        }
        if (args.length >= 3) {
            _stepSize = Double.valueOf(args[2]);
        }
        if (args.length >= 4) {
            _enableLogging = Boolean.valueOf(args[3]);
        }
        if (args.length >= 5) {
            if (args[4].equals("c")) {
                _csvSeparator = ',';
            } else if (args[4].equals("s")) {
                _csvSeparator = ';';
            } else {
                _csvSeparator = args[4].charAt(0);
            }
        }
        if (args.length >= 6) {
            _outputFileName = args[5];
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Set the _enableLogging field.
     *  @param enableLogging the value of the enable logging field.
     */
    protected void _setEnableLogging(boolean enableLogging) {
        // This method exists so as to avoid a warning from FindBugs.
        _enableLogging = enableLogging;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  package protected fields                 ////

    // FindBugs wants these package protected.

    /** The comma separated value separator.  The initial value is
     *  ','.  If the separator is ',', columns are separated by ','
     *     *  and '.' is used for floating-point numbers.  Otherwise, the
     *  given separator (e.g. ';' or '\t') is to separate columns, and
     *  ',' is used as decimal dot in floating-point numbers.
     */
    char _csvSeparator = ',';

    /** True if logging is enabled.
     *  The initial value is false.
     */
    boolean _enableLogging = false;

    /** The end time, in seconds.
     *  The initial default is 1.0.
     */
    double _endTime = 1.0;

    /** The version number of the FMI standard. */
    double _fmiVersion = 0.0;

    /** The name of the .fmu file.
     *  The initial default is the empty string.
     */
    String _fmuFileName = "";

    /** The modelIdentifier from modelDescription.xml. */
    String _modelIdentifier;

    /** The NativeLibrary that contains the functions. */
    NativeLibrary _nativeLibrary;

    /** The output file name.
     *  The initial value is "results.csv".
     */
    String _outputFileName = "results.csv";

    /** The step size, in seconds.
     *  The initial default is 0.1 seconds.
     */
    double _stepSize = 0.1;
}
