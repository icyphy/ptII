/* An attribute that represents a location of a node in a schematic.

 Copyright (c) 1998-2002 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.moml;

import ptolemy.kernel.util.*;

//////////////////////////////////////////////////////////////////////////
//// Location
/**
This attribute represents a location of a node in a schematic.
By default, an instance of this class is not visible in a user interface.
This is indicated to the user interface by returning NONE to the
getVisibility() method.
<p>
This attribute is an empty subclass of ptolemy.kernel.util.Location,
here for backward compatibility only.

@deprecated Use ptolemy.kernel.util.Location.
@author Edward A. Lee
@version $Id$
*/
public class Location extends ptolemy.kernel.util.Location {

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public Location(Workspace workspace) {
        super(workspace);
    }

    /** Construct an attribute with the given name and position.
     *  @param container The container.
     *  @param name The name of the vertex.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public Location(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }
}
