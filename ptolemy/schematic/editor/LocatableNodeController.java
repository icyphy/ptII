/* The graph controller for locatable nodes

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

import ptolemy.actor.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.schematic.util.*;
import ptolemy.schematic.xml.*;
import ptolemy.gui.*;
import ptolemy.moml.*;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*; 
import diva.graph.model.*;
import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.event.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;
import java.awt.geom.Rectangle2D;
import diva.util.Filter;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// LocatableNodeController
/**
 * When this NodeController draws nodes, it locates them at the 
 * coordinate given by the locatable interface, if that interface is
 * implemented by the node's semanticObject.
 *
 * @author Steve Neuendorffer 
 * @version $Id$
 */

public class LocatableNodeController extends NodeController {
    public LocatableNodeController(GraphController controller) {
	super(controller);
    }

    /** Draw the node at it's location.
     */    
    public Figure drawNode(Node n) {
        Figure nf = super.drawNode(n);
        locateFigure(n);
        return nf;
    }

    /** Return true if the node is associated with a desired location.
     *  In this base class, return true if the the node's semantic object is
     *  an instance of Locatable.
     */
    public boolean hasLocation(Node n) {
        return (n.getSemanticObject() instanceof Locatable);
    }

    /** Return the desired location of this node.  Throw an exception if the
     *  node does not have a desired location.
     */
    public int[] getLocation(Node n) {
        Object object = n.getSemanticObject();
        if(object instanceof Locatable) {
            return ((Locatable) object).getLocation();         
        } else throw new GraphException("The node " + n + 
                "does not have a desired location");
    }

    /** Set the desired location of this node.  Throw an exception if the
     *  node can not be given a desired location.
     */
    public void setLocation(Node n, int[] location) {
        Object object = n.getSemanticObject();
        if(object instanceof Locatable) {
            ((Locatable)object).setLocation(location);
        } else throw new GraphException("The node " + n + 
                "can not have a desired location");
    }

    /** Move the node's figure to the location specified in the node's 
     *  semantic object, if that object is an instance of Locatable.
     *  If the semantic object is not locatable, then do nothing.
     */
    public void locateFigure(Node n) {
        Figure nf = (Figure)n.getVisualObject();
        if(hasLocation(n)) {
            int[] location = getLocation(n);
            CanvasUtilities.translateTo(nf, location[0], location[1]);
        }
    }           

    /** Set the location of the node's semantic object, 
     *  if that object is an instance of Locatable, to the current location
     *  of the figure.
     *  If the semantic object is not locatable, then do nothing.
     */
    public void locateNode(Node n) {
        Figure nf = (Figure)n.getVisualObject();
        int[] location = new int[2];
        Rectangle2D bounds = nf.getBounds();
        location[0] = (int)bounds.getX();
        location[1] = (int)bounds.getY();
        setLocation(n, location);        
    }
}




