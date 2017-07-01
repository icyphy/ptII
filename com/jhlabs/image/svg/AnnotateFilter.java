/* An image filter that overlays SVG graphics.

@Copyright (c) 2015 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package com.jhlabs.image.svg;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ptolemy.util.FileUtilities;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.kitfox.svg.app.beans.SVGIcon;

/** An image filter that overlays SVG graphics.
 *  The graphic can be specified by invoking
 *  {@link #setGraphic(String)} or
 *  {@link #setGraphicURI(String)}.
 *  If both are invoked, then the graphic specified in the call to
 *  {@link #setGraphic(String)} will be used.
 *  You can specify a scale factor for the graphic using
 *  {@link #setScale(double)} and a rotation using
 *  {@link #setRotation(double)}.
 *  The graphic can be offset from the default position (which places
 *  its origin at the upper left of the image) by calling
 *  {@link #setXOffset(double)} and {@link #setYOffset(double)}.
 *  The offset will be applied after the scaling and rotation.
 *  <p>
 *  This filter uses SVG Salamander, by Mark McKay, for rendering SVG.
 *  SVG Salamander is available under LGPL and BSD licenses.
 *  See https://java.net/projects/svgsalamander.
 *  <p>
 *  The filter architecture follows the pattern defined by Jerry Huxtable
 *  in the JH Labs Java Image Processing library, available from:
 *    http://www.jhlabs.com/ip/filters
 *  and licensed under the Apache License, Version 2.0
 *  (http://www.apache.org/licenses/LICENSE-2.0).
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class AnnotateFilter extends AbstractBufferedImageOp {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Filter the source image, overlaying graphics that has been specified
     *  by setGraphicURI.
     *
     *  @param source The source image, on which motion is detected.
     *  @param destination The destination image, on which the graphic is added,
     *   or null to specify to add the graphic to the source image.
     *  @return The filtered image.
     */
    @Override
    public BufferedImage filter(BufferedImage source, BufferedImage destination) {
        if (destination == null) {
            // If no destination is provided, overwrite the source.
            destination = source;
        }
        SVGIcon icon = new SVGIcon();
        boolean success = false;
        if (_graphic != null && !_graphic.trim().equals("")) {
            try {
                Reader reader = new StringReader(_graphic);
                // NOTE: The second argument is supposed to be a unique name.
                // If the graphic changes on multiple invocations of this method,
                // will have to use a new name, or the old graphic will be rendered.
                URI uri = icon.getSvgUniverse().loadSVG(reader, "/graphic" + _graphicVersion);
                // Unfortunately, if the string is malformed SVG, the above prints to stderr
                // and returns null rather than throwing an exception.
                if (uri != null) {
                    icon.setSvgURI(uri);
                    success = true;
                }
            } catch (Exception e) {
                // Print the error an proceed to using defaults.
                System.err.println("Failed to load graphic: " + e);
            }
        }
        if (!success && _graphicURI != null && !_graphicURI.trim().equals("")) {
            try {
                URL url = FileUtilities.nameToURL(_graphicURI, null, null);
                icon.setSvgURI(url.toURI());
                success = true;
            } catch (Exception e) {
                // Print the error an proceed to using defaults.
                System.err.println("Failed to load graphicURI: " + e);
            }
        }
        /* This used to provide a default graphic is none is specified.
         * Really, we just want no graphic.
         */
        if (!success) {
            return source;
        }
        /* Default graphic used to be retrieved using this code:
        if (!success) {
            try {
                URL url = FileUtilities.nameToURL(DEFAULT_GRAPHIC_URI, null, null);
                icon.setSvgURI(url.toURI());
            } catch (Exception e) {
                // FIXME Auto-generated catch block
                e.printStackTrace();

                // If all else fails, paint default graphic.
                Graphics2D g = destination.createGraphics();
                g.setStroke(new BasicStroke(2));
                g.setColor(Color.RED);
                g.drawOval((int)Math.round(_xOffset) - 5, (int)Math.round(_yOffset) - 5, 10, 10);
                g.dispose();
            }
        }
        */
        Graphics2D g = destination.createGraphics();

        // Set offsets and rotation.
        AffineTransform transform = new AffineTransform();
        transform.setToScale(_scale, _scale);
        transform.rotate(_rotation);
        AffineTransform translation = new AffineTransform();
        translation.setToTranslation(_xOffset, _yOffset);
        transform.preConcatenate(translation);
        g.transform(transform);
        // Make sure anti-aliasing is turned on.
        Map<RenderingHints.Key,Object> hints = new HashMap<RenderingHints.Key,Object>();
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.addRenderingHints(hints);
        // Above seems insufficient. Have to set it in the icon too.
        icon.setAntiAlias(true);
        icon.paintIcon(null, g, 0, 0);

        g.dispose();

        return destination;
    }

    /** Return the specified SVG for the graphic, or an empty string
     *  if none has been specified.
     *  @return The specified SVG for the graphic.
     *  @see #setGraphic(String)
     */
    public String getGraphic() {
        return _graphic;
    }

    /** Get the specified URI for the graphic.
     *  @return The specified URI for the graphic.
     *  @see #setGraphicURI(String)
     */
    public String getGraphicURI() {
        return _graphicURI;
    }

    /** Get the rotation for the graphic in degrees, which defaults to 0.0.
     *  @return The rotation for the graphic.
     *  @see #setRotation(double)
     */
    public double getRotation() {
        return _rotation * 180.0 / Math.PI;
    }

    /** Get the scale factor for the graphic, which defaults to 1.0.
     *  @return The scale factor for the graphic.
     *  @see #setScale(double)
     */
    public double getScale() {
        return _scale;
    }

    /** Get the horizontal offset for the graphic, in pixels.
     *  @return The horizontal offset for the graphic, in pixels.
     *  @see #setXOffset(double)
     */
    public double getXOffset() {
        return _xOffset;
    }

    /** Get the vertical offset for the graphic, in pixels.
     *  @return The vertical offset for the graphic, in pixels.
     *  @see #setYOffset(double)
     */
    public double getYOffset() {
        return _yOffset;
    }

    /** Set the SVG for the graphic.
     *  @param graphic An SVG specification for the graphic.
     *  @see #getGraphic()
     */
    public void setGraphic(String graphic) {
        if (!_graphic.equals(graphic)) {
            // Use the current date and time for the version to guarantee a
            // unique new version number.  A counter is insufficient
            // if a user edits a non-running model.  (I.e., the counter starts
            // at "1", the user stops the model, edits the svg, runs the model
            // and the counter is reset to "1" again).
            _graphicVersion = (int) (new Date().getTime()/1000);
        }
        _graphic = graphic;
    }

    /** Set the specified URI for the graphic to use if no
     *  graphic is specified using setGraphic().
     *  @param graphicURI An SVG specification for the graphic.
     *  @see #getGraphicURI()
     *  @see #setGraphic(String)
     */
    public void setGraphicURI(String graphicURI) {
        _graphicURI = graphicURI;
    }

    /** Set the rotation for the graphic in degrees.
     *  @param theta The rotation.
     *  @see #getRotation()
     */
    public void setRotation(double theta) {
        _rotation = theta * Math.PI / 180.0;
    }

    /** Set the scale factor for the graphic.
     *  @param scale The scale factor.
     *  @see #getScale()
     */
    public void setScale(double scale) {
        _scale = scale;
    }

    /** Set the horizontal offset for the graphic.
     *  @param x The horizontal offset, in pixels.
     *  @see #getXOffset()
     */
    public void setXOffset(double x) {
        _xOffset = x;
    }

    /** Set the vertical location for the graphic.
     *  @param y The vertical offset, in pixels.
     *  @see #getYOffset()
     */
    public void setYOffset(double y) {
        _yOffset = y;
    }

    /** Return a string description of the filter.
     *  @return The string "AnnotateFilter".
     */
    @Override
    public String toString() {
        return "AnnotateFilter";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The relative URI of the default graphic. */
    // public static String DEFAULT_GRAPHIC_URI = "$CLASSPATH/com/jhlabs/image/svg/CapeCodOutline.svg";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The SVG for the graphic, if it has been given. */
    private String _graphic = "";

    /** The URI for the graphic, if one has been given. */
    private String _graphicURI = null;

    /** The version for the graphic. This gets incremented each time _graphic changes. */
    private int _graphicVersion = 0;

    /** The rotation for the graphic in radians. */
    private double _rotation = 0.0;

    /** The scale factor for the graphic. */
    private double _scale = 1.0;

    /** The horizontal location for the graphic. */
    private double _xOffset = 0;

    /** The vertical location for the graphic. */
    private double _yOffset = 0;
}
