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
import ptolemy.actor.*;
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
	    figure = new IconFigure(background);

            LinkedList inputs = new LinkedList();
            LinkedList outputs = new LinkedList();
            LinkedList inouts = new LinkedList();
            int inCount = 0;
            int outCount = 0;
            int inOutCount = 0;
                                                              
            Iterator nodes = ((CompositeNode) n).nodes();
	    while(nodes.hasNext()) {
		Node portNode = (Node) nodes.next();
                Port port = (Port) portNode.getSemanticObject();
                if(!(port instanceof IOPort)) {
                    inOutCount++;
                    inouts.addLast(portNode);
                } else {
                    IOPort ioport = (IOPort) port;
                    if(ioport.isInput() && ioport.isOutput()) {
                        inOutCount++;
                        inouts.addLast(portNode);
                    } else if(ioport.isInput()) {
                        inCount++;
                        inputs.addLast(portNode);
                    } else if(ioport.isOutput()) {
                        outCount++;
                        outputs.addLast(portNode);
                    }
                }
            }
            System.out.println("incount = "+ inCount);
            System.out.println("outcount = "+ outCount);
            System.out.println("inoutcount = "+ inOutCount);
            int nodeNumber = 0;
        
            nodes = inputs.iterator();            
	    while(nodes.hasNext()) {
                nodeNumber ++;
		Node portNode = (Node) nodes.next();
 		Terminal nodeFigure = new StraightTerminal();                
//render(node);
                //		nodeFigure.setInteractor(getNodeInteractor());
		((IconFigure)figure).addTerminal(nodeFigure, 
                        SwingConstants.EAST, 100.0*nodeNumber/(inCount+1));
                //                CanvasUtilities.translateTo(nodeFigure, nodeX, nodeY);
                nodeFigure.setUserObject(portNode);
		portNode.setVisualObject(nodeFigure);
	    }
    
            nodeNumber = 0;
            nodes = inouts.iterator();            
	    while(nodes.hasNext()) {
                nodeNumber ++;
                Node portNode = (Node) nodes.next();
 		Terminal nodeFigure = new StraightTerminal();                
		((IconFigure)figure).addTerminal(nodeFigure, 
                        SwingConstants.SOUTH, 100.0*nodeNumber/(inOutCount+1));

                nodeFigure.setUserObject(portNode);
		portNode.setVisualObject(nodeFigure);
	    }
   
	} else if(object instanceof Port) {
	    figure = new BasicRectangle(-2, -2, 4, 4, Color.black);
	} else if(object instanceof Vertex) {
	    figure = new BasicRectangle(-4, -4, 8, 8, Color.black);
	} else {
	    figure = new BasicRectangle(-2, -2, 4, 4, Color.red);
	}    

        figure.setUserObject(n);
        n.setVisualObject(figure);

	return figure;
    }

    public void setCompositeScale(double scale) {
	_compositeScale = scale;
    }

    double _compositeScale;
    public static final double DEFAULTCOMPOSITESCALE = 1.0;
}
