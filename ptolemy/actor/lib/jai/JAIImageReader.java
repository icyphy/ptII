/* An actor that produces a javax.media.jai.RenderedOp from an
   image file specified as a URL.

@Copyright (c) 2001 The Regents of the University of California.
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

import com.sun.media.jai.codec.FileSeekableStream;

import java.io.IOException;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import ptolemy.actor.lib.Source;
import ptolemy.data.ObjectToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// JAIImageReader
/**
Load an image and create a RendredOp from it.  The file to be loaded is
specified as a relative URL from the base URL path.  Usually the base
path should be set to the root ptolemy classpath.  Supports BMP, FPX,
GIF, JPEG, PNG, PBM, PGM, PPM, and TIFF file formats.

@author James Yeh, Steve Neuendorffer
@version $Id$
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
        imageURLTemplate = new Parameter(this, "imageURLTemplate",
            new StringToken("ptolemy/actor/lib/jmf/"
                            + "goldhill.gif"));

}

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The image filename template */
    public Parameter imageURLTemplate;

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The RenderedOp being created from the URL template */
    public RenderedOp image1;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.send(0, new JAIImageToken(image1));
        //output.send(0, new ObjectToken(image1));
    }

    /** Initialize this actor.
     *  Read in the image file.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        StringToken urlToken = (StringToken)imageURLTemplate.getToken();
        String fileRoot = urlToken.stringValue();

        FileSeekableStream stream = null;
        try {
            stream = new FileSeekableStream(fileRoot);
        }
        catch (IOException e) {
        }
        image1 = JAI.create("stream", stream);
    }
}

