/* A Vergil Document for Ptolemy models.

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

@ProposedRating Yellow (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.ptolemy;

import ptolemy.vergil.*;
import ptolemy.vergil.graph.*;
import ptolemy.vergil.toolbox.*;
import ptolemy.kernel.*;
import ptolemy.kernel.event.*;
import ptolemy.kernel.util.*;
import ptolemy.actor.*;
import ptolemy.moml.*;

import diva.graph.*;
import diva.graph.layout.*;
import diva.graph.toolbox.*;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.Figure;
import diva.gui.*;
import diva.gui.toolbox.*;
import diva.util.*;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.datatransfer.*;
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.awt.print.PrinterException;
import java.awt.print.PageFormat;
import java.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

/**
 * A Vergil document that contains a Ptolemy model.
 * Ptolemy documents are stored in MoML.
 * <p>
 * In order to create views on itself, this document defers to an attribute
 * that is contained in the toplevel composite entity of the model.  This
 * attribute, a visual notation, creates the view instead.
 *
 * @author Steve Neuendorffer, John Reekie
 * @version $Id$
 */
public class PtolemyDocument extends AbstractDocument
    implements VergilDocument, ClipboardOwner, Printable {

    /** Construct a Ptolemy document that is owned by the given
     *  application.
     */
    public PtolemyDocument(Application a) {
        super(a);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the document. In this class, do nothing.
     */
    public void close() throws Exception {
        // Do nothing
    }

    /** Get the currently selected objects from this document, if any,
     * and place them on the given clipboard.  If the document does not
     * support such an operation, then do nothing. 
     */
    public void copy (Clipboard c) {
	System.out.println("copy");
	JGraph jgraph = getView();
	GraphPane graphPane = jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	SelectionModel model = controller.getSelectionModel();
	Object selection[] = model.getSelectionAsArray();
	PtolemyTransferable transferable = new PtolemyTransferable();
	for(int i = 0; i < selection.length; i++) {
	    if(selection[i] instanceof Figure) {
		Object userObject = ((Figure)selection[i]).getUserObject();
		NamedObj object = (NamedObj)userObject;
		if(object instanceof Icon) {
		    // add the entity, not the icon.
		    NamedObj actual = (NamedObj)object.getContainer();
		    try {
			NamedObj clone = (NamedObj)actual.clone();
			System.out.println("adding " + actual);
			transferable.add(clone);
		    } catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex.getMessage());
		    }		
		} else if(object instanceof Vertex) {
		    // add the relation, not the vertex
		    NamedObj actual = (NamedObj)object.getContainer();
		    try {
			NamedObj clone = (NamedObj)actual.clone();
			System.out.println("adding " + actual);
			transferable.add(clone);
		    } catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex.getMessage());
		    }
		} else if(object instanceof Port) {
		    try {
			NamedObj clone = (NamedObj)object.clone();
			System.out.println("adding " + object);
			transferable.add(clone);
		    } catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex.getMessage());
		    }
		} else if(object instanceof Link) {
		    try {
			NamedObj clone = (NamedObj)object.clone();
			System.out.println("adding " + object);
			transferable.add(clone);
		    } catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex.getMessage());
		    }
		} 
	    }
	}
	c.setContents(transferable, this);
    }
 
    /** Remove the currently selected objects from this document, if any,
     * and place them on the given clipboard.  If the document does not
     * support such an operation, then do nothing.
     */
    public void cut (Clipboard c) {
	System.out.println("cut");
	// First copy everyrhing onto the clipboard.
	copy(c);

	/*
	JGraph jgraph = getView();
	GraphPane graphPane = jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	GraphImpl impl = controller.getGraphImpl();
	SelectionModel model = controller.getSelectionModel();
	Object selection[] = model.getSelectionAsArray();
	*/
    }

    /** Construct a view on this document.  In this class, return an instance
     *  of JGraph that represents the document.   Future calls to getView() 
     *  will return the same view until createView is called again.
     */
    public JComponent createView() {
	//JPanel view = new JPanel();
	//view.setLayout(new OverlayLayout(view));
	
	VisualNotation notation = _getVisualNotation(getModel());
	GraphPane pane = notation.createView(this);
	JGraph jgraph = new JGraph(pane);
	GraphController controller =
	    jgraph.getGraphPane().getGraphController();

	/*	JPanel panel = new JPanel();
	panel.setMinimumSize(new Dimension(200, 100));
	panel.setMaximumSize(new Dimension(200, 100));
	panel.setAlignmentX(1);
	panel.setAlignmentY(1);
	view.add(panel);
	view.add(jgraph);*/

	new EditorDropTarget(jgraph, getApplication());
       
	ActionListener deletionListener = new DeletionListener();
        jgraph.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        jgraph.setRequestFocusEnabled(true);
	jgraph.setAlignmentX(1);
	jgraph.setAlignmentY(1);
	jgraph.setBackground(PtolemyModule.BACKGROUND_COLOR);
	_view = jgraph;
	return jgraph;
    }

    /** Return the toplevel composite entity of the
     * model contained in this document.
     */
    public CompositeEntity getModel() {
	return _model;
    }

    /** Return the current view on this document.  This is updated every time
     *  a new view is created.
     */
    public JGraph getView() {
	return _view;
    }

    /** Do nothing.
     */
    public void lostOwnership(Clipboard clipboard, 
			      Transferable transferable) {
    }

    /** Open the document from its current file.
     *
     * @exception Exception If there is no file, 
     * or if the I/O operation failed.
     */
    public void open() throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
	        "PtolemyDocument " + getTitle() + " has no current file");
        }
	MoMLParser parser = new MoMLParser(new Workspace(), null);
	//, 
	//   ((VergilApplication)getApplication()).classLoadingService.getClassLoader());
	CompositeEntity toplevel =
	    (CompositeEntity) parser.parse(getFile().toURL(),
                    new FileInputStream(getFile()));
	setModel(toplevel);
    }

    /** Clone the objects currently on the clipboard, if any,
     * and place them in the given document.  If the document does not
     * support such an operation, then do nothing.  This method is responsible
     * for copying the data.
     */
    public void paste (Clipboard c) {
	System.out.println("paste");
	Transferable transferable = c.getContents(this);
	JGraph jgraph = getView();
	GraphPane graphPane = jgraph.getGraphPane();
	GraphController controller =
	    (GraphController)graphPane.getGraphController();
	MutableGraphModel model = controller.getGraphModel();
	Workspace workspace = ((NamedObj) model.getRoot()).workspace();
	if(transferable == null) 
	    return;
	try {
	    Iterator objects = (Iterator)
		transferable.getTransferData(PtolemyTransferable.namedObjFlavor);
	    System.out.println("pasting");
	    while(objects.hasNext()) {
		NamedObj object = (NamedObj)objects.next();
		System.out.println("object = " + object.toString());
	        if(object instanceof ComponentEntity) {
		    try {
			ComponentEntity clone = (ComponentEntity)
			    object.clone(workspace);
			System.out.println("clone = " + clone);
			// FIXME the names that this creates are ugly.
			String name = _model.uniqueName(object.getName());
			clone.setName(name);
			clone.setContainer(_model);
			Icon icon = (Icon)clone.getAttribute("_icon");
			model.addNode(icon, model.getRoot());
			controller.drawNode(icon);
		    } catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex.getMessage());
		    } catch (IllegalActionException ex) {
			throw new RuntimeException(ex.getMessage());
		    } catch (NameDuplicationException ex) {
			throw new RuntimeException(ex.getMessage());
		    }
		} /*else if(object instanceof ComponentRelation) {
		    try {
			ComponentRelation clone = 
			    (ComponentRelation)object.clone();
			System.out.println("clone = " + clone);
			String name = _model.uniqueName(object.getName());
			clone.setName(name);
			clone.setContainer(_model);
			Vertex vertex = (V
			Icon icon = (Icon)clone.getAttribute("_icon");
			
			model.addNode(node, controller.getGraph());
			controller.drawNode(node);
		    } catch (CloneNotSupportedException ex) {
			throw new RuntimeException(ex.getMessage());
		    } catch (IllegalActionException ex) {
			throw new RuntimeException(ex.getMessage());
		    } catch (NameDuplicationException ex) {
 			throw new RuntimeException(ex.getMessage());
		    }
		    }*/
	    }
	} catch (UnsupportedFlavorException ex) {
	    System.out.println("no flavor");
	} catch (IOException ex) {
	    System.out.println("io error");
	}
    }

    /** Print the document to a printer, represented by the specified graphics
     *  object.  This method assumes that a view exists of the this document
     *  in the application.
     *  @param graphics The context into which the page is drawn.
     *  @param format The size and orientation of the page being drawn.
     *  @param index The zero based index of the page to be drawn.
     *  @returns PAGE_EXISTS if the page is rendered successfully, or
     *   NO_SUCH_PAGE if pageIndex specifies a non-existent page.
     *  @exception PrinterException If the print job is terminated.
     */
    public int print(Graphics graphics, PageFormat format,
            int index) throws PrinterException {
        JGraph graph = getView();
        if(graph != null) {
            return graph.print(graphics, format, index);
        }
        else return NO_SUCH_PAGE;
    }
        

    /** Save the document to the current file.
     *
     * @exception Exception If there is no file, or if the I/O operation failed.
     */
    public void save() throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
                    "PtolemyDocument " + getTitle() + " has no current file");
        }
        saveAs(getFile());
    }

    /** Save the document to the given file. Do not change the file
     * attribute to the new File object.
     *
     * @exception Exception If the I/O operation failed.
     */
    public void saveAs(File file) throws Exception {
        String filename = file.getName();
        FileWriter writer = new FileWriter(file);
        _model.exportMoML(writer);
        writer.flush();
    }

    /** Save the document to the given URL.
     *
     * @exception UnsupportedOperationException always, as the save to
     * URL operation is not supported.
     */
    public void saveAs(URL url) {
        throw new UnsupportedOperationException(
                "PtolemyDocument " + getTitle() +
                ": save to URL not supported");
    }

    /** Set the model contained in this document.
     */
    public void setModel(CompositeEntity toplevel) {
	_model = toplevel;

	// Create a manager.
	// Attaching these listeners is a nasty business...
	// All Managers are not created equal, since some have
	// listeners attached.
	if(toplevel instanceof Actor) {
	    CompositeActor actor = (CompositeActor)toplevel;
	    Manager manager = actor.getManager();
	    if(manager == null) {
		try {
		    manager =
			new Manager(toplevel.workspace(), "Manager");
		    actor.setManager(manager);
		    manager.addExecutionListener(new PtolemyModule.VergilExecutionListener(getApplication()));
		    manager.addExecutionListener(new StreamExecutionListener());
		    manager.addChangeListener(new IsDirtyListener());
		} catch (IllegalActionException ex) {
		    // This should never happen.
		    throw new InternalErrorException(ex.getMessage());
		}
	    }
	}
    }

    /** Print information about the graph document.
     */
    public String toString() {
        return getClass().getName() + "["
            + "title = " + getTitle()
            + ", file = " + getFile()
            + ", url = " + getURL()
            + "]\n" + _model.exportMoML() + "\n" + _model.description();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /**
     * A class that sets the dirty bit when appropriate
     */
    public class IsDirtyListener implements ChangeListener {
	/** Notify the listener that a change has been successfully executed.
	 *  @param change The change that has been executed.
	 */
	public void changeExecuted(ChangeRequest change) {
	    setDirty(true);
	}
    }

    /**
     * The factory for Ptolemy documents.
     */
    public static class Factory implements VergilDocumentFactory {
        /** Create a document with an empty typed composite actor for
         * its model.
         */
        public Document createDocument(Application app) {
            PtolemyDocument d = new PtolemyDocument(app);
	    TypedCompositeActor toplevel = 
                new TypedCompositeActor(new Workspace());

            d.setModel(toplevel);
            return d;
        }

        /** Create a document and create a new model using the information
         * in the given URL. In this base class, throw and exception, since
         * URL's are not supported.
         * @exception UnsupportedOperationException
         * If the URL's are not supported.
         */
        public Document createDocument(Application app, URL url) {
            throw new UnsupportedOperationException(
                    "Graph documents cannot yet be loaded from a URL");
        }

        /** Create a document and create a new model using the information
         * in the given file.
         */
        public Document createDocument(Application app, File file) {
            PtolemyDocument d = new PtolemyDocument(app);
            d.setFile(file);
            return d;
        }

	/** Return a string which is the name associated with this document
	 * factory.
         * @returns The string "Ptolemy II".
	 */
	public String getName() {
	    return "Ptolemy II";
	}
    }

    /**
     * The factory for Ptolemy FSM documents.
     */
    public static class FSMFactory extends Factory {
        /** Create a document with an empty FSMActor for
         * its model.
         */
        public Document createDocument(Application app) {
            PtolemyDocument d = new PtolemyDocument(app);
            CompositeEntity toplevel;
            try {
                toplevel =
                    new ptolemy.domains.fsm.kernel.FSMActor(new Workspace());
                new ptolemy.vergil.ptolemy.fsm.PtolemyFSMNotation(toplevel, 
                        "notation");
            } catch (Exception e) {
                app.showError("Failed to create new model", e);
                return null;
            }
            d.setModel(toplevel);
            return d;
        }

	/** Return a string which is the name associated with this document
	 * factory.
         * @returns The string "Ptolemy II FSM".
	 */
	public String getName() {
	    return "Ptolemy II FSM";
	}
    }
       
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The document's model.
    private CompositeEntity _model;
    
    // The document's view.
    private JGraph _view;

    // Return a visual notation that can create a view on this document.
    // In this class, we search the toplevel entity in the model for a
    // Ptolemy notation attribute and return the first one found.
    //
    private VisualNotation _getVisualNotation(CompositeEntity model) {
	List notationList = model.attributeList(VisualNotation.class);
	Iterator notations = notationList.iterator();
	VisualNotation notation = null;
        if(notations.hasNext()) {
	    notation = (VisualNotation) notations.next();
	} else {
	    notation = new PtolemyNotation();
	}
	return notation;
    }
}
