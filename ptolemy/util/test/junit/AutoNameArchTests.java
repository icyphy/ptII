/* Run the Ptolemy model tests in the auto/os.name-os.arch directory using JUnit.

   Copyright (c) 2016 The Regents of the University of California.
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

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

///////////////////////////////////////////////////////////////////
//// AutoNameArchTests
/**
 * Run the Ptolemy model tests in the auto/os.name-os.arch directory
 * using JUnit.
 *
 * <p>The name is based on the value of the os.name Java property, but
 * spaces have been removed and the results converted to lower case.
 * The value of the os.arch property is not modified.  Valid directory
 * names are macosx-x86_64 and linux-amd64.</p>
 *
 * <p>
 * This test must be run from the directory that contains the auto/os.name directory,
 * for example:
 * </p>
 *
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/fmi/fmus/jmodelica/test; java -classpath ${PTII}:${PTII}/lib/jna-4.0.0-variadic.jar:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.AutoNameArchTests)
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
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class AutoNameArchTests extends AutoTests {
    /**
     * Execute a model and time out after 900000 ms.
     *
     * @param fullPath
     *            The full path to the model file to be executed. If the
     *            fullPath ends with the value of the
     *            {@link #THERE_ARE_NO_AUTO_TESTS}, then the method returns
     *            immediately.
     * @exception Throwable
     *                If thrown while executing the model.
     */
    @Override
    @Test(timeout = 900000)
    @Parameters(method = "modelValues")
    public void RunModel(String fullPath) throws Throwable {
        // This method is present so that we can run just these tests
        // from the command line.  See the class comment for the command line.
        super.RunModel(fullPath);
    }

    /**
     * Return a two dimensional array of arrays of strings that name the model
     * to be executed. If auto/ does not exist, or does not contain files that
     * end with .xml or .moml, return a list with one element that contains
     * the value of the THERE_ARE_NO_AUTO_TESTS variable.
     *
     * @return The List of model names in auto/
     * @exception IOException If there is a problem accessing the auto/ directory.
     */
    @Override
    public Object[] modelValues() throws IOException {

        // Unfortunately, how architectures are specified under Java
        // is a bit messed up.

        //  Under Darwin, the os.arch property is ok, it is set to
        //  x86_64

        //  The os.name property is set to "Mac OS X", which is not a
        //  good directory name.

        //  We could use the ptolemy.ptII.jni.architecture property,
        //  which is set by configure, but configure is not always
        //  run.

        //  So, why don't we use the name "auto/os.name-os.arch/"
        //  where os.name is the value of the os.name property
        //  downcased and with spaces removed and the os.arch is the
        //  value of os.arch.

        // Thus, for RHEL, we would use auto/linux-amd64/

        // For Darwin: auto/macosx-x86_64/

        // An alternative would be to use the fmi architecture names
        // like linux64 and darwin64, but these names do not have much
        // to do with Java and I'm guessing are going to change.

        //  One problem is that RHEL has an old version of GLIBC,
        //  which means that recent Ubuntu fmu binaries will not run
        //  on the nightly build machine. The workaround for this is
        //  to build the fmus on the nightly build machine. Upgrading
        //  the nightly build machine to Ubuntu is out of scope.

        // The FMI standard should have used with the GNU triplet
        // architecture names or the Debian multiarch names
        // (https://wiki.debian.org/Multiarch/Tuples)

        // So, linux64 is not sufficient to differentiate between RHEL
        // and Ubuntu. A better version number would include the
        // version of GLIBC.
        
        return modelValues(AutoNameArchTests.autoNameArch(), THERE_ARE_NO_AUTO_ARCH_TESTS);
    }

    /** Return the auto directory for the current architecture, for example
     *  "macosx-x86_64/".
     *  @return the auto directory for the current architecture.   
     */   
    public static String autoNameArch() {
        String osName = System.getProperty("os.name").replaceAll("\\s", "")
                .toLowerCase();
        String osArch = System.getProperty("os.arch");
        return "auto/" + osName + "-" + osArch + "/";
    }

}
