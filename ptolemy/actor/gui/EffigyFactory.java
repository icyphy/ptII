/* An object that can create a new Effigy

 Copyright (c) 1997-2014 The Regents of the University of California.
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessControlException;
import java.util.Iterator;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.ClassUtilities;

///////////////////////////////////////////////////////////////////
//// EffigyFactory

/**
 A configuration contains an instance of this class, and uses it to create
 effigies from a URL, or to create blank effigies of a particular kind.
 This base class assumes that it contains other effigy factories.
 Its createEffigy() methods defer to each contained factory in order
 until one is capable of creating an effigy. Subclasses of this class
 will usually be inner classes of an Effigy and will create the Effigy,
 or they might themselves be aggregates of instances of EffigyFactory.

 @author Steve Neuendorffer and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 @see Configuration
 @see Effigy
 */
public class EffigyFactory extends CompositeEntity {
    /** Create a factory in the specified workspace.
     *  @param workspace The workspace.
     */
    public EffigyFactory(Workspace workspace) {
        super(workspace);
    }

    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this entity.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public EffigyFactory(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this effigy factory is capable of creating
     *  an effigy without a URL being specified.  That is, it is capable
     *  of creating a blank effigy with no model data.
     *  In this base class, this method returns true if at least one
     *  contained effigy factory returns true.
     *  @return True if this factory can create a blank effigy.
     */
    public boolean canCreateBlankEffigy() {
        Iterator factories = entityList(EffigyFactory.class).iterator();

        while (factories.hasNext()) {
            EffigyFactory factory = (EffigyFactory) factories.next();

            if (factory.canCreateBlankEffigy()) {
                return true;
            }
        }

        return false;
    }

    /** Check the URL input for a DTD.  Only the first 5 lines are read
     *  from the URL.  Any text that matches <code>&lt;?xml.*?&gt;</code>
     *  is removed before checking.
     *  @param input The DTD to check.
     *  @param dtdStart The start of the DTD, typically "&lt;!DOCTYPE".
     *  @param dtdEndRegExp The optional ending regular expression.  If
     *  this parameter is null, then it is ignored.
     *  @return True if the input starts with dtdStart and, if dtdEndRegExp
     *  is non-null, ends with dtdEndRegExp.
     *  @exception IOException if there is a problem opening or reading
     *  the input.
     */
    public static boolean checkForDTD(URL input, String dtdStart,
            String dtdEndRegExp) throws IOException {
        // This method is a convenience method used to avoid code duplication.
        InputStream stream = null;
        try {
            stream = input.openStream();
        } catch (AccessControlException ex) {
            // Applets will throw this.
            AccessControlException exception = new AccessControlException(
                    "Failed to open \"" + input + "\"");
            exception.initCause(ex);
            throw exception;
        } catch (IOException ex) {

            // If we are running under Web Start, we
            // might have a URL that refers to another
            // jar file.
            URL anotherURL = ClassUtilities.jarURLEntryResource(input
                    .toExternalForm());
            if (anotherURL == null) {
                throw ex;

            }
            stream = anotherURL.openStream();
        }

        boolean foundDTD = false;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            int lineCount = 0;
            while (lineCount < 5) {
                String contents = reader.readLine();
                lineCount++;
                if (contents == null) {
                    break;
                }
                // Change from Ian Brown to handle XSLT where after using XSLT,
                // lines looked like:
                // <?xml version="1.0" encoding="UTF-8"?><!DOCTYPE entity PUBLIC "-//UC Berkeley//DTD MoML 1//EN"

                contents = contents.replaceFirst("<\\?xml.*\\?>", "");
                if (dtdEndRegExp != null) {
                    if (contents.startsWith(dtdStart)
                            && contents.matches(dtdEndRegExp)) {
                        // This file has the DTD for which we are looking.
                        foundDTD = true;
                        break;
                    } else {
                        // Test if DTD public Id is declared on the next line
                        if (contents.startsWith(dtdStart)) {
                            contents += reader.readLine();
                            if (contents.matches(dtdEndRegExp)) {
                                // This file has the DTD for which we are looking.
                                foundDTD = true;
                                break;
                            }
                        }
                    }
                } else if (contents.startsWith(dtdStart)) {
                    // dtdEndRegExp is null so we don't check it
                    foundDTD = true;
                    break;
                }
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return foundDTD;
    }

    /** Create a new blank effigy in the given container. This base class
     *  defers to each contained effigy factory until one returns
     *  an effigy.  If there are no contained effigies, or if none
     *  returns an effigy, then this method returns null. Subclasses will
     *  override this method to create an effigy of an appropriate type.
     *  @param container The container for the effigy.
     *  @return A new effigy.
     *  @exception Exception If the effigy created by one of the contained
     *   factories is incompatible with the specified container, or a name
     *   duplication occurs.
     */
    public Effigy createEffigy(CompositeEntity container) throws Exception {
        return createEffigy(container, null, null);
    }

    /** Create a new effigy in the given container by reading the specified
     *  URL. If the specified URL is null, then create a blank effigy.
     *  The specified base is used to expand any relative file references
     *  within the URL.  This base class defers to each contained effigy
     *  factory until one returns an effigy.  If there are no
     *  contained effigies, or if none
     *  returns an effigy, then this method returns null. Subclasses will
     *  override this method to create an effigy of an appropriate type.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input URL.
     *  @return A new effigy.
     *  @exception Exception If the stream cannot be read, or if the data
     *   is malformed in some way.
     */
    public Effigy createEffigy(CompositeEntity container, URL base, URL in)
            throws Exception {
        Effigy effigy = null;
        Iterator factories = entityList(EffigyFactory.class).iterator();

        while (factories.hasNext() && effigy == null) {
            EffigyFactory factory = (EffigyFactory) factories.next();
            effigy = factory.createEffigy(container, base, in);
        }

        return effigy;
    }

    /** Return the extension on the name of the specified URL.
     *  This is a utility method designed to help derived classes
     *  decide whether the URL matches the particular type of effigy
     *  they can create.  If the URL has no extension, return an
     *  empty string.
     *  @param url A URL.
     *  @return The extension on the URL.
     */
    public static String getExtension(URL url) {
        String filename = url.getFile();
        int dotIndex = filename.lastIndexOf(".");

        if (dotIndex < 0) {
            return "";
        }

        try {
            int slashIndex = filename.lastIndexOf("/");
            if (slashIndex > 0 && slashIndex > dotIndex) {
                // The last / is after the last .
                // for example foo.bar/bif
                return "";
            }
            return filename.substring(dotIndex + 1);
        } catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }
}
