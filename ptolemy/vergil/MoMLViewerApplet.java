/* Basic applet that constructs a Ptolemy II model from a MoML file.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)

*/

package ptolemy.vergil;

import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.*;
import java.io.*;

import com.microstar.xml.XmlException;

import ptolemy.gui.*;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.MoMLApplet;
import ptolemy.actor.gui.Placeable;
import ptolemy.actor.gui.PtolemyApplet;
import ptolemy.actor.gui.HTMLViewer;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.*;
import ptolemy.vergil.ptolemy.*;
import ptolemy.vergil.ptolemy.kernel.*;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyListCellRenderer;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.graph.*;

//////////////////////////////////////////////////////////////////////////
//// MoMLViewerApplet
/**
A moml applet with visualization of the hierarchy.  In addition to the
features of MoMLApplet, this class adds a JModelViewer to the applet which
is a view of the toplevel structure of the model.

@see MoMLApplet
@author  Steve Neuendorffer
@version $Id$
*/
public class MoMLViewerApplet extends MoMLApplet {
    // FIXME: this is a total hack as a placeholder for a general 
    // implementation going through configurations.
    /** Create a MoML parser and parse a file.
     */
    public void init() {
        super.init();
        ViewerGraphController controller = new ViewerGraphController();
        GraphModel model = new PtolemyGraphModel(_toplevel);

	_getDocumentationAction = new GetDocumentationAction();
	controller.getAttributeController().setMenuFactory(
                new ObjectContextMenuFactory(controller));
	controller.getEntityController().setMenuFactory(
                new ObjectContextMenuFactory(controller));
 	controller.getEntityPortController().setMenuFactory(
                new PortContextMenuFactory(controller));
  	controller.getPortController().setMenuFactory(
                new PortContextMenuFactory(controller));
  	controller.getRelationController().setMenuFactory(
                new ObjectContextMenuFactory(controller));
  	controller.getLinkController().setMenuFactory(
                new ObjectContextMenuFactory(controller));
      
        GraphPane pane = new GraphPane(controller, model);
        JGraph modelViewer = new JGraph(pane);
        modelViewer.setMinimumSize(new Dimension(400, 300));
        modelViewer.setPreferredSize(new Dimension(400, 300));
        getContentPane().add(new JScrollPane(modelViewer), 
                BorderLayout.NORTH);
    }

    public class GetDocumentationAction extends FigureAction {
	public GetDocumentationAction() {
	    super("Get Documentation");
	}
	public void actionPerformed(ActionEvent e) {
	    // Create a dialog for configuring the object.
	    super.actionPerformed(e);
            NamedObj target = getTarget();
	    String className = target.getClass().getName();
            String docName = "doc.codeDoc." + className;
            URL docURL = getClass().getClassLoader().getResource(
                    docName.replace('.', '/') + ".html");
            try {
                HTMLViewer viewer = new HTMLViewer();
                viewer.setPage(docURL);
                viewer.pack();
                viewer.show();
            } catch (IOException ex) {
                try {
                    MessageHandler.warning(
                            "Could not find any documentation for\n" + 
                            className);
                } catch (CancelException exception) {}
            }
	}
    };

   /**
     * The factory for creating context menus on visible attributes
     */
    private class ObjectContextMenuFactory extends PtolemyMenuFactory {
	public ObjectContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
        }
    }

    /**
     * The factory for creating context menus on ports.
     */
    public class PortContextMenuFactory extends PtolemyMenuFactory {
	public PortContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new PortDescriptionFactory());
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	}

	public class PortDescriptionFactory extends MenuItemFactory {
	    /**
	     * Add an item to the given context menu that will configure the
	     * parameters on the given target.
	     */
	    public JMenuItem create(JContextMenu menu, NamedObj target) {
		target = _getItemTargetFromMenuTarget(target);
		if(target instanceof IOPort) {
		    IOPort port = (IOPort)target;
		    String string = "";
		    int count = 0;
		    if(port.isInput()) {
			string += "Input";
			count++;
		    }
		    if(port.isOutput()) {
			if(count > 0) {
			    string += ", ";
			}
			string += "Output";
			count++;
		    }
		    if(port.isMultiport()) {
			if(count > 0) {
			    string += ", ";
			}
			string += "Multiport";
			count++;
		    }
		    if(count > 0) {
			return menu.add(new JMenuItem("   " + string));
		    }
		}
		return null;
	    }

	    /**
	     * Get the name of the items that will be created.
	     * This is provided so
	     * that factory can be overriden slightly with the name changed.
	     */
	    protected String _getName() {
		return null;
	    }
	}
    }

    private Action _getDocumentationAction;
}


