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

package ptolemy.vergil.ptolemy.fsm;

// FIXME: Trim this.
import ptolemy.vergil.ptolemy.GraphFrame;
import ptolemy.vergil.ptolemy.EditorDropTarget;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.gui.MessageHandler;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.gui.EditParametersDialog;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.MoMLApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.RunTableau;
import ptolemy.actor.gui.style.EditableChoiceStyle;
import ptolemy.actor.gui.Tableau;
import ptolemy.moml.Location;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.Vertex;
import ptolemy.vergil.icon.IconEditor;
import ptolemy.vergil.toolbox.EditParametersFactory;
import ptolemy.vergil.toolbox.FigureAction;
import ptolemy.vergil.toolbox.MenuActionFactory;
import ptolemy.vergil.toolbox.MenuItemFactory;
import ptolemy.vergil.toolbox.PtolemyListCellRenderer;
import ptolemy.vergil.toolbox.PtolemyMenuFactory;
import ptolemy.vergil.toolbox.XMLIcon;
import ptolemy.vergil.ptolemy.kernel.RenameDialogFactory;

import diva.canvas.CanvasUtilities;
import diva.canvas.Site;
import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;
import diva.canvas.connector.FixedNormalSite;
import diva.canvas.connector.Terminal;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.interactor.CompositeInteractor;
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
import diva.graph.toolbox.DeletionListener;

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
//// FSMGraphFrame
/**

@author  Steve Neuendorffer
@version $Id$
*/
public class FSMGraphFrame extends GraphFrame {

    public FSMGraphFrame(CompositeEntity entity, Tableau tableau) {
	super(entity, tableau);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create a new graph pane.
     */
    protected GraphPane _createGraphPane() {
	// create the graph editor
	// These two things control the view of a ptolemy model.
	_controller = new FSMGraphController();
        _controller.setConfiguration(getConfiguration());
	final FSMGraphModel graphModel = new FSMGraphModel(getModel());

	GraphPane pane = new GraphPane(_controller, graphModel);

        return pane;
    }

    /** Display more detailed information than given by _about().
     */
    protected void _help() {
        try {
            URL doc = getClass().getClassLoader().getResource(
                    "ptolemy/configs/doc/vergilFsmEditorHelp.htm");
            getConfiguration().openModel(null, doc, doc.toExternalForm());
        } catch (Exception ex) {
            _about();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     private inner classes                 ////

    /**
     * The factory for creating context menus on states.
     */
    private class StateContextMenuFactory extends PtolemyMenuFactory {
	public StateContextMenuFactory(GraphController controller) {
	    super(controller);
	    addMenuItemFactory(new EditParametersFactory());
	    addMenuItemFactory(new RenameDialogFactory());
	    addMenuItemFactory(new MenuActionFactory(_getDocumentationAction));
	    //addMenuItemFactory(new MenuActionFactory(_lookInsideAction));
            /** FIXME: removed temporarily until edit icon works.
	    addMenuItemFactory(new MenuActionFactory(_editIconAction));
            */
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
	    if(!(object instanceof CompositeEntity)) return;
	    CompositeEntity entity = (CompositeEntity)object;
	    NamedObj deferredTo = entity.getMoMLInfo().deferTo;
	    if(deferredTo != null) {
		entity = (CompositeEntity)deferredTo;
	    }

	    // FIXME create a new ptolemy effigy and
	    // a new graphTableau for it.
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private FSMGraphController _controller;
    private Action _getDocumentationAction;
    private Action _editIconAction;
    private Action _lookInsideAction;
    private JMenu _executeMenu;
    private Action _executeSystemAction;

    private JComboBox _directorComboBox;
    private DefaultComboBoxModel _directorModel;
}
