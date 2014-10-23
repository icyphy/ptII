/* JUnit tests for BasicGraphFrame ExportImageAction

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

package ptolemy.vergil.basic.test.junit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.lib.image.ImageReader;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.vergil.basic.BasicGraphFrame;

///////////////////////////////////////////////////////////////////
//// ExportImageJUnitTest
/**
 * Test out the Export Image facility, which saves as gif, png, pdf etc.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportImageJUnitTest {

    /** Test the ExportImage facility.
     *
     *  <p>To run, use:</p>
     *
     *  <pre>
     *   java -classpath \
     *      $PTII:$PTII/lib/junit-4.8.2.jar: \
     *      ptolemy.vergil.basic.test.junit.ExportImageJUnitTest
     *  </pre>
     *
     *  @param args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore
        .main("ptolemy.vergil.basic.test.junit.ExportImageJUnitTest");
    }

    /**
     * Test the layout facility by reading in a models, stripping
     * out the graphical elements, laying out the models, comparing
     * the new results with the known good results and then doing
     * undo and redo.
     * @exception Throwable If there is a problem reading or exporting
     * the image.
     */
    @org.junit.Test
    public void runModulation() throws Throwable {
        _exportImageTest("$CLASSPATH/ptolemy/moml/demo/modulation.xml");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Test the Export Image facility by saving images and reading
     * them back in.
     *
     * <p>This is the main entry point for Export Image tests.</p>
     *
     * <p>The caller of this method need <b>not</b>be in the Swing
     * Event Thread.</p>
     *
     * @param modelFileName The file name of the test model.
     * @exception Throwable If the file name cannot be read or laid out.
     */
    protected void _exportImageTest(final String modelFileName)
            throws Throwable {

        // Give the developer feedback about what model is being opened.
        System.out.print(modelFileName + " ");

        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        final CompositeEntity[] model = new CompositeEntity[1];

        /////
        // Open the model.
        // FIXME: Refactor this and KielerLayoutJUnitTest to a common class.
        Runnable openModelAction = new Runnable() {
            @Override
            public void run() {
                try {
                    model[0] = ConfigurationApplication
                            .openModelOrEntity(modelFileName);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        _sleep();

        _basicGraphFrame = BasicGraphFrame.getBasicGraphFrame(model[0]);

        // Open a model that we will use to display the image.

        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        final CompositeEntity[] imageDisplayModel = new CompositeEntity[1];

        // FIXME: Refactor this and KielerLayoutJUnitTest to a common class.
        Runnable openImageDisplayModelAction = new Runnable() {
            @Override
            public void run() {
                try {
                    imageDisplayModel[0] = ConfigurationApplication
                            .openModelOrEntity("$CLASSPATH/ptolemy/actor/lib/image/test/auto/ImageReaderImageDisplay.xml");
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        };
        SwingUtilities.invokeAndWait(openImageDisplayModelAction);
        _sleep();

        // Export images
        // FIXME: we should get the list of acceptable format names from
        // BasicGraphFrame
        String[] formatNames = new String[] { "GIF", "PNG" };
        for (final String formatName : formatNames) {
            Runnable exportImageAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.print(" " + formatName + " ");
                        File imageFile = File.createTempFile(
                                model[0].getName(),
                                formatName.toLowerCase(Locale.getDefault()));
                        imageFile.deleteOnExit();
                        OutputStream out = null;
                        try {
                            out = new FileOutputStream(imageFile);
                            // Export the image.
                            _basicGraphFrame.getJGraph().exportImage(out,
                                    formatName);
                        } finally {
                            if (out != null) {
                                try {
                                    out.close();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        // Run a model that displays the image.
                        ImageReader imageReader = (ImageReader) imageDisplayModel[0]
                                .getEntity("ImageReader");

                        imageReader.fileOrURL.setExpression(imageFile.toURI()
                                .toURL().toString());
                        if (!(model[0] instanceof TypedCompositeActor)) {
                            throw new RuntimeException(model[0].getFullName()
                                    + "is not a TypedCompositeActor? "
                                    + model[0].getClass());
                        } else {
                            Manager manager = ((TypedCompositeActor) model[0])
                                    .getManager();
                            if (manager == null) {
                                manager = new Manager(
                                        imageDisplayModel[0].workspace(),
                                        "MyManager");

                            }
                            if (!(imageDisplayModel[0] instanceof TypedCompositeActor)) {
                                throw new RuntimeException(
                                        imageDisplayModel[0].getFullName()
                                                + "is not a TypedCompositeActor? "
                                                + imageDisplayModel[0]
                                                        .getClass());
                            } else {
                                ((TypedCompositeActor) imageDisplayModel[0])
                                        .setManager(manager);
                                ((TypedCompositeActor) imageDisplayModel[0])
                                        .setModelErrorHandler(new BasicModelErrorHandler());
                                manager.execute();
                            }
                        }

                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        throw new RuntimeException(throwable);
                    }
                }
            };
            SwingUtilities.invokeAndWait(exportImageAction);
            _sleep();
        }

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            @Override
            public void run() {
                try {
                    ConfigurationApplication
                    .closeModelWithoutSavingOrExiting(model[0]);
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

    /** The BasicGraphFrame of the model. */
    private BasicGraphFrame _basicGraphFrame;
}
