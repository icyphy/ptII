/* Butterfly demo

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.sdf.demo.Butterfly;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.lib.Ramp;
import ptolemy.actor.lib.conversions.PolarToCartesian;
import ptolemy.actor.lib.gui.XYPlotter;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;

//////////////////////////////////////////////////////////////////////////
//// Butterfly

/**
 This class defines a Ptolemy II model that traces an elaborate curve
 called the butterfly curve.
 It was described by T. Fay, <i>American Mathematical Monthly</i>, 96(5),
 May, 1989.  Although users will usually prefer to define models using
 MoML, this class illustrates how to define a model in Java.

 <p>To run this model, use:</p>
 <pre>
java -classpath $PTII ptolemy.actor.gui.CompositeActorApplication -class ptolemy.domains.sdf.demo.Butterfly.Butterfly
 </pre>

 @author Christopher Hylands and Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.4
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (reviewmoderator)
 */
public class Butterfly extends TypedCompositeActor {
    /** Construct a Butterfly with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Butterfly(Workspace workspace) throws IllegalActionException,
            NameDuplicationException {
        super(workspace);
        setName("Butterfly");

        // Create the director, and set the number of iterations to execute.
        SDFDirector director = new SDFDirector(this, "director");
        director.iterations.setExpression("2400");
        setDirector(director);

        // Create the actors, and set their parameters.
        // First, the source, which counts up from 0.0 in steps of pi/100.
        Ramp ramp = new Ramp(this, "Ramp");
        ramp.step.setExpression("PI/100.0");

        // Next, the expression, for which we have to create an input port.
        Expression expression = new Expression(this, "Expression");
        TypedIOPort expInput = new TypedIOPort(expression, "ramp");
        expInput.setInput(true);
        expression.expression.setExpression("-2.0*cos(4.0*ramp) + "
                + "exp(cos(ramp)) + (sin(ramp/12.0) * (sin(ramp/12.0))^4)");

        // Next, a conversion to use the ramp as an angle specifier,
        // and the output of the expression as the vector length.
        PolarToCartesian polarToCartesian = new PolarToCartesian(this,
                "Polar to Cartesian");

        // Finally, the plotter.
        XYPlotter xyPlotter = new XYPlotter(this, "xyPlotter");
        xyPlotter.plot = new Plot();
        xyPlotter.plot.setGrid(false);
        xyPlotter.plot.setXRange(-3, 4);
        xyPlotter.plot.setYRange(-4, 4);

        // Make the connections.
        // The ports are public members of these classes.
        // The first connection is a three way connection, so we have
        // to create a relation and then link to it.
        TypedIORelation node = (TypedIORelation) newRelation("node");
        ramp.output.link(node);
        expInput.link(node);
        polarToCartesian.angle.link(node);

        // The rest of the connections are point-to-point, so we can use
        // the connect() method.
        connect(expression.output, polarToCartesian.magnitude);
        connect(polarToCartesian.x, xyPlotter.inputX);
        connect(polarToCartesian.y, xyPlotter.inputY);
    }
}
