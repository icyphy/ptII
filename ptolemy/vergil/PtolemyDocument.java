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

package ptolemy.vergil;

import ptolemy.vergil.graph.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;

import diva.graph.*;
import diva.graph.model.*;
import diva.graph.toolbox.GraphParser;
import diva.graph.toolbox.GraphWriter;

import diva.canvas.interactor.SelectionModel;
import diva.canvas.Figure;
import diva.graph.*;
import diva.graph.editor.*;
import diva.graph.layout.*;
import diva.graph.model.*;
import diva.graph.toolbox.*;
import diva.gui.*;
import diva.gui.toolbox.*;

import java.awt.event.*;
import java.util.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.URL;

import javax.swing.*;

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
    implements VergilDocument {

    /** Construct a Ptolemy document that is owned by the given
     *  application.
     */
    public PtolemyDocument(Application a) {
        super(a);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Close the document. This method doesn't do anything, as
     * the model doesn't change.
     */
    public void close() throws Exception {
        // Do nothing
    }

    /** Construct a view on this document.
     */
    public JComponent createView() {
	VisualNotation notation = getVisualNotation();
	GraphPane pane = notation.createView(this);

	JGraph jgraph = new JGraph(pane);
	new EditorDropTarget(jgraph);
        GraphController controller =
	    jgraph.getGraphPane().getGraphController();

	ActionListener deletionListener = new DeletionListener();
        jgraph.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        jgraph.setRequestFocusEnabled(true);
	return jgraph;
    }

    /** Return the toplevel composite entity of the
     * model contained in this document.
     */
    public CompositeEntity getModel() {
	return _model;
    }

    /** Return a visual notation that can create a view on this document.
     * In this class, we search the toplevel entity in the model for a
     * Ptolemy notation attribute and return the first one found.
     */
    public VisualNotation getVisualNotation() {
	List notationList = _model.attributeList(VisualNotation.class);
	Iterator notations = notationList.iterator();
	VisualNotation notation = null;
        if(notations.hasNext()) {
	    notation = (VisualNotation) notations.next();
	} else {
	    notation = new PtolemyNotation();
	}
	return notation;
    }

    /** Open the document from its current file.
     *
     * @exception Exception If there is no file, or if the I/O operation failed.
     */
    public void open() throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
                    "PtolemyDocument " + getTitle() + " has no current file");
        }
        String filename = getFile().getAbsolutePath();
	URL schematicURL = new URL("file", null, filename);
	MoMLParser parser = new MoMLParser();
	CompositeEntity toplevel =
	    (CompositeEntity) parser.parse(schematicURL,
                    new FileInputStream(getFile()));
	setModel(toplevel);
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
    }

    /** Print information about the graph document.
     */
    public String toString() {
        return getClass().getName() + "["
            + "title = " + getTitle()
            + ", file = " + getFile()
            + ", url = " + getURL()
            + "]\n" + _model.exportMoML();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     public inner classes                  ////

    /**
     * The factory for Ptolemy documents.
     */
    public static class Factory implements VergilDocumentFactory {
        /** Create a document with an empty typed composite actor for
         * its model.
         */
        public Document createDocument(Application app) {
            PtolemyDocument d = new PtolemyDocument(app);
	    TypedCompositeActor toplevel = new TypedCompositeActor();

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
                    new ptolemy.domains.fsm.kernel.FSMActor();
                new ptolemy.vergil.ptolemy.fsm.PtolemyFSMNotation(toplevel, "notation");
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
}
