/* A representative of a Standard Output writer

 Copyright (c) 2002-2005 The Regents of the University of California.
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
package ptolemy.hsif.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// StandardOutEffigy

/**
 An effigy for a web browser.

 <p> We invoke the user's web browser for certain files such as pdf files

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class StandardOutEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public StandardOutEffigy(Workspace workspace) {
        super(workspace);

        // Indicate that we cannot save to URL.
        setModifiable(false);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     */
    public StandardOutEffigy(CompositeEntity container, String name)
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
     *  @return A new instance of StandardOutEffigy.
     *  @exception Exception If the URL cannot be read, or if the data
     *   is malformed in some way.
     */
    public static StandardOutEffigy newStandardOutEffigy(
            CompositeEntity container, URL base, URL in) throws Exception {
        // Create a new effigy.
        StandardOutEffigy effigy = new StandardOutEffigy(container, container
                .uniqueName("standardOutEffigy"));

        // We cannot easily communicate with the StandardOut once we launch
        // it, so mark this effigy as unmodifiable
        effigy.setModifiable(false);

        effigy.uri.setURL(in);

        if (in != null) {
            // A URL has been given.  Read it.
            BufferedReader reader = null;

            try {
                InputStream inputStream = null;

                try {
                    inputStream = in.openStream();
                } catch (NullPointerException npe) {
                    throw new IOException("Failed to open '" + in
                            + "', base: '" + base + "' : openStream() threw a "
                            + "NullPointerException");
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                // openStream throws an IOException, not a
                // FileNotFoundException
            } catch (IOException ex) {
                try {
                    // If we are running under WebStart, and try
                    // view source on a .html file that is not in
                    // ptsupport.jar, then we may end up here,
                    // so we look for the file as a resource.
                    URL jarURL = JNLPUtilities.jarURLEntryResource(in
                            .toString());
                    reader = new BufferedReader(new InputStreamReader(jarURL
                            .openStream()));

                    // We were able to open the URL, so update the
                    // original URL so that the title bar accurately
                    // reflects the location of the file.
                    in = jarURL;
                } catch (Exception ex2) {
                    // Looking for the file as a resource did not work,
                    // so we rethrow the original exception.
                    throw ex;
                }
            }

            String line = reader.readLine();

            while (line != null) {
                // Translate newlines to Java form.
                System.out.println(line);
                line = reader.readLine();
            }

            reader.close();

            // Check the URL to see whether it is a file,
            // and if so, whether it is writable.
            if (in.getProtocol().equals("file")) {
                String filename = in.getFile();
                File file = new File(filename);

                if (!file.canWrite()) {
                    effigy.setModifiable(false);
                }
            } else {
                effigy.setModifiable(false);
            }

            effigy.uri.setURL(in);
        } else {
            // No document associated.  Allow modifications.
            effigy.setModifiable(true);
        }

        return effigy;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** A reference to the most recently created factor for this effigy.
     *  This is provided for use by HTMLViewer when following hyperlinks
     *  that specify that they should be opened by a browser.
     */
    public static StandardOutEffigy.Factory staticFactory = null;

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
         *  @return A new instance of StandardOutEffigy.
         *  @exception Exception If the URL cannot be read, or if the data
         *   is malformed in some way.
         */
        public Effigy createEffigy(CompositeEntity container, URL base, URL in)
                throws Exception {
            if (in == null) {
                return null;
            }

            // Always return an effigy
            Effigy effigy = newStandardOutEffigy(container, base, in);
            return effigy;
        }
    }
}
