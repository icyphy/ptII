/* An abstract base class for transforming input 2D shape

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.gr.kernel.GRActor2D;
import ptolemy.domains.gr.kernel.Scene2DToken;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;

///////////////////////////////////////////////////////////////////
//// GRTransform2D

/**
 An abstract base class for a transform operator of two-dimensional
 GR shapes.

 The parameter <i>accumulate</i> determines whether transformations are
 accumulated or reset during firing.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (chf)
 @Pt.AcceptedRating Red (chf)
 */
abstract public class GRTransform2D extends GRActor2D {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public GRTransform2D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        sceneGraphIn = new TypedIOPort(this, "sceneGraphIn");
        sceneGraphIn.setInput(true);
        sceneGraphIn.setMultiport(true);
        sceneGraphIn.setTypeEquals(Scene2DToken.TYPE);

        sceneGraphOut = new TypedIOPort(this, "sceneGraphOut");
        sceneGraphOut.setOutput(true);
        sceneGraphOut.setTypeEquals(Scene2DToken.TYPE);

        accumulate = new Parameter(this, "accumulate", new BooleanToken(false));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Boolean value determining whether transformations are
     *  accumulated or reset for each firing.
     */
    public Parameter accumulate;

    /** The input port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphIn;

    /** The output port for connecting to other GR Actors in
     *  the scene graph.
     */
    public TypedIOPort sceneGraphOut;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Consume inputs from any input ports and apply transformation
     *  according to the state of this actor.
     *  @exception IllegalActionException If the value of some parameters
     *   can't be obtained.
     */
    @Override
    public void fire() throws IllegalActionException {
        //  all state changes must be done in postfire()
        super.fire();
        _applyTransform(_figure);
    }

    /** Setup the transform object.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        CompositeFigure compositeFigure = new CompositeFigure();
        _figure = compositeFigure;
        _applyInitialTransform(_figure);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Return the value of the <i>accumulate</i> parameter.
     *  @return the accumulation mode.
     *  @exception IllegalActionException If the value of some parameters can't
     *   be obtained
     */
    protected boolean _isAccumulating() throws IllegalActionException {
        return ((BooleanToken) accumulate.getToken()).booleanValue();
    }

    /** Setup the scene graph connections of this actor.
     *
     *  @exception IllegalActionException Always thrown for this base class.
     */
    @Override
    protected void _makeSceneGraphConnection() throws IllegalActionException {
        int width = sceneGraphIn.getWidth();

        for (int i = 0; i < width; i++) {
            if (sceneGraphIn.hasToken(i)) {
                Scene2DToken figureToken = (Scene2DToken) sceneGraphIn.get(i);
                Figure figure = figureToken.getFigure();
                _figure.add(figure);
            }
        }

        sceneGraphOut.send(0, new Scene2DToken(_figure));
    }

    /** Set the initial transform of the given figure.  This method is
     *  invoked by this base class during the initialize() method.
     *  Derived classes should implement it to provide class-specific
     *  behavior.
     *  @param figure The figure to transform.
     *  @exception IllegalActionException If the value of some
     *  parameters can't be obtained.
     */
    abstract protected void _applyInitialTransform(Figure figure)
            throws IllegalActionException;

    /** Consume input tokens, and transform the given figure according
     *  to the current state of the transform.  This method is invoked
     *  by this base classes during the fire() method.  Derived classes
     *  should implement it to provide class-specific behavior.
     *  @param figure The figure to transform.
     *  @exception IllegalActionException If the value of some
     *  parameters can't be obtained.
     */
    abstract protected void _applyTransform(Figure figure)
            throws IllegalActionException;

    private CompositeFigure _figure;
}
