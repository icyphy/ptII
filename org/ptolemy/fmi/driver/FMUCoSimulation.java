/* Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.

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

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.Charset;

import org.ptolemy.fmi.FMI20CallbackFunctions;
import org.ptolemy.fmi.FMICallbackFunctions;
import org.ptolemy.fmi.FMIModelDescription;
import org.ptolemy.fmi.FMUFile;
import org.ptolemy.fmi.FMULibrary;

import com.sun.jna.Function;
import com.sun.jna.Pointer;

///////////////////////////////////////////////////////////////////
//// FMUCoSimulation

/** Read a Functional Mock-up Unit .fmu file and invoke it as a co-simulation.
 *
 * <p>Currently, FMI 1.0 and 2.0RC1 are supported.</p>
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
 * @author Christopher Brooks, based on fmusim_cs/main.c by Jakob Mauss
@version $Id$
@since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class FMUCoSimulation extends FMUDriver {

    /** Perform co-simulation using the named Functional Mock-up Unit (FMU) file.
     *
     *  <p>Usage:</p>
     *  <pre>
     *  java -classpath ../../../../lib/jna-4.0.0-variadic.jar:../../../.. \
     *  org.ptolemy.fmi.driver.FMUCoSimulation \
     *  file.fmu [endTime] [stepTime] [loggingOn] [csvSeparator] [outputFile]
     *  </pre>
     *  <p>For example, under Mac OS X or Linux:
     *  <pre>
     *  java -classpath $PTII/lib/jna-4.0.0-variadic.jar:${PTII} org.ptolemy.fmi.driver.FMUCoSimulation \
     *      ../fmu/cs/inc.fmu
     *  </pre>
     *
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
        FMUCoSimulation fmuCoSimulation = new FMUCoSimulation();
        fmuCoSimulation._processArgs(args);
        fmuCoSimulation.simulate(fmuCoSimulation._fmuFileName,
                fmuCoSimulation._endTime, fmuCoSimulation._stepSize,
                fmuCoSimulation._enableLogging, fmuCoSimulation._csvSeparator,
                fmuCoSimulation._outputFileName);
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
    @Override
    public void simulate(String fmuFileName, double endTime, double stepSize,
            boolean enableLogging, char csvSeparator, String outputFileName)
            throws Exception {

        // Avoid a warning from FindBugs.
        _setEnableLogging(enableLogging);

        // Parse the .fmu file.
        FMIModelDescription fmiModelDescription = FMUFile
                .parseFMUFile(fmuFileName);

        // Load the shared library.
        _nativeLibrary = fmiModelDescription.getNativeLibrary();

        // The modelName may have spaces in it.
        _modelIdentifier = fmiModelDescription.modelIdentifier;

        // The URL of the fmu file.
        String fmuLocation = new File(fmuFileName).toURI().toURL().toString();
        // The tool to use if we have tool coupling.
        String mimeType = "application/x-fmu-sharedlibrary";
        // Timeout in ms., 0 means wait forever.
        double timeout = 1000;
        // There is no simulator UI.
        // FMI-2.0, so we have two variables.
        byte visible = 0;
        int toBeVisibleFMI2 = 0;

        // Run the simulator without user interaction.
        byte interactive = 0;

        // A byte in FMI-1.0, an int in FMI-2.0, so we have two variables.
        byte loggingOn = enableLogging ? (byte) 1 : (byte) 0;
        int loggingOnFMI2 = _enableLogging ? 1 : 0;

        _fmiVersion = Double.valueOf(fmiModelDescription.fmiVersion);

        Pointer fmiComponent = null;
        // Callbacks
        if (_fmiVersion < 1.5) {
            FMICallbackFunctions.ByValue callbacks = new FMICallbackFunctions.ByValue(
                    new FMULibrary.FMULogger(fmiModelDescription),
                    fmiModelDescription.getFMUAllocateMemory(),
                    new FMULibrary.FMUFreeMemory(),
                    new FMULibrary.FMUStepFinished());
            Function instantiateSlave = fmiModelDescription
                    .getFmiFunction("fmiInstantiateSlave");
            fmiComponent = (Pointer) instantiateSlave.invoke(Pointer.class,
                    new Object[] { _modelIdentifier, fmiModelDescription.guid,
                fmuLocation, mimeType, timeout, visible,
                interactive, callbacks, loggingOn });

        } else {
            // FMI 1.5 and greater.
            // In FMI-1.5 and FMI-2.0, this is a pointer to the structure, which
            // is by
            // default how a subclass of Structure is handled, so there is no
            // need for the inner class ByValue, as above.
            FMI20CallbackFunctions callbacks20 = new FMI20CallbackFunctions(
                    new FMULibrary.FMULogger(fmiModelDescription),
                    fmiModelDescription.getFMUAllocateMemory(),
                    new FMULibrary.FMUFreeMemory(),
                    new FMULibrary.FMUStepFinished(),
                    // FIXME: It is not clear if we should pass
                    // fmiComponent here.  Instead, we should
                    // pass an environment?  See the spec
                    fmiComponent);
            Function fmiInstantiateFunction = fmiModelDescription
                    .getFmiFunction("fmiInstantiate");

            // FIXME: Not sure about the fmiType enumeration, see
            // ptolemy/actor/lib/fmi/fmus/jmodelica/CoupledClutches/src/sources/fmiFunctionTypes.h,
            // which was copied from
            // /usr/local/jmodelica/ThirdParty/FMI/2.0/.

            int fmiType = 1; // CoSimulation
            if (fmiModelDescription.modelExchange) {
                // Presumably Hybrid-Cosimulation would be 3?  Ptolemy could be 4?
                fmiType = 0;
            }
            fmiComponent = (Pointer) fmiInstantiateFunction.invoke(
                    Pointer.class, new Object[] { _modelIdentifier, fmiType,
                        fmiModelDescription.guid,
                        fmiModelDescription.fmuResourceLocation,
                            callbacks20, toBeVisibleFMI2, loggingOnFMI2 });
        }

        if (fmiComponent.equals(Pointer.NULL)) {
            throw new RuntimeException("Could not instantiate model.");
        }

        if (enableLogging) {
            System.out.println("instantiatedSlave");
        }

        double startTime = 0;

        if (enableLogging) {
            System.out.println("about to initializeSlave");
        }

        if (_fmiVersion < 1.5) {
            invoke(fmiModelDescription, "fmiInitializeSlave", new Object[] {
                    fmiComponent, startTime, (byte) 1, endTime },
                    "Could not initialize slave: ");
        } else {
            // _fmiVersion => 2.0
            double relativeTolerance = 1e-4;
            byte _toleranceControlled = (byte) 0; // fmiBoolean
            invoke(fmiModelDescription, "fmiSetupExperiment", new Object[] {
                    fmiComponent, _toleranceControlled, relativeTolerance,
                    startTime, (byte) 1, endTime },
                    "Failed to setup the experiment of the FMU: ");
            invoke(fmiModelDescription, "fmiEnterInitializationMode",
                    new Object[] { fmiComponent },
                    "Failed to enter the initialization mode of the FMU: ");
            invoke(fmiModelDescription, "fmiExitInitializationMode",
                    new Object[] { fmiComponent },
                    "Failed to exit the initialization mode of the FMU:");

        }

        File outputFile = new File(outputFileName);
        PrintStream file = null;
        try {
            // gcj does not have this constructor
            //file = new PrintStream(outputFile);

            // Fix for FindBugs: Dm: Reliance on default encoding.
            file = new PrintStream(outputFileName, Charset.defaultCharset()
                    .toString());
            if (enableLogging) {
                System.out.println("FMUCoSimulation: about to write header");
            }
            // Generate header row
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.TRUE);
            // Output the initial values.
            OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                    fmiComponent, startTime, file, csvSeparator, Boolean.FALSE);
            // Loop until the time is greater than the end time.
            double time = startTime;

            Function doStep = fmiModelDescription.getFmiFunction("fmiDoStep");
            while (time < endTime) {
                if (enableLogging) {
                    System.out.println("FMUCoSimulation: about to call "
                            + _modelIdentifier
                            + "_fmiDoStep(Component, /* time */ " + time
                            + ", /* stepSize */" + stepSize + ", 1)");
                }
                invoke(doStep, new Object[] { fmiComponent, time, stepSize,
                        (byte) 1 }, "doStep(): Could not simulate, time was "
                                + time + ": ");
                time += stepSize;
                // Generate a line for this step
                OutputRow.outputRow(_nativeLibrary, fmiModelDescription,
                        fmiComponent, time, file, csvSeparator, Boolean.FALSE);
            }
            if (_fmiVersion < 2.0) {
                invoke(fmiModelDescription, "fmiTerminateSlave",
                        new Object[] { fmiComponent },
                        "Could not terminate slave: ");
                // Don't throw an exception while freeing a slave.  Some
                // fmiTerminateSlave calls free the slave for us.
                Function freeSlaveInstance = fmiModelDescription
                        .getFmiFunction("fmiFreeSlaveInstance");
                // In FMI-1.0, fmiFreeSlaveInstance() returns void.
                freeSlaveInstance.invoke(new Object[] { fmiComponent });
            } else {
                invoke(fmiModelDescription, "fmiTerminate",
                        new Object[] { fmiComponent },
                        "Could not terminate slave:");
                // In FMI-2.0, fmi2FreeInstance() returns void.
                Function function = fmiModelDescription
                        .getFmiFunction("fmiFreeInstance");
                function.invoke(new Object[] { fmiComponent });
            }

        } finally {
            if (file != null) {
                file.close();
            }
            if (fmiModelDescription != null) {
                fmiModelDescription.dispose();
            }
        }

        if (enableLogging) {
            System.out.println("Results are in "
                    + outputFile.getCanonicalPath());
            System.out.flush();
        }
    }
}
