/* An abstract base class for shaded GR Actors

 Copyright (c) 2003-2014 The Regents of the University of California.
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

import java.awt.Paint;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import diva.canvas.toolbox.BasicFigure;

///////////////////////////////////////////////////////////////////
//// GRShape2D

/** An abstract base class for two-dimensional GR Actors representing
 figures.  The color of the figure is chosen from a color chooser dialog,
 or can be entered manually as an array of double values of the form
 {red, green, blue, alpha}.

 @author Steve Neuendorffer, Ismael M. Sarmiento
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
abstract public class GRShape2D extends GRActor2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRShape2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(Scene2DToken.TYPE);

        rgbFillColor = new ColorAttribute(this, "rgbFillColor");
        rgbFillColor.setExpression("{1.0, 1.0, 1.0, 1.0}");

        rgbOutlineColor = new ColorAttribute(this, "rgbOutlineColor");
        rgbOutlineColor.setExpression("{0.0, 0.0, 0.0, 1.0}");

        outlineWidth = new Parameter(this, "outlineWidth", new DoubleToken(1.0));
        outlineWidth.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The output port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphOut;

    /** The red, green, blue, and alpha components of the interior color
     *  of the figure.  This parameter must contain an array of double values.
     *  The default value is {1.0, 1.0, 1.0, 1.0},
     *  corresponding to opaque white.
     */
    public ColorAttribute rgbFillColor;

    /** The red, green, blue and alpha components of the outline color
     *  of the figure.  This parameter must contain an array of double values.
     *  The default value is {0.0, 0.0, 0.0, 1.0},
     *  corresponding to opaque black.
     */
    public ColorAttribute rgbOutlineColor;

    /** The width of the figure's outline.  This parameter must contain a
     *  DoubleToken.  The default value is 1.0.
     */
    public Parameter outlineWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Update the position and location of the figure on the screen when
     *  the user changes the parameters.
     *  @param attribute The attribute which changed.
     *  @exception IllegalActionException If thrown while setting the
     *  appearance of the figure or if thrown by the parent class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if ((attribute == rgbFillColor || attribute == rgbOutlineColor || attribute == outlineWidth)
                && _viewScreen != null) {
            _setAppearance(_figure);
        }

        super.attributeChanged(attribute);
    }

    /** Get the figure represented by this actor.
     *  @return The figure.
     */
    public BasicFigure getFigure() {
        return _figure;
    }

    /** Create the figure for this actor.
     *
     *  @exception IllegalActionException If the current director
     *  is not a GRDirector.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _figure = _createFigure();
        _setAppearance(_figure);
    }

    /** Return false if the scene graph is already initialized.
     *
     *  @return false if the scene graph is already initialized.
     *  @exception IllegalActionException Not thrown in this base class
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        if (!super.prefire()) {
            return false;
        }
        if (_isSceneGraphInitialized) {
            return false;
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the figure for this actor.  Derived classes should implement
     *  this method to create the correct figure.
     *  @return A new Figure.
     *  @exception IllegalActionException If a parameter is not valid.
     */
    abstract protected BasicFigure _createFigure()
            throws IllegalActionException;

    /** Setup the scene graph connections of this actor.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new Scene2DToken(_figure));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The figure represented by this actor. */
    protected BasicFigure _figure;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Set the appearance of the given figure consistent with the
    // parameters of this class.
    private void _setAppearance(BasicFigure figure)
            throws IllegalActionException {
        Paint fillPaint = rgbFillColor.asColor();
        Paint strokePaint = rgbOutlineColor.asColor();
        float lineWidth = (float) ((DoubleToken) outlineWidth.getToken())
                .doubleValue();

        figure.setFillPaint(fillPaint);
        figure.setStrokePaint(strokePaint);
        figure.setLineWidth(lineWidth);
    }
}
