/* An actor that box periodically shifts an image.

 @Copyright (c) 2002-2014 The Regents of the University of California.
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

import ptolemy.actor.lib.Transformer;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// JAIPeriodicShift

/**
 An actor that computes the periodic translation of an image.  The
 output is clamped version of the infinitely periodic image with the
 horizontal and vertical periods equal to the image width and height
 respectively.

 @author James Yeh
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class JAIPeriodicShift extends Transformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIPeriodicShift(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        xShift = new Parameter(this, "xShift", new IntToken(0));
        yShift = new Parameter(this, "yShift", new IntToken(0));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The amount to shift in the horizontal direction.  This value
     *  must be positive.  An increase in this value causes the image
     *  to move to the left.
     */
    public Parameter xShift;

    /** The amount to shift in the vertical direction.  This value must
     *  be positive.  An increase in this value causes the image to move
     *  upwards.
     */
    public Parameter yShift;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class and set the amount to shift vertically
     *  and horizontally.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If a contained method throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == xShift) {
            _xShift = ((IntToken) xShift.getToken()).intValue();
        } else if (attribute == yShift) {
            _yShift = ((IntToken) yShift.getToken()).intValue();
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Fire this actor.
     *  Output the shifted RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ParameterBlock parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();

        parameters.addSource(oldImage);
        parameters.add(_xShift);
        parameters.add(_yShift);

        RenderedOp newImage = JAI.create("PeriodicShift", parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The horizontal shifting amount.  */
    private int _xShift;

    /** The vertical shifting amount.  */
    private int _yShift;
}
