/* An Action that works with BasicGraphFrame to export HTML.

 Copyright (c) 1998-2011 The Regents of the University of California.
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

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
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
import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.BooleanToken;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.export.HTMLExportable;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.JCanvas;
import diva.canvas.toolbox.BasicFigure;
import diva.graph.GraphController;

/** An Action that works with BasicGraphFrame to export HTML.
 *  Given a directory, this action creates a GIF image of the
 *  currently visible portion of the BasicGraphFrame and an
 *  HTML page that displays that GIF image. In addition, it
 *  creates a map of the locations of actors in the GIF image
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
 * <dt>	-Dptolemy.ptII.exportHTML.usePtWebsite=true<.dt>
 * <dd> Include Ptolemy Website (<a href="http://ptolemy.org">http://ptolemy.org</a>)
 * specific Side Includes (SSI) and use JavaScript libraries from the
 * Ptolemy website.</dd>
 * <dt> -Dptolemy.ptII.exportHTML.linkToJNLP=true</dt>
 * <dd> Include a link to the a <code><i>sanitizedModelName</i>.jnlp</code> file.</dd>
 * </dl>
 *
 * <p>Typically, JVM properties are set when Java is invoked.  
 * {@link ptolemy.vergil.basic.export.image.ExportImage} can be called with these
 * properties set to create Ptolemy website specific web pages.</p>
 *
 * <p> See <a href="http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport">http://chess.eecs.berkeley.edu/ptexternal/wiki/Main/HTMLExport</a>
 * for detailed instructions about how to create web pages on the
 * Ptolemy website for models.</p>
 *
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Yellow (eal)
 * @Pt.AcceptedRating Red (eal)
 */
public class ExportHTMLAction extends AbstractAction implements HTMLExportable, WebExporter {

    /** Create a new action to export HTML.
     *  @param basicGraphFrame The Vergil window to export.
     */
    public ExportHTMLAction(BasicGraphFrame basicGraphFrame) {
        super("Export to Web");
        _basicGraphFrame = basicGraphFrame;
        putValue("tooltip", "Export HTML and GIF files showing this model.");
        // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Export a web page.  
     *  @param e The event that triggered this action.
     */
    public void actionPerformed(ActionEvent e) {
        NamedObj model = _basicGraphFrame.getModel();
        WebExportParameters defaultParameters = null;
        boolean restoreBlankDirectoryName = false;
        try {
            List<WebExportParameters> defaultParameterList = model.attributeList(WebExportParameters.class);
            if (defaultParameterList == null || defaultParameterList.size() == 0) {
                defaultParameterList = _basicGraphFrame.getConfiguration().attributeList(WebExportParameters.class);
                if (defaultParameterList == null || defaultParameterList.size() == 0) {
                    defaultParameters = new WebExportParameters(model, model.uniqueName("_defaultWebExportParameters"));
                    if (defaultParameters.directoryToExportTo.getExpression().trim().equals("")) {
                        // Directory is blank. Use the sanitized model name.
                        defaultParameters.directoryToExportTo.setExpression(
                                StringUtilities.sanitizeName(model.getName()));
                    }
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
            EditParametersDialog dialog = new EditParametersDialog(_basicGraphFrame, defaultParameters,
                    "Export to Web for " + model.getName());
            if (!dialog.buttonPressed().equals("Commit")) {
                return;
            }
            
            // If the directory is blank (it won't be unless the user
            // set it to be), then change it to match the santized name
            // of the model.
            if (defaultParameters.directoryToExportTo.getExpression().trim().equals("")) {
                // Make sure the finally clause below restores the blank value.
                restoreBlankDirectoryName = true;
                defaultParameters.directoryToExportTo.setExpression(
                        StringUtilities.sanitizeName(model.getName()));
            }
            
            final File directory = defaultParameters.directoryToExportTo.asFile();

            // directory should not be null, but check just in case.
            if (directory != null) {
                boolean run = ((BooleanToken)defaultParameters.runBeforeExport.getToken()).booleanValue();
                boolean open = ((BooleanToken)defaultParameters.openCompositesBeforeExport.getToken()).booleanValue();
                boolean show = ((BooleanToken)defaultParameters.showInBrowser.getToken()).booleanValue();
                exportToWeb(directory, _basicGraphFrame, run, open, show);
            }
        } catch (KernelException ex) {
            MessageHandler.error("Unable to export HTML.", ex);
        } finally {
            if (defaultParameters != null && restoreBlankDirectoryName) {
                defaultParameters.directoryToExportTo.setExpression("");
            }
        }
    }

    /** Add HTML content at the specified position.
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
    public void addContent(String position, boolean onceOnly, String content) {
        List<String> contents = _contents.get(position);
        if (contents == null) {
            contents = new LinkedList<String>();
            _contents.put(position, contents);
        }
        if (onceOnly) {
            // Check to see whether contents are already present.
            if (contents.contains(content)) {
                return;
            }
        }
        contents.add(content);
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
     *  by attributes of class {@link WebExportable} when their
     *  {@link WebExportable#defineAreaAttributes(WebExporter)} method
     *  is called by this exporter.
     *  @param object The object for which area elements are being added.
     *  @param attribute The attribute to add to the area element.
     *  @param value The value of the attribute.
     *  @param overwrite If true, overwrite any previously defined value for
     *   the specified attribute. If false, then do nothing if there is already
     *   an attribute with the specified name.
     *  @return True if the specified attribute and value was defined (i.e.,
     *   if there was a previous value, it was overwritten).
     */
    public boolean defineAreaAttribute(
            NamedObj object, String attribute, String value, boolean overwrite) {
        HashMap<String,String> areaTable = _areaAttributes.get(object);
        if (areaTable == null) {
            // No previously defined table. Add one.
            areaTable = new HashMap<String,String>();
            _areaAttributes.put(object, areaTable);
        }
        if (overwrite || areaTable.get(attribute) == null) {
            areaTable.put(attribute, _escapeString(value));
            return true;
        } else {
            return false;
        }
    }
    
    /** Export an HTML page and associated subpages to the specified directory.
     *  This method should be invoked in the swing thread.
     *  It will invoke a separate thread to run the model and do the export.
     *  @param directory The directory to write to.
     *  @param graphFrame The frame containing a model to export.
     *  @param run If true, run the model before exporting.
     *  @param open If true, open submodels before exporting.
     *  @param show If true, show the resulting web page in a browser after exporting.
     *  @return The thread that is spawned to (optionally) run and export the model,
     *   or null if the export is cancelled.
     */
    public static Thread exportToWeb(
            final File directory,
            final BasicGraphFrame graphFrame,
            final boolean run,
            final boolean open,
            final boolean show) {
        // See whether the directory has a file called index.html.
        final File indexFile = new File(directory, "index.html");
        if (directory.exists()) {
            // Previously, if directory existed and was a directory, we would always pop
            // up a dialog stating that the directory existed and that the contents would
            // be overwritten.  This seems excessive because the dialog will always
            // be shown.
            if (indexFile.exists()) {
                if (!MessageHandler.yesNoQuestion("\"" + directory
                        + "\" exists and contains an index.html file. Overwrite contents?")) {
                    MessageHandler.message("HTML export canceled.");
                    return null;
                }
            }
            if (!directory.isDirectory()) {
                if (!MessageHandler
                        .yesNoQuestion("\"" + directory
                                + "\" is a file, not a directory. Delete the file named \""
                                + directory + "\" and create a directory with that name?")) {
                    MessageHandler.message("HTML export canceled.");
                    return null;
                }
                if (!directory.delete()) {
                    MessageHandler.message("Unable to delete file \"" + directory + "\".");
                    return null;
                }
                if (!directory.mkdir()) {
                    MessageHandler.message("Unable to create directory \"" + directory + "\".");
                    return null;
                }
            }
        } else {
            if (!directory.mkdir()) {
                MessageHandler.message("Unable to create directory \"" + directory + "\".");
                return null;
            }
        }

        final Set<Tableau> tableauxToClose = new HashSet<Tableau>();
        try {
            // Open submodels, if appropriate.
            if (open) {
                CompositeActor model = (CompositeActor)graphFrame.getModel();
                Effigy masterEffigy = Configuration.findEffigy(model);
                List<Entity> entities = model.entityList();
                for (Entity entity : entities) {
                    _openEntity(entity, tableauxToClose, masterEffigy, graphFrame);
                }
            }
        } catch (Exception ex) {
            MessageHandler.error("Unable to export to web.", ex);
            throw new RuntimeException(ex);
        }
        // Running the model has to occur in a new thread, or the whole
        // process could hang (if the model doesn't return). So finish in a new thread.
        // That thread will, in turn, have to again invoke the swing event thread
        // to close any tableaux that were opened above.
        // It does not wait for the close to complete before finishing itself.
        Runnable exportAction = new Runnable() {
            public void run() {
                CompositeActor model = (CompositeActor)graphFrame.getModel();
                
                // If parameters are set to run the model, then do that.
                if (run && model instanceof CompositeActor) {
                    // Run the model.
                    try {
                        Manager manager = model.getManager();
                        if (manager == null) {
                            manager = new Manager(model.workspace(),
                                    "MyManager");
                            model.setManager(manager);
                        }
                        manager.execute();
                        
                        // Export to web.
                        graphFrame.writeHTML(directory);
                    } catch (Exception ex) {
                        MessageHandler.error("Model execution failed.", ex);
                        throw new RuntimeException(ex);
                    } finally {
                        Runnable closeTableaux = new Runnable() {
                            public void run() {
                                for (Tableau tableau : tableauxToClose) {
                                    tableau.close();
                                }                                
                            }
                        };
                        SwingUtilities.invokeLater(closeTableaux);
                    }
                }
                                
                // Finally, if requested, show the exported page.
                if (show) {
                    Configuration configuration = graphFrame.getConfiguration();
                    try {
                        URL indexURL = new URL(indexFile.toURI().toURL().toString() + "#in_browser");
                        configuration.openModel(indexURL, indexURL, indexURL.toExternalForm(),
                                BrowserEffigy.staticFactory);
                    } catch (Throwable throwable) {
                        MessageHandler.error("Failed to open \"" + indexFile + "\".", throwable);
                        throw new RuntimeException(throwable);
                    }
                }
            }
        };
        // Invoke the new thread.
        Thread result = new Thread(exportAction);
        result.start();
        return result;
    }

    /** During invocation of {@link #writeHTML(File)}, return the directory being written to.
     *  @return The directory being written to.
     */
    public File getExportDirectory() {
        return _exportDirectory;
    }

    /** The frame (window) being exported to HTML.
     *  @return The frame provided to the constructor.
     */
    public PtolemyFrame getFrame() {
        return _basicGraphFrame;
    }

    /** Set the title to be used for the page being exported.
     *  @param title The title.
     *  @param showInHTML True to produce an HTML title prior to the model image.
     */
    public void setTitle(String title, boolean showInHTML) {
        _title = StringUtilities.escapeForXML(title);
        _showTitleInHTML = showInHTML;
    }

    /** Write an HTML page based on the current view of the model
     *  to the specified destination directory. The file will be
     *  named "index.html," and supporting files, including at
     *  least a gif image showing the contents currently visible in
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
     *  @param directory The directory in which to put any associated files.
     *  @exception IOException If unable to write associated files.
     *  @exception PrinterException If unable to write associated files.
     *  @throws IllegalActionException If reading parameters fails.
     */
    public void writeHTML(File directory) throws PrinterException, IOException, IllegalActionException {
        // First, create the gif file showing whatever the current
        // view in this frame shows.
        NamedObj model = _basicGraphFrame.getModel();
        // Use a sanitized model name and avoid problems with special characters in file names.
        _sanitizedModelName = StringUtilities.sanitizeName(model.getName());
        File gifFile = new File(directory, _sanitizedModelName + ".gif");
        OutputStream out = new FileOutputStream(gifFile);
        try {
            _basicGraphFrame.writeImage(out, "gif");
        } finally {
            out.close();
        }
        // Initialize the data structures into which content is collected.
        _areaAttributes = new HashMap<NamedObj,HashMap<String,String>>();
        _contents = new HashMap<String,List<String>>();
        _end = new LinkedList<String>();
        _head = new LinkedList<String>();
        _start = new LinkedList<String>();
        _contents.put("head", _head);
        _contents.put("start", _start);
        _contents.put("end", _end);
        
        // The following try...finally block ensures that the index and toc files
        // get closed even if an exception occurs. It also resets _exportDirectory.
        PrintWriter index = null;
        try {
            _exportDirectory = directory;

            // Clone instances of WebExportable from the Configuration
            // into the model. These are removed in the finally clause
            // of the try block.
            _provideDefaultContent();

            // Next, collect the web content specified by the instances
            // of WebExportable contained by the model.
            List<WebExportable> exportables = model.attributeList(WebExportable.class);
            for (WebExportable exportable : exportables) {
                exportable.provideContent(this);
            }
            
            // If a title has been specified and set to show, then
            // add it to the start HTML section at the beginning.
            if (_showTitleInHTML) {
                _start.add(0, "<h1>");
                _start.add(1, _title);
                _start.add(2, "</h1>\n");
            }

            // Next, collect the web content specified by the contained
            // objects of the model.
            // This looks for outside web content for each object.
            Iterator<NamedObj> contentsIterator = model.containedObjectsIterator();
            while (contentsIterator.hasNext()) {
                NamedObj containedObject = contentsIterator.next();
                exportables = containedObject.attributeList(WebExportable.class);
                for (WebExportable exportable : exportables) {
                    exportable.provideOutsideContent(this);
                }
            }

            // Next, create an HTML file.
	    // Invoke with -Dptolemy.ptII.usePtWebsite=true to get Server
	    // Side Includes (SSI).  FIXME: this is a bit of a hack, we should
	    // use templates instead.
	    boolean usePtWebsite = Boolean.valueOf(StringUtilities.getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));

	    Writer indexWriter = new FileWriter(new File(directory, "index.html"));
	    index = new PrintWriter(indexWriter);

	    // Generate a header that will pass the HTML validator at
	    // http://validator.w3.org/
	    // We use println so as to get the correct eol character for
	    // the local platform.
	    index.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
	    index.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-US\" lang=\"en-US\">");
	    index.println("<head>");
	    index.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"/>");

	    // Define the path to the SSI files on the ptolemy site.
	    String ssiRoot = "http://ptolemy.org/";
	    if (usePtWebsite) {
	        // FIXME: this absolute path is not very safe.  The
	        // problem is that we don't know where $PTII is located on
	        // the website.
	        index.println("<link href=\""
	                + ssiRoot
	                + "ptolemyII/ptIIlatest/ptII/doc/default.css\" rel=\"stylesheet\" type=\"text/css\"/>");
	    }

	    // Title needed for the HTML validator.
	    index.println("<title>" + _title + "</title>");

	    if (usePtWebsite) {
	        // Reference the server-side includes.
	        index.println("<!--#include virtual=\"/ssi/toppremenu.htm\" -->");
	        index.println("<!--#include virtual=\"toc.htm\" -->");
	        index.println("<!--#include virtual=\"/ssi/toppostmenu.htm\" -->");
	    }

	    if (usePtWebsite) {
	        addContent("toc.htm", false, "<div id=\"menu\">");
	        addContent("toc.htm", false, "<ul>");
	        addContent("toc.htm", false, "<li><a href=\"/index.htm\">Ptolemy Home</a></li>");
	        addContent("toc.htm", false, "</ul>");
	        addContent("toc.htm", false, "");
	        addContent("toc.htm", false, "<ul>");
	        addContent("toc.htm", false, " <li><a href=\"../index.html\">Up</a></li>");
	        addContent("toc.htm", false, "</ul>");
	        addContent("toc.htm", false, "<ul>");
	    }
	    
	    // Reference required script files.
	    // If the model contains an instanceof CopyJavaScriptFiles, then
	    // the required files will have been copied into a directory called
	    // "javascript" in the top-level directory of the export.
	    // Otherwise, we want to reference these files at http://ptolemy.org/.
	    // If the usePtWebsite property is true, then reference the files
	    // at http://ptolemy.org/ whether the property is true or not.
	    String jsLibrary = ssiRoot;
	    if (!usePtWebsite) {
	        // If the model or container above it in the hierarchy has
	        // an instance of CopyJavaScriptFiles in it, then set up the
	        // references to refer to the copied files rather than the
	        // website files.
	        // FIXME: This can fail if we export a submodel only but
	        // the enclosing model has an instance of CopyJavaScriptFiles!
	        String copiedLibrary = _findCopiedLibrary(model, "");
	        if (copiedLibrary != null) {
	            jsLibrary = copiedLibrary;
	        }
	    }
            // NOTE: Due to a bug somewhere (browser, Javascript, etc.), can't end this with />. Have to use </script>.
	    index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "javascript/" + FILENAMES[0] + "\"></script>");
            index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "javascript/" + FILENAMES[1] + "\"></script>");
            index.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + jsLibrary + "javascript/" + FILENAMES[2] + "\" media=\"screen\"/>");
            index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "javascript/" + FILENAMES[3] + "\"></script>");
            // Could alternatively use a CDS (Content Delivery Service) for the JavaScript library for jquery.
            // index.println("<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js\"></script>");

            // Next, create the image map.
            String map = _createImageMap(directory);

	    // Write the main part of the HTML file.
            _printHTML(index, "head");
	    index.println("</head><body>");
            _printHTML(index, "start");

	    boolean linkToJNLP = Boolean.valueOf(StringUtilities.getProperty("ptolemy.ptII.exportHTML.linkToJNLP"));
            if (linkToJNLP && model.getContainer() == null) {
                index.println("Below is a browsable image of the model. "
                        + "For an executable version, go to the "
                        + "<a href=\"../" + _sanitizedModelName + ".jnlp\">WebStart version</a>.");
            }
	    // Put the image in.
	    index.println("<img src=\"" + _sanitizedModelName
	            + ".gif\" usemap=\"#iconmap\"/>");
	    index.println(map);
            _printHTML(index, "end");
            
	    if (!usePtWebsite) {
	        index.println("</body>");
	        index.println("</html");
	    } else {
	        index.println("<!-- /body -->");
	        index.println("<!-- /html -->");
	        index.println("<!--#include virtual=\"/ssi/bottom.htm\" -->");

	        addContent("toc.htm", false, " </ul>");
	        addContent("toc.htm", false, "</ul>");
	        addContent("toc.htm", false, "</div><!-- /#menu -->");
	    }
	    
            // If _contents contains any entry other than head, start, or end,
            // then interpret that entry as a file name to write to.
            for (String key : _contents.keySet()) {
                if (!key.equals("end") && !key.equals("head") && !key.equals("start")) {
                    // NOTE: A RESTful version of this would create a resource
                    // that could be addressed by a URL. For now, we just
                    // write to a file. Java documentation doesn't say
                    // whether the following overwrites a pre-existing file,
                    // but it does seem to do that, so I assume that's what it does.
                    Writer fileWriter = new FileWriter(new File(directory, key));
                    PrintWriter printWriter = new PrintWriter(fileWriter);
                    List<String> contents = _contents.get(key);
                    for (String line : contents) {
                        printWriter.println(line);                        
                    }
                    printWriter.close();
                }
            }
	} finally {
	    _exportDirectory = null;
	    _removeDefaultContent();
	    if (index != null) {
		index.close(); // Without this, the output file may be empty
	    }
	}
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         package protected methods         ////

    /** List of filenames needed by jquery and fancybox.
     *  These are automatically provided to every exported web page
     *  either by referencing the ptolemy.org website (the default)
     *  or by copying the files into the target directory (if the
     *  model contains an instance of CopyJavaScriptFiles).
     *  The first three of these should be the JavaScript files to include,
     *  and the fourth should be the CSS file.
     *  The rest are image files to copy over.
     */
    // FIXME: I don't like the hardwired version numbers here.
    // Findbugs wants this package protected and final.
    final static String[] FILENAMES = {
            "jquery-1.4.3.min.js",
            "jquery.fancybox-1.3.4.pack.js",
            "jquery.fancybox-1.3.4.css",
            "pt-1.0.0.js",
            "blank.gif",
            "fancybox.png",
            "fancybox-y.png",
            "fancybox-x.png",
            "fancy_title_right.png",
            "fancy_title_over.png",
            "fancy_title_main.png",
            "fancy_title_left.png",
            "fancy_shadow_w.png",
            "fancy_shadow_sw.png",
            "fancy_shadow_se.png",
            "fancy_shadow_s.png",
            "fancy_shadow_nw.png",
            "fancy_shadow_ne.png",
            "fancy_shadow_n.png",
            "fancy_shadow_e.png",
            "fancy_nav_right.png",
            "fancy_nav_left.png",
            "fancy_loading.png",
            "fancy_close.png",
            "javascript-license.htm"
    };

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the image map. As a side effect, this may create other
     *  HTML files or subdirectories.
     *  @param directory The directory into which to write any HTML
     *   that is created as a side effect.
     *  @throws PrinterException If writing to the toc file fails.
     *  @throws IOException If IO fails.
     *  @throws IllegalActionException If reading parameters fails.
     */
    protected String _createImageMap(File directory)
            throws IllegalActionException, IOException, PrinterException {
        StringBuffer result = new StringBuffer();
        result.append("<map name=\"iconmap\">\n");
        
        // Iterate over the icons.
        List<IconVisibleLocation> iconLocations = _getIconVisibleLocations();
        for (IconVisibleLocation location : iconLocations) {
            // This string will have at least one space at the start and the end.
            StringBuffer attributeString = new StringBuffer();
            attributeString.append(" ");
            HashMap<String,String> areaAttributes = _areaAttributes.get(location.object);
            if (areaAttributes != null) {
                //for (String key : areaAttributes.keySet()) {
                //    String value = areaAttributes.get(key);
                for (Map.Entry<String,String> entry : areaAttributes.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // If the value is empty, omit the entry.
                    if (value != null && !value.trim().equals("")) {
                        attributeString.append(key);
                        attributeString.append("=\"");
                        attributeString.append(StringUtilities.escapeString(value));
                        attributeString.append("\" ");
                    }
                }
            }
                        
            // Write the name of the actor followed by the table.
            result.append("<area shape=\"rect\" coords=\""
                    + (int) location.topLeftX + "," + (int) location.topLeftY
                    + "," + (int) location.bottomRightX + ","
                    + (int) location.bottomRightY
                    + "\""
                    + attributeString
                    + "/>\n");
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

    /** Return the title of the specified object. If it contains a parameter
     *  of class {@see Title}, then return the title specified by that class.
     *  Otherwise, if the object is an instance of FSMActor contained by
     *  a ModalModel, then return the
     *  name of its container, not the name of the FSMActor.
     *  Otherwise, return the name of the object.
     *  @return A title for the object.
     *  @throws IllegalActionException If accessing the title attribute fails..
     */
    protected static String _getTitleText(NamedObj object) throws IllegalActionException {
        // If the object contains an IconLink parameter, then use that instead of the default.
        // If it has more than one, then just use the first one.
        List<Title> links = object.attributeList(Title.class);
        if (links != null && links.size() > 0) {
            return links.get(0).stringValue();
        }
        if (object instanceof FSMActor) {
            NamedObj container = object.getContainer();
            if (container instanceof ModalModel) {
                return container.getName();
            }
        }
        return object.getName();
    }
    
    /** Provide default HTML content by cloning any
     *  default WebExportable attributes provided by
     *  the configuration into the model. In the case
     *  of {@link DefaultIconScript} and {@link DefaultIconLink}
     *  objects, if the model contains one with the same event
     *  type, then the one from the configuration is not used.
     *  @throws IllegalActionException If cloning a configuration attribute fails.
     */
    protected void _provideDefaultContent() throws IllegalActionException {
        Configuration configuration = _basicGraphFrame.getConfiguration();
        if (configuration != null) {
            // Any instances of WebExportable contained by the
            // configuration are cloned into the model.
            NamedObj model = _basicGraphFrame.getModel();
            List<WebExportable> exportables = configuration.attributeList(WebExportable.class);
            for (WebExportable exportable : exportables) {
                if (exportable instanceof Attribute) {
                    boolean foundOverride = false;
                    if (exportable instanceof DefaultIconScript) {
                        // Check whether the script provided by the model overrides the
                        // one given in the configurations. It does if the eventType matches
                        // and it either includes the same objects (Entities or Attributes) or
                        // it includes all objects, and the instancesOf that is specifies matches.
                        String eventType = ((DefaultIconScript)exportable).eventType.stringValue();
                        String include = ((DefaultIconScript)exportable).include.stringValue();
                        String instancesOf = ((DefaultIconScript)exportable).instancesOf.stringValue();
                        List<DefaultIconScript> defaults = model.attributeList(DefaultIconScript.class);
                        for (DefaultIconScript script : defaults) {
                            if (script.eventType.stringValue().equals(eventType) &&
                                    (script.include.stringValue().equals(include)
                                    || script.include.stringValue().toLowerCase().equals("all"))
                                    && script.instancesOf.stringValue().equals(instancesOf)) {
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
                        String include = ((DefaultIconLink)exportable).include.stringValue();
                        String instancesOf = ((DefaultIconLink)exportable).instancesOf.stringValue();
                        List<DefaultIconLink> defaults = model.attributeList(DefaultIconLink.class);
                        for (DefaultIconLink script : defaults) {
                            if ((script.include.stringValue().equals(include)
                                    || script.include.stringValue().toLowerCase().equals("all"))
                                    && script.instancesOf.stringValue().equals(instancesOf)) {
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
                        Attribute clone = (Attribute)((Attribute)exportable).clone(model.workspace());
                        clone.setName(model.uniqueName(clone.getName()));
                        clone.setContainer(model);
                        clone.setPersistent(false);
                        // Make sure this appears earlier in the list of attributes
                        // than any contained by the model. The ones in the model should
                        // override the ones provided by the configuration.
                        clone.moveToFirst();
                    } catch (CloneNotSupportedException e) {
                        throw new InternalErrorException("Can't clone WebExportable attribute in Configuration: " 
                                + ((Attribute)exportable).getName());
                    } catch (NameDuplicationException e) {
                        throw new InternalErrorException("Failed to generate unique name for attribute in Configuration: " 
                                + ((Attribute)exportable).getName());
                    }
                }
            }
        }
    }    
    
    /** Remove default HTML content, which includes all instances of
     *  WebExportable that are not persistent.
     *  @throws IllegalActionException If removing the attribute fails.
     */
    protected void _removeDefaultContent() throws IllegalActionException {
        NamedObj model = _basicGraphFrame.getModel();
        List<WebExportable> exportables = model.attributeList(WebExportable.class);
        for (WebExportable exportable : exportables) {
            if (exportable instanceof Attribute) {
                Attribute attribute = (Attribute)exportable;
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
    ////                       protected methods                   ////

    /** The associated Vergil frame. */
    protected final BasicGraphFrame _basicGraphFrame;
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
                Point2D figureOrigin = figure.getOrigin();

                // NOTE: Calling getBounds() on the figure itself yields an
                // inaccurate bounds, for some reason.
                // Weirdly, to get the size right, we need to use the shape.
                // But to get the location right, we need the other!
                Rectangle2D figureBounds = figure.getShape().getBounds2D();
                
                // If the figure is composite, use the background figure 
                // for the bounds instead.
                if (figure instanceof CompositeFigure) {
                    figure = ((CompositeFigure) figure).getBackgroundFigure();
                    figureBounds = figure.getShape().getBounds2D();
                }
                boolean isCentered = false;
                if (figure instanceof BasicFigure) {
                    isCentered = ((BasicFigure) figure).isCentered();
                }

                double iconX = figureOrigin.getX() + figureBounds.getX();
                double iconY = figureOrigin.getY() + figureBounds.getY();
                
                IconVisibleLocation i = new IconVisibleLocation();
                i.object = object;

                // Calculate the location of the icon relative to the visible rectangle.
                i.topLeftX = iconX * scaleX + translateX;
                i.topLeftY = iconY  * scaleY + translateY;
                i.bottomRightX = (iconX + figureBounds.getWidth()) * scaleX + translateX;
                i.bottomRightY = (iconY + figureBounds.getHeight()) * scaleY + translateY;
                
                // Correction needed if the figure is centered (sadly...
                // that's how AWT APIs work, I guess... you have to guess what it means).
                if (isCentered) {
                    double widthOffset = figureBounds.getWidth()/2.0;
                    double heightOffset = figureBounds.getHeight()/2.0;
                    i.topLeftX -= widthOffset;
                    i.topLeftY -= heightOffset;
                    i.bottomRightX -= widthOffset;
                    i.bottomRightY -= heightOffset;
                }

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
                    result.add(i);
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
    
    /** If the specified model contains an instance of CopyJavaScriptFiles,
     *  then return the specified path. Otherwise, any container above
     *  the specified model in the hierarchy contains an instance of
     *  CopyJavaScriptFiles, then return a path of the form "../../"
     *  repeated as many times as necessary to get to the path of the
     *  copied files. If there is no instance of CopyJavaScriptFiles,
     *  then return null.
     *  @param model The model.
     *  @param path The path so far.
     */
    private String _findCopiedLibrary(NamedObj model, String path) {
        List<CopyJavaScriptFiles> copy = model.attributeList(CopyJavaScriptFiles.class);
        if (copy != null && copy.size() > 0) {
            return path;
        }
        NamedObj container = model.getContainer();
        if (container == null) {
            // Got to the top level without finding an instance of CopyJavaScriptFiles.
            return null;
        }
        return _findCopiedLibrary(container, "../" + path);
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
            Set<Tableau> tableauxToClose, Effigy masterEffigy, BasicGraphFrame graphFrame)
           throws IllegalActionException, NameDuplicationException {
        
        Configuration configuration = graphFrame.getConfiguration();
        Effigy effigy = configuration.getEffigy(entity);

        if (effigy != null) {
            // Effigy exists. See whether it has an open tableau.
            List<Tableau> tableaux = effigy.entityList(Tableau.class);
            if (tableaux == null || tableaux.size() == 0) {
                // No open tableau. Open one.
                tableauxToClose.add(configuration.createPrimaryTableau(effigy));
            }
        } else {
            // No pre-existing effigy.
            Tableau tableau = configuration.openModel(entity);
            tableauxToClose.add(tableau);
        }
        List<Entity> entities = (entity).entityList();
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
    private static void _openEntity(
            Entity entity, Set<Tableau> tableauxToClose, Effigy masterEffigy, BasicGraphFrame graphFrame)
            throws IllegalActionException, NameDuplicationException {
        if (entity instanceof CompositeEntity) {
            _openComposite((CompositeEntity) entity, tableauxToClose, masterEffigy, graphFrame);
        } else if (entity instanceof State) {
            TypedActor[] refinements = ((State) entity).getRefinement();
            for (TypedActor refinement : refinements) {
                _openComposite((CompositeEntity) refinement, tableauxToClose, masterEffigy, graphFrame);
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
        List<String> contents = _contents.get(position);
        for (String content : contents) {
            writer.println(content);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////
    
    /** Data structure storing area attributes to for each Ptolemy II object. */
    private HashMap<NamedObj,HashMap<String,String>> _areaAttributes;

    /** Content added by position. */
    private HashMap<String,List<String>> _contents;
    
    /** Content of the end section. */
    private LinkedList<String> _end;

    /** The directory into which we are writing. */
    private File _exportDirectory;

    /** Content of the head section. */
    private LinkedList<String> _head;
    
    /** Indicator of whether title should be shown in HTML. */
    private boolean _showTitleInHTML = false;
    
    /** Content of the start section. */
    private LinkedList<String> _start;
    
    /** The title of the page. */
    private String _title = "Ptolemy II model";

    // The sanitized modelName
    private String _sanitizedModelName;

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
        public String toString() {
            return (object.getName() + " from (" + topLeftX + ", " + topLeftY
                    + ") to (" + bottomRightX + ", " + bottomRightY + ")");
        }
    }

}
