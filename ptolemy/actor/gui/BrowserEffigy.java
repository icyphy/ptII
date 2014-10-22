/* A representative of a web browser

 Copyright (c) 2002-2014 The Regents of the University of California.
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
 */
package ptolemy.actor.gui;

import java.net.URL;
import java.net.URLConnection;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// BrowserEffigy

/**
 An effigy for a web browser.

 <p> We invoke the user's web browser for certain files such as pdf files

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class BrowserEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public BrowserEffigy(Workspace workspace) {
        super(workspace);

        // Indicate that we cannot save to URL.
        setModifiable(false);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public BrowserEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Indicate that we cannot save to URL.
        setModifiable(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new effigy in the given container by reading the specified
     *  URL. If the specified URL is null, then create a blank effigy.
     *  The extension of the URL is not checked, so this will open any file.
     *  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.  This is ignored in this
     *   class.
     *  @param in The input URL.
     *  @return A new instance of BrowserEffigy.
     *  @exception Exception If the URL cannot be read, or if the data
     *   is malformed in some way.
     */
    public static BrowserEffigy newBrowserEffigy(CompositeEntity container,
            URL base, URL in) throws Exception {
        // Create a new effigy.
        BrowserEffigy effigy = new BrowserEffigy(container,
                container.uniqueName("browserEffigy"));

        // We cannot easily communicate with the Browser once we launch
        // it, so mark this effigy as unmodifiable
        effigy.setModifiable(false);

        // The BrowserLauncher will read the URL for us, so no need
        // to read it here.
        effigy.uri.setURL(in);
        return effigy;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A reference to the most recently created factory for this effigy.
     *  This is provided for use by HTMLViewer and Configuration
     *  when following hyperlinks
     *  that specify that they should be opened by a browser.
     */
    public static BrowserEffigy.Factory staticFactory = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new effigies.
     */
    public static class Factory extends EffigyFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            // Record the latest factory for use by HTMLViewer.
            staticFactory = this;
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return true, indicating that this effigy factory is
         *  capable of creating an effigy without a URL being specified.
         *  @return True.
         */
        @Override
        public boolean canCreateBlankEffigy() {
            return false;
        }

        /** Create a new effigy in the given container by reading the specified
         *  URL. If the specified URL is null, then create a blank effigy.
         *  The extension of the URL is not
         *  checked, so this will open any file.  Thus, this factory
         *  should be last on the list of effigy factories in the
         *  configuration.
         *  The new effigy will have a new instance of
         *  DefaultStyledDocument associated with it.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.  This is ignored in this
         *   class.
         *  @param in The input URL.
         *  @return A new instance of BrowserEffigy.
         *  @exception Exception If the URL cannot be read, or if the data
         *   is malformed in some way.
         */
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base, URL in)
                throws Exception {
            if (in == null) {
                return null;
            }

            String extension = getExtension(in);

            // This could be a list, or a user preference
            if (extension.equals("pdf") || extension.startsWith("htm")
                    || extension.startsWith("shtm")) {
                Effigy effigy = newBrowserEffigy(container, base, in);
                return effigy;
            }

            // The extension doesn't match.  Try the content type.
            URLConnection connection = in.openConnection();

            if (connection == null) {
                return null;
            }

            String contentType = connection.getContentType();

            if (contentType == null) {
                return null;
            }

            if (contentType.startsWith("text/html")
                    || contentType.startsWith("text/rtf")
                    || contentType.startsWith("image/")) {
                Effigy effigy = newBrowserEffigy(container, base, in);
                return effigy;
            }

            return null;
        }
    }
}
