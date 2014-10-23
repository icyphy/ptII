/*
 Copyright (c) 1998-2014 The Regents of the University of California
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
package diva.util.xml;

import java.io.File;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * A simple program that you can run to illustrate the operation
 * of the files in this package.
 *
 * @author John Reekie
 * @version $Id$
 */
public class XmlDemo {
    /** Construct a new instance of Xml demo.
     * @param argv Command line arguments, currently ignored.
     */
    public static void main(String[] argv) {
        File file = new File("xml1.xml");
        XmlReader reader;
        XmlWriter writer;
        XmlDocument document;

        // Construct a document and parse it
        System.out.println("We are going to parse from " + file + ".");
        System.out.println("The parser will be set to verbose mode\n");

        document = new XmlDocument(file);
        reader = new XmlReader();
        reader.setVerbose(true);

        try {
            reader.parse(document);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int errors = reader.getErrorCount();
        int warnings = reader.getWarningCount();

        System.err.println("Completed: " + errors + " errors, " + warnings
                + " warnings");

        if (errors > 0) {
            System.err.println("Unrecoverable errors in XML. Stop.");
            return;
        }

        // Now print it
        System.out
        .println("\nHaving parsed the document, we will print it out.");
        System.out.println("Here is the DTD:\n");

        writer = new XmlWriter();

        Writer out = new OutputStreamWriter(System.out);

        try {
            writer.writeDTD(document, out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("\nHere is the XML:\n");

        try {
            writer.write(document, out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
