/* An attribute representing a window associated with the model.

 Copyright (c) 1999 The Regents of the University of California.
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
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.NameDuplicationException;

import java.awt.Window;
import java.io.Writer;
import java.io.IOException;

//////////////////////////////////////////////////////////////////////////
//// WindowAttribute
/**
An attribute representing a window associated with the model.
This class is used to identify top-level user interface windows
associated with a model, such as run control panels and editors.
It is an attribute so that the model itself keeps track of the
windows that is associated with.

@author  Edward A. Lee
@version $Id$
*/
public class WindowAttribute extends Attribute {

    /** Construct an attribute with the given name  and
     *  given container. The container argument must not be null, otherwise
     *  a NullPointerException will be thrown. This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version number of the workspace.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the container does not accept
     *   this attribute.
     *  @exception NameDuplicationException If the name coincides with an 
     *   attribute already in the container.
     */
    public WindowAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
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

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The window that this attribute represents. */
    protected Window window;
}
