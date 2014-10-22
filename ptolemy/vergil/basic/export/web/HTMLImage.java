/* Attribute for inserting an image into a web page.

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

package ptolemy.vergil.basic.export.web;

import java.awt.Frame;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.gui.ImageExportable;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.ExportParameters;

///////////////////////////////////////////////////////////////////
//// HTMLImage
/**
 * Attribute for inserting an image into a web page.  Drag this attribute onto an
 * actor that produces a BasicGraphFrame (for example,
 * ptolemy.actor.lib.gui.XYPlotter) and specify the caption for this image.
 *
 * By default, this image will be placed at the end of the HTML page,
 * but you can change the position by setting the
 * <i>imagePosition</i> parameter. You can also separately control what
 * text is displayed in the model, or make the attribute disappear altogether
 * in the model (for this, just set <i>displayText</i> to an empty string).
 *
 * Based on ptolemy.vergil.basic.export.web.HTMLText
 *
 * @author Beth Latronico
 * @version $Id$
 * @since Ptolemy II 10.0
 * @see ptolemy.gui.ImageExportable
 * @see ptolemy.vergil.basic.export.web.HTMLText
 * @see ptolemy.vergil.basic.export.web.LinkToOpenTableaux

 * @Pt.ProposedRating Red (ltrnc)
 * @Pt.AcceptedRating Red (ltrnc)
 */
public class HTMLImage extends WebContent {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public HTMLImage(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        imagePosition = new HTMLTextPosition(this, "imagePosition");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Parameter specifying the position into which to export the image.
     *  The parameter offers the following possibilities:
     *  <ul>
     *  <li><b>end</b>: Put the image at the end of the HTML file.
     *  <li><b>header</b>: Put the image in the header section.
     *  <li><b>start</b>: Put the image at the start of the body section.
     *  <li><i>anything_else</i>: Put the image in a separate HTML file
     *   named <i>anything_else</i>.
     *  </ul>
     *  The default is "start".
     */
    // FIXME:  This implementation should be improved.  Should the location be
    // the responsibility of the WebContent class, or the WebExporter?  I think
    // the WebExporter.
    public HTMLTextPosition imagePosition;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return image plus the appropriate file extension, e.g. image/gif
     *
     * @return image plus the appropriate file extension, e.g. image/gif
     */
    // FIXME: Implement other file extensions (do we use any?)
    // How to determine which file extension should be used?  If file has not
    // been created yet?
    @Override
    public String getMimeType() {
        return "image/gif";
    }

    /** Return true, since old images should be overwritten with new.
     *
     * @return True, since old images should be overwritten with new
     */
    @Override
    public boolean isOverwriteable() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate the image file and a &lt;table&gt; element holding an
     *  &lt;img/&gt; element and a caption for the image..
     *
     *  @param exporter The WebExporter to add content to
     *  @exception IllegalActionException If something is wrong generating the
     *  image file or generating the additional web content
     */
    @Override
    public void _provideElements(WebExporter exporter)
            throws IllegalActionException {

        // Copied from LinkToOpenTableau
        // Create a table of effigies associated with any
        // open submodel or plot.
        Map<NamedObj, PtolemyEffigy> openEffigies = new HashMap<NamedObj, PtolemyEffigy>();
        Tableau myTableau = exporter.getFrame().getTableau();
        Effigy myEffigy = (Effigy) myTableau.getContainer();
        List<PtolemyEffigy> effigies = myEffigy.entityList(PtolemyEffigy.class);
        for (PtolemyEffigy effigy : effigies) {
            openEffigies.put(effigy.getModel(), effigy);
        }

        // Get the effigy that goes with this attribute's container
        PtolemyEffigy effigy = openEffigies.get(getContainer());

        // The hierarchy of effigies does not always follow the model hierarchy
        // (e.g., a PlotEffigy will be contained by the top-level effigy for the
        // model for some reason), so if the effigy is null, we search
        // nonetheless for an effigy.
        if (effigy == null) {
            Effigy candidate = Configuration.findEffigy(getContainer());
            if (candidate instanceof PtolemyEffigy) {
                effigy = (PtolemyEffigy) candidate;
            }
        }
        try {
            // _linkTo calls addContent()
            if (effigy != null) {
                // _linkTo() recursively calls writeHTML();
                _linkTo(exporter, effigy, getContainer(), getContainer(),
                        exporter.getExportParameters());
            }
        } catch (Throwable throwable) {
            throw new IllegalActionException(this, throwable,
                    "Failed to generate HTMLImage. ");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _linkTo(WebExporter exporter, PtolemyEffigy effigy,
            NamedObj sourceObject, NamedObj destinationObject,
            ExportParameters parameters) throws IOException, PrinterException,
            IllegalActionException {
        File gifFile;
        WebElement webElement;
        // Look for any open tableaux for the object.
        List<Tableau> tableaux = effigy.entityList(Tableau.class);
        // If there are multiple tableaux open, use only the first one.
        if (tableaux.size() > 0) {
            String name = destinationObject.getName();
            Frame frame = tableaux.get(0).getFrame();
            // If it's a composite actor, export HTML.
            if (frame instanceof ImageExportable) {

                gifFile = new File(parameters.directoryToExportTo, name
                        + ".gif");
                if (parameters.deleteFilesOnExit) {
                    gifFile.deleteOnExit();
                }
                OutputStream gifOut = new FileOutputStream(gifFile);
                try {
                    ((ImageExportable) frame).writeImage(gifOut, "gif");
                } finally {
                    gifOut.close();
                }

                // Check the exporter for the path to use here
                // This can differ depending on the exporter
                // For example, an HttpService uses a URL as a path since the
                // WebServer has a resource handler to serve files
                // ExportToHTML will use a directory since the files are
                // stored locally and located relative to the main web page
                String path = exporter.getExportParameters().HTMLPathForFiles;
                if (path == null) {
                    path = "/";
                } else {
                    if (!path.equals("") && !path.endsWith("/")) {
                        path = path + "/";
                    }
                }

                String content = "<table> <caption align=\"bottom\">"
                        + this.displayText.getExpression()
                        + "</caption> <tr> <td> <img src=\"" + path + name
                        + ".gif\"> </td></tr></table>";

                webElement = WebElement.createWebElement(getContainer(),
                        getName() + "WebElement", getName() + "WebElement");
                webElement.setExpression(content);
                webElement.setParent(imagePosition.stringValue());

                //Add image. Image should only be added once (onceOnly -> true).
                exporter.defineElement(webElement, true);
            }
        }
    }
}
