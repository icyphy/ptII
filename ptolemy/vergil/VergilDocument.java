/* The document for Vergil.

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

package ptolemy.vergil;

import ptolemy.kernel.*;
import ptolemy.actor.*;
import ptolemy.moml.*;

import diva.graph.model.*;
import diva.graph.toolbox.GraphParser;
import diva.graph.toolbox.GraphWriter;

import diva.gui.AbstractDocument;
import diva.gui.Application;
import diva.gui.BasicPage;
import diva.gui.Document;
import diva.gui.DocumentFactory;
import diva.gui.Page;

import java.util.ArrayList;
import java.util.Iterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.net.URL;

/**
 * A class representing graph-structured documents.
 * This class saves and loads Ptolemy models from MoML.
 *
 * @author John Reekie, Steve Neuendorffer
 * @version $Id$
 */
public class VergilDocument extends AbstractDocument {
    //DEBUG
    static int _globalCount = 0;
    int _localID = _globalCount++;
    // What the hell... public String getTitle() { return "Graph" + _localID; }
    CompositeEntity _graph;

    /** Construct a graph document that is owned by the given
     *  application
     */
    public VergilDocument(Application a) {
        super(a);
    }

    /** Close the document. This method doesn't do anything, as
     * graph data doesn't change.
     */
    public void close () throws Exception {
        // Do nothing
    }

    public CompositeEntity getGraph() {
	return _graph;
    }

    /** Open the document from its current file.  If successful, add a
     * new Page to the document containing the model parsed from the
     * current file.
     *
     * @throws Exception  If there is no file, or if the I/O operation failed.
     */
    public void open () throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
                    "VergilDocument " + getTitle() + " has no current file");
        }
        String filename = getFile().getAbsolutePath();
	URL schematicURL = new URL("file", null, filename);
	MoMLParser parser = new MoMLParser();
	CompositeEntity toplevel =
	    (CompositeEntity) parser.parse(schematicURL,
					   new FileInputStream(getFile()));
	setGraph(toplevel);
    }

    /** Save the document to the current file.
     * 
     * @throws Exception  If there is no file, or if the I/O operation failed.
     */
    public void save () throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException(
                    "VergilDocument " + getTitle() + " has no current file");
        }
        saveAs(getFile());
    }

    /** Save the document to the given file. Do not change the file
     * attribute to the new File object.
     *
     * @throws Exception  If the I/O operation failed.
     */
    public void saveAs (File file) throws Exception {
        String filename = file.getName();
        FileWriter writer = new FileWriter(file);
        _graph.exportMoML(writer);
        writer.flush();
    }

    /** Throw an exception, as save to URLs is not supported.
     *
     * @throws UnsupportedOperationException always, as the save to
     * URL operation is not supported.
     */
    public void saveAs (URL url) {
        throw new UnsupportedOperationException(
                "VergilDocument " + getTitle() + ": save to URL not supported");
    }

    public void setGraph(CompositeEntity toplevel) {
	_graph = toplevel; 
    }

    /** Print information about the graph document
     */
    public String toString () {
        return
            getClass().getName() + "["
            + "title = " + getTitle()
            + ", file = " + getFile()
            + ", url = " + getURL()
            + "]\n" + _graph.exportMoML();
    }
 
    /** 
     * A factory for vergil documents.
     */
    public static class Factory implements DocumentFactory {
        /** Create an empty graph document
         */
        public Document createDocument (Application app) {
            VergilDocument d = new VergilDocument(app);
            //
            TypedCompositeActor toplevel = new TypedCompositeActor();

            d.setGraph(toplevel);
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
            VergilDocument d = new VergilDocument(app);
            d.setFile(file);
            return d;
        }
    }
}
