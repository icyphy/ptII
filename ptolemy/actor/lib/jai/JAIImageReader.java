/* An actor that produces a JAIImageToken from an image file specified
 as a URL.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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
package ptolemy.actor.lib.jai;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Source;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

import com.sun.media.jai.codec.FileCacheSeekableStream;
import com.sun.media.jai.codec.SeekableStream;

///////////////////////////////////////////////////////////////////
//// JAIImageReader

/**
 This actor reads an image from a file or a URL.  The file or URL is
 specified using any form acceptable to FileParameter.  Supports BMP, FPX,
 GIF, JPEG, PNG, PBM, PGM, PPM, and TIFF file formats.

 @author James Yeh, Steve Neuendorffer, Christopher Brooks
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIImageReader extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIImageReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        output.setTypeEquals(BaseType.OBJECT);
        fileOrURL = new FileParameter(this, "fileOrURL");
        fileOrURL.setExpression("$CLASSPATH/doc/img/PtolemyII.jpg");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileAttribute.
     *  The initial value is "$CLASSPATH/doc/img/PtolemyII.jpg".
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** An attempt is made to acquire the file name.  If it null,
     *  throw an exception.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL is null.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            _fileURL = fileOrURL.asURL();

            if (_fileURL == null) {
                throw new IllegalActionException("No such file: "
                        + fileOrURL.getExpression());
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new attribute
     *  @return A new director.
     *  @exception CloneNotSupportedException If a derived class contains
     *  an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        JAIImageReader newObject = (JAIImageReader) super.clone(workspace);
        newObject._fileURL = null;
        return newObject;
    }

    /** Output a JAIImageToken containing the image.
     *  @exception IllegalActionException If a contained method throws it,
     *  or if the attempt to load the file has failed.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        InputStream inputStream = null;
        SeekableStream seekableStream = null;

        try {
            try {
                inputStream = _fileURL.openStream();

                // We use a FileCacheSeekableStream here because
                // we need to have a stream that can go backwards.
                // If we are running under the windows installer, Web Start
                // or any other jar based installation, we need to be
                // able to handle images in jar files.
                seekableStream = new FileCacheSeekableStream(inputStream);
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "Unable to load file '" + _fileURL + "'");
            }

            _outputtedImage = JAI.create("stream", seekableStream);

            /*PlanarImage dummy =*/_outputtedImage.getRendering();
        } finally {
            if (seekableStream != null) {
                try {
                    seekableStream.close();
                } catch (Throwable throwable2) {
                    throw new IllegalActionException(this, throwable2,
                            "Unable to close SeekableStream for '" + _fileURL
                            + "'");
                }
            }

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable throwable3) {
                    throw new IllegalActionException(this, throwable3,
                            "Unable to close InputStream for '" + _fileURL
                            + "'");
                }
            }
        }

        output.send(0, new JAIImageToken(_outputtedImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The URL that specifies where the file is located. */
    private URL _fileURL;

    /** The RenderedOp created by JAI from the stream.  This is then
     *  encapsulated by a JAIImageToken.
     */
    private RenderedOp _outputtedImage;
}
