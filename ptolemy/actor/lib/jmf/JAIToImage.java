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
import ptolemy.data.ObjectToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;

import java.awt.Frame;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;

    public class JAIToImage extends Transformer {

	public JAIToImage(CompositeEntity container, String name)
	         throws IllegalActionException, NameDuplicationException {
	    super(container, name);

	    input.setTypeEquals(BaseType.OBJECT);
	    output.setTypeEquals(BaseType.OBJECT);
	}
	
	public void fire() throws IllegalActionException {
	    
	    ObjectToken objectToken = (ObjectToken) input.get(0);
	    RenderedOp renderedOp = (RenderedOp) objectToken.getValue();
	    PlanarImage planarImage = renderedOp.getRendering();
	    BufferedImage bufferedImage = planarImage.getAsBufferedImage();
	    Image image = (Image) bufferedImage;
	    output.send(0, new ObjectToken(image));
	}
    }

