/* An actor that writes to a PNG file.

@Copyright (c) 1998-2002 The Regents of the University of California.
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

import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.media.jai.RenderedOp;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.PNGEncodeParam;

//////////////////////////////////////////////////////////////////////////
//// JAIPNGWriter
/**
   Write a javax.media.jai.RenderedOp to a specified PNG file.
   <p>
   The file is specified by the <i>fileName</i> attribute
   using any form acceptable to FileAttribute.
   <p>
   If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
   then this actor will overwrite the specified file if it exists
   without asking.  If <i>true</i> (the default), then if the file
   exists, then this actor will ask for confirmation before overwriting.

   @see FileAttribute
   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIPNGWriter extends Sink {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIPNGWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);

        fileName = new FileAttribute(this, "fileName");

        confirmOverwrite = new Parameter(this, "confirmOverwrite");
        confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);
        confirmOverwrite.setToken(BooleanToken.TRUE);

        bitDepth = new Parameter(this, "bitDepth", new IntToken(8));

        adam7Interlacing = new Parameter(this, "adam7Interlacing");
        adam7Interlacing.setTypeEquals(BaseType.BOOLEAN);
        adam7Interlacing.setToken(BooleanToken.TRUE);

        setGamma = new Parameter(this, "setGamma");
        setGamma.setTypeEquals(BaseType.BOOLEAN);
        setGamma.setToken(BooleanToken.FALSE);

        gamma = new Parameter(this, "gamma", new DoubleToken(0.455F));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The file name to which to write.  This is a string with
     *  any form accepted by FileAttribute.
     *  @see FileAttribute
     */
    public FileAttribute fileName;

    public Parameter adam7Interlacing;

    public Parameter bitDepth;

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

    public Parameter gamma;

    public Parameter setGamma;

    //public Parameter setTransparency;

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
        } else if (attribute == adam7Interlacing) {
            _adam7Interlacing = 
                ((BooleanToken)adam7Interlacing.getToken()).booleanValue();
        } else if (attribute == setGamma) {
            _setGamma = ((BooleanToken)setGamma.getToken()).booleanValue();
        } else if (attribute == gamma) {
            _gamma = ((DoubleToken)gamma.getToken()).doubleValue();
        } else if (attribute == bitDepth) {
            _bitDepth = ((IntToken)bitDepth.getToken()).intValue();
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
        } else {
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
        PNGEncodeParam parameters = 
            PNGEncodeParam.getDefaultEncodeParam(image);

        parameters.setBitDepth(_bitDepth);
        parameters.setInterlacing(_adam7Interlacing);

        ImageEncoder encoder = ImageCodec.createImageEncoder(
            "PNG", _stream, parameters);
        try {
            encoder.encode(image);
            _stream.close();
        } catch (IOException error) {
            throw new IllegalActionException("Couldn't encode image");
        }
        return true;
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private int _bitDepth;
    
    /** The value of the confirmOverwrite parameter. */
    private boolean _confirmOverwriteValue;

    /** The File to be saved to. */
    private File _file;

    /** The above file as a String. */
    private String _fileRoot;

    /** The value of the storeTopDown parameter. */
    private boolean _adam7Interlacing;

    /** The FileOutputStream for file writing. */
    private FileOutputStream _stream;

    private boolean _setGamma;

    private double _gamma;
}
