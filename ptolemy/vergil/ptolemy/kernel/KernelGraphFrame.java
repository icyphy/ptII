/* A simple graph view for Ptolemy models

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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy.kernel;

import ptolemy.vergil.ptolemy.GraphFrame;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.gui.MessageHandler;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.*;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.URLAttribute;
import ptolemy.moml.Vertex;
import ptolemy.vergil.ptolemy.EditorDropTarget;
import ptolemy.vergil.icon.IconEditor;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyListCellRenderer;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import ptolemy.vergil.toolbox.XMLIcon;
import ptolemy.vergil.form.FormFrameFactory;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.ActionInteractor;

import diva.gui.ApplicationContext;
import diva.gui.Document;
import diva.gui.toolbox.FocusMouseListener;
import diva.gui.toolbox.JContextMenu;
import diva.gui.toolbox.JPanner;

import diva.graph.JGraph;

import diva.graph.GraphController;
import diva.graph.GraphModel;
import diva.graph.GraphPane;
import diva.graph.GraphUtilities;
import diva.graph.MutableGraphModel;
import diva.graph.NodeInteractor;
import diva.graph.basic.BasicLayoutTarget;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.LayoutTarget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.geom.Point2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;

import java.io.IOException;
import java.io.StringWriter;
import java.io.File;

import java.net.URL;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.KeyStroke;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingUtilities;

//////////////////////////////////////////////////////////////////////////
//// KernelGraphFrame
/**
A simple graph view for ptolemy models.  This represents a level of the
hierarchy of a ptolemy model as a diva graph.  Cut, copy and paste operations
are supported using MoML and the graph itself is created using a visual
notation as a a factory

@author  Steve Neuendorffer
@version $Id$
*/
public class KernelGraphFrame extends GraphFrame {

    public KernelGraphFrame(CompositeEntity entity, Tableau tableau) {
	super(entity, tableau);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void _addDoubleClickInteractor(NodeInteractor interactor,
            Interactor doubleClickInteractor) {
        interactor.addInteractor(doubleClickInteractor);
        // FIXME this is a horrible dance so that the
        // doubleclickinteractor gets the events before the drag interactor.
        interactor.setDragInteractor(
                interactor.getDragInteractor());
    }

    /** Create the menus that are used by this frame.
     */
    protected void _addMenus() {
	super._addMenus();
	diva.gui.GUIUtilities.addMenuItem(_graphMenu, _newPortAction);
       	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _newPortAction);

	diva.gui.GUIUtilities.addMenuItem(_graphMenu, _newRelationAction);
	diva.gui.GUIUtilities.addToolBarButton(_toolbar, _newRelationAction);
    }

    /** Create a new graph pane.
     */
    protected GraphPane _createGraphPane() {
	// create the graph editor
	// These two things control the view of a ptolemy model.
	_controller = new EditorGraphController();
	final PtolemyGraphModel graphModel = new PtolemyGraphModel(getModel());

	GraphPane pane = new GraphPane(_controller, graphModel);
	_newPortAction = _controller.getNewPortAction();
	_newRelationAction = _controller.getNewRelationAction();

        // 'Edit Icon' pop up menu not shipped with PtII1.0.
        // See also ptolemy/vergil/ptolemy/GraphFrame.java
	//_editIconAction = new EditIconAction();
	_lookInsideAction = new LookInsideAction();
	_getDocumentationAction = new GetDocumentationAction();
     
        // Double click to edit parameters
        Action action = new AbstractAction("Edit Parameters") {
	    public void actionPerformed(ActionEvent e) {
                LayerEvent event = (LayerEvent)e.getSource();
                Figure figure = event.getFigureSource();
                Object object = figure.getUserObject();
                NamedObj target = 
                (NamedObj)graphModel.getSemanticObject(object);
                // Create a dialog for configuring the object.
                new EditParametersDialog(null, target);
	    }
	};
        ActionInteractor doubleClickInteractor = new ActionInteractor(action);
        doubleClickInteractor.setConsuming(false);
        doubleClickInteractor.setMouseFilter(new MouseFilter(1, 0, 0, 2));

      	_controller.getAttributeController().setMenuFactory(
                new AttributeContextMenuFactory(_controller));
        _addDoubleClickInteractor((NodeInteractor)
                _controller.getAttributeController().getNodeInteractor(),
                doubleClickInteractor);
        
	_controller.getEntityController().setMenuFactory(
                new EntityContextMenuFactory(_controller));        
        _addDoubleClickInteractor((NodeInteractor)
                _controller.getEntityController().getNodeInteractor(),
                doubleClickInteractor);
	
 	_controller.getEntityPortController().setMenuFactory(
                new PortContextMenuFactory(_controller));
        // FIXME: entity ports don't use a NodeInteractor.
        /*_addDoubleClickInteractor((NodeInteractor)
               _controller.getEntityPortController().getNodeInteractor(),
                doubleClickInteractor);
        */
  	_controller.getPortController().setMenuFactory(
                new PortContextMenuFactory(_controller));
        _addDoubleClickInteractor((NodeInteractor)
               _controller.getPortController().getNodeInteractor(),
                doubleClickInteractor);

  	_controller.getRelationController().setMenuFactory(
                new RelationContextMenuFactory(_controller));
        _addDoubleClickInteractor((NodeInteractor)
                _controller.getRelationController().getNodeInteractor(),
                doubleClickInteractor);

  	_controller.getLinkController().setMenuFactory(
                new RelationContextMenuFactory(_controller));
	return pane;
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc = getClass().getClassLoader().getResource(
                    "ptolemy/configs/doc/vergilGraphEditorHelp.htm");
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    /**
     * The factory for creating context menus on visible attributes
     */
    private class AttributeContextMenuFactory extends PtolemyMenuFactory {
	public AttributeContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory("Configure"));
	    addMenuItemFactory(new FormFrameFactory());
	    addMenuItemFactory(new RenameDialogFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	    //addMenuItemFactory(new MenuActionFactory(_editIconAction));
	}
    }
 
    /**
     * The factory for creating context menus on entities.
     */
    private class EntityContextMenuFactory extends PtolemyMenuFactory {
	public EntityContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new FormFrameFactory());
	    addMenuItemFactory(new PortDialogFactory());
	    addMenuItemFactory(new RenameDialogFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	    addMenuItemFactory(new MenuActionFactory(_lookInsideAction));
	    //addMenuItemFactory(new MenuActionFactory(_editIconAction));
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
	    addMenuItemFactory(new FormFrameFactory());
	    addMenuItemFactory(new RenameDialogFactory());
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

    // An action to look inside a composite.
    private class LookInsideAction extends FigureAction {
	public LookInsideAction() {
	    super("Look Inside");
	}
	public void actionPerformed(ActionEvent e) {
	    // Figure out what entity.
	    super.actionPerformed(e);
	    NamedObj object = getTarget();
	    if(!(object instanceof CompositeEntity)) {
                // Open the source code, if possible.
                String filename = object.getClass()
                        .getName().replace('.', '/') + ".java";
                try {
                    URL toRead = getClass().getClassLoader()
                           .getResource(filename);
                    if (toRead != null) {
                        getConfiguration().openModel(null,
                               toRead, toRead.toExternalForm());
                    } else {
                        MessageHandler.error("Cannot find inside definition.");
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Cannot find inside definition.", ex);
                }
                return;
            }
	    CompositeEntity entity = (CompositeEntity)object;

            // If the entity defers its MoML definition to another,
            // then open that other.
	    NamedObj deferredTo = entity.getMoMLInfo().deferTo;
	    if(deferredTo != null) {
		entity = (CompositeEntity)deferredTo;
	    }

            // Search the model library for an effigy that already
            // refers to this model.
            PtolemyEffigy effigy = getEffigy(entity);
            if (effigy != null) {

                // Found one.  Display all open tableaux.
                effigy.showTableaux();

            } else {
                try {

                    // There is no pre-existing effigy.  Create one.
                    effigy = new PtolemyEffigy(getTableau().workspace());
                    effigy.setModel(entity);

                    // Look to see whether the model has a URLAttribute.
                    List attributes = entity.attributeList(URLAttribute.class);
                    if (attributes.size() > 0) {
                        // The entity has a URL, which was probably
                        // inserted by MoMLParser.

                        URL url = ((URLAttribute)attributes.get(0)).getURL();

                        // Set the url and identifier of the effigy.
                        effigy.url.setURL(url);
                        effigy.identifier.setExpression(url.toExternalForm());

                        // Put the effigy into the directory
                        ModelDirectory directory = getDirectory();
                        effigy.setName(directory.uniqueName(entity.getName()));
                        effigy.setContainer(directory);

                        // Create a default tableau.
                        getConfiguration().createPrimaryTableau(effigy);

                    } else {

                        // If we get here, then we are looking inside a model
                        // that is defined within the same file as the parent,
                        // probably.  Create a new PtolemyEffigy
                        // and open a tableau for it.

                        // Put the effigy inside the effigy of the parent,
                        // rather than directly into the directory.
                        CompositeEntity parent =
                            (CompositeEntity)entity.getContainer();
                        boolean isContainerSet = false;
                        if (parent != null) {
                            PtolemyEffigy parentEffigy = getEffigy(parent);
                            if (parentEffigy != null) {
                                // OK, we can put it into this other effigy.
                                effigy.setName(parentEffigy.uniqueName(
                                        entity.getName()));
                                effigy.setContainer(parentEffigy);

                                // Set the identifier of the effigy to be that
                                // of the parent with the model name appended.
                                effigy.identifier.setExpression(
                                        parentEffigy.identifier.getExpression()
                                        + "#" + entity.getName());

                                // Set the url of the effigy to that of
                                // the parent.
                                effigy.url.setURL(parentEffigy.url.getURL());

                                // Indicate success.
                                isContainerSet = true;
                            }
                        }
                        // If the above code did not find an effigy to put
                        // the new effigy within, then put it into the directory.
                        if (!isContainerSet) {
                            CompositeEntity directory = getDirectory();
                            effigy.setName(
                                    directory.uniqueName(entity.getName()));
                            effigy.setContainer(directory);
                            effigy.identifier.setExpression(
                                    entity.getFullName());
                        }

                        getConfiguration().createPrimaryTableau(effigy);
                    }
                } catch (Exception ex) {
                    MessageHandler.error("Look inside failed: ", ex);
                }
            }
	}
    }

    /**
     * The factory for creating context menus on relations.
     */
    private class RelationContextMenuFactory
	extends PtolemyMenuFactory {
	public RelationContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new FormFrameFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private EditorGraphController _controller;
    private Action _getDocumentationAction;
    //private Action _editIconAction;
    private Action _lookInsideAction;
    private Action _newPortAction;
    private Action _newRelationAction;
    private JMenu _executeMenu;
}
