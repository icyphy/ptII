/* The node controller for entities (and icons)

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

package ptolemy.vergil.ptolemy.fsm;

import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.Location;
import ptolemy.vergil.ptolemy.kernel.AttributeController;
import ptolemy.vergil.toolbox.EditorIcon;
import ptolemy.vergil.toolbox.XMLIcon;

import diva.canvas.Figure;
import diva.graph.GraphController;
import diva.graph.NodeRenderer;

//////////////////////////////////////////////////////////////////////////
//// FSMStateController
/**
This class provides interaction with nodes that represent states in an
FSM graph.  It provides a double click binding to edit the parameters
of the state, and a context menu containing a command to edit parameters
("Configure"), a command to rename, and a command to get documentation.

@author Steve Neuendorffer and Edward A. Lee
@version $Id$
*/
public class FSMStateController extends AttributeController {

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     */
    public FSMStateController(GraphController controller) {
        this(controller, FULL);
    }

    /** Create a state controller associated with the specified graph
     *  controller.
     *  @param controller The associated graph controller.
     *  @param access The access level.
     */
    public FSMStateController(GraphController controller, Access access) {
	super(controller, access);
	setNodeRenderer(new StateRenderer());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Render the state as a circle.
     */
    public static class StateRenderer implements NodeRenderer {
	public Figure render(Object n) {
	    Location location = (Location)n;
	    NamedObj object = (NamedObj) location.getContainer();

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
            figure.setToolTipText(object.getName());
	    return figure;
	}
    }
}
