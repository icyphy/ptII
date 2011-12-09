/* Export a model as an image or set of html files.

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

package ptolemy.vergil.basic.export.image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame;

///////////////////////////////////////////////////////////////////
//// ExportImage
/**
 * Export a model as an image or set of html files.
 *
 * <p>The default is to export a .gif file with the same name as the model.
 * See {@link #main(String[])} for usage.</p>
 * 
 * <p> See <a href="http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport">http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport</a>
 * for detailed instructions about how to create web pages on the
 * Ptolemy website for models.</p>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportImage {
    /** Export an image of a model to a file or directory.
     *  The image is written to a file or directory with the same name as the model.
     *  If formatName starts with "HTM" or "htm", then a directory with the
     *  same name as the basename of the model is created.
     *  If the formatName is "GIF", "gif", "PNG" or "png", then a file
     *  with the same basename as the basename of the model is created.
     *
     *  @param formatName The file format of the file to be generated.
     *  One of "GIF", "gif", "HTM", "htm", "PNG", "png".
     *  @param modelFileName A Ptolemy model in MoML format.
     *  The string may start with $CLASSPATH, $HOME or other formats
     *  suitable for {@link ptolemy.util.FileUtilities#nameToFile(String, URI)}.
     *  @param run True if the model should be run first.  If <i>run</i>
     *  is true, and if <i>formatName</i> starts with "htm" or "HTM", then
     *  the output will include images of any plots.
     *  @param openComposites True if the CompositeEntites should be
     *  open.  The <i>openComposites</i> parameter only has an effect
     *  if <i>formatName</i> starts with "htm" or "HTM".
     *  @param save True if the model should be saved after being run.
     *  @param whiteBackground True if the model background should be set to white.
     *  @exception Exception Thrown if there is a problem reading the model
     *  or exporting the image.
     */
    public void exportImage(final String formatName,
            final String modelFileName, final boolean run, final boolean openComposites,
            final boolean save, final boolean whiteBackground) throws Exception {
        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        // Perhaps we should make this a field?
        final TypedCompositeActor[] model = new TypedCompositeActor[1];

        /////
        // Open the model.
        // FIXME: Refactor this and KielerLayoutJUnitTest to a common class.
        Runnable openModelAction = new Runnable() {
            public void run() {
                try {
                    model[0] = ConfigurationApplication
                            .openModel(modelFileName);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    throw new RuntimeException(throwable);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        _sleep();

        _basicGraphFrame = BasicGraphFrame.getBasicGraphFrame(model[0]);

        if (run) {
            // Optionally run the model.
            Runnable runAction = new Runnable() {
                public void run() {
                    try {
                        System.out.println("Running " + model[0].getFullName());
                        Manager manager = model[0].getManager();
                        if (manager == null) {
                            manager = new Manager(model[0].workspace(),
                                    "MyManager");
                            (model[0]).setManager(manager);
                        }
                        (model[0])
                                .setModelErrorHandler(new BasicModelErrorHandler());
                        manager.execute();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(runAction);
            _sleep();
        }

        if (save) {
            // Optionally save the model.
            // Sadly, running the DOPCenter.xml model does not seem to update the
            // graph.  So, we run it and save it and then open it again.
            Runnable saveAction = new Runnable() {
                    public void run() {
                        try {
                            System.out.println("Saving " + model[0].getFullName());
                            ((PtolemyEffigy)(_basicGraphFrame.getTableau().getContainer())).writeFile(new File(modelFileName));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                    }
                };
            SwingUtilities.invokeAndWait(saveAction);
            _sleep();
        }

        if (openComposites) {
            // Optionally open any composites.
            Runnable openCompositesAction = new Runnable() {
                public void run() {
                    try {
                        System.out.println("Opening submodels of " + model[0].getFullName());
                        Configuration configuration = (Configuration)Configuration.findEffigy(model[0].toplevel()).toplevel();

                        List<CompositeEntity> composites = model[0].deepCompositeEntityList();
                        for (CompositeEntity composite: composites) {
                            // Don't open class definitions, then tend not to get closed.
                            //if (!composite.isClassDefinition()) {
                            System.out.println("Opening " + composite.getFullName());
                            Tableau tableau = configuration.openInstance(composite);
                            if (whiteBackground) {
                                JFrame frame = tableau.getFrame();
                                frame.setBackground(java.awt.Color.WHITE);
                                ((ptolemy.vergil.basic.BasicGraphFrame)frame).getJGraph().getCanvasPane().getCanvas().setBackground(java.awt.Color.WHITE);
                            }
                            //}
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(openCompositesAction);
            _sleep();
        }

        if (whiteBackground) {
            // Optionally set the background to white.
            Runnable openCompositesAction = new Runnable() {
                public void run() {
                    try {
                        System.out.println("Setting the background to white.");
                        Configuration configuration = (Configuration)Configuration.findEffigy(model[0].toplevel()).toplevel();
                        ModelDirectory directory = (ModelDirectory) configuration
                            .getEntity(Configuration._DIRECTORY_NAME);
                        Iterator effigies = directory.entityList().iterator();

                        while (effigies.hasNext()) {
                            Effigy effigy = (Effigy) effigies.next();
                            Iterator tableaux = effigy.entityList(Tableau.class).iterator();
                            //System.out.println("Effigy: " + effigy);
                            while (tableaux.hasNext()) {
                                Tableau tableau = (Tableau) tableaux.next();
                                //System.out.println("Tableau: " + tableau);
                                JFrame frame = tableau.getFrame();
                                if (frame instanceof TableauFrame) {
                                    // FIXME: lamely, we skip by the configuration directory and UserLibrary by name?
                                    if (!tableau
                                            .getFullName()
                                            .equals(".configuration.directory.configuration.graphTableau")
                                            && !tableau
                                            .getFullName()
                                            .equals(".configuration.directory.UserLibrary.graphTableau")) {
                                        try {
                                            // Set the background to white.

                                            frame.setBackground(java.awt.Color.WHITE);
                                            ((ptolemy.vergil.basic.BasicGraphFrame)frame).getJGraph().getCanvasPane().getCanvas().setBackground(java.awt.Color.WHITE);

                                            // FIXME: It should be
                                            // possible to use
                                            // PtolemyPreference here,
                                            // but it does not work,
                                            // we have to set the
                                            // frame background by
                                            // hand.
                                            
//                                             PtolemyPreferences.setDefaultPreferences(configuration);
//                                             PtolemyPreferences preferences = PtolemyPreferences
//                                                 .getPtolemyPreferencesWithinConfiguration(configuration);
//                                             preferences.backgroundColor
//                                                 .setExpression("{1.0, 1.0, 1.0, 1.0}");
//                                             //preferences.save();
//                                             preferences.setAsDefault();

                                            //System.out.println("Frame: " + frame);
                                            frame.repaint();
                                        } catch (Exception ex) {
                                            System.out
                                                .println("Failed to set the background to white.");
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(openCompositesAction);
            _sleep();
        }


        // Export images
        Runnable exportImageAction = new Runnable() {
            public void run() {
                try {
                    File imageFile = new File(model[0].getName() + "."
                            + formatName.toLowerCase());
                    OutputStream out = null;
                    try {
                        if (formatName.toLowerCase().equals("htm")) {
                            File directory = new File(model[0].getName());
                            if (!directory.isDirectory()) {
                                if (!directory.mkdirs()) {
                                    throw new Exception("Failed to create "
                                            + directory);
                                }
                            }
                            _basicGraphFrame.writeHTML(directory);
                            System.out.println("Exported html to "
                                    + StringUtilities.getProperty("user.dir") + "/"
                                    + directory + "/index.html");
                        } else {
                            out = new FileOutputStream(imageFile);
                            // Export the image.
                            _basicGraphFrame.getJGraph().exportImage(out,
                                    formatName);
                            System.out.println("Exported "
                                    + imageFile.getCanonicalPath());
                        }
                    } finally {
                        if (out != null) {
                            try {
                                out.close();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(exportImageAction);
        _sleep();

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            public void run() {
                try {
                    ConfigurationApplication
                            .closeModelWithoutSavingOrExiting(model[0]);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(closeAction);
        _sleep();
    }

    /** Export a model as an image.
     *
     *  <p>Note that the a graphical display must be present, this
     *  code displays the model and executes.  To use in a headless
     *  environment under Linux, install Xvfb.</p>
     *
     *  <p>Usage:</p>
     *  <p> To save a gif:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.image.ExportImage model.xml
     *  </pre>
     *
     *  <p>or, to save the current view of model in HTML format without any plots:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.image.ExportImage htm model.xml
     *  </pre>
     *
     *  <p>or, to run the model and save the current view of model in
     *  HTML format with any plots:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.image.ExportImage -run htm model.xml
     *  </pre>
     *
     *  <p>or, to run the model, open any composites and save the
     *  current view of model and the composites HTML format with any
     *  plots:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.image.ExportImage -run -openComposites htm model.xml
     *  </pre>
     *
     *  <p>or, to save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.image.ExportImage png model.xml
     *  </pre>
     *
     *  <p>or, to run the model and then save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.image.ExportImage -run png model.xml
     *  </pre>
     *
     *  <p>To set the background to white, invoke with
     *  <code>-whiteBackground</code>.</p>
     * 
     *  <p>To export an html version in a format suitable for the
     *  Ptolemy website, set the
     *  "ptolemy.ptII.exportHTML.usePtWebsite" property to true,
     *  perhaps by including the following in the command line:</p>
     *  <pre>
     *  -Dptolemy.ptII.exportHTML.usePtWebsite=true
     *  </pre>
     *
     *  <p>Include a link to the a
     *  <code><i>sanitizedModelName</i>.jnlp</code> file, set the
     *  "ptolemy.ptII.exportHTML.linkToJNLP" property to true.</p>
     *  
     *  @param args The arguments for the export image operation.
     *  The arguments should be in the format:
     *  [-openComposites] [-run] [-save] [-whiteBackground] [GIF|gif|HTM*|htm*|PNG|png] model.xml.
     *
     *  @exception args If there is 1 argument, then it names a
     *  Ptolemy MoML file and the model is exported as a .gif file.
     *  If there are two arguments, then the first argument names a
     *  format, current formats are GIF, gif, HTM, htm, PNG and png
     *  and the second argument names a Ptolemy MoML file.
     */
    public static void main(String args[]) {
        String usage = "Usage: java -classpath $PTII "
                + "ptolemy.vergil.basic.ExportImage "
                + "[-openComposites] [-run] [-save] [-whiteBackground] [GIF|gif|HTM*|htm*|PNG|png] model.xml";
        if (args.length == 0 || args.length > 5) {
            // FIXME: we should get the list of acceptable format names from
            // BasicGraphFrame
            System.err.println("Wrong number of arguments");
            System.err.println(usage);
            System.exit(3);
        }
        String formatName = "GIF";
        boolean run = false;
        boolean openComposites = false;
        boolean save = false;
        boolean whiteBackground = false;
        String modelFileName = null;
        if (args.length == 1) {
            modelFileName = args[0];
        } else {
            // FIXME: this is a lame way to process arguments.
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-run")) {
                    run = true;
                } else if (args[i].equals("-save")) {
                    save = true;
                } else if (args[i].equals("-openComposites")) {
                    openComposites = true;
                } else if (args[i].toUpperCase().equals("GIF")
                        || args[i].toUpperCase().startsWith("HTM")
                        || args[i].toUpperCase().equals("PNG")) {
                    formatName = args[i].toUpperCase();
                } else if (args[i].equals("-whiteBackground")) {
                    whiteBackground = true;
                } else {
                    modelFileName = args[i];
                }
            }
        }
        try {
            new ExportImage().exportImage(formatName, modelFileName, run, openComposites, save, whiteBackground);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(5);
        }
    }

    /** Sleep the current thread, which is usually not the Swing Event
     *  Dispatch Thread.
     */
    protected static void _sleep() {
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
