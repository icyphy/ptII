/* An actor that writes to a bitmap file.

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
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.util.MessageHandler;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;

//////////////////////////////////////////////////////////////////////////
//// JAIBMPWriter
/**
Write a javax.media.jai.RenderedOp to a specified BMP file.
<p>
The file is specified by the <i>fileName</i> attribute
using any form acceptable to FileParameter.
<p>
If the <i>storeTopDown</i> parameter has value true, then the data
will be stored from the top on down.  Usually, bitmap files are stored
from the bottom on up.
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
public class JAIBMPWriter extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIBMPWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        fileName = new FileParameter(this, "fileName");

        storeTopDown = new Parameter(this, "storeTopDown");
        storeTopDown.setTypeEquals(BaseType.BOOLEAN);
        storeTopDown.setToken(BooleanToken.FALSE);

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

    /** If <i>true</i>, then write the data in top to bottom order.
     *  If <i>false</i> (the default), then write the data in bottom
     *  to top order.
     */
    public Parameter storeTopDown;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and attempt set either the fileName,
     *  whether to overwrite an existing file or not, or how the data
     *  will be written.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void attributeChanged(Attribute attribute) 
            throws IllegalActionException {
        if (attribute == fileName) {
            _file = fileName.asFile();
            _fileRoot = _file.toString();
        } else if (attribute == confirmOverwrite) {
            _confirmOverwriteValue = 
                ((BooleanToken)confirmOverwrite.getToken()).booleanValue();
        } else if (attribute == storeTopDown) {
            _storeTopDownValue = 
                ((BooleanToken)storeTopDown.getToken()).booleanValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Read an input JAIImageToken and write it to the file.
     *  If the file does not exist then create it.  If the file
     *  already exists, then query the user for overwrite.
     *  @exception IllegalActionException If the file cannot be opened
     *  or created, if the user refuses to overwrite an existing file,
     *  of if the image in unable to be encoded.
     */
    public void fire() throws IllegalActionException {
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp image = jaiImageToken.getValue();
        // Set the encoding parameters.  Note that only Version 3
        // bitmaps are currently supported, otherwise it would be
        // a parameter.
        BMPEncodeParam bmpEncodeParameters = new BMPEncodeParam();
        bmpEncodeParameters.setVersion(BMPEncodeParam.VERSION_3);
        bmpEncodeParameters.setTopDown(_storeTopDownValue);
        boolean confirmOverwriteValue
            = ((BooleanToken)confirmOverwrite.getToken())
            .booleanValue();
        if (_file.exists()) {
            if (_confirmOverwriteValue) {
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
                "BMP", _stream, bmpEncodeParameters);
        try {
            encoder.encode(image);
            _stream.close();
        } catch (IOException error) {
            throw new IllegalActionException("Couldn't encode image");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The value of the confirmOverwrite parameter. */
    private boolean _confirmOverwriteValue;

    /** The File to be saved to. */
    private File _file;

    /** The above file as a String. */
    private String _fileRoot;

    /** The value of the storeTopDown parameter. */
    private boolean _storeTopDownValue;

    /** The FileOutputStream for file writing. */
    private FileOutputStream _stream;
}
