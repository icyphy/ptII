/* The node controller for objects with icons.

 Copyright (c) 1998-2001 The Regents of the University of California.
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

package ptolemy.vergil.ptolemy.kernel;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.toolbox.EditorIcon;
import ptolemy.vergil.toolbox.XMLIcon;
import ptolemy.moml.Location;

import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;

//////////////////////////////////////////////////////////////////////////
//// IconController
/**
This class provides interaction with nodes that represent Ptolemy II
objects that are represented on screen as icons, such as attributes
and entities.   It provides a double click binding to edit the parameters
of the node, and a context menu containing a command to edit parameters
("Configure"). This adds to the base class the ability to render an
icon for the object being controlled, where the icon is specified
by a contained attribute of class EditorIcon named "_icon".

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class IconController extends ParameterizedNodeController {

    /** Create a controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public IconController(GraphController controller) {
	super(controller);
	setNodeRenderer(new IconRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                     inner classes                         ////

    /** An icon renderer. */
    public static class IconRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Location location = (Location)n;
	    NamedObj object = (NamedObj) location.getContainer();

	    // FIXME: this code is the same as in PtolemyTreeCellRenderer
	    EditorIcon icon;
            try {
                icon = (EditorIcon)object.getAttribute("_icon");
		if(icon == null) {
		    icon = new XMLIcon(object, "_icon");
		}
	    } catch (KernelException ex) {
		throw new InternalErrorException("could not create icon " +
                        "in " + object + " even " +
                        "though one did not exist");
	    }

	    Figure figure = icon.createFigure();
            figure.setToolTipText(object.getClass().getName());
	    return figure;
	}
    }
}
