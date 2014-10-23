/* JUnit test the HTMLAbout mechanism

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

package ptolemy.actor.gui.test.junit;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// HTMLAboutJUnitTest
/**
 * Test out HTMLAbout by starting vergil with various URLs.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class HTMLAboutJUnitTest {

    /**
     * Invoke about:allcopyrights, which pops up a window that lists
     * the copyrights.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void aboutAllCopyrights() throws Exception {
        _openModel("about:allcopyrights");
    }

    /**
     * Invoke about:configuration, which expands the actor tree on
     * the left.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void aboutConfiguration() throws Exception {
        _openModel("about:configuration");
    }

    /**
     * Invoke about:copyrights, which pops up a window that lists
     * the copyrights.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void aboutCopyrights() throws Exception {
        _openModel("about:copyrights");
    }

    /** Test the HTMLAbout
     *
     *  <p>To run, use:</p>
     *
     *  <pre>
     *   $PTII/bin/ptinvoke ptolemy.actor.gui.test.junit.HTMLAboutJUnitTest
     *  </pre>
     *  We use ptinvoke so that the classpath is set to include all the packages
     *  used by Ptolemy II.
     *
     *  @param args Ignored.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("ptolemy.actor.gui.test.junit.HTMLAboutJUnitTest");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Test HTMLAbout by opening a URL that starts with "about:".
     *
     * <p>This is the main entry point for HTMLAbout tests.</p>
     *
     * <p>The caller of this method need <b>not</b>be in the Swing
     * Event Thread.</p>
     *
     * @param modelFileName The file name of the test model.
     * @exception Exception If the file name cannot be read or laid out.
     */
    protected void _openModel(final String modelFileName) throws Exception {

        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        final CompositeEntity[] model = new CompositeEntity[1];
        final Throwable[] throwable = new Throwable[1];
        throwable[0] = null;
        /////
        // Open the model.
        Runnable openModelAction = new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.print(" " + modelFileName + " ");
                    model[0] = ConfigurationApplication
                            .openModelOrEntity(modelFileName);
                } catch (Throwable throwableCause) {
                    throwable[0] = throwableCause;
                    throw new RuntimeException(throwableCause);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        _sleep();
        if (throwable[0] != null /*|| model[0] == null*/) {
            throw new Exception("Failed to open " + modelFileName
                    + throwable[0]);
        }

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            @Override
            public void run() {
                try {
                    // FIXME: handle cases where model[0] is null.
                    if (model[0] != null) {
                        ConfigurationApplication
                        .closeModelWithoutSavingOrExiting(model[0]);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(closeAction);
        _sleep();
    }

    /** Sleep the current thread, which is usually not the Swing Event
     *  Dispatch Thread.
     */
    protected void _sleep() {
        try {
            Thread.sleep(1000);
        } catch (Throwable ex) {
            //Ignore
        }
    }
}
