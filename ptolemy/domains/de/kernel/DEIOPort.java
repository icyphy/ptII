/* A port for use by actors specialized to the DE domain.

Copyright (c) 1998-2004 The Regents of the University of California.
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

package ptolemy.domains.de.kernel;

import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// DEIOPort
/**
   This port can be used by actors that are specialized to the discrete-event
   (DE) domain. It supports annotations that inform the scheduler about delays
   and about priorities for handling simultaneous inputs. It also provides
   two additional methods, overloaded versions of broadcast() and send().
   The overloaded versions have a second argument for the time delay,
   allowing actors to send output data with a time delay (relative to current
   time).
   <p>
   Actors in the DE domain are not required to use this port. If they use
   the base class, TypedIOPort, then the data they send is sent with zero delay.

   @author Lukito Muliadi, Edward A. Lee, Jie Liu
   @version $Id$
   @since Ptolemy II 0.2
   @Pt.ProposedRating Green (liuj)
   @Pt.AcceptedRating Green (cxh)
*/
public class DEIOPort extends TypedIOPort {

    // FIXME: this class will be removed..

    /** Construct a DEIOPort with no container and no name that is
     *  neither an input nor an output.
     */
    public DEIOPort() {
        super();
    }


    /** Construct a port in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument
     *  is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the port.
     */
    public DEIOPort(Workspace workspace) {
        super(workspace);
    }

    /** Construct a DEIOPort with the specified container and name
     *  that is neither an input nor an output.  The specified container
     *  must implement the Actor interface, or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public DEIOPort(ComponentEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /** Construct a DEIOPort with the specified container and name that is
     *  either an input, an output, or both, depending on the third
     *  and fourth arguments. The specified container must implement
     *  the Actor interface or an exception will be thrown.
     *
     *  @param container The container actor.
     *  @param name The name of the port.
     *  @param isInput True if this is to be an input port.
     *  @param isOutput True if this is to be an output port.
     *  @exception IllegalActionException If the port is not of an acceptable
     *   class for the container, or if the container does not implement the
     *   Actor interface.
     *  @exception NameDuplicationException If the name coincides with
     *   a port already in the container.
     */
    public DEIOPort(ComponentEntity container, String name,
            boolean isInput, boolean isOutput)
            throws IllegalActionException, NameDuplicationException {
        super(container, name, isInput, isOutput);
    }
}
