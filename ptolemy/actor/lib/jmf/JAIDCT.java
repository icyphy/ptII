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
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

public class JAIDCT extends Transformer {
    
    public JAIDCT(CompositeEntity container, String name)
	     throws IllegalActionException, NameDuplicationException {
	super(container, name);
	input.setTypeEquals(BaseType.OBJECT);
        output.setTypeEquals(BaseType.OBJECT); 

    }
    
    public void initialize() throws IllegalActionException {
	super.initialize();
	//dctParameters = new ParameterBlock();
    }
    
    public void fire() throws IllegalActionException {
	super.fire();
	dctParameters = new ParameterBlock();
	ObjectToken objectToken = (ObjectToken) input.get(0);
	image = (RenderedOp) objectToken.getValue();
	if (_debugging) {
	    _debug("debugging");
	    if (image == imageOld) {
		_debug("same picture");
	    }
	}
	dctParameters.addSource(image);
	RenderedOp Dct = JAI.create("dct", dctParameters, null);
	//if(_debugging) {
	//    if(DCT == null) {
	//	_debug("it's null");
	//	    }}
	//width = Dct.getWidth();
	//height = Dct.getHeight();
	//dctData = Dct.getData().getPixels(0, 0, width, height, (double[])null);
	//bufferedImage = createBI(colorImage, width, height, pixels); 
	output.send(0, new ObjectToken(Dct));
	RenderedOp imageOld = image;
    }
    private RenderedOp image;
    private RenderedOp imageOld = null;
    private ParameterBlock dctParameters;
    private ParameterBlock otherParameters;
    private double[] dctData;
    private double[] pixels;
    private int width;
    private int height;
    // private BufferedImage bufferedImage;
}
