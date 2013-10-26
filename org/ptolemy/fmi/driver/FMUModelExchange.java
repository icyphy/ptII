/* Read a Functional Mock-up Unit .fmu file and invoke it as a model exchange.

   Copyright (c) 2012 The Regents of the University of California.
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

import java.io.File;
import java.io.PrintStream;

import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMIEventInfo;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;

///////////////////////////////////////////////////////////////////
//// FMUModelExchange

/** Read a Functional Mock-up Unit .fmu file and invoke it as a model exchange.
 *
 * <p>This file is based on fmusdk/src/model_exchange/fmusim_me/main.c
 * by Jakob Mauss, which has the following license:</p>
 *
 * <p>FMU SDK license</p>
 *
 * <p>Copyright (c) 2008-2011, QTronic GmbH. All rights reserved.
 * The FmuSdk is licensed by the copyright holder under the BSD License
 * (http://www.opensource.org/licenses/bsd-license.html):
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <br>- Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * <br>- Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.</p>
 *
 * <p>THIS SOFTWARE IS PROVIDED BY QTRONIC GMBH "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL QTRONIC GMBH BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.</p>
 *
 * @author Christopher Brooks, based on fmusim_me/main.c by Jakob Mauss
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUModelExchange extends FMUDriver {

    /** Perform model exchange using the named Functional Mock-up Unit (FMU) file.
     *
     * <p>Usage:</p>
     * <pre>
     * java -classpath ../../../../lib/jna.jar:../../../.. org.ptolemy.fmi.driver.FMUModelExchange \
     * file.fmu [endTime] [stepTime] [loggingOn] [csvSeparator] [outputFile]
     * </pre>
     *
     * <p>For example, under Mac OS X or Linux:
     * <pre>
     * java -classpath $PTII/lib/jna.jar:${PTII} org.ptolemy.fmi.driver.FMUModelExchange \
     * $PTII/org/ptolemy/fmi/fmu/me/bouncingBall.fmu 1.0 0.1 true c foo.csv
     * </pre>
     *
     *  <p>The command line arguments have the following meaning:</p>
     *  <dl>
     *  <dt>file.fmu</dt>
     *  <dd>The model exchange Functional Mock-up
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
     *  <dt>separator</dt>
     *  <dd>The comma separated value separator, the default value is
     *  ',', If the separator is ',', columns are separated by ',' and
     *  '.' is used for floating-point numbers.  Otherwise, the given
     *  separator (e.g. ';' or '\t') is to separate columns, and ','
     *  is used as decimal dot in floating-point numbers.
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
    public static void main(String[] args) throws Exception {
        FMUDriver._processArgs(args);
        new FMUModelExchange().simulate(_fmuFileName, _endTime, _stepSize,
                _enableLogging, _csvSeparator, _outputFileName);
    }

    /** Perform model exchange using the named Functional Mock-up Unit (FMU) file.
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
    public void simulate(String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {
        // Avoid a warning from FindBugs.
        FMUDriver._setEnableLogging(enableLogging);

        // Parse the .fmu file.
        FMIModelDescription fmiModelDescription = FMUFile
                .parseFMUFile(fmuFileName);

        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
        if (enableLogging) {
            System.out.println("FMUModelExchange: about to load "
                    + sharedLibrary);
        }
        _nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

        // The modelName may have spaces in it.
        _modelIdentifier = fmiModelDescription.modelIdentifier;

        new File(fmuFileName).toURI().toURL().toString();
        int numberOfStateEvents = 0;
        int numberOfStepEvents = 0;
        int numberOfSteps = 0;
        int numberOfTimeEvents = 0;

        // Callbacks
        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
                new FMULibrary.FMULogger(fmiModelDescription), fmiModelDescription.getFMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished());
        byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;

        // Instantiate the model.
        Function instantiateModelFunction;
        try {
            instantiateModelFunction = fmiModelDescription.getFmiFunction("fmiInstantiateModel");
        } catch (UnsatisfiedLinkError ex) {
            UnsatisfiedLinkError error = new UnsatisfiedLinkError(
                    "Could not load " + _modelIdentifier
                            + "_fmiInstantiateModel()"
                            + ". This can happen when a co-simulation .fmu "
                            + "is run in a model exchange context.");
            error.initCause(ex);
            throw error;
        }
        Pointer fmiComponent = (Pointer) instantiateModelFunction.invoke(
                Pointer.class, new Object[] { _modelIdentifier,
                        fmiModelDescription.guid, callbacks, loggingOn });
        if (fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException("Could not instantiate model.");
        }

        // Should these be on the heap?
        final int numberOfStates = fmiModelDescription.numberOfContinuousStates;
        final int numberOfEventIndicators = fmiModelDescription.numberOfEventIndicators;
        double[] states = new double[numberOfStates];
        double[] derivatives = new double[numberOfStates];

        double[] eventIndicators = null;
        double[] preEventIndicators = null;
        if (numberOfEventIndicators > 0) {
            eventIndicators = new double[numberOfEventIndicators];
            preEventIndicators = new double[numberOfEventIndicators];
        }

        // Set the start time.
        double startTime = 0.0;
        Function setTime = fmiModelDescription.getFmiFunction("fmiSetTime");
        invoke(setTime, new Object[] { fmiComponent, startTime },
                "Could not set time to start time: " + startTime + ": ");

        // Initialize the model.
        byte toleranceControlled = 0;
        FMIEventInfo eventInfo = new FMIEventInfo();
        invoke("_fmiInitialize", new Object[] { fmiComponent,
                toleranceControlled, startTime, eventInfo },
                "Could not initialize model: ");

        double time = startTime;
        if (eventInfo.terminateSimulation != 0) {
            System.out.println("Model terminated during initialization.");
            endTime = time;
        }

        PrintStream file = null;
        try {
            file = new PrintStream(outputFileName);
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to write header");
            }
            // Generate header row
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
            // Output the initial values.
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);

            // Functions used within the while loop, organized
            // alphabetically.
            Function completedIntegratorStep = fmiModelDescription.getFmiFunction("fmiCompletedIntegratorStep");
            Function eventUpdate = fmiModelDescription.getFmiFunction("fmiEventUpdate");
            Function getContinuousStates = fmiModelDescription.getFmiFunction("fmiGetContinuousStates");
            Function getDerivatives = fmiModelDescription.getFmiFunction("fmiGetDerivatives");
            Function getEventIndicators = fmiModelDescription.getFmiFunction("fmiGetEventIndicators");
            Function setContinuousStates = fmiModelDescription.getFmiFunction("fmiSetContinuousStates");

            boolean stateEvent = false;

            byte stepEvent = (byte) 0;
            // Loop until the time is greater than the end time.
            while (time < endTime) {
                invoke(getContinuousStates, new Object[] { fmiComponent,
                        states, numberOfStates },
                        "Could not get continuous states, time was " + time
                                + ": ");

                invoke(getDerivatives, new Object[] { fmiComponent,
                        derivatives, numberOfStates },
                        "Could not get derivatives, time was " + time + ": ");

                // Update time.
                double stepStartTime = time;
                time = Math.min(time + stepSize, endTime);
                boolean timeEvent = eventInfo.upcomingTimeEvent == 1
                        && eventInfo.nextEventTime < time;
                if (timeEvent) {
                    time = eventInfo.nextEventTime;
                }
                double dt = time - stepStartTime;
                invoke(setTime, new Object[] { fmiComponent, time },
                        "Could not set time, time was " + time + ": ");

                // Perform a step.
                for (int i = 0; i < numberOfStates; i++) {
                    // The forward Euler method.
                    states[i] += dt * derivatives[i];
                }

                invoke(setContinuousStates, new Object[] { fmiComponent,
                        states, numberOfStates },
                        "Could not set continuous states, time was " + time
                                + ": ");

                // Check to see if we have completed the integrator step.
                // Pass stepEvent in by reference. See
                // https://github.com/twall/jna/blob/master/www/ByRefArguments.md
                ByteByReference stepEventReference = new ByteByReference(
                        stepEvent);
                invoke(completedIntegratorStep, new Object[] { fmiComponent,
                        stepEventReference },
                        "Could not set complete integrator step, time was "
                                + time + ": ");

                // Save the state events.
                for (int i = 0; i < numberOfEventIndicators; i++) {
                    preEventIndicators[i] = eventIndicators[i];
                }

                // Get the eventIndicators.
                invoke(getEventIndicators, new Object[] { fmiComponent,
                        eventIndicators, numberOfEventIndicators },
                        "Could not set get event indicators, time was " + time
                                + ": ");

                stateEvent = Boolean.FALSE;
                for (int i = 0; i < numberOfEventIndicators; i++) {
                    stateEvent = stateEvent
                            || preEventIndicators[i] * eventIndicators[i] < 0;
                }

                // Handle Events
                if (stateEvent || stepEvent != (byte) 0 || timeEvent) {
                    if (stateEvent) {
                        numberOfStateEvents++;
                        if (enableLogging) {
                            for (int i = 0; i < numberOfEventIndicators; i++) {
                                System.out
                                        .println("state event "
                                                + (preEventIndicators[i] > 0
                                                        && eventIndicators[i] < 0 ? "-\\-"
                                                        : "-/-")
                                                + " eventIndicator[" + i
                                                + "], time: " + time);
                            }
                        }
                    }
                    if (stepEvent != (byte) 0) {
                        numberOfStepEvents++;
                        if (enableLogging) {
                            System.out.println("step event at " + time);
                        }
                    }
                    if (timeEvent) {
                        numberOfTimeEvents++;
                        if (enableLogging) {
                            System.out.println("Time event at " + time);
                        }
                    }

                    invoke(eventUpdate, new Object[] { fmiComponent, (byte) 0,
                            eventInfo },
                            "Could not set update event, time was " + time
                                    + ": ");

                    if (eventInfo.terminateSimulation != (byte) 0) {
                        System.out.println("Termination requested: " + time);
                        break;
                    }

                    if (eventInfo.stateValuesChanged != (byte) 0
                            && enableLogging) {
                        System.out.println("state values changed: " + time);
                    }
                    if (eventInfo.stateValueReferencesChanged != (byte) 0
                            && enableLogging) {
                        System.out.println("new state variables selected: "
                                + time);
                    }
                }

                // Generate a line for this step
                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
                numberOfSteps++;
            }
            invoke("_fmiTerminate", new Object[] { fmiComponent },
                    "Could not terminate: ");
        } finally {
            if (file != null) {
                file.close();
            }
            if (fmiModelDescription != null) {
                fmiModelDescription.dispose();
            }
        }

        System.out.println("Simulation from " + startTime + " to " + endTime
                + " was successful");
        System.out.println("  steps: " + numberOfSteps);
        System.out.println("  step size: " + stepSize);
        System.out.println("  stateEvents: " + numberOfStateEvents);
        System.out.println("  stepEvents: " + numberOfStepEvents);
        System.out.println("  timeEvents: " + numberOfTimeEvents);
        System.out.flush();
    }
}
