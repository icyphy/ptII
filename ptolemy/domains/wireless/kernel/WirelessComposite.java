/* An aggregation of actors for use in the wireless domain.

 Copyright (c) 2003 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// TypedCompositeActor
/**
This is a composite actor for use in the wireless domain. Unlike
the base class, this composite creates instances of WirelessIOPort
(vs. TypedIOPort) when newPort() is called. Thus, when you add ports
to the composite, they will be instances of WirelessIOPort.

@author Edward A. Lee
@version $Id$
*/
public class WirelessComposite extends TypedCompositeActor {

    /** Construct a composite actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public WirelessComposite(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // By default, when exporting MoML, the class name is whatever
        // the Java class is, which in this case is WirelessComposite.
        // In derived classes, however, we usually do not want to identify
        // the class name as that of the derived class, but rather want
        // to identify it as WirelessComposite.  This way, the MoML
        // that is exported does not depend on the presence of the
        // derived class Java definition. Thus, we force the class name
        // here to be WirelessComposite.
        getMoMLInfo().className =
            "ptolemy.domains.wireless.kernel.WirelessComposite";

        _attachText("_iconDescription", "<svg>\n" +
                "<ellipse cx=\"0\" cy=\"0\" " +
                "rx=\"27\" ry=\"27\" " +
                "style=\"fill:red\"/>\n" +
                "<ellipse cx=\"0\" cy=\"0\" " +
                "rx=\"25\" ry=\"25\" " +
                "style=\"fill:lightgrey\"/>\n" +
                "<rect x=\"-15\" y=\"-10\" width=\"10\" height=\"8\" " +
                "style=\"fill:white\"/>\n" +
                "<rect x=\"-15\" y=\"2\" width=\"10\" height=\"8\" " +
                "style=\"fill:white\"/>\n" +
                "<rect x=\"5\" y=\"-4\" width=\"10\" height=\"8\" " +
                "style=\"fill:white\"/>\n" +
                "<line x1=\"-5\" y1=\"-6\" x2=\"0\" y2=\"-6\"/>" +
                "<line x1=\"-5\" y1=\"6\" x2=\"0\" y2=\"6\"/>" +
                "<line x1=\"0\" y1=\"-6\" x2=\"0\" y2=\"6\"/>" +
                "<line x1=\"0\" y1=\"0\" x2=\"5\" y2=\"0\"/>" +
                "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to create an instance of WirelessIOPort.
     *  @param name The name for the new port.
     *  @return A new TypedIOPort.
     *  @exception NameDuplicationException If this actor already has a
     *   port with the specified name.
     */
    public Port newPort(String name)
            throws NameDuplicationException {
        try {
            workspace().getWriteAccess();
            WirelessIOPort port = new WirelessIOPort(this, name);
            return port;
        } catch (IllegalActionException ex) {
            // This exception should not occur, so we throw a runtime
            // exception.
            throw new InternalErrorException(this, ex, null);
        } finally {
            workspace().doneWriting();
        }
    }
}
