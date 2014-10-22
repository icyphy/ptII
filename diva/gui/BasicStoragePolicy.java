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
package diva.gui;

import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;

/**
 * A StoragePolicy that doesn't really check for correct operation.
 * This is a simple policy that simply closes modified documents
 * and so on. It is recommended only for simple examples and tutorials.
 *
 * @author John Reekie
 * @version $Id$
 */
public class BasicStoragePolicy extends AbstractStoragePolicy {
    /** Close the document. Forward the request to the document.  Do
     * nothing if the document is null. Always return true, unless an
     * I/O exception occurs.
     */
    @Override
    public boolean close(Document d) {
        if (d != null) {
            try {
                d.close();
            } catch (Exception e) {
                d.getApplication().showError("close", e);
                return false;
            }
        }

        return true;
    }

    /** Open a new document. Open a file chooser and open the selected
     * file using the application's document factory.  Return the new
     * document if one was created, otherwise null.  If the file
     * exists, then remember it for the next call to getDirectory.
     */
    @Override
    public Document open(Application app) {
        String dir = getDirectory();
        JFileChooser fc = new JFileChooser(dir);
        int result;
        Document doc;

        result = fc.showOpenDialog(app.getAppContext().makeComponent());

        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                doc = this.open(fc.getSelectedFile(), app);
            } catch (Exception e) {
                app.showError("open", e);
                return null;
            }

            return doc;
        } else {
            return null;
        }
    }

    /** Open a file and create a new document.  Return the new
     * document if one was created, otherwise null.  If the file
     * exists, then remember it for the next call to getDirectory.
     */
    @Override
    public Document open(File file, Application app) {
        Document doc;

        try {
            setDirectory(file);
            doc = app.getDocumentFactory().createDocument(app, file);
            doc.open();
        } catch (Exception e) {
            app.showError("open", e);
            return null;
        }

        return doc;
    }

    /** Open a URL and create a new document. Return the new document
     * if one was created, otherwise null.
     */
    @Override
    public Document open(URL url, Application app) {
        Document doc;

        try {
            doc = app.getDocumentFactory().createDocument(app, url);
            doc.open();
        } catch (Exception e) {
            app.showError("open", e);
            return null;
        }

        return doc;
    }

    /** Save the document. Forward the request to the document.  Do
     * nothing if the document is null. Always return true, unless an
     * I/O exception occurred.
     */
    @Override
    public boolean save(Document d) {
        if (d != null) {
            try {
                d.save();
            } catch (Exception e) {
                d.getApplication().showError("saveAs", e);
                return false;
            }
        }

        return true;
    }

    /** Save the document to a user-specified location. Open a file
     * chooser and forward the request to the document. Don't change
     * the document's file object.  Do nothing if the document is
     * null. Return true if successful, otherwise false.
     */
    @Override
    public boolean saveAs(Document d) {
        if (d != null) {
            String dir = getDirectory();
            JFileChooser fc = new JFileChooser(dir);
            int result;
            Application app = d.getApplication();

            result = fc.showOpenDialog(app.getAppContext().makeComponent());

            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    d.saveAs(fc.getSelectedFile());
                    setDirectory(fc.getSelectedFile());
                } catch (Exception e) {
                    d.getApplication().showError("saveAs", e);
                    return false;
                }

                return true;
            } else {
                return false;
            }
        }

        return true;
    }
}
