/* Export a model as an image or set of html files.

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

package ptolemy.vergil.basic.export;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.BrowserEffigy;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.actor.lib.gui.UsesInvokeAndWait;
import ptolemy.data.BooleanToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.ExportParameters;
import ptolemy.vergil.basic.export.html.ExportHTMLAction;
import ptolemy.vergil.basic.export.web.WebExportParameters;

///////////////////////////////////////////////////////////////////
//// ExportModel
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
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportModel {
    /** Export an image of a model to a file or directory.

     *  <p>The image is written to a file or directory with the same name
     *  as the model.  If formatName starts with "HTM" or "htm", then
     *  a directory with the same name as the basename of the model is
     *  created.  If the formatName is "GIF", "gif", "PNG" or "png",
     *  then a file with the same basename as the basename of the
     *  model is created.</p>
     *
     *  <p>The time out defaults to 30 seconds.</p>
     *
     *  @param copyJavaScriptFiles True if the javascript files should
     *  be copied.  Used only if <i>formatName</i> starts with "htm"
     *  or "HTM".
     *
     *  @param force If true, then remove the image file or htm
     *  directory to be created in advance before creating the image
     *  file or htm directory.  This parameter is primarily used to
     *  avoid prompting the user with questions about overwriting
     *  files after this command is invoked.
     *
     *  @param formatName The file format of the file to be generated.
     *  One of "GIF", "gif", "HTM", "htm", "PNG", "png".
     *
     *  @param modelFileName A Ptolemy model in MoML format.
     *  The string may start with $CLASSPATH, $HOME or other formats
     *  suitable for {@link ptolemy.util.FileUtilities#nameToFile(String, URI)}.
     *
     *  @param run True if the model should be run first.  If <i>run</i>
     *  is true, and if <i>formatName</i> starts with "htm" or "HTM", then
     *  the output will include images of any plots.
     *
     *  @param openComposites True if the CompositeEntities should be
     *  open.  The <i>openComposites</i> parameter only has an effect
     *  if <i>formatName</i> starts with "htm" or "HTM".
     *
     *  @param openResults open the resulting image file or web page.
     *
     *  @param outputFileOrDirectory If non-null, then the file or directory
     *  in which to generate the file(s).
     *
     *  @param save True if the model should be saved after being run.
     *
     *  @param whiteBackground True if the model background should be set to white.
     *
     *  @exception Exception Thrown if there is a problem reading the model
     *  or exporting the image.
     */
    public void exportModel(final boolean copyJavaScriptFiles,
            final boolean force, final String formatName,
            final String modelFileName, final boolean run,
            final boolean openComposites, final boolean openResults,
            final String outputFileOrDirectory, final boolean save,
            final boolean whiteBackground) throws Exception {

        exportModel(copyJavaScriptFiles, force, formatName, modelFileName, run,
                openComposites, openResults, outputFileOrDirectory, save,
                30000, whiteBackground);
    }

    /** Export an image of a model to a file or directory.
     *  The image is written to a file or directory with the same name as the model.
     *  If formatName starts with "HTM" or "htm", then a directory with the
     *  same name as the basename of the model is created.
     *  If the formatName is "GIF", "gif", "PNG" or "png", then a file
     *  with the same basename as the basename of the model is created.
     *
     *  @param copyJavaScriptFiles True if the javascript files should be copied.
     *  Used only if <i>formatName</i> starts with "htm" or "HTM".
     *
     *  @param force If true, then remove the image file or htm directory to be created
     *  in advance before creating the image file or htm directory.  This parameter
     *  is primarily used to avoid prompting the user with questions about overwriting files
     *  after this command is invoked.
     *
     *  @param formatName The file format of the file to be generated.
     *  One of "GIF", "gif", "HTM", "htm", "PNG", "png".
     *
     *  @param modelFileName A Ptolemy model in MoML format.
     *  The string may start with $CLASSPATH, $HOME or other formats
     *  suitable for {@link ptolemy.util.FileUtilities#nameToFile(String, URI)}.
     *
     *  @param run True if the model should be run first.  If <i>run</i>
     *  is true, and if <i>formatName</i> starts with "htm" or "HTM", then
     *  the output will include images of any plots.
     *
     *  @param openComposites True if the CompositeEntities should be
     *  open.  The <i>openComposites</i> parameter only has an effect
     *  if <i>formatName</i> starts with "htm" or "HTM".
     *
     *  @param openResults open the resulting image file or web page.
     *
     *  @param outputFileOrDirectory If non-null, then the file or directory
     *  in which to generate the file(s).
     *
     *  @param save True if the model should be saved after being run.
     *
     *  @param timeOut Time out in milliseconds.  30000 is a good value.
     *
     *  @param whiteBackground True if the model background should be set to white.
     *
     *  @exception Exception Thrown if there is a problem reading the model
     *  or exporting the image.
     */
    public void exportModel(final boolean copyJavaScriptFiles,
            final boolean force, final String formatName,
            final String modelFileName, final boolean run,
            final boolean openComposites, final boolean openResults,
            final String outputFileOrDirectory, final boolean save,
            final long timeOut, final boolean whiteBackground) throws Exception {
        // FIXME: Maybe we should pass an ExportParameter here?

        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        // Perhaps we should make this a field?
        //final TypedCompositeActor[] model = new TypedCompositeActor[1];
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
                    throwable.printStackTrace();
                    throw new RuntimeException(throwable);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        _sleep();

        _basicGraphFrame = BasicGraphFrame.getBasicGraphFrame(model[0]);

        // Set temporary variables before setting the final versions
        // for use inside inner classes.
        File temporaryHTMLDirectory = null;
        File temporaryImageFile = null;

        // Use the model name as the basis for the directory containing
        // the html or as the basis for the image file.
        final boolean isHTM = formatName.toLowerCase(Locale.getDefault())
                .startsWith("htm");
        if (isHTM) {
            if (outputFileOrDirectory != null) {
                temporaryHTMLDirectory = new File(outputFileOrDirectory);
                temporaryImageFile = new File(outputFileOrDirectory
                        + File.separator + "index.html");
            } else {
                temporaryHTMLDirectory = new File(model[0].getName());
                temporaryImageFile = new File(model[0].getName()
                        + File.separator + "index.html");

            }
        } else {
            String suffix = "." + formatName.toLowerCase(Locale.getDefault());
            if (outputFileOrDirectory != null) {
                // If the filename does not end in the formatName,
                // append the format name.
                if (outputFileOrDirectory.endsWith(formatName
                        .toLowerCase(Locale.getDefault()))
                        || outputFileOrDirectory.endsWith(formatName
                                .toUpperCase(Locale.getDefault()))) {
                    suffix = "";
                }
                temporaryImageFile = new File(outputFileOrDirectory + suffix);
            } else {
                // The user did not specify an outputFileOrDirectory,
                // so use the model name.
                temporaryImageFile = new File(model[0].getName() + suffix);
            }
        }

        // The directory where an html file would be generated.
        final File htmlDirectory = temporaryHTMLDirectory;
        // The name of the index.html file or image file.
        final File imageFile = temporaryImageFile;

        // We optionally delete the directory containing the .html file or
        // delete the image file.  Do this after loading the model so that
        // we can get the directory in which the model resides
        if (force) {
            // Delete the directory containing the .html file or
            // delete the image file.
            if (isHTM) {
                if (htmlDirectory.exists()
                        && !FileUtilities.deleteDirectory(htmlDirectory)) {
                    System.err.println("Could not delete \"" + htmlDirectory
                            + "\".");
                }
            } else {
                // A gif/jpg/png file
                if (imageFile.exists() && !imageFile.delete()) {
                    System.err.println("Could not delete \"" + imageFile
                            + "\".");
                }
            }
        }

        if (run) {
            if (!_runnable(model[0])) {
                System.out
                .println("Model \""
                        + model[0].getFullName()
                        + "\" contains actors such cannot be run "
                        + " as part of the export process from ExportModel or "
                        + "it has a WebExportParameters value that runBeforeExport set to false. "
                        + "To export run this model and export it, use vergil.");
            } else {
                // Optionally run the model.
                Runnable runAction = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (!(model[0] instanceof TypedCompositeActor)) {
                                System.out
                                .println(model[0].getFullName()
                                        + " is a "
                                        + model[0].getClass().getName()
                                        + " not a TypedCompositeActor, so it cannot be run.");
                                return;
                            }
                            TypedCompositeActor composite = (TypedCompositeActor) model[0];
                            System.out.println("Running "
                                    + composite.getFullName());
                            Manager manager = composite.getManager();
                            if (manager == null) {
                                manager = new Manager(composite.workspace(),
                                        "MyManager");
                                composite.setManager(manager);
                            }
                            composite
                            .setModelErrorHandler(new BasicModelErrorHandler());
                            _timer = new Timer(true);
                            final Director finalDirector = composite
                                    .getDirector();
                            TimerTask doTimeToDie = new TimerTask() {
                                @Override
                                public void run() {
                                    System.out
                                    .println("ExportHTMLTimer went off after "
                                            + timeOut
                                            + " ms., calling getDirector().finish and getDirector().stopFire()");

                                    // NOTE: This used to call stop() on
                                    // the manager, but that's not the
                                    // right thing to do. In particular,
                                    // this could be used inside a
                                    // RunCompositeActor, and it should
                                    // only stop the inside execution, not
                                    // the outside one.  It's also not
                                    // correct to call stop() on the
                                    // director, because stop() requests
                                    // immediate stopping. To give
                                    // determinate stopping, this actor
                                    // needs to complete the current
                                    // iteration.

                                    // The Stop actor has similar code.
                                    finalDirector.finish();

                                    // To support multithreaded domains,
                                    // also have to call stopFire() to
                                    // request that all actors conclude
                                    // ongoing firings.
                                    finalDirector.stopFire();
                                }
                            };
                            _timer.schedule(doTimeToDie, timeOut);

                            // Calling finish() and stopFire() is not
                            // sufficient if the model is still
                            // initializing, so we call stop() on the
                            // manager after 2x the timeout.
                            // To replicate:

                            // $PTII/bin/ptinvoke ptolemy.vergil.basic.export.ExportModel -force htm -run -openComposites -timeOut 30000 -whiteBackground ptolemy/domains/ddf/demo/RijndaelEncryption/RijndaelEncryption.xml $PTII/ptolemy/domains/ddf/demo/RijndaelEncryption/RijndaelEncryption

                            final Manager finalManager = manager;
                            _failSafeTimer = new Timer(true);
                            TimerTask doFailSafeTimeToDie = new TimerTask() {
                                @Override
                                public void run() {
                                    System.out
                                    .println("ExportHTMLTimer went off after "
                                            + timeOut
                                            * 2
                                            + " ms., calling manager.stop().");

                                    finalManager.stop();
                                }
                            };
                            _failSafeTimer.schedule(doFailSafeTimeToDie,
                                    timeOut * 2);

                            try {
                                manager.execute();
                            } finally {
                                _timer.cancel();
                                _failSafeTimer.cancel();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }
                    }
                };
                SwingUtilities.invokeAndWait(runAction);
                _sleep();
            }
        }

        if (save) {
            // Optionally save the model.
            // Sadly, running the DOPCenter.xml model does not seem to update the
            // graph.  So, we run it and save it and then open it again.
            Runnable saveAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Saving " + model[0].getFullName());
                        ((PtolemyEffigy) _basicGraphFrame.getTableau()
                                .getContainer()).writeFile(new File(
                                        modelFileName));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }
            };
            SwingUtilities.invokeAndWait(saveAction);
            _sleep();
        }

        if (openComposites && !isHTM) {
            // Optionally open any composites.
            Runnable openCompositesAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Opening submodels of "
                                + model[0].getFullName());
                        Configuration configuration = (Configuration) Configuration
                                .findEffigy(model[0].toplevel()).toplevel();

                        List<CompositeEntity> composites = model[0]
                                .deepCompositeEntityList();
                        for (CompositeEntity composite : composites) {
                            // Don't open class definitions, then tend not to get closed.
                            //if (!composite.isClassDefinition()) {
                            System.out.println("Opening "
                                    + composite.getFullName());
                            Tableau tableau = configuration
                                    .openInstance(composite);
                            if (whiteBackground) {
                                JFrame frame = tableau.getFrame();
                                frame.setBackground(java.awt.Color.WHITE);
                                ((ptolemy.vergil.basic.BasicGraphFrame) frame)
                                .getJGraph().getCanvasPane()
                                .getCanvas()
                                .setBackground(java.awt.Color.WHITE);
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

        if (whiteBackground && !isHTM) {
            // Optionally set the background to white.  The
            // ExportParameters facility handles this for us for
            // exporting htm.
            Runnable whiteBackgroundAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Setting the background to white.");
                        Configuration configuration = (Configuration) Configuration
                                .findEffigy(model[0].toplevel()).toplevel();
                        ModelDirectory directory = (ModelDirectory) configuration
                                .getEntity(Configuration._DIRECTORY_NAME);
                        Iterator effigies = directory.entityList().iterator();

                        while (effigies.hasNext()) {
                            Effigy effigy = (Effigy) effigies.next();
                            Iterator tableaux = effigy
                                    .entityList(Tableau.class).iterator();
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
                                            ((ptolemy.vergil.basic.BasicGraphFrame) frame)
                                            .getJGraph()
                                            .getCanvasPane()
                                            .getCanvas()
                                            .setBackground(
                                                    java.awt.Color.WHITE);

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
            SwingUtilities.invokeAndWait(whiteBackgroundAction);
            _sleep();
        }

        // Export images
        Runnable exportModelAction = new Runnable() {
            @Override
            public void run() {
                try {
                    OutputStream out = null;

                    try {
                        if (formatName.toLowerCase(Locale.getDefault())
                                .startsWith("htm")) {
                            if (!htmlDirectory.isDirectory()) {
                                if (!htmlDirectory.mkdirs()) {
                                    throw new Exception("Failed to create \""
                                            + htmlDirectory + "\"");
                                }
                            }
                            // FIXME: ExportParameters handles things like setting
                            // the background color, opening composites before export etc.
                            // However, we that here so that export images and export htm
                            // is the same.  This could be a mistake.
                            ExportParameters parameters = new ExportParameters(
                                    htmlDirectory);
                            if (whiteBackground) {
                                // Set the background of any submodels that are opened.
                                parameters.backgroundColor = Color.white;
                            }
                            parameters.copyJavaScriptFiles = copyJavaScriptFiles;
                            parameters.openCompositesBeforeExport = openComposites;
                            parameters.showInBrowser = openResults;

                            ExportHTMLAction.exportToWeb(_basicGraphFrame,
                                    parameters);
                            System.out.println("Exported " + htmlDirectory
                                    + "/index.html");
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
        SwingUtilities.invokeAndWait(exportModelAction);
        _sleep();

        if (openResults && !isHTM) {
            // Optionally open the results.
            Runnable openResultsAction = new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("Opening " + imageFile);
                        Configuration configuration = (Configuration) Configuration
                                .findEffigy(model[0].toplevel()).toplevel();
                        URL imageURL = new URL(imageFile.toURI().toURL()
                                .toString()
                                + "#in_browser");
                        configuration.openModel(imageURL, imageURL,
                                imageURL.toExternalForm(),
                                BrowserEffigy.staticFactory);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                        throw new RuntimeException(throwable);
                    }
                }
            };
            SwingUtilities.invokeAndWait(openResultsAction);
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
     *  <p>Command line arguments are:</p>
     *  <dl>
     *  <dt>-help|--help|-h</dt>
     *  <dd>Print a help message and return.</dd>
     *  <dt>-copyJavaScriptFiles</dt>
     *  <dd>Copy .js files.  Useful only with -web and htm* format.</dd>
     *  <dt>-force</dt>
     *  <dd>Delete the target file or directory before generating the results.</dd>
     *  <dt>-open</dt>
     *  <dd>Open the generated file in a browser.</dd>
     *  <dt>-openComposites</dt>
     *  <dd>Open any composites before exporting the model.</dd>
     *  <dt>-run</dt>
     *  <dd>Run the model before exporting. This is useful when exporting an html file as plots
     *  are also generated.</dd>
     *  <dt>-save</dt>
     *  <dd>Save the model before closing.</dd>
     *  <dt>-web</dt>
     *  <dd>Common settings for exporting to the web. Short for: <code>-force
     *  -copyJavaScriptFiles -open -openComposites htm</code>.</dd>
     *  <dt>-whiteBackground</dt>
     *  <dd>Set the background color to white.</dd>
     *  <dt>[GIF|gif|HTM*|htm*|PNG|png]</dt>
     *  <dd>The file format.  If no format is selected, then a gif format file is generated.</dd>
     *  <dt><i>model.xml</i></dt>
     *  <dd>The model to be exported. (Required)</dd>
     *  <dt><i>directoryName</i></dt>
     *  <dd>The directory in which to export the file(s) (Optional)</dd>
     *  </dl>
     *
     *  <p>Typical usage:</p>
     *  <p> To save a gif:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel model.xml
     *  </pre>
     *
     *  <p>or, to save the current view of model in HTML format without any plots:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel htm model.xml
     *  </pre>
     *
     *  <p>or, to run the model and save the current view of model in
     *  HTML format with any plots:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel -run htm model.xml
     *  </pre>
     *
     *  <p>or, to run the model, open any composites and save the
     *  current view of model and the composites HTML format with any
     *  plots:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel -run -openComposites htm model.xml
     *  </pre>
     *
     *  <p>Standard setting for exporting to html can be invoked with <code>-web</code>,
     *  which is like <code>-copyJavaScriptFiles -open -openComposites htm</code>.</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel -web model.xml
     *  </pre>
     *
     *  <p>or, to save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel png model.xml
     *  </pre>
     *
     *  <p>or, to run the model and then save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.export.ExportModel -run png model.xml
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
     *  <p>For example:</p>
     *  <pre>
     *  export JAVAFLAGS=-Dptolemy.ptII.exportHTML.usePtWebsite=true
     *  $PTII/bin/ptweb $PTII/ptolemy/moml/demo/modulation.xml
     *  </pre>
     *
     *  <p>To include a link to a <code><i>sanitizedModelName</i>.jnlp</code> file,
     *  set -Dptolemy.ptII.exportHTML.linkToJNLP=true.</p>
     *
     *  <p>Note that the Ptolemy menus will not appear unless you view
     * the page with a web server that has Server Side Includes (SSI)
     * enabled and has the appropriate scripts.  Also, the .html
     * files must be executable.</p>
     *
     *  <p>Include a link to the a
     *  <code><i>sanitizedModelName</i>.jnlp</code> file, set the
     *  "ptolemy.ptII.exportHTML.linkToJNLP" property to true.</p>
     *
     *  @param args The arguments for the export image operation.
     *  The arguments should be in the format:
     *  [-help|-h|--help] | [-copyJavaScriptFiles] [-force] [-open] [-openComposites] [-run] [-save]
     *  [-timeOut ms]
     *  [-web] [-whiteBackground] [GIF|gif|HTM*|htm*|PNG|png] model.xml
     *
     *  @exception IllegalArgumentException If there is 1 argument, then it names a
     *  Ptolemy MoML file and the model is exported as a .gif file.
     *  If there are two arguments, then the first argument names a
     *  format, current formats are GIF, gif, HTM, htm, PNG and png
     *  and the second argument names a Ptolemy MoML file.
     */
    public static void main(String args[]) {
        String eol = System.getProperty("line.separator");
        String usage = "Usage:"
                + eol
                + "java -classpath $PTII "
                + "ptolemy.vergil.basic.export.ExportModel "
                + "[-help|-h|--help] | [-copyJavaScript] [-force] [-open] [-openComposites] "
                + "[-run] [-save] [-web] [-whiteBackground] [GIF|gif|HTM*|htm*|PNG|png] model.xml"
                + eol
                + "Command line arguments are: "
                + eol
                + " -help      Print this message."
                + eol
                + " -copyJavaScriptFiles  Copy .js files.  Useful only with -web and htm* format."
                + eol
                + " -force     Delete the target file or directory before generating the results."
                + eol
                + " -open      Open the generated file."
                + eol
                + " -openComposites       Open any composites before exporting the model."
                + eol
                + " -run       Run the model before exporting. -web and htm*: plots are also generated."
                + eol
                + " -save      Save the model before closing."
                + eol
                + " -timeOut milliseconds   Timeout in milliseconds."
                + eol
                + " -web  Common web export args. Short for: -force -copyJavaScriptFiles -open -openComposites htm."
                + eol
                + " -whiteBackground      Set the background color to white."
                + eol
                + " GIF|gif|HTM*|htm*|PNG|png The file format."
                + eol
                + " model.xml  The Ptolemy model. (Required)"
                + eol
                + "To export html suitable for the Ptolemy website, invoke "
                + eol
                + "Java with -Dptolemy.ptII.exportHTML.usePtWebsite=true"
                + eol
                + "For example:"
                + eol
                + "export JAVAFLAGS=-Dptolemy.ptII.exportHTML.usePtWebsite=true"
                + eol
                + "$PTII/bin/ptweb $PTII/ptolemy/moml/demo/modulation.xml"
                + eol + "To include a link to a sanitizedModelName.jnlp file,"
                + eol + "set -Dptolemy.ptII.exportHTML.linkToJNLP=true";

        if (args.length == 0) {
            // FIXME: we should get the list of acceptable format names from
            // BasicGraphFrame
            System.err.println("Wrong number of arguments");
            System.err.println(usage);
            // Use StringUtilities.exit() so that we can test unit test this code
            // and avoid FindBugs warnings about System.exit().
            StringUtilities.exit(3);
            return;
        }
        boolean copyJavaScriptFiles = false;
        boolean force = false;
        String formatName = "GIF";
        boolean openResults = false;
        boolean openComposites = false;
        String outputFileOrDirectory = null;
        boolean run = false;
        boolean save = false;
        long timeOut = 30000;
        boolean whiteBackground = false;
        boolean web = false;
        String modelFileName = null;
        if (args.length == 1 && !args[0].startsWith("-")) {
            modelFileName = args[0];
        } else {
            // FIXME: this is a lame way to process arguments.
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-help") || args[i].equals("--help")
                        || args[i].equals("-h")) {
                    System.out.println(usage);
                    // Use StringUtilities.exit() so that we can test unit test this code
                    // and avoid FindBugs warnings about System.exit().
                    StringUtilities.exit(0);
                    return;
                } else if (args[i].equals("-copyJavaScriptFiles")) {
                    copyJavaScriptFiles = true;
                } else if (args[i].equals("-force")) {
                    force = true;
                } else if (args[i].equals("-open")
                        || args[i].equals("-openResults")) {
                    openResults = true;
                } else if (args[i].equals("-openComposites")) {
                    openComposites = true;
                } else if (args[i].equals("-run")) {
                    run = true;
                } else if (args[i].equals("-save")) {
                    save = true;
                } else if (args[i].equals("-timeOut")) {
                    try {
                        timeOut = Long.parseLong(args[i + 1]);
                    } catch (NumberFormatException ex) {
                        System.err
                        .println(args[i + 1]
                                + "cannot be parsed to long value for the time out."
                                + ex);
                    }
                    i++;
                } else if (args[i].toUpperCase(Locale.getDefault()).equals(
                        "GIF")
                        || args[i].toUpperCase(Locale.getDefault()).startsWith(
                                "HTM")
                                || args[i].toUpperCase(Locale.getDefault()).equals(
                                        "PNG")) {
                    // The default is GIF.
                    if (web) {
                        throw new IllegalArgumentException("Only one of "
                                + args[i] + " and -web "
                                + "should be specified.");
                    }
                    formatName = args[i].toUpperCase(Locale.getDefault());
                } else if (args[i].equals("-web")) {
                    web = true;
                    copyJavaScriptFiles = true;
                    force = true;
                    formatName = "htm";
                    openResults = true;
                    openComposites = true;
                    whiteBackground = true;
                } else if (args[i].equals("-whiteBackground")) {
                    whiteBackground = true;
                } else {
                    if (args[i].startsWith("-")) {
                        throw new IllegalArgumentException(
                                "The model file name "
                                        + "cannot begin with a '-', the argument was: "
                                        + args[i]);
                    }
                    if (i < args.length - 2) {
                        throw new IllegalArgumentException(
                                "The model file name "
                                        + "should be the last or second to last argument. "
                                        + "The last argument was: " + args[i]);
                    }
                    if (modelFileName != null) {
                        outputFileOrDirectory = args[i];
                    } else {
                        modelFileName = args[i];
                    }
                }
            }
        }
        try {
            // FIXME: Should we use ExportParameter here?
            new ExportModel().exportModel(copyJavaScriptFiles, force,
                    formatName, modelFileName, run, openComposites,
                    openResults, outputFileOrDirectory, save, timeOut,
                    whiteBackground);

        } catch (Exception ex) {
            ex.printStackTrace();
            StringUtilities.exit(5);
        }
        StringUtilities.exit(0);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Sleep the current thread, which is usually not the Swing Event
     *  Dispatch Thread.
     */
    protected static void _sleep() {
        // FIXME: The problem is that we need to wait for all the
        // images to load before getting the images.

        // FIXME: we should be able to call
        // Toolkit.getDefaultToolkit().sync(); but that does not do
        // it.
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

    /** The Timer used to terminate a run by calling finish() and stopFire()*/
    private static Timer _timer = null;

    /** The Timer used to terminate a run by calling stop on the
     * manager. This is called fail safe after the movie by the same
     * name.
     */
    private static Timer _failSafeTimer = null;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Return true if the model is runnable from this context.
     *  Models that invoke SwingUtilities.invokeAndWait()
     *  are not runnable here.  To export such a model, use
     *  vergil.
     *  If the model has a WebExportParameters parameter
     *  then the value of the runBeforeExport Parameter is
     *  returned.
     *  @param model The model to be checked.
     *  @return true if the model is runnable.
     *  @exception IllegalActionException If the WebExportParameter
     *  cannot be read.
     */
    private boolean _runnable(CompositeEntity model)
            throws IllegalActionException {
        // Check for WebExportParameters.runBeforeExport being false.
        List<WebExportParameters> webExportParameters = model
                .attributeList(WebExportParameters.class);
        if (webExportParameters.size() > 0) {
            if (!((BooleanToken) webExportParameters.get(0).runBeforeExport
                    .getToken()).booleanValue()) {
                return false;
            }
        }

        // Check for actors that implement UsesInvokeAndWait.
        Iterator atomicEntities = model.allAtomicEntityList().iterator();
        while (atomicEntities.hasNext()) {
            Entity entity = (Entity) atomicEntities.next();
            if (entity instanceof UsesInvokeAndWait) {
                System.out.println(entity.getFullName()
                        + " invoked SwingUtilities.invokeAndWait()");
                return false;
            }
        }
        return true;
    }

}
