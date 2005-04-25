/* Test for port that has no container with Entity.clone()

Copyright (c) 2004-2005 The Regents of the University of California.
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
package ptolemy.kernel.test;

import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;


/**
 *  Illustrates a problem with port naming and cloning.
 *  <p> In this actor, we set the container of the port to null.

 *  @author Christopher Brooks, based on code from Chad Berkley
 *  @version $Id$
 *  @since Ptolemy II 4.1
 */
public class PortHasNoContainer extends Entity {
    public PortHasNoContainer(Workspace workspace, String name)
            throws IllegalActionException, NameDuplicationException {
        super(workspace, name);

        // This port should be oke
        directoryOrURLPort = new Port(this, "directoryOrURL");

        startTrigger = new Port(this, "startTrigger");
        new Attribute(startTrigger, "_showName");

        startTrigger.setContainer(null);
    }

    /** This port, from actor/lib/io/DirectoryListing.java, is ok because
     * it ends in "Port"
     */
    public Port directoryOrURLPort;

    /**
     * @entity.description This is an optional input port that can be used to
     * help the scheduling of the actor.
     *
     * <P>This port is activated by the hasTrigger parameter. Double-click on
     * the actor to enable. <I>Please enable it <i>ONLY</I> when the actor has
     * no input and it is required for scheduling of the actor.
     */
    public Port startTrigger;
}
