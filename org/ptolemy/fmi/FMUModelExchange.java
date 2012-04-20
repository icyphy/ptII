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
package org.ptolemy.fmi;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;
import java.util.HashSet;

import com.sun.jna.Function;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;


///////////////////////////////////////////////////////////////////
//// FMUModelExchange

/** Read a Functional Mock-up Unit .fmu file and invoke it as a model exchange.
 *  
 * <p>This file is based on fmusdk/src/model_exchange/fmusim_me/main.c:</p>
 * <pre>
 * Author: Jakob Mauss
 * Copyright 2011 QTronic GmbH. All rights reserved. 
 * </pre>

 * @author Christopher Brooks, based on fmusim_me/main.c by Jakob Mauss
 * @version $Id: FMUModelExchange.java 63359 2012-04-16 06:45:49Z cxh $
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUModelExchange {

    // FIXME: factor this out, it duplicates code in FMUCoSimulation
    public interface FMULibrary extends FMILibrary {
        // We need a class that implement the interface because
        // certain methods require interfaces as arguments, yet we
        // need to have method bodies, so we need an actual class.
        public class FMULogger implements FMICallbackLogger {
            // What to do about jni callbacks with varargs?  
            // See http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/JNA#fmiCalbackLogger
            public void apply(Pointer fmiComponent, String instanceName, int status, String category, String message/*, Pointer ... parameters*/) {
                System.out.println("Java FMULogger, status: " + status);
                System.out.println("Java FMULogger, message: " + message/*.getString(0)*/);
            }
        }
        //http://markmail.org/message/6ssggt4q6lkq3hen

        public class FMUAllocateMemory implements FMICallbackAllocateMemory {
            public Pointer apply(NativeSizeT nobj, NativeSizeT size) {
                int numberOfObjects = nobj.intValue();
                if (numberOfObjects <= 0) {
                    // instantiateModel() in fmuTemplate.c
                    // will try to allocate 0 reals, integers, booleans or strings.
                    // However, instantiateModel() later checks to see if
                    // any of the allocated spaces are null and fails with
                    // "out of memory" if they are null.
                    numberOfObjects = 1;
                }
                Memory memory = new Memory(numberOfObjects * size.intValue());
                Memory alignedMemory = memory.align(4);
                memory.clear();
                Pointer pointer = alignedMemory.share(0);

                // Need to keep a reference so the memory does not get gc'd.
                // See http://osdir.com/ml/java.jna.user/2008-09/msg00065.html   
                _pointers.add(pointer);

//                 System.out.println("Java fmiAllocateMemory " + nobj + " " + size
//                         + "\n        memory: " + memory + " " +  + memory.SIZE + " " + memory.SIZE % 4
//                         + "\n alignedMemory: " + alignedMemory + " " + alignedMemory.SIZE + " " + alignedMemory.SIZE %4
//                         + "\n       pointer: " + pointer + " " + pointer.SIZE + " " + (pointer.SIZE % 4));
                return pointer;
            }
        }

        public class FMUFreeMemory implements FMICallbackFreeMemory {
            public void apply(Pointer obj) {
                //System.out.println("Java fmiFreeMemory " + obj);
                _pointers.remove(obj);
            }
        }
	public class FMUStepFinished implements FMIStepFinished {
            public void apply(Pointer c, int status) {
                System.out.println("Java fmiStepFinished: " + c + " " + status);
            }
	};
    }

    /** Perform model exchange using the named Functional Mock-up Unit (FMU) file.
     *          
     *  <p>Usage:</p>
     *  <pre>
     *  java -classpath ../../../lib/jna.jar:../../..\
     *      org.ptolemy.fmi.FMUModelExchange \
     *      file.fmu [endTime] [stepTime] [loggingOn] [csvSeparator] [outputFile]
     *  </pre>
     *  <p>For example:</p>
     *  <pre>
     *  java -classpath "${PTII}/lib/jna.jar:${PTII}/ptII" \
     *       org.ptolemy.fmi.FMUModelExchange \
     *       $PTII/org/ptolemy/fmi/fmu/me/bouncingBall.fmu \
     *       1.0 0.1 true c bouncingBall.csv
     *  </pre>
     *  <p>The command line arguments have the following meaning:</p>
     *  <dl>
     *  <dt>file.fmu</dt>
     *  <dd>The co-simulation Functional Mock-up Unit (FMU) file.  In FMI-1.0,
     *  co-simulation fmu files contain a modelDescription.xml file that 
     *  has an &lt;Implementation&gt; element.  Model exchange fmu files do not
     *  have this element.</dd>
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
    public static void main(String[] args) throws Exception {
        String fmuFileName = args[0];
        double endTime = 1.0; // In seconds
        double stepSize = 0.1; // In seconds
        boolean enableLogging = false;
        char csvSeparator = ',';
        String outputFileName = "results.csv";

        if (args.length >= 2) {
            endTime = Double.valueOf(args[1]);
        }
        if (args.length >= 3) {
            stepSize = Double.valueOf(args[2]);
        }
        if (args.length >= 4) {
            enableLogging = Boolean.valueOf(args[3]);
        }
        if (args.length >= 5) {
            if (args[4].equals("c")) {
                csvSeparator = ',';
            } else if (args[4].equals("s")) {
                csvSeparator = ';';
            } else {
                csvSeparator = args[4].charAt(0);
            }
        }
        if (args.length >= 6) {
            outputFileName = args[5];
        }

        simulate(fmuFileName, endTime, stepSize, enableLogging, csvSeparator, outputFileName);
    }

    /** Perform co-simulation using the named Functional Mock-up Unit (FMU) file.
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
    public static void simulate(String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, char csvSeparator, String outputFileName) throws Exception {
            

        // Parse the .fmu file.
        FMIModelDescription fmiModelDescription = FMUFile.parseFMUFile(fmuFileName);

        // Load the shared library.
        String sharedLibrary = FMUFile.fmuSharedLibrary(fmiModelDescription);
        if (enableLogging) {
            System.out.println("FMUModelExchange: about to load " + sharedLibrary);
        }
        NativeLibrary nativeLibrary = NativeLibrary.getInstance(sharedLibrary);

        // The modelName may have spaces in it.   
        String modelIdentifier = fmiModelDescription.modelIdentifier;
        
        // The URL of the fmu file.
        String fmuLocation = new File(fmuFileName).toURI().toURL().toString();
        // The tool to use if we have tool coupling.
        String mimeType = "application/x-fmu-sharedlibrary";
        // Timeout in ms., 0 means wait forever.
        double timeout = 1000;
        // There is no simulator UI.
        byte visible = 0;
        // Run the simulator without user interaction.
        byte interactive = 0;

        int numberOfStateEvents = 0;
        int numberOfStepEvents = 0;
        int numberOfSteps = 0;
        int numberOfTimeEvents = 0;

        // Callbacks
        FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
                new FMULibrary.FMULogger(),
                new FMULibrary.FMUAllocateMemory(),
                new FMULibrary.FMUFreeMemory(),
                new FMULibrary.FMUStepFinished());
        // Logging tends to cause segfaults because of vararg callbacks.
        byte loggingOn = (enableLogging ? (byte)1 : (byte)0);
        loggingOn = (byte)0;

        // Instantiate the model.
        Function instantiateModelFunction;
        String instantiateModelFunctionName = modelIdentifier + "_fmiInstantiateModel";
        try {
            instantiateModelFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    instantiateModelFunctionName);
        } catch (UnsatisfiedLinkError ex) {
            throw new UnsatisfiedLinkError("Could not load "
                    + instantiateModelFunctionName
                    + ". This can happen when a co-simulation .fmu "
                    + "is run in a model exchange context.");
        }
        Pointer fmiComponent = (Pointer) instantiateModelFunction.invoke(Pointer.class,
                new Object [] {
                    modelIdentifier,
                    fmiModelDescription.guid,
                    callbacks,
                    loggingOn});
        if (fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException("Could not instantiate model.");
        }

        // Allocate memory.
        int numberOfStates = fmiModelDescription.numberOfContinuousStates;
        int numberOfEventIndicators = fmiModelDescription.numberOfEventIndicators;
        FMULibrary.FMUAllocateMemory fmuAllocateMemory = new FMULibrary.FMUAllocateMemory();
        //x    = (double *) calloc(nx, sizeof(double));
        Pointer states = new Memory(numberOfStates * 4).share(0);

        //xdot = (double *) calloc(nx, sizeof(double));
        Pointer derivatives = new Memory(numberOfStates * 4).share(0);

        Pointer eventIndicators = null;
        Pointer preEventIndicators = null;
        if (numberOfEventIndicators > 0) {
            //z    =  (double *) calloc(numberOfEventIndicators, sizeof(double));
            eventIndicators = new Memory(numberOfEventIndicators * 4).share(0);
            //prez =  (double *) calloc(numberOfEventIndicators, sizeof(double));
            preEventIndicators = new Memory(numberOfEventIndicators * 4).share(0);
        }
        if (states == null || derivatives == null 
                || (numberOfEventIndicators > 0
                        && (eventIndicators == null
                                || preEventIndicators == null))) {
            throw new RuntimeException("Out of memory!");
        }

        // Set the start time.
        double startTime = 0.0;
        String setTimeFunctionName = modelIdentifier + "_fmiSetTime";
        Function setTimeFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                setTimeFunctionName);
        int fmiFlag = ((Integer)setTimeFunction.invoke(Integer.class,new Object[] {fmiComponent, startTime})).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new RuntimeException("Could not set time to startTime: " + fmiFlag);
        }

        // Initialize the model;
        String initializeFunctionName = modelIdentifier + "_fmiInitialize";
        Function initializeFunction = FMUFile.getFunction(nativeLibrary, enableLogging, initializeFunctionName);
        byte toleranceControlled = 0;
        FMIEventInfo eventInfo = new FMIEventInfo();
        if (enableLogging) {
            System.out.println("FMUModelExchange: about to call " + initializeFunctionName);
        }
        fmiFlag = ((Integer)initializeFunction.invoke(Integer.class,
                         new Object[] {fmiComponent,
                                       toleranceControlled, startTime, eventInfo})).intValue();
        if (fmiFlag > FMILibrary.FMIStatus.fmiWarning) {
            throw new RuntimeException("Could not initialize Model: " + fmiFlag);
        }
        double time = startTime;
        if (eventInfo.terminateSimulation != 0) {
            System.out.println("Model terminated during initialization.");
            endTime = time;
        }

        File outputFile = new File(outputFileName);
        PrintStream file = null;
        try {
            file = new PrintStream(outputFile);
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to write header");
            }
            // Generate header row
            OutputRow.outputRow(nativeLibrary, fmiModelDescription, fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);  
            // Output the initial values.
            OutputRow.outputRow(nativeLibrary, fmiModelDescription, fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);

            // Functions used within the while loop, organized
            // alphabetically.
            String completedIntegratorStepFunctionName = modelIdentifier + "_fmiCompletedIntegratorStep";
            Function completedIntegratorStepFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    completedIntegratorStepFunctionName);

            String getContinuousStatesFunctionName = modelIdentifier + "_fmiGetContinuousStates";
            Function getContinuousStatesFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    getContinuousStatesFunctionName);

            String getDerivativesFunctionName = modelIdentifier + "_fmiGetDerivatives";
            Function getDerivativesFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    getDerivativesFunctionName);

            String getEventIndicatorsFunctionName = modelIdentifier + "_fmiGetEventIndicators";
            Function getEventIndicatorsFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    getEventIndicatorsFunctionName);

            String eventUpdateFunctionName = modelIdentifier + "_fmiEventUpdate";
            Function eventUpdateFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    eventUpdateFunctionName);

            String setContinuousStatesFunctionName = modelIdentifier + "_fmiSetContinuousStates";
            Function setContinuousStatesFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    setContinuousStatesFunctionName);


            boolean stateEvent = false;

            // Loop until the time is greater than the end time.
            while (time < endTime) {
                if (enableLogging) {
                    System.out.println("FMUModelExchange: about to call " + getContinuousStatesFunctionName);
                }
                fmiFlag = ((Integer)getContinuousStatesFunction.invokeInt(new Object[] {fmiComponent, states, numberOfStates})).intValue();

                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not get continuous states. " + fmiFlag + " Time was " + time);
                }

                if (enableLogging) {
                    System.out.println("FMUModelExchange: about to call " + getDerivativesFunctionName);
                }
                fmiFlag = ((Integer)getDerivativesFunction.invokeInt(new Object[] {fmiComponent, derivatives, numberOfStates})).intValue();

                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not get derivatives. " + fmiFlag + " Time was " + time);
                }

                // Update time.
                double stepStartTime = time;
                time = Math.min(time + stepSize, endTime);
                boolean timeEvent = eventInfo.upcomingTimeEvent == 1 && eventInfo.nextEventTime < time;
                if (timeEvent) {
                    time = eventInfo.nextEventTime;
                }
                double dt = time - stepStartTime;
                if (enableLogging) {
                    System.out.println("FMUModelExchange: about to call " + setTimeFunctionName);
                }
                fmiFlag = ((Integer)setTimeFunction.invoke(Integer.class,new Object[] {fmiComponent, time})).intValue();
                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not set time: " + fmiFlag + ". Time was " + time);
                }

                // Perform a step.
                for (int i = 0; i < numberOfStates; i++) {
                    // The forward Euler method.
                    states.setDouble(i, states.getDouble(i) + (dt * derivatives.getDouble(i)));
                }
                
                
                if (enableLogging) {
                    System.out.println("FMUModelExchange: about to call " + setContinuousStatesFunctionName);
                }
                fmiFlag = ((Integer)setContinuousStatesFunction.invoke(Integer.class,new Object[] {fmiComponent, states, numberOfStates})).intValue();
                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not set continuous states: " + fmiFlag + ". Time was " + time);
                }

                byte stepEvent = (byte)0;
                // Check to see if we have completed the integrator step.
                if (enableLogging) {
                    System.out.println("FMUModelExchange: about to call " + completedIntegratorStepFunctionName);
                }
                fmiFlag = ((Integer)completedIntegratorStepFunction.invoke(Integer.class,new Object[] {fmiComponent, stepEvent})).intValue();
                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not set complete integrator step: " + fmiFlag + ". Time was " + time);
                }


                // Save the state events.
                for (int i = 0; i < numberOfEventIndicators; i++) {
                    preEventIndicators.setDouble(i, eventIndicators.getDouble(i));
                }
                        
                // Get the eventIndicators.
                if (enableLogging) {
                    System.out.println("FMUModelExchange: about to call " + getEventIndicatorsFunctionName);
                }
                fmiFlag = ((Integer)getEventIndicatorsFunction.invoke(Integer.class,new Object[] {fmiComponent, eventIndicators, numberOfEventIndicators})).intValue();
                if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                    throw new Exception("Could not set get event indicators: " + fmiFlag + ". Time was " + time);
                }

                stateEvent = Boolean.FALSE;
                for (int i = 0; i < numberOfEventIndicators; i++) {
                    stateEvent = stateEvent || (preEventIndicators.getDouble(i) * eventIndicators.getDouble(i) < 0);  
                }

                // Handle Events
                if (stateEvent || stepEvent != (byte)0  || timeEvent) {
                    if (stateEvent) {
                        numberOfStateEvents++; 
                        if (enableLogging) {
                            for (int i = 0; i < numberOfEventIndicators; i++) {
                                System.out.println("state event " + 
                                        ((preEventIndicators.getDouble(i) >0 && eventIndicators.getDouble(i)<0)
                                                ? "-\\-" : "-/-")
                                        + " eventIndicator[" + i + "], time: " + time);
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

                    
                    if (enableLogging) {
                        System.out.println("FMUModelExchange: about to call " + eventUpdateFunctionName);
                    }
                    fmiFlag = ((Integer)eventUpdateFunction.invoke(Integer.class,new Object[] {fmiComponent, (byte)0, eventInfo})).intValue();
                    if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                        throw new Exception("Could not set update event: " + fmiFlag + ". Time was " + time);
                    }

                    if (eventInfo.terminateSimulation != (byte) 0) {
                        System.out.println("Termination requested: " + time);
                        break;
                    }

                    if (eventInfo.stateValuesChanged != (byte) 0 && enableLogging) {
                        System.out.println("state values changed: " + time);
                    }
                    if (eventInfo.stateValueReferencesChanged != (byte) 0 && enableLogging) {
                        System.out.println("new state variables selected: " + time);
                    }
                }

                // Generate a line for this step
                OutputRow.outputRow(nativeLibrary, fmiModelDescription, fmiComponent, time, file, csvSeparator, Boolean.FALSE);
                numberOfSteps++;
            }
            String terminateFunctionName = modelIdentifier + "_fmiSetContinuousStates";
            Function terminateFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                    terminateFunctionName);
        
            if (enableLogging) {
                System.out.println("FMUModelExchange: about to call " + terminateFunctionName);
            }
            fmiFlag = ((Integer)terminateFunction.invoke(Integer.class,new Object[] {fmiComponent})).intValue();
            if (fmiFlag != FMILibrary.FMIStatus.fmiOK) {
                throw new Exception("Could not terminates: " + fmiFlag);
            }

        } finally {            
            if (file != null) {
                file.close();
            }
        }

        Function terminateFunction = FMUFile.getFunction(nativeLibrary, enableLogging,
                modelIdentifier + "_fmiTerminateSlave");
        fmiFlag = ((Integer)terminateFunction.invokeInt(new Object[] {fmiComponent})).intValue();

        System.out.println("Simulation from " + startTime + " to " + endTime + " was successful");
        System.out.println("  steps: " + numberOfSteps);
        System.out.println("  step size: " + stepSize);
        System.out.println("  stateEvents: " + numberOfStateEvents);
        System.out.println("  stepEvents: " + numberOfStepEvents);
        System.out.println("  timeEvents: " + numberOfTimeEvents);
    }

    /** Keep references to memory that has been allocated and
     *  avoid problems with the memory being garbage collected.   
     */   
    private static Set<Pointer> _pointers = new HashSet<Pointer>();

}
