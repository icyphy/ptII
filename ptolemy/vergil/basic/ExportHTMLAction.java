/* An Action that works with BasicGraphFrame to export HTML.

 Copyright (c) 1998-2010 The Regents of the University of California.
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
import ptolemy.domains.modal.kernel.FSMActor;
import ptolemy.domains.modal.kernel.State;
import ptolemy.domains.modal.modal.ModalModel;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
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
 *  and a mouse-over handler that displays parameter values
 *  in a table when the mouse passes over an actor.
 *  Moreover, if any plot windows are open when the HTML is
 *  exported, then GIF images of those plot windows are linked
 *  to the plotter icons so that clicking on the icons causes
 *  the plot window to appear in a lightbox style.
 *  In addition, if any composite actors are open, then
 *  clicking on the composite actors will take the viewer
 *  to a new HTML page showing the inside of the composite actor.
 *
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.2
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
        fileDialog.addChoosableFileFilter(new BasicGraphFrame.FolderFileFilter());
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
                    if (!MessageHandler.yesNoQuestion(
                            "Directory exists: " + directory + ". Overwrite contents?")) {
                        MessageHandler.message("HTML export canceled.");
                        return;
                    }
                } else {
                    if (!MessageHandler.yesNoQuestion(
                            "File exists with the same name. Overwrite file?")) {
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
     *  method.
     *  @param directory The directory in which to put any associated files.
     *  @exception IOException If unable to write associated files.
     *  @exception PrinterException If unable to write associated files.
     */
    public void writeHTML(File directory) throws PrinterException, IOException {
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

        // Next, create an HTML file.
        Writer indexWriter = new FileWriter(new File(directory, "index.html"));
        PrintWriter writer = new PrintWriter(indexWriter);
        writer.println("<html><head>");
        // Include jquery and lightbox.
        // Copy Javascript source files into destination directory,
        // if they are available. The files are under an MIT license,
        // which is compatible with the Ptolemy license.
        File jsDirectory = FileUtilities.nameToFile("$CLASSPATH/ptolemy/vergil/javascript", null);
        boolean warn = true;
        // We assume that if the directory exists, then the files exist.
        if (jsDirectory.isDirectory()) {
            warn = false;
            // Copy files into the "js" directory.
            File jsTargetDirectory = new File(directory, "js");
            if (jsTargetDirectory.exists() && !jsTargetDirectory.isDirectory()) {
                jsTargetDirectory.renameTo(new File(directory, "js.bak"));
            }
            if (!jsTargetDirectory.exists() && !jsTargetDirectory.mkdir()) {
                warn = true;
            } else {
                URL jqueryFile = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/vergil/javascript/js/jquery.js", null, null);
                FileUtilities.binaryCopyURLToFile(jqueryFile, new File(
                        jsTargetDirectory, "jquery.js"));

                URL lightboxFile = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/vergil/javascript/js/jquery.lightbox-0.5.pack.js", null, null);
                FileUtilities.binaryCopyURLToFile(lightboxFile, new File(
                        jsTargetDirectory, "jquery.lightbox-0.5.pack.js"));
            }
            // Copy files into the "css" directory.
            File cssTargetDirectory = new File(directory, "css");
            if (cssTargetDirectory.exists() && !cssTargetDirectory.isDirectory()) {
                cssTargetDirectory.renameTo(new File(directory, "css.bak"));
            }
            if (!cssTargetDirectory.exists() && !cssTargetDirectory.mkdir()) {
                warn = true;
            } else {
                URL jqueryFile = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/vergil/javascript/css/jquery.lightbox-0.5.css", null, null);
                FileUtilities.binaryCopyURLToFile(jqueryFile, new File(
                        cssTargetDirectory, "jquery.lightbox-0.5.css"));
            }
            // Copy files into the "images" directory.
            File imagesTargetDirectory = new File(directory, "images");
            if (imagesTargetDirectory.exists() && !imagesTargetDirectory.isDirectory()) {
                imagesTargetDirectory.renameTo(new File(directory, "images.bak"));
            }
            if (!imagesTargetDirectory.exists() && !imagesTargetDirectory.mkdir()) {
                warn = true;
            } else {
                URL jqueryFile = FileUtilities.nameToURL(
                        "$CLASSPATH/ptolemy/vergil/javascript/images/lightbox-btn-close.gif", null, null);
                FileUtilities.binaryCopyURLToFile(jqueryFile, new File(
                        imagesTargetDirectory, "lightbox-btn-close.gif"));
            }
        }
        if (warn) {
            MessageHandler.message("Warning: Cannot find required Javascript files jquery.js" +
                            " and jquery.lightbox-0.5.pack.js. Perhaps your Ptolemy II" +
                            " installation does not include them (because they are GPLd." +
                            " For the exported HTML to work correctly, you will need to find" +
                            " and copy these files into a subdirectory called 'js' of the" +
                            " directory into which the HTML is exported.");
        }

        // Now write the HTML.
        writer.println("<script type=\"text/javascript\" src=\"js/jquery.js\"></script>");
        writer.println("<script type=\"text/javascript\" src=\"js/jquery.lightbox-0.5.pack.js\"></script>");
        writer.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"css/jquery.lightbox-0.5.css\" media=\"screen\"/>");

        // FIXME: Need to parameterize the functions somehow.
        writer.println("<script type=\"text/javascript\">");
        writer.println("function writeText(text) {");
        writer.println("  document.getElementById(\"actorName\").innerHTML = text;");
        writer.println("}");
        // The following requires the jquery lightbox extension.
        writer.println("$(function() {");
        writer.println("  $('area.lightbox').lightBox();");
        writer.println("});");
        writer.println("</script>");
        writer.println("</head><body>");

        // Put a header in. Use the name of the ModalModel rather
        // than the Controller if we have a ModalModel.
        String modelName = model.getName();
        if (model instanceof FSMActor) {
            NamedObj container = model.getContainer();
            if (container instanceof ModalModel) {
                modelName = container.getName();
            }
        }
        writer.println("<h1>" + modelName + "</h1>");

        // Put the image in.
        writer.println("<img src=\"" + _basicGraphFrame.getModel().getName()
                + ".gif\" usemap=\"#actormap\"/>");

        // Write the map next.
        writer.println("<map name=\"actormap\">");

        // Create a table of effigies associated with any
        // open submodel or plot.
        Map<NamedObj,PtolemyEffigy> openEffigies = new HashMap<NamedObj,PtolemyEffigy>();
        Tableau myTableau = _basicGraphFrame.getTableau();
        Effigy myEffigy = (Effigy)myTableau.getContainer();
        List<PtolemyEffigy> effigies = myEffigy.entityList(PtolemyEffigy.class);
        for (PtolemyEffigy effigy : effigies) {
            openEffigies.put(effigy.getModel(), effigy);
        }
        List<IconVisibleLocation> iconLocations = _getIconVisibleLocations();
        for (IconVisibleLocation location : iconLocations) {
            // Create a table with parameter values for the actor.
            String table = _getParameterTable(location.object);

            // If the actor has an open window (either an plot or
            // a vergil window), then create a link to that.
            String linkTo = "";
            PtolemyEffigy effigy = openEffigies.get(location.object);
            if (effigy != null) {
                linkTo = _linkToText(effigy, directory);
            } else {
                if (location.object instanceof State) {
                    // In a ModalModel, location.object is a State
                    // inside the _Controller.  But the effigy is stored
                    // under the refinements of that state, which have the
                    // same container as the _Controller.
                    try {
                        TypedActor[] refinements = ((State)location.object).getRefinement();
                        // FIXME: There may be more
                        // than one refinement. How to open all of them?
                        // We have only one link. For now, just open the first one.
                        if (refinements != null && refinements.length > 0) {
                            effigy = openEffigies.get((NamedObj)refinements[0]);
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
                    Instantiable parent = ((Instantiable)location.object).getParent();
                    if (parent instanceof NamedObj) {
                        Effigy classEffigy = Configuration.findEffigy((NamedObj) parent);
                        if (classEffigy instanceof PtolemyEffigy) {
                            linkTo = _linkToText((PtolemyEffigy)classEffigy, directory);
                        }
                    }
                }
            }

            // Write the name of the actor followed by the table.
            writer.println("<area shape=\"rect\" coords=\""
                    + (int)location.topLeftX + ","
                    + (int)location.topLeftY + ","
                    + (int)location.bottomRightX + ","
                    + (int)location.bottomRightY + "\" onmouseover=\"writeText('<h2>"
                    + location.object.getName()
                    + "</h2>"
                    + table.toString()
                    + "')\""
                    + linkTo
                    + "/>");

        }
        writer.println("</map>");

        // Section into which actor information is written.
        writer.println("<p id=\"actorName\">Mouse over the actors to see their parameters. Click on composites and plotters to reveal their contents (if provided).</p>");

        writer.close(); // Without this, the output file may be empty
    }

    /** Get an HTML table describing the parameters of the object.
     *  @param object The Ptolemy object to return a table for.
     *  @return An HTML table displaying the parameter values for the
     *   specified object, or the string "Has no parameters" if the
     *   object has no parameters.
     */
    private String _getParameterTable(NamedObj object) {
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
                    expression="&nbsp;";
                }
                table.append(expression);
                table.append("</td><td>");
                String value = parameter.getValueAsString();
                value = StringUtilities.escapeForXML(value);
                value = value.replaceAll("'", "\\\\'");
                if (value.length() == 0) {
                    value="&nbsp;";
                }
                table.append(value);
                table.append("</td></tr>");
            }
        }
        if (hasParameter) {
            table.insert(0, "<table border=&quot;1&quot;>" +
                            "<tr><td><b>Parameter</b></td>" +
                            "<td><b>Expression</b></td>" +
                            "<td><b>Value</b></td></tr>");
            table.append("</table>");
        } else {
            table.append("Has no parameters.");
        }
        return table.toString();
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

        JCanvas canvas = _basicGraphFrame.getJGraph().getGraphPane().getCanvas();
        AffineTransform transform = canvas.getCanvasPane().getTransformContext()
                .getTransform();
        double scaleX = transform.getScaleX();
        double scaleY = transform.getScaleY();
        double translateX = transform.getTranslateX();
        double translateY = transform.getTranslateY();

        NamedObj model = _basicGraphFrame.getModel();
        if (model instanceof CompositeEntity) {
            List<Entity> entities = ((CompositeEntity)model).entityList();
            for (Entity entity : entities) {
                Locatable location = null;
                try {
                    location = (Locatable)entity.getAttribute("_location", Locatable.class);
                } catch (IllegalActionException e1) {
                    // NOTE: What to do here? For now, ignoring the node.
                }
                if (location != null) {
                    GraphController controller
                            = _basicGraphFrame.getJGraph().getGraphPane().getGraphController();
                    Figure figure = controller.getFigure(location);
                    if (figure != null) {
                        Figure mainIcon = figure;
                        Point2D origin = ((CompositeFigure)figure).getOrigin();
                        double iconOriginX = origin.getX();
                        double iconOriginY = origin.getY();

                        if (figure instanceof CompositeFigure) {
                            mainIcon = ((CompositeFigure)figure).getBackgroundFigure();
                            origin = ((CompositeFigure)figure).getOrigin();
                            iconOriginX = origin.getX();
                            iconOriginY = origin.getY();
                        }
                        Rectangle2D iconBounds = mainIcon.getBounds();

                        IconVisibleLocation i = new IconVisibleLocation();
                        i.object = entity;

                        // Calculate the location of the icon relative to the visible rectangle.
                        i.topLeftX = (iconOriginX + iconBounds.getX())*scaleX + translateX;
                        i.topLeftY = (iconOriginY + iconBounds.getY())*scaleY + translateY;

                        i.bottomRightX
                                = (iconOriginX + iconBounds.getX() + iconBounds.getWidth())
                                *scaleX + translateX;
                        i.bottomRightY
                                = (iconOriginY + iconBounds.getY() + iconBounds.getHeight())
                                *scaleY + translateY;

                        if (i.bottomRightX < 0.0 || i.bottomRightY < 0.0
                                || i.topLeftX > viewSize.getWidth() || i.topLeftY > viewSize.getHeight()) {
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
     *  @exception FileNotFoundException
     */
    private String _linkToText(PtolemyEffigy effigy, File directory) throws IOException,
            PrinterException {
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
                    throw new IOException("Unable to create directory " + subDirectory);
                }
                ((HTMLExportable)frame).writeHTML(subDirectory);
                linkTo = "href=\""  + name + "/index.html\"" +
                        " title=\"" + name + "\"";
            } else if (frame instanceof ImageExportable) {
                gifFile = new File(directory, name + ".gif");
                OutputStream gifOut = new FileOutputStream(gifFile);
                try {
                    ((ImageExportable)frame).writeImage(gifOut, "gif");
                } finally {
                    gifOut.close();
                }
                linkTo = "href=\""  + name + ".gif\"" +
                        " class=\"lightbox\"" +
                        " title=\"" + name + "\"";
            }
        }
        return linkTo;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The associated Vergil frame. */
    private final BasicGraphFrame _basicGraphFrame;

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
            return (object.getName()
                    + " from (" + topLeftX + ", " + topLeftY + ") to ("
                    + bottomRightX + ", " + bottomRightY + ")");
        }
    }
}
