/* JUnit test that exports the demos.

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

package ptolemy.vergil.basic.export.test.junit;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import ptolemy.actor.Manager;
import ptolemy.kernel.util.KernelException;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.export.ExportModel;

///////////////////////////////////////////////////////////////////
//// ExportModelJUnitTest
/**
 * JUnit test that exports the demos.
 *
 * <p>To run these tests, use:
 * <pre>
 * cd $PTII
 * ./configure
 * ant test.single -Dtest.name=ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest -Djunit.formatter=plain
 * </pre>
 * or
 * <pre>
 * cd $PTII/ptolemy/vergil/basic/export/test/junit/;
 * export CLASSPATH=${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar;
 * export JAVAFLAGS="-Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true"
 * $PTII/bin/ptinvoke org.junit.runner.JUnitCore ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest
 * </pre>
 *
 * <p>
 * This test uses JUnitParams from <a
 * href="http://code.google.com/p/junitparams/#in_browser"
 * >http://code.google.com/p/junitparams/</a>, which is released under <a
 * href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License
 * 2.0</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class ExportModelJUnitTest {
    /** Export a model.
     *  @param modelPath The model to be exported. The code is exported to
     *  the directory contained by the model.
     *  @exception Throwable If there is a problem reading or exporting the model.
     */
    @Test
    @Parameters(method = "demos")
    public void RunExportModel(String modelPath) throws Throwable {
        String modelFile = modelPath.substring(modelPath.lastIndexOf("/") + 1);
        String modelName = modelFile.substring(0, modelFile.lastIndexOf("."));

        String ptolemyPtIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
        String fullModelPath = ptolemyPtIIDir + "/" + modelPath;

        String modelDirectory = modelPath.substring(0,
                modelPath.lastIndexOf("/"));
        // A directory inside the current directory that contains the model because
        // we remove the contents of the outputDirectory with the force parameter.
        String outputDirectory = ptolemyPtIIDir + "/" + modelDirectory + "/"
                + modelName;

        boolean openComposites = _openComposites(modelPath);
        boolean run = _runDemo(modelPath);
        long timeOut = _timeOut(modelPath);

        Date date = new Date();
        ExportModelJUnitTest._incrementCount();
        System.out.println("####### " + ExportModelJUnitTest._getCount() + " "
                + date + " $PTII/bin/ptinvoke "
                + "ptolemy.vergil.basic.export.ExportModel -force htm "
                + (run ? "-run " : " ")
                + (openComposites ? "-openComposites " : " ") + "-timeOut "
                + timeOut + " -whiteBackground " + modelPath + " $PTII/"
                + modelDirectory + "/" + modelName);

        if (!openComposites) {
            System.out
            .println("Warning: not opening composites for "
                    + modelPath
                    + ". See ptolemy/vergil/basic/export/test/junit/ExportModelJUnitTest.java");
        }

        if (!run) {
            System.out
            .println("Warning: not running "
                    + modelPath
                    + ". See ptolemy/vergil/basic/export/test/junit/ExportModelJUnitTest.java");
        }
        // ExportModel.exportModel() calls System.exit() unless we set this property.
        System.setProperty("ptolemy.ptII.doNotExit", "true");

        ExportModel exportModel = new ExportModel();
        try {
            // Set ptolemy.ptII.batchMode so that MessageHandler does
            // not hang on dialogs.
            System.setProperty("ptolemy.ptII.batchMode", "true");

            long startTime = new Date().getTime();
            exportModel.exportModel(false /* copyJavaScriptFiles */,
                    true /* force */, "htm", fullModelPath, run,
                    openComposites, false /* open results */, outputDirectory,
                    false /* save */, timeOut, true /* whitebackground */);
            System.out.println(Manager.timeAndMemory(startTime));
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            // If exporting html throws an exception, then that is a
            // test failure, not a test error.
            Assert.fail("Exporting HTML for " + modelPath + " failed: \n"
                    + KernelException.stackTraceToString(throwable));
        }
    }

    /**
     * Return a two dimensional array of arrays of strings that name the model
     * to be exported.
     *
     * @return a two dimension array of arrays of strings that name the
     * models to be exported.
     * @exception IOException If there is a problem accessing the directory.
     */
    public Object[] demos() throws IOException {
        // Read the contents of $PTII/ptolemy/configs/doc/models.txt
        String ptolemyPtIIDir = StringUtilities.getProperty("ptolemy.ptII.dir");
        String modelsFile = ptolemyPtIIDir + "/ptolemy/configs/doc/models.txt";
        FileInputStream fileInputStream = null;
        DataInputStream dataInputStream = null;
        BufferedReader reader = null;
        List<String> demos = new LinkedList<String>();
        try {
            fileInputStream = new FileInputStream(modelsFile);
            dataInputStream = new DataInputStream(fileInputStream);
            reader = new BufferedReader(new InputStreamReader(dataInputStream, java.nio.charset.Charset.defaultCharset()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Lines in models.txt look like
                // $CLASSPATH/lbnl/demo/CRoom/CRoom.xml
                String prefix = "$CLASSPATH/";
                if (!line.startsWith(prefix)) {
                    throw new IOException("Line in \"" + modelsFile
                            + "\" does not start with \"" + prefix
                            + "\".  Line was:\n" + line);
                }
                String modelPath = line.substring(prefix.length());
                if (_openModel(modelPath)) {
                    demos.add(modelPath);
                }
            }
        } catch (Exception ex) {
            IOException exception = new IOException("Failed to read \""
                    + modelsFile + "\"");
            exception.initCause(ex);
            throw exception;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }

        int i = 0;
        Object[][] data = new Object[demos.size()][1];
        for (String demo : demos) {
            data[i++][0] = demo;
        }
        return data;
    }

    /** Get the number of times RunExportModel has been invoked.
     *  @return the number of times RunExportModel has been invoked.
     */
    protected static int _getCount() {
        // To avoid FindBugs: Write to static field from instance method.
        return _count;
    }

    /** Increment the count of the number of times RunExportModel has been invoked.
     */
    protected static void _incrementCount() {
        // To avoid FindBugs: Write to static field from instance method.
        _count++;
    }

    /** Return true if we should open the composites.
     */
    private boolean _openComposites(String modelPath) {
        // Pathnames that should be skipped
        String[] skip = {
                // Fails with: Cannot render to more than 32 Canvas3Ds
                "Gravitation.xml", "GravitationWithCollisionDetection.xml", };
        for (String element : skip) {
            if (modelPath.indexOf(element) != -1) {
                return false;
            }
        }
        return true;
    }

    /** Return true if we should open the model.
     */
    private boolean _openModel(String modelPath) {
        // Pathnames that should be skipped
        String[] skip = { "ScaleWithEmbeddedCFileActor", // Only works on 32-bit
                "SimplePassPointer", // Only works on 32-bit
        };
        for (String element : skip) {
            if (modelPath.indexOf(element) != -1) {
                return false;
            }
        }
        return true;
    }

    /** Return true if we should run the demo.
     *  It does not make sense to run some demos.
     *  This method returns false for those demos.
     */
    private boolean _runDemo(String modelPath) {
        // Pathnames that should be skipped
        String[] skip = { "ptalon/gt/demo/Adder/Adder.xml", // gt does not have a run button: "Channel index 0 is out of range, because width is only 0."
                "AirManagementSystem.xml", // Assert is thrown.
                "AirManagementSystemCausalityLoop", // Deliberately brings up message on run.
                "AMS_AMSSim", // FMU does not reliably build everywhere.
                // CRoom, MatlabRoom, SystemCommand
                "lbnl/demo/", // FIXME: hangs, probably because the log window is not closed.
                "g4ltl/demo/", // These demos require wiring.
                "gt/demo/BouncingBallX2/BouncingBallX2/index.html",
                //"BrockAckerman.xml", // FIXME: Seems to hang when runnning under code coverage.
                "distributed/demo/Sleep/Sleep.xml", // Requires jini.
                "DECG.xml", // This has links to the DE demos and is not runnable in itself.
                "de/demo/Clock/Clock.xml", // "Audio Device Unavailable"
                //"domains/gr", // FIXME: need to close ViewScreen3D by adding a ViewScreen3D Tableau.
                "GeneratorRegulatorProtectorSimXRhapsodyFMU", //SimX only works under 32-bits.
                "PhysicalPlantCausalityLoop", // Deliberately brings up message on run.  AMS
                "ptango", // Skip running all ptango demos, they do not provide useful exportable output.
                //"GravitationWithCollisionDetection.xml", // "Cannot render to more than 32 Canvas3Ds."
                //"demo/ExecDemo/Demos/BouncingBall.xml", // "Cannot render to more than 32 Canvas3Ds."
                "EPlus70Actuator.xml", // Hangs in a strange way after running.o
                "ElectricPowerSystem.xml", // Just has links to other demos.
                "ExecDemos", // hangs
                "FMUSimulationX", // 32-bit only
                "GeneratorContactorLoad.xml", // The GeneratorContactorLoad file is a component used inElectricPowerSystem models.
                "GeneratorRegulatorProtectorSimXRhapsodyFMU.xml", // 32-bit Windows only.
                "HierarchyFlattening.xml", // gt
                "actor/lib/io", // Don't run the demos in actor/lib/io, some read from stdin and never exit.
                "iRobotCreateVerification.xml", // Annotation says that it does not simulate.
                "JMFJAI.xml", // Requires a video camera
                "KarplusStrong.xml", // "Audio Device Unavailable"
                "MatlabRoom.xml", // Matlab message: Error: Too many inputs passed to SimpleFunctionThunk.
                "MapReduceDDF.xml", // Hangs.
                "ModularCG.xml", // cg model fails to run while exporting.
                "ptolemy/domains/ptides/demo/Speaker/Speaker.xml", // Luminary demo, Annotation says not to run.
                "PtidesBasicOnePlatform.xml", // Annotation says not to run.
                "PublisherTest", // gt
                "RealTimeComposite.xml", // "Audio Device Unavailable"
                "RecordManipulation", // Python demo pops up a dialog.
                "RobotOnCircleKV.xml", // Needs the KeyValue model running.
                "RijndaelEncryption.xml", // FIXME: Hangs during wrapup.
                "ScaleC.xml", // FIXME: the JVM crashes while running.
                "SerialPort.xml", // Requires serial port.
                "Signature.xml", // Throws an exception in the normal course of operations.
                "SimpleTrafficLightSMVModule.xml", // "PedestrianLightSMV can not run in simulation mode."
                "SmartChaseWithSmartIntruder.xml", // Needs the KeyValue model running.
                "SmartIntruder.xml", // Needs the KeyValue model running.
                "SoundSpectrum.xml", // "Audio Device Unavailable"
                "SynthesizedVoice.xml", // "Audio Device Unavailable"
                "SystemLevelType", // The SystemLevelType demos are not meant to be run.
                "TunnelingBallDevice", // Annotation says that it cannot be run.
                "VideoCapture.xml", // Requires a video camera.
        };
        for (String element : skip) {
            if (modelPath.indexOf(element) != -1) {
                return false;
            }
        }
        return true;
    }

    /** Return the time out in milliseconds.
     *  The timeout for most demos is 30 seconds.
     *  Some demos run longer.
     */
    private long _timeOut(String modelPath) {
        // Pathnames for demos that get a longer running time
        String[] longRunningDemos = { "ExecDemos.xml" };
        for (String longRunningDemo : longRunningDemos) {
            if (modelPath.indexOf(longRunningDemo) != -1) {
                return 30000 * 2;
            }
        }
        return 30000;
    }

    /** Number of models exported. */
    private static int _count = 0;
}
