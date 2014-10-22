/* For Testing NamedObjs with null names

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.kernel.util.test;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TestNullNamedObj.java

/**
 TestNullNamedObj is like NamedObj, except that null names are permissible.
 This class is used to test some of the assertions in classes such
 as NamedList.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class TestNullNamedObj extends NamedObj {
    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException Not thrown.
     */
    public TestNullNamedObj() throws IllegalActionException {
        super(_defaultWorkspace, "");
    }

    /** Construct an object in the default workspace with an empty string
     *  as its name. The object is added to the list of objects in
     *  the workspace. Increment the version number of the workspace.
     *  @exception IllegalActionException If the name has a period.
     */
    public TestNullNamedObj(String name) throws IllegalActionException {
        super(_defaultWorkspace, name);
    }

    /** Get an object with the specified name in the specified container.
     *  The type of object sought is an instance of the same class as
     *  this object.  In this base class, return null, as there
     *  is no containment mechanism. Derived classes should override this
     *  method to return an object of their same type.
     *  @param relativeName The name relative to the container.
     *  @param container The container expected to contain the object.
     *  @return null.
     *  @exception IllegalActionException If the object exists
     *   and has the wrong class. Not thrown in this base class.
     */
    public NamedObj getContainedObject(NamedObj container, String relativeName)
            throws IllegalActionException {
        return _getContainedObject(container, relativeName);
    }

    /** Get the name. If no name has been given, or null has been given,
     *  then return an empty string, "".
     *  @return The name of the object.
     */
    @Override
    public String getName() {
        return _name;
    }

    /** Set or change the name.  If a null argument is given the
     *  name is set to an empty string.
     *  Increment the version of the workspace.
     *  This method is write-synchronized on the workspace.
     *  @param name The new name.
     *  @exception NameDuplicationException Not thrown in this base
     *   class. May be thrown by derived classes if the container
     *   already contains an object with this name.
     */
    @Override
    public void setName(String name) throws NameDuplicationException {
        //if (name == null) {
        //    name = new String("");
        //}
        try {
            workspace().getWriteAccess();
            _name = name;
        } finally {
            workspace().doneWriting();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    // Instance of a workspace that can be used if no other is specified.
    private static Workspace _defaultWorkspace = new Workspace();

    /** @serial The name. */
    private String _name;
}
