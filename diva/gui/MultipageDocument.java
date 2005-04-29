/*
  Copyright (c) 1998-2005 The Regents of the University of California
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
  PROVIDED HEREUNDER IS ON AN  BASIS, AND THE UNIVERSITY OF
  CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
  ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
*/
package diva.gui;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URL;

import diva.util.ModelParser;
import diva.util.ModelWriter;


/**
 * A document that contains a linear sequence of Pages.
 * This class is useful for documents which their data into logical
 * pages. Generally this class is most useful for partitioned documents
 * where all the partitions are stored together.  For partitions that are
 * stored separately, it is probably easiest to just use separate documents.
 * Note that a page can contain any kind of data, and the
 * interpretation and graphical representation of a list of page is
 * up to the concrete document class and the corresponding
 * application.  Other than containing a sequence of pages, this
 * class is used the same as AbstractDocument and provides the same abstract
 * methods.
 *
 * @author John Reekie
 * @version $Id$
 */
public abstract class MultipageDocument extends AbstractDocument {
    /** The support object for pages.
     */
    private MultipageModel _model = new MultipageModel();

    /**
     * The writer that can write this document to a stream during a
     * save operation.
     */
    MultipageWriter _writer;

    /**
     * The parser that reads in a document and parses it into a
     * Document structure.  In this case, it reads a sketch document
     * and outputs a SketchDocument object.
     */
    MultipageParser _parser;

    /**
     * This writer is used by the document writer to write out each
     * model in a page.
     */
    ModelWriter _modelWriter;

    /**
     * This parser is used by the document parser to read in each
     * model in a page.
     */
    ModelParser _modelParser;

    /**
     * The title associated with this document.
     */
    String _title = "Untitled";

    /** Construct a multipage document that is owned by the given application
     */
    public MultipageDocument(String title, Application a, ModelParser parser,
            ModelWriter writer) {
        super(a);
        _title = title;
        _modelWriter = writer;
        _modelParser = parser;
        _writer = new MultipageWriter(writer);
        _parser = new MultipageParser(parser);
    }

    /** Close the document. This method doesn't do anything, as
     * graph data doesn't change.
     */
    public void close() throws Exception {
        System.out.println("Close " + _title);

        // Do nothing
    }

    /** Return the title of this documen
     */
    public String getTitle() {
        return _title;
    }

    /** Return the model associated with this document.
     */
    public MultipageModel getMultipageModel() {
        return _model;
    }

    /** Open the document from its current file.  If successful, add a
     * new Page to the document containing the model parsed from the
     * current file.
     *
     * @exception Exception  If there is no file, or if the I/O operation failed.
     */
    public void open() throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException("MultipageDocument " + getTitle()
                    + " has no current file");
        }

        String filename = getFile().getName();
        System.out.println("Parsing " + filename);
        _parser.parse(new FileReader(filename), getMultipageModel());
    }

    /** Save the document to the current file.
     *
     * @exception Exception  If there is no file, or if the I/O operation failed.
     */
    public void save() throws Exception {
        if (getFile() == null) {
            throw new IllegalStateException("MultipageDocument " + getTitle()
                    + " has no current file");
        }

        saveAs(getFile());
    }

    /** Save the document to the given file. Do not change the file
     * attribute to the new File object.
     *
     * @exception Exception  If the I/O operation failed.
     */
    public void saveAs(File file) throws Exception {
        String filename = file.getName();
        _title = filename;

        FileWriter out = new FileWriter(file);
        _writer.write(getMultipageModel(), out);
        out.flush();
        out.close();
    }

    /** Throw an exception, as save to URLs is not supported.
     *
     * @exception Exception Always
     */
    public void saveAs(URL url) {
        throw new UnsupportedOperationException("SketchDocument " + getTitle()
                + ": save to URL not supported");
    }
}
