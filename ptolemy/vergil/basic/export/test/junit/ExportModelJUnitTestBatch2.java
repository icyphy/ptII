/* JUnit test that exports the second batch of demos.

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

import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;

///////////////////////////////////////////////////////////////////
//// ExportModelJUnitTestBatch2
/**
 * JUnit test that exports the demos between two indices.
 *
 * <p>To run these tests, use:
 * <pre>
 * cd $PTII
 * ./configure
 * ant test.single -Dtest.name=ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTestBatch2 -Djunit.formatter=plain
 * </pre>
 * or
 * <pre>
 * cd $PTII/ptolemy/vergil/basic/export/test/junit/;
 * export CLASSPATH=${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar;
 * export JAVAFLAGS="-Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true"
 * $PTII/bin/ptinvoke org.junit.runner.JUnitCore ptolemy.vergil.basic.export.test.junit.ExportModelJUnitTestBatch2
 * </pre>
 * <p>
 * This test uses JUnitParams from <a href="http://code.google.com/p/junitparams/#in_browser">http://code.google.com/p/junitparams/</a>,
 * which is released under <a href="http://www.apache.org/licenses/LICENSE-2.0#in_browser">Apache License 2.0</a>.
 * </p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class ExportModelJUnitTestBatch2 extends ExportModelJUnitTestBatch {

    /** Run demos 151 through 220. */
    @Override
    public Object[] demos() throws IOException {
        return super.demos(151, 220);
    }
}
