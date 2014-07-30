/* JUnit test the Kieler Layout mechanism.

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

package ptolemy.vergil.basic.layout.kieler.test.junit;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.test.Diff;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.PtolemyLayoutAction;
import ptolemy.vergil.basic.layout.kieler.KielerLayoutAction;

///////////////////////////////////////////////////////////////////
//// KielerJUnitTest
/**
 * Test out Kieler by open models, using Kieler to layout the graph
 * and then doing undo and redo.
 *
 * <p>There are two types of tests.</p>

 * <p>1. {@link #_layoutModelCompareAgainstFile(NamedObj, String)} We
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
 * @since Ptolemy II 10.0
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
     *  @param args Not used.
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

    /** Test the layout of the ConstConstDisplay model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */
    @org.junit.Test
    public void runConstConstDisplay() throws Exception {
        _layoutTest(
                "$CLASSPATH/ptolemy/vergil/basic/layout/kieler/test/junit/models/ConstConstDisplay.xml",
                true);
    }

    /** Test the layout of the modulation model.
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
     * @param compareAgainstOriginal  If true, then run the Kieler
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
        final CompositeEntity[] model = new CompositeEntity[1];

        final Throwable[] throwable = new Throwable[1];
        throwable[0] = null;

        // The basic structure of this method is that we call
        // invokeAndWait() on operations that display graphics and
        // then sleep this thread.  This gives us a way to see the
        // model be laid out.

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
        if (throwable[0] != null || model[0] == null) {
            throw new Exception("Failed to open " + modelFileName
                    + throwable[0]);
        }
        String baseMoML = model[0].exportMoML();
        _basicGraphFrame = _getBasicGraphFrame(model[0]);

        _sleep();

        /////
        // Layout the model using either the Kieler layout mechanism
        // or the krufty Ptolemy Layout mechanism.
        Runnable layoutModelAction = new Runnable() {
            @Override
            public void run() {
                try {
                    if (compareAgainstOriginal) {
                        _layoutModelCompareAgainstFile(model[0], modelFileName);
                    } else {
                        // Invoke the crufty Ptolemy layout mechanism and export.
                        new PtolemyLayoutAction().doAction(model[0]);
                        _basicGraphFrame.report("Ptolemy Layout done");
                    }
                } catch (Throwable throwableCause) {
                    throwable[0] = throwableCause;
                    throw new RuntimeException(throwableCause);
                }
            }
        };
        SwingUtilities.invokeAndWait(layoutModelAction);
        _sleep();
        if (throwable[0] != null || model[0] == null) {
            throw new Exception("Failed to layout " + modelFileName
                    + throwable[0]);
        }

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
                @Override
                public void run() {
                    try {
                        // Invoke the Kieler layout mechanism.
                        new KielerLayoutAction().doAction(model[0]);
                    } catch (Throwable throwableCause) {
                        throwable[0] = throwableCause;
                        throw new RuntimeException(throwableCause);
                    }
                }
            };
            SwingUtilities.invokeAndWait(kielerLayoutModelAction);
            _sleep();
            if (throwable[0] != null || model[0] == null) {
                throw new Exception("Failed to layout " + modelFileName
                        + throwable[0]);
            }
        }

        /////
        // Loop through undo and redo
        String laidOutMoML = model[0].exportMoML();
        for (int i = 1; i <= 2; i++) {
            Runnable undoAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        _undo(model[0]);
                    } catch (Throwable throwableCause) {
                        throwable[0] = throwableCause;
                        throw new RuntimeException(throwableCause);
                    }
                }
            };
            SwingUtilities.invokeAndWait(undoAction);
            _sleep();
            if (throwable[0] != null || model[0] == null) {
                throw new Exception("Failed to undo " + modelFileName
                        + throwable[0]);
            }

            String undoMoML = model[0].exportMoML();
            if (_debug || !baseMoML.equals(undoMoML)) {
                System.out
                .println("Difference between original MoML"
                        + " and the exported MoML after Kieler Layout and then undo:");
                System.out.println(Diff.diff(baseMoML, undoMoML));
            }

            assertArrayEquals(baseMoML.getBytes(), undoMoML.getBytes());

            Runnable redoAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        _redo(model[0]);
                    } catch (Throwable throwableCause) {
                        throwable[0] = throwableCause;
                        throw new RuntimeException(throwableCause);
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
            if (throwable[0] != null || model[0] == null) {
                throw new Exception("Failed to redo " + modelFileName
                        + throwable[0]);
            }

        }

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            @Override
            public void run() {
                try {
                    ConfigurationApplication
                    .closeModelWithoutSavingOrExiting(model[0]);
                } catch (Throwable throwableCause) {
                    throwable[0] = throwableCause;
                    throw new RuntimeException(throwableCause);
                }
            }
        };
        SwingUtilities.invokeAndWait(closeAction);
        _sleep();
        if (throwable[0] != null || model[0] == null) {
            throw new Exception("Failed to close " + modelFileName
                    + throwable[0]);
        }
    }

    /** Lay out the model and compare the results against the original
     *  model file name.
     *
     *  <p>The caller of this method should be in the Swing Event
     *  Thread.</p>
     *
     *  @param model The model.
     *  @param modelFileName The pathname of the model, used for
     *  comparing.
     *  @exception Exception If thrown while opening or laying
     *  out the model.
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

    /** Redo the last operation on the model.
     *
     *  <p>The caller of this method should be in the Swing Event
     *  Thread.</p>
     *  @param model The model upon which the last operation
     *  should be redone.
     */
    protected void _redo(NamedObj model) {
        _basicGraphFrame.report("About to redo");
        _basicGraphFrame.redo();
        _basicGraphFrame.report("Redo done");
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
     *  @param model The model upon which the last operation
     *  should be redone.
     */
    protected void _undo(NamedObj model) {
        _basicGraphFrame.report("About to undo");
        _basicGraphFrame.undo();
        _basicGraphFrame.report("Undo done");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private static BasicGraphFrame _getBasicGraphFrame(NamedObj model) {
        // See PtolemyLayoutAction for similar code.
        BasicGraphFrame frame = null;
        Iterator tableaux = Configuration.findEffigy(model)
                .entityList(Tableau.class).iterator();
        while (tableaux.hasNext()) {
            Tableau tableau = (Tableau) tableaux.next();
            if (tableau.getFrame() instanceof BasicGraphFrame) {
                frame = (BasicGraphFrame) tableau.getFrame();
                break;
            }
        }
        // Fetch everything needed to build the LayoutTarget.
        //GraphController graphController = ((BasicGraphFrame)frame).getJGraph()
        //    .getGraphPane().getGraphController();
        //AbstractBasicGraphModel graphModel = (AbstractBasicGraphModel) ((BasicGraphFrame)frame)
        //    .getJGraph().getGraphPane().getGraphController()
        //    .getGraphModel();
        return frame;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The BasicGraphFrame of the model. */
    private BasicGraphFrame _basicGraphFrame;

    /** Set to true for debugging messages. */
    private final boolean _debug = false;

}
