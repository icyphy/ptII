/* An actor that writes to a JPEG file.

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.RenderedOp;

import ptolemy.actor.lib.Sink;
import ptolemy.actor.parameters.IntRangeParameter;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;

//////////////////////////////////////////////////////////////////////////
//// JAIJPEGWriter
/**
   Write a javax.media.jai.RenderedOp to a specified JPEG file.
   <p>
   The file is specified by the <i>fileName</i> attribute
   using any form acceptable to FileParameter.
   <p>
   If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
   then this actor will overwrite the specified file if it exists
   without asking.  If <i>true</i> (the default), then if the file
   exists, then this actor will ask for confirmation before overwriting.

   @see FileParameter
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIJPEGWriter extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIJPEGWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);

        fileName = new FileParameter(this, "fileName");

        writeJFIFHeader = new Parameter(this, "writeJFIFHeader");
        writeJFIFHeader.setTypeEquals(BaseType.BOOLEAN);
        writeJFIFHeader.setToken(BooleanToken.TRUE);

//          writeImageDataOnly = new Parameter(this, "writeImageDataOnly");
//          writeImageDataOnly.setTypeEquals(BaseType.BOOLEAN);
//          writeImageDataOnly.setToken(BooleanToken.FALSE);

//          writeTableDataOnly = new Parameter(this, "writeTableDataOnly");
//          writeTableDataOnly.setTypeEquals(BaseType.BOOLEAN);
//          writeTableDataOnly.setToken(BooleanToken.FALSE);

        quality = new IntRangeParameter(this, "quality");

//          useDefaultLuminanceTable
//              = new Parameter(this, "useDefaultLuminanceTable");
//          useDefaultLuminanceTable.setTypeEquals(BaseType.BOOLEAN);
//          useDefaultLuminanceTable.setToken(BooleanToken.TRUE);

//          userSpecifiedLuminanceTable
//              = new Parameter(this, "userSpecifiedLuminanceTable",
//                      new ArrayToken(_defaultSpecifiedTable));

//          useDefaultChrominanceTable =
//              new Parameter(this, "useDefaultChrominanceTable");
//          useDefaultChrominanceTable.setTypeEquals(BaseType.BOOLEAN);
//          useDefaultChrominanceTable.setToken(BooleanToken.TRUE);

//          userSpecifiedChrominanceTable
//              = new Parameter(this, "userSpecifiedChrominanceTable",
//                      new ArrayToken(_defaultSpecifiedTable));

//          horizontalSubsampling
//              = new Parameter(this, "horizontalSubsampling",
//                      new ArrayToken(_defaultSubsampling));

//          verticalSubsampling
//              = new Parameter(this, "verticalSubsampling",
//                      new ArrayToken(_defaultSubsampling));

        restartInterval
            = new Parameter(this, "restartInterval",
                    new IntToken(0));

        confirmOverwrite = new Parameter(this, "confirmOverwrite");
        confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);
        confirmOverwrite.setToken(BooleanToken.TRUE);

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name to which to write.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FileParameter fileName;

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

    /** The subsampling scheme in the horizontal direction.  This
     *  parameter consists of three integers.  If there is only one
     *  band, such as in grayscale, then only the first value is used.
     *  Otherwise, all three values are used.  The default values are
     *  (1, 2, 2) because the two chrominance bands can be subsampled
     *  because the human visual system has a lower sensitivity to
     *  errors in these two bands compared to the luminance band.
     */
    //public Parameter horizontalSubsampling;

    /** The quality of the file written.  The quality ranges from 0
     *  which is a high amount of compression, small file size, and
     *  poor picture quality, to 100, which is no compression, larger
     *  file size, and high picture quality.
     */
    public IntRangeParameter quality;

    /** The restart interval in number of JPEG Minimum Coded Units
     *  (MCUs).  JPEG images can use these restart markers to
     *  periodically delineate image segments to limit the effect
     *  of bitstream errors to a single interval.  The default
     *  is zero.
     */
    public Parameter restartInterval;

    /** If <i>true</i> (the default), then the encoder will use the
     *  default chrominance quantization table.  The default table
     *  depends on the Quality parameter.
     *  If <i>false</i> then the encoder will use the user specified
     *  luminance table.  The Quality parameter is ignored in the
     *  creation of the chrominance table.
     */
    //public Parameter useDefaultChrominanceTable;

    /** If <i>true</i> (the default), then the encoder will use the
     *  default luminance quantization table.  The default table
     *  depends on the Quality parameter.
     *  If <i>false</i> then the encoder will use the user specified
     *  luminance table.  The Quality parameter is ignored in the
     *  creation of the luminance table.
     */
    //public Parameter useDefaultLuminanceTable;

    /** If the user decides not to use the default chrominance
     *  quantization table then the user must specify a table to use
     *  in zig-zag order.  The table must be 8x8, hence then array
     *  must have 64 values.
     */
    //public Parameter userSpecifiedChrominanceTable;

    /** If the user decides not to use the default luminance
     *  quantization table then the user must specify a table to use
     *  in zig-zag order.  The table must be 8x8, hence then array
     *  must have 64 values.
     */
    //public Parameter userSpecifiedLuminanceTable;

    /** The subsampling scheme in the vertical direction.  This
     *  parameter consists of three integers.  If there is only one
     *  band, such as in grayscale, then only the first value is used.
     *  Otherwise, all three values are used.  The default values are
     *  (1, 2, 2) because the two chrominance bands can be subsampled
     *  because the human visual system has a lower sensitivity to
     *  errors in these two bands compared to the luminance band.
     */
    //public Parameter verticalSubsampling;

    /** If <i>true</i>, the encoder will only write the image data
     *  to the file.  If <i>false</i> (the default) and if
     *  writeTableDataOnly is false, then the file will be written
     *  with both table and image data.  If both are true, an
     *  exception will be thrown.
     */
    //public Parameter writeImageDataOnly;

    /** If <i>true</i> (the default), the encoder will write a JFIF
     *  header, using a marker.The marker includes data such as
     *  version number, x and y pixel density, pixel aspect ratio.
     *  If <i>false</i>, then the encoder will not write a JFIF header.
     */
    public Parameter writeJFIFHeader;

    /** If <i>true</i>, the encoder will only write the table data
     *  to the file.  If <i>false</i> (the default) and if
     *  writeImageDataOnly is false, then the file will be written
     *  with both table and image data.  If both are true, an
     *  exception will be thrown.
     */
    //public Parameter writeTableDataOnly;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize this actor.
     *  Set the encoding parameters.
     *  @exception IllegalActionException If a contained method throws,
     *  it, or if both writeImageDataOnly and writeTableDataOnly are
     *  set to true.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _file = fileName.asFile();
        _fileRoot = _file.toString();
        _jpegEncodeParameters = new JPEGEncodeParam();
        _jpegEncodeParameters.setWriteJFIFHeader(
                ((BooleanToken)writeJFIFHeader.getToken()).booleanValue());
//          boolean writeImageDataOnlyValue =
//              ((BooleanToken)writeImageDataOnly.getToken()).booleanValue();
//          boolean writeTableDataOnlyValue =
//              ((BooleanToken)writeTableDataOnly.getToken()).booleanValue();
//          if (writeImageDataOnlyValue && writeTableDataOnlyValue) {
//              throw new IllegalActionException("Both Parameters cannot be true");
//          } else {
//              _jpegEncodeParameters.setWriteTablesOnly(writeTableDataOnlyValue);
//              _jpegEncodeParameters.setWriteImageOnly(writeImageDataOnlyValue);
//          }
        _jpegEncodeParameters.setQuality(0.01f * quality.getCurrentValue());
//          if (!((BooleanToken)useDefaultLuminanceTable
//                  .getToken()).booleanValue()) {

//              // make a private method to do all this, one that returns
//              // an int array

//              _jpegEncodeParameters.setLumaQTable(
//                      _tableFiller((ArrayToken)userSpecifiedLuminanceTable
//                              .getToken()));
//          }
//          if (!((BooleanToken)useDefaultChrominanceTable
//                  .getToken()).booleanValue()) {
//              _jpegEncodeParameters.setChromaQTable(
//                      _tableFiller((ArrayToken)userSpecifiedChrominanceTable
//                              .getToken()));
//          }

//          Token horizontalSubsamplingData[]
//              = ((ArrayToken)horizontalSubsampling.getToken()).arrayValue();
//          Token verticalSubsamplingData[]
//              = ((ArrayToken)verticalSubsampling.getToken()).arrayValue();
        //for (int i = 0; i < 3; i = i + 1) {
        //    _jpegEncodeParameters.setHorizontalSubsampling(i,
        //            ((IntToken)horizontalSubsamplingData[i]).intValue());
        //    _jpegEncodeParameters.setVerticalSubsampling(i,
        //            ((IntToken)verticalSubsamplingData[i]).intValue());
        //}
        _jpegEncodeParameters.setRestartInterval(((IntToken)restartInterval
                .getToken()).intValue());

    }

    /** Read an input JAIImageToken and write it to the file.
     *  If the file does not exist then create it.  If the file
     *  already exists, then query the user for overwrite.
     *  @exception IllegalActionException If the file cannot be opened
     *  or created, if the user refuses to overwrite an existing file,
     *  of if the image in unable to be encoded.
     */
    public boolean postfire() throws IllegalActionException {
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp image = jaiImageToken.getValue();
        boolean confirmOverwriteValue
            = ((BooleanToken)confirmOverwrite.getToken()).booleanValue();
        if (_file.exists()) {
            if (confirmOverwriteValue) {
                if (!MessageHandler.yesNoQuestion(
                        "OK to overwrite " + _file + "?")) {
                    throw new IllegalActionException(this,
                            "Please select another file name.");
                }
            }
        }
        else {
            //file doesn't exist, so create new file
            try {
                if (!_file.createNewFile()) {
                    throw new IllegalActionException(this, "Couldn't" +
                            " create file");
                }
            }
            catch (IOException error) {
                throw new IllegalActionException("Couldn't create file");
            }
        }
        try {
            _stream = new FileOutputStream(_fileRoot);
        } catch (FileNotFoundException error) {
            throw new IllegalActionException("Could not create stream");
        }
        ImageEncoder encoder = ImageCodec.createImageEncoder(
                "JPEG", _stream, _jpegEncodeParameters);
        try {
            encoder.encode(image);
            _stream.close();
        } catch (IOException error) {
            throw new IllegalActionException("Couldn't encode image");
        }
        //return false;
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Takes a ArrayToken (assumed to be filled with IntTokens), and
     *  returns the array of integers is contains.
     */
    private int[] _tableFiller(ArrayToken array) {
        Token tokenArray[] = array.arrayValue();
        int intArray[] = new int[tokenArray.length];
        for (int i = 0; i < tokenArray.length; i = i + 1) {
            int _value = ((IntToken)(tokenArray[i])).intValue();
            intArray[i] = _value;
        }
        return intArray;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** IntTokens used in the creation of IntToken arrays. */
    private IntToken _one = new IntToken(1);
    private IntToken _two = new IntToken(2);

    /** The default Subsampling array */
    private IntToken _defaultSubsampling[] = {_one, _two, _two};

    /** The default quantization table */
    private IntToken _defaultSpecifiedTable[] =
    {_one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one,
     _one, _one, _one, _one, _one, _one, _one, _one, };

    /** The data structure that contains all the encoding parameters. */
    private JPEGEncodeParam _jpegEncodeParameters;

    /** The File to be saved to. */
    private File _file;

    /** The above file as a String. */
    private String _fileRoot;

    /** The FileOutputStream for file writing. */
    private FileOutputStream _stream;
}
