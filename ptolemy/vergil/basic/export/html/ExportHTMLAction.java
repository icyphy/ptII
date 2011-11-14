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
import ptolemy.gui.ImageExportable;
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
 *  The following actions are supported:
 *  <b>FIXME: The following is obsolete!!! Update it.</b>
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
 *  <li> A click-on handler that responds to user clicks on an
 *       object in the model. If there is no customization of this
 *       action in the model, then by default, upon a click,
 *       the web page will display any open windows associated
 *       with the object. Specifically, if the object is a plotter,
 *       for example, and a plot window is open, then upon clicking
 *       on the plot, the user will see an image of the plot in
 *       a lightbox. If the object is a composite actor that has
 *       an open window, then clicking on the composite actors
 *       will take the viewer to a new HTML page showing the
 *       inside of the composite actor.
 *       <p>
 *       This behavior can be customized in a number of ways.
 *       If an object in the model contains a parameter named
 *       <i>_onClickLinkTo</i>, then the value of this parameter
 *       specifies a URL to go to in response to a click.
 *       If there is no <i>_onClickLinkTo</i> but there is an
 *       <i>_onClickLightBox</i> parameter, then a click on
 *       object will display the HTML specified by the value
 *       of the <i>_onClickLightBox</i> parameter in a
 *       lightbox (a kind of popup that appears in front
 *       of the current HTML page).
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

        File modelDirectory = _basicGraphFrame.getLastDirectory();
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
     *  code and use the JavaScript and fancybox files from the Ptolemy website.
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
	        index.println("<link href=\""
	                + ssiRoot
	                + "/ptolemyII/ptIIlatest/ptII/doc/default.css\" rel=\"stylesheet\" type=\"text/css\"/>");
	    }

	    // Needed for the HTML validator.
	    index.println("<title>" + StringUtilities.escapeForXML(_getTitleText(model))
	            + "</title>");

	    if (usePtWebsite) {
	        index.println("<!--#include virtual=\"/ssi/toppremenu.htm\" -->");
	        index.println("<!--#include virtual=\"toc.htm\" -->");
	        index.println("<!--#include virtual=\"/ssi/toppostmenu.htm\" -->");
	    }

	    // Include Ptolemy-specific JavaScript,
	    // jquery and fancybox. The following files are needed:
	    // The first three of these should be the JavaScript files to include,
	    // and the fourth should be the CSS file.
	    // The rest are image files to copy over.
	    // FIXME: I don't like the hardwired version numbers here.
	    String[] filenames = {
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

	    // Copy Javascript source files into destination directory,
	    // if they are available. The files are under an MIT license,
	    // which is compatible with the Ptolemy license.
	    // For jquery, we could use a CDS (content delivery service) instead
	    // of copying the file.
	    String jsDirectoryName = "$CLASSPATH/ptolemy/vergil/basic/export/html/javascript/";
	    File jsDirectory = FileUtilities.nameToFile(
	            jsDirectoryName, null);
	    boolean warn = true;
	    // We assume that if the directory exists, then the files exist.
	    if (!usePtWebsite && jsDirectory.isDirectory()) {
	        warn = false;
	        // System.out.println("Copying files into the js directory.");
	        // Copy files into the "javascript" directory.
	        File jsTargetDirectory = new File(directory, "javascript");
	        if (jsTargetDirectory.exists() && !jsTargetDirectory.isDirectory()) {
	            jsTargetDirectory.renameTo(new File(directory, "javascript.bak"));
	        }
	        if (!jsTargetDirectory.exists() && !jsTargetDirectory.mkdir()) {
	            warn = true;
	        } else {
	            // Copy css, JavaScript, and image files.
	            for (String filename : filenames) {
	                URL lightboxFile = FileUtilities
	                        .nameToURL(
	                                jsDirectoryName + filename,
	                                null, null);
	                FileUtilities.binaryCopyURLToFile(lightboxFile, new File(
	                        jsTargetDirectory, filename));
	            }
	        }
	    }
	    if (!usePtWebsite && warn) {
	        MessageHandler
	        .message("Warning: Cannot find required JavaScript, CSS, and image files"
	                + " for lightbox effect implemented by the fancybox"
	                + " package. Perhaps your Ptolemy II"
	                + " installation does not include them.");
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
	    
	    // Include required script files.
	    // Use either the files we copied above, or use the Ptolemy website.
	    String jsLibrary = "";
	    if (usePtWebsite) {
	        // If we are using SSI, then use one location for the JavaScript and CSS and image files.
	        jsLibrary = ssiRoot;
	    }
	    // FIXME: The following is not going to work with SSI. Christopher? Where are the files?
            // NOTE: Due to a bug somewhere (browser, Javascript, etc.), can't end this with />. Have to use </script>.
	    index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "javascript/" + filenames[0] + "\"></script>");
            index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "javascript/" + filenames[1] + "\"></script>");
            index.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"" + jsLibrary + "javascript/" + filenames[2] + "\" media=\"screen\"/>");
            index.println("<script type=\"text/javascript\" src=\"" + jsLibrary + "javascript/" + filenames[3] + "\"></script>");
            // Could alternatively use a CDS (Content Delivery Service) for the JavaScript library for jquery.
            // index.println("<script type=\"text/javascript\" src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.4/jquery.min.js\"></script>");

	    // Collect HTML text to insert at various points in the output file.
	    StringBuffer header = new StringBuffer();
            StringBuffer start = new StringBuffer();
            
            // Always start with the title.
            start.append("<h1>" + _getTitleText(model) + "</h1>\n");
            
            StringBuffer end = new StringBuffer();
	    List<HTMLText> texts = model.attributeList(HTMLText.class);
	    if (texts != null && texts.size() > 0) {
	        for(HTMLText text : texts) {
	            String position = text.textPosition.stringValue();
	            if (position.equals("header")) {
	                header.append(text.getContent());
	                header.append("\n");
	            } else if (position.equals("start")) {
	                start.append(text.getContent());
                        start.append("\n");
                    } else if (position.equals("end")) {
                        end.append(text.getContent());
                        end.append("\n");
                    } else {
                        throw new IllegalActionException(text,
                                "Unrecognized textPosition value: " + position);
                    }
	        }
	    }	    
	                
            // Insert the default end text if none was given.
            if (end.length() == 0) {
                end.append("<p id=\"afterImage\">Mouse over the actors to see their parameters. Click on composites and plotters to reveal their contents (if provided).</p>\n");
            }

            // Next, create the image map.
            String map = _createImageMap(directory, toc);

	    // Write the main part of the HTML file.
	    index.println(header.toString());
	    index.println("</head><body>");
	    index.println(start.toString());
	    // Put the image in.
	    index.println("<img src=\"" + _basicGraphFrame.getModel().getName()
	            + ".gif\" usemap=\"#actormap\"/>");
	    index.println(map);
	    index.println(end);

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

    /** Create the image map. As a side effect, this may create other
     *  HTML files or subdirectories.
     *  @param directory The directory into which to write any HTML
     *   that is created as a side effect.
     *  @param toc The table of contents file to write to, or null
     *   to not write to the table of contents.
     *  @throws PrinterException If writing to the toc file fails.
     *  @throws IOException If IO fails.
     *  @throws IllegalActionException If reading parameters fails.
     */
    protected String _createImageMap(File directory, PrintWriter toc)
            throws IllegalActionException, IOException, PrinterException {
        StringBuffer result = new StringBuffer();
        result.append("<map name=\"actormap\">\n");

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

            // If the actor customizes the click-on action with its own
            // link, then use that link.
            String linkTo = _getClickOnLink(location.object, directory);

            // If the behavior has not been customized in the model,
            // then the default behavior is to provide a link to
            // any open tableaux. If the the frame associated with
            // the tableau implements
            // HTMLExportable, then this is an ordinary link to
            // the HTML exported by the frame. If it instead
            // implements ImageExportable, then this a link that
            // brings up the image in a lightbox.
            if (linkTo == null) {
                PtolemyEffigy effigy = openEffigies.get(location.object);
                if (effigy != null) {
                    // _linkToText() recursively calls writeHTML();
                    linkTo = _linkToText(effigy, directory);
                } else {
                    // Default is empty.
                    linkTo = "";
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
            }
            
            // Get the title to associate with the object.
            // This defaults to the name of the object.
            String title = _getTitleText(location.object);

            // Write the name of the actor followed by the table.
            result.append("<area shape=\"rect\" coords=\""
                    + (int) location.topLeftX + "," + (int) location.topLeftY
                    + "," + (int) location.bottomRightX + ","
                    + (int) location.bottomRightY
                    + "\" onmouseover="
                    + mouseOverAction
                    + " "
                    + linkTo
                    + " title=\""
                    + StringUtilities.escapeString(title)
                    + "\"/>\n");

            if (toc != null && linkTo.length() > 1) {
                toc.println(" <li><a " + linkTo + ">" + _getTitleText(location.object) + "</a></li>");
            }
        }
        result.append("</map>\n");
        return result.toString();
    }
    
    /** If the specified object customizes the link to follow upon
     *  clicking on that object, then return that link-to HTML text
     *  here. This will normally have one of the following forms:
     *  <pre>
     *     href="linkvalue" target="targetvalue"
     *  <pre>
     *  or
     *  <pre>
     *     href="linkvalue" class="classname" 
     *  <pre>
     *  The customization is done by inserting an instance of
     *  {@see IconLink} into the object.
     *  @return Custom link reference, or null if there is no customization.
     *  @throws IllegalActionException If accessing the customization attributes fails.
     *  @throws IOException If a file operation fails.
     */
    protected String _getClickOnLink(NamedObj object, File directory) throws IllegalActionException, IOException {
        // If the object contains an IconLink parameter, then use that instead of the default.
        // If it has more than one, then just use the first one.
        List<IconLink> links = object.attributeList(IconLink.class);
        if (links != null && links.size() > 0) {
            return links.get(0).getContent();
        }
        return null;
    }
    
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
            // FIXME: Should include attributes as well, at least directors.
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
                        i.object = entity;

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
            value = StringUtilities.escapeForXML(value);            
            return "\"" + value + "\"";
        }
        String text = null;
        Attribute textSpec = object.getAttribute("_onMouseOverText", StringParameter.class);
        if (textSpec != null) {
            String value = ((StringParameter)textSpec).stringValue();
            text = StringUtilities.escapeForXML(value);
            // Bizarrely, escaping all characters except newlines work.
            // Newlines need to be converted to \n.
            // No idea why so many backslashes are required below.
            text = text.replaceAll("&#10;", "\\\\\\n");
        }
        if (text == null) {
            // NOTE: The following needs to not include any newlines.
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
    
    /** Return the title of the specified object. If it contains a parameter
     *  of class {@see Title}, then return the title specified by that class.
     *  Otherwise, if the object is an instance of FSMActor contained by
     *  a ModalModel, then return the
     *  name of its container, not the name of the FSMActor.
     *  Otherwise, return the name of the object.
     *  @return A title for the object.
     *  @throws IllegalActionException If accessing the title attribute fails..
     */
    protected String _getTitleText(NamedObj object) throws IllegalActionException {
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

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** For the specified effigy, return HTML text for a link
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
                linkTo = "href=\"" + name + "/index.html\"";
            } else if (frame instanceof ImageExportable) {
                gifFile = new File(directory, name + ".gif");
                OutputStream gifOut = new FileOutputStream(gifFile);
                try {
                    ((ImageExportable) frame).writeImage(gifOut, "gif");
                } finally {
                    gifOut.close();
                }
                // Strangely, the clas has to be "iframe".
                // I don't understand why it can't be "lightbox".
                linkTo = "href=\"" + name + ".gif\"" + " class=\"iframe\"";
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
