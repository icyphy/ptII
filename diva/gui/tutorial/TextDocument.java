/*
 * $Id$
 *
@Copyright (c) 1998-2004 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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


 */
package diva.gui.tutorial;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.net.URL;

import diva.gui.AbstractDocument;
import diva.gui.Application;
import diva.gui.Document;
import diva.gui.DocumentFactory;

/**
 * A example document that contains plain boring old text 
 * and saves it to ascii files.  Here you can see some sample
 * implementations of the open, save, saveAs, and close methods.
 * You should also notice that this class adds methods for accessing 
 * the contained document's data in an application-useful format.  
 * (In this case, there are just simple getText and setText methods.)
 * The setText properly realized that the document's data has been changed
 * and sets the Dirty flag.  This is used in the application
 * tutorial by the StoragePolicy to prevent a user from closing the document
 * without saving changes.
 * <p>
 * This class also contains a DocumentFactory for documents of this type.
 * The document factory is used by an application to create documents of 
 * this type.  
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision$
 */
public class TextDocument extends AbstractDocument {

    /** The string contained in this document.
     */
    String _text;

    /** Create an text document for the given application containing an 
     * empty string.
     */
    public TextDocument(Application application) {
	super(application);
	_text = "";
    }

    /** Close the document. Do not attempt to save the document first
     * or do any other user-interface things like that. This method
     * can thrown an exception if there is a failure, but it should
     * only do do if there is no way it can recover. Note that actions
     * such as querying the user to save a modified document and so on
     * are the responsibility of the application, not the Document
     * implementation.
     *
     * @exception Exception If the close operation fails.
     */
    public void close () {
	// DO NOTHING.
    }

    /** Return the text contained in this document.
     */
    public String getText() {
	return _text;
    }

    /** Open the document from its current file or URL. Throw an
     * exception if the operation failed.
     *
     * @exception Exception If the close operation fails.
     */
    public void open () throws Exception {
	BufferedReader reader = new BufferedReader(new FileReader(getFile()));
	char[] buffer = new char[100];
	StringBuffer readResult = new StringBuffer();
	int amountRead;
	while ((amountRead = reader.read(buffer, 0, 100)) == 100) {
	    readResult.append(buffer);
	}
	readResult.append(buffer, 0, amountRead);
	_text = readResult.toString();
    }	

    /** Save the document to its current file or URL.  Throw an
     * exception if the operation failed. Reasons for failure might
     * include the fact that the file is not writable, or that the
     * document has a URL but we haven't implemented HTTP-DAV support
     * yet...
     *
     * @exception Exception If the save operation fails.
     */
    public void save () throws Exception {
	saveAs(getFile());
    }

    /** Save the document to the given file.  Throw an exception if
     * the operation failed. Return true if successful, false if
     * not. Do <i>not</i> change the file attribute to the new File
     * object as that is the responsibility of the application, which
     * it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    public void saveAs (File file) throws Exception {
	Writer writer = new BufferedWriter(new FileWriter(file));
	writer.write(_text);
	writer.flush();
    }

    /** Save the document to the given URL.  Throw an exception if the
     * operation failed.  Do <i>not</i> change the URL attribute to
     * the new URL object as that is the responsibility of the
     * application, which it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    public void saveAs (URL url) throws Exception {
	throw new UnsupportedOperationException("Saving as a URL is not" +
						" supported for" +
						" text documents.");
    }

    /**
     * Set the text contained by this document.  If the given text is 
     * different from the previously contained text, then set the dirty flag.
     */
    public void setText(String text) {
	if(_text != text) {
	    setDirty(true);
	    _text = text;
	}
    }

    /** TextDocument.Factory is a factory for Text Documents
     */
    public static class Factory implements DocumentFactory {
        /** Create an empty document.
         */
        public Document createDocument (Application app) {
            TextDocument d = new TextDocument(app);
            return d;
        }

        /** Create a new document that contains data from the given URL.
         */
        public Document createDocument (Application app, URL url) {
            TextDocument d = new TextDocument(app);
            d.setURL(url);
            return d;
        }

        /** Create a new document that contains data from the given file.
         */
        public Document createDocument (Application app, File file) {
            TextDocument d = new TextDocument(app);
            d.setFile(file);
            return d;
        }
    }
}


