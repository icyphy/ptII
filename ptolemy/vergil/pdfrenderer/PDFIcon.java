/* An icon that displays a specified java.awt.Image.

 Copyright (c) 2009-2014 The Regents of the University of California.
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
package ptolemy.vergil.pdfrenderer;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.Rectangle2D;
import java.net.URL;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.DynamicEditorIcon;

import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFRenderer;

import diva.canvas.Figure;
import diva.canvas.toolbox.PaintedFigure;
import diva.util.java2d.PaintedObject;

///////////////////////////////////////////////////////////////////
//// PDFIcon

/**
 An icon that displays a specified PDF page.

 <p> This class uses pdf-renderer, obtainable from <a
 href="https://pdf-renderer.dev.java.net/">https://pdf-renderer.dev.java.net/</a>.
 This is an "an open source, all Java library which renders PDF
 documents to the screen using Java2D." By using this icon, an actor
 or attribute in Vergil can be defined by a PDF file.  Using this icon
 requires that PDFRenderer.jar in the classpath, it is usually found
 in $PTII/lib/PDFRenderer.jar.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class PDFIcon extends DynamicEditorIcon {
    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PDFIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        PDFIcon newObject = (PDFIcon) super.clone(workspace);
        newObject._page = null;
        return newObject;
    }

    /** Create a new default background figure, which is an instance
     *  of PDFFigure.
     *  @return A figure representing the specified PDF page.
     */
    @Override
    public Figure createBackgroundFigure() {
        PaintedFigure newFigure = new PaintedFigure();
        newFigure.add(new PDFPaintedObject());
        return newFigure;
    }

    /** Specify an PDF page to display.
     *  @param page The PDF page to display.
     */
    public void setPage(PDFPage page) {
        _page = page;
    }

    /** Specify a scaling percentage of the PDF page.
     *  @param scalePercentage The scale percentage.
     */
    public void setScale(double scalePercentage) {
        _scale = scalePercentage / 100.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The PDF page to render. */
    private PDFPage _page;

    // The scale percentage.
    private double _scale = 1.0;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private class PDFPaintedObject implements PaintedObject {

        @Override
        public Rectangle2D getBounds() {
            if (_page == null) {
                return new Rectangle2D.Double(0.0, 0.0, 40.0, 40.0);
            }
            Rectangle2D boundingBox = _page.getBBox();
            return new Rectangle2D.Double(boundingBox.getX(),
                    boundingBox.getY(), boundingBox.getWidth() * _scale,
                    boundingBox.getHeight() * _scale);
        }

        @Override
        public void paint(Graphics2D graphics) {
            if (_page == null) {
                // No page. Paint an error image.
                URL url = getClass().getResource(
                        "/diva/canvas/toolbox/errorImage.gif");
                Toolkit tk = Toolkit.getDefaultToolkit();
                Image image = tk.getImage(url);
                graphics.drawImage(image, null, null);

                return;
            }
            Rectangle2D boundingBox = _page.getBBox();
            PDFRenderer renderer = new PDFRenderer(_page, graphics,
                    new Rectangle(0, 0,
                            (int) (boundingBox.getWidth() * _scale),
                            (int) (boundingBox.getHeight() * _scale)), null, // No clipping.
                    null // Transparent background.
            );
            try {
                _page.waitForFinish();
            } catch (InterruptedException e) {
                // What can we do here?
                e.printStackTrace();
            }
            renderer.run();
        }
    }
}
