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
import java.net.URL;

/**
 * DocumentFactory is an factory interface that creates Document
 * objects. It is used by the Open action to create a new document in
 * response to user selection of a file or URL.
 *
 * @author John Reekie
 * @version $Id$
 */
public interface DocumentFactory {
    /** Create a new empty document.
     */
    public Document createDocument(Application app);

    /** Create a new document based on the given URL.  Typically, this
     * method will parse the contents of the URL and create a Document
     * object containing the parsed form of those contents.
     */
    public Document createDocument(Application app, URL url);

    /** Create a new document based on the given file path.
     * Typically, this method will parse the contents of the file and
     * create a Document object containing the parsed form of those
     * contents.
     */
    public Document createDocument(Application app, File file);
}
