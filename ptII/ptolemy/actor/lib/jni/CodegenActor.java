/* An actor containing codegen code modifiable when designing a simulation.

 Copyright (c) 2007-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.jni;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CodegenActor

/**
 A composite actor that can be optionally code generated and then
 invoked via the Java Native Interface (JNI).

 @author Teale Fristoe
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating red (tbf)
 @Pt.AcceptedRating
 */

public class CodegenActor extends EmbeddedCActor {

    /** Create an actor with a name and a container.  The container
     *  argument must not be null, or a NullPointerException will be
     *  thrown.  This actor will use the workspace of the container
     *  for synchronization and version counts.  If the name argument
     *  is null, then the name is set to the empty string.  Increment
     *  the version of the workspace.  This actor will have no local
     *  director initially, and its executive director will be simply
     *  the director of the container.  You should set a director
     *  before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public CodegenActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-30\" y=\"-15\" " + "width=\"62\" height=\"30\" "
                + "style=\"fill:black\"/>\n" + "<text x=\"-19\" y=\"4\""
                + "style=\"font-size:10; fill:white; font-family:SansSerif\">"
                + "Codegen</text>\n" + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Throw an IllegalActionException to indicate that this actor
     *  is used for code generation only.
     *  @exception IllegalActionException If this object is not a subclass
     *  of ptolemy.actor.lib.jni.EmbeddedCActor.
     */
    public void initialize() throws IllegalActionException {
        throw new IllegalActionException(this, getName() + " can not run in "
                + "simulation mode.");
    }
}
