/* An action that is associated with a figure.

 Copyright (c) 2000 The Regents of the University of California.
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

package ptolemy.vergil.toolbox;

import java.awt.event.*;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.event.*;
import diva.canvas.*;
import diva.canvas.event.*;
import diva.graph.*;
import diva.gui.toolbox.*;
import ptolemy.kernel.util.*;
import ptolemy.moml.*;

//////////////////////////////////////////////////////////////////////////
//// FigureAction
/** 
An action that is attached to a figure on a named object.  
Such an action is usually fired
in two ways.  The first way is through an ActionInteractor that is attached
to the figure.  The second way is through a context menu that is created
on the figure.  Unfortunately, the source of the event is different in 
these two cases.  This class makes it easy to write an action that is
accessed by either mechanism.

@author Steve Neuendorffer
@version $Id$
*/
public class FigureAction extends AbstractAction {
    
    public FigureAction(String name) {
	super(name);
    }

    public void actionPerformed(ActionEvent e) {
	Object source = e.getSource();
	if(source instanceof LayerEvent) {
	    _sourceType = CANVAS_TYPE;
	    // Action activated using an ActionInteractor.
	    LayerEvent event = (LayerEvent) source;
	    CanvasLayer layer = event.getLayerSource();
	    GraphPane pane = (GraphPane)layer.getCanvasPane();
	    GraphController controller = pane.getGraphController();
	    GraphModel model = controller.getGraphModel();
	   
	    Figure figure = (Figure) event.getFigureSource();
	    // Set the target.
	    if(figure == null) {	
		_target = (NamedObj) model.getRoot();
	    } else {
		Object object = figure.getUserObject();
		_target = (NamedObj) model.getSemanticObject(object);
	    }
	    _x = event.getX();
	    _y = event.getY();
	} else if(source instanceof JMenuItem) {
	    // Action activated using a context menu.
	    JMenuItem item = (JMenuItem) source;
	    if(item.getParent() instanceof JContextMenu) {
		_sourceType = CONTEXTMENU_TYPE;
		JContextMenu menu = (JContextMenu)item.getParent();
		_target = (NamedObj) menu.getTarget();
		_x = item.getX();
		_y = item.getY();
	    } else {
		// Not implicit location.. should there be?
		_sourceType = MENUBAR_TYPE;
	    }
	} else if(source instanceof JButton) {
	    // presumably we are in a toolbar...
	    _sourceType = TOOLBAR_TYPE;
	    _target = null;
	} else {
	    _sourceType = null;
	    _target = null;
	}
    }

    public SourceType getSourceType() {
	return _sourceType;
    }

    public NamedObj getTarget() {
	return _target;
    }

    public int getX() {
	return _x;
    }

    public int getY() {
	return _y;
    }

    public static class SourceType {
	private SourceType(String name) {
	    _name = name;
	}
	
	public String getName() {
	    return _name;
	}
	private String _name;
    }

    /** When the action was fired from a canvas interactor.
     */
    public static SourceType CANVAS_TYPE = new SourceType("canvas");

    /** When the action was fired from a context menu.
     */
    public static SourceType CONTEXTMENU_TYPE = new SourceType("contextmenu");

    /** When the action was fired from a toolbar icon.
     */
    public static SourceType TOOLBAR_TYPE = new SourceType("toolbar");

    /** When the action was fired from a menubar.
     */
    public static SourceType MENUBAR_TYPE = new SourceType("menubar");

    private SourceType _sourceType = null;
    private NamedObj _target = null;
    private int _x = 0;
    private int _y = 0;
}
