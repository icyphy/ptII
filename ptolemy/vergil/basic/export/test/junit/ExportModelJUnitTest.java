/* JUnit test that exports the demos.

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
 * (cd $PTII/ptolemy/vergil/basic/export/test/junit/; java -Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true -classpath ${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTest)
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
 * @version $Id: FMUCoSimulationJUnitTest.java 63110 2012-03-06 02:09:03Z cxh $
 * @since Ptolemy II 8.1
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

        _count++;
        Date date = new Date();
        System.out.println("####### " + _count + " " + date
                + " $PTII/bin/ptinvoke "
                + "ptolemy.vergil.basic.export.ExportModel -force htm "
                + (run ? "-run " : " ")
                + (openComposites ? "-openComposites " : " ")
                + " -whiteBackground " + modelPath + " $PTII/" + modelDirectory
                + "/" + modelName);

        // ExportModel.exportModel() calls System.exit() unless we set this property.
        System.setProperty("ptolemy.ptII.doNotExit", "true");

        ExportModel exportModel = new ExportModel();
        try {
            long startTime = new Date().getTime();
            exportModel.exportModel(false /* copyJavaScriptFiles */,
                    true /* force */, "htm", fullModelPath, run,
                    openComposites, false /* open results */, outputDirectory,
                    false /* save */, true /* whitebackground */);
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
            reader = new BufferedReader(new InputStreamReader(dataInputStream));
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

    /** Return true if we should open the composites.
     */
    private boolean _openComposites(String modelPath) {
        // Pathnames that should be skipped
        String[] skip = {
                // Fails with: Cannot render to more than 32 Canvas3Ds
                "Gravitation.xml", "GravitationWithCollisionDetection.xml", };
        for (int i = 0; i < skip.length; i++) {
            if (modelPath.indexOf(skip[i]) != -1) {
                return false;
            }
        }
        return true;
    }

    /** Return true if we should open the model.
     */
    private boolean _openModel(String modelPath) {
        // Pathnames that should be skipped
        String[] skip = {
                //"luminary/adapters/ptolemy/domains/ptides/demo/Speaker/Speaker.xml", // TypeConflict
                //"luminary/adapters/ptolemy/domains/ptides/demo/Accumulator/Accumulator.xml", // TypeConflict
                //"ModularCGPubSub.xml", //Can't find the publisher for "channel".
                //"MonotonicityAnalysis.xml", // Expected '{x = General}' but got '{x = NonMonotonic_{<o...
                "ddf/demo/IfThenElse/IfThenElse.xml", // FIXME: Failed to generate sub-web-page.
                "ddf/demo/IfThenElse/IfThenElseFSM.xml", // FIXME: Failed to generate sub-web-page.

                "ScaleWithEmbeddedCFileActor", // Only works on 32-bit
                "SimplePassPointer", // Only works on 32-bit
                "MatlabWirelessSoundDetection.xml", // Hangs.
        //"ModeReference.xml", // "Cannot call invokeAndWait from the event dispatcher thread"
        //"Signature.xml", // Keystore is not present.
        };
        for (int i = 0; i < skip.length; i++) {
            if (modelPath.indexOf(skip[i]) != -1) {
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
        String[] skip = {
                "ptalon/gt/demo/Adder/Adder.xml", // "Channel index 0 is out of range, because width is only 0."
                "backtrack/demo/PrimeTest/PrimeTest.xml", // FIXME: Channel index 0 out of range.
                "CRoom.xml", // hangs.
                "distributed/demo/Sleep/Sleep.xml", // Requires jini.
                "de/demo/Clock/Clock.xml", // FIXME: "Audio Device Unavailable"
                "domains/gr", // "Cannot render to more than 32 Canvas3Ds",
                              // need to close ViewScreen3D by adding a ViewScreen3D Tableau.
                "ExecDemos.xml", // FIXME: vergil: command not found
                "GravitationWithCollisionDetection.xml", // "Cannot render to more than 32 Canvas3Ds."
                "HierarchyFlattening.xml", // gt
                "iRobotCreateVerification.xml", // Annotation says that it does not simulate.
                "IterateOverArray.xml", // FIXME: no matching function abs( string )
                "JMFJAI.xml", "KarplusStrong.xml",
                "MatlabRoom.xml", // Matlab message: Error: Too many inputs passed to SimpleFunctionThunk.
                "ModelReference.xml", // FIXME: "Cannot call invokeAndWait from the event dispatcher thread"
                "ModularCGPubSub.xml", // FIXME: Can't link Subscriber with Publisher, channel was "channel
                "ptolemy/gt/demo/Adder/Adder.xml", // FIXME: Channel index 0 is out of range, because width is only 0.
                "ptolemy/domains/ptides/demo/Speaker/Speaker.xml", // FIXME: Types resolved to unacceptable types in .Speaker due to the following inequalities:
                "PrintingPress.xml", // FIXME: "Cannot set local time to -Infinity, which is earlier than the last committed current time 0.0"
                "PtidesBasicOnePlatform.xml", // FIXME: Type problem
                "PtidesNetworkLatencyTest.xml", // FIXME: "Cannot set local time to -Infinity, which is earlier than the last committed current time 0.0"
                "PublisherTest", // gt
                "RealTimeComposite.xml", // FIXME: "Audio Device Unavailable"
                "RunDemos.xml", // FIXME: cannot call invokeAndWait from the event dispatcher thread
                "SerialPort.xml",
                "Signature.xml", // FIXME: Cannot read ptKeystore
                "SimpleTrafficLightSMVModule.xml", // "PedestrianLightSMV can not run in simulation mode."
                "SMVLegacyCodeActor", "SoundSpectrum.xml",
                "SynthesizedVoice.xml", "SystemCommand.xml", // Hangs.
                "SystemLevelType", "TunnelingBallDevice", "VideoCapture.xml", };
        for (int i = 0; i < skip.length; i++) {
            if (modelPath.indexOf(skip[i]) != -1) {
                return false;
            }
        }
        return true;
    }

    /** Number of models exported. */
    private static int _count = 0;
}
