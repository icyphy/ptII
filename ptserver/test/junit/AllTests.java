/* Test suite of the ptserver.

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
package ptserver.test.junit;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;
import ptolemy.util.StreamExec;

/**
 * Test suite of the ptserver.
 *
 * @author Anar Huseynov, contributor: Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (ahuseyno)
 * @Pt.AcceptedRating Red (ahuseyno)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({ TokenParserTest.class, ServletTest.class,
        RemoteModelTest.class, FileDownloadTest.class, TypeParserTest.class,
        RESTGetHandlerTest.class })
public class AllTests {

    /** Copy ptserver/PtolemyServerConfig.properties.default and start
     * the mosquitto process.
     */
    @BeforeClass
    public static void startMosquitto() throws IllegalActionException,
            IOException {
        System.out.println("AllTests.startMosquitto()");

        URL defaultConfigURL = FileUtilities.nameToURL(
                "$CLASSPATH/ptserver/PtolemyServerConfig.properties.default",
                null, null);
        File configFile = FileUtilities.nameToFile(
                "$CLASSPATH/ptserver/PtolemyServerConfig.properties", null);
        if (configFile.exists() && !configFile.delete()) {
            System.out.println("Problem deleting " + configFile);
        }
        FileUtilities.binaryCopyURLToFile(defaultConfigURL, configFile);
        System.out.println("AllTests.startMosquitto(): Copied "
                + defaultConfigURL + " to " + configFile);

        _exec = new StreamExec();
        List<String> commands = new LinkedList<String>();

        // FIXME: This probably won't work under Windows.
        commands.add("/usr/local/sbin/mosquitto");

        _exec.setCommands(commands);
        _exec.setWaitForLastSubprocess(false);
        _exec.start();
    }

    /** Stop the mosquitto process. */
    @AfterClass
    public static void stopMosquitto() {
        System.out.println("AllTests.stopMosquitto()");
        _exec.cancel();
    }

    /** Execute the mosquitto daemon. */
    private static StreamExec _exec;
}
