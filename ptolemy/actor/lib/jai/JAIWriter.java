/* An abstract class that writes JAI Images.

@Copyright (c) 2003 The Regents of the University of California.
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
import ptolemy.util.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.ImageEncodeParam;

//////////////////////////////////////////////////////////////////////////
//// JAIWriter
/**
An abstract class that provides support for writing JAIImages
Write a javax.media.jai.RenderedOp to a specified JPEG file.

<p>Derived classes should have a postfire() method that
creates a new _imageEncodeParameters, performs any setup
on _imageEncodeParameters and then calls super.postfire()

<p>
The file is specified by the <i>fileName</i> attribute
using any form acceptable to FileParameter.
<p>
If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
then this actor will overwrite the specified file if it exists
without asking.  If <i>true</i> (the default), then if the file
exists, then this actor will ask for confirmation before overwriting.


   @see FileParameter
   @author Christopher Hylands Brooks,James Yeh
   @version $Id$
   @since Ptolemy II 4.0
*/

public abstract class JAIWriter extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);

        fileName = new FileParameter(this, "fileName");

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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read an input JAIImageToken and write it to the file.
     *  If the file does not exist then create it.  If the file
     *  already exists, then query the user for overwrite.
     *  @exception IllegalActionException If the file cannot be opened
     *  or created, if the user refuses to overwrite an existing file,
     *  of if the image in unable to be encoded.
     *  @return True if the execution can continue.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0) || _alreadyReadImageToken ) {
            if (_alreadyReadImageToken) {
                _alreadyReadImageToken = false;
            } else {
                _jaiImageToken = (JAIImageToken) input.get(0);
                _image = _jaiImageToken.getValue();
            }


            boolean confirmOverwriteValue
                = ((BooleanToken)confirmOverwrite.getToken()).booleanValue();
            _file = fileName.asFile();
            _fileRoot = _file.toString();

            if (_file.exists()) {
                if (confirmOverwriteValue) {
                    if (!MessageHandler.yesNoQuestion(
                                "OK to overwrite " + _file + "?")) {
                        throw new IllegalActionException(this,
                                "Please select another file name.");
                    }
                }
            }

            FileOutputStream stream = null;

            try {
                try {
                    stream = new FileOutputStream(_fileRoot);
                } catch (FileNotFoundException ex) {
                    throw new IllegalActionException(this, ex, 
                            "Could not create stream '" + _fileRoot + "'");
                }
            
                ImageEncoder encoder = ImageCodec.createImageEncoder(
                        _imageEncoderName, stream, _imageEncodeParam);
                if (encoder == null) {
                    throw new IllegalActionException(this,
                            "Could not create encoder for \""
                            + _imageEncoderName + "\", to \""
                            + _fileRoot
                            + "\". Perhaps the encoder name is wrong?"
                            + "encoder was: " + _imageEncodeParam);
                }
                try {
                    encoder.encode(_image);
                } catch (IOException ex) {
                    throw new IllegalActionException(this, ex,
                            "Couldn't encode image");
                }
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (Throwable throwable) {
                        System.out.println("Ignoring failure to close stream "
                                + "on " + _fileRoot);
                        throwable.printStackTrace();
                    }
                }
            }
        }
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Set to true if the input was read in by the derived class.
     *  Certain derived classes want to read in the JAIImageToken so
     *  they can adjust the output format accordingly.  These actors
     *  read the input, set the value of _jaiImageToken and _image set
     *  _alreadyReadImageToken to true and then call super.postfire(),
     *  which acts accordingly.
     *  @see #_jaiImageToken
     *  @see JAIPNGWriter
     */
    protected boolean _alreadyReadImageToken;

    /** The image that was optionally read in by the derived class.
     *  @see #_alreadyReadImageToken
     */
    protected RenderedOp _image;

    /** The name of the encoder in a format suitable for 
      * com.sun.media.jai.codec.ImageCodec, for example "BMP", or "JPG".
      */
    protected String _imageEncoderName;

    /** The encoder parameters.  Derived classes usually instantiate
     *  a format specific subclass of ImageEncodeParam and then
     *  set _imageEncodeParam to the format specific instance and
     *  then call super.postfire().
     */
    protected ImageEncodeParam _imageEncodeParam;

    /** The JAIImageToken that was optionally read in by the derived class.
     *  @see #_alreadyReadImageToken
     */
    protected JAIImageToken _jaiImageToken;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The File to be saved to. */
    private File _file;

    /** The above file as a String. */
    private String _fileRoot;
}
