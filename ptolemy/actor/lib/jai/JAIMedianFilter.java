/* An actor that median filters an image.

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

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.StringAttribute;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.operator.MedianFilterDescriptor;
import javax.media.jai.operator.MedianFilterShape;

//////////////////////////////////////////////////////////////////////////
//// JAIMedianFilter
/**
   An actor that median filter an image.  Median filtering is a useful
   tool when there are noise spikes in the image.  This filter is
   non-linear.

   @author James Yeh
   @version $Id$
   @since Ptolemy II 3.0
*/

public class JAIMedianFilter extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIMedianFilter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        shape = new StringAttribute(this, "shape");
        shape.setExpression("Square");
        _shape = MedianFilterDescriptor.MEDIAN_MASK_SQUARE;

        size = new Parameter(this, "size", new IntToken(3));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The filter shape to use.  There are four shapes available.
     *
     *  Square - A square mask looks at all elements in the square, and
     *  find the median value.
     *
     *  Separable Square - A separable square mask, first calculates
     *  the median of every row, and then takes the median of those
     *  values.  Only supported for size 3 and size 5 filters.
     *
     *  Plus - A 3x3 plus mask looks like the following
     *   x
     *  xxx
     *   x
     *  It calculates the median of those values indicated by an x.
     *
     *  X - A 3x3 X mask looks like the following
     *  x x
     *   x
     *  x x
     *  It calculates the median of those values indicated by an x.
     */
    public StringAttribute shape;

    /** The size of the median filter.  This indicates both the
     *  width and height of the filter.  The size must be an odd
     *  positive integer.
     */
    public Parameter size;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the shape type and size.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the function is not recognized,
     *  or if a contained method throws it.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == shape) {
            String name = shape.getExpression();
            if (name.equals("Square")) {
                _shape =
                    MedianFilterDescriptor.MEDIAN_MASK_SQUARE;
            } else if (name.equals("Separable Square")) {
                _shape =
                    MedianFilterDescriptor.MEDIAN_MASK_SQUARE_SEPARABLE;
            } else if (name.equals("Plus")) {
                _shape = MedianFilterDescriptor.MEDIAN_MASK_PLUS;
            } else if (name.equals("X")) {
                _shape = MedianFilterDescriptor.MEDIAN_MASK_X;
            } else {
                throw new IllegalActionException(this,
                        "Unrecognized Shape Name: " + name);
            }
        } else if (attribute == size) {
            _size = ((IntToken)size.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the median filtered image.
     *  @exception IllegalActionException If a contained method throws
     *  it.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        parameters.addSource(oldImage);
        parameters.add(_shape);
        parameters.add(_size);
        RenderedOp newImage = JAI.create("medianfilter", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The type of median filter shape to use.  */
    private MedianFilterShape _shape;

    /** The size of the filter */
    private int _size;
}
