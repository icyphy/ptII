/* A representative of a doc file.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.vergil.actor;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DocEffigy

/**
 An effigy for a doc file.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.1
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class DocEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public DocEffigy(Workspace workspace) {
        super(workspace);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     */
    public DocEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

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
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return false, indicating that this effigy factory is not
         *  capable of creating an effigy without a URL being specified.
         *  @return False.
         */
        public boolean canCreateBlankEffigy() {
            return false;
        }

        /** Create a new effigy in the given container by reading the
         *  specified URL. If the specified URL is null, or
         *  if the URL does not end with extension ".xml", or
         *  if it does end with ".xml" but the file does not contain
         *  a line that starts with the string
         *  "<!DOCTYPE doc PUBLIC "-//UC Berkeley//DTD DocML"
         *  within the first five lines, then return null.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.  This is ignored in this
         *   class.
         *  @param input The input URL.
         *  @return A new instance of DocEffigy, or null if the URL
         *   does not have a doc file.
         *  @exception Exception If the URL cannot be read.
         */
        public Effigy createEffigy(CompositeEntity container, URL base,
                URL input) throws Exception {
            if (input != null) {
                String extension = getExtension(input);
                if (extension.equals("xml")) {
                    // Check for DTD designation.
                    String dtd = "<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML";
                    InputStream stream = input.openStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(stream));
                    int lineCount = 0;
                    // FIXME: Have to extract the class name from the file,
                    // and provide that in a public method so that DocTableau can
                    // get it and use to create a DocViewer.
                    // NO NOT RIGHT... The DocViewer needs to be able to be
                    // given a URL directly...
                    while (lineCount < 5) {
                        String contents = reader.readLine();
                        lineCount++;
                        if (contents == null) {
                            reader.close();
                            return null;
                        }
                        if (contents.startsWith(dtd)) {
                            // This is a doc file.
                            reader.close();
                            DocEffigy effigy = new DocEffigy(container, container
                                    .uniqueName("effigy"));
                            effigy.uri.setURL(input);
                            return effigy;
                        }
                    }
                    reader.close();
                }
            }
            return null;
        }
    }
}
