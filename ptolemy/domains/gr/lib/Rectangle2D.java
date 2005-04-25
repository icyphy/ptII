/* Create a rectangle, rounded rectangle, or ellipse with the size and
   position specified by the user.

   Copyright (c) 2003-2005 The Regents of the University of California.
   All rights reserved.
   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the above
   copyright notice and the following two paragraphs appear in all copies
   of this software.

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

   PT_COPYRIGHT_VERSION_2
   COPYRIGHTENDKEY

*/
package ptolemy.domains.gr.lib;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import diva.canvas.toolbox.BasicFigure;


//////////////////////////////////////////////////////////////////////////
//// Rectangle2D

/**
   An actor that creates a rectangle or a rounded rectangle.  The initial
   size, position, and type of figure are specified in the parameter edit
   window and can be changed after the figure has been displayed.  The
   arcWidth and arcHeight parameters determine the "roundedness" of the
   corners, and should be zero for a boring old rectangle.

   @author Ismael M. Sarmiento, Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Green (ismael)
   @Pt.AcceptedRating Yellow (chf)
*/
public class Rectangle2D extends RectangularFigure2D {
    public Rectangle2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        arcWidth = new Parameter(this, "arcWidth", new DoubleToken(0.0));
        arcWidth.setTypeEquals(BaseType.DOUBLE);

        arcHeight = new Parameter(this, "arcHeight", new DoubleToken(0.0));
        arcHeight.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The width of the arc rounding the rectangle if applicable.*/
    public Parameter arcWidth;

    /** The height of the arc rounding the rectangle if applicable.*/
    public Parameter arcHeight;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Implement the base class to create a rectangular shape.
     *  @exception IllegalActionException If a parameter value is not valid.
     */
    protected BasicFigure _createFigure() throws IllegalActionException {
        java.awt.geom.RoundRectangle2D.Double rectangle = new java.awt.geom.RoundRectangle2D.Double();
        rectangle.setFrameFromCenter(_getCenterPoint(), _getCornerPoint());
        rectangle.arcwidth = ((DoubleToken) arcWidth.getToken()).doubleValue();
        rectangle.archeight = ((DoubleToken) arcHeight.getToken()).doubleValue();

        return new BasicFigure(rectangle);
    }

    /** Update the figure's position and size when the user changes
     *  the appropriate parameters.
     * @exception IllegalActionException If a parameter value is not valid.
     */
    protected void _updateFigure() throws IllegalActionException {
        java.awt.geom.RoundRectangle2D.Double rectangle = new java.awt.geom.RoundRectangle2D.Double();
        rectangle.setFrameFromCenter(_getCenterPoint(), _getCornerPoint());
        rectangle.arcwidth = ((DoubleToken) arcWidth.getToken()).doubleValue();
        rectangle.archeight = ((DoubleToken) arcHeight.getToken()).doubleValue();
        _figure.setPrototypeShape(rectangle);
    }
}
