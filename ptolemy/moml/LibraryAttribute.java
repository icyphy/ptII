/* An attribute that references a library to use with the container.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (janneck@eecs.berkeley.edu)
*/

package ptolemy.moml;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// LibraryAttribute
/**
This class is a configurable singleton attribute that associates a
component library with a model.  By convention, it is typically named
"_library".  A visual editor that opens a model containing this attribute
will offer the contents of its library as the component library for editing
the model.  "Singleton" means that if this attribute is placed in a model,
it will replace any previous singleton attribute that has the same name.
"Configurable" means that the contents of the library can be set in
a configure element in MoML, or via the configure() method.
The library can also be set by calling setLibrary(); this will override
any library specified by configure.
<p>
When creating a library to associate with this attribute, the
library should be created in the same workspace as this attribute
(as returned by the workspace() method).  Normally, it will have no
container.  The text specified in the configure element (or a call
to the configure() method) is not parsed until the getLibrary()
method is called.  Thus, the overhead of creating the library is
avoided if the library is not used.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 1.0
*/

public class LibraryAttribute extends ConfigurableAttribute {

    /** Construct a new attribute with no
     *  container and an empty string as its name. Add the attribute to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public LibraryAttribute() {
        super();
    }

    /** Construct a new attribute with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  Add the attribute to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public LibraryAttribute(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the given container and name.
     *  If an attribute already exists with the same name as the one
     *  specified here, that is an instance of class
     *  LibraryAttribute (or a derived class), then that
     *  attribute is removed before this one is inserted in the container.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   attribute with this name, and the class of that container is not
     *   LibraryAttribute.
     */
    public LibraryAttribute(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the library specified by the configure() method or the
     *  setLibrary() method, or null if it has not been set.  If the
     *  configure() method has defined the library, then calling this
     *  method will parse the MoML in specified in the configure() call
     *  to create the library.  The parser for the top-level container
     *  of this attribute will be used, if there is one.  Otherwise,
     *  a new parser will be used.  Note that the library will be
     *  reparsed each time this is called.  This method ensures that
     *  the library that is returned contains an attribute called
     *  "_libraryMarker" so that a user interface recognizes it as
     *  a library.
     *  @return The library, or null if none.
     *  @exception Exception If the library specification
     *   is invalid, for example because the MoML cannot be parsed, or
     *   because it does not define an instance of CompositeEntity.
     */
    public CompositeEntity getLibrary() throws Exception {
        if (_librarySet) return _library;
        MoMLParser parser = ParserAttribute.getParser(this);
        parser.reset();
        NamedObj library = parser.parse(value());
        if (!(library instanceof CompositeEntity)) {
            throw new IllegalActionException(this,
                    "Expected library to be in an instance of CompositeEntity,"
                    + " but it is: "
                    + library.getClass().getName());
        }
        // Ensure that the library is marked as a library.
        Attribute marker = library.getAttribute("_libraryMarker");
        if (marker == null) {
            new SingletonAttribute(library, "_libraryMarker");
        }
        return (CompositeEntity)library;
    }

    /** Specify the library, overriding any library that might have been
     *  or might later be specified by a call to configure().  This method
     *  ensures that the library contains an attribute named "_libraryMarker"
     *  by creating one if it is not there.
     *  @param library The library.
     */
    public void setLibrary(CompositeEntity library) {
        _library = library;
        _librarySet = true;
        if (_library != null) {
            Attribute marker = _library.getAttribute("_libraryMarker");
            if (marker == null) {
                try {
                    new SingletonAttribute(_library, "_libraryMarker");
                } catch (KernelException ex) {
                    throw new InternalErrorException(
                            "Can't add library marker: " + ex);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The library.
    private CompositeEntity _library;

    // Flag indicating that the library was set by setLibrary().
    private boolean _librarySet = false;
}
