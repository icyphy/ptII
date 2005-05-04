/* Static methods that Transform java.awt.Images

@Copyright (c) 2001-2005 The Regents of the University of California.
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

PT_COPYRIGHT_VERSION 2
COPYRIGHTENDKEY
*/
package ptolemy.actor.lib.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;


//////////////////////////////////////////////////////////////////////////
//// Transform

/**

<p>Some of the code in this file is based on code from
<a href="http://java.sun.com/docs/books/tutorial/extra/fullscreen/example.html">http://java.sun.com/docs/books/tutorial/extra/fullscreen/example.html</a>

@author  Christopher Hylands
@version $Id$
@since Ptolemy II 3.0
@Pt.ProposedRating Red (cxh)
@Pt.AcceptedRating Red (cxh)
*/
public class Transform {
    /** Rotate an Image.
     *  @param originalImage The java.awt.Image to rotate.
     *  @param rotate The number of degrees to rotate the originalImage
     *  @return The rotated Image.
     */
    public static Image rotate(Image originalImage, int rotate) {
        int width = originalImage.getWidth(null);
        int height = originalImage.getHeight(null);
        int newWidth = width;
        int newHeight = height;

        if ((rotate == 90) || (rotate == 270)) {
            newWidth = height;
            newHeight = width;
        }

        // Create an image buffer in which to paint on.
        BufferedImage outputImage = new BufferedImage(newWidth, newHeight,
                BufferedImage.TYPE_INT_RGB);

        // Set the rotation
        AffineTransform rotateAffineTransform = new AffineTransform();

        // Convert rotate to radians.
        rotateAffineTransform.rotate(rotate * (Math.PI / 180.0F), width / 2,
                height / 2);

        rotateAffineTransform.translate((width / 2) - (height / 2),
                (width / 2) - (height / 2));

        // Paint image.
        Graphics2D graphics2d = outputImage.createGraphics();
        graphics2d.drawImage((Image) originalImage, rotateAffineTransform, null);
        graphics2d.dispose();

        return outputImage;
    }

    /** Scale an image so that its maximum dimension is no larger than
     *  the maximumDimension parameter.
     *  This method is useful for creating thumbnail images.
     *  @param originalImage The java.awt.Image to rotate.
     *  @param maximumDimension The maximum x or y dimension
     *  @return The scaled Image.
     */
    public static Image scale(Image originalImage, int maximumDimension) {
        // Determine the scale.
        double scale = (double) maximumDimension / (double) originalImage
            .getHeight(null);

        if (originalImage.getWidth(null) > originalImage.getHeight(null)) {
            scale = (double) maximumDimension / (double) originalImage.getWidth(null);
        }

        // Determine size of new image.
        // One of them should equal maximumDimension.
        int scaledWidth = (int) (scale * originalImage.getWidth(null));
        int scaledHeight = (int) (scale * originalImage.getHeight(null));

        // Create an image buffer in which to paint on.
        BufferedImage outputImage = new BufferedImage(scaledWidth,
                scaledHeight, BufferedImage.TYPE_INT_RGB);

        // Set the scale.
        AffineTransform scaleAffineTransform = new AffineTransform();

        // If the image is smaller than the desired image size,
        // don't bother scaling.
        //if (scale < 1.0d) {
        scaleAffineTransform.scale(scale, scale);

        //}
        // Paint image.
        Graphics2D graphics2d = outputImage.createGraphics();
        graphics2d.drawImage(originalImage, scaleAffineTransform, null);
        graphics2d.dispose();
        return outputImage;
    }
}
