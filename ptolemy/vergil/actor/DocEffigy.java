/* A representative of a doc file.

 Copyright (c) 2006-2014 The Regents of the University of California.
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

import java.net.URL;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.basic.DocAttribute;

///////////////////////////////////////////////////////////////////
//// DocEffigy

/**
 An effigy for a doc file.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class DocEffigy extends Effigy {

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If thrown by the superclass.
     *  @exception NameDuplicationException If thrown by the superclass.
     */
    public DocEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the DocAttribute represented by this effigy, if any.
     *  @return The DocAttribute represented by this effigy.
     *  @see #setDocAttribute(DocAttribute)
     */
    public DocAttribute getDocAttribute() {
        return _docAttribute;
    }

    /** Set the DocAttribute represented by this effigy, if any.
     *  @param docAttribute The DocAttribute represented by this effigy.
     *  @see #getDocAttribute()
     */
    public void setDocAttribute(DocAttribute docAttribute) {
        _docAttribute = docAttribute;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The doc attribute represented by this effigy, if any. */
    private DocAttribute _docAttribute;

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
        @Override
        public boolean canCreateBlankEffigy() {
            return false;
        }

        /** Create a new effigy in the given container by reading the
         *  specified URL. If the specified URL is null, or
         *  if the URL does not end with extension ".xml", or
         *  if it does end with ".xml" but the file does not contain
         *  a line that starts with the string
         *  "&lt;!DOCTYPE doc PUBLIC "-//UC Berkeley//DTD DocML"
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
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base,
                URL input) throws Exception {
            if (input != null) {
                String extension = getExtension(input);
                if (extension.equals("xml")) {
                    // Check for DTD designation.
                    if (checkForDTD(input,
                            "<!DOCTYPE doc PUBLIC \"-//UC Berkeley//DTD DocML",
                            null)) {
                        // This is a doc file.
                        DocEffigy effigy = new DocEffigy(container,
                                container.uniqueName("effigy"));
                        effigy.uri.setURL(input);
                        return effigy;
                    }
                }
            }
            return null;
        }
    }
}
