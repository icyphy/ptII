/* Calculates the inverse discrete Fourier transform of a RenderedOp.

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

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.DFTDescriptor;

import ptolemy.actor.lib.Transformer;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// JAIIDFT
/**
   Calculate the inverse discrete Fourier transform of an image.  The 
   output has half as many bands as the input.  Bands 0 and 1 get 
   transformed into Band 0.  Bands 2 and 3 get transformed into Band 1,
   etc.
   <p>
   The data of the output is of a high resolution, not suitable for
   displaying or saving.  To display or save the output of this image,
   use the JAIDataCaster Actor to cast the data to an appropriate type
   (for instance, byte).
 
   @see JAIDataCaster
   @see JAIDFT
   @author James Yeh
   @version $Id$
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

    /** Fill in here.
     */
    public StringAttribute dataNature;

    /** Fill in here.
     */
    public StringAttribute scalingType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fill in here.
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
     *  @exception IllegalActionException If a contained method throws it.
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
            throw new IllegalActionException(
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
            throw new IllegalActionException(
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
