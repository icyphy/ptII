/* Calculates the inverse discrete Fourier transform of a RenderedOp.

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
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.DFTDescriptor;

//////////////////////////////////////////////////////////////////////////
//// JAIIDFT
/**
Calculate the inverse discrete Fourier transform of an image.  If the
input is complex, there are two options.  One is to set the dataNature
parameter to <i>complexToReal</i> (the default).  The output would have
half the bands of the input (bands 0 and 1 get transformed into band 0,
bands 2 and 3 would get transformed into band 1, etc.), and could be
display or saved after passing through a {@link JAIDataConvert} actor.

<p>The other option would be to set it to <i>complexToComplex</i>, in which
case the output would have the same number of bands as the input.

<p>If the input is real, the only option is to set the dataNature parameter
to <i>realToComplex</i>.  An alternative to this would be to create a
complex image from this real image by using the real image as a magnitude
image, and creating a phase image of all 0's, and making a complex image
using the JAIPolarToComplex actor.

<p>The data of the output is of a high resolution (doubles), not suitable
for displaying or saving.  To display or save the output of this image,
use the JAIDataConvert Actor to cast the data to an appropriate type
(for instance, byte).

@see JAIDataConvert
@see JAIDFT
@author James Yeh
@version $Id$
@since Ptolemy II 3.1
*/
public class JAIIDFT extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIIDFT(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        scalingType = new StringAttribute(this, "scalingType");
        scalingType.setExpression("dimensions");
        _scalingType = _DIMENSIONS;

        dataNature = new StringAttribute(this, "dataNature");
        dataNature.setExpression("complexToReal");
        _dataNature = _COMPLEX_TO_REAL;

        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** A parameter that describes the nature of the input and output
     *  data.  The default is <i>complexToReal</i> (so that the output
     *  can be saved and/or displayed after putting it through the
     *  {@link JAIDataConvert actor).  The setting
     *  <i>complexToComplex</i> can also be used.  The setting
     *  <i>realToComplex</i> should probably not be used.
     */
    public StringAttribute dataNature;

    /** The scaling to be done on the output.  There are three options,
     *  <i>none</i> (does no scaling), <i>unitary</i> (multiplies by
     *  square root of the product of the dimensions), and
     *  <i>dimensions</i> (the default, multiplies by the product of the
     *  dimensions).  In a DFT-IDFT chain, the overall scaling should
     *  equal the product of the dimensions.
     */
    public StringAttribute scalingType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the parameters.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == dataNature) {
            String natureName = dataNature.getExpression();
            if (natureName.equals("complexToComplex")) {
                _dataNature = _COMPLEX_TO_COMPLEX;
            } else if (natureName.equals("complexToReal")) {
                _dataNature = _COMPLEX_TO_REAL;
            } else if (natureName.equals("realToComplex")) {
                _dataNature = _REAL_TO_COMPLEX;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized dataNature type: " + natureName);
            }
        } else if (attribute == scalingType) {
            String typeName = scalingType.getExpression();
            if (typeName.equals("dimensions")) {
                _scalingType = _DIMENSIONS;
            } else if (typeName.equals("unitary")) {
                _scalingType = _UNITARY;
            } else if (typeName.equals("none")) {
                _scalingType = _NONE;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized scaling type: " + typeName);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the inverse discrete Fourier transform of the inputted
     *  image.
     *  @exception IllegalActionException If a contained method throws it,
     *  or if there is an invalid scaling type, or an invalid data nature
     *  set.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock idftParameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        idftParameters.addSource(oldImage);

        switch(_scalingType) {
        case _DIMENSIONS:
            idftParameters.add(DFTDescriptor.SCALING_DIMENSIONS);
            break;
        case _NONE:
            idftParameters.add(DFTDescriptor.SCALING_NONE);
            break;
        case _UNITARY:
            idftParameters.add(DFTDescriptor.SCALING_UNITARY);
            break;
        default:
            throw new IllegalActionException(this,
                    "Invalid value for scaling type");
        }

        switch(_dataNature) {
        case _COMPLEX_TO_COMPLEX:
            idftParameters.add(DFTDescriptor.COMPLEX_TO_COMPLEX);
            break;
        case _COMPLEX_TO_REAL:
            idftParameters.add(DFTDescriptor.COMPLEX_TO_REAL);
            break;
        case _REAL_TO_COMPLEX:
            idftParameters.add(DFTDescriptor.REAL_TO_COMPLEX);
            break;
        default:
            throw new IllegalActionException(this,
                    "Invalid data natures");
        }
        RenderedOp newImage = JAI.create("idft", idftParameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** An indicator for the type of data being inputted and the type
     *  of data being outputted.
     */
    private int _dataNature;

    /** An indicator for the type of scaling done */
    private int _scalingType;

    /** Constants used for more efficient computation */
    private static final int _COMPLEX_TO_COMPLEX = 0;
    private static final int _COMPLEX_TO_REAL = 1;
    private static final int _REAL_TO_COMPLEX = 2;

    private static final int _DIMENSIONS = 0;
    private static final int _NONE = 1;
    private static final int _UNITARY = 2;
}
