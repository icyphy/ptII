/* Butterfly demo

 Copyright (c) 1999-2000 The Regents of the University of California.
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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.domains.sdf.demo.Butterfly;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.lib.AddSubtract;
import ptolemy.actor.lib.Expression;
import ptolemy.actor.lib.MultiplyDivide;
import ptolemy.actor.lib.Ramp;
import ptolemy.actor.lib.Scale;
import ptolemy.actor.lib.TrigFunction;
import ptolemy.actor.lib.conversions.PolarToRectangular;
import ptolemy.actor.lib.gui.XYPlotter;
import ptolemy.data.DoubleToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.sdf.kernel.SDFDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;
import ptolemy.plot.PlotFrame;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// Butterfly
/**
This class implements the Butterfly demo that is also present in Ptolemy
Classic.
Most models are written using MoML.  This demo is a example of
how to write a demo using Java.

@author Christopher Hylands
@version $Id$
*/
public class Butterfly extends TypedCompositeActor {

    /** Create the Butterfly model
     */
    public Butterfly(Workspace workspace)
	throws IllegalActionException, NameDuplicationException {
	super(workspace);

	setDirector(new SDFDirector(this, "director"));

	Scale scale1 = new Scale(this,"scale1");
	scale1.factor.setToken(new DoubleToken(4.0));
	Scale scale2 = new Scale(this,"scale2");
	scale2.factor.setToken(new DoubleToken(1.0/12.0));
	Scale scale3 = new Scale(this,"scale3");
	scale3.factor.setToken(new DoubleToken(-2.0));

	AddSubtract add1 = new AddSubtract(this,"add1");
	MultiplyDivide multiplyDivide1 =
	    new MultiplyDivide(this,"multiplyDivide1");
	MultiplyDivide multiplyDivide2 =
	    new MultiplyDivide(this,"multiplyDivide2");
	MultiplyDivide multiplyDivide3 =
	    new MultiplyDivide(this,"multiplyDivide3");

	Ramp ramp = new Ramp(this, "ramp");
	ramp.step.setToken(new DoubleToken(Math.PI/100.0));

	PolarToRectangular polarToRect1 =
	    new PolarToRectangular(this, "polarToRect1");

	TrigFunction sin1 = new TrigFunction(this, "sin1");
	sin1.function.setExpression("sin");

	TrigFunction cos1 = new TrigFunction(this, "cos1");
	cos1.function.setExpression("cos");

	// Here, we collapse two actors into one expression actor.
	Expression cos2 = new Expression(this, "cos2");
	TypedIOPort cos2Input = new TypedIOPort(cos2, "cos2Input",
                true, false);
	cos2.expression.setExpression("exp(cos(cos2Input))");
	cos2.output.setTypeEquals(BaseType.DOUBLE);

	XYPlotter xyPlotter = new XYPlotter(this, "xyPlotter");
	xyPlotter.plot = new Plot();
	PlotFrame frame = new PlotFrame(getFullName(), xyPlotter.plot);

        // Make the plot transparent so that the background shows through.
	xyPlotter.plot.setOpaque(false);
        xyPlotter.plot.setGrid(false);
	xyPlotter.plot.setXRange(-3, 4);
	xyPlotter.plot.setYRange(-4, 4);

	this.connect(scale2.output, sin1.input);
	this.connect(scale1.output, cos1.input);
	this.connect(cos1.output, scale3.input);

	TypedIORelation node4 = (TypedIORelation) newRelation("node4");

	multiplyDivide1.output.link(node4);
	multiplyDivide2.multiply.link(node4);
	multiplyDivide2.multiply.link(node4);
	this.connect(multiplyDivide2.output, multiplyDivide3.multiply);

	TypedIORelation node6 = (TypedIORelation) newRelation("node6");
	sin1.output.link(node6);
	multiplyDivide1.multiply.link(node6);
	multiplyDivide1.multiply.link(node6);
	multiplyDivide3.multiply.link(node6);

	connect(scale3.output, add1.plus);
	connect(multiplyDivide3.output, add1.plus);
	connect(cos2.output, add1.plus);

	connect(add1.output, polarToRect1.magnitude);

	TypedIORelation node9 = (TypedIORelation) newRelation("node9");
	ramp.output.link(node9);
	scale1.input.link(node9);
	scale2.input.link(node9);
	polarToRect1.angle.link(node9);
	cos2Input.link(node9);

	connect(polarToRect1.x, xyPlotter.inputX);
	connect(polarToRect1.y, xyPlotter.inputY);
	
	// Export a MoML version of this model to standard output.
	// System.out.println(momlFileWriter.write(exportMoML());
    }
}
