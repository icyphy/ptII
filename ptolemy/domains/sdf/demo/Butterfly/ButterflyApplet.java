/* Butterfly demo

 Copyright (c) 1999 The Regents of the University of California.
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

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
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
//// ButterflyApplet
/**

@author Christopher Hylands
@version : ptmkmodel,v 1.7 1999/07/16 01:17:49 cxh Exp ButterflyApplet.java.java,v 1.1 1999/05/06 20:14:28 cxh Exp $
*/
public class ButterflyApplet extends SDFApplet {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Initialize the applet.
     */
    public void init() {
        super.init();
        try {
	    Scale scale1 = new Scale(_toplevel,"scale1");
	    scale1.gain.setToken(new DoubleToken(4.0));
	    Scale scale2 = new Scale(_toplevel,"scale2");
	    scale2.gain.setToken(new DoubleToken(1.0/12.0));
	    Scale scale3 = new Scale(_toplevel,"scale3");
	    scale3.gain.setToken(new DoubleToken(-2.0));
	    
	    AddSubtract add1 = new AddSubtract(_toplevel,"add1");
	    MultiplyDivide mpy1 = new MultiplyDivide(_toplevel,"mpy1");
	    MultiplyDivide mpy2 = new MultiplyDivide(_toplevel,"mpy2");
	    MultiplyDivide mpy3 = new MultiplyDivide(_toplevel,"mpy3");

	    Ramp ramp = new Ramp(_toplevel, "ramp");
	    ramp.step.setToken(new DoubleToken(Math.PI/100.0));

	    PolarToRectangular polarToRect1 =
		new PolarToRectangular(_toplevel, "polarToRect1");
	    
            Expression sin1 = new Expression(_toplevel, "sin1");
            TypedIOPort sin1Input = new TypedIOPort(sin1, "sin1Input",
						    true, false);
	    sin1.expression.setExpression("sin(sin1Input)");

            Expression cos1 = new Expression(_toplevel, "cos1");
            TypedIOPort cos1Input = new TypedIOPort(cos1, "cos1Input",
						    true, false);
	    cos1.expression.setExpression("cos(cos1Input))");

	    // Here, we collapse to expression actors into one.
            Expression cos2 = new Expression(_toplevel, "cos2");
            TypedIOPort cos2Input = new TypedIOPort(cos2, "cos2Input",
						    true, false);
	    cos2.expression.setExpression("exp(cos(cos2Input))");
	    cos2.output.setTypeEquals(DoubleToken.class);

            //Expression exp1 = new Expression(_toplevel, "exp1");
            //TypedIOPort exp1Input = new TypedIOPort(exp1, "exp1Input",
	    //					    true, false);
	    //exp1.expression.setExpression("exp(exp1Input)");

	    XYPlotter xyPlotter = new XYPlotter(_toplevel, "xyPlotter");
            xyPlotter.setPanel(this);
            Dimension size = getSize();
            xyPlotter.plot.setSize(size.width, size.height - 50);
	    xyPlot.plot.clear(false);
	    xyPlot.plot.setXRange(-3, 4);
	    xyPlot.plot.setYRange(-4, 4);

	    _toplevel.connect(scale2.output, sin1Input);
	    _toplevel.connect(scale1.output, cos1Input);
	    //_toplevel.connect(cos2.output, exp1Input);
	    _toplevel.connect(cos1.output, scale3.input);

	    TypedIORelation node4 = new TypedIORelation(_toplevel, "node4");

            mpy1.output.link(node4);
            mpy2.multiply.link(node4);
            mpy2.multiply.link(node4);
	    _toplevel.connect(mpy2.output, mpy3.multiply);

	    TypedIORelation node6 = new TypedIORelation(_toplevel, "node6");
            sin1.output.link(node6);
            mpy1.multiply.link(node6);
            mpy1.multiply.link(node6);
            mpy3.multiply.link(node6);

	    _toplevel.connect(scale3.output, add1.plus);
	    _toplevel.connect(mpy3.output, add1.plus);
	    //_toplevel.connect(exp1.output, add1.plus);
	    _toplevel.connect(cos2.output, add1.plus);

	    _toplevel.connect(add1.output, polarToRect1.magnitudeInput);

	    TypedIORelation node9 = new TypedIORelation(_toplevel, "node9");
            ramp.output.link(node9);
            scale1.input.link(node9);
            scale2.input.link(node9);
	    polarToRect1.angleInput.link(node9);
	    cos2Input.link(node9);

	    _toplevel.connect(polarToRect1.xOutput, xyPlotter.inputX); 
	    _toplevel.connect(polarToRect1.yOutput, xyPlotter.inputY); 

            add(_createRunControls(2));
        } catch (Exception ex) {
            report("Error constructing model.", ex);
        }
    }
}
