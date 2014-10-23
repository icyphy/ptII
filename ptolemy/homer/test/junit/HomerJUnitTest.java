/* JUnit test for homer

 Copyright (c) 2013-2014 The Regents of the University of California.
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

package ptolemy.homer.test.junit;

import java.io.File;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.homer.HomerApplication;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// HomerJUnitTest
/**
 * Run homer as a junit test.
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Green (cxh)
 * @Pt.AcceptedRating Green (cxh)
 */
public class HomerJUnitTest {

    /**
     * Instantiate the HomerApplication.
     * @exception Throwable If there is a problem instantiating the HomerApplication.
     */
    @org.junit.Test
    public void run() throws Throwable {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    HomerApplication homerApplication = new HomerApplication(
                            new String[0]);
                    // See ptserver/PtolemyServerConfig.properties.  Typically ptserver/demo
                    String modelsDirectory = StringUtilities
                            .getProperty("ptolemy.ptII.dir")
                            + "/"
                            + ResourceBundle.getBundle(
                                    "ptserver.PtolemyServerConfig").getString(
                                            "MODELS_DIRECTORY");
                    File modelFile = new File(modelsDirectory
                            + "/SoundSpectrum_demo.layout.xml");
                    File layoutFile = new File(modelsDirectory
                            + "/SoundSpectrum_demo.layout.xml");
                    homerApplication.getHomerMainFrame().openLayout(
                            modelFile.toURI().toURL(),
                            layoutFile.toURI().toURL());

                    Iterator models = homerApplication.models().iterator();
                    while (models.hasNext()) {
                        NamedObj model = (NamedObj) models.next();
                        if (model instanceof CompositeEntity) {
                            ConfigurationApplication
                            .closeModelWithoutSavingOrExiting((CompositeEntity) model);
                        }
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
        Thread.currentThread();
        Thread.yield();
        try {
            System.out.println("Start sleeping");
            Thread.currentThread();
            Thread.sleep(5000);
            System.out.println("Done sleeping");
        } catch (InterruptedException ex) {
            System.err.println("HomerJUnitTest interrupted while sleeping?");
        }
    }

    /** Run the test that creates the example system.
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("ptolemy.homer.test.junit.HomerJUnitTest");
    }
}
