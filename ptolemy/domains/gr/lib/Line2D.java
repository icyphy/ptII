/* Create a line with the endpoints provided by the user.

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
//// Line2D

/** Create a line with the endpoints provided by the user.

 @author Steve Neuendorffer, Ismael M. Sarmiento
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (ismael)
 @Pt.AcceptedRating Red (chf)
 */
public class Line2D extends GRActor2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Line2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(Scene2DToken.TYPE);

        rgbColor = new ColorAttribute(this, "rgbColor");
        rgbColor.setExpression("{0.0, 0.0, 0.0, 1.0}");

        lineWidth = new Parameter(this, "lineWidth", new DoubleToken(1.0));
        lineWidth.setTypeEquals(BaseType.DOUBLE);

        xStart = new Parameter(this, "xStart", new DoubleToken(0.0));
        xStart.setTypeEquals(BaseType.DOUBLE);

        yStart = new Parameter(this, "yStart", new DoubleToken(0.0));
        yStart.setTypeEquals(BaseType.DOUBLE);

        xEnd = new Parameter(this, "xEnd", new DoubleToken(100.0));
        xEnd.setTypeEquals(BaseType.DOUBLE);

        yEnd = new Parameter(this, "yEnd", new DoubleToken(0.0));
        yEnd.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphOut;

    /** The width of the figure's outline.  This parameter must contain a
     *  DoubleToken.  The default value is 1.0.
     */
    public Parameter lineWidth;

    /** The red, green, blue, and alpha components of the line.  This
     *  parameter must contain an array of double values.  The default
     *  value is {0.0, 0.0, 0.0, 1.0}, corresponding to opaque black.
     */
    public ColorAttribute rgbColor;

    /** The x coordinate of the line's start position in the view screen. */
    public Parameter xStart;

    /** The y coordinate of the line's start position in the view screen. */
    public Parameter yStart;

    /** The x coordinate of the line's end position in the view screen. */
    public Parameter xEnd;

    /** The y coordinate of the line's end position in the view screen. */
    public Parameter yEnd;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /* Update the position and location of the line on the screen when
     * the user changes the parameters.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if ((attribute == xStart || attribute == yStart || attribute == xEnd || attribute == yEnd)
                && _viewScreen != null) {
            _figure.setPrototypeShape(new java.awt.geom.Line2D.Double(
                    ((DoubleToken) xStart.getToken()).doubleValue(),
                    ((DoubleToken) yStart.getToken()).doubleValue(),
                    ((DoubleToken) xEnd.getToken()).doubleValue(),
                    ((DoubleToken) xEnd.getToken()).doubleValue()));
        } else if ((attribute == rgbColor || attribute == lineWidth)
                && _viewScreen != null) {
            _setAppearance(_figure);
        }

        super.attributeChanged(attribute);
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
        if (_isSceneGraphInitialized) {
            return false;
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the figure for this actor.
     *
     *  @return The BasicFigure for this actor.
     *  @exception IllegalActionException If a parameter is not valid.
     */
    protected BasicFigure _createFigure() throws IllegalActionException {
        BasicFigure figure = new BasicFigure(new java.awt.geom.Line2D.Double(
                ((DoubleToken) xStart.getToken()).doubleValue(),
                ((DoubleToken) yStart.getToken()).doubleValue(),
                ((DoubleToken) xEnd.getToken()).doubleValue(),
                ((DoubleToken) yEnd.getToken()).doubleValue()));
        _setAppearance(figure);
        return figure;
    }

    /** Setup the scene graph connections of this actor.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        sceneGraphOut.send(0, new Scene2DToken(_figure));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Set the appearance of the given figure consistent with the
    // parameters of this class.
    private void _setAppearance(BasicFigure figure)
            throws IllegalActionException {
        Paint strokePaint = rgbColor.asColor();
        figure.setStrokePaint(strokePaint);

        float lineWidthValue = (float) ((DoubleToken) lineWidth.getToken())
                .doubleValue();
        figure.setLineWidth(lineWidthValue);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    private BasicFigure _figure;
}
