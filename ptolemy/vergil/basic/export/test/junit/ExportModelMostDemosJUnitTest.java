/* JUnit test that exports most of the demos.

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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.export.ExportModel;

///////////////////////////////////////////////////////////////////
//// ExportModelMostDemosJUnitTest
/**
 * JUnit test that exports most of the demos.
 *
 * <p>To run these tests, use:
 * <pre>
 * (cd $PTII/ptolemy/vergil/basic/export/test/junit/; java -Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true -classpath ${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.vergil.basic.export.test.junit.ExportModeMostDemosJUnitTest)
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
public class ExportModelMostDemosJUnitTest {
    /** Export a model
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

        String modelDirectory = modelPath.substring(0, modelPath.lastIndexOf("/"));        
	// A directory inside the current directory that contains the model because
	// we remove the contents of the outputDirectory with the force parameter.
        String outputDirectory = ptolemyPtIIDir + "/" + modelDirectory + "/" + modelName;

        System.out.println("####### $PTII/bin/ptinvoke ptolemy.vergil.basic.export.ExportModel -force htm -run -openComposites -whiteBackground " + modelPath + " " + outputDirectory);
        ExportModel exportModel = new ExportModel();
        exportModel.exportModel(false /* copyJavaScriptFiles */,
                true /* force */,
                "htm",
                fullModelPath,
                true /* run */,
                true /* open composites */,
                false /* open results */,
                outputDirectory,
                false /* save */,
                true /* whitebackground */);
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
        DataInputStream dataInputStream  = null;
        BufferedReader reader = null;
        List<String> demos = new LinkedList<String>();
        try {
            fileInputStream = new FileInputStream(modelsFile);
            dataInputStream = new DataInputStream(fileInputStream);
            reader = new BufferedReader(new InputStreamReader(dataInputStream));
            String line;
            while ((line = reader.readLine()) != null)   {
                // Lines in models.txt look like
                // $CLASSPATH/lbnl/demo/CRoom/CRoom.xml
                String prefix = "$CLASSPATH/";
                if (! line.startsWith(prefix)) {
                    throw new IOException("Line in \"" + modelsFile
                            + "\" does not start with \"" + prefix
                            + "\".  Line was:\n" + line);
                }
                String modelPath = line.substring(prefix.length());
                String modelFile = modelPath.substring(modelPath.lastIndexOf("/") + 1);
                String modelName = modelFile.substring(0, modelFile.lastIndexOf("."));
                if (_demoIsOk(modelPath)) {
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

    /** Return true if we should export the demo.
     */   
    private boolean _demoIsOk(String modelPath) {
        // Pathnames that should be skipped
        String [] skip = {
            "/cg/", "/codegen/", "/ExecDemos/", "lbnl/", "/matlab/",
            "SMVLegacyCodeActor", "/SystemLevelType/", "/taskpt/",
            "/verification/"
        };
        for (int i = 0; i < skip.length; i++) {
            if (modelPath.indexOf(skip[i]) != -1) {
                return false;
            }
        }
        return true;
    }
}
