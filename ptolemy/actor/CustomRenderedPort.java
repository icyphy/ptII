/* Interface for ports that require custom rendering.

@Copyright (c) 2008-2011 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.actor;

import java.util.List;

import ptolemy.actor.lib.hoc.MirrorPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** This interface is used for ports that require a custom rendering. It
 *  implements a method that returns a list of coordinates for the custom
 *  shape.
 *
 *  @author Patricia Derler
 *  @version $Id$
 *  @since Ptolemy II 8.0
 *  @Pt.ProposedRating Red (derler)
 *  @Pt.AcceptedRating
 */
public abstract class CustomRenderedPort extends MirrorPort {

    /** Create a new CustomRenderedPort with a given container and a name.
     * @param container The container of the port.
     * @param name The name of the port.
     * @throws IllegalActionException If parameters cannot be set.
     * @throws NameDuplicationException If name already exists.
     */
    public CustomRenderedPort(CompositeEntity container, String name) throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // FIXME: the following should use getCoordinatesForShape to
        // generate the correct svg icon.
        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-5\" y=\"-5\" " + "width=\"5\" height=\"5\" "
                + "style=\"fill:black\"/>\n" + "</svg>\n");
    }

    /** Compute and return a list of coordinates for the custom shape.
     *  @return List of coordinates.
     */
    public abstract List<Integer[]> getCoordinatesForShape();

}
