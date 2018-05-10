/* JUnit test that exports the demos.

   Copyright (c) 2018 The Regents of the University of California.
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import ptolemy.actor.Manager;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.kernel.util.KernelException;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.export.ExportModel;

///////////////////////////////////////////////////////////////////
//// ExportModelJUnitTestBatch1
/**
 * JUnit test that exports the demos between two indices.
 *
 * <p>To run these tests, use:
 * <pre>
 * cd $PTII
 * ./configure
 * ant test.single -Dtest.name=ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTestBatch1 -Djunit.formatter=plain
 * </pre>
 * or
 * <pre>
 * cd $PTII/ptolemy/vergil/basic/export/test/junit/;
 * export CLASSPATH=${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar;
 * export JAVAFLAGS="-Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true"
 * $PTII/bin/ptinvoke org.junit.runner.JUnitCore ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTestBatch1
 * </pre>
 * <p>
 * This test uses JUnitParams from <a
 * href="http://code.google.com/p/junitparams/#in_browser"http://code.google.com/p/junitparams/</a>,
 *  which is released under <a href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License 2.0</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class ExportModelJUnitTestBatch extends ExportModelJUnitTest {
    /** Export a model.
     *  @param modelPath The model to be exported. The code is exported to
     *  the directory contained by the model.
     *  @exception Throwable If there is a problem reading or exporting the model.
     */
    @Test
    @Parameters(method = "demos")
    public void RunExportModel(String modelPath) throws Throwable {
        super.RunExportModel(modelPath);
    }

    /**
     * Return a two dimensional array of arrays of strings that name the model
     * to be exported.
     *
     * Only the demos between _start and _end are returned.
     * 
     * @return a two dimension array of arrays of strings that name the
     * models to be exported.
     * @exception IOException If there is a problem accessing the directory.
     */
    public Object[] demos() throws IOException {
        System.out.println("ExportModeJUnitTestBatch: exporting models: " + _start + " to " + _end);
        Object [] allDemos = super.demos();
        Object[][] subDemos = new Object[_end - _start][1];
        System.arraycopy(allDemos,
                         _start,
                         subDemos,
                         0,
                         _end - _start);
        return subDemos;
    }

    /** The index of the first model to export as found in the array
     *  returned by ExportModelJUnitTest.demos().
     *  Derived classes should set _start in a @Before method.
     */
    protected int _start = 1;

    /** The index of the last model to export as found in the array
     *  returned by ExportModelJUnitTest.demos().
     *  Derived classes should set _end in a @Before method.
     */
    protected int _end = 50;
}
