/* A hierarchical library of components specified in MoML.

 Copyright (c) 1998-2000 The Regents of the University of California.
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
@AcceptedRating Red (bilung@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import ptolemy.actor.Configurable;
import java.io.Writer;
import java.io.IOException;
import java.net.URL;

//////////////////////////////////////////////////////////////////////////
//// NonpersistentProcessedString
/**
This class provides a simple way to get a long string into an MoML file. 
Instead of using a Settable attribute (like StringAttribute) to set the
value of the attribute, it uses a configure tag.  Since configure tags 
are often given with processing instructions, this class takes care of 
removing the processing instruction and providing it separately (via the 
<i>getInstruction</i> method.

Unlike ProcessedString, this class does not export moml.

@author Steve Neuendorffer
@version $Id$
*/

public class NonpersistentProcessedString extends ProcessedString {

    /** Construct a new attribute with no
     *  container and an empty string as its name. Add the attribute to the
     *  workspace directory.
     *  Increment the version number of the workspace.
     */
    public NonpersistentProcessedString() {
        super();
    }

    /** Construct a a new attribute with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace. 
     *  Add the attribute to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public NonpersistentProcessedString(Workspace workspace) {
	super(workspace);
    }

    /** Construct a library with the given container and name.
     *  @param container The container.
     *  @param name The name of this library.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public NonpersistentProcessedString(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException  {
	super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Write a MoML description of this object, which in this case is
     *  empty.  Nothing is written.
     *  MoML is an XML modeling markup language.
     *  @param output The output stream to write to.
     *  @param depth The depth in the hierarchy, to determine indenting.
     *  @param name The name to use instead of the current name.
     */
    public void exportMoML(Writer output, int depth, String name)
             throws IOException {
    }
}
