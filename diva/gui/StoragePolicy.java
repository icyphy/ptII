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
 * StoragePolicy captures the notion of <i>how</i> an application
 * deals with persistent storage. For example, suppose a user selects
 * the Close entry on a menu. The application typically needs to
 * check whether the document has been edited, and if so, it needs
 * to decide what to do about it. Two choices are:
 * <ol>
 * <li> Prompt the user asking whether to save the document first.
 * <li> Save it anyway.
 * <li> Save it but checkpoint the current state of the document so
 * it can be undone later.
 * <li> Sing "Daisy"
 * </ol>
 *
 * Each of these is an example of a storage policy. Because different
 * applications may need different policies, yet some applications
 * will use the same policies as other application, the concept of
 * a storage policy has been reified into this interface and a
 * set of implementing classes.
 *
 * @author John Reekie
 * @version $Id$
 */
public interface StoragePolicy {
    /** Close the document. Depending on the policy, this method will
     * probably check whether the document has been edited and annoy
     * the user with dialog boxes. If the document is null, do
     * nothing. Return true if the document closed, otherwise false.
     */
    public boolean close(Document d);

    /** Open a new document. This method will generally prompt the
     * user for a location to open. If a new Document is created,
     * return it, otherwise return null. (Note that if an existing
     * document is re-opened, null is still returned.)
     */
    public Document open(Application app);

    /** Open a file and create a new document. Depending on the
     * policy, this method may choose to check whether the file has
     * already been opened, and use the existing data, open a
     * read-only view, etc. If it does open the file, it will use the
     * passed DocumentFactory to do so.  If a new Document is created,
     * return it, otherwise return null. (Note that if an existing
     * document is re-opened, null is still returned.)
     */
    public Document open(File file, Application app);

    /** Open a URL and create a new document. Depending on the policy,
     * this method may choose to check whether the URL has already
     * been opened, and use the existing data, etc. If it does open
     * the file, it will use the passed DocumentFactory to do so.  If
     * a new Document is created, return it, otherwise return
     * null. (Note that if an existing document is re-opened, null is
     * still returned.)
     */
    public Document open(URL url, Application app);

    /** Save the document. Most policies will just save the file if it
     * is possible to do so, but different policies may take different
     * action of the file is write-protected (for example).  Do
     * nothing if the document is null. Return true if the file saved,
     * otherwise false.
     */
    public boolean save(Document d);

    /** Save the document to a user-specified location. Depending on
     * the policy, this may prompt the user for the file or URL
     * location, and may or may not change the file or URL of the
     * document to the new location. Do nothing if the document is
     * null.  Return true if the file saved, otherwise false.
     */
    public boolean saveAs(Document d);
}
