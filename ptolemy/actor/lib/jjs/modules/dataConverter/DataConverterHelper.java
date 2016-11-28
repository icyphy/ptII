/* Helper for the dataConverter JavaScript module.

 Copyright (c) 2016 The Regents of the University of California.
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

package ptolemy.actor.lib.jjs.modules.dataConverter;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.lib.jjs.HelperBase;
import ptolemy.data.AWTImageToken;
import ptolemy.kernel.util.IllegalActionException;

/** Helper for the dataConverter JavaScript module.
 *  @author Hokeun Kim
 *  @version $Id$
 *  @since Ptolemy II 11.0
 *  @Pt.ProposedRating Yellow (eal)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class DataConverterHelper extends HelperBase {

    /** Constructor for DataConverterHelper.
     *  @param currentObj The JavaScript object that this is helping.
     */
    public DataConverterHelper(ScriptObjectMirror currentObj) {
        super(currentObj);
    }

    /**
     * Convert AWTImageToken to JavaScript array.
     * @param imageToken The image token (AWTImageToken) to be converted.
     * @return JavaScript array converted from the image token.
     * @exception IllegalActionException If the conversion fails.
     */
    public Object imageToJSArray(AWTImageToken imageToken) throws IllegalActionException {
        Image image = imageToken.getValue();
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        // Draw the image on to the buffered image
        Graphics2D gr2D = bufferedImage.createGraphics();
        gr2D.drawImage(image, 0, 0, null);
        gr2D.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "jpg", baos);
        } catch (IOException e) {
            throw new IllegalActionException("Cannot convert AWTImageToken to JavaScript array: " + e.getMessage());
        }
        return _toJSArray(baos.toByteArray());
    }

    /** Convert JavaScript array to AWTImageToken.
     *  @param object The JavaScript array to be converted to a image token.
     *  @return The image token converted from JavaScript array.
     *  @exception IllegalActionException If the conversion fails..
     */
    public AWTImageToken jsArrayToImage(Object object) throws IllegalActionException {
        byte[] bytes = _toJavaBytes(object);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(bais);
        } catch (IOException e) {
            throw new IllegalActionException("Cannot convert JavaScript array to AWTImageToken: " + e.getMessage());
        }
        AWTImageToken imageToken = new AWTImageToken(bufferedImage);
        return imageToken;
    }
}
