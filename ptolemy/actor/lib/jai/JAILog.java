/* An actor that outputs the logarithm of an image.

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
//// JAILog
/**
Output the logarithm of an image.  This actor can be used to reduce the
dynamic range of an image.  For instance, the magnitude of a dft can
have a large dynamic range not suitable for display.  Taking the
logarithm of this image and then casting it using {@link JAIDataConvert} could
produce a better image for displaying.

<p> For integral image data types, the result is rounded and clamped.  For
all integral image data types, the log of 0 is 0.  For signed image
data types (shorts and ints), the log of a negative pixel is -1.  For
floats and doubles, the log of 0 is set to -Infinity, and the log of a
negative pixel is NaN.

<p> The output of this actor may not be suitable for display because of the
high resolution of the data.  To display or save the output of this
image, use the {@link JAIDataConvert} actor to cast the data to an appropriate
type (for instance, byte).

@see JAIDataConvert
@author James Yeh
@version $Id$
@since Ptolemy II 3.1
*/

public class JAILog extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAILog(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the phase of the complex image.
     *  @exception IllegalActionException If a contained method throws it,
     *  or if there is an invalid scaling type, or an invalid data nature
     *  set.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        parameters.addSource(oldImage);
        RenderedOp newImage = JAI.create("log", parameters);
        output.send(0, new JAIImageToken(newImage));
    }
}
