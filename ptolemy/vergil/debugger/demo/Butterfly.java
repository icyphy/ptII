package ptolemy.vergil.debugger.demo;
 
import java.awt.event.*;
import java.awt.Container;
import java.awt.Dimension;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.actor.*;
import ptolemy.actor.lib.*;
import ptolemy.actor.lib.conversions.*;
import ptolemy.actor.gui.*;
import ptolemy.actor.util.*;
import ptolemy.domains.sdf.gui.*;
import ptolemy.domains.sdf.kernel.*;
import ptolemy.domains.sdf.lib.*;

import ptolemy.plot.*;

//////////////////////////////////////////////////////////////////////////
//// Butterfly
/**
 This class implements the Butterfly demo that is also present in Ptolemy
 Classic.

@author Christopher Hylands
@version : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp ButterflyApplet.java.java,v 1.1 1999/05/06 20:14:28 cxh Exp $
*/
public class Butterfly {

    public Ramp ramp;
    public PolarToRectangular polarToRect1;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the demo
     */
    public Butterfly(TypedCompositeActor toplevel, Container panel,
            Dimension size)
            throws Exception {

	Scale scale1 = new Scale(toplevel,"scale1");
	scale1.factor.setToken(new DoubleToken(4.0));
	Scale scale2 = new Scale(toplevel,"scale2");
	scale2.factor.setToken(new DoubleToken(1.0/12.0));
	Scale scale3 = new Scale(toplevel,"scale3");
	scale3.factor.setToken(new DoubleToken(-2.0));

	AddSubtract add1 = new AddSubtract(toplevel,"add1");
	MultiplyDivide mpy1 = new MultiplyDivide(toplevel,"mpy1");
	MultiplyDivide mpy2 = new MultiplyDivide(toplevel,"mpy2");
	MultiplyDivide mpy3 = new MultiplyDivide(toplevel,"mpy3");

        ramp = new Ramp(toplevel, "ramp");
	ramp.step.setToken(new DoubleToken(Math.PI/100.0));

	polarToRect1 =
	    new PolarToRectangular(toplevel, "polarToRect1");

	Expression sin1 = new Expression(toplevel, "sin1");
	TypedIOPort sin1Input = new TypedIOPort(sin1, "sin1Input",
                true, false);
	sin1.expression.setExpression("sin(sin1Input)");

	Expression cos1 = new Expression(toplevel, "cos1");
	TypedIOPort cos1Input = new TypedIOPort(cos1, "cos1Input",
                true, false);
	cos1.expression.setExpression("cos(cos1Input))");

	// Here, we collapse two actors into one expression actor.
	Expression cos2 = new Expression(toplevel, "cos2");
	TypedIOPort cos2Input = new TypedIOPort(cos2, "cos2Input",
                true, false);
	cos2.expression.setExpression("exp(cos(cos2Input))");
	cos2.output.setTypeEquals(BaseType.DOUBLE);

        XYPlotter xyPlotter = new XYPlotter(toplevel, "xyPlotter");
	xyPlotter.place(panel);

        // Make the plot transparent so that the background shows through.
	xyPlotter.plot.setOpaque(false);
        xyPlotter.plot.setGrid(false);
	xyPlotter.plot.setXRange(-3, 4);
	xyPlotter.plot.setYRange(-4, 4);

	toplevel.connect(scale2.output, sin1Input);
	toplevel.connect(scale1.output, cos1Input);
	toplevel.connect(cos1.output, scale3.input);

	TypedIORelation node4 = new TypedIORelation(toplevel, "node4");

	mpy1.output.link(node4);
	mpy2.multiply.link(node4);
	mpy2.multiply.link(node4);
	toplevel.connect(mpy2.output, mpy3.multiply);

	TypedIORelation node6 = new TypedIORelation(toplevel, "node6");
	sin1.output.link(node6);
	mpy1.multiply.link(node6);
	mpy1.multiply.link(node6);
	mpy3.multiply.link(node6);

	toplevel.connect(scale3.output, add1.plus);
	toplevel.connect(mpy3.output, add1.plus);
	toplevel.connect(cos2.output, add1.plus);

	toplevel.connect(add1.output, polarToRect1.magnitudeInput);

	TypedIORelation node9 = new TypedIORelation(toplevel, "node9");
	ramp.output.link(node9);
	scale1.input.link(node9);
	scale2.input.link(node9);
	polarToRect1.angleInput.link(node9);
	cos2Input.link(node9);

	toplevel.connect(polarToRect1.xOutput, xyPlotter.inputX);
	toplevel.connect(polarToRect1.yOutput, xyPlotter.inputY);
    }
}
