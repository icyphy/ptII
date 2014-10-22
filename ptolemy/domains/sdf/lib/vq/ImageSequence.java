/* Load a sequence of binary images from files.

 @Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.sdf.lib.vq;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ptolemy.actor.lib.Source;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// ImageSequence

/**
 Load a sequence of binary images from files, and create a sequence of
 IntMatrixTokens from them.  The data is assumed to row scanned, starting
 at the top row.  Each byte of the binary file is assumed to be the
 greyscale intensity of a single pixel in the image.
 <p>
 The files to be loaded are specified as relative URLs from the base URL path.
 Usually the base path should be set to the root ptolemy classpath.
 The file names are created by replacing *'s in the filename with consecutive
 integers (using zero padding).  For example, specifying a URLtemplate of
 "missa***.qcf" and a starting frame of
 zero, will create the names:
 <ul>
 <li>missa000.qcf
 <li>missa001.qcf
 <li>missa002.qcf
 <li>...
 </ul>
 The name manufacturing algorithm is not especially robust, so
 debug listeners attached to this actor will receive a list of the file names.

 This actor could be greatly expanded to use the Java Advanced Imaging API
 for loading images.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Yellow (neuendor)
 @Pt.AcceptedRating Red
 */
public class ImageSequence extends Source {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageSequence(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.INT_MATRIX);

        imageURLTemplate = new Parameter(this, "imageURLTemplate",
                new StringToken("ptolemy/domains/sdf/lib/vq"
                        + "/data/seq/missa/missa***.qcf"));
        imageColumns = new Parameter(this, "imageColumns", new IntToken("176"));
        imageRows = new Parameter(this, "imageRows", new IntToken("144"));
        startFrame = new Parameter(this, "startFrame", new IntToken("0"));
        endFrame = new Parameter(this, "endFrame", new IntToken("29"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The image filename templates. */
    public Parameter imageURLTemplate;

    /** The number of columns in each image. */
    public Parameter imageColumns;

    /** The number of rows in each image. */
    public Parameter imageRows;

    /** The starting frame number. */
    public Parameter startFrame;

    /** The ending frame number. */
    public Parameter endFrame;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize this actor.
     *  Read in the image files.
     *  @exception IllegalActionException If any of the input files could not
     *  be read.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        InputStream source = null;

        String fileRoot = ((StringToken) imageURLTemplate.getToken())
                .stringValue();
        _startFrame = ((IntToken) startFrame.getToken()).intValue();
        _endFrame = ((IntToken) endFrame.getToken()).intValue();
        _imageColumns = ((IntToken) imageColumns.getToken()).intValue();
        _imageRows = ((IntToken) imageRows.getToken()).intValue();

        // If we've already loaded all these images, then don't load
        // them again.
        if (_images != null) {
            return;
        }

        _frameCount = _endFrame - _startFrame + 1;
        _images = new IntMatrixToken[_frameCount];
        _frameInts = new int[_imageRows][_imageColumns];
        _frameBytes = new byte[_imageRows * _imageColumns];

        for (_frameNumber = 0; _frameNumber < _frameCount; _frameNumber++) {
            try {
                // Assemble the file name, replacing '*'
                byte[] arr = fileRoot.getBytes();
                int i;
                int j;
                int n;
                i = _frameNumber + _startFrame;

                String temporaryFileName = fileRoot;
                int location = temporaryFileName.lastIndexOf('*');

                while (location >= 0) {
                    arr[location] = (byte) ('0' + i % 10);
                    i = i / 10;
                    temporaryFileName = new String(arr);
                    location = temporaryFileName.lastIndexOf('*');
                }

                String fileName = new String(arr);
                _debug("file = " + fileName + "\n");

                URL dataurl = getClass().getClassLoader().getResource(fileName);

                if (dataurl == null) {
                    throw new FileNotFoundException("Failed to find '"
                            + fileName + "' as " + "a resource");
                }

                source = dataurl.openStream();

                // Load the frame from the file.
                if (_fullRead(source, _frameBytes) != _imageRows
                        * _imageColumns) {
                    throw new IllegalActionException("Error reading "
                            + "image file!");
                }

                // This is necessary to convert from bytes to ints
                for (i = 0, n = 0; i < _imageRows; i++) {
                    for (j = 0; j < _imageColumns; j++, n++) {
                        _frameInts[i][j] = _frameBytes[n] & 255;
                    }
                }

                _images[_frameNumber] = new IntMatrixToken(_frameInts);
            } catch (IllegalActionException ex) {
                _images = null;
                throw new IllegalActionException(this, ex,
                        "Failed to initialize");
            } catch (Exception ex) {
                _images = null;
                throw new IllegalActionException(this, ex,
                        "Failed to initialize");
            } finally {
                if (source != null) {
                    try {
                        source.close();
                    } catch (IOException ex) {
                        _images = null;
                        throw new IllegalActionException(this, ex,
                                "Failed to close source");
                    }
                }
            }
        }

        _frameNumber = 0;
    }

    /** Fire this actor.
     *  Output the next image in the sequence.  If the sequence has no more
     *  images, then loop back to the first image in the sequence.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        output.send(0, _images[_frameNumber]);
        _frameNumber++;

        if (_frameNumber >= _frameCount) {
            _frameNumber = 0;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private int _fullRead(InputStream s, byte[] b) throws IOException {
        int length = 0;
        int remaining = b.length;
        int bytesRead = 0;

        while (remaining > 0) {
            bytesRead = s.read(b, length, remaining);

            if (bytesRead == -1) {
                throw new IOException("Unexpected EOF:" + s);
            }

            remaining -= bytesRead;
            length += bytesRead;
        }

        return length;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private int _frameCount;

    private IntMatrixToken[] _images;

    private byte[] _frameBytes;

    private int[][] _frameInts;

    private int _imageColumns;

    private int _imageRows;

    private int _startFrame;

    private int _endFrame;

    private int _frameNumber;
}
