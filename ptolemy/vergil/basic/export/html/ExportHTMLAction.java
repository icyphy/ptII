/* An Action that works with BasicGraphFrame to export HTML.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic.export.html;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.BrowserEffigy;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.modal.kernel.State;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.attributes.VersionAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.CancelException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.actor.ActorGraphTableau;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.ExportParameters;
import ptolemy.vergil.basic.HTMLExportable;
import ptolemy.vergil.basic.export.web.DefaultIconLink;
import ptolemy.vergil.basic.export.web.DefaultIconScript;
import ptolemy.vergil.basic.export.web.WebAttribute;
import ptolemy.vergil.basic.export.web.WebElement;
import ptolemy.vergil.basic.export.web.WebExportParameters;
import ptolemy.vergil.basic.export.web.WebExportable;
import ptolemy.vergil.basic.export.web.WebExporter;
import diva.canvas.Figure;
import diva.canvas.JCanvas;
import diva.graph.GraphController;

/** An Action that works with BasicGraphFrame to export HTML.
 *  Given a directory, this action creates an image of the
 *  currently visible portion of the BasicGraphFrame and an
 *  HTML page that displays that image. In addition, it
 *  creates a map of the locations of actors in the image
 *  and actions associated with each of the actors.
 *  The default content of the web page and the actions
 *  associated with the image map are defined by instances
 *  of {@link WebExportable} that have been inserted at
 *  the top level of the current {@link Configuration}.
 *  The model may customize both the web page content and
 *  the actions in the image map by inserting into the model
 *  instances of {@link WebExportable}.
 *  <p>
 *  If the model contains an instance of
 *  {@link WebExportParameters}, then that instance
 *  defines parameters of the export. If not, but
 *  the current configuration contains one, then that
 *  instance defines the the parameters. Otherwise,
 *  the defaults in {@link WebExportParameters}
 *  are used.
 *
 * <p>The following JVM properties affect the output:</p>
 * <dl>
 * <dt>        -Dptolemy.ptII.exportHTML.usePtWebsite=true</dt>
 * <dd> Include Ptolemy Website (<a href="http://ptolemy.org#in_browser" target="_top">http://ptolemy.org</a>)
 * specific Side Includes (SSI) and use JavaScript libraries from the
 * Ptolemy website.</dd>
 * <dt> -Dptolemy.ptII.exportHTML.linkToJNLP=true</dt>
 * <dd> Include a link to the a <code><i>sanitizedModelName</i>.jnlp</code> file.</dd>
 * </dl>
 *
 * <p>Typically, JVM properties are set when Java is invoked.
 * {@link ptolemy.vergil.basic.export.ExportModel} can be called with these
 * properties set to create Ptolemy website specific web pages.</p>
 *
 * <p> See <a href="http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport#in_browser" target="_top">http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport</a>
 * for detailed instructions about how to create web pages on the
 * Ptolemy website for models.</p>
 *
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (eal)
 */
@SuppressWarnings("serial")
public class ExportHTMLAction extends AbstractAction implements HTMLExportable,
        WebExporter {

    /** Create a new action to export HTML.
     *  @param basicGraphFrame The Vergil window to export.
     */
    public ExportHTMLAction(BasicGraphFrame basicGraphFrame) {
        super("Export to Web");
        _basicGraphFrame = basicGraphFrame;
        putValue("tooltip", "Export HTML and image files showing this model.");
        // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Export a web page.
     *  @param event The event that triggered this action.
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        NamedObj model = _basicGraphFrame.getModel();
        WebExportParameters defaultParameters = null;
        try {
            List<WebExportParameters> defaultParameterList = model
                    .attributeList(WebExportParameters.class);
            if (defaultParameterList == null
                    || defaultParameterList.size() == 0) {
                defaultParameterList = _basicGraphFrame.getConfiguration()
                        .attributeList(WebExportParameters.class);
                if (defaultParameterList == null
                        || defaultParameterList.size() == 0) {
                    defaultParameters = new WebExportParameters(model,
                            model.uniqueName("_defaultWebExportParameters"));
                    // We want this new attribute to look as if it were part of
                    // its container's class definition so that it does not get
                    // exported to MoML unless it changes in some way, e.g. one
                    // of the parameter values it contains changes.
                    defaultParameters.setDerivedLevel(1);
                }
            }
            if (defaultParameters == null) {
                defaultParameters = defaultParameterList.get(0);
            }
            EditParametersDialog dialog = new EditParametersDialog(
                    _basicGraphFrame, defaultParameters, "Export to Web for "
                            + model.getName());
            if (!dialog.buttonPressed().equals("Commit")) {
                return;
            }

            ExportParameters parameters = defaultParameters
                    .getExportParameters();
            // Set the copy directory target to null to indicate that no copying
            // of files has happened.
            parameters.setJSCopier(null);
            exportToWeb(_basicGraphFrame, parameters);
        } catch (KernelException ex) {
            MessageHandler.error("Unable to export HTML.", ex);
        }
    }

    /** If parameters.copyJavaScriptFiles is true and the Java
     *  property ptolemy.ptII.exportHTML.usePtWebsite is false,
     *  then copy the required JavaScript files into the target directory
     *  given in the parameters argument.
     *  @param graphFrame The frame being exported.
     *  @param parameters The export parameters.
     *  @return False if something went wrong and the user requested
     *   canceling the export. True otherwise.
     */
    public static boolean copyJavaScriptFilesIfNeeded(
            final BasicGraphFrame graphFrame, final ExportParameters parameters) {
        // First, if appropriate, copy needed files.
        boolean usePtWebsite = Boolean.valueOf(StringUtilities
                .getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));
        usePtWebsite = usePtWebsite || parameters.usePtWebsite;
        if (parameters.copyJavaScriptFiles && !usePtWebsite) {
            // Copy Javascript source files into destination directory,
            // if they are available. The files are under an MIT license,
            // which is compatible with the Ptolemy license.
            // For jquery, we could use a CDS (content delivery service) instead
            // of copying the file.
            String jsDirectoryName = "$CLASSPATH/ptolemy/vergil/basic/export/html/javascript/";
            File jsDirectory = FileUtilities.nameToFile(jsDirectoryName, null);
            // We assume that if the directory exists, then the files exist.
            if (jsDirectory.isDirectory()) {
                // Copy files into the "javascript" directory.
                File jsTargetDirectory = new File(
                        parameters.directoryToExportTo, "javascript");
                if (jsTargetDirectory.exists()
                        && !jsTargetDirectory.isDirectory()) {
                    File jsBackupDirectory = new File(
                            parameters.directoryToExportTo, "javascript.bak");
                    if (!jsTargetDirectory.renameTo(jsBackupDirectory)) {
                        // It is ok to ignore this.
                        System.out.println("Failed to rename \""
                                + jsTargetDirectory + "\" to \""
                                + jsBackupDirectory + "\"");
                    }
                }

                if (!jsTargetDirectory.exists() && !jsTargetDirectory.mkdir()) {
                    try {
                        MessageHandler
                                .warning("Warning: Cannot find required JavaScript, CSS, and image files"
                                        + " for lightbox effect implemented by the fancybox"
                                        + " package. Perhaps your Ptolemy II"
                                        + " installation does not include them."
                                        + " Will use the files on ptolemy.org.");
                    } catch (CancelException e) {
                        // Cancel the action.
                        return false;
                    }
                    parameters.copyJavaScriptFiles = false;
                } else {
                    // If deleteFilesOnExit is selected, mark the new
                    // Javscript directory for deletion.  Mark it first so
                    // that it will be deleted after its contained files have
                    // been deleted.  Files/directories are deleted in the
                    // reverse order that they are registered.
                    if (parameters.deleteFilesOnExit) {
                        jsTargetDirectory.deleteOnExit();
                    }

                    // Copy css, JavaScript, and image files.
                    for (String filename : FILENAMES) {
                        try {
                            URL lightboxFile = FileUtilities.nameToURL(
                                    jsDirectoryName + filename, null, null);
                            File file = new File(jsTargetDirectory, filename);
                            if (parameters.deleteFilesOnExit) {
                                file.deleteOnExit();
                            }
                            FileUtilities.binaryCopyURLToFile(lightboxFile,
                                    file);
                        } catch (IOException e) {
                            try {
                                MessageHandler
                                        .warning("Warning: failed to copy required files."
                                                + " Use the files on ptolemy.org? "
                                                + e.getMessage());
                            } catch (CancelException e1) {
                                // Cancel the action.
                                return false;
                            }
                            parameters.copyJavaScriptFiles = false;
                        }
                    }
                    parameters.setJSCopier(graphFrame.getModel());
                }
            }
        }
        return true;
    }

    /** Define an attribute to be included in the HTML area element
     *  corresponding to the region of the image map covered by
     *  the specified object. For example, if an <i>attribute</i> "href"
     *  is added, where the <i>value</i> is a URI, then the
     *  area in the image map for the specified object will include
     *  a hyperlink to the specified URI. If the specified object
     *  already has a value for the specified attribute, then
     *  the previous value is replaced by the new one.
     *  If the specified attribute is "default", then all attributes
     *  associated with the object are cleared.
     *  <p>
     *  This method is a callback method that may be performed
     *  by attributes of class
     *  {@link ptolemy.vergil.basic.export.web.WebExportable}
     *  when their
     *  {@link ptolemy.vergil.basic.export.web.WebExportable#provideContent(WebExporter)}
     *  method is called by this exporter.</p>
     *
     *  @param webAttribute The attribute to be included.
     *  @param overwrite If true, overwrite any previously defined value for
     *   the specified attribute. If false, then do nothing if there is already
     *   an attribute with the specified name.
     *  @return True if the specified attribute and value was defined (i.e.,
     *   if there was a previous value, it was overwritten).
     */
    @Override
    public boolean defineAttribute(WebAttribute webAttribute, boolean overwrite) {
        if (webAttribute.getContainer() != null) {
            NamedObj object = webAttribute.getContainer();
            HashMap<String, String> areaTable = _areaAttributes.get(object);
            if (areaTable == null) {
                // No previously defined table. Add one.
                areaTable = new HashMap<String, String>();
                _areaAttributes.put(object, areaTable);
            }
            if (overwrite || areaTable.get(webAttribute.getWebName()) == null) {
                areaTable.put(webAttribute.getWebName(),
                        _escapeString(webAttribute.getExpression()));
                return true;
            }
        }
        return false;
    }

    /** Define an element.
     *  If <i>onceOnly</i> is true, then if identical content has
     *  already been added to the specified position, then it is not
     *  added again.
     *  @param webElement The element.
     *  @param onceOnly True to prevent duplicate content.
     */
    @Override
    public void defineElement(WebElement webElement, boolean onceOnly) {

        List<StringBuffer> contents = _contents.get(webElement.getParent());
        if (contents == null) {
            contents = new LinkedList<StringBuffer>();
            _contents.put(webElement.getParent(), contents);
        }
        StringBuffer webElementBuffer = new StringBuffer(
                webElement.getExpression());
        // Check to see whether contents are already present.
        if (onceOnly) {
            // FIXME: Will List.contains() work if two StringBuffers
            // are constructed from the same String?
            if (contents.contains(webElementBuffer)) {
                return;
            }
        }
        contents.add(new StringBuffer(webElementBuffer));
    }

    /** Export an HTML page and associated subpages for the specified
     *  graph frame as given by the parameters. After setting everything
     *  up, this method will delegate to the {@link BasicGraphFrame#writeHTML(ExportParameters, Writer)}
     *  method, which in turn will delegate back to an instance of this class, ExportHTMLAction.
     *  <p>
     *  This method should be invoked in the swing thread.
     *  It will invoke a separate thread to run the model (if so
     *  specified in the parameters).
     *  When that thread completes the run, it will delegate
     *  back to the swing thread to do the export.
     *  Note that this method will return before the export
     *  is completed. If another thread needs to wait for
     *  this complete, then it can call {@link #waitForExportToComplete()}.
     *  This is synchronized to ensure that only one export can be in progress at a time.
     *  </p>
     *  @param graphFrame The frame containing a model to export.
     *  @param parameters The parameters that control the export.
     *   making the exported web page independent of the ptolemy.org site.
     */
    public static synchronized void exportToWeb(
            final BasicGraphFrame graphFrame, final ExportParameters parameters) {
        try {

            if (parameters.directoryToExportTo == null) {
                MessageHandler.error("No directory specified.");
                return;
            }

            // See whether the directory has a file called index.html.
            final File indexFile = new File(parameters.directoryToExportTo,
                    "index.html");
            if (parameters.directoryToExportTo.exists()) {
                // Previously, if directory existed and was a directory, we would always pop
                // up a dialog stating that the directory existed and that the contents would
                // be overwritten.  This seems excessive because the dialog will always
                // be shown.
                if (indexFile.exists()) {
                    if (!MessageHandler
                            .yesNoQuestion("\""
                                    + parameters.directoryToExportTo
                                    + "\" exists and contains an index.html file. Overwrite contents?")) {
                        MessageHandler.message("HTML export canceled.");
                        return;
                    }
                }
                if (!parameters.directoryToExportTo.isDirectory()) {
                    if (!MessageHandler
                            .yesNoQuestion("\""
                                    + parameters.directoryToExportTo
                                    + "\" is a file, not a directory. Delete the file named \""
                                    + parameters.directoryToExportTo
                                    + "\" and create a directory with that name?")) {
                        MessageHandler.message("HTML export canceled.");
                        return;
                    }
                    if (!parameters.directoryToExportTo.delete()) {
                        MessageHandler.message("Unable to delete file \""
                                + parameters.directoryToExportTo + "\".");
                        return;
                    }
                    if (!parameters.directoryToExportTo.mkdir()) {
                        MessageHandler.message("Unable to create directory \""
                                + parameters.directoryToExportTo + "\".");
                        return;
                    }
                }
            } else {
                if (!parameters.directoryToExportTo.mkdir()) {
                    MessageHandler.message("Unable to create directory \""
                            + parameters.directoryToExportTo + "\".");
                    return;
                }
            }
            // We now have a directory and permission to write to it.
            if (!copyJavaScriptFilesIfNeeded(graphFrame, parameters)) {
                // Canceled the export.
                return;
            }

            // Using a null here causes the write to occur to an index.html file.
            final PrintWriter writer = null;

            openRunAndWriteHTML(graphFrame, parameters, indexFile, writer,
                    false);
        } catch (Exception ex) {
            MessageHandler.error("Unable to export to web.", ex);
            throw new RuntimeException(ex);
        }
    }

    /** During invocation of {@link #writeHTML(ExportParameters, Writer)},
     *  return the parameters being used.
     *  @return The parameters of the current export, or null if there
     *   is not one in progress.
     */
    @Override
    public ExportParameters getExportParameters() {
        return _parameters;
    }

    /** The frame (window) being exported to HTML.
     *  @return The frame provided to the constructor.
     */
    @Override
    public PtolemyFrame getFrame() {
        return _basicGraphFrame;
    }

    /** Depending on the export parameters (see {@link ExportParameters}),
     *  open submodels, run the model, and export HTML.
     *  @param graphFrame The frame being exported.
     *  @param parameters The export parameters.
     *  @param indexFile If you wish to show the exported page in a browser,
     *   then this parameter must specify the file to which the write occurs
     *   and parameters.showInBrowser must be true. Otherwise, this parameter
     *   should be null.
     *  @param writer The writer to write to, or null to write to the default
     *   index.html file.
     *  @param waitForCompletion If true, then do not return until the export
     *   is complete. In this case, everything is run in the calling thread,
     *   which is required to be the Swing event thread.
     *  @exception IllegalActionException If something goes wrong.
     */
    public static void openRunAndWriteHTML(final BasicGraphFrame graphFrame,
            final ExportParameters parameters, final File indexFile,
            final Writer writer, final boolean waitForCompletion)
            throws IllegalActionException {
        if (graphFrame == null) {
            throw new IllegalActionException(
                    "Cannot export without a graphFrame.");
        }
        // Open submodels, if appropriate.
        final Set<Tableau> tableauxToClose = new HashSet<Tableau>();
        if (parameters.openCompositesBeforeExport) {
            NamedObj model = graphFrame.getModel();
            Effigy masterEffigy = Configuration.findEffigy(graphFrame
                    .getModel());
            if (model instanceof CompositeEntity) {
                // graphFrame.getModel() might return a
                // PteraController, which is not a CompositeActor.
                List<Entity> entities = ((CompositeEntity) model).entityList();
                for (Entity entity : entities) {
                    _openEntity(entity, tableauxToClose, masterEffigy,
                            graphFrame);
                }
            }
        }
        // Running the model has to occur in a new thread, or the whole
        // process could hang (if the model doesn't return). So finish in a new thread.
        // That thread will, in turn, have to again invoke the swing event thread
        // to close any tableaux that were opened above.
        // It does not wait for the close to complete before finishing itself.
        Runnable exportAction = new Runnable() {
            @Override
            public void run() {
                try {
                    // graphFrame.getModel() might return a
                    // PteraController, which is not a CompositeActor.
                    NamedObj model = graphFrame.getModel();

                    // If parameters are set to run the model, then do that.
                    if (parameters.runBeforeExport
                            && model instanceof CompositeActor) {
                        // Run the model.
                        Manager manager = ((CompositeActor) model).getManager();
                        if (manager == null) {
                            manager = new Manager(
                                    ((CompositeActor) model).workspace(),
                                    "MyManager");
                            ((CompositeActor) model).setManager(manager);
                        }
                        manager.execute();
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Model execution failed.", ex);
                    throw new RuntimeException(ex);
                } finally {
                    // The rest of the export has to occur in the
                    // swing event thread. We do this whether the
                    // run succeeded or not.
                    Runnable finishExport = new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // -------- Finally, actually export to web.
                                graphFrame.writeHTML(parameters, writer);

                                // Finally, if requested, show the exported page.
                                if (parameters.showInBrowser
                                        && indexFile != null) {
                                    Configuration configuration = graphFrame
                                            .getConfiguration();
                                    try {
                                        URL indexURL = new URL(indexFile
                                                .toURI().toURL().toString()
                                                + "#in_browser");
                                        configuration.openModel(indexURL,
                                                indexURL,
                                                indexURL.toExternalForm(),
                                                BrowserEffigy.staticFactory);
                                    } catch (Throwable throwable) {
                                        MessageHandler.error(
                                                "Failed to open \"" + indexFile
                                                        + "\".", throwable);
                                        throw new RuntimeException(throwable);
                                    }
                                }
                            } catch (Exception ex) {
                                MessageHandler.error(
                                        "Unable to export to web.", ex);
                                throw new RuntimeException(ex);
                            } finally {
                                // Export is finally finished.
                                _exportInProgress = false;
                                synchronized (ExportHTMLAction.class) {
                                    ExportHTMLAction.class.notifyAll();
                                }
                                for (Tableau tableau : tableauxToClose) {
                                    tableau.close();
                                }
                            }

                        }
                    };
                    if (waitForCompletion) {
                        finishExport.run();
                    } else {
                        SwingUtilities.invokeLater(finishExport);
                    }
                }
            }
        };
        // Invoke the new thread. First make sure the flag is set
        // to indicate that an export is in progress.
        _exportInProgress = true;
        if (waitForCompletion) {
            exportAction.run();
        } else {
            Thread result = new Thread(exportAction);
            result.start();
        }
    }

    /** Set the title to be used for the page being exported.
     *  @param title The title.
     *  @param showInHTML True to produce an HTML title prior to the model image.
     */
    // FIXME:  Replaced- a WebExportable will add the title, if any.  If it does not
    // add a title, then there will be no title.

    @Override
    public void setTitle(String title, boolean showInHTML) {
        _title = StringUtilities.escapeForXML(title);
        _showTitleInHTML = showInHTML;
    }

    /** Wait for the current invocation of {@link #exportToWeb(BasicGraphFrame, ExportParameters)}
     *  to complete. If there is not one in progress, return immediately.
     */
    public static synchronized void waitForExportToComplete() {
        while (_exportInProgress) {
            try {
                ExportHTMLAction.class.wait();
            } catch (InterruptedException e) {
                // Ignore and return.
                return;
            }
        }
    }

    /** Write an HTML page based on the current view of the model
     *  to the specified destination directory. The file will be
     *  named "index.html," and supporting files, including at
     *  least an image showing the contents currently visible in
     *  the graph frame, will be created. Any instances of
     *  {@link WebExportable} in the configuration are first
     *  cloned into the model, so these provide default behavior,
     *  for example defining links to any open composite actors
     *  or plot windows.
     *  <p>
     *  If the "ptolemy.ptII.exportHTML.usePtWebsite" property is set to true,
     *  e.g. by invoking with -Dptolemy.ptII.usePtWebsite=true,
     *  then the html files will have Ptolemy website specific Server Side Includes (SSI)
     *  code and use the JavaScript and fancybox files from the Ptolemy website.
     *  In addition, a toc.htm file will be created to aid in navigation.
     *  This facility is not likely to be portable to other websites.
     *  </p>
     *
     *  @param parameters The parameters that control the export.
     *  @param writer The writer to use the write the HTML. If this is null,
     *   then create an index.html file in the
     *   directory given by the directoryToExportTo field of the parameters.
     *  @exception IOException If unable to write associated files.
     *  @exception PrinterException If unable to write associated files.
     *  @exception IllegalActionException If reading parameters fails.
     */
    @Override
    public void writeHTML(ExportParameters parameters, Writer writer)
            throws PrinterException, IOException, IllegalActionException {
        // Invoke with -Dptolemy.ptII.usePtWebsite=true to get Server
        // Side Includes (SSI).  FIXME: this is a bit of a hack, we should
        // use templates instead.
        boolean usePtWebsite = Boolean.valueOf(StringUtilities
                .getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));
        usePtWebsite = usePtWebsite || parameters.usePtWebsite;

        File indexFile = null;
        PrintWriter printWriter = null;

        // The following try...finally block ensures that the index and toc files
        // get closed even if an exception occurs. It also resets _parameters.
        try {
            _parameters = parameters;

            // First, create the image file showing whatever the current
            // view in this frame shows.
            NamedObj model = _basicGraphFrame.getModel();

            // $PTII/bin/ptinvoke ptolemy.vergil.basic.export.ExportModel -force htm -run -openComposites -timeOut 30000 -whiteBackground ptolemy/domains/ptera/demo/CarWash/CarWash.xml
            // needs this.
            if (model.getName().equals("_Controller")) {
                model = model.getContainer();
            }
            // Use a sanitized model name and avoid problems with special characters in file names.
            _sanitizedModelName = StringUtilities.sanitizeName(model.getName());
            File imageFile = new File(parameters.directoryToExportTo,
                    _sanitizedModelName + "." + _parameters.imageFormat);
            if (parameters.deleteFilesOnExit) {
                imageFile.deleteOnExit();
            }
            OutputStream out = new FileOutputStream(imageFile);
            try {
                _basicGraphFrame.writeImage(out, _parameters.imageFormat,
                        parameters.backgroundColor);
            } finally {
                out.close();
            }
            // Initialize the data structures into which content is collected.
            _areaAttributes = new HashMap<NamedObj, HashMap<String, String>>();
            _contents = new HashMap<String, List<StringBuffer>>();
            _end = new LinkedList<StringBuffer>();
            _head = new LinkedList<StringBuffer>();
            _start = new LinkedList<StringBuffer>();
            _contents.put("head", _head);
            _contents.put("start", _start);
            _contents.put("end", _end);

            // Clone instances of WebExportable from the Configuration
            // into the model. These are removed in the finally clause
            // of the try block.
            _provideDefaultContent();

            // Next, collect the web content specified by the instances
            // of WebExportable contained by the model.
            List<WebExportable> exportables = model
                    .attributeList(WebExportable.class);

            // Plus, collect the web content specified by the contained
            // objects of the model.
            Iterator<NamedObj> contentsIterator = model
                    .containedObjectsIterator();
            while (contentsIterator.hasNext()) {
                NamedObj containedObject = contentsIterator.next();
                exportables.addAll(containedObject
                        .attributeList(WebExportable.class));
            }

            // Then, iterate through the list of exportables and extract
            // content from each.
            // Use the class of exportable to determine whether to insert
            // content as an attribute or a seperate element
            for (WebExportable exportable : exportables) {
                exportable.provideContent(this);
            }

            // If a title has been specified and set to show, then
            // add it to the start HTML section at the beginning.
            if (_showTitleInHTML) {
                _start.add(0, new StringBuffer("<h1>"));
                _start.add(1, new StringBuffer(_title));
                _start.add(2, new StringBuffer("</h1>\n"));
            }

            // System.out.println("Location of index.html: "+parameters.directoryToExportTo);

            // Next, create an HTML file.
            if (writer == null) {
                indexFile = new File(parameters.directoryToExportTo,
                        "index.html");
                if (parameters.deleteFilesOnExit) {
                    indexFile.deleteOnExit();
                }
                Writer indexWriter = new FileWriter(indexFile);
                printWriter = new PrintWriter(indexWriter);
            } else {
                printWriter = new PrintWriter(writer);
            }

            // Generate a header that will pass the HTML validator at
            // http://validator.w3.org/
            // Use HTML5 tags.  Use charset utf-8 to support extended characters
            // We use println so as to get the correct eol character for
            // the local platform.
            printWriter.println("<!DOCTYPE html>");
            printWriter.println("<html>");
            printWriter.println("<head>");
            printWriter.println("<meta charset=utf-8>");

            // Define the path to the SSI files on the ptolemy site.
            // ssiRoot always has a trailing slash.
            final String ssiRoot = "http://ptolemy.org/";

            // Reference required script files.
            // If the model contains an instance of CopyJavaScriptFiles, then
            // the required files will have been copied into a directory called
            // "javascript" in the top-level directory of the export.
            // Otherwise, we want to reference these files at http://ptolemy.org/.
            // If the usePtWebsite property is true, then reference the files
            // at http://ptolemy.org/ whether the property is true or not.
            String jsLibrary = ssiRoot;
            if (!usePtWebsite) {
                // If the model or a container above it in the hierarchy has
                // copyJavaScriptFiles set to true, then set up the
                // references to refer to the copied files rather than the
                // website files.
                // FIXME: This can fail if we export a submodel only but
                // the enclosing model has its copyJavaScriptFiles parameter
                // set to true!
                String copiedLibrary = _findCopiedLibrary(model, "",
                        parameters.getJSCopier());
                if (copiedLibrary != null) {
                    jsLibrary = copiedLibrary;
                }
            }

            // In HTML5, can omit "type" attributes for scripts and stylesheets
            printWriter.println("<link rel=\"stylesheet\"  href=\"" + jsLibrary
                    + "javascript/" + FILENAMES[2] + "\" media=\"screen\"/>");
            printWriter.println("<link rel=\"stylesheet\"  href=\"" + jsLibrary
                    + "javascript/" + FILENAMES[4] + "\" media=\"screen\"/>");
            if (usePtWebsite) {
                // FIXME: this absolute path is not very safe.  The
                // problem is that we don't know where $PTII is located on
                // the website.
                printWriter
                        .println("<link href=\""
                                + ssiRoot
                                + "ptolemyII/ptIIlatest/ptII/doc/default.css\" rel=\"stylesheet\" type=\"text/css\"/>");
            }

            // Title needed for the HTML validator.
            printWriter.println("<title>" + _title + "</title>");

            // In HTML5, can omit "type" attributes for scripts and stylesheets
            // NOTE: Due to a bug somewhere (browser, Javascript, etc.), can't end this with />. Have to use </script>.
            printWriter.println("<script src=\"" + jsLibrary + "javascript/"
                    + FILENAMES[0] + "\"></script>");
            printWriter.println("<script src=\"" + jsLibrary + "javascript/"
                    + FILENAMES[1] + "\"></script>");

            // FILENAMES[2] is a stylesheet <link, so it goes in the head, see above.

            printWriter.println("<script src=\"" + jsLibrary + "javascript/"
                    + FILENAMES[3] + "\"></script>");
            printWriter.println("<script src=\"" + jsLibrary + "javascript/"
                    + FILENAMES[5] + "\"></script>");
            // Could alternatively use a CDS (Content Delivery Service) for the JavaScript library for jquery.
            // index.println("<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js\"></script>");

            // Next, create the image map.
            String map = _createImageMap(parameters.directoryToExportTo);

            // Write the main part of the HTML file.
            //
            _printHTML(printWriter, "head");

            //------ <head>...</head> above here
            if (usePtWebsite) {
                // Reference the server-side includes.
                // toppremenu.htm includes </head>...<body>
                printWriter
                        .println("<!--#include virtual=\"/ssi/toppremenu.htm\" -->");
                printWriter.println("<!--#include virtual=\"toc.htm\" -->");
                printWriter
                        .println("<!--#include virtual=\"/ssi/toppostmenu.htm\" -->");
            } else {
                // The Ptolemy website headers include the closing </head> and <body tag>
                printWriter.println("</head>");
                // Place </head> and <body> on separate lines so that
                // tools like the TerraSwarm website can easily find
                // them.
                // Match the background color of the canvas, unless an explicit
                // background color is given.
                Color background = parameters.backgroundColor;
                if (parameters.backgroundColor == null) {
                    JCanvas canvas = _basicGraphFrame.getJGraph()
                            .getGraphPane().getCanvas();
                    background = canvas.getBackground();
                }
                String color = "#" + String.format("%02x", background.getRed())
                        + String.format("%02x", background.getGreen())
                        + String.format("%02x", background.getBlue());

                printWriter.println("<body>");
                printWriter.println("<div style=\"background-color:" + color
                        + "\">");
            }

            _printHTML(printWriter, "start");

            boolean linkToJNLP = Boolean.valueOf(StringUtilities
                    .getProperty("ptolemy.ptII.exportHTML.linkToJNLP"));
            //System.out.println("ExportHTMLAction: model: " + model + " model name: " + model.getName() + " " + model.getContainer() + " isClassDef: " + ((ptolemy.kernel.InstantiableNamedObj)model).isClassDefinition());
            //if (model.getContainer() != null) {
            //    System.out.println("ExportHTMLAction: name: " + model.getContainer().getName() + model.getContainer().getContainer());
            //}
            if (linkToJNLP
                    && ((model.getContainer() == null 
                                    && model instanceof CompositeEntity
                                    // Don't include links to the .xml of class definitions 
                                    && !((CompositeEntity)model).isClassDefinition())
                            || (model.getContainer() != null 
                                    && /* Ptera */model.getContainer().getContainer() == null
                                    && model.getName().equals("_Controller")))) {
                String linkToHelp = "<a href=\""
                        + ssiRoot
                        + "ptolemyII/ptIIlatest/ptII/doc/webStartHelp_index.htm\"><img src=\""
                        + ssiRoot
                        + "image/question.png\" alt=\"What is Web Start\"></a> (<i>Java Plug-in Required</i>)";

                printWriter
                .println("<div id=\"inlineImg\">" // Defined in UCB.css
                                + "<p>Below is a browsable image of the model.</p> "
                        + "<ul>\n");

                StringParameter noJNLPLinkParameter = (StringParameter) model
                    .getAttribute("_noJNLPLink", StringParameter.class);
                if (linkToJNLP && noJNLPLinkParameter != null) {
                    System.out.println("The ptolemy.ptII.exportHTML.linkToJNLP JVM property was set, "
                        + "but the _noJNLPLink parameter was set, so this model " + model.getFullName()
                        + " will not have a link to the JNLP version.  Typically models that don't run well, "
                        + "like the BCVTB models have this parameter set.");
                    printWriter.println("<!-- The model had a _noJNLPLink parameter set, so we are not "
                            + "linking to the JNLP files. -->\n");
                } else {
                    printWriter.println(
                                "<li>For an executable version,"
                                + "<!-- We use the deployJava.js script so that Java "
                                + "will be installed if necessary -->\n"
                                + "<script src=\"http://www.java.com/js/deployJava.js\"></script>\n"
                                + "<script>\n"
                                + "  var dir = location.href.substring(0,location.href.lastIndexOf('/'));\n"
                                + "  var parentDir = dir.substring(0,dir.lastIndexOf('/')+1);\n"
                                + "  var url = parentDir + \""
                                + _sanitizedModelName
                                + ".jnlp\";\n"
                                + "  deployJava.createWebStartLaunchButton(url);\n"
                                + "  document.write(\" the WebStart version. "
                                + linkToHelp.replace("\"", "\\\"")
                                + "\");\n"
                                + "</script>\n"
                                + "<noscript>\n"
                                + "<a href=\"../"
                                + _sanitizedModelName
                                + ".jnlp\">WebStart version</a>. \n"
                                + linkToHelp + "</noscript>\n" + "</li>\n");
                }
                printWriter
                        .println("<li>To view or save the MoML file for this model, "
                                + "<a href=\"../"
                                + _sanitizedModelName
                                + ".xml\">click here</a>.</li>");
                if (usePtWebsite) {
                    if (_isInDomains(model)) {
                        printWriter
                        .println("<li>For a domain overview, "
                                + "<a href=\"../../../doc/\">click here</a>.</li>");
                    }
                }
                printWriter.println("</ul>\n" + "</div> <!-- inlineImg -->\n");

            }
            // Put the image in.
            printWriter.println("<img src=\"" + _sanitizedModelName + "."
                    + _parameters.imageFormat + "\" usemap=\"#iconmap\" "
                    // The HTML Validator at http://validator.w3.org/check wants an alt tag
                    + "alt=\"" + _sanitizedModelName + "model\"/>");
            printWriter.println(map);
            _printHTML(printWriter, "end");

            if (!usePtWebsite) {
                printWriter.println("</div>");
                printWriter.println("</body>");
                printWriter.println("</html>");
            } else {
                printWriter.println("<!-- /body -->");
                printWriter.println("<!-- /html -->");
                printWriter
                        .println("<!--#include virtual=\"/ssi/bottom.htm\" -->");

                String tocContents = ExportHTMLAction._findToc(model);
                //if (tocContents != "") {
                //    _addContent("toc.htm", false, tocContents);
                //} else {
                    // Start the top of the toc.htm file.
                    _addContent("toc.htm", false, "<div id=\"menu\">");
                    _addContent("toc.htm", false, "<ul>");
                    _addContent("toc.htm", false,
                            "<li><a href=\"/index.htm\">Ptolemy Home</a></li>");

                    // The URL of the current release.
                    String ptURL = (usePtWebsite ? "http://ptolemy.org" : "")
                        + "/ptolemyII/ptII"
                        + VersionAttribute.majorCurrentVersion() + "/ptII"
                        + VersionAttribute.CURRENT_VERSION.getExpression()
                        + "/";

                    _addContent("toc.htm", false,
                            "<li><a href=\"" + ptURL + "doc/index.htm\">Ptolemy "
                            + VersionAttribute.majorCurrentVersion()
                            + "</a></li>");
                    _addContent("toc.htm", false, "</ul>");
                    _addContent("toc.htm", false, "");

                    String upHTML = null;
                    if (_isInDomains(model)) {
                        upHTML = "<li><a href=\"../../../doc/\">Up</a></li>";
                    } else {
                        // If there is a _upHTML parameter, use its value.
                        StringParameter upHTMLParameter = (StringParameter) model
                            .getAttribute("_upHTML", StringParameter.class);
                        if (upHTMLParameter != null) {
                            upHTML = upHTMLParameter.stringValue();
                        } else {
                            if (!usePtWebsite) {
                                upHTML = " <li><a href=\"../index.html\">Up</a></li>";
                            } else {
                                // Generate links to the domain docs.
                                String domains[] = { "Continuous", "DDF", "DE",
                                                     "Modal", "PN", "Rendezvous", "SDF", "SR",
                                                     "Wireless" };
                                StringBuffer buffer = new StringBuffer();
                                for (int i = 0; i < domains.length; i++) {
                                    buffer.append("<li><a href=\"" + ptURL
                                            + "ptolemy/domains/"
                                            + domains[i].toLowerCase()
                                            + "/doc/index.htm\">" + domains[i]
                                            + "</a></li>");
                                }
                                upHTML = buffer.toString();
                            }
                        }
                    }
                    
                    // Only add <ul> if we have upHTML
                    if (upHTML != null) {
                        _addContent("toc.htm", false, "<ul>");
                        _addContent("toc.htm", false, upHTML);
                    _addContent("toc.htm", false, "</ul>");
                    }
                    
                    // Get the toc contents and stuff it into toc.htm.
                    List<StringBuffer> contents = _contents.get("tocContents");
                    if (contents != null) {
                        _addContent("toc.htm", false, "<ul>");
                        for (StringBuffer line : contents) {
                            _addContent("toc.htm", false, line.toString());
                        }
                        _addContent("toc.htm", false, "</ul>");
                    }
                    _addContent("toc.htm", false, "</div><!-- /#menu -->");
                    //}
            }

            // If _contents contains any entry other than head, start, or end,
            // then interpret that entry as a file name to write to.
            for (String key : _contents.keySet()) {
                if (!key.equals("end") && !key.equals("head")
                        && !key.equals("start") && !key.equals("tocContents")) {
                    if (key.equals("")) {
                        // FIXME: I'm not sure why the key would be
                        // empty but the command below requires it:

                        // (cd $PTII/doc/papers/y12/designContracts; $PTII/bin/ptinvoke -Dptolemy.ptII.exportHTML.linkToJNLP=true -Dptolemy.ptII.exportHTML.usePtWebsite=true ptolemy.vergil.basic.export.ExportModel -run -whiteBackground -openComposites htm DCMotorTol.xml)

                        System.out
                                .println("Warning, key of _contents was empty?");
                        continue;
                    }
                    // NOTE: A RESTful version of this would create a resource
                    // that could be addressed by a URL. For now, we just
                    // write to a file. Java documentation doesn't say
                    // whether the following overwrites a pre-existing file,
                    // but it does seem to do that, so I assume that's what it does.
                    Writer fileWriter = null;
                    try {
                        File file = new File(parameters.directoryToExportTo,
                                key);
                        if (parameters.deleteFilesOnExit) {
                            file.deleteOnExit();
                        }
                        fileWriter = new FileWriter(file);
                    } catch (IOException ex) {
                        throw new IllegalActionException(model, ex,
                                "Could not open a FileWriter "
                                        + "in directory \""
                                        + parameters.directoryToExportTo
                                        + "\" and file \"" + key + "\".");

                    }
                    PrintWriter otherWriter = new PrintWriter(fileWriter);
                    List<StringBuffer> contents = _contents.get(key);
                    for (StringBuffer line : contents) {
                        otherWriter.println(line);
                    }
                    otherWriter.close();
                }
            }
        } finally {
            _parameters = null;
            _removeDefaultContent();
            if (printWriter != null) {
                printWriter.close(); // Without this, the output file may be empty
            }
            if (usePtWebsite && indexFile != null) {
                if (!indexFile.setExecutable(true, false /*ownerOnly*/)) {
                    System.err.println("Could not make " + indexFile
                            + "executable.");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         package protected methods         ////

    /** List of filenames needed by jquery and fancybox.
     *  These are automatically provided to every exported web page
     *  either by referencing the ptolemy.org website (the default)
     *  or by copying the files into the target directory (if the
     *  model contains an instance of WebExportParameters with
     *  copyJavaScriptFiles set to true).
     *  The first three of these should be the JavaScript files to include,
     *  and the fourth should be the CSS file.
     *  The rest are image files to copy over.
     */
    // FIXME: I don't like the hardwired version numbers here.
    // Findbugs wants this package protected and final.
    final static String[] FILENAMES = {
        "jquery-1.7.2.min.js",
            "jquery.fancybox-1.3.4.pack.js",
        "jquery.fancybox-1.3.4.css",
            "pt-1.0.0.js",
        "tooltipster.css",
        "jquery.tooltipster.min.js",
            // The ones above this line must be in exactly the order given
            // They are referenced below by index.
            "blank.gif", "fancybox.png", "fancybox-y.png", "fancybox-x.png",
        "fancy_title_right.png", "fancy_title_over.png",
            "fancy_title_main.png", "fancy_title_left.png",
            "fancy_shadow_w.png", "fancy_shadow_sw.png", "fancy_shadow_se.png",
            "fancy_shadow_s.png", "fancy_shadow_nw.png", "fancy_shadow_ne.png",
            "fancy_shadow_n.png", "fancy_shadow_e.png", "fancy_nav_right.png",
            "fancy_nav_left.png", "fancy_loading.png", "fancy_close.png",
            "javascript-license.htm" };

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the image map. As a side effect, this may create other
     *  HTML files or subdirectories.
     *  @param directory The directory into which to write any HTML
     *   that is created as a side effect.
     *  @return HTML that describes the image map.
     *  @exception PrinterException If writing to the toc file fails.
     *  @exception IOException If IO fails.
     *  @exception IllegalActionException If reading parameters fails.
     */
    protected String _createImageMap(File directory)
            throws IllegalActionException, IOException, PrinterException {
        StringBuffer result = new StringBuffer();
        // The HTML Validator at http://validator.w3.org/check wants an id tag.
        // For HTML5, the name and id must match
        result.append("<map name=\"iconmap\" id=\"iconmap\">\n");

        // Iterate over the icons.
        List<IconVisibleLocation> iconLocations = _getIconVisibleLocations();
        for (IconVisibleLocation location : iconLocations) {
            // This string will have at least one space at the start and the end.
            StringBuffer attributeString = new StringBuffer(" ");
            String title = "Actor"; // Default in case there is no title key.
            HashMap<String, String> areaAttributes = _areaAttributes
                    .get(location.object);
            // If areaAttributes is null, omit the entry, since an HTML area
            // element is required to have an href attribute
            if (areaAttributes != null) {
                for (Map.Entry<String, String> entry : areaAttributes
                        .entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // If the value is empty, omit the entry.
                    if (value != null && !value.trim().equals("")) {
                        if (key.equals("title")) {
                            title = StringUtilities.escapeString(value);
                        }
                        attributeString.append(key);
                        attributeString.append("=\"");
                        attributeString.append(StringUtilities
                                .escapeString(value));
                        attributeString.append("\" ");
                    }
                }

                // Write the name of the actor followed by the table.
                result.append("<area shape=\"rect\" coords=\""
                        + (int) location.topLeftX + ","
                        + (int) location.topLeftY + ","
                        + (int) location.bottomRightX + ","
                        + (int) location.bottomRightY + "\"\n"
                        + attributeString + "alt=\"" + title + "\"/>\n");
            }

        }
        result.append("</map>\n");
        return result.toString();
    }

    /** Return a list of data structures with one entry for each visible
     *  entity and attribute. Each data structure contains
     *  a reference to the entity and the coordinates
     *  of the upper left corner and lower right corner of the main
     *  part of its icon (not including decorations like the name
     *  and any highlights it may have). The coordinates are relative
     *  to the current visible rectangle, where the upper left corner
     *  of the visible rectangle has coordinates (0,0), and the lower
     *  right corner has coordinates (w,h), where w is the width
     *  and h is the height (in pixels).
     *  @return A list representing the space occupied by each
     *   visible icon for the entities in the model, or an empty
     *   list if no icons are visible.
     */
    protected List<IconVisibleLocation> _getIconVisibleLocations() {
        List<IconVisibleLocation> result = new LinkedList<IconVisibleLocation>();
        Rectangle2D viewSize = _basicGraphFrame.getVisibleRectangle();
        JCanvas canvas = _basicGraphFrame.getJGraph().getGraphPane()
                .getCanvas();
        AffineTransform transform = canvas.getCanvasPane()
                .getTransformContext().getTransform();
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();
        double translateX = transform.getTranslateX();
        double translateY = transform.getTranslateY();

        NamedObj model = _basicGraphFrame.getModel();
        if (model instanceof CompositeEntity) {
            List<Entity> entities = ((CompositeEntity) model).entityList();
            for (Entity entity : entities) {
                _addRectangle(result, viewSize, scaleX, scaleY, translateX,
                        translateY, entity);
            }
        }
        List<Attribute> attributes = ((CompositeEntity) model).attributeList();
        for (Attribute attribute : attributes) {
            _addRectangle(result, viewSize, scaleX, scaleY, translateX,
                    translateY, attribute);
        }
        return result;
    }

    /** Provide default HTML content by cloning any
     *  default WebExportable attributes provided by
     *  the configuration into the model. In the case
     *  of {@link DefaultIconScript} and {@link DefaultIconLink}
     *  objects, if the model contains one with the same event
     *  type, then the one from the configuration is not used.
     *  @exception IllegalActionException If cloning a configuration attribute fails.
     */
    protected void _provideDefaultContent() throws IllegalActionException {
        Configuration configuration = _basicGraphFrame.getConfiguration();
        if (configuration != null) {
            // Any instances of WebExportable contained by the
            // configuration are cloned into the model.
            NamedObj model = _basicGraphFrame.getModel();
            List<WebExportable> exportables = configuration
                    .attributeList(WebExportable.class);
            for (WebExportable exportable : exportables) {
                if (exportable instanceof Attribute) {
                    boolean foundOverride = false;
                    if (exportable instanceof DefaultIconScript) {
                        // Check whether the script provided by the model overrides the
                        // one given in the configurations. It does if the eventType matches
                        // and it either includes the same objects (Entities or Attributes) or
                        // it includes all objects, and the instancesOf that is specifies matches.
                        String eventType = ((DefaultIconScript) exportable).eventType
                                .stringValue();
                        String include = ((DefaultIconScript) exportable).include
                                .stringValue();
                        String instancesOf = ((DefaultIconScript) exportable).instancesOf
                                .stringValue();
                        List<DefaultIconScript> defaults = model
                                .attributeList(DefaultIconScript.class);
                        for (DefaultIconScript script : defaults) {
                            if (script.eventType.stringValue()
                                    .equals(eventType)
                                    && (script.include.stringValue().equals(
                                            include) || script.include
                                            .stringValue()
                                            .toLowerCase(Locale.getDefault())
                                            .equals("all"))
                                    && script.instancesOf.stringValue().equals(
                                            instancesOf)) {
                                // Skip this default from the configuration.
                                foundOverride = true;
                                break;
                            }
                        }
                    } else if (exportable instanceof DefaultIconLink) {
                        // Check whether the link default provided by the model overrides the
                        // one given in the configurations. It does if
                        // it either includes the same objects (Entities or Attributes) or
                        // it includes all objects, and the instancesOf that is specifies matches.
                        String include = ((DefaultIconLink) exportable).include
                                .stringValue();
                        String instancesOf = ((DefaultIconLink) exportable).instancesOf
                                .stringValue();
                        List<DefaultIconLink> defaults = model
                                .attributeList(DefaultIconLink.class);
                        for (DefaultIconLink script : defaults) {
                            if ((script.include.stringValue().equals(include) || script.include
                                    .stringValue()
                                    .toLowerCase(Locale.getDefault())
                                    .equals("all"))
                                    && script.instancesOf.stringValue().equals(
                                            instancesOf)) {
                                // Skip this default from the configuration.
                                foundOverride = true;
                                break;
                            }
                        }
                    }
                    if (foundOverride) {
                        continue;
                    }
                    try {
                        Attribute clone = (Attribute) ((Attribute) exportable)
                                .clone(model.workspace());
                        clone.setName(model.uniqueName(clone.getName()));
                        clone.setContainer(model);
                        clone.setPersistent(false);
                        // Make sure this appears earlier in the list of attributes
                        // than any contained by the model. The ones in the model should
                        // override the ones provided by the configuration.
                        clone.moveToFirst();
                    } catch (CloneNotSupportedException e) {
                        throw new InternalErrorException(
                                "Can't clone WebExportable attribute in Configuration: "
                                        + ((Attribute) exportable).getName());
                    } catch (NameDuplicationException e) {
                        throw new InternalErrorException(
                                "Failed to generate unique name for attribute in Configuration: "
                                        + ((Attribute) exportable).getName());
                    }
                }
            }
        }
    }

    /** Remove default HTML content, which includes all instances of
     *  WebExportable that are not persistent.
     *  @exception IllegalActionException If removing the attribute fails.
     */
    protected void _removeDefaultContent() throws IllegalActionException {
        NamedObj model = _basicGraphFrame.getModel();
        List<WebExportable> exportables = model
                .attributeList(WebExportable.class);
        for (WebExportable exportable : exportables) {
            if (exportable instanceof Attribute) {
                Attribute attribute = (Attribute) exportable;
                if (!attribute.isPersistent()) {
                    try {
                        attribute.setContainer(null);
                    } catch (NameDuplicationException e) {
                        throw new InternalErrorException(e);
                    }
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** The associated Vergil frame. */
    protected final BasicGraphFrame _basicGraphFrame;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Add HTML content at the specified position.  This content is not
     *  associated with any NamedObj.
     *  The position is expected to be one of "head", "start", "end",
     *  or anything else. In the latter case, the value
     *  of the position attribute is a filename
     *  into which the content is written.
     *  If <i>onceOnly</i> is true, then if identical content has
     *  already been added to the specified position, then it is not
     *  added again.
     *  @param position The position for the content.
     *  @param onceOnly True to prevent duplicate content.
     *  @param content The content to add.
     */

    private void _addContent(String position, boolean onceOnly, String content) {
        List<StringBuffer> contents = _contents.get(position);
        if (contents == null) {
            contents = new LinkedList<StringBuffer>();
            _contents.put(position, contents);
        }
        StringBuffer contentsBuffer = new StringBuffer(content);
        // Check to see whether contents are already present.
        if (onceOnly) {
            // FIXME: Will List.contains() work if two StringBuffers
            // are constructed from the same String?
            if (contents.contains(contentsBuffer)) {
                return;
            }
        }
        contents.add(contentsBuffer);
    }

    /** Add to the specified result list the bounds of the icon
     *  for the specified object.
     *  @param result The list to add to.
     *  @param viewSize The view size.
     *  @param scaleX The x scaling factor.
     *  @param scaleY The y scaling factor.
     *  @param translateX The x translation.
     *  @param translateY The y translation.
     *  @param object The object to add.
     */
    private void _addRectangle(List<IconVisibleLocation> result,
            Rectangle2D viewSize, double scaleX, double scaleY,
            double translateX, double translateY, NamedObj object) {
        Locatable location = null;
        try {
            location = (Locatable) object.getAttribute("_location",
                    Locatable.class);
        } catch (IllegalActionException e1) {
            // NOTE: What to do here? For now, ignoring the node.
        }
        if (location != null) {
            GraphController controller = _basicGraphFrame.getJGraph()
                    .getGraphPane().getGraphController();
            Figure figure = controller.getFigure(location);

            if (figure != null) {
                // NOTE: Calling getBounds() on the figure itself yields an
                // inaccurate bounds, for some reason.
                // Weirdly, to get the size right, we need to use the shape.
                // But to get the location right, we need the other!
                Rectangle2D figureBounds = figure.getShape().getBounds2D();

                // If the figure is composite, use the background figure
                // for the bounds instead.  NOTE: This seems to be a mistake.
                // The size and position information yielded appears to have
                // no relationship to reality.
                /*
                if (figure instanceof CompositeFigure) {
                    figure = ((CompositeFigure) figure).getBackgroundFigure();
                    figureBounds = figure.getShape().getBounds2D();
                }
                 */
                // Populate the data structure with bound information
                // relative to the visible rectangle.
                // Sadly, neither the figureOrigin nor the figureBounds
                // tells us where the figure is.  So we have quite a bit
                // of work to do.
                // First, get the width of the figure.
                // This is the only variable that does not depend
                // on the anchor.
                double width = figureBounds.getWidth();
                double height = figureBounds.getHeight();
                IconVisibleLocation i = new IconVisibleLocation();
                i.object = object;
                i.topLeftX = figureBounds.getX() * scaleX + translateX
                        - _PADDING;
                i.topLeftY = figureBounds.getY() * scaleY + translateY
                        - _PADDING;
                i.bottomRightX = i.topLeftX + width * scaleX + 2 * _PADDING;
                i.bottomRightY = i.topLeftY + height * scaleY + 2 * _PADDING;

                // If the rectangle is not visible, no more to do.
                if (i.bottomRightX < 0.0 || i.bottomRightY < 0.0
                        || i.topLeftX > viewSize.getWidth()
                        || i.topLeftY > viewSize.getHeight()) {
                    return;
                } else {
                    // Clip the rectangle so it does not include any portion
                    // that is not in the visible rectangle.
                    if (i.topLeftX < 0.0) {
                        i.topLeftX = 0.0;
                    }
                    if (i.topLeftY < 0.0) {
                        i.topLeftY = 0.0;
                    }
                    if (i.bottomRightX > viewSize.getWidth()) {
                        i.bottomRightX = viewSize.getWidth();
                    }
                    if (i.bottomRightY > viewSize.getHeight()) {
                        i.bottomRightY = viewSize.getHeight();
                    }
                    // Add the data to the result list.
                    // This is inserted at the start, not the end of the
                    // list so that in the image map, items in front appear
                    // earlier rather than later. This ensures that items
                    // in front take precedence.
                    result.add(0, i);
                }
            }
        }
    }

    /** Escape strings for inclusion as the value of HTML attribute.
     *  @param string The string to escape.
     *  @return Escaped string.
     */
    private String _escapeString(String string) {
        // This method is abstracted because it's not really clear
        // what should be escaped.
        String result = StringUtilities.escapeForXML(string);
        // Bizarrely, escaping all characters except newlines work.
        // Newlines need to be converted to \n.
        // No idea why so many backslashes are required below.
        // result = result.replaceAll("&#10;", "\\\\\\n");
        return result;
    }

    /** Construct a path the form "../../", for example, from the specified
     *  model to the specified copier, where the specified copier is either
     *  null or a any container above the specified model in the hierarchy.
     *  If the specified copier is null or is not a container above the
     *  specified model, then return null.
     *  @param model The model.
     *  @param path The path so far.
     *  @param copier The model responsible for the copying, which should be
     *   null if no copying is being done, equal to model if the model is
     *   responsible for copying, or a container of model if a container of model
     *   is doing the copying.
     */
    private String _findCopiedLibrary(NamedObj model, String path,
            NamedObj copier) {
        if (model == copier) {
            return path;
        }
        NamedObj container = model.getContainer();
        if (container == null) {
            // Got to the top level without finding an instance of CopyJavaScriptFiles.
            return null;
        }
        return _findCopiedLibrary(container, "../" + path, copier);
    }

    /** Return the contents of a toc.htm file
     *  that is located in either the current directory
     *  or ../../doc/
     *  @param model The model to be checked
     *  @return the contents of the toc.htm file or the empty string.
     */
    private static String _findToc(NamedObj model) {
        try {
            URIAttribute modelURI = (URIAttribute) model.getAttribute("_uri",
                    URIAttribute.class);
            if (modelURI != null) {
                // Look in the current directory for toc.htm or toc.html, then
                // look for ../../doc/toc.htm and then ../../doc/toc.html.
                File modelFile = new File(modelURI.getURI());
                File tocFile = new File(modelFile.getParent(), "toc.htm");
                if (!tocFile.exists()) {
                    tocFile = new File(modelFile.getParent(), "toc.html");
                    if (!tocFile.exists()) {
                        File docDirectory = new File(modelFile.getParent(), "../../doc/");
                        if (docDirectory.exists() 
                                && docDirectory.isDirectory()) {
                            tocFile = new File(docDirectory, "toc.htm");
                            if (!tocFile.exists()) {
                                tocFile = new File(docDirectory, "toc.html");
                                if (!tocFile.exists()) {
                                    tocFile = null;
                                }
                            }
                        }
                    }
                }
                if (tocFile == null) {
                    return "";
                } else {
                    // Read the contents and return it.
                    System.out.println("Copying the contents of " + tocFile);
                    StringBuffer result = new StringBuffer();
                    FileReader fileReader = null;
                    BufferedReader bufferedReader = null;
                    try {
                        fileReader = new FileReader(tocFile);
                        bufferedReader = new BufferedReader(fileReader);
                        String line = null;
                        while ((line = bufferedReader.readLine()) != null) {
                            result.append(line + "\n");
                        }
                    } catch (IOException x) {
                        System.err.format("IOException: %s%n", x);
                    } finally {
                        if (bufferedReader != null) {
                            bufferedReader.close();
                        }
                    }
                    return result.toString();
                }
            }
        } catch (Throwable throwable) {
            System.out.println("Failed to find toc for " + model.getFullName() + ": " + throwable);
            return "";
        }
        return "";
    }

    /** Return true if the model is in the domains demo directory
     *  and ../../../doc exists and is a directory and
     *  either ../../doc/index.htm or index.html exist
     *  @param model The model to be checked
     *  @return true if it is in the domains directory and doc exists.
     */
    private static boolean _isInDomains(NamedObj model) {
        try {
            URIAttribute modelURI = (URIAttribute) model.getAttribute("_uri",
                    URIAttribute.class);
            if (modelURI != null) {
                String modelURIString = modelURI.getURI().toString();
                if (modelURIString.contains("/domains")) {
                    try {
                        File modelFile = new File(modelURI.getURI());
                        File docDirectory = new File(modelFile, "../../../doc/");
                        if (docDirectory.exists() 
                                && docDirectory.isDirectory()
                                && (new File(docDirectory, "index.htm").exists()
                                        || new File(docDirectory, "index.html").exists())) {
                            return true;
                        }
                    } catch (Throwable throwable) {
                        return false;
                    }
                }
            }
        } catch (IllegalActionException ex) {
            return false;
        }
        return false;
    }

    /** Open a composite entity, if it is not already open,
     *  and recursively open any composite
     *  entities or state refinements that it contains.
     *  @param entity The entity to open.
     *  @param tableauxToClose A list of tableaux are newly opened.
     *  @param masterEffigy The top-level effigy for the modeling being exported.
     *  @param graphFrame The graph frame.
     *  @exception IllegalActionException If opening fails.
     *  @exception NameDuplicationException Not thrown.
     */
    private static void _openComposite(CompositeEntity entity,
            Set<Tableau> tableauxToClose, Effigy masterEffigy,
            BasicGraphFrame graphFrame) throws IllegalActionException {

        Configuration configuration = graphFrame.getConfiguration();
        Effigy effigy = configuration.getEffigy(entity);

        Tableau tableau;
        if (effigy != null) {
            // Effigy exists. See whether it has an open tableau.
            List<Tableau> tableaux = effigy.entityList(Tableau.class);
            if (tableaux == null || tableaux.size() == 0) {
                // No open tableau. Open one.
                tableau = configuration.createPrimaryTableau(effigy);
                tableauxToClose.add(tableau);
            } else {
                // The first tablequ is sufficient to retrieve the model.
                tableau = tableaux.get(0);
            }
        } else {
            // No pre-existing effigy.
            try {
                tableau = configuration.openModel(entity);
                tableauxToClose.add(tableau);
            } catch (NameDuplicationException e) {
                // This should not occur.
                throw new InternalErrorException(e);
            }
        }
        // NOTE: The entity that was opened may not actually be entity
        // because if it was an instance of a class, then class definition
        // will have been opened.
        CompositeEntity actualEntity = entity;
        if (tableau instanceof ActorGraphTableau) {
            PtolemyEffigy actualEffigy = (PtolemyEffigy) tableau.getContainer();
            actualEntity = (CompositeEntity) actualEffigy.getModel();
        }
        List<Entity> entities = actualEntity.entityList();
        for (Entity inside : entities) {
            _openEntity(inside, tableauxToClose, masterEffigy, graphFrame);
        }
    }

    /** Open the specified entity using the specified configuration.
     *  This method will recursively descend through the model, opening
     *  every composite actor and every state refinement.
     *  @param entity The entity to open.
     *  @param tableauxToClose A list of tableaux are newly opened.
     *  @param masterEffigy The top-level effigy for the modeling being exported.
     *  @param graphFrame The graph frame.
     */
    private static void _openEntity(Entity entity,
            Set<Tableau> tableauxToClose, Effigy masterEffigy,
            BasicGraphFrame graphFrame) throws IllegalActionException {
        if (entity instanceof CompositeEntity) {
            _openComposite((CompositeEntity) entity, tableauxToClose,
                    masterEffigy, graphFrame);
        } else if (entity instanceof State) {
            TypedActor[] refinements = ((State) entity).getRefinement();
            // refinements could be null, see ptolemy/domains/ptides/demo/PtidesBasicPowerPlant/PtidesBasicPowerPlant.xml
            if (refinements != null) {
                for (TypedActor refinement : refinements) {
                    _openComposite((CompositeEntity) refinement,
                            tableauxToClose, masterEffigy, graphFrame);
                }
            }
        }
    }

    /** Print the HTML in the _contents structure corresponding to the
     *  specified position to the specified writer. Each item in the
     *  _contents structure is written on one line.
     *  @param writer The writer to print to.
     *  @param position The position.
     */
    private void _printHTML(PrintWriter writer, String position) {
        List<StringBuffer> contents = _contents.get(position);
        for (StringBuffer content : contents) {
            writer.println(content);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** Data structure storing area attributes to for each Ptolemy II object. */
    private HashMap<NamedObj, HashMap<String, String>> _areaAttributes;

    /** Content added by position. */
    private HashMap<String, List<StringBuffer>> _contents;

    /** Content of the end section. */
    private LinkedList<StringBuffer> _end;

    /** Indicator that an export is in progress. */
    private static boolean _exportInProgress = false;

    /** Content of the head section. */
    private LinkedList<StringBuffer> _head;

    /** Padding around figures for bounding box. */
    private static double _PADDING = 4.0;

    /** The parameters of the current export, if there is one. */
    private ExportParameters _parameters;

    /** Indicator of whether title should be shown in HTML. */
    private boolean _showTitleInHTML = false;

    /** Content of the start section. */
    private LinkedList<StringBuffer> _start;

    /** The sanitized modelName */
    private String _sanitizedModelName;

    /** The title of the page. */
    private String _title = "Ptolemy II model";

    ///////////////////////////////////////////////////////////////////
    //// IconVisibleLocation

    /** A data structure consisting of a NamedObj and the coordinates
     *  of the upper left corner and lower right corner of the main
     *  part of its icon (not including decorations like the name
     *  and any highlights it may have). The coordinates are relative
     *  to the current visible rectangle, where the upper left corner
     *  of the visible rectangle has coordinates (0,0), and the lower
     *  right corner has coordinates (w,h), where w is the width
     *  and h is the height (in pixels).
     */
    static private class IconVisibleLocation {

        /** The object with a visible icon. */
        public NamedObj object;

        /** The top left X coordinate. */
        public double topLeftX;

        /** The top left Y coordinate. */
        public double topLeftY;

        /** The bottom right X coordinate. */
        public double bottomRightX;

        /** The bottom right Y coordinate. */
        public double bottomRightY;

        /** String representation. */
        @Override
        public String toString() {
            return object.getName() + " from (" + topLeftX + ", " + topLeftY
                    + ") to (" + bottomRightX + ", " + bottomRightY + ")";
        }
    }
}
