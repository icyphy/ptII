/* The graph controller for the ptolemy schematic editor

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
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Iterator;
import java.net.URL;
import javax.swing.*;
import javax.swing.event.*;

//////////////////////////////////////////////////////////////////////////
//// BasicContextMenu
/**
 * A Graph Controller for the Ptolemy II schematic editor.  
 * Terminal creation: Ctrl-button 1
 * Edge creation: Ctrl-Button 1 Drag
 * Entity creation: Shift-Button 1
 * Edges can connect to Terminals, but not entities.
 *
 * This is a base class for context menus for most objects in the Ptolemy
 * Editor.  It contains a single action, for getting Parameters.
 * @author Steve Neuendorffer 
 * @version $Id$
 */   
public class BasicContextMenu extends JPopupMenu {
    public BasicContextMenu(NamedObj target) {
	super(target.getFullName());
	
	Action action;
	action = new AbstractAction ("Get Parameters") {
	    public void actionPerformed(ActionEvent e) {
		// Create a dialog and attach the dialog values 
		// to the parameters of the object                    
	    NamedObj object = (NamedObj) getValue("target");
	    System.out.println(object);
	    JFrame frame = new JFrame("Parameters for " + object.getName());
	    JPanel pane = (JPanel) frame.getContentPane();
	    Query query;
	    try {
		// FIXME What if this implements the WidgetConfigurable
		// interface?
		query = new ParameterQuery(object);
	    } catch (IllegalActionException ex) {
		ex.printStackTrace();
		throw new RuntimeException(ex.getMessage());
	    }
	    
	    pane.add(query);
	    frame.setVisible(true);
	    frame.pack();
	    }
	};
	
	action.putValue("target", target);
	action.putValue("tooltip", "Get Parameters");
	JMenuItem item = add(action);
	item.setToolTipText("Get Parameters");
	action.putValue("menuItem", item);
    }
}
