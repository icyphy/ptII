/* The graph document for the Ptolemy II graph editor.

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

import ptolemy.schematic.util.*;

import diva.graph.model.GraphModel;
import diva.graph.toolbox.GraphParser;
import diva.graph.toolbox.GraphWriter;

import diva.gui.AbstractDocument;
import diva.gui.Application;
import diva.gui.BasicSheet;
import diva.gui.Document;
import diva.gui.DocumentFactory;
import diva.gui.Sheet;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.net.URL;

/**
 * A class representing graph-structured documents.
 * This class saves and loads graphs to a schematic ptml file.
 *
 * @author John Reekie, Steve Neuendorffer
 * @version $Id$
 */
public class GraphDocument extends AbstractDocument {
    //DEBUG
    static int _globalCount = 0;
    int _localID = _globalCount++;
    // What the hell... public String getTitle() { return "Graph" + _localID; }
    
    /** Construct a graph document that is owned by the given
     *  application
     */
    public GraphDocument(Application a) {
        super(a);
    }

    /** Close the document. This method doesn't do anything, as
     * graph data doesn't change.
     */
    public void close () throws Exception {
        // Do nothing
    }

    /** Open the document from its current file.  If successful, add a
     * new Sheet to the document containing the model parsed from the
     * current file.
     *
     * @throws Exception  If there is no file, or if the I/O operation failed.
     */
    public void open () throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
                    "GraphDocument " + getTitle() + " has no current file");
        }
        String filename = getFile().getCanonicalPath();
	URL urlbase = new URL("file:");// + System.getProperty("PTII"));
	URL iconlibURL = new URL(urlbase,  filename);
	    
        System.out.println("Parsing " + iconlibURL);
        Schematic schematic = 
	    PTMLObjectFactory.parseSchematic(iconlibURL,
		((GraphEditor)getApplication()).getEntityLibrary());

        Sheet s = new BasicSheet(this, "main", schematic);
        addSheet(s);
    }

    /** Save the document to the current file.
     * 
     * @throws Exception  If there is no file, or if the I/O operation failed.
     */
    public void save () throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
                    "GraphDocument " + getTitle() + " has no current file");
        }
        saveAs(getFile());
    }

    /** Save the document to the given file. Do not change the file
     * attribute to the new File object.
     *
     * @throws Exception  If the I/O operation failed.
     */
    public void saveAs (File file) throws Exception {
        // FIXME
        //        String filename = file.getName();
        //FileOutputStream fout = new FileOutputStream(filename);
        //DataOutputStream out = new DataOutputStream(fout);
        //GraphModel model = (GraphModel) getSheet(0).getModel();
        //new GraphWriter().write(model, out);
    }

    /** Throw an exception, as save to URLs is not supported.
     *
     * @throws Exception Always
     */
    public void saveAs (URL url) {
        throw new UnsupportedOperationException(
                "GraphDocument " + getTitle() + ": save to URL not supported");
    }

    /** Print information about the graph document
     */
    public String toString () {
        return
            getClass().getName() + "["
            + "title = " + getTitle()
            + ", file = " + getFile()
            + ", url = " + getURL()
            + "]\n" + ((Schematic)getCurrentSheet().getModel()).description();
    }
 
    /** GraphDocument.Factory is a factory for graph documents.  We
     * put this in an inner class of GraphDocument because this
     * factory can only produce one kind of document.
     */
    public static class Factory implements DocumentFactory {
        /** Create an empty graph document
         */
        public Document createDocument (Application app) {
            GraphDocument d = new GraphDocument(app);
            d.addSheet(new BasicSheet(d, "New graph", new Schematic()));
            return d;
        }

        /** Throw an exception, as URLs are not supported.
         */
        public Document createDocument (Application app, URL url) {
            throw new UnsupportedOperationException(
                    "Graph documents cannot yet be loaded from a URL");
        }

        /** Create a new graph that contains the given file path.
         */
        public Document createDocument (Application app, File file) {
            GraphDocument d = new GraphDocument(app);
            d.setFile(file);
            return d;
        }
    }
}
