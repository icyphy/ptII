/* Calculates the discrete cosine transform of a RenderedOp.

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

import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIDCT
/**
Calculate the discrete cosine transform of an image.  Even though the
input image data may be 3 bytes (one for each band) per pixel, the
output image data is of a higher resolution (doubles), not suitable for
displaying or saving.  To display or save the output of this image,
use the {@link JAIDataConvert} actor to cast the data to an appropriate type
(for instance, byte).  The output of the DCT is zero padded to the
next power of 2 in each dimension (unless that dimensions is already
a power of two, in which case, nothing happens).

@see JAIDataConvert
@see JAIIDCT
@author James Yeh
@version $Id$
@since Ptolemy II 3.1
*/
public class JAIDCT extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIDCT(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the discrete cosine transform of the inputted image.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock dctParameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        dctParameters.addSource(oldImage);
        RenderedOp newImage = JAI.create("dct", dctParameters);
        output.send(0, new JAIImageToken(newImage));
    }
}
