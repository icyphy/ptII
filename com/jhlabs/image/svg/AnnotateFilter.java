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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.net.URL;

import ptolemy.util.FileUtilities;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.kitfox.svg.app.beans.SVGIcon;

/** An image filter that overlays SVG graphics.
 *  FIXME
 *  
 *  This filter uses SVG Salamander, by Mark McKay, for rendering SVG.
 *  SVG Salamander is available under LGPL and BSD licenses.
 *  See https://java.net/projects/svgsalamander.
 *
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
     *  by setSvgURI.
     *
     *  @param source The source image, on which motion is detected.
     *  @param destination The destination image, on which the graphic is added,
     *   or null to specify to add the graphic to the source image.
     */
    @Override
    public BufferedImage filter(BufferedImage source, BufferedImage destination) {
        if (destination == null) {
            // If no destination is provided, overwrite the source.
            destination = source;
        }
        SVGIcon icon = new SVGIcon();
        boolean success = false;
        if (_svgURI != null) {
            try {
                URL url = FileUtilities.nameToURL(_svgURI, null, null);
                icon.setSvgURI(url.toURI());
                success = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (!success) {
            try {
                URL url = FileUtilities.nameToURL(DEFAULT_SVG_URI, null, null);
                icon.setSvgURI(url.toURI());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                
                // If all else fails, paint default graphic.
                Graphics2D g = destination.createGraphics();
                g.setStroke(new BasicStroke(2));
                g.setColor(Color.RED);
                g.drawOval((int)Math.round(_xOffset) - 5, (int)Math.round(_yOffset) - 5, 10, 10);
                g.dispose();
            }
        }
        Graphics2D g = destination.createGraphics();
        
        // Set offsets and rotation.
        AffineTransform transform = new AffineTransform();
        transform.setToTranslation(_xOffset, _yOffset);
        g.transform(transform);
        
        icon.paintIcon(null, g, 0, 0);

        g.dispose();

        return destination;
    }

    /** Get the specified URI for the graphic.
     *  @return The specified URI for the graphic.
     *  @see #setSvgURI(String)
     */
    public String getSvgURI() {
        return _svgURI;
    }
    
    /** Get the horizontal offset for the graphic, in pixels.
     *  @return The horizontal offset for the graphic, in pixels.
     *  @see #setXOffset(String)
     */
    public double getXOffset() {
        return _xOffset;
    }

    /** Get the vertical offset for the graphic, in pixels.
     *  @return The vertical offset for the graphic, in pixels.
     *  @see #setYOffset(String)
     */
    public double getYOffset() {
        return _yOffset;
    }

    /** Set the specified URI for the graphic.
     *  @see #getSvgURI()
     */
    public void setSvgURI(String svgURI) {
        _svgURI = svgURI;
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
    
    public static String DEFAULT_SVG_URI = "$CLASSPATH/com/jhlabs/image/svg/CapeCodOutline.svg";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The URI for the graphic, if one has been given. */
    private String _svgURI = null;
    
    /** The horizontal location for the graphic. */
    private double _xOffset = 0;
    
    /** The vertical location for the graphic. */
    private double _yOffset = 0;
}
