/* An actor that scales a javax.media.jai.RenderedOp

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

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.DoubleToken;
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

//////////////////////////////////////////////////////////////////////////
//// JAIInterpolation
/**
   Scale a RenderedOp using the javax.media.jai.JAI class.

   @author James Yeh
   @version $Id$
 */

public class JAIInterpolation extends Transformer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public JAIInterpolation(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT);

        xScaleFactor =
            new Parameter(this, "xScaleFactor", new DoubleToken("1.0F"));
        yScaleFactor =
            new Parameter(this, "yScaleFactor", new DoubleToken("1.0F"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The scaling factor in the horizontal direction.  The default
     *  value of this parameter is the double value 1.0
     */
    public Parameter xScaleFactor;

    /** The scaling factor in the vertical direction.  The default
     *  value of this parameter is the double value 1.0
     */
    public Parameter yScaleFactor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Fire this actor.
     *  Output the scaled RenderedOp.
     *  @exception IllegalActionException If a contained method throws it,
     *   or if a token is received that contains a null image.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        _parameters = new ParameterBlock();
        JAIImageToken jaiImageToken = (JAIImageToken) input.get(0);
        RenderedOp oldImage = jaiImageToken.getValue();
        _parameters.addSource(oldImage);
        _parameters.add((float)_xScaleFactor);
        _parameters.add((float)_yScaleFactor);
        _parameters.add(0.0F);
        _parameters.add(0.0F);
        _parameters.add(_interp);
        RenderedOp newImage = JAI.create("scale", _parameters);
        output.send(0, new JAIImageToken(newImage));
    }

    /** Initialize this actor.
     *  Set the horizontal and vertical scaling values.
     *  @exception IllegalActionException If a contained method throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _xScaleFactor = ((DoubleToken)xScaleFactor.getToken()).doubleValue();
        _yScaleFactor = ((DoubleToken)yScaleFactor.getToken()).doubleValue();
        _interp = Interpolation.getInstance(
                Interpolation.INTERP_BILINEAR);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The horizontal scaling factor. */
    private double _xScaleFactor;

    /** The vertical scaling factor. */
    private double _yScaleFactor;

    /** The type of Interpolation being used is specified using this
     *  variable.
     */
    private Interpolation _interp;

    /** The block that holds the horizontal and vertical scaling
     *  factor, the horizontal and vertical translation factor
     * (unused),  and the type of interpolation being used.
     */
    private ParameterBlock _parameters;
}
