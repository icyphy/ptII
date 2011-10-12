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
package ptolemy.vergil.basic;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import ptolemy.actor.TypedActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.StringParameter;
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Instantiable;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.JCanvas;
import diva.graph.GraphController;

/** An Action that works with BasicGraphFrame to export HTML.
 *  Given a directory, this action creates a GIF image of the
 *  currently visible portion of the BasicGraphFrame and an
 *  HTML page that displays that GIF image. In addition, it
 *  creates a map of the locations of actors in the GIF image
 *  and actions associated with each of the actors.
 *  The following actions are supported:
 *  <ul>
 *  <li> A mouse-over handler that, by default, displays parameter
 *       values in a table when the mouse passes over an actor.
 *       This default can be overridden by inserting into the
 *       actor a parameter named <i>_onMouseOverText</i>. The
 *       value of that parameter provides HTML text that will
 *       be displayed on mouse over instead of the parameter
 *       value table. This text can reference variables in scope
 *       using the usual mechanisms for string-valued parameters.
 *       For example, if the actor has a parameter named <i>p</i>,
 *       then its value can be displayed by setting
 *       <i>_onMouseOverText</i> to "value of p: $(this.p)".
 *       <p>
 *       If instead (or in addition) the actor
 *       has a parameter named <i>_onMouseOverAction</i>, then
 *       the value of that parameter provides a JavaScript
 *       command that will be invoked on mouse over.
 *       For example, if the value of <i>_onMouseOverAction</i>
 *       is a string "writeText('value of p: $(this.p)')", then
 *       the effect will be the same as in the example above.
 *       The writeText command is defined by default
 *       header text, which can be overridden to provide
 *       other JavaScript function definitions (see below)
 *       <p>
 *  FIXME: 
 *  <li> Moreover, if any plot windows are open when the HTML is
 *  exported, then GIF images of those plot windows are linked
 *  to the plotter icons so that clicking on the icons causes
 *  the plot window to appear in a lightbox style.
 *  In addition, if any composite actors are open, then
 *  clicking on the composite actors will take the viewer
 *  to a new HTML page showing the inside of the composite actor.
 *  </ul>
 *  <p>
 *  In addition to the actions on the regions of the GIF
 *  image, the model can specify text to include in the
 *  header of the HTML file, HTML text to put
 *  before the GIF image, and HTML text to put after the
 *  GIF image.  These are done as follows:
 *  <ul>
 *  <li> Text to include in the header of the HTML file
 *       can be specified by inserting into the model a
 *       <i>_headerText</i> parameter. If no such parameter
 *       is provided, then the following header text is
 *       inserted in the file:
<pre>
&lt;script type="text/javascript"&gt;
function writeText(text) {
  document.getElementById("afterImage").innerHTML = text;
}
&lt;/script&gt;
</pre>
 *         Notice that this defines a function <i>writeText</i>
 *         which can used to insert text into a document element
 *         with ID "afterImage" (see below).
 *         <p>
 *  <li> Text to include before the image in the HTML file
 *       can be specified by inserting into the model a
 *       <i>_beforeImage</i> parameter. If no such parameter
 *       is provided, then the following text is
 *       inserted in the file before the image:
 *       <pre>&lt;h1&gt;modelName&lt;/h1&gt;</pre>
 *       where <i>modelName</i>
 *       is the name of the model.
 *  <li> Text to include after the image in the HTML file
 *       can be specified by inserting into the model a
 *       <i>_afterImage</i> parameter. If no such parameter
 *       is provided, then the following text is
 *       inserted in the file after the image:
 <pre>
 &lt;p id="afterImage"&gt;Mouse over the actors to see their parameters. Click on composites and plotters to reveal their contents (if provided).&lt;/p&gt;
 </pre>
 *       Notice that defines the document element with ID afterImage.
 *  </ul>
 *
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class ExportHTMLAction extends AbstractAction implements HTMLExportable {

    /** Create a new action to export HTML.
     * @param basicGraphFrame TODO
     */
    public ExportHTMLAction(BasicGraphFrame basicGraphFrame) {
        super("Export to Web");
        _basicGraphFrame = basicGraphFrame;
        putValue("tooltip", "Export HTML and GIF files showing this model.");
        // putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_G));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Export an HTML image map.
     *  @param e The event that triggered this action.
     */
    public void actionPerformed(ActionEvent e) {
        // Open a file chooser to select a folder to write to.
        JFileChooser fileDialog = new JFileChooser();
        fileDialog
                .addChoosableFileFilter(new BasicGraphFrame.FolderFileFilter());
        fileDialog.setDialogTitle("Choose a directory to write HTML...");
        fileDialog.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        File modelDirectory = _basicGraphFrame._getDirectory();
        if (modelDirectory != null) {
            fileDialog.setCurrentDirectory(modelDirectory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            String cwd = StringUtilities.getProperty("user.dir");

            if (cwd != null) {
                fileDialog.setCurrentDirectory(new File(cwd));
            }
        }
        int returnVal = fileDialog.showDialog(_basicGraphFrame, "Export HTML");

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File directory = fileDialog.getSelectedFile();
            if (directory.exists()) {
                if (directory.isDirectory()) {
                    if (!MessageHandler.yesNoQuestion("Directory exists: "
                            + directory + ". Overwrite contents?")) {
                        MessageHandler.message("HTML export canceled.");
                        return;
                    }
                } else {
                    if (!MessageHandler
                            .yesNoQuestion("File exists with the same name. Overwrite file?")) {
                        MessageHandler.message("HTML export canceled.");
                        return;
                    }
                    if (!directory.delete()) {
                        MessageHandler.message("Unable to delete file.");
                        return;
                    }
                    if (!directory.mkdir()) {
                        MessageHandler.message("Unable to create directory.");
                        return;
                    }
                }
            } else {
                if (!directory.mkdir()) {
                    MessageHandler.message("Unable to create directory.");
                    return;
                }
            }
            // At this point, file is a directory and we have permission
            // to overwrite its contents.
            try {
                _basicGraphFrame.writeHTML(directory);
            } catch (IOException ex) {
                MessageHandler.error("Unable to export HTML.", ex);
                return;
            } catch (PrinterException e1) {
                MessageHandler.error("Failed to created associated files.", e1);
                return;
            } catch (IllegalActionException e2) {
                MessageHandler.error("Error occurred accessing model.", e2);
                return;
            }
        }
    }

    /** Write an HTML page based on the current view of the model
     *  to the specified destination directory. The file will be
     *  named "index.html," and supporting files, including at
     *  least a gif image showing the contents currently visible in
     *  the graph frame, will be created. If there are any plot windows
     *  open or any composite actors open, then gif and/or HTML will
     *  be generated for those as well and linked to the gif image
     *  created for this frame.
     *  <p>
     *  The generated page has a header with the name of the model,
     *  a reference to a GIF image file with name equal to the name
     *  of the model with a ".gif" extension appended, and a script
     *  that reacts when the mouse is moved over an actor by
     *  displaying a table with the parameter values of the actor.
     *  The gif image is assumed to have been generated with the
     *  current view using the
     *  {@link ptolemy.vergil.basic.BasicGraphFrame#writeImage(OutputStream, String)}
     *  method.</p>
     *
     *  <p>If the "ptolemy.ptII.exportHTML.usePtWebsite" property is set to true,
     *  then the html files will have Ptolemy website specific Server Side Includes (SSI)
     *  code and use the jsquery and jsquery.lightbox files from the Ptolemy website.
     *  In addition, a toc.htm file will be created to aid in navigation.
     *  This facility is not likely to be portable to other websites.</p>
     *
     *  @param directory The directory in which to put any associated files.
     *  @exception IOException If unable to write associated files.
     *  @exception PrinterException If unable to write associated files.
     * @throws IllegalActionException 
     */
    public void writeHTML(File directory) throws PrinterException, IOException, IllegalActionException {
        // First, create the gif file showing whatever the current
        // view in this frame shows.
        NamedObj model = _basicGraphFrame.getModel();
        File gifFile = new File(directory, model.getName() + ".gif");
        OutputStream out = new FileOutputStream(gifFile);
        try {
            _basicGraphFrame.writeImage(out, "gif");
        } finally {
            out.close();
        }

	PrintWriter index = null;
	PrintWriter toc = null;
	try {

        // Next, create an HTML file.

        // Invoke with -Dptolemy.ptII.usePtWebsite=true to get Server
        // Side Includes (SSI) and use JavaScript libraries from the
        // Ptolemy website.  FIXME: this is a bit of a hack, we should
        // use templates instead.
        boolean usePtWebsite = Boolean.valueOf(StringUtilities.getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));

        Writer indexWriter = new FileWriter(new File(directory, "index.html"));
        index = new PrintWriter(indexWriter);

        Writer tocWriter = new FileWriter(new File(directory, "toc.htm"));
        toc = new PrintWriter(tocWriter);
	

        // Generate a header that will pass the HTML validator at
        // http://validator.w3.org/

	// We use println so as to get the correct eol character for
        // the local platform.

        index.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
        index.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-US\" lang=\"en-US\">");
        index.println("<html>");
        index.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\"/>");

        // If non-empty, then the path to the SSI files on the ptolemy site
        //String ssiRoot = "";
        String ssiRoot = "http://ptolemy.eecs.berkeley.edu";
        if (usePtWebsite) {
            if (!_printedSSIMessage) {
                _printedSSIMessage = true;
                System.out.println("The ptolemy.ptII.exportHTML.usePtWebsite property is true, generating Ptolemy website SSI code.");
            }
            // FIXME: this absolute path is not very safe.  The
            // problem is that we don't know where $PTII is located on
            // the website.
            index.println("<link href=\"http://ptolemy.eecs.berkeley.edu/ptolemyII/ptIIlatest/ptII/doc/default.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        }

        // Needed for the HTML validator.
        index.println("<title>" + StringUtilities.sanitizeName(model.getName())
                + "</title>");

        if (usePtWebsite) {
            index.println("<!--#include virtual=\"/ssi/toppremenu.htm\" -->");
            index.println("<!--#include virtual=\"toc.htm\" -->");
            index.println("<!--#include virtual=\"/ssi/toppostmenu.htm\" -->");
        }

        // Include jquery and lightbox.
        // Copy Javascript source files into destination directory,
        // if they are available. The files are under an MIT license,
        // which is compatible with the Ptolemy license.
        File jsDirectory = FileUtilities.nameToFile(
                "$CLASSPATH/ptolemy/vergil/javascript", null);
        boolean warn = true;
        // We assume that if the directory exists, then the files exist.
        if (!usePtWebsite && jsDirectory.isDirectory()) {
            warn = false;
            System.out.println("Copying files into the js directory.");
            // Copy files into the "js" directory.
            File jsTargetDirectory = new File(directory, "js");
            if (jsTargetDirectory.exists() && !jsTargetDirectory.isDirectory()) {
                jsTargetDirectory.renameTo(new File(directory, "js.bak"));
            }
            if (!jsTargetDirectory.exists() && !jsTargetDirectory.mkdir()) {
                warn = true;
            } else {
                URL jqueryFile = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/vergil/javascript/js/jquery.js",
                        null, null);
                FileUtilities.binaryCopyURLToFile(jqueryFile, new File(
                        jsTargetDirectory, "jquery.js"));

                URL lightboxFile = FileUtilities
                        .nameToURL(
                                "$CLASSPATH/ptolemy/vergil/javascript/js/jquery.lightbox-0.5.pack.js",
                                null, null);
                FileUtilities.binaryCopyURLToFile(lightboxFile, new File(
                        jsTargetDirectory, "jquery.lightbox-0.5.pack.js"));
            }
            // Copy files into the "css" directory.
            File cssTargetDirectory = new File(directory, "css");
            if (cssTargetDirectory.exists()
                    && !cssTargetDirectory.isDirectory()) {
                cssTargetDirectory.renameTo(new File(directory, "css.bak"));
            }
            if (!cssTargetDirectory.exists() && !cssTargetDirectory.mkdir()) {
                warn = true;
            } else {
                URL jqueryFile = FileUtilities
                        .nameToURL(
                                "$CLASSPATH/ptolemy/vergil/javascript/css/jquery.lightbox-0.5.css",
                                null, null);
                FileUtilities.binaryCopyURLToFile(jqueryFile, new File(
                        cssTargetDirectory, "jquery.lightbox-0.5.css"));
            }
            // Copy files into the "images" directory.
            File imagesTargetDirectory = new File(directory, "images");
            if (imagesTargetDirectory.exists()
                    && !imagesTargetDirectory.isDirectory()) {
                imagesTargetDirectory
                        .renameTo(new File(directory, "images.bak"));
            }
            if (!imagesTargetDirectory.exists()
                    && !imagesTargetDirectory.mkdir()) {
                warn = true;
            } else {
                URL jqueryFile = FileUtilities
                        .nameToURL(
                                "$CLASSPATH/ptolemy/vergil/javascript/images/lightbox-btn-close.gif",
                                null, null);
                FileUtilities.binaryCopyURLToFile(jqueryFile, new File(
                        imagesTargetDirectory, "lightbox-btn-close.gif"));
            }
        }
        if (!usePtWebsite && warn) {
            MessageHandler
                    .message("Warning: Cannot find required Javascript files jquery.js"
                            + " and jquery.lightbox-0.5.pack.js. Perhaps your Ptolemy II"
                            + " installation does not include them (because they are GPLd."
                            + " For the exported HTML to work correctly, you will need to find"
                            + " and copy these files into a subdirectory called 'js' of the"
                            + " directory into which the HTML is exported.");
        }

        if (usePtWebsite) {
	    toc.println("<div id=\"menu\">");
	    toc.println("<ul>");
	    toc.println("<li><a href=\"/index.htm\">Ptolemy Home</a></li>");
	    toc.println("</ul>");
	    toc.println("");
	    toc.println("<ul>");
	    toc.println(" <li><a href=\"../index.html\">Up</a></li>");
	    toc.println("</ul>");
	    toc.println("<ul>");
        }

        // Now write the HTML.

        String jsLibrary = "";
        if (usePtWebsite) {
            // If we are using SSI, then use one location for the JavaScript and CSS.
            jsLibrary = "http://ptolemy.eecs.berkeley.edu/";
        }
        index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "js/jquery.js\"></script>");
        index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "js/jquery.lightbox-0.5.pack.js\"></script>");
        index.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + jsLibrary + "css/jquery.lightbox-0.5.css\" media=\"screen\"/>");

        Attribute headerText = model.getAttribute("_headerText", StringParameter.class);
        if (headerText != null) {
            index.println(((StringParameter)headerText).stringValue());
        } else {
            index.println("<script type=\"text/javascript\">");
            index.println("function writeText(text) {");
            index.println("  document.getElementById(\"afterImage\").innerHTML = text;");
            index.println("}");
            index.println("</script>");
        }
        // The following requires the jquery lightbox extension.
        index.println("<script type=\"text/javascript\">");
        index.println("$(function() {");
        index.println("  $('area.lightbox').lightBox();");
        index.println("});");
        index.println("</script>");
        index.println("</head><body>");

        // Put a header in. Use the name of the ModalModel rather
        // than the Controller if we have a ModalModel.
        String modelName = model.getFullName();
        if (model instanceof FSMActor) {
            NamedObj container = model.getContainer();
            if (container instanceof ModalModel) {
                modelName = container.getFullName();
            }
        }
        Attribute beforeImage = model.getAttribute("_beforeImage", StringParameter.class);
        if (beforeImage != null) {
            index.println(((StringParameter)beforeImage).stringValue());
        } else {
            index.println("<h1>" + modelName + "</h1>");
        }

        // Put the image in.
        index.println("<img src=\"" + _basicGraphFrame.getModel().getName()
                + ".gif\" usemap=\"#actormap\"/>");

        // Write the map next.
        index.println("<map name=\"actormap\">");

        // Create a table of effigies associated with any
        // open submodel or plot.
        Map<NamedObj, PtolemyEffigy> openEffigies = new HashMap<NamedObj, PtolemyEffigy>();
        Tableau myTableau = _basicGraphFrame.getTableau();
        Effigy myEffigy = (Effigy) myTableau.getContainer();
        List<PtolemyEffigy> effigies = myEffigy.entityList(PtolemyEffigy.class);
        for (PtolemyEffigy effigy : effigies) {
            openEffigies.put(effigy.getModel(), effigy);
        }
        List<IconVisibleLocation> iconLocations = _getIconVisibleLocations();
        for (IconVisibleLocation location : iconLocations) {
            // Create a table with parameter values for the actor.
            String mouseOverAction = _getMouseOverAction(location.object);

            // If the actor has an open window (either an plot or
            // a vergil window), then create a link to that.
            String linkTo = "";
            PtolemyEffigy effigy = openEffigies.get(location.object);
            if (effigy != null) {
		// _linkToText() recursively calls writeHTML();
                linkTo = _linkToText(effigy, directory);
            } else {
                if (location.object instanceof State) {
                    // In a ModalModel, location.object is a State
                    // inside the _Controller.  But the effigy is stored
                    // under the refinements of that state, which have the
                    // same container as the _Controller.
                    try {
                        TypedActor[] refinements = ((State) location.object)
                                .getRefinement();
                        // FIXME: There may be more
                        // than one refinement. How to open all of them?
                        // We have only one link. For now, just open the first one.
                        if (refinements != null && refinements.length > 0) {
                            effigy = openEffigies
                                    .get((NamedObj) refinements[0]);
                            if (effigy != null) {
                                linkTo = _linkToText(effigy, directory);
                            }
                        }
                    } catch (IllegalActionException e) {
                        // Ignore errors here. Just don't export this refinement.
                    }
                } else if (location.object instanceof Instantiable) {
                    // There is no open effigy, but the object might
                    // be an instance of a class where the class definition
                    // is open. Look for that.
                    Instantiable parent = ((Instantiable) location.object)
                            .getParent();
                    if (parent instanceof NamedObj) {
                        Effigy classEffigy = Configuration
                                .findEffigy((NamedObj) parent);
                        if (classEffigy instanceof PtolemyEffigy) {
                            linkTo = _linkToText((PtolemyEffigy) classEffigy,
                                    directory);
                        }
                    }
                }

            }

            // Write the name of the actor followed by the table.
            index.println("<area shape=\"rect\" coords=\""
                    + (int) location.topLeftX + "," + (int) location.topLeftY
                    + "," + (int) location.bottomRightX + ","
                    + (int) location.bottomRightY
                    + "\" onmouseover="
                    + mouseOverAction
                    + linkTo + "/>");

	    if (linkTo.length() > 1) {
		String tocLink = linkTo.replace(" title=\"",">");
		toc.println(" <li><a " + tocLink.substring(0, tocLink.length() - 1) + "</a></li>");
	    }
        }
        index.println("</map>");

        Attribute afterImage = model.getAttribute("_afterImage", StringParameter.class);
        if (afterImage != null) {
            index.println(((StringParameter)afterImage).stringValue());
        } else {
            // Section into which actor information is written.
            index.println("<p id=\"afterImage\">Mouse over the actors to see their parameters. Click on composites and plotters to reveal their contents (if provided).</p>");
        }

        if (!usePtWebsite) {
            index.println("</body>");
            index.println("</html");
        } else {
            index.println("<!-- /body -->");
            index.println("<!-- /html -->");
            index.println("<!--#include virtual=\"/ssi/bottom.htm\" -->");

	    toc.println(" </ul>");
	    toc.println("</ul>");
	    toc.println("</div><!-- /#menu -->");
        }
	} finally {
	    if (toc != null) {
		toc.close();
	    }
	    if (index != null) {
		index.close(); // Without this, the output file may be empty
	    }
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return a list of data structures with one entry for each visible
     *  entity plus the director, if there is one. Each data structure contains
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
        // System.out.println("Visible rectangle: " + viewSize);

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
                Locatable location = null;
                try {
                    location = (Locatable) entity.getAttribute("_location",
                            Locatable.class);
                } catch (IllegalActionException e1) {
                    // NOTE: What to do here? For now, ignoring the node.
                }
                if (location != null) {
                    GraphController controller = _basicGraphFrame.getJGraph()
                            .getGraphPane().getGraphController();
                    Figure figure = controller.getFigure(location);
                    if (figure != null) {
                        Figure mainIcon = figure;
                        Point2D origin = ((CompositeFigure) figure).getOrigin();
                        double iconOriginX = origin.getX();
                        double iconOriginY = origin.getY();

                        if (figure instanceof CompositeFigure) {
                            mainIcon = ((CompositeFigure) figure)
                                    .getBackgroundFigure();
                            origin = ((CompositeFigure) figure).getOrigin();
                            iconOriginX = origin.getX();
                            iconOriginY = origin.getY();
                        }
                        Rectangle2D iconBounds = mainIcon.getBounds();

                        IconVisibleLocation i = new IconVisibleLocation();
                        i.object = entity;

                        // Calculate the location of the icon relative to the visible rectangle.
                        i.topLeftX = (iconOriginX + iconBounds.getX()) * scaleX
                                + translateX;
                        i.topLeftY = (iconOriginY + iconBounds.getY()) * scaleY
                                + translateY;

                        i.bottomRightX = (iconOriginX + iconBounds.getX() + iconBounds
                                .getWidth()) * scaleX + translateX;
                        i.bottomRightY = (iconOriginY + iconBounds.getY() + iconBounds
                                .getHeight()) * scaleY + translateY;

                        if (i.bottomRightX < 0.0 || i.bottomRightY < 0.0
                                || i.topLeftX > viewSize.getWidth()
                                || i.topLeftY > viewSize.getHeight()) {
                            // Icon is out of view.
                            continue;
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
        }
        return result;
    }
    
    /** Return JavaScript text for the mouse over action for the
     *  specified object. By default this returns a writeText
     *  command that produces an HTML header followed by a table
     *  showing the parameter names and value of the specified
     *  object. If, however, the object contains a Settable
     *  Attribute named _onMouseOverAction, then it returns
     *  instead the string representation of that attribute.
     *  If the object contains a Settable
     *  Attribute named _onMouseOverText, then it returns
     *  instead a JavaScript writeText() command with the
     *  text being the value provided by that parameter.
     *  If it has both parameters, _onMouseOverAction dominates.
     *  @param object The object.
     *  @return Mouse over command.
     *  @throws IllegalActionException If accessing the attribute
     *   causes an error.
     */
    protected String _getMouseOverAction(NamedObj object) throws IllegalActionException {
        Attribute action = object.getAttribute("_onMouseOverAction", StringParameter.class);
        if (action != null) {
            String value = ((StringParameter)action).stringValue();
            return "\""
                    + StringUtilities.escapeString(value)
                    + "\"";
        }
        String text = null;
        Attribute textSpec = object.getAttribute("_onMouseOverText", StringParameter.class);
        if (textSpec != null) {
            String value = ((StringParameter)textSpec).stringValue();
            text = StringUtilities.escapeString(value);
        }
        if (text == null) {
            text = "<h2>"
                    + object.getName()
                    + "</h2>"
                    + _getParameterTable(object).toString();
        }
        return "\"writeText('"
                + text
                + "')\"";
    }

    /** Get an HTML table describing the parameters of the object.
     *  @param object The Ptolemy object to return a table for.
     *  @return An HTML table displaying the parameter values for the
     *   specified object, or the string "Has no parameters" if the
     *   object has no parameters.
     */
    protected String _getParameterTable(NamedObj object) {
        StringBuffer table = new StringBuffer();
        List<Settable> parameters = object.attributeList(Settable.class);
        boolean hasParameter = false;
        for (Settable parameter : parameters) {
            if (parameter.getVisibility().equals(Settable.FULL)) {
                hasParameter = true;
                table.append("<tr><td>");
                table.append(parameter.getName());
                table.append("</td><td>");
                String expression = parameter.getExpression();
                expression = StringUtilities.escapeForXML(expression);
                expression = expression.replaceAll("'", "\\\\'");
                if (expression.length() == 0) {
                    expression = "&nbsp;";
                }
                table.append(expression);
                table.append("</td><td>");
                String value = parameter.getValueAsString();
                value = StringUtilities.escapeForXML(value);
                value = value.replaceAll("'", "\\\\'");
                if (value.length() == 0) {
                    value = "&nbsp;";
                }
                table.append(value);
                table.append("</td></tr>");
            }
        }
        if (hasParameter) {
            table.insert(0, "<table border=&quot;1&quot;>"
                    + "<tr><td><b>Parameter</b></td>"
                    + "<td><b>Expression</b></td>"
                    + "<td><b>Value</b></td></tr>");
            table.append("</table>");
        } else {
            table.append("Has no parameters.");
        }
        return table.toString();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** For the specified effigy, return HTML text for a link to link
     *  if the effigy has any open tableaux, and those have frames
     *  that implement either HTMLExportable or ImageExportable.
     *  As a side effect, this may generate HTML files in the specified
     *  directory.
     *  @param effigy The effigy.
     *  @param directory The directory into which to write any HTML.
     *  @return The link to HTML, or an empty string if there is none.
     *  @exception IOException If unable to create required HTML files.
     *  @exception PrinterException If unable to create required HTML files.
     * @throws IllegalActionException If something goes wrong.
     *  @exception FileNotFoundException
     */
    private String _linkToText(PtolemyEffigy effigy, File directory)
            throws IOException, PrinterException, IllegalActionException {
        String linkTo = "";
        NamedObj object = effigy.getModel();
        File gifFile;
        // Look for any open tableaux for the object.
        List<Tableau> tableaux = effigy.entityList(Tableau.class);
        // If there are multiple tableaux open, use only the first one.
        if (tableaux.size() > 0) {
            String name = object.getName();
            Frame frame = tableaux.get(0).getFrame();
            // If it's a composite actor, export HTML.
            if (frame instanceof HTMLExportable) {
                File subDirectory = new File(directory, name);
                if (subDirectory.exists()) {
                    if (!subDirectory.isDirectory()) {
                        // Move file out of the way.
                        File backupFile = new File(directory, name + ".bak");
                        subDirectory.renameTo(backupFile);
                    }
                } else if (!subDirectory.mkdir()) {
                    throw new IOException("Unable to create directory "
                            + subDirectory);
                }
                ((HTMLExportable) frame).writeHTML(subDirectory);
                linkTo = "href=\"" + name + "/index.html\"" + " title=\""
                        + name + "\"";
            } else if (frame instanceof ImageExportable) {
                gifFile = new File(directory, name + ".gif");
                OutputStream gifOut = new FileOutputStream(gifFile);
                try {
                    ((ImageExportable) frame).writeImage(gifOut, "gif");
                } finally {
                    gifOut.close();
                }
                linkTo = "href=\"" + name + ".gif\"" + " class=\"lightbox\""
                        + " title=\"" + name + "\"";
            }
        }
        return linkTo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The associated Vergil frame. */
    private final BasicGraphFrame _basicGraphFrame;

    /** True if we have printed the message about SSI. */
    private static boolean _printedSSIMessage;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

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
