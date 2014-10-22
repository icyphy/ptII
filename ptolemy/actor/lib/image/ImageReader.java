/* An actor that reads an image from a FileParameter and outputs
 an AWTImageToken.

 @Copyright (c) 2001-2014 The Regents of the University of California.
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

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

import ptolemy.actor.lib.Source;
import ptolemy.data.AWTImageToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageReader

/**
 <p>This actor reads an Image from a FileParameter, and outputs it as an
 AWTImageToken.</p>

 <p>FIXME: It would be nice if we could read images from stdin.</p>

 @see FileParameter
 @see AWTImageToken
 @author  Christopher Hylands
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ImageReader extends Source {
    // We don't extend ptolemy.actor.lib.Reader because we are not
    // reading in data by columns.  Probably this class and
    // ptolemy.actor.lib.Reader should extend a common base class?

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        //output.setMultiport(true);
        output.setTypeEquals(BaseType.OBJECT);

        fileOrURL = new FileParameter(this, "fileOrURL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by File Attribute.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>URL</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            // Would it be worth checking to see if the URL exists and
            // is readable?
            _url = fileOrURL.asURL();
        }

        super.attributeChanged(attribute);
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new AWTImageToken(_image));
    }

    /** Open the file at the URL, and set the width of the output.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        attributeChanged(fileOrURL);
    }

    /** Read in an image.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!super.prefire()) {
            return false;
        }
        if (_url == null) {
            throw new IllegalActionException("sourceURL was null");
        }

        _image = new ImageIcon(_url).getImage();

        if (_image.getWidth(null) == -1 && _image.getHeight(null) == -1) {
            throw new IllegalActionException(this,
                    "Image size is -1 x -1.  Failed to open '" + _url + "'");
        }

        return super.prefire();
    }

    // Image that is read in.
    private Image _image;

    // The URL of the file.
    private URL _url;
}
