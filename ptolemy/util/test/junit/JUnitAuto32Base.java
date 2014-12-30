/* Run the 32-bit Java tests in auto/.

 Copyright (c) 2013 The Regents of the University of California.
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

package ptolemy.util.test.junit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

///////////////////////////////////////////////////////////////////
//// JUnitAuto32Base.
/**
 * Run the 32-bit Java tests in auto/.
 * <p>
 * Derived classes should have a method that calls super.run().
 *
 * <p>If the fileName JVM property is set, then the file named by that property is
 * sourced. Otherwise, the testDefs.tcl file is sourced and the doallTests Tcl
 * proc that is defined in $PTII/util/testsuite/testDefs.tcl is invoked and then
 * any models in the auto/ directory are invoked.</p>
 *
 * <p> For example, ptolemy.kernel.test.junit.JUnitTclTest extends this class. To
 * run one test file (Port.tcl):</p>
 *
 * <pre>
 * cd $PTII
 * java -DfileName=Port.tcl -classpath ${PTII}:${PTII}/bin/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.kernel.test.junit.JUnitAut32Base
 * </pre>
 *
 * <p>To run all the .tcl files in the directory above this directory:</p>
 *
 * <pre>
 * cd $PTII
 * java -classpath ${PTII}:${PTII}/bin/ptjacl.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.kernel.test.junit.JUnitAuto32Base
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ ptolemy.util.test.junit.Auto32Tests.class

})
public class JUnitAuto32Base {
}
