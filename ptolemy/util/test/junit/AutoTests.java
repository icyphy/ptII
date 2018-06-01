/* Run the Ptolemy model tests in the auto/ directory using JUnit.

   Copyright (c) 2011-2016 The Regents of the University of California.
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

import java.lang.reflect.Method;

import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import ptolemy.moml.MoMLSimpleApplication;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// AutoTests
/**
 * Run the Ptolemy model tests in the auto/ directory using JUnit.
 *
 * <p>
 * This test must be run from the directory that contains the auto/ directory,
 * for example:
 * </p>
 *
 * <pre>
 * (cd ~/ptII/ptolemy/actor/lib/io/test; java -classpath ${PTII}:${PTII}/lib/junit-4.8.2.jar:${PTII}/lib/JUnitParams-0.3.0.jar org.junit.runner.JUnitCore ptolemy.util.test.junit.AutoTests)
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
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
@RunWith(JUnitParamsRunner.class)
public class AutoTests extends ModelTests {

    /** If the VertxHelperBase class is present, then invoke the
     *  closeVertx() method so that this process does not wait around
     *  for the Vert.x threads.
     */
    @AfterClass
    public static void afterClass() {
        try {
            Class clazz = Class
                .forName("ptolemy.actor.lib.jjs.VertxHelperBase");
            if (clazz != null) {
                Method method = clazz.getMethod("closeVertx");
		System.out.println("AutoTests.java: About to close Vertx.");
                method.invoke(null);
		System.out.println("AutoTests.java: Vertx closed.");
            }
        } catch (NoClassDefFoundError ex) {
            // Ignore this, it means that MoMLSimpleApplication was invoked without the Vert.x jar files.
        } catch (Throwable throwable) {
            System.err.println(
                               "AutoTests: Failed to invoke VertxHelperBase.closeVertx() during exit.  This can be ignored. Error was: "
                               + throwable);
        }

    }

    /** If the fullPath is a hlacerti or accessor demo, then
     *  delay so that the system can stabilize.
     *  @param fullPath The forward slash separated path of the demo.
     */
    public static void delayIfNecessary(String fullPath) {
        int delay = 5000;
        boolean match = fullPath.matches(".*(org/hlacerti|org/terraswarm/accessors|ptolemy/actor/lib/jjs).*");
        // System.out.println("AutoTests.java: fullPath: " + fullPath
        //                   + " match: " + match);
        if (match) {
            System.out.println("----------------- " + (new java.util.Date())
                               + " About to sleep for " + delay / 1000.0
                               + " seconds before running or rerunning.  Test is: " + fullPath);
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                System.err.println(
                                   "Sleep before rerunning was interrupted: "
                                   + ex);
            }
            System.out.println("Done sleeping");
        }
    }

    /**
     * Execute a model and time out after 900000 ms.
     *
     * <p>To check the time, go to the list of tests for AutoTest, for example
     * <a href="http://sisyphus.eecs.berkeley.edu:8079/hudson/job/ptII/724/testReport/ptolemy.util.test.junit/AutoTests/"><code>http://sisyphus.eecs.berkeley.edu:8079/hudson/job/ptII/724/testReport/ptolemy.util.test.junit/AutoTests/</code></a>,
     * where 724 is a recent run of the build.  Look for thhe longest
     * passing test, typically Trilateration, and round up by a factor
     * of 1.5.</p>
     *
     * @param fullPath
     *            The full path to the model file to be executed. If the
     *            fullPath ends with the value of the
     *            {@link #THERE_ARE_NO_AUTO_TESTS}, then the method returns
     *            immediately.
     * @exception Throwable
     *                If thrown while executing the model.
     */
    @Test(timeout = 900000)
    @Parameters(method = "modelValues")
    public void RunModel(String fullPath) throws Throwable {
        if (fullPath.endsWith(THERE_ARE_NO_AUTO_TESTS)) {
            System.out.println(
                               "No auto/*.xml tests in " + System.getProperty("user.dir"));
            return;
        }
        if (fullPath.endsWith(THERE_ARE_NO_AUTO_ARCH_TESTS)) {
            System.out.println(
                               "No " + AutoNameArchTests.autoNameArch()
                               + "*.xml tests in " + System.getProperty("user.dir"));
            return;
        }

        // Only check for the JSAccessor class if there are auto tests.
        // MoMLParser.tcl had a test that was reporting different
        // results between running using ptjacl and JUnit because
        // JUnit was always instantiating a JSAccessor, even
        // if there were no auto tests.  This was causing
        // "StreamChangeRequest.changeExecuted(): AccessorIconLoader succeeded"
        // to appear in the listener.
        if (!AutoTests._haveCheckedForJSAccessor) {
            AutoTests._haveCheckedForJSAccessor = true;
            AutoTests._checkForJSAccessor();
        }
        // Use a whole row of equals to signify the start of a new test.
        System.out.println("\n===========================================================================================");
        if (modelFileIsOK(fullPath)) {

            System.out.println("----------------- testing " + (new java.util.Date()) + " " + fullPath);
            System.out.flush();
            if (_applicationConstructor == null) {
                // Delay instantiating MoMLSimpleApplication so that we
                // can run the kernel tests without requiring moml
                _applicationClass = Class
                    .forName("ptolemy.moml.MoMLSimpleApplication");
                _applicationConstructor = _applicationClass
                    .getConstructor(String.class);
            }

            // _applicationConstructor might have been initialized in
            // AutoKnownFailedTests, so we initialize
            // _applicationToplevelMethod here.
            if (_applicationToplevelMethod == null) {
                _applicationToplevelMethod = _applicationClass
                    .getMethod("toplevel", new Class[] {});
            }

            // If a model is in various directories, including
            // org/terraswarm/accessors/test, the delay before
            // reloading.  See
            // https://chess.eecs.berkeley.edu/ptexternal/wiki/Main/WebSocketDeadlock#Starvation
            AutoTests.delayIfNecessary(fullPath);

            System.out.println("----------------- Instantiating " + fullPath);
            Object instance = _applicationConstructor.newInstance(fullPath);
            Method rerunMethod = _applicationClass.getMethod("rerun",
                                                             (Class<?>[]) null);

            // If JSAccessor is present and the model contains one, then
            // reload all the JSAccessors and rerun the model

            if (_jsAccessorClass == null) {
                // JsAccessor is not present, just rerun.
                System.out.println("----------------- testing again " + fullPath);
                System.out.flush();
                rerunMethod.invoke(instance, (Object[]) null);

            } else {
                // JSAccessor is present, reload and rerun.
                
                System.out.println("----------------- Invoking toplevel() on " + instance);
                _applicationToplevelMethod = _applicationClass
                    .getMethod("toplevel", new Class[] {});
                Object toplevel = _applicationToplevelMethod.invoke(instance,
                                                                    (Object[]) null);
                System.out.println("----------------- Done invoking toplevel() on " + instance);

                if (_jsAccessorReloadAllAccessorsMethod == null) {
                    throw new InternalError(
                                            "Found the JSAccessor class, but not the reloadAllAccessors() method?");
                }
                // Reload all the accessors and invoke rerun.
                if (((Boolean) _jsAccessorReloadAllAccessorsMethod.invoke(null,
                                                                          new Object[] { toplevel })).booleanValue()) {
                    System.out.println("-------------- Reloaded accessors, but skipping rerun for now. "
                                       + (new java.util.Date()) + " "
                                       + fullPath);
                    System.out.flush();
                    // Autotests.delayIfNecessary(fullPath);

                    // System.out.println(
                    //         "----------------- Reloaded Accessors and testing again " + (new java.util.Date()) + " "
                    //                 + fullPath);
                    // System.out.flush();
                    // rerunMethod.invoke(instance, (Object[]) null);
                }
            }
        }
    }

    /** Return true if the model should be run.
     *  This is a hack to avoid a problem where certain models
     *  interact badly with the Cobertura code coverage tool
     *  or with Travis.
     *  @param fullPath The full path of the model to be executed
     *  @return true if the model should be run.
     */
    public boolean modelFileIsOK(String fullPath) {
        if (fullPath.endsWith("de/test/auto/ThreadedComposite.xml")
            && !StringUtilities
            .getProperty("net.sourceforge.cobertura.datafile")
            .equals("")) {
            System.err.println("----------------- *** Skipping testing of " + fullPath
                               + " because it interacts badly with Cobertura.");
            System.err.flush();
            return false;
        }

        // Under Travis, skip certain demos. To see what environment
        // variables are set by Travis, see
        // https://docs.travis-ci.com/user/environment-variables/
        String travis = System.getenv("TRAVIS");
        if (travis != null && travis.equals("true")) {
            String [] travisSkip = {
                "org/hlacerti/test/auto",
                "ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTGet.xml",
                "ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTPostDataTypes.xml",
                "ptolemy/actor/lib/jjs/modules/httpClient/test/auto/RESTSendImage.xml",
                "ptolemy/actor/lib/jjs/modules/httpServer/test/auto/KeyValueStoreClient.xml",
                "ptolemy/actor/lib/jjs/modules/httpServer/test/auto/WebServerBasic.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/Message3.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/Message4.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/Socket1.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/Socket2.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/Socket3.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketByte.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketDoubleArray.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketFloat.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketInt.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketInt.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketShort.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketStringArray.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/SocketTypicalUsage.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/TCPSocketDoubleArrayBatched.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/TCPSocketSecureServerClient.xml",
                "ptolemy/actor/lib/jjs/modules/socket/test/auto/TCPSocketUnsignedShort.xml",
                "ptolemy/actor/lib/jjs/modules/udpSocket/test/auto/UDPSocketInt.xml",
                "ptolemy/actor/lib/jjs/modules/udpSocket/test/auto/UDPSocketSelf.xml",
                "ptolemy/actor/lib/jjs/modules/udpSocket/test/auto/UDPSocketString.xml",
                "ptolemy/actor/lib/jjs/modules/webSocket/test/auto/FullDuplex2.xml",
                "ptolemy/actor/lib/jjs/modules/webSocket/test/auto/WebSocketClient2JS.xml",
                "ptolemy/actor/lib/jjs/modules/webSocket/test/auto/WebSocketClientJS.xml",
                "ptolemy/cg/lib/testKnownFailed/test/auto/knownFailedTests/ScaleC.xml"
            };
            for (String element : travisSkip) {
                if (fullPath.indexOf(element) != -1) {
                    System.err.println("----------------- *** Skipping testing of " + fullPath
                                       + " because it does fails under Travis.  "
                                       + "To updated this list, edit ptolemy/util/test/junit/AutoTests.java");
                    System.err.flush();
                    return false;
                }
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** The org.terraswarm.accessor.JSAccessor class, which is tested
     * by reloading Accessors.
     */
    protected static Class<?> _jsAccessorClass = null;

    /** The method that reloads all the accessors in a CompositeEntity. */
    protected static Method _jsAccessorReloadAllAccessorsMethod = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private static void _checkForJSAccessor() {
        try {
            _jsAccessorClass = Class
                .forName("org.terraswarm.accessor.JSAccessor");
            Class compositeEntityClass = Class
                .forName("ptolemy.kernel.CompositeEntity");
            _jsAccessorReloadAllAccessorsMethod = _jsAccessorClass.getMethod(
                                                                             "reloadAllAccessors", new Class[] { compositeEntityClass });
        } catch (Throwable throwable) {
            // Ignore this, it could be that the JSAccessor class
            // is not present.
            _jsAccessorClass = null;
            _jsAccessorReloadAllAccessorsMethod = null;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** True if we have checked for JSAccessor. */
    private static boolean _haveCheckedForJSAccessor = false;
}
