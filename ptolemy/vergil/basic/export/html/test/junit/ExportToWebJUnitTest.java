/* JUnit test the ExportToWeb mechanism.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html.test.junit;

import java.io.File;

import ptolemy.util.FileUtilities;
import ptolemy.vergil.basic.export.ExportModel;

///////////////////////////////////////////////////////////////////
//// ExportToWebUnitTest
/**
 * Test out export to web facility.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportToWebJUnitTest {

    /** Test the ExportToWeb facility.
     *
     *  <p>To run, use:</p>
     *
     *  <pre>
     *   java -classpath \
     *      $PTII:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar \
     *      ptolemy.vergil.basic.export.html.test.junit.ExportToWebJUnitTest
     *  </pre>
     *
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
                .main("ptolemy.vergil.basic.export.html.test.junit.ExportToWebJUnitTest");
    }

    /** Test the export the Butterfly model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void exportButterfly() throws Exception {
        _exportToWebTest(
                "$CLASSPATH/ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml",
                "Butterfly");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Test the ExportToWeb facility by reading in a model, and exporting it.
     *
     * <p>This is the main entry point for ExportToWeb tests.</p>
     *
     * <p>The caller of this method need <b>not</b>be in the Swing
     * Event Thread.</p>
     *
     * @param modelFileName The file name of the test model.
     * @param directoryName The name of the directory.
     * @exception Exception If the file name cannot be read or exported
     */
    protected void _exportToWebTest(final String modelFileName,
            final String directoryName) throws Exception {
        FileUtilities.deleteDirectory(new File(directoryName));
        String args[] = new String[3];
        args[0] = "-run";
        args[1] = modelFileName;
        args[2] = directoryName;
        // Export.main(args) calls System.exit() unless we set this property.
        System.setProperty("ptolemy.ptII.doNotExit", "true");
        ExportModel.main(args);
        FileUtilities.deleteDirectory(new File(directoryName));
    }
}
