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
import ptolemy.util.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import com.sun.media.jai.codec.BMPEncodeParam;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;

//////////////////////////////////////////////////////////////////////////
//// JAIBMPWriter
/**
Write a javax.media.jai.RenderedOp to a specified BMP file.

<p>The file is specified by the <i>fileName</i> attribute
using any form acceptable to FileParameter.

<p>If the <i>storeTopDown</i> parameter has value true, then the data
will be stored from the top on down.  Usually, bitmap files are stored
from the bottom on up.

<p>If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
then this actor will overwrite the specified file if it exists
without asking.  If <i>true</i> (the default), then if the file
exists, then this actor will ask for confirmation before overwriting.

@see FileParameter
@author James Yeh, Christopher Hylands Brooks
@version $Id$
@since Ptolemy II 3.0
*/
public class JAIBMPWriter extends JAIWriter {

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

        fileName.setExpression("file.bmp");

        storeTopDown = new Parameter(this, "storeTopDown");
        storeTopDown.setTypeEquals(BaseType.BOOLEAN);
        storeTopDown.setToken(BooleanToken.FALSE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If <i>true</i>, then write the data in top to bottom order.
     *  If <i>false</i> (the default), then write the data in bottom
     *  to top order.
     */
    public Parameter storeTopDown;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

   /**  Read an input JAIImageToken and write it to the file.
     *  If the file does not exist then create it.  If the file
     *  already exists, then query the user for overwrite.
     *  @exception IllegalActionException If the file cannot be opened
     *  or created, if the user refuses to overwrite an existing file,
     *  of if the image in unable to be encoded.
     *  @return True if the execution can continue.
     */
    public boolean postfire() throws IllegalActionException {
        _imageEncoderName = "BMP";
        // Set the encoding parameters.  Note that only Version 3
        // bitmaps are currently supported, otherwise it would be
        // a parameter.
        BMPEncodeParam bmpEncodeParam = new BMPEncodeParam();
        bmpEncodeParam.setVersion(BMPEncodeParam.VERSION_3);
        bmpEncodeParam.setTopDown(
                ((BooleanToken)storeTopDown.getToken()).booleanValue());
        _imageEncodeParam = bmpEncodeParam;
        return super.postfire();
    }
}
