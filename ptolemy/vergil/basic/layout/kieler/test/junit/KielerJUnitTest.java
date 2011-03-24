/* JUnit test the Kieler Layout mechanism.

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

package ptolemy.vergil.basic.layout.kieler.test.junit;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.FileUtilities;
import ptolemy.util.test.Diff;
import ptolemy.vergil.basic.PtolemyLayoutAction;
import ptolemy.vergil.basic.layout.KielerLayoutAction;

///////////////////////////////////////////////////////////////////
//// KielerJUnitTest
/** 
 * Test out Kieler by open models, using Kieler to layout the graph
 * and then doing undo and redo.
 *
 * <p>There are two types of tests.</p>

 * <p>1. {@link _layoutModelCompareAgainstFile(NamedObj, String)} We
 * read in a model and run the Kieler layout algorithm on on the model
 * and compare the results against the original model.  This test is
 * run on regression tests to be sure that the Kieler algorithm has
 * not changed.  Typically, the models are in the
 * ptolemy/vergil/basic/layout/kieler/test/junit/models
 * subdirectory.</p>
 *
 * <p>2. We read in a model, use the Ptolemy layouter and then the
 * Kieler layout algorithm.  We then do an undo, a redo and an undo
 * and compare.  the model against the model after the Ptolemy
 * layouter.  This test is used run on models in the Ptolemy tree to
 * make sure that the Kieler layouter and the undo/redo mechanism
 * works.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class KielerJUnitTest {

    /** Test the Kieler layout facility.
     *   
     *  <p>To run, use:</p>
     *
     *  <pre>
     *   java -classpath \
     *      $PTII:$PTII/lib/junit-4.8.2.jar:$PTII/lib/kieler.jar \
     *      ptolemy.vergil.basic.layout.kieler.test.junit.KielerJUnitTest   
     *  </pre>
     * 
     *  @exception args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
                .main("ptolemy.vergil.basic.layout.kieler.test.junit.KielerJUnitTest");
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
    public void runConstDisplay() throws Exception {
        _layoutTest(
                "$CLASSPATH/ptolemy/vergil/basic/layout/kieler/test/junit/models/ConstDisplay.xml",
                true);
    }

    /* Test the layout of the ConstConstDisplay model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runConstConstDisplay() throws Exception {
        _layoutTest(
                "$CLASSPATH/ptolemy/vergil/basic/layout/kieler/test/junit/models/ConstConstDisplay.xml",
                true);
    }

    /* Test the layout of the modulation model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runModulation() throws Exception {
        _layoutTest("$CLASSPATH/ptolemy/moml/demo/modulation.xml", false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Test the layout facility by reading in a model, laying out the
     * model, comparing the new results with the known good results
     * and then doing undo and redo.
     *
     * <p>This is the main entry point for Kieler layout tests.</p>
     *
     * <p>The caller of this method need <b>not</b>be in the Swing
     * Event Thread.</p>
     *
     * @param modelFileName The file name of the test model. 
     * @param compareAgainstOriginal.  If true, then run the Kieler
     * Layouter and compare against the original file.  If false, run
     * the Ptolemy layouter, the Kieler layouter, then undo, redo,
     * undo and compare against the output after the Ptolemy layouter.
     * @exception Exception If the file name cannot be read or laid out.
     */
    protected void _layoutTest(final String modelFileName,
            final boolean compareAgainstOriginal) throws Exception {

        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        final NamedObj[] model = new NamedObj[1];

        // The basic structure of this method is that we call
        // invokeAndWait() on operations that display graphics and
        // then sleep this thread.  This gives us a way to see the
        // model be laid out.

        /////
        // Open the model.
        Runnable openModelAction = new Runnable() {
            public void run() {
                try {
                    model[0] = _openModel(modelFileName);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        String baseMoML = model[0].exportMoML();

        _sleep();

        /////
        // Layout the model using either the Kieler layout mechanism
        // or the krufty Ptolemy Layout mechanism.
        Runnable layoutModelAction = new Runnable() {
            public void run() {
                try {
                    if (compareAgainstOriginal) {
                        _layoutModelCompareAgainstFile(model[0], modelFileName);
                    } else {
                        // Invoke the crufty Ptolemy layout mechanism and export.
                        new PtolemyLayoutAction().doAction(model[0]);
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(layoutModelAction);

        /////
        // Optionally invoke the Ptolemy layout mechanism.
        if (!compareAgainstOriginal) {
            // We just laid out the model with Ptolemy, now sleep so
            // that we see the Ptolemy layout and then layout the
            // model with Kieler.  Don't do a comparison yet.
            _sleep();
            // The "original" model is now the model laid out with 
            // the Ptoelmy mechanism.
            baseMoML = model[0].exportMoML();
            Runnable kielerLayoutModelAction = new Runnable() {
                public void run() {
                    try {
                        // Invoke the Kieler layout mechanism.
                        new KielerLayoutAction().doAction(model[0]);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(kielerLayoutModelAction);
            _sleep();
        }

        /////
        // Loop through undo and redo
        String laidOutMoML = model[0].exportMoML();
        for (int i = 1; i <= 2; i++) {
            Runnable undoAction = new Runnable() {
                public void run() {
                    try {
                        _undo(model[0]);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(undoAction);

            _sleep();

            String undoMoML = model[0].exportMoML();
            if (_debug || !baseMoML.equals(undoMoML)) {
                System.out
                        .println("Difference between original MoML"
                                + " and the exported MoML after Kieler Layout and then undo:");
                System.out.println(Diff.diff(baseMoML, undoMoML));
            }

            assertArrayEquals(baseMoML.getBytes(), undoMoML.getBytes());

            Runnable redoAction = new Runnable() {
                public void run() {
                    try {
                        _redo(model[0]);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(redoAction);
            String redoMoML = model[0].exportMoML();
            if (_debug || !laidOutMoML.equals(redoMoML)) {
                System.out
                        .println("Difference between laid out MoML"
                                + " and the exported MoML after Kieler Layout and then undo, then redo:");
                System.out.println(Diff.diff(laidOutMoML, redoMoML));
            }

            assertArrayEquals(laidOutMoML.getBytes(), redoMoML.getBytes());

            _sleep();
        }

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            public void run() {
                try {
                    if (_debug) {
                        System.out.println("About to close "
                                + model[0].getName());
                    }
                    Effigy effigy = Configuration.findEffigy(model[0]
                            .toplevel());

                    // Avoid being prompted for save.
                    effigy.setModified(false);

                    // Avoid calling System.exit().
                    System.setProperty("ptolemy.ptII.doNotExit", "true");

                    // FIXME: are all these necessary?
                    effigy.closeTableaux();
                    ((TypedCompositeActor) model[0]).setContainer(null);
                    MoMLParser.purgeAllModelRecords();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(closeAction);
        _sleep();
    }

    /** Lay out the model and compare the results against the original
     *  model file name.
     *   
     *  <p>The caller of this method should be in the Swing Event
     *  Thread.</p>
     *
     *  @param model The model, which was read by
     *  {@link openModel(String)}.
     *  @param modelFileName The pathname of the model, used for
     *  comparing.
     */
    protected void _layoutModelCompareAgainstFile(NamedObj model,
            String modelFileName) throws Exception {
        try {
            // Invoke the Kieler layout mechanism.
            new KielerLayoutAction().doAction(model);

            // Export the model and compare it with the original.
            String laidOutMoML = model.exportMoML();
            File canonicalModelFile = FileUtilities.nameToFile(modelFileName,
                    null);
            String canonicalModelFileName = canonicalModelFile
                    .getCanonicalPath();
            byte[] baseMoMLBytes = FileUtilities
                    .binaryReadURLToByteArray(canonicalModelFile.toURI()
                            .toURL());

            if (_debug || !new String(baseMoMLBytes).equals(laidOutMoML)) {
                System.out.println("Difference between "
                        + canonicalModelFileName
                        + " and the exported MoML after Kieler Layout:");
                System.out.println(Diff.diff(new String(baseMoMLBytes),
                        laidOutMoML));
            }
            assertArrayEquals(laidOutMoML.getBytes(), baseMoMLBytes);

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /** Open a model and display it.
     *   
     *  <p>The caller of this method should be in the Swing Event Thread.</p>
     *
     *  @param modelFileName The pathname to the model.  Usually the
     *  pathname starts with "$CLASSPATH".
     */
    protected NamedObj _openModel(String modelFileName) {
        NamedObj model = null;
        try {
            // We set the list of MoMLFilters to handle Backward Compatibility.
            MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters());

            // Give the developer feedback about what model is being opened.
            System.out.print(modelFileName + " ");

            // Convert the file name to a canonical file name so that
            // this test may be run from any directory or from within Eclipse.
            File canonicalModelFile = FileUtilities.nameToFile(modelFileName,
                    null);
            String canonicalModelFileName = canonicalModelFile
                    .getCanonicalPath();

            // FIXME: are we in the right thread?
            ConfigurationApplication application = new ConfigurationApplication(
                    new String[] {
                            // Need to display a frame or Kieler fails.
                            //"ptolemy/actor/gui/test/testConfiguration.xml",
                            "ptolemy/configs/full/configuration.xml",
                            canonicalModelFileName });

            // Find the first TypedCompositeActor, skipping the
            // Configuration etc.
            StringBuffer names = new StringBuffer();
            Iterator models = application.models().iterator();
            while (models.hasNext()) {
                model = (NamedObj) models.next();
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(model.getFullName());
                if (model instanceof TypedCompositeActor) {
                    if (_debug) {
                        System.out.println("openModel(" + modelFileName + ")\n"
                                + model.getName());
                    }
                    break;
                }
            }
            if (!(model instanceof TypedCompositeActor)) {
                throw new Exception(
                        "Failed to find a TypedComposite.  Models were: "
                                + names);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return model;
    }

    /** Redo the last operation on the model.
     *   
     *  <p>The caller of this method should be in the Swing Event
     *  Thread.</p>
     */
    protected void _redo(NamedObj model) {
        try {
            Manager manager = new Manager(model.workspace(), "KJUT");
            // Invoke redo and compare against the Kieler layout.
            RedoChangeRequest redo = new RedoChangeRequest(model, model);
            manager.requestChange(redo);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
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

    /** Undo the last operation on the model.
     *   
     *  <p>The caller of this method should be in the Swing Event
     *  Thread.</p>
     */
    protected void _undo(NamedObj model) {
        try {
            Manager manager = new Manager(model.workspace(), "KJUT");
            // Invoke undo and compare against the Ptolemy layout.
            // See ptolemy/moml/test/UndoEntity.tcl
            UndoChangeRequest undo = new UndoChangeRequest(model, model);
            manager.requestChange(undo);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Set to true for debugging messages. */
    private final boolean _debug = false;
}
