/* The node renderer for ptolemy schematic objects.

 Copyright (c) 1998-1999 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.editor;

import diva.graph.*;
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.toolbox.*;
import diva.util.java2d.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.SwingConstants;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;
import ptolemy.schematic.util.*;

/**
 * A factory which creates figures for nodes in the schematic editor.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Id$
 */
public class EditorNodeRenderer implements NodeRenderer {
    /** Create a new editor node renderer with a default composite scale
     */
    public EditorNodeRenderer() {
	this(DEFAULTCOMPOSITESCALE);
    }

    /** Create a new editor node renderer with the given comsposite scale
     */
    public EditorNodeRenderer(double scale) {
	_compositeScale = scale;
    }

    /** return the current composite scale of this renderer
     */
    public double getCompositeScale() {
	return _compositeScale;
    }

    /**
     * Return the rendered visual representation of this node.
     */
    public Figure render(Node n) {
        Figure figure;
	NamedObj object = (NamedObj)n.getSemanticObject();
	
	if(object == null) {
	    figure = new BasicRectangle(-2, -2, 4, 4, Color.red);
	} else if(object instanceof Entity) {
	    BasicCompositeNode node = (BasicCompositeNode) n;
	    Entity entity = (Entity)object;
            EditorIcon icon = (EditorIcon)entity.getAttribute("_icon");
            //           Figure background = new BasicRectangle(-10, -10, 20, 20, Color.red);
            //icon.createFigure();
	    Figure background = icon.createFigure(); 
	    figure = new CompositeFigure(background);
            LabelFigure label = new LabelFigure(entity.getName());
            label.setSize(10);
            ((CompositeFigure)figure).add(label);
	    
	} else if(object instanceof Port) {
	    figure = new BasicRectangle(-2, -2, 4, 4, Color.black);
	} else if(object instanceof Vertex) {
	    figure = new BasicRectangle(-4, -4, 8, 8, Color.black);
	} else {
	    figure = new BasicRectangle(-2, -2, 4, 4, Color.red);
	}
	return figure;
    }

    public void setCompositeScale(double scale) {
	_compositeScale = scale;
    }

    double _compositeScale;
    public static final double DEFAULTCOMPOSITESCALE = 1.0;
}
