/*
 * $Id$
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import diva.util.ModelWriter;

/**
 * MultipageWriter writes out all pages of a multi-page document to a
 * file.  The document writer must be provided with a model writer
 * which is used to write out the app-specific model on a single page.
 *
 * @author Heloise Hse  (hwawen@eecs.berkeley.edu)
 * @author  Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision$
 * @rating  Red
 */
public class MultipageWriter {
    /**
     * Model parser is used to parse the content of a page.
     */
    private ModelWriter _modelWriter;

    /**
     * Create a MultipageWriter with the specified model parser which
     * is used to write the content of a page.
     */
    public MultipageWriter(ModelWriter pageWriter) {
        _modelWriter = pageWriter;
    }

    /**
     * Write the given document to the character stream (out).  Use the
     * given page writer to write out each page in the document.
     */
    public void write(MultipageModel multipage, Writer out) throws IOException {
        writeHeader(out);
        out.write("<"+ MultipageParser.MULTIPAGE_TAG + " " + MultipageParser.TITLE_TAG + "=\"");
        out.write(multipage.getTitle());
        out.write("\">\n");
        int ct=0;
        for(Iterator iter = multipage.pages(); iter.hasNext();){
            Page s = (Page)iter.next();
            Object model = s.getModel();
            String label = s.getTitle();

            out.write("<" + MultipageParser.PAGE_TAG);
            if(label != null){
                out.write(" " + MultipageParser.PAGE_TITLE_TAG + "=\"");
                out.write(label);
                out.write("\"");
            }
            out.write(" " + MultipageParser.PAGE_NUM_TAG + "=\"");
            out.write(String.valueOf(ct));
            out.write("\">\n");
            out.write("<![CDATA[\n");
            _modelWriter.writeModel(model, out);
            out.write("]]>");
            out.write("</" + MultipageParser.PAGE_TAG + ">\n");
            ct++;
        }
        out.write("</" + MultipageParser.MULTIPAGE_TAG + ">\n");
    }

    /**
     * Write out header information.
     */
    private void writeHeader(Writer writer) throws IOException {
        writer.write("<?xml version=\"1.0\" standalone=\"no\"?>\n");
        writer.write("<!DOCTYPE " + MultipageParser.MULTIPAGE_TAG + " PUBLIC \""
                + MultipageParser.PUBLIC_ID + "\"\n\t\""
                + MultipageParser.DTD_URL + "\">\n\n");
    }
}


