/* An actor that produces a JAIImageToken from an image file specified
as a URL.

@Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jai;

import java.io.IOException;
import java.net.URL;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Source;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.media.jai.codec.FileSeekableStream;

//////////////////////////////////////////////////////////////////////////
//// JAIImageReader
/**
This actor reads an image from a file or a URL.  The file or URL is
specified using any form acceptable to FileParameter.  Supports BMP, FPX,
GIF, JPEG, PNG, PBM, PGM, PPM, and TIFF file formats.

@author James Yeh, Steve Neuendorffer
@version $Id$
@since Ptolemy II 3.0
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
        fileOrURL.setExpression("$PTII/doc/img/PtolemyII.jpg");
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

    /** An attempt is made to acquire the file name.  If it null,
     *  throw an exception.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the URL is null.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileOrURL) {
            URL url = fileOrURL.asURL();
            if (url == null) {
                throw new IllegalActionException("URLToken was null");
            } else {
                _fileRoot = url.getFile();
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Output a JAIImageToken containing the image.
     *  @exception IllegalActionException If a contained method throws it,
     *  or if the attempt to load the file has failed.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        try {
            _stream = new FileSeekableStream(_fileRoot);
        } catch (IOException error) {
            throw new IllegalActionException("Unable to load file");
        }
        _outputtedImage = JAI.create("stream", _stream);
        PlanarImage dummy = _outputtedImage.getRendering();
        try {
            _stream.close();
        } catch (IOException error) {
            throw new IllegalActionException("Unable to close stream");
        }
        output.send(0, new JAIImageToken(_outputtedImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The String that specifies where the file is located. */
    private String _fileRoot;

    /** The RenderedOp created by JAI from the stream.  This is then
     *  encapsulated by a JAIImageToken.
     */
    private RenderedOp _outputtedImage;

    /** A stream which JAI uses to create RenderedOp's */
    private FileSeekableStream _stream;
}
