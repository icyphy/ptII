/* Load a sequence of binary images from files.

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

package ptolemy.actor.lib.jmf;

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

public class JAIInterpolation extends Transformer {
    
    public JAIInterpolation(CompositeEntity container, String name)
	    throws IllegalActionException, NameDuplicationException {
	super(container, name);
	input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT); 

	xScaleFactor =
	    new Parameter(this, "xScaleFactor", new DoubleToken("2.0F"));
	yScaleFactor =
	    new Parameter(this, "yScaleFactor", new DoubleToken("2.0F"));
	xTranslate = 
	    new Parameter(this, "xTranslate", new DoubleToken("0.0F"));
	yTranslate = 
	    new Parameter(this, "yTranslate", new DoubleToken("0.0F"));
    }
    
    public Parameter xScaleFactor;
    public Parameter yScaleFactor;
    public Parameter xTranslate;
    public Parameter yTranslate;
   
    public void initialize() throws IllegalActionException {
	super.initialize();
	_xScaleFactor = ((DoubleToken)xScaleFactor.getToken()).doubleValue();
	_yScaleFactor = ((DoubleToken)yScaleFactor.getToken()).doubleValue();
	_xTranslate = ((DoubleToken)xTranslate.getToken()).doubleValue();
	_yTranslate = ((DoubleToken)yTranslate.getToken()).doubleValue();

	interp = Interpolation.getInstance(
		      Interpolation.INTERP_BILINEAR);
	parameters = new ParameterBlock();
    }
    
    public void fire() throws IllegalActionException {
	super.fire();
	ObjectToken objectToken = (ObjectToken) input.get(0);
	RenderedOp oldImage = (RenderedOp) objectToken.getValue();
	parameters.addSource(oldImage);
	parameters.add((float)_xScaleFactor);
	parameters.add((float)_yScaleFactor);
	parameters.add((float)_xTranslate);
	parameters.add((float)_yTranslate);
	parameters.add(interp);
	RenderedOp newImage = JAI.create("scale", parameters);
	output.send(0, new ObjectToken(newImage));
    }

    private double _xScaleFactor;
    private double _yScaleFactor;
    private double _xTranslate;
    private double _yTranslate;
    private ParameterBlock parameters;
    private Interpolation interp;
}
