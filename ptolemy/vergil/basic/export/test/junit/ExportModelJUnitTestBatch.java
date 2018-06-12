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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

///////////////////////////////////////////////////////////////////
//// ExportModelJUnitTestBatch
/**
 * JUnit test that exports the demos between two indices.
 *
 * Derived classes should override the demos() method and invoke
 * super.demos(N,M);, where N and M are the start and end indices
 * of the models to be exported.
 *
 * <p>To run the first batch of models using the baseclass:
 * <pre>
 * cd $PTII
 * ./configure
 * ant test.single -Dtest.name=ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTestBatch -Djunit.formatter=plain
 * </pre>
 * or
 * <pre>
 * cd $PTII/ptolemy/vergil/basic/export/test/junit/;
 * export CLASSPATH=${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar;
 * export JAVAFLAGS="-Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true"
 * $PTII/bin/ptinvoke org.junit.runner.JUnitCore ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTestBatch
 * </pre>
 * <p>
 * This test uses JUnitParams from <a
 * href="http://code.google.com/p/junitparams/#in_browser"http://code.google.com/p/junitparams/</a>,
 *  which is released under <a href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License 2.0</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
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
    @Override
    @Test
    @Parameters(method = "demos")
    public void RunExportModel(String modelPath) throws Throwable {
        super.RunExportModel(modelPath);
    }

    /**
     * Return a two dimensional array of arrays of strings that name the
     * first 51 models to be exported.
     *
     * Derived classes should override this method with the
     * range of demos to be exported.
     *
     * @return a two dimension array of arrays of strings that name the
     * models to be exported.
     * @exception IOException If there is a problem accessing the directory.
     */
    @Override
    public Object[] demos() throws IOException {
        return demos(0, 50);
    }

    /** Return a two dimensional array of strings that name the models to
     *  to be exported.
     *  @param start The 0-based index of the first model in
     *  $PTII/ptolemy/configs/doc/models.txt
     *  @param end The index of the last model.
     * @return a two dimension array of arrays of strings that name the
     * models to be exported.
     * @exception IOException If there is a problem accessing the directory.
     */
    public Object[] demos(int start, int end) throws IOException {
        if (start < 0 || end < 0) {
            throw new IllegalArgumentException("The start indice (" + start
                    + ") or the end indice (" + end + ") is less than zero.");
        }
        if (start >= end) {
            throw new IllegalArgumentException("The start indice (" + start
                    + ") must be greater than the end indice (" + end + ")");
        }
        Object[] allDemos = super.demos();
        System.out.println("ExportModeJUnitTestBatch: There are "
                + allDemos.length + " demos. Exporting demos: " + start + " to "
                + end + ".");

        if (allDemos.length < start) {
            System.out.println(
                    "ExportModeJUnitTestBatch: There are " + allDemos.length
                            + " demos, which is less than the start index "
                            + start + ". Returning an empty array of demos.");
            return new Object[0][1];
        }
        if (allDemos.length < end) {
            end = allDemos.length - 1;
            System.out.println(
                    "ExportModeJUnitTestBatch: There are " + allDemos.length
                            + " demos, which is less than the end index "
                            + start + ". Setting the end index to " + end);
        }
        Object[][] subDemos = new Object[end - start + 1][1];
        System.arraycopy(allDemos, start, subDemos, 0, end - start + 1);
        return subDemos;
    }
}
