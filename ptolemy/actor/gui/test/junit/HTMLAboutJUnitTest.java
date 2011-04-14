/* JUnit test the HTMLAbout mechanism

 Copyright (c) 2011 The Regents of the University of California.
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

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.FileUtilities;
import ptolemy.util.test.Diff;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.PtolemyLayoutAction;
import ptolemy.vergil.basic.layout.KielerLayoutAction;

///////////////////////////////////////////////////////////////////
//// HTMLAboutJUnitTest
/** 
 * Test out HTMLAbout by starting vergil with various URLs.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class HTMLAboutJUnitTest {

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
     *  @exception args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
            .main("ptolemy.actor.gui.test.junit.HTMLAboutJUnitTest");
    }

    /** 
     * Test the layout facility by reading in a models, stripping
     * out the graphical elements, laying out the models, comparing
     * the new results with the known good results and then doing
     * undo and redo.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void aboutConfiguration() throws Exception {
        _openModel("about:configuration");
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
        final TypedCompositeActor[] model = new TypedCompositeActor[1];
        final Throwable[] throwable = new Throwable[1];
        throwable[0] = null;
        /////
        // Open the model.
        Runnable openModelAction = new Runnable() {
            public void run() {
                try {
                    System.out.print(" " + modelFileName + " ");
                    model[0] = ConfigurationApplication.openModel(modelFileName);
                } catch (Throwable throwableCause) {
                    throwable[0] = throwableCause;
                    throw new RuntimeException(throwableCause);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        _sleep();
        if (throwable[0] != null || model[0] == null) {
            throw new Exception("Failed to open " + modelFileName
                    + throwable[0]);
        }

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            public void run() {
                try {
                    ConfigurationApplication.closeModelWithoutSavingOrExiting(model[0]);
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

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Set to true for debugging messages. */
    private final boolean _debug = false;

}
