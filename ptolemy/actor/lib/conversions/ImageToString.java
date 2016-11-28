/* An actor that converts an AWTImage to a base-64 encoded String.

 @Copyright (c) 2003-2016 The Regents of the University of California.
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
package ptolemy.actor.lib.conversions;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.imageio.ImageIO;

import ptolemy.data.AWTImageToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

///////////////////////////////////////////////////////////////////
//// ImageToBase64

/**
 Convert an AWT Image to a base-64 String.

 @author Marten Lohstroh
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (liuj)
 @Pt.AcceptedRating Red (liuj)
 */
public class ImageToString extends Converter {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageToString(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);


        // Set the type of the input port.
        input.setTypeEquals(BaseType.GENERAL); // FIXME: No image type available...
        input.setMultiport(false);
        // Set the type of the output port.
        output.setTypeEquals(BaseType.STRING);
        output.setMultiport(false);

        compression = new StringParameter(this, "compression");
        compression.setExpression("png");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Parameter that determines the encoding of the image ('gif',
     * 'png', 'jpg').
     */
    public StringParameter compression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *  @exception IllegalActionException If the conversion fails.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final RenderedImage im = getRenderedImage(
                ((AWTImageToken) input.get(0)).getValue());
        try {
                OutputStream enc = Base64.getUrlEncoder().wrap(os);
            ImageIO.write(im, "png", enc); // FIXME: Use parameter instead.
            enc.close(); // Important, flushes the output buffer.
            StringToken tk = new StringToken(os.toString(StandardCharsets.US_ASCII.name()));
            output.send(0, tk);
        } catch (final IOException ioe) {
            throw new IllegalActionException(
                    "Unable to convert image to base-64 string.");
        }

    }

    /** Return false if the input port has no token, otherwise return
     *  what the superclass returns (presumably true).
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!input.hasToken(0)) {
            return false;
        }

        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Convert an AWT Image object to a BufferedImage.
     *  @param in An AWT Image object.
     *  @return a BufferedImage.
     */
    private BufferedImage getRenderedImage(Image in) {
        BufferedImage out = new BufferedImage(in.getWidth(null),
                in.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = out.createGraphics();
        g2.drawImage(in, 0, 0, null);
        g2.dispose();
        return out;
    }

}
